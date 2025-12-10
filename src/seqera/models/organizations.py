"""
Organization models for Seqera SDK.
"""

from datetime import datetime

from pydantic import Field

from seqera.models.base import SeqeraModel


class Organization(SeqeraModel):
    """
    Organization entity from the Seqera Platform.

    Organizations are the top-level containers that hold workspaces,
    teams, and members.
    """

    org_id: int = Field(alias="orgId")
    name: str
    full_name: str | None = Field(None, alias="fullName")
    description: str | None = None
    logo_id: str | None = Field(None, alias="logoId")
    logo_url: str | None = Field(None, alias="logoUrl")
    website: str | None = None
    location: str | None = None
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
    member_id: int | None = Field(None, alias="memberId")
    member_role: str | None = Field(None, alias="memberRole")

    @property
    def id(self) -> int:
        """Alias for org_id for convenience."""
        return self.org_id
