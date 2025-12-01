"""
Tests for organizations commands.

Ported from OrganizationsCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestOrganizationsCmd:
    """Test organizations commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing organizations.

        Ported from testList() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 27736513644467,
                    "orgName": "organization1",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                },
                {
                    "orgId": 37736513644467,
                    "orgName": "organization2",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                },
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        # Run the command
        out = exec_cmd(
            "organizations",
            "list",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["userName"] == "jordi"
            assert len(data["organizations"]) == 2
            assert data["organizations"][0]["orgId"] == 27736513644467
            assert data["organizations"][0]["orgName"] == "organization1"
            assert data["organizations"][1]["orgId"] == 37736513644467
            assert data["organizations"][1]["orgName"] == "organization2"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["userName"] == "jordi"
            assert len(data["organizations"]) == 2
            assert data["organizations"][0]["orgId"] == 27736513644467
            assert data["organizations"][0]["orgName"] == "organization1"
        else:  # console
            assert "jordi" in out.stdout
            assert "organization1" in out.stdout
            assert "organization2" in out.stdout

    def test_list_empty(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing organizations with empty result.

        Ported from testListEmpty() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {"orgsAndWorkspaces": []}

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        # Run the command
        out = exec_cmd("organizations", "list")

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "jordi" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_view(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test viewing organization details.

        Ported from testView() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 27736513644467,
                    "orgName": "organization1",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                }
            ]
        }

        org_response = {
            "organization": {
                "orgId": 27736513644467,
                "name": "organization1",
                "fullName": "organization1",
                "description": None,
                "location": None,
                "website": None,
                "logoId": None,
                "logoUrl": None,
                "memberId": None,
                "memberRole": None,
            }
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467", method="GET"
        ).respond_with_json(org_response, status=200)

        # Run the command
        out = exec_cmd(
            "organizations",
            "view",
            "-n",
            "organization1",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["orgId"] == 27736513644467
            assert data["name"] == "organization1"
            assert data["fullName"] == "organization1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["orgId"] == 27736513644467
            assert data["name"] == "organization1"
        else:  # console
            assert "organization1" in out.stdout
            assert "27736513644467" in out.stdout

    def test_view_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test viewing non-existent organization.

        Ported from testViewNotFound() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 27736513644467,
                    "orgName": "organization1",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                }
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        # Run the command
        out = exec_cmd("organizations", "view", "-n", "organization11")

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "organization11" in out.stderr
        assert "not found" in out.stderr.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting organization.

        Ported from testDelete() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 27736513644467,
                    "orgName": "organization1",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                }
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467", method="DELETE"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "organizations",
            "delete",
            "-n",
            "organization1",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["organizationName"] == "organization1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["organizationName"] == "organization1"
        else:  # console
            assert "organization1" in out.stdout
            assert "deleted" in out.stdout.lower()

    def test_delete_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test deleting non-existent organization.

        Ported from testDeleteNotFound() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 27736513644467,
                    "orgName": "organization1",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                }
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        # Run the command
        out = exec_cmd("organizations", "delete", "-n", "organization11")

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "organization11" in out.stderr
        assert "not found" in out.stderr.lower()

    def test_delete_error(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test deleting organization with server error.

        Ported from testDeleteError() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 27736513644467,
                    "orgName": "organization1",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                }
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467", method="DELETE"
        ).respond_with_data("", status=500)

        # Run the command
        out = exec_cmd("organizations", "delete", "-n", "organization1")

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "organization1" in out.stderr
        assert ("could not be deleted" in out.stderr.lower() or "500" in out.stderr)

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding organization.

        Ported from testAdd() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        org_response = {
            "organization": {
                "orgId": 275484385882108,
                "name": "sample-organization",
                "fullName": "sample organization",
                "description": "sample organization description",
                "location": "office",
                "website": "http://www.seqera.io",
                "logoId": None,
                "logoUrl": None,
                "memberId": None,
                "memberRole": None,
            }
        }

        httpserver.expect_request("/orgs", method="POST").respond_with_json(
            org_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "organizations",
            "add",
            "-n",
            "sample-organization",
            "-f",
            "sample organization",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["orgId"] == 275484385882108
            assert data["name"] == "sample-organization"
            assert data["fullName"] == "sample organization"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["orgId"] == 275484385882108
            assert data["name"] == "sample-organization"
        else:  # console
            assert "sample-organization" in out.stdout
            assert "275484385882108" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_with_overwrite(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding organization with overwrite.

        Ported from testAddWithOverwrite() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 275484385882108,
                    "orgName": "sample-organization",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                }
            ]
        }

        org_response = {
            "organization": {
                "orgId": 275484385882108,
                "name": "sample-organization",
                "fullName": "sample organization",
                "description": "sample organization description",
                "location": "office",
                "website": "http://www.seqera.io",
                "logoId": None,
                "logoUrl": None,
                "memberId": None,
                "memberRole": None,
            }
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/275484385882108", method="DELETE"
        ).respond_with_data("", status=200)

        httpserver.expect_request("/orgs", method="POST").respond_with_json(
            org_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "organizations",
            "add",
            "--overwrite",
            "-n",
            "sample-organization",
            "-f",
            "sample organization",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["orgId"] == 275484385882108
            assert data["name"] == "sample-organization"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["orgId"] == 275484385882108
            assert data["name"] == "sample-organization"
        else:  # console
            assert "sample-organization" in out.stdout

    def test_add_error(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test adding organization with server error.

        Ported from testAddError() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        httpserver.expect_request("/orgs", method="POST").respond_with_data(
            "", status=500
        )

        # Run the command
        out = exec_cmd(
            "organizations",
            "add",
            "-n",
            "sample-organization",
            "-f",
            "sample organization",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test updating organization.

        Ported from testUpdate() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 27736513644467,
                    "orgName": "organization1",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                }
            ]
        }

        org_response = {
            "organization": {
                "orgId": 27736513644467,
                "name": "organization1",
                "fullName": "organization1",
                "description": None,
                "location": None,
                "website": None,
                "logoId": None,
                "logoUrl": None,
                "memberId": None,
                "memberRole": None,
            }
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467", method="GET"
        ).respond_with_json(org_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467", method="PUT"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "organizations",
            "update",
            "-n",
            "organization1",
            "-f",
            "sample organization",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["orgId"] == 27736513644467
            assert data["organizationName"] == "organization1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["orgId"] == 27736513644467
            assert data["organizationName"] == "organization1"
        else:  # console
            assert "organization1" in out.stdout
            assert "updated" in out.stdout.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update_name(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test updating organization name.

        Ported from testUpdateName() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 27736513644467,
                    "orgName": "organization1",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                }
            ]
        }

        org_response = {
            "organization": {
                "orgId": 27736513644467,
                "name": "organization1",
                "fullName": "organization1",
                "description": None,
                "location": None,
                "website": None,
                "logoId": None,
                "logoUrl": None,
                "memberId": None,
                "memberRole": None,
            }
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467", method="GET"
        ).respond_with_json(org_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467", method="PUT"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "organizations",
            "update",
            "-n",
            "organization1",
            "--new-name",
            "organization2",
            "-f",
            "sample organization",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["orgId"] == 27736513644467
            assert data["organizationName"] == "organization1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["orgId"] == 27736513644467
            assert data["organizationName"] == "organization1"
        else:  # console
            assert "organization1" in out.stdout
            assert "updated" in out.stdout.lower()

    def test_update_error(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test updating organization with server error.

        Ported from testUpdateError() in OrganizationsCmdTest.java
        """
        # Setup mock HTTP expectations
        user_response = {
            "user": {
                "id": 1264,
                "userName": "jordi",
                "email": "jordi@seqera.io",
            }
        }

        workspaces_response = {
            "orgsAndWorkspaces": [
                {
                    "orgId": 27736513644467,
                    "orgName": "organization1",
                    "orgLogoUrl": None,
                    "workspaceId": None,
                    "workspaceName": None,
                }
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467", method="PUT"
        ).respond_with_data("", status=500)

        # Run the command
        out = exec_cmd(
            "organizations",
            "update",
            "-n",
            "organization1",
            "-f",
            "sample organization",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
