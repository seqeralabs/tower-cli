"""
Member models for Seqera SDK.
"""

from datetime import datetime

from pydantic import Field

from seqera.models.base import SeqeraModel


class Member(SeqeraModel):
    """
    Organization member entity from the Seqera Platform.

    Members are users who belong to an organization.
    """

    member_id: int = Field(alias="memberId")
    user_name: str | None = Field(None, alias="userName")
    email: str | None = None
    first_name: str | None = Field(None, alias="firstName")
    last_name: str | None = Field(None, alias="lastName")
    avatar: str | None = None
    avatar_url: str | None = Field(None, alias="avatarUrl")
    role: str | None = None
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")

    @property
    def id(self) -> int:
        """Alias for member_id for convenience."""
        return self.member_id
