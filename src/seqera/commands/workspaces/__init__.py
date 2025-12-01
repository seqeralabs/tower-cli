"""
Workspaces commands for Seqera CLI.

Manage workspaces in organizations.
"""

import sys
from typing import Optional

import typer
from typing_extensions import Annotated

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    NotFoundError,
    OrganizationNotFoundException,
    SeqeraError,
    WorkspaceNotFoundException,
)
from seqera.main import get_client, get_output_format
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


def get_user_workspaces(client: SeqeraClient) -> tuple:
    """Get user info and workspaces list.

    Returns:
        tuple: (user_name, orgs_and_workspaces list)
    """
    # Get user info
    user_info = client.get("/user-info")
    user = user_info.get("user", {})
    user_name = user.get("userName", "")
    user_id = user.get("id")

    # Get workspaces
    workspaces_response = client.get(f"/user/{user_id}/workspaces")
    orgs_and_workspaces = workspaces_response.get("orgsAndWorkspaces", [])

    return user_name, orgs_and_workspaces


def find_workspace_by_id(orgs_and_workspaces: list, workspace_id: str) -> Optional[dict]:
    """Find workspace by ID in orgs and workspaces list.

    Args:
        orgs_and_workspaces: List of org/workspace entries
        workspace_id: Workspace ID to find

    Returns:
        Workspace entry if found, None otherwise
    """
    workspace_id_int = int(workspace_id)
    for entry in orgs_and_workspaces:
        if entry.get("workspaceId") == workspace_id_int:
            return entry
    return None


def find_workspace_by_name(orgs_and_workspaces: list, workspace_name: str, org_name: Optional[str] = None) -> Optional[dict]:
    """Find workspace by name (and optionally org name) in orgs and workspaces list.

    Args:
        orgs_and_workspaces: List of org/workspace entries
        workspace_name: Workspace name to find
        org_name: Optional organization name to filter by

    Returns:
        Workspace entry if found, None otherwise
    """
    for entry in orgs_and_workspaces:
        if entry.get("workspaceName") == workspace_name:
            if org_name is None or entry.get("orgName") == org_name:
                return entry
    return None


def find_org_by_name(orgs_and_workspaces: list, org_name: str) -> Optional[dict]:
    """Find organization by name in orgs and workspaces list.

    Args:
        orgs_and_workspaces: List of org/workspace entries
        org_name: Organization name to find

    Returns:
        Organization entry if found, None otherwise
    """
    for entry in orgs_and_workspaces:
        if entry.get("orgName") == org_name and entry.get("workspaceId") is None:
            return entry
    return None


@app.command("list")
def list_workspaces(
    organization: Annotated[
        Optional[str],
        typer.Option("-o", "--organization", help="Filter by organization name"),
    ] = None,
) -> None:
    """List all workspaces."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Filter to only entries with workspaces
        workspaces = [
            entry for entry in orgs_and_workspaces
            if entry.get("workspaceId") is not None
        ]

        # Filter by organization if specified
        if organization:
            workspaces = [
                ws for ws in workspaces
                if ws.get("orgName") == organization
            ]

        # Output response
        result = WorkspacesList(
            user_name=user_name,
            workspaces=workspaces,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)


@app.command("view")
def view_workspace(
    workspace_id: Annotated[
        Optional[str],
        typer.Option("-i", "--id", help="Workspace ID"),
    ] = None,
    workspace_name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Workspace name or organization/workspace format"),
    ] = None,
) -> None:
    """View workspace details."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find workspace
        workspace_entry = None
        if workspace_id:
            workspace_entry = find_workspace_by_id(orgs_and_workspaces, workspace_id)
            if not workspace_entry:
                raise WorkspaceNotFoundException(workspace_id)
        elif workspace_name:
            # Check if name is in org/workspace format
            if "/" in workspace_name:
                org_name, ws_name = workspace_name.split("/", 1)
                workspace_entry = find_workspace_by_name(orgs_and_workspaces, ws_name, org_name)
            else:
                workspace_entry = find_workspace_by_name(orgs_and_workspaces, workspace_name)

            if not workspace_entry:
                raise WorkspaceNotFoundException(workspace_name)
        else:
            output_error("Either --id or --name must be specified")
            sys.exit(1)

        # Get workspace details
        org_id = workspace_entry.get("orgId")
        ws_id = workspace_entry.get("workspaceId")

        workspace_response = client.get(f"/orgs/{org_id}/workspaces/{ws_id}")
        workspace = workspace_response.get("workspace", {})

        # Output response
        result = WorkspaceView(workspace=workspace)
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
        Optional[str],
        typer.Option("-f", "--full-name", help="Workspace full name"),
    ] = None,
    description: Annotated[
        Optional[str],
        typer.Option("-d", "--description", help="Workspace description"),
    ] = None,
    visibility: Annotated[
        Optional[str],
        typer.Option("-v", "--visibility", help="Workspace visibility (PRIVATE, SHARED)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if workspace already exists"),
    ] = False,
) -> None:
    """Add a new workspace to an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find organization
        org_entry = find_org_by_name(orgs_and_workspaces, organization)
        if not org_entry:
            raise OrganizationNotFoundException(organization)

        org_id = org_entry.get("orgId")

        # Check if workspace already exists with overwrite
        if overwrite:
            existing_workspace = find_workspace_by_name(orgs_and_workspaces, name, organization)
            if existing_workspace:
                # Delete existing workspace
                existing_ws_id = existing_workspace.get("workspaceId")
                client.delete(f"/orgs/{org_id}/workspaces/{existing_ws_id}")

        # Validate workspace name
        client.get(f"/orgs/{org_id}/workspaces/validate", params={"name": name})

        # Build workspace payload
        payload = {
            "workspace": {
                "name": name,
            }
        }

        if full_name:
            payload["workspace"]["fullName"] = full_name
        if description:
            payload["workspace"]["description"] = description
        if visibility:
            payload["workspace"]["visibility"] = visibility

        # Create workspace
        response = client.post(f"/orgs/{org_id}/workspaces", json=payload)
        workspace = response.get("workspace", {})

        # Output response
        result = WorkspaceAdded(
            workspace_name=name,
            org_name=organization,
            visibility=workspace.get("visibility", visibility or "PRIVATE"),
        )

        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)


@app.command("delete")
def delete_workspace(
    workspace_id: Annotated[
        Optional[str],
        typer.Option("-i", "--id", help="Workspace ID"),
    ] = None,
    workspace_name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Workspace name or organization/workspace format"),
    ] = None,
) -> None:
    """Delete a workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find workspace
        workspace_entry = None
        if workspace_id:
            workspace_entry = find_workspace_by_id(orgs_and_workspaces, workspace_id)
            if not workspace_entry:
                raise WorkspaceNotFoundException(workspace_id)
        elif workspace_name:
            # Check if name is in org/workspace format
            if "/" in workspace_name:
                org_name, ws_name = workspace_name.split("/", 1)
                workspace_entry = find_workspace_by_name(orgs_and_workspaces, ws_name, org_name)
            else:
                workspace_entry = find_workspace_by_name(orgs_and_workspaces, workspace_name)

            if not workspace_entry:
                raise WorkspaceNotFoundException(workspace_name)
        else:
            output_error("Either --id or --name must be specified")
            sys.exit(1)

        # Delete workspace
        org_id = workspace_entry.get("orgId")
        ws_id = workspace_entry.get("workspaceId")
        ws_name = workspace_entry.get("workspaceName")
        org_name = workspace_entry.get("orgName")

        client.delete(f"/orgs/{org_id}/workspaces/{ws_id}")

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
        Optional[str],
        typer.Option("-i", "--id", help="Workspace ID"),
    ] = None,
    workspace_name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Workspace name or organization/workspace format"),
    ] = None,
    new_name: Annotated[
        Optional[str],
        typer.Option("--new-name", help="New workspace name"),
    ] = None,
    full_name: Annotated[
        Optional[str],
        typer.Option("-f", "--full-name", help="Workspace full name"),
    ] = None,
    description: Annotated[
        Optional[str],
        typer.Option("-d", "--description", help="Workspace description"),
    ] = None,
    visibility: Annotated[
        Optional[str],
        typer.Option("-v", "--visibility", help="Workspace visibility (PRIVATE, SHARED)"),
    ] = None,
) -> None:
    """Update a workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find workspace
        workspace_entry = None
        if workspace_id:
            workspace_entry = find_workspace_by_id(orgs_and_workspaces, workspace_id)
            if not workspace_entry:
                raise WorkspaceNotFoundException(workspace_id)
        elif workspace_name:
            # Check if name is in org/workspace format
            if "/" in workspace_name:
                org_name, ws_name = workspace_name.split("/", 1)
                workspace_entry = find_workspace_by_name(orgs_and_workspaces, ws_name, org_name)
            else:
                workspace_entry = find_workspace_by_name(orgs_and_workspaces, workspace_name)

            if not workspace_entry:
                raise WorkspaceNotFoundException(workspace_name)
        else:
            output_error("Either --id or --name must be specified")
            sys.exit(1)

        # Get current workspace details
        org_id = workspace_entry.get("orgId")
        ws_id = workspace_entry.get("workspaceId")
        ws_name = workspace_entry.get("workspaceName")
        org_name = workspace_entry.get("orgName")

        workspace_response = client.get(f"/orgs/{org_id}/workspaces/{ws_id}")
        current_workspace = workspace_response.get("workspace", {})

        # Build update payload with current values as defaults
        payload = {
            "workspace": {
                "name": new_name if new_name else current_workspace.get("name"),
                "fullName": full_name if full_name else current_workspace.get("fullName"),
                "description": description if description else current_workspace.get("description"),
                "visibility": visibility if visibility else current_workspace.get("visibility"),
            }
        }

        # Update workspace
        client.put(f"/orgs/{org_id}/workspaces/{ws_id}", json=payload)

        # Output response (use original workspace name for response)
        result = WorkspaceUpdated(
            workspace_name=ws_name,
            org_name=org_name,
            visibility=payload["workspace"]["visibility"],
        )

        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)


@app.command("leave")
def leave_workspace(
    workspace_id: Annotated[
        Optional[str],
        typer.Option("-i", "--id", help="Workspace ID"),
    ] = None,
    workspace_name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Workspace name or organization/workspace format"),
    ] = None,
) -> None:
    """Leave a workspace as a participant."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find workspace
        workspace_entry = None
        if workspace_id:
            workspace_entry = find_workspace_by_id(orgs_and_workspaces, workspace_id)
            if not workspace_entry:
                raise WorkspaceNotFoundException(workspace_id)
        elif workspace_name:
            # Check if name is in org/workspace format
            if "/" in workspace_name:
                org_name, ws_name = workspace_name.split("/", 1)
                workspace_entry = find_workspace_by_name(orgs_and_workspaces, ws_name, org_name)
            else:
                workspace_entry = find_workspace_by_name(orgs_and_workspaces, workspace_name)

            if not workspace_entry:
                raise WorkspaceNotFoundException(workspace_name)
        else:
            output_error("Either --id or --name must be specified")
            sys.exit(1)

        # Leave workspace (delete participant)
        org_id = workspace_entry.get("orgId")
        ws_id = workspace_entry.get("workspaceId")
        ws_name = workspace_entry.get("workspaceName")

        client.delete(f"/orgs/{org_id}/workspaces/{ws_id}/participants")

        # Output response
        result = ParticipantLeft(workspace_name=ws_name)
        output_response(result, output_format)

    except Exception as e:
        handle_workspaces_error(e)
