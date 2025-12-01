"""
Google Batch platform implementation for compute environments.
"""

import sys
from pathlib import Path
from typing import Annotated

import typer

from seqera.main import get_client, get_output_format
from seqera.responses.computeenvs import ComputeEnvAdded
from seqera.utils.output import output_error


def read_file_content(file_path: Path | None) -> str | None:
    """Read and return the content of a file."""
    if file_path is None:
        return None
    try:
        with open(file_path) as f:
            return f.read()
    except Exception as e:
        output_error(f"Failed to read file {file_path}: {e}")
        sys.exit(1)


def parse_environment_variables(env_vars: list[str] | None) -> list[dict] | None:
    """Parse environment variables from key=value format."""
    if not env_vars:
        return None

    result = []
    for env in env_vars:
        if "=" not in env:
            output_error(f"Invalid environment variable format: {env}. Expected key=value")
            sys.exit(1)
        key, value = env.split("=", 1)
        result.append(
            {
                "name": key,
                "value": value,
                "head": False,
                "compute": True,
            }
        )
    return result


def add_google_batch(
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
        typer.Option(
            "-l",
            "--location",
            help="The location where the job executions are deployed to Google Batch API",
        ),
    ],
    # Optional main options
    spot: Annotated[
        bool,
        typer.Option("--spot", help="Use Spot virtual machines"),
    ] = False,
    fusion_v2: Annotated[
        bool,
        typer.Option(
            "--fusion-v2",
            help="With Fusion v2 enabled, S3 buckets specified in the Pipeline work directory and Allowed S3 Buckets fields will be accessible in the compute nodes storage (requires Wave containers service)",
        ),
    ] = False,
    wave: Annotated[
        bool,
        typer.Option(
            "--wave",
            help="Allow access to private container repositories and the provisioning of containers in your Nextflow pipelines via the Wave containers service",
        ),
    ] = False,
    # Advanced options
    use_private_address: Annotated[
        bool,
        typer.Option(
            "--use-private-address",
            help="Do not attach a public IP address to the VM. When enabled only Google internal services are accessible",
        ),
    ] = False,
    boot_disk_size: Annotated[
        int | None,
        typer.Option("--boot-disk-size", help="Enter the boot disk size as GB"),
    ] = None,
    head_job_cpus: Annotated[
        int | None,
        typer.Option(
            "--head-job-cpus", help="The number of CPUs to be allocated for the Nextflow runner job"
        ),
    ] = None,
    head_job_memory: Annotated[
        int | None,
        typer.Option(
            "--head-job-memory",
            help="The number of MiB of memory reserved for the Nextflow runner job (value should be a multiple of 256MiB and from 0.5 GB to 8 GB per CPU)",
        ),
    ] = None,
    service_account_email: Annotated[
        str | None,
        typer.Option(
            "--service-account-email",
            help="The service account email address used when deploying pipeline executions with this compute environment",
        ),
    ] = None,
    # Common platform options
    pre_run: Annotated[
        Path | None,
        typer.Option("--pre-run", help="Pre-run script file"),
    ] = None,
    post_run: Annotated[
        Path | None,
        typer.Option("--post-run", help="Post-run script file"),
    ] = None,
    environment: Annotated[
        list[str] | None,
        typer.Option(
            "--environment",
            "-e",
            help="Environment variables (key=value format, can be specified multiple times)",
        ),
    ] = None,
    nextflow_config: Annotated[
        Path | None,
        typer.Option("--nextflow-config", help="Nextflow config file"),
    ] = None,
    # Credentials option
    credentials_id: Annotated[
        str | None,
        typer.Option("-c", "--credentials", help="Credentials identifier"),
    ] = None,
    # Workspace option
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Add new Google Batch compute environment."""
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
            creds_response = client.get("/credentials", params={"platformId": "google-batch"})
            credentials = creds_response.get("credentials", [])

            if not credentials:
                output_error(
                    "No Google Batch credentials found. Please create Google credentials first."
                )
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

        # Build config payload
        config = {
            "workDir": work_dir,
            "location": location,
            "fusion2Enabled": fusion_v2,
            "waveEnabled": wave,
        }

        # Add optional main fields
        if spot:
            config["spot"] = spot

        # Add common platform options
        if pre_run_content:
            config["preRunScript"] = pre_run_content
        if post_run_content:
            config["postRunScript"] = post_run_content
        if nextflow_config_content:
            config["nextflowConfig"] = nextflow_config_content
        if env_vars:
            config["environment"] = env_vars

        # Add advanced options
        if use_private_address:
            config["usePrivateAddress"] = use_private_address
        if boot_disk_size is not None:
            config["bootDiskSizeGb"] = boot_disk_size
        if head_job_cpus is not None:
            config["headJobCpus"] = head_job_cpus
        if head_job_memory is not None:
            config["headJobMemoryMb"] = head_job_memory
        if service_account_email:
            config["serviceAccount"] = service_account_email

        # Build full payload
        payload = {
            "computeEnv": {
                "name": name,
                "platform": "google-batch",
                "credentialsId": credentials_id,
                "config": config,
            }
        }

        # Create compute environment
        response = client.post("/compute-envs", json=payload)

        # Output response
        result = ComputeEnvAdded(
            platform="google-batch",
            compute_env_id=response.get("computeEnvId", ""),
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
