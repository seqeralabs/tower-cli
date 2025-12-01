"""
Tests for collaborators commands.

Ported from CollaboratorsCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestCollaboratorsCmd:
    """Test collaborators commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing organization collaborators.

        Ported from testListCollaborators() in CollaboratorsCmdTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "jordi",
                    "email": "jordi@seqera.io",
                }
            }
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": None,
                        "workspaceName": None,
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/collaborators",
            method="GET",
            query_string="max=100&offset=0",
        ).respond_with_json(
            {
                "members": [
                    {
                        "memberId": 175703974560466,
                        "userName": "jfernandez74",
                        "email": "jfernandez74@gmail.com",
                        "firstName": None,
                        "lastName": None,
                        "avatar": "https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404",
                        "role": "collaborator",
                    },
                    {
                        "memberId": 255080245994226,
                        "userName": "julio",
                        "email": "julio@seqera.io",
                        "firstName": None,
                        "lastName": None,
                        "avatar": "https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404",
                        "role": "collaborator",
                    },
                ],
                "totalSize": 2,
            }
        )

        # Run the command
        out = exec_cmd("collaborators", "list", "-o", "27736513644467", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "console":
            # Check console output contains expected information
            # Note: Email addresses may be truncated in table display
            assert "jfernandez74" in out.stdout
            assert "julio" in out.stdout
            assert "Collaborators in organization" in out.stdout
            assert "collaborator" in out.stdout
        elif output_format == "json":
            # Parse and validate JSON output
            output_data = json.loads(out.stdout)
            assert output_data["orgId"] == 27736513644467
            assert len(output_data["collaborators"]) == 2
            assert output_data["collaborators"][0]["userName"] == "jfernandez74"
            assert output_data["collaborators"][1]["userName"] == "julio"
        elif output_format == "yaml":
            # Check YAML output contains expected keys
            assert "orgId: 27736513644467" in out.stdout
            assert "jfernandez74" in out.stdout
            assert "julio" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding a collaborator to an organization.
        """
        # Setup mock HTTP expectations
        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "jordi",
                    "email": "jordi@seqera.io",
                }
            }
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": None,
                        "workspaceName": None,
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/collaborators/add",
            method="PUT",
        ).respond_with_json(
            {
                "member": {
                    "memberId": 175703974560466,
                    "userName": "jfernandez74",
                    "email": "jfernandez74@gmail.com",
                    "firstName": None,
                    "lastName": None,
                    "avatar": "https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404",
                    "role": "collaborator",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "collaborators",
            "add",
            "-o",
            "27736513644467",
            "-u",
            "jfernandez74",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "console":
            # Check console output contains expected information
            assert "jfernandez74" in out.stdout
            assert "collaborator" in out.stdout.lower()
        elif output_format == "json":
            # Parse and validate JSON output
            output_data = json.loads(out.stdout)
            assert output_data["userName"] == "jfernandez74"
            assert output_data["orgId"] == 27736513644467
        elif output_format == "yaml":
            # Check YAML output contains expected keys
            assert "userName: jfernandez74" in out.stdout
            assert "orgId: 27736513644467" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting a collaborator from an organization.
        """
        # Setup mock HTTP expectations
        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "jordi",
                    "email": "jordi@seqera.io",
                }
            }
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": None,
                        "workspaceName": None,
                    },
                ]
            }
        )

        # First get the list of collaborators to find the member ID
        httpserver.expect_request(
            "/orgs/27736513644467/collaborators",
            method="GET",
            query_string="search=jfernandez74",
        ).respond_with_json(
            {
                "members": [
                    {
                        "memberId": 175703974560466,
                        "userName": "jfernandez74",
                        "email": "jfernandez74@gmail.com",
                        "firstName": None,
                        "lastName": None,
                        "avatar": "https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404",
                        "role": "collaborator",
                    },
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/collaborators/175703974560466",
            method="DELETE",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "collaborators",
            "delete",
            "-o",
            "27736513644467",
            "-u",
            "jfernandez74",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "console":
            # Check console output contains expected information
            assert "jfernandez74" in out.stdout
            assert "deleted" in out.stdout.lower() or "removed" in out.stdout.lower()
        elif output_format == "json":
            # Parse and validate JSON output
            output_data = json.loads(out.stdout)
            assert output_data["userName"] == "jfernandez74"
            assert output_data["orgId"] == 27736513644467
        elif output_format == "yaml":
            # Check YAML output contains expected keys
            assert "userName: jfernandez74" in out.stdout
            assert "orgId: 27736513644467" in out.stdout
