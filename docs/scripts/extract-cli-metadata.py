#!/usr/bin/env python3
"""
CLI Metadata Extractor for tower-cli

Parses picocli annotations from Java source files to extract:
- Command hierarchy (parent → subcommand → sub-subcommand)
- @Command annotations: name, description, subcommands
- @Option annotations: names, description, required, default, arity
- @Parameters annotations: description, arity, paramLabel
- @Mixin references for shared options

Output: Structured JSON representing the full CLI command tree

Usage:
    python extract_cli_metadata.py /path/to/tower-cli/src/main/java > cli-metadata.json
"""

import os
import re
import json
import sys
from pathlib import Path
from dataclasses import dataclass, field, asdict
from typing import Optional

# Global constants cache for resolving references
CONSTANTS_CACHE = {}


@dataclass
class Parameter:
    """Represents a @Parameters annotation (positional argument)"""
    index: str = "0"
    param_label: Optional[str] = None
    description: Optional[str] = None
    arity: Optional[str] = None
    required: bool = True


@dataclass
class Option:
    """Represents an @Option annotation"""
    names: list = field(default_factory=list)  # e.g., ["-n", "--name"]
    description: Optional[str] = None
    required: bool = False
    default_value: Optional[str] = None
    arity: Optional[str] = None
    split: Optional[str] = None  # For comma-separated values
    hidden: bool = False


@dataclass 
class Mixin:
    """Represents a @Mixin annotation referencing shared options"""
    class_name: str
    field_name: str


@dataclass
class Command:
    """Represents a @Command annotation"""
    name: str
    description: Optional[str] = None
    subcommands: list = field(default_factory=list)  # List of subcommand class names
    options: list = field(default_factory=list)
    parameters: list = field(default_factory=list)
    mixins: list = field(default_factory=list)
    source_file: Optional[str] = None
    parent: Optional[str] = None  # Parent command name (resolved later)
    full_command: Optional[str] = None  # Full command path like "compute-envs add aws-batch"


def extract_constants_from_file(file_path: Path) -> dict:
    """Extract public static final String constants from a Java file."""
    try:
        content = file_path.read_text(encoding='utf-8')
    except:
        return {}

    constants = {}
    class_name = get_fully_qualified_class_name(file_path)
    if not class_name:
        # Try to extract simple class name from file
        simple_name = file_path.stem
        class_name = simple_name

    # Pattern to match: public static final String CONSTANT_NAME = "value";
    pattern = r'public\s+static\s+final\s+String\s+(\w+)\s*=\s*([^;]+);'

    for match in re.finditer(pattern, content):
        const_name = match.group(1)
        const_value_raw = match.group(2).strip()

        # Try to extract the actual string value
        const_value = extract_string_value_simple(const_value_raw)
        if const_value:
            # Store both simple name and qualified name
            constants[const_name] = const_value
            constants[f"{class_name}.{const_name}"] = const_value
            # Also store short class name + constant
            simple_name = class_name.split('.')[-1]
            constants[f"{simple_name}.{const_name}"] = const_value

    return constants


def extract_string_value_simple(text: str) -> Optional[str]:
    """Simple string extraction without constant resolution."""
    if not text:
        return None

    text = text.strip()

    # Handle concatenated strings like "foo" + "bar"
    if '+' in text:
        parts = re.findall(r'"([^"]*)"', text)
        if parts:
            return ''.join(parts)

    # Handle simple quoted strings
    if text.startswith('"') and text.endswith('"'):
        return text[1:-1]

    # Handle any remaining quoted strings
    parts = re.findall(r'"([^"]*)"', text)
    if parts:
        return ''.join(parts)

    return None


def extract_string_value(text: str) -> Optional[str]:
    """Extract string value from annotation, handling multiline, concatenation, and constant references."""
    if not text:
        return None

    # Remove outer quotes and handle string concatenation
    text = text.strip()

    # Handle concatenated strings like "foo" + "bar" first (before simple check)
    if '+' in text:
        parts = re.findall(r'"([^"]*)"', text)
        if parts:
            return ''.join(parts)

    # Handle simple quoted strings
    if text.startswith('"') and text.endswith('"'):
        return text[1:-1]

    # Handle any remaining quoted strings
    parts = re.findall(r'"([^"]*)"', text)
    if parts:
        return ''.join(parts)

    # Check if it's a constant reference (e.g., "DESCRIPTION" or "ClassName.DESCRIPTION")
    if text and not text.startswith('"') and text in CONSTANTS_CACHE:
        return CONSTANTS_CACHE[text]

    return text


def extract_array_values(text: str) -> list:
    """Extract array values from annotation like {"-n", "--name"} or {Cmd1.class, Cmd2.class}"""
    if not text:
        return []
    
    text = text.strip()
    
    # Remove curly braces if present
    if text.startswith('{') and text.endswith('}'):
        text = text[1:-1]
    
    # Handle .class references (for subcommands)
    class_matches = re.findall(r'(\w+)\.class', text)
    if class_matches:
        return class_matches
    
    # Handle string arrays
    string_matches = re.findall(r'"([^"]*)"', text)
    if string_matches:
        return string_matches
    
    return []


def parse_annotation_params(content: str) -> dict:
    """Parse annotation parameters into a dictionary."""
    params = {}
    
    # Handle the case where content might span multiple lines
    content = ' '.join(content.split())
    
    # Pattern to match key=value pairs, handling nested braces and quotes
    # This is a simplified parser - handles most common cases
    
    current_key = None
    current_value = []
    brace_depth = 0
    paren_depth = 0
    in_string = False
    
    i = 0
    while i < len(content):
        char = content[i]
        
        if char == '"' and (i == 0 or content[i-1] != '\\'):
            in_string = not in_string
            current_value.append(char)
        elif in_string:
            current_value.append(char)
        elif char == '{':
            brace_depth += 1
            current_value.append(char)
        elif char == '}':
            brace_depth -= 1
            current_value.append(char)
        elif char == '(':
            paren_depth += 1
            current_value.append(char)
        elif char == ')':
            paren_depth -= 1
            current_value.append(char)
        elif char == '=' and brace_depth == 0 and paren_depth == 0:
            current_key = ''.join(current_value).strip()
            current_value = []
        elif char == ',' and brace_depth == 0 and paren_depth == 0:
            if current_key:
                params[current_key] = ''.join(current_value).strip()
            current_key = None
            current_value = []
        else:
            current_value.append(char)
        
        i += 1
    
    # Don't forget the last parameter
    if current_key:
        params[current_key] = ''.join(current_value).strip()
    
    return params


def parse_command_annotation(content: str) -> Optional[Command]:
    """Parse @Command annotation content."""
    params = parse_annotation_params(content)
    
    name = extract_string_value(params.get('name', ''))
    if not name:
        return None
    
    description = extract_string_value(params.get('description', ''))
    subcommands = extract_array_values(params.get('subcommands', ''))
    
    return Command(
        name=name,
        description=description,
        subcommands=subcommands
    )


def parse_option_annotation(content: str) -> Option:
    """Parse @Option annotation content."""
    params = parse_annotation_params(content)
    
    names = extract_array_values(params.get('names', ''))
    description = extract_string_value(params.get('description', ''))
    required = params.get('required', 'false').lower() == 'true'
    default_value = extract_string_value(params.get('defaultValue', ''))
    arity = extract_string_value(params.get('arity', ''))
    split = extract_string_value(params.get('split', ''))
    hidden = params.get('hidden', 'false').lower() == 'true'
    
    return Option(
        names=names,
        description=description,
        required=required,
        default_value=default_value,
        arity=arity,
        split=split,
        hidden=hidden
    )


def parse_parameters_annotation(content: str) -> Parameter:
    """Parse @Parameters annotation content."""
    params = parse_annotation_params(content)
    
    index = extract_string_value(params.get('index', '0')) or '0'
    param_label = extract_string_value(params.get('paramLabel', ''))
    description = extract_string_value(params.get('description', ''))
    arity = extract_string_value(params.get('arity', ''))
    
    return Parameter(
        index=index,
        param_label=param_label,
        description=description,
        arity=arity
    )


def extract_annotation_content(text: str, start_pos: int) -> str:
    """Extract the full content of an annotation starting at the opening parenthesis."""
    if start_pos >= len(text) or text[start_pos] != '(':
        return ""
    
    depth = 0
    content_start = start_pos + 1
    i = start_pos
    
    while i < len(text):
        if text[i] == '(':
            depth += 1
        elif text[i] == ')':
            depth -= 1
            if depth == 0:
                return text[content_start:i]
        i += 1
    
    return text[content_start:]


def get_fully_qualified_class_name(file_path: Path) -> str:
    """Extract fully qualified class name from Java file (package + class name)."""
    try:
        content = file_path.read_text(encoding='utf-8')

        # Extract package name
        package_match = re.search(r'package\s+([\w.]+)\s*;', content)
        package = package_match.group(1) if package_match else None

        # Extract class name
        class_match = re.search(r'(?:public\s+)?(?:abstract\s+)?class\s+(\w+)', content)
        class_name = class_match.group(1) if class_match else file_path.stem

        # Return fully qualified name
        if package and class_name:
            return f"{package}.{class_name}"
        return class_name

    except Exception:
        return file_path.stem


def parse_imports(file_path: Path) -> dict:
    """Parse import statements from Java file.

    Returns:
        dict mapping simple class names to fully qualified names
        Example: {"MembersCmd": "io.seqera.tower.cli.commands.teams.MembersCmd"}
    """
    try:
        content = file_path.read_text(encoding='utf-8')
    except Exception:
        return {}

    imports = {}
    for match in re.finditer(r'import\s+([\w.]+)\s*;', content):
        qualified_name = match.group(1)
        simple_name = qualified_name.split('.')[-1]
        imports[simple_name] = qualified_name

    return imports


def parse_java_file(file_path: Path) -> Optional[Command]:
    """Parse a single Java file for picocli annotations."""
    try:
        content = file_path.read_text(encoding='utf-8')
    except Exception as e:
        print(f"Warning: Could not read {file_path}: {e}", file=sys.stderr)
        return None
    
    # Find @Command annotation (both @Command and @CommandLine.Command)
    command_match = re.search(r'@(?:CommandLine\.)?Command\s*\(', content)
    if not command_match:
        return None
    
    annotation_content = extract_annotation_content(content, command_match.end() - 1)
    command = parse_command_annotation(annotation_content)
    
    if not command:
        return None
    
    command.source_file = str(file_path)
    
    # Find all @Option annotations
    for match in re.finditer(r'@(?:CommandLine\.)?Option\s*\(', content):
        opt_content = extract_annotation_content(content, match.end() - 1)
        option = parse_option_annotation(opt_content)
        if option.names:  # Only add if it has names
            command.options.append(option)
    
    # Find all @Parameters annotations
    for match in re.finditer(r'@(?:CommandLine\.)?Parameters\s*\(', content):
        param_content = extract_annotation_content(content, match.end() - 1)
        param = parse_parameters_annotation(param_content)
        command.parameters.append(param)
    
    # Find all @Mixin annotations
    # Pattern: @Mixin or @CommandLine.Mixin followed by type and field name
    mixin_pattern = r'@(?:CommandLine\.)?Mixin\s+(?:public\s+)?(?:final\s+)?(\w+)\s+(\w+)'
    for match in re.finditer(mixin_pattern, content):
        mixin = Mixin(
            class_name=match.group(1),
            field_name=match.group(2)
        )
        command.mixins.append(mixin)
    
    return command


def build_command_tree(commands: dict) -> dict:
    """Build the command hierarchy tree using import-based resolution."""
    # Create mapping from simple class names to all possible qualified names
    simple_to_qualified_list = {}
    for qualified_name in commands.keys():
        simple_name = qualified_name.split('.')[-1]
        if simple_name not in simple_to_qualified_list:
            simple_to_qualified_list[simple_name] = []
        simple_to_qualified_list[simple_name].append(qualified_name)

    # Map class names to command names for subcommand resolution
    class_to_command = {}
    for class_name, cmd in commands.items():
        class_to_command[class_name] = cmd.name

    # Build parent relationships
    for parent_qualified_name, cmd in commands.items():
        parent_package = '.'.join(parent_qualified_name.split('.')[:-1])
        parent_command_name = cmd.name

        # Parse imports from parent's source file for cross-package resolution
        parent_imports = {}
        if cmd.source_file:
            parent_imports = parse_imports(Path(cmd.source_file))

        for subcommand_class in cmd.subcommands:
            qualified_subcommand = None

            # PRIORITY 1: Try import-based resolution (handles cross-package references)
            if subcommand_class in parent_imports:
                qualified_subcommand = parent_imports[subcommand_class]
                if qualified_subcommand in commands:
                    commands[qualified_subcommand].parent = parent_qualified_name
                    continue

            # PRIORITY 2: Try package-based heuristics
            if subcommand_class in simple_to_qualified_list:
                candidates = simple_to_qualified_list[subcommand_class]

                # First, try to find in same package as parent
                for candidate in candidates:
                    candidate_package = '.'.join(candidate.split('.')[:-1])
                    if candidate_package == parent_package:
                        qualified_subcommand = candidate
                        break

                # If not found, try subpackage matching parent command name
                # e.g., SecretsCmd references AddCmd → look for io.seqera...commands.secrets.AddCmd
                # Handle hyphenated names: "compute-envs" → "computeenvs"
                if not qualified_subcommand:
                    # Try exact match first
                    expected_subpackage = f"{parent_package}.{parent_command_name}"
                    for candidate in candidates:
                        candidate_package = '.'.join(candidate.split('.')[:-1])
                        if candidate_package == expected_subpackage:
                            qualified_subcommand = candidate
                            break

                    # Try with hyphens removed
                    if not qualified_subcommand:
                        normalized_name = parent_command_name.replace('-', '')
                        expected_subpackage_normalized = f"{parent_package}.{normalized_name}"
                        for candidate in candidates:
                            candidate_package = '.'.join(candidate.split('.')[:-1])
                            if candidate_package == expected_subpackage_normalized:
                                qualified_subcommand = candidate
                                break

                # If still not found, try any subpackage starting with parent package
                if not qualified_subcommand:
                    for candidate in candidates:
                        if candidate.startswith(parent_package + '.'):
                            qualified_subcommand = candidate
                            break

                # Fall back to first candidate if still not found
                if not qualified_subcommand:
                    qualified_subcommand = candidates[0]
            else:
                qualified_subcommand = subcommand_class

            if qualified_subcommand in commands:
                commands[qualified_subcommand].parent = parent_qualified_name
    
    # Build full command paths
    def get_full_command(cmd: Command, commands: dict) -> str:
        parts = [cmd.name]
        current = cmd
        visited = set()

        while current.parent and current.parent not in visited:
            visited.add(current.parent)
            # Direct lookup using qualified class name
            parent_cmd = commands.get(current.parent)
            if parent_cmd:
                parts.insert(0, parent_cmd.name)
                current = parent_cmd
            else:
                break

        return ' '.join(parts)
    
    for cmd in commands.values():
        cmd.full_command = get_full_command(cmd, commands)
    
    return commands


def find_java_files(root_dir: Path) -> list:
    """Find all Java files that might contain CLI commands."""
    java_files = []
    
    for path in root_dir.rglob('*.java'):
        # Focus on command files
        if 'Cmd' in path.name or 'Command' in path.name:
            java_files.append(path)
        # Also include Tower.java (the root command)
        elif path.name == 'Tower.java':
            java_files.append(path)
        # Include Options files for mixins
        elif 'Options' in path.name:
            java_files.append(path)
    
    return java_files


def extract_mixin_options(root_dir: Path) -> dict:
    """Extract options from mixin classes like WorkspaceOptionalOptions."""
    mixins = {}

    for path in root_dir.rglob('*Options.java'):
        try:
            content = path.read_text(encoding='utf-8')
        except:
            continue

        # Get class name (simple name is fine for mixins, but use qualified for consistency)
        class_name = get_fully_qualified_class_name(path)
        if not class_name:
            continue

        options = []
        
        # Find all @Option annotations
        for match in re.finditer(r'@(?:CommandLine\.)?Option\s*\(', content):
            opt_content = extract_annotation_content(content, match.end() - 1)
            option = parse_option_annotation(opt_content)
            if option.names:
                options.append(option)
        
        if options:
            mixins[class_name] = options
    
    return mixins


def resolve_mixins(commands: dict, mixins: dict) -> dict:
    """Resolve mixin references and add their options to commands."""
    # Create a mapping from simple class names to fully qualified names
    simple_to_qualified = {}
    for qualified_name in mixins.keys():
        simple_name = qualified_name.split('.')[-1]
        simple_to_qualified[simple_name] = qualified_name

    for cmd in commands.values():
        for mixin in cmd.mixins:
            # Try to find mixin by simple name or qualified name
            mixin_key = simple_to_qualified.get(mixin.class_name, mixin.class_name)

            if mixin_key in mixins:
                # Add mixin options to command (avoiding duplicates)
                existing_names = set()
                for opt in cmd.options:
                    existing_names.update(opt.names)

                for mixin_opt in mixins[mixin_key]:
                    if not any(name in existing_names for name in mixin_opt.names):
                        cmd.options.append(mixin_opt)
                        existing_names.update(mixin_opt.names)

    return commands


def serialize_commands(commands: dict) -> dict:
    """Convert commands to a JSON-serializable format."""
    result = {}

    for class_name, cmd in commands.items():
        # Convert qualified parent name back to simple command name
        parent_name = None
        if cmd.parent and cmd.parent in commands:
            parent_name = commands[cmd.parent].name
        elif cmd.parent:
            # Fallback if parent is not found (shouldn't happen)
            parent_name = cmd.parent

        cmd_dict = {
            'name': cmd.name,
            'description': cmd.description,
            'full_command': cmd.full_command,
            'parent': parent_name,
            'subcommands': cmd.subcommands,
            'source_file': cmd.source_file,
            'options': [asdict(opt) for opt in cmd.options],
            'parameters': [asdict(param) for param in cmd.parameters],
            'mixins': [asdict(mixin) for mixin in cmd.mixins]
        }
        result[class_name] = cmd_dict

    return result


def build_hierarchy(commands: dict) -> dict:
    """Build a nested hierarchy for easier consumption."""
    # Create mapping from simple class names to all possible qualified names
    simple_to_qualified_list = {}
    for qualified_name in commands.keys():
        simple_name = qualified_name.split('.')[-1]
        if simple_name not in simple_to_qualified_list:
            simple_to_qualified_list[simple_name] = []
        simple_to_qualified_list[simple_name].append(qualified_name)

    # Find root command (tw)
    root = None
    root_qualified_name = None
    for class_name, cmd in commands.items():
        if cmd['name'] == 'tw':
            root = cmd
            root_qualified_name = class_name
            break

    if not root:
        return commands

    def resolve_subcommand(parent_qualified_name: str, subcommand_class: str, parent_cmd_name: str, parent_source_file: str) -> str:
        """Resolve a subcommand class name using imports and package context."""
        # PRIORITY 1: Try import-based resolution
        if parent_source_file:
            parent_imports = parse_imports(Path(parent_source_file))
            if subcommand_class in parent_imports:
                return parent_imports[subcommand_class]

        # PRIORITY 2: Package-based heuristics
        parent_package = '.'.join(parent_qualified_name.split('.')[:-1])

        if subcommand_class in simple_to_qualified_list:
            candidates = simple_to_qualified_list[subcommand_class]

            # Try same package first
            for candidate in candidates:
                candidate_package = '.'.join(candidate.split('.')[:-1])
                if candidate_package == parent_package:
                    return candidate

            # Try subpackage matching parent command name
            # Handle hyphenated names: "compute-envs" → "computeenvs"
            expected_subpackage = f"{parent_package}.{parent_cmd_name}"
            for candidate in candidates:
                candidate_package = '.'.join(candidate.split('.')[:-1])
                if candidate_package == expected_subpackage:
                    return candidate

            # Try with hyphens removed
            normalized_name = parent_cmd_name.replace('-', '')
            expected_subpackage_normalized = f"{parent_package}.{normalized_name}"
            for candidate in candidates:
                candidate_package = '.'.join(candidate.split('.')[:-1])
                if candidate_package == expected_subpackage_normalized:
                    return candidate

            # Try any subpackage
            for candidate in candidates:
                if candidate.startswith(parent_package + '.'):
                    return candidate

            # Fall back to first
            return candidates[0]

        return subcommand_class

    def build_subtree(cmd_dict: dict, parent_qualified_name: str, all_commands: dict) -> dict:
        subtree = cmd_dict.copy()
        subtree['children'] = []

        for subcommand_class in cmd_dict.get('subcommands', []):
            # Resolve with imports and package context
            qualified_subcommand = resolve_subcommand(
                parent_qualified_name,
                subcommand_class,
                cmd_dict['name'],
                cmd_dict.get('source_file')
            )

            if qualified_subcommand in all_commands:
                child = build_subtree(all_commands[qualified_subcommand], qualified_subcommand, all_commands)
                subtree['children'].append(child)

        return subtree

    # Build hierarchy starting from root
    if root_qualified_name:
        return build_subtree(commands[root_qualified_name], root_qualified_name, commands)

    return commands


def main():
    if len(sys.argv) < 2:
        print("Usage: python extract_cli_metadata.py /path/to/tower-cli/src/main/java", file=sys.stderr)
        print("       Output is written to stdout as JSON", file=sys.stderr)
        sys.exit(1)
    
    root_dir = Path(sys.argv[1])
    
    if not root_dir.exists():
        print(f"Error: Directory not found: {root_dir}", file=sys.stderr)
        sys.exit(1)
    
    # Find all relevant Java files
    java_files = find_java_files(root_dir)
    print(f"Found {len(java_files)} Java files to process", file=sys.stderr)

    # Extract all constants first (needed for resolving constant references in descriptions)
    global CONSTANTS_CACHE
    for file_path in java_files:
        file_constants = extract_constants_from_file(file_path)
        CONSTANTS_CACHE.update(file_constants)
    print(f"Extracted {len(CONSTANTS_CACHE)} string constants", file=sys.stderr)

    # Parse all command files
    commands = {}
    for file_path in java_files:
        cmd = parse_java_file(file_path)
        if cmd:
            # Use fully qualified class name as key to avoid collisions
            class_name = get_fully_qualified_class_name(file_path)
            commands[class_name] = cmd
    
    print(f"Extracted {len(commands)} commands", file=sys.stderr)
    
    # Extract mixin options
    mixins = extract_mixin_options(root_dir)
    print(f"Extracted {len(mixins)} mixin classes", file=sys.stderr)
    
    # Resolve mixins
    commands = resolve_mixins(commands, mixins)
    
    # Build command tree
    commands = build_command_tree(commands)
    
    # Serialize
    serialized = serialize_commands(commands)
    
    # Build hierarchy
    output = {
        'metadata': {
            'extractor_version': '1.0.0',
            'source_dir': str(root_dir),
            'total_commands': len(commands),
            'total_mixins': len(mixins)
        },
        'commands': serialized,
        'hierarchy': build_hierarchy(serialized),
        'mixins': {name: [asdict(opt) if hasattr(opt, '__dataclass_fields__') else opt for opt in opts] 
                   for name, opts in mixins.items()}
    }
    
    # Output JSON
    print(json.dumps(output, indent=2))


if __name__ == '__main__':
    main()