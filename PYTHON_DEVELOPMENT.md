# Python Development Guide

Guide for developing the Python version of tower-cli.

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
pytest tests/credentials/test_aws_provider.py

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
tw --help

# Example: Add AWS credentials (with mock server in tests)
tw --url http://localhost:8080 --access-token fake_token credentials add aws --name test -a key -s secret
```

## Project Structure

```
tower-cli/
├── src/seqera/              # Python source code
│   ├── main.py              # CLI entry point
│   ├── api/                 # API client
│   │   └── client.py        # TowerClient class
│   ├── commands/            # CLI commands
│   │   ├── credentials/     # Credentials commands
│   │   ├── computeenvs/     # Compute environment commands
│   │   ├── pipelines/       # Pipeline commands
│   │   └── ...
│   ├── exceptions/          # Custom exceptions
│   ├── responses/           # Response models
│   └── utils/               # Utilities (output formatting, etc.)
└── tests/                   # Tests
    ├── conftest.py          # Pytest fixtures
    ├── credentials/         # Credentials tests
    └── ...
```

## Development Workflow

### Test-Driven Development

Follow this workflow for each new feature:

1. **Read Java Test** - Find the corresponding Java test file
   ```bash
   # Example: For AWS credentials
   cat src/test/java/io/seqera/tower/cli/credentials/providers/AwsProviderTest.java
   ```

2. **Port Test to Python** - Create the Python test
   ```python
   # tests/credentials/test_azure_provider.py
   def test_add_azure_credentials(httpserver, exec_cmd):
       httpserver.expect_request(...).respond_with_json(...)
       out = exec_cmd("credentials", "add", "azure", ...)
       assert out.exit_code == 0
   ```

3. **Run Test (Should Fail)** - Verify test fails
   ```bash
   pytest tests/credentials/test_azure_provider.py -v
   # Should fail with NotImplementedError or similar
   ```

4. **Implement Feature** - Write the code
   ```python
   # src/seqera/commands/credentials/__init__.py
   @add_app.command("azure")
   def add_azure(...):
       # Implementation
   ```

5. **Run Test (Should Pass)** - Verify test passes
   ```bash
   pytest tests/credentials/test_azure_provider.py -v
   # Should pass ✅
   ```

6. **Refactor** - Improve code quality

7. **Commit** - Commit your changes
   ```bash
   git add .
   git commit -m "Add Azure credentials provider"
   ```

## Code Style

### Type Hints

Use type hints on all functions:

```python
from typing import Optional, Dict, Any

def create_credentials(
    name: str,
    provider: str,
    keys: Dict[str, Any],
) -> Dict[str, Any]:
    """Create new credentials."""
    pass
```

### Docstrings

Use Google-style docstrings:

```python
def add_credentials(name: str, provider: str) -> Dict[str, Any]:
    """
    Add new credentials to workspace.

    Args:
        name: Credentials name
        provider: Provider type (aws, azure, etc.)

    Returns:
        Dictionary containing credentials ID and metadata

    Raises:
        AuthenticationError: If authentication fails
        ValidationError: If inputs are invalid
    """
    pass
```

### Formatting

Use black and ruff for formatting:

```bash
# Format code
black src/ tests/

# Lint code
ruff check src/ tests/

# Type check
mypy src/
```

## Testing

### Test Structure

Follow this structure for tests:

```python
import pytest
from pytest_httpserver import HTTPServer

class TestAzureProvider:
    """Test Azure credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """Test adding Azure credentials."""
        # Setup mock
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={"credentials": {...}},
        ).respond_with_json(
            {"credentialsId": "abc123"},
            status=200,
        )

        # Run command
        out = exec_cmd(
            "credentials", "add", "azure",
            "--name", "test",
            "--key", "value",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "abc123"
```

### Common Fixtures

Available in `tests/conftest.py`:

- `cli_runner` - CLI runner for testing
- `httpserver` - Mock HTTP server
- `api_url` - Mock API URL
- `auth_token` - Fake auth token
- `base_args` - Common CLI args
- `exec_cmd` - Execute CLI commands
- `user_workspace_name` - Default workspace name

## Common Patterns

### Adding a New Credential Provider

1. Port test from Java:
   ```python
   # tests/credentials/test_xyz_provider.py
   class TestXyzProvider:
       def test_add(self, httpserver, exec_cmd):
           # ...
   ```

2. Add command:
   ```python
   # src/seqera/commands/credentials/__init__.py
   @add_app.command("xyz")
   def add_xyz(
       name: str = typer.Option(..., "-n", "--name"),
       some_key: str = typer.Option(..., "-k", "--key"),
   ):
       client = get_client()
       payload = {
           "credentials": {
               "name": name,
               "provider": "xyz",
               "keys": {"someKey": some_key},
           }
       }
       response = client.post("/credentials", json=payload)
       # Output response...
   ```

3. Add update command similarly

### Error Handling

Use custom exceptions:

```python
from seqera.exceptions import (
    AuthenticationError,
    NotFoundError,
    ValidationError,
)

try:
    response = client.get(f"/credentials/{cred_id}")
except NotFoundError:
    raise CredentialsNotFoundException(cred_id, workspace)
```

### Output Formatting

Use response models:

```python
from seqera.responses import CredentialsAdded
from seqera.utils.output import output_json, output_yaml, output_console

result = CredentialsAdded(
    provider="AWS",
    credentials_id="abc123",
    name="my-creds",
    workspace="user",
)

if output_format == OutputFormat.JSON:
    output_json(result.to_dict())
elif output_format == OutputFormat.YAML:
    output_yaml(result.to_dict())
else:
    output_console(result.to_console())
```

## Debugging

### Enable Verbose Mode

```bash
# See HTTP requests/responses
tw --verbose credentials add aws --name test ...
```

### Debug in Tests

```bash
# Run with print statements visible
pytest -v -s tests/credentials/test_aws_provider.py

# Drop into debugger on failure
pytest --pdb tests/credentials/test_aws_provider.py
```

### Check Request/Response

In the API client, set `verbose=True` to see all HTTP traffic:

```python
client = TowerClient(
    base_url="http://localhost:8080",
    token="fake",
    verbose=True,  # Enables HTTP logging
)
```

## Performance Tips

1. **Parallel Tests** - pytest runs tests in parallel by default
2. **Mock HTTP** - Use httpserver, don't make real API calls in tests
3. **Incremental Testing** - Run only the tests you're working on

## Troubleshooting

### Import Errors

```bash
# Reinstall in development mode
pip install -e ".[dev]"
```

### Test Failures

```bash
# Run single test with verbose output
pytest -v -s tests/credentials/test_aws_provider.py::TestAwsProvider::test_add

# See full diff
pytest -vv tests/credentials/test_aws_provider.py
```

### Type Checking Issues

```bash
# Run mypy
mypy src/seqera/

# Ignore specific errors
# Add "# type: ignore" comment
```

## Resources

- **Typer Documentation**: https://typer.tiangolo.com/
- **httpx Documentation**: https://www.python-httpx.org/
- **pytest Documentation**: https://docs.pytest.org/
- **pytest-httpserver**: https://pytest-httpserver.readthedocs.io/
- **Rich Documentation**: https://rich.readthedocs.io/

---

**Happy Coding!** 🚀
