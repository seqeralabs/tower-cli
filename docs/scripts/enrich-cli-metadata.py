#!/usr/bin/env python3
"""
Enrich CLI metadata with descriptions from OpenAPI specification.

This script takes CLI metadata extracted from Java source and enriches it with
high-quality descriptions from the decorated OpenAPI spec, following the mapping
rules defined in cli-to-api-mapping.json.

Usage:
    python enrich-cli-metadata.py [--openapi PATH] [--mapping PATH] [--metadata PATH] [--output PATH]

Arguments:
    --openapi   Path to OpenAPI YAML spec (default: ../seqera-api-latest-decorated.yaml)
    --mapping   Path to CLI-to-API mapping JSON (default: cli-to-api-mapping.json)
    --metadata  Path to CLI metadata JSON (default: ../cli-metadata.json)
    --output    Path to output enriched metadata (default: ../cli-metadata-enriched.json)
"""

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Dict, Optional

try:
    import yaml
except ImportError:
    print("Error: PyYAML is required. Install with: pip install PyYAML", file=sys.stderr)
    sys.exit(1)


class CLIMetadataEnricher:
    """Enriches CLI metadata with OpenAPI descriptions."""

    def __init__(self, openapi_spec: Dict[str, Any], mapping: Dict[str, Any], cli_metadata: Dict[str, Any]):
        self.openapi_spec = openapi_spec
        self.mapping = mapping
        self.cli_metadata = cli_metadata
        self.stats = {
            "commands_processed": 0,
            "options_enriched": 0,
            "options_skipped": 0,
            "api_descriptions_not_found": 0,
            "warnings": []
        }

    def enrich(self) -> Dict[str, Any]:
        """
        Enrich CLI metadata with API descriptions.

        Returns:
            Enhanced metadata structure with API descriptions merged in
        """
        enriched = {
            "metadata": {
                **self.cli_metadata.get("metadata", {}),
                "enrichment_version": "1.0.0",
                "enrichment_source": "OpenAPI decorated spec + cli-to-api-mapping.json"
            },
            "commands": {}
        }

        # Process each command in the CLI metadata
        for qualified_name, command_data in self.cli_metadata.get("commands", {}).items():
            enriched_command = self._enrich_command(qualified_name, command_data)
            enriched["commands"][qualified_name] = enriched_command

        # Add statistics
        enriched["enrichment_stats"] = self.stats

        return enriched

    def _enrich_command(self, qualified_name: str, command_data: Dict[str, Any]) -> Dict[str, Any]:
        """Enrich a single command with API descriptions."""
        # Start with a copy of the original command data
        enriched_command = {**command_data}

        # Check if this command has mappings defined
        # Try multiple lookup strategies:
        # 1. Direct qualified name match (for exact matches)
        # 2. java_class field in mapping (for disambiguating multiple AddCmd, UpdateCmd, etc.)
        # 3. Simple class name match (backward compatible)

        command_class_name = qualified_name.split(".")[-1]  # e.g., "LaunchCmd"
        command_mapping = None

        # Strategy 1: Check if qualified name exists directly in mappings
        if qualified_name in self.mapping.get("mappings", {}):
            command_mapping = self.mapping["mappings"][qualified_name]

        # Strategy 2: Try by class name, but verify with java_class if present
        if not command_mapping and command_class_name in self.mapping.get("mappings", {}):
            potential_mapping = self.mapping["mappings"][command_class_name]
            # If mapping has java_class, verify it matches
            if "java_class" in potential_mapping:
                if potential_mapping["java_class"] == qualified_name:
                    command_mapping = potential_mapping
            else:
                # No java_class, use it
                command_mapping = potential_mapping

        # Strategy 3: Look for pattern-based keys like "AddCmd__secrets"
        if not command_mapping:
            for mapping_key, mapping_value in self.mapping.get("mappings", {}).items():
                if "java_class" in mapping_value and mapping_value["java_class"] == qualified_name:
                    command_mapping = mapping_value
                    break

        if not command_mapping:
            # No mapping for this command, return original data
            return enriched_command

        self.stats["commands_processed"] += 1
        api_schema_name = command_mapping.get("api_schema")

        if not api_schema_name:
            self._add_warning(f"Command {command_class_name} has no api_schema defined in mapping")
            return enriched_command

        # Enrich options
        if "options" in enriched_command:
            enriched_command["options"] = [
                self._enrich_option(opt, command_mapping, api_schema_name)
                for opt in enriched_command["options"]
            ]

        return enriched_command

    def _enrich_option(
        self,
        option: Dict[str, Any],
        command_mapping: Dict[str, Any],
        api_schema_name: str
    ) -> Dict[str, Any]:
        """Enrich a single option with API description."""
        # Start with original option
        enriched_option = {**option}

        # Try to find mapping for this option
        # Look up by the primary option name (without leading dashes)
        option_names = option.get("names", [])
        if not option_names:
            return enriched_option

        # Try to match by the long form option name first (e.g., --params-file)
        mapping_key = None
        for name in option_names:
            # Remove leading dashes
            clean_name = name.lstrip("-")
            if clean_name in command_mapping.get("options", {}):
                mapping_key = clean_name
                break

        if not mapping_key:
            # No mapping found for this option
            self.stats["options_skipped"] += 1
            return enriched_option

        option_mapping = command_mapping["options"][mapping_key]

        # Get API field description
        api_field = option_mapping.get("api_field")
        if not api_field:
            self._add_warning(f"Option {mapping_key} has no api_field defined")
            self.stats["options_skipped"] += 1
            return enriched_option

        # Extract description from OpenAPI spec
        api_description = self._get_api_description(api_schema_name, api_field)

        if not api_description:
            self._add_warning(f"API description not found for {api_schema_name}.{api_field}")
            self.stats["api_descriptions_not_found"] += 1
            return enriched_option

        # Adapt description for CLI context
        original_cli_description = option.get("description", "")
        cli_description = self._adapt_description_for_cli(
            api_description,
            option_mapping,
            original_cli_description
        )

        # Update option with enriched description
        enriched_option["description"] = cli_description
        enriched_option["api_source"] = {
            "schema": api_schema_name,
            "field": api_field,
            "original_description": api_description,
            "transformation": option_mapping.get("transformation", "direct")
        }

        self.stats["options_enriched"] += 1
        return enriched_option

    def _get_api_description(self, schema_name: str, field_name: str) -> Optional[str]:
        """
        Extract description from OpenAPI spec.

        Args:
            schema_name: Name of the schema (e.g., "WorkflowLaunchRequest")
            field_name: Name of the field (e.g., "preRunScript")

        Returns:
            Description string or None if not found
        """
        try:
            schemas = self.openapi_spec.get("components", {}).get("schemas", {})
            schema = schemas.get(schema_name, {})
            properties = schema.get("properties", {})
            field = properties.get(field_name, {})
            return field.get("description")
        except (KeyError, AttributeError) as e:
            self._add_warning(f"Error accessing {schema_name}.{field_name}: {e}")
            return None

    def _adapt_description_for_cli(
        self,
        api_description: str,
        option_mapping: Dict[str, Any],
        original_cli_description: str = ""
    ) -> str:
        """
        Adapt API description for CLI context.

        Adds contextual notes based on transformation type:
        - file_to_text: Add note about providing file path
        - name_to_id: Add note about providing name or identifier
        - objects_to_ids: Blend API description with CLI-specific format info
        - direct: Use as-is

        Also converts markdown links to plain text format suitable for CLI help output.

        Args:
            api_description: Original API description
            option_mapping: Mapping configuration for this option
            original_cli_description: Original CLI description (for preserving CLI-specific details)

        Returns:
            Adapted description string
        """
        transformation = option_mapping.get("transformation", "direct")
        cli_type = option_mapping.get("cli_type", "")

        # Convert markdown links to plain text format for CLI
        adapted = self._convert_markdown_links_to_plain_text(api_description)

        # Add transformation-specific context
        if transformation == "file_to_text":
            # CLI accepts a file path, but API expects content
            if "Path" in cli_type:
                adapted += " Provide the path to a file containing the content."

        elif transformation == "name_to_id":
            # CLI accepts name, but API expects ID
            adapted += " Provide the name or identifier."

        elif transformation == "objects_to_ids":
            # CLI accepts objects, but API expects IDs
            # Blend API description with CLI-specific format information
            cli_field = option_mapping.get("cli_field", "")

            if cli_field == "labels":
                # Special handling for labels: preserve CLI format info
                # Extract format info from original CLI description
                if "key=value" in original_cli_description:
                    # Use API description for "what it does", add CLI format info
                    # API says "Array of label IDs to assign to each pipeline run"
                    # CLI needs "provide labels directly, use key=value for resource labels"
                    adapted = "Labels to assign to each pipeline run. Provide comma-separated label values (use key=value format for resource labels)"
                    if "notes" in option_mapping and "create" in option_mapping["notes"].lower():
                        adapted += ". Labels will be created if they don't exist"
                else:
                    # Generic objects_to_ids handling
                    if "notes" in option_mapping and "create" in option_mapping["notes"].lower():
                        adapted += ". Labels can be created if they don't exist"

        # Add any additional notes from mapping (if not already covered)
        if "notes" in option_mapping and transformation not in ["objects_to_ids"]:
            notes = option_mapping["notes"]
            # Skip note if we've already incorporated the information in the adapted description
            # For file_to_text, we already added file path context, so skip notes about file paths
            if transformation == "file_to_text" and "file" in notes.lower():
                # Already covered with "Provide the path to a file containing the content."
                pass
            elif "file path" not in adapted.lower() and "file path" in notes.lower():
                # Add note only if not already mentioned in adapted description
                adapted += f" Note: {notes}"

        return adapted

    def _convert_markdown_links_to_plain_text(self, text: str) -> str:
        """
        Convert markdown links to plain text format suitable for CLI help.

        Converts: "Some text. See [Link Text](https://url)"
        To: "Some text. See: https://url"

        Args:
            text: Text potentially containing markdown links

        Returns:
            Text with markdown links converted to plain text
        """
        import re

        # Pattern to match markdown links: [text](url)
        # Handle case where link is preceded by "See " to avoid duplication
        pattern = r'(?:See\s+)?\[([^\]]+)\]\(([^\)]+)\)'

        def replace_link(match):
            url = match.group(2)
            # If the text already had "See " before the link, just use the URL
            # Otherwise include "See:" prefix
            return f"See: {url}"

        return re.sub(pattern, replace_link, text)

    def _add_warning(self, message: str):
        """Add a warning to the stats."""
        self.stats["warnings"].append(message)
        print(f"Warning: {message}", file=sys.stderr)


def load_yaml(path: Path) -> Dict[str, Any]:
    """Load and parse a YAML file."""
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return yaml.safe_load(f)
    except Exception as e:
        print(f"Error loading YAML from {path}: {e}", file=sys.stderr)
        sys.exit(1)


def load_json(path: Path) -> Dict[str, Any]:
    """Load and parse a JSON file."""
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"Error loading JSON from {path}: {e}", file=sys.stderr)
        sys.exit(1)


def save_json(data: Dict[str, Any], path: Path):
    """Save data to a JSON file with pretty formatting."""
    try:
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"Enriched metadata saved to: {path}")
    except Exception as e:
        print(f"Error saving JSON to {path}: {e}", file=sys.stderr)
        sys.exit(1)


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description="Enrich CLI metadata with OpenAPI descriptions",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__
    )

    # Get script directory for default paths
    script_dir = Path(__file__).parent

    parser.add_argument(
        "--openapi",
        type=Path,
        default=script_dir.parent / "seqera-api-latest-decorated.yaml",
        help="Path to OpenAPI YAML spec"
    )
    parser.add_argument(
        "--mapping",
        type=Path,
        default=script_dir / "cli-to-api-mapping.json",
        help="Path to CLI-to-API mapping JSON"
    )
    parser.add_argument(
        "--metadata",
        type=Path,
        default=script_dir.parent / "cli-metadata.json",
        help="Path to CLI metadata JSON"
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=script_dir.parent / "cli-metadata-enriched.json",
        help="Path to output enriched metadata"
    )

    args = parser.parse_args()

    # Validate input files exist
    for file_path, name in [(args.openapi, "OpenAPI spec"),
                             (args.mapping, "Mapping file"),
                             (args.metadata, "CLI metadata")]:
        if not file_path.exists():
            print(f"Error: {name} not found at: {file_path}", file=sys.stderr)
            sys.exit(1)

    # Load input files
    print(f"Loading OpenAPI spec from: {args.openapi}")
    openapi_spec = load_yaml(args.openapi)

    print(f"Loading mapping configuration from: {args.mapping}")
    mapping = load_json(args.mapping)

    print(f"Loading CLI metadata from: {args.metadata}")
    cli_metadata = load_json(args.metadata)

    # Enrich metadata
    print("\nEnriching CLI metadata with API descriptions...")
    enricher = CLIMetadataEnricher(openapi_spec, mapping, cli_metadata)
    enriched_metadata = enricher.enrich()

    # Save output
    save_json(enriched_metadata, args.output)

    # Print statistics
    stats = enriched_metadata["enrichment_stats"]
    print("\n=== Enrichment Statistics ===")
    print(f"Commands processed: {stats['commands_processed']}")
    print(f"Options enriched: {stats['options_enriched']}")
    print(f"Options skipped (no mapping): {stats['options_skipped']}")
    print(f"API descriptions not found: {stats['api_descriptions_not_found']}")

    if stats['warnings']:
        print(f"\nWarnings: {len(stats['warnings'])}")
        print("Check stderr for details")

    print("\n✅ Enrichment complete!")


if __name__ == "__main__":
    main()
