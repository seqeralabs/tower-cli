"""
Tests for Azure credentials provider.

Ported from AzureProviderTest.java
"""

import json

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
        """
        Test adding Azure credentials.

        Ported from testAdd() in AzureProviderTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "batchName": "batchName",
                        "batchKey": "batchKey",
                        "storageName": "storageName",
                        "storageKey": "storageKey",
                    },
                    "name": "azure",
                    "provider": "azure",
                }
            },
        ).respond_with_json(
            {"credentialsId": "1cz5A8cuBkB5iJliCwJCFU"},
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "add",
            "azure",
            "--name=azure",
            "--batch-key=batchKey",
            "--batch-name=batchName",
            "--storage-key=storageKey",
            "--storage-name=storageName",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "AZURE"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "azure"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "AZURE"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "azure"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "AZURE" in out.stdout
            assert "azure" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout
