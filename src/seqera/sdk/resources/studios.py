"""
Studios resource for the Seqera SDK.
"""

from __future__ import annotations

from seqera.models.common import PaginatedList
from seqera.models.studios import Studio, StudioCheckpoint
from seqera.sdk.resources.base import BaseResource


class StudiosResource(BaseResource):
    """
    SDK resource for managing Data Studios.

    Studios are interactive analysis environments that can be started
    and stopped on demand.
    """

    def list(
        self,
        workspace: str | int | None = None,
    ) -> PaginatedList[Studio]:
        """
        List studios in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of Studio objects
        """
        ws_id = self._get_workspace(workspace)

        def fetch_page(offset: int, limit: int) -> tuple[list[Studio], int]:
            response = self._client.get(f"/workspaces/{ws_id}/studios")

            studios = [Studio.model_validate(s) for s in response.get("sessions", [])]
            total_size = len(studios)
            paginated = studios[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        studio_id: str,
        workspace: str | int | None = None,
    ) -> Studio:
        """
        Get a studio by ID.

        Args:
            studio_id: Studio session ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Studio object
        """
        ws_id = self._get_workspace(workspace)
        response = self._client.get(f"/workspaces/{ws_id}/studios/{studio_id}")

        studio_data = response.get("session", {})
        return Studio.model_validate(studio_data)

    def start(
        self,
        studio_id: str,
        workspace: str | int | None = None,
    ) -> Studio:
        """
        Start a studio.

        Args:
            studio_id: Studio session ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Started Studio object
        """
        ws_id = self._get_workspace(workspace)
        self._client.post(f"/workspaces/{ws_id}/studios/{studio_id}/start")
        return self.get(studio_id, workspace=ws_id)

    def stop(
        self,
        studio_id: str,
        workspace: str | int | None = None,
    ) -> Studio:
        """
        Stop a studio.

        Args:
            studio_id: Studio session ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Stopped Studio object
        """
        ws_id = self._get_workspace(workspace)
        self._client.post(f"/workspaces/{ws_id}/studios/{studio_id}/stop")
        return self.get(studio_id, workspace=ws_id)

    def delete(
        self,
        studio_id: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete a studio.

        Args:
            studio_id: Studio session ID
            workspace: Workspace ID or "org/workspace" reference
        """
        ws_id = self._get_workspace(workspace)
        self._client.delete(f"/workspaces/{ws_id}/studios/{studio_id}")

    def checkpoints(
        self,
        studio_id: str,
        workspace: str | int | None = None,
    ) -> PaginatedList[StudioCheckpoint]:
        """
        List checkpoints for a studio.

        Args:
            studio_id: Studio session ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of StudioCheckpoint objects
        """
        ws_id = self._get_workspace(workspace)

        def fetch_page(offset: int, limit: int) -> tuple[list[StudioCheckpoint], int]:
            response = self._client.get(f"/workspaces/{ws_id}/studios/{studio_id}/checkpoints")

            checkpoints = [
                StudioCheckpoint.model_validate(c) for c in response.get("checkpoints", [])
            ]
            total_size = len(checkpoints)
            paginated = checkpoints[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)
