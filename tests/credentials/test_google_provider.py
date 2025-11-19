"""
Tests for Google credentials provider.

Ported from GoogleProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer

from tests.conftest import ExecOut


class TestGoogleProvider:
    """Test Google credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        temp_file: callable,
        output_format: str,
    ) -> None:
        """
        Test adding Google credentials.

        Ported from testAdd() in GoogleProviderTest.java
        """
        # Create a temporary file with private key content
        key_file = temp_file("private_key", "id_rsa", "")

        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {"data": "private_key"},
                    "name": "google",
                    "provider": "google",
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
            "google",
            "-n",
            "google",
            "-k",
            key_file,
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "GOOGLE"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "google"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "GOOGLE"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "google"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "GOOGLE" in out.stdout
            assert "google" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout

    def test_file_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test with non-existent key file.

        Ported from testFileNotFound() in GoogleProviderTest.java
        """
        # Run the command with a non-existent file path
        out = exec_cmd(
            "credentials",
            "add",
            "google",
            "-n",
            "google",
            "-k",
            "random_path_not_found.key",
        )

        # Assertions
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "random_path_not_found.key" in out.stderr
