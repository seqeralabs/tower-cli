#!/usr/bin/env python3
"""
Apply enriched CLI descriptions to Java source files.

This script takes enriched CLI metadata and updates @Option annotations in Java
source files with the enhanced descriptions. It preserves all other annotation
attributes and code formatting.

Usage:
    python apply-descriptions.py [--metadata PATH] [--command CLASS_NAME] [--source-dir PATH] [--dry-run]

Arguments:
    --metadata    Path to enriched CLI metadata JSON (default: ../cli-metadata-enriched.json)
    --command     Specific command class to update (e.g., LaunchCmd). If omitted, updates all commands.
    --source-dir  Root directory of Java source files (default: ../../src/main/java)
    --dry-run     Show changes without modifying files
"""

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Dict, List, Optional, Tuple


class JavaDescriptionUpdater:
    """Updates @Option descriptions in Java source files."""

    def __init__(self, metadata: Dict, source_dir: Path, dry_run: bool = False):
        self.metadata = metadata
        self.source_dir = source_dir
        self.dry_run = dry_run
        self.stats = {
            "commands_processed": 0,
            "files_updated": 0,
            "options_updated": 0,
            "options_skipped": 0,
            "errors": []
        }

    def update_all_commands(self) -> Dict:
        """Update all commands in the metadata."""
        for qualified_name, command_data in self.metadata.get("commands", {}).items():
            # Only process commands that have a source file
            if "source_file" not in command_data:
                continue

            # Only process commands that have options with api_source (enriched)
            has_enriched = any(
                "api_source" in opt
                for opt in command_data.get("options", [])
            )

            if not has_enriched:
                continue

            self._update_command(qualified_name, command_data)

        return self.stats

    def update_command(self, command_class_name: str) -> Dict:
        """Update a specific command by class name or fully qualified name."""
        # Find the command in metadata
        qualified_name = None

        # First try exact match (fully qualified name)
        if command_class_name in self.metadata.get("commands", {}):
            qualified_name = command_class_name
        else:
            # Try matching by simple class name (suffix)
            for qn, cmd_data in self.metadata.get("commands", {}).items():
                if qn.endswith(f".{command_class_name}"):
                    qualified_name = qn
                    break

        if not qualified_name:
            raise ValueError(f"Command {command_class_name} not found in metadata")

        command_data = self.metadata["commands"][qualified_name]
        self._update_command(qualified_name, command_data)

        return self.stats

    def _update_command(self, qualified_name: str, command_data: Dict):
        """Update a single command's Java source file."""
        source_file_rel = command_data.get("source_file")
        if not source_file_rel:
            self.stats["errors"].append(f"No source file for {qualified_name}")
            return

        # Resolve source file path
        # source_file_rel is like "../src/main/java/io/seqera/tower/cli/commands/LaunchCmd.java"
        # We need to extract just the package path part
        parts = Path(source_file_rel).parts
        # Skip the ".." parts and reconstruct from "io/seqera/..." onwards
        if parts[0] == "..":
            # Find where "io" starts (package root)
            try:
                io_index = parts.index("io")
                relative_path = Path(*parts[io_index:])
                source_file = self.source_dir / relative_path
            except ValueError:
                # Fallback: try to use as-is minus the ".."
                relative_path = Path(*[p for p in parts if p != ".."])
                source_file = self.source_dir / relative_path
        else:
            source_file = self.source_dir / source_file_rel

        if not source_file.exists():
            self.stats["errors"].append(f"Source file not found: {source_file}")
            return

        print(f"\nProcessing: {command_data['name']} ({source_file.name})")

        # Read the source file
        try:
            with open(source_file, 'r', encoding='utf-8') as f:
                content = f.read()
        except Exception as e:
            self.stats["errors"].append(f"Error reading {source_file}: {e}")
            return

        # Update options that have api_source (enriched descriptions)
        modified_content = content
        options_updated = 0

        for option in command_data.get("options", []):
            # Only update options with enriched descriptions
            if "api_source" not in option:
                self.stats["options_skipped"] += 1
                continue

            # Get the option names and enriched description
            names = option.get("names", [])
            new_description = option.get("description", "")

            if not names or not new_description:
                continue

            # Try to update the @Option annotation
            updated, modified_content = self._update_option_description(
                modified_content,
                names,
                new_description,
                option
            )

            if updated:
                options_updated += 1
                # Show the primary option name (usually the long form)
                primary_name = names[-1] if len(names) > 1 else names[0]
                print(f"  ✓ Updated {primary_name}")

        # Write back if there were changes
        if options_updated > 0:
            if self.dry_run:
                print(f"\n[DRY RUN] Would update {options_updated} options in {source_file}")
            else:
                try:
                    with open(source_file, 'w', encoding='utf-8') as f:
                        f.write(modified_content)
                    print(f"  → {options_updated} options updated")
                    self.stats["files_updated"] += 1
                except Exception as e:
                    self.stats["errors"].append(f"Error writing {source_file}: {e}")
                    return

            self.stats["options_updated"] += options_updated

        self.stats["commands_processed"] += 1

    def _update_option_description(
        self,
        content: str,
        names: List[str],
        new_description: str,
        option: Dict
    ) -> Tuple[bool, str]:
        """
        Update a single @Option annotation's description.

        Returns:
            (updated: bool, modified_content: str)
        """
        # Escape special characters in the new description for Java string
        escaped_description = self._escape_java_string(new_description)

        # Build a pattern to match the @Option annotation
        # We need to match by the option names to find the right annotation
        names_pattern = self._build_names_pattern(names)

        # Pattern to match @Option or @CommandLine.Option annotation with description
        # This handles both single-line and potential multi-line (though they're usually single-line)
        pattern = (
            r'(@(?:CommandLine\.)?Option\s*\(\s*'    # @Option( or @CommandLine.Option(
            r'names\s*=\s*\{' + names_pattern + r'\}'  # names = {...}
            r'[^)]*?'                         # any other attributes before description
            r',?\s*description\s*=\s*'       # description =
            r'"[^"]*"'                        # old description in quotes
            r'[^)]*'                          # any other attributes after description
            r'\))'                            # )
        )

        # Find the match
        match = re.search(pattern, content, re.DOTALL)

        if not match:
            # Try alternative pattern without description (to add it)
            # For now, skip options without existing descriptions
            return False, content

        # Extract the full annotation
        old_annotation = match.group(0)

        # Replace just the description part within the annotation
        desc_pattern = r'description\s*=\s*"[^"]*"'
        new_annotation = re.sub(
            desc_pattern,
            f'description = "{escaped_description}"',
            old_annotation
        )

        # Replace in content
        modified_content = content.replace(old_annotation, new_annotation, 1)

        return True, modified_content

    def _build_names_pattern(self, names: List[str]) -> str:
        """
        Build a regex pattern to match option names array.

        Handles variations like:
        - {"--name"}
        - {"-n", "--name"}
        - {"--name", "-n"}
        """
        # Escape special regex characters in option names
        escaped_names = [re.escape(name) for name in names]

        # Build pattern that matches names in any order
        # This is a simplified version - it assumes names are quoted strings separated by commas
        if len(names) == 1:
            return rf'"\s*{escaped_names[0]}\s*"'
        else:
            # Match all names in the array (order doesn't matter for matching)
            patterns = [rf'"\s*{name}\s*"' for name in escaped_names]
            # Join with optional whitespace and comma
            return r'\s*,\s*'.join(patterns)

    def _escape_java_string(self, text: str) -> str:
        r"""
        Escape special characters for Java string literals.

        Handles:
        - Double quotes: " -> \"
        - Backslashes: \ -> \\
        - Newlines: \n -> \\n
        - Tabs: \t -> \\t
        """
        # Replace backslashes first (before other escapes that add backslashes)
        escaped = text.replace('\\', '\\\\')
        # Replace double quotes
        escaped = escaped.replace('"', '\\"')
        # Replace newlines and tabs (though descriptions shouldn't have these)
        escaped = escaped.replace('\n', '\\n')
        escaped = escaped.replace('\t', '\\t')
        return escaped


def load_metadata(path: Path) -> Dict:
    """Load enriched metadata JSON."""
    try:
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"Error loading metadata from {path}: {e}", file=sys.stderr)
        sys.exit(1)


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description="Apply enriched descriptions to Java source files",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__
    )

    # Get script directory for default paths
    script_dir = Path(__file__).parent

    parser.add_argument(
        "--metadata",
        type=Path,
        default=script_dir.parent / "cli-metadata-enriched.json",
        help="Path to enriched CLI metadata JSON"
    )
    parser.add_argument(
        "--command",
        type=str,
        help="Specific command class to update (e.g., LaunchCmd)"
    )
    parser.add_argument(
        "--source-dir",
        type=Path,
        default=script_dir.parent.parent / "src" / "main" / "java",
        help="Root directory of Java source files"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show changes without modifying files"
    )

    args = parser.parse_args()

    # Validate inputs
    if not args.metadata.exists():
        print(f"Error: Metadata file not found: {args.metadata}", file=sys.stderr)
        sys.exit(1)

    if not args.source_dir.exists():
        print(f"Error: Source directory not found: {args.source_dir}", file=sys.stderr)
        sys.exit(1)

    # Load metadata
    print(f"Loading metadata from: {args.metadata}")
    metadata = load_metadata(args.metadata)

    # Create updater
    print(f"Source directory: {args.source_dir}")
    if args.dry_run:
        print("\n*** DRY RUN MODE - No files will be modified ***\n")

    updater = JavaDescriptionUpdater(metadata, args.source_dir, args.dry_run)

    # Update command(s)
    if args.command:
        print(f"Updating command: {args.command}")
        stats = updater.update_command(args.command)
    else:
        print("Updating all commands with enriched descriptions")
        stats = updater.update_all_commands()

    # Print statistics
    print("\n=== Update Statistics ===")
    print(f"Commands processed: {stats['commands_processed']}")
    print(f"Files updated: {stats['files_updated']}")
    print(f"Options updated: {stats['options_updated']}")
    print(f"Options skipped: {stats['options_skipped']}")

    if stats['errors']:
        print(f"\nErrors: {len(stats['errors'])}")
        for error in stats['errors']:
            print(f"  - {error}")

    if args.dry_run:
        print("\n(Dry run complete - no files were modified)")
    else:
        print("\n✅ Update complete!")


if __name__ == "__main__":
    main()
