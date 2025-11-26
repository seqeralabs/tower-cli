"""
Tests for UGE (Univa Grid Engine) platform compute environment commands.

Ported from UnivaPlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestUgePlatform:
    """Test UGE platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding UGE compute environment.

        Ported from testAdd() in UnivaPlatformTest.java
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
            query_string="platformId=uge-platform",
        ).respond_with_json(credentials_response, status=200)

        # 2. POST to create compute environment
        expected_request = {
            "computeEnv": {
                "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                "name": "uge",
                "platform": "uge-platform",
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
            "uge",
            "-n",
            "uge",
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
            assert data["platform"] == "uge-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "uge"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "uge-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "uge"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "uge-platform" in out.stdout.lower()
            assert "uge" in out.stdout

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding UGE compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in UnivaPlatformTest.java
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
            query_string="platformId=uge-platform",
        ).respond_with_json(credentials_response, status=200)

        # 2. POST to create compute environment with advanced options
        expected_request = {
            "computeEnv": {
                "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                "name": "uge",
                "platform": "uge-platform",
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
            "uge",
            "-n",
            "uge",
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
        assert "uge-platform" in out.stdout.lower() or "uge" in out.stdout
