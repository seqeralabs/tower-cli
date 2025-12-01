"""
Output formatting utilities for the Seqera CLI.

Handles formatting of command responses in different formats:
- console: Human-readable table format
- json: JSON format
- yaml: YAML format
"""

import json
from enum import Enum
from typing import Any

import yaml
from rich.console import Console
from rich.table import Table

console = Console()


class OutputFormat(str, Enum):
    """Output format options."""

    CONSOLE = "console"
    JSON = "json"
    YAML = "yaml"


def output_json(data: Any) -> None:
    """
    Output data in JSON format.

    Args:
        data: Data to output
    """
    json_str = json.dumps(data, indent=2)
    print(json_str)


def output_yaml(data: Any) -> None:
    """
    Output data in YAML format.

    Args:
        data: Data to output
    """
    yaml_str = yaml.dump(data, default_flow_style=False, sort_keys=False)
    print(yaml_str)


def output_console(message: str) -> None:
    """
    Output message in console format.

    Args:
        message: Message to output
    """
    console.print(message)


def output_table(
    headers: list[str],
    rows: list[list[Any]],
    title: str | None = None,
) -> None:
    """
    Output data as a table in console format.

    Args:
        headers: Column headers
        rows: Row data
        title: Optional table title
    """
    table = Table(title=title, show_header=True, header_style="bold magenta")

    for header in headers:
        table.add_column(header)

    for row in rows:
        table.add_row(*[str(cell) for cell in row])

    console.print(table)


def output_error(message: str) -> None:
    """
    Output error message to stderr.

    Args:
        message: Error message
    """
    error_console = Console(stderr=True)
    error_console.print(f"[bold red]Error:[/bold red] {message}")


def format_response(data: Any, format: OutputFormat) -> None:
    """
    Format and output response data based on output format.

    Args:
        data: Response data to output
        format: Output format
    """
    if format == OutputFormat.JSON:
        output_json(data)
    elif format == OutputFormat.YAML:
        output_yaml(data)
    else:
        # For console format, the command should handle formatting
        # This is a fallback for simple cases
        if isinstance(data, dict):
            for key, value in data.items():
                console.print(f"{key}: {value}")
        elif isinstance(data, list):
            for item in data:
                console.print(item)
        else:
            console.print(data)
