"""
Tests for AWS Batch Forge platform compute environment commands.

Ported from AwsBatchForgePlatformTest.java
"""

import json
import tempfile
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


class TestAwsBatchForgePlatform:
    """Test AWS Batch Forge platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding AWS Batch Forge compute environment.

        Ported from testAdd() in AwsBatchForgePlatformTest.java
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
                "name": "demo",
                "platform": "aws-batch",
                "config": {
                    "region": "eu-west-1",
                    "fusion2Enabled": True,
                    "waveEnabled": True,
                    "workDir": "s3://nextflow-ci/jordeu",
                    "forge": {
                        "type": "SPOT",
                        "minCpus": 0,
                        "maxCpus": 123,
                        "gpuEnabled": False,
                        "ebsAutoScale": True,
                        "disposeOnDeletion": True,
                        "fargateHeadEnabled": False,
                    },
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
            "forge",
            "-n",
            "demo",
            "-r",
            "eu-west-1",
            "--work-dir",
            "s3://nextflow-ci/jordeu",
            "--max-cpus",
            "123",
            "--fusion-v2",
            "--wave",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["platform"] == "aws-batch"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "demo"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "aws-batch"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "demo"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "AWS-BATCH" in out.stdout  # Platform name is uppercased in console output
            assert "demo" in out.stdout
            assert user_workspace_name in out.stdout

    def test_add_with_efs(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding AWS Batch Forge compute environment with EFS.

        Ported from testAddWithEFS() in AwsBatchForgePlatformTest.java
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
                "name": "demo",
                "platform": "aws-batch",
                "config": {
                    "region": "eu-west-1",
                    "fusion2Enabled": False,
                    "waveEnabled": False,
                    "workDir": "s3://nextflow-ci/jordeu",
                    "forge": {
                        "type": "SPOT",
                        "minCpus": 0,
                        "maxCpus": 123,
                        "gpuEnabled": False,
                        "ebsAutoScale": True,
                        "disposeOnDeletion": True,
                        "efsCreate": True,
                        "fargateHeadEnabled": False,
                    },
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
            "forge",
            "-n",
            "demo",
            "-r",
            "eu-west-1",
            "--work-dir",
            "s3://nextflow-ci/jordeu",
            "--max-cpus",
            "123",
            "--create-efs",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "AWS-BATCH" in out.stdout
        assert "demo" in out.stdout
        assert user_workspace_name in out.stdout

    def test_add_with_fsx(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding AWS Batch Forge compute environment with FSX.

        Ported from testAddWithFSX() in AwsBatchForgePlatformTest.java
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
                "name": "demo",
                "platform": "aws-batch",
                "config": {
                    "region": "eu-west-1",
                    "fusion2Enabled": False,
                    "waveEnabled": False,
                    "workDir": "/workdir",
                    "forge": {
                        "type": "SPOT",
                        "minCpus": 0,
                        "maxCpus": 123,
                        "gpuEnabled": False,
                        "ebsAutoScale": True,
                        "fsxSize": 1200,
                        "disposeOnDeletion": True,
                        "fargateHeadEnabled": False,
                    },
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
            "forge",
            "-n",
            "demo",
            "-r",
            "eu-west-1",
            "--work-dir",
            "/workdir",
            "--max-cpus",
            "123",
            "--fsx-size",
            "1200",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "AWS-BATCH" in out.stdout
        assert "demo" in out.stdout
        assert user_workspace_name in out.stdout

    def test_add_with_advanced(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding AWS Batch Forge compute environment with advanced options.

        Ported from testAddWithAdvanced() in AwsBatchForgePlatformTest.java
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
                "name": "demo",
                "platform": "aws-batch",
                "config": {
                    "region": "eu-west-1",
                    "fusion2Enabled": True,
                    "waveEnabled": True,
                    "nvnmeStorageEnabled": True,
                    "cliPath": "/bin/aws",
                    "workDir": "s3://nextflow-ci/jordeu",
                    "forge": {
                        "type": "SPOT",
                        "minCpus": 8,
                        "maxCpus": 123,
                        "gpuEnabled": False,
                        "ebsAutoScale": True,
                        "disposeOnDeletion": True,
                        "allowBuckets": ["bkt1", "bkt2"],
                        "fargateHeadEnabled": False,
                    },
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
            "forge",
            "-n",
            "demo",
            "-r",
            "eu-west-1",
            "--work-dir",
            "s3://nextflow-ci/jordeu",
            "--max-cpus",
            "123",
            "--fusion-v2",
            "--wave",
            "--fast-storage",
            "--cli-path",
            "/bin/aws",
            "--min-cpus",
            "8",
            "--allow-buckets",
            "bkt1,bkt2",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "AWS-BATCH" in out.stdout
        assert "demo" in out.stdout
        assert user_workspace_name in out.stdout

    def test_add_with_env_vars(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding AWS Batch Forge compute environment with environment variables.

        Ported from testAddWithEnvVars() in AwsBatchForgePlatformTest.java
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
                "name": "demo",
                "platform": "aws-batch",
                "config": {
                    "region": "eu-west-1",
                    "fusion2Enabled": False,
                    "waveEnabled": False,
                    "workDir": "s3://nextflow-ci/jordeu",
                    "environment": [
                        {"name": "HEAD", "value": "value1", "head": True, "compute": False},
                        {"name": "COMPUTE", "value": "value2", "head": False, "compute": True},
                        {"name": "BOTH", "value": "value3", "head": True, "compute": True},
                        {"name": "HEAD", "value": "value4", "head": True, "compute": False},
                    ],
                    "forge": {
                        "type": "SPOT",
                        "minCpus": 0,
                        "maxCpus": 123,
                        "gpuEnabled": False,
                        "ebsAutoScale": True,
                        "disposeOnDeletion": True,
                        "fargateHeadEnabled": False,
                    },
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
            "forge",
            "-n",
            "demo",
            "-r",
            "eu-west-1",
            "--work-dir",
            "s3://nextflow-ci/jordeu",
            "--max-cpus",
            "123",
            "-e",
            "HEAD=value1",
            "-e",
            "compute:COMPUTE=value2",
            "-e",
            "both:BOTH=value3",
            "-e",
            "head:HEAD=value4",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "AWS-BATCH" in out.stdout
        assert "demo" in out.stdout
        assert user_workspace_name in out.stdout

    def test_add_with_deprecated_fusion(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test that deprecated --fusion flag raises error.

        Ported from testAddWithDeprecated() in AwsBatchForgePlatformTest.java
        """
        # Setup mock HTTP expectations for credentials
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

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "aws-batch",
            "forge",
            "-n",
            "demo",
            "-r",
            "eu-west-1",
            "--work-dir",
            "s3://nextflow-ci/jordeu",
            "--max-cpus",
            "123",
            "--fusion",
        )

        # Assertions
        assert out.exit_code == 1
        assert "Fusion v1 is deprecated, please use '--fusion-v2' instead" in out.stderr
        assert out.stdout == ""

    def test_add_with_nextflow_config(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding AWS Batch Forge compute environment with nextflow config.

        Ported from testAddWithNextflowConfig() in AwsBatchForgePlatformTest.java
        """
        # Create temporary nextflow config file
        with tempfile.NamedTemporaryFile(mode="w", suffix=".config", delete=False) as config_file:
            config_file.write("nextflow_config")
            config_path = config_file.name

        try:
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
                    "name": "demo",
                    "platform": "aws-batch",
                    "config": {
                        "region": "eu-west-1",
                        "fusion2Enabled": True,
                        "waveEnabled": True,
                        "workDir": "s3://nextflow-ci/jordeu",
                        "nextflowConfig": "nextflow_config",
                        "forge": {
                            "type": "SPOT",
                            "minCpus": 0,
                            "maxCpus": 123,
                            "gpuEnabled": False,
                            "ebsAutoScale": True,
                            "disposeOnDeletion": True,
                            "fargateHeadEnabled": False,
                        },
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
                "forge",
                "-n",
                "demo",
                "-r",
                "eu-west-1",
                "--work-dir",
                "s3://nextflow-ci/jordeu",
                "--max-cpus",
                "123",
                "--fusion-v2",
                "--wave",
                "--nextflow-config",
                config_path,
            )

            # Assertions
            assert out.exit_code == 0
            assert out.stderr == ""
            assert "AWS-BATCH" in out.stdout
            assert "demo" in out.stdout
            assert user_workspace_name in out.stdout
        finally:
            # Clean up temp file
            Path(config_path).unlink(missing_ok=True)
