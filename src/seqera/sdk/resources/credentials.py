"""
Credentials resource for the Seqera SDK.
"""

from __future__ import annotations

from typing import Any

from seqera.exceptions import CredentialsNotFoundException
from seqera.models.common import PaginatedList
from seqera.models.credentials import Credentials
from seqera.sdk.resources.base import BaseResource


class CredentialsResource(BaseResource):
    """
    SDK resource for managing credentials.

    Credentials store authentication information for cloud providers,
    git repositories, and container registries.
    """

    def list(
        self,
        workspace: str | int | None = None,
    ) -> PaginatedList[Credentials]:
        """
        List credentials in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of Credentials objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[Credentials], int]:
            params = self._build_params(workspace=workspace)
            response = self._client.get("/credentials", params=params)

            creds = [Credentials.model_validate(c) for c in response.get("credentials", [])]
            total_size = len(creds)
            paginated = creds[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        credentials_id: str,
        workspace: str | int | None = None,
    ) -> Credentials:
        """
        Get credentials by ID.

        Args:
            credentials_id: Credentials ID
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Credentials object

        Raises:
            CredentialsNotFoundException: If credentials not found
        """
        params = self._build_params(workspace=workspace)
        response = self._client.get(f"/credentials/{credentials_id}", params=params)

        creds_data = response.get("credentials", {})
        if not creds_data:
            raise CredentialsNotFoundException(credentials_id, str(workspace or "user"))

        return Credentials.model_validate(creds_data)

    def add_aws(
        self,
        name: str,
        access_key: str,
        secret_key: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        assume_role_arn: str | None = None,
    ) -> Credentials:
        """
        Add AWS credentials.

        Args:
            name: Credentials name
            access_key: AWS access key ID
            secret_key: AWS secret access key
            workspace: Workspace ID or "org/workspace" reference
            description: Description
            assume_role_arn: IAM role ARN to assume

        Returns:
            Created Credentials object
        """
        keys: dict[str, Any] = {
            "accessKey": access_key,
            "secretKey": secret_key,
        }
        if assume_role_arn:
            keys["assumeRoleArn"] = assume_role_arn

        return self._add_credentials(
            name=name,
            provider="aws",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def add_azure(
        self,
        name: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        batch_name: str | None = None,
        batch_key: str | None = None,
        storage_name: str | None = None,
        storage_key: str | None = None,
    ) -> Credentials:
        """
        Add Azure credentials.

        Args:
            name: Credentials name
            workspace: Workspace ID or "org/workspace" reference
            description: Description
            batch_name: Azure Batch account name
            batch_key: Azure Batch account key
            storage_name: Azure Storage account name
            storage_key: Azure Storage account key

        Returns:
            Created Credentials object
        """
        keys: dict[str, Any] = {}
        if batch_name:
            keys["batchName"] = batch_name
        if batch_key:
            keys["batchKey"] = batch_key
        if storage_name:
            keys["storageName"] = storage_name
        if storage_key:
            keys["storageKey"] = storage_key

        return self._add_credentials(
            name=name,
            provider="azure",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def add_google(
        self,
        name: str,
        key_file: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
    ) -> Credentials:
        """
        Add Google Cloud credentials.

        Args:
            name: Credentials name
            key_file: Path to service account key file or JSON content
            workspace: Workspace ID or "org/workspace" reference
            description: Description

        Returns:
            Created Credentials object
        """
        import json
        from pathlib import Path

        # Read key file if it's a path
        key_content = key_file
        if Path(key_file).exists():
            key_content = Path(key_file).read_text()

        # Parse JSON to validate
        try:
            json.loads(key_content)
        except json.JSONDecodeError:
            pass  # Might already be parsed or in different format

        keys = {"data": key_content}

        return self._add_credentials(
            name=name,
            provider="google",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def add_github(
        self,
        name: str,
        username: str,
        password: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
    ) -> Credentials:
        """
        Add GitHub credentials.

        Args:
            name: Credentials name
            username: GitHub username
            password: GitHub personal access token
            workspace: Workspace ID or "org/workspace" reference
            description: Description

        Returns:
            Created Credentials object
        """
        keys = {
            "username": username,
            "password": password,
        }

        return self._add_credentials(
            name=name,
            provider="github",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def add_container_registry(
        self,
        name: str,
        username: str,
        password: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        registry: str = "docker.io",
    ) -> Credentials:
        """
        Add container registry credentials.

        Args:
            name: Credentials name
            username: Registry username
            password: Registry password or token
            workspace: Workspace ID or "org/workspace" reference
            description: Description
            registry: Registry URL (default: docker.io)

        Returns:
            Created Credentials object
        """
        keys = {
            "userName": username,
            "password": password,
            "registry": registry,
        }

        return self._add_credentials(
            name=name,
            provider="container-reg",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def delete(
        self,
        credentials_id: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete credentials.

        Args:
            credentials_id: Credentials ID
            workspace: Workspace ID or "org/workspace" reference
        """
        params = self._build_params(workspace=workspace)
        self._client.delete(f"/credentials/{credentials_id}", params=params)

    def _add_credentials(
        self,
        name: str,
        provider: str,
        keys: dict[str, Any],
        workspace: str | int | None = None,
        description: str | None = None,
    ) -> Credentials:
        """Internal method to add credentials."""
        params = self._build_params(workspace=workspace)

        payload: dict[str, Any] = {
            "credentials": {
                "name": name,
                "provider": provider,
                "keys": keys,
            }
        }

        if description:
            payload["credentials"]["description"] = description

        response = self._client.post("/credentials", json=payload, params=params)

        # Return a minimal credentials object with the ID
        creds_id = response.get("credentialsId", "")
        return Credentials(
            id=creds_id,
            name=name,
            provider=provider,
        )
