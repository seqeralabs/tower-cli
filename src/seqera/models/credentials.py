"""
Credentials models for Seqera SDK.
"""

from datetime import datetime
from typing import Any

from pydantic import Field

from seqera.models.base import SeqeraModel


class Credentials(SeqeraModel):
    """
    Credentials entity from the Seqera Platform.

    Credentials store authentication information for various cloud providers,
    git repositories, and container registries.
    """

    id: str
    name: str
    description: str | None = None
    provider: str | None = None
    base_url: str | None = Field(None, alias="baseUrl")
    category: str | None = None
    deleted: bool | None = None
    last_used: datetime | None = Field(None, alias="lastUsed")
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
    keys: dict[str, Any] | None = None
