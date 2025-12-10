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
        from seqera.exceptions import NotFoundError

        params = self._build_params(workspace=workspace)
        try:
            response = self._client.get(f"/credentials/{credentials_id}", params=params)
        except NotFoundError:
            raise CredentialsNotFoundException(credentials_id, str(workspace or "user"))

        creds_data = response.get("credentials", {})
        if not creds_data:
            raise CredentialsNotFoundException(credentials_id, str(workspace or "user"))

        return Credentials.model_validate(creds_data)

    def add_aws(
        self,
        name: str,
        workspace: str | int | None = None,
        *,
        access_key: str | None = None,
        secret_key: str | None = None,
        description: str | None = None,
        assume_role_arn: str | None = None,
    ) -> Credentials:
        """
        Add AWS credentials.

        Args:
            name: Credentials name
            workspace: Workspace ID or "org/workspace" reference
            access_key: AWS access key ID
            secret_key: AWS secret access key
            description: Description
            assume_role_arn: IAM role ARN to assume

        Returns:
            Created Credentials object
        """
        keys: dict[str, Any] = {}
        if access_key and secret_key:
            keys["accessKey"] = access_key
            keys["secretKey"] = secret_key
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

    def add_gitlab(
        self,
        name: str,
        username: str,
        password: str,
        token: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
    ) -> Credentials:
        """
        Add GitLab credentials.

        Args:
            name: Credentials name
            username: GitLab username
            password: GitLab password
            token: GitLab access token
            workspace: Workspace ID or "org/workspace" reference
            description: Description

        Returns:
            Created Credentials object
        """
        keys = {
            "username": username,
            "password": password,
            "token": token,
        }

        return self._add_credentials(
            name=name,
            provider="gitlab",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def add_gitea(
        self,
        name: str,
        username: str,
        password: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
    ) -> Credentials:
        """
        Add Gitea credentials.

        Args:
            name: Credentials name
            username: Gitea username
            password: Gitea password
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
            provider="gitea",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def add_bitbucket(
        self,
        name: str,
        username: str,
        password: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
    ) -> Credentials:
        """
        Add Bitbucket credentials.

        Args:
            name: Credentials name
            username: Bitbucket username
            password: Bitbucket App password
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
            provider="bitbucket",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def add_codecommit(
        self,
        name: str,
        access_key: str,
        secret_key: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        base_url: str | None = None,
    ) -> Credentials:
        """
        Add CodeCommit credentials.

        Args:
            name: Credentials name
            access_key: AWS access key
            secret_key: AWS secret key
            workspace: Workspace ID or "org/workspace" reference
            description: Description
            base_url: Repository base URL

        Returns:
            Created Credentials object
        """
        keys = {
            "username": access_key,
            "password": secret_key,
        }

        return self._add_credentials(
            name=name,
            provider="codecommit",
            keys=keys,
            workspace=workspace,
            description=description,
            base_url=base_url,
        )

    def add_ssh(
        self,
        name: str,
        private_key: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        passphrase: str | None = None,
    ) -> Credentials:
        """
        Add SSH credentials.

        Args:
            name: Credentials name
            private_key: SSH private key content or path to file
            workspace: Workspace ID or "org/workspace" reference
            description: Description
            passphrase: Private key passphrase

        Returns:
            Created Credentials object
        """
        from pathlib import Path

        # Read key file if it's a path
        key_content = private_key
        if Path(private_key).exists():
            key_content = Path(private_key).read_text()

        keys: dict[str, Any] = {"privateKey": key_content}
        if passphrase:
            keys["passphrase"] = passphrase

        return self._add_credentials(
            name=name,
            provider="ssh",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def add_k8s(
        self,
        name: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        token: str | None = None,
        certificate: str | None = None,
        private_key: str | None = None,
    ) -> Credentials:
        """
        Add Kubernetes credentials.

        Args:
            name: Credentials name
            workspace: Workspace ID or "org/workspace" reference
            description: Description
            token: Service account token (alternative to certificate+private_key)
            certificate: Client certificate content or path
            private_key: Client key content or path

        Returns:
            Created Credentials object
        """
        from pathlib import Path

        keys: dict[str, Any] = {}

        if token:
            keys["token"] = token
        elif certificate and private_key:
            # Read files if they're paths
            cert_content = certificate
            if Path(certificate).exists():
                cert_content = Path(certificate).read_text()

            key_content = private_key
            if Path(private_key).exists():
                key_content = Path(private_key).read_text()

            keys["certificate"] = cert_content
            keys["privateKey"] = key_content

        return self._add_credentials(
            name=name,
            provider="k8s",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def add_agent(
        self,
        name: str,
        connection_id: str,
        workspace: str | int | None = None,
        *,
        description: str | None = None,
        work_dir: str = "$TW_AGENT_WORK",
    ) -> Credentials:
        """
        Add Tower Agent credentials.

        Args:
            name: Credentials name
            connection_id: Agent connection identifier
            workspace: Workspace ID or "org/workspace" reference
            description: Description
            work_dir: Default work directory

        Returns:
            Created Credentials object
        """
        keys = {
            "connectionId": connection_id,
            "workDir": work_dir,
        }

        return self._add_credentials(
            name=name,
            provider="tw-agent",
            keys=keys,
            workspace=workspace,
            description=description,
        )

    def update(
        self,
        credentials_id: str,
        workspace: str | int | None = None,
        *,
        keys: dict[str, Any] | None = None,
    ) -> Credentials:
        """
        Update credentials.

        Args:
            credentials_id: Credentials ID
            workspace: Workspace ID or "org/workspace" reference
            keys: New credential keys

        Returns:
            Updated Credentials object
        """
        params = self._build_params(workspace=workspace)

        # Fetch existing credentials
        existing = self.get(credentials_id, workspace=workspace)

        payload: dict[str, Any] = {
            "credentials": {
                "id": credentials_id,
                "name": existing.name,
                "provider": existing.provider_type,  # Use provider_type to handle discriminator
            }
        }

        if keys:
            payload["credentials"]["keys"] = keys

        self._client.put(f"/credentials/{credentials_id}", json=payload, params=params)

        return existing

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
        base_url: str | None = None,
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
        if base_url:
            payload["credentials"]["baseUrl"] = base_url

        response = self._client.post("/credentials", json=payload, params=params)

        # Return a minimal credentials object with the ID
        creds_id = response.get("credentialsId", "")
        return Credentials(
            id=creds_id,
            name=name,
            provider=provider,
        )
