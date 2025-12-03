"""
UGE (Univa Grid Engine) compute environment platform implementation.
"""

import sys
from pathlib import Path
from typing import Annotated

import typer

from seqera.exceptions import SeqeraError
from seqera.main import get_client, get_output_format
from seqera.responses.computeenvs import ComputeEnvAdded
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

USER_WORKSPACE_NAME = "user"


def output_response(response, output_format: OutputFormat) -> None:
    """Output response in the requested format."""
    if output_format == OutputFormat.JSON:
        output_json(response.to_dict())
    elif output_format == OutputFormat.YAML:
        output_yaml(response.to_dict())
    else:
        output_console(response.to_console())


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

        # Handle head: and compute: prefixes
        key, value = env.split("=", 1)
        head = True
        compute = False

        if key.startswith("compute:"):
            key = key[8:]  # Remove "compute:" prefix
            head = False
            compute = True
        elif key.startswith("head:"):
            key = key[5:]  # Remove "head:" prefix
            head = True
            compute = False

        result.append(
            {
                "name": key,
                "value": value,
                "head": head,
                "compute": compute,
            }
        )
    return result


def add_uge(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Compute environment name"),
    ],
    work_dir: Annotated[
        str,
        typer.Option("--work-dir", help="Work directory"),
    ],
    head_queue: Annotated[
        str,
        typer.Option(
            "-q", "--head-queue", help="Queue for launching execution of the Nextflow pipeline"
        ),
    ],
    user_name: Annotated[
        str | None,
        typer.Option(
            "-u",
            "--user-name",
            help="Username on the cluster used to launch the pipeline execution",
        ),
    ] = None,
    host_name: Annotated[
        str | None,
        typer.Option(
            "-H",
            "--host-name",
            help="Pipeline execution hostname for SSH connection (cluster login node). Local IP addresses (127.*, 172.*, 192.*, etc.) are not allowed, use a fully qualified hostname instead",
        ),
    ] = None,
    port: Annotated[
        int | None,
        typer.Option("-p", "--port", help="Port number for the login connection"),
    ] = None,
    compute_queue: Annotated[
        str | None,
        typer.Option(
            "--compute-queue",
            help="Queue for pipeline jobs submission. This queue can be overridden by the pipeline configuration",
        ),
    ] = None,
    launch_dir: Annotated[
        str | None,
        typer.Option(
            "--launch-dir",
            help="Directory where Nextflow runs. It must be an absolute directory and the user should have read-write access permissions to it [default: pipeline work directory]",
        ),
    ] = None,
    max_queue_size: Annotated[
        int | None,
        typer.Option(
            "--max-queue-size",
            help="Maximum number of jobs that Nextflow can submit to the UGE queue at the same time [default: 100]",
        ),
    ] = None,
    head_job_options: Annotated[
        str | None,
        typer.Option(
            "--head-job-options",
            help="UGE submit options for the Nextflow head job. These options are added to the submit command run by Seqera to launch the pipeline execution",
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
    """Add new UNIVA grid engine compute environment."""
    try:
        client = get_client()
        output_format = get_output_format()

        platform_id = "uge-platform"

        # Resolve credentials ID if not provided
        if not credentials_id:
            # Get credentials for this platform
            creds_response = client.get("/credentials", params={"platformId": platform_id})
            credentials = creds_response.get("credentials", [])

            if not credentials:
                output_error(
                    "No SSH credentials found for UGE platform. Please create SSH credentials first."
                )
                sys.exit(1)

            if len(credentials) == 1:
                credentials_id = credentials[0].get("id")
            else:
                # Multiple credentials found
                output_error(
                    "Multiple credentials match this compute environment. "
                    "Please provide the credentials identifier that you want to use with --credentials"
                )
                sys.exit(1)

        # Read file contents
        pre_run_content = read_file_content(pre_run)
        post_run_content = read_file_content(post_run)
        nextflow_config_content = read_file_content(nextflow_config)

        # Parse environment variables
        env_vars = parse_environment_variables(environment)

        # Build config payload
        config = {
            "workDir": work_dir,
            "headQueue": head_queue,
        }

        # Add optional main parameters
        if user_name:
            config["userName"] = user_name
        if host_name:
            config["hostName"] = host_name
        if port:
            config["port"] = port
        if compute_queue:
            config["computeQueue"] = compute_queue
        if launch_dir:
            config["launchDir"] = launch_dir

        # Add advanced options
        if max_queue_size:
            config["maxQueueSize"] = max_queue_size
        if head_job_options:
            config["headJobOptions"] = head_job_options

        # Add staging options
        if pre_run_content:
            config["preRunScript"] = pre_run_content
        if post_run_content:
            config["postRunScript"] = post_run_content
        if nextflow_config_content:
            config["nextflowConfig"] = nextflow_config_content
        if env_vars:
            config["environment"] = env_vars

        # Build full payload
        payload = {
            "computeEnv": {
                "name": name,
                "platform": platform_id,
                "credentialsId": credentials_id,
                "config": config,
            }
        }

        # Create compute environment
        response = client.post("/compute-envs", json=payload)

        # Output response
        result = ComputeEnvAdded(
            platform=platform_id,
            compute_env_id=response.get("computeEnvId", ""),
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except SeqeraError as e:
        output_error(str(e))
        sys.exit(1)
    except Exception as e:
        output_error(f"Unexpected error: {e}")
        sys.exit(1)
