"""
Tests for Google Batch platform compute environment commands.

Ported from GoogleBatchPlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestGoogleBatchPlatform:
    """Test Google Batch platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding Google Batch compute environment.

        Ported from testAdd() in GoogleBatchPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=google-batch",
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
                        "lastUsed": "2021-09-08T18:20:46Z",
                        "dateCreated": "2021-09-08T12:57:04Z",
                        "lastUpdated": "2021-09-08T12:57:04Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "name": "google",
                "platform": "google-batch",
                "config": {
                    "location": "europe",
                    "fusion2Enabled": False,
                    "waveEnabled": False,
                    "workDir": "gs://workdir",
                },
                "credentialsId": "6XfOhoztUq6de3Dw3X9LSb",
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
            "google-batch",
            "-n",
            "google",
            "--work-dir",
            "gs://workdir",
            "-l",
            "europe",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["platform"] == "google-batch"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "google"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "google-batch"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "google"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "GOOGLE-BATCH" in out.stdout  # Platform name is uppercased in console output
            assert "google" in out.stdout
            assert user_workspace_name in out.stdout

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Google Batch compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in GoogleBatchPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=google-batch",
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
                        "lastUsed": "2021-09-08T18:20:46Z",
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
                "name": "google",
                "platform": "google-batch",
                "config": {
                    "location": "europe",
                    "workDir": "gs://workdir",
                    "usePrivateAddress": True,
                    "waveEnabled": True,
                    "fusion2Enabled": True,
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
            "google-batch",
            "-n",
            "google",
            "--work-dir",
            "gs://workdir",
            "-l",
            "europe",
            "--fusion-v2",
            "--wave",
            "--use-private-address",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "GOOGLE-BATCH" in out.stdout  # Platform name is uppercased in console output
        assert "google" in out.stdout
        assert user_workspace_name in out.stdout
