"""
Seqera Compute platform implementation for compute environments.
"""

import sys
from pathlib import Path
from typing import Dict, List, Optional

import typer
from typing_extensions import Annotated

from seqera.commands.computeenvs import (
    USER_WORKSPACE_NAME,
    add_app,
    handle_compute_env_error,
    output_response,
)
from seqera.exceptions import SeqeraError
from seqera.main import get_client, get_output_format
from seqera.responses.computeenvs import ComputeEnvAdded


def read_file_content(file_path: Optional[Path]) -> Optional[str]:
    """Read content from a file if provided."""
    if file_path is None:
        return None
    try:
        return file_path.read_text()
    except Exception as e:
        raise SeqeraError(f"Failed to read file {file_path}: {e}")


def parse_environment_variables(env_vars: Optional[List[str]]) -> Optional[List[Dict[str, any]]]:
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

        result.append({
            "name": var_name,
            "value": value,
            "head": head,
            "compute": compute,
        })

    return result


@add_app.command("seqera-compute")
def add_seqera_compute(
    name: Annotated[str, typer.Option("-n", "--name", help="Compute environment name.")],
    region: Annotated[str, typer.Option("-r", "--region", help="AWS region.")],
    work_dir: Annotated[Optional[str], typer.Option("--work-dir", help="Work directory suffix relative to the S3 bucket that will be created by Seqera Compute.")] = None,
    pre_run: Annotated[Optional[Path], typer.Option("--pre-run", help="Pre-run script.")] = None,
    post_run: Annotated[Optional[Path], typer.Option("--post-run", help="Post-run script.")] = None,
    nextflow_config: Annotated[Optional[Path], typer.Option("--nextflow-config", help="Nextflow config")] = None,
    env: Annotated[Optional[List[str]], typer.Option("-e", "--env", help="Add environment variables. By default are only added to the Nextflow head job process, if you want to add them to the process task prefix the name with 'compute:' or 'both:' if you want to make it available to both locations.")] = None,
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

        # Create compute environment
        # Note: Seqera Compute doesn't require explicit credentials
        payload = {
            "computeEnv": {
                "name": name,
                "platform": "seqeracompute-platform",
                "config": config,
            }
        }

        response = client.post("/compute-envs", json=payload)

        result = ComputeEnvAdded(
            platform="seqeracompute-platform",
            compute_env_id=response["computeEnvId"],
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
