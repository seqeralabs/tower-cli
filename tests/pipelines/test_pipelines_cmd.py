"""
Tests for pipelines commands.

Ported from PipelinesCmdTest.java
"""

import json
import tempfile
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


class TestPipelinesListCmd:
    """Test pipelines list command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test listing pipelines.

        Ported from testList() in PipelinesCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/pipelines",
            method="GET",
        ).respond_with_json(
            {
                "pipelines": [
                    {
                        "pipelineId": 183522618315672,
                        "name": "sleep_one_minute",
                        "description": None,
                        "icon": None,
                        "repository": "https://github.com/pditommaso/nf-sleep",
                        "userId": 4,
                        "userName": "jordi",
                        "userFirstName": None,
                        "userLastName": None,
                        "orgId": None,
                        "orgName": None,
                        "workspaceId": None,
                        "workspaceName": None,
                        "visibility": None,
                    }
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("pipelines", "list", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceRef"] == f"[{user_workspace_name}]"
            assert len(data["pipelines"]) == 1
            assert data["pipelines"][0]["name"] == "sleep_one_minute"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceRef"] == f"[{user_workspace_name}]"
            assert len(data["pipelines"]) == 1
            assert data["pipelines"][0]["name"] == "sleep_one_minute"
        else:  # console
            assert "sleep_one_minute" in out.stdout
            assert "Pipelines" in out.stdout

    def test_list_empty(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing pipelines when there are none.

        Ported from testListEmpty() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/pipelines",
            method="GET",
        ).respond_with_json({"pipelines": [], "totalSize": 0})

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("pipelines", "list")

        # Assertions
        assert out.exit_code == 0
        assert "No pipelines found" in out.stdout

    def test_list_with_pagination(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test listing pipelines with pagination.

        Ported from testListWithOffset() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        httpserver.expect_request(
            "/pipelines",
            method="GET",
            query_string="offset=1&max=2",
        ).respond_with_json(
            {
                "pipelines": [
                    {
                        "pipelineId": 183522618315672,
                        "name": "sleep_one_minute",
                        "repository": "https://github.com/pditommaso/nf-sleep",
                        "userId": 4,
                        "userName": "jordi",
                    }
                ],
                "totalSize": 10,
            }
        )

        # Run the command
        out = exec_cmd("pipelines", "list", "--offset", "1", "--max", "2")

        # Assertions
        assert out.exit_code == 0
        assert "sleep_one_minute" in out.stdout


class TestPipelinesViewCmd:
    """Test pipelines view command."""

    def test_view_by_name(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test viewing pipeline by name.

        Ported from testView() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/pipelines",
            method="GET",
        ).respond_with_json(
            {
                "pipelines": [
                    {
                        "pipelineId": 213164477645856,
                        "name": "sleep_one_minute",
                        "repository": "https://github.com/pditommaso/nf-sleep",
                    }
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/pipelines/213164477645856/launch",
            method="GET",
        ).respond_with_json(
            {
                "launch": {
                    "id": "aB5VzZ5MGKnnAh6xsiKAV",
                    "pipeline": "https://github.com/pditommaso/nf-sleep",
                    "workDir": "$TW_AGENT_WORK",
                    "paramsText": "timeout: 60\n\n",
                    "computeEnv": {
                        "id": "509cXW9NmIKYTe7KbjxyZn",
                        "name": "slurm_vallibierna",
                    },
                }
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("pipelines", "view", "-n", "sleep_one_minute")

        # Assertions
        assert out.exit_code == 0
        assert "sleep_one_minute" in out.stdout
        assert "slurm_vallibierna" in out.stdout


class TestPipelinesAddCmd:
    """Test pipelines add command."""

    def test_add_with_params(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding pipeline with parameters.

        Ported from testAdd() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/compute-envs",
            method="GET",
            query_string="status=AVAILABLE",
        ).respond_with_json(
            {
                "computeEnvs": [
                    {
                        "id": "vYOK4vn7spw7bHHWBDXZ2",
                        "name": "demo",
                        "platform": "aws-batch",
                        "status": "AVAILABLE",
                        "primary": True,
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/compute-envs/vYOK4vn7spw7bHHWBDXZ2",
            method="GET",
        ).respond_with_json(
            {
                "computeEnv": {
                    "id": "vYOK4vn7spw7bHHWBDXZ2",
                    "name": "demo",
                    "config": {
                        "workDir": "s3://nextflow-ci/jordeu",
                    },
                }
            }
        )

        httpserver.expect_request(
            "/pipelines",
            method="POST",
        ).respond_with_json(
            {
                "pipeline": {
                    "pipelineId": 18388134856008,
                    "name": "sleep_one_minute",
                }
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Create temp params file
        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".yml", delete=False
        ) as f:
            f.write("timeout: 60\n")
            params_file = f.name

        try:
            # Run the command
            out = exec_cmd(
                "pipelines",
                "add",
                "-n",
                "sleep_one_minute",
                "--params-file",
                params_file,
                "https://github.com/pditommaso/nf-sleep",
            )

            # Assertions
            assert out.exit_code == 0
            assert "sleep_one_minute" in out.stdout
            assert "added" in out.stdout.lower()
        finally:
            Path(params_file).unlink()

    def test_add_with_compute_env(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test adding pipeline with specific compute environment.

        Ported from testAddWithComputeEnv() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/compute-envs",
            method="GET",
            query_string="status=AVAILABLE",
        ).respond_with_json(
            {
                "computeEnvs": [
                    {
                        "id": "vYOK4vn7spw7bHHWBDXZ2",
                        "name": "demo",
                        "platform": "aws-batch",
                        "status": "AVAILABLE",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/compute-envs/vYOK4vn7spw7bHHWBDXZ2",
            method="GET",
        ).respond_with_json(
            {
                "computeEnv": {
                    "id": "vYOK4vn7spw7bHHWBDXZ2",
                    "name": "demo",
                    "config": {
                        "workDir": "s3://nextflow-ci/jordeu",
                    },
                }
            }
        )

        httpserver.expect_request(
            "/pipelines",
            method="POST",
        ).respond_with_json(
            {
                "pipeline": {
                    "pipelineId": 18388134856008,
                    "name": "demo",
                }
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "pipelines",
            "add",
            "-n",
            "demo",
            "-c",
            "demo",
            "https://github.com/pditommaso/nf-sleep",
        )

        # Assertions
        assert out.exit_code == 0
        assert "demo" in out.stdout
        assert "added" in out.stdout.lower()

    def test_add_missing_compute_environment(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test adding pipeline when no compute environment is available.

        Ported from testMissingComputeEnvironment() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/compute-envs",
            method="GET",
            query_string="status=AVAILABLE",
        ).respond_with_json({"computeEnvs": []})

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "pipelines",
            "add",
            "-n",
            "sleep_one_minute",
            "https://github.com/pditommaso/nf-sleep",
        )

        # Assertions
        assert out.exit_code == 1
        assert "No compute environment available" in out.stderr


class TestPipelinesDeleteCmd:
    """Test pipelines delete command."""

    def test_delete_by_name(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test deleting pipeline by name.

        Ported from testDelete() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/pipelines",
            method="GET",
        ).respond_with_json(
            {
                "pipelines": [
                    {
                        "pipelineId": 183522618315672,
                        "name": "sleep",
                        "repository": "https://github.com/pditommaso/nf-sleep",
                    }
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/pipelines/183522618315672",
            method="DELETE",
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("pipelines", "delete", "-n", "sleep")

        # Assertions
        assert out.exit_code == 0
        assert "sleep" in out.stdout
        assert "deleted" in out.stdout.lower()

    def test_delete_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test deleting non-existent pipeline.

        Ported from testDeleteNotFound() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/pipelines",
            method="GET",
        ).respond_with_json({"pipelines": [], "totalSize": 0})

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("pipelines", "delete", "-n", "sleep_all")

        # Assertions
        assert out.exit_code == 1
        assert "not found" in out.stderr.lower()

    def test_delete_multiple_match(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test deleting when multiple pipelines match.

        Ported from testDeleteMultipleMatch() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/pipelines",
            method="GET",
        ).respond_with_json(
            {
                "pipelines": [
                    {"pipelineId": 1, "name": "hello1"},
                    {"pipelineId": 2, "name": "hello2"},
                ],
                "totalSize": 2,
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("pipelines", "delete", "-n", "hello")

        # Assertions
        assert out.exit_code == 1
        assert "Multiple pipelines found" in out.stderr


class TestPipelinesUpdateCmd:
    """Test pipelines update command."""

    def test_update_description(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test updating pipeline description.

        Ported from testUpdate() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/pipelines",
            method="GET",
        ).respond_with_json(
            {
                "pipelines": [
                    {
                        "pipelineId": 217997727159863,
                        "name": "sleep_one_minute",
                        "repository": "https://github.com/pditommaso/nf-sleep",
                    }
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/pipelines/217997727159863/launch",
            method="GET",
        ).respond_with_json(
            {
                "launch": {
                    "computeEnv": {"id": "vYOK4vn7spw7bHHWBDXZ2"},
                    "pipeline": "https://github.com/pditommaso/nf-sleep",
                    "workDir": "s3://nextflow-ci/jordeu",
                    "paramsText": "timeout: 60\n",
                    "pullLatest": False,
                    "stubRun": False,
                }
            }
        )

        httpserver.expect_request(
            "/pipelines/217997727159863",
            method="PUT",
        ).respond_with_json(
            {
                "pipeline": {
                    "pipelineId": 217997727159863,
                    "name": "sleep_one_minute",
                    "description": "Sleep one minute and exit",
                }
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "pipelines",
            "update",
            "-n",
            "sleep_one_minute",
            "-d",
            "Sleep one minute and exit",
        )

        # Assertions
        assert out.exit_code == 0
        assert "sleep_one_minute" in out.stdout
        assert "updated" in out.stdout.lower()


class TestPipelinesExportCmd:
    """Test pipelines export command."""

    def test_export(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test exporting pipeline configuration.

        Ported from testExport() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/pipelines",
            method="GET",
        ).respond_with_json(
            {
                "pipelines": [
                    {
                        "pipelineId": 183522618315672,
                        "name": "sleep",
                        "repository": "https://github.com/pditommaso/nf-sleep",
                    }
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/pipelines/183522618315672",
            method="GET",
        ).respond_with_json(
            {
                "pipeline": {
                    "pipelineId": 183522618315672,
                    "name": "sleep",
                    "repository": "https://github.com/pditommaso/nf-sleep",
                }
            }
        )

        httpserver.expect_request(
            "/pipelines/183522618315672/launch",
            method="GET",
        ).respond_with_json(
            {
                "launch": {
                    "computeEnv": {"id": "vYOK4vn7spw7bHHWBDXZ2"},
                    "pipeline": "https://github.com/pditommaso/nf-sleep",
                    "workDir": "s3://nextflow-ci/jordeu",
                    "paramsText": "timeout: 60\n",
                    "pullLatest": False,
                    "stubRun": False,
                    "resume": False,
                }
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("pipelines", "export", "-n", "sleep")

        # Assertions
        assert out.exit_code == 0
        # Output should be JSON
        config = json.loads(out.stdout)
        assert "launch" in config
        assert config["launch"]["pipeline"] == "https://github.com/pditommaso/nf-sleep"


class TestPipelinesImportCmd:
    """Test pipelines import command."""

    def test_import(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test importing pipeline from config file.

        Ported from testImport() in PipelinesCmdTest.java
        """
        httpserver.expect_request(
            "/compute-envs",
            method="GET",
        ).respond_with_json(
            {
                "computeEnvs": [
                    {
                        "id": "isnEDBLvHDAIteOEF44ow",
                        "name": "demo",
                        "platform": "aws-batch",
                        "status": "AVAILABLE",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/compute-envs/isnEDBLvHDAIteOEF44ow",
            method="GET",
        ).respond_with_json(
            {
                "computeEnv": {
                    "id": "isnEDBLvHDAIteOEF44ow",
                    "name": "demo",
                    "config": {
                        "workDir": "s3://nextflow-ci/jordeu",
                    },
                }
            }
        )

        httpserver.expect_request(
            "/pipelines",
            method="POST",
        ).respond_with_json(
            {
                "pipeline": {
                    "pipelineId": 112055935685449,
                    "name": "pipelineNew",
                }
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Create temp config file
        config = {
            "name": "pipelineNew",
            "launch": {
                "computeEnvId": "isnEDBLvHDAIteOEF44ow",
                "pipeline": "https://github.com/grananda/nextflow-hello",
                "workDir": "s3://nextflow-ci/julio",
                "revision": "main",
                "resume": False,
                "pullLatest": False,
                "stubRun": False,
            },
        }

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".json", delete=False
        ) as f:
            json.dump(config, f)
            config_file = f.name

        try:
            # Run the command
            out = exec_cmd(
                "pipelines",
                "import",
                config_file,
                "-n",
                "pipelineNew",
            )

            # Assertions
            assert out.exit_code == 0
            assert "pipelineNew" in out.stdout
            assert "added" in out.stdout.lower()
        finally:
            Path(config_file).unlink()

    def test_import_with_overwrite(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test importing pipeline with overwrite flag.

        Ported from testImportWithOverwrite() in PipelinesCmdTest.java
        """
        # Setup existing pipeline search
        httpserver.expect_request(
            "/pipelines",
            method="GET",
        ).respond_with_json(
            {
                "pipelines": [
                    {
                        "pipelineId": 183522618315672,
                        "name": "sleep_one_minute",
                        "repository": "https://github.com/pditommaso/nf-sleep",
                    }
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/pipelines/183522618315672",
            method="DELETE",
        ).respond_with_data("", status=200)

        httpserver.expect_request(
            "/compute-envs",
            method="GET",
        ).respond_with_json(
            {
                "computeEnvs": [
                    {
                        "id": "isnEDBLvHDAIteOEF44ow",
                        "name": "demo",
                        "platform": "aws-batch",
                        "status": "AVAILABLE",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/compute-envs/isnEDBLvHDAIteOEF44ow",
            method="GET",
        ).respond_with_json(
            {
                "computeEnv": {
                    "id": "isnEDBLvHDAIteOEF44ow",
                    "name": "demo",
                    "config": {
                        "workDir": "s3://nextflow-ci/jordeu",
                    },
                }
            }
        )

        httpserver.expect_request(
            "/pipelines",
            method="POST",
        ).respond_with_json(
            {
                "pipeline": {
                    "pipelineId": 112055935685449,
                    "name": "sleep_one_minute",
                }
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        # Create temp config file
        config = {
            "name": "sleep_one_minute",
            "launch": {
                "computeEnvId": "isnEDBLvHDAIteOEF44ow",
                "pipeline": "https://github.com/grananda/nextflow-hello",
                "workDir": "s3://nextflow-ci/julio",
                "revision": "main",
                "resume": False,
                "pullLatest": False,
                "stubRun": False,
            },
        }

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".json", delete=False
        ) as f:
            json.dump(config, f)
            config_file = f.name

        try:
            # Run the command
            out = exec_cmd(
                "pipelines",
                "import",
                config_file,
                "--overwrite",
            )

            # Assertions
            assert out.exit_code == 0
            assert "sleep_one_minute" in out.stdout
            assert "added" in out.stdout.lower()
        finally:
            Path(config_file).unlink()
