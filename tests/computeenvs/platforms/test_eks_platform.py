"""
Tests for EKS (Elastic Kubernetes Service) platform compute environment commands.

Ported from EksPlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestEksPlatform:
    """Test EKS platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding EKS compute environment.

        Ported from testAdd() in EksPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=eks-platform",
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
                "name": "eks",
                "platform": "eks-platform",
                "config": {
                    "workDir": "/workdir",
                    "region": "europe",
                    "clusterName": "seqera",
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
            "eks",
            "-n",
            "eks",
            "--work-dir",
            "/workdir",
            "-r",
            "europe",
            "--cluster-name",
            "seqera",
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
            assert data["platform"] == "eks-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "eks"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "eks-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "eks"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "EKS-PLATFORM" in out.stdout  # Platform name is uppercased in console output
            assert "eks" in out.stdout
            assert user_workspace_name in out.stdout

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding EKS compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in EksPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=eks-platform",
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
                "name": "eks",
                "platform": "eks-platform",
                "config": {
                    "workDir": "/workdir",
                    "region": "europe",
                    "clusterName": "seqera",
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
            "eks",
            "-n",
            "eks",
            "--work-dir",
            "/workdir",
            "-r",
            "europe",
            "--cluster-name",
            "seqera",
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
        assert "EKS-PLATFORM" in out.stdout  # Platform name is uppercased in console output
        assert "eks" in out.stdout
        assert user_workspace_name in out.stdout
