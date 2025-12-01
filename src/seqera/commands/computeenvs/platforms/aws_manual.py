"""
AWS Batch Manual platform implementation for compute environments.

Manual mode: User manages existing AWS Batch resources (queues, etc.)
"""

import sys
from pathlib import Path
from typing import List, Optional

import typer
from typing_extensions import Annotated

from seqera.exceptions import SeqeraError
from seqera.main import get_client, get_output_format
from seqera.responses.computeenvs import ComputeEnvAdded
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
    """Parse environment variables from key=value format with optional scope prefix.

    Format: [scope:]key=value
    Scopes: head, compute, both (default is head)

    Examples:
    - FOO=bar -> head only
    - compute:FOO=bar -> compute only
    - both:FOO=bar -> both head and compute
    """
    if not env_vars:
        return None

    result = []
    for env in env_vars:
        if '=' not in env:
            output_error(f"Invalid environment variable format: {env}. Expected [scope:]key=value")
            sys.exit(1)

        # Check for scope prefix
        head = True
        compute = False
        value_part = env

        if ':' in env:
            parts = env.split(':', 1)
            scope = parts[0].lower()
            if scope in ['head', 'compute', 'both']:
                value_part = parts[1]
                if scope == 'head':
                    head = True
                    compute = False
                elif scope == 'compute':
                    head = False
                    compute = True
                elif scope == 'both':
                    head = True
                    compute = True

        if '=' not in value_part:
            output_error(f"Invalid environment variable format: {env}. Expected [scope:]key=value")
            sys.exit(1)

        key, value = value_part.split('=', 1)
        result.append({
            "name": key,
            "value": value,
            "head": head,
            "compute": compute,
        })
    return result


def add_aws_manual(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Compute environment name"),
    ],
    work_dir: Annotated[
        str,
        typer.Option("--work-dir", help="Work directory (e.g., s3://bucket/path)"),
    ],
    region: Annotated[
        str,
        typer.Option("-r", "--region", help="AWS region"),
    ],
    head_queue: Annotated[
        str,
        typer.Option("--head-queue", help="Batch queue for Nextflow head job (non-spot recommended)"),
    ],
    compute_queue: Annotated[
        str,
        typer.Option("--compute-queue", help="Batch queue for Nextflow compute jobs"),
    ],
    # Feature flags
    fusion_v2: Annotated[
        bool,
        typer.Option("--fusion-v2", help="Enable Fusion v2 for S3 bucket access (requires Wave)"),
    ] = False,
    wave: Annotated[
        bool,
        typer.Option("--wave", help="Enable Wave containers service for private repositories"),
    ] = False,
    fast_storage: Annotated[
        bool,
        typer.Option("--fast-storage", help="Enable NVMe instance storage for faster I/O (requires Fusion v2)"),
    ] = False,
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
        typer.Option("--environment", "-e", help="Environment variables ([scope:]key=value, scope: head|compute|both)"),
    ] = None,
    nextflow_config: Annotated[
        Optional[Path],
        typer.Option("--nextflow-config", help="Nextflow config file"),
    ] = None,
    # Advanced options
    head_job_cpus: Annotated[
        Optional[int],
        typer.Option("--head-job-cpus", help="Number of CPUs for Nextflow head job"),
    ] = None,
    head_job_memory: Annotated[
        Optional[int],
        typer.Option("--head-job-memory", help="Memory in MiB for Nextflow head job"),
    ] = None,
    head_job_role: Annotated[
        Optional[str],
        typer.Option("--head-job-role", help="IAM role for Nextflow head job"),
    ] = None,
    compute_job_role: Annotated[
        Optional[str],
        typer.Option("--compute-job-role", help="IAM role for compute jobs"),
    ] = None,
    batch_execution_role: Annotated[
        Optional[str],
        typer.Option("--batch-execution-role", help="Execution role for ECS container"),
    ] = None,
    cli_path: Annotated[
        Optional[str],
        typer.Option("--cli-path", help="AWS CLI path in EC2 instances"),
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
    """Add new AWS Batch compute environment using existing AWS Batch resources."""
    # Import here to avoid circular dependency
    from seqera.commands.computeenvs import (
        USER_WORKSPACE_NAME,
        handle_compute_env_error,
        output_response,
    )

    try:
        client = get_client()
        output_format = get_output_format()

        # Validate Fusion v2 requires Wave
        if fusion_v2 and not wave:
            raise SeqeraError("Fusion v2 requires Wave service")

        # Resolve credentials ID if not provided
        if not credentials_id:
            # Get credentials for this platform
            creds_response = client.get("/credentials", params={"platformId": "aws-batch"})
            credentials = creds_response.get("credentials", [])

            if not credentials:
                output_error("No AWS Batch credentials found. Please create AWS credentials first.")
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
            "region": region,
            "fusion2Enabled": fusion_v2,
            "waveEnabled": wave,
            "headQueue": head_queue,
            "computeQueue": compute_queue,
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
        if fast_storage:
            config["nvnmeStorageEnabled"] = fast_storage
        if cli_path:
            config["cliPath"] = cli_path
        if batch_execution_role:
            config["executionRole"] = batch_execution_role
        if compute_job_role:
            config["computeJobRole"] = compute_job_role
        if head_job_cpus:
            config["headJobCpus"] = head_job_cpus
        if head_job_memory:
            config["headJobMemoryMb"] = head_job_memory
        if head_job_role:
            config["headJobRole"] = head_job_role

        # Build full payload
        payload = {
            "computeEnv": {
                "name": name,
                "platform": "aws-batch",
                "credentialsId": credentials_id,
                "config": config,
            }
        }

        # Create compute environment
        response = client.post("/compute-envs", json=payload)

        # Output response
        result = ComputeEnvAdded(
            platform="aws-batch",
            compute_env_id=response.get("computeEnvId", ""),
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
