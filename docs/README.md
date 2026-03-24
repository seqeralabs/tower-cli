# CLI Metadata Generator

This directory documents the CLI metadata generator used by `tower-cli`.

## Overview

The metadata generator inspects the compiled CLI command tree through picocli and emits a structured JSON document for downstream documentation tooling.

The generator:
- lives in the `tower-cli` repository
- is implemented in Java
- runs as the Gradle task `extractCliMetadata`
- does not modify CLI source files
- produces build output that is not tracked in Git

## Manual generation

Generate metadata locally with:

```bash
./gradlew extractCliMetadata
```

The task writes the artifact to:

```text
build/cli-metadata/cli-metadata.json
```

## Output contents

The generated JSON includes:
- top-level extraction metadata
- the full command hierarchy
- a flat command lookup map
- command options, positional parameters, types, arity, and descriptions
- resolved picocli mixins

## Implementation

The extractor is implemented in:

```text
src/main/java/io/seqera/tower/cli/utils/metadata/CliMetadataExtractor.java
```

It builds the `tw` command tree, walks the picocli `CommandSpec` model, filters standard help/version options, and serializes the resulting structure with Jackson.

## Release flow

The metadata file is a release artifact, similar to the CLI binaries.

At release time the build pipeline:
1. runs `./gradlew extractCliMetadata`
2. produces `build/cli-metadata/cli-metadata.json`
3. lets JReleaser publish that file to the GitHub release assets

Downstream consumers should fetch metadata from the release assets rather than from a tracked repository file.

**Documentation:** See `.claude/skills/enrich-cli-help/` for complete workflow guide

## Troubleshooting

### Missing GitHub package credentials

If the build fails with `Username must not be null!`, configure GitHub credentials for the private `tower-java-sdk` dependency:

```bash
# In ~/.gradle/gradle.properties
github.packages.user=your-github-username
github.packages.token=your-github-token
```

You can also inline credentials for a one-off run:

```bash
GITHUB_USERNAME=<username> GITHUB_TOKEN=<token> ./gradlew extractCliMetadata
```

or

```bash
GITHUB_USERNAME=<username> GITHUB_TOKEN=$(cat gh_token.txt) ./gradlew extractCliMetadata
```

### Commands missing from output

Check for:
- Proper `@Command` annotation on class
- Parent command has `subcommands = {ChildCmd.class}` reference
- Import statement if subcommand in different package

### Options missing from output

Check for:
- Ensure mixin classes are properly annotated with `@Mixin`
- Verify option annotations: `@Option` or `@CommandLine.Option` (both supported)
- Check that picocli can access the option at runtime

## Related Documentation

- **Java extractor:** `src/main/java/io/seqera/tower/cli/utils/metadata/CliMetadataExtractor.java`
- **Gradle task:** `extractCliMetadata` in `build.gradle`
- **Release workflow:** `.github/workflows/build.yml`
- **JReleaser config:** `jreleaser.yml`
- **Claude Code skill:** `.claude/skills/enrich-cli-help/SKILL.md`
- **CLI documentation:** https://docs.seqera.io/platform/latest/cli/overview
