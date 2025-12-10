"""
Team models for Seqera SDK.
"""

from datetime import datetime

from pydantic import Field

from seqera.models.base import SeqeraModel


class Team(SeqeraModel):
    """
    Team entity from the Seqera Platform.

    Teams are groups of members within an organization that can be
    granted access to workspaces.
    """

    id: int = Field(alias="teamId")
    name: str
    description: str | None = None
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")
    org_id: int | None = Field(None, alias="orgId")
    avatar_url: str | None = Field(None, alias="avatarUrl")
    members_count: int | None = Field(None, alias="membersCount")

    @property
    def team_id(self) -> int:
        """Alias for id for consistency with API responses."""
        return self.id


class TeamMember(SeqeraModel):
    """
    Team member entity from the Seqera Platform.

    Represents a user's membership in a team.
    """

    member_id: int = Field(alias="memberId")
    user_name: str | None = Field(None, alias="userName")
    email: str | None = None
    first_name: str | None = Field(None, alias="firstName")
    last_name: str | None = Field(None, alias="lastName")
    avatar: str | None = None
    avatar_url: str | None = Field(None, alias="avatarUrl")

    @property
    def id(self) -> int:
        """Alias for member_id for convenience."""
        return self.member_id
