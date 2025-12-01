"""
Tests for credentials list and delete commands.

Ported from CredentialsCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestCredentialsCmd:
    """Test credentials list and delete commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test deleting credentials.

        Ported from testDelete() in CredentialsCmdTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials/1cz5A8cuBkB5iJliCwJCFU",
            method="DELETE",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "credentials",
            "delete",
            "-i",
            "1cz5A8cuBkB5iJliCwJCFU",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout
            assert "deleted" in out.stdout.lower()

    def test_delete_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test deleting non-existent credentials.

        Ported from testDeleteNotFound() in CredentialsCmdTest.java
        """
        # Setup mock HTTP expectation - 403 returned for not found
        httpserver.expect_request(
            "/credentials/1cz5A8cuBkB5iKKiCwJCFU",
            method="DELETE",
        ).respond_with_data("", status=403)

        # Run the command
        out = exec_cmd(
            "credentials",
            "delete",
            "-i",
            "1cz5A8cuBkB5iKKiCwJCFU",
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
        Test listing credentials.

        Ported from testList() in CredentialsCmdTest.java
        """
        # Setup mock HTTP expectations
        credentials_response = {
            "credentials": [
                {
                    "id": "2ba2oekqeTEBzwSDgXg7xf",
                    "name": "ssh",
                    "description": None,
                    "provider": "ssh",
                    "baseUrl": None,
                    "category": None,
                    "deleted": None,
                    "lastUsed": "2021-09-06T08:53:51Z",
                    "dateCreated": "2021-09-06T06:54:53Z",
                    "lastUpdated": "2021-09-06T06:54:53Z",
                },
                {
                    "id": "57Ic6reczFn78H1DTaaXkp",
                    "name": "azure",
                    "description": None,
                    "provider": "azure",
                    "baseUrl": None,
                    "category": None,
                    "deleted": None,
                    "lastUsed": None,
                    "dateCreated": "2021-09-07T13:50:21Z",
                    "lastUpdated": "2021-09-07T13:50:21Z",
                },
            ]
        }

        user_response = {
            "needConsent": False,
            "user": {
                "avatar": "https://www.gravatar.com/avatar/KDFSAJDKFJDSKFJK?d=404",
                "dateCreated": "2020-12-01T10:43:51Z",
                "deleted": False,
                "description": None,
                "email": "jordi@seqera.io",
                "firstName": None,
                "id": 1264,
                "lastAccess": "2021-07-21T10:30:25Z",
                "lastName": None,
                "lastUpdated": "2021-07-21T10:30:25Z",
                "marketingConsent": True,
                "notification": None,
                "options": {
                    "githubToken": "gho_UtDGKFASDFDSHQRevdRDQ548gnmB",
                    "maxRuns": None,
                },
                "organization": None,
                "termsOfUseConsent": True,
                "trusted": True,
                "userName": "jordi",
            },
        }

        httpserver.expect_request("/credentials", method="GET").respond_with_json(
            credentials_response, status=200
        )

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "list",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceRef"] == user_workspace_name
            assert len(data["credentials"]) == 2
            assert data["credentials"][0]["id"] == "2ba2oekqeTEBzwSDgXg7xf"
            assert data["credentials"][0]["name"] == "ssh"
            assert data["credentials"][0]["provider"] == "ssh"
            assert data["credentials"][1]["id"] == "57Ic6reczFn78H1DTaaXkp"
            assert data["credentials"][1]["name"] == "azure"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceRef"] == user_workspace_name
            assert len(data["credentials"]) == 2
            assert data["credentials"][0]["id"] == "2ba2oekqeTEBzwSDgXg7xf"
            assert data["credentials"][0]["name"] == "ssh"
        else:  # console
            assert user_workspace_name in out.stdout
            assert "ssh" in out.stdout
            assert "azure" in out.stdout
            assert "2ba2oekqeTEBzwSDgXg7xf" in out.stdout
            assert "57Ic6reczFn78H1DTaaXkp" in out.stdout

    def test_list_empty(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test listing credentials with empty result.

        Ported from testListEmpty() in CredentialsCmdTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request("/credentials", method="GET").respond_with_json(
            {"credentials": []}, status=200
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
        out = exec_cmd("credentials", "list")

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert user_workspace_name in out.stdout
        assert "No credentials found" in out.stdout or len(out.stdout.strip()) == 0

    def test_invalid_auth(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test delete with invalid authentication.

        Ported from testInvalidAuth() in CredentialsCmdTest.java
        """
        # Setup mock HTTP expectation - 401 for invalid auth
        httpserver.expect_request(
            "/credentials/1cz5A8cuBkB5iJliCwJCFT",
            method="DELETE",
        ).respond_with_data("Unauthorized", status=401)

        # Run the command
        out = exec_cmd(
            "credentials",
            "delete",
            "-i",
            "1cz5A8cuBkB5iJliCwJCFT",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert (
            "401" in out.stderr
            or "Unauthorized" in out.stderr
            or "authentication" in out.stderr.lower()
        )
