"""
Teams resource for the Seqera SDK.
"""

from __future__ import annotations

from seqera.exceptions import OrganizationNotFoundException
from seqera.models.common import PaginatedList
from seqera.models.teams import Team, TeamMember
from seqera.sdk.resources.base import BaseResource


class TeamsResource(BaseResource):
    """
    SDK resource for managing teams.

    Teams are groups of members within an organization that can be
    granted access to workspaces.
    """

    def list(
        self,
        organization: str | int,
    ) -> PaginatedList[Team]:
        """
        List teams in an organization.

        Args:
            organization: Organization ID or name

        Returns:
            Auto-paginating iterator of Team objects
        """
        org_id = self._resolve_org_id(organization)

        def fetch_page(offset: int, limit: int) -> tuple[list[Team], int]:
            response = self._client.get(f"/orgs/{org_id}/teams")

            teams = [Team.model_validate(t) for t in response.get("teams", [])]
            total_size = len(teams)
            paginated = teams[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        team: str | int,
        organization: str | int,
    ) -> Team:
        """
        Get a team by ID or name.

        Args:
            team: Team ID or name
            organization: Organization ID or name

        Returns:
            Team object
        """
        org_id = self._resolve_org_id(organization)

        # If it's a name, find the ID first
        if isinstance(team, str) and not team.isdigit():
            for t in self.list(organization=org_id):
                if t.name.lower() == team.lower():
                    team = t.id
                    break
            else:
                raise Exception(f"Team '{team}' not found")

        response = self._client.get(f"/orgs/{org_id}/teams/{team}")
        team_data = response.get("team", {})
        return Team.model_validate(team_data)

    def add(
        self,
        name: str,
        organization: str | int,
        *,
        description: str | None = None,
    ) -> Team:
        """
        Create a new team.

        Args:
            name: Team name
            organization: Organization ID or name
            description: Team description

        Returns:
            Created Team object
        """
        org_id = self._resolve_org_id(organization)

        payload = {
            "team": {
                "name": name,
            }
        }
        if description:
            payload["team"]["description"] = description

        response = self._client.post(f"/orgs/{org_id}/teams", json=payload)
        team_data = response.get("team", {})
        return Team.model_validate(team_data)

    def update(
        self,
        team: str | int,
        organization: str | int,
        *,
        name: str | None = None,
        description: str | None = None,
    ) -> Team:
        """
        Update a team.

        Args:
            team: Team ID or name
            organization: Organization ID or name
            name: New name
            description: New description

        Returns:
            Updated Team object
        """
        org_id = self._resolve_org_id(organization)
        team_obj = self.get(team, organization=org_id)

        payload = {
            "team": {
                "name": name or team_obj.name,
            }
        }
        if description is not None:
            payload["team"]["description"] = description
        elif team_obj.description:
            payload["team"]["description"] = team_obj.description

        response = self._client.put(f"/orgs/{org_id}/teams/{team_obj.id}", json=payload)
        team_data = response.get("team", {})
        return Team.model_validate(team_data)

    def delete(
        self,
        team: str | int,
        organization: str | int,
    ) -> None:
        """
        Delete a team.

        Args:
            team: Team ID or name
            organization: Organization ID or name
        """
        org_id = self._resolve_org_id(organization)

        # Resolve team ID if name was provided
        if isinstance(team, str) and not team.isdigit():
            team_obj = self.get(team, organization=org_id)
            team = team_obj.id

        self._client.delete(f"/orgs/{org_id}/teams/{team}")

    def list_members(
        self,
        team: str | int,
        organization: str | int,
    ) -> PaginatedList[TeamMember]:
        """
        List members of a team.

        Args:
            team: Team ID or name
            organization: Organization ID or name

        Returns:
            Auto-paginating iterator of TeamMember objects
        """
        org_id = self._resolve_org_id(organization)

        # Resolve team ID if name was provided
        if isinstance(team, str) and not team.isdigit():
            team_obj = self.get(team, organization=org_id)
            team_id = team_obj.id
        else:
            team_id = int(team)

        def fetch_page(offset: int, limit: int) -> tuple[list[TeamMember], int]:
            response = self._client.get(f"/orgs/{org_id}/teams/{team_id}/members")

            members = [TeamMember.model_validate(m) for m in response.get("members", [])]
            total_size = len(members)
            paginated = members[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def add_member(
        self,
        team: str | int,
        organization: str | int,
        user: str,
    ) -> TeamMember:
        """
        Add a member to a team.

        Args:
            team: Team ID or name
            organization: Organization ID or name
            user: Username or email of the user to add

        Returns:
            Added TeamMember object
        """
        org_id = self._resolve_org_id(organization)

        # Resolve team ID if name was provided
        if isinstance(team, str) and not team.isdigit():
            team_obj = self.get(team, organization=org_id)
            team_id = team_obj.id
        else:
            team_id = int(team)

        payload = {"user": user}
        response = self._client.post(f"/orgs/{org_id}/teams/{team_id}/members", json=payload)
        member_data = response.get("member", {})
        return TeamMember.model_validate(member_data)

    def remove_member(
        self,
        team: str | int,
        organization: str | int,
        user: str,
    ) -> None:
        """
        Remove a member from a team.

        Args:
            team: Team ID or name
            organization: Organization ID or name
            user: Username or email of the user to remove
        """
        org_id = self._resolve_org_id(organization)

        # Resolve team ID if name was provided
        if isinstance(team, str) and not team.isdigit():
            team_obj = self.get(team, organization=org_id)
            team_id = team_obj.id
        else:
            team_id = int(team)

        # Find member by username or email
        for member in self.list_members(team=team_id, organization=org_id):
            if member.user_name == user or member.email == user:
                self._client.delete(f"/orgs/{org_id}/teams/{team_id}/members/{member.member_id}")
                return

        raise Exception(f"User '{user}' not found in team")

    def _resolve_org_id(self, organization: str | int) -> int:
        """Resolve organization name or ID to ID."""
        if isinstance(organization, int):
            return organization

        if isinstance(organization, str) and organization.isdigit():
            return int(organization)

        # Look up by name
        user_id = self._get_user_id()
        response = self._client.get(f"/user/{user_id}/workspaces")

        for entry in response.get("orgsAndWorkspaces", []):
            if entry.get("orgName", "").lower() == organization.lower():
                return entry.get("orgId")

        raise OrganizationNotFoundException(str(organization))
