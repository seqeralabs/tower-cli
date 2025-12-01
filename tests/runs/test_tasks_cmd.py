"""
Tests for runs tasks commands.

Ported from TasksCmdTest.java
"""

import json
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


def load_resource(name: str) -> bytes:
    """Load a test resource file."""
    resource_path = Path(__file__).parent.parent / "resources" / "runs" / f"{name}.json"
    return resource_path.read_bytes()


class TestTasksCmd:
    """Test runs tasks commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list_run_tasks(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing tasks for a workflow run.

        Ported from listRunTasksTests() in TasksCmdTest.java
        """
        # Setup mock HTTP expectation
        tasks_response = load_resource("tasks_list_response")

        httpserver.expect_request(
            "/workflow/2zGxKoqlnVmGL/tasks",
            method="GET",
        ).respond_with_data(tasks_response, status=200, content_type="application/json")

        # Run the command
        out = exec_cmd(
            "runs",
            "tasks",
            "-i",
            "2zGxKoqlnVmGL",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["runId"] == "2zGxKoqlnVmGL"
            assert len(data["tasks"]) == 2
            assert data["tasks"][0]["taskId"] == 1
            assert (
                data["tasks"][0]["process"]
                == "NFCORE_RNASEQ:RNASEQ:PREPARE_GENOME:GUNZIP_ADDITIONAL_FASTA"
            )
            assert data["tasks"][0]["tag"] == "gfp.fa.gz"
            assert data["tasks"][0]["status"] == "COMPLETED"
            assert data["tasks"][1]["taskId"] == 2
            assert (
                data["tasks"][1]["process"]
                == "NFCORE_RNASEQ:RNASEQ:PREPARE_GENOME:UNTAR_STAR_INDEX"
            )
            assert data["tasks"][1]["status"] == "COMPLETED"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["runId"] == "2zGxKoqlnVmGL"
            assert len(data["tasks"]) == 2
            assert data["tasks"][0]["taskId"] == 1
        else:  # console
            assert "2zGxKoqlnVmGL" in out.stdout
            assert "PREPARE_GENOME" in out.stdout  # Part of process name
            assert "COMPLETED" in out.stdout
