"""
Teams commands for Seqera CLI.

Manage teams in the Seqera Platform.
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
    TeamAdded,
    TeamDeleted,
    TeamMemberAdded,
    TeamMemberDeleted,
    TeamMembersList,
    TeamsList,
    TeamView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create teams app
app = typer.Typer(
    name="teams",
    help="Manage teams",
    no_args_is_help=True,
)

# Create members sub-app
members_app = typer.Typer(
    name="members",
    help="Manage team members",
    no_args_is_help=True,
)
app.add_typer(members_app, name="members")


def handle_teams_error(e: Exception) -> None:
    """Handle teams command errors."""
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


def find_team_by_name(client: SeqeraClient, org_id: int, team_name: str) -> Optional[dict]:
    """
    Find a team by name within an organization.

    Returns:
        Team dict with teamId if found, None otherwise
    """
    teams_response = client.get(f"/orgs/{org_id}/teams")
    teams = teams_response.get("teams", [])
    for team in teams:
        if team.get("name") == team_name:
            return team
    return None


def find_member_by_username_or_email(
    client: SeqeraClient, org_id: int, team_id: int, username_or_email: str
) -> Optional[dict]:
    """
    Find a team member by username or email.

    Returns:
        Member dict with memberId if found, None otherwise
    """
    members_response = client.get(f"/orgs/{org_id}/teams/{team_id}/members")
    members = members_response.get("members", [])
    for member in members:
        if member.get("userName") == username_or_email or member.get("email") == username_or_email:
            return member
    return None


@app.command("list")
def list_teams(
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
    """List teams in an organization."""
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

        # Get teams
        teams_response = client.get(f"/orgs/{org_id}/teams", params=params)
        teams = teams_response.get("teams", [])

        # Output response
        result = TeamsList(
            organization=organization,
            teams=teams,
            base_workspace_url=client.base_url,
            pagination_info=pagination_info,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_teams_error(e)


@app.command("view")
def view_team(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Team name"),
    ] = None,
    team_id: Annotated[
        Optional[int],
        typer.Option("-i", "--id", help="Team ID"),
    ] = None,
) -> None:
    """View team details."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Must provide either name or ID
        if not name and not team_id:
            output_error("Must provide either --name or --id")
            sys.exit(1)

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # If name provided, find the team ID
        if name and not team_id:
            team = find_team_by_name(client, org_id, name)
            if not team:
                raise NotFoundError(f"Team '{name}' not found in organization '{organization}'")
            team_id = team.get("teamId")

        # Get team details
        teams_response = client.get(f"/orgs/{org_id}/teams")
        teams = teams_response.get("teams", [])
        team = None
        for t in teams:
            if t.get("teamId") == team_id:
                team = t
                break

        if not team:
            raise NotFoundError(f"Team with ID {team_id} not found")

        # Output response
        result = TeamView(organization=organization, team=team)
        output_response(result, output_format)

    except Exception as e:
        handle_teams_error(e)


@app.command("add")
def add_team(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Team name"),
    ],
    description: Annotated[
        Optional[str],
        typer.Option("-d", "--description", help="Team description"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if team already exists"),
    ] = False,
) -> None:
    """Add a new team to an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # If overwrite, delete existing team first
        if overwrite:
            try:
                existing_team = find_team_by_name(client, org_id, name)
                if existing_team:
                    team_id = existing_team.get("teamId")
                    client.delete(f"/orgs/{org_id}/teams/{team_id}")
            except Exception:
                # Ignore errors during overwrite deletion
                pass

        # Build team payload
        payload = {
            "team": {
                "name": name,
            }
        }

        if description:
            payload["team"]["description"] = description

        # Create team
        team_response = client.post(f"/orgs/{org_id}/teams", json=payload)

        # Output response
        result = TeamAdded(organization=organization, team_name=name)
        output_response(result, output_format)

    except Exception as e:
        handle_teams_error(e)


@app.command("delete")
def delete_team(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Team name"),
    ] = None,
    team_id: Annotated[
        Optional[int],
        typer.Option("-i", "--id", help="Team ID"),
    ] = None,
) -> None:
    """Delete a team from an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Must provide either name or ID
        if not name and not team_id:
            output_error("Must provide either --name or --id")
            sys.exit(1)

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # If name provided, find the team ID
        team_ref = name if name else str(team_id)
        if name and not team_id:
            team = find_team_by_name(client, org_id, name)
            if not team:
                raise NotFoundError(f"Team '{name}' not found in organization '{organization}'")
            team_id = team.get("teamId")

        # Delete team
        client.delete(f"/orgs/{org_id}/teams/{team_id}")

        # Output response
        result = TeamDeleted(organization=organization, team_ref=team_ref)
        output_response(result, output_format)

    except Exception as e:
        handle_teams_error(e)


# Team Members Commands


@members_app.command("list")
def list_members(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    team: Annotated[
        str,
        typer.Option("-t", "--team", help="Team name"),
    ],
) -> None:
    """List members of a team."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # Find team
        team_obj = find_team_by_name(client, org_id, team)
        if not team_obj:
            raise NotFoundError(f"Team '{team}' not found in organization '{organization}'")
        team_id = team_obj.get("teamId")

        # Get team members
        members_response = client.get(f"/orgs/{org_id}/teams/{team_id}/members")
        members = members_response.get("members", [])

        # Output response
        result = TeamMembersList(team_name=team, members=members)
        output_response(result, output_format)

    except Exception as e:
        handle_teams_error(e)


@members_app.command("add")
def add_member(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    team: Annotated[
        str,
        typer.Option("-t", "--team", help="Team name"),
    ],
    member: Annotated[
        str,
        typer.Option("-m", "--member", help="Member username or email"),
    ],
) -> None:
    """Add a member to a team."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # Find team
        team_obj = find_team_by_name(client, org_id, team)
        if not team_obj:
            raise NotFoundError(f"Team '{team}' not found in organization '{organization}'")
        team_id = team_obj.get("teamId")

        # Add member
        payload = {"userNameOrEmail": member}
        member_response = client.post(
            f"/orgs/{org_id}/teams/{team_id}/members",
            json=payload
        )
        member_data = member_response.get("member", {})

        # Output response
        result = TeamMemberAdded(team_name=team, member=member_data)
        output_response(result, output_format)

    except Exception as e:
        handle_teams_error(e)


@members_app.command("delete")
def delete_member(
    organization: Annotated[
        str,
        typer.Option("-o", "--organization", help="Organization name"),
    ],
    team: Annotated[
        str,
        typer.Option("-t", "--team", help="Team name"),
    ],
    member: Annotated[
        str,
        typer.Option("-m", "--member", help="Member username or email"),
    ],
) -> None:
    """Remove a member from a team."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find organization
        org = find_organization_by_name(client, organization)
        if not org:
            raise OrganizationNotFoundException(organization)
        org_id = org.get("orgId")

        # Find team
        team_obj = find_team_by_name(client, org_id, team)
        if not team_obj:
            raise NotFoundError(f"Team '{team}' not found in organization '{organization}'")
        team_id = team_obj.get("teamId")

        # Find member
        member_obj = find_member_by_username_or_email(client, org_id, team_id, member)
        if not member_obj:
            raise NotFoundError(f"Member '{member}' not found in team '{team}'")
        member_id = member_obj.get("memberId")

        # Delete member
        client.delete(f"/orgs/{org_id}/teams/{team_id}/members/{member_id}/delete")

        # Output response
        result = TeamMemberDeleted(team_name=team, member_ref=member)
        output_response(result, output_format)

    except Exception as e:
        handle_teams_error(e)
