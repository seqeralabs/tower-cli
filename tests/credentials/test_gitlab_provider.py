"""
Tests for GitLab credentials provider.

Ported from GitlabProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestGitlabProvider:
    """Test GitLab credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding GitLab credentials.

        Ported from testAdd() in GitlabProviderTest.java
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
                        "token": "my_gitlab_token",
                    },
                    "name": "gitlab",
                    "provider": "gitlab",
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
            "gitlab",
            "-n",
            "gitlab",
            "-u",
            "jordi@seqera.io",
            "-p",
            "mysecret",
            "-t",
            "my_gitlab_token",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "GITLAB"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "gitlab"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "GITLAB"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "gitlab"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "GITLAB" in out.stdout
            assert "gitlab" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout
