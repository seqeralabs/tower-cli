"""
Secrets commands for Seqera CLI.

Manage pipeline secrets in the workspace.
"""

import sys
from typing import Annotated, Optional

import typer

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    NotFoundError,
    SeqeraError,
)
from seqera.main import get_client, get_output_format
from seqera.responses import (
    SecretAdded,
    SecretDeleted,
    SecretsList,
    SecretUpdated,
    SecretView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create secrets app
app = typer.Typer(
    name="secrets",
    help="Manage pipeline secrets",
    no_args_is_help=True,
)

# Default workspace name
USER_WORKSPACE_NAME = "user"


def handle_secrets_error(e: Exception) -> None:
    """Handle secrets command errors."""
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


def get_secret_by_name(client: SeqeraClient, name: str) -> dict | None:
    """
    Find a secret by name.

    Args:
        client: API client
        name: Secret name

    Returns:
        Secret dict or None if not found
    """
    response = client.get("/pipeline-secrets")
    secrets = response.get("pipelineSecrets", [])

    for secret in secrets:
        if secret.get("name") == name:
            return secret

    return None


@app.command("list")
def list_secrets() -> None:
    """List all pipeline secrets in the workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get secrets list
        response = client.get("/pipeline-secrets")
        secrets = response.get("pipelineSecrets", [])

        # Output response
        result = SecretsList(
            workspace=USER_WORKSPACE_NAME,
            secrets=secrets,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_secrets_error(e)


@app.command("add")
def add_secret(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Secret name"),
    ],
    value: Annotated[
        str,
        typer.Option("-v", "--value", help="Secret value"),
    ],
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if secret already exists"),
    ] = False,
) -> None:
    """Add a new pipeline secret."""
    try:
        client = get_client()
        output_format = get_output_format()

        # If overwrite is set, check if secret exists and delete it
        if overwrite:
            existing_secret = get_secret_by_name(client, name)
            if existing_secret:
                secret_id = existing_secret.get("id")
                client.delete(f"/pipeline-secrets/{secret_id}")

        # Add the secret
        payload = {
            "name": name,
            "value": value,
        }

        response = client.post("/pipeline-secrets", json=payload)
        secret_id = response.get("secretId")

        # Output response
        result = SecretAdded(
            workspace=USER_WORKSPACE_NAME,
            secret_id=secret_id,
            name=name,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_secrets_error(e)


@app.command("delete")
def delete_secret(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Secret name to delete"),
    ],
) -> None:
    """Delete a pipeline secret by name."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find secret by name
        secret = get_secret_by_name(client, name)
        if not secret:
            raise NotFoundError(f"Secret '{name}' not found")

        secret_id = secret.get("id")

        # Delete the secret
        client.delete(f"/pipeline-secrets/{secret_id}")

        # Output response
        result = SecretDeleted(
            secret=secret,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_secrets_error(e)


@app.command("view")
def view_secret(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Secret name to view"),
    ],
) -> None:
    """View details of a pipeline secret."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find secret by name
        secret = get_secret_by_name(client, name)
        if not secret:
            raise NotFoundError(f"Secret '{name}' not found")

        # Output response
        result = SecretView(
            workspace=USER_WORKSPACE_NAME,
            secret=secret,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_secrets_error(e)


@app.command("update")
def update_secret(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Secret name to update"),
    ],
    value: Annotated[
        str,
        typer.Option("-v", "--value", help="New secret value"),
    ],
) -> None:
    """Update an existing pipeline secret."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Find secret by name
        secret = get_secret_by_name(client, name)
        if not secret:
            raise NotFoundError(f"Secret '{name}' not found")

        secret_id = secret.get("id")

        # Update the secret
        payload = {
            "value": value,
        }

        client.put(f"/pipeline-secrets/{secret_id}", json=payload)

        # Output response
        result = SecretUpdated(
            workspace=USER_WORKSPACE_NAME,
            name=name,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_secrets_error(e)
