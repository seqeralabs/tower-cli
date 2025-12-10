"""
Data links resource for the Seqera SDK.
"""

from __future__ import annotations

from typing import Any

from seqera.models.common import PaginatedList
from seqera.models.data_links import DataLink
from seqera.sdk.resources.base import BaseResource


class DataLinksResource(BaseResource):
    """
    SDK resource for managing data links.

    Data links connect cloud storage locations to workspaces.
    """

    def list(
        self,
        workspace: str | int | None = None,
    ) -> PaginatedList[DataLink]:
        """
        List data links in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of DataLink objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[DataLink], int]:
            params = self._build_params(workspace=workspace)
            response = self._client.get("/data-links", params=params)

            links = [DataLink.model_validate(dl) for dl in response.get("dataLinks", [])]
            total_size = len(links)
            paginated = links[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        data_link_id: str,
        workspace: str | int | None = None,
    ) -> DataLink:
        """
        Get a data link by ID.

        Args:
            data_link_id: Data link ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            DataLink object
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get(f"/data-links/{data_link_id}", params=params)

        link_data = response.get("dataLink", {})
        return DataLink.model_validate(link_data)

    def add(
        self,
        name: str,
        provider: str,
        resource_ref: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        credentials_id: str | None = None,
        config: dict[str, Any] | None = None,
    ) -> DataLink:
        """
        Create a new data link.

        Args:
            name: Data link name
            provider: Cloud provider (aws, azure, google)
            resource_ref: Resource reference (e.g., S3 bucket path)
            workspace: Workspace ID or "org/workspace" reference
            description: Description
            credentials_id: Credentials ID to use
            config: Additional configuration

        Returns:
            Created DataLink object
        """
        params = self._build_params(workspace=workspace)

        payload: dict[str, Any] = {
            "name": name,
            "provider": provider,
            "resourceRef": resource_ref,
        }

        if description:
            payload["description"] = description
        if credentials_id:
            payload["credentialsId"] = credentials_id
        if config:
            payload["config"] = config

        response = self._client.post("/data-links", json=payload, params=params)
        link_data = response.get("dataLink", {})
        return DataLink.model_validate(link_data)

    def delete(
        self,
        data_link_id: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete a data link.

        Args:
            data_link_id: Data link ID
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        self._client.delete(f"/data-links/{data_link_id}", params=params)
