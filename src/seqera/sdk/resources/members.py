"""
Members resource for the Seqera SDK.
"""

from __future__ import annotations

from seqera.exceptions import OrganizationNotFoundException
from seqera.models.common import PaginatedList
from seqera.models.members import Member
from seqera.sdk.resources.base import BaseResource


class MembersResource(BaseResource):
    """
    SDK resource for managing organization members.

    Members are users who belong to an organization.
    """

    def list(
        self,
        organization: str | int,
    ) -> PaginatedList[Member]:
        """
        List members in an organization.

        Args:
            organization: Organization ID or name

        Returns:
            Auto-paginating iterator of Member objects
        """
        org_id = self._resolve_org_id(organization)

        def fetch_page(offset: int, limit: int) -> tuple[list[Member], int]:
            params = {"offset": offset, "max": limit}
            response = self._client.get(f"/orgs/{org_id}/members", params=params)

            members = [Member.model_validate(m) for m in response.get("members", [])]
            total_size = response.get("totalSize", len(members))
            return members, total_size

        return PaginatedList(fetch_page)

    def add(
        self,
        user: str,
        organization: str | int,
        *,
        role: str = "member",
    ) -> Member:
        """
        Add a member to an organization.

        Args:
            user: Username or email
            organization: Organization ID or name
            role: Member role (owner, member)

        Returns:
            Added Member object
        """
        org_id = self._resolve_org_id(organization)

        payload = {
            "user": user,
            "role": role.upper(),
        }

        response = self._client.post(f"/orgs/{org_id}/members", json=payload)
        member_data = response.get("member", {})
        return Member.model_validate(member_data)

    def update(
        self,
        user: str,
        organization: str | int,
        *,
        role: str,
    ) -> Member:
        """
        Update a member's role.

        Args:
            user: Username or email
            organization: Organization ID or name
            role: New role (owner, member)

        Returns:
            Updated Member object
        """
        org_id = self._resolve_org_id(organization)

        # Find member by username or email
        member_id = None
        for member in self.list(organization=org_id):
            if member.user_name == user or member.email == user:
                member_id = member.member_id
                break

        if not member_id:
            raise Exception(f"Member '{user}' not found")

        payload = {"role": role.upper()}
        response = self._client.put(f"/orgs/{org_id}/members/{member_id}", json=payload)
        member_data = response.get("member", {})
        return Member.model_validate(member_data)

    def delete(
        self,
        user: str,
        organization: str | int,
    ) -> None:
        """
        Remove a member from an organization.

        Args:
            user: Username or email
            organization: Organization ID or name
        """
        org_id = self._resolve_org_id(organization)

        # Find member by username or email
        for member in self.list(organization=org_id):
            if member.user_name == user or member.email == user:
                self._client.delete(f"/orgs/{org_id}/members/{member.member_id}")
                return

        raise Exception(f"Member '{user}' not found")

    def leave(
        self,
        organization: str | int,
    ) -> None:
        """
        Leave an organization.

        Args:
            organization: Organization ID or name
        """
        org_id = self._resolve_org_id(organization)
        self._client.delete(f"/orgs/{org_id}/members")

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
