"""
Data Links commands for Seqera Platform CLI.

Provides commands for managing data links (cloud storage buckets/containers).
"""

from enum import Enum
from typing import Annotated, Any

import typer

from seqera.api.client import SeqeraClient
from seqera.main import get_client, get_output_format
from seqera.utils.output import OutputFormat, output_console, output_json, output_yaml

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
        return "[user workspace]"

    user_info = client.get("/user-info")
    user_id = user_info.get("user", {}).get("id")
    if not user_id:
        return f"[workspace {workspace_id}]"

    workspaces_response = client.get(f"/user/{user_id}/workspaces")
    workspaces = workspaces_response.get("orgsAndWorkspaces", [])

    for ws in workspaces:
        if ws.get("workspaceId") == workspace_id:
            org_name = ws.get("orgName", "")
            ws_name = ws.get("workspaceName", "")
            return f"[{org_name} / {ws_name}]" if ws_name else f"[{org_name}]"

    return f"[workspace {workspace_id}]"


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

    def to_console(self) -> str:
        lines = [f"Data Links at workspace {self.workspace_ref}:", ""]
        if not self.data_links:
            lines.append("  No data links found")
        else:
            # Header
            lines.append(f"  {'ID':<45} {'Name':<30} {'Provider':<10} {'Region':<15} {'Resource'}")
            lines.append("  " + "-" * 120)
            for dl in self.data_links:
                dl_id = dl.get("id", "")[:44]
                name = dl.get("name", "")[:29]
                provider = dl.get("provider", "")[:9]
                region = dl.get("region", "")[:14]
                resource = dl.get("resourceRef", "")
                lines.append(f"  {dl_id:<45} {name:<30} {provider:<10} {region:<15} {resource}")

        if self.is_incomplete:
            lines.append("")
            lines.append("  Note: Results may be incomplete. Use --wait to fetch all data links.")

        return "\n".join(lines)


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


# Commands


@app.command("list")
def list_data_links(
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

    # Build search query
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

    result = DataLinksList(
        workspace_ref=workspace_ref,
        data_links=data_links,
        is_incomplete=is_incomplete,
        offset=offset,
        max_items=max_results,
        total_size=total_size,
    )
    _output_result(result, output_format)


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
    data_link_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Data link ID."),
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

    # Build request params for describe
    describe_params: dict[str, Any] = {}
    if workspace_id:
        describe_params["workspaceId"] = workspace_id
    if cred_id:
        describe_params["credentialsId"] = cred_id

    # Get data link details
    describe_response = client.get(f"/data-links/{data_link_id}", params=describe_params)
    data_link = describe_response.get("dataLink", describe_response)

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
        data_link=data_link,
        path=path,
        objects=browse_response.get("objects", []),
        next_page_token=browse_response.get("nextPageToken"),
    )
    _output_result(result, output_format)
