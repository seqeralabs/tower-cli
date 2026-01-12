# CLI Documentation Style Guide

**Date**: 2026-01-12
**Source**: https://docs.seqera.io/platform-cloud/cli/commands
**Purpose**: Ensure consistency across all CLI command descriptions in both Java annotations and generated documentation

---

## Command Descriptions

### Format
- **Imperative verb + object/outcome**
- Describe what the command does, not how to use it
- Keep it concise (1-2 short sentences maximum)

### Good Examples
```
✅ "Add credentials to the workspace"
✅ "Delete a compute environment"
✅ "List available datasets"
✅ "Update dataset metadata"
✅ "Manage labels and resource labels"
```

### Bad Examples
```
❌ "This command adds credentials" (too verbose, passive)
❌ "Adding credentials" (gerund form, not imperative)
❌ "Adds credentials" (third person, sounds like code doc)
❌ "Use this to add credentials" (instructional, not descriptive)
```

### CRUD Operation Patterns

**Add/Create**:
- Format: `"Add <resource> [qualifier]"`
- Examples:
  - "Add credentials to the workspace"
  - "Add a new compute environment"
  - "Add organization members"

**List**:
- Format: `"List <resources> [context]"`
- Examples:
  - "List available credentials"
  - "List compute environments in a workspace"
  - "List all datasets"

**Delete/Remove**:
- Format: `"Delete <resource> [qualifier]"`
- Examples:
  - "Delete a compute environment"
  - "Delete credentials from the workspace"
  - "Remove team members"

**Update/Modify**:
- Format: `"Update <resource> [what can be changed]"`
- Examples:
  - "Update dataset metadata"
  - "Update compute environment configuration"
  - "Modify workspace settings"

**View/Show**:
- Format: `"View <resource> [details]"`
- Examples:
  - "View compute environment details"
  - "View pipeline information"
  - "Show workspace configuration"

---

## Option Descriptions

### Format
- **Sentence case** (capitalize first word only, unless proper noun)
- **Present tense, descriptive voice**
- Use periods for complete sentences, omit for fragments
- Describe what the option does, not how to use it

### Good Examples
```
✅ "Use the credentials previously added to the workspace"
✅ "Provision a maximum of 256 CPUs"
✅ "Wait until the compute environment has been successfully created"
✅ "Specify the workspace ID (optional when TOWER_WORKSPACE_ID is set)"
```

### Bad Examples
```
❌ "The number of CPUs" (too vague, doesn't explain purpose)
❌ "Sets the max CPUs" (third person, code-doc style)
❌ "Use this to set the CPU limit" (instructional rather than descriptive)
❌ "cpu_max" (no description, just restating the flag name)
```

### Default Value Pattern
When documenting defaults:
```
✅ "Timeout in seconds (default: 300)"
✅ "Enable verbose logging (default: false)"
✅ "Output format: json, yaml, or table (default: table)"
```

### Required vs Optional
- Don't say "optional" in the description (the tool shows this automatically)
- For required fields: just describe what it does
- For conditionally required: "Required when..." or "Must be specified if..."

---

## Parameter Descriptions

### Format
- **Describe the expected value and its purpose**
- Include format hints for complex types
- Mention validation rules if relevant

### Good Examples
```
✅ "Pipeline repository name (organization/repository format)"
✅ "Compute environment ID or name"
✅ "Dataset ID (must be a valid UUID)"
✅ "Provider type: aws, azure, gcp, k8s"
```

---

## Terminology Standards

### Seqera Platform Concepts
Use exact Platform terminology:

| Correct term | INCORRECT |
|--------------|-----------|
| workspace | work space, work-space |
| compute environment | CE, compute-env, executor |
| credentials | credential, creds |
| pipeline | workflow, nextflow script |
| dataset | data set, data-set |
| organization | org (in descriptions) |
| data link | data-link, datalink |
| action | task, step |
| launch | run, execute, start |

### Resource Naming
- **Singular when referring to one**: "a compute environment"
- **Plural for listing**: "available credentials"
- **Use "the" for specific instances**: "the workspace", "the pipeline"

### Technical Terms
- Use full names on first reference: "Amazon Web Services (AWS)"
- Can abbreviate after: "AWS region"
- Platform-specific terms stay capitalized: "Tower", "Seqera Platform"

---

## Punctuation Rules

### Periods
- ✅ Use periods for complete sentences: "This command creates a new workspace."
- ✅ Use periods for multiple clauses: "Add credentials to the workspace. The credentials will be validated."
- ❌ No periods for fragments: "Add credentials to workspace" (not "Add credentials to workspace.")

### Parentheses
- Use for clarifications: "Workspace ID (optional if TOWER_WORKSPACE_ID is set)"
- Use for defaults: "Timeout in seconds (default: 300)"
- Use for format hints: "Repository name (org/repo format)"

### Hyphens
- Command names: `compute-envs`, `data-links` (follow CLI conventions)
- Compound adjectives: "AWS-specific configuration", "JSON-formatted output"

---

## Tone and Voice

### Writing Style
- **Professional but accessible**: Clear, direct language
- **No contractions**: Use "do not" instead of "don't"
- **Active voice preferred**: "Create a workspace" not "A workspace is created"
- **Imperative for commands**: "Add", "Delete", "List" (not "Adds", "Deletes", "Lists")

### Things to Avoid
- ❌ Marketing language: "powerful", "easy", "simply", "just"
- ❌ Redundancy: "delete and remove" (pick one)
- ❌ Vague qualifiers: "some", "various", "several"
- ❌ Apologetic tone: "Note that you must..." (just state it directly)

---

## Help Text References

### Standard Pattern
When referring users to help output:
```
"Run 'tw [command] -h' to view [available options]"
```

### Good Examples
```
✅ "Run 'tw credentials add -h' to view the required and optional fields"
✅ "Run 'tw compute-envs list -h' to view available filters"
✅ "Use -h flag to see all configuration options"
```

---

## Output Format Descriptions

### When documenting JSON output
```
✅ "Output in JSON format for programmatic access"
✅ "Return structured JSON for automation workflows"
✅ "Use --output=json for machine-readable output"
```

### When documenting table output
```
✅ "Display results in table format"
✅ "Show a summary table of available resources"
```

---

## Examples and Code Blocks

### Command Examples
- Use actual commands with realistic values
- Include expected output
- Show both success and error cases when relevant

### Format
```bash
# Good: realistic example with context
tw credentials add aws --name=my-aws-creds --access-key=AKIA... --secret-key=...

# Output
New AWS credentials 'my-aws-creds' added at workspace 'my-workspace'
```

---

## Hierarchy and Navigation

### Breadcrumb Style
For nested commands, use hierarchical notation:
```
tw credentials add <provider>
tw compute-envs add <platform>
tw teams members add
```

### Subcommand Descriptions
Parent commands that contain subcommands:
```
✅ "Manage workspace credentials"
✅ "Configure compute environments"
✅ "Administer team members"
```

---

## Special Cases

### Hidden Options
If an option is rarely used or advanced:
```
"Advanced: [description]" or "[Description] (advanced users only)"
```

### Deprecated Options
```
"[Description] (deprecated: use --new-flag instead)"
```

### Beta Features
```
"[Description] (beta feature, subject to change)"
```

### Environment Variables
```
"Can be set via TOWER_WORKSPACE_ID environment variable"
"Overrides the TOWER_API_ENDPOINT environment variable"
```

---

## Quality Checklist

Before finalizing any description, verify:

- [ ] Uses imperative verb form for commands
- [ ] Describes *what* it does, not *how* to use it
- [ ] Follows Seqera Platform terminology exactly
- [ ] Uses sentence case (not title case)
- [ ] Has appropriate punctuation (period for sentences, none for fragments)
- [ ] Is concise (1-2 sentences maximum)
- [ ] Contains no marketing fluff ("easy", "simply", "powerful")
- [ ] Uses active voice where possible
- [ ] Matches patterns from existing documentation
- [ ] Provides value beyond repeating the option name

---

## Validation Against This Guide

When reviewing descriptions:

1. **Read it aloud** - Does it sound natural?
2. **Compare to examples** - Does it match the patterns?
3. **Check terminology** - Are all Platform terms correct?
4. **Test comprehension** - Would a new user understand what this does?
5. **Verify consistency** - Does it match similar commands?

---

## References

- Primary source: https://docs.seqera.io/platform-cloud/cli/commands
- Platform terminology: https://docs.seqera.io/platform-cloud
- Style inspiration: Google Developer Documentation Style Guide
