"""
Response models for Seqera CLI commands.

These classes represent the output responses from CLI commands,
matching the Java implementation.
"""

from typing import Any, Dict, Optional


class Response:
    """Base response class."""

    def to_dict(self) -> Dict[str, Any]:
        """Convert response to dictionary for JSON/YAML output."""
        return {}

    def to_console(self) -> str:
        """Convert response to console output."""
        return ""


class CredentialsAdded(Response):
    """Response for credentials added command."""

    def __init__(
        self,
        provider: str,
        credentials_id: str,
        name: str,
        workspace: str,
    ) -> None:
        self.provider = provider
        self.credentials_id = credentials_id
        self.name = name
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "provider": self.provider,
            "id": self.credentials_id,
            "name": self.name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return (
            f"  New {self.provider} credentials '{self.name}' added at {self.workspace} workspace\n"
            f"\n"
            f"    ID: {self.credentials_id}"
        )


class CredentialsUpdated(Response):
    """Response for credentials updated command."""

    def __init__(
        self,
        provider: str,
        name: str,
        workspace: str,
    ) -> None:
        self.provider = provider
        self.name = name
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "provider": self.provider,
            "name": self.name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  {self.provider} credentials '{self.name}' updated at {self.workspace} workspace"


class CredentialsDeleted(Response):
    """Response for credentials deleted command."""

    def __init__(
        self,
        name: str,
        workspace: str,
    ) -> None:
        self.name = name
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "name": self.name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Credentials '{self.name}' deleted at {self.workspace} workspace"


class CredentialsList(Response):
    """Response for credentials list command."""

    def __init__(
        self,
        credentials: list,
        workspace: str,
    ) -> None:
        self.credentials = credentials
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "credentials": self.credentials,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        # This will be formatted as a table by the command
        return ""
