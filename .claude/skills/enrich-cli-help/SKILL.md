---
name: enrich-cli-help
description: Enrich Seqera Platform CLI command option descriptions with OpenAPI-quality standards. Use when improving help text for CLI commands, working with picocli annotations, or preparing documentation for CLI releases. Encapsulates the proven workflow of parallel agent research, manual enrichment, and verification.
---

# CLI Help Text Enrichment for Seqera Platform CLI

This skill provides a systematic approach to enriching CLI command option descriptions in the Seqera Platform CLI Java codebase, ensuring help text meets documentation quality standards.

## When to Use This Skill

Use this skill when:
- Enriching option descriptions for a CLI command family (e.g., pipelines, workspaces, runs)
- Improving help text quality for user-facing CLI commands
- Adding technical context and practical guidance to option descriptions
- Preparing CLI documentation updates for a new Seqera Platform CLI release
- Following up on metadata extraction to add OpenAPI-quality descriptions

## Prerequisites

- Seqera Platform CLI repository: `tower-cli` project root
- Working directory: Repository root directory
- Git branch: Feature branch for enrichment work (e.g., `feature/enrich-[command-family]`)
- OpenAPI spec available at: `docs/seqera-api-latest-decorated.yaml`

## Core Workflow

The enrichment process follows a proven 4-phase workflow that has been successfully applied to all command families.

### Phase 1: Parallel Agent Research

Launch 4 specialized agents in parallel to gather comprehensive intelligence about the command family.

**Agent 1: codebase-locator** - File Discovery
```
Prompt template:
"Find all command files in the [FAMILY_NAME] command family. Look for:
- Files in commands/[family]/ directory
- All *Cmd.java files
- Any nested subcommand directories
- Mixin/Options classes used by the commands

List all command files found with their full paths."
```

**Agent 2: codebase-analyzer** - Architecture Analysis
```
Prompt template:
"Analyze the [FAMILY_NAME] command family to identify:
1. All command files and their @CommandLine.Option annotations
2. Count total options across all commands
3. Identify any Platform/Provider mixin patterns (like compute-envs/credentials)
4. List current option descriptions to assess quality
5. Check if options are extracted by metadata system or hidden by mixins

Focus on understanding the architecture and option extraction feasibility."
```

**Agent 3: codebase-analyzer OR codebase-pattern-finder** - OpenAPI/Patterns
```
Option A (OpenAPI schemas available):
"Find OpenAPI schema references for [FAMILY_NAME] entities. Search for:
1. Related schemas in docs/seqera-api-latest-decorated.yaml
2. Entity DTOs and response models
3. Enum types and SecurityKeys patterns
4. Schema descriptions that can inform CLI option descriptions

Provide file:line references where these schemas are used."

Option B (Pattern finding):
"Find patterns in [FAMILY_NAME] commands:
1. Common option patterns (filters, pagination, identifiers)
2. Similar implementations across commands
3. Naming conventions and transform patterns
4. Examples of good existing descriptions"
```

**Agent 4: Decision Making / Additional Analysis**
```
Use for supplementary analysis based on findings from other agents:
- Deep dive into specific files
- Validate architectural assumptions
- Check for edge cases
- Plan enrichment approach
```

**How to Execute**:
1. Launch all 4 agents in parallel using Task tool with single message containing 4 tool calls
2. Wait for all agents to complete
3. Review and synthesize findings
4. Document agent IDs in case resumption is needed

### Phase 2: Synthesis & Planning

After agents complete, synthesize findings and plan enrichment:

**Analysis Checklist**:
- [ ] Total command files identified
- [ ] Total options to enrich
- [ ] Architecture pattern identified (Platform/Provider mixin? Direct options? ArgGroups?)
- [ ] Current description quality assessed
- [ ] OpenAPI schemas mapped (if applicable)
- [ ] Common patterns documented
- [ ] Special cases noted

**Create Enrichment Plan**:
1. List all files to modify in priority order
2. Note shared mixins (high-leverage enrichment targets)
3. Identify options needing OpenAPI schema lookups
4. Flag options requiring security warnings or data loss warnings
5. Document common patterns to apply consistently

**Decision Point**: Manual enrichment vs scripted
- **Manual enrichment**: Best for <30 files, complex options, quality-critical commands
- **Scripted enrichment**: Only for highly repetitive patterns (e.g., 13+ similar Platform classes)
- **Default**: Manual enrichment (proven effective in all phases)

### Phase 3: Manual Enrichment

Systematically enrich option descriptions following quality standards.

**Enrichment Quality Standards**:

1. **Technical Precision**
   - Avoid vague terms like "identifier" without context
   - Specify data types (numeric ID, string name, file path)
   - Add format specifications (e.g., "Must be a valid URL")
   - Include units for metrics (minutes, GB, hours)

2. **Practical Guidance**
   - Add examples where helpful (e.g., "e.g., s3://bucket/path")
   - Reference related commands (e.g., "Find IDs using 'tw teams list'")
   - Explain prerequisites (e.g., "Must be organization member first")
   - Document lookup behavior

3. **Security & Safety Context**
   - Warn about data loss for destructive operations
   - Flag sensitive fields (passwords, keys, tokens)
   - Explain cascade effects (deletion → membership loss → access loss)
   - Recommend secure practices (tokens over passwords)

4. **Operational Context**
   - Clarify scope (workflow-level vs task-level, org vs workspace)
   - Explain default values and behavior
   - Document special cases and constraints
   - Add troubleshooting hints where applicable

5. **Consistency Patterns**
   - Use standard descriptions for common options across commands
   - Example: "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'."
   - Maintain terminology consistency (workspace not work-space, Fusion not fusion)

**Enrichment Process**:

1. **Read files to enrich** (use Read tool in parallel for multiple files)
2. **Edit descriptions** (use Edit tool for each option):
   ```java
   // BEFORE:
   @CommandLine.Option(names = {"-u", "--user"}, description = "User email address", required = true)

   // AFTER:
   @CommandLine.Option(names = {"-u", "--user"}, description = "User email address to invite. If the user doesn't have a Seqera Platform account, they will receive an invitation email to join the organization.", required = true)
   ```
3. **Apply patterns consistently** across similar options
4. **Prioritize high-leverage targets** (mixins used by multiple commands)

**Tips**:
- Start with shared mixin classes (impacts multiple commands)
- Enrich related commands together for consistency
- Use OpenAPI schema descriptions as quality baseline
- Incorporate user feedback if provided during enrichment

### Phase 4: Verification & Commit

Verify quality and commit changes atomically.

**Verification Steps**:

1. **Review changes**:
   ```bash
   # From tower-cli project root
   git diff --stat
   git diff [file1] [file2] | head -100
   ```

2. **Quality checks**:
   - [ ] All targeted files modified
   - [ ] No syntax errors in Java files
   - [ ] Descriptions are complete sentences with periods (or proper fragments)
   - [ ] Technical terms capitalized correctly (Nextflow, Platform, Fusion)
   - [ ] Examples use valid formats
   - [ ] Security warnings included where needed

3. **Stage and commit**:
   ```bash
   git add -A
   git status  # Verify files staged

   # Create comprehensive commit message
   git commit -m "$(cat <<'EOF'
   Phase 3[x]: Enrich [FAMILY_NAME] command options

   Enriched option descriptions across [FAMILY_NAME] command family with
   technical context, usage examples, and [specific improvements].

   Files enriched ([N] total):
   [List of files with option counts]

   Key improvements:
   - [Major enhancement 1]
   - [Major enhancement 2]
   - [Pattern standardization 3]

   Total options enriched: [N] across [M] command files

   Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
   EOF
   )"
   ```

4. **Push changes**:
   ```bash
   git push
   ```

**Commit Message Template**:
- **Title**: Phase 3[x]: Enrich [FAMILY_NAME] command options
- **Body**:
  - Overview paragraph
  - "Files enriched ([N] total):" section with file list
  - Key improvements as bullet list
  - Statistics (options enriched, families covered)
  - Common patterns section if applicable

## Command Family Architectures

Understanding architecture patterns found in the Seqera Platform CLI helps plan enrichment approach.

### Pattern 1: Platform/Provider Mixin (compute-envs, credentials)

**Characteristics**:
- Options defined in Platform/Provider classes
- `@CommandLine.Mixin` used in add/update commands
- Metadata extractor does NOT capture these options
- Typically 20-40+ options per Platform class

**Example**:
```java
// AddAwsCmd.java
@CommandLine.Command(name = "aws-batch-forge")
public class AddAwsCmd extends AbstractPlatformsCmd {
    @CommandLine.Mixin
    public AwsBatchForgePlatform platform;  // Options hidden in mixin
}

// AwsBatchForgePlatform.java
public class AwsBatchForgePlatform extends AbstractPlatform {
    @CommandLine.Option(names = {"--work-dir"}, description = "...", required = true)
    public String workDir;

    @CommandLine.Option(names = {"--region"}, description = "...")
    public String region;
    // ... 36 more options
}
```

**Enrichment approach**: Manual enrichment of Platform/Provider classes, supplemented by batch scripts for highly similar classes (e.g., HPC schedulers).

### Pattern 2: Direct Options (runs, organizations, teams, members)

**Characteristics**:
- Options defined directly in command classes
- Metadata extractor DOES capture these options
- Typically 1-10 options per command
- May use shared option mixins (e.g., PaginationOptions)

**Example**:
```java
@CommandLine.Command(name = "add", description = "Add a team")
public class AddCmd extends AbstractTeamsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Team name", required = true)
    public String teamName;

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or identifier", required = true)
    public String organizationRef;
}
```

**Enrichment approach**: Manual enrichment for quality and context, even though metadata extraction works.

### Pattern 3: Parent Command Inheritance (nested subcommands)

**Characteristics**:
- Nested subcommands use `@CommandLine.ParentCommand`
- Options inherited from parent command
- Child commands may have additional options

**Example**:
```java
// teams members add
@CommandLine.Command(name = "add")
public class AddCmd extends AbstractApiCmd {
    @CommandLine.Option(names = {"-m", "--member"}, description = "...", required = true)
    public String userNameOrEmail;

    @CommandLine.ParentCommand
    public MembersCmd parent;  // Inherits teamName, organizationRef from parent
}
```

**Enrichment approach**: Enrich both parent and child options, document inheritance relationship.

## Quality Standards Reference

### Common Option Patterns

**Organization Reference** (used 11+ times across families):
```java
@CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
public String organizationRef;
```

**Workspace Reference**:
```java
@CommandLine.Option(names = {"-w", "--workspace"}, description = "Workspace name or identifier. Specify either the workspace name or numeric identifier.")
public String workspace;
```

**Filter Options**:
```java
@CommandLine.Option(names = {"-f", "--filter"}, description = "Filter results by name prefix. Case-insensitive prefix matching on the name field.")
public String filter;
```

**Overwrite Flags**:
```java
@CommandLine.Option(names = {"--overwrite"}, description = "Overwrite existing [entity]. If [entity] with this name already exists, delete it first before creating the new one. Use with caution as this permanently deletes the existing [entity] and all associated data.", defaultValue = "false")
public Boolean overwrite;
```

**Display Flags**:
```java
@CommandLine.Option(names = {"--show-details"}, description = "Display additional [specific details]. Shows [what details include].")
public boolean showDetails;
```

### OpenAPI Schema Integration

When OpenAPI schemas are available, use them to inform CLI descriptions:

**Mapping Strategy**:
1. Find corresponding schema in `docs/seqera-api-latest-decorated.yaml`
2. Review schema property descriptions
3. Adapt API language for CLI context:
   - API: "The AWS region where resources are deployed"
   - CLI: "AWS region where the EKS cluster is deployed (e.g., us-east-1, eu-west-1)"
4. Add CLI-specific context (file paths, command references, defaults)

**Common Transformations**:
- kebab-case endpoint names → camelCase option names
- API field names → CLI option names (may differ)
- API descriptions → CLI descriptions (add "the" and context)
- Enum values in schemas → option documentation

## Examples from Previous Phases

### Phase 3g: Compute-Envs Platform Classes (500+ options)

**Challenge**: 13 Platform classes with provider-specific options, mixin pattern prevents extraction.

**Approach**:
1. Research: Located all Platform files, mapped to OpenAPI schemas
2. Batch scripting: Created Python script for 5 similar HPC platforms
3. Manual enrichment: Individual attention for cloud platforms (AWS, Azure, GCP, K8s)
4. Quality verification: Checked examples, formats, dependencies

**Result**: Complete compute-envs coverage, consistent quality across all platforms.

### Phase 3h: Credentials Provider Classes (29 options)

**Challenge**: 12 Provider classes with security-sensitive options.

**Approach**:
1. Research: Mapped to SecurityKeys schemas, identified authentication patterns
2. Manual enrichment: Emphasized security guidance, token generation instructions
3. Provider-specific examples: Registry hostnames, key formats, service account paths

**Result**: All credential providers enriched with security-conscious descriptions.

### Phase 3i: Runs Family (38 options)

**Challenge**: Display control flags needing statistical context, user feedback during enrichment.

**Approach**:
1. Research: Identified display flag patterns, statistical metric types
2. Manual enrichment: Added units (GB, hours), explained quartiles, documented file formats
3. User feedback: Real-time adjustments to description phrasing
4. Pattern consistency: Standardized workflow vs task-level operation descriptions

**Result**: Complete runs family with precise metric documentation, user-approved quality.

### Phase 3j: Organizations/Teams/Members (26 options)

**Challenge**: Role-based access control needing clear permission explanations.

**Approach**:
1. Research: Analyzed OrgRole enum, participant types, organizational structure
2. Pattern standardization: "Organization name or numeric ID" used 11 times
3. Permission documentation: Detailed OWNER/MEMBER/COLLABORATOR role descriptions
4. Safety warnings: Data loss cascade effects for deletions

**Result**: Coherent access control documentation across three related families.

## Troubleshooting

**Issue**: Agent research taking too long
- **Solution**: Use `model: "haiku"` parameter for faster agents when task is straightforward

**Issue**: Too many files to enrich manually
- **Solution**: Consider batch scripting for highly repetitive patterns, but default to manual for quality

**Issue**: Uncertain about OpenAPI mapping
- **Solution**: Use WebFetch to check API docs at docs.seqera.io for examples

**Issue**: Git conflicts during commit
- **Solution**: Pull latest changes, resolve conflicts, recommit

**Issue**: Descriptions too verbose
- **Solution**: Follow user feedback pattern - concise, technical, practical. Aim for 1-2 sentences per option.

## Success Metrics

- **Coverage**: All command files in family enriched
- **Quality**: Descriptions meet technical precision + practical guidance standards
- **Consistency**: Common patterns applied uniformly
- **Safety**: Warnings included for destructive operations
- **Verification**: Clean git diff, comprehensive commit message

## Post-Enrichment

After enrichment phase completes:

1. **Update progress tracking**: Document new phase with statistics (if using progress tracking)
2. **Extract metadata**: Run `./gradlew extractCliMetadata` to regenerate `docs/cli-metadata.json`
3. **Verify extraction**: Check that new descriptions appear in `docs/cli-metadata.json`
4. **Plan next family**: Identify next high-impact command family for enrichment

---

## Quick Reference Commands

```bash
# Working directory: tower-cli project root

# Launch parallel agents (use Task tool with 4 tool calls in single message)
# Agent types: codebase-locator, codebase-analyzer, codebase-pattern-finder

# Read multiple files in parallel
# Use Read tool with multiple tool calls in single message

# Edit options (one Edit tool call per option)
# old_string: exact current description line
# new_string: enhanced description line

# Verify changes
git diff --stat
git diff [files] | head -100

# Commit with template
git add -A && git commit -m "[message]" && git push

# Extract updated metadata (Java reflection-based)
./gradlew extractCliMetadata
```

## References

- **CLI metadata**: `docs/cli-metadata.json` (regenerated via `./gradlew extractCliMetadata`)
- **Metadata extractor**: `src/main/java/io/seqera/tower/cli/utils/metadata/CliMetadataExtractor.java`
- **OpenAPI spec**: `docs/seqera-api-latest-decorated.yaml`
- **Documentation**: `docs/README.md` (process and quality standards)

---

*This skill encapsulates learnings from Phases 3g-3j (2026-01-13), enriching 593+ option descriptions across 52 files in 5 command families.*
