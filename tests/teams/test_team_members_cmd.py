"""
Tests for team members commands.

Ported from TeamMembersCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestTeamMembersCmd:
    """Test team members commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing team members.

        Ported from testList() in TeamMembersCmdTest.java
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
                    "teamId": 267477500890054,
                    "name": "team1",
                    "description": "Team 1",
                    "avatarUrl": None,
                    "membersCount": 1,
                },
            ]
        }

        members_response = {
            "members": [
                {
                    "memberId": 80726606082762,
                    "userName": "julio2",
                    "email": "julio2@seqera.io",
                    "firstName": None,
                    "lastName": None,
                    "avatar": None,
                    "role": "member",
                },
                {
                    "memberId": 142050384398323,
                    "userName": "julio789",
                    "email": "julio789@seqera.io",
                    "firstName": None,
                    "lastName": None,
                    "avatar": None,
                    "role": "member",
                },
                {
                    "memberId": 199966343791197,
                    "userName": "julio7899",
                    "email": "julio7899@seqera.io",
                    "firstName": None,
                    "lastName": None,
                    "avatar": None,
                    "role": "member",
                },
            ],
            "totalSize": 3,
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467/teams", method="GET"
        ).respond_with_json(teams_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467/teams/267477500890054/members", method="GET"
        ).respond_with_json(members_response, status=200)

        # Run the command
        out = exec_cmd(
            "teams",
            "members",
            "list",
            "-o",
            "organization1",
            "-t",
            "team1",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["teamName"] == "team1"
            assert len(data["members"]) == 3
            assert data["members"][0]["userName"] == "julio2"
            assert data["members"][0]["email"] == "julio2@seqera.io"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["teamName"] == "team1"
            assert len(data["members"]) == 3
        else:  # console
            assert "team1" in out.stdout
            assert "julio2" in out.stdout
            assert "julio789" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding a member to a team.

        Ported from testAdd() in TeamMembersCmdTest.java
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
                    "teamId": 267477500890054,
                    "name": "team1",
                    "description": "Team 1",
                    "avatarUrl": None,
                    "membersCount": 1,
                },
            ]
        }

        member_add_response = {
            "member": {
                "memberId": 42005399330152,
                "userName": "abc",
                "email": "abc@seqera.io",
                "firstName": None,
                "lastName": None,
                "avatar": None,
                "role": "member",
            }
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467/teams", method="GET"
        ).respond_with_json(teams_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467/teams/267477500890054/members", method="POST"
        ).respond_with_json(member_add_response, status=200)

        # Run the command
        out = exec_cmd(
            "teams",
            "members",
            "add",
            "-o",
            "organization1",
            "-t",
            "team1",
            "-m",
            "abc@seqera.io",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["teamName"] == "team1"
            assert data["member"]["userName"] == "abc"
            assert data["member"]["email"] == "abc@seqera.io"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["teamName"] == "team1"
            assert data["member"]["userName"] == "abc"
        else:  # console
            assert "team1" in out.stdout
            assert "abc" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting a member from a team.

        Ported from testDelete() in TeamMembersCmdTest.java
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
                    "teamId": 267477500890054,
                    "name": "team1",
                    "description": "Team 1",
                    "avatarUrl": None,
                    "membersCount": 1,
                },
            ]
        }

        members_response = {
            "members": [
                {
                    "memberId": 80726606082762,
                    "userName": "julio2",
                    "email": "julio2@seqera.io",
                    "firstName": None,
                    "lastName": None,
                    "avatar": None,
                    "role": "member",
                },
            ],
            "totalSize": 1,
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request(
            "/user/1264/workspaces", method="GET"
        ).respond_with_json(workspaces_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467/teams", method="GET"
        ).respond_with_json(teams_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467/teams/267477500890054/members", method="GET"
        ).respond_with_json(members_response, status=200)

        httpserver.expect_request(
            "/orgs/27736513644467/teams/267477500890054/members/80726606082762/delete",
            method="DELETE",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "teams",
            "members",
            "delete",
            "-o",
            "organization1",
            "-t",
            "team1",
            "-m",
            "julio2",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["teamName"] == "team1"
            assert data["memberRef"] == "julio2"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["teamName"] == "team1"
        else:  # console
            assert "team1" in out.stdout
            assert "julio2" in out.stdout
