"""
Tests for AWS Batch Manual platform compute environment commands.

Ported from AwsBatchManualPlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestAwsBatchManualPlatform:
    """Test AWS Batch Manual platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding AWS Batch Manual compute environment.

        Ported from testAdd() in AwsBatchManualPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=aws-batch",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "6g0ER59L4ZoE5zpOmUP48D",
                        "name": "aws",
                        "description": None,
                        "discriminator": "aws",
                        "baseUrl": None,
                        "category": None,
                        "deleted": None,
                        "lastUsed": "2021-09-08T11:09:58Z",
                        "dateCreated": "2021-09-08T05:48:51Z",
                        "lastUpdated": "2021-09-08T05:48:51Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "name": "manual",
                "platform": "aws-batch",
                "config": {
                    "region": "eu-west-1",
                    "fusion2Enabled": False,
                    "waveEnabled": False,
                    "computeQueue": "TowerForge-isnEDBLvHDAIteOEF44ow-work",
                    "headQueue": "TowerForge-isnEDBLvHDAIteOEF44ow-head",
                    "workDir": "s3://nextflow-ci/jordeu",
                },
                "credentialsId": "6g0ER59L4ZoE5zpOmUP48D",
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
            "aws-batch",
            "manual",
            "-n",
            "manual",
            "-r",
            "eu-west-1",
            "--work-dir",
            "s3://nextflow-ci/jordeu",
            "--head-queue",
            "TowerForge-isnEDBLvHDAIteOEF44ow-head",
            "--compute-queue",
            "TowerForge-isnEDBLvHDAIteOEF44ow-work",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["platform"] == "aws-batch"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "manual"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "aws-batch"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "manual"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "AWS-BATCH" in out.stdout  # Platform name is uppercased in console output
            assert "manual" in out.stdout
            assert user_workspace_name in out.stdout

    def test_add_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding AWS Batch Manual compute environment with advanced options.

        Ported from testAddAdvanceOptions() in AwsBatchManualPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=aws-batch",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "6g0ER59L4ZoE5zpOmUP48D",
                        "name": "aws",
                        "description": None,
                        "discriminator": "aws",
                        "baseUrl": None,
                        "category": None,
                        "deleted": None,
                        "lastUsed": "2021-09-08T11:09:58Z",
                        "dateCreated": "2021-09-08T05:48:51Z",
                        "lastUpdated": "2021-09-08T05:48:51Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "name": "manual",
                "platform": "aws-batch",
                "config": {
                    "region": "eu-west-1",
                    "fusion2Enabled": True,
                    "waveEnabled": True,
                    "nvnmeStorageEnabled": True,
                    "computeQueue": "TowerForge-isnEDBLvHDAIteOEF44ow-work",
                    "executionRole": "execution-arn",
                    "headQueue": "TowerForge-isnEDBLvHDAIteOEF44ow-head",
                    "cliPath": "/bin/aws",
                    "workDir": "s3://nextflow-ci/jordeu",
                },
                "credentialsId": "6g0ER59L4ZoE5zpOmUP48D",
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
            "aws-batch",
            "manual",
            "-n",
            "manual",
            "-r",
            "eu-west-1",
            "--work-dir",
            "s3://nextflow-ci/jordeu",
            "--fusion-v2",
            "--wave",
            "--fast-storage",
            "--head-queue",
            "TowerForge-isnEDBLvHDAIteOEF44ow-head",
            "--compute-queue",
            "TowerForge-isnEDBLvHDAIteOEF44ow-work",
            "--cli-path",
            "/bin/aws",
            "--batch-execution-role",
            "execution-arn",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "AWS-BATCH" in out.stdout  # Platform name is uppercased in console output
        assert "manual" in out.stdout
        assert user_workspace_name in out.stdout
