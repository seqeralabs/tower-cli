"""
Runs commands for Seqera CLI.

Manage workflow runs in workspaces.
"""

import sys
from typing import Optional

import typer
from typing_extensions import Annotated

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    NotFoundError,
    RunNotFoundException,
    SeqeraError,
)
from seqera.main import get_client, get_output_format
from seqera.responses import (
    MetricsList,
    RunCancelled,
    RunDeleted,
    RunsList,
    RunView,
    TasksList,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create runs app
app = typer.Typer(
    name="runs",
    help="Manage workflow runs",
    no_args_is_help=True,
)

# Default workspace name
USER_WORKSPACE_NAME = "user"


def handle_runs_error(e: Exception) -> None:
    """Handle runs command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
        sys.exit(1)
    elif isinstance(e, RunNotFoundException):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, NotFoundError):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, SeqeraError):
        output_error(str(e))
        sys.exit(1)
    else:
        output_error(f"Unexpected error: {e}")
        sys.exit(1)


def output_response(response: object, output_format: OutputFormat) -> None:
    """Output a response in the specified format."""
    if output_format == OutputFormat.JSON:
        output_json(response.to_dict())
    elif output_format == OutputFormat.YAML:
        output_yaml(response.to_dict())
    else:  # console
        output_console(response.to_console())


def get_workspace_name(client: SeqeraClient) -> str:
    """Get the workspace name from user info."""
    try:
        user_response = client.get("/user-info")
        user_name = user_response.get("user", {}).get("userName", USER_WORKSPACE_NAME)
        return user_name
    except Exception:
        return USER_WORKSPACE_NAME


@app.command("list")
def list_runs(
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """List workflow runs in workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get workflows
        response = client.get("/workflow")
        workflows = response.get("workflows", [])

        # Get workspace name
        workspace_name = get_workspace_name(client)

        # Output response
        result = RunsList(
            workspace=workspace_name,
            runs=workflows,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


@app.command("view")
def view_run(
    run_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Workflow run ID"),
    ],
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """View workflow run details."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get workflow
        try:
            response = client.get(f"/workflow/{run_id}")
        except NotFoundError:
            raise RunNotFoundException(run_id, USER_WORKSPACE_NAME)

        workflow = response.get("workflow", {})
        progress = response.get("progress", {}).get("workflowProgress", {})

        # Get workspace name
        workspace_name = get_workspace_name(client)

        # Build general info
        general = {
            "id": workflow.get("id"),
            "runName": workflow.get("runName"),
            "startingDate": workflow.get("start"),
            "commitId": workflow.get("commitId"),
            "sessionId": workflow.get("sessionId"),
            "username": workflow.get("userName"),
            "workdir": workflow.get("workDir"),
            "container": workflow.get("container"),
            "executors": ", ".join(progress.get("executors", [])) if progress.get("executors") else None,
            "nextflowVersion": workflow.get("nextflow", {}).get("version") if workflow.get("nextflow") else None,
            "status": workflow.get("status"),
        }

        # Output response
        result = RunView(
            workspace=workspace_name,
            general=general,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


@app.command("delete")
def delete_run(
    run_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Workflow run ID"),
    ],
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Delete a workflow run."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Delete workflow
        try:
            client.delete(f"/workflow/{run_id}")
        except NotFoundError:
            raise RunNotFoundException(run_id, USER_WORKSPACE_NAME)

        # Get workspace name
        workspace_name = get_workspace_name(client)

        # Output response
        result = RunDeleted(
            run_id=run_id,
            workspace=workspace_name,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


@app.command("cancel")
def cancel_run(
    run_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Workflow run ID"),
    ],
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Cancel a running workflow."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Cancel workflow
        try:
            client.post(f"/workflow/{run_id}/cancel")
        except NotFoundError:
            raise RunNotFoundException(run_id, USER_WORKSPACE_NAME)

        # Get workspace name
        workspace_name = get_workspace_name(client)

        # Output response
        result = RunCancelled(
            run_id=run_id,
            workspace=workspace_name,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


@app.command("tasks")
def list_tasks(
    run_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Workflow run ID"),
    ],
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """List tasks for a workflow run."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get tasks
        response = client.get(f"/workflow/{run_id}/tasks")
        tasks_data = response.get("tasks", [])

        # Extract task objects from response
        tasks = []
        for task_data in tasks_data:
            task = task_data.get("task", {})
            tasks.append(task)

        # Output response
        result = TasksList(
            run_id=run_id,
            tasks=tasks,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


@app.command("metrics")
def view_metrics(
    run_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Workflow run ID"),
    ],
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """View workflow run metrics."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get metrics
        response = client.get(f"/workflow/{run_id}/metrics")
        metrics = response.get("metrics", [])

        # Output response
        result = MetricsList(
            run_id=run_id,
            metrics=metrics,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)
