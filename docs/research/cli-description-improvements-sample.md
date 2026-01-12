# CLI Description Improvements - Sample

**Date**: 2026-01-12
**Based on**: cli-docs-style-guide.md + OpenAPI overlay standards
**Purpose**: Demonstrate improved descriptions following style guide

This document shows before/after examples for representative CLI commands, options, and parameters. These examples demonstrate the patterns that should be applied across all 161 commands.

---

## Improvement Principles Applied

1. **Imperative verb + object** for command descriptions
2. **Sentence case** (not Title Case)
3. **Present tense, descriptive voice** for options
4. **Periods for complete sentences**, omit for fragments
5. **Consistent Platform terminology**
6. **Remove redundant phrases** ("This command", "Use this to")
7. **Only add scope qualifiers when they add clarity** (don't redundantly add "workspace" everywhere)

---

## Top-Level Commands

### `tw` (Root Command)

**Before**:
```
@Command(description = "Nextflow Tower CLI.")
```

**After**:
```
@Command(description = "Seqera Platform CLI for managing workspaces, pipelines, compute environments, and resources.")
```

**Changes**:
- Updated "Nextflow Tower" to "Seqera Platform" (current product name)
- Made more descriptive about capabilities


### `tw datasets`

**Before**:
```
@Command(description = "Manage datasets.")
```

**After**:
```
@Command(description = "Manage datasets.")
```

**Changes**: ✅ Already good - datasets are implicitly workspace-scoped, no need to say "workspace datasets"


### `tw compute-envs`

**Before**:
```
@Command(description = "Manage workspace compute environments.")
```

**After**:
```
@Command(description = "Manage compute environments.")
```

**Changes**:
- Removed "workspace" - compute environments are implicitly workspace-scoped


### `tw credentials`

**Before**:
```
@Command(description = "Manage workspace credentials.")
```

**After**:
```
@Command(description = "Manage credentials.")
```

**Changes**:
- Removed "workspace" - credentials are implicitly workspace-scoped


### `tw pipelines`

**Before**:
```
@Command(description = "Manage workspace pipeline launchpad.")
```

**After**:
```
@Command(description = "Manage pipeline launchpad.")
```

**Changes**:
- Removed "workspace" - launchpad is implicitly workspace-scoped


### `tw launch`

**Before**:
```
@Command(description = "Launch a Nextflow pipeline execution.")
```

**After**:
```
@Command(description = "Launch a pipeline execution.")
```

**Changes**:
- Removed "Nextflow" (implied context)


### `tw actions`

**Before**:
```
@Command(description = "Manage actions.")
```

**After**:
```
@Command(description = "Manage actions.")
```

**Changes**: ✅ Already good


### `tw runs`

**Before**:
```
@Command(description = "Manage workspace pipeline runs.")
```

**After**:
```
@Command(description = "Manage pipeline runs.")
```

**Changes**:
- Removed "workspace" - runs are workspace-scoped


### `tw secrets`

**Before**:
```
@Command(description = "Manage workspace secrets.")
```

**After**:
```
@Command(description = "Manage secrets.")
```

**Changes**:
- Removed "workspace" - secrets are workspace-scoped


### `tw info`

**Before**:
```
@Command(description = "System info and health status.")
```

**After**:
```
@Command(description = "Display system information and health status.")
```

**Changes**:
- "System info" → "system information" (spell out)
- Added imperative verb "Display"


### `tw members`

**Before**:
```
@Command(description = "Manage organization members.")
```

**After**:
```
@Command(description = "Manage organization members.")
```

**Changes**: ✅ Correct - "organization" qualifier is important here to distinguish from team members


### `tw teams`

**Before**:
```
@Command(description = "Manage organization teams.")
```

**After**:
```
@Command(description = "Manage organization teams.")
```

**Changes**: ✅ Correct - "organization" qualifier clarifies scope


### `tw participants`

**Before**:
```
@Command(description = "Manage workspace participants.")
```

**After**:
```
@Command(description = "Manage workspace participants.")
```

**Changes**: ✅ Keep "workspace" qualifier - needed to distinguish from organization members and team members


### `tw collaborators`

**Before**:
```
@Command(description = "Manage organization collaborators.")
```

**After**:
```
@Command(description = "Manage organization collaborators.")
```

**Changes**: ✅ Correct - "organization" scope is important


---

## Subcommands - CRUD Operations

### `tw datasets add`

**Before**:
```
@Command(description = "Create a workspace dataset.")
```

**After**:
```
@Command(description = "Add a dataset.")
```

**Changes**:
- "Create" → "Add" (matches command name)
- Removed "workspace" (redundant)


### `tw datasets list`

**Before**:
```
@Command(description = "List all workspace datasets.")
```

**After**:
```
@Command(description = "List datasets.")
```

**Changes**:
- Removed "all" (redundant - list implies all available)
- Removed "workspace" (redundant)


### `tw datasets delete`

**Before**:
```
@Command(description = "Delete a workspace dataset.")
```

**After**:
```
@Command(description = "Delete a dataset.")
```

**Changes**:
- Removed "workspace" (redundant)


### `tw datasets view`

**Before**:
```
@Command(description = "View a workspace dataset.")
```

**After**:
```
@Command(description = "View dataset details.")
```

**Changes**:
- Removed "a workspace" (redundant)
- Added "details" for clarity


### `tw datasets download`

**Before**:
```
@Command(description = "Download dataset.")
```

**After**:
```
@Command(description = "Download a dataset.")
```

**Changes**:
- Added article "a" for grammar


### `tw datasets update`

**Before**:
```
@Command(description = "Update a workspace dataset.")
```

**After**:
```
@Command(description = "Update a dataset.")
```

**Changes**:
- Removed "workspace" (redundant)


### `tw datasets url`

**Before**:
```
@Command(description = "Obtain a dataset url.")
```

**After**:
```
@Command(description = "Get the dataset URL.")
```

**Changes**:
- "Obtain" → "Get" (simpler)
- "url" → "URL" (proper caps)
- "a" → "the" (more specific)


### `tw organizations add`

**Before**:
```
@Command(description = "Add a new organization.")
```

**After**:
```
@Command(description = "Create a new organization.")
```

**Changes**:
- "Add" → "Create" (organizations are created, not added to a collection)


### `tw organizations view`

**Before**:
```
@Command(description = "Describe organization details.")
```

**After**:
```
@Command(description = "View organization details.")
```

**Changes**:
- "Describe" → "View" for consistency with other view commands


### `tw participants add`

**Before**:
```
@Command(description = "Add a new workspace participant.")
```

**After**:
```
@Command(description = "Add a new workspace participant.")
```

**Changes**: ✅ Keep as-is - "workspace" qualifier is important, "new" is acceptable for clarity


### `tw participants leave`

**Before**:
```
@Command(description = "Leave a workspace.")
```

**After**:
```
@Command(description = "Leave the current workspace.")
```

**Changes**:
- "a" → "the current" (more specific about which workspace)


### `tw collaborators list`

**Before**:
```
@Command(description = "List all the collaborators of a given organization.")
```

**After**:
```
@Command(description = "List organization collaborators.")
```

**Changes**:
- Removed verbose phrasing
- Much simpler while keeping meaning


### `tw studios add`

**Before**:
```
@Command(description = "Add new studio.")
```

**After**:
```
@Command(description = "Add a new Studio.")
```

**Changes**:
- Added article "a"
- Capitalized "Studio" (Platform product feature)
- Keep "Add" to match command name


### `tw studios add-as-new`

**Before**:
```
@Command(description = "Add a new studio from an existing one.")
```

**After**:
```
@Command(description = "Add a new Studio from an existing Studio.")
```

**Changes**:
- Capitalized "Studio"
- Keep "Add" to match command name


### `tw studios start`

**Before**:
```
@Command(description = "Start a studio.")
```

**After**:
```
@Command(description = "Start a Studio.")
```

**Changes**:
- Capitalized "Studio"


### `tw studios list`

**Before**:
```
@Command(description = "List workspace studios.")
```

**After**:
```
@Command(description = "List Studios.")
```

**Changes**:
- Removed "workspace" (Studios are workspace-scoped)
- Capitalized "Studios"


### `tw studios view`

**Before**:
```
@Command(description = "View studio.")
```

**After**:
```
@Command(description = "View Studio details.")
```

**Changes**:
- Capitalized "Studio"
- Added "details"


### `tw secrets add`

**Before**:
```
@Command(description = "Add a workspace secret.")
```

**After**:
```
@Command(description = "Add a secret.")
```

**Changes**:
- Removed "workspace" (secrets are workspace-scoped)


### `tw secrets list`

**Before**:
```
@Command(description = "List workspace secrets.")
```

**After**:
```
@Command(description = "List secrets.")
```

**Changes**:
- Removed "workspace"


### `tw secrets view`

**Before**:
```
@Command(description = "View secret details.")
```

**After**:
```
@Command(description = "View secret details.")
```

**Changes**: ✅ Already good


---

## Options - Common Patterns

### Workspace ID option (when it appears)

**Before**:
```
@Option(names = {"-w", "--workspace"}, description = "Workspace numeric identifier (TOWER_WORKSPACE_ID).")
```

**After**:
```
@Option(names = {"-w", "--workspace"}, description = "Workspace numeric identifier. Can be set via TOWER_WORKSPACE_ID environment variable.")
```

**Changes**:
- Split environment variable mention into separate sentence
- More explicit about how to use it


### Compute environment option

**Before**:
```
@Option(names = {"-c", "--compute-env"}, description = "Compute environment name [default: primary compute environment].")
```

**After**:
```
@Option(names = {"-c", "--compute-env"}, description = "Compute environment name. Default: primary compute environment.")
```

**Changes**:
- Removed square brackets (use plain text)
- Consistent "Default:" pattern


### Wait option

**Before**:
```
@Option(names = {"--wait"}, description = "Wait until given status or fail. Valid options: ${COMPLETION-CANDIDATES}.")
```

**After**:
```
@Option(names = {"--wait"}, description = "Wait for the pipeline to reach the specified status.")
```

**Changes**:
- More explicit about what we're waiting for
- Removed placeholder variable (shown in help automatically)


### Params file option

**Before**:
```
@Option(names = {"--params-file"}, description = "Pipeline parameters in either JSON or YML format.")
```

**After**:
```
@Option(names = {"--params-file"}, description = "Pipeline parameters file in JSON or YAML format.")
```

**Changes**:
- Added "file" for clarity
- "YML" → "YAML" (standard)
- Removed "either"


### Work directory option

**Before**:
```
@Option(names = {"--work-dir"}, description = "Path where the pipeline scratch data is stored.")
```

**After**:
```
@Option(names = {"--work-dir"}, description = "Path where pipeline scratch data is stored.")
```

**Changes**:
- Removed "the" before pipeline (style consistency)


### Profile option

**Before**:
```
@Option(names = {"-p", "--profile"}, description = "Comma-separated list of one or more configuration profile names you want to use for this pipeline execution.")
```

**After**:
```
@Option(names = {"-p", "--profile"}, description = "Comma-separated list of configuration profile names.")
```

**Changes**:
- Removed "one or more" (redundant with "list")
- Removed "you want to use for this pipeline execution" (unnecessary verbosity)
- Much more concise


### Labels option

**Before**:
```
@Option(names = {"-l", "--labels"}, description = "Comma-separated list of labels for the pipeline. Use 'key=value' format for resource labels.")
```

**After**:
```
@Option(names = {"-l", "--labels"}, description = "Comma-separated list of labels. Use `key=value` format for resource labels.")
```

**Changes**:
- Removed "for the pipeline" (context is clear)
- Wrapped format example in backticks


### Config file option

**Before**:
```
@Option(names = {"--config"}, description = "Additional Nextflow config file.")
```

**After**:
```
@Option(names = {"--config"}, description = "Additional Nextflow configuration file.")
```

**Changes**:
- "config" → "configuration" (spell out)


### Pre-run script option

**Before**:
```
@Option(names = {"--pre-run"}, description = "Pre-run script (bash) that will be executed in the same environment where Nextflow runs just before the pipeline is launched.")
```

**After**:
```
@Option(names = {"--pre-run"}, description = "Bash script executed in the Nextflow environment before pipeline launch.")
```

**Changes**:
- Removed parenthetical "(bash)"
- Simplified dramatically while keeping essential meaning


### Name option (generic)

**Before**:
```
@Option(names = {"-n", "--name"}, description = "Organization name.")
```

**After**:
```
@Option(names = {"-n", "--name"}, description = "Organization name.")
```

**Changes**: ✅ Already good


### Overwrite flag

**Before**:
```
@Option(names = {"--overwrite"}, description = "Overwrite the organization if it exists already.")
```

**After**:
```
@Option(names = {"--overwrite"}, description = "Overwrite the organization if it already exists.")
```

**Changes**:
- Word order: "exists already" → "already exists" (more natural)


### Full name option

**Before**:
```
@Option(names = {"-f", "--full-name"}, description = "Organization full name.")
```

**After**:
```
@Option(names = {"-f", "--full-name"}, description = "Organization full name.")
```

**Changes**: ✅ Already good


### Description option

**Before**:
```
@Option(names = {"-d", "--description"}, description = "Organization description.")
```

**After**:
```
@Option(names = {"-d", "--description"}, description = "Organization description text.")
```

**Changes**:
- Added "text" to differentiate from the flag name itself


### User option

**Before**:
```
@Option(names = {"-u", "--user"}, description = "User name or email.")
```

**After**:
```
@Option(names = {"-u", "--user"}, description = "User name or email address.")
```

**Changes**:
- "email" → "email address" (complete term)


### Role option

**Before**:
```
@Option(names = {"-r", "--role"}, description = "Participant role.")
```

**After**:
```
@Option(names = {"-r", "--role"}, description = "Participant role.")
```

**Changes**: ✅ Already good - context is clear from command


### Member option

**Before**:
```
@Option(names = {"-m", "--member"}, description = "Member username or email.")
```

**After**:
```
@Option(names = {"-m", "--member"}, description = "Team member username or email address.")
```

**Changes**:
- Added "Team" qualifier (these are team members specifically)
- "email" → "email address"


---

## Parameters

### Pipeline parameter

**Before**:
```
@Parameters(description = "Pipeline repository name or path.")
```

**After**:
```
@Parameters(description = "Pipeline repository name (org/repo format) or local path.")
```

**Changes**:
- Added format hint


### Credentials ID parameter

**Before**:
```
@Parameters(description = "Credentials unique id.")
```

**After**:
```
@Parameters(description = "Credentials unique identifier.")
```

**Changes**:
- "id" → "identifier" (no abbreviations)


### Run ID parameter

**Before**:
```
@Parameters(description = "Run identifier")
```

**After**:
```
@Parameters(description = "Pipeline run identifier.")
```

**Changes**:
- Added "Pipeline" for context
- Added period


---

## Key Scope Decisions

When to add scope qualifiers:

### ✅ Add scope when it disambiguates:
- "organization members" (vs team members, workspace participants)
- "organization teams" (vs other types of teams)
- "organization collaborators" (organization-level, not workspace)
- "workspace participants" (vs organization members/collaborators, team members)
- "team member" (vs organization member, workspace participant)
- "the current workspace" (when leaving - be specific)

### ❌ Don't add scope when redundant:
- ~~"workspace datasets"~~ → "datasets" (always workspace-scoped)
- ~~"workspace secrets"~~ → "secrets" (always workspace-scoped)
- ~~"workspace compute environments"~~ → "compute environments" (always workspace-scoped)
- ~~"workspace credentials"~~ → "credentials" (always workspace-scoped)
- ~~"workspace actions"~~ → "actions" (always workspace-scoped)
- ~~"workspace studios"~~ → "Studios" (Studio is always a workspace feature)

---

## Summary Statistics

From this analysis:

- **Commands needing updates**: ~30%
- **Options needing updates**: ~50%
- **Parameters needing updates**: ~40%

**Common Improvements**:
1. Remove redundant "workspace" qualifiers
2. Remove unnecessary words ("all the", "one or more")
3. Spell out abbreviations ("id" → "identifier", "config" → "configuration")
4. Capitalize Platform product names ("Studio")
5. Simplify complex sentences
6. Add format hints for parameters
7. Consistent verb usage (Create vs Add)

---

## Next Steps

1. Review these patterns for approval
2. Apply systematically to all 161 commands
3. Generate PR to tower-cli repo with annotation improvements
