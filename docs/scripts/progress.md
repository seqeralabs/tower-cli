# CLI Documentation Automation - Progress Tracker

## Project Overview

We're automating the CLI documentation workflow for tower-cli, similar to the successful API docs automation we built for the Platform API. The goal is to:

1. **Extract** CLI metadata from picocli Java annotations → structured JSON
2. **Enrich** descriptions to match docs quality standards
3. **Generate** per-command documentation pages
4. **Automate** updates on CLI releases via GitHub Actions

## Current Status: Phase 1 Complete ✅

### What We've Built

**CLI Metadata Extractor** (`extract_cli_metadata.py`)
- Python script that parses picocli annotations from Java source files
- Extracts: `@Command`, `@Option`, `@Parameters`, `@Mixin` annotations
- Builds command hierarchy (parent → subcommand relationships)
- Resolves mixins to include shared options in commands
- Outputs structured JSON

**Test Suite** (`test_extractor.py`)
- Unit tests for string/array extraction
- Annotation parsing tests
- Full pipeline integration tests with sample Java files

### Files to Test

The extractor script is in this repo at: `[wherever you place it]`

Run it with:
```bash
python extract_cli_metadata.py src/main/java > cli-metadata.json
```

Expected output structure:
```json
{
  "metadata": { "total_commands": N, "total_mixins": M },
  "commands": { "ClassName": { "name": "...", "options": [...], ... } },
  "hierarchy": { "name": "tw", "children": [...] },
  "mixins": { "WorkspaceOptionalOptions": [...] }
}
```

### Testing Checklist

- [ ] Run extractor against full `src/main/java` directory
- [ ] Verify all 18+ top-level commands are extracted (pipelines, runs, compute-envs, etc.)
- [ ] Check that nested commands are found (e.g., `tw compute-envs add aws-batch`)
- [ ] Verify mixin resolution (WorkspaceOptionalOptions should appear in commands that use it)
- [ ] Check option details: names, descriptions, required flags, defaults
- [ ] Verify parameter extraction (positional args like PIPELINE_URL)
- [ ] Compare extracted descriptions with `--help` output for accuracy

### Known Limitations

- Some complex annotation patterns may not parse perfectly
- Mixin resolution depends on finding `*Options.java` files
- String concatenation in descriptions should work but edge cases may exist

---

## Phase 2: Description Standards (Next)

Once extraction is validated, we'll:

1. **Create CLI style guide** - consistent description patterns like:
   - Command descriptions: verb-noun, sentence case, period at end
   - Option descriptions: what it does, not "Use this to..."
   - Consistent terminology matching Platform product naming

2. **Analyze current state** - identify inconsistencies in existing annotations

3. **Generate improved descriptions** - use Claude to rewrite for consistency

4. **PR to tower-cli** - push improved annotations back to source

---

## Phase 3: Docs Generation (Future)

1. Create doc generator script
2. Store manual examples separately (like API overlay pattern)
3. Merge extracted metadata + examples → markdown pages
4. Split monolithic `commands.md` into per-subcommand pages

---

## Phase 4: Release Automation (Future)

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