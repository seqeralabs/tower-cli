"""
Tests for launch command.

Tests the 'launch' command that launches pipelines in workspaces.
"""

import json
from collections.abc import Callable
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


def load_test_resource(name: str) -> dict:
    """Load a test resource JSON file."""
    resource_path = Path(__file__).parent / "resources" / "launch" / name
    with open(resource_path) as f:
        return json.load(f)


class TestLaunchCmd:
    """Test cases for the launch command."""

    def test_invalid_auth(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
    ) -> None:
        """Test launch command with invalid authentication (401)."""
        # Setup mock responses
        user_info = load_test_resource("user.json")
        httpserver.expect_request("/user-info").respond_with_json(user_info)
        httpserver.expect_request("/pipelines").respond_with_data("", status=401)

        # Execute command
        result = exec_cmd("launch", "hello")

        # Assert failure
        assert result.exit_code == 1, "Command should fail with exit code 1"
        assert "Unauthorized" in result.stderr

    def test_pipeline_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
    ) -> None:
        """Test launch command when pipeline is not found."""
        # Setup mock responses
        user_info = load_test_resource("user.json")
        pipelines_none = load_test_resource("pipelines_none.json")

        httpserver.expect_request("/user-info").respond_with_json(user_info)
        httpserver.expect_request("/pipelines").respond_with_json(pipelines_none)

        # Execute command
        result = exec_cmd("launch", "hello")

        # Assert failure
        assert result.exit_code == 1, "Command should fail with exit code 1"
        assert "Pipeline 'hello' not found" in result.stderr

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_submit_launchpad_pipeline(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
        output_format: str,
    ) -> None:
        """Test launching a launchpad pipeline."""
        # Setup mock responses
        pipelines_sarek = load_test_resource("pipelines_sarek.json")
        pipeline_launch_describe = load_test_resource("pipeline_launch_describe.json")
        workflow_launch = load_test_resource("workflow_launch.json")
        user_info = load_test_resource("user.json")

        httpserver.expect_request("/pipelines").respond_with_json(pipelines_sarek)
        httpserver.expect_request("/pipelines/250911634275687/launch").respond_with_json(
            pipeline_launch_describe
        )
        httpserver.expect_request("/workflow/launch", method="POST").respond_with_json(
            workflow_launch
        )
        httpserver.expect_request("/user-info").respond_with_json(user_info)

        # Execute command
        result = exec_cmd("launch", "sarek", output_format=output_format)

        # Assert success
        assert result.exit_code == 0, f"Command failed: {result.stderr}"

        # Verify output contains expected information
        if output_format == "json":
            output_data = json.loads(result.stdout)
            assert output_data["workflowId"] == "35aLiS0bIM5efd"
            assert output_data["workspaceRef"] == "user"
        elif output_format == "yaml":
            import yaml

            output_data = yaml.safe_load(result.stdout)
            assert output_data["workflowId"] == "35aLiS0bIM5efd"
            assert output_data["workspaceRef"] == "user"
        else:  # console
            assert "35aLiS0bIM5efd" in result.stdout
            assert "user" in result.stdout
            assert "Workflow" in result.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_submit_github_pipeline(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
        output_format: str,
    ) -> None:
        """Test launching a pipeline from GitHub URL."""
        # Setup mock responses
        compute_envs_response = {
            "computeEnvs": [
                {
                    "id": "1uJweHHZTo7gydE6pyDt7x",
                    "name": "demo",
                    "platform": "aws-batch",
                    "status": "AVAILABLE",
                    "primary": True,
                }
            ]
        }

        compute_env_details = {
            "computeEnv": {
                "id": "1uJweHHZTo7gydE6pyDt7x",
                "name": "demo",
                "platform": "aws-batch",
                "status": "AVAILABLE",
                "config": {
                    "workDir": "s3://nextflow-ci/jordeu",
                },
            }
        }

        workflow_launch = {"workflowId": "57ojrWRzTyous"}
        user_info = load_test_resource("user.json")

        httpserver.expect_request("/compute-envs").respond_with_json(compute_envs_response)
        httpserver.expect_request("/compute-envs/1uJweHHZTo7gydE6pyDt7x").respond_with_json(
            compute_env_details
        )
        httpserver.expect_request("/workflow/launch", method="POST").respond_with_json(
            workflow_launch
        )
        httpserver.expect_request("/user-info").respond_with_json(user_info)

        # Execute command
        result = exec_cmd(
            "launch", "https://github.com/nextflow-io/hello", output_format=output_format
        )

        # Assert success
        assert result.exit_code == 0, f"Command failed: {result.stderr}"

        # Verify output contains expected information
        if output_format == "json":
            output_data = json.loads(result.stdout)
            assert output_data["workflowId"] == "57ojrWRzTyous"
            assert output_data["workspaceRef"] == "user"
        elif output_format == "yaml":
            import yaml

            output_data = yaml.safe_load(result.stdout)
            assert output_data["workflowId"] == "57ojrWRzTyous"
            assert output_data["workspaceRef"] == "user"
        else:  # console
            assert "57ojrWRzTyous" in result.stdout

    def test_submit_launchpad_pipeline_with_advanced_options(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
        temp_file: Callable,
    ) -> None:
        """Test launching a pipeline with advanced options."""
        # Setup mock responses
        pipelines_sarek = load_test_resource("pipelines_sarek.json")
        pipeline_launch_describe = load_test_resource("pipeline_launch_describe.json")
        workflow_launch = load_test_resource("workflow_launch.json")
        user_info = load_test_resource("user.json")

        httpserver.expect_request("/pipelines").respond_with_json(pipelines_sarek)
        httpserver.expect_request("/pipelines/250911634275687/launch").respond_with_json(
            pipeline_launch_describe
        )
        httpserver.expect_request("/workflow/launch", method="POST").respond_with_json(
            workflow_launch
        )
        httpserver.expect_request("/user-info").respond_with_json(user_info)

        # Create temp files
        config_file = temp_file("extra_config", "nextflow", ".config")
        pre_run_file = temp_file("pre_run_me", "pre", ".sh")
        post_run_file = temp_file("post_run_me", "post", ".sh")

        # Execute command
        result = exec_cmd(
            "launch",
            "sarek",
            "--profile",
            "test,docker",
            "--revision",
            "develop",
            "--work-dir",
            "/my_work_dir",
            "--config",
            config_file,
            "--pull-latest",
            "--stub-run",
            "--pre-run",
            pre_run_file,
            "--post-run",
            post_run_file,
            "--main-script",
            "alternate.nf",
            "--entry-name",
            "dsl2",
            "--schema-name",
            "my_schema.json",
            "--disable-optimization",
        )

        # Assert success
        assert result.exit_code == 0, f"Command failed: {result.stderr}"
        assert "35aLiS0bIM5efd" in result.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_submit_launchpad_pipeline_with_custom_name(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
        output_format: str,
    ) -> None:
        """Test launching a pipeline with custom run name."""
        # Setup mock responses
        pipelines_sarek = load_test_resource("pipelines_sarek.json")
        pipeline_launch_describe = load_test_resource("pipeline_launch_describe.json")
        workflow_launch = load_test_resource("workflow_launch.json")
        user_info = load_test_resource("user.json")

        httpserver.expect_request("/pipelines").respond_with_json(pipelines_sarek)
        httpserver.expect_request("/pipelines/250911634275687/launch").respond_with_json(
            pipeline_launch_describe
        )
        httpserver.expect_request("/workflow/launch", method="POST").respond_with_json(
            workflow_launch
        )
        httpserver.expect_request("/user-info").respond_with_json(user_info)

        # Execute command
        result = exec_cmd(
            "launch", "sarek", "--name", "custom_run_name", output_format=output_format
        )

        # Assert success
        assert result.exit_code == 0, f"Command failed: {result.stderr}"

        # Verify output contains expected information
        if output_format == "json":
            output_data = json.loads(result.stdout)
            assert output_data["workflowId"] == "35aLiS0bIM5efd"
        elif output_format == "yaml":
            import yaml

            output_data = yaml.safe_load(result.stdout)
            assert output_data["workflowId"] == "35aLiS0bIM5efd"
        else:  # console
            assert "35aLiS0bIM5efd" in result.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_submit_launchpad_pipeline_with_labels(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
        output_format: str,
    ) -> None:
        """Test launching a pipeline with labels."""
        # Setup mock responses
        labels_user = load_test_resource("labels_user.json")
        new_label_response = {
            "id": 3,
            "name": "LabelThree",
            "resource": False,
            "isDefault": False,
        }

        pipelines_sarek = load_test_resource("pipelines_sarek.json")
        pipeline_launch_describe = load_test_resource("pipeline_launch_describe.json")
        workflow_launch = load_test_resource("workflow_launch.json")
        user_info = load_test_resource("user.json")

        httpserver.expect_request("/labels").respond_with_json(labels_user)
        httpserver.expect_request("/labels", method="POST").respond_with_json(new_label_response)
        httpserver.expect_request("/pipelines").respond_with_json(pipelines_sarek)
        httpserver.expect_request("/pipelines/250911634275687/launch").respond_with_json(
            pipeline_launch_describe
        )
        httpserver.expect_request("/workflow/launch", method="POST").respond_with_json(
            workflow_launch
        )
        httpserver.expect_request("/user-info").respond_with_json(user_info)

        # Execute command
        result = exec_cmd(
            "launch", "sarek", "--label", "LabelTwo,LabelThree", output_format=output_format
        )

        # Assert success
        assert result.exit_code == 0, f"Command failed: {result.stderr}"

        # Verify output contains expected information
        if output_format == "json":
            output_data = json.loads(result.stdout)
            assert output_data["workflowId"] == "35aLiS0bIM5efd"
        elif output_format == "yaml":
            import yaml

            output_data = yaml.safe_load(result.stdout)
            assert output_data["workflowId"] == "35aLiS0bIM5efd"
        else:  # console
            assert "35aLiS0bIM5efd" in result.stdout

    def test_submit_to_a_workspace(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
    ) -> None:
        """Test launching a pipeline to a specific workspace."""
        # Setup mock responses
        compute_envs_response = {
            "computeEnvs": [
                {
                    "id": "4iqCDE6C2Stq0jzBsHJvHn",
                    "name": "aws",
                    "platform": "aws-batch",
                    "status": "AVAILABLE",
                    "primary": True,
                    "workspaceName": "cli",
                    "visibility": "PRIVATE",
                }
            ]
        }

        compute_env_details = {
            "computeEnv": {
                "id": "4iqCDE6C2Stq0jzBsHJvHn",
                "name": "aws",
                "platform": "aws-batch",
                "status": "AVAILABLE",
                "config": {
                    "workDir": "s3://nextflow-ci/jordeu",
                },
            }
        }

        workflow_launch = {"workflowId": "52KAMEcqXFyhZ9"}
        user_info = load_test_resource("user.json")

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 166815615776895,
                    "name": "Seqera",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                },
                {
                    "orgId": 166815615776895,
                    "orgName": "Seqera",
                    "orgLogoUrl": None,
                    "workspaceId": 222756650686576,
                    "workspaceName": "cli",
                },
            ]
        }

        httpserver.expect_request("/compute-envs").respond_with_json(compute_envs_response)
        httpserver.expect_request("/compute-envs/4iqCDE6C2Stq0jzBsHJvHn").respond_with_json(
            compute_env_details
        )
        httpserver.expect_request("/workflow/launch", method="POST").respond_with_json(
            workflow_launch
        )
        httpserver.expect_request("/user-info").respond_with_json(user_info)
        httpserver.expect_request("/user/1264/workspaces").respond_with_json(workspaces_response)

        # Execute command
        result = exec_cmd(
            "launch",
            "https://github.com/nextflow-io/hello",
            "--workspace",
            "222756650686576",
        )

        # Assert success
        assert result.exit_code == 0, f"Command failed: {result.stderr}"
        assert "52KAMEcqXFyhZ9" in result.stdout
        assert "Seqera/cli" in result.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_submit_launchpad_pipeline_with_optimization_disabled(
        self,
        httpserver: HTTPServer,
        exec_cmd: Callable,
        output_format: str,
    ) -> None:
        """Test launching a pipeline with optimization disabled."""
        # Setup mock responses
        pipelines_sarek = load_test_resource("pipelines_sarek.json")
        pipeline_launch_describe = load_test_resource("pipeline_launch_describe.json")
        workflow_launch = load_test_resource("workflow_launch.json")
        user_info = load_test_resource("user.json")

        httpserver.expect_request("/pipelines").respond_with_json(pipelines_sarek)
        httpserver.expect_request("/pipelines/250911634275687/launch").respond_with_json(
            pipeline_launch_describe
        )
        httpserver.expect_request("/workflow/launch", method="POST").respond_with_json(
            workflow_launch
        )
        httpserver.expect_request("/user-info").respond_with_json(user_info)

        # Execute command
        result = exec_cmd(
            "launch",
            "sarek",
            "--name",
            "custom_run_name",
            "--disable-optimization",
            output_format=output_format,
        )

        # Assert success
        assert result.exit_code == 0, f"Command failed: {result.stderr}"

        # Verify output contains expected information
        if output_format == "json":
            output_data = json.loads(result.stdout)
            assert output_data["workflowId"] == "35aLiS0bIM5efd"
        elif output_format == "yaml":
            import yaml

            output_data = yaml.safe_load(result.stdout)
            assert output_data["workflowId"] == "35aLiS0bIM5efd"
        else:  # console
            assert "35aLiS0bIM5efd" in result.stdout
