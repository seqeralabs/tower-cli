"""
Pipelines commands for Seqera CLI.

Manage pipelines in workspaces.
"""

import json
import sys
from pathlib import Path
from typing import Annotated, Optional

import typer
import yaml

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    InvalidResponseException,
    MultiplePipelinesFoundException,
    NoComputeEnvironmentException,
    NotFoundError,
    PipelineNotFoundException,
    SeqeraError,
    WorkspaceNotFoundException,
)
from seqera.main import get_client, get_output_format
from seqera.responses import (
    PipelineAdded,
    PipelineDeleted,
    PipelineExport,
    PipelinesList,
    PipelineUpdated,
    PipelineView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create pipelines app
app = typer.Typer(
    name="pipelines",
    help="Manage pipelines",
    no_args_is_help=True,
)


def handle_pipelines_error(e: Exception) -> None:
    """Handle pipelines command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
        sys.exit(1)
    elif isinstance(e, PipelineNotFoundException | WorkspaceNotFoundException | NotFoundError):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, MultiplePipelinesFoundException | NoComputeEnvironmentException):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, InvalidResponseException):
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


def get_workspace_info(client: SeqeraClient, workspace_id: str | None = None) -> tuple:
    """Get workspace reference and workspace ID.

    Args:
        client: Seqera API client
        workspace_id: Optional workspace ID

    Returns:
        Tuple of (workspace_ref, workspace_id)
    """
    # Get user info
    user_info = client.get("/user-info")
    user = user_info.get("user", {})
    user_id = user.get("id")
    user_name = user.get("userName", "")

    if workspace_id:
        # Get workspace details
        workspaces_response = client.get(f"/user/{user_id}/workspaces")
        orgs_and_workspaces = workspaces_response.get("orgsAndWorkspaces", [])

        # Find workspace
        workspace_id_int = int(workspace_id)
        workspace_entry = None
        for entry in orgs_and_workspaces:
            if entry.get("workspaceId") == workspace_id_int:
                workspace_entry = entry
                break

        if not workspace_entry:
            raise WorkspaceNotFoundException(workspace_id)

        org_name = workspace_entry.get("orgName", "")
        workspace_name = workspace_entry.get("workspaceName", "")
        workspace_ref = f"[{org_name} / {workspace_name}]"
        return workspace_ref, workspace_id
    else:
        # Use user workspace
        workspace_ref = f"[{user_name}]"
        return workspace_ref, None


def find_pipeline(
    client: SeqeraClient,
    pipeline_name: str | None = None,
    pipeline_id: str | None = None,
    workspace_id: str | None = None,
) -> tuple:
    """Find a pipeline by name or ID.

    Args:
        client: Seqera API client
        pipeline_name: Optional pipeline name
        pipeline_id: Optional pipeline ID
        workspace_id: Optional workspace ID

    Returns:
        Tuple of (pipeline, workspace_ref)
    """
    workspace_ref, ws_id = get_workspace_info(client, workspace_id)

    if pipeline_id:
        # Get by ID
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        response = client.get(f"/pipelines/{pipeline_id}", params=params)
        pipeline = response.get("pipeline")
        if not pipeline:
            raise PipelineNotFoundException(pipeline_id, workspace_ref)
        return pipeline, workspace_ref

    elif pipeline_name:
        # Search by name
        params = {"search": f'"{pipeline_name}"'}
        if ws_id:
            params["workspaceId"] = ws_id

        response = client.get("/pipelines", params=params)
        pipelines = response.get("pipelines", [])

        if not pipelines:
            raise PipelineNotFoundException(pipeline_name, workspace_ref)

        if len(pipelines) > 1:
            raise MultiplePipelinesFoundException(pipeline_name, workspace_ref)

        return pipelines[0], workspace_ref

    else:
        raise SeqeraError("Either pipeline name or ID must be specified")


def get_compute_env(
    client: SeqeraClient,
    compute_env_name: str | None = None,
    workspace_id: str | None = None,
    use_primary: bool = True,
) -> dict | None:
    """Get compute environment by name or get primary.

    Args:
        client: Seqera API client
        compute_env_name: Optional compute environment name
        workspace_id: Optional workspace ID
        use_primary: Whether to use primary compute env if name not specified

    Returns:
        Compute environment dict or None
    """
    params = {"status": "AVAILABLE"}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/compute-envs", params=params)
    compute_envs = response.get("computeEnvs", [])

    if not compute_envs:
        workspace_ref, _ = get_workspace_info(client, workspace_id)
        raise NoComputeEnvironmentException(workspace_ref)

    if compute_env_name:
        # Find by name
        for ce in compute_envs:
            if ce.get("name") == compute_env_name:
                # Get full details
                ce_id = ce.get("id")
                ce_params = {}
                if workspace_id:
                    ce_params["workspaceId"] = workspace_id
                ce_response = client.get(f"/compute-envs/{ce_id}", params=ce_params)
                return ce_response.get("computeEnv")
        return None
    elif use_primary:
        # Get primary compute env
        for ce in compute_envs:
            if ce.get("primary"):
                ce_id = ce.get("id")
                ce_params = {}
                if workspace_id:
                    ce_params["workspaceId"] = workspace_id
                ce_response = client.get(f"/compute-envs/{ce_id}", params=ce_params)
                return ce_response.get("computeEnv")
        # If no primary, use first one
        ce_id = compute_envs[0].get("id")
        ce_params = {}
        if workspace_id:
            ce_params["workspaceId"] = workspace_id
        ce_response = client.get(f"/compute-envs/{ce_id}", params=ce_params)
        return ce_response.get("computeEnv")

    return None


@app.command("list")
def list_pipelines(
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
    offset: Annotated[
        int | None,
        typer.Option("--offset", help="Pagination offset"),
    ] = None,
    max_items: Annotated[
        int | None,
        typer.Option("--max", help="Maximum number of items to return"),
    ] = None,
    page: Annotated[
        int | None,
        typer.Option("--page", help="Page number (1-indexed)"),
    ] = None,
) -> None:
    """List all pipelines in a workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Check for conflicting pagination options
        if page is not None and offset is not None:
            raise SeqeraError("Please use either --page or --offset as pagination parameter")

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Build params
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        # Add pagination params
        pagination_info = None
        if page is not None:
            if max_items is None:
                max_items = 20
            offset = (page - 1) * max_items
            params["offset"] = offset
            params["max"] = max_items
            pagination_info = {"page": page, "max": max_items}
        elif offset is not None or max_items is not None:
            if offset is not None:
                params["offset"] = offset
            if max_items is not None:
                params["max"] = max_items
            pagination_info = {"offset": offset or 0, "max": max_items}

        # Get pipelines
        response = client.get("/pipelines", params=params)
        pipelines = response.get("pipelines", [])
        total_size = response.get("totalSize", 0)

        if pagination_info:
            pagination_info["totalSize"] = total_size

        # Output response
        result = PipelinesList(
            workspace=workspace_ref,
            pipelines=pipelines,
            pagination_info=pagination_info,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)


@app.command("view")
def view_pipeline(
    pipeline_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Pipeline name"),
    ] = None,
    pipeline_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Pipeline ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """View pipeline details."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not pipeline_name and not pipeline_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find pipeline
        pipeline, workspace_ref = find_pipeline(client, pipeline_name, pipeline_id, workspace)

        # Get launch configuration
        pipeline_id = pipeline.get("pipelineId")
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        launch_response = client.get(f"/pipelines/{pipeline_id}/launch", params=params)
        launch = launch_response.get("launch")

        # Output response
        result = PipelineView(
            workspace=workspace_ref,
            pipeline=pipeline,
            launch=launch,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)


@app.command("add")
def add_pipeline(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Pipeline name"),
    ],
    repository: Annotated[
        str,
        typer.Argument(help="Pipeline repository URL"),
    ],
    compute_env: Annotated[
        str | None,
        typer.Option("-c", "--compute-env", help="Compute environment name"),
    ] = None,
    work_dir: Annotated[
        str | None,
        typer.Option("--work-dir", help="Work directory"),
    ] = None,
    revision: Annotated[
        str | None,
        typer.Option("-r", "--revision", help="Pipeline revision"),
    ] = None,
    config_profiles: Annotated[
        str | None,
        typer.Option("--config-profiles", help="Comma-separated config profiles"),
    ] = None,
    params_file: Annotated[
        Path | None,
        typer.Option("--params-file", help="Parameters file (YAML/JSON)"),
    ] = None,
    pre_run: Annotated[
        Path | None,
        typer.Option("--pre-run", help="Pre-run script file"),
    ] = None,
    post_run: Annotated[
        Path | None,
        typer.Option("--post-run", help="Post-run script file"),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("-d", "--description", help="Pipeline description"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Add a new pipeline."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Get compute environment
        ce = get_compute_env(client, compute_env, ws_id)
        if not ce:
            raise SeqeraError(f"Compute environment '{compute_env}' not found")

        ce_id = ce.get("id")
        ce_work_dir = ce.get("config", {}).get("workDir")

        # Build launch configuration
        launch_config = {
            "computeEnvId": ce_id,
            "pipeline": repository,
        }

        # Add work directory (from compute env if not specified)
        if work_dir:
            launch_config["workDir"] = work_dir
        elif ce_work_dir:
            launch_config["workDir"] = ce_work_dir

        # Add optional fields
        if revision:
            launch_config["revision"] = revision

        if config_profiles:
            launch_config["configProfiles"] = config_profiles.split(",")

        if params_file:
            params_text = params_file.read_text()
            launch_config["paramsText"] = params_text

        if pre_run:
            launch_config["preRunScript"] = pre_run.read_text()

        if post_run:
            launch_config["postRunScript"] = post_run.read_text()

        # Build payload
        payload = {
            "name": name,
            "launch": launch_config,
        }

        if description:
            payload["description"] = description

        # Create pipeline
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        response = client.post("/pipelines", json=payload, params=params)
        pipeline = response.get("pipeline", {})
        pipeline_id = pipeline.get("pipelineId")

        # Output response
        result = PipelineAdded(
            workspace=workspace_ref,
            pipeline_name=name,
            pipeline_id=pipeline_id,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)


@app.command("delete")
def delete_pipeline(
    pipeline_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Pipeline name"),
    ] = None,
    pipeline_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Pipeline ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Delete a pipeline."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not pipeline_name and not pipeline_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find pipeline
        pipeline, workspace_ref = find_pipeline(client, pipeline_name, pipeline_id, workspace)

        # Delete pipeline
        pipeline_id = pipeline.get("pipelineId")
        name = pipeline.get("name")

        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.delete(f"/pipelines/{pipeline_id}", params=params)

        # Output response
        result = PipelineDeleted(
            pipeline_name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)


@app.command("update")
def update_pipeline(
    pipeline_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Pipeline name"),
    ] = None,
    pipeline_id: Annotated[
        str | None,
        typer.Option("--id", help="Pipeline ID"),
    ] = None,
    new_name: Annotated[
        str | None,
        typer.Option("--new-name", help="New pipeline name"),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("-d", "--description", help="Pipeline description"),
    ] = None,
    compute_env: Annotated[
        str | None,
        typer.Option("-c", "--compute-env", help="Compute environment name"),
    ] = None,
    work_dir: Annotated[
        str | None,
        typer.Option("--work-dir", help="Work directory"),
    ] = None,
    revision: Annotated[
        str | None,
        typer.Option("-r", "--revision", help="Pipeline revision"),
    ] = None,
    config_profiles: Annotated[
        str | None,
        typer.Option("--config-profiles", help="Comma-separated config profiles"),
    ] = None,
    params_file: Annotated[
        Path | None,
        typer.Option("--params-file", help="Parameters file (YAML/JSON)"),
    ] = None,
    pre_run: Annotated[
        Path | None,
        typer.Option("--pre-run", help="Pre-run script file"),
    ] = None,
    post_run: Annotated[
        Path | None,
        typer.Option("--post-run", help="Post-run script file"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Update a pipeline."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not pipeline_name and not pipeline_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find pipeline
        pipeline, workspace_ref = find_pipeline(client, pipeline_name, pipeline_id, workspace)
        pid = pipeline.get("pipelineId")
        current_name = pipeline.get("name")

        # Validate new name if provided
        if new_name and new_name != current_name:
            params = {"name": new_name}
            if workspace:
                params["workspaceId"] = workspace
            try:
                client.get("/pipelines/validate", params=params)
            except Exception:
                raise InvalidResponseException(f"Pipeline name '{new_name}' is not valid")

        # Get current launch configuration
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        launch_response = client.get(f"/pipelines/{pid}/launch", params=params)
        current_launch = launch_response.get("launch", {})

        # Build updated launch configuration
        launch_config = {
            "pipeline": current_launch.get("pipeline"),
            "pullLatest": current_launch.get("pullLatest", False),
            "stubRun": current_launch.get("stubRun", False),
        }

        # Update compute environment if specified
        if compute_env:
            ce = get_compute_env(client, compute_env, workspace)
            if not ce:
                raise SeqeraError(f"Compute environment '{compute_env}' not found")
            launch_config["computeEnvId"] = ce.get("id")
        elif current_launch.get("computeEnv"):
            launch_config["computeEnvId"] = current_launch["computeEnv"].get("id")

        # Update other fields
        if work_dir:
            launch_config["workDir"] = work_dir
        elif current_launch.get("workDir"):
            launch_config["workDir"] = current_launch["workDir"]

        if revision is not None:
            launch_config["revision"] = revision
        elif current_launch.get("revision"):
            launch_config["revision"] = current_launch["revision"]

        if config_profiles is not None:
            launch_config["configProfiles"] = config_profiles.split(",")
        elif current_launch.get("configProfiles"):
            launch_config["configProfiles"] = current_launch["configProfiles"]

        if params_file:
            launch_config["paramsText"] = params_file.read_text()
        elif current_launch.get("paramsText"):
            launch_config["paramsText"] = current_launch["paramsText"]

        if pre_run:
            launch_config["preRunScript"] = pre_run.read_text()
        elif current_launch.get("preRunScript"):
            launch_config["preRunScript"] = current_launch["preRunScript"]

        if post_run:
            launch_config["postRunScript"] = post_run.read_text()
        elif current_launch.get("postRunScript"):
            launch_config["postRunScript"] = current_launch["postRunScript"]

        # Build update payload
        payload = {
            "name": new_name if new_name else current_name,
            "launch": launch_config,
        }

        if description is not None:
            payload["description"] = description

        # Update pipeline
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/pipelines/{pid}", json=payload, params=params)

        # Output response
        result = PipelineUpdated(
            workspace=workspace_ref,
            pipeline_name=current_name,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)


@app.command("export")
def export_pipeline(
    pipeline_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Pipeline name"),
    ] = None,
    pipeline_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Pipeline ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
    output_file: Annotated[
        Path | None,
        typer.Option("-o", "--output", help="Output file path"),
    ] = None,
) -> None:
    """Export pipeline configuration."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not pipeline_name and not pipeline_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find pipeline
        pipeline, workspace_ref = find_pipeline(client, pipeline_name, pipeline_id, workspace)
        pid = pipeline.get("pipelineId")

        # Get launch configuration
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        launch_response = client.get(f"/pipelines/{pid}/launch", params=params)
        launch = launch_response.get("launch", {})

        # Build export payload (similar to import format)
        export_data = {}

        if pipeline.get("description"):
            export_data["description"] = pipeline["description"]

        if pipeline.get("icon"):
            export_data["icon"] = pipeline["icon"]

        # Build launch config for export
        launch_export = {}

        if launch.get("computeEnv"):
            launch_export["computeEnvId"] = launch["computeEnv"].get("id")

        launch_export["pipeline"] = launch.get("pipeline")

        if launch.get("workDir"):
            launch_export["workDir"] = launch["workDir"]

        if launch.get("revision"):
            launch_export["revision"] = launch["revision"]

        if launch.get("configText"):
            launch_export["configText"] = launch["configText"]

        if launch.get("seqeraConfig"):
            launch_export["seqeraConfig"] = launch["seqeraConfig"]

        if launch.get("paramsText"):
            launch_export["paramsText"] = launch["paramsText"]

        if launch.get("preRunScript"):
            launch_export["preRunScript"] = launch["preRunScript"]

        if launch.get("postRunScript"):
            launch_export["postRunScript"] = launch["postRunScript"]

        if launch.get("mainScript"):
            launch_export["mainScript"] = launch["mainScript"]

        if launch.get("entryName"):
            launch_export["entryName"] = launch["entryName"]

        if launch.get("schemaName"):
            launch_export["schemaName"] = launch["schemaName"]

        launch_export["resume"] = launch.get("resume", False)
        launch_export["pullLatest"] = launch.get("pullLatest", False)
        launch_export["stubRun"] = launch.get("stubRun", False)

        if launch.get("sessionId"):
            launch_export["sessionId"] = launch["sessionId"]

        if launch.get("runName"):
            launch_export["runName"] = launch["runName"]

        if launch.get("configProfiles"):
            launch_export["configProfiles"] = launch["configProfiles"]

        if launch.get("userSecrets"):
            launch_export["userSecrets"] = launch["userSecrets"]

        if launch.get("workspaceSecrets"):
            launch_export["workspaceSecrets"] = launch["workspaceSecrets"]

        if launch.get("optimizationId"):
            launch_export["optimizationId"] = launch["optimizationId"]

        if launch.get("optimizationTargets"):
            launch_export["optimizationTargets"] = launch["optimizationTargets"]

        if launch.get("headJobCpus"):
            launch_export["headJobCpus"] = launch["headJobCpus"]

        if launch.get("headJobMemoryMb"):
            launch_export["headJobMemoryMb"] = launch["headJobMemoryMb"]

        if launch.get("launchContainer"):
            launch_export["launchContainer"] = launch["launchContainer"]

        export_data["launch"] = launch_export

        # Format output
        config_output = json.dumps(export_data, indent=2)

        # Write to file if specified
        if output_file:
            output_file.write_text(config_output)

        # Output response
        result = PipelineExport(
            config=config_output,
            file_path=str(output_file) if output_file else None,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)


@app.command("import")
def import_pipeline(
    config_file: Annotated[
        Path,
        typer.Argument(help="Configuration file (JSON/YAML)"),
    ],
    name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Pipeline name (overrides config)"),
    ] = None,
    compute_env: Annotated[
        str | None,
        typer.Option("-c", "--compute-env", help="Compute environment name (overrides config)"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if pipeline already exists"),
    ] = False,
) -> None:
    """Import pipeline from configuration file."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Read configuration file
        config_text = config_file.read_text()
        try:
            if config_file.suffix in [".yaml", ".yml"]:
                config = yaml.safe_load(config_text)
            else:
                config = json.loads(config_text)
        except Exception as e:
            raise SeqeraError(f"Failed to parse configuration file: {e}")

        # Override name if specified
        if name:
            config["name"] = name
        elif "name" not in config:
            raise SeqeraError(
                "Pipeline name must be specified either in config file or with --name"
            )

        pipeline_name = config["name"]

        # Handle overwrite
        if overwrite:
            try:
                # Try to find and delete existing pipeline
                existing_pipeline, _ = find_pipeline(client, pipeline_name, None, ws_id)
                existing_id = existing_pipeline.get("pipelineId")
                params = {}
                if ws_id:
                    params["workspaceId"] = ws_id
                client.delete(f"/pipelines/{existing_id}", params=params)
            except PipelineNotFoundException:
                pass  # Pipeline doesn't exist, that's fine

        # Get compute environment
        launch_config = config.get("launch", {})

        if compute_env:
            # Use specified compute env
            ce = get_compute_env(client, compute_env, ws_id)
            if not ce:
                raise SeqeraError(f"Compute environment '{compute_env}' not found")
            launch_config["computeEnvId"] = ce.get("id")
        elif "computeEnvId" not in launch_config:
            # Use primary compute env
            ce = get_compute_env(client, None, ws_id)
            if not ce:
                raise NoComputeEnvironmentException(workspace_ref)
            launch_config["computeEnvId"] = ce.get("id")

        # Add work directory from compute env if not in config
        if "workDir" not in launch_config and ce:
            ce_work_dir = ce.get("config", {}).get("workDir")
            if ce_work_dir:
                launch_config["workDir"] = ce_work_dir

        config["launch"] = launch_config

        # Create pipeline
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        response = client.post("/pipelines", json=config, params=params)
        pipeline = response.get("pipeline", {})
        pipeline_id = pipeline.get("pipelineId")

        # Output response
        result = PipelineAdded(
            workspace=workspace_ref,
            pipeline_name=pipeline_name,
            pipeline_id=pipeline_id,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)
