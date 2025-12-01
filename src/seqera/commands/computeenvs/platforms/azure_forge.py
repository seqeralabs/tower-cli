"""
Azure Batch Forge platform implementation for compute environments.

Forge mode: Seqera creates Azure resources automatically.
"""

import sys
from pathlib import Path
from typing import List, Optional

import typer
from typing_extensions import Annotated

from seqera.main import get_client, get_output_format
from seqera.responses.computeenvs import ComputeEnvAdded
from seqera.exceptions import SeqeraError
from seqera.utils.output import output_error


def read_file_content(file_path: Optional[Path]) -> Optional[str]:
    """Read and return the content of a file."""
    if file_path is None:
        return None
    try:
        with open(file_path, 'r') as f:
            return f.read()
    except Exception as e:
        output_error(f"Failed to read file {file_path}: {e}")
        sys.exit(1)


def parse_environment_variables(env_vars: Optional[List[str]]) -> Optional[List[dict]]:
    """Parse environment variables from key=value format."""
    if not env_vars:
        return None

    result = []
    for env in env_vars:
        if '=' not in env:
            output_error(f"Invalid environment variable format: {env}. Expected key=value")
            sys.exit(1)
        key, value = env.split('=', 1)
        result.append({
            "name": key,
            "value": value,
            "head": False,
            "compute": True,
        })
    return result


def add_azure_forge(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Compute environment name"),
    ],
    work_dir: Annotated[
        str,
        typer.Option("--work-dir", help="Work directory"),
    ],
    location: Annotated[
        str,
        typer.Option("-l", "--location", help="The Azure location where the workload will be deployed"),
    ],
    vm_count: Annotated[
        int,
        typer.Option("--vm-count", help="The number of virtual machines in this pool. When autoscaling feature is enabled, this option represents the maximum number of virtual machines to which the pool can grow and automatically scales to zero when unused"),
    ],
    vm_type: Annotated[
        Optional[str],
        typer.Option("--vm-type", help="Specify the virtual machine type used by this pool. It must be a valid Azure Batch VM type [default: Standard_D4_v3]"),
    ] = None,
    no_auto_scale: Annotated[
        bool,
        typer.Option("--no-auto-scale", help="Disable pool autoscaling which automatically adjust the pool size depending the number submitted jobs and scale to zero when the pool is unused"),
    ] = False,
    preserve_resources: Annotated[
        bool,
        typer.Option("--preserve-resources", help="Enable this if you want to preserve the Batch compute pool created by Tower independently from the lifecycle of this compute environment"),
    ] = False,
    registry_credentials: Annotated[
        Optional[List[str]],
        typer.Option("--registry-credentials", help="Container registry credentials name (can be specified multiple times)"),
    ] = None,
    fusion_v2: Annotated[
        bool,
        typer.Option("--fusion-v2", help="With Fusion v2 enabled, Azure blob containers specified in the pipeline work directory and blob containers within the Azure storage account will be accessible in the compute nodes storage (requires Wave containers service)"),
    ] = False,
    wave: Annotated[
        bool,
        typer.Option("--wave", help="Allow access to private container repositories and the provisioning of containers in your Nextflow pipelines via the Wave containers service"),
    ] = False,
    # Advanced options
    jobs_cleanup: Annotated[
        Optional[str],
        typer.Option("--jobs-cleanup", help="Enable the automatic deletion of Batch jobs created by the pipeline execution (ON_SUCCESS, ALWAYS, NEVER)"),
    ] = None,
    token_duration: Annotated[
        Optional[str],
        typer.Option("--token-duration", help="The duration of the shared access signature token created by Nextflow when the 'sasToken' option is not specified [default: 12h]"),
    ] = None,
    # Common platform options
    pre_run: Annotated[
        Optional[Path],
        typer.Option("--pre-run", help="Pre-run script file"),
    ] = None,
    post_run: Annotated[
        Optional[Path],
        typer.Option("--post-run", help="Post-run script file"),
    ] = None,
    environment: Annotated[
        Optional[List[str]],
        typer.Option("--environment", "-e", help="Environment variables (key=value format, can be specified multiple times)"),
    ] = None,
    nextflow_config: Annotated[
        Optional[Path],
        typer.Option("--nextflow-config", help="Nextflow config file"),
    ] = None,
    # Credentials option
    credentials_id: Annotated[
        Optional[str],
        typer.Option("-c", "--credentials", help="Credentials identifier"),
    ] = None,
    # Workspace option
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Add new Azure Batch compute environment with automatic provisioning of compute resources."""
    # Import here to avoid circular dependency
    from seqera.commands.computeenvs import (
        USER_WORKSPACE_NAME,
        handle_compute_env_error,
        output_response,
    )

    try:
        client = get_client()
        output_format = get_output_format()

        # Resolve credentials ID if not provided
        if not credentials_id:
            # Get credentials for this platform
            creds_response = client.get("/credentials", params={"platformId": "azure-batch"})
            credentials = creds_response.get("credentials", [])

            if not credentials:
                output_error("No Azure Batch credentials found. Please create Azure Batch credentials first.")
                sys.exit(1)

            # Use the first matching credential with the same name as compute env
            matching_cred = None
            for cred in credentials:
                if cred.get("name") == name:
                    matching_cred = cred
                    break

            if matching_cred:
                credentials_id = matching_cred.get("id")
            else:
                # Use first available credential
                credentials_id = credentials[0].get("id")

        # Read file contents
        pre_run_content = read_file_content(pre_run)
        post_run_content = read_file_content(post_run)
        nextflow_config_content = read_file_content(nextflow_config)

        # Parse environment variables
        env_vars = parse_environment_variables(environment)

        # Build forge config
        forge_config = {
            "vmCount": vm_count,
            "autoScale": not no_auto_scale,
            "disposeOnDeletion": not preserve_resources,
        }

        if vm_type:
            forge_config["vmType"] = vm_type

        # Resolve registry credentials if provided
        if registry_credentials:
            # Get all credentials and filter for container registry type
            creds_response = client.get("/credentials")
            all_credentials = creds_response.get("credentials", [])

            container_reg_ids = []
            for cred_name in registry_credentials:
                found = False
                for cred in all_credentials:
                    if cred.get("name") == cred_name and cred.get("provider") == "container-reg":
                        container_reg_ids.append(cred.get("id"))
                        found = True
                        break
                if not found:
                    output_error(f"Container registry credential '{cred_name}' not found")
                    sys.exit(1)

            if container_reg_ids:
                forge_config["containerRegIds"] = container_reg_ids

        # Build config payload
        config = {
            "workDir": work_dir,
            "region": location,
            "fusion2Enabled": fusion_v2,
            "waveEnabled": wave,
            "forge": forge_config,
        }

        # Add optional fields
        if pre_run_content:
            config["preRunScript"] = pre_run_content
        if post_run_content:
            config["postRunScript"] = post_run_content
        if nextflow_config_content:
            config["nextflowConfig"] = nextflow_config_content
        if env_vars:
            config["environment"] = env_vars
        if jobs_cleanup:
            config["deleteJobsOnCompletion"] = jobs_cleanup
        if token_duration:
            config["tokenDuration"] = token_duration

        # Build full payload
        payload = {
            "computeEnv": {
                "name": name,
                "platform": "azure-batch",
                "credentialsId": credentials_id,
                "config": config,
            }
        }

        # Create compute environment
        response = client.post("/compute-envs", json=payload)

        # Output response
        result = ComputeEnvAdded(
            platform="azure-batch",
            compute_env_id=response.get("computeEnvId", ""),
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
