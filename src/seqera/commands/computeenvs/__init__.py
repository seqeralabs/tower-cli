"""
Compute environment commands for Seqera CLI.

Manage workspace compute environments for various cloud platforms.
"""

import sys
from typing import Annotated, Optional

import typer

from seqera.exceptions import (
    AuthenticationError,
    ComputeEnvNotFoundException,
    NotFoundError,
    SeqeraError,
)
from seqera.main import get_sdk, get_output_format
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

# Create aws-batch subcommand app (nested under add)
aws_batch_app = typer.Typer(
    name="aws-batch",
    help="Add new AWS Batch compute environment",
    no_args_is_help=True,
)
add_app.add_typer(aws_batch_app)

# Create azure-batch subcommand app (nested under add)
azure_batch_app = typer.Typer(
    name="azure-batch",
    help="Add new Azure Batch compute environment",
    no_args_is_help=True,
)
add_app.add_typer(azure_batch_app)

# Create primary subcommand app
primary_app = typer.Typer(
    name="primary",
    help="Manage primary compute environment",
    no_args_is_help=True,
)
app.add_typer(primary_app)

# Import and register platform commands (after add_app and helper functions are defined)
from seqera.commands.computeenvs.platforms.altair import add_altair
from seqera.commands.computeenvs.platforms.aws_forge import add_aws_forge
from seqera.commands.computeenvs.platforms.aws_manual import add_aws_manual
from seqera.commands.computeenvs.platforms.azure_forge import add_azure_forge
from seqera.commands.computeenvs.platforms.azure_manual import add_azure_manual
from seqera.commands.computeenvs.platforms.eks import add_eks
from seqera.commands.computeenvs.platforms.gke import add_gke
from seqera.commands.computeenvs.platforms.google_batch import add_google_batch
from seqera.commands.computeenvs.platforms.google_life_sciences import add_google_life_sciences
from seqera.commands.computeenvs.platforms.k8s import add_k8s
from seqera.commands.computeenvs.platforms.lsf import add_lsf
from seqera.commands.computeenvs.platforms.moab import add_moab
from seqera.commands.computeenvs.platforms.uge import add_uge

add_app.command("altair", help="Add new Altair PBS Pro compute environment")(add_altair)
add_app.command("eks", help="Add new Amazon EKS compute environment")(add_eks)
add_app.command("gke", help="Add new Google GKE compute environment")(add_gke)
add_app.command("google-batch", help="Add new Google Batch compute environment")(add_google_batch)
add_app.command("google-ls", help="Add new Google Life Sciences compute environment")(
    add_google_life_sciences
)
add_app.command("k8s", help="Add new Kubernetes compute environment")(add_k8s)
add_app.command("uge", help="Add new UNIVA grid engine compute environment")(add_uge)
add_app.command("lsf", help="Add new IBM LSF compute environment")(add_lsf)
add_app.command("moab", help="Add new MOAB compute environment")(add_moab)

# Register AWS Batch subcommands
aws_batch_app.command("manual", help="Add AWS Batch compute environment using existing resources")(
    add_aws_manual
)
aws_batch_app.command(
    "forge", help="Add AWS Batch compute environment with automatic provisioning"
)(add_aws_forge)

# Register Azure Batch subcommands
azure_batch_app.command(
    "manual", help="Add new Azure Batch compute environment using an existing environment"
)(add_azure_manual)
azure_batch_app.command(
    "forge",
    help="Add new Azure Batch compute environment with automatic provisioning of compute resources",
)(add_azure_forge)

# Import slurm and seqera_compute modules to register their commands (use decorator)
from seqera.commands.computeenvs.platforms import seqera_compute, slurm  # noqa: F401


@app.command("delete")
def delete_compute_env(
    compute_env_id: Annotated[
        str,
        typer.Option("-i", "--id", help="Compute environment ID to delete"),
    ],
) -> None:
    """Delete a compute environment."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Delete compute environment using SDK
        sdk.compute_envs.delete(compute_env_id)

        # Output response
        result = ComputeEnvDeleted(
            compute_env_id=compute_env_id,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except NotFoundError:
        # Handle 403 as not found
        handle_compute_env_error(ComputeEnvNotFoundException(compute_env_id, USER_WORKSPACE_NAME))
    except Exception as e:
        handle_compute_env_error(e)


@app.command("list")
def list_compute_envs() -> None:
    """List all compute environments in the workspace."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Get compute environments using SDK
        envs = list(sdk.compute_envs.list())

        # Convert to dicts for response formatting (mode='json' to serialize datetimes)
        compute_envs = [ce.model_dump(by_alias=True, mode="json") for ce in envs]

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
        str | None,
        typer.Option("-i", "--id", help="Compute environment ID to view"),
    ] = None,
    compute_env_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Compute environment name to view"),
    ] = None,
) -> None:
    """View compute environment details."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        if not compute_env_id and not compute_env_name:
            output_error("Either --id or --name must be provided")
            sys.exit(1)

        # Get compute environment using SDK (supports both ID and name)
        ce_ref = compute_env_id or compute_env_name
        ce = sdk.compute_envs.get(ce_ref)

        # Convert to dict for response formatting (mode='json' to serialize datetimes)
        compute_env = {"computeEnv": ce.model_dump(by_alias=True, mode="json")}

        # Output response
        result = ComputeEnvView(
            workspace=USER_WORKSPACE_NAME,
            compute_env=compute_env,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)


@primary_app.command("get")
def primary_get() -> None:
    """Get the primary compute environment."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Get primary compute environment using SDK
        primary_ce = sdk.compute_envs.get_primary()

        if not primary_ce:
            # No primary set
            result = ComputeEnvsPrimaryGet(
                compute_env_id=None,
                name=None,
                workspace=USER_WORKSPACE_NAME,
            )
        else:
            result = ComputeEnvsPrimaryGet(
                compute_env_id=primary_ce.id,
                name=primary_ce.name,
                workspace=USER_WORKSPACE_NAME,
            )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)


@primary_app.command("set")
def primary_set(
    compute_env_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Compute environment ID to set as primary"),
    ] = None,
    compute_env_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Compute environment name to set as primary"),
    ] = None,
) -> None:
    """Set a compute environment as primary."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        if not compute_env_id and not compute_env_name:
            output_error("Either --id or --name must be provided")
            sys.exit(1)

        # Get compute environment using SDK (for name resolution and validation)
        ce_ref = compute_env_id or compute_env_name
        ce = sdk.compute_envs.get(ce_ref)

        # Set as primary using SDK
        sdk.compute_envs.set_primary(ce.id)

        # Output response
        result = ComputeEnvsPrimarySet(
            compute_env_id=ce.id,
            name=ce.name,
            workspace=USER_WORKSPACE_NAME,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)


@app.command("update")
def update_compute_env(
    compute_env_id: Annotated[
        str | None,
        typer.Option("-i", "--id", help="Compute environment ID to update"),
    ] = None,
    compute_env_name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Compute environment name to update"),
    ] = None,
    new_name: Annotated[
        str | None,
        typer.Option("--new-name", help="New name for the compute environment"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ] = None,
) -> None:
    """Update compute environment settings."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        if not compute_env_id and not compute_env_name:
            output_error("Either --id or --name must be provided")
            sys.exit(1)

        if not new_name:
            output_error("--new-name is required")
            sys.exit(1)

        # Get compute environment using SDK (for name resolution)
        ce_ref = compute_env_id or compute_env_name
        ce = sdk.compute_envs.get(ce_ref, workspace=workspace)

        # Validate new name
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        try:
            client.get(f"/compute-envs/validate?name={new_name}", params=params)
        except Exception:
            output_error(f"Compute environment name '{new_name}' is not valid")
            sys.exit(1)

        # Get workspace reference for display
        workspace_ref = USER_WORKSPACE_NAME
        if workspace:
            for ws in sdk.workspaces.list():
                if str(ws.workspace_id) == str(workspace):
                    workspace_ref = f"{ws.org_name} / {ws.workspace_name}"
                    break

        # Update the compute environment
        update_payload = {"name": new_name}
        client.put(f"/compute-envs/{ce.id}", json=update_payload, params=params)

        # Output response
        result = ComputeEnvUpdated(
            compute_env_id=ce.id,
            name=new_name,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)


@app.command("export")
def export_compute_env(
    compute_env_name: Annotated[
        str,
        typer.Option("-n", "--name", help="Compute environment name to export"),
    ],
    filename: Annotated[
        str | None,
        typer.Argument(help="Output filename (optional, defaults to stdout)"),
    ] = None,
) -> None:
    """Export compute environment configuration to JSON."""
    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Export config using SDK
        export_data = sdk.compute_envs.export_config(compute_env_name)

        # Wrap config for export format
        export_config = {"config": export_data.get("config", {})}

        # Save to file if specified
        if filename and filename != "-":
            import json

            with open(filename, "w") as f:
                json.dump(export_config, f, indent=2)

        # Output response
        result = ComputeEnvExport(config=export_config)
        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)


@app.command("import")
def import_compute_env(
    config_file: Annotated[
        str,
        typer.Argument(help="Configuration file (JSON)"),
    ],
    name: Annotated[
        str | None,
        typer.Option("-n", "--name", help="Compute environment name (overrides config)"),
    ] = None,
    workspace: Annotated[
        str | None,
        typer.Option("-w", "--workspace", help="Workspace ID"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if compute environment already exists"),
    ] = False,
) -> None:
    """Import compute environment from configuration file."""
    import json
    from pathlib import Path

    try:
        sdk = get_sdk()
        output_format = get_output_format()

        # Read configuration file
        config_path = Path(config_file)
        if not config_path.exists():
            output_error(f"Configuration file not found: {config_file}")
            sys.exit(1)

        config_text = config_path.read_text()
        try:
            config = json.loads(config_text)
        except json.JSONDecodeError as e:
            output_error(f"Failed to parse configuration file: {e}")
            sys.exit(1)

        # Extract config from the wrapper if needed
        if "config" in config:
            config = config["config"]

        # Override name if specified
        ce_name = name or config.get("name")
        if not ce_name:
            output_error(
                "Compute environment name must be specified either in config file or with --name"
            )
            sys.exit(1)

        # Handle overwrite - delete existing compute environment if it exists
        if overwrite:
            try:
                existing = sdk.compute_envs.get(ce_name, workspace=workspace)
                sdk.compute_envs.delete(existing.id, workspace=workspace)
            except Exception:
                pass  # CE doesn't exist, that's fine

        # Get workspace reference for display
        workspace_ref = "[user]"
        if workspace:
            for ws in sdk.workspaces.list():
                if str(ws.workspace_id) == str(workspace):
                    workspace_ref = f"[{ws.org_name} / {ws.workspace_name}]"
                    break

        # Create compute environment using the API directly
        client = sdk._http_client
        params = {}
        if workspace:
            params["workspaceId"] = workspace

        # Set the name in config
        config["name"] = ce_name

        # Submit the compute environment
        response = client.post("/compute-envs", json={"computeEnv": config}, params=params)
        ce_id = response.get("computeEnvId", "")

        # Get platform from config
        ce_platform = config.get("platform", "unknown")

        # Parse workspace ID
        ws_id = int(workspace) if workspace and workspace.isdigit() else None

        # Output response
        result = ComputeEnvAdded(
            platform=ce_platform,
            name=ce_name,
            compute_env_id=ce_id,
            workspace_id=ws_id,
            workspace=workspace_ref,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_compute_env_error(e)
