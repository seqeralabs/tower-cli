"""
Tests for runs metrics commands.

Ported from MetricsCmdTest.java
"""

import json
from pathlib import Path

import pytest
from pytest_httpserver import HTTPServer


def load_resource(name: str) -> bytes:
    """Load a test resource file."""
    resource_path = Path(__file__).parent.parent / "resources" / "runs" / f"{name}.json"
    return resource_path.read_bytes()


class TestMetricsCmd:
    """Test runs metrics commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_run_metrics(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test viewing workflow run metrics.

        Ported from testRunMetricsExpanded() and testRunMetricsCondensed() in MetricsCmdTest.java
        """
        # Setup mock HTTP expectation
        metrics_response = load_resource("runs_metrics")

        httpserver.expect_request(
            "/workflow/5dAZoXrcmZXRO4/metrics",
            method="GET",
        ).respond_with_data(metrics_response, status=200, content_type="application/json")

        # Run the command
        out = exec_cmd(
            "runs",
            "metrics",
            "-i",
            "5dAZoXrcmZXRO4",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["runId"] == "5dAZoXrcmZXRO4"
            assert "metrics" in data
            assert len(data["metrics"]) > 0
            # Check first metric has expected structure
            metric = data["metrics"][0]
            assert "process" in metric
            assert metric["process"] == "SAMPLESHEET_CHECK"
            assert "cpu" in metric
            assert "mem" in metric
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["runId"] == "5dAZoXrcmZXRO4"
            assert "metrics" in data
            assert len(data["metrics"]) > 0
        else:  # console
            assert "5dAZoXrcmZXRO4" in out.stdout or "Metrics" in out.stdout
            assert "SAMPLESHEET_CHECK" in out.stdout
