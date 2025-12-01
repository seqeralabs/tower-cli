"""
Info command for Seqera CLI.

Display system information and health status.
"""

import sys
from typing import Dict, Optional

import typer

from seqera.exceptions import AuthenticationError, SeqeraError
from seqera.main import get_client, get_output_format
from seqera.responses import InfoResponse
from seqera.utils.output import OutputFormat, output_console, output_error, output_json, output_yaml


def get_cli_version() -> str:
    """Get CLI version from package metadata."""
    try:
        from importlib.metadata import version

        return version("seqera-cli")
    except Exception:
        return "0.1.0"


def get_cli_api_version() -> str:
    """Get CLI minimum API version."""
    # This should match the minimum supported Tower API version
    return "1.0.0"


def parse_version(version_str: str) -> tuple:
    """Parse version string into tuple for comparison."""
    try:
        parts = version_str.split(".")
        return tuple(int(p) for p in parts if p.isdigit())
    except Exception:
        return (0,)


def info() -> None:
    """
    Display system information and health status.

    Shows CLI version, Tower API endpoint, Tower version, and performs
    health checks on connectivity, API version compatibility, and credentials.
    """
    connection_check = 1
    version_check = -1  # -1 = skipped
    credentials_check = -1  # -1 = skipped

    tower_version: str | None = None
    tower_api_version: str | None = None
    user_name: str | None = None

    # Get CLI version info
    cli_version = get_cli_version()
    cli_api_version = get_cli_api_version()

    client = get_client()
    output_format = get_output_format()

    # Get Tower API endpoint
    tower_api_endpoint = client.base_url

    # Try to get service info
    try:
        service_info_response = client.get("/service-info")
        service_info = service_info_response.get("serviceInfo", {})

        if service_info:
            tower_api_version = service_info.get("apiVersion")
            tower_version = service_info.get("version")

            # Check version compatibility
            if tower_api_version and cli_api_version:
                tower_ver_tuple = parse_version(tower_api_version)
                cli_ver_tuple = parse_version(cli_api_version)
                version_check = 1 if tower_ver_tuple >= cli_ver_tuple else 0
    except Exception:
        # Connection failed
        connection_check = 0

    # Try to get user info (only if connection succeeded)
    if connection_check == 1:
        try:
            user_info_response = client.get("/user-info")
            user = user_info_response.get("user", {})
            if user:
                user_name = user.get("userName")
                credentials_check = 1
        except AuthenticationError:
            # Authentication failed (401)
            credentials_check = 0
        except Exception:
            # Other errors don't necessarily mean auth failure
            pass

    # Build options dict
    opts: dict[str, str | None] = {
        "cliVersion": cli_version,
        "cliApiVersion": cli_api_version,
        "towerApiVersion": tower_api_version,
        "towerVersion": tower_version,
        "towerApiEndpoint": tower_api_endpoint,
        "userName": user_name,
    }

    # Create response
    response = InfoResponse(
        connection_check=connection_check,
        version_check=version_check,
        credentials_check=credentials_check,
        opts=opts,
    )

    # Output response
    if output_format == OutputFormat.JSON:
        output_json(response.to_dict())
    elif output_format == OutputFormat.YAML:
        output_yaml(response.to_dict())
    else:  # console
        output_console(response.to_console())

    # Exit with appropriate code
    sys.exit(response.get_exit_code())
