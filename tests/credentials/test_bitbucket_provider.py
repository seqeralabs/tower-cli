"""
Tests for Bitbucket credentials provider.

Ported from BitbucketProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestBitbucketProvider:
    """Test Bitbucket credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding Bitbucket credentials.

        Ported from testAdd() in BitbucketProviderTest.java
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
                    "name": "bitbucket",
                    "provider": "bitbucket",
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
            "bitbucket",
            "-n",
            "bitbucket",
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
            assert data["provider"] == "BITBUCKET"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "bitbucket"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "BITBUCKET"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "bitbucket"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "BITBUCKET" in out.stdout
            assert "bitbucket" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout
