"""
Collaborators commands for Seqera CLI.

Manage organization collaborators (external users who can be added to workspaces).
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
)
from seqera.main import get_client, get_output_format
from seqera.responses import (
    CollaboratorAdded,
    CollaboratorDeleted,
    CollaboratorsList,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create collaborators app
app = typer.Typer(
    name="collaborators",
    help="Manage organization collaborators",
    no_args_is_help=True,
)


def handle_collaborators_error(e: Exception) -> None:
    """Handle collaborators command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
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


def find_organization_by_id(orgs_and_workspaces: list, org_id: str) -> Optional[dict]:
    """Find organization by ID in orgs and workspaces list."""
    org_id_int = int(org_id)
    for entry in orgs_and_workspaces:
        if entry.get("orgId") == org_id_int and entry.get("workspaceId") is None:
            return entry
    return None


def find_collaborator_by_username(collaborators: list, username: str) -> Optional[dict]:
    """Find collaborator by username in collaborators list."""
    for collaborator in collaborators:
        if collaborator.get("userName") == username:
            return collaborator
    return None


@app.command("list")
def list_collaborators(
    organization_id: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization ID"),
    ],
    filter_text: Annotated[
        Optional[str],
        typer.Option("-f", "--filter", help="Filter collaborators by username prefix"),
    ] = None,
    offset: Annotated[
        Optional[int],
        typer.Option("--offset", help="Pagination offset"),
    ] = None,
    max_items: Annotated[
        Optional[int],
        typer.Option("--max", help="Maximum number of items to return"),
    ] = None,
) -> None:
    """List organization collaborators."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces to validate organization
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find organization
        org_entry = find_organization_by_id(orgs_and_workspaces, organization_id)
        if not org_entry:
            raise OrganizationNotFoundException(organization_id)

        org_id = org_entry.get("orgId")

        # Build query parameters
        params = {}
        # Default pagination values (matching Java implementation)
        params["max"] = max_items if max_items is not None else 100
        params["offset"] = offset if offset is not None else 0

        if filter_text:
            params["search"] = filter_text

        # Get collaborators
        response = client.get(f"/orgs/{org_id}/collaborators", params=params)
        collaborators = response.get("members", [])
        total_size = response.get("totalSize", len(collaborators))

        # Output response
        pagination_info = {
            "offset": params["offset"],
            "max": params["max"],
            "totalSize": total_size,
        }

        result = CollaboratorsList(
            org_id=org_id,
            collaborators=collaborators,
            pagination_info=pagination_info,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_collaborators_error(e)


@app.command("add")
def add_collaborator(
    organization_id: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization ID"),
    ],
    username: Annotated[
        str,
        typer.Option("-u", "--user", help="Username or email of the collaborator to add"),
    ],
) -> None:
    """Add a collaborator to an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces to validate organization
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find organization
        org_entry = find_organization_by_id(orgs_and_workspaces, organization_id)
        if not org_entry:
            raise OrganizationNotFoundException(organization_id)

        org_id = org_entry.get("orgId")

        # Build add collaborator payload
        payload = {
            "userNameOrEmail": username,
        }

        # Add collaborator
        response = client.put(f"/orgs/{org_id}/collaborators/add", json=payload)
        member = response.get("member", {})

        # Output response
        result = CollaboratorAdded(
            org_id=org_id,
            user_name=member.get("userName", username),
            email=member.get("email"),
        )

        output_response(result, output_format)

    except Exception as e:
        handle_collaborators_error(e)


@app.command("delete")
def delete_collaborator(
    organization_id: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization ID"),
    ],
    username: Annotated[
        str,
        typer.Option("-u", "--user", help="Username of the collaborator to delete"),
    ],
) -> None:
    """Delete a collaborator from an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get user workspaces to validate organization
        user_name, orgs_and_workspaces = get_user_workspaces(client)

        # Find organization
        org_entry = find_organization_by_id(orgs_and_workspaces, organization_id)
        if not org_entry:
            raise OrganizationNotFoundException(organization_id)

        org_id = org_entry.get("orgId")

        # Get collaborators with search filter to find the member ID
        params = {"search": username}
        response = client.get(f"/orgs/{org_id}/collaborators", params=params)
        collaborators = response.get("members", [])

        # Find collaborator
        collaborator = find_collaborator_by_username(collaborators, username)
        if not collaborator:
            raise NotFoundError(f"Collaborator '{username}' not found in organization")

        member_id = collaborator.get("memberId")

        # Delete collaborator
        client.delete(f"/orgs/{org_id}/collaborators/{member_id}")

        # Output response
        result = CollaboratorDeleted(
            org_id=org_id,
            user_name=username,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_collaborators_error(e)
