"""
pytest configuration and fixtures for Seqera CLI tests.

This module provides test infrastructure similar to BaseCmdTest.java:
- HTTP server mocking via pytest-httpserver
- CLI runner for testing commands
- Common fixtures for authentication and API URLs
"""

import json
from collections.abc import Callable
from pathlib import Path
from typing import Any

import pytest
from pytest_httpserver import HTTPServer
from typer.testing import CliRunner

from seqera.main import app


@pytest.fixture
def cli_runner() -> CliRunner:
    """
    Provide a CLI runner for testing Typer commands.

    Similar to Java's exec() method in BaseCmdTest.
    """
    return CliRunner()


@pytest.fixture
def api_url(httpserver: HTTPServer) -> str:
    """
    Provide the mock API server URL.

    Similar to Java's url(MockServerClient mock) method.
    """
    return httpserver.url_for("/")


@pytest.fixture
def auth_token() -> str:
    """
    Provide a fake authentication token.

    Similar to Java's token() method.
    """
    return "fake_auth_token"


@pytest.fixture
def base_args(api_url: str, auth_token: str) -> list[str]:
    """
    Provide base CLI arguments for API URL and authentication.

    These are prepended to all CLI commands in tests.
    """
    return [
        "--insecure",
        f"--url={api_url}",
        f"--access-token={auth_token}",
    ]


class ExecOut:
    """
    Container for command execution output.

    Similar to Java's ExecOut class in BaseCmdTest.
    """

    def __init__(
        self,
        stdout: str = "",
        stderr: str = "",
        exit_code: int = 0,
    ) -> None:
        self.stdout = stdout
        self.stderr = stderr
        self.exit_code = exit_code


def exec_command(
    cli_runner: CliRunner,
    base_args: list[str],
    command_args: list[str],
    output_format: str = "console",
) -> ExecOut:
    """
    Execute a CLI command with the given arguments.

    Similar to Java's exec() method in BaseCmdTest.

    Args:
        cli_runner: The CLI runner fixture
        base_args: Base arguments (URL, token)
        command_args: Command-specific arguments
        output_format: Output format (console, json, yaml)

    Returns:
        ExecOut object containing stdout, stderr, and exit code
    """
    # Build full argument list
    args = []

    # Add output format if not console
    if output_format != "console":
        args.append(f"--output={output_format}")

    # Add base args (URL, token, insecure)
    args.extend(base_args)

    # Add command args
    args.extend(command_args)

    # Execute command
    result = cli_runner.invoke(app, args, catch_exceptions=False)

    return ExecOut(
        stdout=result.stdout,
        stderr=result.stderr if hasattr(result, "stderr") else "",
        exit_code=result.exit_code,
    )


@pytest.fixture
def exec_cmd(cli_runner: CliRunner, base_args: list[str]) -> Callable:
    """
    Provide a function to execute CLI commands with base args pre-applied.

    Similar to Java's exec() method.

    Returns:
        A function that takes command args and optional output format
    """

    def _exec(
        *command_args: str,
        output_format: str = "console",
    ) -> ExecOut:
        return exec_command(
            cli_runner,
            base_args,
            list(command_args),
            output_format,
        )

    return _exec


@pytest.fixture
def temp_file(tmp_path: Path) -> Callable:
    """
    Provide a function to create temporary files for testing.

    Similar to Java's tempFile() method.

    Returns:
        A function that creates a temp file with given content
    """

    def _create_temp_file(
        content: str,
        prefix: str = "test",
        suffix: str = ".txt",
    ) -> str:
        file_path = tmp_path / f"{prefix}{suffix}"
        file_path.write_text(content)
        return str(file_path)

    return _create_temp_file


def load_test_resource(name: str, ext: str = "json") -> bytes:
    """
    Load a test resource file.

    Similar to Java's loadResource() method.

    Args:
        name: Resource name (without extension)
        ext: File extension (default: json)

    Returns:
        File contents as bytes
    """
    resource_path = Path(__file__).parent / "resources" / f"{name}.{ext}"
    if resource_path.exists():
        return resource_path.read_bytes()
    raise FileNotFoundError(f"Test resource not found: {resource_path}")


def assert_output_format(
    exec_out: ExecOut,
    expected_data: dict[str, Any],
    output_format: str,
) -> None:
    """
    Assert that command output matches expected data for the given format.

    Similar to Java's assertOutput() method.

    Args:
        exec_out: Command execution output
        expected_data: Expected response data
        output_format: Output format used
    """
    # Check exit code
    assert (
        exec_out.exit_code == 0
    ), f"Command failed with exit code {exec_out.exit_code}: {exec_out.stderr}"

    # Check stderr is empty on success
    assert exec_out.stderr == "", f"Unexpected stderr output: {exec_out.stderr}"

    # Validate output based on format
    if output_format == "json":
        output_data = json.loads(exec_out.stdout)
        assert output_data == expected_data
    elif output_format == "yaml":
        import yaml

        output_data = yaml.safe_load(exec_out.stdout)
        assert output_data == expected_data
    # For console format, we'll check specific strings in the test


@pytest.fixture
def user_workspace_name() -> str:
    """
    Provide the default user workspace name.

    Similar to Java's USER_WORKSPACE_NAME constant.
    """
    return "user"
