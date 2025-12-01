"""
Tests for TW Agent credentials provider.

Ported from AgentProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer

from tests.conftest import ExecOut


class TestAgentProvider:
    """Test TW Agent credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding TW Agent credentials.

        Ported from testAdd() in AgentProviderTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "connectionId": "connection_id",
                        "workDir": "/work",
                    },
                    "name": "agent_test",
                    "provider": "tw-agent",
                }
            },
        ).respond_with_json(
            {"credentialsId": "1cz5A8cuBkB5iJliCwJCFJ"},
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "add",
            "agent",
            "-n",
            "agent_test",
            "--connection-id",
            "connection_id",
            "--work-dir",
            "/work",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "TW_AGENT"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFJ"
            assert data["name"] == "agent_test"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "TW_AGENT"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFJ"
            assert data["name"] == "agent_test"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "TW_AGENT" in out.stdout
            assert "agent_test" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFJ" in out.stdout
