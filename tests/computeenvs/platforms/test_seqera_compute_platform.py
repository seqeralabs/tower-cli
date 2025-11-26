"""
Tests for Seqera Compute platform compute environment commands.

Ported from SeqeraComputePlatformTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestSeqeraComputePlatform:
    """Test Seqera Compute platform compute environment commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding Seqera Compute environment.

        Ported from testAdd() in SeqeraComputePlatformTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/compute-envs",
            method="POST",
            json={
                "computeEnv": {
                    "name": "my-compute-env",
                    "platform": "seqeracompute-platform",
                    "config": {
                        "region": "eu-west-1",
                    },
                }
            },
        ).respond_with_json({"computeEnvId": "isnEDBLvHDAIteOEF44ow"}, status=200)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "seqera-compute",
            "--name",
            "my-compute-env",
            "--region",
            "eu-west-1",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["platform"] == "seqeracompute-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "my-compute-env"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["platform"] == "seqeracompute-platform"
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "my-compute-env"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "seqera" in out.stdout.lower() or "my-compute-env" in out.stdout
            assert user_workspace_name in out.stdout
            assert "added" in out.stdout.lower()

    def test_add_with_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        tmp_path,
    ) -> None:
        """
        Test adding Seqera Compute environment with all options.

        Ported from testAddWithOptions() in SeqeraComputePlatformTest.java
        """
        # Create temporary files for staging options
        pre_run_script = tmp_path / "pre_run_me.sh"
        pre_run_script.write_text("pre_run_me")

        post_run_script = tmp_path / "post_run_me.sh"
        post_run_script.write_text("post_run_me")

        nextflow_config = tmp_path / "nextflow.config"
        nextflow_config.write_text("nextflow_config")

        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/compute-envs",
            method="POST",
            json={
                "computeEnv": {
                    "name": "another-compute-env",
                    "platform": "seqeracompute-platform",
                    "config": {
                        "region": "eu-west-2",
                        "preRunScript": "pre_run_me",
                        "postRunScript": "post_run_me",
                        "environment": [
                            {"name": "KEY1", "value": "value1", "head": True, "compute": False},
                            {"name": "KEY2", "value": "value2", "head": True, "compute": True},
                        ],
                        "nextflowConfig": "nextflow_config",
                    },
                }
            },
        ).respond_with_json({"computeEnvId": "isnEDBLvHDAIteOEF44ow"}, status=200)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "seqera-compute",
            "-n",
            "another-compute-env",
            "-r",
            "eu-west-2",
            "--work-dir",
            "my-work-dir",
            "--nextflow-config",
            str(nextflow_config),
            "-e",
            "head:KEY1=value1",
            "-e",
            "both:KEY2=value2",
            "--pre-run",
            str(pre_run_script),
            "--post-run",
            str(post_run_script),
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "seqera" in out.stdout.lower() or "another-compute-env" in out.stdout
        assert user_workspace_name in out.stdout
        assert "added" in out.stdout.lower()

    def test_add_minimal(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Seqera Compute environment with minimal options (no work-dir).
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/compute-envs",
            method="POST",
            json={
                "computeEnv": {
                    "name": "minimal-compute-env",
                    "platform": "seqeracompute-platform",
                    "config": {
                        "region": "us-east-1",
                    },
                }
            },
        ).respond_with_json({"computeEnvId": "minimalComputeEnvId"}, status=200)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "seqera-compute",
            "--name",
            "minimal-compute-env",
            "--region",
            "us-east-1",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "minimal-compute-env" in out.stdout
        assert "added" in out.stdout.lower()

    def test_add_with_work_dir(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Seqera Compute environment with work directory option.
        Note: workDir is accepted but not sent in the config (auto-configured by Seqera Compute).
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/compute-envs",
            method="POST",
            json={
                "computeEnv": {
                    "name": "work-dir-compute-env",
                    "platform": "seqeracompute-platform",
                    "config": {
                        "region": "us-west-2",
                    },
                }
            },
        ).respond_with_json({"computeEnvId": "workDirComputeEnvId"}, status=200)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "seqera-compute",
            "--name",
            "work-dir-compute-env",
            "--region",
            "us-west-2",
            "--work-dir",
            "/my/custom/work",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "work-dir-compute-env" in out.stdout
        assert "added" in out.stdout.lower()

    def test_add_with_environment_variables(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding Seqera Compute environment with environment variables.
        """
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/compute-envs",
            method="POST",
            json={
                "computeEnv": {
                    "name": "env-compute-env",
                    "platform": "seqeracompute-platform",
                    "config": {
                        "region": "ap-southeast-1",
                        "environment": [
                            {"name": "VAR1", "value": "val1", "head": True, "compute": False},
                            {"name": "VAR2", "value": "val2", "head": False, "compute": True},
                            {"name": "VAR3", "value": "val3", "head": True, "compute": True},
                        ],
                    },
                }
            },
        ).respond_with_json({"computeEnvId": "envComputeEnvId"}, status=200)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "add",
            "seqera-compute",
            "--name",
            "env-compute-env",
            "--region",
            "ap-southeast-1",
            "-e",
            "head:VAR1=val1",
            "-e",
            "compute:VAR2=val2",
            "-e",
            "both:VAR3=val3",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "env-compute-env" in out.stdout
        assert "added" in out.stdout.lower()
