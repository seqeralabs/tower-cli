# Seqera Platform CLI

`seqera` is the command line interface for [Seqera Platform](https://cloud.seqera.io/). It brings Seqera concepts like pipelines, actions, and compute environments to the terminal.

Seqera Platform is a full-stack application for the management of data pipelines and compute resources. It enables collaborative data analysis at scale, on-premises or in any cloud.

The CLI interacts with Seqera to provide an interface to launch pipelines, manage cloud resources, and administer your analysis.

![tw](assets/img/tw-screenshot.png)

The key features are:

- **A Nextflow-like experience**: The CLI provides a developer-friendly environment. Pipelines can be launched similarly to Nextflow but with the Seqera benefits of monitoring, logging, resource provisioning, dataset management, and collaborative sharing.

- **Infrastructure as Code**: All Seqera resources, including pipelines and compute environments, can be described in a declarative manner. This enables a complete definition of an analysis environment that can be versioned and treated as code.

- **Built on OpenAPI**: The CLI interacts with Seqera via the [Seqera Platform API](https://cloud.seqera.io/openapi/index.html) which uses the OpenAPI 3.0 specification.

See the [Seqera Platform documentation](https://docs.seqera.io/platform/latest) to learn more about the application.

## Availability

The Seqera CLI can be installed on macOS, Windows, and Linux.

It is compatible with [Seqera Cloud](https://cloud.seqera.io/) and Enterprise versions 21.08 and later.

## Getting Started

### 1. Installation

#### Using pip (recommended)

The CLI requires Python 3.10 or later:

```bash
pip install seqera-cli
```

Or install from source:

```bash
git clone https://github.com/seqeralabs/tower-cli.git
cd seqera-cli
pip install .
```

After installation, the `seqera` command will be available:

```bash
seqera --help
```

### 2. Configuration

You need an access token for the CLI to interact with your Seqera instance. Select **User tokens** from the user menu in the [Seqera UI](https://cloud.seqera.io), then select **Add token** to create a new token.

Copy the access token value and use it with the CLI in one of two ways:

- **Environment variable** (recommended):

    ```bash
    export SEQERA_ACCESS_TOKEN=<your access token>
    ```

    Add this to your `.bashrc`, `.zshrc`, or `.bash_profile` for it to persist across sessions.

- **Command flag**:

    ```bash
    seqera --access-token=<your access token> <command>
    ```

#### Additional Configuration

Configure the following optional environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SEQERA_ACCESS_TOKEN` | Access token for authentication | (required) |
| `SEQERA_API_ENDPOINT` | Seqera API URL | `https://api.cloud.seqera.io` |
| `SEQERA_WORKSPACE_ID` | Default workspace ID | User workspace |
| `SEQERA_OUTPUT_FORMAT` | Output format: console, json, yaml | console |

> **Note**: Legacy `TOWER_*` environment variables are also supported for backwards compatibility.

### 3. Health check

Confirm the installation, configuration, and connection:

```console
$ seqera info

    System health status
    ---------------------------------------+------------------
     Remote API server connection check    | OK
     Seqera Platform API version check     | OK
     Authentication API credential's token | OK
```

## Commands

See [Usage](./USAGE.md) for detailed instructions to use the CLI.

### Available Commands

```text
seqera actions        Manage pipeline actions
seqera collaborators  List organization collaborators
seqera compute-envs   Manage compute environments
seqera credentials    Manage workspace credentials
seqera data-links     Manage data links
seqera datasets       Manage datasets
seqera info           System info and health status
seqera labels         Manage labels
seqera launch         Launch a pipeline
seqera members        Manage organization members
seqera organizations  Manage organizations
seqera participants   Manage workspace participants
seqera pipelines      Manage pipelines
seqera runs           Manage pipeline runs
seqera secrets        Manage workspace secrets
seqera studios        Manage data studios
seqera teams          Manage teams
seqera workspaces     Manage workspaces
```

## Development

See [Development Guide](./DEVELOPMENT.md) for information on contributing to the CLI.

### Running Tests

```bash
pip install -e ".[dev]"
pytest tests/
```

### Code Quality

```bash
ruff check src/ tests/
ruff format src/ tests/
mypy src/seqera
```

## License

[Apache 2.0](https://github.com/seqeralabs/tower-cli/blob/master/LICENSE.txt)
