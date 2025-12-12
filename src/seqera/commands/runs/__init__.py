"""
Runs commands for Seqera CLI.

Manage workflow runs in workspaces.
"""

import sys
from typing import Annotated, Any

import typer

from seqera.exceptions import (
    AuthenticationError,
    NotFoundError,
    RunNotFoundException,
    SeqeraError,
)
from seqera.main import get_output_format, get_sdk
from seqera.responses import (
    LabelsManaged,
    MetricsList,
    RunCancelled,
    RunDeleted,
    RunDump,
    RunFileDownloaded,
    RunRelaunched,
    RunsList,
    RunView,
    TasksList,
    TaskView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create runs app
app = typer.Typer(
    name="runs",
    help="Manage workflow runs",
    no_args_is_help=True,
)

# Create view subcommand app
view_app = typer.Typer(
    name="view",
    help="View pipeline's runs",
    no_args_is_help=True,
    invoke_without_command=True,
)
app.add_typer(view_app)

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
    filter_str: Annotated[
        str | None,
        typer.Option("-f", "--filter", help="Show only runs that match the defined filter(s)"),
    ] = None,
    show_labels: Annotated[
        bool,
        typer.Option("-l", "--labels", help="Show labels"),
    ] = False,
    page: Annotated[
        int | None,
        typer.Option("--page", help="Pages to display"),
    ] = None,
    offset: Annotated[
        int | None,
        typer.Option("--offset", help="Rows record offset"),
    ] = None,
    max_results: Annotated[
        int | None,
        typer.Option("--max", help="Maximum number of records to display"),
    ] = None,
) -> None:
    """List workflow runs in workspace."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Handle page vs offset conflict
        if page is not None and offset is not None:
            output_error("Cannot specify both --page and --offset")
            sys.exit(1)

        # Get workspace name for display
        workspace_name = get_workspace_ref(sdk, workspace)

        # Build search query from filter
        search = filter_str

        # Get workflows using SDK
        runs = list(sdk.runs.list(workspace=workspace, search=search))

        # Apply pagination manually if needed (SDK may not support it directly)
        if page is not None:
            page_size = max_results or 100
            start_offset = (page - 1) * page_size
            runs = runs[start_offset : start_offset + page_size]
        elif offset is not None:
            runs = runs[offset:]
            if max_results:
                runs = runs[:max_results]
        elif max_results:
            runs = runs[:max_results]

        # Convert to dicts for response formatting (mode='json' to serialize datetimes)
        workflows = []
        for run in runs:
            workflows.append({"workflow": run.model_dump(by_alias=True, mode="json")})

        # Output response
        result = RunsList(
            workspace=workspace_name,
            runs=workflows,
            show_labels=show_labels,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


@view_app.callback(invoke_without_command=True)
def view_run(
    ctx: typer.Context,
    run_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Pipeline run identifier"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
    ] = None,
    show_config: Annotated[
        bool,
        typer.Option("--config", help="Display pipeline run configuration"),
    ] = False,
    show_params: Annotated[
        bool,
        typer.Option("--params", help="Display pipeline run parameters"),
    ] = False,
    show_command: Annotated[
        bool,
        typer.Option("--command", help="Display pipeline run command"),
    ] = False,
    show_status: Annotated[
        bool,
        typer.Option("--status", help="Display pipeline run status"),
    ] = False,
    show_processes: Annotated[
        bool,
        typer.Option("--processes", help="Display pipeline run processes"),
    ] = False,
    show_stats: Annotated[
        bool,
        typer.Option("--stats", help="Display pipeline run stats"),
    ] = False,
    show_load: Annotated[
        bool,
        typer.Option("--load", help="Display pipeline run load"),
    ] = False,
    show_utilization: Annotated[
        bool,
        typer.Option("--utilization", help="Display pipeline run utilization"),
    ] = False,
    show_metrics_memory: Annotated[
        bool,
        typer.Option("--metrics-memory", help="Display pipeline run memory metrics"),
    ] = False,
    show_metrics_cpu: Annotated[
        bool,
        typer.Option("--metrics-cpu", help="Display pipeline run CPU metrics"),
    ] = False,
    show_metrics_time: Annotated[
        bool,
        typer.Option("--metrics-time", help="Display pipeline run job time metrics"),
    ] = False,
    show_metrics_io: Annotated[
        bool,
        typer.Option("--metrics-io", help="Display pipeline run I/O metrics"),
    ] = False,
) -> None:
    """View pipeline's runs."""
    # If a subcommand was invoked, pass along context and return
    if ctx.invoked_subcommand is not None:
        # Store run_id and workspace in context for subcommands
        ctx.obj = {"run_id": run_id, "workspace": workspace}
        return

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

        # Get launch info if needed for config/params/command
        launch_info = None
        if show_config or show_params or show_command:
            client = sdk._http_client
            params = {}
            if workspace:
                params["workspaceId"] = workspace
            try:
                launch_response = client.get(f"/workflow/{run_id}/launch", params=params)
                launch_info = launch_response.get("launch", {})
            except Exception:
                launch_info = {}

        # Build response based on what options are requested
        if show_config:
            config_text = launch_info.get("configText", "") if launch_info else ""
            output_console(f"\nPipeline run configuration:\n\n{config_text}")
            return

        if show_params:
            params_text = launch_info.get("paramsText", "") if launch_info else ""
            output_console(f"\nPipeline run parameters:\n\n{params_text}")
            return

        if show_command:
            command_line = run.command_line or ""
            output_console(f"\nPipeline run command:\n\n{command_line}")
            return

        if show_status:
            # Display status with task counts
            pending = workflow_progress.get("pending", 0)
            submitted = workflow_progress.get("submitted", 0)
            running = workflow_progress.get("running", 0)
            succeeded = workflow_progress.get("succeeded", 0)
            failed = workflow_progress.get("failed", 0)
            cached = workflow_progress.get("cached", 0)
            output_console(
                f"\nPipeline run status: {run.status}\n\n"
                f"  Pending:   {pending}\n"
                f"  Submitted: {submitted}\n"
                f"  Running:   {running}\n"
                f"  Succeeded: {succeeded}\n"
                f"  Failed:    {failed}\n"
                f"  Cached:    {cached}\n"
            )
            return

        if show_processes:
            processes = workflow_progress.get("processes", [])
            output_console("\nPipeline run processes:\n")
            for proc in processes:
                name = proc.get("name", "")
                pending = proc.get("pending", 0)
                running = proc.get("running", 0)
                succeeded = proc.get("succeeded", 0)
                failed = proc.get("failed", 0)
                output_console(
                    f"  {name}: pending={pending}, running={running}, succeeded={succeeded}, failed={failed}"
                )
            return

        if show_stats:
            stats = run.stats or {}
            output_console(f"\nPipeline run stats:\n\n{stats}")
            return

        if show_load:
            load_cpus = workflow_progress.get("loadCpus", 0)
            load_memory = workflow_progress.get("loadMemory", 0)
            peak_cpus = workflow_progress.get("peakCpus", 0)
            peak_memory = workflow_progress.get("peakMemory", 0)
            output_console(
                f"\nPipeline run load:\n\n"
                f"  Load CPUs:    {load_cpus}\n"
                f"  Load Memory:  {load_memory}\n"
                f"  Peak CPUs:    {peak_cpus}\n"
                f"  Peak Memory:  {peak_memory}\n"
            )
            return

        if show_utilization:
            # Get metrics for utilization
            metrics = sdk.runs.metrics(run_id, workspace=workspace)
            output_console(f"\nPipeline run utilization:\n\n{metrics}")
            return

        if show_metrics_memory or show_metrics_cpu or show_metrics_time or show_metrics_io:
            metrics = sdk.runs.metrics(run_id, workspace=workspace)
            metric_type = "all"
            if show_metrics_memory:
                metric_type = "memory"
            elif show_metrics_cpu:
                metric_type = "cpu"
            elif show_metrics_time:
                metric_type = "time"
            elif show_metrics_io:
                metric_type = "io"
            output_console(f"\nPipeline run {metric_type} metrics:\n\n{metrics}")
            return

        # Default: show general info
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


@view_app.command("tasks")
def view_tasks(
    ctx: typer.Context,
    run_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Pipeline run identifier"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
    ] = None,
    columns: Annotated[
        str | None,
        typer.Option(
            "-c",
            "--columns",
            help="Additional task columns to display: taskId, process, tag, status, hash, exit, container, nativeId, submit, duration, realtime, pcpu, pmem, peakRss, peakVmem, rchar, wchar, volCtxt, invCtxt",
        ),
    ] = None,
    filter_str: Annotated[
        str | None,
        typer.Option(
            "-f", "--filter", help="Only show task with parameters that start with the given word"
        ),
    ] = None,
    page: Annotated[
        int | None,
        typer.Option("--page", help="Pages to display"),
    ] = None,
    offset: Annotated[
        int | None,
        typer.Option("--offset", help="Rows record offset"),
    ] = None,
    max_results: Annotated[
        int | None,
        typer.Option("--max", help="Maximum number of records to display"),
    ] = None,
) -> None:
    """Display pipeline's run tasks."""
    try:
        # Get run_id from context if not provided directly
        if ctx.obj and not run_id:
            run_id = ctx.obj.get("run_id")
            workspace = workspace or ctx.obj.get("workspace")

        if not run_id:
            output_error("Pipeline run ID is required (use -i/--id)")
            sys.exit(1)

        sdk = get_sdk()
        output_format = get_output_format()

        # Get tasks using SDK
        tasks_list = list(sdk.runs.tasks(run_id, workspace=workspace))

        # Apply filter if specified
        if filter_str:
            tasks_list = [t for t in tasks_list if filter_str.lower() in (t.process or "").lower()]

        # Apply pagination
        if page is not None:
            page_size = max_results or 100
            start_offset = (page - 1) * page_size
            tasks_list = tasks_list[start_offset : start_offset + page_size]
        elif offset is not None:
            tasks_list = tasks_list[offset:]
            if max_results:
                tasks_list = tasks_list[:max_results]
        elif max_results:
            tasks_list = tasks_list[:max_results]

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


@view_app.command("metrics")
def view_metrics(
    ctx: typer.Context,
    run_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Pipeline run identifier"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
    ] = None,
    filter_str: Annotated[
        str | None,
        typer.Option("-f", "--filter", help="Filters by process name"),
    ] = None,
    metric_type: Annotated[
        str | None,
        typer.Option(
            "-t", "--type", help="Process metric types separated by comma: cpu, mem, time, io"
        ),
    ] = None,
    columns: Annotated[
        str | None,
        typer.Option(
            "-c", "--columns", help="Process metric columns to display: mean, min, q1, q2, q3, max"
        ),
    ] = None,
    view_mode: Annotated[
        str | None,
        typer.Option("-v", "--view", help="Metric table view mode: expanded, condensed"),
    ] = None,
) -> None:
    """Display pipeline's run metrics."""
    try:
        # Get run_id from context if not provided directly
        if ctx.obj and not run_id:
            run_id = ctx.obj.get("run_id")
            workspace = workspace or ctx.obj.get("workspace")

        if not run_id:
            output_error("Pipeline run ID is required (use -i/--id)")
            sys.exit(1)

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


@app.command("relaunch")
def relaunch_run(
    run_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Pipeline run ID to relaunch"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    pipeline: Annotated[
        str | None,
        typer.Option("--pipeline", help="Pipeline to launch"),
    ] = None,
    no_resume: Annotated[
        bool,
        typer.Option("--no-resume", help="Do not resume the pipeline run"),
    ] = False,
    name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Custom workflow run name"),
    ] = None,
    work_dir: Annotated[
        str | None,
        typer.Option("--work-dir", help="Work directory"),
    ] = None,
    compute_env: Annotated[
        str | None,
        typer.Option("-c", "--compute-env", help="Compute environment name or ID"),
    ] = None,
    revision: Annotated[
        str | None,
        typer.Option("-r", "--revision", help="Pipeline revision"),
    ] = None,
    params_file: Annotated[
        str | None,
        typer.Option("-p", "--params-file", help="Parameters file (JSON/YAML)"),
    ] = None,
    config: Annotated[
        str | None,
        typer.Option("--config", help="Nextflow config file"),
    ] = None,
    pre_run: Annotated[
        str | None,
        typer.Option("--pre-run", help="Pre-run script file"),
    ] = None,
    post_run: Annotated[
        str | None,
        typer.Option("--post-run", help="Post-run script file"),
    ] = None,
    profile: Annotated[
        str | None,
        typer.Option("--profile", help="Nextflow profile(s) (comma-separated)"),
    ] = None,
    launch_container: Annotated[
        str | None,
        typer.Option(
            "--launch-container", help="Container to be used to run the Nextflow head job (BETA)"
        ),
    ] = None,
    pull_latest: Annotated[
        bool | None,
        typer.Option("--pull-latest", help="Pull latest repository version before running"),
    ] = None,
    stub_run: Annotated[
        bool | None,
        typer.Option(
            "--stub-run", help="Execute workflow replacing process scripts with command stubs"
        ),
    ] = None,
    main_script: Annotated[
        str | None,
        typer.Option("--main-script", help="Pipeline main script file if different from main.nf"),
    ] = None,
    entry_name: Annotated[
        str | None,
        typer.Option(
            "--entry-name", help="Main workflow name to be executed when using DLS2 syntax"
        ),
    ] = None,
    schema_name: Annotated[
        str | None,
        typer.Option("--schema-name", help="Schema name"),
    ] = None,
    user_secrets: Annotated[
        str | None,
        typer.Option(
            "--user-secrets", help="User secrets (comma-separated) for the pipeline execution"
        ),
    ] = None,
    workspace_secrets: Annotated[
        str | None,
        typer.Option(
            "--workspace-secrets",
            help="Workspace secrets (comma-separated) for the pipeline execution",
        ),
    ] = None,
) -> None:
    """Relaunch a pipeline run."""
    from pathlib import Path

    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Validate work-dir and no-resume combination
        if work_dir and not no_resume:
            output_error(
                "Not allowed to change '--work-dir' option when resuming. "
                "Use '--no-resume' if you want to relaunch into a different working directory without resuming."
            )
            sys.exit(1)

        # Get workspace name for display
        workspace_name = get_workspace_ref(sdk, workspace)

        # Get original workflow run
        original_run = sdk.runs.get(run_id, workspace=workspace)

        # Get launch info for the original run
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        launch_response = client.get(f"/workflow/{run_id}/launch", params=params)
        launch = launch_response.get("launch", {})

        # Check if resume is possible
        if not launch.get("resumeCommitId"):
            no_resume = True

        # Determine compute env ID
        compute_env_id = None
        if compute_env:
            ce = sdk.compute_envs.get(compute_env, workspace=workspace)
            compute_env_id = ce.id
        else:
            compute_env_id = launch.get("computeEnv", {}).get("id")

        # Determine work directory
        if work_dir:
            final_work_dir = work_dir
        elif not no_resume and launch.get("resumeDir"):
            final_work_dir = launch.get("resumeDir")
        elif launch.get("workDir"):
            final_work_dir = launch.get("workDir")
        else:
            final_work_dir = original_run.work_dir

        # Determine revision
        if revision:
            final_revision = revision
        elif not no_resume and launch.get("resumeCommitId"):
            final_revision = launch.get("resumeCommitId")
        else:
            final_revision = launch.get("revision")

        # Read files if provided
        params_text = None
        if params_file:
            params_text = Path(params_file).read_text()
        elif launch.get("paramsText"):
            params_text = launch.get("paramsText")

        config_text = None
        if config:
            config_text = Path(config).read_text()
        elif launch.get("configText"):
            config_text = launch.get("configText")

        pre_run_script = None
        if pre_run:
            pre_run_script = Path(pre_run).read_text()
        elif launch.get("preRunScript"):
            pre_run_script = launch.get("preRunScript")

        post_run_script = None
        if post_run:
            post_run_script = Path(post_run).read_text()
        elif launch.get("postRunScript"):
            post_run_script = launch.get("postRunScript")

        # Config profiles
        config_profiles = None
        if profile:
            config_profiles = profile.split(",")
        elif launch.get("configProfiles"):
            config_profiles = launch.get("configProfiles")

        # Build launch request
        launch_request = {
            "id": original_run.launch_id,
            "sessionId": launch.get("sessionId") if no_resume else original_run.session_id,
            "computeEnvId": compute_env_id,
            "pipeline": pipeline or launch.get("pipeline"),
            "workDir": final_work_dir,
            "revision": final_revision,
            "configProfiles": config_profiles,
            "configText": config_text,
            "paramsText": params_text,
            "preRunScript": pre_run_script,
            "postRunScript": post_run_script,
            "mainScript": main_script if main_script else launch.get("mainScript"),
            "entryName": entry_name if entry_name else launch.get("entryName"),
            "schemaName": schema_name if schema_name else launch.get("schemaName"),
            "resume": not no_resume,
            "pullLatest": pull_latest if pull_latest is not None else launch.get("pullLatest"),
            "stubRun": stub_run if stub_run is not None else launch.get("stubRun"),
        }

        # Handle launch container (BETA)
        if launch_container:
            launch_request["headJobContainer"] = launch_container
        elif launch.get("headJobContainer"):
            launch_request["headJobContainer"] = launch.get("headJobContainer")

        # Handle secrets
        if user_secrets:
            secret_names = [s.strip() for s in user_secrets.split(",")]
            launch_request["userSecrets"] = secret_names
        elif launch.get("userSecrets"):
            launch_request["userSecrets"] = launch.get("userSecrets")

        if workspace_secrets:
            secret_names = [s.strip() for s in workspace_secrets.split(",")]
            launch_request["workspaceSecrets"] = secret_names
        elif launch.get("workspaceSecrets"):
            launch_request["workspaceSecrets"] = launch.get("workspaceSecrets")

        if name:
            launch_request["runName"] = name

        # Submit the workflow
        submit_request = {"launch": launch_request}
        response = client.post("/workflow/launch", json=submit_request, params=params)

        new_workflow_id = response.get("workflowId")

        # Build watch URL
        watch_url = None
        if workspace:
            # Get org and workspace names
            for ws in sdk.workspaces.list():
                if str(ws.workspace_id) == str(workspace):
                    watch_url = f"{sdk._http_client.base_url}/orgs/{ws.org_name}/workspaces/{ws.workspace_name}/watch/{new_workflow_id}"
                    break

        # Output response
        result = RunRelaunched(
            run_id=new_workflow_id,
            workspace=workspace_name,
            watch_url=watch_url,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


@app.command("dump")
def dump_run(
    run_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Pipeline run identifier"),
    ],
    output_file: Annotated[
        str,
        typer.Option("-o", "--output", help="Output file (.tar.xz or .tar.gz)"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    add_task_logs: Annotated[
        bool,
        typer.Option("--add-task-logs", help="Add all task stdout, stderr and log files"),
    ] = False,
    add_fusion_logs: Annotated[
        bool,
        typer.Option("--add-fusion-logs", help="Add all Fusion task logs"),
    ] = False,
    only_failed: Annotated[
        bool,
        typer.Option("--only-failed", help="Dump only failed tasks"),
    ] = False,
    silent: Annotated[
        bool,
        typer.Option("--silent", help="Do not show download progress"),
    ] = False,
) -> None:
    """Dump all logs and details of a run into a compressed tarball file."""
    import json
    import tarfile
    from pathlib import Path

    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Validate output file format
        if not (output_file.endswith(".tar.xz") or output_file.endswith(".tar.gz")):
            output_error("Output file must end with .tar.xz or .tar.gz")
            sys.exit(1)

        compression = "xz" if output_file.endswith(".tar.xz") else "gz"
        mode = f"w:{compression}"

        # Get workspace name for display
        workspace_name = get_workspace_ref(sdk, workspace)

        if not silent:
            typer.echo(f"Dumping run {run_id} from {workspace_name}...")

        # Get workflow details
        run = sdk.runs.get(run_id, workspace=workspace)

        # Get progress info
        progress = sdk.runs.progress(run_id, workspace=workspace)

        # Get metrics
        metrics = sdk.runs.metrics(run_id, workspace=workspace)

        # Get tasks
        tasks_list = list(sdk.runs.tasks(run_id, workspace=workspace))
        if only_failed:
            tasks_list = [t for t in tasks_list if t.status == "FAILED"]

        # Create tarball
        with tarfile.open(output_file, mode) as tar:
            # Add workflow metadata
            workflow_data = run.model_dump(by_alias=True, mode="json")
            workflow_json = json.dumps(workflow_data, indent=2)
            _add_string_to_tar(tar, "workflow.json", workflow_json)

            # Add progress
            progress_json = json.dumps(progress, indent=2)
            _add_string_to_tar(tar, "progress.json", progress_json)

            # Add metrics
            metrics_json = json.dumps(metrics, indent=2)
            _add_string_to_tar(tar, "metrics.json", metrics_json)

            # Add tasks
            tasks_data = [t.model_dump(by_alias=True, mode="json") for t in tasks_list]
            tasks_json = json.dumps(tasks_data, indent=2)
            _add_string_to_tar(tar, "tasks.json", tasks_json)

            if not silent:
                typer.echo(f"  Added workflow metadata, {len(tasks_list)} tasks")

            # Add task logs if requested
            if add_task_logs:
                if not silent:
                    typer.echo("  Fetching task logs...")
                # Note: Task log fetching would require additional API calls
                # This is a simplified implementation

        # Output response
        result = RunDump(
            run_id=run_id,
            output_file=output_file,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


def _add_string_to_tar(tar: Any, name: str, content: str) -> None:
    """Add a string as a file to a tarball."""
    import io
    import tarfile

    data = content.encode("utf-8")
    info = tarfile.TarInfo(name=name)
    info.size = len(data)
    tar.addfile(info, io.BytesIO(data))


@view_app.command("task")
def view_task(
    ctx: typer.Context,
    task_id: Annotated[
        int | None,
        typer.Option("-t", help="Pipeline's run task identifier"),
    ] = None,
    run_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Pipeline run identifier"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
    ] = None,
    execution_time: Annotated[
        bool,
        typer.Option("--execution-time", help="Task execution time data"),
    ] = False,
    resources_requested: Annotated[
        bool,
        typer.Option("--resources-requested", help="Task requested resources data"),
    ] = False,
    resources_usage: Annotated[
        bool,
        typer.Option("--resources-usage", help="Task resources usage data"),
    ] = False,
) -> None:
    """Display pipeline's run task details."""
    try:
        # Get run_id from context if not provided directly
        if ctx.obj and not run_id:
            run_id = ctx.obj.get("run_id")
            workspace = workspace or ctx.obj.get("workspace")

        if not run_id:
            output_error("Pipeline run ID is required (use -i/--id)")
            sys.exit(1)

        if task_id is None:
            output_error("Task ID is required (use -t)")
            sys.exit(1)

        sdk = get_sdk()
        output_format = get_output_format()

        # Get task details using API client directly
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        response = client.get(f"/workflow/{run_id}/task/{task_id}", params=params)
        task = response.get("task", {})

        # Output response
        result = TaskView(
            run_id=run_id,
            task=task,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


def parse_labels(labels_str: str) -> list[dict]:
    """Parse comma-separated labels into a list of dicts.

    Supports format: name=value or just name (for simple labels).
    """
    labels = []
    for label in labels_str.split(","):
        label = label.strip()
        if not label:
            continue
        if "=" in label:
            name, value = label.split("=", 1)
            labels.append({"name": name.strip(), "value": value.strip()})
        else:
            labels.append({"name": label})
    return labels


def find_or_create_label_ids(
    client,
    labels: list[dict],
    workspace_id: str | None,
    no_create: bool = False,
    operation: str = "set",
) -> list[int]:
    """Find label IDs, optionally creating labels that don't exist."""
    params = {"type": "all", "max": 1000}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/labels", params=params)
    existing_labels = response.get("labels", [])

    label_map = {}
    for existing in existing_labels:
        key = existing.get("name")
        if existing.get("value"):
            key = f"{key}={existing.get('value')}"
        label_map[key] = existing.get("id")

    label_ids = []
    for label in labels:
        name = label.get("name")
        value = label.get("value")
        key = f"{name}={value}" if value else name

        if key in label_map:
            label_ids.append(label_map[key])
        elif operation == "delete":
            continue
        elif no_create:
            raise SeqeraError(f"Label '{key}' not found and --no-create specified")
        else:
            payload = {"name": name, "resource": value is not None}
            if value:
                payload["value"] = value

            create_params = {}
            if workspace_id:
                create_params["workspaceId"] = workspace_id

            create_response = client.post("/labels", json=payload, params=create_params)
            label_ids.append(create_response.get("id"))

    return label_ids


@view_app.command("download")
def view_download(
    ctx: typer.Context,
    run_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Pipeline run identifier"),
    ] = None,
    file_type: Annotated[
        str,
        typer.Option(
            "--type",
            help="File type to download: stdout, log, stderr (tasks only), or timeline (workflow only)",
        ),
    ] = "stdout",
    task_id: Annotated[
        int | None,
        typer.Option("-t", "--task", help="Task identifier (for task-specific files)"),
    ] = None,
    output_path: Annotated[
        str | None,
        typer.Option(
            "-o", "--output", help="Output file path (optional, defaults to current directory)"
        ),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
    ] = None,
) -> None:
    """Download a pipeline's run related files."""
    from pathlib import Path

    try:
        # Get run_id from context if not provided directly
        if ctx.obj and not run_id:
            run_id = ctx.obj.get("run_id")
            workspace = workspace or ctx.obj.get("workspace")

        if not run_id:
            output_error("Pipeline run ID is required (use -i/--id)")
            sys.exit(1)

        sdk = get_sdk()
        output_format = get_output_format()

        # Validate file type
        valid_types = ["stdout", "log", "stderr", "timeline"]
        if file_type not in valid_types:
            output_error(
                f"Invalid file type '{file_type}'. Valid options: {', '.join(valid_types)}"
            )
            sys.exit(1)

        # Download the file using SDK
        content = sdk.runs.download_log(
            run_id,
            workspace=workspace,
            file_type=file_type,
            task_id=task_id,
        )

        # Determine output filename
        if output_path:
            file_path = Path(output_path)
        else:
            if task_id is None:
                # Workflow files
                if file_type == "stdout":
                    file_path = Path(f"nf-{run_id}.txt")
                elif file_type == "log":
                    file_path = Path(f"nf-{run_id}.log")
                elif file_type == "timeline":
                    file_path = Path(f"timeline-{run_id}.html")
                else:
                    file_path = Path(f"nf-{run_id}.txt")
            else:
                # Task files
                if file_type == "stdout":
                    file_path = Path(f"task-{task_id}.command.out")
                elif file_type == "log":
                    file_path = Path(f"task-{task_id}.command.log")
                elif file_type == "stderr":
                    file_path = Path(f"task-{task_id}.command.err")
                else:
                    file_path = Path(f"task-{task_id}.out")

        # Write the file
        file_path.write_bytes(content)

        # Output response
        result = RunFileDownloaded(
            file_path=str(file_path),
            file_type=file_type,
            run_id=run_id,
            task_id=task_id,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)


@app.command("labels")
def manage_run_labels(
    run_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Workflow run ID"),
    ],
    labels: Annotated[
        str,
        typer.Argument(help="Comma-separated list of labels (format: name or name=value)"),
    ] = "",
    operation: Annotated[
        str,
        typer.Option("-o", "--operation", help="Operation type: set, append, or delete"),
    ] = "set",
    no_create: Annotated[
        bool,
        typer.Option("--no-create", help="Don't create labels that don't exist"),
    ] = False,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Manage labels for a workflow run."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        if not labels:
            output_error("Labels argument is required")
            sys.exit(1)

        if operation not in ("set", "append", "delete"):
            output_error("Operation must be 'set', 'append', or 'delete'")
            sys.exit(1)

        # Parse labels
        parsed_labels = parse_labels(labels)
        if not parsed_labels:
            output_error("No valid labels provided")
            sys.exit(1)

        # Use underlying client for labels operations
        client = sdk._http_client

        # Find or create label IDs
        label_ids = find_or_create_label_ids(client, parsed_labels, workspace, no_create, operation)

        if not label_ids:
            output_error("No labels to apply")
            sys.exit(1)

        # Build request
        request_payload = {
            "labelIds": label_ids,
            "workflowIds": [run_id],
        }

        params = {}
        if workspace:
            params["workspaceId"] = workspace

        # Apply labels based on operation
        if operation == "set":
            client.post("/labels/workflows/apply", json=request_payload, params=params)
        elif operation == "append":
            client.post("/labels/workflows/add", json=request_payload, params=params)
        elif operation == "delete":
            client.post("/labels/workflows/remove", json=request_payload, params=params)

        # Output response
        result = LabelsManaged(
            operation=operation,
            entity_type="workflow",
            entity_id=run_id,
            workspace_id=int(workspace) if workspace else None,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_runs_error(e)
