"""
Studios commands for Seqera CLI.

Manage data studios in workspaces.
"""

import sys
from typing import Annotated, Optional

import typer

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    InvalidResponseException,
    NotFoundError,
    SeqeraError,
    WorkspaceNotFoundException,
)
from seqera.main import get_client, get_output_format
from seqera.responses import (
    StudioCheckpoints,
    StudioDeleted,
    StudiosList,
    StudioStarted,
    StudioStopped,
    StudioView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create studios app
app = typer.Typer(
    name="studios",
    help="Manage data studios",
    no_args_is_help=True,
)


def handle_studios_error(e: Exception) -> None:
    """Handle studios command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
        sys.exit(1)
    elif isinstance(e, WorkspaceNotFoundException | NotFoundError):
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


def find_studio(
    client: SeqeraClient,
    studio_name: str | None = None,
    studio_id: str | None = None,
    workspace_id: str | None = None,
) -> tuple:
    """Find a studio by name or ID.

    Args:
        client: Seqera API client
        studio_name: Optional studio name
        studio_id: Optional studio ID
        workspace_id: Optional workspace ID

    Returns:
        Tuple of (studio, workspace_ref)
    """
    workspace_ref, ws_id = get_workspace_info(client, workspace_id)

    if studio_id:
        # Get by ID
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        response = client.get(f"/studios/{studio_id}", params=params)
        studio = response.get("studio")
        if not studio:
            raise NotFoundError(f"Studio '{studio_id}' not found in {workspace_ref}")
        return studio, workspace_ref

    elif studio_name:
        # Search by name
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        response = client.get("/studios", params=params)
        studios = response.get("studios", [])

        # Find by name
        matching_studios = [s for s in studios if s.get("name") == studio_name]

        if not matching_studios:
            raise NotFoundError(f"Studio '{studio_name}' not found in {workspace_ref}")

        if len(matching_studios) > 1:
            raise SeqeraError(
                f"Multiple studios found with name '{studio_name}' in {workspace_ref}"
            )

        return matching_studios[0], workspace_ref

    else:
        raise SeqeraError("Either studio name or ID must be specified")


@app.command("list")
def list_studios(
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
    filter: Annotated[
        str | None,
        typer.Option("--filter", help="Filter studios by search criteria"),
    ] = None,
    labels: Annotated[
        bool,
        typer.Option("--labels", help="Show labels in output"),
    ] = False,
) -> None:
    """List all studios in a workspace."""
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

        # Add filter
        if filter:
            params["search"] = filter

        # Add labels attribute
        if labels:
            params["attributes"] = "labels"

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

        # Get studios
        response = client.get("/studios", params=params)
        studios = response.get("studios", [])
        total_size = response.get("totalSize", 0)

        if pagination_info:
            pagination_info["totalSize"] = total_size

        # Output response
        result = StudiosList(
            workspace=workspace_ref,
            studios=studios,
            show_labels=labels,
            pagination_info=pagination_info,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("view")
def view_studio(
    studio_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Studio name"),
    ] = None,
    studio_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Studio ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """View studio details."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not studio_name and not studio_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find studio
        studio, workspace_ref = find_studio(client, studio_name, studio_id, workspace)

        # Output response
        result = StudioView(
            workspace=workspace_ref,
            studio=studio,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("start")
def start_studio(
    studio_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Studio name"),
    ] = None,
    studio_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Studio ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
    cpu: Annotated[
        int | None,
        typer.Option("--cpu", help="Number of CPUs"),
    ] = None,
    memory: Annotated[
        int | None,
        typer.Option("--memory", help="Memory in MB"),
    ] = None,
    gpu: Annotated[
        int | None,
        typer.Option("--gpu", help="Number of GPUs"),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("--description", help="Studio description"),
    ] = None,
) -> None:
    """Start a studio session."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not studio_name and not studio_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find studio
        studio, workspace_ref = find_studio(client, studio_name, studio_id, workspace)

        # Get studio ID
        session_id = studio.get("sessionId")
        studio_display_name = studio_name if studio_name else studio_id

        # Build start payload with existing configuration
        configuration = studio.get("configuration", {})
        payload_config = {
            "gpu": gpu if gpu is not None else configuration.get("gpu", 0),
            "cpu": cpu if cpu is not None else configuration.get("cpu", 2),
            "memory": memory if memory is not None else configuration.get("memory", 8192),
        }

        # Add mount data if exists
        mount_data = configuration.get("mountData")
        if mount_data:
            payload_config["mountData"] = mount_data

        payload = {
            "configuration": payload_config,
        }

        # Add description
        if description:
            payload["description"] = description
        elif studio.get("description"):
            payload["description"] = studio.get("description")

        # Start studio
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        response = client.put(f"/studios/{session_id}/start", json=payload, params=params)
        job_submitted = response.get("jobSubmitted", False)

        # Output response
        result = StudioStarted(
            session_id=session_id,
            studio_name=studio_display_name,
            workspace=workspace_ref,
            workspace_id=int(workspace) if workspace else None,
            job_submitted=job_submitted,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("stop")
def stop_studio(
    studio_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Studio name"),
    ] = None,
    studio_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Studio ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Stop a studio session."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not studio_name and not studio_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find studio if needed
        if studio_name:
            studio, workspace_ref = find_studio(client, studio_name, None, workspace)
            session_id = studio.get("sessionId")
            studio_display_name = studio_name
        else:
            workspace_ref, _ = get_workspace_info(client, workspace)
            session_id = studio_id
            studio_display_name = studio_id

        # Stop studio
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        response = client.put(f"/studios/{session_id}/stop", params=params)
        job_submitted = response.get("jobSubmitted", False)

        # Output response
        result = StudioStopped(
            session_id=session_id,
            studio_name=studio_display_name,
            workspace=workspace_ref,
            workspace_id=int(workspace) if workspace else None,
            job_submitted=job_submitted,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("delete")
def delete_studio(
    studio_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Studio name"),
    ] = None,
    studio_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Studio ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Delete a studio."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not studio_name and not studio_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find studio if needed
        if studio_name:
            studio, workspace_ref = find_studio(client, studio_name, None, workspace)
            session_id = studio.get("sessionId")
        else:
            workspace_ref, _ = get_workspace_info(client, workspace)
            session_id = studio_id

        # Delete studio
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.delete(f"/studios/{session_id}", params=params)

        # Output response
        result = StudioDeleted(
            session_id=session_id,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("checkpoints")
def list_checkpoints(
    studio_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Studio ID"),
    ],
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
) -> None:
    """List checkpoints for a studio."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Build params
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id
        if offset is not None:
            params["offset"] = offset
        if max_items is not None:
            params["max"] = max_items

        # Get checkpoints
        response = client.get(f"/studios/{studio_id}/checkpoints", params=params)
        checkpoints = response.get("checkpoints", [])
        total_size = response.get("totalSize", 0)

        pagination_info = None
        if offset is not None or max_items is not None:
            pagination_info = {"offset": offset or 0, "max": max_items, "totalSize": total_size}

        # Output response
        result = StudioCheckpoints(
            session_id=studio_id,
            workspace=workspace_ref,
            checkpoints=checkpoints,
            pagination_info=pagination_info,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)
