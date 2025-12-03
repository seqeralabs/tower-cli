"""
Tests for Azure Batch Manual platform compute environment commands.

Ported from AzBatchManualPlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestAzureBatchManualPlatform:
    """Test Azure Batch Manual platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding Azure Batch Manual compute environment.

        Ported from testAdd() in AzBatchManualPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=azure-batch",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "57Ic6reczFn78H1DTaaXkp",
                        "name": "azure",
                        "description": None,
                        "discriminator": "azure",
                        "baseUrl": None,
                        "category": None,
                        "deleted": None,
                        "lastUsed": None,
                        "dateCreated": "2021-09-07T13:50:21Z",
                        "lastUpdated": "2021-09-07T13:50:21Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "name": "azure-manual",
                "platform": "azure-batch",
                "config": {
                    "workDir": "az://nextflow-ci/jordeu",
                    "region": "europe",
                    "fusion2Enabled": False,
                    "waveEnabled": False,
                    "headPool": "seqera_pool",
                },
                "credentialsId": "57Ic6reczFn78H1DTaaXkp",
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
            "azure-batch",
            "manual",
            "-n",
            "azure-manual",
            "-l",
            "europe",
            "--work-dir",
            "az://nextflow-ci/jordeu",
            "--compute-pool-name",
            "seqera_pool",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["platform"] == "azure-batch"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "azure-manual"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "azure-batch"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "azure-manual"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "AZURE-BATCH" in out.stdout  # Platform name is uppercased in console output
            assert "azure-manual" in out.stdout
            assert user_workspace_name in out.stdout

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Azure Batch Manual compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in AzBatchManualPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=azure-batch",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "57Ic6reczFn78H1DTaaXkp",
                        "name": "azure",
                        "description": None,
                        "discriminator": "azure",
                        "baseUrl": None,
                        "category": None,
                        "deleted": None,
                        "lastUsed": None,
                        "dateCreated": "2021-09-07T13:50:21Z",
                        "lastUpdated": "2021-09-07T13:50:21Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "name": "azure-manual",
                "platform": "azure-batch",
                "config": {
                    "workDir": "az://nextflow-ci/jordeu",
                    "region": "europe",
                    "fusion2Enabled": True,
                    "waveEnabled": True,
                    "headPool": "seqera_pool",
                    "tokenDuration": "24",
                },
                "credentialsId": "57Ic6reczFn78H1DTaaXkp",
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
            "azure-batch",
            "manual",
            "-n",
            "azure-manual",
            "-l",
            "europe",
            "--work-dir",
            "az://nextflow-ci/jordeu",
            "--fusion-v2",
            "--wave",
            "--compute-pool-name",
            "seqera_pool",
            "--token-duration",
            "24",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "AZURE-BATCH" in out.stdout  # Platform name is uppercased in console output
        assert "azure-manual" in out.stdout
        assert user_workspace_name in out.stdout
