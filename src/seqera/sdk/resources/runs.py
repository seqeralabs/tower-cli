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
