"""
Tests for compute environment core commands.

Ported from ComputeEnvsCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestComputeEnvsCmd:
    """Test compute environment core commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test deleting compute environment.

        Ported from testDelete() in ComputeEnvsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/compute-envs/vYOK4vn7spw7bHHWBDXZ2",
            method="DELETE",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "delete",
            "-i",
            "vYOK4vn7spw7bHHWBDXZ2",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "vYOK4vn7spw7bHHWBDXZ2"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == "vYOK4vn7spw7bHHWBDXZ2"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "vYOK4vn7spw7bHHWBDXZ2" in out.stdout
            assert "deleted" in out.stdout.lower()

    def test_delete_invalid_auth(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test delete with invalid authentication.

        Ported from testDeleteInvalidAuth() in ComputeEnvsCmdTest.java
        """
        # Setup mock HTTP expectation - 401 for invalid auth
        httpserver.expect_request(
            "/compute-envs/vYOK4vn7spw7bHHWBDXZ8",
            method="DELETE",
        ).respond_with_data("Unauthorized", status=401)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "delete",
            "-i",
            "vYOK4vn7spw7bHHWBDXZ8",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "401" in out.stderr or "Unauthorized" in out.stderr or "authentication" in out.stderr.lower()

    def test_delete_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test deleting non-existent compute environment.

        Ported from testDeleteNotFound() in ComputeEnvsCmdTest.java
        """
        # Setup mock HTTP expectation - 403 returned for not found
        httpserver.expect_request(
            "/compute-envs/vYOK4vn7spw7bHHWBDXZ9",
            method="DELETE",
        ).respond_with_data("", status=403)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "delete",
            "-i",
            "vYOK4vn7spw7bHHWBDXZ9",
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
        Test listing compute environments.

        Ported from testList() in ComputeEnvsCmdTest.java
        """
        # Setup mock HTTP expectations
        compute_envs_response = {
            "computeEnvs": [
                {
                    "id": "vYOK4vn7spw7bHHWBDXZ2",
                    "name": "aws-batch",
                    "platform": "aws-batch",
                    "status": "AVAILABLE",
                    "primary": True,
                    "lastUsed": "2021-09-06T08:53:51Z",
                    "dateCreated": "2021-09-06T06:54:53Z",
                    "lastUpdated": "2021-09-06T06:54:53Z",
                },
                {
                    "id": "2ba2oekqeTEBzwSDgXg7xf",
                    "name": "gke-compute",
                    "platform": "gke",
                    "status": "AVAILABLE",
                    "primary": False,
                    "lastUsed": None,
                    "dateCreated": "2021-09-07T13:50:21Z",
                    "lastUpdated": "2021-09-07T13:50:21Z",
                },
            ]
        }

        httpserver.expect_request("/compute-envs", method="GET").respond_with_json(
            compute_envs_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "list",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceRef"] == user_workspace_name
            assert len(data["computeEnvs"]) == 2
            assert data["computeEnvs"][0]["id"] == "vYOK4vn7spw7bHHWBDXZ2"
            assert data["computeEnvs"][0]["name"] == "aws-batch"
            assert data["computeEnvs"][0]["platform"] == "aws-batch"
            assert data["computeEnvs"][1]["id"] == "2ba2oekqeTEBzwSDgXg7xf"
            assert data["computeEnvs"][1]["name"] == "gke-compute"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceRef"] == user_workspace_name
            assert len(data["computeEnvs"]) == 2
            assert data["computeEnvs"][0]["id"] == "vYOK4vn7spw7bHHWBDXZ2"
            assert data["computeEnvs"][0]["name"] == "aws-batch"
        else:  # console
            assert user_workspace_name in out.stdout
            assert "aws-batch" in out.stdout
            assert "gke-compute" in out.stdout
            # IDs may be truncated in table display, so check for partial match
            assert "vYOK4vn7spw7" in out.stdout or "vYOK4vn7spw7bHHWBDXZ2" in out.stdout
            assert "2ba2oekqeT" in out.stdout or "2ba2oekqeTEBzwSDgXg7xf" in out.stdout

    def test_list_empty(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test listing compute environments with empty result.

        Ported from testListEmpty() in ComputeEnvsCmdTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request("/compute-envs", method="GET").respond_with_json(
            {"computeEnvs": []}, status=200
        )

        # Run the command
        out = exec_cmd("compute-envs", "list")

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert user_workspace_name in out.stdout
        assert "No compute environment found" in out.stdout
