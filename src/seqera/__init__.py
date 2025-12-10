"""
Seqera Platform SDK and CLI.

This package provides both a command-line interface and a programmatic Python SDK
for interacting with the Seqera Platform (formerly Nextflow Tower).

SDK Usage:
    >>> from seqera import Seqera
    >>>
    >>> # Initialize client (reads token from SEQERA_ACCESS_TOKEN env var)
    >>> client = Seqera()
    >>>
    >>> # List pipelines
    >>> for pipeline in client.pipelines.list():
    ...     print(f"{pipeline.name}: {pipeline.repository}")
    >>>
    >>> # Launch a pipeline
    >>> result = client.pipelines.launch(
    ...     "rnaseq",
    ...     workspace="my-org/production",
    ...     params={"input": "samples.csv"},
    ... )
    >>> print(f"Launched: {result.workflow_id}")
    >>>
    >>> # Monitor the run
    >>> run = client.runs.get(result.workflow_id)
    >>> print(f"Status: {run.status}")

Async Usage:
    >>> from seqera import AsyncSeqera
    >>> import asyncio
    >>>
    >>> async def main():
    ...     async with AsyncSeqera() as client:
    ...         async for pipeline in client.pipelines.list():
    ...             print(pipeline.name)
    >>>
    >>> asyncio.run(main())

CLI Usage:
    $ seqera pipelines list
    $ seqera runs view -i <run-id>
    $ seqera --help

Environment Variables:
    SEQERA_ACCESS_TOKEN: API access token (required)
    SEQERA_API_ENDPOINT: API URL (default: https://api.cloud.seqera.io)
    SEQERA_WORKSPACE: Default workspace (org/workspace format)
"""

from seqera.sdk.client import Seqera, AsyncSeqera

# Models - for type annotations
from seqera.models.pipelines import Pipeline, LaunchInfo, LaunchResult
from seqera.models.runs import Workflow, Task, WorkflowProgress
from seqera.models.workspaces import Workspace, OrgAndWorkspace
from seqera.models.organizations import Organization
from seqera.models.credentials import Credentials
from seqera.models.compute_envs import ComputeEnv
from seqera.models.secrets import Secret
from seqera.models.labels import Label
from seqera.models.datasets import Dataset, DatasetVersion
from seqera.models.teams import Team, TeamMember
from seqera.models.members import Member
from seqera.models.participants import Participant
from seqera.models.actions import Action
from seqera.models.studios import Studio, StudioCheckpoint
from seqera.models.collaborators import Collaborator
from seqera.models.data_links import DataLink
from seqera.models.common import PaginatedList

# Exceptions - for error handling
from seqera.exceptions import (
    SeqeraError,
    ApiError,
    AuthenticationError,
    NotFoundError,
    ValidationError,
    CredentialsNotFoundException,
    ComputeEnvNotFoundException,
    WorkspaceNotFoundException,
    OrganizationNotFoundException,
    PipelineNotFoundException,
    RunNotFoundException,
    ActionNotFoundException,
    MultiplePipelinesFoundException,
    NoComputeEnvironmentException,
)

__version__ = "0.2.0"

__all__ = [
    # Main clients
    "Seqera",
    "AsyncSeqera",
    # Models
    "Pipeline",
    "LaunchInfo",
    "LaunchResult",
    "Workflow",
    "Task",
    "WorkflowProgress",
    "Workspace",
    "OrgAndWorkspace",
    "Organization",
    "Credentials",
    "ComputeEnv",
    "Secret",
    "Label",
    "Dataset",
    "DatasetVersion",
    "Team",
    "TeamMember",
    "Member",
    "Participant",
    "Action",
    "Studio",
    "StudioCheckpoint",
    "Collaborator",
    "DataLink",
    "PaginatedList",
    # Exceptions
    "SeqeraError",
    "ApiError",
    "AuthenticationError",
    "NotFoundError",
    "ValidationError",
    "CredentialsNotFoundException",
    "ComputeEnvNotFoundException",
    "WorkspaceNotFoundException",
    "OrganizationNotFoundException",
    "PipelineNotFoundException",
    "RunNotFoundException",
    "ActionNotFoundException",
    "MultiplePipelinesFoundException",
    "NoComputeEnvironmentException",
]
