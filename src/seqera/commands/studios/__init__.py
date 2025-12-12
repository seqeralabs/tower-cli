"""
Studios commands for Seqera CLI.

Manage data studios in workspaces.
"""

import sys
from typing import Annotated, Optional

import typer

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    InvalidResponseException,
    NotFoundError,
    SeqeraError,
    WorkspaceNotFoundException,
)
from seqera.main import get_client, get_output_format
from seqera.responses import (
    StudioCheckpoints,
    StudioDeleted,
    StudiosCreated,
    StudiosList,
    StudioStarted,
    StudioStopped,
    StudioView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create studios app
app = typer.Typer(
    name="studios",
    help="Manage data studios",
    no_args_is_help=True,
)


def handle_studios_error(e: Exception) -> None:
    """Handle studios command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
        sys.exit(1)
    elif isinstance(e, WorkspaceNotFoundException | NotFoundError):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, InvalidResponseException):
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


def get_workspace_info(client: SeqeraClient, workspace_id: str | None = None) -> tuple:
    """Get workspace reference and workspace ID.

    Args:
        client: Seqera API client
        workspace_id: Optional workspace ID

    Returns:
        Tuple of (workspace_ref, workspace_id)
    """
    # Get user info
    user_info = client.get("/user-info")
    user = user_info.get("user", {})
    user_id = user.get("id")
    user_name = user.get("userName", "")

    if workspace_id:
        # Get workspace details
        workspaces_response = client.get(f"/user/{user_id}/workspaces")
        orgs_and_workspaces = workspaces_response.get("orgsAndWorkspaces", [])

        # Find workspace
        workspace_id_int = int(workspace_id)
        workspace_entry = None
        for entry in orgs_and_workspaces:
            if entry.get("workspaceId") == workspace_id_int:
                workspace_entry = entry
                break

        if not workspace_entry:
            raise WorkspaceNotFoundException(workspace_id)

        org_name = workspace_entry.get("orgName", "")
        workspace_name = workspace_entry.get("workspaceName", "")
        workspace_ref = f"[{org_name} / {workspace_name}]"
        return workspace_ref, workspace_id
    else:
        # Use user workspace
        workspace_ref = f"[{user_name}]"
        return workspace_ref, None


def find_studio(
    client: SeqeraClient,
    studio_name: str | None = None,
    studio_id: str | None = None,
    workspace_id: str | None = None,
) -> tuple:
    """Find a studio by name or ID.

    Args:
        client: Seqera API client
        studio_name: Optional studio name
        studio_id: Optional studio ID
        workspace_id: Optional workspace ID

    Returns:
        Tuple of (studio, workspace_ref)
    """
    workspace_ref, ws_id = get_workspace_info(client, workspace_id)

    if studio_id:
        # Get by ID
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        response = client.get(f"/studios/{studio_id}", params=params)
        studio = response.get("studio")
        if not studio:
            raise NotFoundError(f"Studio '{studio_id}' not found in {workspace_ref}")
        return studio, workspace_ref

    elif studio_name:
        # Search by name
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        response = client.get("/studios", params=params)
        studios = response.get("studios", [])

        # Find by name
        matching_studios = [s for s in studios if s.get("name") == studio_name]

        if not matching_studios:
            raise NotFoundError(f"Studio '{studio_name}' not found in {workspace_ref}")

        if len(matching_studios) > 1:
            raise SeqeraError(
                f"Multiple studios found with name '{studio_name}' in {workspace_ref}"
            )

        return matching_studios[0], workspace_ref

    else:
        raise SeqeraError("Either studio name or ID must be specified")


@app.command("list")
def list_studios(
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
    offset: Annotated[
        int | None,
        typer.Option("--offset", help="Pagination offset"),
    ] = None,
    max_items: Annotated[
        int | None,
        typer.Option("--max", help="Maximum number of items to return"),
    ] = None,
    page: Annotated[
        int | None,
        typer.Option("--page", help="Page number (1-indexed)"),
    ] = None,
    filter: Annotated[
        str | None,
        typer.Option("--filter", help="Filter studios by search criteria"),
    ] = None,
    labels: Annotated[
        bool,
        typer.Option("--labels", help="Show labels in output"),
    ] = False,
) -> None:
    """List all studios in a workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Check for conflicting pagination options
        if page is not None and offset is not None:
            raise SeqeraError("Please use either --page or --offset as pagination parameter")

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Build params
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        # Add filter
        if filter:
            params["search"] = filter

        # Add labels attribute
        if labels:
            params["attributes"] = "labels"

        # Add pagination params
        pagination_info = None
        if page is not None:
            if max_items is None:
                max_items = 20
            offset = (page - 1) * max_items
            params["offset"] = offset
            params["max"] = max_items
            pagination_info = {"page": page, "max": max_items}
        elif offset is not None or max_items is not None:
            if offset is not None:
                params["offset"] = offset
            if max_items is not None:
                params["max"] = max_items
            pagination_info = {"offset": offset or 0, "max": max_items}

        # Get studios
        response = client.get("/studios", params=params)
        studios = response.get("studios", [])
        total_size = response.get("totalSize", 0)

        if pagination_info:
            pagination_info["totalSize"] = total_size

        # Output response
        result = StudiosList(
            workspace=workspace_ref,
            studios=studios,
            show_labels=labels,
            pagination_info=pagination_info,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("view")
def view_studio(
    studio_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Studio name"),
    ] = None,
    studio_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Studio ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """View studio details."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not studio_name and not studio_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find studio
        studio, workspace_ref = find_studio(client, studio_name, studio_id, workspace)

        # Output response
        result = StudioView(
            workspace=workspace_ref,
            studio=studio,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("start")
def start_studio(
    studio_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Studio name"),
    ] = None,
    studio_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Studio ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
    cpu: Annotated[
        int | None,
        typer.Option("--cpu", help="Number of CPUs"),
    ] = None,
    memory: Annotated[
        int | None,
        typer.Option("--memory", help="Memory in MB"),
    ] = None,
    gpu: Annotated[
        int | None,
        typer.Option("--gpu", help="Number of GPUs"),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("--description", help="Studio description"),
    ] = None,
) -> None:
    """Start a studio session."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not studio_name and not studio_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find studio
        studio, workspace_ref = find_studio(client, studio_name, studio_id, workspace)

        # Get studio ID
        session_id = studio.get("sessionId")
        studio_display_name = studio_name if studio_name else studio_id

        # Build start payload with existing configuration
        configuration = studio.get("configuration", {})
        payload_config = {
            "gpu": gpu if gpu is not None else configuration.get("gpu", 0),
            "cpu": cpu if cpu is not None else configuration.get("cpu", 2),
            "memory": memory if memory is not None else configuration.get("memory", 8192),
        }

        # Add mount data if exists
        mount_data = configuration.get("mountData")
        if mount_data:
            payload_config["mountData"] = mount_data

        payload = {
            "configuration": payload_config,
        }

        # Add description
        if description:
            payload["description"] = description
        elif studio.get("description"):
            payload["description"] = studio.get("description")

        # Start studio
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        response = client.put(f"/studios/{session_id}/start", json=payload, params=params)
        job_submitted = response.get("jobSubmitted", False)

        # Output response
        result = StudioStarted(
            session_id=session_id,
            studio_name=studio_display_name,
            workspace=workspace_ref,
            workspace_id=int(workspace) if workspace else None,
            job_submitted=job_submitted,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("stop")
def stop_studio(
    studio_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Studio name"),
    ] = None,
    studio_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Studio ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Stop a studio session."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not studio_name and not studio_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find studio if needed
        if studio_name:
            studio, workspace_ref = find_studio(client, studio_name, None, workspace)
            session_id = studio.get("sessionId")
            studio_display_name = studio_name
        else:
            workspace_ref, _ = get_workspace_info(client, workspace)
            session_id = studio_id
            studio_display_name = studio_id

        # Stop studio
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        response = client.put(f"/studios/{session_id}/stop", params=params)
        job_submitted = response.get("jobSubmitted", False)

        # Output response
        result = StudioStopped(
            session_id=session_id,
            studio_name=studio_display_name,
            workspace=workspace_ref,
            workspace_id=int(workspace) if workspace else None,
            job_submitted=job_submitted,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("delete")
def delete_studio(
    studio_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Studio name"),
    ] = None,
    studio_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Studio ID"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Delete a studio."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not studio_name and not studio_id:
            output_error("Either --name or --id must be specified")
            sys.exit(1)

        # Find studio if needed
        if studio_name:
            studio, workspace_ref = find_studio(client, studio_name, None, workspace)
            session_id = studio.get("sessionId")
        else:
            workspace_ref, _ = get_workspace_info(client, workspace)
            session_id = studio_id

        # Delete studio
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.delete(f"/studios/{session_id}", params=params)

        # Output response
        result = StudioDeleted(
            session_id=session_id,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("checkpoints")
def list_checkpoints(
    studio_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Studio ID"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
    offset: Annotated[
        int | None,
        typer.Option("--offset", help="Pagination offset"),
    ] = None,
    max_items: Annotated[
        int | None,
        typer.Option("--max", help="Maximum number of items to return"),
    ] = None,
) -> None:
    """List checkpoints for a studio."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Build params
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id
        if offset is not None:
            params["offset"] = offset
        if max_items is not None:
            params["max"] = max_items

        # Get checkpoints
        response = client.get(f"/studios/{studio_id}/checkpoints", params=params)
        checkpoints = response.get("checkpoints", [])
        total_size = response.get("totalSize", 0)

        pagination_info = None
        if offset is not None or max_items is not None:
            pagination_info = {"offset": offset or 0, "max": max_items, "totalSize": total_size}

        # Output response
        result = StudioCheckpoints(
            session_id=studio_id,
            workspace=workspace_ref,
            checkpoints=checkpoints,
            pagination_info=pagination_info,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("templates")
def list_templates(
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """List available studio templates."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Build params
        params = {}
        if ws_id:
            params["workspaceId"] = ws_id

        # Get templates
        response = client.get("/studios/templates", params=params)
        templates = response.get("templates", [])

        # Output response
        if output_format == OutputFormat.JSON:
            output_json({"workspace": workspace_ref, "templates": templates})
        elif output_format == OutputFormat.YAML:
            output_yaml({"workspace": workspace_ref, "templates": templates})
        else:
            lines = [f"  Studio templates at {workspace_ref}:", ""]
            if not templates:
                lines.append("    No templates found")
            else:
                lines.append(f"    {'ID':<12} {'Name':<30} {'Container'}")
                lines.append("    " + "-" * 70)
                for template in templates:
                    template_id = str(template.get("id", ""))[:11]
                    name = template.get("name", "")[:29]
                    container = template.get("containerImage", "")
                    lines.append(f"    {template_id:<12} {name:<30} {container}")
            output_console("\n".join(lines))

    except Exception as e:
        handle_studios_error(e)


def find_compute_env_by_name(client: SeqeraClient, name: str, workspace_id: str | None) -> dict:
    """Find a compute environment by name.

    Args:
        client: Seqera API client
        name: Compute environment name
        workspace_id: Optional workspace ID

    Returns:
        Compute environment dict

    Raises:
        SeqeraError: If compute environment not found
    """
    params = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/compute-envs", params=params)
    compute_envs = response.get("computeEnvs", [])

    for ce in compute_envs:
        if ce.get("name") == name:
            return ce

    raise SeqeraError(f"Compute environment '{name}' not found")


import time


def wait_for_studio_status(
    client: SeqeraClient,
    session_id: str,
    target_status: str,
    workspace_id: str | None,
    timeout: int = 600,
    poll_interval: int = 10,
) -> bool:
    """Wait for a studio to reach the target status.

    Args:
        client: Seqera API client
        session_id: Studio session ID
        target_status: Target status to wait for (e.g., RUNNING)
        workspace_id: Optional workspace ID
        timeout: Maximum time to wait in seconds (default 600)
        poll_interval: Time between status checks in seconds (default 10)

    Returns:
        True if target status reached, False if timeout or terminal error state
    """
    terminal_states = ["STOPPED", "ERRORED", "DELETED"]
    target_upper = target_status.upper()
    start_time = time.time()

    while time.time() - start_time < timeout:
        params = {}
        if workspace_id:
            params["workspaceId"] = workspace_id

        try:
            response = client.get(f"/studios/{session_id}", params=params)
            studio = response.get("studio", {})
            current_status = studio.get("status", "").upper()

            if current_status == target_upper:
                return True

            if current_status in terminal_states and current_status != target_upper:
                return False

        except Exception:
            pass

        time.sleep(poll_interval)

    return False


def find_data_link_by_name(
    client: SeqeraClient, name: str, workspace_id: str | None
) -> dict | None:
    """Find a data link by name.

    Args:
        client: Seqera API client
        name: Data link name
        workspace_id: Optional workspace ID

    Returns:
        Data link dict or None if not found
    """
    params = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/data-links", params=params)
    data_links = response.get("dataLinks", [])

    for dl in data_links:
        if dl.get("name") == name:
            return dl

    return None


def find_data_link_by_uri(client: SeqeraClient, uri: str, workspace_id: str | None) -> dict | None:
    """Find a data link by resource reference (URI).

    Args:
        client: Seqera API client
        uri: Data link URI (e.g., s3://bucket)
        workspace_id: Optional workspace ID

    Returns:
        Data link dict or None if not found
    """
    params = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/data-links", params=params)
    data_links = response.get("dataLinks", [])

    for dl in data_links:
        if dl.get("resourceRef") == uri:
            return dl

    return None


def resolve_mount_data(
    client: SeqeraClient,
    workspace_id: str | None,
    mount_data_link: str | None = None,
    mount_data_ids: str | None = None,
    mount_data: str | None = None,
    mount_data_uris: str | None = None,
) -> list[dict] | None:
    """Resolve mount data specifications to data link IDs.

    Args:
        client: Seqera API client
        workspace_id: Optional workspace ID
        mount_data_link: Single data link ID (deprecated)
        mount_data_ids: Comma-separated data link IDs
        mount_data: Comma-separated data link names
        mount_data_uris: Comma-separated data link URIs

    Returns:
        List of mount data dicts or None

    Raises:
        SeqeraError: If mount options are mutually exclusive or data link not found
    """
    # Count how many options are provided
    provided = sum(
        1 for opt in [mount_data_link, mount_data_ids, mount_data, mount_data_uris] if opt
    )
    if provided > 1:
        raise SeqeraError(
            "--mount-data-link, --mount-data-ids, --mount-data, and --mount-data-uris are mutually exclusive"
        )

    if not provided:
        return None

    mount_data_list = []

    if mount_data_link:
        # Single ID (deprecated option)
        mount_data_list.append({"dataLinkId": mount_data_link})

    elif mount_data_ids:
        # Multiple IDs
        for dl_id in mount_data_ids.split(","):
            dl_id = dl_id.strip()
            if dl_id:
                mount_data_list.append({"dataLinkId": dl_id})

    elif mount_data:
        # Names - resolve to IDs
        for name in mount_data.split(","):
            name = name.strip()
            if name:
                dl = find_data_link_by_name(client, name, workspace_id)
                if not dl:
                    raise SeqeraError(f"Data link '{name}' not found")
                mount_data_list.append({"dataLinkId": dl.get("id")})

    elif mount_data_uris:
        # URIs - resolve to IDs
        for uri in mount_data_uris.split(","):
            uri = uri.strip()
            if uri:
                dl = find_data_link_by_uri(client, uri, workspace_id)
                if not dl:
                    raise SeqeraError(f"Data link with URI '{uri}' not found")
                mount_data_list.append({"dataLinkId": dl.get("id")})

    return mount_data_list if mount_data_list else None


def find_template_by_name(client: SeqeraClient, name: str, workspace_id: str | None) -> dict | None:
    """Find a studio template by name or repository.

    Args:
        client: Seqera API client
        name: Template name or repository URL
        workspace_id: Optional workspace ID

    Returns:
        Template dict or None if not found
    """
    params = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/studios/templates", params=params)
    templates = response.get("templates", [])

    for template in templates:
        if template.get("repository") == name or template.get("name") == name:
            return template

    return None


@app.command("add")
def add_studio(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Studio name"),
    ],
    compute_env: Annotated[
        str,
        typer.Option("-c", "--compute-env", help="Compute environment name"),
    ],
    template: Annotated[
        str | None,
        typer.Option("-t", "--template", help="Studio template name or repository URL"),
    ] = None,
    custom_template: Annotated[
        str | None,
        typer.Option("-ct", "--custom-template", help="Custom container image URL"),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("-d", "--description", help="Studio description"),
    ] = None,
    conda_env_yml: Annotated[
        str | None,
        typer.Option("--conda-env-yml", help="Path to conda environment YAML file"),
    ] = None,
    cpu: Annotated[
        int,
        typer.Option("--cpu", help="Number of CPUs"),
    ] = 2,
    memory: Annotated[
        int,
        typer.Option("--memory", help="Memory in MB"),
    ] = 8192,
    gpu: Annotated[
        int,
        typer.Option("--gpu", help="Number of GPUs"),
    ] = 0,
    auto_start: Annotated[
        bool,
        typer.Option("-a", "--auto-start", help="Start studio immediately after creation"),
    ] = False,
    private: Annotated[
        bool,
        typer.Option("--private", help="Create a private studio"),
    ] = False,
    labels: Annotated[
        str | None,
        typer.Option("--labels", help="Comma-separated list of labels"),
    ] = None,
    mount_data_link: Annotated[
        str | None,
        typer.Option(
            "--mount-data-link", help="Data link ID to mount (deprecated, use --mount-data-ids)"
        ),
    ] = None,
    mount_data_ids: Annotated[
        str | None,
        typer.Option("--mount-data-ids", help="Comma-separated list of data link IDs to mount"),
    ] = None,
    mount_data: Annotated[
        str | None,
        typer.Option("--mount-data", help="Comma-separated list of data link names to mount"),
    ] = None,
    mount_data_uris: Annotated[
        str | None,
        typer.Option(
            "--mount-data-uris",
            help="Comma-separated list of data link URIs to mount (e.g., s3://bucket)",
        ),
    ] = None,
    lifespan: Annotated[
        int | None,
        typer.Option("--lifespan", help="Lifespan in hours (defaults to workspace setting)"),
    ] = None,
    wait: Annotated[
        str | None,
        typer.Option("--wait", help="Wait until Studio reaches specified status (e.g., RUNNING)"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Add a new studio.

    Either --template or --custom-template must be specified.
    """
    from pathlib import Path

    try:
        client = get_client()
        output_format = get_output_format()

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Validate template requirements
        if not template and not custom_template:
            output_error("Either --template or --custom-template must be specified")
            sys.exit(1)

        if custom_template and conda_env_yml:
            output_error("Cannot use --conda-env-yml with --custom-template")
            sys.exit(1)

        # Find compute environment
        ce = find_compute_env_by_name(client, compute_env, workspace)
        ce_id = ce.get("id")

        # Resolve template
        template_url = None
        if template:
            template_info = find_template_by_name(client, template, workspace)
            if not template_info:
                output_error(f"Template '{template}' not found")
                sys.exit(1)
            template_url = template_info.get("repository")
        else:
            template_url = custom_template

        # Read conda env file if specified
        conda_env_string = None
        if conda_env_yml:
            conda_path = Path(conda_env_yml)
            if not conda_path.exists():
                output_error(f"Conda environment file not found: {conda_env_yml}")
                sys.exit(1)
            conda_env_string = conda_path.read_text()

        # Build configuration
        configuration = {
            "gpu": gpu,
            "cpu": cpu,
            "memory": memory,
        }

        if conda_env_string:
            configuration["condaEnvironment"] = conda_env_string

        if lifespan is not None:
            configuration["lifespan"] = lifespan

        # Resolve mount data options
        mount_data_resolved = resolve_mount_data(
            client, workspace, mount_data_link, mount_data_ids, mount_data, mount_data_uris
        )
        if mount_data_resolved:
            configuration["mountData"] = mount_data_resolved

        # Build request payload
        request = {
            "name": name,
            "computeEnvId": ce_id,
            "dataStudioToolUrl": template_url,
            "configuration": configuration,
            "isPrivate": private,
        }

        if description:
            request["description"] = description

        # Handle labels
        if labels:
            label_ids = []
            # Parse and resolve label IDs
            params = {}
            if workspace:
                params["workspaceId"] = workspace

            labels_response = client.get("/labels", params=params)
            existing_labels = {
                l.get("name"): l.get("id") for l in labels_response.get("labels", [])
            }

            for label_name in labels.split(","):
                label_name = label_name.strip()
                if label_name in existing_labels:
                    label_ids.append(existing_labels[label_name])

            if label_ids:
                request["labelIds"] = label_ids

        # Create studio
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        response = client.post(
            f"/studios?autoStart={str(auto_start).lower()}", json=request, params=params
        )
        studio = response.get("studio", {})
        session_id = studio.get("sessionId", "")

        # Wait for status if requested
        if wait:
            typer.echo(f"Waiting for studio to reach '{wait}' status...")
            if wait_for_studio_status(client, session_id, wait, workspace):
                typer.echo(f"Studio reached '{wait}' status")
            else:
                typer.echo(f"Warning: Studio did not reach '{wait}' status within timeout")

        # Build base URL for studio link
        base_url = None
        if workspace:
            base_url = f"{client.base_url}/orgs/{workspace}/studios"

        # Output response
        result = StudiosCreated(
            session_id=session_id,
            workspace_id=int(workspace) if workspace else None,
            workspace_ref=workspace_ref,
            base_workspace_url=base_url,
            auto_start=auto_start,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)


@app.command("add-as-new")
def add_studio_from_existing(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Studio name"),
    ],
    parent_studio_id: Annotated[
        str | None,
        typer.Option("-p", "--parent-id", help="Parent studio ID"),
    ] = None,
    parent_studio_name: Annotated[
        str | None,
        typer.Option("--parent-name", help="Parent studio name"),
    ] = None,
    parent_checkpoint_id: Annotated[
        str | None,
        typer.Option(
            "--parent-checkpoint-id", help="Parent checkpoint ID (defaults to most recent)"
        ),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("-d", "--description", help="Studio description"),
    ] = None,
    cpu: Annotated[
        int | None,
        typer.Option("--cpu", help="Number of CPUs (defaults to parent)"),
    ] = None,
    memory: Annotated[
        int | None,
        typer.Option("--memory", help="Memory in MB (defaults to parent)"),
    ] = None,
    gpu: Annotated[
        int | None,
        typer.Option("--gpu", help="Number of GPUs (defaults to parent)"),
    ] = None,
    auto_start: Annotated[
        bool,
        typer.Option("-a", "--auto-start", help="Start studio immediately after creation"),
    ] = False,
    private: Annotated[
        bool,
        typer.Option("--private", help="Create a private studio"),
    ] = False,
    labels: Annotated[
        str | None,
        typer.Option("--labels", help="Comma-separated list of labels"),
    ] = None,
    mount_data_ids: Annotated[
        str | None,
        typer.Option("--mount-data-ids", help="Comma-separated list of data link IDs to mount"),
    ] = None,
    mount_data: Annotated[
        str | None,
        typer.Option("--mount-data", help="Comma-separated list of data link names to mount"),
    ] = None,
    mount_data_uris: Annotated[
        str | None,
        typer.Option(
            "--mount-data-uris",
            help="Comma-separated list of data link URIs to mount (e.g., s3://bucket)",
        ),
    ] = None,
    lifespan: Annotated[
        int | None,
        typer.Option("--lifespan", help="Lifespan in hours (defaults to workspace setting)"),
    ] = None,
    wait: Annotated[
        str | None,
        typer.Option("--wait", help="Wait until Studio reaches specified status (e.g., RUNNING)"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID (numeric)"),
    ] = None,
) -> None:
    """Add a new studio from an existing one."""
    try:
        client = get_client()
        output_format = get_output_format()

        if not parent_studio_id and not parent_studio_name:
            output_error("Either --parent-id or --parent-name must be specified")
            sys.exit(1)

        # Get workspace info
        workspace_ref, ws_id = get_workspace_info(client, workspace)

        # Find parent studio
        parent_studio, _ = find_studio(client, parent_studio_name, parent_studio_id, workspace)
        parent_session_id = parent_studio.get("sessionId")

        # Get checkpoint ID
        checkpoint_id = None
        if parent_checkpoint_id:
            # Validate checkpoint exists
            params = {}
            if workspace:
                params["workspaceId"] = workspace
            try:
                checkpoint_response = client.get(
                    f"/studios/{parent_session_id}/checkpoints/{parent_checkpoint_id}",
                    params=params,
                )
                checkpoint_id = checkpoint_response.get("id")
            except Exception:
                output_error(f"Checkpoint '{parent_checkpoint_id}' not found")
                sys.exit(1)
        else:
            # Get most recent checkpoint
            params = {"max": 1}
            if workspace:
                params["workspaceId"] = workspace
            checkpoints_response = client.get(
                f"/studios/{parent_session_id}/checkpoints", params=params
            )
            checkpoints = checkpoints_response.get("checkpoints", [])
            if checkpoints:
                checkpoint_id = checkpoints[0].get("id")

        # Get parent configuration
        parent_config = parent_studio.get("configuration", {})
        parent_template = parent_studio.get("template", {})
        parent_compute_env = parent_studio.get("computeEnv", {})

        # Build configuration (use provided values or fallback to parent)
        configuration = {
            "gpu": gpu if gpu is not None else parent_config.get("gpu", 0),
            "cpu": cpu if cpu is not None else parent_config.get("cpu", 2),
            "memory": memory if memory is not None else parent_config.get("memory", 8192),
        }

        if lifespan is not None:
            configuration["lifespan"] = lifespan

        # Resolve mount data options (use provided values or fallback to parent)
        mount_data_resolved = resolve_mount_data(
            client, workspace, None, mount_data_ids, mount_data, mount_data_uris
        )
        if mount_data_resolved:
            configuration["mountData"] = mount_data_resolved
        else:
            # Preserve mount data from parent if no new mount data specified
            parent_mount_data = parent_config.get("mountData")
            if parent_mount_data:
                configuration["mountData"] = parent_mount_data

        # Build description
        final_description = description
        if not final_description:
            final_description = (
                f"Started from studio {parent_studio.get('name', parent_session_id)}"
            )

        # Build request payload
        request = {
            "name": name,
            "description": final_description,
            "computeEnvId": parent_compute_env.get("id"),
            "dataStudioToolUrl": parent_template.get("repository"),
            "configuration": configuration,
            "isPrivate": private,
        }

        if checkpoint_id:
            request["initialCheckpointId"] = checkpoint_id

        # Handle labels
        if labels:
            label_ids = []
            params = {}
            if workspace:
                params["workspaceId"] = workspace

            labels_response = client.get("/labels", params=params)
            existing_labels = {
                l.get("name"): l.get("id") for l in labels_response.get("labels", [])
            }

            for label_name in labels.split(","):
                label_name = label_name.strip()
                if label_name in existing_labels:
                    label_ids.append(existing_labels[label_name])

            if label_ids:
                request["labelIds"] = label_ids

        # Create studio
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        response = client.post(
            f"/studios?autoStart={str(auto_start).lower()}", json=request, params=params
        )
        studio = response.get("studio", {})
        session_id = studio.get("sessionId", "")

        # Wait for status if requested
        if wait:
            typer.echo(f"Waiting for studio to reach '{wait}' status...")
            if wait_for_studio_status(client, session_id, wait, workspace):
                typer.echo(f"Studio reached '{wait}' status")
            else:
                typer.echo(f"Warning: Studio did not reach '{wait}' status within timeout")

        # Build base URL for studio link
        base_url = None
        if workspace:
            base_url = f"{client.base_url}/orgs/{workspace}/studios"

        # Output response
        result = StudiosCreated(
            session_id=session_id,
            workspace_id=int(workspace) if workspace else None,
            workspace_ref=workspace_ref,
            base_workspace_url=base_url,
            auto_start=auto_start,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_studios_error(e)
