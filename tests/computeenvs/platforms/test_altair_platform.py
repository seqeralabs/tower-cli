"""
Tests for Altair PBS Pro platform compute environment commands.

Ported from AltairPlatformTest.java
"""

import json
import tempfile
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


class TestAltairPlatform:
    """Test Altair PBS Pro platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding Altair compute environment.

        Ported from testAdd() in AltairPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=altair-platform",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "2ba2oekqeTEBzwSDgXg7xf",
                        "name": "jdeu",
                        "description": None,
                        "discriminator": "ssh",
                        "baseUrl": None,
                        "category": None,
                        "deleted": None,
                        "lastUsed": "2021-09-06T08:53:51Z",
                        "dateCreated": "2021-09-06T06:54:53Z",
                        "lastUpdated": "2021-09-06T06:54:53Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                "name": "altair",
                "platform": "altair-platform",
                "config": {
                    "userName": "jordi",
                    "hostName": "ssh.mydomain.net",
                    "workDir": "/home/jordeu/nf",
                    "headQueue": "normal",
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
            "altair",
            "-n",
            "altair",
            "--work-dir",
            "/home/jordeu/nf",
            "-u",
            "jordi",
            "-H",
            "ssh.mydomain.net",
            "-q",
            "normal",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["platform"] == "altair-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "altair"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "altair-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "altair"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "altair-platform" in out.stdout.lower()
            assert "isnEDBLvHDAIteOEF44ow" in out.stdout or "altair" in out.stdout

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Altair compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in AltairPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=altair-platform",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "2ba2oekqeTEBzwSDgXg7xf",
                        "name": "jdeu",
                        "description": None,
                        "discriminator": "ssh",
                        "baseUrl": None,
                        "category": None,
                        "deleted": None,
                        "lastUsed": "2021-09-06T08:53:51Z",
                        "dateCreated": "2021-09-06T06:54:53Z",
                        "lastUpdated": "2021-09-06T06:54:53Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                "name": "altair",
                "platform": "altair-platform",
                "config": {
                    "userName": "jordi",
                    "hostName": "ssh.mydomain.net",
                    "maxQueueSize": 200,
                    "workDir": "/home/jordeu/nf",
                    "headQueue": "normal",
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
            "altair",
            "-n",
            "altair",
            "--work-dir",
            "/home/jordeu/nf",
            "-u",
            "jordi",
            "-H",
            "ssh.mydomain.net",
            "-q",
            "normal",
            "--max-queue-size=200",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "altair-platform" in out.stdout.lower()
        assert "altair" in out.stdout

    def test_add_with_env_vars(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Altair compute environment with environment variables.

        Ported from testAddWithEnvVars() in AltairPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=altair-platform",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "2ba2oekqeTEBzwSDgXg7xf",
                        "name": "jdeu",
                        "description": None,
                        "discriminator": "ssh",
                        "baseUrl": None,
                        "category": None,
                        "deleted": None,
                        "lastUsed": "2021-09-06T08:53:51Z",
                        "dateCreated": "2021-09-06T06:54:53Z",
                        "lastUpdated": "2021-09-06T06:54:53Z",
                    }
                ]
            },
            status=200,
        )

        expected_payload = {
            "computeEnv": {
                "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                "name": "altair",
                "platform": "altair-platform",
                "config": {
                    "environment": [
                        {
                            "name": "HEAD",
                            "value": "value1",
                            "head": True,
                            "compute": False,
                        },
                        {
                            "name": "COMPUTE",
                            "value": "value2",
                            "head": False,
                            "compute": True,
                        },
                        {
                            "name": "BOTH",
                            "value": "value3",
                            "head": True,
                            "compute": True,
                        },
                        {
                            "name": "HEAD",
                            "value": "value4",
                            "head": True,
                            "compute": False,
                        },
                    ],
                    "workDir": "/home/jordeu/nf",
                    "headQueue": "normal",
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
            "altair",
            "-n",
            "altair",
            "--work-dir",
            "/home/jordeu/nf",
            "-q",
            "normal",
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
        assert "altair-platform" in out.stdout.lower()
        assert "altair" in out.stdout

    def test_add_with_nextflow_config_file(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Altair compute environment with Nextflow config file.

        Ported from testAddWithNextflowConfigFile() in AltairPlatformTest.java
        """
        # Create temporary Nextflow config file
        with tempfile.NamedTemporaryFile(mode='w', suffix='.config', delete=False) as config_file:
            config_file.write('nextflow_config')
            config_path = config_file.name

        try:
            # Setup mock HTTP expectations
            httpserver.expect_request(
                "/credentials",
                method="GET",
                query_string="platformId=altair-platform",
            ).respond_with_json(
                {
                    "credentials": [
                        {
                            "id": "2ba2oekqeTEBzwSDgXg7xf",
                            "name": "jdeu",
                            "description": None,
                            "discriminator": "ssh",
                            "baseUrl": None,
                            "category": None,
                            "deleted": None,
                            "lastUsed": "2021-09-06T08:53:51Z",
                            "dateCreated": "2021-09-06T06:54:53Z",
                            "lastUpdated": "2021-09-06T06:54:53Z",
                        }
                    ]
                },
                status=200,
            )

            expected_payload = {
                "computeEnv": {
                    "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                    "name": "altair",
                    "platform": "altair-platform",
                    "config": {
                        "userName": "jordi",
                        "hostName": "ssh.mydomain.net",
                        "workDir": "/home/jordeu/nf",
                        "nextflowConfig": "nextflow_config",
                        "headQueue": "normal",
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
                "altair",
                "-n",
                "altair",
                "--work-dir",
                "/home/jordeu/nf",
                "-u",
                "jordi",
                "-H",
                "ssh.mydomain.net",
                "-q",
                "normal",
                "--nextflow-config",
                config_path,
            )

            # Assertions
            assert out.exit_code == 0
            assert out.stderr == ""
            assert "altair-platform" in out.stdout.lower()
            assert "altair" in out.stdout
        finally:
            # Clean up temp file
            Path(config_path).unlink(missing_ok=True)
