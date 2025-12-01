"""
Labels commands for Seqera CLI.

Manage workspace labels for organizing and filtering resources.
"""

import sys
from typing import Optional

import typer
from typing_extensions import Annotated

from seqera.api.client import SeqeraClient
from seqera.exceptions import AuthenticationError, NotFoundError, SeqeraError
from seqera.main import get_client, get_output_format
from seqera.responses import LabelAdded, LabelDeleted, LabelsList
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create labels app
app = typer.Typer(
    name="labels",
    help="Manage workspace labels",
    no_args_is_help=True,
)


def handle_labels_error(e: Exception) -> None:
    """Handle labels command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
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


@app.command("add")
def add_label(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Label name"),
    ],
    value: Annotated[
        Optional[str],
        typer.Option("-v", "--value", help="Label value (for resource labels)"),
    ] = None,
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ] = None,
) -> None:
    """Add a new label to the workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build label payload
        is_resource = value is not None
        payload = {
            "name": name,
            "resource": is_resource,
        }
        if value:
            payload["value"] = value

        # Add workspace ID to query params if provided
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        # Create label
        response = client.post("/labels", json=payload, params=params)

        # Output response
        result = LabelAdded(
            label_id=response.get("id", 0),
            name=response.get("name", name),
            resource=response.get("resource", is_resource),
            value=response.get("value"),
            workspace_id=workspace or "",
        )

        output_response(result, output_format)

    except Exception as e:
        handle_labels_error(e)


@app.command("delete")
def delete_label(
    label_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Label ID to delete"),
    ],
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ] = None,
) -> None:
    """Delete a label by ID."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Add workspace ID to query params if provided
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        # Delete label
        client.delete(f"/labels/{label_id}", params=params)

        # Output response
        result = LabelDeleted(
            label_id=int(label_id),
            workspace_id=int(workspace) if workspace else 0,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_labels_error(e)


@app.command("list")
def list_labels(
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ] = None,
    label_type: Annotated[
        str,
        typer.Option("-t", "--type", help="Label type filter: all, simple, or resource"),
    ] = "all",
    filter_text: Annotated[
        Optional[str],
        typer.Option("-f", "--filter", help="Text to filter labels by name"),
    ] = None,
    max_results: Annotated[
        int,
        typer.Option("--max", help="Maximum number of results to return"),
    ] = 100,
    offset: Annotated[
        int,
        typer.Option("--offset", help="Offset for pagination"),
    ] = 0,
) -> None:
    """List labels in the workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build query parameters
        params = {
            "max": max_results,
            "offset": offset,
        }

        if workspace:
            params["workspaceId"] = workspace

        # Add type filter if not 'all', otherwise use 'all' as default
        if label_type and label_type != "all":
            params["type"] = label_type
        else:
            params["type"] = "all"

        # Add text filter if provided
        if filter_text:
            params["search"] = filter_text

        # Get labels list
        response = client.get("/labels", params=params)
        labels = response.get("labels", [])

        # Output response
        result = LabelsList(
            workspace_id=int(workspace) if workspace else 0,
            label_type=label_type,
            labels=labels,
            filter_text=filter_text,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_labels_error(e)
