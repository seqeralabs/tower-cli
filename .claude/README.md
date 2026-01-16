# Claude Code Configuration for Seqera Platform CLI

This directory contains Claude Code configuration and skills for contributors working on the Seqera Platform CLI codebase.

## Skills Available

### enrich-cli-help

Systematic workflow for enriching CLI command option descriptions with OpenAPI-quality standards.

**Use this skill when:**
- Improving help text for CLI command families
- Adding technical context and practical guidance to options
- Preparing CLI documentation updates for releases
- Following up on metadata extraction to enhance descriptions

**Quick start:**
```
/enrich-cli-help [command-family-name]
```

Or simply mention: "Let's enrich the [family] command options"

**Documentation:**
- `skills/enrich-cli-help/SKILL.md` - Complete workflow guide
- `skills/enrich-cli-help/README.md` - Quick reference
- `skills/enrich-cli-help/references/` - Quality standards and architecture patterns

**Proven track record:**
- Phase 3g: Compute-envs (13 platforms, 500+ options)
- Phase 3h: Credentials (12 providers, 29 options)
- Phase 3i: Runs (12 commands, 38 options)
- Phase 3j: Organizations/Teams/Members (15 commands, 26 options)

**Total:** 593+ option descriptions enriched across 52 files

## Project Context

### Repository Structure
```
tower-cli/
├── src/main/java/io/seqera/tower/cli/commands/  # CLI command implementations
├── docs/
│   ├── scripts/
│   │   ├── extract-cli-metadata.py              # Metadata extractor
│   │   └── progress.md                          # Project progress tracker
│   ├── cli-metadata.json                        # Extracted CLI metadata
│   └── seqera-api-latest-decorated.yaml         # OpenAPI spec with overlays
└── .claude/                                     # Claude Code configuration (this folder)
```

### Key Files

**Metadata Extraction:**
- `docs/scripts/extract-cli-metadata.py` - Extracts CLI metadata from picocli Java annotations
- `docs/cli-metadata.json` - Generated metadata for all 161 commands

**Documentation:**
- `docs/scripts/progress.md` - Complete project progress and phase documentation
- `docs/research/cli-docs-style-guide.md` - CLI documentation style standards

**OpenAPI Integration:**
- `docs/seqera-api-latest-decorated.yaml` - Decorated OpenAPI spec (API descriptions inform CLI help text quality)

## Workflow Overview

### CLI Documentation Automation Pipeline

```
1. Extract Metadata
   └─> python docs/scripts/extract-cli-metadata.py src/main/java > docs/cli-metadata.json

2. Enrich Descriptions
   └─> Use /enrich-cli-help skill for command families
   └─> Manual editing of @CommandLine.Option descriptions in Java files
   └─> OpenAPI schemas provide quality baseline

3. Verify & Commit
   └─> git diff to review changes
   └─> Comprehensive commit message with statistics
   └─> Push to feature branch

4. Update Metadata
   └─> Re-run extraction to capture enriched descriptions
   └─> Commit updated cli-metadata.json
```

### Branch Strategy

**Current work branch:** `ll-metadata-extractor-and-docs-automation`

All CLI enrichment work should be done on this branch or a related feature branch.

## Getting Started

### For New Contributors

1. **Read the progress tracker:**
   ```bash
   cat docs/scripts/progress.md
   ```

2. **Check enrichment status:**
   - Phase 3g-3j: Complete (compute-envs, credentials, runs, orgs/teams/members)
   - Remaining: ~60 command families available for enrichment

3. **Choose a command family to enrich:**
   - Pipelines, workspaces, actions, datasets, secrets, labels, participants, etc.
   - Use `/enrich-cli-help [family-name]` to start

4. **Follow the proven workflow:**
   - Phase 1: Parallel agent research (4 agents)
   - Phase 2: Synthesis & planning
   - Phase 3: Manual enrichment with quality standards
   - Phase 4: Verification & commit

### Quality Standards

All enriched descriptions must meet these criteria:
- **Technical precision**: Specify data types, formats, units
- **Practical guidance**: Include examples, prerequisites, command references
- **Security context**: Warn about sensitive fields, data loss
- **Operational clarity**: Explain scope, defaults, constraints
- **Pattern consistency**: Use standard descriptions for common options

See `skills/enrich-cli-help/references/quality-standards.md` for detailed examples.

## Architecture Patterns

The tower-cli codebase uses several option definition patterns:

### Pattern 1: Platform/Provider Mixin
- **Examples**: compute-envs, credentials
- **Characteristics**: Options in separate Platform/Provider classes
- **Metadata extraction**: Does NOT capture these options
- **Enrichment**: Manual editing of Platform/Provider Java files

### Pattern 2: Direct Options
- **Examples**: runs, organizations, teams, members
- **Characteristics**: Options defined directly in command classes
- **Metadata extraction**: DOES capture these options
- **Enrichment**: Manual editing for quality enhancement

See `skills/enrich-cli-help/references/architecture-patterns.md` for complete pattern documentation.

## Contributing

### Adding New Skills

To add a new skill to this repository:

1. Create skill directory:
   ```bash
   mkdir -p .claude/skills/[skill-name]
   ```

2. Add required files:
   - `SKILL.md` - Main skill documentation with workflow
   - `README.md` - Quick reference guide
   - `references/` - Supporting documentation
   - `scripts/` - Automation scripts (optional)

3. Update this README with skill description

4. Commit to repository:
   ```bash
   git add .claude/
   git commit -m "Add [skill-name] skill for contributors"
   ```

### Settings

Local Claude Code settings can be configured in `.claude/settings.local.json`.

Current settings preserve conversation history for context continuity.

## Resources

### Documentation
- **Progress tracker**: `docs/scripts/progress.md`
- **Style guide**: `docs/research/cli-docs-style-guide.md`
- **Enrichment examples**: See Phase 3g-3j sections in progress.md

### External Links
- **CLI documentation**: https://docs.seqera.io/platform/latest/cli/overview
- **Seqera Platform API**: https://docs.seqera.io/platform/latest/api/overview
- **Claude Code docs**: https://claude.com/claude-code

---

**Questions or issues?** Open an issue in the tower-cli repository or ask in #team-documentation Slack channel.
