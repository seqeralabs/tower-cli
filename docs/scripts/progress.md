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

## 📋 Phase 2: Description Standards (IN PROGRESS - 2026-01-12)

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

### 🚧 Remaining Work

**Commands to Update**: 150 commands remaining (161 total - 11 compute-envs complete)

**Top-Level Commands** (18 remaining):
- `tw` (root) - Update to "Seqera Platform CLI"
- `actions`, `collaborators`, `credentials`, `datasets`, `data-links`
- `info`, `labels`, `launch`, `members`, `organizations`
- `participants`, `pipelines`, `runs`, `secrets`, `studios`
- `teams`, `workspaces`

**Subcommands by Family**:
- Actions: ~8 commands
- Collaborators: ~2 commands
- Credentials: ~6 commands
- Datasets: ~7 commands
- Data-links: ~6 commands
- Info: 1 command
- Labels: ~4 commands
- Launch: 1 command (but many options)
- Members: ~5 commands
- Organizations: ~5 commands
- Participants: ~5 commands
- Pipelines: ~7 commands
- Runs: ~7 commands
- Secrets: ~5 commands
- Studios: ~8 commands
- Teams: ~15 commands
- Workspaces: ~7 commands

**Mixin Classes to Review**:
- `WorkspaceOptionalOptions` - Used in 50+ commands, has placeholder "DESCRIPTION"
- Other shared option groups

### Next Steps

1. **Update remaining command families** - Apply patterns from compute-envs to all 150 remaining commands
2. **Update mixin classes** - Fix WorkspaceOptionalOptions and other shared option groups
3. **Re-run metadata extraction** - Verify all improvements applied correctly
4. **Create PR to tower-cli** - Submit improved annotations
5. **Update cli-metadata.json** - Commit improved metadata to docs repo

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