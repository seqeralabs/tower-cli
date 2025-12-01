"""
Organizations commands for Seqera CLI.

Manage organizations in the Seqera Platform.
"""

import sys
from typing import Optional

import typer
from typing_extensions import Annotated

from seqera.api.client import SeqeraClient
from seqera.exceptions import (
    AuthenticationError,
    NotFoundError,
    OrganizationNotFoundException,
    SeqeraError,
)
from seqera.main import get_client, get_output_format
from seqera.responses import (
    OrganizationAdded,
    OrganizationDeleted,
    OrganizationsList,
    OrganizationUpdated,
    OrganizationView,
)
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml

# Create organizations app
app = typer.Typer(
    name="organizations",
    help="Manage organizations",
    no_args_is_help=True,
)


def handle_organizations_error(e: Exception) -> None:
    """Handle organizations command errors."""
    if isinstance(e, AuthenticationError):
        output_error("Unauthorized")
        sys.exit(1)
    elif isinstance(e, OrganizationNotFoundException):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, NotFoundError):
        output_error(str(e))
        sys.exit(1)
    elif isinstance(e, SeqeraError):
        output_error(str(e))
        sys.exit(1)
    else:
        output_error(f"Unexpected error: {e}")
        sys.exit(1)


def output_response(response: object, output_format: OutputFormat) -> None:
    """Output a response in the specified format."""
    if output_format == OutputFormat.JSON:
        output_json(response.to_dict())
    elif output_format == OutputFormat.YAML:
        output_yaml(response.to_dict())
    else:  # console
        output_console(response.to_console())


def get_organizations_list(client: SeqeraClient) -> tuple:
    """
    Fetch the list of organizations for the user.

    Returns:
        Tuple of (user_name, organizations_list)
    """
    # Get user info
    user_info = client.get("/user-info")
    user_id = user_info.get("user", {}).get("id")
    user_name = user_info.get("user", {}).get("userName", "")

    # Get workspaces (which includes organizations)
    workspaces_response = client.get(f"/user/{user_id}/workspaces")
    orgs_and_workspaces = workspaces_response.get("orgsAndWorkspaces", [])

    # Extract unique organizations (filter out workspace entries)
    organizations_map = {}
    for item in orgs_and_workspaces:
        org_id = item.get("orgId")
        if org_id and not item.get("workspaceId"):  # Only org-level entries
            if org_id not in organizations_map:
                organizations_map[org_id] = {
                    "orgId": org_id,
                    "orgName": item.get("orgName"),
                    "orgLogoUrl": item.get("orgLogoUrl"),
                }

    organizations = list(organizations_map.values())
    return user_name, organizations


def find_organization_by_name(client: SeqeraClient, org_name: str) -> Optional[dict]:
    """
    Find an organization by name.

    Returns:
        Organization dict with orgId if found, None otherwise
    """
    user_name, organizations = get_organizations_list(client)
    for org in organizations:
        if org.get("orgName") == org_name:
            return org
    return None


@app.command("list")
def list_organizations() -> None:
    """List all organizations for the current user."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Get organizations list
        user_name, organizations = get_organizations_list(client)

        # Output response
        result = OrganizationsList(
            user_name=user_name,
            organizations=organizations,
        )

        output_response(result, output_format)

    except Exception as e:
        handle_organizations_error(e)


@app.command("view")
def view_organization(
    name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Organization name"),
    ] = None,
    org_id: Annotated[
        Optional[int],
        typer.Option("-i", "--id", help="Organization ID"),
    ] = None,
) -> None:
    """View organization details."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Must provide either name or ID
        if not name and not org_id:
            output_error("Must provide either --name or --id")
            sys.exit(1)

        # If name provided, find the org ID
        if name and not org_id:
            org = find_organization_by_name(client, name)
            if not org:
                raise OrganizationNotFoundException(name)
            org_id = org.get("orgId")

        # Get organization details
        org_response = client.get(f"/orgs/{org_id}")
        organization = org_response.get("organization", {})

        # Output response
        result = OrganizationView(organization=organization)
        output_response(result, output_format)

    except Exception as e:
        handle_organizations_error(e)


@app.command("delete")
def delete_organization(
    name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Organization name"),
    ] = None,
    org_id: Annotated[
        Optional[int],
        typer.Option("-i", "--id", help="Organization ID"),
    ] = None,
) -> None:
    """Delete an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Must provide either name or ID
        if not name and not org_id:
            output_error("Must provide either --name or --id")
            sys.exit(1)

        # If name provided, find the org ID
        org_name = name
        if name and not org_id:
            org = find_organization_by_name(client, name)
            if not org:
                raise OrganizationNotFoundException(name)
            org_id = org.get("orgId")
            org_name = org.get("orgName", name)

        # Delete organization
        try:
            client.delete(f"/orgs/{org_id}")
        except Exception as e:
            # Check if it's a server error
            if hasattr(e, "status_code") and e.status_code >= 500:
                raise SeqeraError(f"Organization {org_name} could not be deleted")
            raise

        # Output response
        result = OrganizationDeleted(organization_name=org_name)
        output_response(result, output_format)

    except Exception as e:
        handle_organizations_error(e)


@app.command("add")
def add_organization(
    name: Annotated[
        str,
        typer.Option("-n", "--name", help="Organization name"),
    ],
    full_name: Annotated[
        str,
        typer.Option("-f", "--full-name", help="Organization full name"),
    ],
    description: Annotated[
        Optional[str],
        typer.Option("-d", "--description", help="Organization description"),
    ] = None,
    location: Annotated[
        Optional[str],
        typer.Option("-l", "--location", help="Organization location"),
    ] = None,
    website: Annotated[
        Optional[str],
        typer.Option("-w", "--website", help="Organization website"),
    ] = None,
    overwrite: Annotated[
        bool,
        typer.Option("--overwrite", help="Overwrite if organization already exists"),
    ] = False,
) -> None:
    """Add a new organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # If overwrite, delete existing organization first
        if overwrite:
            try:
                org = find_organization_by_name(client, name)
                if org:
                    org_id = org.get("orgId")
                    client.delete(f"/orgs/{org_id}")
            except Exception:
                # Ignore errors during overwrite deletion
                pass

        # Build organization payload
        payload = {
            "organization": {
                "name": name,
                "fullName": full_name,
            }
        }

        if description:
            payload["organization"]["description"] = description
        if location:
            payload["organization"]["location"] = location
        if website:
            payload["organization"]["website"] = website

        # Create organization
        org_response = client.post("/orgs", json=payload)
        organization = org_response.get("organization", {})

        # Output response
        result = OrganizationAdded(organization=organization)
        output_response(result, output_format)

    except Exception as e:
        handle_organizations_error(e)


@app.command("update")
def update_organization(
    name: Annotated[
        Optional[str],
        typer.Option("-n", "--name", help="Organization name"),
    ] = None,
    org_id: Annotated[
        Optional[int],
        typer.Option("-i", "--id", help="Organization ID"),
    ] = None,
    new_name: Annotated[
        Optional[str],
        typer.Option("--new-name", help="New organization name"),
    ] = None,
    full_name: Annotated[
        Optional[str],
        typer.Option("-f", "--full-name", help="Organization full name"),
    ] = None,
    description: Annotated[
        Optional[str],
        typer.Option("-d", "--description", help="Organization description"),
    ] = None,
    location: Annotated[
        Optional[str],
        typer.Option("-l", "--location", help="Organization location"),
    ] = None,
    website: Annotated[
        Optional[str],
        typer.Option("-w", "--website", help="Organization website"),
    ] = None,
) -> None:
    """Update an organization."""
    try:
        client = get_client()
        output_format = get_output_format()

        # Must provide either name or ID
        if not name and not org_id:
            output_error("Must provide either --name or --id")
            sys.exit(1)

        # If name provided, find the org ID
        org_name = name
        if name and not org_id:
            org = find_organization_by_name(client, name)
            if not org:
                raise OrganizationNotFoundException(name)
            org_id = org.get("orgId")
            org_name = org.get("orgName", name)

        # Get existing organization details
        org_response = client.get(f"/orgs/{org_id}")
        existing_org = org_response.get("organization", {})

        # Build update payload with existing values as defaults
        payload = {
            "organization": {
                "name": new_name if new_name else existing_org.get("name"),
                "fullName": full_name if full_name else existing_org.get("fullName"),
            }
        }

        # Add optional fields if provided, otherwise keep existing
        if description is not None:
            payload["organization"]["description"] = description
        elif existing_org.get("description"):
            payload["organization"]["description"] = existing_org.get("description")

        if location is not None:
            payload["organization"]["location"] = location
        elif existing_org.get("location"):
            payload["organization"]["location"] = existing_org.get("location")

        if website is not None:
            payload["organization"]["website"] = website
        elif existing_org.get("website"):
            payload["organization"]["website"] = existing_org.get("website")

        # Update organization
        client.put(f"/orgs/{org_id}", json=payload)

        # Output response
        result = OrganizationUpdated(org_id=org_id, organization_name=org_name)
        output_response(result, output_format)

    except Exception as e:
        handle_organizations_error(e)
