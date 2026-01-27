# CLI Option Description Quality Standards

## Core Principles

1. **Technical Precision**: Avoid vague terms, specify data types, formats, and units
2. **Practical Guidance**: Include examples, references, prerequisites, and lookup hints
3. **Security & Safety**: Warn about data loss, flag sensitive fields, explain cascades
4. **Operational Context**: Clarify scope, defaults, special cases, and constraints
5. **Pattern Consistency**: Reuse standard descriptions for common options

## Before/After Examples

### Technical Precision

❌ **Before**: "Pipeline run identifier"
✅ **After**: "Pipeline run identifier. The unique workflow ID to display details for. Use additional flags to control which sections are shown."

❌ **Before**: "Organization role"
✅ **After**: "Organization role to assign. OWNER: full administrative access including member management and billing. MEMBER: standard access with ability to create workspaces and teams. COLLABORATOR: limited access, cannot create resources but can participate in shared workspaces."

### Practical Guidance

❌ **Before**: "User email address"
✅ **After**: "User email address to invite. If the user doesn't have a Seqera Platform account, they will receive an invitation email to join the organization."

❌ **Before**: "Team identifier"
✅ **After**: "Team numeric identifier. The unique ID assigned when the team was created. Find team IDs using 'tw teams list'."

### Security & Safety Context

❌ **Before**: "AWS secret key"
✅ **After**: "AWS secret access key. Part of AWS IAM credentials used for programmatic access to AWS services. Keep this value secure."

❌ **Before**: "Overwrite the organization if it already exists"
✅ **After**: "Overwrite existing organization. If an organization with this name already exists, delete it first before creating the new one. Use with caution as this permanently deletes the existing organization and all associated data."

## Standard Descriptions for Common Options

### Organization Reference
```java
@CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
public String organizationRef;
```

### Workspace Reference
```java
@CommandLine.Option(names = {"-w", "--workspace"}, description = "Workspace name or identifier. Specify either the workspace name or numeric identifier.")
public String workspace;
```

### Filter Options
```java
@CommandLine.Option(names = {"-f", "--filter"}, description = "Filter [entities] by [field] prefix. Case-insensitive prefix matching on the [field] field (e.g., 'example' matches 'example-name').")
public String filter;
```

### Overwrite Flags
```java
@CommandLine.Option(names = {"--overwrite"}, description = "Overwrite existing [entity]. If [entity] with this name already exists, delete it first before creating the new one. Use with caution as this permanently deletes the existing [entity] and all associated data.", defaultValue = "false")
public Boolean overwrite;
```

## Terminology Consistency

### Capitalization
- ✅ Nextflow (always capitalize)
- ✅ Seqera Platform
- ✅ Fusion (file system)
- ✅ AWS, Azure, GCP, K8s

### Word Choice
- ✅ workspace (one word)
- ✅ workflow run (not "pipeline execution")
- ✅ compute environment (not "compute-env" in descriptions)
- ✅ numeric ID (not "id number")

### Tone & Style
- Use present tense: "Displays statistics" not "Will display statistics"
- Use active voice: "Shows details" not "Details are shown"
- Be concise: 1-2 sentences per option is ideal
- End with periods for complete sentences

## Security & Safety Language

### Security Context
- "Keep this value secure"
- "Personal access tokens are recommended for security"
- "Used for authentication. Store securely."

### Data Loss Warnings
- "Use with caution as this permanently deletes [entity] and all associated data"
- "Cannot be undone except by [action]"
- "By default, only [safe state] can be [action]. Use this flag to [risky action]"
