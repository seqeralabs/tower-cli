# Seqera Platform Python SDK

Programmatic access to the Seqera Platform API.

## Installation

```bash
pip install seqera
```

## Quick Start

```python
from seqera import Seqera

client = Seqera()  # uses SEQERA_ACCESS_TOKEN env var

# List pipelines
for pipeline in client.pipelines.list():
    print(f"{pipeline.name}: {pipeline.repository}")

# Launch a pipeline
result = client.pipelines.launch("my-pipeline")
print(f"Run ID: {result.workflow_id}")
```

## Client Initialization

```python
from seqera import Seqera

# From environment variables (recommended)
client = Seqera()

# Explicit configuration
client = Seqera(
    access_token="tw_xxxx",
    url="https://api.cloud.seqera.io",
    workspace="my-org/my-workspace",  # default workspace
)

# Context manager (auto-closes connection)
with Seqera() as client:
    pipelines = list(client.pipelines.list())
```

### Environment Variables

| Variable | Description |
|----------|-------------|
| `SEQERA_ACCESS_TOKEN` | API access token (required) |
| `SEQERA_API_ENDPOINT` | API URL (default: `https://api.cloud.seqera.io`) |
| `SEQERA_WORKSPACE` | Default workspace (`org/workspace` format) |

Legacy `TOWER_*` variables are also supported.

## Pagination

List methods return auto-paginating iterators:

```python
# Iterate (fetches pages automatically)
for pipeline in client.pipelines.list():
    print(pipeline.name)

# Collect all
all_pipelines = list(client.pipelines.list())

# First N items
from itertools import islice
first_10 = list(islice(client.pipelines.list(), 10))
```

## Workspaces

All operations accept an optional `workspace` parameter. If omitted, uses the default workspace.

```python
# Use default workspace
client.pipelines.list()

# Specify workspace by reference
client.pipelines.list(workspace="my-org/production")

# Specify workspace by ID
client.pipelines.list(workspace=12345)
```

---

## Pipelines

Manage pre-configured workflows.

### List pipelines

```python
for pipeline in client.pipelines.list():
    print(f"{pipeline.pipeline_id}: {pipeline.name}")

# With search filter
for pipeline in client.pipelines.list(search="rnaseq"):
    print(pipeline.name)
```

### Get pipeline

```python
# By ID
pipeline = client.pipelines.get(123)

# By name
pipeline = client.pipelines.get_by_name("rnaseq-nf")

print(f"Repository: {pipeline.repository}")
print(f"Description: {pipeline.description}")
```

### Add pipeline

```python
pipeline = client.pipelines.add(
    name="my-rnaseq",
    repository="https://github.com/nf-core/rnaseq",
    description="RNA-seq analysis pipeline",
    revision="3.12.0",
    compute_env="aws-batch",
    work_dir="s3://my-bucket/work",
    params={"input": "samples.csv", "outdir": "results"},
    config_profiles=["docker"],
    labels=["production", "rnaseq"],
)
```

### Update pipeline

```python
pipeline = client.pipelines.update(
    pipeline_id=123,
    description="Updated description",
    revision="3.13.0",
    params={"input": "new_samples.csv"},
)
```

### Delete pipeline

```python
client.pipelines.delete(123)
```

### Launch pipeline

```python
# Basic launch
result = client.pipelines.launch("my-rnaseq")
print(f"Workflow ID: {result.workflow_id}")

# With parameters
result = client.pipelines.launch(
    "my-rnaseq",
    workspace="my-org/production",
    params={"input": "s3://bucket/samples.csv"},
    compute_env="aws-batch-spot",
    work_dir="s3://bucket/work",
    revision="main",
    config_profiles=["docker", "test"],
    run_name="rnaseq-batch-001",
    labels=["batch", "Q4-2024"],
    pre_run_script="echo 'Starting'",
    post_run_script="echo 'Done'",
    pull_latest=True,
    resume=False,
    stub_run=False,
)
```

### Export pipeline config

```python
config = client.pipelines.export_config(123)
print(config)  # dict with pipeline configuration
```

---

## Runs

Monitor and manage workflow executions.

### List runs

```python
for run in client.runs.list():
    print(f"{run.run_name}: {run.status}")

# With search filter
for run in client.runs.list(search="rnaseq"):
    print(run.id)
```

### Get run

```python
run = client.runs.get("abc123")

print(f"Run: {run.run_name}")
print(f"Status: {run.status}")
print(f"Started: {run.start}")
print(f"Work dir: {run.work_dir}")
```

### Cancel run

```python
client.runs.cancel("abc123")
```

### Delete run

```python
client.runs.delete("abc123")
```

### List tasks

```python
for task in client.runs.tasks("abc123"):
    print(f"{task.name}: {task.status}")

# With search
for task in client.runs.tasks("abc123", search="FASTQC"):
    print(f"{task.task_id}: {task.exit_code}")
```

### Get metrics

```python
metrics = client.runs.metrics("abc123")
print(metrics)  # dict with process metrics
```

### Get progress

```python
progress = client.runs.progress("abc123")
print(progress)  # dict with workflow progress
```

### Get single task

```python
task = client.runs.get_task("abc123", task_id=1)
print(f"Task: {task.name}")
print(f"Status: {task.status}")
print(f"Exit code: {task.exit_code}")
```

### Relaunch run

```python
# Relaunch with resume (default)
new_workflow_id = client.runs.relaunch("abc123")
print(f"New workflow: {new_workflow_id}")

# Relaunch without resume
new_workflow_id = client.runs.relaunch(
    "abc123",
    resume=False,
    work_dir="s3://bucket/new-work",
    compute_env="different-ce",
)
```

### Dump run details

Get all data needed to create a dump/archive of a workflow run:

```python
dump_data = client.runs.dump(
    "abc123",
    add_task_logs=True,
    add_fusion_logs=True,
    only_failed=False,
)
# Returns dict with: workflow, tasks, metrics, progress, task_logs
```

---

## Compute Environments

Manage execution platforms.

### List compute environments

```python
for ce in client.compute_envs.list():
    print(f"{ce.name}: {ce.platform} ({ce.status})")

# Only available environments
for ce in client.compute_envs.list(status="AVAILABLE"):
    print(ce.name)
```

### Get compute environment

```python
# By ID
ce = client.compute_envs.get("abc123")

# By name
ce = client.compute_envs.get("aws-batch-prod")

print(f"Platform: {ce.platform}")
print(f"Status: {ce.status}")
```

### Get/Set primary

```python
# Get primary compute environment
primary = client.compute_envs.get_primary()
if primary:
    print(f"Primary: {primary.name}")

# Set primary
client.compute_envs.set_primary("abc123")
```

### Delete compute environment

```python
client.compute_envs.delete("abc123")
```

### Export config

```python
config = client.compute_envs.export_config("aws-batch-prod")
```

### Import config

```python
# Import from config dict
config = {"name": "new-ce", "platform": "aws-batch", ...}
ce_id = client.compute_envs.import_config(config)

# Import with name override
ce_id = client.compute_envs.import_config(config, name="custom-name")

# Import with overwrite (deletes existing CE with same name)
ce_id = client.compute_envs.import_config(config, overwrite=True)
```

---

## Credentials

Manage authentication credentials for cloud providers and repositories.

### List credentials

```python
for cred in client.credentials.list():
    print(f"{cred.name}: {cred.provider}")
```

### Add AWS credentials

```python
creds = client.credentials.add_aws(
    name="my-aws-creds",
    access_key="AKIA...",
    secret_key="...",
    description="Production AWS credentials",
    assume_role_arn="arn:aws:iam::123456789:role/MyRole",  # optional
)
```

### Add Azure credentials

```python
creds = client.credentials.add_azure(
    name="my-azure-creds",
    batch_name="mybatchaccount",
    batch_key="...",
    storage_name="mystorageaccount",
    storage_key="...",
)
```

### Add Google Cloud credentials

```python
creds = client.credentials.add_google(
    name="my-gcp-creds",
    key_file="/path/to/service-account.json",  # or JSON string
)
```

### Add GitHub credentials

```python
creds = client.credentials.add_github(
    name="my-github-creds",
    username="myuser",
    password="ghp_xxxx",  # personal access token
)
```

### Add GitLab credentials

```python
creds = client.credentials.add_gitlab(
    name="my-gitlab-creds",
    username="myuser",
    password="mypassword",
    token="glpat-xxxx",
)
```

### Add container registry credentials

```python
creds = client.credentials.add_container_registry(
    name="dockerhub",
    username="myuser",
    password="...",
    registry="docker.io",  # or ghcr.io, quay.io, etc.
)
```

### Add SSH credentials

```python
creds = client.credentials.add_ssh(
    name="my-ssh-key",
    private_key="/path/to/id_rsa",  # or key content
    passphrase="optional-passphrase",
)
```

### Add Kubernetes credentials

```python
# With service account token
creds = client.credentials.add_k8s(
    name="my-k8s-creds",
    token="eyJhbGc...",
)

# With certificate
creds = client.credentials.add_k8s(
    name="my-k8s-creds",
    certificate="/path/to/cert.pem",
    private_key="/path/to/key.pem",
)
```

### Add Tower Agent credentials

```python
creds = client.credentials.add_agent(
    name="my-agent",
    connection_id="abc123",
    work_dir="/scratch/work",
)
```

### Delete credentials

```python
client.credentials.delete("credentials-id")
```

---

## Workspaces

Manage collaborative environments.

### List workspaces

```python
for ws in client.workspaces.list():
    print(f"{ws.org_name}/{ws.workspace_name}")

# Filter by organization
for ws in client.workspaces.list(organization="my-org"):
    print(ws.workspace_name)
```

### Get workspace

```python
# By reference
ws = client.workspaces.get("my-org/my-workspace")

# By ID
ws = client.workspaces.get(12345)

print(f"Name: {ws.name}")
print(f"Visibility: {ws.visibility}")
```

### Add workspace

```python
ws = client.workspaces.add(
    name="new-workspace",
    organization="my-org",
    full_name="New Workspace",
    description="A workspace for testing",
    visibility="PRIVATE",  # or "SHARED"
)
```

### Update workspace

```python
ws = client.workspaces.update(
    "my-org/my-workspace",
    description="Updated description",
    visibility="SHARED",
)
```

### Delete workspace

```python
client.workspaces.delete("my-org/my-workspace")
```

### Leave workspace

```python
client.workspaces.leave("my-org/my-workspace")
```

---

## Secrets

Manage pipeline secrets.

### List secrets

```python
for secret in client.secrets.list():
    print(secret.name)
```

### Add secret

```python
secret = client.secrets.add(
    name="MY_SECRET",
    value="secret-value",
)
```

### Delete secret

```python
client.secrets.delete("secret-id")
```

---

## Labels

Manage resource labels.

### List labels

```python
for label in client.labels.list():
    print(f"{label.name}: {label.value}")
```

### Add label

```python
label = client.labels.add(
    name="environment",
    value="production",
)
```

### Update label

```python
# Update name
label = client.labels.update(label_id=123, name="new-name")

# Update value (for resource labels)
label = client.labels.update(label_id=123, value="new-value")
```

### Delete label

```python
client.labels.delete(label_id=123)
```

### Manage labels on workflows

```python
# Apply labels (replaces existing)
client.labels.apply_to_workflows(
    label_ids=[123, 456],
    workflow_ids=["abc123", "def456"],
)

# Add labels (appends to existing)
client.labels.add_to_workflows(
    label_ids=[123, 456],
    workflow_ids=["abc123"],
)

# Remove labels
client.labels.remove_from_workflows(
    label_ids=[123],
    workflow_ids=["abc123"],
)
```

### Manage labels on pipelines

```python
# Apply labels (replaces existing)
client.labels.apply_to_pipelines(
    label_ids=[123, 456],
    pipeline_ids=[1, 2, 3],
)

# Add/remove work the same as workflows
client.labels.add_to_pipelines(label_ids=[123], pipeline_ids=[1])
client.labels.remove_from_pipelines(label_ids=[123], pipeline_ids=[1])
```

### Manage labels on actions

```python
client.labels.apply_to_actions(label_ids=[123], action_ids=["action-1"])
client.labels.add_to_actions(label_ids=[123], action_ids=["action-1"])
client.labels.remove_from_actions(label_ids=[123], action_ids=["action-1"])
```

---

## Datasets

Manage versioned data inputs.

### List datasets

```python
for ds in client.datasets.list():
    print(f"{ds.name}: {ds.description}")
```

### Get dataset

```python
ds = client.datasets.get("dataset-id")
```

### Add dataset

```python
ds = client.datasets.add(
    name="samples",
    description="Sample sheet",
    header=True,
)
```

### Delete dataset

```python
client.datasets.delete("dataset-id")
```

---

## Organizations

Manage organizations.

### List organizations

```python
for org in client.organizations.list():
    print(f"{org.name}: {org.full_name}")
```

### Get organization

```python
org = client.organizations.get("my-org")
```

---

## Teams

Manage organization teams.

### List teams

```python
for team in client.teams.list(organization="my-org"):
    print(team.name)
```

### Add team

```python
team = client.teams.add(
    name="developers",
    organization="my-org",
    description="Development team",
)
```

### Delete team

```python
client.teams.delete(team_id=123, organization="my-org")
```

---

## Members

Manage organization members.

### List members

```python
for member in client.members.list(organization="my-org"):
    print(f"{member.user_name}: {member.role}")
```

### Add member

```python
client.members.add(
    user="user@example.com",
    organization="my-org",
)
```

---

## Participants

Manage workspace participants.

### List participants

```python
for p in client.participants.list(workspace="my-org/my-workspace"):
    print(f"{p.user_name}: {p.role}")
```

### Add participant

```python
client.participants.add(
    user="user@example.com",
    workspace="my-org/my-workspace",
    role="LAUNCH",  # ADMIN, MAINTAIN, LAUNCH, VIEW
)
```

---

## Actions

Manage pipeline automation.

### List actions

```python
for action in client.actions.list():
    print(f"{action.name}: {action.event}")
```

---

## Studios

Manage Data Studios interactive environments.

### List studios

```python
for studio in client.studios.list():
    print(f"{studio.name}: {studio.status}")
```

### Start/Stop studio

```python
client.studios.start("studio-id")
client.studios.stop("studio-id")
```

### List templates

```python
templates = client.studios.templates()
for template in templates:
    print(f"{template['id']}: {template['name']}")
```

---

## Data Links

Manage cloud storage connections.

### List data links

```python
for link in client.data_links.list():
    print(f"{link.name}: {link.resource_ref}")
```

### Add data link

```python
link = client.data_links.add(
    name="my-bucket",
    provider="aws",
    resource_ref="s3://my-bucket",
    credentials_id="creds-id",  # optional for public buckets
    description="My data bucket",
)
```

### Update data link

```python
link = client.data_links.update(
    data_link_id="v1-user-abc123",
    name="renamed-bucket",
    description="Updated description",
)
```

### Delete data link

```python
client.data_links.delete("v1-user-abc123")
```

### Browse data link

```python
# List root contents
files = client.data_links.browse("v1-user-abc123")
for f in files:
    print(f"{f['type']}: {f['name']}")

# List subdirectory
files = client.data_links.browse("v1-user-abc123", path="data/samples/")
```

### Download files

Get a presigned URL and download a file:

```python
import httpx

# Get presigned download URL
url = client.data_links.get_download_url(
    data_link_id="v1-user-abc123",
    path="data/sample.fastq.gz",
    credentials_id="creds-id",
)

# Download the file
with httpx.stream("GET", url) as response:
    response.raise_for_status()
    with open("sample.fastq.gz", "wb") as f:
        for chunk in response.iter_bytes():
            f.write(chunk)
```

Download with progress tracking:

```python
import httpx
from pathlib import Path

def download_file(client, data_link_id: str, remote_path: str,
                  local_path: str, credentials_id: str):
    """Download a file from a data link with progress."""
    url = client.data_links.get_download_url(
        data_link_id=data_link_id,
        path=remote_path,
        credentials_id=credentials_id,
    )

    with httpx.stream("GET", url) as response:
        response.raise_for_status()
        total = int(response.headers.get("content-length", 0))

        with open(local_path, "wb") as f:
            downloaded = 0
            for chunk in response.iter_bytes(chunk_size=8192):
                f.write(chunk)
                downloaded += len(chunk)
                if total:
                    print(f"\rDownloading: {downloaded}/{total} bytes "
                          f"({100*downloaded/total:.1f}%)", end="")
        print()  # newline after progress

# Usage
download_file(
    client,
    data_link_id="v1-user-abc123",
    remote_path="results/analysis.csv",
    local_path="./analysis.csv",
    credentials_id="my-aws-creds",
)
```

### Upload files

Upload a small file (< 5GB) using a presigned URL:

```python
import httpx
from pathlib import Path

def upload_file(client, data_link_id: str, local_path: str,
                remote_path: str, credentials_id: str):
    """Upload a file to a data link."""
    # Determine content type
    import mimetypes
    content_type = mimetypes.guess_type(local_path)[0] or "application/octet-stream"

    # Get presigned upload URL
    result = client.data_links.get_upload_url(
        data_link_id=data_link_id,
        path=remote_path,
        credentials_id=credentials_id,
        content_type=content_type,
    )

    # Upload the file
    with open(local_path, "rb") as f:
        response = httpx.put(
            result["url"],
            content=f,
            headers={"Content-Type": content_type},
        )
        response.raise_for_status()

    print(f"Uploaded {local_path} to {remote_path}")

# Usage
upload_file(
    client,
    data_link_id="v1-user-abc123",
    local_path="./results.csv",
    remote_path="data/results/results.csv",
    credentials_id="my-aws-creds",
)
```

Upload large files using multipart upload (for files > 5MB):

```python
import httpx
import math
from pathlib import Path

PART_SIZE = 100 * 1024 * 1024  # 100 MB per part

def upload_large_file(client, data_link_id: str, local_path: str,
                      remote_path: str, credentials_id: str):
    """Upload a large file using multipart upload."""
    file_size = Path(local_path).stat().st_size
    num_parts = math.ceil(file_size / PART_SIZE)

    # Initiate multipart upload (first request without part_number)
    init_result = client.data_links.get_upload_url(
        data_link_id=data_link_id,
        path=remote_path,
        credentials_id=credentials_id,
    )
    upload_id = init_result.get("uploadId")

    # Upload each part
    parts = []
    with open(local_path, "rb") as f:
        for part_num in range(1, num_parts + 1):
            chunk = f.read(PART_SIZE)

            # Get presigned URL for this part
            part_result = client.data_links.get_upload_url(
                data_link_id=data_link_id,
                path=remote_path,
                credentials_id=credentials_id,
                part_number=part_num,
                upload_id=upload_id,
            )

            # Upload the part
            response = httpx.put(part_result["url"], content=chunk)
            response.raise_for_status()

            # Store ETag for completion
            etag = response.headers.get("ETag")
            parts.append({"PartNumber": part_num, "ETag": etag})

            print(f"Uploaded part {part_num}/{num_parts}")

    # Complete multipart upload (implementation depends on cloud provider)
    print(f"Upload complete: {remote_path}")
    return parts

# Usage
upload_large_file(
    client,
    data_link_id="v1-user-abc123",
    local_path="./large_dataset.bam",
    remote_path="data/alignments/large_dataset.bam",
    credentials_id="my-aws-creds",
)
```

Download multiple files from a directory:

```python
def download_directory(client, data_link_id: str, remote_dir: str,
                       local_dir: str, credentials_id: str):
    """Download all files from a data link directory."""
    from pathlib import Path
    import httpx

    Path(local_dir).mkdir(parents=True, exist_ok=True)

    # Browse the remote directory
    files = client.data_links.browse(data_link_id, path=remote_dir)

    for item in files:
        if item["type"] == "FILE":
            remote_path = f"{remote_dir}/{item['name']}".lstrip("/")
            local_path = Path(local_dir) / item["name"]

            # Get download URL and download
            url = client.data_links.get_download_url(
                data_link_id=data_link_id,
                path=remote_path,
                credentials_id=credentials_id,
            )

            response = httpx.get(url)
            response.raise_for_status()
            local_path.write_bytes(response.content)

            print(f"Downloaded: {item['name']}")

        elif item["type"] == "FOLDER":
            # Recursively download subdirectories
            subdir = f"{remote_dir}/{item['name']}".lstrip("/")
            download_directory(
                client, data_link_id, subdir,
                str(Path(local_dir) / item["name"]),
                credentials_id,
            )

# Usage
download_directory(
    client,
    data_link_id="v1-user-abc123",
    remote_dir="results/2024-01/",
    local_dir="./downloaded_results",
    credentials_id="my-aws-creds",
)
```

---

## Error Handling

```python
from seqera import (
    Seqera,
    SeqeraError,
    ApiError,
    AuthenticationError,
    NotFoundError,
    PipelineNotFoundException,
    RunNotFoundException,
    WorkspaceNotFoundException,
    CredentialsNotFoundException,
    ComputeEnvNotFoundException,
)

client = Seqera()

try:
    pipeline = client.pipelines.get(999999)
except PipelineNotFoundException as e:
    print(f"Pipeline not found: {e}")
except AuthenticationError:
    print("Invalid or expired token")
except ApiError as e:
    print(f"API error: {e.status_code} - {e.message}")
except SeqeraError as e:
    print(f"Error: {e}")
```

---

## Async Support

```python
from seqera import AsyncSeqera
import asyncio

async def main():
    async with AsyncSeqera() as client:
        # List pipelines
        async for pipeline in client.pipelines.list():
            print(pipeline.name)

        # List runs
        async for run in client.runs.list():
            print(f"{run.run_name}: {run.status}")

asyncio.run(main())
```

---

## Type Hints

All SDK methods are fully typed. Models are available for import:

```python
from seqera import (
    # Client
    Seqera,
    AsyncSeqera,
    # Models
    Pipeline,
    LaunchResult,
    Workflow,
    Task,
    Workspace,
    ComputeEnv,
    Credentials,
    Secret,
    Label,
    Dataset,
    Team,
    Member,
    Participant,
    Action,
    Studio,
    DataLink,
    # Pagination
    PaginatedList,
)

def process_pipeline(pipeline: Pipeline) -> None:
    print(f"Processing {pipeline.name}")

client = Seqera()
for pipeline in client.pipelines.list():
    process_pipeline(pipeline)
```

---

## API Reference

| Resource | Methods |
|----------|---------|
| `client.pipelines` | `list`, `get`, `get_by_name`, `add`, `update`, `delete`, `launch`, `get_launch_info`, `export_config` |
| `client.runs` | `list`, `get`, `get_task`, `cancel`, `delete`, `relaunch`, `dump`, `tasks`, `metrics`, `progress` |
| `client.compute_envs` | `list`, `get`, `get_by_name`, `delete`, `get_primary`, `set_primary`, `export_config`, `import_config` |
| `client.credentials` | `list`, `get`, `add_aws`, `add_azure`, `add_google`, `add_github`, `add_gitlab`, `add_gitea`, `add_bitbucket`, `add_codecommit`, `add_container_registry`, `add_ssh`, `add_k8s`, `add_agent`, `update`, `delete` |
| `client.workspaces` | `list`, `get`, `add`, `update`, `delete`, `leave` |
| `client.secrets` | `list`, `get`, `add`, `update`, `delete` |
| `client.labels` | `list`, `add`, `update`, `delete`, `apply_to_workflows`, `add_to_workflows`, `remove_from_workflows`, `apply_to_pipelines`, `add_to_pipelines`, `remove_from_pipelines`, `apply_to_actions`, `add_to_actions`, `remove_from_actions` |
| `client.datasets` | `list`, `get`, `add`, `update`, `delete` |
| `client.organizations` | `list`, `get`, `add`, `update`, `delete` |
| `client.teams` | `list`, `get`, `add`, `update`, `delete` |
| `client.members` | `list`, `add`, `update`, `delete`, `leave` |
| `client.participants` | `list`, `add`, `update`, `delete` |
| `client.actions` | `list`, `get`, `add`, `update`, `delete` |
| `client.studios` | `list`, `get`, `delete`, `start`, `stop`, `checkpoints`, `templates` |
| `client.collaborators` | `list`, `add`, `delete` |
| `client.data_links` | `list`, `get`, `add`, `update`, `delete`, `browse`, `get_download_url`, `get_upload_url` |
| `client.info()` | Get API info |
| `client.user_info()` | Get current user info |
