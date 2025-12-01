"""
Datasets commands for Seqera CLI.

Manage datasets in workspaces.
"""

import sys
from pathlib import Path
from typing import Optional

import typer
from typing_extensions import Annotated

from seqera.api.client import SeqeraClient
from seqera.exceptions import NotFoundError, SeqeraError
from seqera.main import get_client, get_output_format
from seqera.responses import (
    DatasetAdded,
    DatasetDeleted,
    DatasetDownload,
    DatasetsList,
    DatasetUpdated,
    DatasetUrl,
    DatasetVersionsList,
    DatasetView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create datasets app
app = typer.Typer(
    name="datasets",
    help="Manage workspace datasets",
    no_args_is_help=True,
)


def handle_datasets_error(e: Exception) -> None:
    """Handle datasets command errors."""
    if isinstance(e, NotFoundError):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, SeqeraError):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, FileNotFoundError):
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


@app.command("list")
def list_datasets(
    workspace: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
) -> None:
    """List all datasets in a workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get datasets list
        response = client.get(f"/workspaces/{workspace}/datasets")
        datasets = response.get("datasets", [])

        # Output response
        result = DatasetsList(
            workspace_id=workspace,
            datasets=datasets,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_datasets_error(e)


@app.command("view")
def view_dataset(
    workspace: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    dataset_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Dataset ID"),
    ],
    subcommand: Annotated[
        Optional[str],
        typer.Argument(help="Subcommand: versions"),
    ] = None,
) -> None:
    """View dataset details or versions."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get dataset metadata first to verify it exists
        metadata_response = client.get(f"/workspaces/{workspace}/datasets/{dataset_id}/metadata")

        if subcommand == "versions":
            # Get dataset versions
            versions_response = client.get(f"/workspaces/{workspace}/datasets/{dataset_id}/versions")
            versions = versions_response.get("versions", [])

            # Output response
            result = DatasetVersionsList(
                dataset_id=dataset_id,
                workspace_id=workspace,
                versions=versions,
            )
        else:
            # View dataset details
            dataset = metadata_response.get("dataset", {})

            # Output response
            result = DatasetView(
                dataset=dataset,
                workspace_id=workspace,
            )

        output_response(result, output_format)

    except Exception as e:
        handle_datasets_error(e)


@app.command("delete")
def delete_dataset(
    workspace: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    dataset_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Dataset ID"),
    ],
) -> None:
    """Delete a dataset."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get dataset metadata first to verify it exists
        metadata_response = client.get(f"/workspaces/{workspace}/datasets/{dataset_id}/metadata")

        # Delete dataset
        client.delete(f"/workspaces/{workspace}/datasets/{dataset_id}")

        # Output response
        result = DatasetDeleted(
            dataset_id=dataset_id,
            workspace_id=workspace,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_datasets_error(e)


@app.command("url")
def get_dataset_url(
    workspace: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    dataset_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Dataset ID"),
    ],
    version: Annotated[
        Optional[int],
        typer.Option("-v", "--version", help="Dataset version (default: latest)"),
    ] = None,
) -> None:
    """Get the URL for a dataset version."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get dataset metadata first to verify it exists
        metadata_response = client.get(f"/workspaces/{workspace}/datasets/{dataset_id}/metadata")

        # Get dataset versions
        versions_response = client.get(f"/workspaces/{workspace}/datasets/{dataset_id}/versions")
        versions = versions_response.get("versions", [])

        if not versions:
            raise SeqeraError("No versions found for this dataset")

        # Get the requested version or latest
        if version:
            version_data = next((v for v in versions if v.get("version") == version), None)
            if not version_data:
                raise SeqeraError(f"Version {version} not found")
        else:
            # Get latest version (highest version number)
            version_data = max(versions, key=lambda v: v.get("version", 0))

        url = version_data.get("url", "")

        # Output response
        result = DatasetUrl(
            url=url,
            dataset_id=dataset_id,
            workspace_id=workspace,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_datasets_error(e)


@app.command("download")
def download_dataset(
    workspace: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    dataset_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Dataset ID"),
    ],
    version: Annotated[
        Optional[int],
        typer.Option("-v", "--version", help="Dataset version (default: latest)"),
    ] = None,
    output_file: Annotated[
        Optional[str],
        typer.Option("-o", "--output", help="Output file path"),
    ] = None,
) -> None:
    """Download a dataset version."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get dataset metadata first to verify it exists
        metadata_response = client.get(f"/workspaces/{workspace}/datasets/{dataset_id}/metadata")

        # Get dataset versions
        versions_response = client.get(f"/workspaces/{workspace}/datasets/{dataset_id}/versions")
        versions = versions_response.get("versions", [])

        if not versions:
            raise SeqeraError("No versions found for this dataset")

        # Get the requested version or latest
        if version:
            version_data = next((v for v in versions if v.get("version") == version), None)
            if not version_data:
                raise SeqeraError(f"Version {version} not found")
        else:
            # Get latest version (highest version number)
            version_data = max(versions, key=lambda v: v.get("version", 0))

        url = version_data.get("url", "")
        file_name = version_data.get("fileName", "dataset")

        # Determine output file path
        if output_file:
            output_path = Path(output_file)
        else:
            output_path = Path(file_name)

        # Download the file
        import requests
        response = requests.get(url)
        response.raise_for_status()

        # Write to file
        output_path.write_bytes(response.content)

        # Output response
        result = DatasetDownload(
            file_path=str(output_path),
            file_name=file_name,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_datasets_error(e)


@app.command("add")
def add_dataset(
    file_path: Annotated[
        str,
        typer.Argument(help="Path to dataset file"),
    ],
    workspace: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Dataset name"),
    ],
    description: Annotated[
        Optional[str],
        typer.Option("-d", "--description", help="Dataset description"),
    ] = None,
    header: Annotated[
        bool,
        typer.Option("--header", help="Dataset has header row"),
    ] = False,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if dataset already exists"),
    ] = False,
) -> None:
    """Add a new dataset."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Verify file exists
        file = Path(file_path)
        if not file.exists():
            raise FileNotFoundError(f"File path '{file_path}' do not exists.")

        # Check if dataset with same name exists if overwrite is True
        if overwrite:
            # Get existing datasets
            datasets_response = client.get(f"/workspaces/{workspace}/datasets")
            datasets = datasets_response.get("datasets", [])
            existing = next((d for d in datasets if d.get("name") == name), None)
            if existing:
                # Delete existing dataset
                existing_id = existing.get("id")
                client.delete(f"/workspaces/{workspace}/datasets/{existing_id}")

        # Create dataset metadata
        payload = {
            "name": name,
        }
        if description:
            payload["description"] = description

        create_response = client.post(f"/workspaces/{workspace}/datasets", json=payload)
        dataset = create_response.get("dataset", {})
        dataset_id = dataset.get("id")

        # Upload dataset file
        with open(file, "rb") as f:
            files = {"file": (file.name, f, "application/octet-stream")}
            params = {"header": str(header).lower()}
            # Use httpx client directly for multipart upload
            url = f"{client.base_url}/workspaces/{workspace}/datasets/{dataset_id}/upload"
            headers = {
                "Authorization": f"Bearer {client.token}",
            }
            upload_response = client.client.post(url, files=files, params=params, headers=headers)
            if upload_response.status_code not in (200, 201):
                raise SeqeraError(f"Failed to upload dataset file: HTTP {upload_response.status_code}")

        # Output response
        result = DatasetAdded(
            dataset_id=dataset_id,
            name=name,
            workspace_id=workspace,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_datasets_error(e)


@app.command("update")
def update_dataset(
    workspace: Annotated[
        str,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ],
    dataset_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Dataset ID"),
    ],
    new_name: Annotated[
        Optional[str],
        typer.Option("--new-name", help="New dataset name"),
    ] = None,
    description: Annotated[
        Optional[str],
        typer.Option("-d", "--description", help="Dataset description"),
    ] = None,
    file_path: Annotated[
        Optional[str],
        typer.Option("-f", "--file", help="Path to new dataset file"),
    ] = None,
    header: Annotated[
        bool,
        typer.Option("--header", help="Dataset has header row"),
    ] = False,
) -> None:
    """Update an existing dataset."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get existing dataset
        metadata_response = client.get(f"/workspaces/{workspace}/datasets/{dataset_id}/metadata")
        existing_dataset = metadata_response.get("dataset", {})

        # Build update payload
        payload = {
            "name": new_name or existing_dataset.get("name"),
        }
        if description:
            payload["description"] = description

        # Update dataset metadata
        client.put(f"/workspaces/{workspace}/datasets/{dataset_id}", json=payload)

        # Upload new file if provided
        if file_path:
            file = Path(file_path)
            if not file.exists():
                raise FileNotFoundError(f"File path '{file_path}' do not exists.")

            with open(file, "rb") as f:
                files = {"file": (file.name, f, "application/octet-stream")}
                params = {"header": str(header).lower()}
                # Use httpx client directly for multipart upload
                url = f"{client.base_url}/workspaces/{workspace}/datasets/{dataset_id}/upload"
                headers = {
                    "Authorization": f"Bearer {client.token}",
                }
                upload_response = client.client.post(url, files=files, params=params, headers=headers)
                if upload_response.status_code not in (200, 201):
                    raise SeqeraError(f"Failed to upload dataset file: HTTP {upload_response.status_code}")

        # Output response
        result = DatasetUpdated(
            name=new_name or existing_dataset.get("name"),
            workspace_id=workspace,
            dataset_id=dataset_id,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_datasets_error(e)
