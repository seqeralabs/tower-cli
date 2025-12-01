"""
Tests for GitHub credentials provider.

Ported from GithubProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer

from tests.conftest import ExecOut


class TestGithubProvider:
    """Test GitHub credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding GitHub credentials.

        Ported from testAdd() in GithubProviderTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "username": "jordi@seqera.io",
                        "password": "mysecret",
                    },
                    "name": "github",
                    "provider": "github",
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
            "github",
            "-n",
            "github",
            "-u",
            "jordi@seqera.io",
            "-p",
            "mysecret",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "GITHUB"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "github"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "GITHUB"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "github"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "GITHUB" in out.stdout
            assert "github" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout
