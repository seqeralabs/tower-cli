"""
Studio models for Seqera SDK.
"""

from datetime import datetime
from typing import Any

from pydantic import Field

from seqera.models.base import SeqeraModel


class Studio(SeqeraModel):
    """
    Data Studio entity from the Seqera Platform.

    Studios are interactive analysis environments that can be started
    and stopped on demand.
    """

    id: str = Field(alias="sessionId")
    name: str | None = None
    description: str | None = None
    compute_env_id: str | None = Field(None, alias="computeEnvId")
    status: str | None = None
    template: str | None = None
    user_name: str | None = Field(None, alias="userName")
    workspace_id: int | None = Field(None, alias="workspaceId")
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
    mount_paths: list[str] | None = Field(None, alias="mountPaths")
    configuration: dict[str, Any] | None = None

    @property
    def session_id(self) -> str:
        """Alias for id for consistency with API responses."""
        return self.id


class StudioCheckpoint(SeqeraModel):
    """
    Studio checkpoint entity from the Seqera Platform.

    Checkpoints are saved states of a studio session.
    """

    id: str = Field(alias="checkpointId")
    parent: str | None = None
    session_id: str | None = Field(None, alias="sessionId")
    status: str | None = None
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")

    @property
    def checkpoint_id(self) -> str:
        """Alias for id for consistency with API responses."""
        return self.id
