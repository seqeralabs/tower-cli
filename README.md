# Nextflow Tower CLI

`tw` is [Tower](https://landing.tower.nf/) (a.k.a Nextflow Tower) on the command line. It brings Tower concepts including Pipelines, Actions and Compute Environments to the terminal.

Tower is a full-stack application for the management of data pipelines and compute resources. It enables collaborative data analysis at scale, on-premises or in any cloud infrastructure.

The Tower CLI (a.k.a Nextflow Tower CLI) interacts with instances of Tower, providing an interface to launch pipelines, manage cloud resources and administer the application directly from the terminal.

![tw](assets/img/tw-screenshot.png)

The key features of the Tower CLI are:

- **A Nextflow-like experience**: Tower CLI provides a developer friendly environment. Pipelines can be launched on the CLI as with Nextflow but utilizing all of the benefits of Tower such as monitoring, logging, resource provisioning, dataset management and collaborative sharing.

- **Infrastructure as Code**: All Tower resources including Pipelines and Compute Environments can be described in a declarative manner. This allows a complete definition of an analysis environment that can be versioned and treated as code. It greatly simplifies sharing and re-use of configuration as well as routine administration.

- **Built on OpenAPI**: Tower CLI interacts with Tower via the [Tower API](https://tower.nf/openapi/index.html) which is created using the latest OpenAPI 3.0 specification. Tower CLI provides full control of the Tower application allowing users to get maximum insights into their pipeline submissions and execution environments.

For more information on Tower, see the [user documentation](https://help.tower.nf) on the Tower Cloud website.

# Availability

Tower CLI can be installed on macOS, Windows, and Linux.

It is compatible with [Tower Cloud](https://tower.nf/) and Tower Enterprise versions 21.08 and later.

# Getting Started

This guide covers the installation and configuration of the CLI, cloud infrastructure provisioning and launching pipelines into an AWS Batch compute environment.

## 1. Installation

1. Download the latest version for your OS from the assets in the [releases](https://github.com/seqeralabs/tower-cli/releases) page.

2. Unzip the binary file:

    ```bash
    unzip tw*.zip
    ```

3. Make the file executable:

    ```bash
    chmod +x ./tw
    ```

4. Move the file to a directory accessible by your `$PATH` variable:

    ```bash
    sudo mv tw /usr/local/bin/
    ```

## 2. Configuration

If you haven't got one already, create a Tower access token using the [Tower](https://tower.nf/) web interface via the **Your Tokens** page in your profile.

It is mandatory to provide the Tower access to `tw` and it can be made available in a number of ways:

1. Export it directly into your terminal before you run `tw`:

    ```bash
    export TOWER_ACCESS_TOKEN=<your access token>
    ```

2. Add the above `export` command to a file such as `.bashrc` which means that it will be automatically injected into your environment whenever you open a terminal.

3. Specify it as a parameter when running `tw`:

    ```bash
    tw --access-token=<your access token> <other options>
    ```

If required, you can also set the following non-mandatory environment variables using one of the methods outlined above:

- `TOWER_WORKSPACE_ID`: Workspace id. Defaults to the user workspace.
- `TOWER_API_ENDPOINT`: Tower API URL. Default `api.tower.nf`.

> You can find the `TOWER_WORKSPACE_ID` for a given organisation in its **Workspaces** page as highlighted in [this image](assets/img/workspace_id.png).

## 3. Health check

Confirm the installation, configuration and connection is working as expected:

```console
$ tw health

    System health status
    ---------------------------------------+------------------
     Remote API server connection check    | OK
     Tower API version check               | OK
     Authentication API credential's token | OK
```

## 4. View available commands

Use the `-h` or `--help` parameter to list the available commands and their associated options.

```bash
tw --help
```

## 5. Add credentials

To launch pipelines on AWS Batch, you will need to add credentials to the appropriate Tower Workspace.

```bash
tw \
    credentials \
    create \
    aws \
    --name=my_aws_creds \
    --access-key=<aws access key> \
    --secret-key=<aws secret key>
```

> See the [IAM policy](https://github.com/seqeralabs/nf-tower-aws/tree/master/forge) for Tower Forge for recommendations on AWS Batch permissions.

## 6. List credentials

List the credentials available in the workspace.

```bash
tw credentials list
```

## 7. Provision a Compute Environment

Create a Compute Environment for AWS Batch with automatic provisioning of cloud computing resources:

```bash
tw \
    compute-envs \
    create \
    aws-batch \
    forge \
    --name=my_aws_ce \
    --region=eu-west-1 \
    --max-cpus=256 \
    --work-dir=s3://<bucket name>
```

The above command will create all of the required AWS Batch resources in the AWS Ireland (`eu-west-1`) region with a total of 256 CPUs provisioned in the compute environment. An existing S3 bucket will be used as the work directory when running Nextflow.

More comprehensive details about Tower Forge can be obtained from the [user documentation](https://help.tower.nf/compute-envs/aws-batch/#forge).

> If you have multiple credentials matching the same compute environment then you will need to provide the `--credentials-id` obtained by running `tw credentials list`.

## 8. Create a pipeline

Create a pre-configured pipeline that can be re-used later:

```bash
tw \
    pipelines \
    create \
    --name=my_sleepy_pipeline \
    --params=<(echo 'timeout: 60') \
    https://github.com/pditommaso/nf-sleep
```

Pipelines consists of a pipeline repository, launch parameters, and a Compute Environment. When no Compute Environment is specified, the primary one is used.

> The `params` option should be a YAML or JSON file. Here we use a Bash pipe to convert a command into a YAML file automatically.

## 9. Launch it!

```bash
tw launch sleepy-flow
```

## 10. Change launch parameters

Launch the pipeline with different parameters.

```bash
tw launch sleepy-flow --params=<(echo 'timeout: 30') 
```

## 11. Update a pipeline

Pipelines can be modified with new launch parameters using the `update` command.

```bash
tw pipelines update -n sleepy-flow --params=<(echo 'timeout: 30')
```

## 12. Launch a pipeline directly

It possible to directly launch Git pipelines that have not been added to a workspace as Pipelines.

```bash
tw launch nextflow-io/hello
```

# Launch Examples

The `tw launch` command provides a similar user experience to `nextflow run` with the benefits of using Tower.

1. Run a workspace Pipeline with a custom parameters file.

    ```bash
    tw launch sarek --params ./myparams.yaml
    ```

2. Run any Nextflow pipeline using the primary Compute Environment.

    ```bash
    tw launch nextflow-io/hello
    ```

3. Run any Nextflow pipeline setting a profile.

    ```bash
    tw launch nf-core/sarek --profile test,docker --params ./myparams.yaml
    ```

4. Select a specific Compute Environment.

    ```bash
    tw launch nf-core/sarek --compute-env "aws-ce" --profile test,docker
    ```

# Activate autocomplete

It is possible to activate autocomplete in your current session.

```bash
source <(tw generate-completion)
```

# Build binary development versions

Tower CLI is based on a Java GraalVM native compilation to produce a platform binary executable.

1. Download GraalVM (Java 11 version) from [this link](https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-20.2.0).

2. Install `native-image` tool:

    ```bash
    gu install native-image
    ```

3. Create the native client:

    ```bash
    ./gradlew nativeImage
    ```

4. then run

    ```bash
    ./build/graal/tw
    ```

# Using non-binary development versions

You can run a non-binary development version running the `./tw` script in the root of this repository.

# License

[Mozilla Public License v2.0](https://github.com/seqeralabs/tower-cli/blob/master/LICENSE.txt)
