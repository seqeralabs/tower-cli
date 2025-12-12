"""
Seqera Compute platform implementation for compute environments.
"""

from pathlib import Path
from typing import Annotated

import typer

from seqera.commands.computeenvs import (
    USER_WORKSPACE_NAME,
    add_app,
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


@add_app.command("seqera-compute")
def add_seqera_compute(
    name: Annotated[str, typer.Option("-n", "--name", help="Compute environment name.")],
    region: Annotated[str, typer.Option("-r", "--region", help="AWS region.")],
    work_dir: Annotated[
        str | None,
        typer.Option(
            "--work-dir",
            help="Work directory suffix relative to the S3 bucket that will be created by Seqera Compute.",
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
    labels: Annotated[
        str | None,
        typer.Option("--labels", help="Comma-separated list of labels"),
    ] = None,
    wait: Annotated[
        str | None,
        typer.Option(
            "--wait",
            help="Wait until compute environment reaches status (CREATING, AVAILABLE, ERRORED, INVALID)",
        ),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Add new Seqera Compute environment."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build config payload
        # NOTE: even though 'SeqeraComputeConfig' extends 'AwsBatchConfig', most
        # settings will automatically be configured by seqera compute and can't
        # be overridden. The workDir option is accepted but not sent in the config.
        config = {
            "region": region,
        }

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

        # Create compute environment payload
        # Note: Seqera Compute doesn't require explicit credentials
        compute_env_payload = {
            "name": name,
            "platform": "seqeracompute-platform",
            "config": config,
        }

        # Add labels if specified
        if labels:
            label_ids = find_or_create_label_ids(client, labels, workspace)
            if label_ids:
                compute_env_payload["labelIds"] = label_ids

        payload = {"computeEnv": compute_env_payload}

        response = client.post("/compute-envs", json=payload)
        compute_env_id = response["computeEnvId"]

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
            platform="seqeracompute-platform",
            compute_env_id=compute_env_id,
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
