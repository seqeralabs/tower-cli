"""
Tests for AWS credentials provider.

Ported from AwsProviderTest.java
"""

import json

import pytest
from pytest_httpserver import HTTPServer
from werkzeug.wrappers import Response as WerkzeugResponse

from tests.conftest import ExecOut


class TestAwsProvider:
    """Test AWS credentials provider commands."""

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add_with_only_assume_role(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding AWS credentials with only assume role ARN.

        Ported from testAddWithOnlyAssumeRole() in AwsProviderTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {"assumeRoleArn": "arn_role"},
                    "name": "test_credentials",
                    "provider": "aws",
                }
            },
        ).respond_with_json(
            {"credentialsId": "6Kyn17toiABGu47qpBXsVX"},
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "add",
            "aws",
            "--name=test_credentials",
            "--assume-role-arn=arn_role",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "AWS"
            assert data["id"] == "6Kyn17toiABGu47qpBXsVX"
            assert data["name"] == "test_credentials"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "AWS"
            assert data["id"] == "6Kyn17toiABGu47qpBXsVX"
            assert data["name"] == "test_credentials"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "AWS" in out.stdout
            assert "test_credentials" in out.stdout
            assert "6Kyn17toiABGu47qpBXsVX" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_add(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test adding AWS credentials with access key and secret key.

        Ported from testAdd() in AwsProviderTest.java
        """
        # Setup mock HTTP expectation
        httpserver.expect_request(
            "/credentials",
            method="POST",
            json={
                "credentials": {
                    "keys": {
                        "accessKey": "access_key",
                        "secretKey": "secret_key",
                    },
                    "name": "aws",
                    "provider": "aws",
                }
            },
        ).respond_with_json(
            {"credentialsId": "1cz5A8cuBkB5iJliCwJCFU"},
            status=200,
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "add",
            "aws",
            "-n",
            "aws",
            "-a",
            "access_key",
            "-s",
            "secret_key",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "AWS"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "aws"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "AWS"
            assert data["id"] == "1cz5A8cuBkB5iJliCwJCFU"
            assert data["name"] == "aws"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "AWS" in out.stdout
            assert "aws" in out.stdout
            assert "1cz5A8cuBkB5iJliCwJCFU" in out.stdout

    @pytest.mark.parametrize("output_format", ["console", "json", "yaml"])
    def test_update(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
        output_format: str,
    ) -> None:
        """
        Test updating AWS credentials.

        Ported from testUpdate() in AwsProviderTest.java
        """
        # Setup mock HTTP expectations
        # First, GET to fetch existing credentials
        httpserver.expect_request(
            "/credentials/kfKx9xRgzpIIZrbCMOcU4",
            method="GET",
        ).respond_with_json(
            {
                "credentials": {
                    "id": "kfKx9xRgzpIIZrbCMOcU4",
                    "name": "aws",
                    "description": None,
                    "discriminator": "aws",
                    "baseUrl": None,
                    "category": None,
                    "deleted": None,
                    "lastUsed": "2021-09-06T15:16:52Z",
                    "dateCreated": "2021-09-03T13:23:37Z",
                    "lastUpdated": "2021-09-03T13:23:37Z",
                }
            },
            status=200,
        )

        # Second, PUT to update credentials
        httpserver.expect_request(
            "/credentials/kfKx9xRgzpIIZrbCMOcU4",
            method="PUT",
            json={
                "credentials": {
                    "keys": {"assumeRoleArn": "changeAssumeRole"},
                    "id": "kfKx9xRgzpIIZrbCMOcU4",
                    "name": "aws",
                    "provider": "aws",
                }
            },
        ).respond_with_data(
            "",
            status=204,
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "update",
            "aws",
            "-i",
            "kfKx9xRgzpIIZrbCMOcU4",
            "-r",
            "changeAssumeRole",
            output_format=output_format,
        )

        # Assertions
        assert out.exit_code == 0
        assert out.stderr == ""

        if output_format == "json":
            data = json.loads(out.stdout)
            assert data["provider"] == "AWS"
            assert data["name"] == "aws"
            assert data["workspace"] == user_workspace_name
        elif output_format == "yaml":
            import yaml

            data = yaml.safe_load(out.stdout)
            assert data["provider"] == "AWS"
            assert data["name"] == "aws"
            assert data["workspace"] == user_workspace_name
        else:  # console
            assert "AWS" in out.stdout
            assert "aws" in out.stdout
            assert "updated" in out.stdout

    def test_update_not_found(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
        user_workspace_name: str,
    ) -> None:
        """
        Test updating non-existent AWS credentials.

        Ported from testUpdateNotFound() in AwsProviderTest.java
        """
        # Setup mock HTTP expectation - 403 Forbidden
        httpserver.expect_request(
            "/credentials/kfKx9xRgzpIIZrbCMOcU5",
            method="GET",
        ).respond_with_data(
            "",
            status=403,
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "update",
            "aws",
            "-i",
            "kfKx9xRgzpIIZrbCMOcU5",
            "-r",
            "changeAssumeRole",
        )

        # Assertions
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "kfKx9xRgzpIIZrbCMOcU5" in out.stderr
        assert "not found" in out.stderr.lower()

    def test_invalid_auth(
        self,
        httpserver: HTTPServer,
        exec_cmd: callable,
    ) -> None:
        """
        Test with invalid authentication.

        Ported from testInvalidAuth() in AwsProviderTest.java
        """
        # Setup mock HTTP expectation - 401 Unauthorized
        httpserver.expect_request(
            "/credentials/kfKx9xRgzpIIZrbCMOcU5",
            method="GET",
        ).respond_with_data(
            "",
            status=401,
        )

        # Run the command
        out = exec_cmd(
            "credentials",
            "update",
            "aws",
            "-i",
            "kfKx9xRgzpIIZrbCMOcU5",
            "-r",
            "changeAssumeRole",
        )

        # Assertions
        assert out.exit_code == 1
        assert out.stdout == ""
        assert "401" in out.stderr or "Unauthorized" in out.stderr
