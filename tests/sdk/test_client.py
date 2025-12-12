"""Tests for the Seqera SDK client."""

import os

import pytest
from pytest_httpserver import HTTPServer


class TestSeqeraClient:
    """Tests for the main Seqera SDK client."""

    def test_client_requires_token(self):
        """Test that client raises error without token."""
        from seqera import Seqera

        # Clear environment variables
        old_token = os.environ.pop("SEQERA_ACCESS_TOKEN", None)
        old_tower = os.environ.pop("TOWER_ACCESS_TOKEN", None)

        try:
            with pytest.raises(ValueError, match="Access token required"):
                Seqera()
        finally:
            # Restore environment
            if old_token:
                os.environ["SEQERA_ACCESS_TOKEN"] = old_token
            if old_tower:
                os.environ["TOWER_ACCESS_TOKEN"] = old_tower

    def test_client_accepts_token(self, httpserver: HTTPServer):
        """Test that client accepts explicit token."""
        from seqera import Seqera

        client = Seqera(
            access_token="test-token",
            url=httpserver.url_for("/"),
            insecure=True,
        )
        assert client is not None
        client.close()

    def test_client_context_manager(self, httpserver: HTTPServer):
        """Test client as context manager."""
        from seqera import Seqera

        with Seqera(
            access_token="test-token",
            url=httpserver.url_for("/"),
            insecure=True,
        ) as client:
            assert client is not None

    def test_client_has_resources(self, httpserver: HTTPServer):
        """Test that client exposes all resource types."""
        from seqera import Seqera

        with Seqera(
            access_token="test-token",
            url=httpserver.url_for("/"),
            insecure=True,
        ) as client:
            assert hasattr(client, "pipelines")
            assert hasattr(client, "runs")
            assert hasattr(client, "workspaces")
            assert hasattr(client, "organizations")
            assert hasattr(client, "credentials")
            assert hasattr(client, "compute_envs")
            assert hasattr(client, "secrets")
            assert hasattr(client, "labels")
            assert hasattr(client, "datasets")
            assert hasattr(client, "teams")
            assert hasattr(client, "members")
            assert hasattr(client, "participants")
            assert hasattr(client, "actions")
            assert hasattr(client, "studios")
            assert hasattr(client, "collaborators")
            assert hasattr(client, "data_links")

    def test_client_info(self, httpserver: HTTPServer):
        """Test client info endpoint."""
        from seqera import Seqera

        httpserver.expect_request("/service-info").respond_with_json(
            {
                "serviceInfo": {
                    "version": "24.1.0",
                    "apiVersion": "1.0.0",
                }
            }
        )

        with Seqera(
            access_token="test-token",
            url=httpserver.url_for("/"),
            insecure=True,
        ) as client:
            info = client.info()
            assert "serviceInfo" in info


class TestPipelinesResource:
    """Tests for the pipelines resource."""

    def test_list_pipelines(self, httpserver: HTTPServer):
        """Test listing pipelines."""
        from seqera import Seqera

        httpserver.expect_request("/pipelines").respond_with_json(
            {
                "pipelines": [
                    {
                        "pipelineId": 1,
                        "name": "test-pipeline",
                        "pipeline": "https://github.com/test/repo",
                    },
                    {
                        "pipelineId": 2,
                        "name": "another-pipeline",
                        "pipeline": "https://github.com/test/another",
                    },
                ],
                "totalSize": 2,
            }
        )

        with Seqera(
            access_token="test-token",
            url=httpserver.url_for("/"),
            insecure=True,
        ) as client:
            pipelines = list(client.pipelines.list())

            assert len(pipelines) == 2
            assert pipelines[0].name == "test-pipeline"
            assert pipelines[0].pipeline_id == 1
            assert pipelines[1].name == "another-pipeline"

    def test_get_pipeline(self, httpserver: HTTPServer):
        """Test getting a pipeline by ID."""
        from seqera import Seqera

        httpserver.expect_request("/pipelines/123").respond_with_json(
            {
                "pipeline": {
                    "pipelineId": 123,
                    "name": "my-pipeline",
                    "pipeline": "https://github.com/test/repo",
                    "description": "A test pipeline",
                }
            }
        )

        with Seqera(
            access_token="test-token",
            url=httpserver.url_for("/"),
            insecure=True,
        ) as client:
            pipeline = client.pipelines.get(123)

            assert pipeline.pipeline_id == 123
            assert pipeline.name == "my-pipeline"
            assert pipeline.description == "A test pipeline"


class TestRunsResource:
    """Tests for the runs resource."""

    def test_list_runs(self, httpserver: HTTPServer):
        """Test listing workflow runs."""
        from seqera import Seqera

        httpserver.expect_request("/workflow").respond_with_json(
            {
                "workflows": [
                    {
                        "workflow": {
                            "id": "abc123",
                            "runName": "run-1",
                            "status": "SUCCEEDED",
                        }
                    },
                    {
                        "workflow": {
                            "id": "def456",
                            "runName": "run-2",
                            "status": "RUNNING",
                        }
                    },
                ],
                "totalSize": 2,
            }
        )

        with Seqera(
            access_token="test-token",
            url=httpserver.url_for("/"),
            insecure=True,
        ) as client:
            runs = list(client.runs.list())

            assert len(runs) == 2
            assert runs[0].id == "abc123"
            assert runs[0].status == "SUCCEEDED"
            assert runs[1].run_name == "run-2"

    def test_get_run(self, httpserver: HTTPServer):
        """Test getting a workflow run by ID."""
        from seqera import Seqera

        httpserver.expect_request("/workflow/abc123").respond_with_json(
            {
                "workflow": {
                    "id": "abc123",
                    "runName": "my-run",
                    "status": "SUCCEEDED",
                    "workDir": "/work/dir",
                }
            }
        )

        with Seqera(
            access_token="test-token",
            url=httpserver.url_for("/"),
            insecure=True,
        ) as client:
            run = client.runs.get("abc123")

            assert run.id == "abc123"
            assert run.run_name == "my-run"
            assert run.status == "SUCCEEDED"


class TestModels:
    """Tests for Pydantic models."""

    def test_pipeline_model(self):
        """Test Pipeline model."""
        from seqera.models.pipelines import Pipeline

        data = {
            "pipelineId": 1,
            "name": "test",
            "pipeline": "https://github.com/test/repo",
            "description": "A description",
        }

        pipeline = Pipeline.model_validate(data)
        assert pipeline.pipeline_id == 1
        assert pipeline.name == "test"
        assert pipeline.repository == "https://github.com/test/repo"
        assert pipeline.id == 1  # alias property

    def test_workflow_model(self):
        """Test Workflow model."""
        from seqera.models.runs import Workflow

        data = {
            "id": "abc123",
            "runName": "my-run",
            "status": "RUNNING",
            "workDir": "/work",
        }

        workflow = Workflow.model_validate(data)
        assert workflow.id == "abc123"
        assert workflow.run_name == "my-run"
        assert workflow.work_dir == "/work"

    def test_model_allows_extra_fields(self):
        """Test that models allow extra fields from API."""
        from seqera.models.pipelines import Pipeline

        data = {
            "pipelineId": 1,
            "name": "test",
            "pipeline": "url",
            "unknownField": "value",
            "anotherExtra": 123,
        }

        # Should not raise
        pipeline = Pipeline.model_validate(data)
        assert pipeline.pipeline_id == 1


class TestPaginatedList:
    """Tests for paginated list functionality."""

    def test_auto_pagination(self, httpserver: HTTPServer):
        """Test that pagination automatically fetches more pages."""
        from seqera import Seqera

        # First page
        httpserver.expect_ordered_request(
            "/pipelines", query_string="offset=0&max=50"
        ).respond_with_json(
            {
                "pipelines": [
                    {"pipelineId": i, "name": f"p{i}", "pipeline": "url"} for i in range(50)
                ],
                "totalSize": 75,
            }
        )

        # Second page
        httpserver.expect_ordered_request(
            "/pipelines", query_string="offset=50&max=50"
        ).respond_with_json(
            {
                "pipelines": [
                    {"pipelineId": i, "name": f"p{i}", "pipeline": "url"} for i in range(50, 75)
                ],
                "totalSize": 75,
            }
        )

        with Seqera(
            access_token="test-token",
            url=httpserver.url_for("/"),
            insecure=True,
        ) as client:
            all_pipelines = list(client.pipelines.list())
            assert len(all_pipelines) == 75


class TestPackageExports:
    """Tests for package-level exports."""

    def test_main_exports(self):
        """Test that main classes are exported from package root."""
        from seqera import (
            AsyncSeqera,
            Pipeline,
            PipelineNotFoundException,
            Seqera,
            SeqeraError,
            Workflow,
            Workspace,
        )

        assert Seqera is not None
        assert AsyncSeqera is not None
        assert Pipeline is not None
        assert Workflow is not None
        assert Workspace is not None
        assert SeqeraError is not None
        assert PipelineNotFoundException is not None

    def test_version(self):
        """Test that version is defined."""
        import seqera

        assert hasattr(seqera, "__version__")
        assert seqera.__version__ == "0.2.0"
