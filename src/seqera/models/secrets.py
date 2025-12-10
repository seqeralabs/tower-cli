"""
Secret models for Seqera SDK.
"""

from datetime import datetime

from pydantic import Field

from seqera.models.base import SeqeraModel


class Secret(SeqeraModel):
    """
    Pipeline secret entity from the Seqera Platform.

    Secrets store sensitive values that can be used in pipeline executions.
    """

    id: int
    name: str
    last_used: datetime | None = Field(None, alias="lastUsed")
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
