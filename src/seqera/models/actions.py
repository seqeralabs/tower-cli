"""
Action models for Seqera SDK.
"""

from datetime import datetime
from typing import Any

from pydantic import Field

from seqera.models.base import SeqeraModel


class Action(SeqeraModel):
    """
    Pipeline action entity from the Seqera Platform.

    Actions are automation triggers that can launch pipelines in response
    to events like webhooks.
    """

    id: str
    name: str
    pipeline_id: int | None = Field(None, alias="pipelineId")
    source: str | None = None
    event: str | None = None
    status: str | None = None
    hook_id: str | None = Field(None, alias="hookId")
    hook_url: str | None = Field(None, alias="hookUrl")
    last_seen: datetime | None = Field(None, alias="lastSeen")
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
    endpoint: str | None = None
    launch: dict[str, Any] | None = None
    labels: list[dict[str, Any]] | None = None
