"""
Tests for SSH credentials provider.

Ported from SshProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestSshProvider:
    """Test SSH credentials provider commands."""

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
        Test adding SSH credentials.

        Ported from testAdd() in SshProviderTest.java
        """
        # Create a temporary file with private key content
        key_file = temp_file("privat_key", "id_rsa", "")

        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "privateKey": "privat_key",
                        "passphrase": "my_secret",
                    },
                    "name": "ssh",
                    "provider": "ssh",
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
            "ssh",
            "-n",
            "ssh",
            "-k",
            key_file,
            "-p",
            "my_secret",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "SSH"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "ssh"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "SSH"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "ssh"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "SSH" in out.stdout
            assert "ssh" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout

    def test_invalid_private_key(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        temp_file: callable,
    ) -> None:
        """
        Test with invalid private key.

        Ported from testInvalidPrivateKey() in SshProviderTest.java
        """
        # Create a temporary file with invalid private key content
        key_file = temp_file("invalid_private_key", "id_rsa", "")

        # Setup mock HTTP expectation - 400 Bad Request
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "privateKey": "invalid_private_key",
                        "passphrase": "my_secret",
                    },
                    "name": "ssh",
                    "provider": "ssh",
                }
            },
        ).respond_with_json(
            {"message": "Unrecognised SSH private key type"},
            status=400,
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "add",
            "ssh",
            "-n",
            "ssh",
            "-k",
            key_file,
            "-p",
            "my_secret",
        )

        # Assertions
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "Unrecognised SSH private key type" in out.stderr or "400" in out.stderr
