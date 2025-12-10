"""
Secrets resource for the Seqera SDK.
"""

from __future__ import annotations

from seqera.models.common import PaginatedList
from seqera.models.secrets import Secret
from seqera.sdk.resources.base import BaseResource


class SecretsResource(BaseResource):
    """
    SDK resource for managing pipeline secrets.

    Secrets store sensitive values that can be used in pipeline executions.
    """

    def list(
        self,
        workspace: str | int | None = None,
    ) -> PaginatedList[Secret]:
        """
        List secrets in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of Secret objects
        """

        def fetch_page(offset: int, limit: int) -> tuple[list[Secret], int]:
            params = self._build_params(workspace=workspace)
            response = self._client.get("/pipeline-secrets", params=params)

            secrets = [Secret.model_validate(s) for s in response.get("pipelineSecrets", [])]
            total_size = len(secrets)
            paginated = secrets[offset : offset + limit]
            return paginated, total_size

        return PaginatedList(fetch_page)

    def get(
        self,
        name: str,
        workspace: str | int | None = None,
    ) -> Secret:
        """
        Get a secret by name.

        Args:
            name: Secret name
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Secret object

        Raises:
            Exception: If secret not found
        """
        for secret in self.list(workspace=workspace):
            if secret.name == name:
                return secret

        raise Exception(f"Secret '{name}' not found")

    def add(
        self,
        name: str,
        value: str,
        workspace: str | int | None = None,
    ) -> Secret:
        """
        Add a new secret.

        Args:
            name: Secret name
            value: Secret value
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Created Secret object
        """
        params = self._build_params(workspace=workspace)

        payload = {
            "name": name,
            "value": value,
        }

        response = self._client.post("/pipeline-secrets", json=payload, params=params)

        return Secret(
            id=response.get("secretId", 0),
            name=name,
        )

    def update(
        self,
        name: str,
        value: str,
        workspace: str | int | None = None,
    ) -> Secret:
        """
        Update an existing secret.

        Args:
            name: Secret name
            value: New secret value
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Updated Secret object
        """
        # Find secret by name to get ID
        secret = self.get(name, workspace=workspace)

        params = self._build_params(workspace=workspace)

        payload = {
            "value": value,
        }

        self._client.put(f"/pipeline-secrets/{secret.id}", json=payload, params=params)

        return secret

    def delete(
        self,
        name: str,
        workspace: str | int | None = None,
    ) -> None:
        """
        Delete a secret.

        Args:
            name: Secret name
            workspace: Workspace ID or "org/workspace" reference
        """
        # Find secret by name to get ID
        secret = self.get(name, workspace=workspace)

        params = self._build_params(workspace=workspace)
        self._client.delete(f"/pipeline-secrets/{secret.id}", params=params)
