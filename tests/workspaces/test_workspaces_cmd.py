"""
Tests for workspaces commands.

Ported from WorkspacesCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestWorkspacesCmd:
    """Test workspaces commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing workspaces.

        Ported from testList() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 37736513644467,
                        "orgName": "organization2",
                        "orgLogoUrl": None,
                        "workspaceId": None,
                        "workspaceName": None,
                    },
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                    {
                        "orgId": 37736513644467,
                        "orgName": "organization2",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211590,
                        "workspaceName": "workspace2",
                    },
                ]
            }
        )

        # Run the command
        out = exec_cmd("workspaces", "list", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["userName"] == "jordi"
            assert len(data["workspaces"]) == 2
            assert data["workspaces"][0]["workspaceName"] == "workspace1"
            assert data["workspaces"][1]["workspaceName"] == "workspace2"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["userName"] == "jordi"
            assert len(data["workspaces"]) == 2
            assert data["workspaces"][0]["workspaceName"] == "workspace1"
        else:  # console
            assert "workspace1" in out.stdout
            assert "workspace2" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list_by_organization(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing workspaces filtered by organization.

        Ported from testListByOrganization() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 37736513644467,
                        "orgName": "organization2",
                        "orgLogoUrl": None,
                        "workspaceId": None,
                        "workspaceName": None,
                    },
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                    {
                        "orgId": 37736513644467,
                        "orgName": "organization2",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211590,
                        "workspaceName": "workspace2",
                    },
                ]
            }
        )

        # Run the command
        out = exec_cmd("workspaces", "list", "-o", "organization1", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["userName"] == "jordi"
            assert len(data["workspaces"]) == 1
            assert data["workspaces"][0]["workspaceName"] == "workspace1"
            assert data["workspaces"][0]["orgName"] == "organization1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["userName"] == "jordi"
            assert len(data["workspaces"]) == 1
            assert data["workspaces"][0]["workspaceName"] == "workspace1"
        else:  # console
            assert "workspace1" in out.stdout
            assert "workspace2" not in out.stdout

    def test_list_empty(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing workspaces when there are none.

        Ported from testListEmpty() in WorkspacesCmdTest.java
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
            {"orgsAndWorkspaces": []}
        )

        # Run the command
        out = exec_cmd("workspaces", "list")

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting workspace by ID.

        Ported from testDeleteById() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589", method="DELETE"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd("workspaces", "delete", "-i", "75887156211589", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceName"] == "workspace1"
            assert data["orgName"] == "organization1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceName"] == "workspace1"
            assert data["orgName"] == "organization1"
        else:  # console
            assert "workspace1" in out.stdout
            assert "deleted" in out.stdout.lower()

    def test_delete_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test deleting non-existent workspace.

        Ported from testDeleteNotFound() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589", method="DELETE"
        ).respond_with_data("", status=204)

        # Run the command - different ID that doesn't exist
        out = exec_cmd("workspaces", "delete", "-i", "7588715621158")

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "not found" in out.stderr.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_view_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test viewing workspace by ID.

        Ported from testViewById() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589", method="GET"
        ).respond_with_json(
            {
                "workspace": {
                    "id": 75887156211589,
                    "name": "workspace1",
                    "fullName": "workspace 1",
                    "description": "Workspace description",
                    "visibility": "PRIVATE",
                    "dateCreated": "2021-09-21T12:54:03Z",
                    "lastUpdated": "2021-09-21T12:54:03Z",
                }
            }
        )

        # Run the command
        out = exec_cmd("workspaces", "view", "-i", "75887156211589", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspace"]["id"] == 75887156211589
            assert data["workspace"]["name"] == "workspace1"
            assert data["workspace"]["visibility"] == "PRIVATE"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspace"]["id"] == 75887156211589
            assert data["workspace"]["name"] == "workspace1"
        else:  # console
            assert "workspace1" in out.stdout
            assert "PRIVATE" in out.stdout

    def test_view_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test viewing non-existent workspace.

        Ported from testViewNotFound() in WorkspacesCmdTest.java
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
            {"orgsAndWorkspaces": []}
        )

        # Run the command
        out = exec_cmd("workspaces", "view", "-i", "7588715621158")

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "not found" in out.stderr.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding a new workspace.

        Ported from testAdd() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/validate", method="GET", query_string="name=wspNew"
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces", method="POST"
        ).respond_with_json(
            {
                "workspace": {
                    "id": 52957572657821,
                    "name": "wspNew",
                    "fullName": "wsp-new",
                    "description": "workspace description",
                    "visibility": "PRIVATE",
                    "dateCreated": "2021-09-28T07:15:26.696627Z",
                    "lastUpdated": "2021-09-28T07:15:26.696627Z",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "workspaces",
            "add",
            "-n",
            "wspNew",
            "-o",
            "organization1",
            "-f",
            "wsp-new",
            "-d",
            "workspace description",
            "-v",
            "PRIVATE",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceName"] == "wspNew"
            assert data["orgName"] == "organization1"
            assert data["visibility"] == "PRIVATE"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceName"] == "wspNew"
            assert data["orgName"] == "organization1"
        else:  # console
            assert "wspNew" in out.stdout
            assert "organization1" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_with_overwrite(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding workspace with overwrite flag.

        Ported from testAddWithOverwrite() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/validate", method="GET", query_string="name=workspace1"
        ).respond_with_data("", status=204)

        # SDK get() calls GET to get full workspace details when checking for overwrite
        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589", method="GET"
        ).respond_with_json(
            {
                "workspace": {
                    "id": 75887156211589,
                    "name": "workspace1",
                    "fullName": "workspace 1",
                    "description": "Workspace description",
                    "visibility": "PRIVATE",
                }
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589", method="DELETE"
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces", method="POST"
        ).respond_with_json(
            {
                "workspace": {
                    "id": 52957572657821,
                    "name": "workspace1",
                    "fullName": "wsp-one",
                    "description": "workspace description",
                    "visibility": "PRIVATE",
                    "dateCreated": "2021-09-28T07:15:26.696627Z",
                    "lastUpdated": "2021-09-28T07:15:26.696627Z",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "workspaces",
            "add",
            "--overwrite",
            "-n",
            "workspace1",
            "-o",
            "organization1",
            "-f",
            "wsp-one",
            "-d",
            "workspace description",
            "-v",
            "PRIVATE",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceName"] == "workspace1"
            assert data["orgName"] == "organization1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceName"] == "workspace1"
        else:  # console
            assert "workspace1" in out.stdout

    def test_add_organization_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test adding workspace to non-existent organization.

        Ported from testAddOrganizationNotFound() in WorkspacesCmdTest.java
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
            {"orgsAndWorkspaces": []}
        )

        # Run the command
        out = exec_cmd(
            "workspaces",
            "add",
            "-n",
            "wspNew",
            "-o",
            "organization1",
            "-f",
            "wsp-new",
            "-d",
            "workspace description",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "not found" in out.stderr.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test updating workspace by ID.

        Ported from testUpdateById() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589", method="GET"
        ).respond_with_json(
            {
                "workspace": {
                    "id": 75887156211589,
                    "name": "workspace1",
                    "fullName": "workspace 1",
                    "description": "Workspace description",
                    "visibility": "PRIVATE",
                    "dateCreated": "2021-09-21T12:54:03Z",
                    "lastUpdated": "2021-09-21T12:54:03Z",
                }
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589", method="PUT"
        ).respond_with_json(
            {
                "workspace": {
                    "id": 75887156211589,
                    "name": "workspace2",
                    "fullName": "workspace-1",
                    "description": "workspace description",
                    "visibility": "PRIVATE",
                    "dateCreated": "2021-09-28T07:15:26.696627Z",
                    "lastUpdated": "2021-09-28T07:15:26.696627Z",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "workspaces",
            "update",
            "-i",
            "75887156211589",
            "-f",
            "wsp-new",
            "-d",
            "workspace description",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceName"] == "workspace1"
            assert data["orgName"] == "organization1"
            assert data["visibility"] == "PRIVATE"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceName"] == "workspace1"
            assert data["orgName"] == "organization1"
        else:  # console
            assert "workspace1" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update_name_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test updating workspace name by ID.

        Ported from testUpdateNameById() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589", method="GET"
        ).respond_with_json(
            {
                "workspace": {
                    "id": 75887156211589,
                    "name": "workspace1",
                    "fullName": "workspace 1",
                    "description": "Workspace description",
                    "visibility": "PRIVATE",
                    "dateCreated": "2021-09-21T12:54:03Z",
                    "lastUpdated": "2021-09-21T12:54:03Z",
                }
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589", method="PUT"
        ).respond_with_json(
            {
                "workspace": {
                    "id": 75887156211589,
                    "name": "workspace2",
                    "fullName": "wsp-new",
                    "description": "workspace description",
                    "visibility": "PRIVATE",
                    "dateCreated": "2021-09-28T07:15:26.696627Z",
                    "lastUpdated": "2021-09-28T07:15:26.696627Z",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "workspaces",
            "update",
            "-i",
            "75887156211589",
            "--new-name",
            "workspace2",
            "-f",
            "wsp-new",
            "-d",
            "workspace description",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceName"] == "workspace1"
            assert data["orgName"] == "organization1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceName"] == "workspace1"
        else:  # console
            assert "workspace1" in out.stdout

    def test_update_workspace_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test updating non-existent workspace.

        Ported from testUpdateWorkspaceNotFound() in WorkspacesCmdTest.java
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
            {"orgsAndWorkspaces": []}
        )

        # Run the command
        out = exec_cmd(
            "workspaces",
            "update",
            "-i",
            "7588715621158",
            "-f",
            "wsp-new",
            "-d",
            "workspace description",
        )

        # Assertions - should fail
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "not found" in out.stderr.lower()

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_leave_workspace_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test leaving workspace as participant by ID.

        Ported from testLeaveWorkspaceAsParticipantById() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants", method="DELETE"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd("workspaces", "leave", "-i", "75887156211589", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceName"] == "workspace1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceName"] == "workspace1"
        else:  # console
            assert "workspace1" in out.stdout

    def test_leave_workspace_by_workspace_reference(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test leaving workspace using workspace reference.

        Ported from leaveWorkspaceAsParticipantByIdAndWorkspaceReference() in WorkspacesCmdTest.java
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
                    {
                        "orgId": 27736513644467,
                        "orgName": "organization1",
                        "orgLogoUrl": None,
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants", method="DELETE"
        ).respond_with_data("", status=204)

        # Run the command with workspace reference format
        out = exec_cmd("workspaces", "leave", "-n", "organization1/workspace1")

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "workspace1" in out.stdout
