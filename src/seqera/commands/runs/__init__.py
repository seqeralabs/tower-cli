"""
Runs commands for Seqera CLI.

Manage workflow runs in workspaces.
"""

import sys
from typing import Annotated

import typer

from seqera.exceptions import (
    AuthenticationError,
    NotFoundError,
    RunNotFoundException,
    SeqeraError,
)
from seqera.main import get_sdk, get_output_format
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


def get_workspace_ref(sdk, workspace_id: str | None) -> str:
    """Get workspace reference string for display."""
    if not workspace_id:
        return USER_WORKSPACE_NAME

    # Get workspace details from user's workspaces
    for ws in sdk.workspaces.list():
        if str(ws.workspace_id) == str(workspace_id):
            return f"{ws.org_name} / {ws.workspace_name}"

    return f"workspace {workspace_id}"


@app.command("list")
def list_runs(
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """List workflow runs in workspace."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Get workspace name for display
        workspace_name = get_workspace_ref(sdk, workspace)

        # Get workflows using SDK
        runs = list(sdk.runs.list(workspace=workspace))

        # Convert to dicts for response formatting (mode='json' to serialize datetimes)
        workflows = []
        for run in runs:
            workflows.append({"workflow": run.model_dump(by_alias=True, mode="json")})

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
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """View workflow run details."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Get workspace name for display
        workspace_name = get_workspace_ref(sdk, workspace)

        # Get workflow using SDK
        run = sdk.runs.get(run_id, workspace=workspace)

        # Get progress info
        progress = sdk.runs.progress(run_id, workspace=workspace)
        workflow_progress = progress.get("workflowProgress", {})

        # Build general info
        general = {
            "id": run.id,
            "runName": run.run_name,
            "startingDate": run.start.isoformat() if run.start else None,
            "commitId": run.commit_id,
            "sessionId": run.session_id,
            "username": run.user_name,
            "workdir": run.work_dir,
            "container": run.container,
            "executors": (
                ", ".join(workflow_progress.get("executors", []))
                if workflow_progress.get("executors")
                else None
            ),
            "nextflowVersion": (run.nextflow.version if run.nextflow else None),
            "status": run.status,
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
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Delete a workflow run."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Get workspace name for display
        workspace_name = get_workspace_ref(sdk, workspace)

        # Delete workflow using SDK
        sdk.runs.delete(run_id, workspace=workspace)

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
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Cancel a running workflow."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Get workspace name for display
        workspace_name = get_workspace_ref(sdk, workspace)

        # Cancel workflow using SDK
        sdk.runs.cancel(run_id, workspace=workspace)

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
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """List tasks for a workflow run."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Get tasks using SDK
        tasks_list = list(sdk.runs.tasks(run_id, workspace=workspace))

        # Convert to dicts for response formatting (mode='json' to serialize datetimes)
        tasks = [task.model_dump(by_alias=True, mode="json") for task in tasks_list]

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
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """View workflow run metrics."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Get metrics using SDK
        metrics = sdk.runs.metrics(run_id, workspace=workspace)

        # Output response
        result = MetricsList(
            run_id=run_id,
            metrics=metrics,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)
