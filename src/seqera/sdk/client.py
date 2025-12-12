"""
Seqera Platform SDK Client.

Main entry points for programmatic access to the Seqera Platform API.
"""

from __future__ import annotations

import os
from typing import Any

from seqera.api.client import SeqeraClient
from seqera.sdk.resources.actions import ActionsResource
from seqera.sdk.resources.collaborators import CollaboratorsResource
from seqera.sdk.resources.compute_envs import ComputeEnvsResource
from seqera.sdk.resources.credentials import CredentialsResource
from seqera.sdk.resources.data_links import DataLinksResource
from seqera.sdk.resources.datasets import DatasetsResource
from seqera.sdk.resources.labels import LabelsResource
from seqera.sdk.resources.members import MembersResource
from seqera.sdk.resources.organizations import OrganizationsResource
from seqera.sdk.resources.participants import ParticipantsResource
from seqera.sdk.resources.pipelines import PipelinesResource
from seqera.sdk.resources.runs import RunsResource
from seqera.sdk.resources.secrets import SecretsResource
from seqera.sdk.resources.studios import StudiosResource
from seqera.sdk.resources.teams import TeamsResource
from seqera.sdk.resources.workspaces import WorkspacesResource


class Seqera:
    """
    Seqera Platform SDK Client.

    Main entry point for programmatic access to the Seqera Platform API.
    Provides typed access to all platform resources including pipelines,
    runs, workspaces, and more.

    Example:
        >>> from seqera import Seqera
        >>>
        >>> # Initialize with token from environment
        >>> client = Seqera()
        >>>
        >>> # Or provide credentials explicitly
        >>> client = Seqera(
        ...     access_token="your-token",
        ...     url="https://api.cloud.seqera.io",
        ... )
        >>>
        >>> # List pipelines
        >>> for pipeline in client.pipelines.list():
        ...     print(f"{pipeline.name}: {pipeline.repository}")
        >>>
        >>> # Launch a pipeline
        >>> result = client.pipelines.launch(
        ...     "rnaseq",
        ...     workspace="my-org/my-workspace",
        ...     params={"input": "samples.csv"},
        ... )
        >>> print(f"Launched: {result.workflow_id}")
        >>>
        >>> # Monitor the run
        >>> run = client.runs.get(result.workflow_id)
        >>> print(f"Status: {run.status}")
        >>>
        >>> # Use as context manager for automatic cleanup
        >>> with Seqera() as client:
        ...     pipelines = list(client.pipelines.list())
    """

    def __init__(
        self,
        access_token: str | None = None,
        url: str = "https://api.cloud.seqera.io",
        *,
        workspace: str | int | None = None,
        insecure: bool = False,
        timeout: float = 30.0,
    ) -> None:
        """
        Initialize the Seqera SDK client.

        Args:
            access_token: Seqera Platform access token. If not provided,
                reads from SEQERA_ACCESS_TOKEN or TOWER_ACCESS_TOKEN env vars.
            url: Seqera Platform API URL. Defaults to Seqera Cloud.
                Can also be set via SEQERA_API_ENDPOINT or TOWER_API_ENDPOINT.
            workspace: Default workspace for operations. Can be workspace ID
                or "org/workspace" reference. Can also be set via SEQERA_WORKSPACE.
            insecure: Allow non-SSL connections (not recommended for production).
            timeout: Request timeout in seconds.

        Raises:
            ValueError: If access token is not provided and not found in environment.

        Example:
            >>> # From environment variables
            >>> client = Seqera()
            >>>
            >>> # Explicit configuration
            >>> client = Seqera(
            ...     access_token="tw_...",
            ...     url="https://tower.mycompany.com/api",
            ...     workspace="my-org/production",
            ... )
        """
        # Resolve access token
        if access_token is None:
            access_token = os.environ.get("SEQERA_ACCESS_TOKEN") or os.environ.get(
                "TOWER_ACCESS_TOKEN"
            )

        if not access_token:
            raise ValueError(
                "Access token required. Provide via access_token parameter "
                "or SEQERA_ACCESS_TOKEN environment variable."
            )

        # Resolve API URL
        if url == "https://api.cloud.seqera.io":
            env_url = os.environ.get("SEQERA_API_ENDPOINT") or os.environ.get("TOWER_API_ENDPOINT")
            if env_url:
                url = env_url

        # Resolve default workspace
        if workspace is None:
            env_workspace = os.environ.get("SEQERA_WORKSPACE")
            if env_workspace:
                workspace = env_workspace

        # Create HTTP client
        self._http_client = SeqeraClient(
            base_url=url,
            token=access_token,
            insecure=insecure,
            timeout=timeout,
        )

        self._default_workspace = workspace

        # Initialize resource instances
        self._pipelines = PipelinesResource(self._http_client, workspace)
        self._runs = RunsResource(self._http_client, workspace)
        self._workspaces = WorkspacesResource(self._http_client, workspace)
        self._organizations = OrganizationsResource(self._http_client, workspace)
        self._credentials = CredentialsResource(self._http_client, workspace)
        self._compute_envs = ComputeEnvsResource(self._http_client, workspace)
        self._secrets = SecretsResource(self._http_client, workspace)
        self._labels = LabelsResource(self._http_client, workspace)
        self._datasets = DatasetsResource(self._http_client, workspace)
        self._teams = TeamsResource(self._http_client, workspace)
        self._members = MembersResource(self._http_client, workspace)
        self._participants = ParticipantsResource(self._http_client, workspace)
        self._actions = ActionsResource(self._http_client, workspace)
        self._studios = StudiosResource(self._http_client, workspace)
        self._collaborators = CollaboratorsResource(self._http_client, workspace)
        self._data_links = DataLinksResource(self._http_client, workspace)

    @property
    def pipelines(self) -> PipelinesResource:
        """
        Access pipelines resource.

        Manage pipelines in workspaces - list, create, update, delete, and launch.

        Example:
            >>> for p in client.pipelines.list():
            ...     print(p.name)
            >>> result = client.pipelines.launch("rnaseq")
        """
        return self._pipelines

    @property
    def runs(self) -> RunsResource:
        """
        Access workflow runs resource.

        Monitor and manage workflow executions - list, view, cancel, delete.

        Example:
            >>> for run in client.runs.list():
            ...     print(f"{run.run_name}: {run.status}")
            >>> client.runs.cancel("abc123")
        """
        return self._runs

    @property
    def workspaces(self) -> WorkspacesResource:
        """
        Access workspaces resource.

        Manage workspaces within organizations.

        Example:
            >>> for ws in client.workspaces.list():
            ...     print(f"{ws.org_name}/{ws.workspace_name}")
        """
        return self._workspaces

    @property
    def organizations(self) -> OrganizationsResource:
        """
        Access organizations resource.

        Manage organizations - list, create, update, delete.

        Example:
            >>> for org in client.organizations.list():
            ...     print(org.name)
        """
        return self._organizations

    @property
    def credentials(self) -> CredentialsResource:
        """
        Access credentials resource.

        Manage cloud and repository credentials.

        Example:
            >>> client.credentials.add_aws("my-creds", access_key, secret_key)
        """
        return self._credentials

    @property
    def compute_envs(self) -> ComputeEnvsResource:
        """
        Access compute environments resource.

        Manage compute environments for pipeline execution.

        Example:
            >>> primary = client.compute_envs.get_primary()
            >>> print(f"Primary: {primary.name}")
        """
        return self._compute_envs

    @property
    def secrets(self) -> SecretsResource:
        """
        Access pipeline secrets resource.

        Manage secrets for pipeline execution.

        Example:
            >>> client.secrets.add("MY_SECRET", "secret-value")
        """
        return self._secrets

    @property
    def labels(self) -> LabelsResource:
        """
        Access labels resource.

        Manage workspace labels for organizing resources.

        Example:
            >>> client.labels.add("environment", value="production")
        """
        return self._labels

    @property
    def datasets(self) -> DatasetsResource:
        """
        Access datasets resource.

        Manage versioned datasets for pipeline inputs.

        Example:
            >>> for ds in client.datasets.list():
            ...     print(ds.name)
        """
        return self._datasets

    @property
    def teams(self) -> TeamsResource:
        """
        Access teams resource.

        Manage teams within organizations.

        Example:
            >>> team = client.teams.add("dev-team", organization="my-org")
        """
        return self._teams

    @property
    def members(self) -> MembersResource:
        """
        Access organization members resource.

        Manage organization membership.

        Example:
            >>> client.members.add("user@example.com", organization="my-org")
        """
        return self._members

    @property
    def participants(self) -> ParticipantsResource:
        """
        Access workspace participants resource.

        Manage workspace access for members and teams.

        Example:
            >>> client.participants.add("user@example.com", workspace="org/ws")
        """
        return self._participants

    @property
    def actions(self) -> ActionsResource:
        """
        Access pipeline actions resource.

        Manage automation actions and webhooks.

        Example:
            >>> for action in client.actions.list():
            ...     print(action.name)
        """
        return self._actions

    @property
    def studios(self) -> StudiosResource:
        """
        Access Data Studios resource.

        Manage interactive analysis environments.

        Example:
            >>> client.studios.start("studio-123")
        """
        return self._studios

    @property
    def collaborators(self) -> CollaboratorsResource:
        """
        Access organization collaborators resource.

        Manage external collaborator access.

        Example:
            >>> client.collaborators.add("external@example.com", organization="my-org")
        """
        return self._collaborators

    @property
    def data_links(self) -> DataLinksResource:
        """
        Access data links resource.

        Manage cloud storage connections.

        Example:
            >>> client.data_links.add("my-bucket", "aws", "s3://my-bucket")
        """
        return self._data_links

    def info(self) -> dict[str, Any]:
        """
        Get API information and health status.

        Returns:
            Dictionary containing API version and status information.

        Example:
            >>> info = client.info()
            >>> print(f"API Version: {info.get('apiVersion')}")
        """
        return self._http_client.get("/service-info")

    def user_info(self) -> dict[str, Any]:
        """
        Get current user information.

        Returns:
            Dictionary containing user details.

        Example:
            >>> user = client.user_info()
            >>> print(f"Logged in as: {user.get('user', {}).get('userName')}")
        """
        return self._http_client.get("/user-info")

    def close(self) -> None:
        """
        Close the HTTP client and release resources.

        Called automatically when using as a context manager.
        """
        self._http_client.close()

    def __enter__(self) -> Seqera:
        """Context manager entry."""
        return self

    def __exit__(self, *args: Any) -> None:
        """Context manager exit."""
        self.close()

    def __repr__(self) -> str:
        """String representation."""
        return f"Seqera(url={self._http_client.base_url!r})"


class AsyncSeqera:
    """
    Async Seqera Platform SDK Client.

    Provides asynchronous access to the Seqera Platform API using httpx's
    async capabilities. All methods are async and return awaitable results.

    Example:
        >>> from seqera import AsyncSeqera
        >>> import asyncio
        >>>
        >>> async def main():
        ...     async with AsyncSeqera() as client:
        ...         async for pipeline in client.pipelines.list():
        ...             print(pipeline.name)
        >>>
        >>> asyncio.run(main())
    """

    def __init__(
        self,
        access_token: str | None = None,
        url: str = "https://api.cloud.seqera.io",
        *,
        workspace: str | int | None = None,
        insecure: bool = False,
        timeout: float = 30.0,
    ) -> None:
        """
        Initialize the async Seqera SDK client.

        Args:
            access_token: Seqera Platform access token. If not provided,
                reads from SEQERA_ACCESS_TOKEN or TOWER_ACCESS_TOKEN env vars.
            url: Seqera Platform API URL.
            workspace: Default workspace for operations.
            insecure: Allow non-SSL connections.
            timeout: Request timeout in seconds.
        """
        import httpx

        # Resolve access token
        if access_token is None:
            access_token = os.environ.get("SEQERA_ACCESS_TOKEN") or os.environ.get(
                "TOWER_ACCESS_TOKEN"
            )

        if not access_token:
            raise ValueError(
                "Access token required. Provide via access_token parameter "
                "or SEQERA_ACCESS_TOKEN environment variable."
            )

        # Resolve API URL
        if url == "https://api.cloud.seqera.io":
            env_url = os.environ.get("SEQERA_API_ENDPOINT") or os.environ.get("TOWER_API_ENDPOINT")
            if env_url:
                url = env_url

        # Resolve default workspace
        if workspace is None:
            env_workspace = os.environ.get("SEQERA_WORKSPACE")
            if env_workspace:
                workspace = env_workspace

        self._base_url = url.rstrip("/")
        self._default_workspace = workspace

        # Determine the scheme
        if insecure and not self._base_url.startswith("http"):
            self._base_url = f"http://{self._base_url}"
        elif not self._base_url.startswith("http"):
            self._base_url = f"https://{self._base_url}"

        self._client = httpx.AsyncClient(
            base_url=self._base_url,
            headers={
                "Authorization": f"Bearer {access_token}",
                "Content-Type": "application/json",
                "Accept": "application/json",
            },
            timeout=timeout,
            verify=not insecure,
        )

        # Note: Async resources would need to be implemented separately
        # For now, provide basic async HTTP methods
        self._pipelines: AsyncPipelinesResource | None = None
        self._runs: AsyncRunsResource | None = None

    @property
    def pipelines(self) -> AsyncPipelinesResource:
        """Access async pipelines resource."""
        if self._pipelines is None:
            self._pipelines = AsyncPipelinesResource(self._client, self._default_workspace)
        return self._pipelines

    @property
    def runs(self) -> AsyncRunsResource:
        """Access async runs resource."""
        if self._runs is None:
            self._runs = AsyncRunsResource(self._client, self._default_workspace)
        return self._runs

    async def info(self) -> dict[str, Any]:
        """Get API information asynchronously."""
        response = await self._client.get("/service-info")
        response.raise_for_status()
        return response.json()

    async def close(self) -> None:
        """Close the async HTTP client."""
        await self._client.aclose()

    async def __aenter__(self) -> AsyncSeqera:
        """Async context manager entry."""
        return self

    async def __aexit__(self, *args: Any) -> None:
        """Async context manager exit."""
        await self.close()


class AsyncPipelinesResource:
    """Async pipelines resource (basic implementation)."""

    def __init__(self, client: Any, default_workspace: str | int | None) -> None:
        self._client = client
        self._default_workspace = default_workspace

    async def list(
        self,
        workspace: str | int | None = None,
    ) -> AsyncPaginatedList:
        """List pipelines asynchronously."""
        from seqera.models.pipelines import Pipeline

        async def fetch_page(offset: int, limit: int) -> tuple[list[Pipeline], int]:
            params: dict[str, Any] = {"offset": offset, "max": limit}
            ws = workspace or self._default_workspace
            if ws:
                params["workspaceId"] = ws

            response = await self._client.get("/pipelines", params=params)
            response.raise_for_status()
            data = response.json()

            pipelines = [Pipeline.model_validate(p) for p in data.get("pipelines", [])]
            total_size = data.get("totalSize", len(pipelines))
            return pipelines, total_size

        return AsyncPaginatedList(fetch_page)


class AsyncRunsResource:
    """Async runs resource (basic implementation)."""

    def __init__(self, client: Any, default_workspace: str | int | None) -> None:
        self._client = client
        self._default_workspace = default_workspace

    async def list(
        self,
        workspace: str | int | None = None,
    ) -> AsyncPaginatedList:
        """List runs asynchronously."""
        from seqera.models.runs import Workflow

        async def fetch_page(offset: int, limit: int) -> tuple[list[Workflow], int]:
            params: dict[str, Any] = {"offset": offset, "max": limit}
            ws = workspace or self._default_workspace
            if ws:
                params["workspaceId"] = ws

            response = await self._client.get("/workflow", params=params)
            response.raise_for_status()
            data = response.json()

            workflows = []
            for item in data.get("workflows", []):
                wf_data = item.get("workflow", item) if isinstance(item, dict) else item
                workflows.append(Workflow.model_validate(wf_data))

            total_size = data.get("totalSize", len(workflows))
            return workflows, total_size

        return AsyncPaginatedList(fetch_page)


class AsyncPaginatedList:
    """Async paginated list that supports async iteration."""

    def __init__(
        self,
        fetch_page: Any,
        page_size: int = 50,
    ) -> None:
        self._fetch_page = fetch_page
        self._page_size = page_size

    def __aiter__(self) -> AsyncPaginatedList:
        self._items: list[Any] = []
        self._offset = 0
        self._index = 0
        self._exhausted = False
        self._total_size: int | None = None
        return self

    async def __anext__(self) -> Any:
        # Fetch more if needed
        while self._index >= len(self._items):
            if self._exhausted:
                raise StopAsyncIteration

            items, total_size = await self._fetch_page(self._offset, self._page_size)
            self._total_size = total_size

            if not items:
                self._exhausted = True
                raise StopAsyncIteration

            self._items.extend(items)
            self._offset += len(items)

            if self._offset >= total_size:
                self._exhausted = True

        item = self._items[self._index]
        self._index += 1
        return item
