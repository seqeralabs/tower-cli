"""
Organization members commands for Seqera CLI.

Manage organization members in the Seqera Platform.
"""

import sys
from typing import Optional

import typer
from typing_extensions import Annotated

from seqera.api.client import SeqeraClient
from seqera.commands.organizations import find_organization_by_name
from seqera.exceptions import (
    AuthenticationError,
    NotFoundError,
    OrganizationNotFoundException,
    SeqeraError,
)
from seqera.main import get_client, get_output_format
from seqera.responses import (
    MemberAdded,
    MemberDeleted,
    MemberLeft,
    MemberUpdated,
    MembersList,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create members app
app = typer.Typer(
    name="members",
    help="Manage organization members",
    no_args_is_help=True,
)


def handle_members_error(e: Exception) -> None:
    """Handle members command errors."""
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


def find_member_by_username_or_email(
    client: SeqeraClient, org_id: int, username_or_email: str
) -> Optional[dict]:
    """
    Find an organization member by username or email.

    Returns:
        Member dict with memberId if found, None otherwise
    """
    members_response = client.get(f"/orgs/{org_id}/members")
    members = members_response.get("members", [])
    for member in members:
        if member.get("userName") == username_or_email or member.get("email") == username_or_email:
            return member
    return None


@app.command("list")
def list_members(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    offset: Annotated[
        Optional[int],
        typer.Option("--offset", help="Pagination offset (conflicts with --page)"),
    ] = None,
    max_results: Annotated[
        Optional[int],
        typer.Option("--max", help="Maximum number of results"),
    ] = None,
    page: Annotated[
        Optional[int],
        typer.Option("--page", help="Page number (conflicts with --offset)"),
    ] = None,
) -> None:
    """List members in an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Check for conflicting pagination parameters
        if page is not None and offset is not None:
            raise SeqeraError("Please use either --page or --offset as pagination parameter")

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # Build query parameters
        params = {}
        pagination_info = None

        if page is not None:
            # Convert page to offset (page 1 = offset 0)
            calculated_offset = (page - 1) * (max_results or 50)
            params["offset"] = calculated_offset
            if max_results:
                params["max"] = max_results
            pagination_info = {"page": page, "max": max_results or 50}
        elif offset is not None:
            params["offset"] = offset
            if max_results:
                params["max"] = max_results
            pagination_info = {"offset": offset, "max": max_results}
        elif max_results:
            params["max"] = max_results

        # Get members
        members_response = client.get(f"/orgs/{org_id}/members", params=params)
        members = members_response.get("members", [])

        # Output response
        result = MembersList(
            organization=organization,
            members=members,
            pagination_info=pagination_info,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_members_error(e)


@app.command("add")
def add_member(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    user: Annotated[
        str,
        typer.Option("-u", "--user", help="Username or email to add"),
    ],
) -> None:
    """Add a member to an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # Add member
        payload = {"userNameOrEmail": user}
        member_response = client.put(f"/orgs/{org_id}/members/add", json=payload)
        member_data = member_response.get("member", {})

        # Output response
        result = MemberAdded(organization=organization, member=member_data)
        output_response(result, output_format)

    except Exception as e:
        handle_members_error(e)


@app.command("delete")
def delete_member(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    user: Annotated[
        str,
        typer.Option("-u", "--user", help="Username or email to remove"),
    ],
) -> None:
    """Remove a member from an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # Find member
        member = find_member_by_username_or_email(client, org_id, user)
        if not member:
            raise NotFoundError(f"Member '{user}' not found in organization '{organization}'")
        member_id = member.get("memberId")

        # Delete member
        client.delete(f"/orgs/{org_id}/members/{member_id}")

        # Output response
        result = MemberDeleted(user_ref=user, organization=organization)
        output_response(result, output_format)

    except Exception as e:
        handle_members_error(e)


@app.command("update")
def update_member(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    user: Annotated[
        str,
        typer.Option("-u", "--user", help="Username or email to update"),
    ],
    role: Annotated[
        str,
        typer.Option("-r", "--role", help="New role (OWNER or MEMBER)"),
    ],
) -> None:
    """Update a member's role in an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # Find member
        member = find_member_by_username_or_email(client, org_id, user)
        if not member:
            raise NotFoundError(f"Member '{user}' not found in organization '{organization}'")
        member_id = member.get("memberId")

        # Convert role to lowercase for API
        role_lower = role.lower()

        # Update member role
        payload = {"role": role_lower}
        client.put(f"/orgs/{org_id}/members/{member_id}/role", json=payload)

        # Output response
        result = MemberUpdated(user_ref=user, organization=organization, role=role_lower)
        output_response(result, output_format)

    except Exception as e:
        handle_members_error(e)


@app.command("leave")
def leave_organization(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
) -> None:
    """Leave an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # Leave organization
        client.delete(f"/orgs/{org_id}/members/leave")

        # Output response
        result = MemberLeft(organization=organization)
        output_response(result, output_format)

    except Exception as e:
        handle_members_error(e)
