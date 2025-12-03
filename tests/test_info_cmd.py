"""
Tests for info command.

Tests the 'info' command that displays system information and health status.
"""

import json
from collections.abc import Callable
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


def load_test_resource(name: str) -> dict:
    """Load a test resource JSON file."""
    resource_path = Path(__file__).parent / "resources" / name
    with open(resource_path) as f:
        return json.load(f)


class TestInfoCmd:
    """Test cases for the info command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_info(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
        output_format: str,
    ) -> None:
        """Test info command with different output formats."""
        # Setup mock responses
        service_info = load_test_resource("info/service-info.json")
        user_info = load_test_resource("user.json")

        httpserver.expect_request("/service-info").respond_with_json(service_info)
        httpserver.expect_request("/user-info").respond_with_json(user_info)

        # Execute command
        result = exec_cmd("info", output_format=output_format)

        # Assert success
        assert result.exit_code == 0, f"Command failed: {result.stderr}"

        # Verify output contains expected information
        if output_format == "json":
            output_data = json.loads(result.stdout)
            assert output_data["details"]["cliVersion"] == "0.1.0"
            assert output_data["details"]["cliApiVersion"] == "1.0.0"
            assert output_data["details"]["seqeraApiVersion"] == "1.38.0"
            assert output_data["details"]["seqeraVersion"] == "22.3.0-torricelli"
            assert output_data["details"]["userName"] == "jordi"
            assert output_data["checks"]["connectionCheck"] == 1
            assert output_data["checks"]["versionCheck"] == 1
            assert output_data["checks"]["credentialsCheck"] == 1
        elif output_format == "yaml":
            import yaml

            output_data = yaml.safe_load(result.stdout)
            assert output_data["details"]["cliVersion"] == "0.1.0"
            assert output_data["details"]["cliApiVersion"] == "1.0.0"
            assert output_data["details"]["seqeraApiVersion"] == "1.38.0"
            assert output_data["details"]["seqeraVersion"] == "22.3.0-torricelli"
            assert output_data["details"]["userName"] == "jordi"
            assert output_data["checks"]["connectionCheck"] == 1
            assert output_data["checks"]["versionCheck"] == 1
            assert output_data["checks"]["credentialsCheck"] == 1
        else:  # console
            # Check for key information in console output
            # Note: Rich adds ANSI color codes, so we check for parts of the strings
            assert "Details" in result.stdout
            assert "Seqera Platform API endpoint" in result.stdout
            assert "Seqera Platform API version" in result.stdout
            assert "Seqera version" in result.stdout
            assert "torricelli" in result.stdout
            assert "1.38" in result.stdout
            assert "jordi" in result.stdout
            assert "System health status" in result.stdout
            assert "OK" in result.stdout

    def test_info_token_fail(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
    ) -> None:
        """Test info command when authentication fails (401)."""
        # Setup mock responses
        service_info = load_test_resource("info/service-info.json")

        httpserver.expect_request("/service-info").respond_with_json(service_info)
        httpserver.expect_request("/user-info").respond_with_data("", status=401)

        # Execute command
        result = exec_cmd("info")

        # Assert failure with exit code 1
        assert result.exit_code == 1, "Command should fail with exit code 1"

        # Verify output contains expected information
        # Note: Rich adds ANSI color codes, so we check for parts of the strings
        assert "Details" in result.stdout
        assert "torricelli" in result.stdout
        assert "1.38" in result.stdout
        assert "System health status" in result.stdout
        assert "FAILED" in result.stdout
        assert "credential" in result.stdout

    def test_info_version_fail(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
    ) -> None:
        """Test info command when Seqera Platform API version is too old."""
        # Setup mock responses with obsolete version
        service_info = load_test_resource("info/service-info-obsolete.json")
        user_info = load_test_resource("user.json")

        httpserver.expect_request("/service-info").respond_with_json(service_info)
        httpserver.expect_request("/user-info").respond_with_json(user_info)

        # Execute command
        result = exec_cmd("info")

        # Assert failure with exit code 1
        assert result.exit_code == 1, "Command should fail with exit code 1"

        # Verify output contains expected information
        # Note: Rich adds ANSI color codes, so we check for parts of the strings
        assert "Details" in result.stdout
        assert "21.10" in result.stdout  # Old version
        assert "0.1" in result.stdout  # Old API version
        assert "jordi" in result.stdout
        assert "System health status" in result.stdout
        assert "FAILED" in result.stdout
        assert "version check" in result.stdout

    def test_info_url_fail(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
    ) -> None:
        """Test info command when API URL is not available."""
        # Setup mock to return non-JSON response (simulating connection failure)
        httpserver.expect_request("/service-info").respond_with_data(
            "<html>Not Found</html>", status=404, content_type="text/html"
        )

        # Execute command
        result = exec_cmd("info")

        # Assert failure with exit code 1
        assert result.exit_code == 1, "Command should fail with exit code 1"

        # Verify output contains expected information
        assert "Details" in result.stdout
        assert "System health status" in result.stdout
        assert "FAILED" in result.stdout
        assert "Remote API server connection check" in result.stdout
