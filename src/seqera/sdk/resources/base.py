"""
Base resource class for the Seqera SDK.
"""

from __future__ import annotations

import os
from typing import TYPE_CHECKING, Any, TypeVar

from seqera.exceptions import WorkspaceNotFoundException
from seqera.models.base import SeqeraModel

if TYPE_CHECKING:
    from seqera.api.client import SeqeraClient

T = TypeVar("T", bound=SeqeraModel)


class BaseResource:
    """
    Base class for all SDK resource classes.

    Provides common functionality for resolving workspaces and making API calls.
    """

    def __init__(
        self,
        client: "SeqeraClient",
        default_workspace: str | int | None = None,
    ) -> None:
        """
        Initialize the resource.

        Args:
            client: The HTTP client for making API requests
            default_workspace: Default workspace to use if not specified per-call.
                Can be workspace ID or "org/workspace" reference.
        """
        self._client = client
        self._default_workspace = default_workspace
        self._workspace_cache: dict[str, int] = {}
        self._user_id: int | None = None

    def _get_workspace(self, workspace: str | int | None) -> int | None:
        """
        Get workspace ID to use for a request.

        Priority:
        1. Explicit workspace parameter
        2. Default workspace from client initialization
        3. SEQERA_WORKSPACE environment variable
        4. None (user's personal workspace)

        Args:
            workspace: Explicit workspace ID or "org/workspace" reference

        Returns:
            Workspace ID as integer, or None for user's personal workspace
        """
        # Use explicit workspace if provided
        if workspace is not None:
            return self._resolve_workspace_id(workspace)

        # Fall back to default workspace
        if self._default_workspace is not None:
            return self._resolve_workspace_id(self._default_workspace)

        # Check environment variable
        env_workspace = os.environ.get("SEQERA_WORKSPACE")
        if env_workspace:
            return self._resolve_workspace_id(env_workspace)

        # Use personal workspace
        return None

    def _resolve_workspace_id(self, workspace: str | int) -> int:
        """
        Resolve a workspace reference to its ID.

        Args:
            workspace: Either numeric workspace ID or "org/workspace" string

        Returns:
            Workspace ID as integer

        Raises:
            WorkspaceNotFoundException: If workspace cannot be found
        """
        # If it's already an integer, return it
        if isinstance(workspace, int):
            return workspace

        # If it's a numeric string, convert to int
        if isinstance(workspace, str) and workspace.isdigit():
            return int(workspace)

        # It must be an "org/workspace" reference - look it up
        cache_key = workspace.lower()
        if cache_key in self._workspace_cache:
            return self._workspace_cache[cache_key]

        # Get user info to find workspaces
        user_id = self._get_user_id()

        # Get all workspaces
        response = self._client.get(f"/user/{user_id}/workspaces")
        orgs_and_workspaces = response.get("orgsAndWorkspaces", [])

        # Parse org/workspace format
        if "/" in workspace:
            org_name, ws_name = workspace.split("/", 1)
        else:
            # Assume it's just workspace name, match any org
            org_name = None
            ws_name = workspace

        # Find matching workspace
        for entry in orgs_and_workspaces:
            entry_org = entry.get("orgName") or ""
            entry_ws = entry.get("workspaceName") or ""
            entry_ws_id = entry.get("workspaceId")

            if org_name:
                # Match both org and workspace name
                if entry_org.lower() == org_name.lower() and entry_ws.lower() == ws_name.lower():
                    self._workspace_cache[cache_key] = entry_ws_id
                    return entry_ws_id
            else:
                # Match just workspace name
                if entry_ws.lower() == ws_name.lower():
                    self._workspace_cache[cache_key] = entry_ws_id
                    return entry_ws_id

        raise WorkspaceNotFoundException(workspace)

    def _get_user_id(self) -> int:
        """Get the current user's ID."""
        if self._user_id is None:
            response = self._client.get("/user-info")
            self._user_id = response.get("user", {}).get("id")
        return self._user_id

    def _build_params(
        self,
        workspace: str | int | None = None,
        **extra_params: Any,
    ) -> dict[str, Any]:
        """
        Build query parameters for an API request.

        Args:
            workspace: Workspace ID or reference
            **extra_params: Additional parameters to include

        Returns:
            Dictionary of query parameters
        """
        params: dict[str, Any] = {}

        # Add workspace ID if specified
        ws_id = self._get_workspace(workspace)
        if ws_id is not None:
            params["workspaceId"] = ws_id

        # Add extra parameters (excluding None values)
        for key, value in extra_params.items():
            if value is not None:
                params[key] = value

        return params
