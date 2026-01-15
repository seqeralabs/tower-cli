#!/usr/bin/env python3
"""
Extract CLI command examples from test files.

This script parses Java test files to extract command invocations and their
expected outputs, generating structured examples for documentation.

Usage:
    python extract-cli-examples.py <test_directory> [--output examples.json]
"""

import os
import re
import json
import argparse
from pathlib import Path
from typing import List, Dict, Any, Optional
from dataclasses import dataclass, asdict


@dataclass
class CommandExample:
    """Represents a single command example extracted from tests."""
    command: str  # Full command: "tw runs list --offset 1 --max 2"
    args: List[str]  # Parsed arguments: ["runs", "list", "--offset", "1", "--max", "2"]
    test_method: str  # Test method name: "testListWithOffset"
    test_file: str  # Test file name: "RunsCmdTest.java"
    test_class: str  # Test class name: "RunsCmdTest"
    command_family: str  # Top-level command: "runs"
    line_number: int  # Line number in test file
    description: Optional[str] = None  # Inferred from test method name
    has_format_param: bool = False  # Whether test uses OutputType format parameter
    response_type: Optional[str] = None  # Response class name if available
    category: str = "success"  # "success", "error", "validation", or "edge_case"


@dataclass
class ExamplesCollection:
    """Collection of all extracted examples."""
    examples: List[CommandExample]
    total_commands: int
    command_families: List[str]
    test_files_processed: int


class TestFileParser:
    """Parser for Java test files to extract command examples."""

    # Regex to match exec() calls
    # Matches: exec(format, mock, "runs", "list", "-i", "value")
    #      or: exec(mock, "runs", "list", "-i", "value")
    EXEC_PATTERN = re.compile(
        r'exec\s*\(\s*(?:(\w+)\s*,\s*)?mock\s*,\s*(.+?)\s*\);',
        re.MULTILINE
    )

    # Regex to match assertOutput calls to extract Response type
    ASSERT_OUTPUT_PATTERN = re.compile(
        r'assertOutput\s*\(\s*\w+\s*,\s*\w+\s*,\s*new\s+(\w+)\s*\(',
        re.MULTILINE
    )

    # Regex to extract test method names
    TEST_METHOD_PATTERN = re.compile(
        r'@Test|@ParameterizedTest.*?\n\s+(?:void|public\s+void)\s+(\w+)\s*\(',
        re.MULTILINE | re.DOTALL
    )

    def __init__(self, test_file_path: Path):
        self.test_file_path = test_file_path
        self.test_file_name = test_file_path.name
        self.test_class_name = test_file_path.stem
        self.content = test_file_path.read_text()
        self.lines = self.content.split('\n')

    def extract_command_family(self) -> str:
        """Extract command family from test class name or package."""
        # OrganizationsCmdTest -> organizations
        # RunsCmdTest -> runs
        name = self.test_class_name.replace('CmdTest', '').replace('Test', '')
        # Convert camelCase to kebab-case
        return re.sub(r'(?<!^)(?=[A-Z])', '-', name).lower()

    def parse_exec_args(self, args_str: str) -> List[str]:
        """Parse the arguments string from exec() call into list of args."""
        # Split by comma, but respect quoted strings
        args = []
        current = ""
        in_quotes = False

        for char in args_str:
            if char == '"':
                in_quotes = not in_quotes
            elif char == ',' and not in_quotes:
                arg = current.strip().strip('"')
                if arg:
                    args.append(arg)
                current = ""
            else:
                current += char

        # Add last argument
        arg = current.strip().strip('"')
        if arg:
            args.append(arg)

        return args

    def build_command_string(self, args: List[str]) -> str:
        """Build full command string from arguments."""
        return "tw " + " ".join(args)

    def infer_description(self, method_name: str, args: List[str]) -> str:
        """Infer a description from the test method name and arguments."""
        # Remove 'test' prefix and convert camelCase to sentence
        desc = method_name.replace('test', '', 1)
        # Add spaces before capitals
        desc = re.sub(r'(?<!^)(?=[A-Z])', ' ', desc)
        return desc.strip()

    def infer_category(self, method_name: str) -> str:
        """Infer the category from the test method name."""
        method_lower = method_name.lower()

        # Error cases
        error_keywords = ['error', 'fail', 'notfound', 'invalid', 'unauthorized',
                         'forbidden', 'exception', 'missing', 'bad']
        if any(keyword in method_lower for keyword in error_keywords):
            return "error"

        # Validation/edge cases
        validation_keywords = ['conflicting', 'duplicate', 'empty', 'null',
                              'withoutoptional', 'withoutworkspace']
        if any(keyword in method_lower for keyword in validation_keywords):
            return "edge_case"

        # Default to success
        return "success"

    def find_response_type(self, line_number: int, context_lines: int = 5) -> Optional[str]:
        """Find the Response type from assertOutput call near the exec() call."""
        # Look at the next few lines after exec() for assertOutput
        start = line_number
        end = min(line_number + context_lines, len(self.lines))

        for i in range(start, end):
            line = self.lines[i]
            match = self.ASSERT_OUTPUT_PATTERN.search(line)
            if match:
                return match.group(1)

        return None

    def find_test_method(self, line_number: int) -> Optional[str]:
        """Find the test method name that contains this line."""
        # Search backwards from line_number to find the method declaration
        for i in range(line_number, max(0, line_number - 50), -1):
            line = self.lines[i]
            # Look for method declaration
            method_match = re.search(r'(?:void|public\s+void)\s+(\w+)\s*\(', line)
            if method_match:
                # Verify it's a test method by checking for @Test or @ParameterizedTest above
                for j in range(max(0, i - 5), i):
                    if '@Test' in self.lines[j] or '@ParameterizedTest' in self.lines[j]:
                        return method_match.group(1)

        return None

    def extract_examples(self) -> List[CommandExample]:
        """Extract all command examples from this test file."""
        examples = []
        command_family = self.extract_command_family()

        # Find all exec() calls
        for match in self.EXEC_PATTERN.finditer(self.content):
            format_param = match.group(1)  # Will be None if exec(mock, ...) format
            args_str = match.group(2)

            # Find line number
            line_number = self.content[:match.start()].count('\n') + 1

            # Parse arguments
            args = self.parse_exec_args(args_str)

            if not args:
                continue

            # Build command string
            command = self.build_command_string(args)

            # Find test method
            test_method = self.find_test_method(line_number)
            if not test_method:
                continue

            # Find response type
            response_type = self.find_response_type(line_number)

            # Infer description and category
            description = self.infer_description(test_method, args)
            category = self.infer_category(test_method)

            example = CommandExample(
                command=command,
                args=args,
                test_method=test_method,
                test_file=self.test_file_name,
                test_class=self.test_class_name,
                command_family=command_family,
                line_number=line_number,
                description=description,
                has_format_param=format_param is not None,
                response_type=response_type,
                category=category
            )

            examples.append(example)

        return examples


class ExamplesExtractor:
    """Main extractor class that processes all test files."""

    def __init__(self, test_directory: Path):
        self.test_directory = test_directory
        self.examples: List[CommandExample] = []

    def find_test_files(self) -> List[Path]:
        """Find all *CmdTest.java files in the test directory."""
        test_files = []

        for root, dirs, files in os.walk(self.test_directory):
            for file in files:
                if file.endswith('CmdTest.java'):
                    test_files.append(Path(root) / file)

        return sorted(test_files)

    def extract_from_file(self, test_file: Path) -> List[CommandExample]:
        """Extract examples from a single test file."""
        parser = TestFileParser(test_file)
        return parser.extract_examples()

    def extract_all(self) -> ExamplesCollection:
        """Extract examples from all test files."""
        test_files = self.find_test_files()

        print(f"Found {len(test_files)} test files to process")

        all_examples = []

        for test_file in test_files:
            try:
                examples = self.extract_from_file(test_file)
                all_examples.extend(examples)
                print(f"  {test_file.name}: {len(examples)} examples")
            except Exception as e:
                print(f"  Error processing {test_file.name}: {e}")

        # Get unique command families
        command_families = sorted(set(ex.command_family for ex in all_examples))

        collection = ExamplesCollection(
            examples=all_examples,
            total_commands=len(all_examples),
            command_families=command_families,
            test_files_processed=len(test_files)
        )

        return collection

    def to_json(self, collection: ExamplesCollection) -> str:
        """Convert examples collection to JSON."""
        data = {
            "metadata": {
                "total_examples": collection.total_commands,
                "command_families": collection.command_families,
                "test_files_processed": collection.test_files_processed
            },
            "examples": [asdict(ex) for ex in collection.examples]
        }

        return json.dumps(data, indent=2)

    def group_by_family(self, collection: ExamplesCollection) -> Dict[str, List[CommandExample]]:
        """Group examples by command family."""
        grouped = {}

        for example in collection.examples:
            family = example.command_family
            if family not in grouped:
                grouped[family] = []
            grouped[family].append(example)

        return grouped

    def print_summary(self, collection: ExamplesCollection):
        """Print summary statistics."""
        print(f"\n{'='*60}")
        print(f"CLI Examples Extraction Summary")
        print(f"{'='*60}")
        print(f"Total examples extracted: {collection.total_commands}")
        print(f"Test files processed: {collection.test_files_processed}")
        print(f"Command families: {len(collection.command_families)}")
        print(f"\nExamples per family:")

        grouped = self.group_by_family(collection)
        for family in sorted(grouped.keys()):
            examples = grouped[family]
            print(f"  {family:20s}: {len(examples):3d} examples")

        print(f"{'='*60}\n")


def main():
    parser = argparse.ArgumentParser(
        description='Extract CLI command examples from test files'
    )
    parser.add_argument(
        'test_directory',
        type=Path,
        help='Directory containing test files (e.g., src/test/java)'
    )
    parser.add_argument(
        '-o', '--output',
        type=Path,
        help='Output JSON file (default: stdout)'
    )
    parser.add_argument(
        '--summary',
        action='store_true',
        help='Print summary statistics'
    )

    args = parser.parse_args()

    if not args.test_directory.exists():
        print(f"Error: Test directory not found: {args.test_directory}")
        return 1

    # Extract examples
    extractor = ExamplesExtractor(args.test_directory)
    collection = extractor.extract_all()

    # Print summary if requested
    if args.summary or not args.output:
        extractor.print_summary(collection)

    # Generate JSON output
    json_output = extractor.to_json(collection)

    if args.output:
        args.output.write_text(json_output)
        print(f"Examples written to {args.output}")
    else:
        print(json_output)

    return 0


if __name__ == '__main__':
    exit(main())
