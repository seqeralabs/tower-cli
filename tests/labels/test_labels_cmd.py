"""
Tests for labels commands.

Ported from LabelsCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestLabelsCmd:
    """Test labels commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_simple(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding a simple label (without value).

        Ported from testAddSimple() in LabelsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/labels",
            method="POST",
        ).respond_with_json(
            {
                "id": 10,
                "name": "some-label",
                "resource": False,
                "value": None,
            },
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "labels",
            "add",
            "-n",
            "some-label",
            "-w",
            "123",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == 10
            assert data["name"] == "some-label"
            assert data["resource"] is False
            assert data["value"] is None
            assert data["workspaceId"] == "123"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == 10
            assert data["name"] == "some-label"
            assert data["resource"] is False
            assert data["value"] is None
            assert data["workspaceId"] == "123"
        else:  # console
            assert "some-label" in out.stdout
            assert "added" in out.stdout.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_resource(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding a resource label (with value).

        Ported from testAddResource() in LabelsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/labels",
            method="POST",
        ).respond_with_json(
            {
                "id": 10,
                "name": "res",
                "resource": True,
                "value": "val",
            },
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "labels",
            "add",
            "-n",
            "res",
            "-v",
            "val",
            "-w",
            "4343",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == 10
            assert data["name"] == "res"
            assert data["resource"] is True
            assert data["value"] == "val"
            assert data["workspaceId"] == "4343"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == 10
            assert data["name"] == "res"
            assert data["resource"] is True
            assert data["value"] == "val"
            assert data["workspaceId"] == "4343"
        else:  # console
            assert "res" in out.stdout
            assert "val" in out.stdout
            assert "added" in out.stdout.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete_label(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting a label.

        Ported from testDeleteLabel() in LabelsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/labels/1234",
            method="DELETE",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "labels",
            "delete",
            "-i",
            "1234",
            "-w",
            "5662",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == 1234
            assert data["workspaceId"] == 5662
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == 1234
            assert data["workspaceId"] == 5662
        else:  # console
            assert "1234" in out.stdout
            assert "deleted" in out.stdout.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list_labels(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing all labels.

        Ported from testListLabels() in LabelsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/labels",
            method="GET",
        ).respond_with_json(
            {
                "labels": [
                    {
                        "id": 97027588903667,
                        "name": "awesome-label",
                        "value": None,
                        "resource": False,
                    },
                    {
                        "id": 250670995082875,
                        "name": "new-label",
                        "value": None,
                        "resource": False,
                    },
                    {
                        "id": 55286297817389,
                        "name": "newx-label",
                        "value": None,
                        "resource": False,
                    },
                    {
                        "id": 232243090533688,
                        "name": "res-label",
                        "value": "aaaa",
                        "resource": True,
                    },
                ],
                "totalSize": 4,
            },
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "labels",
            "list",
            "-w",
            "5662512677752",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceId"] == 5662512677752
            assert data["type"] == "all"
            assert len(data["labels"]) == 4
            assert data["labels"][0]["id"] == 97027588903667
            assert data["labels"][0]["name"] == "awesome-label"
            assert data["labels"][0]["value"] is None
            assert data["labels"][0]["resource"] is False
            assert data["labels"][3]["name"] == "res-label"
            assert data["labels"][3]["value"] == "aaaa"
            assert data["labels"][3]["resource"] is True
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceId"] == 5662512677752
            assert data["type"] == "all"
            assert len(data["labels"]) == 4
            assert data["labels"][0]["name"] == "awesome-label"
            assert data["labels"][3]["name"] == "res-label"
        else:  # console
            assert "awesome-label" in out.stdout
            assert "new-label" in out.stdout
            assert "newx-label" in out.stdout
            assert "res-label" in out.stdout
            assert "aaaa" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list_labels_with_simple_type_filter(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing labels with simple type filter.

        Ported from testListLabelsWithSimpleTypeFilter() in LabelsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/labels",
            method="GET",
        ).respond_with_json(
            {
                "labels": [
                    {
                        "id": 97027588903667,
                        "name": "awesome-label",
                        "value": None,
                        "resource": False,
                    },
                    {
                        "id": 250670995082875,
                        "name": "new-label",
                        "value": None,
                        "resource": False,
                    },
                    {
                        "id": 55286297817389,
                        "name": "newx-label",
                        "value": None,
                        "resource": False,
                    },
                ],
                "totalSize": 3,
            },
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "labels",
            "list",
            "-w",
            "5662512677752",
            "-t",
            "simple",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceId"] == 5662512677752
            assert data["type"] == "simple"
            assert len(data["labels"]) == 3
            assert data["labels"][0]["name"] == "awesome-label"
            assert data["labels"][0]["resource"] is False
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceId"] == 5662512677752
            assert data["type"] == "simple"
            assert len(data["labels"]) == 3
        else:  # console
            assert "awesome-label" in out.stdout
            assert "new-label" in out.stdout
            assert "newx-label" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list_labels_with_resource_type_filter(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing labels with resource type filter.

        Ported from testListLabelsWithResourceTypeFilter() in LabelsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/labels",
            method="GET",
        ).respond_with_json(
            {
                "labels": [
                    {
                        "id": 97027588903667,
                        "name": "awesome-label",
                        "value": "aaa",
                        "resource": True,
                    },
                    {
                        "id": 250670995082875,
                        "name": "new-label",
                        "value": "bbb",
                        "resource": True,
                    },
                    {
                        "id": 55286297817389,
                        "name": "newx-label",
                        "value": "ccc",
                        "resource": True,
                    },
                ],
                "totalSize": 3,
            },
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "labels",
            "list",
            "-w",
            "5662512677752",
            "-t",
            "resource",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceId"] == 5662512677752
            assert data["type"] == "resource"
            assert len(data["labels"]) == 3
            assert data["labels"][0]["name"] == "awesome-label"
            assert data["labels"][0]["value"] == "aaa"
            assert data["labels"][0]["resource"] is True
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceId"] == 5662512677752
            assert data["type"] == "resource"
            assert len(data["labels"]) == 3
        else:  # console
            assert "awesome-label" in out.stdout
            assert "new-label" in out.stdout
            assert "newx-label" in out.stdout
            assert "aaa" in out.stdout
            assert "bbb" in out.stdout
            assert "ccc" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list_labels_with_text_filter(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing labels with text search filter.

        Ported from testListLabelsWithTextFilter() in LabelsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/labels",
            method="GET",
        ).respond_with_json(
            {
                "labels": [
                    {
                        "id": 97027588903667,
                        "name": "res-find-label",
                        "value": "aaa",
                        "resource": True,
                    },
                    {
                        "id": 250670995082875,
                        "name": "label-to-find",
                        "value": None,
                        "resource": False,
                    },
                    {
                        "id": 55286297817389,
                        "name": "find-label",
                        "value": "ccc",
                        "resource": True,
                    },
                ],
                "totalSize": 3,
            },
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "labels",
            "list",
            "-w",
            "5662512677752",
            "-f",
            "find",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceId"] == 5662512677752
            assert len(data["labels"]) == 3
            assert data["labels"][0]["name"] == "res-find-label"
            assert data["labels"][1]["name"] == "label-to-find"
            assert data["labels"][2]["name"] == "find-label"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceId"] == 5662512677752
            assert len(data["labels"]) == 3
        else:  # console
            assert "res-find-label" in out.stdout
            assert "label-to-find" in out.stdout
            assert "find-label" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update_label_name(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """Test updating a label name."""
        # Setup mock HTTP expectations
        # First GET existing label
        httpserver.expect_request(
            "/labels/1234",
            method="GET",
        ).respond_with_json(
            {
                "id": 1234,
                "name": "old-name",
                "value": None,
                "resource": False,
            },
            status=200,
        )

        # Then PUT to update
        httpserver.expect_request(
            "/labels/1234",
            method="PUT",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "labels",
            "update",
            "-i",
            "1234",
            "-n",
            "new-name",
            "-w",
            "5662",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == 1234
            assert data["name"] == "new-name"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == 1234
            assert data["name"] == "new-name"
        else:  # console
            assert "new-name" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update_label_value(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """Test updating a label value."""
        # Setup mock HTTP expectations
        # First GET existing label
        httpserver.expect_request(
            "/labels/1234",
            method="GET",
        ).respond_with_json(
            {
                "id": 1234,
                "name": "res-label",
                "value": "old-value",
                "resource": True,
            },
            status=200,
        )

        # Then PUT to update
        httpserver.expect_request(
            "/labels/1234",
            method="PUT",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "labels",
            "update",
            "-i",
            "1234",
            "-v",
            "new-value",
            "-w",
            "5662",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == 1234
            assert data["name"] == "res-label"
            assert data["value"] == "new-value"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == 1234
            assert data["value"] == "new-value"
        else:  # console
            assert "res-label" in out.stdout
