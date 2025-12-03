"""
Tests for studios commands.

Ported from StudiosCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestStudiosListCmd:
    """Test studios list command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test listing studios.

        Ported from testList() in StudiosCmdTest.java
        """
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        httpserver.expect_request(
            "/user/1264/workspaces",
            method="GET",
        ).respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 154662193913883,
                        "orgName": "organization1",
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/studios",
            method="GET",
            query_string="workspaceId=75887156211589",
        ).respond_with_json(
            {
                "studios": [
                    {
                        "sessionId": "ddfd5e14",
                        "workspaceId": 75887156211589,
                        "user": {
                            "id": 1,
                            "userName": "samurai-jack",
                            "email": "jack@seqera.io",
                        },
                        "name": "studio-7728",
                        "description": "Local studio",
                        "studioUrl": "http://addfd5e14.studio.localhost:9191",
                        "computeEnv": {
                            "id": "16esMgELkyQ3QPcHGNTiXQ",
                            "name": "my-other-local-ce",
                            "platform": "local-platform",
                        },
                        "template": {
                            "repository": "cr.seqera.io/public/data-studio-jupyter:4.2.5-snapshot",
                            "icon": "jupyter",
                        },
                        "configuration": {
                            "gpu": 0,
                            "cpu": 2,
                            "memory": 8192,
                            "mountData": [],
                        },
                        "dateCreated": "2025-01-14T11:51:05.393498Z",
                        "lastUpdated": "2025-01-15T09:10:30.016752Z",
                        "statusInfo": {
                            "status": "running",
                            "message": "",
                        },
                    },
                    {
                        "sessionId": "3e8370e7",
                        "workspaceId": 75887156211589,
                        "user": {
                            "id": 1,
                            "userName": "johnny-bravo",
                            "email": "johnny@seqera.io",
                        },
                        "name": "studio-a66d",
                        "description": "my first studio",
                        "studioUrl": "http://a3e8370e7.studio.localhost:9191",
                        "computeEnv": {
                            "id": "61DYXYj3XQAYbJIHrI1XSg",
                            "name": "my-local-ce",
                            "platform": "local-platform",
                        },
                        "template": {
                            "repository": "cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                            "icon": "vscode",
                        },
                        "configuration": {
                            "gpu": 0,
                            "cpu": 2,
                            "memory": 8192,
                            "mountData": ["v1-user-1ccf131810375d303bf0402dd8423433"],
                        },
                        "dateCreated": "2025-01-10T17:26:36.83703Z",
                        "lastUpdated": "2025-01-12T03:00:30.014415Z",
                        "statusInfo": {
                            "status": "errored",
                            "message": "",
                        },
                    },
                ],
                "totalSize": 2,
            }
        )

        # Run the command
        out = exec_cmd("studios", "list", "-w", "75887156211589", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceRef"] == "[organization1 / workspace1]"
            assert len(data["studios"]) == 2
            assert data["studios"][0]["name"] == "studio-7728"
            assert data["studios"][1]["name"] == "studio-a66d"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceRef"] == "[organization1 / workspace1]"
            assert len(data["studios"]) == 2
        else:  # console
            assert "studio-7728" in out.stdout
            assert "studio-a66d" in out.stdout
            assert "Studios" in out.stdout

    def test_list_with_pagination(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing studios with pagination.

        Ported from testListWithOffset() in StudiosCmdTest.java
        """
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        httpserver.expect_request(
            "/user/1264/workspaces",
            method="GET",
        ).respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 154662193913883,
                        "orgName": "organization1",
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/studios",
            method="GET",
            query_string="workspaceId=75887156211589&offset=1&max=2",
        ).respond_with_json(
            {
                "studios": [
                    {
                        "sessionId": "3e8370e7",
                        "workspaceId": 75887156211589,
                        "name": "studio-a66d",
                        "description": "my first studio",
                        "user": {"id": 1, "userName": "johnny-bravo"},
                        "computeEnv": {"id": "123", "name": "ce"},
                        "template": {"repository": "repo", "icon": "vscode"},
                        "configuration": {"gpu": 0, "cpu": 2, "memory": 8192},
                        "statusInfo": {"status": "running"},
                    }
                ],
                "totalSize": 2,
            }
        )

        # Run the command
        out = exec_cmd("studios", "list", "-w", "75887156211589", "--offset", "1", "--max", "2")

        # Assertions
        assert out.exit_code == 0
        assert "studio-a66d" in out.stdout


class TestStudiosViewCmd:
    """Test studios view command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_view_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test viewing studio by ID.

        Ported from testView() in StudiosCmdTest.java
        """
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        httpserver.expect_request(
            "/user/1264/workspaces",
            method="GET",
        ).respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 154662193913883,
                        "orgName": "organization1",
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/studios/3e8370e7",
            method="GET",
            query_string="workspaceId=75887156211589",
        ).respond_with_json(
            {
                "studio": {
                    "sessionId": "3e8370e7",
                    "workspaceId": 75887156211589,
                    "user": {
                        "id": 2345,
                        "userName": "John Doe",
                        "email": "john@seqera.io",
                    },
                    "name": "studio-a66d",
                    "description": "my first studio",
                    "studioUrl": "https://a3e8370e7.dev-seqera.com",
                    "computeEnv": {
                        "id": "3xkkzYH2nbD3nZjrzKm0oR",
                        "name": "ce1",
                        "platform": "aws-batch",
                        "region": "us-east-2",
                    },
                    "template": {
                        "repository": "cr.seqera.io/public/data-studio-vscode:1.93.1-snapshot",
                        "icon": "vscode",
                    },
                    "configuration": {
                        "gpu": 0,
                        "cpu": 2,
                        "memory": 8192,
                        "mountData": ["v1-user-1ccf131810375d303bf0402dd8423433"],
                    },
                    "dateCreated": "2024-12-19T06:49:24.893122+01:00",
                    "lastUpdated": "2024-12-19T06:52:50.686822+01:00",
                    "statusInfo": {
                        "status": "running",
                        "message": "",
                    },
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "studios", "view", "-w", "75887156211589", "-i", "3e8370e7", output_format=output_format
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["studio"]["name"] == "studio-a66d"
            assert data["studio"]["sessionId"] == "3e8370e7"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["studio"]["name"] == "studio-a66d"
        else:  # console
            assert "studio-a66d" in out.stdout
            assert "3e8370e7" in out.stdout


class TestStudiosStartCmd:
    """Test studios start command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_start_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test starting studio by ID.

        Ported from testStart() in StudiosCmdTest.java
        """
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        httpserver.expect_request(
            "/user/1264/workspaces",
            method="GET",
        ).respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 154662193913883,
                        "orgName": "organization1",
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/studios/3e8370e7",
            method="GET",
            query_string="workspaceId=75887156211589",
        ).respond_with_json(
            {
                "studio": {
                    "sessionId": "3e8370e7",
                    "name": "studio-a66d",
                    "description": "my first studio",
                    "configuration": {
                        "gpu": 0,
                        "cpu": 2,
                        "memory": 8192,
                        "mountData": ["v1-user-1ccf131810375d303bf0402dd8423433"],
                    },
                    "statusInfo": {"status": "stopped"},
                }
            }
        )

        httpserver.expect_request(
            "/studios/3e8370e7/start",
            method="PUT",
            query_string="workspaceId=75887156211589",
        ).respond_with_json(
            {
                "jobSubmitted": True,
                "sessionId": "3e8370e7",
                "statusInfo": {
                    "status": "starting",
                    "message": "",
                },
            }
        )

        # Run the command
        out = exec_cmd(
            "studios",
            "start",
            "-w",
            "75887156211589",
            "-i",
            "3e8370e7",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["sessionId"] == "3e8370e7"
            assert data["jobSubmitted"] is True
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["sessionId"] == "3e8370e7"
        else:  # console
            assert "3e8370e7" in out.stdout


class TestStudiosStopCmd:
    """Test studios stop command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_stop_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test stopping studio by ID.

        Ported from testStop() in StudiosCmdTest.java
        """
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        httpserver.expect_request(
            "/user/1264/workspaces",
            method="GET",
        ).respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 154662193913883,
                        "orgName": "organization1",
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/studios/3e8370e7/stop",
            method="PUT",
            query_string="workspaceId=75887156211589",
        ).respond_with_json(
            {
                "jobSubmitted": True,
                "sessionId": "3e8370e7",
                "statusInfo": {
                    "status": "stopping",
                    "message": "",
                },
            }
        )

        # Run the command
        out = exec_cmd(
            "studios", "stop", "-w", "75887156211589", "-i", "3e8370e7", output_format=output_format
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["sessionId"] == "3e8370e7"
            assert data["jobSubmitted"] is True
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["sessionId"] == "3e8370e7"
        else:  # console
            assert "3e8370e7" in out.stdout


class TestStudiosDeleteCmd:
    """Test studios delete command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete_by_id(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test deleting studio by ID.

        Ported from testDelete() in StudiosCmdTest.java
        """
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        httpserver.expect_request(
            "/user/1264/workspaces",
            method="GET",
        ).respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 154662193913883,
                        "orgName": "organization1",
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/studios/3e8370e7",
            method="DELETE",
            query_string="workspaceId=75887156211589",
        ).respond_with_data("", status=200)

        # Run the command
        out = exec_cmd(
            "studios",
            "delete",
            "-w",
            "75887156211589",
            "-i",
            "3e8370e7",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["sessionId"] == "3e8370e7"
            assert data["workspaceRef"] == "[organization1 / workspace1]"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["sessionId"] == "3e8370e7"
        else:  # console
            assert "3e8370e7" in out.stdout
            assert "deleted" in out.stdout


class TestStudiosCheckpointsCmd:
    """Test studios checkpoints command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_checkpoints(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        output_format: str,
    ) -> None:
        """
        Test listing studio checkpoints.

        Ported from testCheckpoints() in StudiosCmdTest.java
        """
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": "user",
                    "email": "user@seqera.io",
                }
            }
        )

        httpserver.expect_request(
            "/user/1264/workspaces",
            method="GET",
        ).respond_with_json(
            {
                "orgsAndWorkspaces": [
                    {
                        "orgId": 154662193913883,
                        "orgName": "organization1",
                        "workspaceId": 75887156211589,
                        "workspaceName": "workspace1",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/studios/3e8370e7/checkpoints",
            method="GET",
            query_string="workspaceId=75887156211589",
        ).respond_with_json(
            {
                "checkpoints": [
                    {
                        "id": 2,
                        "name": "studio-a66d_2",
                        "dateSaved": "2025-01-30T09:34:33Z",
                        "dateCreated": "2025-01-30T09:29:31Z",
                        "author": {
                            "id": 100,
                            "userName": "johnny-bravo",
                        },
                    },
                    {
                        "id": 1,
                        "name": "studio-a66d_1",
                        "dateSaved": "2025-01-28T14:05:07Z",
                        "dateCreated": "2025-01-28T12:49:06Z",
                        "author": {
                            "id": 100,
                            "userName": "johnny-bravo",
                        },
                    },
                ],
                "totalSize": 2,
            }
        )

        # Run the command
        out = exec_cmd(
            "studios",
            "checkpoints",
            "-w",
            "75887156211589",
            "-i",
            "3e8370e7",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["sessionId"] == "3e8370e7"
            assert len(data["checkpoints"]) == 2
            assert data["checkpoints"][0]["name"] == "studio-a66d_2"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["sessionId"] == "3e8370e7"
            assert len(data["checkpoints"]) == 2
        else:  # console
            assert "studio-a66d_2" in out.stdout
            assert "studio-a66d_1" in out.stdout
            assert "Checkpoints" in out.stdout
