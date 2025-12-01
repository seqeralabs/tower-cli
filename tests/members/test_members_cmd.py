"""
Tests for members commands.

Ported from MembersCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestMembersCmd:
    """Test members commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing organization members.

        Ported from testList() in MembersCmdTest.java
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

        members_response = {
            "members": [
                {
                    "memberId": 175703974560466,
                    "userName": "jfernandez74",
                    "email": "jfernandez74@gmail.com",
                    "firstName": None,
                    "lastName": None,
                    "avatar": "https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404",
                    "role": "owner",
                },
                {
                    "memberId": 255080245994226,
                    "userName": "julio",
                    "email": "julio@seqera.io",
                    "firstName": None,
                    "lastName": None,
                    "avatar": "https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404",
                    "role": "member",
                },
            ],
            "totalSize": 2,
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request("/orgs/27736513644467/members", method="GET").respond_with_json(
            members_response, status=200
        )

        # Run the command
        out = exec_cmd(
            "members",
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
            assert len(data["members"]) == 2
            assert data["members"][0]["memberId"] == 175703974560466
            assert data["members"][0]["userName"] == "jfernandez74"
            assert data["members"][0]["role"] == "owner"
            assert data["members"][1]["memberId"] == 255080245994226
            assert data["members"][1]["userName"] == "julio"
            assert data["members"][1]["role"] == "member"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["organization"] == "organization1"
            assert len(data["members"]) == 2
        else:  # console
            assert "organization1" in out.stdout
            assert "jfernandez74" in out.stdout
            assert "julio" in out.stdout

    def test_list_with_offset(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing members with offset pagination.

        Ported from testListWithOffset() in MembersCmdTest.java
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

        members_response = {
            "members": [
                {
                    "memberId": 175703974560466,
                    "userName": "jfernandez74",
                    "email": "jfernandez74@gmail.com",
                    "firstName": None,
                    "lastName": None,
                    "avatar": "https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404",
                    "role": "owner",
                },
                {
                    "memberId": 255080245994226,
                    "userName": "julio",
                    "email": "julio@seqera.io",
                    "firstName": None,
                    "lastName": None,
                    "avatar": "https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404",
                    "role": "member",
                },
            ],
            "totalSize": 2,
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request(
            "/orgs/27736513644467/members", method="GET", query_string="offset=1&max=2"
        ).respond_with_json(members_response, status=200)

        # Run the command
        out = exec_cmd(
            "members",
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
        assert "jfernandez74" in out.stdout

    def test_list_with_page(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing members with page pagination.

        Ported from testListWithPage() in MembersCmdTest.java
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

        members_response = {
            "members": [
                {
                    "memberId": 175703974560466,
                    "userName": "jfernandez74",
                    "email": "jfernandez74@gmail.com",
                    "firstName": None,
                    "lastName": None,
                    "avatar": "https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404",
                    "role": "owner",
                },
                {
                    "memberId": 255080245994226,
                    "userName": "julio",
                    "email": "julio@seqera.io",
                    "firstName": None,
                    "lastName": None,
                    "avatar": "https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404",
                    "role": "member",
                },
            ],
            "totalSize": 2,
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request(
            "/orgs/27736513644467/members", method="GET", query_string="offset=0&max=2"
        ).respond_with_json(members_response, status=200)

        # Run the command
        out = exec_cmd(
            "members",
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
        assert "jfernandez74" in out.stdout

    def test_list_with_conflicting_pageable(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing members with conflicting pagination parameters.

        Ported from testListWithConflictingPageable() in MembersCmdTest.java
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
            "members",
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

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test adding a member to organization.

        Ported from testAdd() in MembersCmdTest.java
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

        member_add_response = {
            "member": {
                "memberId": 440905637173,
                "userName": "julio123",
                "email": "julio123@seqera.io",
                "firstName": None,
                "lastName": None,
                "avatar": None,
                "role": "member",
            }
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request(
            "/orgs/27736513644467/members/add", method="PUT"
        ).respond_with_json(member_add_response, status=200)

        # Run the command
        out = exec_cmd(
            "members",
            "add",
            "-o",
            "organization1",
            "-u",
            "julio123",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["organization"] == "organization1"
            assert data["member"]["userName"] == "julio123"
            assert data["member"]["email"] == "julio123@seqera.io"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["organization"] == "organization1"
            assert data["member"]["userName"] == "julio123"
        else:  # console
            assert "julio123" in out.stdout
            assert "organization1" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting a member from organization.

        Ported from testDelete() in MembersCmdTest.java
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

        members_response = {
            "members": [
                {
                    "memberId": 255080245994226,
                    "userName": "julio",
                    "email": "julio@seqera.io",
                    "firstName": None,
                    "lastName": None,
                    "avatar": "https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404",
                    "role": "member",
                }
            ],
            "totalSize": 1,
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request("/orgs/27736513644467/members", method="GET").respond_with_json(
            members_response, status=200
        )

        httpserver.expect_request(
            "/orgs/27736513644467/members/255080245994226", method="DELETE"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "members",
            "delete",
            "-o",
            "organization1",
            "-u",
            "julio@seqera.io",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["userRef"] == "julio@seqera.io"
            assert data["organization"] == "organization1"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["userRef"] == "julio@seqera.io"
            assert data["organization"] == "organization1"
        else:  # console
            assert "julio@seqera.io" in out.stdout
            assert "organization1" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test updating a member role in organization.

        Ported from testUpdate() in MembersCmdTest.java
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

        members_response = {
            "members": [
                {
                    "memberId": 255080245994226,
                    "userName": "julio",
                    "email": "julio@seqera.io",
                    "firstName": None,
                    "lastName": None,
                    "avatar": "https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404",
                    "role": "member",
                }
            ],
            "totalSize": 1,
        }

        httpserver.expect_request("/user-info", method="GET").respond_with_json(
            user_response, status=200
        )

        httpserver.expect_request("/user/1264/workspaces", method="GET").respond_with_json(
            workspaces_response, status=200
        )

        httpserver.expect_request("/orgs/27736513644467/members", method="GET").respond_with_json(
            members_response, status=200
        )

        httpserver.expect_request(
            "/orgs/27736513644467/members/255080245994226/role", method="PUT"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "members",
            "update",
            "-o",
            "organization1",
            "-u",
            "julio@seqera.io",
            "-r",
            "OWNER",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["userRef"] == "julio@seqera.io"
            assert data["organization"] == "organization1"
            assert data["role"] == "owner"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["userRef"] == "julio@seqera.io"
            assert data["organization"] == "organization1"
            assert data["role"] == "owner"
        else:  # console
            assert "julio@seqera.io" in out.stdout
            assert "organization1" in out.stdout
            assert "owner" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_leave(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test leaving an organization.

        Ported from testLeave() in MembersCmdTest.java
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
            "/orgs/27736513644467/members/leave", method="DELETE"
        ).respond_with_data("", status=204)

        # Run the command
        out = exec_cmd(
            "members",
            "leave",
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
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["organization"] == "organization1"
        else:  # console
            assert "organization1" in out.stdout
