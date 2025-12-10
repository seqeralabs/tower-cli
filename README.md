# Seqera Platform CLI & Python SDK

`seqera` is the official Python package for [Seqera Platform](https://cloud.seqera.io/), providing both a command-line interface and a programmatic SDK.

- **CLI**: Manage pipelines, runs, and infrastructure from the terminal
- **SDK**: Integrate Seqera Platform into Python applications and scripts

Built on the [Seqera Platform API](https://cloud.seqera.io/openapi/index.html).

See the [Seqera Platform documentation](https://docs.seqera.io/platform/latest) to learn more.

## Installation

Requires Python 3.10+. Compatible with [Seqera Cloud](https://cloud.seqera.io/) and Enterprise 21.08+.

```bash
pip install seqera
```

## Configuration

Create an access token in the [Seqera UI](https://cloud.seqera.io) under **User tokens**.

```bash
export SEQERA_ACCESS_TOKEN=<your token>
```

| Variable | Description | Default |
|----------|-------------|---------|
| `SEQERA_ACCESS_TOKEN` | API access token | (required) |
| `SEQERA_API_ENDPOINT` | API URL | `https://api.cloud.seqera.io` |
| `SEQERA_WORKSPACE` | Default workspace | User workspace |

## Python SDK

```python
from seqera import Seqera

client = Seqera()  # uses SEQERA_ACCESS_TOKEN

# List pipelines
for pipeline in client.pipelines.list():
    print(f"{pipeline.name}: {pipeline.repository}")

# Launch a pipeline
result = client.pipelines.launch(
    "my-pipeline",
    workspace="my-org/my-workspace",
    params={"input": "s3://bucket/samples.csv"},
)
print(f"Run ID: {result.workflow_id}")

# Monitor runs
run = client.runs.get(result.workflow_id)
print(f"Status: {run.status}")

# List tasks for a run
for task in client.runs.tasks(run.id):
    print(f"{task.name}: {task.status}")
```

### Async support

```python
from seqera import AsyncSeqera
import asyncio

async def main():
    async with AsyncSeqera() as client:
        async for run in client.runs.list():
            print(f"{run.run_name}: {run.status}")

asyncio.run(main())
```

### Available resources

| Resource | Methods |
|----------|---------|
| `client.pipelines` | `list`, `get`, `add`, `update`, `delete`, `launch` |
| `client.runs` | `list`, `get`, `cancel`, `delete`, `tasks`, `metrics` |
| `client.compute_envs` | `list`, `get`, `delete`, `get_primary`, `set_primary` |
| `client.credentials` | `list`, `add_*`, `delete` |
| `client.workspaces` | `list`, `get`, `add`, `update`, `delete` |
| `client.datasets` | `list`, `get`, `add`, `update`, `delete` |
| `client.secrets` | `list`, `get`, `add`, `update`, `delete` |
| `client.labels` | `list`, `add`, `delete` |

### Error handling

```python
from seqera import Seqera, PipelineNotFoundException, AuthenticationError

client = Seqera()

try:
    pipeline = client.pipelines.get("nonexistent")
except PipelineNotFoundException:
    print("Pipeline not found")
except AuthenticationError:
    print("Invalid token")
```

## CLI

```bash
seqera --help
```

Verify connection:

```console
$ seqera info

    System health status
    ---------------------------------------+------------------
     Remote API server connection check    | OK
     Seqera Platform API version check     | OK
     Authentication API credential's token | OK
```

### CLI commands

```
pipelines      Manage workspace pipelines
runs           Manage pipeline runs
compute-envs   Manage compute environments
credentials    Manage workspace credentials
workspaces     Manage workspaces
datasets       Manage datasets
secrets        Manage workspace secrets
labels         Manage labels
organizations  Manage organizations
teams          Manage teams
```

See [USAGE.md](./USAGE.md) for detailed CLI documentation.

## Development

```bash
pip install -e ".[dev]"
pytest tests/
ruff check src/ tests/
```

See [DEVELOPMENT.md](./DEVELOPMENT.md) for contribution guidelines.

## License

[Apache 2.0](https://github.com/seqeralabs/tower-cli/blob/master/LICENSE.txt)
