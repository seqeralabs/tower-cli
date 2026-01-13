# OpenAPI to CLI Mapping Strategy

**Date**: 2026-01-13
**Status**: Design Complete, Ready for Implementation

---

## Overview

This document describes the strategy for mapping OpenAPI schema descriptions to CLI option descriptions, enabling:
1. **Consistency** - Single source of truth for parameter descriptions
2. **Quality** - Rich, well-documented API descriptions enhance CLI help text
3. **Automation** - Automatic updates when API descriptions improve

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Source Inputs                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. seqera-api-latest-decorated.yaml                            │
│     - OpenAPI spec with enhanced descriptions                    │
│     - Fields: preRunScript, computeEnvId, workDir, etc.         │
│                                                                   │
│  2. cli-to-api-mapping.json                                     │
│     - Maps CLI options to API fields                             │
│     - Transformation rules                                       │
│                                                                   │
│  3. cli-metadata.json                                            │
│     - Extracted from Java source (@Option annotations)           │
│     - Current CLI descriptions                                   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Enhancement Script                             │
│              (enrich-cli-metadata.py)                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Process:                                                         │
│  1. Load OpenAPI spec                                            │
│  2. Load mapping file                                            │
│  3. Load CLI metadata                                            │
│  4. For each CLI option:                                         │
│     - Look up API field via mapping                              │
│     - Extract API description                                    │
│     - Adapt description for CLI context                          │
│     - Add transformation notes if needed                         │
│  5. Output enriched metadata                                     │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     Enriched Metadata                             │
│              (cli-metadata-enriched.json)                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Enhanced with:                                                   │
│  - API descriptions (authoritative source)                       │
│  - Links to documentation                                        │
│  - Transformation notes                                          │
│  - CLI-specific adaptations                                      │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────┴─────────┐
                    ↓                   ↓
┌──────────────────────────────┐  ┌──────────────────────────────┐
│   Update Java Source Files   │  │   Generate CLI Docs          │
│   (apply-descriptions.py)    │  │   (generate-cli-docs.py)     │
├──────────────────────────────┤  ├──────────────────────────────┤
│                              │  │                              │
│  For each command:           │  │  For each command:           │
│  1. Read Java file           │  │  1. Create markdown page     │
│  2. Find @Option annotation  │  │  2. Include description      │
│  3. Update description       │  │  3. Add usage examples       │
│  4. Preserve formatting      │  │  4. Link to API docs         │
│  5. Write back               │  │                              │
│                              │  │  Output:                     │
│  Updates:                    │  │  - tw-launch.md              │
│  - LaunchCmd.java            │  │  - tw-compute-envs-add.md    │
│  - ComputeEnvsCmd.java       │  │  - etc.                      │
│  - PipelinesCmd.java         │  │                              │
│  - etc.                      │  │                              │
│                              │  │                              │
└──────────────────────────────┘  └──────────────────────────────┘
                    │                            │
                    └────────────┬───────────────┘
                                 ↓
                    ┌────────────────────────────┐
                    │      User Impact           │
                    ├────────────────────────────┤
                    │                            │
                    │  1. tw launch --help       │
                    │     → Shows enhanced desc  │
                    │                            │
                    │  2. docs.seqera.io         │
                    │     → Updated CLI pages    │
                    │                            │
                    └────────────────────────────┘
```

---

## Components

### 1. Enhancement Script (`enrich-cli-metadata.py`)

**Purpose**: Merge API descriptions into CLI metadata

**Input**:
- `seqera-api-latest-decorated.yaml` - OpenAPI spec
- `cli-to-api-mapping.json` - Mapping rules
- `cli-metadata.json` - Current CLI metadata

**Output**:
- `cli-metadata-enriched.json` - Enhanced metadata with API descriptions

**Logic**:
```python
def enrich_option(cli_option, mapping, openapi_spec):
    # Get API field from mapping
    api_field = mapping[cli_option]["api_field"]
    api_schema = mapping[cli_option]["api_schema"]

    # Extract API description
    api_description = openapi_spec["components"]["schemas"][api_schema]["properties"][api_field]["description"]

    # Adapt for CLI context
    cli_description = adapt_for_cli(api_description, mapping[cli_option])

    return {
        "description": cli_description,
        "api_source": {
            "schema": api_schema,
            "field": api_field,
            "original_description": api_description
        }
    }

def adapt_for_cli(api_description, mapping_entry):
    """
    Adapt API description for CLI context:
    - Remove API-specific jargon
    - Adjust for file paths vs content (e.g., --pre-run takes a path, not content)
    - Add CLI-specific notes about transformations
    """
    adapted = api_description

    if mapping_entry["transformation"] == "file_to_text":
        adapted += f" (provide file path)"
    elif mapping_entry["transformation"] == "name_to_id":
        adapted += f" (provide name or identifier)"

    return adapted
```

### 2. Java Source Updater (`apply-descriptions.py`)

**Purpose**: Update @Option descriptions in Java source files

**Input**:
- `cli-metadata-enriched.json` - Enhanced metadata
- Java source files in `src/main/java/`

**Output**:
- Updated Java files with new descriptions

**Logic**:
```python
def update_java_file(file_path, enriched_metadata):
    content = read_file(file_path)

    for option in enriched_metadata[file_path]["options"]:
        # Find @Option annotation
        pattern = rf'@Option\(names\s*=\s*\{[^}]*"{option["cli_option"]}"[^}]*\}\s*,\s*description\s*=\s*"[^"]*"'

        # Replace description
        new_description = option["description"]
        content = re.sub(pattern, f'@Option(..., description = "{new_description}"', content)

    write_file(file_path, content)
```

### 3. Documentation Generator (`generate-cli-docs.py`)

**Purpose**: Generate markdown documentation for CLI commands

**Input**:
- `cli-metadata-enriched.json` - Enhanced metadata
- Optional: examples overlay (similar to API docs pattern)

**Output**:
- Markdown files for docs.seqera.io

**Template**:
```markdown
# tw launch

Launch a pipeline workflow.

## Usage

```bash
tw launch [pipeline] [options]
```

## Options

### --pre-run

**Type**: File path

**Description**: Add a script that executes in the nf-launch script prior to invoking Nextflow processes.

Provide a path to a bash script file. The script content will be executed in the same environment where Nextflow runs, just before the pipeline is launched.

See [Pre and post-run scripts](https://docs.seqera.io/platform-cloud/launch/advanced#pre-and-post-run-scripts) for more information.

### --compute-env, -c

**Type**: String

**Description**: Compute environment identifier where the pipeline will run.

Provide the name or identifier of an existing compute environment. If omitted, defaults to the workspace primary compute environment.

## Examples

[Include curated examples from examples overlay]
```

---

## Mapping File Format

### Structure

```json
{
  "metadata": { /* version, dates, etc. */ },
  "mappings": {
    "CommandClass": {
      "api_schema": "APISchemaName",
      "options": {
        "option-name": {
          "cli_option": "--option-name",
          "cli_field": "fieldName",
          "api_field": "apiFieldName",
          "transformation": "type",
          "description_source": "api" | "cli" | "manual"
        }
      }
    }
  },
  "transformation_types": { /* documentation */ },
  "patterns": { /* reusable patterns */ }
}
```

### Transformation Types

1. **direct**: CLI and API fields match directly
2. **file_to_text**: CLI accepts file path, API expects content
3. **name_to_id**: CLI accepts name, API expects ID
4. **objects_to_ids**: CLI accepts objects, API expects IDs

---

## Description Adaptation Rules

### Rule 1: File Path Context

**API Description**: "Add a script that executes in the nf-launch script prior to invoking Nextflow processes."

**CLI Adaptation**: "Add a script that executes in the nf-launch script prior to invoking Nextflow processes. Provide the path to a bash script file."

**Why**: CLI users provide file paths, not content. Make this explicit.

### Rule 2: ID Resolution Context

**API Description**: "Compute environment identifier where the pipeline will run."

**CLI Adaptation**: "Compute environment name or identifier. If omitted, defaults to the workspace primary compute environment."

**Why**: CLI accepts names (more user-friendly), which are resolved to IDs internally.

### Rule 3: Preserve Documentation Links

**API Description**: "Add a script that executes in the nf-launch script prior to invoking Nextflow processes. See [Pre and post-run scripts](https://docs.seqera.io/platform-cloud/launch/advanced#pre-and-post-run-scripts)."

**CLI Adaptation**: Same (preserve links)

**Why**: Links provide valuable additional context.

### Rule 4: Default Values

**API Description**: "Defaults to workspace primary compute environment if omitted."

**CLI Adaptation**: Keep if relevant to CLI usage

**Why**: Helps users understand optional parameters.

---

## Automation Workflow

### Phase 1: Initial Setup (Manual)

1. ✅ Create `cli-to-api-mapping.json` (DONE)
2. ✅ Port `seqera-api-latest-decorated.yaml` to this repo (DONE)
3. Implement `enrich-cli-metadata.py`
4. Test enrichment on LaunchCmd
5. Implement `apply-descriptions.py`
6. Test updates on LaunchCmd.java
7. Review and validate

### Phase 2: Expansion

1. Extend mapping to cover all commands:
   - ComputeEnvsCmd (add/update subcommands)
   - PipelinesCmd (add/update subcommands)
   - CredentialsCmd (all providers)
   - SecretsCmd
   - WorkspacesCmd
   - etc.
2. Document platform-specific mappings (AWS, Azure, K8s, etc.)
3. Handle edge cases and exceptions

### Phase 3: CI/CD Integration

**Trigger**: New CLI release

**Workflow**:
```yaml
name: Update CLI Metadata

on:
  release:
    types: [published]
  workflow_dispatch:

jobs:
  update-metadata:
    runs-on: ubuntu-latest
    steps:
      # 1. Extract CLI metadata from new release
      - name: Extract CLI metadata
        run: python scripts/extract-cli-metadata.py src/main/java > cli-metadata.json

      # 2. Fetch latest decorated OpenAPI spec from docs repo
      - name: Fetch OpenAPI spec
        run: curl -o seqera-api-latest-decorated.yaml https://raw.githubusercontent.com/seqera/docs/main/api/seqera-api-latest-decorated.yaml

      # 3. Enrich CLI metadata with API descriptions
      - name: Enrich metadata
        run: python scripts/enrich-cli-metadata.py

      # 4. Generate CLI documentation
      - name: Generate docs
        run: python scripts/generate-cli-docs.py

      # 5. Create PR to docs repo with updates
      - name: Create PR
        run: |
          # Create PR with:
          # - Updated CLI reference pages
          # - Changelog of new/changed options
          # - List of options missing API descriptions (need manual review)
```

---

## Benefits

### 1. Consistency

Single source of truth (OpenAPI spec) ensures CLI and API docs stay aligned.

### 2. Quality Improvements

API descriptions are generally more comprehensive (e.g., "Add a script that executes in the nf-launch script prior to invoking Nextflow processes" vs "Pre-run script").

**Before** (CLI):
```
--pre-run: Bash script that is executed in the same environment where Nextflow runs just before the pipeline is launched.
```

**After** (with API description):
```
--pre-run: Add a script that executes in the nf-launch script prior to invoking Nextflow processes.
           Provide the path to a bash script file. The script content will be executed in the same
           environment where Nextflow runs, just before the pipeline is launched.
           See https://docs.seqera.io/platform-cloud/launch/advanced#pre-and-post-run-scripts
```

### 3. Maintainability

When API descriptions are updated (e.g., new features, clarifications), CLI docs automatically inherit those improvements.

### 4. Developer Experience

Clear, comprehensive help text makes the CLI more accessible to new users.

### 5. Documentation Accuracy

Generated docs always reflect the actual CLI implementation (extracted from source) plus high-quality descriptions (from API spec).

---

## Edge Cases & Considerations

### Case 1: CLI-Only Options

Some CLI options have no API equivalent (e.g., `--wait`, `--output`, `--verbose`).

**Solution**: Mark `description_source: "cli"` in mapping. Use existing CLI description.

### Case 2: Divergent Descriptions

Sometimes CLI and API descriptions serve different purposes.

**Solution**: Mark `description_source: "manual"` and provide custom description in mapping file.

### Case 3: Missing API Descriptions

Some API fields may not have descriptions yet.

**Solution**: Enrichment script logs warnings. Use existing CLI description as fallback.

### Case 4: Platform-Specific Options

Compute environment options vary by platform (AWS, Azure, K8s, etc.).

**Solution**: Each platform has its own mapping section. OpenAPI spec has platform-specific schemas (AwsBatchConfig, AzureBatchConfig, etc.).

---

## Next Steps

1. **Implement Enhancement Script**
   - Parse OpenAPI YAML
   - Apply mapping rules
   - Handle transformations
   - Output enriched metadata

2. **Test on LaunchCmd**
   - Run enhancement
   - Review enriched descriptions
   - Compare with original
   - Validate quality improvements

3. **Implement Java Updater**
   - Parse Java source
   - Update @Option annotations
   - Preserve formatting
   - Test on LaunchCmd.java

4. **Extend Mapping Coverage**
   - Add remaining commands
   - Document platform-specific mappings
   - Handle edge cases

5. **Implement Doc Generator**
   - Create markdown templates
   - Generate per-command pages
   - Include examples overlay pattern

6. **Set Up Automation**
   - Create GitHub Action workflow
   - Test on release candidate
   - Deploy to production

---

## Success Metrics

- **Coverage**: 90%+ of CLI options mapped to API fields
- **Quality**: Descriptions include context, links, and examples
- **Consistency**: CLI and API docs use identical terminology
- **Automation**: Updates flow from API spec → CLI source → docs with <30min manual review
- **User Feedback**: Reduced support questions about CLI usage

---

## References

- **API Docs Automation**: Existing successful pattern using Speakeasy overlays
- **CLI Metadata Extraction**: `extract-cli-metadata.py` (Phase 1 complete)
- **OpenAPI Spec**: `seqera-api-latest-decorated.yaml` (from docs repo)
- **Mapping File**: `cli-to-api-mapping.json` (Phase 1 complete)
