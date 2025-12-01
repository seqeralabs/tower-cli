# Development Guide

Guide for developing the Seqera Platform CLI.

## Quick Start

### Installation

```bash
# Install in development mode with all dependencies
pip install -e ".[dev]"
```

### Running Tests

```bash
# Run all tests
pytest

# Run specific test file
pytest tests/credentials/test_credentials_cmd.py

# Run with verbose output
pytest -v

# Run with coverage
pytest --cov=seqera --cov-report=html
```

### Running the CLI

```bash
# Run the CLI directly
python -m seqera.main --help

# Or if installed
seqera --help

# Example: List credentials
seqera credentials list

# Example with custom server
seqera --url http://localhost:8080 --access-token your_token credentials list
```

## Project Structure

```text
tower-cli/
├── src/seqera/              # Python source code
│   ├── main.py              # CLI entry point
│   ├── api/                 # API client
│   │   └── client.py        # SeqeraClient class
│   ├── commands/            # CLI commands
│   │   ├── actions/         # Actions commands
│   │   ├── computeenvs/     # Compute environment commands
│   │   ├── credentials/     # Credentials commands
│   │   ├── datalinks/       # Data links commands
│   │   ├── datasets/        # Datasets commands
│   │   ├── labels/          # Labels commands
│   │   ├── members/         # Members commands
│   │   ├── organizations/   # Organizations commands
│   │   ├── participants/    # Participants commands
│   │   ├── pipelines/       # Pipeline commands
│   │   ├── runs/            # Runs commands
│   │   ├── secrets/         # Secrets commands
│   │   ├── studios/         # Data studios commands
│   │   ├── teams/           # Teams commands
│   │   ├── workspaces/      # Workspaces commands
│   │   ├── info.py          # Info command
│   │   └── launch.py        # Launch command
│   ├── exceptions/          # Custom exceptions
│   ├── responses/           # Response models
│   └── utils/               # Utilities (output formatting, etc.)
└── tests/                   # Tests
    ├── conftest.py          # Pytest fixtures
    └── ...                  # Command-specific tests
```

## Code Quality

### Linting

```bash
# Check code style
ruff check src/ tests/

# Fix auto-fixable issues
ruff check --fix src/ tests/
```

### Formatting

```bash
# Format code
ruff format src/ tests/
```

### Type Checking

```bash
# Run mypy
mypy src/seqera
```

### Pre-commit Hooks

Install pre-commit hooks to run checks automatically:

```bash
pre-commit install
```

## Testing

### Test Structure

Tests are organized by command:

```text
tests/
├── conftest.py              # Common fixtures
├── test_info_cmd.py         # Info command tests
├── test_launch_cmd.py       # Launch command tests
├── actions/                 # Actions tests
├── credentials/             # Credentials tests
└── ...
```

### Writing Tests

Tests use pytest and pytest-httpserver for mocking API responses:

```python
import pytest
from pytest_httpserver import HTTPServer
from tests.conftest import exec_command


def test_list_credentials(
    httpserver: HTTPServer,
    cli_runner,
    base_args,
) -> None:
    """Test credentials list command."""
    # Mock API response
    httpserver.expect_request(
        "/credentials",
        method="GET",
    ).respond_with_json({
        "credentials": [
            {"id": "1", "name": "test-cred"}
        ]
    })

    # Run command
    out = exec_command(
        cli_runner,
        base_args,
        ["credentials", "list"],
        output_format="json"
    )

    # Assert
    assert out.exit_code == 0
    assert "test-cred" in out.stdout
```

### Test Fixtures

Common fixtures in `conftest.py`:

- `cli_runner` - Typer CLI test runner
- `base_args` - Base CLI arguments (--url, --access-token, --insecure)
- `api_url` - Mock server URL
- `auth_token` - Test authentication token
- `user_workspace_name` - Test user workspace name

### Running Specific Tests

```bash
# Run tests for a specific command
pytest tests/credentials/ -v

# Run tests matching a pattern
pytest -k "test_list" -v

# Run with output capture disabled (for debugging)
pytest -s tests/test_info_cmd.py
```

## Adding New Commands

### 1. Create Command Module

```python
# src/seqera/commands/mycommand/__init__.py
import typer

app = typer.Typer(name="mycommand", help="My command description")


@app.command("list")
def list_items(
    workspace: str | None = typer.Option(None, "-w", "--workspace"),
) -> None:
    """List items."""
    # Implementation
    pass
```

### 2. Register Command

```python
# src/seqera/main.py
from seqera.commands import mycommand

app.add_typer(mycommand.app, name="mycommand")
```

### 3. Add Tests

```python
# tests/mycommand/test_mycommand_cmd.py
def test_list(httpserver, cli_runner, base_args):
    httpserver.expect_request("/my-endpoint").respond_with_json({...})
    out = exec_command(cli_runner, base_args, ["mycommand", "list"])
    assert out.exit_code == 0
```

## Dependencies

### Runtime Dependencies

- `typer` - CLI framework
- `httpx` - HTTP client
- `rich` - Terminal formatting
- `pydantic` - Data validation
- `pyyaml` - YAML support

### Development Dependencies

- `pytest` - Testing framework
- `pytest-httpserver` - HTTP mocking
- `ruff` - Linter and formatter
- `mypy` - Type checker
- `pre-commit` - Git hooks

## Release Process

1. Update version in `pyproject.toml`
2. Run tests: `pytest`
3. Run quality checks: `ruff check && mypy src/seqera`
4. Create release commit and tag
5. Push to trigger CI/CD

## Troubleshooting

### Import Errors

If you get import errors, ensure the package is installed in development mode:

```bash
pip install -e .
```

### Test Discovery Issues

Ensure test files and functions follow naming conventions:
- Files: `test_*.py`
- Functions: `test_*`
- Classes: `Test*`

### HTTP Mock Issues

If tests fail with unexpected requests, check the mock server logs for the actual requests being made vs. what was expected.
