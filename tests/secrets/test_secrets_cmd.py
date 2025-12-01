"""
Tests for secrets commands.

Port of SecretsCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer

from tests.conftest import ExecOut, exec_command


@pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
def test_list(
    httpserver: HTTPServer,
    cli_runner,
    base_args,
    output_format: str,
    user_workspace_name: str,
) -> None:
    """Test secrets list command."""
    # Mock API response
    httpserver.expect_request("/pipeline-secrets", method="GET").respond_with_json(
        {
            "pipelineSecrets": [
                {
                    "id": 5002114781502,
                    "name": "name01",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T12:42:21Z",
                    "lastUpdated": "2022-10-25T12:42:21Z",
                },
                {
                    "id": 171740984431657,
                    "name": "name02",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T13:21:15Z",
                    "lastUpdated": "2022-10-25T13:21:15Z",
                },
            ],
            "totalSize": 2,
        }
    )

    # Run command
    out = exec_command(cli_runner, base_args, ["secrets", "list"], output_format)

    # Assert success
    assert out.exit_code == 0, f"Command failed: {out.stderr}"

    # Validate output based on format
    if output_format == "json":
        output_data = json.loads(out.stdout)
        assert output_data["workspaceRef"] == user_workspace_name
        assert len(output_data["secrets"]) == 2
        assert output_data["secrets"][0]["name"] == "name01"
        assert output_data["secrets"][1]["name"] == "name02"
    elif output_format == "yaml":
        import yaml

        output_data = yaml.safe_load(out.stdout)
        assert output_data["workspaceRef"] == user_workspace_name
        assert len(output_data["secrets"]) == 2
    else:  # console
        assert "name01" in out.stdout
        assert "name02" in out.stdout
        assert user_workspace_name in out.stdout


@pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
def test_add(
    httpserver: HTTPServer,
    cli_runner,
    base_args,
    output_format: str,
    user_workspace_name: str,
) -> None:
    """Test secrets add command."""
    # Mock API response
    httpserver.expect_request(
        "/pipeline-secrets",
        method="POST",
        json={"name": "name03", "value": "value03"},
    ).respond_with_json({"secretId": 164410928765888})

    # Run command
    out = exec_command(
        cli_runner,
        base_args,
        ["secrets", "add", "-n", "name03", "-v", "value03"],
        output_format,
    )

    # Assert success
    assert out.exit_code == 0, f"Command failed: {out.stderr}"

    # Validate output based on format
    if output_format == "json":
        output_data = json.loads(out.stdout)
        assert output_data["id"] == "164410928765888"
        assert output_data["name"] == "name03"
        assert output_data["workspace"] == user_workspace_name
    elif output_format == "yaml":
        import yaml

        output_data = yaml.safe_load(out.stdout)
        assert output_data["id"] == "164410928765888"
        assert output_data["name"] == "name03"
    else:  # console
        assert "name03" in out.stdout
        assert user_workspace_name in out.stdout


@pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
def test_add_with_overwrite(
    httpserver: HTTPServer,
    cli_runner,
    base_args,
    output_format: str,
    user_workspace_name: str,
) -> None:
    """Test secrets add command with overwrite flag."""
    # Mock API - first GET to check if exists
    httpserver.expect_request("/pipeline-secrets", method="GET").respond_with_json(
        {
            "pipelineSecrets": [
                {
                    "id": 5002114781502,
                    "name": "name01",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T12:42:21Z",
                    "lastUpdated": "2022-10-25T12:42:21Z",
                },
                {
                    "id": 171740984431657,
                    "name": "name02",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T13:21:15Z",
                    "lastUpdated": "2022-10-25T13:21:15Z",
                },
                {
                    "id": 164410928765888,
                    "name": "name03",
                    "lastUsed": None,
                    "dateCreated": "2022-10-26T07:05:17Z",
                    "lastUpdated": "2022-10-26T07:05:17Z",
                },
            ],
            "totalSize": 3,
        }
    )

    # Mock DELETE request
    httpserver.expect_request(
        "/pipeline-secrets/164410928765888", method="DELETE"
    ).respond_with_data(status=204)

    # Mock POST request
    httpserver.expect_request(
        "/pipeline-secrets",
        method="POST",
        json={"name": "name03", "value": "value03"},
    ).respond_with_json({"secretId": 164410928765888})

    # Run command
    out = exec_command(
        cli_runner,
        base_args,
        ["secrets", "add", "--overwrite", "-n", "name03", "-v", "value03"],
        output_format,
    )

    # Assert success
    assert out.exit_code == 0, f"Command failed: {out.stderr}"

    # Validate output based on format
    if output_format == "json":
        output_data = json.loads(out.stdout)
        assert output_data["id"] == "164410928765888"
        assert output_data["name"] == "name03"


@pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
def test_delete(
    httpserver: HTTPServer,
    cli_runner,
    base_args,
    output_format: str,
    user_workspace_name: str,
) -> None:
    """Test secrets delete command."""
    # Mock GET to find secret by name
    httpserver.expect_request("/pipeline-secrets", method="GET").respond_with_json(
        {
            "pipelineSecrets": [
                {
                    "id": 5002114781502,
                    "name": "name01",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T12:42:21Z",
                    "lastUpdated": "2022-10-25T12:42:21Z",
                },
                {
                    "id": 171740984431657,
                    "name": "name02",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T13:21:15Z",
                    "lastUpdated": "2022-10-25T13:21:15Z",
                },
                {
                    "id": 164410928765888,
                    "name": "name03",
                    "lastUsed": None,
                    "dateCreated": "2022-10-26T07:05:17Z",
                    "lastUpdated": "2022-10-26T07:05:17Z",
                },
            ],
            "totalSize": 3,
        }
    )

    # Mock DELETE request
    httpserver.expect_request(
        "/pipeline-secrets/164410928765888", method="DELETE"
    ).respond_with_data(status=204)

    # Run command
    out = exec_command(
        cli_runner,
        base_args,
        ["secrets", "delete", "-n", "name03"],
        output_format,
    )

    # Assert success
    assert out.exit_code == 0, f"Command failed: {out.stderr}"

    # Validate output based on format
    if output_format == "json":
        output_data = json.loads(out.stdout)
        assert output_data["name"] == "name03"
        assert output_data["workspace"] == user_workspace_name
    elif output_format == "yaml":
        import yaml

        output_data = yaml.safe_load(out.stdout)
        assert output_data["name"] == "name03"
    else:  # console
        assert "name03" in out.stdout
        assert user_workspace_name in out.stdout


@pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
def test_view(
    httpserver: HTTPServer,
    cli_runner,
    base_args,
    output_format: str,
    user_workspace_name: str,
) -> None:
    """Test secrets view command."""
    # Mock GET to find secret by name
    httpserver.expect_request("/pipeline-secrets", method="GET").respond_with_json(
        {
            "pipelineSecrets": [
                {
                    "id": 5002114781502,
                    "name": "name01",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T12:42:21Z",
                    "lastUpdated": "2022-10-25T12:42:21Z",
                },
                {
                    "id": 171740984431657,
                    "name": "name02",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T13:21:15Z",
                    "lastUpdated": "2022-10-25T13:21:15Z",
                },
            ],
            "totalSize": 2,
        }
    )

    # Run command
    out = exec_command(
        cli_runner,
        base_args,
        ["secrets", "view", "-n", "name02"],
        output_format,
    )

    # Assert success
    assert out.exit_code == 0, f"Command failed: {out.stderr}"

    # Validate output based on format
    if output_format == "json":
        output_data = json.loads(out.stdout)
        assert output_data["name"] == "name02"
        assert output_data["workspace"] == user_workspace_name
        assert output_data["secret"]["id"] == 171740984431657
    elif output_format == "yaml":
        import yaml

        output_data = yaml.safe_load(out.stdout)
        assert output_data["name"] == "name02"
    else:  # console
        assert "name02" in out.stdout
        assert user_workspace_name in out.stdout


@pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
def test_update(
    httpserver: HTTPServer,
    cli_runner,
    base_args,
    output_format: str,
    user_workspace_name: str,
) -> None:
    """Test secrets update command."""
    # Mock GET to find secret by name
    httpserver.expect_request("/pipeline-secrets", method="GET").respond_with_json(
        {
            "pipelineSecrets": [
                {
                    "id": 5002114781502,
                    "name": "name01",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T12:42:21Z",
                    "lastUpdated": "2022-10-25T12:42:21Z",
                },
                {
                    "id": 171740984431657,
                    "name": "name02",
                    "lastUsed": None,
                    "dateCreated": "2022-10-25T13:21:15Z",
                    "lastUpdated": "2022-10-25T13:21:15Z",
                },
            ],
            "totalSize": 2,
        }
    )

    # Mock PUT request
    httpserver.expect_request(
        "/pipeline-secrets/171740984431657",
        method="PUT",
        json={"value": "updateValue"},
    ).respond_with_data(status=204)

    # Run command
    out = exec_command(
        cli_runner,
        base_args,
        ["secrets", "update", "-n", "name02", "-v", "updateValue"],
        output_format,
    )

    # Assert success
    assert out.exit_code == 0, f"Command failed: {out.stderr}"

    # Validate output based on format
    if output_format == "json":
        output_data = json.loads(out.stdout)
        assert output_data["name"] == "name02"
        assert output_data["workspace"] == user_workspace_name
    elif output_format == "yaml":
        import yaml

        output_data = yaml.safe_load(out.stdout)
        assert output_data["name"] == "name02"
    else:  # console
        assert "name02" in out.stdout
        assert user_workspace_name in out.stdout
