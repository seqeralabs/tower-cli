"""
Pipelines resource for the Seqera SDK.
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from seqera.exceptions import (
    MultiplePipelinesFoundException,
    NoComputeEnvironmentException,
    PipelineNotFoundException,
)
from seqera.models.common import PaginatedList
from seqera.models.pipelines import LaunchInfo, LaunchResult, Pipeline
from seqera.sdk.resources.base import BaseResource


class PipelinesResource(BaseResource):
    """
    SDK resource for managing pipelines.

    Pipelines represent pre-configured Nextflow workflows that can be
    launched on demand from the Seqera Platform.

    Example:
        >>> from seqera import Seqera
        >>> client = Seqera()
        >>>
        >>> # List all pipelines
        >>> for pipeline in client.pipelines.list():
        ...     print(f"{pipeline.name}: {pipeline.repository}")
        >>>
        >>> # Get a specific pipeline
        >>> pipeline = client.pipelines.get(123)
        >>>
        >>> # Launch a pipeline
        >>> result = client.pipelines.launch("rnaseq", params={"input": "samples.csv"})
    """

    def list(
        self,
        workspace: str | int | None = None,
        *,
        search: str | None = None,
    ) -> PaginatedList[Pipeline]:
        """
        List pipelines in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference.
                Falls back to default workspace or SEQERA_WORKSPACE env var.
            search: Search filter for pipeline names

        Returns:
            Auto-paginating iterator of Pipeline objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[Pipeline], int]:
            params = self._build_params(
                workspace=workspace,
                offset=offset,
                max=limit,
            )
            if search:
                params["search"] = search

            response = self._client.get("/pipelines", params=params)
            pipelines = [Pipeline.model_validate(p) for p in response.get("pipelines", [])]
            total_size = response.get("totalSize", len(pipelines))
            return pipelines, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        pipeline_id: int | str,
        workspace: str | int | None = None,
    ) -> Pipeline:
        """
        Get a pipeline by ID.

        Args:
            pipeline_id: Pipeline ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Pipeline object

        Raises:
            PipelineNotFoundException: If pipeline not found
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get(f"/pipelines/{pipeline_id}", params=params)

        pipeline_data = response.get("pipeline")
        if not pipeline_data:
            raise PipelineNotFoundException(str(pipeline_id), str(workspace or "user"))

        return Pipeline.model_validate(pipeline_data)

    def get_by_name(
        self,
        name: str,
        workspace: str | int | None = None,
    ) -> Pipeline:
        """
        Get a pipeline by name.

        Args:
            name: Pipeline name
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Pipeline object

        Raises:
            PipelineNotFoundException: If pipeline not found
            MultiplePipelinesFoundException: If multiple pipelines match
        """
        params = self._build_params(
            workspace=workspace,
            search=f'"{name}"',
        )

        response = self._client.get("/pipelines", params=params)
        pipelines = response.get("pipelines", [])

        if not pipelines:
            raise PipelineNotFoundException(name, str(workspace or "user"))

        if len(pipelines) > 1:
            raise MultiplePipelinesFoundException(name, str(workspace or "user"))

        return Pipeline.model_validate(pipelines[0])

    def get_launch_info(
        self,
        pipeline_id: int | str,
        workspace: str | int | None = None,
    ) -> LaunchInfo:
        """
        Get the launch configuration for a pipeline.

        Args:
            pipeline_id: Pipeline ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            LaunchInfo with pipeline configuration
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get(f"/pipelines/{pipeline_id}/launch", params=params)

        launch_data = response.get("launch", {})
        return LaunchInfo.model_validate(launch_data)

    def launch(
        self,
        pipeline: str | int,
        workspace: str | int | None = None,
        *,
        compute_env: str | None = None,
        work_dir: str | None = None,
        revision: str | None = None,
        params: dict[str, Any] | str | None = None,
        params_file: str | Path | None = None,
        config_profiles: list[str] | None = None,
        run_name: str | None = None,
        pre_run_script: str | None = None,
        post_run_script: str | None = None,
        pull_latest: bool = False,
        stub_run: bool = False,
        resume: bool = False,
        labels: list[str] | None = None,
    ) -> LaunchResult:
        """
        Launch a pipeline.

        Args:
            pipeline: Pipeline name or ID
            workspace: Workspace ID or "org/workspace" reference
            compute_env: Compute environment name (uses primary if not specified)
            work_dir: Working directory for the run
            revision: Pipeline revision (branch/tag/commit)
            params: Parameters as dict or YAML/JSON string
            params_file: Path to parameters file
            config_profiles: List of Nextflow config profiles
            run_name: Name for the workflow run
            pre_run_script: Script to run before the pipeline
            post_run_script: Script to run after the pipeline
            pull_latest: Pull latest pipeline code
            stub_run: Run in stub mode (dry run)
            resume: Resume from previous run
            labels: Labels to apply to the run

        Returns:
            LaunchResult with workflow ID

        Raises:
            PipelineNotFoundException: If pipeline not found
            NoComputeEnvironmentException: If no compute environment available
        """
        ws_id = self._get_workspace(workspace)
        workspace_ref = str(workspace or "user")

        # Resolve pipeline by name or ID
        if isinstance(pipeline, str) and not pipeline.isdigit():
            pipeline_obj = self.get_by_name(pipeline, workspace=ws_id)
            pipeline_id = pipeline_obj.pipeline_id
        else:
            pipeline_id = int(pipeline)

        # Get existing launch config
        launch_info = self.get_launch_info(pipeline_id, workspace=ws_id)

        # Get compute environment
        ce_id = launch_info.compute_env_id
        if compute_env:
            ce_id = self._get_compute_env_id(compute_env, ws_id, workspace_ref)
        elif not ce_id:
            ce_id = self._get_primary_compute_env_id(ws_id, workspace_ref)

        # Build params text
        params_text = launch_info.params_text or ""
        if params:
            if isinstance(params, dict):
                import yaml

                params_text = yaml.dump(params, default_flow_style=False)
            else:
                params_text = params

        if params_file:
            params_path = Path(params_file)
            if params_path.exists():
                params_text = params_path.read_text()

        # Build launch payload
        launch_payload: dict[str, Any] = {
            "id": launch_info.id,
            "computeEnvId": ce_id,
            "pipeline": launch_info.pipeline,
            "workDir": work_dir or launch_info.work_dir,
            "revision": revision or launch_info.revision,
            "configProfiles": config_profiles or launch_info.config_profiles or [],
            "paramsText": params_text,
            "preRunScript": pre_run_script or launch_info.pre_run_script,
            "postRunScript": post_run_script or launch_info.post_run_script,
            "pullLatest": pull_latest,
            "stubRun": stub_run,
            "resume": resume,
        }

        if run_name:
            launch_payload["runName"] = run_name

        # Add labels if specified
        if labels:
            label_ids = self._resolve_labels(labels, ws_id)
            launch_payload["labelIds"] = label_ids

        # Build request params
        post_params: dict[str, Any] = {}
        if ws_id:
            post_params["workspaceId"] = ws_id

        response = self._client.post(
            "/workflow/launch",
            json={"launch": launch_payload},
            params=post_params,
        )

        return LaunchResult.model_validate(response)

    def add(
        self,
        name: str,
        repository: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        compute_env: str | None = None,
        work_dir: str | None = None,
        revision: str | None = None,
        config_profiles: list[str] | None = None,
        params: dict[str, Any] | str | None = None,
        pre_run_script: str | None = None,
        post_run_script: str | None = None,
        labels: list[str] | None = None,
    ) -> Pipeline:
        """
        Add a new pipeline to the workspace.

        Args:
            name: Pipeline name
            repository: Git repository URL
            workspace: Workspace ID or "org/workspace" reference
            description: Pipeline description
            compute_env: Default compute environment name
            work_dir: Default working directory
            revision: Default revision (branch/tag)
            config_profiles: Default config profiles
            params: Default parameters
            pre_run_script: Default pre-run script
            post_run_script: Default post-run script
            labels: Labels to apply

        Returns:
            Created Pipeline object
        """
        ws_id = self._get_workspace(workspace)
        workspace_ref = str(workspace or "user")

        # Build launch config
        launch: dict[str, Any] = {
            "pipeline": repository,
        }

        if compute_env:
            launch["computeEnvId"] = self._get_compute_env_id(compute_env, ws_id, workspace_ref)

        if work_dir:
            launch["workDir"] = work_dir
        if revision:
            launch["revision"] = revision
        if config_profiles:
            launch["configProfiles"] = config_profiles
        if params:
            if isinstance(params, dict):
                import yaml

                launch["paramsText"] = yaml.dump(params, default_flow_style=False)
            else:
                launch["paramsText"] = params
        if pre_run_script:
            launch["preRunScript"] = pre_run_script
        if post_run_script:
            launch["postRunScript"] = post_run_script

        # Build pipeline payload
        payload: dict[str, Any] = {
            "name": name,
            "launch": launch,
        }
        if description:
            payload["description"] = description

        # Add labels if specified
        if labels:
            label_ids = self._resolve_labels(labels, ws_id)
            payload["labelIds"] = label_ids

        params_dict = self._build_params(workspace=ws_id)
        response = self._client.post("/pipelines", json=payload, params=params_dict)

        pipeline_data = response.get("pipeline", {})
        return Pipeline.model_validate(pipeline_data)

    def update(
        self,
        pipeline_id: int | str,
        workspace: str | int | None = None,
        *,
        name: str | None = None,
        description: str | None = None,
        compute_env: str | None = None,
        work_dir: str | None = None,
        revision: str | None = None,
        config_profiles: list[str] | None = None,
        params: dict[str, Any] | str | None = None,
        pre_run_script: str | None = None,
        post_run_script: str | None = None,
    ) -> Pipeline:
        """
        Update an existing pipeline.

        Args:
            pipeline_id: Pipeline ID to update
            workspace: Workspace ID or "org/workspace" reference
            name: New pipeline name
            description: New description
            compute_env: New compute environment
            work_dir: New working directory
            revision: New revision
            config_profiles: New config profiles
            params: New parameters
            pre_run_script: New pre-run script
            post_run_script: New post-run script

        Returns:
            Updated Pipeline object
        """
        ws_id = self._get_workspace(workspace)
        workspace_ref = str(workspace or "user")

        # Get existing pipeline and launch config
        existing = self.get(pipeline_id, workspace=ws_id)
        launch_info = self.get_launch_info(pipeline_id, workspace=ws_id)

        # Build updated launch config
        launch: dict[str, Any] = {
            "id": launch_info.id,
            "pipeline": launch_info.pipeline,
            "computeEnvId": launch_info.compute_env_id,
            "workDir": launch_info.work_dir,
            "revision": launch_info.revision,
            "configProfiles": launch_info.config_profiles or [],
            "paramsText": launch_info.params_text or "",
            "preRunScript": launch_info.pre_run_script or "",
            "postRunScript": launch_info.post_run_script or "",
        }

        if compute_env:
            launch["computeEnvId"] = self._get_compute_env_id(compute_env, ws_id, workspace_ref)
        if work_dir is not None:
            launch["workDir"] = work_dir
        if revision is not None:
            launch["revision"] = revision
        if config_profiles is not None:
            launch["configProfiles"] = config_profiles
        if params is not None:
            if isinstance(params, dict):
                import yaml

                launch["paramsText"] = yaml.dump(params, default_flow_style=False)
            else:
                launch["paramsText"] = params
        if pre_run_script is not None:
            launch["preRunScript"] = pre_run_script
        if post_run_script is not None:
            launch["postRunScript"] = post_run_script

        # Build pipeline payload
        payload: dict[str, Any] = {
            "name": name or existing.name,
            "launch": launch,
        }
        if description is not None:
            payload["description"] = description
        elif existing.description:
            payload["description"] = existing.description

        params_dict = self._build_params(workspace=ws_id)
        response = self._client.put(f"/pipelines/{pipeline_id}", json=payload, params=params_dict)

        pipeline_data = response.get("pipeline", {})
        return Pipeline.model_validate(pipeline_data)

    def delete(
        self,
        pipeline_id: int | str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete a pipeline.

        Args:
            pipeline_id: Pipeline ID to delete
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        self._client.delete(f"/pipelines/{pipeline_id}", params=params)

    def export_config(
        self,
        pipeline_id: int | str,
        workspace: str | int | None = None,
    ) -> dict[str, Any]:
        """
        Export pipeline configuration.

        Args:
            pipeline_id: Pipeline ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Pipeline configuration as dictionary
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get(f"/pipelines/{pipeline_id}/launch", params=params)
        return response.get("launch", {})

    def _get_compute_env_id(
        self,
        name: str,
        workspace_id: int | None,
        workspace_ref: str,
    ) -> str:
        """Get compute environment ID by name."""
        params: dict[str, Any] = {"status": "AVAILABLE"}
        if workspace_id:
            params["workspaceId"] = workspace_id

        response = self._client.get("/compute-envs", params=params)
        for ce in response.get("computeEnvs", []):
            if ce.get("name") == name:
                return ce.get("id")

        raise NoComputeEnvironmentException(workspace_ref)

    def _get_primary_compute_env_id(
        self,
        workspace_id: int | None,
        workspace_ref: str,
    ) -> str:
        """Get primary compute environment ID."""
        params: dict[str, Any] = {"status": "AVAILABLE"}
        if workspace_id:
            params["workspaceId"] = workspace_id

        response = self._client.get("/compute-envs", params=params)
        compute_envs = response.get("computeEnvs", [])

        if not compute_envs:
            raise NoComputeEnvironmentException(workspace_ref)

        # Find primary or use first
        for ce in compute_envs:
            if ce.get("primary"):
                return ce.get("id")

        return compute_envs[0].get("id")

    def _resolve_labels(
        self,
        labels: list[str],
        workspace_id: int | None,
    ) -> list[int]:
        """Resolve label names to IDs, creating if needed."""
        params: dict[str, Any] = {}
        if workspace_id:
            params["workspaceId"] = workspace_id

        response = self._client.get("/labels", params=params)
        existing_labels = {label.get("name"): label.get("id") for label in response.get("labels", [])}

        label_ids = []
        for label in labels:
            if label in existing_labels:
                label_ids.append(existing_labels[label])
            else:
                # Create new label
                create_response = self._client.post(
                    "/labels",
                    json={"name": label},
                    params=params,
                )
                label_ids.append(create_response.get("id"))

        return label_ids
