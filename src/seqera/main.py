"""
Seqera Platform CLI

Main entry point for the Seqera CLI application.
"""

from __future__ import annotations

import importlib.metadata
import os
import sys
from typing import TYPE_CHECKING, Annotated

import typer

from seqera.api.client import SeqeraClient
from seqera.config import SeqeraConfig, load_config
from seqera.utils.output import OutputFormat


def get_version() -> str:
    """Get the package version."""
    try:
        return importlib.metadata.version("seqera")
    except importlib.metadata.PackageNotFoundError:
        return "unknown"


def version_callback(value: bool) -> None:
    """Print version information and exit."""
    if value:
        version = get_version()
        typer.echo(f"seqera {version}")
        raise typer.Exit()


if TYPE_CHECKING:
    from seqera.sdk.client import Seqera

# Create main app
app = typer.Typer(
    name="seqera",
    help="Seqera Platform CLI - Command line interface for Seqera Platform",
    no_args_is_help=True,
    add_completion=False,
)


# Global state for API client and options
class GlobalState:
    """Global state for CLI application."""

    def __init__(self) -> None:
        self.client: SeqeraClient | None = None
        self._sdk_client: Seqera | None = None
        self.output_format: OutputFormat = OutputFormat.CONSOLE
        self.workspace_id: str | None = None
        self.workspace_ref: str | None = None
        self.config: SeqeraConfig | None = None


# Global state instance
state = GlobalState()


def get_client() -> SeqeraClient:
    """Get the global API client instance (for backwards compatibility)."""
    if state.client is None:
        raise RuntimeError("API client not initialized. This is a bug.")
    return state.client


def get_sdk() -> Seqera:
    """Get the global SDK client instance."""
    if state._sdk_client is None:
        raise RuntimeError("SDK client not initialized. This is a bug.")
    return state._sdk_client


def set_client(client: SeqeraClient) -> None:
    """Set the global API client instance."""
    state.client = client


def set_sdk(sdk: Seqera) -> None:
    """Set the global SDK client instance."""
    state._sdk_client = sdk


def get_config() -> SeqeraConfig:
    """Get the global configuration."""
    if state.config is None:
        raise RuntimeError("Configuration not loaded. This is a bug.")
    return state.config


def set_config(config: SeqeraConfig) -> None:
    """Set the global configuration."""
    state.config = config


def set_output_format(format: OutputFormat) -> None:
    """Set the global output format."""
    state.output_format = format


def get_output_format() -> OutputFormat:
    """Get the global output format."""
    return state.output_format


# Global options callback
@app.callback()
def main_callback(
    ctx: typer.Context,
    version: Annotated[
        bool | None,
        typer.Option(
            "-V",
            "--version",
            callback=version_callback,
            is_eager=True,
            help="Show version information and exit.",
        ),
    ] = None,
    access_token: Annotated[
        str | None,
        typer.Option(
            "-t",
            "--access-token",
            envvar=["SEQERA_ACCESS_TOKEN", "TOWER_ACCESS_TOKEN"],
            help="Seqera Platform access token (SEQERA_ACCESS_TOKEN or TOWER_ACCESS_TOKEN).",
        ),
    ] = None,
    url: Annotated[
        str,
        typer.Option(
            "-u",
            "--url",
            envvar=["SEQERA_API_ENDPOINT", "TOWER_API_ENDPOINT"],
            help="Seqera Platform API endpoint URL (SEQERA_API_ENDPOINT or TOWER_API_ENDPOINT).",
        ),
    ] = "https://api.cloud.seqera.io",
    output: Annotated[
        str,
        typer.Option(
            "-o",
            "--output",
            envvar=["SEQERA_OUTPUT_FORMAT", "SEQERA_CLI_OUTPUT_FORMAT"],
            help="Output format: console, json, or yaml.",
        ),
    ] = "console",
    verbose: Annotated[
        bool,
        typer.Option(
            "-v",
            "--verbose",
            help="Show HTTP request/response logs at stderr.",
        ),
    ] = False,
    insecure: Annotated[
        bool,
        typer.Option(
            "--insecure",
            help="Explicitly allow connecting to non-SSL secured server (not recommended).",
        ),
    ] = False,
) -> None:
    """
    Seqera Platform CLI.

    Interact with Seqera Platform from the command line.

    Configuration is loaded with the following precedence (highest to lowest):
    1. Command-line options and environment variables
    2. TOML config file ($XDG_CONFIG_HOME/seqera/config.toml)
    3. Nextflow auth config ($NXF_HOME/seqera-auth.config)
    """
    # Set output format
    try:
        output_format = OutputFormat(output.lower())
        set_output_format(output_format)
    except ValueError:
        typer.echo(
            f"Error: Invalid output format '{output}'. Must be one of: console, json, yaml.",
            err=True,
        )
        raise typer.Exit(1)

    # Skip client initialization for help commands
    if ctx.invoked_subcommand is None or ctx.resilient_parsing:
        return

    # Load layered configuration
    # Note: access_token and url from Typer are already resolved from env vars
    # We only pass them if they were explicitly set (not default)
    config = load_config(
        env_access_token=access_token,
        env_url=url if url != "https://api.cloud.seqera.io" else None,
    )
    set_config(config)

    # Use the final resolved access token
    final_token = config.access_token
    final_url = config.url

    # Check for access token
    if not final_token:
        typer.echo(
            "Error: Missing Seqera Platform access token. "
            "Set SEQERA_ACCESS_TOKEN environment variable, use --access-token option, "
            "or configure in $XDG_CONFIG_HOME/seqera/config.toml or $NXF_HOME/seqera-auth.config.",
            err=True,
        )
        raise typer.Exit(1)

    # Show config source in verbose mode
    if verbose:
        token_source = config.get_source("access_token") or "default"
        url_source = config.get_source("url") or "default"
        typer.echo(f"[config] access_token from: {token_source}", err=True)
        typer.echo(f"[config] url from: {url_source}", err=True)
        if config.workspace_id:
            ws_source = config.get_source("workspace_id") or "default"
            typer.echo(f"[config] workspace_id from: {ws_source}", err=True)
        if config.compute_env_id:
            ce_source = config.get_source("compute_env_id") or "default"
            typer.echo(f"[config] compute_env_id from: {ce_source}", err=True)

    # Initialize API client (for backwards compatibility during transition)
    client = SeqeraClient(
        base_url=final_url,
        token=final_token,
        insecure=insecure,
        verbose=verbose,
    )
    set_client(client)

    # Initialize SDK client
    from seqera.sdk.client import Seqera

    sdk = Seqera(
        access_token=final_token,
        url=final_url,
        insecure=insecure,
    )
    set_sdk(sdk)


# Import and register subcommands
from seqera.commands import (
    actions,
    collaborators,
    completion,
    computeenvs,
    credentials,
    datalinks,
    datasets,
    info,
    labels,
    launch,
    members,
    organizations,
    participants,
    pipelines,
    runs,
    secrets,
    studios,
    teams,
    workspaces,
)

app.add_typer(actions.app, name="actions")
app.add_typer(collaborators.app, name="collaborators")
app.add_typer(computeenvs.app, name="compute-envs")
app.add_typer(credentials.app, name="credentials")
app.add_typer(datalinks.app, name="data-links")
app.add_typer(datasets.app, name="datasets")
app.command(name="generate-completion", help="Generate shell completion script")(
    completion.generate_completion
)
app.command(name="info", help="System info and health status")(info.info)
app.add_typer(labels.app, name="labels")
app.command(name="launch", help="Launch a pipeline")(launch.launch)
app.add_typer(members.app, name="members")
app.add_typer(organizations.app, name="organizations")
app.add_typer(participants.app, name="participants")
app.add_typer(pipelines.app, name="pipelines")
app.add_typer(runs.app, name="runs")
app.add_typer(secrets.app, name="secrets")
app.add_typer(studios.app, name="studios")
app.add_typer(teams.app, name="teams")
app.add_typer(workspaces.app, name="workspaces")


def main() -> None:
    """Main entry point for the CLI."""
    try:
        app()
    except KeyboardInterrupt:
        typer.echo("\nAborted!", err=True)
        sys.exit(130)
    except Exception as e:
        # This should rarely happen as we handle exceptions in commands
        typer.echo(f"Unexpected error: {e}", err=True)
        if os.getenv("DEBUG"):
            raise
        sys.exit(1)


if __name__ == "__main__":
    main()
