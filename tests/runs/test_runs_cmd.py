"""
Tests for runs commands.

Ported from RunsCmdTest.java
"""

import json
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


def load_resource(name: str) -> bytes:
    """Load a test resource file."""
    resource_path = Path(__file__).parent.parent / "resources" / "runs" / f"{name}.json"
    return resource_path.read_bytes()


class TestRunsCmd:
    """Test runs commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test deleting a workflow run.

        Ported from testDelete() in RunsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/workflow/5dAZoXrcmZXRO4",
            method="DELETE",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "runs",
            "delete",
            "-i",
            "5dAZoXrcmZXRO4",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "5dAZoXrcmZXRO4"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == "5dAZoXrcmZXRO4"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "5dAZoXrcmZXRO4" in out.stdout
            assert "deleted" in out.stdout.lower()

    def test_delete_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test deleting non-existent workflow run.

        Ported from testDeleteNotFound() in RunsCmdTest.java
        """
        # Setup mock HTTP expectation - 403 returned for not found
        httpserver.expect_request(
            "/workflow/5dAZoXrcmZXRO4",
            method="DELETE",
        ).respond_with_data("", status=403)

        # Run the command
        out = exec_cmd(
            "runs",
            "delete",
            "-i",
            "5dAZoXrcmZXRO4",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "not found" in out.stderr.lower() or "403" in out.stderr

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_cancel(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test canceling a workflow run.

        Ported from testCancel() in RunsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/workflow/5dAZoXrcmZXRO4/cancel",
            method="POST",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "runs",
            "cancel",
            "-i",
            "5dAZoXrcmZXRO4",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "5dAZoXrcmZXRO4"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == "5dAZoXrcmZXRO4"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "5dAZoXrcmZXRO4" in out.stdout
            assert "cancel" in out.stdout.lower()

    def test_cancel_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test canceling non-existent workflow run.

        Ported from testCancelNotFound() in RunsCmdTest.java
        """
        # Setup mock HTTP expectation - 403 returned for not found
        httpserver.expect_request(
            "/workflow/5dAZoXrcmZXRO4/cancel",
            method="POST",
        ).respond_with_data("", status=403)

        # Run the command
        out = exec_cmd(
            "runs",
            "cancel",
            "-i",
            "5dAZoXrcmZXRO4",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "not found" in out.stderr.lower() or "403" in out.stderr

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test listing workflow runs.

        Ported from testList() in RunsCmdTest.java
        """
        # Setup mock HTTP expectations
        workflow_list = load_resource("workflow_list")

        httpserver.expect_request("/workflow", method="GET").respond_with_data(
            workflow_list, status=200, content_type="application/json"
        )

        user_response = {
            "needConsent": False,
            "user": {
                "userName": "jordi",
            },
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "runs",
            "list",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceRef"] == user_workspace_name
            assert len(data["runs"]) == 2
            assert data["runs"][0]["workflow"]["id"] == "5mDfiUtqyptDib"
            assert data["runs"][0]["workflow"]["runName"] == "spontaneous_easley"
            assert data["runs"][1]["workflow"]["id"] == "6mDfiUtqyptDib"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceRef"] == user_workspace_name
            assert len(data["runs"]) == 2
            assert data["runs"][0]["workflow"]["id"] == "5mDfiUtqyptDib"
        else:  # console
            assert user_workspace_name in out.stdout
            assert "5mDfiUtqyptDib" in out.stdout
            assert "spontaneous" in out.stdout  # May be truncated in table

    def test_list_empty(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test listing workflow runs with empty result.

        Ported from testListEmpty() in RunsCmdTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request("/workflow", method="GET").respond_with_json(
            {"workflows": []}, status=200
        )

        user_response = {
            "needConsent": False,
            "user": {
                "userName": "jordi",
            },
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        # Run the command
        out = exec_cmd("runs", "list")

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert user_workspace_name in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_view(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test viewing a workflow run.

        Ported from testView() in RunsCmdTest.java
        """
        # Setup mock HTTP expectations
        workflow_view = load_resource("workflow_view")

        httpserver.expect_request("/workflow/5mDfiUtqyptDib", method="GET").respond_with_data(
            workflow_view, status=200, content_type="application/json"
        )

        # Also mock the progress endpoint which is called separately by the SDK
        httpserver.expect_request(
            "/workflow/5mDfiUtqyptDib/progress", method="GET"
        ).respond_with_json(
            {
                "progress": {
                    "workflowProgress": {
                        "executors": None,
                    }
                }
            },
            status=200,
        )

        user_response = {
            "needConsent": False,
            "user": {
                "userName": "jordi",
            },
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "runs",
            "view",
            "-i",
            "5mDfiUtqyptDib",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceRef"] == user_workspace_name
            assert data["general"]["id"] == "5mDfiUtqyptDib"
            assert data["general"]["runName"] == "spontaneous_easley"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceRef"] == user_workspace_name
            assert data["general"]["id"] == "5mDfiUtqyptDib"
        else:  # console
            assert "5mDfiUtqyptDib" in out.stdout
            assert "spontaneous_easley" in out.stdout

    def test_view_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test viewing non-existent workflow run.

        Ported from testViewNotFound() in RunsCmdTest.java
        """
        # Setup mock HTTP expectation - 403 returned for not found
        httpserver.expect_request(
            "/workflow/5dAZoXrcmZXRO4",
            method="GET",
        ).respond_with_data("", status=403)

        # Run the command
        out = exec_cmd(
            "runs",
            "view",
            "-i",
            "5dAZoXrcmZXRO4",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "not found" in out.stderr.lower() or "403" in out.stderr

    def test_invalid_auth(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test delete with invalid authentication.

        Ported from testInvalidAuth() in RunsCmdTest.java
        """
        # Setup mock HTTP expectation - 401 for invalid auth
        httpserver.expect_request(
            "/workflow/5dAZoXrcmZXRO4",
            method="DELETE",
        ).respond_with_data("Unauthorized", status=401)

        # Run the command
        out = exec_cmd(
            "runs",
            "delete",
            "-i",
            "5dAZoXrcmZXRO4",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert (
            "401" in out.stderr
            or "Unauthorized" in out.stderr
            or "authentication" in out.stderr.lower()
        )

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_relaunch(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """Test relaunching a workflow run."""
        # Setup mock HTTP expectations
        workflow_view = load_resource("workflow_view")

        httpserver.expect_request("/workflow/5mDfiUtqyptDib", method="GET").respond_with_data(
            workflow_view, status=200, content_type="application/json"
        )

        # Mock the launch info endpoint (GET /workflow/{id}/launch)
        httpserver.expect_request(
            "/workflow/5mDfiUtqyptDib/launch",
            method="GET",
        ).respond_with_json(
            {
                "launch": {
                    "id": "5mDfiUtqyptDib",
                    "pipeline": "https://github.com/nextflow-io/hello",
                    "workDir": "s3://bucket/work",
                    "resumeCommitId": "abc123",
                    "computeEnv": {
                        "id": "ce-id-123",
                        "name": "aws-batch",
                    },
                }
            },
            status=200,
        )

        httpserver.expect_request(
            "/workflow/launch",
            method="POST",
        ).respond_with_json(
            {"workflowId": "6nEghVtrzyqEjc"},
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "runs",
            "relaunch",
            "-i",
            "5mDfiUtqyptDib",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workflowId"] == "6nEghVtrzyqEjc"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workflowId"] == "6nEghVtrzyqEjc"
        else:  # console
            assert "6nEghVtrzyqEjc" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_relaunch_no_resume(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """Test relaunching a workflow run without resume."""
        # Setup mock HTTP expectations
        workflow_view = load_resource("workflow_view")

        httpserver.expect_request("/workflow/5mDfiUtqyptDib", method="GET").respond_with_data(
            workflow_view, status=200, content_type="application/json"
        )

        # Mock the launch info endpoint (GET /workflow/{id}/launch)
        httpserver.expect_request(
            "/workflow/5mDfiUtqyptDib/launch",
            method="GET",
        ).respond_with_json(
            {
                "launch": {
                    "id": "5mDfiUtqyptDib",
                    "pipeline": "https://github.com/nextflow-io/hello",
                    "workDir": "s3://bucket/work",
                    "resumeCommitId": "abc123",
                    "computeEnv": {
                        "id": "ce-id-123",
                        "name": "aws-batch",
                    },
                }
            },
            status=200,
        )

        httpserver.expect_request(
            "/workflow/launch",
            method="POST",
        ).respond_with_json(
            {"workflowId": "7oFghWusAzrFkd"},
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "runs",
            "relaunch",
            "-i",
            "5mDfiUtqyptDib",
            "--no-resume",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workflowId"] == "7oFghWusAzrFkd"

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_task(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """Test viewing a single task."""
        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/workflow/5mDfiUtqyptDib/task/1",
            method="GET",
        ).respond_with_json(
            {
                "task": {
                    "taskId": 1,
                    "name": "NFCORE_RNASEQ:RNASEQ:FASTQ_FASTQC_UMITOOLS_TRIMGALORE:FASTQC",
                    "process": "NFCORE_RNASEQ:RNASEQ:FASTQ_FASTQC_UMITOOLS_TRIMGALORE:FASTQC",
                    "status": "COMPLETED",
                    "hash": "a1/b2c3d4",
                    "exit": 0,
                    "workdir": "s3://bucket/work/a1/b2c3d4",
                    "cpus": 2,
                    "memory": 8589934592,
                }
            },
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "runs",
            "task",
            "-i",
            "5mDfiUtqyptDib",
            "-t",
            "1",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["task"]["taskId"] == 1
            assert data["task"]["status"] == "COMPLETED"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["task"]["taskId"] == 1
            assert data["task"]["status"] == "COMPLETED"
        else:  # console
            assert "COMPLETED" in out.stdout
            assert "FASTQC" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_labels(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """Test managing labels on a workflow run."""
        # Setup mock HTTP expectations
        # Get existing labels
        httpserver.expect_request("/labels", method="GET").respond_with_json(
            {"labels": [{"id": 1, "name": "test-label", "resource": False}]},
            status=200,
        )

        # Apply labels to workflow
        httpserver.expect_request(
            "/labels/workflows/apply",
            method="POST",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "runs",
            "labels",
            "-i",
            "5mDfiUtqyptDib",
            "test-label",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["operation"] == "set"
            assert data["type"] == "workflow"
            assert data["id"] == "5mDfiUtqyptDib"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["operation"] == "set"
            assert data["type"] == "workflow"
        else:  # console
            assert "set" in out.stdout
            assert "workflow" in out.stdout
