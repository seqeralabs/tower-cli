"""
Google Kubernetes Engine (GKE) platform implementation for compute environments.
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


def add_gke(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Compute environment name"),
    ],
    work_dir: Annotated[
        str,
        typer.Option("--work-dir", help="Work directory"),
    ],
    region: Annotated[
        str,
        typer.Option("-r", "--region", help="GCP region"),
    ],
    cluster_name: Annotated[
        str,
        typer.Option("--cluster-name", help="The GKE cluster name"),
    ],
    namespace: Annotated[
        str,
        typer.Option("--namespace", help="Namespace"),
    ],
    head_account: Annotated[
        str,
        typer.Option("--head-account", help="Head service account"),
    ],
    storage_claim: Annotated[
        str | None,
        typer.Option("--storage-claim", help="Storage claim name"),
    ] = None,
    # Advanced options
    storage_mount: Annotated[
        str | None,
        typer.Option("--storage-mount", help="Storage mount path"),
    ] = None,
    compute_account: Annotated[
        str | None,
        typer.Option("--compute-account", help="Compute service account"),
    ] = None,
    pod_cleanup: Annotated[
        str | None,
        typer.Option("--pod-cleanup", help="Pod cleanup policy (ON_SUCCESS, ALWAYS, NEVER)"),
    ] = None,
    head_pod_spec: Annotated[
        Path | None,
        typer.Option("--head-pod-spec", help="Custom head pod specs file"),
    ] = None,
    service_pod_spec: Annotated[
        Path | None,
        typer.Option("--service-pod-spec", help="Custom service pod specs file"),
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
    """Add new Google GKE compute environment."""
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
            creds_response = client.get("/credentials", params={"platformId": "gke-platform"})
            credentials = creds_response.get("credentials", [])

            if not credentials:
                output_error("No GKE credentials found. Please create Google credentials first.")
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
        head_pod_spec_content = read_file_content(head_pod_spec)
        service_pod_spec_content = read_file_content(service_pod_spec)

        # Parse environment variables
        env_vars = parse_environment_variables(environment)

        # Build config payload
        config = {
            "workDir": work_dir,
            "region": region,
            "clusterName": cluster_name,
            "namespace": namespace,
            "headServiceAccount": head_account,
        }

        # Add optional fields
        if storage_claim:
            config["storageClaimName"] = storage_claim
        if pre_run_content:
            config["preRunScript"] = pre_run_content
        if post_run_content:
            config["postRunScript"] = post_run_content
        if nextflow_config_content:
            config["nextflowConfig"] = nextflow_config_content
        if env_vars:
            config["environment"] = env_vars
        if storage_mount:
            config["storageMountPath"] = storage_mount
        if compute_account:
            config["computeServiceAccount"] = compute_account
        if pod_cleanup:
            config["podCleanup"] = pod_cleanup
        if head_pod_spec_content:
            config["headPodSpec"] = head_pod_spec_content
        if service_pod_spec_content:
            config["servicePodSpec"] = service_pod_spec_content

        # Build full payload
        payload = {
            "computeEnv": {
                "name": name,
                "platform": "gke-platform",
                "credentialsId": credentials_id,
                "config": config,
            }
        }

        # Create compute environment
        response = client.post("/compute-envs", json=payload)

        # Output response
        result = ComputeEnvAdded(
            platform="gke-platform",
            compute_env_id=response.get("computeEnvId", ""),
            name=name,
            workspace_id=None,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
