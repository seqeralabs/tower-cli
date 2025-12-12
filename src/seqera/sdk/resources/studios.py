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

    def templates(
        self,
        workspace: str | int | None = None,
    ) -> list[dict]:
        """
        List available studio templates.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            List of template dictionaries
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get("/studios/templates", params=params)
        return response.get("templates", [])

    def create(
        self,
        name: str,
        compute_env_id: str,
        template_url: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        cpu: int = 2,
        memory: int = 8192,
        gpu: int = 0,
        conda_environment: str | None = None,
        mount_data: list[dict] | None = None,
        label_ids: list[int] | None = None,
        is_private: bool = False,
        auto_start: bool = False,
    ) -> Studio:
        """
        Create a new studio.

        Args:
            name: Studio name
            compute_env_id: Compute environment ID
            template_url: Studio template repository URL
            workspace: Workspace ID or "org/workspace" reference
            description: Studio description
            cpu: Number of CPUs (default 2)
            memory: Memory in MB (default 8192)
            gpu: Number of GPUs (default 0)
            conda_environment: Conda environment YAML string
            mount_data: Data links to mount
            label_ids: Label IDs to apply
            is_private: Create as private studio
            auto_start: Start studio immediately

        Returns:
            Created Studio object
        """
        params = self._build_params(workspace=workspace)

        # Build configuration
        configuration = {
            "gpu": gpu,
            "cpu": cpu,
            "memory": memory,
        }

        if conda_environment:
            configuration["condaEnvironment"] = conda_environment

        if mount_data:
            configuration["mountData"] = mount_data

        # Build request payload
        request = {
            "name": name,
            "computeEnvId": compute_env_id,
            "dataStudioToolUrl": template_url,
            "configuration": configuration,
            "isPrivate": is_private,
        }

        if description:
            request["description"] = description

        if label_ids:
            request["labelIds"] = label_ids

        # Create studio
        params["autoStart"] = str(auto_start).lower()
        response = self._client.post("/studios", json=request, params=params)
        studio_data = response.get("studio", {})
        return Studio.model_validate(studio_data)

    def create_from_existing(
        self,
        name: str,
        parent_studio_id: str,
        workspace: str | int | None = None,
        *,
        parent_checkpoint_id: int | None = None,
        description: str | None = None,
        cpu: int | None = None,
        memory: int | None = None,
        gpu: int | None = None,
        label_ids: list[int] | None = None,
        is_private: bool = False,
        auto_start: bool = False,
    ) -> Studio:
        """
        Create a new studio from an existing studio's checkpoint.

        Args:
            name: Studio name
            parent_studio_id: Parent studio session ID
            workspace: Workspace ID or "org/workspace" reference
            parent_checkpoint_id: Specific checkpoint ID (defaults to most recent)
            description: Studio description
            cpu: Number of CPUs (defaults to parent config)
            memory: Memory in MB (defaults to parent config)
            gpu: Number of GPUs (defaults to parent config)
            label_ids: Label IDs to apply
            is_private: Create as private studio
            auto_start: Start studio immediately

        Returns:
            Created Studio object
        """
        params = self._build_params(workspace=workspace)

        # Get parent studio details
        parent = self.get(parent_studio_id, workspace=workspace)

        # Get checkpoint ID
        checkpoint_id = parent_checkpoint_id
        if not checkpoint_id:
            # Get most recent checkpoint
            checkpoints = list(self.checkpoints(parent_studio_id, workspace=workspace))
            if checkpoints:
                checkpoint_id = checkpoints[0].id

        # Get parent configuration
        parent_config = parent.configuration or {}

        # Build configuration (use provided values or fallback to parent)
        configuration = {
            "gpu": gpu if gpu is not None else getattr(parent_config, "gpu", 0),
            "cpu": cpu if cpu is not None else getattr(parent_config, "cpu", 2),
            "memory": memory if memory is not None else getattr(parent_config, "memory", 8192),
        }

        # Preserve mount data from parent
        mount_data = getattr(parent_config, "mount_data", None)
        if mount_data:
            configuration["mountData"] = mount_data

        # Build description
        final_description = description or f"Started from studio {parent.name or parent_studio_id}"

        # Build request payload
        request = {
            "name": name,
            "description": final_description,
            "computeEnvId": parent.compute_env.id if parent.compute_env else None,
            "dataStudioToolUrl": parent.template.repository if parent.template else None,
            "configuration": configuration,
            "isPrivate": is_private,
        }

        if checkpoint_id:
            request["initialCheckpointId"] = checkpoint_id

        if label_ids:
            request["labelIds"] = label_ids

        # Create studio
        params["autoStart"] = str(auto_start).lower()
        response = self._client.post("/studios", json=request, params=params)
        studio_data = response.get("studio", {})
        return Studio.model_validate(studio_data)
