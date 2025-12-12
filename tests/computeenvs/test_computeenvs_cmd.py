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
        assert (
            "401" in out.stderr
            or "Unauthorized" in out.stderr
            or "authentication" in out.stderr.lower()
        )

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

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_primary_get(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test getting primary compute environment.

        Ported from testPrimaryGet() in ComputeEnvsCmdTest.java
        """
        # Setup mock HTTP expectations
        compute_envs_response = {
            "computeEnvs": [
                {
                    "id": "isnEDBLvHDAIteOEF44ow",
                    "name": "demo",
                    "platform": "aws-batch",
                    "status": "AVAILABLE",
                    "primary": True,
                    "lastUsed": None,
                }
            ]
        }

        httpserver.expect_request("/compute-envs", method="GET").respond_with_json(
            compute_envs_response, status=200
        )

        # SDK's get_primary() calls get() which expects computeEnv wrapper
        httpserver.expect_request(
            "/compute-envs/isnEDBLvHDAIteOEF44ow", method="GET"
        ).respond_with_json(
            {
                "computeEnv": {
                    "id": "isnEDBLvHDAIteOEF44ow",
                    "name": "demo",
                    "platform": "aws-batch",
                    "status": "AVAILABLE",
                }
            },
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "primary",
            "get",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "demo"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "demo"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "isnEDBLvHDAIteOEF44ow" in out.stdout
            assert "demo" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_primary_set(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test setting primary compute environment.

        Ported from testPrimarySet() in ComputeEnvsCmdTest.java
        """
        # Setup mock HTTP expectations - SDK's get() expects computeEnv wrapper
        httpserver.expect_request(
            "/compute-envs/isnEDBLvHDAIteOEF44ow", method="GET"
        ).respond_with_json(
            {
                "computeEnv": {
                    "id": "isnEDBLvHDAIteOEF44ow",
                    "name": "demo",
                    "platform": "aws-batch",
                    "status": "AVAILABLE",
                }
            },
            status=200,
        )

        httpserver.expect_request(
            "/compute-envs/isnEDBLvHDAIteOEF44ow/primary", method="POST"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "primary",
            "set",
            "-i",
            "isnEDBLvHDAIteOEF44ow",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "demo"
            assert data["workspaceRef"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == "isnEDBLvHDAIteOEF44ow"
            assert data["name"] == "demo"
            assert data["workspaceRef"] == user_workspace_name
        else:  # console
            assert "demo" in out.stdout
            assert user_workspace_name in out.stdout


class TestComputeEnvsImportCmd:
    """Test compute-envs import command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_import(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
        tmp_path,
    ) -> None:
        """Test importing compute environment from config file."""
        import json as json_module

        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/compute-envs",
            method="POST",
        ).respond_with_json(
            {"computeEnvId": "new-ce-id-123"},
            status=200,
        )

        # Create config file - uses "config" wrapper (like export output)
        config = {
            "config": {
                "name": "imported-ce",
                "platform": "aws-batch",
                "region": "us-east-1",
                "workDir": "s3://my-bucket/work",
            }
        }

        config_file = tmp_path / "ce-config.json"
        config_file.write_text(json_module.dumps(config))

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "import",
            str(config_file),
            output_format=output_format,
        )

        # Assertions
        assert (
            out.exit_code == 0
        ), f"Command failed with exit code {out.exit_code}. stdout: {out.stdout!r}, stderr: {out.stderr!r}"
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "new-ce-id-123"
            assert data["name"] == "imported-ce"
            assert data["platform"] == "aws-batch"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == "new-ce-id-123"
            assert data["name"] == "imported-ce"
            assert data["platform"] == "aws-batch"
        else:  # console
            assert "new-ce-id-123" in out.stdout or "imported-ce" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_import_with_name_override(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
        tmp_path,
    ) -> None:
        """Test importing compute environment with name override."""
        import json as json_module

        # Setup mock HTTP expectations
        httpserver.expect_request(
            "/compute-envs",
            method="POST",
        ).respond_with_json(
            {"computeEnvId": "new-ce-id-456"},
            status=200,
        )

        # Create config file - uses "config" wrapper (like export output)
        config = {
            "config": {
                "name": "original-name",
                "platform": "aws-batch",
            }
        }

        config_file = tmp_path / "ce-config.json"
        config_file.write_text(json_module.dumps(config))

        # Run the command with name override
        out = exec_cmd(
            "compute-envs",
            "import",
            str(config_file),
            "-n",
            "overridden-name",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "new-ce-id-456"
            assert data["name"] == "overridden-name"

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_import_with_overwrite(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
        tmp_path,
    ) -> None:
        """Test importing compute environment with overwrite flag."""
        import json as json_module

        # Setup mock HTTP expectations
        # First, list to find existing CE
        httpserver.expect_request(
            "/compute-envs",
            method="GET",
        ).respond_with_json(
            {
                "computeEnvs": [
                    {
                        "id": "existing-ce-id",
                        "name": "existing-ce",
                        "platform": "aws-batch",
                        "status": "AVAILABLE",
                    }
                ]
            },
            status=200,
        )

        # Delete existing CE
        httpserver.expect_request(
            "/compute-envs/existing-ce-id",
            method="DELETE",
        ).respond_with_data("", status=204)

        # Create new CE
        httpserver.expect_request(
            "/compute-envs",
            method="POST",
        ).respond_with_json(
            {"computeEnvId": "new-ce-id-789"},
            status=200,
        )

        # Create config file - uses "config" wrapper (like export output)
        config = {
            "config": {
                "name": "existing-ce",
                "platform": "aws-batch",
            }
        }

        config_file = tmp_path / "ce-config.json"
        config_file.write_text(json_module.dumps(config))

        # Run the command with overwrite
        out = exec_cmd(
            "compute-envs",
            "import",
            str(config_file),
            "--overwrite",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "new-ce-id-789"
            assert data["name"] == "existing-ce"


class TestComputeEnvsUpdateCmd:
    """Test compute-envs update command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """Test updating compute environment by ID."""
        # Get compute env by ID
        httpserver.expect_request(
            "/compute-envs/4Ks2gtNrdv7VWyHnRaeCa4",
            method="GET",
        ).respond_with_json(
            {
                "computeEnv": {
                    "id": "4Ks2gtNrdv7VWyHnRaeCa4",
                    "name": "old-ce-name",
                    "platform": "aws-batch",
                }
            },
            status=200,
        )

        # Validate new name
        httpserver.expect_request(
            "/compute-envs/validate",
            method="GET",
        ).respond_with_data("", status=200)

        # Update compute env
        httpserver.expect_request(
            "/compute-envs/4Ks2gtNrdv7VWyHnRaeCa4",
            method="PUT",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "compute-envs",
            "update",
            "-i",
            "4Ks2gtNrdv7VWyHnRaeCa4",
            "--new-name",
            "renamed-ce",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["name"] == "renamed-ce"
            assert data["id"] == "4Ks2gtNrdv7VWyHnRaeCa4"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["name"] == "renamed-ce"
        else:  # console
            assert "updated" in out.stdout.lower()
            assert "renamed-ce" in out.stdout

    def test_update_missing_new_name(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """Test update fails without --new-name."""
        # Run the command
        out = exec_cmd(
            "compute-envs",
            "update",
            "-i",
            "4Ks2gtNrdv7VWyHnRaeCa4",
        )

        # Assertions
        assert out.exit_code == 1
        assert "--new-name" in out.stderr or "required" in out.stderr.lower()
