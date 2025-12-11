"""
Response models for Seqera CLI commands.

These classes represent the output responses from CLI commands,
matching the Java implementation.
"""

from typing import Any, Dict, Optional


class Response:
    """Base response class."""

    def to_dict(self) -> dict[str, Any]:
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

    def to_dict(self) -> dict[str, Any]:
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

    def to_dict(self) -> dict[str, Any]:
        return {
            "provider": self.provider,
            "name": self.name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  {self.provider} credentials '{self.name}' updated at {self.workspace} workspace"


class CredentialsList(Response):
    """Response for credentials list command."""

    def __init__(
        self,
        workspace: str,
        credentials: list,
        base_workspace_url: str | None = None,
    ) -> None:
        self.workspace = workspace
        self.credentials = credentials
        self.base_workspace_url = base_workspace_url

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "credentials": self.credentials,
        }

    def to_console(self) -> str:
        """Format credentials as a table."""
        from datetime import datetime

        from rich.console import Console
        from rich.table import Table

        Console()

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

    def to_dict(self) -> dict[str, Any]:
        return {
            "id": self.credentials_id,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Credentials '{self.credentials_id}' deleted at {self.workspace} workspace"


# Secrets Response Classes


class SecretsList(Response):
    """Response for secrets list command."""

    def __init__(
        self,
        workspace: str,
        secrets: list,
    ) -> None:
        self.workspace = workspace
        self.secrets = secrets

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "secrets": self.secrets,
        }

    def to_console(self) -> str:
        """Format secrets as a table."""
        from datetime import datetime

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Secrets at {self.workspace} workspace:\n\n"

        if not self.secrets:
            output += "    No secrets found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("ID", style="cyan")
        table.add_column("Name")
        table.add_column("Last Updated")

        for secret in self.secrets:
            secret_id = str(secret.get("id", ""))
            name = secret.get("name", "")
            last_updated = secret.get("lastUpdated", "")

            # Format timestamp
            if last_updated:
                try:
                    dt = datetime.fromisoformat(last_updated.replace("Z", "+00:00"))
                    last_updated_str = dt.strftime("%Y-%m-%d %H:%M:%S")
                except:
                    last_updated_str = last_updated
            else:
                last_updated_str = "-"

            table.add_row(secret_id, name, last_updated_str)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class SecretAdded(Response):
    """Response for secret added command."""

    def __init__(
        self,
        workspace: str,
        secret_id: int,
        name: str,
    ) -> None:
        self.workspace = workspace
        self.secret_id = secret_id
        self.name = name

    def to_dict(self) -> dict[str, Any]:
        return {
            "id": str(self.secret_id),
            "name": self.name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return (
            f"  New secret '{self.name}' added at {self.workspace} workspace\n"
            f"\n"
            f"    ID: {self.secret_id}"
        )


class SecretDeleted(Response):
    """Response for secret deleted command."""

    def __init__(
        self,
        secret: dict,
        workspace: str,
    ) -> None:
        self.secret = secret
        self.workspace = workspace
        self.name = secret.get("name", "")

    def to_dict(self) -> dict[str, Any]:
        return {
            "name": self.name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Secret '{self.name}' deleted at {self.workspace} workspace"


class SecretView(Response):
    """Response for secret view command."""

    def __init__(
        self,
        workspace: str,
        secret: dict,
    ) -> None:
        self.workspace = workspace
        self.secret = secret
        self.name = secret.get("name", "")

    def to_dict(self) -> dict[str, Any]:
        return {
            "name": self.name,
            "workspace": self.workspace,
            "secret": self.secret,
        }

    def to_console(self) -> str:
        """Format secret details."""
        from datetime import datetime

        output = f"\n  Secret '{self.name}' at {self.workspace} workspace:\n\n"

        # Display details
        secret_id = self.secret.get("id", "")
        date_created = self.secret.get("dateCreated", "")
        last_updated = self.secret.get("lastUpdated", "")
        last_used = self.secret.get("lastUsed", "")

        output += f"    ID: {secret_id}\n"
        output += f"    Name: {self.name}\n"

        # Format timestamps
        if date_created:
            try:
                dt = datetime.fromisoformat(date_created.replace("Z", "+00:00"))
                output += f"    Created: {dt.strftime('%Y-%m-%d %H:%M:%S')}\n"
            except:
                output += f"    Created: {date_created}\n"

        if last_updated:
            try:
                dt = datetime.fromisoformat(last_updated.replace("Z", "+00:00"))
                output += f"    Last Updated: {dt.strftime('%Y-%m-%d %H:%M:%S')}\n"
            except:
                output += f"    Last Updated: {last_updated}\n"

        if last_used:
            try:
                dt = datetime.fromisoformat(last_used.replace("Z", "+00:00"))
                output += f"    Last Used: {dt.strftime('%Y-%m-%d %H:%M:%S')}\n"
            except:
                output += f"    Last Used: {last_used}\n"
        else:
            output += "    Last Used: Never\n"

        return output


class SecretUpdated(Response):
    """Response for secret updated command."""

    def __init__(
        self,
        workspace: str,
        name: str,
    ) -> None:
        self.workspace = workspace
        self.name = name

    def to_dict(self) -> dict[str, Any]:
        return {
            "name": self.name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Secret '{self.name}' updated at {self.workspace} workspace"


# Labels Response Classes


class LabelAdded(Response):
    """Response for label added command."""

    def __init__(
        self,
        label_id: int,
        name: str,
        resource: bool,
        value: str | None,
        workspace_id: str,
    ) -> None:
        self.label_id = label_id
        self.name = name
        self.resource = resource
        self.value = value
        self.workspace_id = workspace_id

    def to_dict(self) -> dict[str, Any]:
        return {
            "id": self.label_id,
            "name": self.name,
            "resource": self.resource,
            "value": self.value,
            "workspaceId": self.workspace_id,
        }

    def to_console(self) -> str:
        label_type = "Resource label" if self.resource else "Simple label"
        value_str = f" (value: {self.value})" if self.value else ""
        return (
            f"  {label_type} '{self.name}' added{value_str}\n"
            f"\n"
            f"    ID: {self.label_id}\n"
            f"    Workspace: {self.workspace_id}"
        )


class LabelDeleted(Response):
    """Response for label deleted command."""

    def __init__(
        self,
        label_id: int,
        workspace_id: int,
    ) -> None:
        self.label_id = label_id
        self.workspace_id = workspace_id

    def to_dict(self) -> dict[str, Any]:
        return {
            "id": self.label_id,
            "workspaceId": self.workspace_id,
        }

    def to_console(self) -> str:
        return f"  Label '{self.label_id}' deleted at workspace {self.workspace_id}"


class LabelsList(Response):
    """Response for labels list command."""

    def __init__(
        self,
        workspace_id: int,
        label_type: str,
        labels: list,
        filter_text: str | None = None,
    ) -> None:
        self.workspace_id = workspace_id
        self.label_type = label_type
        self.labels = labels
        self.filter_text = filter_text

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceId": self.workspace_id,
            "type": self.label_type,
            "labels": self.labels,
        }

    def to_console(self) -> str:
        """Format labels as a table."""
        from rich.console import Console
        from rich.table import Table

        Console()

        # Header
        output = f"\n  Labels at workspace {self.workspace_id}:\n\n"

        if not self.labels:
            output += "    No labels found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("ID", style="cyan")
        table.add_column("Name")
        table.add_column("Type")
        table.add_column("Value")

        for label in self.labels:
            label_id = str(label.get("id", ""))
            name = label.get("name", "")
            is_resource = label.get("resource", False)
            value = label.get("value", "")

            label_type = "resource" if is_resource else "simple"
            display_value = value if value else "-"

            table.add_row(label_id, name, label_type, display_value)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


# Organizations Response Models


class OrganizationsList(Response):
    """Response for organizations list command."""

    def __init__(
        self,
        user_name: str,
        organizations: list,
        base_workspace_url: str | None = None,
    ) -> None:
        self.user_name = user_name
        self.organizations = organizations
        self.base_workspace_url = base_workspace_url

    def to_dict(self) -> dict[str, Any]:
        return {
            "userName": self.user_name,
            "organizations": self.organizations,
        }

    def to_console(self) -> str:
        """Format organizations as a table."""
        from rich.console import Console
        from rich.table import Table

        Console()

        # Header
        output = f"\n  Organizations for user '{self.user_name}':\n\n"

        if not self.organizations:
            output += "    No organizations found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Org ID", style="cyan")
        table.add_column("Org Name")

        for org in self.organizations:
            org_id = str(org.get("orgId", ""))
            org_name = org.get("orgName", "")
            table.add_row(org_id, org_name)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class OrganizationView(Response):
    """Response for organization view command."""

    def __init__(
        self,
        organization: dict[str, Any],
        base_workspace_url: str | None = None,
    ) -> None:
        self.organization = organization
        self.base_workspace_url = base_workspace_url

    def to_dict(self) -> dict[str, Any]:
        return self.organization

    def to_console(self) -> str:
        """Format organization details."""
        org = self.organization
        output = "\n  Organization Details:\n\n"
        output += f"    ID:          {org.get('orgId', 'N/A')}\n"
        output += f"    Name:        {org.get('name', 'N/A')}\n"
        output += f"    Full Name:   {org.get('fullName', 'N/A')}\n"

        if org.get("description"):
            output += f"    Description: {org.get('description')}\n"
        if org.get("location"):
            output += f"    Location:    {org.get('location')}\n"
        if org.get("website"):
            output += f"    Website:     {org.get('website')}\n"
        if org.get("logoUrl"):
            output += f"    Logo URL:    {org.get('logoUrl')}\n"

        return output


class OrganizationAdded(Response):
    """Response for organization added command."""

    def __init__(
        self,
        organization: dict[str, Any],
    ) -> None:
        self.organization = organization

    def to_dict(self) -> dict[str, Any]:
        return self.organization

    def to_console(self) -> str:
        org = self.organization
        return (
            f"  New organization '{org.get('name', 'N/A')}' added\n"
            f"\n"
            f"    ID:        {org.get('orgId', 'N/A')}\n"
            f"    Full Name: {org.get('fullName', 'N/A')}"
        )


class OrganizationDeleted(Response):
    """Response for organization deleted command."""

    def __init__(
        self,
        organization_name: str,
    ) -> None:
        self.organization_name = organization_name

    def to_dict(self) -> dict[str, Any]:
        return {
            "organizationName": self.organization_name,
        }

    def to_console(self) -> str:
        return f"  Organization '{self.organization_name}' deleted"


class OrganizationUpdated(Response):
    """Response for organization updated command."""

    def __init__(
        self,
        org_id: int,
        organization_name: str,
    ) -> None:
        self.org_id = org_id
        self.organization_name = organization_name

    def to_dict(self) -> dict[str, Any]:
        return {
            "orgId": self.org_id,
            "organizationName": self.organization_name,
        }

    def to_console(self) -> str:
        return f"  Organization '{self.organization_name}' updated"


class InfoResponse(Response):
    """Response for info command."""

    def __init__(
        self,
        connection_check: int,
        version_check: int,
        credentials_check: int,
        opts: dict[str, str | None],
    ) -> None:
        self.connection_check = connection_check
        self.version_check = version_check
        self.credentials_check = credentials_check
        self.opts = opts

    def to_dict(self) -> dict[str, Any]:
        """Convert to dictionary for JSON/YAML output."""
        return {
            "details": {
                "seqeraApiEndpoint": self.opts.get("seqeraApiEndpoint"),
                "seqeraApiVersion": self.opts.get("seqeraApiVersion"),
                "seqeraVersion": self.opts.get("seqeraVersion"),
                "cliVersion": self.opts.get("cliVersion"),
                "cliApiVersion": self.opts.get("cliApiVersion"),
                "userName": self.opts.get("userName"),
            },
            "checks": {
                "connectionCheck": self.connection_check,
                "versionCheck": self.version_check,
                "credentialsCheck": self.credentials_check,
            },
        }

    def to_console(self) -> str:
        """Format info response for console output with colors."""
        from io import StringIO

        from rich.console import Console
        from rich.table import Table

        # Status strings with colors
        ok = "[green]OK[/green]"
        fail = "[red]FAILED[/red]"
        undefined = "[yellow]UNDEFINED[/yellow]"
        skipped = "[yellow]SKIPPED[/yellow]"

        def get_opt(key: str, default: str = "[yellow]UNDEFINED[/yellow]") -> str:
            """Get option value or default."""
            value = self.opts.get(key)
            return value if value is not None else default

        # Build output - pre-render all Rich markup to ANSI codes
        output_parts = []
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=True, width=120)
        temp_console.print("[bold yellow]Details[/bold yellow]")
        output_parts.append("\n    " + string_io.getvalue().strip())

        # Details table
        details_table = Table(show_header=False, show_edge=False, padding=(0, 2), box=None)
        details_table.add_column("Key", style="bold")
        details_table.add_column("Value")

        details_table.add_row(
            "Seqera Platform API endpoint", get_opt("seqeraApiEndpoint", undefined)
        )
        details_table.add_row("Seqera Platform API version", get_opt("seqeraApiVersion", undefined))
        details_table.add_row("Seqera version", get_opt("seqeraVersion", undefined))
        details_table.add_row("CLI version", get_opt("cliVersion", undefined))
        details_table.add_row("CLI minimum API version", get_opt("cliApiVersion", undefined))
        details_table.add_row("Authenticated user", get_opt("userName", undefined))

        # Render details table
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=True, width=120)
        temp_console.print(details_table)
        details_str = string_io.getvalue()
        output_parts.append("    " + details_str.replace("\n", "\n    ").rstrip())

        # Health status section - pre-render markup
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=True, width=120)
        temp_console.print("[bold yellow]System health status[/bold yellow]")
        output_parts.append("\n    " + string_io.getvalue().strip())

        # Health table
        health_table = Table(show_header=False, show_edge=False, padding=(0, 2), box=None)
        health_table.add_column("Check", style="bold")
        health_table.add_column("Status")

        # Connection check
        conn_status = (
            ok if self.connection_check == 1 else (fail if self.connection_check == 0 else skipped)
        )
        health_table.add_row("Remote API server connection check", conn_status)

        # Version check
        ver_status = (
            ok if self.version_check == 1 else (fail if self.version_check == 0 else skipped)
        )
        health_table.add_row("Seqera Platform API version check", ver_status)

        # Credentials check
        cred_status = (
            ok
            if self.credentials_check == 1
            else (fail if self.credentials_check == 0 else skipped)
        )
        health_table.add_row("Authentication API credential's token", cred_status)

        # Render health table
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=True, width=120)
        temp_console.print(health_table)
        health_str = string_io.getvalue()
        output_parts.append("    " + health_str.replace("\n", "\n    ").rstrip())

        # Error messages - pre-render each with Rich
        def render_error(message: str) -> str:
            """Pre-render error message with Rich."""
            sio = StringIO()
            c = Console(file=sio, force_terminal=True, width=120)
            c.print(message)
            return sio.getvalue().strip()

        if self.connection_check == 0:
            endpoint = self.opts.get("seqeraApiEndpoint", "")
            if "/api" in endpoint:
                output_parts.append(
                    f"\n    {render_error(f'[bold red]Seqera Platform API URL {endpoint} is not available[/bold red]')}\n"
                )
            else:
                output_parts.append(
                    f"\n    {render_error(f'[bold red]Seqera Platform API URL {endpoint} is not available (did you mean {endpoint}/api?)[/bold red]')}\n"
                )

        if self.version_check == 0:
            seqera_api_ver = self.opts.get("seqeraApiVersion", "")
            cli_api_ver = self.opts.get("cliApiVersion", "")
            output_parts.append(
                f"\n    {render_error(f'[bold red]Seqera Platform API veseqera_api{seqera_api_ver} while the minimum required version to be fully compatible is {cli_api_ver}[/bold red]')}\n"
            )

        if self.credentials_check == 0:
            output_parts.append(
                f"\n    {render_error('[bold red]Review that your current access token is valid and active.[/bold red]')}\n"
            )

        output_parts.append("")

        # All parts are pre-rendered, just join and return
        return "\n".join(output_parts)

    def get_exit_code(self) -> int:
        """Get exit code based on check results."""
        # Exit code 0 if all checks pass, 1 otherwise
        return (
            0 if (self.connection_check + self.version_check + self.credentials_check == 3) else 1
        )


# Workspaces Response Classes


class WorkspacesList(Response):
    """Response for workspaces list command."""

    def __init__(
        self,
        user_name: str,
        workspaces: list,
        base_url: str | None = None,
    ) -> None:
        self.user_name = user_name
        self.workspaces = workspaces
        self.base_url = base_url

    def to_dict(self) -> dict[str, Any]:
        return {
            "userName": self.user_name,
            "workspaces": self.workspaces,
        }

    def to_console(self) -> str:
        """Format workspaces as a table."""
        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Workspaces for user {self.user_name}:\n\n"

        if not self.workspaces:
            output += "    No workspaces found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Workspace ID", style="cyan")
        table.add_column("Workspace Name")
        table.add_column("Organization Name")
        table.add_column("Organization ID")

        for workspace in self.workspaces:
            workspace_id = str(workspace.get("workspaceId", ""))
            workspace_name = workspace.get("workspaceName", "")
            org_name = workspace.get("orgName", "")
            org_id = str(workspace.get("orgId", ""))

            table.add_row(workspace_id, workspace_name, org_name, org_id)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class WorkspaceView(Response):
    """Response for workspace view command."""

    def __init__(self, workspace: dict[str, Any]) -> None:
        self.workspace = workspace

    def to_dict(self) -> dict[str, Any]:
        return {"workspace": self.workspace}

    def to_console(self) -> str:
        """Format workspace details for console output."""
        ws = self.workspace
        output = "\n  Workspace Details:\n\n"
        output += f"    ID:          {ws.get('id', '')}\n"
        output += f"    Name:        {ws.get('name', '')}\n"
        output += f"    Full Name:   {ws.get('fullName', '')}\n"
        output += f"    Description: {ws.get('description', '')}\n"
        output += f"    Visibility:  {ws.get('visibility', '')}\n"
        output += f"    Created:     {ws.get('dateCreated', '')}\n"
        output += f"    Updated:     {ws.get('lastUpdated', '')}\n"
        return output


class WorkspaceAdded(Response):
    """Response for workspace added command."""

    def __init__(
        self,
        workspace_name: str,
        org_name: str,
        visibility: str,
    ) -> None:
        self.workspace_name = workspace_name
        self.org_name = org_name
        self.visibility = visibility

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceName": self.workspace_name,
            "orgName": self.org_name,
            "visibility": self.visibility,
        }

    def to_console(self) -> str:
        return (
            f"  Workspace '{self.workspace_name}' added to organization '{self.org_name}'\n"
            f"\n"
            f"    Visibility: {self.visibility}"
        )


class WorkspaceDeleted(Response):
    """Response for workspace deleted command."""

    def __init__(
        self,
        workspace_name: str,
        org_name: str,
    ) -> None:
        self.workspace_name = workspace_name
        self.org_name = org_name

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceName": self.workspace_name,
            "orgName": self.org_name,
        }

    def to_console(self) -> str:
        return f"  Workspace '{self.workspace_name}' deleted from organization '{self.org_name}'"


class WorkspaceUpdated(Response):
    """Response for workspace updated command."""

    def __init__(
        self,
        workspace_name: str,
        org_name: str,
        visibility: str,
    ) -> None:
        self.workspace_name = workspace_name
        self.org_name = org_name
        self.visibility = visibility

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceName": self.workspace_name,
            "orgName": self.org_name,
            "visibility": self.visibility,
        }

    def to_console(self) -> str:
        return (
            f"  Workspace '{self.workspace_name}' updated in organization '{self.org_name}'\n"
            f"\n"
            f"    Visibility: {self.visibility}"
        )


class ParticipantLeft(Response):
    """Response for participant left command."""

    def __init__(self, workspace_name: str) -> None:
        self.workspace_name = workspace_name

    def to_dict(self) -> dict[str, Any]:
        return {"workspaceName": self.workspace_name}

    def to_console(self) -> str:
        return f"  Left workspace '{self.workspace_name}'"


class ParticipantsList(Response):
    """Response for participants list command."""

    def __init__(
        self,
        org_name: str,
        workspace_name: str,
        participants: list,
    ) -> None:
        self.org_name = org_name
        self.workspace_name = workspace_name
        self.participants = participants

    def to_dict(self) -> dict[str, Any]:
        return {
            "orgName": self.org_name,
            "workspaceName": self.workspace_name,
            "participants": self.participants,
        }

    def to_console(self) -> str:
        """Format participants as a table."""
        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Participants at {self.org_name}/{self.workspace_name} workspace:\n\n"

        if not self.participants:
            output += "    No participants found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Participant ID", style="cyan")
        table.add_column("Type")
        table.add_column("Name")
        table.add_column("Email")
        table.add_column("Workspace Role")
        table.add_column("Org Role")

        for participant in self.participants:
            participant_id = str(participant.get("participantId", ""))
            participant_type = participant.get("type", "")

            # Get name based on type
            if participant_type == "MEMBER":
                name = participant.get("userName", "")
            else:  # TEAM
                name = participant.get("teamName", "")

            email = participant.get("email", "-")
            wsp_role = participant.get("wspRole", "")
            org_role = participant.get("orgRole", "-")

            table.add_row(participant_id, participant_type, name, email, wsp_role, org_role)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class ParticipantAdded(Response):
    """Response for participant added command."""

    def __init__(
        self,
        participant: dict[str, Any],
        workspace_name: str,
    ) -> None:
        self.participant = participant
        self.workspace_name = workspace_name

    def to_dict(self) -> dict[str, Any]:
        return {
            "participant": self.participant,
            "workspaceName": self.workspace_name,
        }

    def to_console(self) -> str:
        participant_type = self.participant.get("type", "")
        if participant_type == "MEMBER":
            name = self.participant.get("userName", "")
        else:  # TEAM
            name = self.participant.get("teamName", "")

        role = self.participant.get("wspRole", "")
        participant_id = self.participant.get("participantId", "")

        return (
            f"  Participant '{name}' added to workspace '{self.workspace_name}'\n"
            f"\n"
            f"    ID: {participant_id}\n"
            f"    Type: {participant_type}\n"
            f"    Role: {role}"
        )


class ParticipantDeleted(Response):
    """Response for participant deleted command."""

    def __init__(
        self,
        name: str,
        workspace_name: str,
    ) -> None:
        self.name = name
        self.workspace_name = workspace_name

    def to_dict(self) -> dict[str, Any]:
        return {
            "name": self.name,
            "workspaceName": self.workspace_name,
        }

    def to_console(self) -> str:
        return f"  Participant '{self.name}' deleted from workspace '{self.workspace_name}'"


class ParticipantUpdated(Response):
    """Response for participant updated command."""

    def __init__(
        self,
        workspace_name: str,
        name: str,
        role: str,
    ) -> None:
        self.workspace_name = workspace_name
        self.name = name
        self.role = role

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceName": self.workspace_name,
            "name": self.name,
            "role": self.role,
        }

    def to_console(self) -> str:
        return f"  Participant '{self.name}' role updated to '{self.role}' in workspace '{self.workspace_name}'"


# Pipelines Response Classes


class PipelinesList(Response):
    """Response for pipelines list command."""

    def __init__(
        self,
        workspace: str,
        pipelines: list,
        base_workspace_url: str | None = None,
        show_labels: bool = False,
        pagination_info: dict[str, Any] | None = None,
    ) -> None:
        self.workspace = workspace
        self.pipelines = pipelines
        self.base_workspace_url = base_workspace_url
        self.show_labels = show_labels
        self.pagination_info = pagination_info

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "pipelines": self.pipelines,
        }

    def to_console(self) -> str:
        """Format pipelines as a table."""
        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Pipelines at {self.workspace} workspace:\n\n"

        if not self.pipelines:
            output += "    No pipelines found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("ID", style="cyan")
        table.add_column("Name")
        table.add_column("Repository")
        table.add_column("Last Updated")

        for pipeline in self.pipelines:
            pipeline_id = str(pipeline.get("pipelineId", ""))
            name = pipeline.get("name", "")
            repository = pipeline.get("repository", "")
            last_updated = pipeline.get("lastUpdated", "")

            # Format timestamp
            if last_updated:
                try:
                    from datetime import datetime

                    dt = datetime.fromisoformat(last_updated.replace("Z", "+00:00"))
                    last_updated_str = dt.strftime("%Y-%m-%d %H:%M:%S")
                except:
                    last_updated_str = last_updated
            else:
                last_updated_str = "-"

            table.add_row(pipeline_id, name, repository, last_updated_str)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        result = output + indented_table

        # Add pagination info if present
        if self.pagination_info:
            page = self.pagination_info.get("page")
            offset = self.pagination_info.get("offset")
            max_items = self.pagination_info.get("max")
            total_size = self.pagination_info.get("totalSize")

            if page is not None:
                result += f"\n\n    Page {page} of {(total_size + max_items - 1) // max_items if max_items else 1}"
            elif offset is not None:
                result += f"\n\n    Showing {offset + 1} to {min(offset + max_items, total_size)} of {total_size}"

        result += "\n"
        return result


class PipelineView(Response):
    """Response for pipeline view command."""

    def __init__(
        self,
        workspace: str,
        pipeline: dict[str, Any],
        launch: dict[str, Any] | None = None,
        base_workspace_url: str | None = None,
    ) -> None:
        self.workspace = workspace
        self.pipeline = pipeline
        self.launch = launch
        self.base_workspace_url = base_workspace_url

    def to_dict(self) -> dict[str, Any]:
        result = {
            "workspaceRef": self.workspace,
            "pipeline": self.pipeline,
        }
        if self.launch:
            result["launch"] = self.launch
        return result

    def to_console(self) -> str:
        """Format pipeline details."""
        from datetime import datetime

        output = (
            f"\n  Pipeline '{self.pipeline.get('name', '')}' at {self.workspace} workspace:\n\n"
        )

        # Display pipeline details
        output += f"    ID:          {self.pipeline.get('pipelineId', '')}\n"
        output += f"    Name:        {self.pipeline.get('name', '')}\n"

        if self.pipeline.get("description"):
            output += f"    Description: {self.pipeline.get('description')}\n"

        output += f"    Repository:  {self.pipeline.get('repository', '')}\n"

        if self.launch:
            output += "\n    Launch Configuration:\n"
            compute_env = self.launch.get("computeEnv")
            if compute_env:
                output += f"      Compute Env: {compute_env.get('name', '')} ({compute_env.get('id', '')})\n"

            work_dir = self.launch.get("workDir")
            if work_dir:
                output += f"      Work Dir:    {work_dir}\n"

            revision = self.launch.get("revision")
            if revision:
                output += f"      Revision:    {revision}\n"

            params_text = self.launch.get("paramsText")
            if params_text:
                output += f"      Params:      {params_text.strip()}\n"

            pre_run = self.launch.get("preRunScript")
            if pre_run:
                output += (
                    f"      Pre-run:     {pre_run[:50]}...\n"
                    if len(pre_run) > 50
                    else f"      Pre-run:     {pre_run}\n"
                )

            post_run = self.launch.get("postRunScript")
            if post_run:
                output += (
                    f"      Post-run:    {post_run[:50]}...\n"
                    if len(post_run) > 50
                    else f"      Post-run:    {post_run}\n"
                )

            config_profiles = self.launch.get("configProfiles")
            if config_profiles:
                output += f"      Profiles:    {', '.join(config_profiles)}\n"

        return output


class PipelineAdded(Response):
    """Response for pipeline added command."""

    def __init__(
        self,
        workspace: str,
        pipeline_name: str,
        pipeline_id: int | None = None,
    ) -> None:
        self.workspace = workspace
        self.pipeline_name = pipeline_name
        self.pipeline_id = pipeline_id

    def to_dict(self) -> dict[str, Any]:
        result = {
            "pipelineName": self.pipeline_name,
            "workspace": self.workspace,
        }
        if self.pipeline_id:
            result["pipelineId"] = self.pipeline_id
        return result

    def to_console(self) -> str:
        output = f"  Pipeline '{self.pipeline_name}' added at {self.workspace} workspace"
        if self.pipeline_id:
            output += f"\n\n    ID: {self.pipeline_id}"
        return output


class PipelineDeleted(Response):
    """Response for pipeline deleted command."""

    def __init__(
        self,
        pipeline_name: str,
        workspace: str,
    ) -> None:
        self.pipeline_name = pipeline_name
        self.workspace = workspace

    def to_dict(self) -> dict[str, Any]:
        return {
            "pipelineName": self.pipeline_name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Pipeline '{self.pipeline_name}' deleted at {self.workspace} workspace"


class PipelineUpdated(Response):
    """Response for pipeline updated command."""

    def __init__(
        self,
        workspace: str,
        pipeline_name: str,
    ) -> None:
        self.workspace = workspace
        self.pipeline_name = pipeline_name

    def to_dict(self) -> dict[str, Any]:
        return {
            "pipelineName": self.pipeline_name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Pipeline '{self.pipeline_name}' updated at {self.workspace} workspace"


class PipelineExport(Response):
    """Response for pipeline export command."""

    def __init__(
        self,
        config: str,
        file_path: str | None = None,
    ) -> None:
        self.config = config
        self.file_path = file_path

    def to_dict(self) -> dict[str, Any]:
        import json

        return json.loads(self.config)

    def to_console(self) -> str:
        if self.file_path:
            return f"  Pipeline configuration exported to {self.file_path}"
        return self.config


# Datasets Response Classes


class DatasetsList(Response):
    """Response for datasets list command."""

    def __init__(
        self,
        workspace_id: str,
        datasets: list,
    ) -> None:
        self.workspace_id = workspace_id
        self.datasets = datasets

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceId": self.workspace_id,
            "datasets": self.datasets,
        }

    def to_console(self) -> str:
        """Format datasets as a table."""
        from datetime import datetime

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Datasets at workspace {self.workspace_id}:\n\n"

        if not self.datasets:
            output += "    No datasets found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("ID", style="cyan")
        table.add_column("Name")
        table.add_column("Description")
        table.add_column("Last Updated")

        for dataset in self.datasets:
            dataset_id = dataset.get("id", "")
            name = dataset.get("name", "")
            description = dataset.get("description", "") or "-"
            last_updated = dataset.get("lastUpdated", "")

            # Format timestamp
            if last_updated:
                try:
                    dt = datetime.fromisoformat(last_updated.replace("Z", "+00:00"))
                    last_updated_str = dt.strftime("%Y-%m-%d %H:%M:%S")
                except:
                    last_updated_str = last_updated
            else:
                last_updated_str = "-"

            table.add_row(dataset_id, name, description, last_updated_str)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class DatasetView(Response):
    """Response for dataset view command."""

    def __init__(
        self,
        dataset: dict[str, Any],
        workspace_id: str,
    ) -> None:
        self.dataset = dataset
        self.workspace_id = workspace_id
        self.name = dataset.get("name", "")

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceId": self.workspace_id,
            "dataset": self.dataset,
        }

    def to_console(self) -> str:
        """Format dataset details."""
        from datetime import datetime

        output = f"\n  Dataset '{self.name}' at workspace {self.workspace_id}:\n\n"

        # Display details
        dataset_id = self.dataset.get("id", "")
        description = self.dataset.get("description", "")
        media_type = self.dataset.get("mediaType", "")
        date_created = self.dataset.get("dateCreated", "")
        last_updated = self.dataset.get("lastUpdated", "")

        output += f"    ID: {dataset_id}\n"
        output += f"    Name: {self.name}\n"

        if description:
            output += f"    Description: {description}\n"
        if media_type:
            output += f"    Media Type: {media_type}\n"

        # Format timestamps
        if date_created:
            try:
                dt = datetime.fromisoformat(date_created.replace("Z", "+00:00"))
                output += f"    Created: {dt.strftime('%Y-%m-%d %H:%M:%S')}\n"
            except:
                output += f"    Created: {date_created}\n"

        if last_updated:
            try:
                dt = datetime.fromisoformat(last_updated.replace("Z", "+00:00"))
                output += f"    Last Updated: {dt.strftime('%Y-%m-%d %H:%M:%S')}\n"
            except:
                output += f"    Last Updated: {last_updated}\n"

        return output


class DatasetVersionsList(Response):
    """Response for dataset versions list command."""

    def __init__(
        self,
        dataset_id: str,
        workspace_id: str,
        versions: list,
    ) -> None:
        self.dataset_id = dataset_id
        self.workspace_id = workspace_id
        self.versions = versions

    def to_dict(self) -> dict[str, Any]:
        return {
            "datasetId": self.dataset_id,
            "workspaceId": self.workspace_id,
            "versions": self.versions,
        }

    def to_console(self) -> str:
        """Format versions as a table."""
        from datetime import datetime

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Versions for dataset {self.dataset_id}:\n\n"

        if not self.versions:
            output += "    No versions found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Version", style="cyan")
        table.add_column("File Name")
        table.add_column("Media Type")
        table.add_column("Last Updated")

        for version in self.versions:
            version_num = str(version.get("version", ""))
            file_name = version.get("fileName", "")
            media_type = version.get("mediaType", "")
            last_updated = version.get("lastUpdated", "")

            # Format timestamp
            if last_updated:
                try:
                    dt = datetime.fromisoformat(last_updated.replace("Z", "+00:00"))
                    last_updated_str = dt.strftime("%Y-%m-%d %H:%M:%S")
                except:
                    last_updated_str = last_updated
            else:
                last_updated_str = "-"

            table.add_row(version_num, file_name, media_type, last_updated_str)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class DatasetAdded(Response):
    """Response for dataset added command."""

    def __init__(
        self,
        dataset_id: str,
        name: str,
        workspace_id: str,
    ) -> None:
        self.dataset_id = dataset_id
        self.name = name
        self.workspace_id = workspace_id

    def to_dict(self) -> dict[str, Any]:
        return {
            "id": self.dataset_id,
            "name": self.name,
            "workspaceId": self.workspace_id,
        }

    def to_console(self) -> str:
        return (
            f"  New dataset '{self.name}' added at workspace {self.workspace_id}\n"
            f"\n"
            f"    ID: {self.dataset_id}"
        )


class DatasetDeleted(Response):
    """Response for dataset deleted command."""

    def __init__(
        self,
        dataset_id: str,
        workspace_id: str,
    ) -> None:
        self.dataset_id = dataset_id
        self.workspace_id = workspace_id

    def to_dict(self) -> dict[str, Any]:
        return {
            "id": self.dataset_id,
            "workspaceId": self.workspace_id,
        }

    def to_console(self) -> str:
        return f"  Dataset '{self.dataset_id}' deleted at workspace {self.workspace_id}"


class DatasetUpdated(Response):
    """Response for dataset updated command."""

    def __init__(
        self,
        name: str,
        workspace_id: str,
        dataset_id: str,
    ) -> None:
        self.name = name
        self.workspace_id = workspace_id
        self.dataset_id = dataset_id

    def to_dict(self) -> dict[str, Any]:
        return {
            "name": self.name,
            "workspaceId": self.workspace_id,
            "id": self.dataset_id,
        }

    def to_console(self) -> str:
        return f"  Dataset '{self.name}' updated at workspace {self.workspace_id}"


class DatasetUrl(Response):
    """Response for dataset url command."""

    def __init__(
        self,
        url: str,
        dataset_id: str,
        workspace_id: str,
    ) -> None:
        self.url = url
        self.dataset_id = dataset_id
        self.workspace_id = workspace_id

    def to_dict(self) -> dict[str, Any]:
        return {
            "url": self.url,
            "datasetId": self.dataset_id,
            "workspaceId": self.workspace_id,
        }

    def to_console(self) -> str:
        return f"  Dataset URL: {self.url}"


class DatasetDownload(Response):
    """Response for dataset download command."""

    def __init__(
        self,
        file_path: str,
        file_name: str,
    ) -> None:
        self.file_path = file_path
        self.file_name = file_name

    def to_dict(self) -> dict[str, Any]:
        return {
            "filePath": self.file_path,
            "fileName": self.file_name,
        }

    def to_console(self) -> str:
        return f"  Dataset downloaded to: {self.file_path}"


# Runs Response Classes


class RunsList(Response):
    """Response for runs list command."""

    def __init__(
        self,
        workspace: str,
        runs: list,
        base_workspace_url: str | None = None,
    ) -> None:
        self.workspace = workspace
        self.runs = runs
        self.base_workspace_url = base_workspace_url

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "runs": self.runs,
        }

    def to_console(self) -> str:
        """Format runs as a table."""
        from datetime import datetime

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Runs at {self.workspace} workspace:\n\n"

        if not self.runs:
            output += "    No runs found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("ID", style="cyan")
        table.add_column("Run Name")
        table.add_column("Project")
        table.add_column("Status")
        table.add_column("Submitted")

        for run in self.runs:
            workflow = run.get("workflow", {})
            run_id = workflow.get("id", "")
            run_name = workflow.get("runName", "")
            project = workflow.get("projectName", "")
            status = workflow.get("status", "")
            submit = workflow.get("submit", "")

            # Format timestamp
            if submit:
                try:
                    dt = datetime.fromisoformat(submit.replace("Z", "+00:00"))
                    submit_str = dt.strftime("%Y-%m-%d %H:%M:%S")
                except:
                    submit_str = submit
            else:
                submit_str = "-"

            table.add_row(run_id, run_name, project, status, submit_str)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class RunView(Response):
    """Response for run view command."""

    def __init__(
        self,
        workspace: str,
        general: dict[str, Any],
        config_files: list = None,
        config_text: str = None,
        params: dict[str, Any] = None,
        command: str = None,
        status: dict[str, Any] = None,
        processes: list = None,
        stats: dict[str, Any] = None,
        load: dict[str, Any] = None,
        utilization: dict[str, Any] = None,
        base_workspace_url: str | None = None,
    ) -> None:
        self.workspace = workspace
        self.general = general
        self.config_files = config_files or []
        self.config_text = config_text
        self.params = params or {}
        self.command = command
        self.status = status or {}
        self.processes = processes or []
        self.stats = stats or {}
        self.load = load or {}
        self.utilization = utilization or {}
        self.base_workspace_url = base_workspace_url

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "general": self.general,
            "configFiles": self.config_files,
            "configText": self.config_text,
            "params": self.params,
            "command": self.command,
            "status": self.status,
            "processes": self.processes,
            "stats": self.stats,
            "load": self.load,
            "utilization": self.utilization,
        }

    def to_console(self) -> str:
        """Format run details."""
        output = f"\n  Run '{self.general.get('runName', '')}' at {self.workspace} workspace:\n\n"

        # General info
        output += "    General:\n"
        for key, value in self.general.items():
            if value is not None:
                output += f"      {key}: {value}\n"

        return output


class RunDeleted(Response):
    """Response for run deleted command."""

    def __init__(
        self,
        run_id: str,
        workspace: str,
    ) -> None:
        self.run_id = run_id
        self.workspace = workspace

    def to_dict(self) -> dict[str, Any]:
        return {
            "id": self.run_id,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Run '{self.run_id}' deleted at {self.workspace} workspace"


class RunCancelled(Response):
    """Response for run cancelled command."""

    def __init__(
        self,
        run_id: str,
        workspace: str,
    ) -> None:
        self.run_id = run_id
        self.workspace = workspace

    def to_dict(self) -> dict[str, Any]:
        return {
            "id": self.run_id,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Run '{self.run_id}' cancelled at {self.workspace} workspace"


class RunRelaunched(Response):
    """Response for run relaunched command."""

    def __init__(
        self,
        run_id: str,
        workspace: str,
        watch_url: str | None = None,
    ) -> None:
        self.run_id = run_id
        self.workspace = workspace
        self.watch_url = watch_url

    def to_dict(self) -> dict[str, Any]:
        result = {
            "workflowId": self.run_id,
            "workspace": self.workspace,
        }
        if self.watch_url:
            result["watchUrl"] = self.watch_url
        return result

    def to_console(self) -> str:
        lines = [f"  Workflow {self.run_id} submitted at {self.workspace} workspace."]
        if self.watch_url:
            lines.append("")
            lines.append(f"    Watch: {self.watch_url}")
        return "\n".join(lines)


class RunDump(Response):
    """Response for run dump command."""

    def __init__(
        self,
        run_id: str,
        output_file: str,
    ) -> None:
        self.run_id = run_id
        self.output_file = output_file

    def to_dict(self) -> dict[str, Any]:
        return {
            "runId": self.run_id,
            "outputFile": self.output_file,
        }

    def to_console(self) -> str:
        return f"  Run {self.run_id} dumped to {self.output_file}"


class TaskView(Response):
    """Response for viewing a single task."""

    def __init__(
        self,
        run_id: str,
        task: dict,
    ) -> None:
        self.run_id = run_id
        self.task = task

    def to_dict(self) -> dict[str, Any]:
        return {
            "runId": self.run_id,
            "task": self.task,
        }

    def to_console(self) -> str:
        lines = [f"  Task details for run {self.run_id}:", ""]
        lines.append(f"    Task ID:    {self.task.get('taskId', '')}")
        lines.append(f"    Process:    {self.task.get('process', '')}")
        lines.append(f"    Tag:        {self.task.get('tag', '') or '-'}")
        lines.append(f"    Status:     {self.task.get('status', '')}")
        lines.append(f"    Exit code:  {self.task.get('exit', '')}")
        lines.append(f"    Container:  {self.task.get('container', '') or '-'}")
        lines.append(f"    Executor:   {self.task.get('executor', '') or '-'}")
        if self.task.get("workdir"):
            lines.append(f"    Work dir:   {self.task.get('workdir')}")
        return "\n".join(lines)


class TasksList(Response):
    """Response for tasks list command."""

    def __init__(
        self,
        run_id: str,
        tasks: list,
    ) -> None:
        self.run_id = run_id
        self.tasks = tasks

    def to_dict(self) -> dict[str, Any]:
        return {
            "runId": self.run_id,
            "tasks": self.tasks,
        }

    def to_console(self) -> str:
        """Format tasks as a table."""
        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Tasks for run {self.run_id}:\n\n"

        if not self.tasks:
            output += "    No tasks found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Task ID", style="cyan")
        table.add_column("Process")
        table.add_column("Tag")
        table.add_column("Status")

        for task in self.tasks:
            task_id = str(task.get("taskId", ""))
            process = task.get("process", "")
            tag = task.get("tag", "") or "-"
            status = task.get("status", "")

            table.add_row(task_id, process, tag, status)

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class MetricsList(Response):
    """Response for metrics list command."""

    def __init__(
        self,
        run_id: str,
        metrics: list,
    ) -> None:
        self.run_id = run_id
        self.metrics = metrics

    def to_dict(self) -> dict[str, Any]:
        return {
            "runId": self.run_id,
            "metrics": self.metrics,
        }

    def to_console(self) -> str:
        """Format metrics as a table."""
        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Metrics for run {self.run_id}:\n\n"

        if not self.metrics:
            output += "    No metrics found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Process", style="cyan")
        table.add_column("CPU Mean")
        table.add_column("Memory Mean")
        table.add_column("Time Mean")

        for metric in self.metrics:
            process = metric.get("process", "")
            cpu_mean = metric.get("cpu", {}).get("mean", 0)
            mem_mean = metric.get("mem", {}).get("mean", 0)
            time = metric.get("time")
            time_mean = time.get("mean", 0) if time else 0

            table.add_row(
                process,
                str(cpu_mean),
                str(mem_mean),
                str(time_mean),
            )

        # Render table to string
        from io import StringIO

        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


# Teams Response Classes


class TeamsList(Response):
    """Response for teams list command."""

    def __init__(
        self,
        organization: str,
        teams: list,
        base_workspace_url: str | None = None,
        pagination_info: dict[str, Any] | None = None,
    ) -> None:
        self.organization = organization
        self.teams = teams
        self.base_workspace_url = base_workspace_url
        self.pagination_info = pagination_info

    def to_dict(self) -> dict[str, Any]:
        result = {
            "organization": self.organization,
            "teams": self.teams,
        }
        if self.pagination_info:
            result["paginationInfo"] = self.pagination_info
        return result

    def to_console(self) -> str:
        """Format teams as a table."""
        from io import StringIO

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Teams at {self.organization} organization:\n\n"

        if not self.teams:
            output += "    No teams found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Team ID", style="cyan")
        table.add_column("Name")
        table.add_column("Description")
        table.add_column("Members")

        for team in self.teams:
            team_id = str(team.get("teamId", ""))
            name = team.get("name", "")
            description = team.get("description", "")
            members_count = str(team.get("membersCount", 0))

            table.add_row(team_id, name, description, members_count)

        # Render table to string
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        output += indented_table + "\n"

        # Add pagination info if present
        if self.pagination_info:
            offset = self.pagination_info.get("offset")
            max_results = self.pagination_info.get("max")
            page = self.pagination_info.get("page")
            if page is not None:
                output += f"\n    Showing page {page} (max results: {max_results})\n"
            elif offset is not None:
                output += (
                    f"\n    Showing results from offset {offset} (max results: {max_results})\n"
                )

        return output


class TeamView(Response):
    """Response for team view command."""

    def __init__(
        self,
        organization: str,
        team: dict[str, Any],
    ) -> None:
        self.organization = organization
        self.team = team

    def to_dict(self) -> dict[str, Any]:
        return {
            "organization": self.organization,
            "team": self.team,
        }

    def to_console(self) -> str:
        """Format team details."""
        output = "\n  Team Details:\n\n"
        output += f"    Team ID:     {self.team.get('teamId', '')}\n"
        output += f"    Name:        {self.team.get('name', '')}\n"
        output += f"    Description: {self.team.get('description', '')}\n"
        output += f"    Members:     {self.team.get('membersCount', 0)}\n"
        return output


class TeamAdded(Response):
    """Response for team added command."""

    def __init__(
        self,
        organization: str,
        team_name: str,
    ) -> None:
        self.organization = organization
        self.team_name = team_name

    def to_dict(self) -> dict[str, Any]:
        return {
            "organization": self.organization,
            "teamName": self.team_name,
        }

    def to_console(self) -> str:
        return f"  Team '{self.team_name}' added at {self.organization} organization"


class TeamDeleted(Response):
    """Response for team deleted command."""

    def __init__(
        self,
        organization: str,
        team_ref: str,
    ) -> None:
        self.organization = organization
        self.team_ref = team_ref

    def to_dict(self) -> dict[str, Any]:
        return {
            "organization": self.organization,
            "teamRef": self.team_ref,
        }

    def to_console(self) -> str:
        return f"  Team '{self.team_ref}' deleted at {self.organization} organization"


# Team Members Response Classes


class TeamMembersList(Response):
    """Response for team members list command."""

    def __init__(
        self,
        team_name: str,
        members: list,
    ) -> None:
        self.team_name = team_name
        self.members = members

    def to_dict(self) -> dict[str, Any]:
        return {
            "teamName": self.team_name,
            "members": self.members,
        }

    def to_console(self) -> str:
        """Format team members as a table."""
        from io import StringIO

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Members of team '{self.team_name}':\n\n"

        if not self.members:
            output += "    No members found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Member ID", style="cyan")
        table.add_column("Username")
        table.add_column("Email")
        table.add_column("Role")

        for member in self.members:
            member_id = str(member.get("memberId", ""))
            username = member.get("userName", "")
            email = member.get("email", "")
            role = member.get("role", "")

            table.add_row(member_id, username, email, role)

        # Render table to string
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class TeamMemberAdded(Response):
    """Response for team member added command."""

    def __init__(
        self,
        team_name: str,
        member: dict[str, Any],
    ) -> None:
        self.team_name = team_name
        self.member = member

    def to_dict(self) -> dict[str, Any]:
        return {
            "teamName": self.team_name,
            "member": self.member,
        }

    def to_console(self) -> str:
        username = self.member.get("userName", "")
        email = self.member.get("email", "")
        return f"  Member '{username}' ({email}) added to team '{self.team_name}'"


class TeamMemberDeleted(Response):
    """Response for team member deleted command."""

    def __init__(
        self,
        team_name: str,
        member_ref: str,
    ) -> None:
        self.team_name = team_name
        self.member_ref = member_ref

    def to_dict(self) -> dict[str, Any]:
        return {
            "teamName": self.team_name,
            "memberRef": self.member_ref,
        }

    def to_console(self) -> str:
        return f"  Member '{self.member_ref}' deleted from team '{self.team_name}'"


# Studios Response Classes


class StudiosList(Response):
    """Response for studios list command."""

    def __init__(
        self,
        workspace: str,
        studios: list,
        show_labels: bool = False,
        pagination_info: dict[str, Any] | None = None,
    ) -> None:
        self.workspace = workspace
        self.studios = studios
        self.show_labels = show_labels
        self.pagination_info = pagination_info

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "studios": self.studios,
        }

    def to_console(self) -> str:
        """Format studios as a table."""
        from datetime import datetime
        from io import StringIO

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Studios at {self.workspace} workspace:\n\n"

        if not self.studios:
            output += "    No studios found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Session ID", style="cyan")
        table.add_column("Name")
        table.add_column("Status")
        table.add_column("Compute Env")
        table.add_column("Template")

        if self.show_labels:
            table.add_column("Labels")

        for studio in self.studios:
            session_id = studio.get("sessionId", "")
            name = studio.get("name", "")
            status_info = studio.get("statusInfo", {})
            status = status_info.get("status", "")

            compute_env = studio.get("computeEnv", {})
            compute_env_name = compute_env.get("name", "")

            template = studio.get("template", {})
            template_icon = template.get("icon", "")

            row_data = [session_id, name, status, compute_env_name, template_icon]

            if self.show_labels:
                labels = studio.get("labels", [])
                labels_str = (
                    ", ".join([f"{l.get('name')}={l.get('value', '')}" for l in labels])
                    if labels
                    else "-"
                )
                row_data.append(labels_str)

            table.add_row(*row_data)

        # Render table to string
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        result = output + indented_table

        # Add pagination info if present
        if self.pagination_info:
            page = self.pagination_info.get("page")
            offset = self.pagination_info.get("offset")
            max_items = self.pagination_info.get("max")
            total_size = self.pagination_info.get("totalSize")

            if page is not None:
                result += f"\n\n    Page {page} of {(total_size + max_items - 1) // max_items if max_items else 1}"
            elif offset is not None:
                result += f"\n\n    Showing {offset + 1} to {min(offset + max_items, total_size)} of {total_size}"

        result += "\n"
        return result


class StudioView(Response):
    """Response for studio view command."""

    def __init__(
        self,
        workspace: str,
        studio: dict[str, Any],
    ) -> None:
        self.workspace = workspace
        self.studio = studio

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "studio": self.studio,
        }

    def to_console(self) -> str:
        """Format studio details."""
        from datetime import datetime

        output = f"\n  Studio '{self.studio.get('name', '')}' at {self.workspace} workspace:\n\n"

        # Display studio details
        output += f"    Session ID:  {self.studio.get('sessionId', '')}\n"
        output += f"    Name:        {self.studio.get('name', '')}\n"

        if self.studio.get("description"):
            output += f"    Description: {self.studio.get('description')}\n"

        status_info = self.studio.get("statusInfo", {})
        if status_info:
            output += f"    Status:      {status_info.get('status', '')}\n"

        compute_env = self.studio.get("computeEnv", {})
        if compute_env:
            output += "\n    Compute Environment:\n"
            output += f"      Name:     {compute_env.get('name', '')}\n"
            output += f"      Platform: {compute_env.get('platform', '')}\n"
            if compute_env.get("region"):
                output += f"      Region:   {compute_env.get('region')}\n"

        template = self.studio.get("template", {})
        if template:
            output += "\n    Template:\n"
            output += f"      Repository: {template.get('repository', '')}\n"
            output += f"      Icon:       {template.get('icon', '')}\n"

        configuration = self.studio.get("configuration", {})
        if configuration:
            output += "\n    Configuration:\n"
            output += f"      CPU:    {configuration.get('cpu', 0)}\n"
            output += f"      Memory: {configuration.get('memory', 0)} MB\n"
            output += f"      GPU:    {configuration.get('gpu', 0)}\n"

        if self.studio.get("studioUrl"):
            output += f"\n    URL: {self.studio.get('studioUrl')}\n"

        return output


class StudioStarted(Response):
    """Response for studio started command."""

    def __init__(
        self,
        session_id: str,
        studio_name: str,
        workspace: str,
        workspace_id: int | None = None,
        job_submitted: bool = False,
    ) -> None:
        self.session_id = session_id
        self.studio_name = studio_name
        self.workspace = workspace
        self.workspace_id = workspace_id
        self.job_submitted = job_submitted

    def to_dict(self) -> dict[str, Any]:
        result = {
            "sessionId": self.session_id,
            "studioName": self.studio_name,
            "workspace": self.workspace,
            "jobSubmitted": self.job_submitted,
        }
        if self.workspace_id:
            result["workspaceId"] = self.workspace_id
        return result

    def to_console(self) -> str:
        status = "started" if self.job_submitted else "start requested"
        return f"  Studio '{self.studio_name}' (ID: {self.session_id}) {status} at {self.workspace} workspace"


class StudioStopped(Response):
    """Response for studio stopped command."""

    def __init__(
        self,
        session_id: str,
        studio_name: str,
        workspace: str,
        workspace_id: int | None = None,
        job_submitted: bool = False,
    ) -> None:
        self.session_id = session_id
        self.studio_name = studio_name
        self.workspace = workspace
        self.workspace_id = workspace_id
        self.job_submitted = job_submitted

    def to_dict(self) -> dict[str, Any]:
        result = {
            "sessionId": self.session_id,
            "studioName": self.studio_name,
            "workspace": self.workspace,
            "jobSubmitted": self.job_submitted,
        }
        if self.workspace_id:
            result["workspaceId"] = self.workspace_id
        return result

    def to_console(self) -> str:
        status = "stopped" if self.job_submitted else "stop requested"
        return f"  Studio '{self.studio_name}' (ID: {self.session_id}) {status} at {self.workspace} workspace"


class StudioDeleted(Response):
    """Response for studio deleted command."""

    def __init__(
        self,
        session_id: str,
        workspace: str,
    ) -> None:
        self.session_id = session_id
        self.workspace = workspace

    def to_dict(self) -> dict[str, Any]:
        return {
            "sessionId": self.session_id,
            "workspaceRef": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Studio '{self.session_id}' deleted at {self.workspace} workspace"


class StudioCheckpoints(Response):
    """Response for studio checkpoints list command."""

    def __init__(
        self,
        session_id: str,
        workspace: str,
        checkpoints: list,
        pagination_info: dict[str, Any] | None = None,
    ) -> None:
        self.session_id = session_id
        self.workspace = workspace
        self.checkpoints = checkpoints
        self.pagination_info = pagination_info

    def to_dict(self) -> dict[str, Any]:
        return {
            "sessionId": self.session_id,
            "workspaceRef": self.workspace,
            "checkpoints": self.checkpoints,
        }

    def to_console(self) -> str:
        """Format checkpoints as a table."""
        from datetime import datetime
        from io import StringIO

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Checkpoints for studio {self.session_id}:\n\n"

        if not self.checkpoints:
            output += "    No checkpoints found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("ID", style="cyan")
        table.add_column("Name")
        table.add_column("Author")
        table.add_column("Date Saved")

        for checkpoint in self.checkpoints:
            checkpoint_id = str(checkpoint.get("id", ""))
            name = checkpoint.get("name", "")

            author = checkpoint.get("author", {})
            author_name = author.get("userName", "")

            date_saved = checkpoint.get("dateSaved", "")

            # Format timestamp
            if date_saved:
                try:
                    dt = datetime.fromisoformat(date_saved.replace("Z", "+00:00"))
                    date_saved_str = dt.strftime("%Y-%m-%d %H:%M:%S")
                except:
                    date_saved_str = date_saved
            else:
                date_saved_str = "-"

            table.add_row(checkpoint_id, name, author_name, date_saved_str)

        # Render table to string
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        result = output + indented_table

        # Add pagination info if present
        if self.pagination_info:
            offset = self.pagination_info.get("offset")
            max_items = self.pagination_info.get("max")
            total_size = self.pagination_info.get("totalSize")

            if offset is not None:
                result += f"\n\n    Showing {offset + 1} to {min(offset + max_items, total_size)} of {total_size}"

        result += "\n"
        return result


# Organization Members Response Classes


class MembersList(Response):
    """Response for organization members list command."""

    def __init__(
        self,
        organization: str,
        members: list,
        pagination_info: dict[str, Any] | None = None,
    ) -> None:
        self.organization = organization
        self.members = members
        self.pagination_info = pagination_info

    def to_dict(self) -> dict[str, Any]:
        result = {
            "organization": self.organization,
            "members": self.members,
        }
        if self.pagination_info:
            result["paginationInfo"] = self.pagination_info
        return result

    def to_console(self) -> str:
        """Format organization members as a table."""
        from io import StringIO

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Members at {self.organization} organization:\n\n"

        if not self.members:
            output += "    No members found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Member ID", style="cyan")
        table.add_column("Username")
        table.add_column("Email")
        table.add_column("Role")

        for member in self.members:
            member_id = str(member.get("memberId", ""))
            username = member.get("userName", "")
            email = member.get("email", "")
            role = member.get("role", "")

            table.add_row(member_id, username, email, role)

        # Render table to string
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        output += indented_table + "\n"

        # Add pagination info if present
        if self.pagination_info:
            offset = self.pagination_info.get("offset")
            max_results = self.pagination_info.get("max")
            page = self.pagination_info.get("page")
            if page is not None:
                output += f"\n    Showing page {page} (max results: {max_results})\n"
            elif offset is not None:
                output += (
                    f"\n    Showing results from offset {offset} (max results: {max_results})\n"
                )

        return output


class MemberAdded(Response):
    """Response for member added to organization command."""

    def __init__(
        self,
        organization: str,
        member: dict[str, Any],
    ) -> None:
        self.organization = organization
        self.member = member

    def to_dict(self) -> dict[str, Any]:
        return {
            "organization": self.organization,
            "member": self.member,
        }

    def to_console(self) -> str:
        username = self.member.get("userName", "")
        email = self.member.get("email", "")
        return f"  Member '{username}' ({email}) added to organization '{self.organization}'"


class MemberDeleted(Response):
    """Response for member deleted from organization command."""

    def __init__(
        self,
        user_ref: str,
        organization: str,
    ) -> None:
        self.user_ref = user_ref
        self.organization = organization

    def to_dict(self) -> dict[str, Any]:
        return {
            "userRef": self.user_ref,
            "organization": self.organization,
        }

    def to_console(self) -> str:
        return f"  Member '{self.user_ref}' deleted from organization '{self.organization}'"


class MemberUpdated(Response):
    """Response for member updated in organization command."""

    def __init__(
        self,
        user_ref: str,
        organization: str,
        role: str,
    ) -> None:
        self.user_ref = user_ref
        self.organization = organization
        self.role = role

    def to_dict(self) -> dict[str, Any]:
        return {
            "userRef": self.user_ref,
            "organization": self.organization,
            "role": self.role,
        }

    def to_console(self) -> str:
        return f"  Member '{self.user_ref}' updated to role '{self.role}' in organization '{self.organization}'"


class MemberLeft(Response):
    """Response for leaving organization command."""

    def __init__(
        self,
        organization: str,
    ) -> None:
        self.organization = organization

    def to_dict(self) -> dict[str, Any]:
        return {
            "organization": self.organization,
        }

    def to_console(self) -> str:
        return f"  Left organization '{self.organization}'"


# Actions Response Classes


class ActionsList(Response):
    """Response for actions list command."""

    def __init__(
        self,
        workspace: str,
        actions: list,
        base_workspace_url: str | None = None,
    ) -> None:
        self.workspace = workspace
        self.actions = actions
        self.base_workspace_url = base_workspace_url

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "actions": self.actions,
        }

    def to_console(self) -> str:
        """Format actions as a table."""
        from datetime import datetime
        from io import StringIO

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Actions at {self.workspace} workspace:\n\n"

        if not self.actions:
            output += "    No actions found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("ID", style="cyan")
        table.add_column("Name")
        table.add_column("Source")
        table.add_column("Pipeline")
        table.add_column("Status")
        table.add_column("Last Seen")

        for action in self.actions:
            action_id = action.get("id", "")
            name = action.get("name", "")
            source = action.get("source", "")
            pipeline = action.get("pipeline", "")
            status = action.get("status", "")
            last_seen = action.get("lastSeen", "")

            # Format timestamp
            if last_seen:
                try:
                    dt = datetime.fromisoformat(last_seen.replace("Z", "+00:00"))
                    last_seen_str = dt.strftime("%Y-%m-%d %H:%M:%S")
                except:
                    last_seen_str = last_seen
            else:
                last_seen_str = "-"

            table.add_row(action_id, name, source, pipeline, status, last_seen_str)

        # Render table to string
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        return output + indented_table + "\n"


class ActionView(Response):
    """Response for action view command."""

    def __init__(
        self,
        action: dict[str, Any],
        workspace: str,
    ) -> None:
        self.action = action
        self.workspace = workspace
        self.name = action.get("name", "")

    def to_dict(self) -> dict[str, Any]:
        return {
            "workspaceRef": self.workspace,
            "action": self.action,
        }

    def to_console(self) -> str:
        """Format action details."""
        from datetime import datetime

        output = f"\n  Action '{self.name}' at {self.workspace} workspace:\n\n"

        # Display action details
        action_id = self.action.get("id", "")
        source = self.action.get("source", "")
        status = self.action.get("status", "")
        pipeline = self.action.get("pipeline", "") or self.action.get("launch", {}).get(
            "pipeline", ""
        )
        hook_url = self.action.get("hookUrl", "")
        date_created = self.action.get("dateCreated", "")
        last_updated = self.action.get("lastUpdated", "")
        last_seen = self.action.get("lastSeen", "")

        output += f"    ID:          {action_id}\n"
        output += f"    Name:        {self.name}\n"
        output += f"    Source:      {source}\n"
        output += f"    Status:      {status}\n"

        if pipeline:
            output += f"    Pipeline:    {pipeline}\n"

        if hook_url:
            output += f"    Hook URL:    {hook_url}\n"

        # Format timestamps
        if date_created:
            try:
                dt = datetime.fromisoformat(date_created.replace("Z", "+00:00"))
                output += f"    Created:     {dt.strftime('%Y-%m-%d %H:%M:%S')}\n"
            except:
                output += f"    Created:     {date_created}\n"

        if last_updated:
            try:
                dt = datetime.fromisoformat(last_updated.replace("Z", "+00:00"))
                output += f"    Last Updated: {dt.strftime('%Y-%m-%d %H:%M:%S')}\n"
            except:
                output += f"    Last Updated: {last_updated}\n"

        if last_seen:
            try:
                dt = datetime.fromisoformat(last_seen.replace("Z", "+00:00"))
                output += f"    Last Seen:   {dt.strftime('%Y-%m-%d %H:%M:%S')}\n"
            except:
                output += f"    Last Seen:   {last_seen}\n"

        # Display launch configuration if available
        launch = self.action.get("launch")
        if launch:
            output += "\n    Launch Configuration:\n"
            compute_env = launch.get("computeEnv")
            if compute_env:
                output += f"      Compute Env: {compute_env.get('name', '')} ({compute_env.get('id', '')})\n"

            work_dir = launch.get("workDir")
            if work_dir:
                output += f"      Work Dir:    {work_dir}\n"

            revision = launch.get("revision")
            if revision:
                output += f"      Revision:    {revision}\n"

        # Display event information if available
        event = self.action.get("event")
        if event:
            output += "\n    Last Event:\n"
            ref = event.get("ref")
            if ref:
                output += f"      Ref:         {ref}\n"
            commit_id = event.get("commitId")
            if commit_id:
                output += f"      Commit:      {commit_id}\n"
            commit_msg = event.get("commitMessage")
            if commit_msg:
                output += f"      Message:     {commit_msg}\n"

        return output


class ActionAdded(Response):
    """Response for action added command."""

    def __init__(
        self,
        action_name: str,
        workspace: str,
        action_id: str | None = None,
    ) -> None:
        self.action_name = action_name
        self.workspace = workspace
        self.action_id = action_id

    def to_dict(self) -> dict[str, Any]:
        result = {
            "actionName": self.action_name,
            "workspace": self.workspace,
        }
        if self.action_id:
            result["actionId"] = self.action_id
        return result

    def to_console(self) -> str:
        output = f"  Action '{self.action_name}' added at {self.workspace} workspace"
        if self.action_id:
            output += f"\n\n    ID: {self.action_id}"
        return output


class ActionDeleted(Response):
    """Response for action deleted command."""

    def __init__(
        self,
        action_name: str,
        workspace: str,
    ) -> None:
        self.action_name = action_name
        self.workspace = workspace

    def to_dict(self) -> dict[str, Any]:
        return {
            "actionName": self.action_name,
            "workspace": self.workspace,
        }

    def to_console(self) -> str:
        return f"  Action '{self.action_name}' deleted at {self.workspace} workspace"


class ActionUpdated(Response):
    """Response for action updated command."""

    def __init__(
        self,
        action_name: str,
        workspace: str,
        action_id: str | None = None,
    ) -> None:
        self.action_name = action_name
        self.workspace = workspace
        self.action_id = action_id

    def to_dict(self) -> dict[str, Any]:
        result = {
            "actionName": self.action_name,
            "workspace": self.workspace,
        }
        if self.action_id:
            result["actionId"] = self.action_id
        return result

    def to_console(self) -> str:
        return f"  Action '{self.action_name}' updated at {self.workspace} workspace"


# Collaborators Response Classes


class CollaboratorsList(Response):
    """Response for collaborators list command."""

    def __init__(
        self,
        org_id: int,
        collaborators: list,
        pagination_info: dict[str, Any] | None = None,
    ) -> None:
        self.org_id = org_id
        self.collaborators = collaborators
        self.pagination_info = pagination_info

    def to_dict(self) -> dict[str, Any]:
        result = {
            "orgId": self.org_id,
            "collaborators": self.collaborators,
        }
        if self.pagination_info:
            result["paginationInfo"] = self.pagination_info
        return result

    def to_console(self) -> str:
        """Format collaborators as a table."""
        from io import StringIO

        from rich.console import Console
        from rich.table import Table

        # Header
        output = f"\n  Collaborators in organization {self.org_id}:\n\n"

        if not self.collaborators:
            output += "    No collaborators found\n"
            return output

        # Create table
        table = Table(show_header=True, header_style="bold", show_edge=False, padding=(0, 2))
        table.add_column("Member ID", style="cyan")
        table.add_column("Username")
        table.add_column("Email")
        table.add_column("Name")
        table.add_column("Role")

        for collaborator in self.collaborators:
            member_id = str(collaborator.get("memberId", ""))
            username = collaborator.get("userName", "")
            email = collaborator.get("email", "")
            first_name = collaborator.get("firstName") or ""
            last_name = collaborator.get("lastName") or ""
            full_name = f"{first_name} {last_name}".strip() or "-"
            role = collaborator.get("role", "")

            table.add_row(member_id, username, email, full_name, role)

        # Render table to string
        string_io = StringIO()
        temp_console = Console(file=string_io, force_terminal=False)
        temp_console.print(table)
        table_str = string_io.getvalue()

        # Add indentation
        indented_table = "\n".join("    " + line for line in table_str.split("\n") if line)

        output += indented_table + "\n"

        # Add pagination info if present
        if self.pagination_info:
            offset = self.pagination_info.get("offset")
            max_results = self.pagination_info.get("max")
            total_size = self.pagination_info.get("totalSize")
            if offset is not None and max_results is not None and total_size is not None:
                output += f"\n    Showing {offset + 1} to {min(offset + max_results, total_size)} of {total_size}\n"

        return output


class CollaboratorAdded(Response):
    """Response for collaborator added command."""

    def __init__(
        self,
        org_id: int,
        user_name: str,
        email: str | None = None,
    ) -> None:
        self.org_id = org_id
        self.user_name = user_name
        self.email = email

    def to_dict(self) -> dict[str, Any]:
        result = {
            "orgId": self.org_id,
            "userName": self.user_name,
        }
        if self.email:
            result["email"] = self.email
        return result

    def to_console(self) -> str:
        email_str = f" ({self.email})" if self.email else ""
        return f"  Collaborator '{self.user_name}'{email_str} added to organization {self.org_id}"


class CollaboratorDeleted(Response):
    """Response for collaborator deleted command."""

    def __init__(
        self,
        org_id: int,
        user_name: str,
    ) -> None:
        self.org_id = org_id
        self.user_name = user_name

    def to_dict(self) -> dict[str, Any]:
        return {
            "orgId": self.org_id,
            "userName": self.user_name,
        }

    def to_console(self) -> str:
        return f"  Collaborator '{self.user_name}' deleted from organization {self.org_id}"


# Launch Response Classes


class LaunchSubmitted(Response):
    """Response for launch submitted command."""

    def __init__(
        self,
        workflow_id: str,
        workspace_id: int | None = None,
        base_url: str | None = None,
        workspace_ref: str | None = None,
    ) -> None:
        self.workflow_id = workflow_id
        self.workspace_id = workspace_id
        self.base_url = base_url
        self.workspace_ref = workspace_ref or "user"

    def to_dict(self) -> dict[str, Any]:
        result = {
            "workflowId": self.workflow_id,
            "workspaceRef": self.workspace_ref,
        }
        if self.workspace_id:
            result["workspaceId"] = self.workspace_id
        return result

    def to_console(self) -> str:
        output = f"  Workflow '{self.workflow_id}' submitted at {self.workspace_ref} workspace"
        if self.base_url and self.workspace_id:
            # Build URL for workspace run
            output += f"\n\n    {self.base_url}/orgs/{self.workspace_id}/watch/{self.workflow_id}"
        elif self.base_url:
            # Build URL for user workspace run
            output += f"\n\n    {self.base_url}/watch/{self.workflow_id}"
        return output


class LabelsManaged(Response):
    """Response for labels management operations (set, append, delete)."""

    def __init__(
        self,
        operation: str,
        entity_type: str,
        entity_id: str,
        workspace_id: int | None = None,
    ) -> None:
        self.operation = operation
        self.entity_type = entity_type
        self.entity_id = entity_id
        self.workspace_id = workspace_id

    def to_dict(self) -> dict[str, Any]:
        result = {
            "operation": self.operation,
            "type": self.entity_type,
            "id": self.entity_id,
        }
        if self.workspace_id:
            result["workspaceId"] = self.workspace_id
        return result

    def to_console(self) -> str:
        workspace_ref = str(self.workspace_id) if self.workspace_id else "user"
        return f"  '{self.operation}' labels on '{self.entity_type}' with id '{self.entity_id}' at {workspace_ref} workspace"
