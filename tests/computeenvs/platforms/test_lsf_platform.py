"""
Tests for LSF platform compute environment commands.

Ported from LsfPlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestLsfPlatform:
    """Test LSF platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding LSF compute environment.

        Ported from testAdd() in LsfPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=lsf-platform",
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
                "name": "lsf",
                "platform": "lsf-platform",
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
            "lsf",
            "-n",
            "lsf",
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
        if out.exit_code != 0:
            print(f"\n=== STDOUT ===\n{out.stdout}\n=== STDERR ===\n{out.stderr}\n===")
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["platform"] == "lsf-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "lsf"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "lsf-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "lsf"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "LSF-PLATFORM" in out.stdout or "lsf-platform" in out.stdout
            assert "lsf" in out.stdout

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding LSF compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in LsfPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=lsf-platform",
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
                "name": "lsf",
                "platform": "lsf-platform",
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
            "lsf",
            "-n",
            "lsf",
            "--work-dir",
            "/home/jordeu/nf",
            "-u",
            "jordi",
            "-H",
            "ssh.mydomain.net",
            "-q",
            "normal",
            "--max-queue-size",
            "200",
        )

        # Assertions
        if out.exit_code != 0:
            print(f"\n=== STDOUT ===\n{out.stdout}\n=== STDERR ===\n{out.stderr}\n===")
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "LSF-PLATFORM" in out.stdout or "lsf-platform" in out.stdout
        assert "lsf" in out.stdout
