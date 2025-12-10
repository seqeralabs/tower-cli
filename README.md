# Seqera Platform CLI & Python SDK

`seqera` is the official Python package for [Seqera Platform](https://cloud.seqera.io/), providing both a command-line interface and a programmatic SDK.

Built on the [Seqera Platform API](https://cloud.seqera.io/openapi/index.html). See the [Seqera Platform documentation](https://docs.seqera.io/platform/latest) to learn more.

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

### Commands

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

## Python SDK

```python
from seqera import Seqera

client = Seqera()

# List pipelines
for pipeline in client.pipelines.list():
    print(f"{pipeline.name}: {pipeline.repository}")

# Launch a pipeline
result = client.pipelines.launch("my-pipeline", workspace="my-org/my-workspace")

# Monitor runs
run = client.runs.get(result.workflow_id)
print(f"Status: {run.status}")
```

See [SDK.md](./SDK.md) for comprehensive SDK documentation.

## Development

```bash
pip install -e ".[dev]"
pytest tests/
ruff check src/ tests/
```

See [DEVELOPMENT.md](./DEVELOPMENT.md) for contribution guidelines.

## License

[Apache 2.0](https://github.com/seqeralabs/tower-cli/blob/master/LICENSE.txt)
