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

    def update(
        self,
        data_link_id: str,
        workspace: str | int | None = None,
        *,
        name: str | None = None,
        description: str | None = None,
        credentials_id: str | None = None,
    ) -> DataLink:
        """
        Update a data link.

        Args:
            data_link_id: Data link ID
            workspace: Workspace ID or "org/workspace" reference
            name: New name (optional)
            description: New description (optional)
            credentials_id: New credentials ID (optional)

        Returns:
            Updated DataLink object
        """
        params = self._build_params(workspace=workspace)

        # Get existing data link to preserve fields
        existing = self.get(data_link_id, workspace=workspace)

        payload: dict[str, Any] = {
            "name": name if name else existing.name,
            "provider": existing.provider,
            "resourceRef": existing.resource_ref,
        }

        if description is not None:
            payload["description"] = description
        elif existing.description:
            payload["description"] = existing.description

        if credentials_id is not None:
            payload["credentialsId"] = credentials_id
        elif existing.credentials_id:
            payload["credentialsId"] = existing.credentials_id

        self._client.put(f"/data-links/{data_link_id}", json=payload, params=params)

        return self.get(data_link_id, workspace=workspace)

    def browse(
        self,
        data_link_id: str,
        path: str = "",
        workspace: str | int | None = None,
    ) -> list[dict[str, Any]]:
        """
        Browse contents of a data link.

        Args:
            data_link_id: Data link ID
            path: Path within the data link to browse
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            List of files/directories in the path
        """
        params = self._build_params(workspace=workspace)
        if path:
            params["path"] = path

        response = self._client.get(f"/data-links/{data_link_id}/browse", params=params)
        return response.get("files", [])

    def get_download_url(
        self,
        data_link_id: str,
        path: str,
        credentials_id: str,
        workspace: str | int | None = None,
    ) -> str:
        """
        Get a presigned URL for downloading a file from a data link.

        Args:
            data_link_id: Data link ID
            path: Path to the file within the data link
            credentials_id: Credentials ID to use for generating the URL
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Presigned download URL
        """
        params = self._build_params(workspace=workspace)
        params["path"] = path
        params["credentialsId"] = credentials_id

        response = self._client.get(f"/data-links/{data_link_id}/download", params=params)
        return response.get("url", "")

    def get_upload_url(
        self,
        data_link_id: str,
        path: str,
        credentials_id: str,
        workspace: str | int | None = None,
        *,
        content_type: str | None = None,
        part_number: int | None = None,
        upload_id: str | None = None,
    ) -> dict[str, Any]:
        """
        Get a presigned URL for uploading a file to a data link.

        Args:
            data_link_id: Data link ID
            path: Path where the file should be uploaded
            credentials_id: Credentials ID to use for generating the URL
            workspace: Workspace ID or "org/workspace" reference
            content_type: MIME type of the file
            part_number: Part number for multipart upload
            upload_id: Upload ID for multipart upload

        Returns:
            Dictionary containing upload URL and any additional info
        """
        params = self._build_params(workspace=workspace)
        params["path"] = path
        params["credentialsId"] = credentials_id

        if content_type:
            params["contentType"] = content_type
        if part_number is not None:
            params["partNumber"] = part_number
        if upload_id:
            params["uploadId"] = upload_id

        return self._client.get(f"/data-links/{data_link_id}/upload", params=params)
