"""
Datasets resource for the Seqera SDK.
"""

from __future__ import annotations

from pathlib import Path
from typing import Any

from seqera.models.common import PaginatedList
from seqera.models.datasets import Dataset, DatasetVersion
from seqera.sdk.resources.base import BaseResource


class DatasetsResource(BaseResource):
    """
    SDK resource for managing datasets.

    Datasets are versioned data files that can be used as inputs to pipelines.
    """

    def list(
        self,
        workspace: str | int | None = None,
    ) -> PaginatedList[Dataset]:
        """
        List datasets in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of Dataset objects
        """
        ws_id = self._get_workspace(workspace)

        def fetch_page(offset: int, limit: int) -> tuple[list[Dataset], int]:
            response = self._client.get(f"/workspaces/{ws_id}/datasets")

            datasets = [Dataset.model_validate(d) for d in response.get("datasets", [])]
            total_size = len(datasets)
            paginated = datasets[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        dataset_id: str,
        workspace: str | int | None = None,
    ) -> Dataset:
        """
        Get a dataset by ID.

        Args:
            dataset_id: Dataset ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Dataset object
        """
        ws_id = self._get_workspace(workspace)
        response = self._client.get(f"/workspaces/{ws_id}/datasets/{dataset_id}/metadata")

        dataset_data = response.get("dataset", {})
        return Dataset.model_validate(dataset_data)

    def versions(
        self,
        dataset_id: str,
        workspace: str | int | None = None,
    ) -> PaginatedList[DatasetVersion]:
        """
        List versions of a dataset.

        Args:
            dataset_id: Dataset ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of DatasetVersion objects
        """
        ws_id = self._get_workspace(workspace)

        def fetch_page(offset: int, limit: int) -> tuple[list[DatasetVersion], int]:
            response = self._client.get(f"/workspaces/{ws_id}/datasets/{dataset_id}/versions")

            versions = [DatasetVersion.model_validate(v) for v in response.get("versions", [])]
            total_size = len(versions)
            paginated = versions[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def url(
        self,
        dataset_id: str,
        workspace: str | int | None = None,
        *,
        version: int | None = None,
    ) -> str:
        """
        Get the download URL for a dataset version.

        Args:
            dataset_id: Dataset ID
            workspace: Workspace ID or "org/workspace" reference
            version: Specific version number (uses latest if not specified)

        Returns:
            Download URL
        """
        ws_id = self._get_workspace(workspace)

        # Get versions to find the right one
        response = self._client.get(f"/workspaces/{ws_id}/datasets/{dataset_id}/versions")
        versions = response.get("versions", [])

        if not versions:
            raise Exception(f"No versions found for dataset {dataset_id}")

        if version:
            for v in versions:
                if v.get("version") == version:
                    return v.get("url", "")
            raise Exception(f"Version {version} not found for dataset {dataset_id}")

        # Return latest version URL
        return versions[0].get("url", "")

    def download(
        self,
        dataset_id: str,
        output_path: str | Path,
        workspace: str | int | None = None,
        *,
        version: int | None = None,
    ) -> Path:
        """
        Download a dataset to a local file.

        Args:
            dataset_id: Dataset ID
            output_path: Path to save the file
            workspace: Workspace ID or "org/workspace" reference
            version: Specific version number (uses latest if not specified)

        Returns:
            Path to downloaded file
        """
        import httpx

        url = self.url(dataset_id, workspace=workspace, version=version)
        output = Path(output_path)

        # Download the file
        with httpx.stream("GET", url) as response:
            response.raise_for_status()
            with output.open("wb") as f:
                for chunk in response.iter_bytes():
                    f.write(chunk)

        return output

    def add(
        self,
        name: str,
        file_path: str | Path,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        has_header: bool = True,
    ) -> Dataset:
        """
        Add a new dataset by uploading a file.

        Args:
            name: Dataset name
            file_path: Path to the file to upload
            workspace: Workspace ID or "org/workspace" reference
            description: Dataset description
            has_header: Whether the file has a header row

        Returns:
            Created Dataset object
        """
        ws_id = self._get_workspace(workspace)
        file_path = Path(file_path)

        # Create dataset metadata
        payload: dict[str, Any] = {
            "name": name,
            "hasHeader": has_header,
        }
        if description:
            payload["description"] = description

        response = self._client.post(f"/workspaces/{ws_id}/datasets/upload", json=payload)

        # The response contains the dataset ID and upload URL
        dataset_id = response.get("datasetId", "")

        return Dataset(
            id=dataset_id,
            name=name,
            description=description,
        )

    def update(
        self,
        dataset_id: str,
        workspace: str | int | None = None,
        *,
        name: str | None = None,
        description: str | None = None,
    ) -> Dataset:
        """
        Update dataset metadata.

        Args:
            dataset_id: Dataset ID
            workspace: Workspace ID or "org/workspace" reference
            name: New name
            description: New description

        Returns:
            Updated Dataset object
        """
        ws_id = self._get_workspace(workspace)

        # Get current dataset
        current = self.get(dataset_id, workspace=ws_id)

        payload = {
            "name": name or current.name,
        }
        if description is not None:
            payload["description"] = description
        elif current.description:
            payload["description"] = current.description

        self._client.put(f"/workspaces/{ws_id}/datasets/{dataset_id}", json=payload)

        return self.get(dataset_id, workspace=ws_id)

    def delete(
        self,
        dataset_id: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete a dataset.

        Args:
            dataset_id: Dataset ID
            workspace: Workspace ID or "org/workspace" reference
        """
        ws_id = self._get_workspace(workspace)
        self._client.delete(f"/workspaces/{ws_id}/datasets/{dataset_id}")
