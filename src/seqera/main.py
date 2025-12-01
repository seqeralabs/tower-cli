"""
Seqera Platform CLI

Main entry point for the Seqera CLI application.
"""

import os
import sys
from typing import Annotated

import typer

from seqera.api.client import SeqeraClient
from seqera.utils.output import OutputFormat

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
        self.output_format: OutputFormat = OutputFormat.CONSOLE
        self.workspace_id: str | None = None
        self.workspace_ref: str | None = None


# Global state instance
state = GlobalState()


def get_client() -> SeqeraClient:
    """Get the global API client instance."""
    if state.client is None:
        raise RuntimeError("API client not initialized. This is a bug.")
    return state.client


def set_client(client: SeqeraClient) -> None:
    """Set the global API client instance."""
    state.client = client


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
            envvar=["SEQERA_OUTPUT_FORMAT", "TOWER_CLI_OUTPUT_FORMAT"],
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

    # Check for access token
    if not access_token:
        typer.echo(
            "Error: Missing Seqera Platform access token. "
            "Set SEQERA_ACCESS_TOKEN environment variable or use --access-token option.",
            err=True,
        )
        raise typer.Exit(1)

    # Initialize API client
    client = SeqeraClient(
        base_url=url,
        token=access_token,
        insecure=insecure,
        verbose=verbose,
    )
    set_client(client)


# Import and register subcommands
from seqera.commands import (
    actions,
    collaborators,
    computeenvs,
    credentials,
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
app.add_typer(credentials.app, name="credentials")
app.add_typer(computeenvs.app, name="compute-envs")
app.add_typer(datasets.app, name="datasets")
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
