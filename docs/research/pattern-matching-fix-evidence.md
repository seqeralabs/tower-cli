# Pattern Matching Fix - Evidence

**Date**: 2026-01-13
**Issue**: apply-descriptions.py couldn't match @CommandLine.Option annotations, only @Option
**Impact**: 4 enriched options in workspaces/AddCmd not applied

---

## Problem Statement

The apply-descriptions.py script used a regex pattern that only matched `@Option` annotations:

```python
pattern = (
    r'(@Option\s*\(\s*'              # @Option(
    r'names\s*=\s*\{' + names_pattern + r'\}'
    ...
)
```

However, some command files use the fully qualified annotation: `@CommandLine.Option`

Example from workspaces/AddCmd.java:
```java
@CommandLine.Option(names = {"-n", "--name"}, description = "Workspace short name...")
```

**Result**: Pattern didn't match → 4 enriched options skipped

---

## Solution

Updated the regex pattern to support both forms using a non-capturing optional group:

```python
pattern = (
    r'(@(?:CommandLine\.)?Option\s*\(\s*'    # @Option( or @CommandLine.Option(
    r'names\s*=\s*\{' + names_pattern + r'\}'
    ...
)
```

**Regex Breakdown**:
- `@` - Literal @ symbol
- `(?:CommandLine\.)?` - Non-capturing group for optional "CommandLine."
  - `(?:...)` - Non-capturing group (doesn't create a match group)
  - `CommandLine\.` - Literal "CommandLine." (dot is escaped)
  - `?` - Make the entire group optional (0 or 1 occurrences)
- `Option\s*\(\s*` - "Option" followed by optional whitespace and opening paren

**Matches Both**:
- `@Option(`
- `@CommandLine.Option(`

---

## Additional Fix: Fully Qualified Name Support

Also updated the update_command method to accept fully qualified names:

```python
def update_command(self, command_class_name: str) -> Dict:
    """Update a specific command by class name or fully qualified name."""
    # First try exact match (fully qualified name)
    if command_class_name in self.metadata.get("commands", {}):
        qualified_name = command_class_name
    else:
        # Try matching by simple class name (suffix)
        ...
```

This allows calling the script with:
```bash
python apply-descriptions.py --command "io.seqera.tower.cli.commands.workspaces.AddCmd"
```

Instead of just:
```bash
python apply-descriptions.py --command "AddCmd"  # Ambiguous - multiple AddCmd classes
```

---

## Testing

### Dry Run Test

```bash
python apply-descriptions.py --command "io.seqera.tower.cli.commands.workspaces.AddCmd" --dry-run
```

**Result**:
```
Processing: add (AddCmd.java)
  ✓ Updated --name
  ✓ Updated --full-name
  ✓ Updated --description
  ✓ Updated --visibility

[DRY RUN] Would update 4 options
```

### Actual Application

```bash
python apply-descriptions.py --command "io.seqera.tower.cli.commands.workspaces.AddCmd"
```

**Result**:
```
Commands processed: 1
Files updated: 1
Options updated: 4
Options skipped: 2
```

---

## Before/After Comparisons

### Option 1: `--name`

**Before**:
```java
@CommandLine.Option(names = {"-n", "--name"}, description = "Workspace short name (only alphanumeric, dash, and underscore characters allowed)", required = true)
```

**After**:
```java
@CommandLine.Option(names = {"-n", "--name"}, description = "Unique workspace name within the organization. Must be 2-40 characters, start and end with alphanumeric characters, and can contain hyphens or underscores between characters.", required = true)
```

**Improvements**:
- ✅ Explicit uniqueness constraint
- ✅ Character length limits (2-40)
- ✅ Start/end character requirements
- ✅ More precise wording

---

### Option 2: `--full-name`

**Before**:
```java
@CommandLine.Option(names = {"-f", "--full-name"}, description = "Workspace full name", required = true)
```

**After**:
```java
@CommandLine.Option(names = {"-f", "--full-name"}, description = "Full display name for the workspace. Maximum 100 characters.", required = true)
```

**Improvements**:
- ✅ Clarifies purpose (display name)
- ✅ Documents maximum length constraint

---

### Option 3: `--description`

**Before**:
```java
@CommandLine.Option(names = {"-d", "--description"}, description = "Workspace description")
```

**After**:
```java
@CommandLine.Option(names = {"-d", "--description"}, description = "Optional description of the workspace. Maximum 1000 characters.")
```

**Improvements**:
- ✅ Explicitly notes it's optional
- ✅ Documents maximum length constraint

---

### Option 4: `--visibility`

**Before**:
```java
@CommandLine.Option(names = {"-v", "--visibility"}, description = "Workspace visibility: PRIVATE or SHARED (default: PRIVATE)")
```

**After**:
```java
@CommandLine.Option(names = {"-v", "--visibility"}, description = "Workspace visibility setting. Accepts `PRIVATE` (only participants can access) or `SHARED` (all organization members can view).")
```

**Improvements**:
- ✅ Clearer structure ("Accepts ... or ...")
- ✅ Explains what each value means
- ✅ Uses markdown backticks for enum values (rendered in help)

---

## Impact Summary

**Options Unlocked**: +4 (from 22 to 26 total applied)

**Files Updated**: 1 (workspaces/AddCmd.java)

**Coverage Improvement**:
- Before: 22/27 enriched options applied (81.5%)
- After: 26/27 enriched options applied (96.3%)

**Remaining Gap**: 1 option (pipelines/AddCmd --labels in mixin class)

---

## Git Changes

```bash
git diff src/main/java/io/seqera/tower/cli/commands/workspaces/AddCmd.java
```

**Statistics**:
- Lines changed: 8 (4 insertions, 4 deletions)
- Options updated: 4
- No syntax errors introduced

---

## Validation

### Code Quality
- ✅ All @CommandLine.Option annotations syntactically correct
- ✅ All option attributes preserved
- ✅ No escaped characters broken
- ✅ Formatting preserved

### Functionality
- ✅ Script runs without errors
- ✅ All 4 options successfully found and updated
- ✅ Pattern matching now handles both annotation styles
- ✅ Fully qualified name lookup working

---

## Files Modified

1. **docs/scripts/apply-descriptions.py**:
   - Line 190: Updated regex pattern to support @CommandLine.Option
   - Lines 62-78: Enhanced update_command to accept fully qualified names

2. **src/main/java/io/seqera/tower/cli/commands/workspaces/AddCmd.java**:
   - Lines 43, 46, 49, 52: Updated 4 option descriptions

---

## Success Criteria Met

| Criterion | Status |
|-----------|--------|
| **Pattern matches @CommandLine.Option** | ✅ |
| **All 4 workspaces/AddCmd options updated** | ✅ |
| **No syntax errors** | ✅ |
| **Code formatting preserved** | ✅ |
| **Supports fully qualified names** | ✅ |

---

## Next Steps

**Remaining Gap**: 1 option (pipelines/AddCmd --labels)
- Defined in mixin class LabelsOptionalOptions
- Requires mixin option support (separate fix)

**Ready For**: Commit and move to next fix (nested API fields or mixin support)
