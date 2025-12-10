"""
Collaborator models for Seqera SDK.
"""

from pydantic import Field

from seqera.models.base import SeqeraModel


class Collaborator(SeqeraModel):
    """
    Organization collaborator entity from the Seqera Platform.

    Collaborators are external users who have been invited to
    participate in an organization's workspaces.
    """

    id: int = Field(alias="memberId")
    user_name: str | None = Field(None, alias="userName")
    email: str | None = None
    first_name: str | None = Field(None, alias="firstName")
    last_name: str | None = Field(None, alias="lastName")
    avatar_url: str | None = Field(None, alias="avatarUrl")

    @property
    def member_id(self) -> int:
        """Alias for id for consistency with API responses."""
        return self.id
