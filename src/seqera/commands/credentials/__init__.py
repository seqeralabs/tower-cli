"""
Credentials commands for Seqera CLI.

Manage workspace credentials for various cloud providers and services.
"""

import sys
from typing import Optional

import typer
from typing_extensions import Annotated

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    CredentialsNotFoundException,
    NotFoundError,
    SeqeraError,
)
from seqera.main import get_client, get_output_format
from seqera.responses import CredentialsAdded, CredentialsUpdated
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


# AWS Credentials Commands

@add_app.command("aws")
def add_aws(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Credentials name"),
    ],
    access_key: Annotated[
        Optional[str],
        typer.Option("-a", "--access-key", help="AWS access key"),
    ] = None,
    secret_key: Annotated[
        Optional[str],
        typer.Option("-s", "--secret-key", help="AWS secret key"),
    ] = None,
    assume_role_arn: Annotated[
        Optional[str],
        typer.Option("-r", "--assume-role-arn", help="IAM role ARN to assume"),
    ] = None,
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new AWS workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build credentials payload
        keys = {}
        if access_key and secret_key:
            keys["accessKey"] = access_key
            keys["secretKey"] = secret_key
        if assume_role_arn:
            keys["assumeRoleArn"] = assume_role_arn

        payload = {
            "credentials": {
                "name": name,
                "provider": "aws",
                "keys": keys,
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="AWS",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)


@update_app.command("aws")
def update_aws(
    credentials_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Credentials ID"),
    ],
    assume_role_arn: Annotated[
        Optional[str],
        typer.Option("-r", "--assume-role-arn", help="IAM role ARN to assume"),
    ] = None,
    access_key: Annotated[
        Optional[str],
        typer.Option("-a", "--access-key", help="AWS access key"),
    ] = None,
    secret_key: Annotated[
        Optional[str],
        typer.Option("-s", "--secret-key", help="AWS secret key"),
    ] = None,
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
) -> None:
    """Update existing AWS workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # First, fetch existing credentials
        try:
            existing_response = client.get(f"/credentials/{credentials_id}")
        except NotFoundError:
            raise CredentialsNotFoundException(credentials_id, USER_WORKSPACE_NAME)

        existing = existing_response.get("credentials", {})

        # Build updated credentials payload
        keys = {}
        if access_key and secret_key:
            keys["accessKey"] = access_key
            keys["secretKey"] = secret_key
        if assume_role_arn:
            keys["assumeRoleArn"] = assume_role_arn

        payload = {
            "credentials": {
                "id": credentials_id,
                "name": existing.get("name", ""),
                "provider": "aws",
                "keys": keys,
            }
        }

        # Update credentials
        client.put(f"/credentials/{credentials_id}", json=payload)

        # Output response
        result = CredentialsUpdated(
            provider="AWS",
            name=existing.get("name", ""),
            workspace=USER_WORKSPACE_NAME,
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
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Azure workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "azure",
                "keys": {
                    "batchName": batch_name,
                    "batchKey": batch_key,
                    "storageName": storage_name,
                    "storageKey": storage_key,
                },
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="AZURE",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Google workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Read the service account key file
        from pathlib import Path

        key_path = Path(key)
        if not key_path.exists():
            raise FileNotFoundError(f"Service account key file not found: {key}")

        key_content = key_path.read_text()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "google",
                "keys": {
                    "data": key_content,
                },
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="GOOGLE",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        typer.Option("-p", "--password", help="GitHub account password or access token (recommended)"),
    ],
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new GitHub workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "github",
                "keys": {
                    "username": username,
                    "password": password,
                },
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="GITHUB",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        typer.Option("-p", "--password", help="GitLab account password or access token (recommended)"),
    ],
    token: Annotated[
        str,
        typer.Option("-t", "--token", help="GitLab account access token"),
    ],
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new GitLab workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "gitlab",
                "keys": {
                    "username": username,
                    "password": password,
                    "token": token,
                },
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="GITLAB",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Gitea workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "gitea",
                "keys": {
                    "username": username,
                    "password": password,
                },
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="GITEA",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Bitbucket workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "bitbucket",
                "keys": {
                    "username": username,
                    "password": password,
                },
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="BITBUCKET",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        Optional[str],
        typer.Option("--base-url", help="Repository base URL"),
    ] = None,
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new CodeCommit workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "codecommit",
                "keys": {
                    "username": access_key,
                    "password": secret_key,
                },
            }
        }

        # Add base URL if provided
        if base_url:
            payload["credentials"]["baseUrl"] = base_url

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="CODECOMMIT",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        typer.Option("-u", "--username", help="The user name to grant you access to the container registry"),
    ],
    password: Annotated[
        str,
        typer.Option("-p", "--password", help="The password to grant you access to the container registry"),
    ],
    registry: Annotated[
        str,
        typer.Option("-r", "--registry", help="The container registry server name"),
    ] = "docker.io",
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Container Registry workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "container-reg",
                "keys": {
                    "userName": username,  # Note: camelCase with capital N
                    "password": password,
                    "registry": registry,
                },
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="CONTAINER_REG",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        Optional[str],
        typer.Option("-p", "--passphrase", help="Passphrase associated with the private key"),
    ] = None,
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new SSH workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Read the private key file
        from pathlib import Path

        key_path = Path(key)
        if not key_path.exists():
            raise FileNotFoundError(f"SSH private key file not found: {key}")

        key_content = key_path.read_text()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "ssh",
                "keys": {
                    "privateKey": key_content,
                },
            }
        }

        # Add passphrase if provided
        if passphrase:
            payload["credentials"]["keys"]["passphrase"] = passphrase

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="SSH",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        Optional[str],
        typer.Option("-t", "--token", help="Service account token"),
    ] = None,
    certificate: Annotated[
        Optional[str],
        typer.Option("-c", "--certificate", help="Client certificate file"),
    ] = None,
    private_key: Annotated[
        Optional[str],
        typer.Option("-k", "--private-key", help="Client key file"),
    ] = None,
    workspace: Annotated[
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new Kubernetes workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Validate input - either token OR certificate+private_key
        if token:
            # Token mode
            keys = {"token": token}
        elif certificate and private_key:
            # Certificate mode
            from pathlib import Path

            cert_path = Path(certificate)
            if not cert_path.exists():
                raise FileNotFoundError(f"Certificate file not found: {certificate}")

            key_path = Path(private_key)
            if not key_path.exists():
                raise FileNotFoundError(f"Private key file not found: {private_key}")

            cert_content = cert_path.read_text()
            key_content = key_path.read_text()

            keys = {
                "certificate": cert_content,
                "privateKey": key_content,
            }
        else:
            raise ValueError("Must provide either --token OR both --certificate and --private-key")

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "k8s",
                "keys": keys,
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="K8S",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
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
        Optional[str],
        typer.Option("-w", "--workspace", help="Workspace reference (organization/workspace)"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if credentials already exist"),
    ] = False,
) -> None:
    """Add new TW Agent workspace credentials."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Build credentials payload
        payload = {
            "credentials": {
                "name": name,
                "provider": "tw-agent",
                "keys": {
                    "connectionId": connection_id,
                    "workDir": work_dir,
                },
            }
        }

        # Create credentials
        response = client.post("/credentials", json=payload)

        # Output response
        result = CredentialsAdded(
            provider="TW_AGENT",
            credentials_id=response.get("credentialsId", ""),
            name=name,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_credentials_error(e)
