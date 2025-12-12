"""
Actions commands for Seqera CLI.

Manage pipeline actions in workspaces (webhooks and triggers).
"""

import sys
from typing import Annotated, Optional

import typer

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    ActionNotFoundException,
    AuthenticationError,
    InvalidResponseException,
    SeqeraError,
)
from seqera.main import get_client, get_output_format
from seqera.responses import (
    ActionAdded,
    ActionDeleted,
    ActionsList,
    ActionUpdated,
    ActionView,
    LabelsManaged,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create actions app
app = typer.Typer(
    name="actions",
    help="Manage pipeline actions",
    no_args_is_help=True,
)


def handle_actions_error(e: Exception) -> None:
    """Handle actions command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
        sys.exit(1)
    elif isinstance(e, ActionNotFoundException):
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
            raise SeqeraError(f"Workspace not found: {workspace_id}")

        org_name = workspace_entry.get("orgName", "")
        workspace_name = workspace_entry.get("workspaceName", "")
        workspace_ref = f"[{org_name} / {workspace_name}]"
        return workspace_ref, workspace_id
    else:
        # Use user workspace
        workspace_ref = f"[{user_name}]"
        return workspace_ref, None


def find_action(
    client: SeqeraClient,
    action_name: str | None = None,
    action_id: str | None = None,
    workspace_id: str | None = None,
) -> tuple:
    """Find an action by name or ID.

    Args:
        client: Seqera API client
        action_name: Optional action name
        action_id: Optional action ID
        workspace_id: Optional workspace ID

    Returns:
        Tuple of (action, workspace_ref)
    """
    workspace_ref, ws_id = get_workspace_info(client, workspace_id)

    # Get actions list
    params = {}
    if ws_id:
        params["workspaceId"] = ws_id

    response = client.get("/actions", params=params)
    actions = response.get("actions", [])

    if not actions:
        if action_name:
            raise ActionNotFoundException(action_name, workspace_ref)
        elif action_id:
            raise ActionNotFoundException(action_id, workspace_ref)
        else:
            raise ActionNotFoundException("unknown", workspace_ref)

    # Find by ID or name
    if action_id:
        for action in actions:
            if action.get("id") == action_id:
                # Get full action details
                action_params = {}
                if ws_id:
                    action_params["workspaceId"] = ws_id
                action_response = client.get(f"/actions/{action_id}", params=action_params)
                return action_response.get("action"), workspace_ref

        raise ActionNotFoundException(action_id, workspace_ref)

    elif action_name:
        for action in actions:
            if action.get("name") == action_name:
                # Get full action details
                action_id = action.get("id")
                action_params = {}
                if ws_id:
                    action_params["workspaceId"] = ws_id
                action_response = client.get(f"/actions/{action_id}", params=action_params)
                return action_response.get("action"), workspace_ref

        raise ActionNotFoundException(action_name, workspace_ref)

    else:
        raise SeqeraError("Either action name or ID must be specified")


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
        raise SeqeraError(f"No compute environments found at {workspace_ref} workspace")

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
def list_actions(
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
) -> None:
    """List the available Pipeline Actions for the authenticated user or given workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Build params
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        # Get actions
        response = client.get("/actions", params=params)
        actions = response.get("actions", [])

        # Output response
        result = ActionsList(
            workspace=workspace_ref,
            actions=actions,
            show_labels=show_labels,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_actions_error(e)


@app.command("view")
def view_action(
    action_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Action name"),
    ] = None,
    action_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Action ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """View action details."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not action_name and not action_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find action
        action, workspace_ref = find_action(client, action_name, action_id, workspace)

        # Output response
        result = ActionView(
            action=action,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_actions_error(e)


@app.command("delete")
def delete_action(
    action_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Action name"),
    ] = None,
    action_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Action ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Delete an action."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not action_name and not action_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find action
        action, workspace_ref = find_action(client, action_name, action_id, workspace)

        # Delete action
        action_id_to_delete = action.get("id")
        name = action.get("name")

        params = {}
        if workspace:
            params["workspaceId"] = workspace

        try:
            client.delete(f"/actions/{action_id_to_delete}", params=params)
        except Exception:
            raise SeqeraError(f"Unable to delete action '{name}' for workspace '{workspace_ref}'")

        # Output response
        result = ActionDeleted(
            action_name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_actions_error(e)


# Create add subcommand group
add_app = typer.Typer(
    name="add",
    help="Add a new action",
    no_args_is_help=True,
)


@add_app.command("github")
def add_github_action(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Action name"),
    ],
    pipeline: Annotated[
        str,
        typer.Option("--pipeline", help="Pipeline to launch"),
    ],
    labels: Annotated[
        str | None,
        typer.Option("--labels", help="Comma-separated list of labels"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
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
        str | None,
        typer.Option("--params-file", help="Pipeline parameters in either JSON or YML format"),
    ] = None,
    revision: Annotated[
        str | None,
        typer.Option("--revision", help="A valid repository commit Id, tag or branch name"),
    ] = None,
    config: Annotated[
        str | None,
        typer.Option("--config", help="Path to a Nextflow config file"),
    ] = None,
    pre_run: Annotated[
        str | None,
        typer.Option(
            "--pre-run",
            help="Bash script that is executed in the same environment where Nextflow runs just before the pipeline is launched",
        ),
    ] = None,
    post_run: Annotated[
        str | None,
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
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite the action if it already exists"),
    ] = False,
) -> None:
    """Add a GitHub action."""
    from pathlib import Path

    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Handle overwrite
        if overwrite:
            try:
                # Try to find and delete existing action
                existing_action, _ = find_action(client, name, None, ws_id)
                existing_id = existing_action.get("id")
                params = {}
                if ws_id:
                    params["workspaceId"] = ws_id
                client.delete(f"/actions/{existing_id}", params=params)
            except ActionNotFoundException:
                pass  # Action doesn't exist, that's fine

        # Get compute environment
        ce = get_compute_env(client, compute_env, ws_id)
        if not ce:
            raise SeqeraError(f"Compute environment '{compute_env}' not found")

        ce_id = ce.get("id")
        ce_work_dir = ce.get("config", {}).get("workDir")

        # Build launch configuration
        launch_config = {
            "computeEnvId": ce_id,
            "pipeline": pipeline,
        }

        # Add work directory
        if work_dir:
            launch_config["workDir"] = work_dir
        elif ce_work_dir:
            launch_config["workDir"] = ce_work_dir

        # Add optional launch parameters
        if revision:
            launch_config["revision"] = revision
        if profile:
            launch_config["configProfiles"] = [p.strip() for p in profile.split(",")]
        if params_file:
            launch_config["paramsText"] = Path(params_file).read_text()
        if config:
            launch_config["configText"] = Path(config).read_text()
        if pre_run:
            launch_config["preRunScript"] = Path(pre_run).read_text()
        if post_run:
            launch_config["postRunScript"] = Path(post_run).read_text()
        if pull_latest:
            launch_config["pullLatest"] = True
        if stub_run:
            launch_config["stubRun"] = True
        if main_script:
            launch_config["mainScript"] = main_script
        if entry_name:
            launch_config["entryName"] = entry_name
        if schema_name:
            launch_config["schemaName"] = schema_name
        if user_secrets:
            launch_config["userSecrets"] = [s.strip() for s in user_secrets.split(",")]
        if workspace_secrets:
            launch_config["workspaceSecrets"] = [s.strip() for s in workspace_secrets.split(",")]

        # Build payload
        payload = {
            "name": name,
            "launch": launch_config,
            "source": "github",
        }

        # Handle labels
        if labels:
            parsed_labels = parse_labels(labels)
            if parsed_labels:
                label_ids = find_or_create_label_ids(
                    client, parsed_labels, ws_id, no_create=False, operation="set"
                )
                if label_ids:
                    payload["labelIds"] = label_ids

        # Create action
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        try:
            response = client.post("/actions", json=payload, params=params)
            action_id = response.get("actionId")
        except Exception:
            raise SeqeraError(f"Unable to add action for workspace '{workspace_ref}'")

        # Output response
        result = ActionAdded(
            action_name=name,
            workspace=workspace_ref,
            action_id=action_id,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_actions_error(e)


@add_app.command("seqera")
def add_seqera_action(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Action name"),
    ],
    pipeline: Annotated[
        str,
        typer.Option("--pipeline", help="Pipeline to launch"),
    ],
    labels: Annotated[
        str | None,
        typer.Option("--labels", help="Comma-separated list of labels"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
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
        str | None,
        typer.Option("--params-file", help="Pipeline parameters in either JSON or YML format"),
    ] = None,
    revision: Annotated[
        str | None,
        typer.Option("--revision", help="A valid repository commit Id, tag or branch name"),
    ] = None,
    config: Annotated[
        str | None,
        typer.Option("--config", help="Path to a Nextflow config file"),
    ] = None,
    pre_run: Annotated[
        str | None,
        typer.Option(
            "--pre-run",
            help="Bash script that is executed in the same environment where Nextflow runs just before the pipeline is launched",
        ),
    ] = None,
    post_run: Annotated[
        str | None,
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
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite the action if it already exists"),
    ] = False,
) -> None:
    """Add a Tower action."""
    from pathlib import Path

    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Handle overwrite
        if overwrite:
            try:
                # Try to find and delete existing action
                existing_action, _ = find_action(client, name, None, ws_id)
                existing_id = existing_action.get("id")
                params = {}
                if ws_id:
                    params["workspaceId"] = ws_id
                client.delete(f"/actions/{existing_id}", params=params)
            except ActionNotFoundException:
                pass  # Action doesn't exist, that's fine

        # Get compute environment
        ce = get_compute_env(client, compute_env, ws_id)
        if not ce:
            raise SeqeraError(f"Compute environment '{compute_env}' not found")

        ce_id = ce.get("id")
        ce_work_dir = ce.get("config", {}).get("workDir")

        # Build launch configuration
        launch_config = {
            "computeEnvId": ce_id,
            "pipeline": pipeline,
        }

        # Add work directory
        if work_dir:
            launch_config["workDir"] = work_dir
        elif ce_work_dir:
            launch_config["workDir"] = ce_work_dir

        # Add optional launch parameters
        if revision:
            launch_config["revision"] = revision
        if profile:
            launch_config["configProfiles"] = [p.strip() for p in profile.split(",")]
        if params_file:
            launch_config["paramsText"] = Path(params_file).read_text()
        if config:
            launch_config["configText"] = Path(config).read_text()
        if pre_run:
            launch_config["preRunScript"] = Path(pre_run).read_text()
        if post_run:
            launch_config["postRunScript"] = Path(post_run).read_text()
        if pull_latest:
            launch_config["pullLatest"] = True
        if stub_run:
            launch_config["stubRun"] = True
        if main_script:
            launch_config["mainScript"] = main_script
        if entry_name:
            launch_config["entryName"] = entry_name
        if schema_name:
            launch_config["schemaName"] = schema_name
        if user_secrets:
            launch_config["userSecrets"] = [s.strip() for s in user_secrets.split(",")]
        if workspace_secrets:
            launch_config["workspaceSecrets"] = [s.strip() for s in workspace_secrets.split(",")]

        # Build payload
        payload = {
            "name": name,
            "launch": launch_config,
            "source": "seqera",
        }

        # Handle labels
        if labels:
            parsed_labels = parse_labels(labels)
            if parsed_labels:
                label_ids = find_or_create_label_ids(
                    client, parsed_labels, ws_id, no_create=False, operation="set"
                )
                if label_ids:
                    payload["labelIds"] = label_ids

        # Create action
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        try:
            response = client.post("/actions", json=payload, params=params)
            action_id = response.get("actionId")
        except Exception:
            raise SeqeraError(f"Unable to add action for workspace '{workspace_ref}'")

        # Output response
        result = ActionAdded(
            action_name=name,
            workspace=workspace_ref,
            action_id=action_id,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_actions_error(e)


# Register add subcommands
app.add_typer(add_app, name="add")


@app.command("update")
def update_action(
    action_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Action unique id"),
    ] = None,
    action_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Action name"),
    ] = None,
    status: Annotated[
        str | None,
        typer.Option("-s", "--status", help="Action status (pause or active)"),
    ] = None,
    new_name: Annotated[
        str | None,
        typer.Option("--new-name", help="Action new name"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
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
        str | None,
        typer.Option("--params-file", help="Pipeline parameters in either JSON or YML format"),
    ] = None,
    revision: Annotated[
        str | None,
        typer.Option("--revision", help="A valid repository commit Id, tag or branch name"),
    ] = None,
    config: Annotated[
        str | None,
        typer.Option("--config", help="Path to a Nextflow config file"),
    ] = None,
    pre_run: Annotated[
        str | None,
        typer.Option(
            "--pre-run",
            help="Bash script that is executed in the same environment where Nextflow runs just before the pipeline is launched",
        ),
    ] = None,
    post_run: Annotated[
        str | None,
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
    """Update a Pipeline Action."""
    from pathlib import Path

    try:
        client = get_client()
        output_format = get_output_format()

        if not action_name and not action_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find action
        action, workspace_ref = find_action(client, action_name, action_id, workspace)
        aid = action.get("id")
        current_name = action.get("name")
        current_status = action.get("status")

        # Get current launch config
        launch_config = action.get("launch", {}).copy() if action.get("launch") else {}

        # Update compute environment if specified
        if compute_env:
            ws_id = workspace if workspace else None
            ce = get_compute_env(client, compute_env, ws_id)
            if ce:
                launch_config["computeEnvId"] = ce.get("id")

        # Update launch configuration
        if work_dir:
            launch_config["workDir"] = work_dir
        if revision:
            launch_config["revision"] = revision
        if profile:
            launch_config["configProfiles"] = [p.strip() for p in profile.split(",")]
        if params_file:
            launch_config["paramsText"] = Path(params_file).read_text()
        if config:
            launch_config["configText"] = Path(config).read_text()
        if pre_run:
            launch_config["preRunScript"] = Path(pre_run).read_text()
        if post_run:
            launch_config["postRunScript"] = Path(post_run).read_text()
        if pull_latest:
            launch_config["pullLatest"] = True
        if stub_run:
            launch_config["stubRun"] = True
        if main_script:
            launch_config["mainScript"] = main_script
        if entry_name:
            launch_config["entryName"] = entry_name
        if schema_name:
            launch_config["schemaName"] = schema_name
        if user_secrets:
            launch_config["userSecrets"] = [s.strip() for s in user_secrets.split(",")]
        if workspace_secrets:
            launch_config["workspaceSecrets"] = [s.strip() for s in workspace_secrets.split(",")]

        # Build update payload
        payload = {}
        if new_name and new_name != current_name:
            # Validate new name if provided
            validate_params = {"name": new_name}
            if workspace:
                validate_params["workspaceId"] = workspace
            try:
                client.get("/actions/validate", params=validate_params)
            except Exception:
                raise SeqeraError(f"Action name '{new_name}' is not valid")
            payload["name"] = new_name
        else:
            payload["name"] = current_name

        # Add launch config to payload
        if launch_config:
            payload["launch"] = launch_config

        # Update the action
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        try:
            client.put(f"/actions/{aid}", json=payload, params=params)
        except Exception:
            raise SeqeraError(
                f"Unable to update action '{current_name}' for workspace '{workspace_ref}'"
            )

        # Handle status change (pause/active) after update
        if status:
            status_upper = status.upper()

            # Check if already in that state
            if current_status == status_upper:
                raise SeqeraError(f"The action is already set to '{status_upper}'")

            # Now pause/unpause if needed
            if status_upper == "PAUSE":
                try:
                    client.post(f"/actions/{aid}/pause", params=params)
                except Exception:
                    raise SeqeraError(
                        f"An error has occur while setting the action '{current_name}' to 'PAUSE'"
                    )

        # Output response
        result = ActionUpdated(
            action_name=current_name,
            workspace=workspace_ref,
            action_id=aid,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_actions_error(e)


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
    client: SeqeraClient,
    labels: list[dict],
    workspace_id: str | None,
    no_create: bool = False,
    operation: str = "set",
) -> list[int]:
    """Find label IDs, optionally creating labels that don't exist.

    Args:
        client: Seqera API client
        labels: List of label dicts with 'name' and optional 'value'
        workspace_id: Workspace ID
        no_create: If True, fail if label doesn't exist
        operation: Operation type (set, append, delete)

    Returns:
        List of label IDs
    """
    params = {"type": "all", "max": 1000}
    if workspace_id:
        params["workspaceId"] = workspace_id

    # Get existing labels
    response = client.get("/labels", params=params)
    existing_labels = response.get("labels", [])

    # Build a lookup map
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
            # For delete, skip labels that don't exist
            continue
        elif no_create:
            raise SeqeraError(f"Label '{key}' not found and --no-create specified")
        else:
            # Create the label
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
def manage_action_labels(
    action_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Action name"),
    ] = None,
    action_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Action ID"),
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
    """Manage labels for an action."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not action_name and not action_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        if not labels:
            output_error("Labels argument is required")
            sys.exit(1)

        if operation not in ("set", "append", "delete"):
            output_error("Operation must be 'set', 'append', or 'delete'")
            sys.exit(1)

        # Find action
        action, workspace_ref = find_action(client, action_name, action_id, workspace)
        aid = action.get("id")

        # Parse labels
        parsed_labels = parse_labels(labels)
        if not parsed_labels:
            output_error("No valid labels provided")
            sys.exit(1)

        # Find or create label IDs
        label_ids = find_or_create_label_ids(client, parsed_labels, workspace, no_create, operation)

        if not label_ids:
            output_error("No labels to apply")
            sys.exit(1)

        # Build request
        request_payload = {
            "labelIds": label_ids,
            "actionIds": [aid],
        }

        params = {}
        if workspace:
            params["workspaceId"] = workspace

        # Apply labels based on operation
        if operation == "set":
            client.post("/labels/actions/apply", json=request_payload, params=params)
        elif operation == "append":
            client.post("/labels/actions/add", json=request_payload, params=params)
        elif operation == "delete":
            client.post("/labels/actions/remove", json=request_payload, params=params)

        # Output response
        result = LabelsManaged(
            operation=operation,
            entity_type="action",
            entity_id=aid,
            workspace_id=int(workspace) if workspace else None,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_actions_error(e)
