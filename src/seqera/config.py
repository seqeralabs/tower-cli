"""
Configuration loader for Seqera CLI.

Implements layered configuration with the following precedence (highest to lowest):
1. Environment variables (SEQERA_* or TOWER_*)
2. TOML config file ($XDG_CONFIG_HOME/seqera/config.toml or ~/.config/seqera/config.toml)
3. Nextflow auth config ($NXF_HOME/seqera-auth.config or ~/.nextflow/seqera-auth.config)
4. Default values
"""

from __future__ import annotations

import os
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


@dataclass
class SeqeraConfig:
    """Configuration for Seqera CLI."""

    access_token: str | None = None
    url: str = "https://api.cloud.seqera.io"
    workspace_id: str | None = None
    compute_env_id: str | None = None

    # Track where each value came from for debugging
    _sources: dict[str, str] = field(default_factory=dict)

    def get_source(self, key: str) -> str | None:
        """Get the source of a configuration value."""
        return self._sources.get(key)


def get_xdg_config_home() -> Path:
    """Get XDG_CONFIG_HOME or default to ~/.config."""
    xdg_config = os.environ.get("XDG_CONFIG_HOME")
    if xdg_config:
        return Path(xdg_config)
    return Path.home() / ".config"


def get_nxf_home() -> Path:
    """Get NXF_HOME or default to ~/.nextflow."""
    nxf_home = os.environ.get("NXF_HOME")
    if nxf_home:
        return Path(nxf_home)
    return Path.home() / ".nextflow"


def parse_toml_config(config_path: Path) -> dict[str, Any]:
    """Parse a TOML configuration file.

    Args:
        config_path: Path to the TOML file

    Returns:
        Dictionary of configuration values
    """
    if not config_path.exists():
        return {}

    try:
        # Try to use tomllib (Python 3.11+) or tomli as fallback
        try:
            import tomllib

            with open(config_path, "rb") as f:
                return tomllib.load(f)
        except ImportError:
            try:
                import tomli

                with open(config_path, "rb") as f:
                    return tomli.load(f)
            except ImportError:
                # Manual simple TOML parsing for basic key=value pairs
                return _parse_simple_toml(config_path)
    except Exception:
        return {}


def _parse_simple_toml(config_path: Path) -> dict[str, Any]:
    """Simple TOML parser for basic key=value pairs.

    Handles basic TOML syntax without external dependencies.
    """
    result: dict[str, Any] = {}
    current_section: dict[str, Any] = result

    with open(config_path) as f:
        for line in f:
            line = line.strip()

            # Skip empty lines and comments
            if not line or line.startswith("#"):
                continue

            # Handle section headers [section]
            if line.startswith("[") and line.endswith("]"):
                section_name = line[1:-1].strip()
                if section_name not in result:
                    result[section_name] = {}
                current_section = result[section_name]
                continue

            # Handle key = value pairs
            if "=" in line:
                key, value = line.split("=", 1)
                key = key.strip()
                value = value.strip()

                # Remove quotes from string values
                if (value.startswith('"') and value.endswith('"')) or (
                    value.startswith("'") and value.endswith("'")
                ):
                    value = value[1:-1]

                # Handle boolean values
                if value.lower() == "true":
                    current_section[key] = True
                elif value.lower() == "false":
                    current_section[key] = False
                # Handle integer values
                elif value.isdigit():
                    current_section[key] = int(value)
                else:
                    current_section[key] = value

    return result


def parse_nextflow_config(config_path: Path) -> dict[str, Any]:
    """Parse a Nextflow-style configuration file.

    Handles basic Nextflow config syntax:
        tower {
            accessToken = 'xxx'
            endpoint = 'https://...'
            workspaceId = '123'
        }

    Args:
        config_path: Path to the Nextflow config file

    Returns:
        Dictionary of configuration values
    """
    if not config_path.exists():
        return {}

    result: dict[str, Any] = {}

    try:
        content = config_path.read_text()

        # Pattern to match tower or seqera block
        block_pattern = r"(?:tower|seqera)\s*\{([^}]*)\}"
        block_match = re.search(block_pattern, content, re.DOTALL | re.IGNORECASE)

        if block_match:
            block_content = block_match.group(1)

            # Pattern to match key = value pairs
            # Handles: key = 'value', key = "value", key = value
            pair_pattern = r"(\w+)\s*=\s*['\"]?([^'\"\n]+)['\"]?"

            for match in re.finditer(pair_pattern, block_content):
                key = match.group(1).strip()
                value = match.group(2).strip()

                # Map Nextflow config keys to our config keys
                if key == "accessToken":
                    result["access_token"] = value
                elif key == "endpoint":
                    result["url"] = value
                elif key == "workspaceId":
                    result["workspace_id"] = value
                elif key == "computeEnvId":
                    result["compute_env_id"] = value

    except Exception:
        pass

    return result


def load_toml_config() -> tuple[dict[str, Any], str | None]:
    """Load configuration from TOML file.

    Returns:
        Tuple of (config dict, source path or None)
    """
    config_path = get_xdg_config_home() / "seqera" / "config.toml"

    if config_path.exists():
        config = parse_toml_config(config_path)
        return config, str(config_path)

    return {}, None


def load_nextflow_config() -> tuple[dict[str, Any], str | None]:
    """Load configuration from Nextflow auth config file.

    Returns:
        Tuple of (config dict, source path or None)
    """
    nxf_home = get_nxf_home()
    config_path = nxf_home / "seqera-auth.config"

    if config_path.exists():
        config = parse_nextflow_config(config_path)
        return config, str(config_path)

    return {}, None


def load_config(
    env_access_token: str | None = None,
    env_url: str | None = None,
) -> SeqeraConfig:
    """Load configuration with layered precedence.

    Precedence (highest to lowest):
    1. Values passed as arguments (from env vars via Typer)
    2. TOML config file ($XDG_CONFIG_HOME/seqera/config.toml)
    3. Nextflow auth config ($NXF_HOME/seqera-auth.config)
    4. Default values

    Args:
        env_access_token: Access token from environment/CLI (highest priority)
        env_url: API URL from environment/CLI (highest priority)

    Returns:
        SeqeraConfig with merged configuration
    """
    config = SeqeraConfig()

    # Layer 3: Load Nextflow config (lowest priority)
    nxf_config, nxf_source = load_nextflow_config()
    if nxf_config:
        if "access_token" in nxf_config:
            config.access_token = nxf_config["access_token"]
            config._sources["access_token"] = nxf_source or "nextflow"
        if "url" in nxf_config:
            config.url = nxf_config["url"]
            config._sources["url"] = nxf_source or "nextflow"
        if "workspace_id" in nxf_config:
            config.workspace_id = nxf_config["workspace_id"]
            config._sources["workspace_id"] = nxf_source or "nextflow"
        if "compute_env_id" in nxf_config:
            config.compute_env_id = nxf_config["compute_env_id"]
            config._sources["compute_env_id"] = nxf_source or "nextflow"

    # Layer 2: Load TOML config (medium priority, overrides Nextflow)
    toml_config, toml_source = load_toml_config()
    if toml_config:
        if "access_token" in toml_config:
            config.access_token = toml_config["access_token"]
            config._sources["access_token"] = toml_source or "toml"
        if "url" in toml_config:
            config.url = toml_config["url"]
            config._sources["url"] = toml_source or "toml"
        if "workspace_id" in toml_config:
            config.workspace_id = toml_config["workspace_id"]
            config._sources["workspace_id"] = toml_source or "toml"
        if "compute_env_id" in toml_config:
            config.compute_env_id = toml_config["compute_env_id"]
            config._sources["compute_env_id"] = toml_source or "toml"

    # Layer 1: Environment variables / CLI arguments (highest priority)
    if env_access_token:
        config.access_token = env_access_token
        config._sources["access_token"] = "environment"
    if env_url:
        config.url = env_url
        config._sources["url"] = "environment"

    # Also check for workspace from environment (not in Typer options currently)
    env_workspace = os.environ.get("SEQERA_WORKSPACE") or os.environ.get("TOWER_WORKSPACE_ID")
    if env_workspace:
        config.workspace_id = env_workspace
        config._sources["workspace_id"] = "environment"

    env_compute_env = os.environ.get("SEQERA_COMPUTE_ENV") or os.environ.get("TOWER_COMPUTE_ENV")
    if env_compute_env:
        config.compute_env_id = env_compute_env
        config._sources["compute_env_id"] = "environment"

    return config
