"""
MOAB platform implementation for compute environments.
"""

import sys
from pathlib import Path
from typing import Annotated

import typer

from seqera.api.client import SeqeraClient
from seqera.exceptions import SeqeraError
from seqera.main import get_client, get_output_format
from seqera.responses.computeenvs import ComputeEnvAdded
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Constants
USER_WORKSPACE_NAME = "user"
PLATFORM_ID = "moab-platform"


def output_response(response, output_format: OutputFormat) -> None:
    """Output response in the requested format."""
    if output_format == OutputFormat.JSON:
        output_json(response.to_dict())
    elif output_format == OutputFormat.YAML:
        output_yaml(response.to_dict())
    else:
        output_console(response.to_console())


def handle_error(error: Exception) -> None:
    """Handle errors and exit."""
    output_error(str(error))
    sys.exit(1)


def find_credentials(client: SeqeraClient, platform_id: str) -> str:
    """Find credentials for the platform."""
    try:
        response = client.get("/credentials", params={"platformId": platform_id})
        credentials = response.get("credentials", [])

        if not credentials:
            raise SeqeraError("No valid credentials found at the workspace")

        if len(credentials) > 1:
            raise SeqeraError(
                "Multiple credentials match this compute environment. "
                "Please provide the credentials identifier that you want to use"
            )

        return credentials[0]["id"]
    except Exception as e:
        raise SeqeraError(f"Failed to fetch credentials: {str(e)}")


def read_file_content(file_path: Path | None) -> str | None:
    """Read file content if path is provided."""
    if file_path is None:
        return None
    try:
        return file_path.read_text()
    except Exception as e:
        raise SeqeraError(f"Failed to read file {file_path}: {str(e)}")


def parse_environment_variables(env_vars: list[str] | None) -> list[dict] | None:
    """Parse environment variables in the format KEY=VALUE.

    Supports prefixes:
    - compute: - only for compute nodes
    - head: - only for head node (default)
    - both: - for both head and compute nodes
    """
    if not env_vars:
        return None

    parsed_vars = []
    for var in env_vars:
        if "=" not in var:
            raise SeqeraError(f"Invalid environment variable format: {var}. Expected KEY=VALUE")

        name, value = var.split("=", 1)

        # Determine head and compute flags based on prefix
        head = True
        compute = False
        var_name = name

        if name.startswith("compute:"):
            var_name = name[8:]  # Remove "compute:" prefix
            head = False
            compute = True
        elif name.startswith("head:"):
            var_name = name[5:]  # Remove "head:" prefix
            compute = False
        elif name.startswith("both:"):
            var_name = name[5:]  # Remove "both:" prefix
            compute = True

        parsed_vars.append({"name": var_name, "value": value, "head": head, "compute": compute})

    return parsed_vars


def add_moab(
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
            "-q",
            "--head-queue",
            help="The name of the queue on the cluster used to launch the execution of the Nextflow pipeline",
        ),
    ],
    user_name: Annotated[
        str | None,
        typer.Option(
            "-u",
            "--user-name",
            help="The username on the cluster used to launch the pipeline execution",
        ),
    ] = None,
    host_name: Annotated[
        str | None,
        typer.Option(
            "-H",
            "--host-name",
            help="The pipeline execution is launched by connecting via SSH to the hostname specified. This usually is the cluster login node. Local IP addresses e.g. 127.*, 172.*, 192.*, etc. are not allowed, use a fully qualified hostname instead",
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
            help="The name of queue on the cluster to which pipeline jobs are submitted. This queue can be overridden by the pipeline configuration",
        ),
    ] = None,
    launch_dir: Annotated[
        str | None,
        typer.Option(
            "--launch-dir",
            help="The directory where Nextflow runs. It must be an absolute directory and the user should have read-write access permissions to it [default: pipeline work directory]",
        ),
    ] = None,
    max_queue_size: Annotated[
        int | None,
        typer.Option(
            "--max-queue-size",
            help="This option limits the number of jobs Nextflow can submit to the Slurm queue at the same time [default: 100]",
        ),
    ] = None,
    head_job_options: Annotated[
        str | None,
        typer.Option(
            "--head-job-options",
            help="Slurm submit options for the Nextflow head job. These options are added to the 'sbatch' command run by Tower to launch the pipeline execution",
        ),
    ] = None,
    credentials: Annotated[
        str | None,
        typer.Option(
            "-c", "--credentials", help="Credentials identifier [default: workspace credentials]"
        ),
    ] = None,
    pre_run: Annotated[
        Path | None,
        typer.Option("--pre-run", help="Pre-run script"),
    ] = None,
    post_run: Annotated[
        Path | None,
        typer.Option("--post-run", help="Post-run script"),
    ] = None,
    nextflow_config: Annotated[
        Path | None,
        typer.Option("--nextflow-config", help="Nextflow config"),
    ] = None,
    env: Annotated[
        list[str] | None,
        typer.Option(
            "-e",
            "--env",
            help="Add environment variables. By default are only added to the Nextflow head job process, if you want to add them to the process task prefix the name with 'compute:' or 'both:' if you want to make it available to both locations",
        ),
    ] = None,
) -> None:
    """Add new MOAB compute environment."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find or use provided credentials
        if credentials:
            credentials_id = credentials
        else:
            credentials_id = find_credentials(client, PLATFORM_ID)

        # Build config
        config = {
            "workDir": work_dir,
            "headQueue": head_queue,
        }

        # Add optional main fields
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
        pre_run_content = read_file_content(pre_run)
        if pre_run_content:
            config["preRunScript"] = pre_run_content

        post_run_content = read_file_content(post_run)
        if post_run_content:
            config["postRunScript"] = post_run_content

        nextflow_config_content = read_file_content(nextflow_config)
        if nextflow_config_content:
            config["nextflowConfig"] = nextflow_config_content

        # Add environment variables
        env_vars = parse_environment_variables(env)
        if env_vars:
            config["environment"] = env_vars

        # Create compute environment request
        request = {
            "computeEnv": {
                "name": name,
                "platform": PLATFORM_ID,
                "credentialsId": credentials_id,
                "config": config,
            }
        }

        # Create compute environment
        response = client.post("/compute-envs", json=request)
        compute_env_id = response.get("computeEnvId")

        # Output response
        result = ComputeEnvAdded(
            platform=PLATFORM_ID,
            compute_env_id=compute_env_id,
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_error(e)
