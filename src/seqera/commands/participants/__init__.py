"""
Participants commands for Seqera CLI.

Manage workspace participants (members and teams).
"""

import sys
from typing import Annotated, Optional

import typer

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
    ParticipantAdded,
    ParticipantDeleted,
    ParticipantLeft,
    ParticipantsList,
    ParticipantUpdated,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create participants app
app = typer.Typer(
    name="participants",
    help="Manage workspace participants",
    no_args_is_help=True,
)


def handle_participants_error(e: Exception) -> None:
    """Handle participants command errors."""
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


def find_workspace_by_id(orgs_and_workspaces: list, workspace_id: str) -> dict | None:
    """Find workspace by ID in orgs and workspaces list."""
    workspace_id_int = int(workspace_id)
    for entry in orgs_and_workspaces:
        if entry.get("workspaceId") == workspace_id_int:
            return entry
    return None


def find_participant_by_name(participants: list, name: str, participant_type: str) -> dict | None:
    """Find participant by name and type."""
    for participant in participants:
        if participant_type == "MEMBER":
            if participant.get("userName") == name and participant.get("type") == "MEMBER":
                return participant
        elif participant_type == "TEAM":
            if participant.get("teamName") == name and participant.get("type") == "TEAM":
                return participant
    return None


def find_member_by_username(members: list, username: str) -> dict | None:
    """Find member by username in members list."""
    for member in members:
        if member.get("userName") == username:
            return member
    return None


@app.command("list")
def list_participants(
    workspace_id: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    participant_type: Annotated[
        str | None,
        typer.Option("-t", "--type", help="Filter by type: MEMBER or TEAM"),
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
        typer.Option("--page", help="Page number (alternative to offset)"),
    ] = None,
) -> None:
    """List workspace participants."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Check for conflicting pagination parameters
        if page is not None and offset is not None:
            output_error("Please use either --page or --offset as pagination parameter")
            sys.exit(1)

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find workspace
        workspace_entry = find_workspace_by_id(orgs_and_workspaces, workspace_id)
        if not workspace_entry:
            raise WorkspaceNotFoundException(workspace_id)

        org_id = workspace_entry.get("orgId")
        ws_id = workspace_entry.get("workspaceId")
        ws_name = workspace_entry.get("workspaceName")
        org_name = workspace_entry.get("orgName")

        # Build query parameters
        params = {}
        if page is not None and max_items is not None:
            # Convert page to offset (page starts at 1)
            params["offset"] = (page - 1) * max_items
            params["max"] = max_items
        elif offset is not None or max_items is not None:
            if offset is not None:
                params["offset"] = offset
            if max_items is not None:
                params["max"] = max_items

        # Get participants
        participants_response = client.get(
            f"/orgs/{org_id}/workspaces/{ws_id}/participants", params=params
        )
        participants = participants_response.get("participants", [])

        # Filter by type if specified
        if participant_type:
            participants = [p for p in participants if p.get("type") == participant_type]

        # Output response
        result = ParticipantsList(
            org_name=org_name,
            workspace_name=ws_name,
            participants=participants,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_participants_error(e)


@app.command("add")
def add_participant(
    workspace_id: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Member username or team name"),
    ],
    participant_type: Annotated[
        str,
        typer.Option("-t", "--type", help="Participant type: MEMBER or TEAM"),
    ],
    role: Annotated[
        str | None,
        typer.Option(
            "-r", "--role", help="Workspace role (OWNER, ADMIN, MAINTAIN, LAUNCH, CONNECT, VIEW)"
        ),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if participant already exists"),
    ] = False,
) -> None:
    """Add a participant to a workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find workspace
        workspace_entry = find_workspace_by_id(orgs_and_workspaces, workspace_id)
        if not workspace_entry:
            raise WorkspaceNotFoundException(workspace_id)

        org_id = workspace_entry.get("orgId")
        ws_id = workspace_entry.get("workspaceId")
        ws_name = workspace_entry.get("workspaceName")
        workspace_entry.get("orgName")

        # If overwrite is True, check for existing participants and delete them
        if overwrite:
            # Get all participants
            participants_response = client.get(f"/orgs/{org_id}/workspaces/{ws_id}/participants")
            existing_participants = participants_response.get("participants", [])

            # Check if participant already exists
            existing = find_participant_by_name(existing_participants, name, participant_type)
            if existing:
                participant_id = existing.get("participantId")
                client.delete(f"/orgs/{org_id}/workspaces/{ws_id}/participants/{participant_id}")

        # Find member or team ID
        if participant_type == "MEMBER":
            # Get organization members
            members_response = client.get(f"/orgs/{org_id}/members")
            members = members_response.get("members", [])
            member = find_member_by_username(members, name)
            if not member:
                raise NotFoundError(f"Member '{name}' not found in organization")
            member_id = member.get("memberId")
            team_id = None
        elif participant_type == "TEAM":
            # For teams, we need the team ID
            # Get organization teams (this would need to be implemented)
            raise NotImplementedError("Team participant addition not yet implemented")
        else:
            output_error(f"Invalid participant type: {participant_type}")
            sys.exit(1)

        # Build add participant payload
        payload = {
            "memberId": member_id if participant_type == "MEMBER" else None,
            "teamId": team_id if participant_type == "TEAM" else None,
            "userNameOrEmail": name if participant_type == "MEMBER" else None,
            "teamName": name if participant_type == "TEAM" else None,
        }

        if role:
            payload["wspRole"] = role

        # Add participant
        response = client.put(f"/orgs/{org_id}/workspaces/{ws_id}/participants/add", json=payload)
        participant = response.get("participant", {})

        # Output response
        result = ParticipantAdded(
            participant=participant,
            workspace_name=ws_name,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_participants_error(e)


@app.command("delete")
def delete_participant(
    workspace_id: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Member username or team name"),
    ],
    participant_type: Annotated[
        str,
        typer.Option("-t", "--type", help="Participant type: MEMBER or TEAM"),
    ],
) -> None:
    """Delete a participant from a workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find workspace
        workspace_entry = find_workspace_by_id(orgs_and_workspaces, workspace_id)
        if not workspace_entry:
            raise WorkspaceNotFoundException(workspace_id)

        org_id = workspace_entry.get("orgId")
        ws_id = workspace_entry.get("workspaceId")
        ws_name = workspace_entry.get("workspaceName")
        workspace_entry.get("orgName")

        # Get participants with search filter
        participants_response = client.get(
            f"/orgs/{org_id}/workspaces/{ws_id}/participants", params={"search": name}
        )
        participants = participants_response.get("participants", [])

        # Find participant
        participant = find_participant_by_name(participants, name, participant_type)
        if not participant:
            raise NotFoundError(
                f"Participant '{name}' of type {participant_type} not found in workspace"
            )

        participant_id = participant.get("participantId")

        # Delete participant
        client.delete(f"/orgs/{org_id}/workspaces/{ws_id}/participants/{participant_id}")

        # Output response
        result = ParticipantDeleted(
            name=name,
            workspace_name=ws_name,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_participants_error(e)


@app.command("update")
def update_participant(
    workspace_id: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Member username or team name"),
    ],
    role: Annotated[
        str,
        typer.Option(
            "-r",
            "--role",
            help="New workspace role (OWNER, ADMIN, MAINTAIN, LAUNCH, CONNECT, VIEW)",
        ),
    ],
    participant_type: Annotated[
        str,
        typer.Option("-t", "--type", help="Participant type: MEMBER or TEAM"),
    ],
) -> None:
    """Update a participant's role in a workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find workspace
        workspace_entry = find_workspace_by_id(orgs_and_workspaces, workspace_id)
        if not workspace_entry:
            raise WorkspaceNotFoundException(workspace_id)

        org_id = workspace_entry.get("orgId")
        ws_id = workspace_entry.get("workspaceId")
        ws_name = workspace_entry.get("workspaceName")
        workspace_entry.get("orgName")

        # Get participants with search filter
        participants_response = client.get(
            f"/orgs/{org_id}/workspaces/{ws_id}/participants", params={"search": name}
        )
        participants = participants_response.get("participants", [])

        # Find participant
        participant = find_participant_by_name(participants, name, participant_type)
        if not participant:
            raise NotFoundError(
                f"Participant '{name}' of type {participant_type} not found in workspace"
            )

        participant_id = participant.get("participantId")

        # Update participant role
        payload = {"role": role}
        client.put(
            f"/orgs/{org_id}/workspaces/{ws_id}/participants/{participant_id}/role",
            json=payload,
        )

        # Output response
        result = ParticipantUpdated(
            workspace_name=ws_name,
            name=name,
            role=role,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_participants_error(e)


@app.command("leave")
def leave_workspace(
    workspace_id: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
) -> None:
    """Leave a workspace as a participant."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find workspace
        workspace_entry = find_workspace_by_id(orgs_and_workspaces, workspace_id)
        if not workspace_entry:
            raise WorkspaceNotFoundException(workspace_id)

        org_id = workspace_entry.get("orgId")
        ws_id = workspace_entry.get("workspaceId")
        ws_name = workspace_entry.get("workspaceName")

        # Leave workspace (delete participant)
        client.delete(f"/orgs/{org_id}/workspaces/{ws_id}/participants")

        # Output response
        result = ParticipantLeft(workspace_name=ws_name)
        output_response(result, output_format)

    except Exception as e:
        handle_participants_error(e)
