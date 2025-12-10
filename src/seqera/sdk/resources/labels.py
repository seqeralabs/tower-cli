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
