"""
Credentials commands for Seqera CLI.

Manage workspace credentials for various cloud providers and services.
"""

import sys
from pathlib import Path
from typing import Annotated

import typer

from seqera.exceptions import (
    AuthenticationError,
    CredentialsNotFoundException,
    NotFoundError,
    SeqeraError,
)
from seqera.main import get_output_format, get_sdk
from seqera.responses import (
    CredentialsAdded,
    CredentialsDeleted,
    CredentialsList,
    CredentialsUpdated,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create credentials app
app = typer.Typer(
    name="credentials",
    help="Manage workspace credentials",
    no_args_is_help=True,
)

# Create add and update subcommands
add_app = typer.Typer(
    name="add",
    help="Add new workspace credentials",
    no_args_is_help=True,
)

update_app = typer.Typer(
    name="update",
    help="Update existing workspace credentials",
    no_args_is_help=True,
)

# Register subcommands
app.add_typer(add_app, name="add")
app.add_typer(update_app, name="update")

# Default workspace name
USER_WORKSPACE_NAME = "user"


def handle_credentials_error(e: Exception) -> None:
    """Handle credentials command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
        sys.exit(1)
    elif isinstance(e, CredentialsNotFoundException):
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


def get_workspace_name_by_ref(sdk, workspace_id: str | None) -> str | None:
    """Get workspace name for display formatting."""
    if not workspace_id:
        return None

    for ws in sdk.workspaces.list():
        if str(ws.workspace_id) == str(workspace_id):
            return f"{ws.org_name} / {ws.workspace_name}"

    return None


@update_app.command("aws")
def update_aws(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    access_key: Annotated[
        str | None,
        typer.Option("-a", "--access-key", help="AWS access key"),
    ] = None,
    secret_key: Annotated[
        str | None,
        typer.Option("-s", "--secret-key", help="AWS secret key"),
    ] = None,
    assume_role_arn: Annotated[
        str | None,
        typer.Option("-r", "--assume-role-arn", help="IAM role ARN to assume"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing AWS workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials to preserve current values
        existing = sdk.credentials.get(credentials_id, workspace=workspace)

        # Build keys - only include values that are provided or exist
        keys = {}
        if access_key:
            keys["accessKey"] = access_key
        if secret_key:
            keys["secretKey"] = secret_key
        if assume_role_arn:
            keys["assumeRoleArn"] = assume_role_arn

        # Build update payload
        payload = {
            "credentials": {
                "id": credentials_id,
                "name": existing.name,
                "provider": "aws",
                "keys": keys,
            }
        }

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="AWS",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("google")
def update_google(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    key: Annotated[
        str | None,
        typer.Option("-k", "--key", help="JSON file with the service account key"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing Google workspace credentials."""

    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Read key file if provided
        key_data = None
        if key:
            key_path = Path(key)
            if key_path.exists():
                key_data = key_path.read_text()
            else:
                key_data = key
        else:
            key_data = keys.get("data")

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "google",
                "keys": {
                    "data": key_data,
                },
            }
        }

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="Google",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("azure")
def update_azure(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    batch_name: Annotated[
        str | None,
        typer.Option("--batch-name", help="Azure batch account name"),
    ] = None,
    batch_key: Annotated[
        str | None,
        typer.Option("--batch-key", help="Azure batch account key"),
    ] = None,
    storage_name: Annotated[
        str | None,
        typer.Option("--storage-name", help="Azure blob storage account name"),
    ] = None,
    storage_key: Annotated[
        str | None,
        typer.Option("--storage-key", help="Azure blob storage account key"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing Azure workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "azure",
                "keys": {
                    "batchName": batch_name or keys.get("batchName"),
                    "batchKey": batch_key or keys.get("batchKey"),
                    "storageName": storage_name or keys.get("storageName"),
                    "storageKey": storage_key or keys.get("storageKey"),
                },
            }
        }

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="Azure",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("github")
def update_github(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    username: Annotated[
        str | None,
        typer.Option("-u", "--username", help="GitHub username"),
    ] = None,
    password: Annotated[
        str | None,
        typer.Option("-p", "--password", help="GitHub account password or access token"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing GitHub workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "github",
                "keys": {
                    "username": username or keys.get("username"),
                    "password": password or keys.get("password"),
                },
            }
        }

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="GitHub",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("gitlab")
def update_gitlab(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    username: Annotated[
        str | None,
        typer.Option("-u", "--username", help="GitLab username"),
    ] = None,
    password: Annotated[
        str | None,
        typer.Option("-p", "--password", help="GitLab account password or access token"),
    ] = None,
    token: Annotated[
        str | None,
        typer.Option("-t", "--token", help="GitLab account access token"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing GitLab workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "gitlab",
                "keys": {
                    "username": username or keys.get("username"),
                    "password": password or token or keys.get("password"),
                },
            }
        }

        if token or keys.get("token"):
            payload["credentials"]["keys"]["token"] = token or keys.get("token")

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="GitLab",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("container-reg")
def update_container_registry(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    username: Annotated[
        str | None,
        typer.Option("-u", "--username", help="Username for container registry"),
    ] = None,
    password: Annotated[
        str | None,
        typer.Option("-p", "--password", help="Password for container registry"),
    ] = None,
    registry: Annotated[
        str | None,
        typer.Option("-r", "--registry", help="Container registry server name"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing Container Registry workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "container-reg",
                "keys": {
                    "userName": username or keys.get("userName"),
                    "password": password or keys.get("password"),
                    "registry": registry or keys.get("registry", "docker.io"),
                },
            }
        }

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="Container Registry",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("bitbucket")
def update_bitbucket(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    username: Annotated[
        str | None,
        typer.Option("-u", "--username", help="Bitbucket username"),
    ] = None,
    password: Annotated[
        str | None,
        typer.Option("-p", "--password", help="Bitbucket App password"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing Bitbucket workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "bitbucket",
                "keys": {
                    "username": username or keys.get("username"),
                    "password": password or keys.get("password"),
                },
            }
        }

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="Bitbucket",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("codecommit")
def update_codecommit(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    access_key: Annotated[
        str | None,
        typer.Option("--access-key", help="CodeCommit AWS access key"),
    ] = None,
    secret_key: Annotated[
        str | None,
        typer.Option("--secret-key", help="CodeCommit AWS secret key"),
    ] = None,
    base_url: Annotated[
        str | None,
        typer.Option("--base-url", help="Repository base URL"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing CodeCommit workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "codecommit",
                "keys": {
                    "username": access_key or keys.get("username"),
                    "password": secret_key or keys.get("password"),
                },
            }
        }

        if base_url or existing.base_url:
            payload["credentials"]["baseUrl"] = base_url or existing.base_url

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="CodeCommit",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("ssh")
def update_ssh(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    key: Annotated[
        str | None,
        typer.Option("-k", "--key", help="SSH private key file"),
    ] = None,
    passphrase: Annotated[
        str | None,
        typer.Option("-p", "--passphrase", help="Passphrase associated with the private key"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing SSH workspace credentials."""

    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Read key file if provided
        key_data = None
        if key:
            key_path = Path(key)
            if key_path.exists():
                key_data = key_path.read_text()
            else:
                key_data = key
        else:
            key_data = keys.get("privateKey")

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "ssh",
                "keys": {
                    "privateKey": key_data,
                },
            }
        }

        if passphrase or keys.get("passphrase"):
            payload["credentials"]["keys"]["passphrase"] = passphrase or keys.get("passphrase")

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="SSH",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("k8s")
def update_k8s(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    token: Annotated[
        str | None,
        typer.Option("-t", "--token", help="Service account token"),
    ] = None,
    certificate: Annotated[
        str | None,
        typer.Option("-c", "--certificate", help="Client certificate file"),
    ] = None,
    private_key: Annotated[
        str | None,
        typer.Option("-k", "--private-key", help="Client key file"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing Kubernetes workspace credentials."""

    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Read files if provided
        cert_data = None
        if certificate:
            cert_path = Path(certificate)
            if cert_path.exists():
                cert_data = cert_path.read_text()
            else:
                cert_data = certificate
        else:
            cert_data = keys.get("certificate")

        key_data = None
        if private_key:
            key_path = Path(private_key)
            if key_path.exists():
                key_data = key_path.read_text()
            else:
                key_data = private_key
        else:
            key_data = keys.get("privateKey")

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "k8s",
                "keys": {},
            }
        }

        if token or keys.get("token"):
            payload["credentials"]["keys"]["token"] = token or keys.get("token")
        if cert_data:
            payload["credentials"]["keys"]["certificate"] = cert_data
        if key_data:
            payload["credentials"]["keys"]["privateKey"] = key_data

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="Kubernetes",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("agent")
def update_agent(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    connection_id: Annotated[
        str | None,
        typer.Option("--connection-id", help="Connection identifier"),
    ] = None,
    work_dir: Annotated[
        str | None,
        typer.Option("--work-dir", help="Default work directory"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing TW Agent workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)
        workspace_name = get_workspace_name_by_ref(sdk, workspace)

        # Get existing credentials
        existing = sdk.credentials.get(credentials_id, workspace=workspace)
        keys = existing.keys or {}

        # Build update payload
        payload = {
            "credentials": {
                "name": existing.name,
                "provider": "tw-agent",
                "keys": {
                    "connectionId": connection_id or keys.get("connectionId"),
                },
            }
        }

        if work_dir or keys.get("workDir"):
            payload["credentials"]["keys"]["workDir"] = work_dir or keys.get("workDir")

        # Update credentials
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        # Output response
        result = CredentialsUpdated(
            provider="Tower Agent",
            name=existing.name,
            workspace=f"[{workspace_name}]" if workspace_name else workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# AWS Credentials Commands


@add_app.command("aws")
def add_aws(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    access_key: Annotated[
        str | None,
        typer.Option("-a", "--access-key", help="AWS access key"),
    ] = None,
    secret_key: Annotated[
        str | None,
        typer.Option("-s", "--secret-key", help="AWS secret key"),
    ] = None,
    assume_role_arn: Annotated[
        str | None,
        typer.Option("-r", "--assume-role-arn", help="IAM role ARN to assume"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new AWS workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK
        creds = sdk.credentials.add_aws(
            name=name,
            workspace=workspace,
            access_key=access_key,
            secret_key=secret_key,
            assume_role_arn=assume_role_arn,
        )

        # Output response
        result = CredentialsAdded(
            provider="AWS",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# Azure Credentials Commands


@add_app.command("azure")
def add_azure(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    batch_name: Annotated[
        str,
        typer.Option("--batch-name", help="Azure batch account name"),
    ],
    batch_key: Annotated[
        str,
        typer.Option("--batch-key", help="Azure batch account key"),
    ],
    storage_name: Annotated[
        str,
        typer.Option("--storage-name", help="Azure blob storage account name"),
    ],
    storage_key: Annotated[
        str,
        typer.Option("--storage-key", help="Azure blob storage account key"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Azure workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK
        creds = sdk.credentials.add_azure(
            name=name,
            workspace=workspace,
            batch_name=batch_name,
            batch_key=batch_key,
            storage_name=storage_name,
            storage_key=storage_key,
        )

        # Output response
        result = CredentialsAdded(
            provider="AZURE",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# Google Credentials Commands


@add_app.command("google")
def add_google(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    key: Annotated[
        str,
        typer.Option("-k", "--key", help="JSON file with the service account key"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Google workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK (it handles file reading)
        creds = sdk.credentials.add_google(
            name=name,
            key_file=key,
            workspace=workspace,
        )

        # Output response
        result = CredentialsAdded(
            provider="GOOGLE",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# GitHub Credentials Commands


@add_app.command("github")
def add_github(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    username: Annotated[
        str,
        typer.Option("-u", "--username", help="GitHub username"),
    ],
    password: Annotated[
        str,
        typer.Option(
            "-p", "--password", help="GitHub account password or access token (recommended)"
        ),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new GitHub workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK
        creds = sdk.credentials.add_github(
            name=name,
            username=username,
            password=password,
            workspace=workspace,
        )

        # Output response
        result = CredentialsAdded(
            provider="GITHUB",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# GitLab Credentials Commands


@add_app.command("gitlab")
def add_gitlab(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    username: Annotated[
        str,
        typer.Option("-u", "--username", help="GitLab username"),
    ],
    password: Annotated[
        str,
        typer.Option(
            "-p", "--password", help="GitLab account password or access token (recommended)"
        ),
    ],
    token: Annotated[
        str,
        typer.Option("-t", "--token", help="GitLab account access token"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new GitLab workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK
        creds = sdk.credentials.add_gitlab(
            name=name,
            username=username,
            password=password,
            token=token,
            workspace=workspace,
        )

        # Output response
        result = CredentialsAdded(
            provider="GITLAB",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# Gitea Credentials Commands


@add_app.command("gitea")
def add_gitea(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    username: Annotated[
        str,
        typer.Option("-u", "--username", help="Gitea username"),
    ],
    password: Annotated[
        str,
        typer.Option("-p", "--password", help="Gitea account password"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Gitea workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK
        creds = sdk.credentials.add_gitea(
            name=name,
            username=username,
            password=password,
            workspace=workspace,
        )

        # Output response
        result = CredentialsAdded(
            provider="GITEA",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# Bitbucket Credentials Commands


@add_app.command("bitbucket")
def add_bitbucket(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    username: Annotated[
        str,
        typer.Option("-u", "--username", help="Bitbucket username"),
    ],
    password: Annotated[
        str,
        typer.Option("-p", "--password", help="Bitbucket App password"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Bitbucket workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK
        creds = sdk.credentials.add_bitbucket(
            name=name,
            username=username,
            password=password,
            workspace=workspace,
        )

        # Output response
        result = CredentialsAdded(
            provider="BITBUCKET",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# CodeCommit Credentials Commands


@add_app.command("codecommit")
def add_codecommit(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    access_key: Annotated[
        str,
        typer.Option("--access-key", help="CodeCommit AWS access key"),
    ],
    secret_key: Annotated[
        str,
        typer.Option("--secret-key", help="CodeCommit AWS secret key"),
    ],
    base_url: Annotated[
        str | None,
        typer.Option("--base-url", help="Repository base URL"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new CodeCommit workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK
        creds = sdk.credentials.add_codecommit(
            name=name,
            access_key=access_key,
            secret_key=secret_key,
            workspace=workspace,
            base_url=base_url,
        )

        # Output response
        result = CredentialsAdded(
            provider="CODECOMMIT",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# Container Registry Credentials Commands


@add_app.command("container-reg")
def add_container_registry(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    username: Annotated[
        str,
        typer.Option(
            "-u", "--username", help="The user name to grant you access to the container registry"
        ),
    ],
    password: Annotated[
        str,
        typer.Option(
            "-p", "--password", help="The password to grant you access to the container registry"
        ),
    ],
    registry: Annotated[
        str,
        typer.Option("-r", "--registry", help="The container registry server name"),
    ] = "docker.io",
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Container Registry workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK
        creds = sdk.credentials.add_container_registry(
            name=name,
            username=username,
            password=password,
            workspace=workspace,
            registry=registry,
        )

        # Output response
        result = CredentialsAdded(
            provider="CONTAINER_REG",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# SSH Credentials Commands


@add_app.command("ssh")
def add_ssh(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    key: Annotated[
        str,
        typer.Option("-k", "--key", help="SSH private key file"),
    ],
    passphrase: Annotated[
        str | None,
        typer.Option("-p", "--passphrase", help="Passphrase associated with the private key"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new SSH workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK (it handles file reading)
        creds = sdk.credentials.add_ssh(
            name=name,
            private_key=key,
            workspace=workspace,
            passphrase=passphrase,
        )

        # Output response
        result = CredentialsAdded(
            provider="SSH",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# Kubernetes Credentials Commands


@add_app.command("k8s")
def add_k8s(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    token: Annotated[
        str | None,
        typer.Option("-t", "--token", help="Service account token"),
    ] = None,
    certificate: Annotated[
        str | None,
        typer.Option("-c", "--certificate", help="Client certificate file"),
    ] = None,
    private_key: Annotated[
        str | None,
        typer.Option("-k", "--private-key", help="Client key file"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Kubernetes workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Validate input - either token OR certificate+private_key
        if not token and not (certificate and private_key):
            raise ValueError("Must provide either --token OR both --certificate and --private-key")

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK (it handles file reading)
        creds = sdk.credentials.add_k8s(
            name=name,
            workspace=workspace,
            token=token,
            certificate=certificate,
            private_key=private_key,
        )

        # Output response
        result = CredentialsAdded(
            provider="K8S",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


# TW Agent Credentials Commands


@add_app.command("agent")
def add_agent(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    connection_id: Annotated[
        str,
        typer.Option("--connection-id", help="Connection identifier"),
    ],
    work_dir: Annotated[
        str,
        typer.Option("--work-dir", help="Default work directory"),
    ] = "$TW_AGENT_WORK",
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new TW Agent workspace credentials."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Add credentials using SDK
        creds = sdk.credentials.add_agent(
            name=name,
            connection_id=connection_id,
            workspace=workspace,
            work_dir=work_dir,
        )

        # Output response
        result = CredentialsAdded(
            provider="TW_AGENT",
            credentials_id=creds.id,
            name=name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@app.command("list")
def list_credentials(
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """List all credentials in the workspace."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Get credentials using SDK
        creds_list = list(sdk.credentials.list(workspace=workspace))

        # Convert to dicts for response formatting (mode='json' to serialize datetimes)
        credentials = [cred.model_dump(by_alias=True, mode="json") for cred in creds_list]

        # Output response
        result = CredentialsList(
            workspace=workspace_ref,
            credentials=credentials,
            base_workspace_url=None,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@app.command("delete")
def delete_credentials(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID to delete"),
    ],
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Delete credentials by ID."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        workspace_ref = get_workspace_ref(sdk, workspace)

        # Delete credentials using SDK
        sdk.credentials.delete(credentials_id, workspace=workspace)

        # Output response
        result = CredentialsDeleted(
            credentials_id=credentials_id,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)
