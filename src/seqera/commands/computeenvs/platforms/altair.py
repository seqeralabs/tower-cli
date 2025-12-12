"""
Altair PBS Pro platform implementation for compute environments.
"""

from pathlib import Path
from typing import Annotated

import typer

from seqera.commands.computeenvs import (
    USER_WORKSPACE_NAME,
    find_or_create_label_ids,
    handle_compute_env_error,
    output_response,
    wait_for_compute_env_status,
)
from seqera.exceptions import SeqeraError
from seqera.main import get_client, get_output_format
from seqera.responses.computeenvs import ComputeEnvAdded


def read_file_content(file_path: Path | None) -> str | None:
    """Read content from a file if provided."""
    if file_path is None:
        return None
    try:
        return file_path.read_text()
    except Exception as e:
        raise SeqeraError(f"Failed to read file {file_path}: {e}")


def parse_environment_variables(env_vars: list[str] | None) -> list[dict[str, any]] | None:
    """
    Parse environment variables from key=value format.

    Supports prefixes:
    - compute: - only for compute nodes
    - head: - only for head job
    - both: - for both head and compute
    - no prefix - head only (default)
    """
    if not env_vars:
        return None

    result = []
    for var in env_vars:
        if "=" not in var:
            raise SeqeraError(f"Invalid environment variable format: {var}. Expected key=value")

        key, value = var.split("=", 1)
        head = True
        compute = False
        var_name = key

        if key.startswith("compute:"):
            var_name = key[8:]
            head = False
            compute = True
        elif key.startswith("head:"):
            var_name = key[5:]
            compute = False
        elif key.startswith("both:"):
            var_name = key[5:]
            compute = True

        result.append(
            {
                "name": var_name,
                "value": value,
                "head": head,
                "compute": compute,
            }
        )

    return result


def find_credentials(client, platform_id: str, credentials_ref: str | None) -> str | None:
    """
    Find credentials for the platform.

    If credentials_ref is provided, use it to find the credential.
    Otherwise, auto-detect by querying credentials for the platform.
    """
    if credentials_ref:
        # Find credential by name or ID
        response = client.get("/credentials", params={"platformId": platform_id})
        credentials = response.get("credentials", [])

        for cred in credentials:
            if cred.get("id") == credentials_ref or cred.get("name") == credentials_ref:
                return cred.get("id")

        raise SeqeraError(f"Credentials '{credentials_ref}' not found for platform '{platform_id}'")
    else:
        # Auto-detect credentials
        response = client.get("/credentials", params={"platformId": platform_id})
        credentials = response.get("credentials", [])

        if len(credentials) == 0:
            raise SeqeraError("No valid credentials found at the workspace")
        elif len(credentials) > 1:
            raise SeqeraError(
                "Multiple credentials match this compute environment. "
                "Please provide the credentials identifier that you want to use"
            )

        return credentials[0].get("id")


def add_altair(
    name: Annotated[str, typer.Option("-n", "--name", help="Compute environment name")],
    work_dir: Annotated[str, typer.Option("--work-dir", help="Work directory")],
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
            help="The username on the cluster used to launch the pipeline execution.",
        ),
    ] = None,
    host_name: Annotated[
        str | None,
        typer.Option(
            "-H",
            "--host-name",
            help="The pipeline execution is launched by connecting via SSH to the hostname specified. This usually is the cluster login node. Local IP addresses e.g. 127.*, 172.*, 192.*, etc. are not allowed, use a fully qualified hostname instead.",
        ),
    ] = None,
    port: Annotated[
        int | None, typer.Option("-p", "--port", help="Port number for the login connection.")
    ] = None,
    compute_queue: Annotated[
        str | None,
        typer.Option(
            "--compute-queue",
            help="The name of queue on the cluster to which pipeline jobs are submitted. This queue can be overridden by the pipeline configuration.",
        ),
    ] = None,
    launch_dir: Annotated[
        str | None,
        typer.Option(
            "--launch-dir",
            help="The directory where Nextflow runs. It must be an absolute directory and the user should have read-write access permissions to it [default: pipeline work directory].",
        ),
    ] = None,
    max_queue_size: Annotated[
        int | None,
        typer.Option(
            "--max-queue-size",
            help="This option limits the number of jobs Nextflow can submit to the Altair PBS queue at the same time [default: 100].",
        ),
    ] = None,
    head_job_options: Annotated[
        str | None,
        typer.Option(
            "--head-job-options",
            help="Altair PBS submit options for the Nextflow head job. These options are added to the 'qsub' command run by Seqera to launch the pipeline execution.",
        ),
    ] = None,
    pre_run: Annotated[Path | None, typer.Option("--pre-run", help="Pre-run script.")] = None,
    post_run: Annotated[Path | None, typer.Option("--post-run", help="Post-run script.")] = None,
    nextflow_config: Annotated[
        Path | None, typer.Option("--nextflow-config", help="Nextflow config")
    ] = None,
    env: Annotated[
        list[str] | None,
        typer.Option(
            "-e",
            "--env",
            help="Add environment variables. By default are only added to the Nextflow head job process, if you want to add them to the process task prefix the name with 'compute:' or 'both:' if you want to make it available to both locations.",
        ),
    ] = None,
    credentials: Annotated[
        str | None,
        typer.Option(
            "-c", "--credentials", help="Credentials identifier [default: workspace credentials]."
        ),
    ] = None,
    # Labels option
    labels: Annotated[
        str | None,
        typer.Option("--labels", help="Comma-separated list of labels"),
    ] = None,
    # Wait option
    wait: Annotated[
        str | None,
        typer.Option(
            "--wait",
            help="Wait until compute environment reaches status (CREATING, AVAILABLE, ERRORED, INVALID)",
        ),
    ] = None,
    # Workspace option
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Add new Altair PBS Pro compute environment."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find credentials
        credentials_id = find_credentials(client, "altair-platform", credentials)

        # Build config payload
        config = {
            "workDir": work_dir,
            "headQueue": head_queue,
        }

        # Add optional main fields
        if user_name is not None:
            config["userName"] = user_name
        if host_name is not None:
            config["hostName"] = host_name
        if port is not None:
            config["port"] = port
        if compute_queue is not None:
            config["computeQueue"] = compute_queue
        if launch_dir is not None:
            config["launchDir"] = launch_dir

        # Add advanced options
        if max_queue_size is not None:
            config["maxQueueSize"] = max_queue_size
        if head_job_options is not None:
            config["headJobOptions"] = head_job_options

        # Add staging options
        pre_run_script = read_file_content(pre_run)
        if pre_run_script is not None:
            config["preRunScript"] = pre_run_script

        post_run_script = read_file_content(post_run)
        if post_run_script is not None:
            config["postRunScript"] = post_run_script

        nextflow_config_content = read_file_content(nextflow_config)
        if nextflow_config_content is not None:
            config["nextflowConfig"] = nextflow_config_content

        # Add environment variables
        environment_vars = parse_environment_variables(env)
        if environment_vars is not None:
            config["environment"] = environment_vars

        # Build compute env payload
        compute_env_payload = {
            "name": name,
            "platform": "altair-platform",
            "config": config,
            "credentialsId": credentials_id,
        }

        # Add labels if specified
        if labels:
            label_ids = find_or_create_label_ids(client, labels, workspace)
            if label_ids:
                compute_env_payload["labelIds"] = label_ids

        # Create compute environment
        payload = {"computeEnv": compute_env_payload}

        response = client.post("/compute-envs", json=payload)

        compute_env_id = response.get("computeEnvId", "")

        # Wait for status if requested
        if wait:
            typer.echo(f"Waiting for compute environment to reach '{wait}' status...")
            if wait_for_compute_env_status(client, compute_env_id, wait, workspace):
                typer.echo(f"Compute environment reached '{wait}' status")
            else:
                typer.echo(
                    f"Warning: Compute environment did not reach '{wait}' status within timeout"
                )

        result = ComputeEnvAdded(
            platform="altair-platform",
            compute_env_id=compute_env_id,
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
