"""
Tests for participants commands.

Ported from ParticipantsCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestParticipantsCmd:
    """Test participants commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing workspace participants.

        Ported from testList() in ParticipantsCmdTest.java
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
            "/orgs/27736513644467/workspaces/75887156211589/participants", method="GET"
        ).respond_with_json(
            {
                "participants": [
                    {
                        "participantId": 48516118433516,
                        "memberId": 175703974560466,
                        "userName": "jfernandez74",
                        "firstName": None,
                        "lastName": None,
                        "email": "jfernandez74@gmail.com",
                        "orgRole": "owner",
                        "teamId": None,
                        "teamName": None,
                        "wspRole": "owner",
                        "type": "MEMBER",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": "https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404",
                    },
                    {
                        "participantId": 36791779798370,
                        "memberId": 255080245994226,
                        "userName": "julio",
                        "firstName": None,
                        "lastName": None,
                        "email": "julio@seqera.io",
                        "orgRole": "member",
                        "teamId": None,
                        "teamName": None,
                        "wspRole": "admin",
                        "type": "MEMBER",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": "https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404",
                    },
                    {
                        "participantId": 110330443206779,
                        "memberId": 80726606082762,
                        "userName": "julio2",
                        "firstName": None,
                        "lastName": None,
                        "email": "julio2@seqera.io",
                        "orgRole": "member",
                        "teamId": None,
                        "teamName": None,
                        "wspRole": "launch",
                        "type": "MEMBER",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": None,
                    },
                    {
                        "participantId": 110330443206780,
                        "memberId": 80726606082770,
                        "userName": "jordi",
                        "firstName": None,
                        "lastName": None,
                        "email": "jordi@seqera.io",
                        "orgRole": "member",
                        "teamId": None,
                        "teamName": None,
                        "wspRole": "launch",
                        "type": "MEMBER",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": None,
                    },
                    {
                        "participantId": 179548688376545,
                        "memberId": None,
                        "userName": None,
                        "firstName": None,
                        "lastName": None,
                        "email": None,
                        "orgRole": None,
                        "teamId": 255717345477198,
                        "teamName": "team-test-2",
                        "wspRole": "launch",
                        "type": "TEAM",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": None,
                    },
                ],
                "totalSize": 4,
            }
        )

        # Run the command
        out = exec_cmd("participants", "list", "-w", "75887156211589", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["orgName"] == "organization1"
            assert data["workspaceName"] == "workspace1"
            assert len(data["participants"]) == 5
            assert data["participants"][0]["userName"] == "jfernandez74"
            assert data["participants"][1]["userName"] == "julio"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["orgName"] == "organization1"
            assert data["workspaceName"] == "workspace1"
            assert len(data["participants"]) == 5
        else:  # console
            assert "organization1" in out.stdout
            assert "workspace1" in out.stdout
            # Names may be truncated in table output
            assert ("jfernandez74" in out.stdout or "jfernand" in out.stdout)
            assert "julio" in out.stdout

    def test_list_with_pagination(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing participants with pagination.

        Ported from testListWithOffset() in ParticipantsCmdTest.java
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
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants",
            method="GET",
            query_string="offset=1&max=2",
        ).respond_with_json(
            {
                "participants": [
                    {
                        "participantId": 36791779798370,
                        "memberId": 255080245994226,
                        "userName": "julio",
                        "firstName": None,
                        "lastName": None,
                        "email": "julio@seqera.io",
                        "orgRole": "member",
                        "teamId": None,
                        "teamName": None,
                        "wspRole": "admin",
                        "type": "MEMBER",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": None,
                    },
                ],
                "totalSize": 5,
            }
        )

        # Run the command
        out = exec_cmd("participants", "list", "-w", "75887156211589", "--offset", "1", "--max", "2")

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "julio" in out.stdout

    def test_list_filter_by_type(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing participants filtered by type (TEAM).

        Ported from testListTeam() in ParticipantsCmdTest.java
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
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants", method="GET"
        ).respond_with_json(
            {
                "participants": [
                    {
                        "participantId": 110330443206780,
                        "memberId": 80726606082770,
                        "userName": "jordi",
                        "firstName": None,
                        "lastName": None,
                        "email": "jordi@seqera.io",
                        "orgRole": "member",
                        "teamId": None,
                        "teamName": None,
                        "wspRole": "launch",
                        "type": "MEMBER",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": None,
                    },
                    {
                        "participantId": 179548688376545,
                        "memberId": None,
                        "userName": None,
                        "firstName": None,
                        "lastName": None,
                        "email": None,
                        "orgRole": None,
                        "teamId": 255717345477198,
                        "teamName": "team-test-2",
                        "wspRole": "launch",
                        "type": "TEAM",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": None,
                    },
                ],
                "totalSize": 2,
            }
        )

        # Run the command with type filter
        out = exec_cmd("participants", "list", "-w", "75887156211589", "-t", "TEAM")

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""
        assert "team-test-2" in out.stdout
        assert "jordi" not in out.stdout  # Should be filtered out

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete_member(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting a member participant.

        Ported from testDeleteMemberParticipant() in ParticipantsCmdTest.java
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
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants",
            method="GET",
            query_string="search=julio",
        ).respond_with_json(
            {
                "participants": [
                    {
                        "participantId": 36791779798370,
                        "memberId": 255080245994226,
                        "userName": "julio",
                        "firstName": None,
                        "lastName": None,
                        "email": "julio@seqera.io",
                        "orgRole": "member",
                        "teamId": None,
                        "teamName": None,
                        "wspRole": "admin",
                        "type": "MEMBER",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": None,
                    },
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants/36791779798370",
            method="DELETE",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "participants",
            "delete",
            "-w",
            "75887156211589",
            "-n",
            "julio",
            "-t",
            "MEMBER",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["name"] == "julio"
            assert data["workspaceName"] == "workspace1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["name"] == "julio"
            assert data["workspaceName"] == "workspace1"
        else:  # console
            assert "julio" in out.stdout
            assert "workspace1" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_leave(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test leaving a workspace.

        Ported from testLeave() in ParticipantsCmdTest.java
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
        out = exec_cmd("participants", "leave", "-w", "75887156211589", output_format=output_format)

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

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update_member_role(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test updating a member participant role.

        Ported from testUpdateMemberParticipantRole() in ParticipantsCmdTest.java
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
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants",
            method="GET",
            query_string="search=julio",
        ).respond_with_json(
            {
                "participants": [
                    {
                        "participantId": 36791779798370,
                        "memberId": 255080245994226,
                        "userName": "julio",
                        "firstName": None,
                        "lastName": None,
                        "email": "julio@seqera.io",
                        "orgRole": "member",
                        "teamId": None,
                        "teamName": None,
                        "wspRole": "admin",
                        "type": "MEMBER",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": None,
                    },
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants/36791779798370/role",
            method="PUT",
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "participants",
            "update",
            "-w",
            "75887156211589",
            "-n",
            "julio",
            "-r",
            "OWNER",
            "-t",
            "MEMBER",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceName"] == "workspace1"
            assert data["name"] == "julio"
            assert data["role"] == "OWNER"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceName"] == "workspace1"
            assert data["name"] == "julio"
            assert data["role"] == "OWNER"
        else:  # console
            assert "workspace1" in out.stdout
            assert "julio" in out.stdout
            assert "OWNER" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_member(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding a member participant.

        Ported from testAddMemberParticipantRole() in ParticipantsCmdTest.java
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
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        httpserver.expect_request("/orgs/27736513644467/members", method="GET").respond_with_json(
            {
                "members": [
                    {
                        "memberId": 255080245994226,
                        "userName": "julio",
                        "email": "julio@seqera.io",
                        "firstName": None,
                        "lastName": None,
                        "avatar": None,
                        "role": "member",
                    }
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants/add", method="PUT"
        ).respond_with_json(
            {
                "participant": {
                    "participantId": 110330443206779,
                    "memberId": 80726606082762,
                    "userName": "julio",
                    "firstName": None,
                    "lastName": None,
                    "email": "user@seqera.io",
                    "orgRole": "member",
                    "teamId": None,
                    "teamName": None,
                    "wspRole": "launch",
                    "type": "MEMBER",
                    "teamAvatarUrl": None,
                    "userAvatarUrl": None,
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "participants",
            "add",
            "-w",
            "75887156211589",
            "-n",
            "julio",
            "-t",
            "MEMBER",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["participant"]["userName"] == "julio"
            assert data["workspaceName"] == "workspace1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["participant"]["userName"] == "julio"
            assert data["workspaceName"] == "workspace1"
        else:  # console
            assert "julio" in out.stdout
            assert "workspace1" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_with_overwrite(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding participant with overwrite flag.

        Ported from testAddParticipantWithOverwrite() in ParticipantsCmdTest.java
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
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    },
                ]
            }
        )

        # First GET to check existing participants
        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants", method="GET"
        ).respond_with_json(
            {
                "participants": [
                    {
                        "participantId": 48516118433516,
                        "memberId": 175703974560466,
                        "userName": "jfernandez74",
                        "firstName": None,
                        "lastName": None,
                        "email": "jfernandez74@gmail.com",
                        "orgRole": "owner",
                        "teamId": None,
                        "teamName": None,
                        "wspRole": "owner",
                        "type": "MEMBER",
                        "teamAvatarUrl": None,
                        "userAvatarUrl": None,
                    },
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request("/orgs/27736513644467/members", method="GET").respond_with_json(
            {
                "members": [
                    {
                        "memberId": 255080245994226,
                        "userName": "julio",
                        "email": "julio@seqera.io",
                        "firstName": None,
                        "lastName": None,
                        "avatar": None,
                        "role": "member",
                    }
                ],
                "totalSize": 1,
            }
        )

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants/48516118433516",
            method="DELETE",
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/orgs/27736513644467/workspaces/75887156211589/participants/add", method="PUT"
        ).respond_with_json(
            {
                "participant": {
                    "participantId": 110330443206779,
                    "memberId": 80726606082762,
                    "userName": "julio",
                    "firstName": None,
                    "lastName": None,
                    "email": "user@seqera.io",
                    "orgRole": "member",
                    "teamId": None,
                    "teamName": None,
                    "wspRole": "launch",
                    "type": "MEMBER",
                    "teamAvatarUrl": None,
                    "userAvatarUrl": None,
                }
            }
        )

        # Run the command with overwrite
        out = exec_cmd(
            "participants",
            "add",
            "--overwrite",
            "-w",
            "75887156211589",
            "-n",
            "julio",
            "-t",
            "MEMBER",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["participant"]["userName"] == "julio"
            assert data["workspaceName"] == "workspace1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["participant"]["userName"] == "julio"
        else:  # console
            assert "julio" in out.stdout
