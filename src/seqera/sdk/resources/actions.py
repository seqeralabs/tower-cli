"""
Actions resource for the Seqera SDK.
"""

from __future__ import annotations

from typing import Any

from seqera.exceptions import ActionNotFoundException
from seqera.models.actions import Action
from seqera.models.common import PaginatedList
from seqera.sdk.resources.base import BaseResource


class ActionsResource(BaseResource):
    """
    SDK resource for managing pipeline actions.

    Actions are automation triggers that can launch pipelines in response
    to events like webhooks.
    """

    def list(
        self,
        workspace: str | int | None = None,
    ) -> PaginatedList[Action]:
        """
        List actions in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of Action objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[Action], int]:
            params = self._build_params(workspace=workspace)
            response = self._client.get("/actions", params=params)

            actions = [Action.model_validate(a) for a in response.get("actions", [])]
            total_size = len(actions)
            paginated = actions[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        action: str,
        workspace: str | int | None = None,
    ) -> Action:
        """
        Get an action by ID or name.

        Args:
            action: Action ID or name
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Action object

        Raises:
            ActionNotFoundException: If action not found
        """
        params = self._build_params(workspace=workspace)

        # Try to get by ID first
        try:
            response = self._client.get(f"/actions/{action}", params=params)
            action_data = response.get("action", {})
            if action_data:
                return Action.model_validate(action_data)
        except Exception:
            pass

        # Try to find by name
        for a in self.list(workspace=workspace):
            if a.name == action:
                response = self._client.get(f"/actions/{a.id}", params=params)
                return Action.model_validate(response.get("action", {}))

        raise ActionNotFoundException(action, str(workspace or "user"))

    def add(
        self,
        name: str,
        pipeline_id: int,
        workspace: str | int | None = None,
        *,
        source: str = "tower",
        launch_config: dict[str, Any] | None = None,
    ) -> Action:
        """
        Create a new action.

        Args:
            name: Action name
            pipeline_id: Pipeline ID to launch
            workspace: Workspace ID or "org/workspace" reference
            source: Action source (tower, github, etc.)
            launch_config: Custom launch configuration

        Returns:
            Created Action object
        """
        params = self._build_params(workspace=workspace)

        payload: dict[str, Any] = {
            "name": name,
            "pipelineId": pipeline_id,
            "source": source,
        }

        if launch_config:
            payload["launch"] = launch_config

        response = self._client.post("/actions", json=payload, params=params)
        action_data = response.get("action", {})
        return Action.model_validate(action_data)

    def update(
        self,
        action: str,
        workspace: str | int | None = None,
        *,
        name: str | None = None,
        launch_config: dict[str, Any] | None = None,
    ) -> Action:
        """
        Update an action.

        Args:
            action: Action ID or name
            workspace: Workspace ID or "org/workspace" reference
            name: New name
            launch_config: New launch configuration

        Returns:
            Updated Action object
        """
        existing = self.get(action, workspace=workspace)
        params = self._build_params(workspace=workspace)

        payload: dict[str, Any] = {}
        if name:
            payload["name"] = name
        if launch_config:
            payload["launch"] = launch_config

        response = self._client.put(f"/actions/{existing.id}", json=payload, params=params)
        action_data = response.get("action", {})
        return Action.model_validate(action_data)

    def delete(
        self,
        action: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete an action.

        Args:
            action: Action ID or name
            workspace: Workspace ID or "org/workspace" reference
        """
        existing = self.get(action, workspace=workspace)
        params = self._build_params(workspace=workspace)
        self._client.delete(f"/actions/{existing.id}", params=params)
