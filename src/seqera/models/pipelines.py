"""
Pipeline models for Seqera SDK.
"""

from datetime import datetime
from typing import Any

from pydantic import Field

from seqera.models.base import SeqeraModel


class Pipeline(SeqeraModel):
    """
    Pipeline entity from the Seqera Platform.

    A pipeline represents a pre-configured Nextflow workflow that can be
    launched on demand.
    """

    pipeline_id: int = Field(alias="pipelineId")
    name: str
    description: str | None = None
    icon: str | None = None
    repository: str = Field(alias="pipeline")
    compute_env_id: str | None = Field(None, alias="computeEnvId")
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
    workspace_id: int | None = Field(None, alias="workspaceId")
    visibility: str | None = None
    optimized: bool | None = None
    labels: list[dict[str, Any]] | None = None

    @property
    def id(self) -> int:
        """Alias for pipeline_id for convenience."""
        return self.pipeline_id


class LaunchInfo(SeqeraModel):
    """
    Launch configuration for a pipeline.

    Contains all the settings needed to launch a pipeline run.
    """

    id: str | None = None
    compute_env_id: str | None = Field(None, alias="computeEnvId")
    pipeline: str | None = None
    work_dir: str | None = Field(None, alias="workDir")
    revision: str | None = None
    config_profiles: list[str] | None = Field(None, alias="configProfiles")
    params_text: str | None = Field(None, alias="paramsText")
    pre_run_script: str | None = Field(None, alias="preRunScript")
    post_run_script: str | None = Field(None, alias="postRunScript")
    pull_latest: bool = Field(False, alias="pullLatest")
    stub_run: bool = Field(False, alias="stubRun")
    main_script: str | None = Field(None, alias="mainScript")
    entry_name: str | None = Field(None, alias="entryName")
    schema_name: str | None = Field(None, alias="schemaName")
    resume: bool = False
    resume_launch_id: str | None = Field(None, alias="resumeLaunchId")
    user_secrets: list[str] | None = Field(None, alias="userSecrets")
    workspace_secrets: list[str] | None = Field(None, alias="workspaceSecrets")
    date_created: datetime | None = Field(None, alias="dateCreated")


class LaunchResult(SeqeraModel):
    """
    Result of launching a pipeline.

    Contains the workflow ID that can be used to track the run.
    """

    workflow_id: str = Field(alias="workflowId")

    @property
    def id(self) -> str:
        """Alias for workflow_id for convenience."""
        return self.workflow_id
