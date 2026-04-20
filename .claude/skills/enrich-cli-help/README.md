# CLI Metadata Generation Skill

This skill documents how to generate and validate structured CLI metadata from the `tower-cli` source tree.

## Quick Start

Run:

```bash
./gradlew extractCliMetadata
```

The output is written to `build/cli-metadata/cli-metadata.json`.

## What This Skill Does

1. Generates CLI metadata from picocli command definitions
2. Verifies the extractor output shape
3. Documents the release-artifact workflow
4. Keeps metadata generation independent from external OpenAPI specs

## Release strategy

- Metadata is generated from source code without modifying it
- Metadata is a build and release artifact, not a tracked repository file
- Downstream docs tooling should consume the release asset

## Files

- `SKILL.md` - Complete skill documentation

## Requirements

- Repository root: `tower-cli`
- Gradle task: `extractCliMetadata`
- Extractor: `src/main/java/io/seqera/tower/cli/utils/metadata/CliMetadataExtractor.java`
