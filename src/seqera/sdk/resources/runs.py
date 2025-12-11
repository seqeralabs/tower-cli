"""
Runs resource for the Seqera SDK.
"""

from __future__ import annotations

from typing import Any

from seqera.exceptions import RunNotFoundException
from seqera.models.common import PaginatedList
from seqera.models.runs import Task, Workflow
from seqera.sdk.resources.base import BaseResource


class RunsResource(BaseResource):
    """
    SDK resource for managing workflow runs.

    Workflow runs represent executions of Nextflow pipelines on the
    Seqera Platform.

    Example:
        >>> from seqera import Seqera
        >>> client = Seqera()
        >>>
        >>> # List all runs
        >>> for run in client.runs.list():
        ...     print(f"{run.run_name}: {run.status}")
        >>>
        >>> # Get a specific run
        >>> run = client.runs.get("abc123")
        >>>
        >>> # Cancel a running workflow
        >>> client.runs.cancel("abc123")
    """

    def list(
        self,
        workspace: str | int | None = None,
        *,
        search: str | None = None,
    ) -> PaginatedList[Workflow]:
        """
        List workflow runs in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference.
                Falls back to default workspace or SEQERA_WORKSPACE env var.
            search: Search filter

        Returns:
            Auto-paginating iterator of Workflow objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[Workflow], int]:
            params = self._build_params(
                workspace=workspace,
                offset=offset,
                max=limit,
            )
            if search:
                params["search"] = search

            response = self._client.get("/workflow", params=params)

            # The API returns workflows as a list of objects with 'workflow' key
            workflows_data = response.get("workflows", [])
            workflows = []
            for item in workflows_data:
                # Each item may have a 'workflow' key or be the workflow directly
                wf_data = item.get("workflow", item) if isinstance(item, dict) else item
                workflows.append(Workflow.model_validate(wf_data))

            total_size = response.get("totalSize", len(workflows))
            return workflows, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        run_id: str,
        workspace: str | int | None = None,
    ) -> Workflow:
        """
        Get a workflow run by ID.

        Args:
            run_id: Workflow run ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Workflow object

        Raises:
            RunNotFoundException: If run not found
        """
        params = self._build_params(workspace=workspace)

        try:
            response = self._client.get(f"/workflow/{run_id}", params=params)
        except Exception:
            raise RunNotFoundException(run_id, str(workspace or "user"))

        workflow_data = response.get("workflow")
        if not workflow_data:
            raise RunNotFoundException(run_id, str(workspace or "user"))

        return Workflow.model_validate(workflow_data)

    def cancel(
        self,
        run_id: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Cancel a running workflow.

        Args:
            run_id: Workflow run ID
            workspace: Workspace ID or "org/workspace" reference

        Raises:
            RunNotFoundException: If run not found
        """
        params = self._build_params(workspace=workspace)

        try:
            self._client.post(f"/workflow/{run_id}/cancel", params=params)
        except Exception:
            raise RunNotFoundException(run_id, str(workspace or "user"))

    def delete(
        self,
        run_id: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete a workflow run.

        Args:
            run_id: Workflow run ID
            workspace: Workspace ID or "org/workspace" reference

        Raises:
            RunNotFoundException: If run not found
            AuthenticationError: If unauthorized
        """
        from seqera.exceptions import AuthenticationError, NotFoundError

        params = self._build_params(workspace=workspace)

        try:
            self._client.delete(f"/workflow/{run_id}", params=params)
        except AuthenticationError:
            raise  # Re-raise authentication errors as-is
        except NotFoundError:
            raise RunNotFoundException(run_id, str(workspace or "user"))
        except Exception:
            raise RunNotFoundException(run_id, str(workspace or "user"))

    def tasks(
        self,
        run_id: str,
        workspace: str | int | None = None,
        *,
        search: str | None = None,
    ) -> PaginatedList[Task]:
        """
        List tasks for a workflow run.

        Args:
            run_id: Workflow run ID
            workspace: Workspace ID or "org/workspace" reference
            search: Search filter

        Returns:
            Auto-paginating iterator of Task objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[Task], int]:
            params = self._build_params(
                workspace=workspace,
                offset=offset,
                max=limit,
            )
            if search:
                params["search"] = search

            response = self._client.get(f"/workflow/{run_id}/tasks", params=params)

            tasks_data = response.get("tasks", [])
            tasks = []
            for item in tasks_data:
                # Each item may have a 'task' key or be the task directly
                task_data = item.get("task", item) if isinstance(item, dict) else item
                tasks.append(Task.model_validate(task_data))

            total_size = response.get("total", len(tasks))
            return tasks, total_size

        return PaginatedList(fetch_page)

    def metrics(
        self,
        run_id: str,
        workspace: str | int | None = None,
    ) -> dict[str, Any]:
        """
        Get metrics for a workflow run.

        Args:
            run_id: Workflow run ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Dictionary containing workflow metrics
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get(f"/workflow/{run_id}/metrics", params=params)
        return response.get("metrics", {})

    def progress(
        self,
        run_id: str,
        workspace: str | int | None = None,
    ) -> dict[str, Any]:
        """
        Get progress information for a workflow run.

        Args:
            run_id: Workflow run ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Dictionary containing workflow progress information
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get(f"/workflow/{run_id}/progress", params=params)
        return response.get("progress", {})

    def get_task(
        self,
        run_id: str,
        task_id: int,
        workspace: str | int | None = None,
    ) -> Task:
        """
        Get details of a single task in a workflow run.

        Args:
            run_id: Workflow run ID
            task_id: Task ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Task object with full details
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get(f"/workflow/{run_id}/task/{task_id}", params=params)
        task_data = response.get("task", {})
        return Task.model_validate(task_data)

    def relaunch(
        self,
        run_id: str,
        workspace: str | int | None = None,
        *,
        pipeline: str | None = None,
        work_dir: str | None = None,
        compute_env: str | None = None,
        resume: bool = True,
        params: dict[str, Any] | None = None,
        config_profiles: list[str] | None = None,
        pre_run_script: str | None = None,
        post_run_script: str | None = None,
    ) -> str:
        """
        Relaunch a pipeline run.

        Args:
            run_id: Workflow run ID to relaunch
            workspace: Workspace ID or "org/workspace" reference
            pipeline: Pipeline repository URL (optional, uses original if not specified)
            work_dir: Work directory (optional)
            compute_env: Compute environment name or ID (optional)
            resume: Whether to resume from previous run (default True)
            params: Pipeline parameters (optional)
            config_profiles: Nextflow config profiles (optional)
            pre_run_script: Pre-run script (optional)
            post_run_script: Post-run script (optional)

        Returns:
            New workflow ID
        """
        query_params = self._build_params(workspace=workspace)

        # Get original run details
        original = self.get(run_id, workspace=workspace)

        # Build launch payload from original run
        launch_payload: dict[str, Any] = {
            "sessionId": original.session_id,
            "resume": resume,
        }

        # Use provided values or fall back to original
        if pipeline:
            launch_payload["pipeline"] = pipeline
        if work_dir:
            launch_payload["workDir"] = work_dir
        if compute_env:
            # Resolve compute environment ID if name provided
            launch_payload["computeEnvId"] = compute_env
        if params:
            launch_payload["paramsText"] = params
        if config_profiles:
            launch_payload["configProfiles"] = config_profiles
        if pre_run_script:
            launch_payload["preRunScript"] = pre_run_script
        if post_run_script:
            launch_payload["postRunScript"] = post_run_script

        response = self._client.post(
            "/workflow/launch",
            json={"launch": launch_payload},
            params=query_params,
        )
        return response.get("workflowId", "")

    def dump(
        self,
        run_id: str,
        workspace: str | int | None = None,
        *,
        add_task_logs: bool = False,
        add_fusion_logs: bool = False,
        only_failed: bool = False,
    ) -> dict[str, Any]:
        """
        Get all data needed to create a dump of a workflow run.

        This returns the workflow details, tasks, and optionally logs
        that can be used to create a compressed archive.

        Args:
            run_id: Workflow run ID
            workspace: Workspace ID or "org/workspace" reference
            add_task_logs: Include task logs in dump
            add_fusion_logs: Include fusion logs in dump
            only_failed: Only include failed tasks

        Returns:
            Dictionary containing workflow data, tasks, and logs
        """
        params = self._build_params(workspace=workspace)

        # Get workflow details
        workflow = self.get(run_id, workspace=workspace)

        # Get all tasks
        all_tasks = list(self.tasks(run_id, workspace=workspace))

        # Filter to failed tasks if requested
        if only_failed:
            all_tasks = [t for t in all_tasks if t.status == "FAILED"]

        # Get metrics
        metrics = self.metrics(run_id, workspace=workspace)

        # Get progress
        progress_data = self.progress(run_id, workspace=workspace)

        result = {
            "workflow": workflow.model_dump(by_alias=True, mode="json"),
            "tasks": [t.model_dump(by_alias=True, mode="json") for t in all_tasks],
            "metrics": metrics,
            "progress": progress_data,
        }

        # Optionally get task logs
        if add_task_logs:
            task_logs = {}
            for task in all_tasks:
                try:
                    log_response = self._client.get(
                        f"/workflow/{run_id}/log/{task.task_id}",
                        params=params,
                    )
                    task_logs[str(task.task_id)] = log_response
                except Exception:
                    pass  # Skip tasks without logs
            result["task_logs"] = task_logs

        return result
