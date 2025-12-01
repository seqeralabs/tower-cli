"""
Tests for datasets commands.

Ported from DatasetsCmdTest.java
"""

import json
import pytest
from pytest_httpserver import HTTPServer


class TestDatasetsCmd:
    """Test datasets commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing datasets.

        Ported from testList() in DatasetsCmdTest.java
        """
        workspace_id = "249664655368293"

        # Setup mock HTTP expectation
        datasets_response = {
            "datasets": [
                {
                    "id": "4D9TP0w2pM0qmwqVHgrgBK",
                    "name": "dataset1",
                    "description": None,
                    "mediaType": None,
                    "deleted": False,
                    "dateCreated": "2021-11-26T14:51:20+01:00",
                    "lastUpdated": "2021-11-26T14:51:20+01:00"
                },
                {
                    "id": "1W2FqBiI6WoNokQTkPkEzo",
                    "name": "dataset2",
                    "description": None,
                    "mediaType": None,
                    "deleted": False,
                    "dateCreated": "2021-11-29T08:05:44+01:00",
                    "lastUpdated": "2021-11-29T08:05:44+01:00"
                }
            ]
        }

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets",
            method="GET",
        ).respond_with_json(datasets_response, status=200)

        # Run the command
        out = exec_cmd(
            "datasets",
            "list",
            "-w",
            workspace_id,
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceId"] == workspace_id
            assert len(data["datasets"]) == 2
            assert data["datasets"][0]["id"] == "4D9TP0w2pM0qmwqVHgrgBK"
            assert data["datasets"][0]["name"] == "dataset1"
            assert data["datasets"][1]["id"] == "1W2FqBiI6WoNokQTkPkEzo"
            assert data["datasets"][1]["name"] == "dataset2"
        elif output_format == "yaml":
            import yaml
            data = yaml.safe_load(out.stdout)
            assert data["workspaceId"] == workspace_id
            assert len(data["datasets"]) == 2
        else:  # console
            assert workspace_id in out.stdout
            assert "dataset1" in out.stdout
            assert "dataset2" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_view(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test viewing a dataset.

        Ported from testView() in DatasetsCmdTest.java
        """
        workspace_id = "249664655368293"
        dataset_id = "4D9TP0w2pM0qmwqVHgrgBK"

        # Setup mock HTTP expectation
        dataset_response = {
            "dataset": {
                "id": dataset_id,
                "name": "dataset1",
                "description": None,
                "mediaType": None,
                "deleted": False,
                "dateCreated": "2021-11-26T14:51:20+01:00",
                "lastUpdated": "2021-11-26T14:51:20+01:00"
            }
        }

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}/metadata",
            method="GET",
        ).respond_with_json(dataset_response, status=200)

        # Run the command
        out = exec_cmd(
            "datasets",
            "view",
            "-w",
            workspace_id,
            "-i",
            dataset_id,
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceId"] == workspace_id
            assert data["dataset"]["id"] == dataset_id
            assert data["dataset"]["name"] == "dataset1"
        elif output_format == "yaml":
            import yaml
            data = yaml.safe_load(out.stdout)
            assert data["workspaceId"] == workspace_id
            assert data["dataset"]["id"] == dataset_id
        else:  # console
            assert "dataset1" in out.stdout
            assert dataset_id in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_versions(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing dataset versions.

        Ported from testVersions() in DatasetsCmdTest.java
        """
        workspace_id = "249664655368293"
        dataset_id = "4D9TP0w2pM0qmwqVHgrgBK"

        # Setup mock HTTP expectations
        dataset_response = {
            "dataset": {
                "id": dataset_id,
                "name": "dataset1",
                "description": None,
                "mediaType": None,
                "deleted": False,
                "dateCreated": "2021-11-26T14:51:20+01:00",
                "lastUpdated": "2021-11-26T14:51:20+01:00"
            }
        }

        versions_response = {
            "versions": [
                {
                    "datasetId": dataset_id,
                    "datasetName": "dataset1",
                    "hasHeader": False,
                    "version": 1,
                    "lastUpdated": "2021-11-29T09:28:09+01:00",
                    "fileName": "transaciones_2021-11-26_filter-advanced.csv",
                    "mediaType": "text/csv",
                    "url": f"https://example.com/api/workspaces/{workspace_id}/datasets/{dataset_id}/v/1/n/transaciones_2021-11-26_filter-advanced.csv"
                },
                {
                    "datasetId": dataset_id,
                    "datasetName": "dataset1",
                    "hasHeader": False,
                    "version": 2,
                    "lastUpdated": "2021-11-29T09:28:09+01:00",
                    "fileName": "transaciones_2021-11-26_filter-advanced.csv",
                    "mediaType": "text/csv",
                    "url": f"https://example.com/api/workspaces/{workspace_id}/datasets/{dataset_id}/v/2/n/transaciones_2021-11-26_filter-advanced.csv"
                }
            ]
        }

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}/metadata",
            method="GET",
        ).respond_with_json(dataset_response, status=200)

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}/versions",
            method="GET",
        ).respond_with_json(versions_response, status=200)

        # Run the command
        out = exec_cmd(
            "datasets",
            "view",
            "-w",
            workspace_id,
            "-i",
            dataset_id,
            "versions",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["datasetId"] == dataset_id
            assert data["workspaceId"] == workspace_id
            assert len(data["versions"]) == 2
            assert data["versions"][0]["version"] == 1
            assert data["versions"][1]["version"] == 2
        elif output_format == "yaml":
            import yaml
            data = yaml.safe_load(out.stdout)
            assert len(data["versions"]) == 2
        else:  # console
            assert "1" in out.stdout
            assert "2" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting a dataset.

        Ported from testDelete() in DatasetsCmdTest.java
        """
        workspace_id = "249664655368293"
        dataset_id = "4D9TP0w2pM0qmwqVHgrgBK"

        # Setup mock HTTP expectations
        dataset_response = {
            "dataset": {
                "id": dataset_id,
                "name": "dataset1",
                "description": None,
                "mediaType": None,
                "deleted": False,
                "dateCreated": "2021-11-26T14:51:20+01:00",
                "lastUpdated": "2021-11-26T14:51:20+01:00"
            }
        }

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}/metadata",
            method="GET",
        ).respond_with_json(dataset_response, status=200)

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}",
            method="DELETE",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "datasets",
            "delete",
            "-w",
            workspace_id,
            "-i",
            dataset_id,
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == dataset_id
            assert data["workspaceId"] == workspace_id
        elif output_format == "yaml":
            import yaml
            data = yaml.safe_load(out.stdout)
            assert data["id"] == dataset_id
        else:  # console
            assert dataset_id in out.stdout
            assert "deleted" in out.stdout.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_url(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test getting dataset URL.

        Ported from testUrl() in DatasetsCmdTest.java
        """
        workspace_id = "249664655368293"
        dataset_id = "4D9TP0w2pM0qmwqVHgrgBK"
        expected_url = f"https://example.com/api/workspaces/{workspace_id}/datasets/{dataset_id}/v/2/n/transaciones_2021-11-26_filter-advanced.csv"

        # Setup mock HTTP expectations
        dataset_response = {
            "dataset": {
                "id": dataset_id,
                "name": "dataset1",
                "description": None,
                "mediaType": None,
                "deleted": False,
                "dateCreated": "2021-11-26T14:51:20+01:00",
                "lastUpdated": "2021-11-26T14:51:20+01:00"
            }
        }

        versions_response = {
            "versions": [
                {
                    "datasetId": dataset_id,
                    "datasetName": "dataset1",
                    "hasHeader": False,
                    "version": 2,
                    "lastUpdated": "2021-11-29T09:28:09+01:00",
                    "fileName": "transaciones_2021-11-26_filter-advanced.csv",
                    "mediaType": "text/csv",
                    "url": expected_url
                }
            ]
        }

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}/metadata",
            method="GET",
        ).respond_with_json(dataset_response, status=200)

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}/versions",
            method="GET",
        ).respond_with_json(versions_response, status=200)

        # Run the command
        out = exec_cmd(
            "datasets",
            "url",
            "-w",
            workspace_id,
            "-i",
            dataset_id,
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["url"] == expected_url
            assert data["datasetId"] == dataset_id
            assert data["workspaceId"] == workspace_id
        elif output_format == "yaml":
            import yaml
            data = yaml.safe_load(out.stdout)
            assert data["url"] == expected_url
        else:  # console
            # The URL might be wrapped across lines in console output, so check for key parts
            # Remove line breaks to properly check the URL
            stdout_no_breaks = out.stdout.replace("\n", "")
            assert "Dataset URL:" in out.stdout
            assert dataset_id in stdout_no_breaks
            assert "transaciones_2021-11-26_filter-advanced.csv" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        temp_file: callable,
        output_format: str,
    ) -> None:
        """
        Test adding a dataset.

        Ported from testAdd() in DatasetsCmdTest.java
        """
        workspace_id = "249664655368293"
        dataset_name = "dataset3"
        dataset_id = "1W3BTHWgRH71OJmOPMdG7S"

        # Create temporary CSV file
        csv_content = "col1,col2,col3\nval1,val2,val3\n"
        csv_file = temp_file(csv_content, "data", ".csv")

        # Setup mock HTTP expectations
        create_response = {
            "dataset": {
                "id": dataset_id,
                "name": dataset_name,
                "description": "Dataset 3 description.",
                "mediaType": None,
                "deleted": False,
                "dateCreated": "2021-11-29T11:18:06.108+01:00",
                "lastUpdated": "2021-11-29T11:18:06.108+01:00"
            }
        }

        upload_response = {
            "version": 1
        }

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets",
            method="POST",
        ).respond_with_json(create_response, status=200)

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}/upload",
            method="POST",
            query_string="header=false",
        ).respond_with_json(upload_response, status=200)

        # Run the command
        out = exec_cmd(
            "datasets",
            "add",
            csv_file,
            "-w",
            workspace_id,
            "-n",
            dataset_name,
            "-d",
            "Dataset 3 description.",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["name"] == dataset_name
            assert data["workspaceId"] == workspace_id
            assert data["id"] == dataset_id
        elif output_format == "yaml":
            import yaml
            data = yaml.safe_load(out.stdout)
            assert data["name"] == dataset_name
        else:  # console
            assert dataset_name in out.stdout
            assert workspace_id in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test updating a dataset.

        Ported from testUpdate() in DatasetsCmdTest.java
        """
        workspace_id = "249664655368293"
        dataset_id = "4D9TP0w2pM0qmwqVHgrgBK"
        new_name = "dataset1"

        # Setup mock HTTP expectations
        dataset_response = {
            "dataset": {
                "id": dataset_id,
                "name": "dataset1",
                "description": None,
                "mediaType": None,
                "deleted": False,
                "dateCreated": "2021-11-26T14:51:20+01:00",
                "lastUpdated": "2021-11-26T14:51:20+01:00"
            }
        }

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}/metadata",
            method="GET",
        ).respond_with_json(dataset_response, status=200)

        httpserver.expect_request(
            f"/workspaces/{workspace_id}/datasets/{dataset_id}",
            method="PUT",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "datasets",
            "update",
            "-w",
            workspace_id,
            "-i",
            dataset_id,
            "--new-name",
            new_name,
            "-d",
            "Dataset 3 description.",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["name"] == new_name
            assert data["workspaceId"] == workspace_id
            assert data["id"] == dataset_id
        elif output_format == "yaml":
            import yaml
            data = yaml.safe_load(out.stdout)
            assert data["name"] == new_name
        else:  # console
            assert new_name in out.stdout
            assert workspace_id in out.stdout

    def test_file_not_exists_error(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test error when file doesn't exist.

        Ported from testFileNotExistsError() in DatasetsCmdTest.java
        """
        workspace_id = "249664655368293"

        # Run the command with non-existent file
        out = exec_cmd(
            "datasets",
            "add",
            "-w",
            workspace_id,
            "-n",
            "name",
            "path/that/do/not/exist/file.tsv",
        )

        # Assertions
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "not" in out.stderr.lower() and "exist" in out.stderr.lower()
