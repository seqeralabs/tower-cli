"""
Compute environment commands for Seqera CLI.

Manage workspace compute environments for various cloud platforms.
"""

import sys
from typing import Optional

import typer
from typing_extensions import Annotated

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    ComputeEnvNotFoundException,
    NotFoundError,
    SeqeraError,
)
from seqera.main import get_client, get_output_format
from seqera.responses.computeenvs import (
    ComputeEnvAdded,
    ComputeEnvDeleted,
    ComputeEnvExport,
    ComputeEnvImported,
    ComputeEnvList,
    ComputeEnvsPrimaryGet,
    ComputeEnvsPrimarySet,
    ComputeEnvUpdated,
    ComputeEnvView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create compute-envs app
app = typer.Typer(
    name="compute-envs",
    help="Manage workspace compute environments",
    no_args_is_help=True,
)

# Create add subcommand app
add_app = typer.Typer(
    name="add",
    help="Add new compute environment",
    no_args_is_help=True,
)
app.add_typer(add_app)

# Create primary subcommand app
primary_app = typer.Typer(
    name="primary",
    help="Manage primary compute environment",
    no_args_is_help=True,
)
app.add_typer(primary_app)

# Constants
USER_WORKSPACE_NAME = "user"


def output_response(response, output_format: OutputFormat) -> None:
    """Output response in the requested format."""
    if output_format == OutputFormat.JSON:
        output_json(response.to_dict())
    elif output_format == OutputFormat.YAML:
        output_yaml(response.to_dict())
    else:
        output_console(response.to_console())


def handle_compute_env_error(error: Exception) -> None:
    """Handle compute environment errors and exit."""
    if isinstance(error, AuthenticationError):
        output_error(str(error))
        sys.exit(1)
    elif isinstance(error, ComputeEnvNotFoundException):
        output_error(str(error))
        sys.exit(1)
    elif isinstance(error, NotFoundError):
        output_error(str(error))
        sys.exit(1)
    elif isinstance(error, SeqeraError):
        output_error(str(error))
        sys.exit(1)
    else:
        output_error(str(error))
        sys.exit(1)


@app.command("delete")
def delete_compute_env(
    compute_env_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Compute environment ID to delete"),
    ],
) -> None:
    """Delete a compute environment."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Delete compute environment
        client.delete(f"/compute-envs/{compute_env_id}")

        # Output response
        result = ComputeEnvDeleted(
            compute_env_id=compute_env_id,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except NotFoundError:
        # Handle 403 as not found
        handle_compute_env_error(
            ComputeEnvNotFoundException(compute_env_id, USER_WORKSPACE_NAME)
        )
    except Exception as e:
        handle_compute_env_error(e)


@app.command("list")
def list_compute_envs() -> None:
    """List all compute environments in the workspace."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get compute environments list
        response = client.get("/compute-envs")
        compute_envs = response.get("computeEnvs", [])

        # Output response
        result = ComputeEnvList(
            workspace=USER_WORKSPACE_NAME,
            compute_envs=compute_envs,
            base_workspace_url=None,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)


@app.command("view")
def view_compute_env(
    compute_env_id: Annotated[
        Optional[str],
        typer.Option("-i", "--id", help="Compute environment ID to view"),
    ] = None,
    compute_env_name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Compute environment name to view"),
    ] = None,
) -> None:
    """View compute environment details."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Resolve compute environment ID if name is provided
        if compute_env_name and not compute_env_id:
            # Get list and find by name
            response = client.get("/compute-envs")
            compute_envs = response.get("computeEnvs", [])
            for ce in compute_envs:
                if ce.get("name") == compute_env_name:
                    compute_env_id = ce.get("id")
                    break
            if not compute_env_id:
                raise ComputeEnvNotFoundException(compute_env_name, USER_WORKSPACE_NAME)

        if not compute_env_id:
            output_error("Either --id or --name must be provided")
            sys.exit(1)

        # Get compute environment details
        compute_env = client.get(f"/compute-envs/{compute_env_id}")

        # Output response
        result = ComputeEnvView(
            workspace=USER_WORKSPACE_NAME,
            compute_env=compute_env,
        )

        output_response(result, output_format)

    except Exception as e:
        # Handle 403 as not found
        if hasattr(e, "status_code") and e.status_code == 403:
            ref = compute_env_id or compute_env_name
            handle_compute_env_error(
                ComputeEnvNotFoundException(ref, USER_WORKSPACE_NAME)
            )
        handle_compute_env_error(e)
