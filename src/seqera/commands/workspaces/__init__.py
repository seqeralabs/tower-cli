"""
Workspaces commands for Seqera CLI.

Manage workspaces in organizations.
"""

import sys
from typing import Annotated

import typer

from seqera.exceptions import (
    AuthenticationError,
    NotFoundError,
    OrganizationNotFoundException,
    SeqeraError,
    WorkspaceNotFoundException,
)
from seqera.main import get_sdk, get_output_format
from seqera.responses import (
    ParticipantLeft,
    WorkspaceAdded,
    WorkspaceDeleted,
    WorkspacesList,
    WorkspaceUpdated,
    WorkspaceView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create workspaces app
app = typer.Typer(
    name="workspaces",
    help="Manage workspaces",
    no_args_is_help=True,
)


def handle_workspaces_error(e: Exception) -> None:
    """Handle workspaces command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
        sys.exit(1)
    elif isinstance(e, WorkspaceNotFoundException):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, OrganizationNotFoundException):
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


@app.command("list")
def list_workspaces(
    organization: Annotated[
        str | None,
        typer.Option("-o", "--organization", help="Filter by organization name"),
    ] = None,
) -> None:
    """List all workspaces."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Get user info for display
        user_info = sdk.user_info()
        user_name = user_info.get("user", {}).get("userName", "")

        # Get workspaces using SDK
        all_workspaces = list(sdk.workspaces.list(organization=organization))

        # Filter to only entries with workspaces
        workspaces = [ws for ws in all_workspaces if ws.workspace_id is not None]

        # Convert to dicts for response formatting
        workspaces_dicts = [ws.model_dump(by_alias=True) for ws in workspaces]

        # Output response
        result = WorkspacesList(
            user_name=user_name,
            workspaces=workspaces_dicts,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)


@app.command("view")
def view_workspace(
    workspace_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Workspace ID"),
    ] = None,
    workspace_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Workspace name or organization/workspace format"),
    ] = None,
) -> None:
    """View workspace details."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        if not workspace_id and not workspace_name:
            output_error("Either --id or --name must be specified")
            sys.exit(1)

        # Get workspace using SDK
        workspace_ref = workspace_id or workspace_name
        workspace = sdk.workspaces.get(workspace_ref)

        # Convert to dict for response formatting (mode='json' to serialize datetimes)
        workspace_dict = workspace.model_dump(by_alias=True, mode="json")

        # Output response
        result = WorkspaceView(workspace=workspace_dict)
        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)


@app.command("add")
def add_workspace(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Workspace name"),
    ],
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    full_name: Annotated[
        str | None,
        typer.Option("-f", "--full-name", help="Workspace full name"),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("-d", "--description", help="Workspace description"),
    ] = None,
    visibility: Annotated[
        str | None,
        typer.Option("-v", "--visibility", help="Workspace visibility (PRIVATE, SHARED)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if workspace already exists"),
    ] = False,
) -> None:
    """Add a new workspace to an organization."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Handle overwrite - delete existing workspace if it exists
        if overwrite:
            try:
                existing = sdk.workspaces.get(f"{organization}/{name}")
                sdk.workspaces.delete(existing.id)
            except WorkspaceNotFoundException:
                pass  # Workspace doesn't exist, that's fine

        # Create workspace using SDK
        workspace = sdk.workspaces.add(
            name=name,
            organization=organization,
            full_name=full_name,
            description=description,
            visibility=visibility or "PRIVATE",
        )

        # Output response
        result = WorkspaceAdded(
            workspace_name=name,
            org_name=organization,
            visibility=workspace.visibility or visibility or "PRIVATE",
        )

        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)


@app.command("delete")
def delete_workspace(
    workspace_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Workspace ID"),
    ] = None,
    workspace_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Workspace name or organization/workspace format"),
    ] = None,
) -> None:
    """Delete a workspace."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        if not workspace_id and not workspace_name:
            output_error("Either --id or --name must be specified")
            sys.exit(1)

        workspace_ref = workspace_id or workspace_name

        # Get workspace details before deleting for response
        # Find it in the workspaces list to get org info
        ws_name = None
        org_name = None
        for ws in sdk.workspaces.list():
            if workspace_id and str(ws.workspace_id) == str(workspace_id):
                ws_name = ws.workspace_name
                org_name = ws.org_name
                break
            elif workspace_name:
                if "/" in workspace_name:
                    org, name = workspace_name.split("/", 1)
                    if ws.org_name == org and ws.workspace_name == name:
                        ws_name = ws.workspace_name
                        org_name = ws.org_name
                        break
                elif ws.workspace_name == workspace_name:
                    ws_name = ws.workspace_name
                    org_name = ws.org_name
                    break

        if not ws_name:
            raise WorkspaceNotFoundException(workspace_ref)

        # Delete workspace using SDK
        sdk.workspaces.delete(workspace_ref)

        # Output response
        result = WorkspaceDeleted(
            workspace_name=ws_name,
            org_name=org_name,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)


@app.command("update")
def update_workspace(
    workspace_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Workspace ID"),
    ] = None,
    workspace_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Workspace name or organization/workspace format"),
    ] = None,
    new_name: Annotated[
        str | None,
        typer.Option("--new-name", help="New workspace name"),
    ] = None,
    full_name: Annotated[
        str | None,
        typer.Option("-f", "--full-name", help="Workspace full name"),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("-d", "--description", help="Workspace description"),
    ] = None,
    visibility: Annotated[
        str | None,
        typer.Option("-v", "--visibility", help="Workspace visibility (PRIVATE, SHARED)"),
    ] = None,
) -> None:
    """Update a workspace."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        if not workspace_id and not workspace_name:
            output_error("Either --id or --name must be specified")
            sys.exit(1)

        workspace_ref = workspace_id or workspace_name

        # Get original workspace info for response
        ws_name = None
        org_name = None
        for ws in sdk.workspaces.list():
            if workspace_id and str(ws.workspace_id) == str(workspace_id):
                ws_name = ws.workspace_name
                org_name = ws.org_name
                break
            elif workspace_name:
                if "/" in workspace_name:
                    org, name = workspace_name.split("/", 1)
                    if ws.org_name == org and ws.workspace_name == name:
                        ws_name = ws.workspace_name
                        org_name = ws.org_name
                        break
                elif ws.workspace_name == workspace_name:
                    ws_name = ws.workspace_name
                    org_name = ws.org_name
                    break

        if not ws_name:
            raise WorkspaceNotFoundException(workspace_ref)

        # Update workspace using SDK
        updated = sdk.workspaces.update(
            workspace_ref,
            name=new_name,
            full_name=full_name,
            description=description,
            visibility=visibility,
        )

        # Output response
        result = WorkspaceUpdated(
            workspace_name=ws_name,
            org_name=org_name,
            visibility=updated.visibility or "PRIVATE",
        )

        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)


@app.command("leave")
def leave_workspace(
    workspace_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Workspace ID"),
    ] = None,
    workspace_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Workspace name or organization/workspace format"),
    ] = None,
) -> None:
    """Leave a workspace as a participant."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        if not workspace_id and not workspace_name:
            output_error("Either --id or --name must be specified")
            sys.exit(1)

        workspace_ref = workspace_id or workspace_name

        # Get workspace name for response
        ws_name = None
        for ws in sdk.workspaces.list():
            if workspace_id and str(ws.workspace_id) == str(workspace_id):
                ws_name = ws.workspace_name
                break
            elif workspace_name:
                if "/" in workspace_name:
                    org, name = workspace_name.split("/", 1)
                    if ws.org_name == org and ws.workspace_name == name:
                        ws_name = ws.workspace_name
                        break
                elif ws.workspace_name == workspace_name:
                    ws_name = ws.workspace_name
                    break

        if not ws_name:
            raise WorkspaceNotFoundException(workspace_ref)

        # Leave workspace using SDK
        sdk.workspaces.leave(workspace_ref)

        # Output response
        result = ParticipantLeft(workspace_name=ws_name)
        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)
