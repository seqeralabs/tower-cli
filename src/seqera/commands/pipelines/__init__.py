"""
Pipelines commands for Seqera CLI.

Manage pipelines in workspaces.
"""

import json
import sys
from pathlib import Path
from typing import Annotated

import typer
import yaml

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
from seqera.main import get_output_format, get_sdk
from seqera.responses import (
    LabelsManaged,
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


def get_workspace_ref(sdk, workspace_id: str | None) -> str:
    """Get workspace reference string for display."""
    if not workspace_id:
        return "[user]"

    # Get workspace details from user's workspaces
    for ws in sdk.workspaces.list():
        if str(ws.workspace_id) == str(workspace_id):
            return f"[{ws.org_name} / {ws.workspace_name}]"

    return f"[workspace {workspace_id}]"


@app.command("list")
def list_pipelines(
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
    ] = None,
    show_labels: Annotated[
        bool,
        typer.Option("-l", "--labels", help="Show labels"),
    ] = False,
    filter_str: Annotated[
        str | None,
        typer.Option("-f", "--filter", help="Show only pipelines that contain the given word"),
    ] = None,
    visibility: Annotated[
        str | None,
        typer.Option("--visibility", help="Show pipelines: all, private, shared"),
    ] = None,
    page: Annotated[
        int | None,
        typer.Option("--page", help="Pages to display"),
    ] = None,
    offset: Annotated[
        int | None,
        typer.Option("--offset", help="Rows record offset"),
    ] = None,
    max_items: Annotated[
        int | None,
        typer.Option("--max", help="Maximum number of records to display"),
    ] = None,
) -> None:
    """List workspace pipelines."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Check for conflicting pagination options
        if page is not None and offset is not None:
            raise SeqeraError("Please use either --page or --offset as pagination parameter")

        # Get workspace reference for display
        workspace_ref = get_workspace_ref(sdk, workspace)

        # For CLI, we need to handle pagination manually since we want to show
        # specific pages rather than auto-iterate. Use the underlying client.
        client = sdk._http_client

        params = {}
        if workspace:
            params["workspaceId"] = workspace
        if filter_str:
            params["search"] = filter_str
        if visibility:
            params["visibility"] = visibility.upper()

        # Add pagination params
        pagination_info = None
        if page is not None:
            if max_items is None:
                max_items = 100
            computed_offset = (page - 1) * max_items
            params["offset"] = computed_offset
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
            show_labels=show_labels,
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
        sdk = get_sdk()
        output_format = get_output_format()

        if not pipeline_name and not pipeline_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Get pipeline using SDK
        if pipeline_id:
            pipeline = sdk.pipelines.get(pipeline_id, workspace=workspace)
        else:
            pipeline = sdk.pipelines.get_by_name(pipeline_name, workspace=workspace)

        # Get launch info
        launch_info = sdk.pipelines.get_launch_info(pipeline.pipeline_id, workspace=workspace)

        # Convert to dict for response formatting
        pipeline_dict = pipeline.model_dump(by_alias=True)
        launch_dict = launch_info.model_dump(by_alias=True)

        # Output response
        result = PipelineView(
            workspace=workspace_ref,
            pipeline=pipeline_dict,
            launch=launch_dict,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)


@app.command("add")
def add_pipeline(
    repository: Annotated[
        str,
        typer.Argument(help="Nextflow pipeline URL"),
    ],
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Pipeline name"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("-d", "--description", help="Pipeline description"),
    ] = None,
    labels: Annotated[
        str | None,
        typer.Option("--labels", help="Comma-separated list of labels"),
    ] = None,
    compute_env: Annotated[
        str | None,
        typer.Option("-c", "--compute-env", help="Compute environment name"),
    ] = None,
    work_dir: Annotated[
        str | None,
        typer.Option("--work-dir", help="Path where the pipeline scratch data is stored"),
    ] = None,
    profile: Annotated[
        str | None,
        typer.Option(
            "-p",
            "--profile",
            help="Comma-separated list of one or more configuration profile names you want to use for this pipeline execution",
        ),
    ] = None,
    params_file: Annotated[
        Path | None,
        typer.Option("--params-file", help="Pipeline parameters in either JSON or YML format"),
    ] = None,
    revision: Annotated[
        str | None,
        typer.Option("--revision", help="A valid repository commit Id, tag or branch name"),
    ] = None,
    config: Annotated[
        Path | None,
        typer.Option("--config", help="Path to a Nextflow config file"),
    ] = None,
    pre_run: Annotated[
        Path | None,
        typer.Option(
            "--pre-run",
            help="Bash script that is executed in the same environment where Nextflow runs just before the pipeline is launched",
        ),
    ] = None,
    post_run: Annotated[
        Path | None,
        typer.Option(
            "--post-run",
            help="Bash script that is executed in the same environment where Nextflow runs immediately after the pipeline completion",
        ),
    ] = None,
    pull_latest: Annotated[
        bool,
        typer.Option(
            "--pull-latest",
            help="Enable Nextflow to pull the latest repository version before running the pipeline",
        ),
    ] = False,
    stub_run: Annotated[
        bool,
        typer.Option(
            "--stub-run", help="Execute the workflow replacing process scripts with command stubs"
        ),
    ] = False,
    main_script: Annotated[
        str | None,
        typer.Option("--main-script", help="Pipeline main script file if different from `main.nf`"),
    ] = None,
    entry_name: Annotated[
        str | None,
        typer.Option(
            "--entry-name", help="Main workflow name to be executed when using DLS2 syntax"
        ),
    ] = None,
    schema_name: Annotated[
        str | None,
        typer.Option("--schema-name", help="Schema name"),
    ] = None,
    user_secrets: Annotated[
        str | None,
        typer.Option(
            "--user-secrets",
            help="Pipeline Secrets required by the pipeline execution that belong to the launching user personal context",
        ),
    ] = None,
    workspace_secrets: Annotated[
        str | None,
        typer.Option(
            "--workspace-secrets", help="Pipeline Secrets required by the pipeline execution"
        ),
    ] = None,
) -> None:
    """Add a workspace pipeline."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Check that compute environments exist (required by CLI policy)
        available_envs = list(sdk.compute_envs.list(workspace=workspace, status="AVAILABLE"))
        if not available_envs:
            from seqera.exceptions import NoComputeEnvironmentException

            raise NoComputeEnvironmentException(workspace_ref)

        # Read params from file if provided
        params = None
        if params_file:
            params = params_file.read_text()

        # Read config from file if provided
        config_text = None
        if config:
            config_text = config.read_text()

        # Read scripts if provided
        pre_run_script = pre_run.read_text() if pre_run else None
        post_run_script = post_run.read_text() if post_run else None

        # Parse config profiles
        profiles = profile.split(",") if profile else None

        # Build additional launch options
        extra_options = {}
        if pull_latest:
            extra_options["pullLatest"] = True
        if stub_run:
            extra_options["stubRun"] = True
        if main_script:
            extra_options["mainScript"] = main_script
        if entry_name:
            extra_options["entryName"] = entry_name
        if schema_name:
            extra_options["schemaName"] = schema_name
        if user_secrets:
            extra_options["userSecrets"] = [s.strip() for s in user_secrets.split(",")]
        if workspace_secrets:
            extra_options["workspaceSecrets"] = [s.strip() for s in workspace_secrets.split(",")]

        # Add pipeline using SDK
        pipeline = sdk.pipelines.add(
            name=name,
            repository=repository,
            workspace=workspace,
            description=description,
            compute_env=compute_env,
            work_dir=work_dir,
            revision=revision,
            config_profiles=profiles,
            params=params,
            config_text=config_text,
            pre_run_script=pre_run_script,
            post_run_script=post_run_script,
            **extra_options,
        )

        # Handle labels if specified
        if labels:
            parsed_labels = parse_labels(labels)
            if parsed_labels:
                client = sdk._http_client
                label_ids = find_or_create_label_ids(
                    client, parsed_labels, workspace, no_create=False, operation="set"
                )
                if label_ids:
                    # Apply labels to pipeline
                    request_payload = {
                        "labelIds": label_ids,
                        "pipelineIds": [pipeline.pipeline_id],
                    }
                    params = {}
                    if workspace:
                        params["workspaceId"] = workspace
                    client.post("/labels/pipelines/apply", json=request_payload, params=params)

        # Output response
        result = PipelineAdded(
            workspace=workspace_ref,
            pipeline_name=name,
            pipeline_id=pipeline.pipeline_id,
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
        sdk = get_sdk()
        output_format = get_output_format()

        if not pipeline_name and not pipeline_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Get pipeline to get its name for the response
        if pipeline_id:
            pipeline = sdk.pipelines.get(pipeline_id, workspace=workspace)
        else:
            pipeline = sdk.pipelines.get_by_name(pipeline_name, workspace=workspace)

        name = pipeline.name
        pid = pipeline.pipeline_id

        # Delete using SDK
        sdk.pipelines.delete(pid, workspace=workspace)

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
        sdk = get_sdk()
        output_format = get_output_format()

        if not pipeline_name and not pipeline_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Get pipeline to get ID if using name
        if pipeline_id:
            pipeline = sdk.pipelines.get(pipeline_id, workspace=workspace)
        else:
            pipeline = sdk.pipelines.get_by_name(pipeline_name, workspace=workspace)

        current_name = pipeline.name
        pid = pipeline.pipeline_id

        # Read params from file if provided
        params = params_file.read_text() if params_file else None

        # Read scripts if provided
        pre_run_script = pre_run.read_text() if pre_run else None
        post_run_script = post_run.read_text() if post_run else None

        # Parse config profiles
        profiles = config_profiles.split(",") if config_profiles else None

        # Update using SDK
        sdk.pipelines.update(
            pid,
            workspace=workspace,
            name=new_name,
            description=description,
            compute_env=compute_env,
            work_dir=work_dir,
            revision=revision,
            config_profiles=profiles,
            params=params,
            pre_run_script=pre_run_script,
            post_run_script=post_run_script,
        )

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
        sdk = get_sdk()
        output_format = get_output_format()

        if not pipeline_name and not pipeline_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Get pipeline
        if pipeline_id:
            pipeline = sdk.pipelines.get(pipeline_id, workspace=workspace)
        else:
            pipeline = sdk.pipelines.get_by_name(pipeline_name, workspace=workspace)

        pid = pipeline.pipeline_id

        # Export config using SDK
        export_data = sdk.pipelines.export_config(pid, workspace=workspace)

        # Add pipeline metadata
        if pipeline.description:
            export_data = {"description": pipeline.description, "launch": export_data}
        else:
            export_data = {"launch": export_data}

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
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

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

        # Handle overwrite - delete existing pipeline if it exists
        if overwrite:
            try:
                existing = sdk.pipelines.get_by_name(pipeline_name, workspace=workspace)
                sdk.pipelines.delete(existing.pipeline_id, workspace=workspace)
            except PipelineNotFoundException:
                pass  # Pipeline doesn't exist, that's fine

        # Extract launch config
        launch_config = config.get("launch", {})

        # Get repository from launch config
        repository = launch_config.get("pipeline")
        if not repository:
            raise SeqeraError("Pipeline repository must be specified in launch config")

        # Add pipeline using SDK
        pipeline = sdk.pipelines.add(
            name=pipeline_name,
            repository=repository,
            workspace=workspace,
            description=config.get("description"),
            compute_env=compute_env,
            work_dir=launch_config.get("workDir"),
            revision=launch_config.get("revision"),
            config_profiles=launch_config.get("configProfiles"),
            params=launch_config.get("paramsText"),
            pre_run_script=launch_config.get("preRunScript"),
            post_run_script=launch_config.get("postRunScript"),
        )

        # Output response
        result = PipelineAdded(
            workspace=workspace_ref,
            pipeline_name=pipeline_name,
            pipeline_id=pipeline.pipeline_id,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)


def parse_labels(labels_str: str) -> list[dict]:
    """Parse comma-separated labels into a list of dicts.

    Supports format: name=value or just name (for simple labels).
    """
    labels = []
    for label in labels_str.split(","):
        label = label.strip()
        if not label:
            continue
        if "=" in label:
            name, value = label.split("=", 1)
            labels.append({"name": name.strip(), "value": value.strip()})
        else:
            labels.append({"name": label})
    return labels


def find_or_create_label_ids(
    client,
    labels: list[dict],
    workspace_id: str | None,
    no_create: bool = False,
    operation: str = "set",
) -> list[int]:
    """Find label IDs, optionally creating labels that don't exist."""
    params = {"type": "all", "max": 1000}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/labels", params=params)
    existing_labels = response.get("labels", [])

    label_map = {}
    for existing in existing_labels:
        key = existing.get("name")
        if existing.get("value"):
            key = f"{key}={existing.get('value')}"
        label_map[key] = existing.get("id")

    label_ids = []
    for label in labels:
        name = label.get("name")
        value = label.get("value")
        key = f"{name}={value}" if value else name

        if key in label_map:
            label_ids.append(label_map[key])
        elif operation == "delete":
            continue
        elif no_create:
            raise SeqeraError(f"Label '{key}' not found and --no-create specified")
        else:
            payload = {"name": name, "resource": value is not None}
            if value:
                payload["value"] = value

            create_params = {}
            if workspace_id:
                create_params["workspaceId"] = workspace_id

            create_response = client.post("/labels", json=payload, params=create_params)
            label_ids.append(create_response.get("id"))

    return label_ids


@app.command("labels")
def manage_pipeline_labels(
    pipeline_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Pipeline name"),
    ] = None,
    pipeline_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Pipeline ID"),
    ] = None,
    labels: Annotated[
        str,
        typer.Argument(help="Comma-separated list of labels (format: name or name=value)"),
    ] = "",
    operation: Annotated[
        str,
        typer.Option("-o", "--operation", help="Operation type: set, append, or delete"),
    ] = "set",
    no_create: Annotated[
        bool,
        typer.Option("--no-create", help="Don't create labels that don't exist"),
    ] = False,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Manage labels for a pipeline."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        if not pipeline_name and not pipeline_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        if not labels:
            output_error("Labels argument is required")
            sys.exit(1)

        if operation not in ("set", "append", "delete"):
            output_error("Operation must be 'set', 'append', or 'delete'")
            sys.exit(1)

        # Get pipeline
        if pipeline_id:
            pipeline = sdk.pipelines.get(pipeline_id, workspace=workspace)
        else:
            pipeline = sdk.pipelines.get_by_name(pipeline_name, workspace=workspace)

        pid = pipeline.pipeline_id

        # Parse labels
        parsed_labels = parse_labels(labels)
        if not parsed_labels:
            output_error("No valid labels provided")
            sys.exit(1)

        # Use underlying client for labels operations
        client = sdk._http_client

        # Find or create label IDs
        label_ids = find_or_create_label_ids(client, parsed_labels, workspace, no_create, operation)

        if not label_ids:
            output_error("No labels to apply")
            sys.exit(1)

        # Build request
        request_payload = {
            "labelIds": label_ids,
            "pipelineIds": [pid],
        }

        params = {}
        if workspace:
            params["workspaceId"] = workspace

        # Apply labels based on operation
        if operation == "set":
            client.post("/labels/pipelines/apply", json=request_payload, params=params)
        elif operation == "append":
            client.post("/labels/pipelines/add", json=request_payload, params=params)
        elif operation == "delete":
            client.post("/labels/pipelines/remove", json=request_payload, params=params)

        # Output response
        result = LabelsManaged(
            operation=operation,
            entity_type="pipeline",
            entity_id=str(pid),
            workspace_id=int(workspace) if workspace else None,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_pipelines_error(e)
