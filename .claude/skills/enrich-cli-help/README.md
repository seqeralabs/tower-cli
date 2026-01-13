# CLI Help Text Enrichment Skill

This skill encapsulates the proven workflow for enriching Seqera Platform CLI command option descriptions with OpenAPI-quality standards.

## Quick Start

To use this skill:

```
/enrich-cli-help [command-family-name]
```

Or simply mention the family you want to enrich and I'll use this skill automatically.

## What This Skill Does

1. **Research Phase**: Launches 4 parallel agents to analyze command family architecture
2. **Planning Phase**: Synthesizes findings and creates enrichment plan
3. **Enrichment Phase**: Guides systematic manual enrichment with quality standards
4. **Verification Phase**: Reviews changes and creates comprehensive commit

## Proven Track Record

Successfully used to enrich:
- **Phase 3g**: Compute-envs (13 platforms, 500+ options)
- **Phase 3h**: Credentials (12 providers, 29 options)
- **Phase 3i**: Runs (12 commands, 38 options)
- **Phase 3j**: Organizations/Teams/Members (15 commands, 26 options)

**Total**: 593+ option descriptions across 52 files in 5 command families

## Key Features

- Parallel agent research for efficient intelligence gathering
- Architecture pattern recognition (Platform/Provider vs Direct Options)
- OpenAPI schema integration for quality baseline
- Consistent pattern application across options
- Security and safety warning guidance
- Atomic commits with comprehensive documentation

## Files

- `SKILL.md` - Complete skill documentation
- `references/quality-standards.md` - Description quality standards and examples
- `references/architecture-patterns.md` - CLI architecture patterns reference

## Requirements

- Repository: `/Users/llewelyn-van-der-berg/Documents/GitHub/tower-cli`
- Branch: `ll-metadata-extractor-and-docs-automation`
- OpenAPI spec: `docs/seqera-api-latest-decorated.yaml`
