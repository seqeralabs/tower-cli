"""
Tests for Google Life Sciences platform compute environment commands.

Ported from GoogleLifeSciencesPlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestGoogleLifeSciencesPlatform:
    """Test Google Life Sciences platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding Google Life Sciences compute environment.

        Ported from testAdd() in GoogleLifeSciencesPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=google-lifesciences",
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
                "platform": "google-lifesciences",
                "config": {
                    "region": "europe",
                    "workDir": "gs://workdir",
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
            "google-ls",
            "-n",
            "google",
            "--work-dir",
            "gs://workdir",
            "-r",
            "europe",
            output_format=output_format,
        )

        # Assertions
        if out.exit_code != 0:
            print(f"\n=== STDOUT ===\n{out.stdout}\n=== STDERR ===\n{out.stderr}\n===")
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["platform"] == "google-lifesciences"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "google"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "google-lifesciences"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "google"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "GOOGLE-LIFESCIENCES" in out.stdout or "google-lifesciences" in out.stdout
            assert "google" in out.stdout

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Google Life Sciences compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in GoogleLifeSciencesPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=google-lifesciences",
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
                "platform": "google-lifesciences",
                "config": {
                    "region": "europe",
                    "workDir": "gs://workdir",
                    "usePrivateAddress": True,
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
            "google-ls",
            "-n",
            "google",
            "--work-dir",
            "gs://workdir",
            "-r",
            "europe",
            "--use-private-address",
        )

        # Assertions
        if out.exit_code != 0:
            print(f"\n=== STDOUT ===\n{out.stdout}\n=== STDERR ===\n{out.stderr}\n===")
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "GOOGLE-LIFESCIENCES" in out.stdout or "google-lifesciences" in out.stdout
        assert "google" in out.stdout

    def test_add_with_filestore(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Google Life Sciences compute environment with filestore.

        Ported from testAddWithFileStore() in GoogleLifeSciencesPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=google-lifesciences",
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
                "platform": "google-lifesciences",
                "config": {
                    "region": "europe",
                    "workDir": "gs://workdir",
                    "nfsTarget": "1.2.3.4:/my_share_name",
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
            "google-ls",
            "-n",
            "google",
            "--work-dir",
            "gs://workdir",
            "-r",
            "europe",
            "--nfs-target",
            "1.2.3.4:/my_share_name",
        )

        # Assertions
        if out.exit_code != 0:
            print(f"\n=== STDOUT ===\n{out.stdout}\n=== STDERR ===\n{out.stderr}\n===")
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "GOOGLE-LIFESCIENCES" in out.stdout or "google-lifesciences" in out.stdout
        assert "google" in out.stdout
