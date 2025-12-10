"""
Participants resource for the Seqera SDK.
"""

from __future__ import annotations

from seqera.models.common import PaginatedList
from seqera.models.participants import Participant
from seqera.sdk.resources.base import BaseResource


class ParticipantsResource(BaseResource):
    """
    SDK resource for managing workspace participants.

    Participants are members or teams that have been granted access
    to a specific workspace.
    """

    def list(
        self,
        workspace: str | int,
    ) -> PaginatedList[Participant]:
        """
        List participants in a workspace.

        Args:
            workspace: Workspace ID or "org/workspace" reference

        Returns:
            Auto-paginating iterator of Participant objects
        """
        ws_id = self._resolve_workspace_id(workspace)

        # Find org ID for this workspace
        org_id = self._get_org_id_for_workspace(ws_id)

        def fetch_page(offset: int, limit: int) -> tuple[list[Participant], int]:
            params = {"offset": offset, "max": limit}
            response = self._client.get(
                f"/orgs/{org_id}/workspaces/{ws_id}/participants",
                params=params,
            )

            participants = [Participant.model_validate(p) for p in response.get("participants", [])]
            total_size = response.get("totalSize", len(participants))
            return participants, total_size

        return PaginatedList(fetch_page)

    def add(
        self,
        user: str,
        workspace: str | int,
        *,
        role: str = "launch",
    ) -> Participant:
        """
        Add a participant to a workspace.

        Args:
            user: Username or email
            workspace: Workspace ID or "org/workspace" reference
            role: Participant role (admin, maintain, launch, view, connect)

        Returns:
            Added Participant object
        """
        ws_id = self._resolve_workspace_id(workspace)
        org_id = self._get_org_id_for_workspace(ws_id)

        payload = {
            "memberId": self._find_member_id(user, org_id),
            "role": role.upper(),
        }

        response = self._client.post(
            f"/orgs/{org_id}/workspaces/{ws_id}/participants",
            json=payload,
        )
        participant_data = response.get("participant", {})
        return Participant.model_validate(participant_data)

    def update(
        self,
        user: str,
        workspace: str | int,
        *,
        role: str,
    ) -> Participant:
        """
        Update a participant's role.

        Args:
            user: Username or email
            workspace: Workspace ID or "org/workspace" reference
            role: New role

        Returns:
            Updated Participant object
        """
        ws_id = self._resolve_workspace_id(workspace)
        org_id = self._get_org_id_for_workspace(ws_id)

        # Find participant by username or email
        participant_id = None
        for p in self.list(workspace=ws_id):
            if p.user_name == user or p.email == user:
                participant_id = p.participant_id
                break

        if not participant_id:
            raise Exception(f"Participant '{user}' not found")

        payload = {"role": role.upper()}
        response = self._client.put(
            f"/orgs/{org_id}/workspaces/{ws_id}/participants/{participant_id}",
            json=payload,
        )
        participant_data = response.get("participant", {})
        return Participant.model_validate(participant_data)

    def delete(
        self,
        user: str,
        workspace: str | int,
    ) -> None:
        """
        Remove a participant from a workspace.

        Args:
            user: Username or email
            workspace: Workspace ID or "org/workspace" reference
        """
        ws_id = self._resolve_workspace_id(workspace)
        org_id = self._get_org_id_for_workspace(ws_id)

        # Find participant by username or email
        for p in self.list(workspace=ws_id):
            if p.user_name == user or p.email == user:
                self._client.delete(
                    f"/orgs/{org_id}/workspaces/{ws_id}/participants/{p.participant_id}"
                )
                return

        raise Exception(f"Participant '{user}' not found")

    def _get_org_id_for_workspace(self, ws_id: int) -> int:
        """Get the organization ID for a workspace."""
        user_id = self._get_user_id()
        response = self._client.get(f"/user/{user_id}/workspaces")

        for entry in response.get("orgsAndWorkspaces", []):
            if entry.get("workspaceId") == ws_id:
                return entry.get("orgId")

        raise Exception(f"Workspace {ws_id} not found")

    def _find_member_id(self, user: str, org_id: int) -> int:
        """Find member ID by username or email."""
        response = self._client.get(f"/orgs/{org_id}/members")

        for member in response.get("members", []):
            if member.get("userName") == user or member.get("email") == user:
                return member.get("memberId")

        raise Exception(f"Member '{user}' not found in organization")
