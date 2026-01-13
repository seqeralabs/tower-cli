# CLI Architecture Patterns Reference

This document catalogs the architectural patterns found in Seqera Platform CLI command families, helping guide enrichment approach decisions.

## Pattern 1: Platform/Provider Mixin

**Found in**: compute-envs, credentials

### Characteristics
- Options defined in separate Platform/Provider classes
- `@CommandLine.Mixin` used in add/update commands
- Metadata extractor does NOT capture these options
- Each platform/provider has 20-40+ options
- Often extends AbstractPlatform/AbstractProvider

### Example Structure
```
commands/
  computeenvs/
    AddCmd.java                    # Container with subcommands
    platforms/
      AwsBatchForgePlatform.java  # 38 options
      EksPlatform.java            # 20 options
      GkePlatform.java            # 18 options
      ...
```

### Code Pattern
```java
// AddAwsCmd.java - Container command
@CommandLine.Command(name = "aws-batch-forge")
public class AddAwsCmd extends AbstractPlatformsCmd {
    @CommandLine.Mixin
    public AwsBatchForgePlatform platform;  // Options hidden here
}

// AwsBatchForgePlatform.java - Actual options
public class AwsBatchForgePlatform extends AbstractPlatform {
    @CommandLine.Option(names = {"--work-dir"}, description = "...", required = true)
    public String workDir;

    @CommandLine.Option(names = {"--region"}, description = "...")
    public String region;

    // ... 36 more options
}
```

### Enrichment Approach
1. **Manual enrichment** of Platform/Provider classes
2. Use **batch scripting** for highly similar classes (e.g., 5 HPC schedulers with identical options)
3. Start with **OpenAPI schemas** as quality baseline
4. Document **platform-specific** dependencies and constraints

### Previous Successes
- **Phase 3g**: 13 compute-env platforms, 500+ options
- **Phase 3h**: 12 credential providers, 29 options

---

## Pattern 2: Direct Options

**Found in**: runs, organizations, teams, members, pipelines, actions, workspaces

### Characteristics
- Options defined directly in command class
- Metadata extractor DOES capture these options
- Typically 1-10 options per command
- May use shared option mixins (PaginationOptions, WorkspaceOptionalOptions)

### Enrichment Approach
1. **Manual enrichment** for quality and contextual richness
2. Enrich **shared mixins first** (high-leverage: impacts multiple commands)
3. Apply **consistent patterns** across similar options

### Previous Successes
- **Phase 3i**: Runs family, 12 commands, 38 options
- **Phase 3j**: Orgs/Teams/Members, 15 commands, 26 options

---

## Pattern 3: Parent Command Inheritance

**Found in**: Nested subcommands (teams members, runs tasks, compute-envs add)

### Code Pattern
```java
// Parent command
@CommandLine.Command(
    name = "members",
    subcommands = {AddCmd.class, DeleteCmd.class}
)
public class MembersCmd {
    @CommandLine.Option(names = {"-t", "--team"})
    public String teamName;

    @CommandLine.ParentCommand
    public MembersCmd parent;  // Child accesses parent options
}
```

---

*For complete pattern documentation, see the full architecture-patterns.md reference file.*
