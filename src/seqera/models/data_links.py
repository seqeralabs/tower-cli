"""
Data link models for Seqera SDK.
"""

from datetime import datetime
from typing import Any

from pydantic import Field

from seqera.models.base import SeqeraModel


class DataLink(SeqeraModel):
    """
    Data link entity from the Seqera Platform.

    Data links connect cloud storage locations to workspaces.
    """

    id: str
    name: str
    description: str | None = None
    provider: str | None = None
    resource_ref: str | None = Field(None, alias="resourceRef")
    credentials_id: str | None = Field(None, alias="credentialsId")
    status: str | None = None
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
    config: dict[str, Any] | None = None
