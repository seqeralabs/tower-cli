# Claude Code Configuration for Seqera Platform CLI

This directory contains Claude Code configuration and skill documentation for contributors working on the Seqera Platform CLI codebase.

## Skills Available

### enrich-cli-help

Workflow documentation for generating structured CLI metadata from the `tower-cli` source tree.

**Use this skill when:**
- Updating the metadata extractor
- Validating metadata output
- Reviewing the release-artifact workflow for CLI docs metadata

**Quick start:**
```bash
./gradlew extractCliMetadata
```

This generates `build/cli-metadata/cli-metadata.json`.

**Documentation:**
- `skills/enrich-cli-help/SKILL.md` - Complete workflow guide
- `skills/enrich-cli-help/README.md` - Quick reference

## Project Context

### Repository Structure
```text
tower-cli/
├── src/main/java/io/seqera/tower/cli/
│   └── utils/metadata/
│       └── CliMetadataExtractor.java
├── docs/
│   └── README.md
├── .claude/
└── build.gradle
```

### Key Files

- `src/main/java/io/seqera/tower/cli/utils/metadata/CliMetadataExtractor.java`
- `build.gradle`
- `docs/README.md`
- `.github/workflows/build.yml`

## Workflow Overview

```text
1. Extract metadata
   └─> ./gradlew extractCliMetadata
   └─> Outputs build/cli-metadata/cli-metadata.json

2. Verify
   └─> Run tests when the extractor changes
   └─> Confirm metadata shape stays stable

3. Release
   └─> Generate cli-metadata.json in CI
   └─> Upload it to GitHub release assets
   └─> Dispatch docs automation with the release asset URL
```
