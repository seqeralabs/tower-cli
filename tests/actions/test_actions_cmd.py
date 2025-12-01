"""
Tests for actions commands.

Ported from ActionsCmdTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer


class TestActionsListCmd:
    """Test actions list command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_list(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test listing actions.

        Ported from testList() in ActionsCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                        "pipeline": "https://github.com/pditommaso/hello",
                        "source": "github",
                        "status": "ACTIVE",
                        "lastSeen": "2021-06-18T10:10:33Z",
                        "dateCreated": "2021-06-18T10:10:05Z",
                    },
                    {
                        "id": "58byWxhmUDLLWIF4J97XEP",
                        "name": "Bye",
                        "pipeline": "https://github.com/pditommaso/hello",
                        "source": "github",
                        "status": "ACTIVE",
                        "lastSeen": "2021-06-18T10:10:33Z",
                        "dateCreated": "2021-06-18T10:10:05Z",
                    },
                ]
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": user_workspace_name,
                    "email": f"{user_workspace_name}@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("actions", "list", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceRef"] == f"[{user_workspace_name}]"
            assert len(data["actions"]) == 2
            assert data["actions"][0]["name"] == "hello"
            assert data["actions"][1]["name"] == "Bye"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceRef"] == f"[{user_workspace_name}]"
            assert len(data["actions"]) == 2
            assert data["actions"][0]["name"] == "hello"
        else:  # console
            assert "hello" in out.stdout
            assert "Bye" in out.stdout
            assert "Actions" in out.stdout

    def test_list_empty(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test listing actions when there are none.

        Ported from testListEmpty() in ActionsCmdTest.java
        """
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json({"actions": []})

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

        # Run the command
        out = exec_cmd("actions", "list")

        # Assertions
        assert out.exit_code == 0
        assert "No actions found" in out.stdout


class TestActionsViewCmd:
    """Test actions view command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_view_by_name(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test viewing action by name.

        Ported from testView() in ActionsCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                        "pipeline": "https://github.com/pditommaso/hello",
                        "source": "github",
                        "status": "ACTIVE",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="GET",
        ).respond_with_json(
            {
                "action": {
                    "id": "57byWxhmUDLLWIF4J97XEP",
                    "name": "hello",
                    "source": "github",
                    "status": "ACTIVE",
                    "launch": {
                        "id": "3htPtgK2KufwvQcovOko",
                        "pipeline": "https://github.com/pditommaso/hello",
                        "workDir": "/home/ubuntu/nf-work",
                        "computeEnv": {
                            "id": "1NcvsrdHeaKsrpgQ85NYpe",
                            "name": "demo-compute",
                        },
                    },
                    "dateCreated": "2021-06-18T10:10:05Z",
                    "lastUpdated": "2021-06-18T10:10:33Z",
                }
            }
        )

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": user_workspace_name,
                    "email": f"{user_workspace_name}@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("actions", "view", "-n", "hello", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["workspaceRef"] == f"[{user_workspace_name}]"
            assert data["action"]["name"] == "hello"
            assert data["action"]["id"] == "57byWxhmUDLLWIF4J97XEP"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["workspaceRef"] == f"[{user_workspace_name}]"
            assert data["action"]["name"] == "hello"
        else:  # console
            assert "hello" in out.stdout
            assert "57byWxhmUDLLWIF4J97XEP" in out.stdout

    def test_view_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test viewing action that doesn't exist.

        Ported from testViewNoActionFound() in ActionsCmdTest.java
        """
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                    }
                ]
            }
        )

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

        # Run the command
        out = exec_cmd("actions", "view", "-n", "test")

        # Assertions
        assert out.exit_code == 1
        assert "not found" in out.stderr.lower() or "not found" in out.stdout.lower()


class TestActionsDeleteCmd:
    """Test actions delete command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_delete(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test deleting action.

        Ported from testDelete() in ActionsCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="GET",
        ).respond_with_json(
            {
                "action": {
                    "id": "57byWxhmUDLLWIF4J97XEP",
                    "name": "hello",
                    "source": "github",
                    "status": "ACTIVE",
                }
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="DELETE",
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": user_workspace_name,
                    "email": f"{user_workspace_name}@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("actions", "delete", "-n", "hello", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["actionName"] == "hello"
            assert data["workspace"] == f"[{user_workspace_name}]"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["actionName"] == "hello"
        else:  # console
            assert "hello" in out.stdout
            assert "deleted" in out.stdout

    def test_delete_error(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test deleting action with error.

        Ported from testDeleteError() in ActionsCmdTest.java
        """
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="GET",
        ).respond_with_json(
            {
                "action": {
                    "id": "57byWxhmUDLLWIF4J97XEP",
                    "name": "hello",
                }
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="DELETE",
        ).respond_with_data("", status=500)

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": user_workspace_name,
                    "email": f"{user_workspace_name}@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("actions", "delete", "-n", "hello")

        # Assertions
        assert out.exit_code == 1


class TestActionsAddCmd:
    """Test actions add command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_github(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding GitHub action.

        Ported from testAdd() in ActionsCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/compute-envs",
            method="GET",
            query_string="status=AVAILABLE",
        ).respond_with_json(
            {
                "computeEnvs": [
                    {
                        "id": "vYOK4vn7spw7bHHWBDXZ2",
                        "name": "demo",
                        "platform": "aws-batch",
                        "status": "AVAILABLE",
                        "primary": True,
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/compute-envs/vYOK4vn7spw7bHHWBDXZ2",
            method="GET",
        ).respond_with_json(
            {
                "computeEnv": {
                    "id": "vYOK4vn7spw7bHHWBDXZ2",
                    "name": "demo",
                    "config": {
                        "workDir": "/work",
                    },
                }
            }
        )

        httpserver.expect_request(
            "/actions",
            method="POST",
        ).respond_with_json({"actionId": "2Z1g6MCWpOLgHLA65cw1qt"})

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": user_workspace_name,
                    "email": f"{user_workspace_name}@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "actions",
            "add",
            "github",
            "-n",
            "new-action",
            "--pipeline",
            "https://github.com/pditommaso/nf-sleep",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["actionName"] == "new-action"
            assert data["workspace"] == f"[{user_workspace_name}]"
            assert data["actionId"] == "2Z1g6MCWpOLgHLA65cw1qt"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["actionName"] == "new-action"
        else:  # console
            assert "new-action" in out.stdout
            assert "added" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_with_overwrite(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding action with overwrite flag.

        Ported from testAddWithOverwrite() in ActionsCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": user_workspace_name,
                    "email": f"{user_workspace_name}@seqera.io",
                }
            }
        )

        # First call to check if action exists
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="GET",
        ).respond_with_json(
            {
                "action": {
                    "id": "57byWxhmUDLLWIF4J97XEP",
                    "name": "hello",
                }
            }
        )

        # Delete existing action
        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="DELETE",
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/compute-envs",
            method="GET",
            query_string="status=AVAILABLE",
        ).respond_with_json(
            {
                "computeEnvs": [
                    {
                        "id": "vYOK4vn7spw7bHHWBDXZ2",
                        "name": "demo",
                        "platform": "aws-batch",
                        "status": "AVAILABLE",
                        "primary": True,
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/compute-envs/vYOK4vn7spw7bHHWBDXZ2",
            method="GET",
        ).respond_with_json(
            {
                "computeEnv": {
                    "id": "vYOK4vn7spw7bHHWBDXZ2",
                    "name": "demo",
                    "config": {
                        "workDir": "/work",
                    },
                }
            }
        )

        httpserver.expect_request(
            "/actions",
            method="POST",
        ).respond_with_json({"actionId": "2Z1g6MCWpOLgHLA65cw1qt"})

        # Run the command
        out = exec_cmd(
            "actions",
            "add",
            "github",
            "--overwrite",
            "-n",
            "hello",
            "--pipeline",
            "https://github.com/pditommaso/nf-sleep",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["actionName"] == "hello"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["actionName"] == "hello"
        else:  # console
            assert "hello" in out.stdout


class TestActionsUpdateCmd:
    """Test actions update command."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test updating action.

        Ported from testUpdate() in ActionsCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="GET",
        ).respond_with_json(
            {
                "action": {
                    "id": "57byWxhmUDLLWIF4J97XEP",
                    "name": "hello",
                    "status": "ACTIVE",
                    "launch": {
                        "id": "3htPtgK2KufwvQcovOko",
                        "pipeline": "https://github.com/pditommaso/hello",
                    },
                }
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="PUT",
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": user_workspace_name,
                    "email": f"{user_workspace_name}@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd("actions", "update", "-n", "hello", output_format=output_format)

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["actionName"] == "hello"
            assert data["workspace"] == f"[{user_workspace_name}]"
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["actionName"] == "hello"
        else:  # console
            assert "hello" in out.stdout
            assert "updated" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update_name(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test updating action name.

        Ported from testUpdateName() in ActionsCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="GET",
        ).respond_with_json(
            {
                "action": {
                    "id": "57byWxhmUDLLWIF4J97XEP",
                    "name": "hello",
                    "status": "ACTIVE",
                    "launch": {},
                }
            }
        )

        httpserver.expect_request(
            "/actions/validate",
            method="GET",
            query_string="name=hello_world",
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="PUT",
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": user_workspace_name,
                    "email": f"{user_workspace_name}@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "actions",
            "update",
            "-n",
            "hello",
            "--new-name",
            "hello_world",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_pause(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test pausing action.

        Ported from testPause() in ActionsCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="GET",
        ).respond_with_json(
            {
                "action": {
                    "id": "57byWxhmUDLLWIF4J97XEP",
                    "name": "hello",
                    "status": "ACTIVE",
                    "launch": {},
                }
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="PUT",
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP/pause",
            method="POST",
        ).respond_with_data("", status=204)

        httpserver.expect_request(
            "/user-info",
            method="GET",
        ).respond_with_json(
            {
                "user": {
                    "id": 1264,
                    "userName": user_workspace_name,
                    "email": f"{user_workspace_name}@seqera.io",
                }
            }
        )

        # Run the command
        out = exec_cmd(
            "actions", "update", "-n", "hello", "-s", "pause", output_format=output_format
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

    def test_pause_already_paused(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test pausing action that is already paused.

        Ported from testPauseAlreadyPausedItem() in ActionsCmdTest.java
        """
        # Setup mock responses
        httpserver.expect_request(
            "/actions",
            method="GET",
        ).respond_with_json(
            {
                "actions": [
                    {
                        "id": "57byWxhmUDLLWIF4J97XEP",
                        "name": "hello",
                    }
                ]
            }
        )

        httpserver.expect_request(
            "/actions/57byWxhmUDLLWIF4J97XEP",
            method="GET",
        ).respond_with_json(
            {
                "action": {
                    "id": "57byWxhmUDLLWIF4J97XEP",
                    "name": "hello",
                    "status": "ACTIVE",
                    "launch": {},
                }
            }
        )

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

        # Run the command (trying to set to ACTIVE when already ACTIVE)
        out = exec_cmd("actions", "update", "-n", "hello", "-s", "active")

        # Assertions
        assert out.exit_code == 1
        assert "already set" in out.stderr.lower() or "already set" in out.stdout.lower()
