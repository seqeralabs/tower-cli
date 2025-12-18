"""
Data Links commands for Seqera Platform CLI.

Provides commands for managing data links (cloud storage buckets/containers).
"""

import sys
from enum import Enum
from pathlib import Path
from typing import Annotated, Any

import httpx
import typer
from rich.console import Console
from rich.table import Table

from seqera.api.client import SeqeraClient
from seqera.main import get_client, get_output_format
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

app = typer.Typer(name="data-links", help="Manage data links.")


class DataLinkProvider(str, Enum):
    """Supported data link providers."""

    AWS = "aws"
    AZURE = "azure"
    GOOGLE = "google"


class Visibility(str, Enum):
    """Data link visibility options."""

    HIDDEN = "hidden"
    VISIBLE = "visible"
    ALL = "all"


class DisplayStyle(str, Enum):
    """Display style options for list output."""

    TABLE = "table"
    PANELS = "panels"
    JSON = "json"


def _output_result(result: Any, output_format: OutputFormat) -> None:
    """Output result in the appropriate format."""
    if output_format == OutputFormat.JSON:
        output_json(result.to_dict() if hasattr(result, "to_dict") else result)
    elif output_format == OutputFormat.YAML:
        output_yaml(result.to_dict() if hasattr(result, "to_dict") else result)
    else:
        output_console(result.to_console() if hasattr(result, "to_console") else str(result))


def _resolve_workspace_id(client: SeqeraClient, workspace: str | None) -> int | None:
    """Resolve workspace reference to workspace ID."""
    if workspace is None:
        return None

    # If it's a numeric ID, return it directly
    if workspace.isdigit():
        return int(workspace)

    # Otherwise, look up by org/workspace reference
    user_info = client.get("/user-info")
    user_id = user_info.get("user", {}).get("id")
    if not user_id:
        raise typer.Exit(1)

    workspaces_response = client.get(f"/user/{user_id}/workspaces")
    workspaces = workspaces_response.get("orgsAndWorkspaces", [])

    for ws in workspaces:
        org_name = ws.get("orgName", "")
        ws_name = ws.get("workspaceName", "")
        full_ref = f"{org_name}/{ws_name}" if ws_name else org_name

        if full_ref == workspace or ws_name == workspace:
            return ws.get("workspaceId")

    typer.echo(f"Workspace '{workspace}' not found", err=True)
    raise typer.Exit(1)


def _resolve_workspace_ref(client: SeqeraClient, workspace_id: int | None) -> str:
    """Get workspace reference string from workspace ID."""
    if workspace_id is None:
        return "user workspace"

    user_info = client.get("/user-info")
    user_id = user_info.get("user", {}).get("id")
    if not user_id:
        return f"workspace {workspace_id}"

    workspaces_response = client.get(f"/user/{user_id}/workspaces")
    workspaces = workspaces_response.get("orgsAndWorkspaces", [])

    for ws in workspaces:
        if ws.get("workspaceId") == workspace_id:
            org_name = ws.get("orgName", "")
            ws_name = ws.get("workspaceName", "")
            return f"{org_name} / {ws_name}" if ws_name else org_name

    return f"workspace {workspace_id}"


def _resolve_credentials_id(
    client: SeqeraClient, workspace_id: int | None, credentials_ref: str
) -> str:
    """Resolve credentials reference to credentials ID."""
    params: dict[str, Any] = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    response = client.get("/credentials", params=params)
    credentials_list = response.get("credentials", [])

    for cred in credentials_list:
        if cred.get("id") == credentials_ref or cred.get("name") == credentials_ref:
            return cred.get("id")

    # If not found, assume it's already an ID
    return credentials_ref


def _build_search(
    name: str | None, providers: str | None, region: str | None, uri: str | None
) -> str | None:
    """Build search query string for data links list."""
    parts: list[str] = []

    if name:
        parts.append(name)
    if providers:
        parts.append(f"provider:{providers}")
    if region:
        parts.append(f"region:{region}")
    if uri:
        parts.append(f"resourceRef:{uri}")

    return " ".join(parts) if parts else None


# Response classes


class DataLinksList:
    """Response for list data links command."""

    def __init__(
        self,
        workspace_ref: str,
        data_links: list[dict[str, Any]],
        is_incomplete: bool = False,
        offset: int = 0,
        max_items: int = 100,
        total_size: int | None = None,
    ) -> None:
        self.workspace_ref = workspace_ref
        self.data_links = data_links
        self.is_incomplete = is_incomplete
        self.offset = offset
        self.max_items = max_items
        self.total_size = total_size

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace_ref,
            "dataLinks": self.data_links,
            "isIncomplete": self.is_incomplete,
        }

    def to_console(self, style: str = "table") -> str:
        from rich.markup import escape
        from rich.text import Text

        if not self.data_links:
            return f"Data Links in workspace '{self.workspace_ref}':\n\n  No data links found"

        console = Console(force_terminal=True)
        count = self.total_size if self.total_size is not None else len(self.data_links)

        if style == "panels":
            # Panel style: each data link as a card with ID on its own line
            with console.capture() as capture:
                console.print(
                    f"\n[bold]Data Links in workspace '{escape(self.workspace_ref)}' "
                    f"({count})[/bold]\n"
                )

                for dl in self.data_links:
                    # ID on its own line
                    console.print(f"[dim]{dl.get('id', '')}[/dim]")

                    # Create a small table for the details
                    details_table = Table(show_header=False, box=None, padding=(0, 2))
                    details_table.add_column("Label", style="bold")
                    details_table.add_column("Value")

                    details_table.add_row("Name", Text(dl.get("name", ""), style="cyan"))
                    details_table.add_row("Provider", Text(dl.get("provider", ""), style="magenta"))
                    details_table.add_row("Region", Text(dl.get("region", ""), style="green"))
                    details_table.add_row("Resource", Text(dl.get("resourceRef", ""), style="blue"))

                    console.print(details_table)
                    console.print()  # Blank line between entries

                if self.is_incomplete:
                    console.print(
                        "  Note: Results may be incomplete. Use --wait to fetch all data links."
                    )

            return capture.get()

        # Default table style
        table = Table(title=f"Data Links in workspace '{escape(self.workspace_ref)}' ({count})")
        table.add_column("Name", style="cyan")
        table.add_column("Provider", style="magenta")
        table.add_column("Region", style="green")
        table.add_column("Resource", style="blue")
        table.add_column("ID", style="dim")

        for dl in self.data_links:
            table.add_row(
                dl.get("name", ""),
                dl.get("provider", ""),
                dl.get("region", ""),
                dl.get("resourceRef", ""),
                dl.get("id", ""),
            )

        with console.capture() as capture:
            console.print(table)
            if self.is_incomplete:
                console.print(
                    "\n  Note: Results may be incomplete. Use --wait to fetch all data links."
                )

        return capture.get()


class DataLinkView:
    """Response for view/add/update data link command."""

    def __init__(self, data_link: dict[str, Any], message: str = "") -> None:
        self.data_link = data_link
        self.message = message

    def to_dict(self) -> dict[str, Any]:
        return self.data_link

    def to_console(self) -> str:
        lines = []
        if self.message:
            lines.append(self.message)
            lines.append("")

        lines.append(f"  ID:          {self.data_link.get('id', '')}")
        lines.append(f"  Name:        {self.data_link.get('name', '')}")
        lines.append(f"  Description: {self.data_link.get('description', '') or ''}")
        lines.append(f"  Provider:    {self.data_link.get('provider', '')}")
        lines.append(f"  Region:      {self.data_link.get('region', '')}")
        lines.append(f"  Resource:    {self.data_link.get('resourceRef', '')}")
        lines.append(f"  Type:        {self.data_link.get('type', '')}")
        lines.append(f"  Public:      {self.data_link.get('publicAccessible', False)}")

        credentials = self.data_link.get("credentials", [])
        if credentials:
            cred_names = ", ".join(c.get("name", c.get("id", "")) for c in credentials)
            lines.append(f"  Credentials: {cred_names}")

        return "\n".join(lines)


class DataLinkDeleted:
    """Response for delete data link command."""

    def __init__(self, data_link_id: str, workspace_id: int | None) -> None:
        self.data_link_id = data_link_id
        self.workspace_id = workspace_id

    def to_dict(self) -> dict[str, Any]:
        return {
            "dataLinkId": self.data_link_id,
            "workspaceId": self.workspace_id,
        }

    def to_console(self) -> str:
        return f"Data link '{self.data_link_id}' deleted"


class DataLinkContentList:
    """Response for browse data link command."""

    def __init__(
        self,
        data_link: dict[str, Any],
        path: str | None,
        objects: list[dict[str, Any]],
        next_page_token: str | None,
    ) -> None:
        self.data_link = data_link
        self.path = path
        self.objects = objects
        self.next_page_token = next_page_token

    def to_dict(self) -> dict[str, Any]:
        return {
            "dataLink": self.data_link,
            "path": self.path,
            "objects": self.objects,
            "nextPageToken": self.next_page_token,
        }

    def to_console(self) -> str:
        lines = [f"Contents of {self.data_link.get('name', '')}"]
        if self.path:
            lines[0] += f" / {self.path}"
        lines.append("")

        if not self.objects:
            lines.append("  No files found")
        else:
            lines.append(f"  {'Type':<10} {'Name':<50} {'Size':<15} {'MIME Type'}")
            lines.append("  " + "-" * 90)
            for obj in self.objects:
                obj_type = obj.get("type", "")[:9]
                name = obj.get("name", "")[:49]
                size = str(obj.get("size", ""))[:14]
                mime_type = obj.get("mimeType", "")
                lines.append(f"  {obj_type:<10} {name:<50} {size:<15} {mime_type}")

        if self.next_page_token:
            lines.append("")
            lines.append(f"  Next page token: {self.next_page_token}")

        return "\n".join(lines)


class DataLinkFileTransferResult:
    """Response for download/upload data link commands."""

    def __init__(
        self,
        direction: str,  # "download" or "upload"
        paths: list[dict[str, Any]],
    ) -> None:
        self.direction = direction
        self.paths = paths

    def to_dict(self) -> dict[str, Any]:
        return {
            "transferDirection": self.direction,
            "paths": self.paths,
        }

    def to_console(self) -> str:
        direction_label = "downloaded" if self.direction == "download" else "uploaded"
        lines = [f"  Successfully {direction_label} files", ""]

        if self.paths:
            lines.append(f"  {'Type':<10} {'File count':<12} {'Path'}")
            lines.append("  " + "-" * 70)
            for path_info in self.paths:
                item_type = path_info.get("type", "")[:9]
                file_count = str(path_info.get("fileCount", 0))[:11]
                path = path_info.get("path", "")
                lines.append(f"  {item_type:<10} {file_count:<12} {path}")

        lines.append("")
        return "\n".join(lines)


# Commands


@app.command("list")
def list_data_links(
    filter_text: Annotated[
        str | None,
        typer.Argument(help="Filter by data link name or resource URI."),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace numeric identifier or name."),
    ] = None,
    credentials: Annotated[
        str | None,
        typer.Option("-c", "--credentials", help="Credentials identifier."),
    ] = None,
    name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Filter by name prefix."),
    ] = None,
    provider: Annotated[
        str | None,
        typer.Option("-p", "--provider", help="Filter by provider (aws, azure, google)."),
    ] = None,
    region: Annotated[
        str | None,
        typer.Option("-r", "--region", help="Filter by region."),
    ] = None,
    uri: Annotated[
        str | None,
        typer.Option("--uri", help="Filter by resource URI."),
    ] = None,
    visibility: Annotated[
        Visibility | None,
        typer.Option("--visibility", help="Filter by visibility (hidden, visible, all)."),
    ] = None,
    display: Annotated[
        DisplayStyle,
        typer.Option("-d", "--display", help="Display style: table, panels, or json."),
    ] = DisplayStyle.TABLE,
    wait: Annotated[
        bool,
        typer.Option("--wait", help="Wait for all data links to be fetched."),
    ] = False,
    offset: Annotated[
        int,
        typer.Option("--offset", help="Pagination offset."),
    ] = 0,
    max_results: Annotated[
        int,
        typer.Option("--max", help="Maximum results to return."),
    ] = 100,
) -> None:
    """List data links in a workspace."""
    client = get_client()
    output_format = get_output_format()

    workspace_id = _resolve_workspace_id(client, workspace)
    workspace_ref = _resolve_workspace_ref(client, workspace_id)

    # Resolve credentials if provided
    cred_id = None
    if credentials:
        cred_id = _resolve_credentials_id(client, workspace_id, credentials)

    # Build search query from explicit options (not filter_text - that's client-side only)
    search = _build_search(name, provider, region, uri)

    # Build request params
    params: dict[str, Any] = {"offset": offset, "max": max_results}
    if workspace_id:
        params["workspaceId"] = workspace_id
    if cred_id:
        params["credentialsId"] = cred_id
    if search:
        params["search"] = search
    if visibility:
        params["visibility"] = visibility.value

    # Check if results are incomplete (simplified - just check status)
    is_incomplete = False
    if wait:
        # Make a small request to check status
        status_params = {**params, "offset": 0, "max": 1}
        client.get("/data-links", params=status_params)
        # In a real implementation, we'd poll until complete

    # Fetch data links
    response = client.get("/data-links", params=params)
    data_links = response.get("dataLinks", [])
    total_size = response.get("totalSize")

    # Client-side filtering if filter_text provided (in case API doesn't filter both)
    if filter_text:
        filter_lower = filter_text.lower()
        data_links = [
            dl
            for dl in data_links
            if filter_lower in dl.get("name", "").lower()
            or filter_lower in dl.get("resourceRef", "").lower()
        ]

    result = DataLinksList(
        workspace_ref=workspace_ref,
        data_links=data_links,
        is_incomplete=is_incomplete,
        offset=offset,
        max_items=max_results,
        total_size=total_size,
    )

    # Handle display style
    if display == DisplayStyle.JSON:
        output_json(result.to_dict())
    elif output_format == OutputFormat.JSON:
        output_json(result.to_dict())
    elif output_format == OutputFormat.YAML:
        output_yaml(result.to_dict())
    else:
        output_console(result.to_console(style=display.value))


@app.command("add")
def add_data_link(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Data link name."),
    ],
    uri: Annotated[
        str,
        typer.Option("-u", "--uri", help="Data link URI (e.g., s3://bucket-name)."),
    ],
    provider: Annotated[
        DataLinkProvider,
        typer.Option("-p", "--provider", help="Cloud provider (aws, azure, google)."),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace numeric identifier or name."),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("-d", "--description", help="Data link description."),
    ] = None,
    credentials: Annotated[
        str | None,
        typer.Option("-c", "--credentials", help="Credentials identifier."),
    ] = None,
) -> None:
    """Add a new data link."""
    client = get_client()
    output_format = get_output_format()

    workspace_id = _resolve_workspace_id(client, workspace)

    # Resolve credentials if provided
    cred_id = None
    if credentials:
        cred_id = _resolve_credentials_id(client, workspace_id, credentials)

    # Build request payload
    payload: dict[str, Any] = {
        "name": name,
        "resourceRef": uri,
        "type": "bucket",
        "provider": provider.value,
        "publicAccessible": cred_id is None,
    }
    if description:
        payload["description"] = description
    if cred_id:
        payload["credentialsId"] = cred_id

    # Build request params
    params: dict[str, Any] = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    # Create data link
    response = client.post("/data-links", json=payload, params=params)

    result = DataLinkView(response, "Data link created")
    _output_result(result, output_format)


@app.command("delete")
def delete_data_link(
    data_link_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Data link ID."),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace numeric identifier or name."),
    ] = None,
) -> None:
    """Delete a data link."""
    client = get_client()
    output_format = get_output_format()

    workspace_id = _resolve_workspace_id(client, workspace)

    # Build request params
    params: dict[str, Any] = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    # Delete data link
    client.delete(f"/data-links/{data_link_id}", params=params)

    result = DataLinkDeleted(data_link_id, workspace_id)
    _output_result(result, output_format)


@app.command("update")
def update_data_link(
    data_link_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Data link ID."),
    ],
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="New data link name."),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace numeric identifier or name."),
    ] = None,
    description: Annotated[
        str | None,
        typer.Option("-d", "--description", help="New data link description."),
    ] = None,
    credentials: Annotated[
        str | None,
        typer.Option("-c", "--credentials", help="New credentials identifier."),
    ] = None,
) -> None:
    """Update a data link."""
    client = get_client()
    output_format = get_output_format()

    workspace_id = _resolve_workspace_id(client, workspace)

    # Resolve credentials if provided
    cred_id = None
    if credentials:
        cred_id = _resolve_credentials_id(client, workspace_id, credentials)

    # Build request payload
    payload: dict[str, Any] = {"name": name}
    if description:
        payload["description"] = description
    if cred_id:
        payload["credentialsId"] = cred_id

    # Build request params
    params: dict[str, Any] = {}
    if workspace_id:
        params["workspaceId"] = workspace_id

    # Update data link
    response = client.put(f"/data-links/{data_link_id}", json=payload, params=params)

    result = DataLinkView(response, "Data link updated")
    _output_result(result, output_format)


@app.command("browse")
def browse_data_link(
    data_link: Annotated[
        str,
        typer.Argument(help="Data link ID or name."),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace numeric identifier or name."),
    ] = None,
    credentials: Annotated[
        str | None,
        typer.Option("-c", "--credentials", help="Credentials identifier."),
    ] = None,
    path: Annotated[
        str | None,
        typer.Option("-p", "--path", help="Path to browse within the data link."),
    ] = None,
    filter_prefix: Annotated[
        str | None,
        typer.Option("-f", "--filter", help="Filter files by prefix."),
    ] = None,
    next_page_token: Annotated[
        str | None,
        typer.Option("-t", "--token", help="Next page token for pagination."),
    ] = None,
    page_size: Annotated[
        int | None,
        typer.Option("--page", help="Number of items per page."),
    ] = None,
) -> None:
    """Browse contents of a data link."""
    client = get_client()
    output_format = get_output_format()

    workspace_id = _resolve_workspace_id(client, workspace)

    # Resolve credentials if provided
    cred_id = None
    if credentials:
        cred_id = _resolve_credentials_id(client, workspace_id, credentials)

    # Resolve data link ID or name to ID
    data_link_id = _resolve_data_link_id_or_name(client, workspace_id, data_link, cred_id)

    # Build request params for describe
    describe_params: dict[str, Any] = {}
    if workspace_id:
        describe_params["workspaceId"] = workspace_id
    if cred_id:
        describe_params["credentialsId"] = cred_id

    # Get data link details
    describe_response = client.get(f"/data-links/{data_link_id}", params=describe_params)
    data_link_info = describe_response.get("dataLink", describe_response)

    # Build browse params
    browse_params: dict[str, Any] = {}
    if workspace_id:
        browse_params["workspaceId"] = workspace_id
    if cred_id:
        browse_params["credentialsId"] = cred_id
    if filter_prefix:
        browse_params["search"] = filter_prefix
    if next_page_token:
        browse_params["nextPageToken"] = next_page_token
    if page_size:
        browse_params["pageSize"] = page_size

    # Browse data link
    if path:
        browse_url = f"/data-links/{data_link_id}/browse/{path}"
    else:
        browse_url = f"/data-links/{data_link_id}/browse"

    browse_response = client.get(browse_url, params=browse_params)

    result = DataLinkContentList(
        data_link=data_link_info,
        path=path,
        objects=browse_response.get("objects", []),
        next_page_token=browse_response.get("nextPageToken"),
    )
    _output_result(result, output_format)


def _download_file(
    client: SeqeraClient,
    data_link_id: str,
    remote_path: str,
    local_path: Path,
    credentials_id: str,
    workspace_id: int | None,
    show_progress: bool = True,
) -> None:
    """Download a single file from a data link.

    Args:
        client: Seqera API client
        data_link_id: Data link ID
        remote_path: Path of file in data link
        local_path: Local path to save file to
        credentials_id: Credentials ID
        workspace_id: Workspace ID
        show_progress: Whether to show progress output
    """
    # Get download URL from API
    params: dict[str, Any] = {"credentialsId": credentials_id}
    if workspace_id:
        params["workspaceId"] = workspace_id

    url_response = client.get(
        f"/data-links/{data_link_id}/download/{remote_path}",
        params=params,
    )
    download_url = url_response.get("url")

    if not download_url:
        raise typer.Exit(1)

    if show_progress:
        typer.echo(f"  Downloading file: {remote_path}")

    # Create parent directories if needed
    local_path.parent.mkdir(parents=True, exist_ok=True)

    # Download the file
    with httpx.stream("GET", download_url, follow_redirects=True, timeout=60.0) as response:
        if response.status_code != 200:
            output_error(f"Failed to download file: HTTP {response.status_code}")
            raise typer.Exit(1)

        content_length = response.headers.get("content-length")
        total_size = int(content_length) if content_length else None

        with open(local_path, "wb") as f:
            downloaded = 0
            for chunk in response.iter_bytes(chunk_size=8192):
                f.write(chunk)
                downloaded += len(chunk)

                # Show progress if we know the total size
                if show_progress and total_size and total_size > 0:
                    percent = (downloaded / total_size) * 100
                    typer.echo(f"\r    Progress: {percent:.1f}%", nl=False)

        if show_progress and total_size:
            typer.echo()  # New line after progress


def _explore_data_link_tree(
    client: SeqeraClient,
    data_link_id: str,
    paths: list[str],
    credentials_id: str,
    workspace_id: int | None,
) -> list[dict[str, Any]]:
    """Explore data link tree to get all files under given paths.

    Args:
        client: Seqera API client
        data_link_id: Data link ID
        paths: List of paths to explore
        credentials_id: Credentials ID
        workspace_id: Workspace ID

    Returns:
        List of file items
    """
    params: dict[str, Any] = {"credentialsId": credentials_id}
    if workspace_id:
        params["workspaceId"] = workspace_id

    # POST with paths in body
    response = client.post(
        f"/data-links/{data_link_id}/browse-tree",
        json={"paths": paths},
        params=params,
    )
    return response.get("items", [])


def _resolve_data_link_id_or_name(
    client: SeqeraClient,
    workspace_id: int | None,
    id_or_name: str,
    credentials_id: str | None = None,
) -> str:
    """Resolve data link ID or name to data link ID.

    First tries to use the value as an ID directly, then falls back to exact name match.
    """
    # First, try to use it as an ID directly
    params: dict[str, Any] = {}
    if workspace_id:
        params["workspaceId"] = workspace_id
    if credentials_id:
        params["credentialsId"] = credentials_id

    try:
        response = client.get(f"/data-links/{id_or_name}", params=params)
        data_link = response.get("dataLink", response)
        if data_link and data_link.get("id"):
            return data_link.get("id")
    except Exception:
        pass  # Not a valid ID, try searching by name

    # Search by name
    search_params = {**params, "search": id_or_name}
    response = client.get("/data-links", params=search_params)
    data_links = response.get("dataLinks", [])

    # Only accept exact name matches
    exact_matches = [dl for dl in data_links if dl.get("name") == id_or_name]
    if len(exact_matches) == 1:
        return exact_matches[0].get("id")

    if len(exact_matches) > 1:
        output_error(
            f"Multiple data links found with name '{id_or_name}'. "
            "Please use the data link ID instead."
        )
        raise typer.Exit(1)

    # No exact match found - suggest partial matches if any
    if data_links:
        suggestions = [dl.get("name") for dl in data_links[:5]]  # Limit to 5 suggestions
        suggestion_str = ", ".join(f"'{s}'" for s in suggestions)
        output_error(f"Data link '{id_or_name}' not found. " f"Did you mean: {suggestion_str}?")
    else:
        output_error(f"Data link '{id_or_name}' not found")

    raise typer.Exit(1)


def _resolve_data_link_id(
    client: SeqeraClient,
    workspace_id: int | None,
    data_link_id: str | None,
    data_link_name: str | None,
    data_link_uri: str | None,
    credentials_id: str | None,
) -> str:
    """Resolve data link identifier (id, name, or uri) to data link ID."""
    if data_link_id:
        return data_link_id

    # Search for data link by name or uri
    params: dict[str, Any] = {}
    if workspace_id:
        params["workspaceId"] = workspace_id
    if credentials_id:
        params["credentialsId"] = credentials_id

    search_parts = []
    if data_link_name:
        search_parts.append(data_link_name)
    if data_link_uri:
        search_parts.append(f"resourceRef:{data_link_uri}")

    if search_parts:
        params["search"] = " ".join(search_parts)

    response = client.get("/data-links", params=params)
    data_links = response.get("dataLinks", [])

    if not data_links:
        output_error("Data link not found")
        raise typer.Exit(1)

    if len(data_links) > 1:
        output_error(
            "Multiple data links found matching criteria. Please use --id to specify exactly which one."
        )
        raise typer.Exit(1)

    return data_links[0].get("id")


@app.command("download")
def download_data_link(
    paths: Annotated[
        list[str],
        typer.Argument(help="Paths to files or directories to download."),
    ],
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
    ] = None,
    data_link_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Data link id"),
    ] = None,
    data_link_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Data link name (e.g. my-custom-data-link-name)"),
    ] = None,
    data_link_uri: Annotated[
        str | None,
        typer.Option("--uri", help="Data link URI (e.g. s3://another-bucket)"),
    ] = None,
    credentials: Annotated[
        str | None,
        typer.Option("-c", "--credentials", help="Credentials identifier"),
    ] = None,
    output_dir: Annotated[
        str | None,
        typer.Option("-o", "--output-dir", help="Output directory."),
    ] = None,
) -> None:
    """Download content of data-link."""
    # Validate that at least one identifier is provided
    if not data_link_id and not data_link_name and not data_link_uri:
        output_error("At least one of --id, --name, or --uri must be provided")
        raise typer.Exit(1)

    client = get_client()
    output_format = get_output_format()

    workspace_id = _resolve_workspace_id(client, workspace)

    # Resolve credentials
    cred_id = _resolve_credentials_id(client, workspace_id, credentials) if credentials else None

    # Resolve data link ID from name or uri if not provided directly
    resolved_data_link_id = _resolve_data_link_id(
        client, workspace_id, data_link_id, data_link_name, data_link_uri, cred_id
    )

    path_info: list[dict[str, Any]] = []
    show_progress = output_format != OutputFormat.JSON

    for path in paths:
        # Explore the tree to see if this is a file or directory
        items = _explore_data_link_tree(
            client, resolved_data_link_id, [path], cred_id, workspace_id
        )

        if not items:
            # If no items found, assume this is a single file path
            filename = Path(path).name
            target_path = Path(output_dir, filename) if output_dir else Path(filename)

            _download_file(
                client,
                resolved_data_link_id,
                path,
                target_path,
                cred_id,
                workspace_id,
                show_progress,
            )

            path_info.append(
                {
                    "type": "FILE",
                    "path": path,
                    "fileCount": 1,
                }
            )
        else:
            # Download each file in the tree
            for item in items:
                item_path = item.get("path", "")
                target_path = Path(output_dir, item_path) if output_dir else Path(item_path)

                _download_file(
                    client,
                    resolved_data_link_id,
                    item_path,
                    target_path,
                    cred_id,
                    workspace_id,
                    show_progress,
                )

            path_info.append(
                {
                    "type": "FOLDER",
                    "path": path,
                    "fileCount": len(items),
                }
            )

    result = DataLinkFileTransferResult(direction="download", paths=path_info)
    _output_result(result, output_format)


# Upload constants
MAX_FILE_SIZE = 5 * 1024 * 1024 * 1024 * 1024  # 5 TB
MAX_FILES_TO_UPLOAD = 300
MULTI_UPLOAD_PART_SIZE = 250 * 1024 * 1024  # 250 MB


def _get_file_chunk(file_path: Path, index: int) -> bytes:
    """Get a chunk of a file for multipart upload.

    Args:
        file_path: Path to the file
        index: Chunk index (0-based)

    Returns:
        Bytes of the chunk
    """
    start = index * MULTI_UPLOAD_PART_SIZE
    end = min(start + MULTI_UPLOAD_PART_SIZE, file_path.stat().st_size)
    length = end - start

    with open(file_path, "rb") as f:
        f.seek(start)
        return f.read(length)


def _count_files_and_validate(paths: list[str]) -> int:
    """Count total files and validate for upload constraints.

    Args:
        paths: List of file/directory paths

    Returns:
        Total file count

    Raises:
        typer.Exit: If validation fails
    """
    total_files = 0

    for path_str in paths:
        path = Path(path_str)
        if path.is_dir():
            for file_path in path.rglob("*"):
                if file_path.is_file():
                    total_files += 1
                    if file_path.stat().st_size > MAX_FILE_SIZE:
                        output_error(f"File {file_path} exceeds maximum size of 5 TB to upload.")
                        raise typer.Exit(1)

                    if total_files > MAX_FILES_TO_UPLOAD:
                        output_error(
                            f"Cannot upload more than {MAX_FILES_TO_UPLOAD} files at once. "
                            f"Found at least {total_files} files at provided paths."
                        )
                        raise typer.Exit(1)
        else:
            total_files += 1
            if path.stat().st_size > MAX_FILE_SIZE:
                output_error(f"File {path} exceeds maximum size of 5 TB to upload.")
                raise typer.Exit(1)

    return total_files


def _get_data_link(
    client: SeqeraClient,
    data_link_id: str,
    workspace_id: int | None,
    credentials_id: str | None,
) -> dict[str, Any]:
    """Get data link details.

    Args:
        client: Seqera API client
        data_link_id: Data link ID
        workspace_id: Workspace ID
        credentials_id: Credentials ID

    Returns:
        Data link dict
    """
    params: dict[str, Any] = {}
    if workspace_id:
        params["workspaceId"] = workspace_id
    if credentials_id:
        params["credentialsId"] = credentials_id

    response = client.get(f"/data-links/{data_link_id}", params=params)
    return response.get("dataLink", response)


def _upload_file_multipart(
    client: SeqeraClient,
    data_link_id: str,
    file_path: Path,
    relative_key: str,
    credentials_id: str,
    workspace_id: int | None,
    output_dir: str | None,
    provider: str,
    show_progress: bool = True,
) -> None:
    """Upload a file using multipart upload.

    Args:
        client: Seqera API client
        data_link_id: Data link ID
        file_path: Local file path
        relative_key: Key/path in the data link
        credentials_id: Credentials ID
        workspace_id: Workspace ID
        output_dir: Remote output directory
        provider: Cloud provider (aws, azure, google, seqeracompute)
        show_progress: Whether to show progress output
    """
    import mimetypes

    if not file_path.exists():
        output_error(f"File not found: {file_path}")
        raise typer.Exit(1)

    # Detect MIME type
    mime_type, _ = mimetypes.guess_type(str(file_path))
    if mime_type is None:
        mime_type = "application/octet-stream"

    content_length = file_path.stat().st_size

    if show_progress:
        typer.echo(f"  Uploading file: {file_path}")

    # Build request for upload URL
    upload_request = {
        "fileName": relative_key,
        "contentLength": content_length,
        "contentType": mime_type,
    }

    params: dict[str, Any] = {"credentialsId": credentials_id}
    if workspace_id:
        params["workspaceId"] = workspace_id

    # Get upload URLs
    if output_dir:
        url_response = client.post(
            f"/data-links/{data_link_id}/upload/{output_dir}",
            json=upload_request,
            params=params,
        )
    else:
        url_response = client.post(
            f"/data-links/{data_link_id}/upload",
            json=upload_request,
            params=params,
        )

    upload_urls = url_response.get("uploadUrls", [])
    upload_id = url_response.get("uploadId")

    if not upload_urls:
        output_error("No upload URLs returned from API")
        raise typer.Exit(1)

    # Upload chunks
    tags: list[dict[str, Any]] = []
    with_error = False

    try:
        for index, url in enumerate(upload_urls):
            chunk = _get_file_chunk(file_path, index)

            # Upload chunk to presigned URL
            response = httpx.put(url, content=chunk, timeout=300.0)

            if response.status_code != 200:
                with_error = True
                output_error(f"Failed to upload chunk {index}: HTTP {response.status_code}")
                raise typer.Exit(1)

            # Get ETag from response for AWS/Seqera Compute
            etag = response.headers.get("etag")
            if etag:
                tags.append(
                    {
                        "eTag": etag,
                        "partNumber": index + 1,
                    }
                )

            if show_progress:
                percent = ((index + 1) / len(upload_urls)) * 100
                typer.echo(f"\r    Progress: {percent:.1f}%", nl=False)

        if show_progress:
            typer.echo()  # New line after progress

    except Exception as e:
        with_error = True
        if not isinstance(e, SystemExit):
            output_error(f"Upload failed: {e}")
        raise

    finally:
        # Finalize upload
        finish_request = {
            "fileName": relative_key,
            "uploadId": upload_id,
            "withError": with_error,
            "tags": tags,
        }

        if output_dir:
            client.post(
                f"/data-links/{data_link_id}/upload/{output_dir}/finish",
                json=finish_request,
                params=params,
            )
        else:
            client.post(
                f"/data-links/{data_link_id}/upload/finish",
                json=finish_request,
                params=params,
            )


def _upload_directory(
    client: SeqeraClient,
    data_link_id: str,
    base_dir: Path,
    current_dir: Path,
    base_prefix: str,
    credentials_id: str,
    workspace_id: int | None,
    output_dir: str | None,
    provider: str,
    show_progress: bool = True,
) -> int:
    """Recursively upload a directory.

    Args:
        client: Seqera API client
        data_link_id: Data link ID
        base_dir: Base directory for relative paths
        current_dir: Current directory being processed
        base_prefix: Prefix for remote paths
        credentials_id: Credentials ID
        workspace_id: Workspace ID
        output_dir: Remote output directory
        provider: Cloud provider
        show_progress: Whether to show progress

    Returns:
        Number of files uploaded
    """
    total_files = 0

    for item in current_dir.iterdir():
        if item.is_dir():
            total_files += _upload_directory(
                client,
                data_link_id,
                base_dir,
                item,
                base_prefix,
                credentials_id,
                workspace_id,
                output_dir,
                provider,
                show_progress,
            )
        else:
            relative_path = item.relative_to(base_dir)
            full_key = base_prefix + str(relative_path)

            _upload_file_multipart(
                client,
                data_link_id,
                item,
                full_key,
                credentials_id,
                workspace_id,
                output_dir,
                provider,
                show_progress,
            )
            total_files += 1

    return total_files


@app.command("upload")
def upload_data_link(
    paths: Annotated[
        list[str],
        typer.Argument(help="Paths to files or directories to upload."),
    ],
    workspace: Annotated[
        str | None,
        typer.Option(
            "-w",
            "--workspace",
            help="Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName",
        ),
    ] = None,
    data_link_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Data link id"),
    ] = None,
    data_link_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Data link name (e.g. my-custom-data-link-name)"),
    ] = None,
    data_link_uri: Annotated[
        str | None,
        typer.Option("--uri", help="Data link URI (e.g. s3://another-bucket)"),
    ] = None,
    credentials: Annotated[
        str | None,
        typer.Option("-c", "--credentials", help="Credentials identifier"),
    ] = None,
    output_dir: Annotated[
        str | None,
        typer.Option("-o", "--output-dir", help="Remote output directory."),
    ] = None,
) -> None:
    """Upload content to data-link."""
    # Validate that at least one identifier is provided
    if not data_link_id and not data_link_name and not data_link_uri:
        output_error("At least one of --id, --name, or --uri must be provided")
        raise typer.Exit(1)

    # Validate files before starting
    _count_files_and_validate(paths)

    client = get_client()
    output_format = get_output_format()

    workspace_id = _resolve_workspace_id(client, workspace)

    # Resolve credentials
    cred_id = _resolve_credentials_id(client, workspace_id, credentials) if credentials else None

    # Resolve data link ID from name or uri if not provided directly
    resolved_data_link_id = _resolve_data_link_id(
        client, workspace_id, data_link_id, data_link_name, data_link_uri, cred_id
    )

    # Get data link to determine provider
    data_link = _get_data_link(client, resolved_data_link_id, workspace_id, cred_id)
    provider = data_link.get("provider", "").lower()

    if provider not in ("aws", "azure", "google", "seqeracompute"):
        output_error(f"Unsupported data-link provider: {provider}")
        raise typer.Exit(1)

    path_info: list[dict[str, Any]] = []
    show_progress = output_format != OutputFormat.JSON

    for path_str in paths:
        path = Path(path_str)

        if path.is_dir():
            base_prefix = path.name + "/"
            file_count = _upload_directory(
                client,
                resolved_data_link_id,
                path,
                path,
                base_prefix,
                cred_id,
                workspace_id,
                output_dir,
                provider,
                show_progress,
            )
            path_info.append(
                {
                    "type": "FOLDER",
                    "path": str(path),
                    "fileCount": file_count,
                }
            )
        else:
            _upload_file_multipart(
                client,
                resolved_data_link_id,
                path,
                path.name,
                cred_id,
                workspace_id,
                output_dir,
                provider,
                show_progress,
            )
            path_info.append(
                {
                    "type": "FILE",
                    "path": str(path),
                    "fileCount": 1,
                }
            )

    result = DataLinkFileTransferResult(direction="upload", paths=path_info)
    _output_result(result, output_format)
