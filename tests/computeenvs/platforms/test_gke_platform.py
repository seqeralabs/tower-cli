"""
Tests for GKE (Google Kubernetes Engine) platform compute environment commands.

Ported from GkePlatformTest.java
"""

import json
import tempfile
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


class TestGkePlatform:
    """Test GKE platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding GKE compute environment.

        Ported from testAdd() in GkePlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=gke-platform",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "6XfOhoztUq6de3Dw3X9LSb",
                        "name": "google",
                        "description": None,
                        "discriminator": "google",
                        "baseUrl": None,
                        "category": None,
                        "deleted": None,
                        "lastUsed": None,
                        "dateCreated": "2021-09-08T12:57:04Z",
                        "lastUpdated": "2021-09-08T12:57:04Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "credentialsId": "6XfOhoztUq6de3Dw3X9LSb",
                "name": "gke",
                "platform": "gke-platform",
                "config": {
                    "workDir": "/workdir",
                    "region": "europe",
                    "clusterName": "tower",
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
            "gke",
            "-n",
            "gke",
            "--work-dir",
            "/workdir",
            "-r",
            "europe",
            "--cluster-name",
            "tower",
            "--namespace",
            "nf",
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
            assert data["platform"] == "gke-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "gke"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "gke-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "gke"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "GKE-PLATFORM" in out.stdout  # Platform name is uppercased in console output
            assert "gke" in out.stdout
            assert user_workspace_name in out.stdout

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding GKE compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in GkePlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=gke-platform",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "6XfOhoztUq6de3Dw3X9LSb",
                        "name": "google",
                        "description": None,
                        "discriminator": "google",
                        "baseUrl": None,
                        "category": None,
                        "deleted": None,
                        "lastUsed": None,
                        "dateCreated": "2021-09-08T12:57:04Z",
                        "lastUpdated": "2021-09-08T12:57:04Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "credentialsId": "6XfOhoztUq6de3Dw3X9LSb",
                "name": "gke",
                "platform": "gke-platform",
                "config": {
                    "workDir": "/workdir",
                    "region": "europe",
                    "clusterName": "tower",
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
            "gke",
            "-n",
            "gke",
            "--work-dir",
            "/workdir",
            "-r",
            "europe",
            "--cluster-name",
            "tower",
            "--namespace",
            "nf",
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
        assert "GKE-PLATFORM" in out.stdout  # Platform name is uppercased in console output
        assert "gke" in out.stdout
        assert user_workspace_name in out.stdout
