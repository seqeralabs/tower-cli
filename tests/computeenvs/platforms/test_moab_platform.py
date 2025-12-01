"""
Tests for MOAB platform compute environment commands.

Ported from MoabPlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestMoabPlatform:
    """Test MOAB platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding MOAB compute environment.

        Ported from testAdd() in MoabPlatformTest.java
        """
        # Setup mock HTTP expectations
        # 1. GET credentials for platform
        credentials_response = {
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
        }

        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=moab-platform",
        ).respond_with_json(credentials_response, status=200)

        # 2. POST to create compute environment
        expected_request = {
            "computeEnv": {
                "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                "name": "moab",
                "platform": "moab-platform",
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
            json=expected_request,
        ).respond_with_json(
            {"computeEnvId": "isnEDBLvHDAIteOEF44ow"},
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "moab",
            "-n",
            "moab",
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
            assert data["platform"] == "moab-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "moab"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "moab-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "moab"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "MOAB" in out.stdout or "moab" in out.stdout
            assert "added" in out.stdout.lower()
            assert user_workspace_name in out.stdout

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding MOAB compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in MoabPlatformTest.java
        """
        # Setup mock HTTP expectations
        # 1. GET credentials for platform
        credentials_response = {
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
        }

        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=moab-platform",
        ).respond_with_json(credentials_response, status=200)

        # 2. POST to create compute environment with advanced options
        expected_request = {
            "computeEnv": {
                "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                "name": "moab",
                "platform": "moab-platform",
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
            json=expected_request,
        ).respond_with_json(
            {"computeEnvId": "isnEDBLvHDAIteOEF44ow"},
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "moab",
            "-n",
            "moab",
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
        assert out.exit_code == 0
        assert out.stderr == ""

        # Verify output
        assert "MOAB" in out.stdout or "moab" in out.stdout
        assert "added" in out.stdout.lower()
        assert user_workspace_name in out.stdout
