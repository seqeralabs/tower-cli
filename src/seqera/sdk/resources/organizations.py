"""
Organizations resource for the Seqera SDK.
"""

from __future__ import annotations

from seqera.exceptions import OrganizationNotFoundException
from seqera.models.common import PaginatedList
from seqera.models.organizations import Organization
from seqera.sdk.resources.base import BaseResource


class OrganizationsResource(BaseResource):
    """
    SDK resource for managing organizations.

    Organizations are top-level containers that hold workspaces, teams, and members.
    """

    def list(self) -> PaginatedList[Organization]:
        """
        List all organizations accessible to the current user.

        Returns:
            Auto-paginating iterator of Organization objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[Organization], int]:
            user_id = self._get_user_id()
            response = self._client.get(f"/user/{user_id}/workspaces")

            # Extract unique organizations
            orgs_seen: set[int] = set()
            orgs: list[Organization] = []

            for entry in response.get("orgsAndWorkspaces", []):
                org_id = entry.get("orgId")
                if org_id and org_id not in orgs_seen:
                    orgs_seen.add(org_id)
                    # Get full organization details
                    org_response = self._client.get(f"/orgs/{org_id}")
                    org_data = org_response.get("organization", {})
                    orgs.append(Organization.model_validate(org_data))

            total_size = len(orgs)
            paginated = orgs[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        organization: str | int,
    ) -> Organization:
        """
        Get an organization by ID or name.

        Args:
            organization: Organization ID or name

        Returns:
            Organization object

        Raises:
            OrganizationNotFoundException: If organization not found
        """
        org_id = self._resolve_org_id(organization)
        response = self._client.get(f"/orgs/{org_id}")
        org_data = response.get("organization", {})

        if not org_data:
            raise OrganizationNotFoundException(str(organization))

        return Organization.model_validate(org_data)

    def add(
        self,
        name: str,
        *,
        full_name: str | None = None,
        description: str | None = None,
        website: str | None = None,
        location: str | None = None,
    ) -> Organization:
        """
        Create a new organization.

        Args:
            name: Organization name (unique)
            full_name: Display name
            description: Organization description
            website: Website URL
            location: Location

        Returns:
            Created Organization object
        """
        payload = {
            "organization": {
                "name": name,
            }
        }

        if full_name:
            payload["organization"]["fullName"] = full_name
        if description:
            payload["organization"]["description"] = description
        if website:
            payload["organization"]["website"] = website
        if location:
            payload["organization"]["location"] = location

        response = self._client.post("/orgs", json=payload)
        org_data = response.get("organization", {})
        return Organization.model_validate(org_data)

    def update(
        self,
        organization: str | int,
        *,
        name: str | None = None,
        full_name: str | None = None,
        description: str | None = None,
        website: str | None = None,
        location: str | None = None,
    ) -> Organization:
        """
        Update an existing organization.

        Args:
            organization: Organization ID or name
            name: New name
            full_name: New display name
            description: New description
            website: New website
            location: New location

        Returns:
            Updated Organization object
        """
        org_id = self._resolve_org_id(organization)

        # Get current organization
        current = self.get(org_id)

        payload = {
            "organization": {
                "name": name or current.name,
            }
        }

        if full_name is not None:
            payload["organization"]["fullName"] = full_name
        elif current.full_name:
            payload["organization"]["fullName"] = current.full_name

        if description is not None:
            payload["organization"]["description"] = description
        elif current.description:
            payload["organization"]["description"] = current.description

        if website is not None:
            payload["organization"]["website"] = website
        elif current.website:
            payload["organization"]["website"] = current.website

        if location is not None:
            payload["organization"]["location"] = location
        elif current.location:
            payload["organization"]["location"] = current.location

        response = self._client.put(f"/orgs/{org_id}", json=payload)
        org_data = response.get("organization", {})
        return Organization.model_validate(org_data)

    def delete(
        self,
        organization: str | int,
    ) -> None:
        """
        Delete an organization.

        Args:
            organization: Organization ID or name
        """
        org_id = self._resolve_org_id(organization)
        self._client.delete(f"/orgs/{org_id}")

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
