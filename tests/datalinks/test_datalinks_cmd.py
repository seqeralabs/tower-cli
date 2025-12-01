"""
Tests for data-links commands.

Port of DataLinksCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer

from tests.conftest import exec_command


# Test resource data
CREDENTIALS_RESPONSE = {
    "credentials": [
        {
            "id": "57Ic6reczFn78H1DTaaXkp",
            "name": "aws",
            "description": None,
            "discriminator": "aws",
            "baseUrl": None,
            "category": None,
            "deleted": None,
            "lastUsed": "2021-09-09T07:20:53Z",
            "dateCreated": "2021-09-08T05:48:51Z",
            "lastUpdated": "2021-09-08T05:48:51Z",
        }
    ]
}

USER_INFO_RESPONSE = {
    "user": {
        "id": 1264,
        "userName": "user",
        "email": "user@seqera.io",
    }
}

WORKSPACES_RESPONSE = {
    "orgsAndWorkspaces": [
        {
            "orgId": 38659136604200,
            "orgName": "organization1",
            "orgLogoUrl": None,
            "workspaceId": 75887156211589,
            "workspaceName": "workspace1",
            "workspaceFullName": "organization1/workspace1",
        }
    ]
}

DATALINKS_LIST_RESPONSE = {
    "dataLinks": [
        {
            "id": "v1-cloud-c2875f38a7b5c8fe34a5b382b5f9e0c4",
            "name": "a-test-bucket-eend-us-east-1",
            "description": None,
            "resourceRef": "s3://a-test-bucket-eend-us-east-1",
            "type": "bucket",
            "provider": "aws",
            "region": "us-east-1",
            "credentials": [
                {"id": "57Ic6reczFn78H1DTaaXkp", "name": "aws", "provider": "aws"}
            ],
            "publicAccessible": False,
            "hidden": False,
            "status": None,
            "message": None,
        },
        {
            "id": "v1-cloud-b89b60014c225c11f59048294354d174",
            "name": "adrian-navarro-test",
            "description": None,
            "resourceRef": "s3://adrian-navarro-test",
            "type": "bucket",
            "provider": "aws",
            "region": "us-east-1",
            "credentials": [
                {"id": "57Ic6reczFn78H1DTaaXkp", "name": "aws", "provider": "aws"}
            ],
            "publicAccessible": False,
            "hidden": False,
            "status": None,
            "message": None,
        },
        {
            "id": "v1-cloud-422306eddadfc64de0676a5923517733",
            "name": "adrian-navarro-test-us-west-2",
            "description": None,
            "resourceRef": "s3://adrian-navarro-test-us-west-2",
            "type": "bucket",
            "provider": "aws",
            "region": "us-west-2",
            "credentials": [
                {"id": "57Ic6reczFn78H1DTaaXkp", "name": "aws", "provider": "aws"}
            ],
            "publicAccessible": False,
            "hidden": False,
            "status": None,
            "message": None,
        },
    ]
}


class TestDataLinksCmd:
    """Tests for data-links commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        cli_runner,
        base_args,
        output_format: str,
        user_workspace_name: str,
    ) -> None:
        """Test data-links list command."""
        # Mock user-info
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(USER_INFO_RESPONSE)

        # Mock workspaces
        httpserver.expect_request(
            "/user/1264/workspaces",
            method="GET",
        ).respond_with_json(WORKSPACES_RESPONSE)

        # Mock credentials fetch
        httpserver.expect_request(
            "/credentials",
            method="GET",
        ).respond_with_json(CREDENTIALS_RESPONSE)

        # Mock data-links list (initial fetch for status check)
        httpserver.expect_request(
            "/data-links",
            method="GET",
        ).respond_with_json(DATALINKS_LIST_RESPONSE)

        # Run command
        out = exec_command(
            cli_runner,
            base_args,
            ["data-links", "list", "-w", "75887156211589"],
            output_format,
        )

        # Assert success
        assert out.exit_code == 0, f"Command failed: {out.stderr}"

        # Validate output based on format
        if output_format == "json":
            output_data = json.loads(out.stdout)
            assert "dataLinks" in output_data
            assert len(output_data["dataLinks"]) == 3
            assert (
                output_data["dataLinks"][0]["name"] == "a-test-bucket-eend-us-east-1"
            )
        elif output_format == "yaml":
            import yaml

            output_data = yaml.safe_load(out.stdout)
            assert "dataLinks" in output_data
            assert len(output_data["dataLinks"]) == 3
        else:  # console
            assert "a-test-bucket-eend-us-east-1" in out.stdout
            assert "adrian-navarro-test" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_filtered_list(
        self,
        httpserver: HTTPServer,
        cli_runner,
        base_args,
        output_format: str,
        user_workspace_name: str,
    ) -> None:
        """Test data-links list command with filters."""
        filtered_response = {
            "dataLinks": [
                {
                    "id": "v1-cloud-b89b60014c225c11f59048294354d174",
                    "name": "adrian-navarro-test",
                    "description": None,
                    "resourceRef": "s3://adrian-navarro-test",
                    "type": "bucket",
                    "provider": "aws",
                    "region": "us-east-1",
                    "credentials": [
                        {
                            "id": "57Ic6reczFn78H1DTaaXkp",
                            "name": "aws",
                            "provider": "aws",
                        }
                    ],
                    "publicAccessible": False,
                    "hidden": False,
                    "status": None,
                    "message": None,
                }
            ]
        }

        # Mock user-info
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(USER_INFO_RESPONSE)

        # Mock workspaces
        httpserver.expect_request(
            "/user/1264/workspaces",
            method="GET",
        ).respond_with_json(WORKSPACES_RESPONSE)

        # Mock credentials fetch
        httpserver.expect_request(
            "/credentials",
            method="GET",
        ).respond_with_json(CREDENTIALS_RESPONSE)

        # Mock filtered data-links list
        httpserver.expect_request(
            "/data-links",
            method="GET",
        ).respond_with_json(filtered_response)

        # Run command with filters
        out = exec_command(
            cli_runner,
            base_args,
            [
                "data-links",
                "list",
                "-w",
                "75887156211589",
                "-n",
                "adrian",
                "-p",
                "aws",
                "-r",
                "us-east-1",
            ],
            output_format,
        )

        # Assert success
        assert out.exit_code == 0, f"Command failed: {out.stderr}"

        if output_format == "json":
            output_data = json.loads(out.stdout)
            assert len(output_data["dataLinks"]) == 1
            assert output_data["dataLinks"][0]["name"] == "adrian-navarro-test"

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        cli_runner,
        base_args,
        output_format: str,
    ) -> None:
        """Test data-links add command."""
        created_datalink = {
            "id": "v1-user-ad5192871d3d65e1218ec6c4a2f7cde5",
            "name": "name",
            "description": "somedesc",
            "resourceRef": "s3://bucket",
            "type": "bucket",
            "provider": "aws",
            "region": "us-east-1",
            "credentials": [
                {"id": "57Ic6reczFn78H1DTaaXkp", "name": "aws", "provider": "aws"}
            ],
            "publicAccessible": False,
            "hidden": False,
            "status": None,
            "message": None,
        }

        # Mock credentials fetch
        httpserver.expect_request(
            "/credentials",
            method="GET",
        ).respond_with_json(CREDENTIALS_RESPONSE)

        # Mock data-links add
        httpserver.expect_request(
            "/data-links",
            method="POST",
        ).respond_with_json(created_datalink)

        # Run command
        out = exec_command(
            cli_runner,
            base_args,
            [
                "data-links",
                "add",
                "-w",
                "75887156211589",
                "-n",
                "name",
                "-d",
                "somedesc",
                "-p",
                "aws",
                "-u",
                "s3://bucket",
                "-c",
                "57Ic6reczFn78H1DTaaXkp",
            ],
            output_format,
        )

        # Assert success
        assert out.exit_code == 0, f"Command failed: {out.stderr}"

        if output_format == "json":
            output_data = json.loads(out.stdout)
            assert output_data["id"] == "v1-user-ad5192871d3d65e1218ec6c4a2f7cde5"
            assert output_data["name"] == "name"

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        cli_runner,
        base_args,
        output_format: str,
    ) -> None:
        """Test data-links delete command."""
        # Mock data-links delete
        httpserver.expect_request(
            "/data-links/v1-datalinkid",
            method="DELETE",
        ).respond_with_data("", status=200)

        # Run command
        out = exec_command(
            cli_runner,
            base_args,
            ["data-links", "delete", "-w", "75887156211589", "-i", "v1-datalinkid"],
            output_format,
        )

        # Assert success
        assert out.exit_code == 0, f"Command failed: {out.stderr}"

        if output_format == "json":
            output_data = json.loads(out.stdout)
            assert output_data["dataLinkId"] == "v1-datalinkid"
            assert output_data["workspaceId"] == 75887156211589

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update(
        self,
        httpserver: HTTPServer,
        cli_runner,
        base_args,
        output_format: str,
    ) -> None:
        """Test data-links update command."""
        updated_datalink = {
            "id": "v1-user-ad5192871d3d65e1218ec6c4a2f7cde5",
            "name": "name",
            "description": "somedesc",
            "resourceRef": "s3://bucket",
            "type": "bucket",
            "provider": "aws",
            "region": "us-east-1",
            "credentials": [
                {"id": "57Ic6reczFn78H1DTaaXkp", "name": "aws", "provider": "aws"}
            ],
            "publicAccessible": False,
            "hidden": False,
            "status": None,
            "message": None,
        }

        # Mock credentials fetch
        httpserver.expect_request(
            "/credentials",
            method="GET",
        ).respond_with_json(CREDENTIALS_RESPONSE)

        # Mock data-links update
        httpserver.expect_request(
            "/data-links/v1-somedatalinkid",
            method="PUT",
        ).respond_with_json(updated_datalink)

        # Run command
        out = exec_command(
            cli_runner,
            base_args,
            [
                "data-links",
                "update",
                "-w",
                "75887156211589",
                "-i",
                "v1-somedatalinkid",
                "-n",
                "name",
                "-d",
                "somedesc",
                "-c",
                "57Ic6reczFn78H1DTaaXkp",
            ],
            output_format,
        )

        # Assert success
        assert out.exit_code == 0, f"Command failed: {out.stderr}"

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_browse(
        self,
        httpserver: HTTPServer,
        cli_runner,
        base_args,
        output_format: str,
    ) -> None:
        """Test data-links browse command."""
        datalink_describe_response = {
            "dataLink": {
                "id": "v1-cloud-9beb9b921331ce1427d23bd62ac4c4f7",
                "name": "adrian-navarro-test",
                "description": None,
                "resourceRef": "s3://adrian-navarro-test",
                "type": "bucket",
                "provider": "aws",
                "region": "us-east-1",
                "credentials": [
                    {"id": "5EXFx2R5zWuwYtpKHhCZ0X", "name": "seqera_aws", "provider": "aws"}
                ],
                "publicAccessible": False,
                "hidden": False,
                "status": None,
                "message": None,
            }
        }

        browse_response = {
            "originalPath": "s3://adrian-navarro-test/lambda_tutorial_adrian",
            "objects": [
                {"type": "FILE", "name": "test4.csv", "size": 1286, "mimeType": "text/csv"}
            ],
            "nextPageToken": None,
        }

        # Mock credentials fetch
        httpserver.expect_request(
            "/credentials",
            method="GET",
        ).respond_with_json(CREDENTIALS_RESPONSE)

        # Mock data-link describe
        httpserver.expect_request(
            "/data-links/v1-somedatalinkid",
            method="GET",
        ).respond_with_json(datalink_describe_response)

        # Mock browse
        httpserver.expect_request(
            "/data-links/v1-somedatalinkid/browse/path",
            method="GET",
        ).respond_with_json(browse_response)

        # Run command
        out = exec_command(
            cli_runner,
            base_args,
            [
                "data-links",
                "browse",
                "-w",
                "75887156211589",
                "-i",
                "v1-somedatalinkid",
                "-p",
                "path",
                "-c",
                "57Ic6reczFn78H1DTaaXkp",
                "-f",
                "name",
                "-t",
                "sometoken",
                "--page",
                "1",
            ],
            output_format,
        )

        # Assert success
        assert out.exit_code == 0, f"Command failed: {out.stderr}"

        if output_format == "json":
            output_data = json.loads(out.stdout)
            assert "objects" in output_data
            assert len(output_data["objects"]) == 1
            assert output_data["objects"][0]["name"] == "test4.csv"


class TestBuildSearchParameter:
    """Test the search parameter building logic."""

    def test_build_search_with_all_params(self) -> None:
        """Test building search with all parameters."""
        from seqera.commands.datalinks import _build_search

        result = _build_search(
            "bucket-name", "aws", "eu-west-1", "s3://some-bucket/is/path"
        )
        assert result is not None
        assert "bucket-name" in result
        assert "provider:aws" in result
        assert "region:eu-west-1" in result
        assert "resourceRef:s3://some-bucket/is/path" in result

    def test_build_search_with_some_params(self) -> None:
        """Test building search with some parameters."""
        from seqera.commands.datalinks import _build_search

        result = _build_search(None, "aws,azure", "eu-west-1", None)
        assert result is not None
        assert "provider:aws,azure" in result
        assert "region:eu-west-1" in result

    def test_build_search_with_provider_only(self) -> None:
        """Test building search with provider only."""
        from seqera.commands.datalinks import _build_search

        result = _build_search(None, "google", None, None)
        assert result == "provider:google"

    def test_build_search_with_no_params(self) -> None:
        """Test building search with no parameters."""
        from seqera.commands.datalinks import _build_search

        result = _build_search(None, None, None, None)
        assert result is None
