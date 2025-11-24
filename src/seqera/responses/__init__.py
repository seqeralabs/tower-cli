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
        workspace: str,
        credentials: list,
        base_workspace_url: Optional[str] = None,
    ) -> None:
        self.workspace = workspace
        self.credentials = credentials
        self.base_workspace_url = base_workspace_url

    def to_dict(self) -> Dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "credentials": self.credentials,
        }

    def to_console(self) -> str:
        """Format credentials as a table."""
        from rich.console import Console
        from rich.table import Table
        from datetime import datetime

        console = Console()

        # Header
        output = f"\n  Credentials at {self.workspace} workspace:\n\n"

        if not self.credentials:
            output += "    No credentials found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("ID", style="cyan")
        table.add_column("Provider")
        table.add_column("Name")
        table.add_column("Last activity")

        for cred in self.credentials:
            cred_id = cred.get("id", "")
            provider = cred.get("provider", "").upper()
            name = cred.get("name", "")
            last_used = cred.get("lastUsed", "")

            # Format timestamp
            if last_used:
                try:
                    dt = datetime.fromisoformat(last_used.replace("Z", "+00:00"))
                    last_activity = dt.strftime("%Y-%m-%d %H:%M:%S")
                except:
                    last_activity = last_used
            else:
                last_activity = "-"

            table.add_row(cred_id, provider, name, last_activity)

        # Render table to string
        from io import StringIO
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class CredentialsDeleted(Response):
    """Response for credentials deleted command."""

    def __init__(
        self,
        credentials_id: str,
        workspace: str,
    ) -> None:
        self.credentials_id = credentials_id
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.credentials_id,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Credentials '{self.credentials_id}' deleted at {self.workspace} workspace"
