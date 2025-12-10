"""
Participant models for Seqera SDK.
"""

from datetime import datetime

from pydantic import Field

from seqera.models.base import SeqeraModel


class Participant(SeqeraModel):
    """
    Workspace participant entity from the Seqera Platform.

    Participants are members or teams that have been granted access
    to a specific workspace.
    """

    participant_id: int = Field(alias="participantId")
    member_id: int | None = Field(None, alias="memberId")
    team_id: int | None = Field(None, alias="teamId")
    user_name: str | None = Field(None, alias="userName")
    email: str | None = None
    first_name: str | None = Field(None, alias="firstName")
    last_name: str | None = Field(None, alias="lastName")
    team_name: str | None = Field(None, alias="teamName")
    team_avatar_url: str | None = Field(None, alias="teamAvatarUrl")
    avatar_url: str | None = Field(None, alias="avatarUrl")
    ws_role: str | None = Field(None, alias="wspRole")
    type: str | None = None
    date_created: datetime | None = Field(None, alias="dateCreated")

    @property
    def id(self) -> int:
        """Alias for participant_id for convenience."""
        return self.participant_id
