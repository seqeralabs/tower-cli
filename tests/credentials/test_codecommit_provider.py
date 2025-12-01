"""
Tests for CodeCommit credentials provider.

Ported from CodeCommitProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer

from tests.conftest import ExecOut


class TestCodeCommitProvider:
    """Test CodeCommit credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding CodeCommit credentials.

        Ported from testAdd() in CodeCommitProviderTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "username": "<access_token>",
                        "password": "<secret_token>",
                    },
                    "name": "cc-test",
                    "provider": "codecommit",
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
            "codecommit",
            "--access-key=<access_token>",
            "--secret-key=<secret_token>",
            "--name=cc-test",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "CODECOMMIT"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "cc-test"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "CODECOMMIT"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "cc-test"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "CODECOMMIT" in out.stdout
            assert "cc-test" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_with_repo_url(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding CodeCommit credentials with base URL.

        Ported from testAddWithRepoURL() in CodeCommitProviderTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "username": "<access_token>",
                        "password": "<secret_token>",
                    },
                    "name": "cc-test",
                    "provider": "codecommit",
                    "baseUrl": "https://git-codecommit.eu-west-1.amazonaws.com",
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
            "codecommit",
            "--access-key=<access_token>",
            "--secret-key=<secret_token>",
            "--name=cc-test",
            "--base-url=https://git-codecommit.eu-west-1.amazonaws.com",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "CODECOMMIT"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "cc-test"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "CODECOMMIT"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "cc-test"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "CODECOMMIT" in out.stdout
            assert "cc-test" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout
