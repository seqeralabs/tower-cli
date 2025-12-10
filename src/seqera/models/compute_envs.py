"""
Compute environment models for Seqera SDK.
"""

from datetime import datetime
from typing import Any

from pydantic import Field

from seqera.models.base import SeqeraModel


class ComputeEnv(SeqeraModel):
    """
    Compute environment entity from the Seqera Platform.

    Compute environments define the infrastructure where Nextflow
    pipelines will be executed.
    """

    id: str
    name: str
    description: str | None = None
    platform: str | None = None
    status: str | None = None
    message: str | None = None
    primary: bool = False
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
    last_used: datetime | None = Field(None, alias="lastUsed")
    credentials_id: str | None = Field(None, alias="credentialsId")
    workspace_id: int | None = Field(None, alias="workspaceId")
    org_id: int | None = Field(None, alias="orgId")

    # Configuration fields (vary by platform)
    config: dict[str, Any] | None = None
    region: str | None = None
    work_dir: str | None = Field(None, alias="workDir")
