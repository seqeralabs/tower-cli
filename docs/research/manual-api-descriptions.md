# Manual API Descriptions for Missing Fields

**Date**: 2026-01-13

These 11 API schema field descriptions are missing from the OpenAPI spec and have been written manually to complete the CLI enrichment. These descriptions were applied directly to Java source files and should be added to the OpenAPI spec via an overlay for future consistency.

---

## 1. CreatePipelineSecretRequest.name

**Schema**: `CreatePipelineSecretRequest`
**Field**: `name`
**Type**: `string`

**Applied CLI Description**:
```
Secret name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters.
```

**Rationale**: Follows the standardized naming pattern established for all name fields. Clarifies uniqueness constraint and allowed characters.

**File**: `src/main/java/io/seqera/tower/cli/commands/secrets/AddCmd.java`

---

## 2. CreatePipelineSecretRequest.value

**Schema**: `CreatePipelineSecretRequest`
**Field**: `value`
**Type**: `string`

**Applied CLI Description**:
```
Secret value, to be stored securely. The secret is made available to pipeline executions at runtime.
```

**Rationale**: Explains what happens to the value and how it's used in pipelines.

**File**: `src/main/java/io/seqera/tower/cli/commands/secrets/AddCmd.java`

---

## 3. UpdatePipelineSecretRequest.value

**Schema**: `UpdatePipelineSecretRequest`
**Field**: `value`
**Type**: `string`

**Applied CLI Description**:
```
New secret value, to be stored securely. The secret is made available to pipeline executions at runtime.
```

**Rationale**: Clarifies this is an update operation (uses "New") and maintains consistency with create operation.

**File**: `src/main/java/io/seqera/tower/cli/commands/secrets/UpdateCmd.java`

---

## 4. CreateDatasetRequest.name

**Schema**: `CreateDatasetRequest`
**Field**: `name`
**Type**: `string`

**Applied CLI Description**:
```
Dataset name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters.
```

**Rationale**: Follows the standardized naming pattern. Clarifies uniqueness scope and naming rules.

**File**: `src/main/java/io/seqera/tower/cli/commands/datasets/AddCmd.java`

---

## 5. CreateDatasetRequest.description

**Schema**: `CreateDatasetRequest`
**Field**: `description`
**Type**: `string`

**Applied CLI Description**:
```
Optional dataset description.
```

**Rationale**: Clarifies it's optional. Concise and consistent with CLI style.

**File**: `src/main/java/io/seqera/tower/cli/commands/datasets/AddCmd.java`

---

## 6. UpdateDatasetRequest.name

**Schema**: `UpdateDatasetRequest`
**Field**: `name`
**Type**: `string`

**Applied CLI Description**:
```
Updated dataset name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters.
```

**Rationale**: Follows the standardized update naming pattern (uses "Updated" prefix). Maintains uniqueness constraint and naming rules.

**File**: `src/main/java/io/seqera/tower/cli/commands/datasets/UpdateCmd.java`

---

## 7. UpdateDatasetRequest.description

**Schema**: `UpdateDatasetRequest`
**Field**: `description`
**Type**: `string`

**Applied CLI Description**:
```
Updated dataset description.
```

**Rationale**: Clarifies this is an update operation and maintains consistency with create operation.

**File**: `src/main/java/io/seqera/tower/cli/commands/datasets/UpdateCmd.java`

---

## 8. UpdateWorkspaceRequest.name

**Schema**: `UpdateWorkspaceRequest`
**Field**: `name`
**Type**: `string`

**Applied CLI Description**:
```
Updated workspace name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters. Must be 2-40 characters.
```

**Rationale**: Follows the standardized update naming pattern with workspace-specific character length constraint (2-40 characters).

**File**: `src/main/java/io/seqera/tower/cli/commands/workspaces/UpdateCmd.java`

---

## 9. UpdateWorkspaceRequest.fullName

**Schema**: `UpdateWorkspaceRequest`
**Field**: `fullName`
**Type**: `string`

**Applied CLI Description**:
```
Updated full display name for the workspace. Maximum 100 characters.
```

**Rationale**: Clarifies this is a display name (vs short name) and provides length constraint.

**File**: `src/main/java/io/seqera/tower/cli/commands/workspaces/UpdateCmd.java`

---

## 10. UpdateWorkspaceRequest.description

**Schema**: `UpdateWorkspaceRequest`
**Field**: `description`
**Type**: `string`

**Applied CLI Description**:
```
Updated workspace description. Maximum 1000 characters.
```

**Rationale**: Provides length constraint consistent with workspace schema.

**File**: `src/main/java/io/seqera/tower/cli/commands/workspaces/UpdateCmd.java`

---

## 11. UpdateActionRequest.name

**Schema**: `UpdateActionRequest`
**Field**: `name`
**Type**: `string`

**Applied CLI Description**:
```
Updated action name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters.
```

**Rationale**: Follows the standardized update naming pattern. Clarifies uniqueness scope and naming rules.

**File**: `src/main/java/io/seqera/tower/cli/commands/actions/UpdateCmd.java`

---

## Summary Table

| Schema | Field | Applied CLI Description | Status |
|--------|-------|------------------------|--------|
| CreatePipelineSecretRequest | name | Secret name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters. | ✅ Applied |
| CreatePipelineSecretRequest | value | Secret value, to be stored securely. The secret is made available to pipeline executions at runtime. | ✅ Applied |
| UpdatePipelineSecretRequest | value | New secret value, to be stored securely. The secret is made available to pipeline executions at runtime. | ✅ Applied |
| CreateDatasetRequest | name | Dataset name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters. | ✅ Applied |
| CreateDatasetRequest | description | Optional dataset description. | ✅ Applied |
| UpdateDatasetRequest | name | Updated dataset name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters. | ✅ Applied |
| UpdateDatasetRequest | description | Updated dataset description. | ✅ Applied |
| UpdateWorkspaceRequest | name | Updated workspace name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters. Must be 2-40 characters. | ✅ Applied |
| UpdateWorkspaceRequest | fullName | Updated full display name for the workspace. Maximum 100 characters. | ✅ Applied |
| UpdateWorkspaceRequest | description | Updated workspace description. Maximum 1000 characters. | ✅ Applied |
| UpdateActionRequest | name | Updated action name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters. | ✅ Applied |

---

## Standardized Naming Pattern

All name fields now follow this consistent pattern:

**For AddCmd/Create operations**:
```
<Entity> name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters.
```

**For UpdateCmd/Update operations**:
```
Updated <entity> name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters.
```

**Special cases**:
- Workspace names add: "Must be 2-40 characters."
- Description fields use "Optional" for Add, "Updated" for Update

---

## OpenAPI Overlay Instructions

Create an overlay file (e.g., `manual-field-descriptions-overlay.yaml`) with the following content:

```yaml
overlay: 1.0.0
info:
  title: Manual Field Descriptions Overlay
  version: 1.0.0
actions:
  - target: "$.components.schemas.CreatePipelineSecretRequest.properties.name"
    update:
      description: "Unique name for the pipeline secret within the workspace. Used to reference the secret in workflow configurations."

  - target: "$.components.schemas.CreatePipelineSecretRequest.properties.value"
    update:
      description: "The secret value to be securely stored. This value will be made available to pipeline executions as an environment variable or configuration value."

  - target: "$.components.schemas.UpdatePipelineSecretRequest.properties.value"
    update:
      description: "New secret value to replace the existing value. The secret value is securely stored and made available to pipeline executions."

  - target: "$.components.schemas.CreateDatasetRequest.properties.name"
    update:
      description: "Unique dataset name within the workspace. Must be a valid identifier that can be referenced in workflow configurations."

  - target: "$.components.schemas.CreateDatasetRequest.properties.description"
    update:
      description: "Optional description of the dataset. Provides context about the dataset contents, source, or intended use."

  - target: "$.components.schemas.UpdateDatasetRequest.properties.name"
    update:
      description: "New name for the dataset. Must be unique within the workspace and can be used to reference the dataset in workflow configurations."

  - target: "$.components.schemas.UpdateDatasetRequest.properties.description"
    update:
      description: "Updated description for the dataset. Provides context about the dataset contents, source, or intended use."

  - target: "$.components.schemas.UpdateWorkspaceRequest.properties.name"
    update:
      description: "New short name for the workspace. Must be 2-40 characters, start and end with alphanumeric characters, and can contain hyphens or underscores between characters. Must be unique within the organization."

  - target: "$.components.schemas.UpdateWorkspaceRequest.properties.fullName"
    update:
      description: "Updated full display name for the workspace. Maximum 100 characters."

  - target: "$.components.schemas.UpdateWorkspaceRequest.properties.description"
    update:
      description: "Updated description of the workspace. Maximum 1000 characters."

  - target: "$.components.schemas.UpdateActionRequest.properties.name"
    update:
      description: "New name for the pipeline action. Must be unique within the workspace."
```

---

## Notes

- All descriptions follow the same quality standards as existing enriched descriptions
- Uniqueness constraints are explicitly stated where applicable
- Length/character constraints are documented (based on workspace schema patterns)
- Update operations clarify that they're providing "new" or "updated" values
- Descriptions explain purpose and usage context, not just field names
- Consistent terminology with existing API documentation
