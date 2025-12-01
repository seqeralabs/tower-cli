# Phase 6: Finalization Plan

**Objective:** Complete the Python rewrite, remove Java code, and finalize documentation.

---

## Summary of Gap Analysis

### Missing Commands

| Command | Status | Test Count | Priority |
|---------|--------|------------|----------|
| `data-links` | ❌ Missing | 15 Java tests | HIGH |

The `data-links` command includes:
- `list` - List data links in a workspace
- `add` - Add a new data link
- `delete` - Delete a data link
- `update` - Update a data link
- `browse` - Browse files in a data link
- `download` - Download files from a data link
- `upload` - Upload files to a data link (AWS, Azure, Google)

### Commands Verified Complete

All other commands have been implemented and tested:
- actions, collaborators, compute-envs, credentials
- datasets, info, labels, launch, members
- organizations, participants, pipelines, runs
- secrets, studios, teams, workspaces

### Test Coverage

- **Java tests:** ~299 test methods
- **Python tests:** 457 tests (more due to parameterization)
- **Missing tests:** ~45 (data-links × 3 output formats)

---

## Phase 6 Tasks

### Task 1: Implement data-links Command
**Estimated effort:** Medium-High (complex file upload/download)

1. Create `src/seqera/commands/datalinks/__init__.py`
2. Implement subcommands:
   - `list` - List data links with pagination and filtering
   - `add` - Add data link with provider/credentials
   - `delete` - Delete by ID
   - `update` - Update name/description/credentials
   - `browse` - Browse files with pagination
   - `download` - Download files/directories
   - `upload` - Multipart upload (AWS/Azure/Google variations)

3. Create response models in `src/seqera/responses/`
4. Port tests from `DataLinksCmdTest.java`

### Task 2: Remove Java CLI Code
**Estimated effort:** Low

Remove all Java source files while preserving:
- Test resource files (JSON fixtures used by both)
- Git history (no force push)

Files/directories to remove:
```
src/main/java/          # All Java source code
src/test/java/          # All Java tests
build.gradle            # Gradle build file
settings.gradle         # Gradle settings
gradlew, gradlew.bat    # Gradle wrappers
gradle/                 # Gradle wrapper files
tw                      # Java runner script
conf/                   # Java configuration files
.sdkmanrc               # SDKMan config for Java
jreleaser.yml           # Java release config
VERSION                 # Java version file
VERSION-API             # API version file
```

### Task 3: Update Documentation
**Estimated effort:** Low

1. Update `README.md`:
   - Remove Java installation instructions
   - Remove GraalVM native compilation section
   - Remove Java development sections
   - Keep Python as the only installation method
   - Update command examples to use `seqera` instead of `tw`

2. Update `USAGE.md`:
   - Review all examples for correctness
   - Ensure Python CLI compatibility

3. Update `PYTHON_DEVELOPMENT.md`:
   - Rename to `DEVELOPMENT.md`
   - Remove references to Java porting
   - Make it the primary development guide

4. Clean up progress reports:
   - Archive or remove `PYTHON_REWRITE_PROGRESS.md` after completion

### Task 4: Final Testing & Cleanup
**Estimated effort:** Low

1. Run full test suite
2. Verify all commands work end-to-end
3. Update pyproject.toml if needed
4. Clean up any remaining Java references
5. Final commit and push

---

## Execution Order

1. **Task 1: Implement data-links** (do first - most complex)
2. **Task 4: Run tests** (verify data-links works)
3. **Task 2: Remove Java code** (after Python is complete)
4. **Task 3: Update documentation** (after code removal)
5. **Final testing and commit**

---

## Risk Assessment

| Risk | Mitigation |
|------|------------|
| Upload/download complexity | Simplify initial implementation, add cloud-specific features incrementally |
| Breaking changes in API | Test against existing JSON fixtures |
| Missing edge cases | Port all Java tests faithfully |

---

## Success Criteria

- [ ] All 15+ data-links test cases ported and passing
- [ ] Total test count: ~500+ tests
- [ ] No Java source code remaining (except test fixtures)
- [ ] Documentation reflects Python-only CLI
- [ ] CLI command `seqera data-links --help` works
- [ ] All existing 457 tests still pass

---

**Created:** 2025-12-01
**Branch:** claude/resume-previous-session-018bzdwnTZzUSmxJHtRs7dk8
