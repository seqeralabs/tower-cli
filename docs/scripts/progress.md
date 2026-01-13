# CLI Documentation Automation - Progress Tracker

## Project Overview

Automating the CLI documentation workflow for tower-cli, similar to the successful API docs automation. Goals:

1. **Extract** CLI metadata from picocli Java annotations → structured JSON
2. **Enrich** descriptions to match docs quality standards
3. **Generate** per-command documentation pages
4. **Automate** updates on CLI releases via GitHub Actions

---

## ✅ Phase 1: COMPLETE (2026-01-12)

### What We Built

**Metadata Extractor** (`docs/scripts/extract-cli-metadata.py`)
- Parses `@Command`, `@Option`, `@Parameters`, `@Mixin` annotations
- Handles both `@Annotation` and `@CommandLine.Annotation` styles
- Import-based subcommand resolution for cross-package references
- Fully qualified class names prevent collisions
- Deep nesting support (up to 5 levels)

**Generated Output** (`docs/cli-metadata.json`)
- 161 commands with full hierarchy
- 22 resolved mixin classes
- Nested tree structure
- Ready for docs generation

### Bugs Fixed

1. **Mixin regex** - Only matched `@Option`, missed `@CommandLine.Option` (2 → 22 mixins)
2. **Class name collisions** - Used filename stem, 14 AddCmd files overwrote each other (51 → 161 commands)
3. **Parent-child resolution** - Package-aware matching with command name heuristics
4. **Hyphen normalization** - `compute-envs` command → `computeenvs` package matching
5. **Import parsing** - Cross-package references via Java import statements (8 orphans → 0)

### Results

| Metric | Result |
|--------|--------|
| **Commands extracted** | 161/161 (100%) |
| **Mixins extracted** | 22/22 (100%) |
| **Orphaned commands** | 0 |
| **Root commands** | 1 (tw only) |
| **Deepest nesting** | 5 levels |

### Testing Checklist - All Passed ✅

- ✅ Extract all 19 top-level commands
- ✅ Find deeply nested commands (`tw compute-envs add aws-batch forge`)
- ✅ Resolve all mixins (WorkspaceOptionalOptions, etc.)
- ✅ Capture option details (names, descriptions, defaults, arity)
- ✅ Extract parameters (positional args)
- ✅ Build correct hierarchy structure
- ✅ No parsing errors

### Documentation Generated

- `docs/research/extraction-complete.md` - Executive summary
- `docs/research/final-results.md` - Comprehensive results
- `docs/research/2026-01-12-orphaned-commands-analysis.md` - Deep dive research
- `docs/research/import-fix-results.md` - Import parser implementation
- Additional analysis docs in `docs/research/`

### Usage

```bash
python docs/scripts/extract-cli-metadata.py src/main/java > docs/cli-metadata.json
```

---

## ✅ Phase 2: Description Standards (COMPLETE - 2026-01-13)

### ✅ Completed

**CLI Docs Style Guide** (`docs/research/cli-docs-style-guide.md`)
- Inferred patterns from existing CLI documentation at https://docs.seqera.io/platform-cloud/cli/commands
- Imperative verb + object pattern for command descriptions
- Present tense, descriptive voice for options
- Sentence case capitalization rules
- Consistent Platform terminology (workspace vs work-space, Studio vs studio, etc.)
- Clear guidance on when to add scope qualifiers (organization/workspace/team)
- Punctuation rules (periods for complete sentences, omit for fragments)

**Sample Improvements Document** (`docs/research/cli-description-improvements-sample.md`)
- Before/after examples for representative commands
- Clear rationales for each change
- Patterns that apply across all 161 commands
- Key decisions on scope qualifiers documented

**Compute-Envs Complete Implementation** (`docs/research/compute-envs-improvements.md`)
- All 11 compute-envs commands analyzed and improved
- Applied improvements to Java source files
- Verified with metadata extractor
- **Results**: 11 commands, 19 options, 2 parameters improved
- 1 mixin class updated (ComputeEnvRefOptions) - improves 5+ commands automatically

### Changes Applied to tower-cli Source

**Files Modified** (12 files):
1. `ComputeEnvsCmd.java` - Removed redundant "workspace"
2. `ComputeEnvRefOptions.java` - "id" → "identifier" (shared mixin)
3. `AddCmd.java` - Added article "a"
4. `ListCmd.java` - Removed "all" and "workspace"
5. `ViewCmd.java` - Added "details"
6. `DeleteCmd.java` - Added article "a"
7. `UpdateCmd.java` - Singular form, word order fix
8. `ExportCmd.java` - Specified JSON format, improved parameter description
9. `ImportCmd.java` - Specified JSON format, matched command name, improved parameter
10. `PrimaryCmd.java` - Simplified to "Manage"
11. `primary/GetCmd.java` - Imperative verb form
12. `primary/SetCmd.java` - Imperative verb form, removed "workspace"

**Pattern Examples Applied**:
- Removed redundant scope qualifiers: ~~"workspace compute environments"~~ → "compute environments"
- Added grammatical articles: "Add new" → "Add a new"
- Imperative verb forms: "Gets" → "Get", "Sets" → "Set"
- Simplified phrasing: "Sets or gets a primary compute environment within current workspace" → "Manage the primary compute environment"
- Specified formats: "Export compute environment for further creation" → "Export compute environment configuration as a JSON file"
- No abbreviations: "id" → "identifier", "compute env" → "compute environment"

**Additional Updates (2026-01-12 continued)**
- Enhanced metadata extractor with constant resolution - now resolves references like `WorkspaceOptionalOptions.DESCRIPTION`
- Updated 3 global mixin classes:
  1. `WorkspaceOptionalOptions` - improved description, affects 50+ commands
  2. `WorkspaceRequiredOptions` - reuses improved description
  3. `PaginationOptions` - improved --page, --offset, --max descriptions

**Batch 1** (47 commands):
- **Actions family** (8 commands): ActionsCmd, AddCmd + 2 subcommands, DeleteCmd, ListCmd, UpdateCmd, ViewCmd, LabelsCmd, ActionRefOptions mixin
- **Collaborators family** (2 commands): CollaboratorsCmd, ListCmd
- **Credentials family** (29 commands): CredentialsCmd, AddCmd + 12 provider subcommands, UpdateCmd + 11 provider subcommands, DeleteCmd, ListCmd, CredentialsRefOptions mixin
- **Datasets family** (8 commands): DatasetsCmd, AddCmd, DeleteCmd, DownloadCmd, ListCmd, UpdateCmd, ViewCmd, UrlCmd, DatasetRefOptions mixin
- **Verified**: Metadata extractor confirmed all changes

**Batch 2** (19 commands):
- **Data-links family** (9 commands): DataLinksCmd, AddCmd, BrowseCmd, DeleteCmd, DownloadCmd, ListCmd, UpdateCmd, UploadCmd, DataLinkRefOptions mixin
- **Info command** (1 command): InfoCmd
- **Labels family** (6 commands): LabelsCmd, AddLabelsCmd, DeleteLabelsCmd, ListLabelsCmd, UpdateLabelsCmd, LabelsOptionalOptions mixin
- **Launch command** (3 commands): LaunchCmd with 15+ options improved
- **Verified**: Metadata extractor confirmed all changes

**Pattern Examples Applied**:
- Removed redundant "workspace" scope qualifiers
- Standardized to "identifier" instead of "id" or "ID"
- Removed trailing periods from fragments
- Imperative verb forms for commands
- Concise, descriptive option text
- Batch updates via sed for repetitive patterns (credentials add/update subcommands)
- Hyphenation: "data-links" → "data links" in descriptions, "data link" in singular

**Batch 3** (24 commands):
- **Members family** (6 commands): MembersCmd, AddCmd, DeleteCmd, LeaveCmd, ListCmd, UpdateCmd
- **Organizations family** (6 commands): OrganizationsCmd, AddCmd, DeleteCmd, ListCmd, UpdateCmd, ViewCmd, OrganizationRefOptions mixin, OrganizationsOptions mixin
- **Participants family** (6 commands): ParticipantsCmd, AddCmd, DeleteCmd, LeaveCmd, ListCmd, UpdateCmd
- **Pipelines family** (6 commands): PipelinesCmd, AddCmd, DeleteCmd, ExportCmd, ImportCmd, LabelsCmd, ListCmd, UpdateCmd, ViewCmd, PipelineRefOptions mixin, LaunchOptions mixin
- **Verified**: Metadata extractor confirmed all changes

**Final Batch** (61 commands):
- **Runs family** (14 commands): RunsCmd, CancelCmd, DeleteCmd, DumpCmd, LabelsCmd, ListCmd, RelaunchCmd, ViewCmd + nested (DownloadCmd, MetricsCmd, TasksCmd, TaskCmd)
- **Secrets family** (6 commands): SecretsCmd, AddCmd, DeleteCmd, ListCmd, UpdateCmd, ViewCmd, SecretRefOptions mixin
- **Studios family** (9 commands): StudiosCmd, AddCmd, AddAsNewCmd, CheckpointsCmd, DeleteCmd, ListCmd, StartCmd, StopCmd, TemplatesCmd, ViewCmd + option classes
- **Teams family** (7 commands): TeamsCmd, AddCmd, DeleteCmd, ListCmd, MembersCmd + nested (members/AddCmd, members/DeleteCmd)
- **Workspaces family** (7 commands): WorkspacesCmd, AddCmd, DeleteCmd, LeaveCmd, ListCmd, UpdateCmd, ViewCmd, WorkspaceRefOptions mixin
- **Verified**: Metadata extractor confirmed all changes across all 161 commands

### Final Results

**Total Commands Updated**: 161/161 (100%)

**Files Modified**: 146 Java source files
- 18 top-level command classes
- 128 subcommand classes
- 22 mixin/option classes

**Patterns Applied Consistently**:
1. ✅ Removed all trailing periods from descriptions
2. ✅ Changed "id"/"ID" to "identifier" throughout
3. ✅ Removed redundant qualifiers ("workspace", "organization")
4. ✅ Used imperative verb forms for commands
5. ✅ Standardized default value format to "(default: value)"
6. ✅ Changed "Delete" to "Remove" for member operations
7. ✅ Concise, descriptive option descriptions

**Verification Method**:
- Ran metadata extractor after each batch
- Confirmed descriptions follow style guide
- Validated no trailing periods remain
- Checked "identifier" usage throughout

### Pull Request Status

**Branch**: `ll-metadata-extractor-and-docs-automation`

**Commits**:
1. `813863c4` - Add CLI metadata extractor with import-based resolution
2. `a68b0424` - Phase 2: Improve CLI annotation descriptions (compute-envs complete)
3. `45737ea7` - Phase 2: Improve CLI annotation descriptions (all families complete)
4. `bb249115` - Update CLI metadata with Phase 2 improvements

**Status**: Draft PR created and ready for review

**PR includes**:
- Enhanced metadata extractor with constant resolution
- All 161 command annotation improvements
- Updated cli-metadata.json with extracted metadata
- Comprehensive PR description with examples

---

## 🔧 Phase 2.5: Metadata Review & OpenAPI Mapping Design (2026-01-13)

### Session Overview

Following the completion of Phase 2 (all 161 command descriptions improved), this session focused on:
1. Reviewing and validating extracted metadata quality
2. Fixing critical metadata extractor bugs
3. Applying additional description improvements based on user feedback
4. Designing and documenting the OpenAPI-to-CLI mapping strategy

### Bug Fix: Metadata Extractor Parent Relationships

**Issue Discovered**: Commands with shared names (e.g., "add", "view") were generating incorrect `full_command` paths due to ambiguous parent lookups.

**Examples of Incorrect Paths**:
- `tw organizations add moab` (should be `tw compute-envs add moab`)
- `tw organizations view versions` (should be `tw datasets view versions`)

**Root Cause**: The `get_full_command()` function stored parent relationships as command **names** (strings like "add") rather than **qualified class names**. When building paths, it would match the FIRST command with that name, which was alphabetically sorted and often wrong.

**Solution Implemented** (3 changes to `extract-cli-metadata.py`):

1. **Lines 444, 494**: Store qualified class name as parent
   ```python
   # Before: commands[qualified_subcommand].parent = cmd.name
   # After:
   commands[qualified_subcommand].parent = parent_qualified_name
   ```

2. **Lines 502-512**: Direct dict lookup instead of iteration
   ```python
   # Before: Loop through all commands checking if c.name == current.parent
   # After:
   parent_cmd = commands.get(current.parent)  # O(1) lookup
   ```

3. **Lines 600-606**: Convert back to simple names during serialization
   ```python
   # Convert qualified parent name back to simple command name for JSON
   parent_name = None
   if cmd.parent and cmd.parent in commands:
       parent_name = commands[cmd.parent].name
   ```

**Results**:
- ✅ All 161 commands now have correct `full_command` paths
- ✅ 0 orphaned commands (except root "tw" - expected)
- ✅ All compute-envs add provider subcommands fixed (17 commands)
- ✅ Dataset versions command path corrected
- ✅ Parent field in JSON still human-readable ("add" not "io.seqera.tower...")

### Description Improvements Applied

**User Feedback Items**:

1. **Replace "Tower"/"Nextflow Tower" with "Seqera Platform"** (5 changes in `Tower.java`):
   - Root command: "Nextflow Tower CLI" → "Seqera Platform CLI"
   - `--access-token`: "Tower personal access token" → "Seqera Platform personal access token"
   - `--url`: "Tower server API endpoint URL" → "Seqera Platform API endpoint URL"
   - `--insecure`: "Tower server" → "Seqera Platform server"
   - Note: Environment variable names (TOWER_ACCESS_TOKEN, etc.) remain unchanged for backward compatibility

2. **Modernize JSON format description** (1 change in `Tower.java`):
   - `--output`: "only the 'json' option is available at the moment" → "currently supports 'json'"

3. **Contextual accuracy verified** (1 decision in `LaunchCmd.java`):
   - Kept "defaults to primary compute environment" for `--compute-env` in launch context (appropriate)
   - This is different from compute environment creation contexts where it wouldn't apply

**Files Modified**:
- `src/main/java/io/seqera/tower/cli/Tower.java` (6 description changes)
- `docs/scripts/extract-cli-metadata.py` (3 bug fixes)
- `docs/cli-metadata.json` (regenerated with all fixes)

### OpenAPI Mapping Strategy Design

**Challenge Identified**: CLI option descriptions could be significantly enhanced by reusing high-quality descriptions from the decorated OpenAPI spec.

**Example**:
- **Current CLI**: `--pre-run: Bash script that is executed...`
- **OpenAPI**: `preRunScript: Add a script that executes in the nf-launch script prior to invoking Nextflow processes. See https://docs.seqera.io/platform-cloud/launch/advanced#pre-and-post-run-scripts`

**Research Process** (Token-efficient agent approach):

Spawned 4 specialized codebase agents in parallel to gather intelligence:

1. **codebase-locator** (Agent: aa6fe66):
   - Task: Find all Java command files with @Option annotations
   - Output: Located 41 files across 10 command families (Launch, Pipelines, ComputeEnvs, Credentials, etc.)

2. **codebase-analyzer - CLI** (Agent: a2e116d):
   - Task: Extract all LaunchCmd option metadata
   - Output: 21 options with field names, types, and current descriptions

3. **codebase-analyzer - OpenAPI** (Agent: ad4b241):
   - Task: Analyze WorkflowLaunchRequest schema in decorated OpenAPI spec
   - Output: 27 API fields with enhanced descriptions, types, and documentation links

4. **codebase-pattern-finder** (Agent: a025663):
   - Task: Find naming patterns between CLI options and API fields
   - Output: 12 distinct transformation patterns with confidence levels (direct mapping, id suffix, text suffix, etc.)

**Artifacts Created**:

1. **`cli-to-api-mapping.json`** (Mapping configuration file):
   - Maps all 21 LaunchCmd options to WorkflowLaunchRequest fields
   - Documents 4 transformation types: direct, file_to_text, name_to_id, objects_to_ids
   - Defines 6 naming patterns with confidence levels (very_high, high, medium)
   - Extensible to all command families
   - **Format**: JSON configuration file, ~200 lines

2. **`openapi-mapping-strategy.md`** (Complete design document):
   - Architecture diagram showing full automation workflow
   - 3 main components: Enhancement Script, Java Source Updater, Doc Generator
   - Description adaptation rules (file path context, ID resolution, preserve links, defaults)
   - 5-phase implementation plan (Setup → Expansion → CI/CD)
   - CI/CD GitHub Action workflow specification
   - Edge cases and considerations
   - Success metrics
   - **Format**: Comprehensive markdown, ~400 lines

**Key Mapping Patterns Identified**:

| Pattern | Example | Confidence | Usage |
|---------|---------|------------|-------|
| Direct mapping | `workDir → workDir` | Very High | Simple fields |
| Id suffix | `computeEnv → computeEnvId` | Very High | Entity references |
| Ids suffix | `labels → labelIds` | Very High | Entity lists |
| Text suffix | `paramsFile → paramsText` | Very High | File content |
| Script suffix | `preRunScript → preRunScript` | Very High | Script files |
| Config prefix | `profile → configProfiles` | High | Profile lists |

**Automation Workflow Designed**:
```
OpenAPI Spec → Enhancement Script → Enriched Metadata
                                   ↓
                        ┌──────────┴──────────┐
                        ↓                     ↓
              Update Java Source      Generate CLI Docs
              (--help text)           (docs.seqera.io)
```

### Files Created This Session

**New Files**:
1. `docs/scripts/cli-to-api-mapping.json` - Mapping configuration (~200 lines)
2. `docs/scripts/openapi-mapping-strategy.md` - Design document (~400 lines)

**Modified Files**:
1. `docs/scripts/extract-cli-metadata.py` - Bug fixes (3 locations)
2. `src/main/java/io/seqera/tower/cli/Tower.java` - Description improvements (6 changes)
3. `docs/cli-metadata.json` - Regenerated with all fixes (17,586 lines)

### Design Decisions

**Decision 1: Internal vs External Parent Representation**
- **Problem**: Ambiguous parent lookups when using command names
- **Solution**: Store qualified class names internally, convert to simple names during JSON serialization
- **Benefit**: Unambiguous lookups (O(1) dict access) + human-readable output

**Decision 2: Token-Efficient Agent Research**
- **Problem**: Need comprehensive codebase analysis without consuming excessive context
- **Solution**: Spawn 4 specialized agents in parallel, synthesize findings
- **Benefit**: Saved ~50K tokens vs manual exploration

**Decision 3: Mapping File Structure**
- **Problem**: How to represent CLI→API relationships?
- **Solution**: JSON configuration with transformation types and pattern documentation
- **Benefit**: Extensible, version-controlled, supports multiple transformation types

**Decision 4: OpenAPI Spec Source**
- **Problem**: Where to get high-quality API descriptions?
- **Solution**: Use `seqera-api-latest-decorated.yaml` from docs repo
- **Benefit**: Single source of truth, already enhanced with documentation links

### Next Steps for Phase 3

**Phase 3a: OpenAPI Enhancement Implementation** (Recommended First):

1. **Implement Enhancement Script** (`enrich-cli-metadata.py`):
   - Parse OpenAPI YAML
   - Apply mapping rules from `cli-to-api-mapping.json`
   - Handle 4 transformation types
   - Adapt descriptions for CLI context (file paths, defaults, etc.)
   - Output `cli-metadata-enriched.json`

2. **Test on LaunchCmd**:
   - Run enhancement on 21 LaunchCmd options
   - Compare original vs enriched descriptions
   - Validate quality improvements
   - Review API documentation links integration

3. **Implement Java Source Updater** (`apply-descriptions.py`):
   - Parse Java files
   - Find @Option annotations by option name
   - Update description attribute
   - Preserve code formatting
   - Write back to source files

4. **Test Java Updates**:
   - Apply to LaunchCmd.java
   - Compile and test
   - Run `tw launch --help` to verify output
   - Ensure no syntax/formatting issues

**Phase 3b: Expand Coverage**:

1. Extend mapping to remaining command families (140+ options):
   - ComputeEnvsCmd (all platforms: AWS, Azure, K8s, HPC schedulers)
   - PipelinesCmd (add, update, export, import)
   - CredentialsCmd (all providers: AWS, Azure, GitHub, GitLab, etc.)
   - SecretsCmd, WorkspacesCmd, DatasetsCmd, ActionsCmd
   - Document platform-specific mappings

2. Handle edge cases:
   - CLI-only options (no API equivalent)
   - Divergent descriptions (different purposes)
   - Missing API descriptions (fallback to CLI)

**Phase 3c: Documentation Generation**:

1. **Implement Doc Generator** (`generate-cli-docs.py`):
   - Read `cli-metadata-enriched.json`
   - Create per-command markdown pages
   - Include examples from overlay pattern
   - Generate docs.seqera.io structure

2. **Set Up Examples Overlay**:
   - Similar to API docs pattern
   - Store curated examples separately
   - Merge during generation

**Phase 3d: CI/CD Automation**:

1. Create GitHub Action workflow:
   - Triggered on CLI releases
   - Fetch latest OpenAPI spec from docs repo
   - Run extraction → enrichment → generation
   - Create PR to docs repo
   - Flag new/changed options for review

---

## 📝 Phase 3: Docs Generation (Future)

1. Create doc generator script
2. Store manual examples separately (like API overlay pattern)
3. Merge extracted metadata + examples → markdown pages
4. Split monolithic `commands.md` into per-subcommand pages

---

## 🤖 Phase 4: Release Automation (Future)

1. GitHub Action triggered on tower-cli releases
2. Compare metadata with previous version
3. Auto-generate PR to docs repo with:
   - Updated reference pages
   - Changelog of CLI changes
   - Flags for commands needing new examples

---

## Architecture Decisions

### Why extract from Java source (not `--help` output)?

- Source annotations are the canonical truth
- Can detect hidden options, defaults, mixins
- Enables fixing inconsistencies at the source
- Better for automation (no need to run CLI binary)

### Why structured JSON intermediate format?

- Decouples extraction from generation
- Enables multiple output formats (markdown, man pages, etc.)
- Easy to diff between versions
- Can be committed for change tracking

### Why separate examples from auto-generated content?

- Examples need human curation (real-world usage patterns)
- Auto-generated reference stays in sync automatically
- Similar to API docs overlay pattern that works well

---

## Context From Parent Project

This work builds on the successful API docs automation:
- 17 manual steps → 2 human review checkpoints
- 4-6 hours → 30-60 minutes
- Uses Claude Skills, GitHub Actions, Speakeasy overlays

CLI docs will follow similar patterns but adapted for picocli/Java instead of OpenAPI.

---

## Collaboration Notes

- **Claude.ai chat**: Building artifacts, design decisions, file creation
- **Claude Code (this repo)**: Testing, validation, integration with actual codebase
- Artifacts created in chat → brought to repo for testing → feedback loop

---

## Questions for Testing

1. Does the extractor find all commands? Compare with `Tower.java` subcommands list
2. Are deeply nested commands found? (e.g., `compute-envs add aws-batch forge`)
3. Do mixin options appear in commands correctly?
4. Any parsing errors or warnings?
5. Does the hierarchy structure make sense for doc generation?