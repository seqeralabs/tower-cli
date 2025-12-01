"""
Response models for compute environment commands.

These classes represent the output responses from compute environment CLI commands,
matching the Java implementation.
"""

from typing import Any, Dict, List, Optional


class ComputeEnvResponse:
    """Base response class for compute environment commands."""

    def to_dict(self) -> Dict[str, Any]:
        """Convert response to dictionary for JSON/YAML output."""
        return {}

    def to_console(self) -> str:
        """Convert response to console output."""
        return ""


class ComputeEnvDeleted(ComputeEnvResponse):
    """Response for compute environment deleted command."""

    def __init__(self, compute_env_id: str, workspace: str) -> None:
        self.compute_env_id = compute_env_id
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.compute_env_id,
            "workspaceRef": self.workspace,
        }

    def to_console(self) -> str:
        return f"\n  Compute environment '{self.compute_env_id}' deleted at {self.workspace} workspace\n"


class ComputeEnvAdded(ComputeEnvResponse):
    """Response for compute environment added command."""

    def __init__(
        self,
        platform: str,
        compute_env_id: str,
        name: str,
        workspace_id: Optional[int],
        workspace: str,
    ) -> None:
        self.platform = platform
        self.compute_env_id = compute_env_id
        self.name = name
        self.workspace_id = workspace_id
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "platform": self.platform,
            "id": self.compute_env_id,
            "name": self.name,
            "workspaceId": self.workspace_id,
            "workspaceRef": self.workspace,
        }

    def to_console(self) -> str:
        return f"\n  New {self.platform.upper()} compute environment '{self.name}' added at {self.workspace} workspace\n"


class ComputeEnvList(ComputeEnvResponse):
    """Response for compute environments list command."""

    def __init__(
        self,
        workspace: str,
        compute_envs: List[Dict[str, Any]],
        base_workspace_url: Optional[str] = None,
    ) -> None:
        self.workspace = workspace
        self.compute_envs = compute_envs
        self.base_workspace_url = base_workspace_url

    def to_dict(self) -> Dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "computeEnvs": self.compute_envs,
        }

    def to_console(self) -> str:
        """Format compute environments as a table."""
        from rich.table import Table
        from datetime import datetime
        from io import StringIO
        from rich.console import Console

        output = f"\n  Compute environments at {self.workspace} workspace:\n\n"

        if not self.compute_envs:
            output += "    No compute environment found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("  ID", style="cyan")
        table.add_column("Status")
        table.add_column("Platform")
        table.add_column("Name")
        table.add_column("Last activity")

        for ce in self.compute_envs:
            ce_id = ce.get("id", "")
            primary = ce.get("primary", False)
            status = ce.get("status", "")
            platform = ce.get("platform", "")
            name = ce.get("name", "")
            last_used = ce.get("lastUsed", "")

            # Add primary marker
            if primary:
                ce_id = f"* {ce_id}"
            else:
                ce_id = f"  {ce_id}"

            # Format timestamp
            if last_used:
                try:
                    dt = datetime.fromisoformat(last_used.replace("Z", "+00:00"))
                    last_activity = dt.strftime("%Y-%m-%d %H:%M:%S")
                except:
                    last_activity = last_used
            else:
                last_activity = "-"

            table.add_row(ce_id, status, platform, name, last_activity)

        # Render table to string
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class ComputeEnvView(ComputeEnvResponse):
    """Response for compute environment view command."""

    def __init__(
        self,
        workspace: str,
        compute_env: Dict[str, Any],
    ) -> None:
        self.workspace = workspace
        self.compute_env = compute_env

    def to_dict(self) -> Dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "computeEnv": self.compute_env,
        }

    def to_console(self) -> str:
        """Format compute environment details."""
        import json
        return f"\n  Compute environment at {self.workspace} workspace:\n\n" + json.dumps(self.compute_env, indent=2) + "\n"


class ComputeEnvUpdated(ComputeEnvResponse):
    """Response for compute environment updated command."""

    def __init__(self, compute_env_id: str, name: str, workspace: str) -> None:
        self.compute_env_id = compute_env_id
        self.name = name
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.compute_env_id,
            "name": self.name,
            "workspaceRef": self.workspace,
        }

    def to_console(self) -> str:
        return f"\n  Compute environment '{self.name}' updated at {self.workspace} workspace\n"


class ComputeEnvExport(ComputeEnvResponse):
    """Response for compute environment export command."""

    def __init__(self, config: Dict[str, Any]) -> None:
        self.config = config

    def to_dict(self) -> Dict[str, Any]:
        return self.config

    def to_console(self) -> str:
        """Export as JSON."""
        import json
        return json.dumps(self.config, indent=2) + "\n"


class ComputeEnvImported(ComputeEnvResponse):
    """Response for compute environment import command."""

    def __init__(
        self,
        compute_env_id: str,
        name: str,
        workspace: str,
    ) -> None:
        self.compute_env_id = compute_env_id
        self.name = name
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.compute_env_id,
            "name": self.name,
            "workspaceRef": self.workspace,
        }

    def to_console(self) -> str:
        return f"\n  Compute environment '{self.name}' imported at {self.workspace} workspace\n"


class ComputeEnvsPrimaryGet(ComputeEnvResponse):
    """Response for compute environment primary get command."""

    def __init__(self, compute_env_id: Optional[str], name: Optional[str], workspace: str) -> None:
        self.compute_env_id = compute_env_id
        self.name = name
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.compute_env_id,
            "name": self.name,
            "workspaceRef": self.workspace,
        }

    def to_console(self) -> str:
        if self.compute_env_id:
            return f"\n  Primary compute environment at {self.workspace} workspace:\n\n    ID: {self.compute_env_id}\n    Name: {self.name}\n"
        else:
            return f"\n  No primary compute environment set at {self.workspace} workspace\n"


class ComputeEnvsPrimarySet(ComputeEnvResponse):
    """Response for compute environment primary set command."""

    def __init__(self, compute_env_id: str, name: str, workspace: str) -> None:
        self.compute_env_id = compute_env_id
        self.name = name
        self.workspace = workspace

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.compute_env_id,
            "name": self.name,
            "workspaceRef": self.workspace,
        }

    def to_console(self) -> str:
        return f"\n  Primary compute environment '{self.name}' set at {self.workspace} workspace\n"
