"""
Tests for teams commands.

Ported from TeamsCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestTeamsCmd:
    """Test teams commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing teams.

        Ported from testList() in TeamsCmdTest.java
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
            ]
        }

        teams_response = {
            "teams": [
                {
                    "teamId": 249211453903161,
                    "name": "team-test-3",
                    "description": "AAAAAA",
                    "avatarUrl": None,
                    "membersCount": 0,
                },
                {
                    "teamId": 69076469523589,
                    "name": "team-test-1",
                    "description": "a new team",
                    "avatarUrl": None,
                    "membersCount": 0,
                },
                {
                    "teamId": 255717345477198,
                    "name": "team-test-2",
                    "description": "a new team",
                    "avatarUrl": None,
                    "membersCount": 0,
                },
                {
                    "teamId": 267477500890054,
                    "name": "team1",
                    "description": "Team 1",
                    "avatarUrl": None,
                    "membersCount": 1,
                },
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request("/orgs/27736513644467/teams", method="GET").respond_with_json(
            teams_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "teams",
            "list",
            "-o",
            "organization1",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["organization"] == "organization1"
            assert len(data["teams"]) == 4
            assert data["teams"][0]["teamId"] == 249211453903161
            assert data["teams"][0]["name"] == "team-test-3"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["organization"] == "organization1"
            assert len(data["teams"]) == 4
        else:  # console
            assert "organization1" in out.stdout
            assert "team-test-3" in out.stdout
            assert "team1" in out.stdout

    def test_list_with_offset(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing teams with offset pagination.

        Ported from testListWithOffset() in TeamsCmdTest.java
        """
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
            ]
        }

        teams_response = {
            "teams": [
                {
                    "teamId": 69076469523589,
                    "name": "team-test-1",
                    "description": "a new team",
                    "avatarUrl": None,
                    "membersCount": 0,
                },
                {
                    "teamId": 255717345477198,
                    "name": "team-test-2",
                    "description": "a new team",
                    "avatarUrl": None,
                    "membersCount": 0,
                },
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request(
            "/orgs/27736513644467/teams", method="GET", query_string="offset=1&max=2"
        ).respond_with_json(teams_response, status=200)

        # Run the command
        out = exec_cmd(
            "teams",
            "list",
            "-o",
            "organization1",
            "--offset",
            "1",
            "--max",
            "2",
            output_format="console",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "team-test-1" in out.stdout
        assert "offset 1" in out.stdout

    def test_list_with_page(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing teams with page pagination.

        Ported from testListWithPage() in TeamsCmdTest.java
        """
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
            ]
        }

        teams_response = {
            "teams": [
                {
                    "teamId": 249211453903161,
                    "name": "team-test-3",
                    "description": "AAAAAA",
                    "avatarUrl": None,
                    "membersCount": 0,
                },
                {
                    "teamId": 69076469523589,
                    "name": "team-test-1",
                    "description": "a new team",
                    "avatarUrl": None,
                    "membersCount": 0,
                },
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request(
            "/orgs/27736513644467/teams", method="GET", query_string="offset=0&max=2"
        ).respond_with_json(teams_response, status=200)

        # Run the command
        out = exec_cmd(
            "teams",
            "list",
            "-o",
            "organization1",
            "--page",
            "1",
            "--max",
            "2",
            output_format="console",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "team-test-3" in out.stdout
        assert "page 1" in out.stdout

    def test_list_with_conflicting_pageable(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing teams with conflicting pagination parameters.

        Ported from testListWithConflictingPageable() in TeamsCmdTest.java
        """
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
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        # Run the command (should fail with conflicting params)
        out = exec_cmd(
            "teams",
            "list",
            "-o",
            "organization1",
            "--page",
            "1",
            "--offset",
            "0",
            "--max",
            "2",
            output_format="console",
        )

        # Assertions
        assert out.exit_code == 1
        assert "Please use either --page or --offset" in out.stderr

    def test_list_empty(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing teams when there are none.

        Ported from testListEmpty() in TeamsCmdTest.java
        """
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
            ]
        }

        teams_response = {"teams": []}

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request("/orgs/27736513644467/teams", method="GET").respond_with_json(
            teams_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "teams",
            "list",
            "-o",
            "organization1",
            output_format="console",
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "No teams found" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding a team.

        Ported from testAdd() in TeamsCmdTest.java
        """
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
            ]
        }

        team_add_response = {
            "team": {
                "teamId": 249211453903161,
                "name": "team-test",
                "description": None,
                "avatarUrl": None,
                "membersCount": 0,
            }
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request("/orgs/27736513644467/teams", method="POST").respond_with_json(
            team_add_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "teams",
            "add",
            "-o",
            "organization1",
            "-n",
            "team-test",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["organization"] == "organization1"
            assert data["teamName"] == "team-test"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["organization"] == "organization1"
            assert data["teamName"] == "team-test"
        else:  # console
            assert "team-test" in out.stdout
            assert "organization1" in out.stdout

    def test_add_with_organization_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test adding a team when organization is not found.

        Ported from testAddWithOrganizationNotFound() in TeamsCmdTest.java
        """
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
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "teams",
            "add",
            "-o",
            "organization-not-found",
            "-n",
            "team-test",
            output_format="console",
        )

        # Assertions
        assert out.exit_code == 1
        assert "organization-not-found" in out.stderr

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_with_overwrite(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding a team with overwrite flag.

        Ported from testAddWithOverwrite() in TeamsCmdTest.java
        """
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
            ]
        }

        teams_response = {
            "teams": [
                {
                    "teamId": 249211453903161,
                    "name": "team-test-3",
                    "description": "AAAAAA",
                    "avatarUrl": None,
                    "membersCount": 0,
                },
            ]
        }

        team_add_response = {
            "team": {
                "teamId": 249211453903161,
                "name": "team-test-3",
                "description": None,
                "avatarUrl": None,
                "membersCount": 0,
            }
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request("/orgs/27736513644467/teams", method="GET").respond_with_json(
            teams_response, status=200
        )

        httpserver.expect_request(
            "/orgs/27736513644467/teams/249211453903161", method="DELETE"
        ).respond_with_data("", status=200)

        httpserver.expect_request("/orgs/27736513644467/teams", method="POST").respond_with_json(
            team_add_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "teams",
            "add",
            "-o",
            "organization1",
            "-n",
            "team-test-3",
            "--overwrite",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete_team(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting a team.

        Ported from testDeleteTeam() in TeamsCmdTest.java
        """
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
            ]
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request(
            "/orgs/27736513644467/teams/69076469523589", method="DELETE"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "teams",
            "delete",
            "-o",
            "organization1",
            "-i",
            "69076469523589",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["organization"] == "organization1"
            assert data["teamRef"] == "69076469523589"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["organization"] == "organization1"
        else:  # console
            assert "69076469523589" in out.stdout
            assert "organization1" in out.stdout
