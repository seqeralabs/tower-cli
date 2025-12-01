"""
Tests for Container Registry credentials provider.

Ported from ContainerRegistryProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer

from tests.conftest import ExecOut


class TestContainerRegistryProvider:
    """Test Container Registry credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding Container Registry credentials.

        Ported from testAdd() in ContainerRegistryProviderTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "userName": "jordeu",
                        "password": "kkdevaka",
                        "registry": "docker.io",
                    },
                    "name": "docker-reg",
                    "provider": "container-reg",
                }
            },
        ).respond_with_json(
            {"credentialsId": "5JFPt8U5J4zYcnjD7qQaiF"},
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "add",
            "container-reg",
            "-u",
            "jordeu",
            "-p",
            "kkdevaka",
            "-n",
            "docker-reg",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "CONTAINER_REG"
            assert data["id"] == "5JFPt8U5J4zYcnjD7qQaiF"
            assert data["name"] == "docker-reg"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "CONTAINER_REG"
            assert data["id"] == "5JFPt8U5J4zYcnjD7qQaiF"
            assert data["name"] == "docker-reg"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "CONTAINER_REG" in out.stdout
            assert "docker-reg" in out.stdout
            assert "5JFPt8U5J4zYcnjD7qQaiF" in out.stdout
