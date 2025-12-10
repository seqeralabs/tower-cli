"""
Workspaces resource for the Seqera SDK.
"""

from __future__ import annotations

from seqera.exceptions import OrganizationNotFoundException, WorkspaceNotFoundException
from seqera.models.common import PaginatedList
from seqera.models.workspaces import OrgAndWorkspace, Workspace
from seqera.sdk.resources.base import BaseResource


class WorkspacesResource(BaseResource):
    """
    SDK resource for managing workspaces.

    Workspaces are collaborative environments within organizations where
    teams can manage pipelines, compute environments, and runs.

    Example:
        >>> from seqera import Seqera
        >>> client = Seqera()
        >>>
        >>> # List all workspaces
        >>> for ws in client.workspaces.list():
        ...     print(f"{ws.org_name}/{ws.workspace_name}")
        >>>
        >>> # Create a new workspace
        >>> workspace = client.workspaces.add(
        ...     name="my-workspace",
        ...     organization="my-org",
        ...     description="My new workspace",
        ... )
    """

    def list(
        self,
        organization: str | None = None,
    ) -> PaginatedList[OrgAndWorkspace]:
        """
        List all workspaces accessible to the current user.

        Args:
            organization: Filter by organization name

        Returns:
            Auto-paginating iterator of OrgAndWorkspace objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[OrgAndWorkspace], int]:
            user_id = self._get_user_id()
            response = self._client.get(f"/user/{user_id}/workspaces")

            items = []
            for entry in response.get("orgsAndWorkspaces", []):
                # Filter by organization if specified
                if organization:
                    if entry.get("orgName", "").lower() != organization.lower():
                        continue
                items.append(OrgAndWorkspace.model_validate(entry))

            # Apply pagination manually since API doesn't support it
            total_size = len(items)
            paginated = items[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        workspace: str | int,
    ) -> Workspace:
        """
        Get a workspace by ID or name.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Workspace object

        Raises:
            WorkspaceNotFoundException: If workspace not found
        """
        ws_id = self._resolve_workspace_id(workspace)

        # Find the workspace in the user's list to get org info
        user_id = self._get_user_id()
        response = self._client.get(f"/user/{user_id}/workspaces")

        for entry in response.get("orgsAndWorkspaces", []):
            if entry.get("workspaceId") == ws_id:
                org_id = entry.get("orgId")
                # Get full workspace details
                ws_response = self._client.get(f"/orgs/{org_id}/workspaces/{ws_id}")
                workspace_data = ws_response.get("workspace", {})
                return Workspace.model_validate(workspace_data)

        raise WorkspaceNotFoundException(str(workspace))

    def add(
        self,
        name: str,
        organization: str | int,
        *,
        full_name: str | None = None,
        description: str | None = None,
        visibility: str = "PRIVATE",
    ) -> Workspace:
        """
        Create a new workspace in an organization.

        Args:
            name: Workspace name (unique within organization)
            organization: Organization name or ID
            full_name: Display name for the workspace
            description: Workspace description
            visibility: Visibility level (PRIVATE, SHARED)

        Returns:
            Created Workspace object

        Raises:
            OrganizationNotFoundException: If organization not found
        """
        org_id = self._resolve_org_id(organization)

        payload = {
            "workspace": {
                "name": name,
                "visibility": visibility,
            }
        }

        if full_name:
            payload["workspace"]["fullName"] = full_name
        if description:
            payload["workspace"]["description"] = description

        response = self._client.post(f"/orgs/{org_id}/workspaces", json=payload)
        workspace_data = response.get("workspace", {})
        return Workspace.model_validate(workspace_data)

    def update(
        self,
        workspace: str | int,
        *,
        name: str | None = None,
        full_name: str | None = None,
        description: str | None = None,
        visibility: str | None = None,
    ) -> Workspace:
        """
        Update an existing workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference
            name: New workspace name
            full_name: New display name
            description: New description
            visibility: New visibility level

        Returns:
            Updated Workspace object
        """
        ws_id = self._resolve_workspace_id(workspace)

        # Get current workspace to find org
        user_id = self._get_user_id()
        response = self._client.get(f"/user/{user_id}/workspaces")

        org_id = None
        current_ws = None
        for entry in response.get("orgsAndWorkspaces", []):
            if entry.get("workspaceId") == ws_id:
                org_id = entry.get("orgId")
                # Get full workspace details
                ws_response = self._client.get(f"/orgs/{org_id}/workspaces/{ws_id}")
                current_ws = ws_response.get("workspace", {})
                break

        if not org_id or not current_ws:
            raise WorkspaceNotFoundException(str(workspace))

        # Build update payload
        payload = {
            "workspace": {
                "name": name or current_ws.get("name"),
                "visibility": visibility or current_ws.get("visibility"),
            }
        }

        if full_name is not None:
            payload["workspace"]["fullName"] = full_name
        elif current_ws.get("fullName"):
            payload["workspace"]["fullName"] = current_ws["fullName"]

        if description is not None:
            payload["workspace"]["description"] = description
        elif current_ws.get("description"):
            payload["workspace"]["description"] = current_ws["description"]

        response = self._client.put(f"/orgs/{org_id}/workspaces/{ws_id}", json=payload)
        workspace_data = response.get("workspace", {})
        return Workspace.model_validate(workspace_data)

    def delete(
        self,
        workspace: str | int,
    ) -> None:
        """
        Delete a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference
        """
        ws_id = self._resolve_workspace_id(workspace)

        # Find org ID
        user_id = self._get_user_id()
        response = self._client.get(f"/user/{user_id}/workspaces")

        for entry in response.get("orgsAndWorkspaces", []):
            if entry.get("workspaceId") == ws_id:
                org_id = entry.get("orgId")
                self._client.delete(f"/orgs/{org_id}/workspaces/{ws_id}")
                return

        raise WorkspaceNotFoundException(str(workspace))

    def leave(
        self,
        workspace: str | int,
    ) -> None:
        """
        Leave a workspace as a participant.

        Args:
            workspace: Workspace ID or "org/workspace" reference
        """
        ws_id = self._resolve_workspace_id(workspace)

        # Find org ID and participant info
        user_id = self._get_user_id()
        response = self._client.get(f"/user/{user_id}/workspaces")

        for entry in response.get("orgsAndWorkspaces", []):
            if entry.get("workspaceId") == ws_id:
                org_id = entry.get("orgId")
                self._client.delete(f"/orgs/{org_id}/workspaces/{ws_id}/participants")
                return

        raise WorkspaceNotFoundException(str(workspace))

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
