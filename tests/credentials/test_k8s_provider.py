"""
Tests for Kubernetes credentials provider.

Ported from K8sProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestK8sProvider:
    """Test Kubernetes credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_with_certificate(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        temp_file: callable,
        output_format: str,
    ) -> None:
        """
        Test adding K8s credentials with certificate.

        Ported from testAddWithCertificate() in K8sProviderTest.java
        """
        # Create temporary files with certificate and private key content
        cert_file = temp_file("my_certificate", "", ".crt")
        key_file = temp_file("my_private_key", "", ".key")

        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "certificate": "my_certificate",
                        "privateKey": "my_private_key",
                    },
                    "name": "k8s",
                    "provider": "k8s",
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
            "k8s",
            "-n",
            "k8s",
            "-k",
            key_file,
            "-c",
            cert_file,
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "K8S"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "k8s"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "K8S"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "k8s"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "K8S" in out.stdout
            assert "k8s" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_with_token(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding K8s credentials with token.

        Ported from testAddWithToken() in K8sProviderTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "token": "my_token",
                    },
                    "name": "k8s",
                    "provider": "k8s",
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
            "k8s",
            "-n",
            "k8s",
            "-t",
            "my_token",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "K8S"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "k8s"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "K8S"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "k8s"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "K8S" in out.stdout
            assert "k8s" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout
