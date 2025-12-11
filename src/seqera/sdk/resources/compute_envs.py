"""
Compute environments resource for the Seqera SDK.
"""

from __future__ import annotations

from typing import Any

from seqera.exceptions import ComputeEnvNotFoundException
from seqera.models.common import PaginatedList
from seqera.models.compute_envs import ComputeEnv
from seqera.sdk.resources.base import BaseResource


class ComputeEnvsResource(BaseResource):
    """
    SDK resource for managing compute environments.

    Compute environments define the infrastructure where Nextflow pipelines
    will be executed.
    """

    def list(
        self,
        workspace: str | int | None = None,
        *,
        status: str | None = None,
    ) -> PaginatedList[ComputeEnv]:
        """
        List compute environments in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference
            status: Filter by status (e.g., "AVAILABLE")

        Returns:
            Auto-paginating iterator of ComputeEnv objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[ComputeEnv], int]:
            params = self._build_params(workspace=workspace)
            if status:
                params["status"] = status

            response = self._client.get("/compute-envs", params=params)

            envs = [ComputeEnv.model_validate(ce) for ce in response.get("computeEnvs", [])]
            total_size = len(envs)
            paginated = envs[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        compute_env: str | int,
        workspace: str | int | None = None,
    ) -> ComputeEnv:
        """
        Get a compute environment by ID or name.

        Args:
            compute_env: Compute environment ID or name
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            ComputeEnv object

        Raises:
            ComputeEnvNotFoundException: If compute environment not found
        """
        params = self._build_params(workspace=workspace)

        # Check if it's an ID (UUID format) or name
        if self._looks_like_id(compute_env):
            response = self._client.get(f"/compute-envs/{compute_env}", params=params)
            ce_data = response.get("computeEnv", {})
            if not ce_data:
                raise ComputeEnvNotFoundException(str(compute_env), str(workspace or "user"))
            return ComputeEnv.model_validate(ce_data)

        # Look up by name
        return self.get_by_name(str(compute_env), workspace=workspace)

    def get_by_name(
        self,
        name: str,
        workspace: str | int | None = None,
    ) -> ComputeEnv:
        """
        Get a compute environment by name.

        Args:
            name: Compute environment name
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            ComputeEnv object

        Raises:
            ComputeEnvNotFoundException: If compute environment not found
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get("/compute-envs", params=params)

        for ce in response.get("computeEnvs", []):
            if ce.get("name") == name:
                # Get full details
                ce_id = ce.get("id")
                detail_response = self._client.get(f"/compute-envs/{ce_id}", params=params)
                return ComputeEnv.model_validate(detail_response.get("computeEnv", {}))

        raise ComputeEnvNotFoundException(name, str(workspace or "user"))

    def get_primary(
        self,
        workspace: str | int | None = None,
    ) -> ComputeEnv | None:
        """
        Get the primary compute environment for a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Primary ComputeEnv object, or None if not set
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get("/compute-envs", params=params)

        for ce in response.get("computeEnvs", []):
            if ce.get("primary"):
                ce_id = ce.get("id")
                detail_response = self._client.get(f"/compute-envs/{ce_id}", params=params)
                return ComputeEnv.model_validate(detail_response.get("computeEnv", {}))

        return None

    def set_primary(
        self,
        compute_env: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Set the primary compute environment for a workspace.

        Args:
            compute_env: Compute environment ID or name
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)

        # Get compute environment ID
        if self._looks_like_id(compute_env):
            ce_id = compute_env
        else:
            ce = self.get_by_name(compute_env, workspace=workspace)
            ce_id = ce.id

        self._client.post(f"/compute-envs/{ce_id}/primary", params=params)

    def delete(
        self,
        compute_env: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete a compute environment.

        Args:
            compute_env: Compute environment ID or name
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)

        # Get compute environment ID
        if self._looks_like_id(compute_env):
            ce_id = compute_env
        else:
            ce = self.get_by_name(compute_env, workspace=workspace)
            ce_id = ce.id

        self._client.delete(f"/compute-envs/{ce_id}", params=params)

    def export_config(
        self,
        compute_env: str,
        workspace: str | int | None = None,
    ) -> dict[str, Any]:
        """
        Export compute environment configuration.

        Args:
            compute_env: Compute environment ID or name
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Compute environment configuration as dictionary
        """
        ce = self.get(compute_env, workspace=workspace)
        return ce.model_dump(by_alias=True, exclude_none=True)

    def import_config(
        self,
        config: dict[str, Any],
        workspace: str | int | None = None,
        *,
        name: str | None = None,
        overwrite: bool = False,
    ) -> str:
        """
        Import a compute environment from configuration.

        Args:
            config: Compute environment configuration dictionary
            workspace: Workspace ID or "org/workspace" reference
            name: Override name in config (optional)
            overwrite: Delete existing CE with same name first

        Returns:
            New compute environment ID
        """
        params = self._build_params(workspace=workspace)

        # Extract config from wrapper if needed
        if "config" in config:
            config = config["config"]

        # Override name if specified
        ce_name = name or config.get("name")
        if not ce_name:
            raise ValueError("Compute environment name must be specified either in config or with name parameter")

        # Handle overwrite - delete existing CE if it exists
        if overwrite:
            try:
                existing = self.get_by_name(ce_name, workspace=workspace)
                self.delete(existing.id, workspace=workspace)
            except ComputeEnvNotFoundException:
                pass

        # Set the name in config
        config["name"] = ce_name

        response = self._client.post("/compute-envs", json={"computeEnv": config}, params=params)
        return response.get("computeEnvId", "")

    def _looks_like_id(self, value: str | int) -> bool:
        """Check if value looks like a compute environment ID (UUID or numeric)."""
        if isinstance(value, int):
            return True
        # UUIDs have dashes and are 36 chars, or could be a short ID
        return "-" in value or (len(value) > 10 and value.replace("-", "").isalnum())
