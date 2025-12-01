"""
Tests for K8s (Kubernetes) platform compute environment commands.

Ported from K8sPlatformTest.java
"""

import json
import tempfile
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


class TestK8sPlatform:
    """Test K8s platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding K8s compute environment.

        Ported from testAdd() in K8sPlatformTest.java
        """
        # Create temporary SSL certificate file
        with tempfile.NamedTemporaryFile(mode="w", suffix=".crt", delete=False) as ssl_file:
            ssl_file.write("ssl_cert")
            ssl_cert_path = ssl_file.name

        try:
            # Setup mock HTTP expectations
            httpserver.expect_request(
                "/credentials",
                method="GET",
                query_string="platformId=k8s-platform",
            ).respond_with_json(
                {
                    "credentials": [
                        {
                            "id": "2iEjbUUbqbuOaQEx03OxyH",
                            "name": "k8s",
                            "description": None,
                            "discriminator": "k8s",
                            "baseUrl": None,
                            "category": None,
                            "deleted": None,
                            "lastUsed": None,
                            "dateCreated": "2021-09-08T13:03:27Z",
                            "lastUpdated": "2021-09-08T13:03:27Z",
                        }
                    ]
                },
                status=200,
            )

            expected_payload = {
                "computeEnv": {
                    "credentialsId": "2iEjbUUbqbuOaQEx03OxyH",
                    "name": "k8s",
                    "platform": "k8s-platform",
                    "config": {
                        "workDir": "/workdir",
                        "server": "k8s.mydomain.net",
                        "sslCert": "ssl_cert",
                        "namespace": "nf",
                        "headServiceAccount": "head",
                        "storageClaimName": "nf",
                    },
                }
            }

            httpserver.expect_request(
                "/compute-envs",
                method="POST",
                json=expected_payload,
            ).respond_with_json(
                {"computeEnvId": "isnEDBLvHDAIteOEF44ow"},
                status=200,
            )

            # Run the command
            out = exec_cmd(
                "compute-envs",
                "add",
                "k8s",
                "-n",
                "k8s",
                "--work-dir",
                "/workdir",
                "-s",
                "k8s.mydomain.net",
                "--namespace",
                "nf",
                "--ssl-cert",
                ssl_cert_path,
                "--head-account",
                "head",
                "--storage-claim",
                "nf",
                output_format=output_format,
            )

            # Assertions
            assert out.exit_code == 0
            assert out.stderr == ""

            if output_format == "json":
                data = json.loads(out.stdout)
                assert data["platform"] == "k8s-platform"
                assert data["id"] == "isnEDBLvHDAIteOEF44ow"
                assert data["name"] == "k8s"
                assert data["workspaceRef"] == user_workspace_name
            elif output_format == "yaml":
                import yaml

                data = yaml.safe_load(out.stdout)
                assert data["platform"] == "k8s-platform"
                assert data["id"] == "isnEDBLvHDAIteOEF44ow"
                assert data["name"] == "k8s"
                assert data["workspaceRef"] == user_workspace_name
            else:  # console
                assert "K8S-PLATFORM" in out.stdout  # Platform name is uppercased in console output
                assert "k8s" in out.stdout
                assert user_workspace_name in out.stdout
        finally:
            # Clean up temp file
            Path(ssl_cert_path).unlink(missing_ok=True)

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding K8s compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in K8sPlatformTest.java
        """
        # Create temporary SSL certificate file
        with tempfile.NamedTemporaryFile(mode="w", suffix=".crt", delete=False) as ssl_file:
            ssl_file.write("ssl_cert")
            ssl_cert_path = ssl_file.name

        try:
            # Setup mock HTTP expectations
            httpserver.expect_request(
                "/credentials",
                method="GET",
                query_string="platformId=k8s-platform",
            ).respond_with_json(
                {
                    "credentials": [
                        {
                            "id": "2iEjbUUbqbuOaQEx03OxyH",
                            "name": "k8s",
                            "description": None,
                            "discriminator": "k8s",
                            "baseUrl": None,
                            "category": None,
                            "deleted": None,
                            "lastUsed": None,
                            "dateCreated": "2021-09-08T13:03:27Z",
                            "lastUpdated": "2021-09-08T13:03:27Z",
                        }
                    ]
                },
                status=200,
            )

            expected_payload = {
                "computeEnv": {
                    "credentialsId": "2iEjbUUbqbuOaQEx03OxyH",
                    "name": "k8s",
                    "platform": "k8s-platform",
                    "config": {
                        "workDir": "/workdir",
                        "server": "k8s.mydomain.net",
                        "sslCert": "ssl_cert",
                        "namespace": "nf",
                        "headServiceAccount": "head",
                        "storageClaimName": "nf",
                        "storageMountPath": "/workdir",
                    },
                }
            }

            httpserver.expect_request(
                "/compute-envs",
                method="POST",
                json=expected_payload,
            ).respond_with_json(
                {"computeEnvId": "isnEDBLvHDAIteOEF44ow"},
                status=200,
            )

            # Run the command
            out = exec_cmd(
                "compute-envs",
                "add",
                "k8s",
                "-n",
                "k8s",
                "--work-dir",
                "/workdir",
                "-s",
                "k8s.mydomain.net",
                "--namespace",
                "nf",
                "--ssl-cert",
                ssl_cert_path,
                "--head-account",
                "head",
                "--storage-claim",
                "nf",
                "--storage-mount",
                "/workdir",
            )

            # Assertions
            assert out.exit_code == 0
            assert out.stderr == ""
            assert "K8S-PLATFORM" in out.stdout  # Platform name is uppercased in console output
            assert "k8s" in out.stdout
            assert user_workspace_name in out.stdout
        finally:
            # Clean up temp file
            Path(ssl_cert_path).unlink(missing_ok=True)

    def test_add_with_staging(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding K8s compute environment with staging (pre/post run scripts).

        Ported from testAddWithStaging() in K8sPlatformTest.java
        """
        # Create temporary files
        with tempfile.NamedTemporaryFile(mode="w", suffix=".crt", delete=False) as ssl_file:
            ssl_file.write("ssl_cert")
            ssl_cert_path = ssl_file.name

        with tempfile.NamedTemporaryFile(
            mode="w", prefix="pre", suffix=".sh", delete=False
        ) as pre_file:
            pre_file.write("pre_run_me")
            pre_run_path = pre_file.name

        with tempfile.NamedTemporaryFile(
            mode="w", prefix="post", suffix=".sh", delete=False
        ) as post_file:
            post_file.write("post_run_me")
            post_run_path = post_file.name

        try:
            # Setup mock HTTP expectations
            httpserver.expect_request(
                "/credentials",
                method="GET",
                query_string="platformId=k8s-platform",
            ).respond_with_json(
                {
                    "credentials": [
                        {
                            "id": "2iEjbUUbqbuOaQEx03OxyH",
                            "name": "k8s",
                            "description": None,
                            "discriminator": "k8s",
                            "baseUrl": None,
                            "category": None,
                            "deleted": None,
                            "lastUsed": None,
                            "dateCreated": "2021-09-08T13:03:27Z",
                            "lastUpdated": "2021-09-08T13:03:27Z",
                        }
                    ]
                },
                status=200,
            )

            expected_payload = {
                "computeEnv": {
                    "credentialsId": "2iEjbUUbqbuOaQEx03OxyH",
                    "name": "k8s",
                    "platform": "k8s-platform",
                    "config": {
                        "workDir": "/workdir",
                        "preRunScript": "pre_run_me",
                        "postRunScript": "post_run_me",
                        "server": "k8s.mydomain.net",
                        "sslCert": "ssl_cert",
                        "namespace": "nf",
                        "headServiceAccount": "head",
                        "storageClaimName": "nf",
                    },
                }
            }

            httpserver.expect_request(
                "/compute-envs",
                method="POST",
                json=expected_payload,
            ).respond_with_json(
                {"computeEnvId": "isnEDBLvHDAIteOEF44ow"},
                status=200,
            )

            # Run the command
            out = exec_cmd(
                "compute-envs",
                "add",
                "k8s",
                "-n",
                "k8s",
                "--work-dir",
                "/workdir",
                "-s",
                "k8s.mydomain.net",
                "--namespace",
                "nf",
                "--ssl-cert",
                ssl_cert_path,
                "--head-account",
                "head",
                "--storage-claim",
                "nf",
                "--pre-run",
                pre_run_path,
                "--post-run",
                post_run_path,
            )

            # Assertions
            assert out.exit_code == 0
            assert out.stderr == ""
            assert "K8S-PLATFORM" in out.stdout  # Platform name is uppercased in console output
            assert "k8s" in out.stdout
            assert user_workspace_name in out.stdout
        finally:
            # Clean up temp files
            Path(ssl_cert_path).unlink(missing_ok=True)
            Path(pre_run_path).unlink(missing_ok=True)
            Path(post_run_path).unlink(missing_ok=True)
