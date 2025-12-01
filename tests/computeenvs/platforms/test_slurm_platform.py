"""
Tests for Slurm platform compute environment commands.

Ported from SlurmPlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestSlurmPlatform:
    """Test Slurm platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding Slurm compute environment.

        Ported from testAdd() in SlurmPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=slurm-platform",
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

        httpserver.expect_request(
            "/compute-envs",
            method="POST",
            json={
                "computeEnv": {
                    "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                    "name": "slurm",
                    "platform": "slurm-platform",
                    "config": {
                        "userName": "jordi",
                        "hostName": "ssh.mydomain.net",
                        "workDir": "/home/jordeu/nf",
                        "headQueue": "normal",
                    },
                }
            },
        ).respond_with_json({"computeEnvId": "isnEDBLvHDAIteOEF44ow"}, status=200)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "slurm",
            "-n",
            "slurm",
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
            assert data["platform"] == "slurm-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "slurm"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "slurm-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "slurm"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "slurm" in out.stdout.lower()
            assert user_workspace_name in out.stdout
            assert "added" in out.stdout.lower()

    def test_add_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Slurm compute environment with advanced options.

        Ported from testAddWithAdvancedOptions() in SlurmPlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=slurm-platform",
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

        httpserver.expect_request(
            "/compute-envs",
            method="POST",
            json={
                "computeEnv": {
                    "credentialsId": "2ba2oekqeTEBzwSDgXg7xf",
                    "name": "slurm",
                    "platform": "slurm-platform",
                    "config": {
                        "userName": "jordi",
                        "hostName": "ssh.mydomain.net",
                        "maxQueueSize": 200,
                        "workDir": "/home/jordeu/nf",
                        "headQueue": "normal",
                    },
                }
            },
        ).respond_with_json({"computeEnvId": "isnEDBLvHDAIteOEF44ow"}, status=200)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "slurm",
            "-n",
            "slurm",
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
        assert "slurm" in out.stdout.lower()
        assert user_workspace_name in out.stdout
        assert "added" in out.stdout.lower()

    def test_add_with_all_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        tmp_path,
    ) -> None:
        """
        Test adding Slurm compute environment with all options including staging scripts.
        """
        # Create temporary files for staging options
        pre_run_script = tmp_path / "pre-run.sh"
        pre_run_script.write_text("#!/bin/bash\necho 'Pre-run script'")

        post_run_script = tmp_path / "post-run.sh"
        post_run_script.write_text("#!/bin/bash\necho 'Post-run script'")

        nextflow_config = tmp_path / "nextflow.config"
        nextflow_config.write_text("process.executor = 'slurm'")

        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=slurm-platform",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "2ba2oekqeTEBzwSDgXg7xf",
                        "name": "jdeu",
                        "description": None,
                        "discriminator": "ssh",
                    }
                ]
            },
            status=200,
        )

        httpserver.expect_request(
            "/compute-envs",
            method="POST",
        ).respond_with_json({"computeEnvId": "isnEDBLvHDAIteOEF44ow"}, status=200)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "slurm",
            "-n",
            "slurm-full",
            "--work-dir",
            "/home/jordeu/nf",
            "-u",
            "jordi",
            "-H",
            "ssh.mydomain.net",
            "-p",
            "22",
            "-q",
            "normal",
            "--compute-queue",
            "batch",
            "--launch-dir",
            "/home/jordeu/launch",
            "--max-queue-size",
            "200",
            "--head-job-options",
            "--mem=4G",
            "--pre-run",
            str(pre_run_script),
            "--post-run",
            str(post_run_script),
            "--nextflow-config",
            str(nextflow_config),
            "-e",
            "FOO=bar",
            "-e",
            "compute:BAZ=qux",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "slurm" in out.stdout.lower()
        assert "added" in out.stdout.lower()

    def test_add_no_credentials_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test adding Slurm compute environment when no credentials are found.
        """
        # Setup mock HTTP expectations - no credentials
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=slurm-platform",
        ).respond_with_json({"credentials": []}, status=200)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "slurm",
            "-n",
            "slurm",
            "--work-dir",
            "/home/jordeu/nf",
            "-q",
            "normal",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "No valid credentials found" in out.stderr

    def test_add_multiple_credentials_no_ref(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test adding Slurm compute environment when multiple credentials exist but none specified.
        """
        # Setup mock HTTP expectations - multiple credentials
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=slurm-platform",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "cred1",
                        "name": "ssh1",
                        "discriminator": "ssh",
                    },
                    {
                        "id": "cred2",
                        "name": "ssh2",
                        "discriminator": "ssh",
                    },
                ]
            },
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "slurm",
            "-n",
            "slurm",
            "--work-dir",
            "/home/jordeu/nf",
            "-q",
            "normal",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "Multiple credentials match" in out.stderr

    def test_add_with_credentials_ref(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Slurm compute environment with explicit credentials reference.
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/credentials",
            method="GET",
            query_string="platformId=slurm-platform",
        ).respond_with_json(
            {
                "credentials": [
                    {
                        "id": "cred1",
                        "name": "ssh1",
                        "discriminator": "ssh",
                    },
                    {
                        "id": "cred2",
                        "name": "ssh2",
                        "discriminator": "ssh",
                    },
                ]
            },
            status=200,
        )

        httpserver.expect_request(
            "/compute-envs",
            method="POST",
            json={
                "computeEnv": {
                    "credentialsId": "cred2",
                    "name": "slurm",
                    "platform": "slurm-platform",
                    "config": {
                        "workDir": "/home/jordeu/nf",
                        "headQueue": "normal",
                    },
                }
            },
        ).respond_with_json({"computeEnvId": "isnEDBLvHDAIteOEF44ow"}, status=200)

        # Run the command with explicit credentials
        out = exec_cmd(
            "compute-envs",
            "add",
            "slurm",
            "-n",
            "slurm",
            "--work-dir",
            "/home/jordeu/nf",
            "-q",
            "normal",
            "-c",
            "ssh2",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "slurm" in out.stdout.lower()
        assert "added" in out.stdout.lower()
