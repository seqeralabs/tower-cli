#!/usr/bin/env python3
"""
CLI Description Improvement Generator

Analyzes current CLI metadata descriptions and generates improved versions
following the CLI docs style guide and OpenAPI overlay language standards.

Output: cli-description-improvements.yaml - an overlay-style file showing
        before/after descriptions for all CLI commands, options, and parameters.
"""

import json
import re
import sys
from pathlib import Path
from typing import Dict, List, Tuple, Any


# Style guide rules
STYLE_RULES = {
    'command_description': {
        'format': 'imperative_verb_object',
        'examples': ['Manage datasets', 'Launch pipeline execution', 'Add credentials'],
        'avoid': ['This command', 'Used to', 'Adds', 'Lists'],
        'capitalization': 'sentence_case',
        'punctuation': 'period_for_sentences'
    },
    'option_description': {
        'format': 'present_tense_descriptive',
        'examples': ['Compute environment name', 'Wait until given status'],
        'avoid': ['Use this to', 'Sets the', 'The name of'],
        'capitalization': 'sentence_case',
        'punctuation': 'period_for_complete_sentences'
    }
}

# Terminology mappings
TERMINOLOGY_FIXES = {
    'work space': 'workspace',
    'work-space': 'workspace',
    'compute-env': 'compute environment',
    'CE': 'compute environment',
    'creds': 'credentials',
    'credential': 'credentials',
    'org ': 'organization ',
    'data links': 'data-links',
    'datalinks': 'data-links',
    'data set': 'dataset',
    'data-set': 'dataset'
}


class DescriptionImprover:
    """Improves CLI descriptions following style guide."""

    def __init__(self, metadata_path: Path, style_guide_path: Path):
        self.metadata = self._load_json(metadata_path)
        self.improvements = {
            'metadata': {
                'source': str(metadata_path),
                'style_guide': str(style_guide_path),
                'total_commands': 0,
                'improved_commands': 0,
                'improved_options': 0,
                'improved_parameters': 0
            },
            'commands': {}
        }

    def _load_json(self, path: Path) -> Dict:
        """Load JSON file."""
        with open(path) as f:
            return json.load(f)

    def _needs_improvement(self, text: str) -> bool:
        """Check if description needs improvement."""
        if not text:
            return True

        # Check for common issues
        issues = [
            text.startswith('This command'),
            text.startswith('Used to'),
            text.startswith('Use this'),
            ' CE ' in text,  # compute environment abbreviation
            ' creds' in text.lower(),
            'work space' in text.lower(),
            'data links' in text.lower(),
            'datalinks' in text.lower(),
        ]

        return any(issues)

    def _fix_terminology(self, text: str) -> str:
        """Apply terminology fixes with word boundaries."""
        result = text
        for wrong, correct in TERMINOLOGY_FIXES.items():
            # Use word boundaries for most replacements
            # Handle trailing space in pattern specially
            if wrong.endswith(' '):
                pattern = r'\b' + re.escape(wrong.rstrip()) + r'\s+'
                result = re.sub(pattern, correct.rstrip() + ' ', result, flags=re.IGNORECASE)
            else:
                pattern = r'\b' + re.escape(wrong) + r'\b'
                result = re.sub(pattern, correct, result, flags=re.IGNORECASE)
        return result

    def _improve_command_description(self, description: str, command_name: str) -> Tuple[str, List[str]]:
        """
        Improve command description following style guide.

        Returns: (improved_description, list_of_changes)
        """
        if not description:
            return description, ['Missing description']

        changes = []
        improved = description

        # Fix terminology
        fixed = self._fix_terminology(improved)
        if fixed != improved:
            changes.append('Fixed terminology')
            improved = fixed

        # Remove problematic phrases
        replacements = {
            r'^This command ': '',
            r'^Used to ': '',
            r'^Use this to ': '',
        }
        for pattern, replacement in replacements.items():
            new_text = re.sub(pattern, replacement, improved, flags=re.IGNORECASE)
            if new_text != improved:
                changes.append(f'Removed "{pattern}"')
                improved = new_text

        # Ensure sentence case
        if improved and improved[0].islower():
            improved = improved[0].upper() + improved[1:]
            changes.append('Fixed capitalization')

        # Ensure proper punctuation
        if improved and not improved.endswith('.'):
            improved = improved + '.'
            changes.append('Added period')

        return improved, changes

    def _improve_option_description(self, description: str, option_names: List[str]) -> Tuple[str, List[str]]:
        """
        Improve option description following style guide.

        Returns: (improved_description, list_of_changes)
        """
        if not description:
            return description, ['Missing description']

        changes = []
        improved = description

        # Fix terminology
        fixed = self._fix_terminology(improved)
        if fixed != improved:
            changes.append('Fixed terminology')
            improved = fixed

        # Remove problematic phrases
        replacements = {
            r'^Use this to ': '',
            r'^Sets the ': '',
            r'^The ': '',
        }
        for pattern, replacement in replacements.items():
            new_text = re.sub(pattern, replacement, improved, flags=re.IGNORECASE)
            if new_text != improved:
                changes.append(f'Removed "{pattern}"')
                improved = new_text

        # Ensure sentence case
        if improved and improved[0].islower():
            improved = improved[0].upper() + improved[1:]
            changes.append('Fixed capitalization')

        # Handle punctuation - period only for complete sentences
        # Heuristic: if it has a verb or is multi-clause, it's a sentence
        is_sentence = ' ' in improved and any(
            word in improved.lower() for word in ['is', 'are', 'will', 'can', 'must', 'wait', 'provide', 'specify']
        )

        if is_sentence and not improved.endswith('.'):
            improved = improved + '.'
            changes.append('Added period (complete sentence)')
        elif not is_sentence and improved.endswith('.'):
            improved = improved[:-1]
            changes.append('Removed period (fragment)')

        return improved, changes

    def analyze_and_improve(self):
        """Analyze all descriptions and generate improvements."""
        commands = self.metadata.get('commands', {})
        self.improvements['metadata']['total_commands'] = len(commands)

        for class_name, cmd_data in commands.items():
            cmd_improvements = {
                'name': cmd_data.get('name'),
                'full_command': cmd_data.get('full_command'),
                'source_file': cmd_data.get('source_file'),
                'changes': {}
            }

            # Improve command description
            original_desc = cmd_data.get('description', '')
            if self._needs_improvement(original_desc) or True:  # Check all for now
                improved_desc, changes = self._improve_command_description(
                    original_desc, cmd_data.get('name', '')
                )
                if changes:
                    cmd_improvements['changes']['description'] = {
                        'original': original_desc,
                        'improved': improved_desc,
                        'changes': changes
                    }
                    self.improvements['metadata']['improved_commands'] += 1

            # Improve option descriptions
            options_improvements = []
            for opt in cmd_data.get('options', []):
                opt_desc = opt.get('description', '')
                if self._needs_improvement(opt_desc) or True:  # Check all for now
                    improved_desc, changes = self._improve_option_description(
                        opt_desc, opt.get('names', [])
                    )
                    if changes:
                        options_improvements.append({
                            'names': opt['names'],
                            'original': opt_desc,
                            'improved': improved_desc,
                            'changes': changes
                        })
                        self.improvements['metadata']['improved_options'] += 1

            if options_improvements:
                cmd_improvements['changes']['options'] = options_improvements

            # Improve parameter descriptions
            params_improvements = []
            for param in cmd_data.get('parameters', []):
                param_desc = param.get('description', '')
                if self._needs_improvement(param_desc) or True:  # Check all for now
                    improved_desc, changes = self._improve_option_description(
                        param_desc, [param.get('name', '')]
                    )
                    if changes:
                        params_improvements.append({
                            'name': param.get('name'),
                            'original': param_desc,
                            'improved': improved_desc,
                            'changes': changes
                        })
                        self.improvements['metadata']['improved_parameters'] += 1

            if params_improvements:
                cmd_improvements['changes']['parameters'] = params_improvements

            # Only add to improvements if there are actual changes
            if cmd_improvements['changes']:
                self.improvements['commands'][class_name] = cmd_improvements

    def generate_report(self, output_path: Path):
        """Generate YAML-style improvement report."""
        import yaml

        with open(output_path, 'w') as f:
            yaml.dump(self.improvements, f, default_flow_style=False, sort_keys=False, width=120)

        print(f"Generated improvement report: {output_path}")
        print(f"Total commands: {self.improvements['metadata']['total_commands']}")
        print(f"Commands with improvements: {self.improvements['metadata']['improved_commands']}")
        print(f"Options improved: {self.improvements['metadata']['improved_options']}")
        print(f"Parameters improved: {self.improvements['metadata']['improved_parameters']}")


def main():
    """Main entry point."""
    if len(sys.argv) < 2:
        print("Usage: python improve-cli-descriptions.py <cli-metadata.json>")
        sys.exit(1)

    metadata_path = Path(sys.argv[1])
    if not metadata_path.exists():
        print(f"Error: {metadata_path} not found")
        sys.exit(1)

    style_guide_path = Path('docs/research/cli-docs-style-guide.md')

    improver = DescriptionImprover(metadata_path, style_guide_path)
    improver.analyze_and_improve()

    output_path = Path('docs/cli-description-improvements.yaml')
    improver.generate_report(output_path)


if __name__ == '__main__':
    main()
