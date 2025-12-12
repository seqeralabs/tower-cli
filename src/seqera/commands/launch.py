"""
Launch command for Seqera CLI.

Launch pipelines in workspaces.
"""

import json
import sys
from pathlib import Path
from typing import Annotated

import typer
import yaml

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    InvalidResponseException,
    NotFoundError,
    SeqeraError,
)
from seqera.main import get_client, get_output_format
from seqera.responses import LaunchSubmitted
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Default workspace name
USER_WORKSPACE_NAME = "user"


def handle_launch_error(e: Exception) -> None:
    """Handle launch command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
        sys.exit(1)
    elif isinstance(e, InvalidResponseException):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, NotFoundError):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, SeqeraError):
        output_error(str(e))
        sys.exit(1)
    else:
        output_error(f"Unexpected error: {e}")
        sys.exit(1)


def output_response(response: object, output_format: OutputFormat) -> None:
    """Output a response in the specified format."""
    if output_format == OutputFormat.JSON:
        output_json(response.to_dict())
    elif output_format == OutputFormat.YAML:
        output_yaml(response.to_dict())
    else:  # console
        output_console(response.to_console())


def get_workspace_info(client: SeqeraClient, workspace_id: str | None = None) -> dict:
    """Get workspace information."""
    user_response = client.get("/user-info")
    user_name = user_response.get("user", {}).get("userName", USER_WORKSPACE_NAME)
    user_id = user_response.get("user", {}).get("id")

    if workspace_id:
        # Get workspace details
        workspaces_response = client.get(f"/user/{user_id}/workspaces")
        workspaces = workspaces_response.get("orgsAndWorkspaces", [])

        # Find matching workspace
        for ws in workspaces:
            if str(ws.get("workspaceId")) == workspace_id:
                org_name = ws.get("orgName", "")
                ws_name = ws.get("workspaceName", "")
                return {
                    "workspace_id": workspace_id,
                    "workspace_ref": f"{org_name}/{ws_name}",
                    "user_name": user_name,
                }

        # Workspace not found
        return {
            "workspace_id": workspace_id,
            "workspace_ref": workspace_id,
            "user_name": user_name,
        }

    return {
        "workspace_id": None,
        "workspace_ref": USER_WORKSPACE_NAME,
        "user_name": user_name,
    }


def find_pipeline_by_name(
    client: SeqeraClient, pipeline_name: str, workspace_id: str | None = None
) -> dict | None:
    """Find pipeline by name in workspace."""
    params = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/pipelines", params=params)
    pipelines = response.get("pipelines", [])

    for pipeline in pipelines:
        if pipeline.get("name") == pipeline_name:
            return pipeline

    return None


def get_pipeline_launch_config(
    client: SeqeraClient, pipeline_id: int, workspace_id: str | None = None
) -> dict:
    """Get pipeline launch configuration."""
    params = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get(f"/pipelines/{pipeline_id}/launch", params=params)
    return response.get("launch", {})


def get_primary_compute_env(client: SeqeraClient, workspace_id: str | None = None) -> dict:
    """Get primary compute environment."""
    params = {"status": "AVAILABLE"}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/compute-envs", params=params)
    compute_envs = response.get("computeEnvs", [])

    # Find primary compute environment
    for ce in compute_envs:
        if ce.get("primary"):
            return ce

    # If no primary, return first available
    if compute_envs:
        return compute_envs[0]

    raise InvalidResponseException("No compute environment available")


def get_compute_env_details(
    client: SeqeraClient, compute_env_id: str, workspace_id: str | None = None
) -> dict:
    """Get compute environment details."""
    params = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get(f"/compute-envs/{compute_env_id}", params=params)
    return response.get("computeEnv", {})


def read_params_file(params_file: str) -> str:
    """Read parameters from file (JSON or YAML)."""
    path = Path(params_file)
    if not path.exists():
        raise InvalidResponseException(f"Parameters file not found: {params_file}")

    content = path.read_text()

    # Try to parse as JSON or YAML
    try:
        data = json.loads(content)
        return yaml.dump(data)
    except json.JSONDecodeError:
        # Try YAML
        try:
            data = yaml.safe_load(content)
            return yaml.dump(data)
        except yaml.YAMLError:
            # Return as-is
            return content


def read_file_content(file_path: str) -> str:
    """Read content from file."""
    path = Path(file_path)
    if not path.exists():
        raise InvalidResponseException(f"File not found: {file_path}")
    return path.read_text()


def resolve_labels(
    client: SeqeraClient, label_names: list[str], workspace_id: str | None = None
) -> list[int]:
    """Resolve label names to label IDs, creating missing labels."""
    params = {"type": "simple"}
    if workspace_id:
        params["workspaceId"] = workspace_id

    # Get existing labels
    response = client.get("/labels", params=params)
    existing_labels = response.get("labels", [])

    # Build name to ID mapping
    label_map = {label.get("name"): label.get("id") for label in existing_labels}

    label_ids = []
    for label_name in label_names:
        if label_name in label_map:
            label_ids.append(label_map[label_name])
        else:
            # Create new label
            new_label_data = {
                "name": label_name,
                "resource": False,
                "isDefault": False,
            }

            post_params = {}
            if workspace_id:
                post_params["workspaceId"] = workspace_id

            create_response = client.post("/labels", json=new_label_data, params=post_params)
            label_ids.append(create_response.get("id"))

    return label_ids


def is_repository_url(pipeline: str) -> bool:
    """Check if pipeline is a repository URL."""
    return pipeline.startswith("http://") or pipeline.startswith("https://")


def launch(
    pipeline: Annotated[
        str,
        typer.Argument(help="Pipeline name or repository URL"),
    ],
    name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Workflow run name"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID or reference"),
    ] = None,
    compute_env: Annotated[
        str | None,
        typer.Option("-c", "--compute-env", help="Compute environment name or ID"),
    ] = None,
    work_dir: Annotated[
        str | None,
        typer.Option("--work-dir", help="Work directory"),
    ] = None,
    params_file: Annotated[
        str | None,
        typer.Option("-p", "--params-file", help="Parameters file (JSON/YAML)"),
    ] = None,
    revision: Annotated[
        str | None,
        typer.Option("-r", "--revision", help="Pipeline revision/branch"),
    ] = None,
    config: Annotated[
        str | None,
        typer.Option("--config", help="Nextflow config file"),
    ] = None,
    pre_run: Annotated[
        str | None,
        typer.Option("--pre-run", help="Pre-run script file"),
    ] = None,
    post_run: Annotated[
        str | None,
        typer.Option("--post-run", help="Post-run script file"),
    ] = None,
    profile: Annotated[
        str | None,
        typer.Option("--profile", help="Nextflow profile(s) (comma-separated)"),
    ] = None,
    main_script: Annotated[
        str | None,
        typer.Option("--main-script", help="Main script"),
    ] = None,
    entry_name: Annotated[
        str | None,
        typer.Option("--entry-name", help="Entry name"),
    ] = None,
    schema_name: Annotated[
        str | None,
        typer.Option("--schema-name", help="Schema name"),
    ] = None,
    pull_latest: Annotated[
        bool,
        typer.Option("--pull-latest", help="Pull latest"),
    ] = False,
    stub_run: Annotated[
        bool,
        typer.Option("--stub-run", help="Stub run"),
    ] = False,
    resume: Annotated[
        bool,
        typer.Option("--resume", help="Resume previous run"),
    ] = False,
    label: Annotated[
        str | None,
        typer.Option("-l", "--label", help="Resource labels (comma-separated)"),
    ] = None,
    disable_optimization: Annotated[
        bool,
        typer.Option("--disable-optimization", help="Disable optimization"),
    ] = False,
    wait: Annotated[
        str | None,
        typer.Option("--wait", help="Wait for completion (optional)"),
    ] = None,
    launch_container: Annotated[
        str | None,
        typer.Option(
            "--launch-container", help="Container to be used to run the Nextflow head job (BETA)"
        ),
    ] = None,
    user_secrets: Annotated[
        str | None,
        typer.Option(
            "--user-secrets", help="User secrets (comma-separated) for the pipeline execution"
        ),
    ] = None,
    workspace_secrets: Annotated[
        str | None,
        typer.Option(
            "--workspace-secrets",
            help="Workspace secrets (comma-separated) for the pipeline execution",
        ),
    ] = None,
) -> None:
    """Launch a pipeline."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_info = get_workspace_info(client, workspace)
        workspace_id = workspace_info["workspace_id"]
        workspace_ref = workspace_info["workspace_ref"]

        # Build launch payload
        launch_payload: dict = {}

        # Determine if launching from pipeline name or repository URL
        if is_repository_url(pipeline):
            # Launch from repository URL
            # Get compute environment
            if compute_env:
                # TODO: Look up compute env by name
                ce_details = get_compute_env_details(client, compute_env, workspace_id)
            else:
                ce = get_primary_compute_env(client, workspace_id)
                ce_details = get_compute_env_details(client, ce["id"], workspace_id)

            launch_payload["computeEnvId"] = ce_details["id"]
            launch_payload["pipeline"] = pipeline

            # Use work dir from compute env if not specified
            if work_dir:
                launch_payload["workDir"] = work_dir
            else:
                work_dir_from_ce = ce_details.get("config", {}).get("workDir")
                if work_dir_from_ce:
                    launch_payload["workDir"] = work_dir_from_ce
        else:
            # Launch from pipeline name
            # Find pipeline
            pipeline_obj = find_pipeline_by_name(client, pipeline, workspace_id)
            if not pipeline_obj:
                raise InvalidResponseException(
                    f"Pipeline '{pipeline}' not found on this workspace."
                )

            # Get launch configuration
            launch_config = get_pipeline_launch_config(
                client, pipeline_obj["pipelineId"], workspace_id
            )

            # Start with launch config
            launch_payload["id"] = launch_config.get("id")
            launch_payload["computeEnvId"] = launch_config.get("computeEnv", {}).get("id")
            launch_payload["pipeline"] = launch_config.get("pipeline")
            launch_payload["workDir"] = launch_config.get("workDir")
            launch_payload["pullLatest"] = launch_config.get("pullLatest", False)
            launch_payload["stubRun"] = launch_config.get("stubRun", False)

            # Add optimization fields if not disabled
            if not disable_optimization:
                optimization_id = launch_config.get("optimizationId")
                optimization_targets = launch_config.get("optimizationTargets")

                if optimization_id:
                    launch_payload["optimizationId"] = optimization_id
                if optimization_targets:
                    launch_payload["optimizationTargets"] = optimization_targets

        # Override with command-line options
        if name:
            launch_payload["runName"] = name

        if work_dir:
            launch_payload["workDir"] = work_dir

        if revision:
            launch_payload["revision"] = revision

        if profile:
            # Split comma-separated profiles
            profiles = [p.strip() for p in profile.split(",")]
            launch_payload["configProfiles"] = profiles

        if config:
            launch_payload["configText"] = read_file_content(config)

        if pre_run:
            launch_payload["preRunScript"] = read_file_content(pre_run)

        if post_run:
            launch_payload["postRunScript"] = read_file_content(post_run)

        if main_script:
            launch_payload["mainScript"] = main_script

        if entry_name:
            launch_payload["entryName"] = entry_name

        if schema_name:
            launch_payload["schemaName"] = schema_name

        if pull_latest:
            launch_payload["pullLatest"] = pull_latest

        if stub_run:
            launch_payload["stubRun"] = stub_run

        if params_file:
            launch_payload["paramsText"] = read_params_file(params_file)

        # Handle labels
        if label:
            label_names = [l.strip() for l in label.split(",")]
            label_ids = resolve_labels(client, label_names, workspace_id)
            launch_payload["labelIds"] = label_ids

        # Handle launch container (BETA)
        if launch_container:
            launch_payload["headJobContainer"] = launch_container

        # Handle secrets
        if user_secrets:
            secret_names = [s.strip() for s in user_secrets.split(",")]
            launch_payload["userSecrets"] = secret_names

        if workspace_secrets:
            secret_names = [s.strip() for s in workspace_secrets.split(",")]
            launch_payload["workspaceSecrets"] = secret_names

        # Submit launch
        post_params = {}
        if workspace_id:
            post_params["workspaceId"] = workspace_id

        launch_request = {"launch": launch_payload}
        response = client.post("/workflow/launch", json=launch_request, params=post_params)
        workflow_id = response.get("workflowId")

        # Get base URL for workspace
        base_url = client.base_url.replace("/api", "")

        # Output response
        result = LaunchSubmitted(
            workflow_id=workflow_id,
            workspace_id=int(workspace_id) if workspace_id else None,
            base_url=base_url,
            workspace_ref=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_launch_error(e)
