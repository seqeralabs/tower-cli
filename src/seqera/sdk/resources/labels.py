"""
Labels resource for the Seqera SDK.
"""

from __future__ import annotations

from seqera.models.common import PaginatedList
from seqera.models.labels import Label
from seqera.sdk.resources.base import BaseResource


class LabelsResource(BaseResource):
    """
    SDK resource for managing workspace labels.

    Labels are key-value pairs used to organize and filter resources.
    """

    def list(
        self,
        workspace: str | int | None = None,
    ) -> PaginatedList[Label]:
        """
        List labels in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of Label objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[Label], int]:
            params = self._build_params(workspace=workspace)
            response = self._client.get("/labels", params=params)

            labels = [Label.model_validate(l) for l in response.get("labels", [])]
            total_size = len(labels)
            paginated = labels[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def add(
        self,
        name: str,
        workspace: str | int | None = None,
        *,
        value: str | None = None,
    ) -> Label:
        """
        Add a new label.

        Args:
            name: Label name
            workspace: Workspace ID or "org/workspace" reference
            value: Optional label value

        Returns:
            Created Label object
        """
        params = self._build_params(workspace=workspace)

        payload = {"name": name}
        if value:
            payload["value"] = value

        response = self._client.post("/labels", json=payload, params=params)

        return Label(
            id=response.get("id", 0),
            name=name,
            value=value,
        )

    def delete(
        self,
        label_id: int,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete a label.

        Args:
            label_id: Label ID
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        self._client.delete(f"/labels/{label_id}", params=params)

    def update(
        self,
        label_id: int,
        workspace: str | int | None = None,
        *,
        name: str | None = None,
        value: str | None = None,
    ) -> Label:
        """
        Update an existing label.

        Args:
            label_id: Label ID
            workspace: Workspace ID or "org/workspace" reference
            name: New label name (optional)
            value: New label value (optional)

        Returns:
            Updated Label object
        """
        params = self._build_params(workspace=workspace)

        # Get existing label to preserve fields
        existing = self._client.get(f"/labels/{label_id}", params=params)

        payload = {
            "name": name if name else existing.get("name"),
            "resource": existing.get("resource", False),
        }
        if value is not None:
            payload["value"] = value
        elif existing.get("value"):
            payload["value"] = existing.get("value")

        self._client.put(f"/labels/{label_id}", json=payload, params=params)

        return Label(
            id=label_id,
            name=payload["name"],
            value=payload.get("value"),
        )

    def apply_to_workflows(
        self,
        label_ids: list[int],
        workflow_ids: list[str],
        workspace: str | int | None = None,
    ) -> None:
        """
        Apply labels to workflow runs (replaces existing labels).

        Args:
            label_ids: List of label IDs to apply
            workflow_ids: List of workflow run IDs
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        payload = {"labelIds": label_ids, "workflowIds": workflow_ids}
        self._client.post("/labels/workflows/apply", json=payload, params=params)

    def add_to_workflows(
        self,
        label_ids: list[int],
        workflow_ids: list[str],
        workspace: str | int | None = None,
    ) -> None:
        """
        Add labels to workflow runs (appends to existing labels).

        Args:
            label_ids: List of label IDs to add
            workflow_ids: List of workflow run IDs
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        payload = {"labelIds": label_ids, "workflowIds": workflow_ids}
        self._client.post("/labels/workflows/add", json=payload, params=params)

    def remove_from_workflows(
        self,
        label_ids: list[int],
        workflow_ids: list[str],
        workspace: str | int | None = None,
    ) -> None:
        """
        Remove labels from workflow runs.

        Args:
            label_ids: List of label IDs to remove
            workflow_ids: List of workflow run IDs
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        payload = {"labelIds": label_ids, "workflowIds": workflow_ids}
        self._client.post("/labels/workflows/remove", json=payload, params=params)

    def apply_to_pipelines(
        self,
        label_ids: list[int],
        pipeline_ids: list[int],
        workspace: str | int | None = None,
    ) -> None:
        """
        Apply labels to pipelines (replaces existing labels).

        Args:
            label_ids: List of label IDs to apply
            pipeline_ids: List of pipeline IDs
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        payload = {"labelIds": label_ids, "pipelineIds": pipeline_ids}
        self._client.post("/labels/pipelines/apply", json=payload, params=params)

    def add_to_pipelines(
        self,
        label_ids: list[int],
        pipeline_ids: list[int],
        workspace: str | int | None = None,
    ) -> None:
        """
        Add labels to pipelines (appends to existing labels).

        Args:
            label_ids: List of label IDs to add
            pipeline_ids: List of pipeline IDs
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        payload = {"labelIds": label_ids, "pipelineIds": pipeline_ids}
        self._client.post("/labels/pipelines/add", json=payload, params=params)

    def remove_from_pipelines(
        self,
        label_ids: list[int],
        pipeline_ids: list[int],
        workspace: str | int | None = None,
    ) -> None:
        """
        Remove labels from pipelines.

        Args:
            label_ids: List of label IDs to remove
            pipeline_ids: List of pipeline IDs
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        payload = {"labelIds": label_ids, "pipelineIds": pipeline_ids}
        self._client.post("/labels/pipelines/remove", json=payload, params=params)

    def apply_to_actions(
        self,
        label_ids: list[int],
        action_ids: list[str],
        workspace: str | int | None = None,
    ) -> None:
        """
        Apply labels to actions (replaces existing labels).

        Args:
            label_ids: List of label IDs to apply
            action_ids: List of action IDs
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        payload = {"labelIds": label_ids, "actionIds": action_ids}
        self._client.post("/labels/actions/apply", json=payload, params=params)

    def add_to_actions(
        self,
        label_ids: list[int],
        action_ids: list[str],
        workspace: str | int | None = None,
    ) -> None:
        """
        Add labels to actions (appends to existing labels).

        Args:
            label_ids: List of label IDs to add
            action_ids: List of action IDs
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        payload = {"labelIds": label_ids, "actionIds": action_ids}
        self._client.post("/labels/actions/add", json=payload, params=params)

    def remove_from_actions(
        self,
        label_ids: list[int],
        action_ids: list[str],
        workspace: str | int | None = None,
    ) -> None:
        """
        Remove labels from actions.

        Args:
            label_ids: List of label IDs to remove
            action_ids: List of action IDs
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        payload = {"labelIds": label_ids, "actionIds": action_ids}
        self._client.post("/labels/actions/remove", json=payload, params=params)
