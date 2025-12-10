"""
Workspace models for Seqera SDK.
"""

from datetime import datetime

from pydantic import Field

from seqera.models.base import SeqeraModel


class Workspace(SeqeraModel):
    """
    Workspace entity from the Seqera Platform.

    A workspace is a collaborative environment within an organization
    where teams can manage pipelines, compute environments, and runs.
    """

    id: int
    name: str
    full_name: str | None = Field(None, alias="fullName")
    description: str | None = None
    visibility: str | None = None
    date_created: datetime | None = Field(None, alias="dateCreated")
    last_updated: datetime | None = Field(None, alias="lastUpdated")


class OrgAndWorkspace(SeqeraModel):
    """
    Combined organization and workspace information.

    This model represents the entries returned by the user workspaces endpoint,
    which includes both organization and workspace details.
    """

    workspace_id: int | None = Field(None, alias="workspaceId")
    workspace_name: str | None = Field(None, alias="workspaceName")
    workspace_full_name: str | None = Field(None, alias="workspaceFullName")
    org_id: int | None = Field(None, alias="orgId")
    org_name: str | None = Field(None, alias="orgName")
    org_logo_url: str | None = Field(None, alias="orgLogoUrl")
    roles: list[str] | None = None

    @property
    def ref(self) -> str:
        """Get workspace reference string (org_name/workspace_name)."""
        if self.org_name and self.workspace_name:
            return f"{self.org_name}/{self.workspace_name}"
        return self.workspace_name or ""
