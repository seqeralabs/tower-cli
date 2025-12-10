"""
Collaborators resource for the Seqera SDK.
"""

from __future__ import annotations

from seqera.exceptions import OrganizationNotFoundException
from seqera.models.collaborators import Collaborator
from seqera.models.common import PaginatedList
from seqera.sdk.resources.base import BaseResource


class CollaboratorsResource(BaseResource):
    """
    SDK resource for managing organization collaborators.

    Collaborators are external users who have been invited to participate
    in an organization's workspaces.
    """

    def list(
        self,
        organization: str | int,
    ) -> PaginatedList[Collaborator]:
        """
        List collaborators in an organization.

        Args:
            organization: Organization ID or name

        Returns:
            Auto-paginating iterator of Collaborator objects
        """
        org_id = self._resolve_org_id(organization)

        def fetch_page(offset: int, limit: int) -> tuple[list[Collaborator], int]:
            response = self._client.get(f"/orgs/{org_id}/collaborators")

            collaborators = [
                Collaborator.model_validate(c) for c in response.get("collaborators", [])
            ]
            total_size = len(collaborators)
            paginated = collaborators[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def add(
        self,
        user: str,
        organization: str | int,
    ) -> Collaborator:
        """
        Invite a collaborator to an organization.

        Args:
            user: Username or email
            organization: Organization ID or name

        Returns:
            Added Collaborator object
        """
        org_id = self._resolve_org_id(organization)

        payload = {"user": user}
        response = self._client.post(f"/orgs/{org_id}/collaborators", json=payload)
        collaborator_data = response.get("collaborator", {})
        return Collaborator.model_validate(collaborator_data)

    def delete(
        self,
        user: str,
        organization: str | int,
    ) -> None:
        """
        Remove a collaborator from an organization.

        Args:
            user: Username or email
            organization: Organization ID or name
        """
        org_id = self._resolve_org_id(organization)

        # Find collaborator by username or email
        for c in self.list(organization=org_id):
            if c.user_name == user or c.email == user:
                self._client.delete(f"/orgs/{org_id}/collaborators/{c.member_id}")
                return

        raise Exception(f"Collaborator '{user}' not found")

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
