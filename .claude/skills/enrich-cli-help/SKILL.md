---
name: enrich-cli-help
description: Generate structured CLI metadata from tower-cli source code using the built-in Java extractor. Use when validating metadata generation, updating release automation, or checking the downstream artifact consumed by docs tooling.
---

# CLI Metadata Generation for Seqera Platform CLI

This skill is for generating and validating CLI metadata from the `tower-cli` source tree.

## When to Use This Skill

Use this skill when:
- Updating the metadata extractor implementation
- Verifying that the Gradle task still produces valid metadata
- Reviewing release automation for the metadata artifact
- Investigating command coverage in the generated metadata
- Confirming downstream docs consumers can rely on the release artifact

## Scope

The generator is deterministic and works directly from the compiled CLI command tree. It does not edit CLI source code and it does not depend on external OpenAPI specifications.

## Workflow

### Step 1: Generate metadata locally

Run the Gradle task from the repository root:

```bash
./gradlew extractCliMetadata
```

The generated file is written to:

```text
build/cli-metadata/cli-metadata.json
```

### Step 2: Validate the output

Check that the JSON contains the expected top-level keys:

```text
metadata
hierarchy
commands
```

If you are changing the extractor, add or update unit tests under `src/test/`.

### Step 3: Review only the metadata generation path

Focus on:
- `src/main/java/io/seqera/tower/cli/utils/metadata/CliMetadataExtractor.java`
- `build.gradle`
- release automation in `.github/workflows/`
- docs describing metadata generation

Do not mix this workflow with CLI help-text enrichment or external API specs.

### Step 4: Release behavior

At release time the metadata should be:
1. generated from the current `tower-cli` source tree
2. uploaded as a GitHub release artifact
3. passed to downstream docs tooling via the release asset URL

## Notes

- Remove hard-coded local paths from all documentation.
- Keep generated metadata out of Git.
- Prefer battle-tested libraries already on the classpath over hand-written serializers.
- Keep the workflow focused on metadata generation only.
