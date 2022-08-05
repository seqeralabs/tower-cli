# Nextflow Tower CLI

`tw` is [Tower](https://tower.nf/) on the command line. It brings Tower concepts including Pipelines, Actions and Compute Environments to the terminal.

Tower is a full-stack application for the management of data pipelines and compute resources. It enables collaborative data analysis at scale, on-premises or in any cloud.

The Tower CLI interacts with Tower, providing an interface to launch pipelines, manage cloud resources and administer your analysis.

![tw](assets/img/tw-screenshot.png)

The key features are:

- **A Nextflow-like experience**: Tower CLI provides a developer friendly environment. Pipelines can be launched with the CLI similar to Nextflow but with the benefits of Tower such as monitoring, logging, resource provisioning, dataset management and collaborative sharing.

- **Infrastructure as Code**: All Tower resources including Pipelines and Compute Environments can be described in a declarative manner. This allows a complete definition of an analysis environment that can be versioned and treated as code. It greatly simplifies sharing and re-use of configuration as well as routine administration.

- **Built on OpenAPI**: Tower CLI interacts with Tower via the [Tower API](https://tower.nf/openapi/index.html) which is created using the latest OpenAPI 3.0 specification. Tower CLI provides full control of the Tower application allowing users to get maximum insights into their pipeline submissions and execution environments.

For more information on Tower, see the [user documentation](https://help.tower.nf) on the Tower Cloud website.

## Availability

Tower CLI can be installed on macOS, Windows, and Linux.

It is compatible with [Tower Cloud](https://tower.nf/) and Tower Enterprise versions 21.08 and later.

## Getting Started

This guide covers the installation and configuration of the CLI, cloud infrastructure provisioning and launching pipelines into an AWS Batch compute environment.

### 1. Installation

1. Download the latest version for your OS from the assets in the [releases](https://github.com/seqeralabs/tower-cli/releases) page.

2. Rename and make the file executable:

    ```bash
    mv tw-* tw
    chmod +x ./tw
    ```

3. Move the file to a directory accessible by your `$PATH` variable:

    ```bash
    sudo mv tw /usr/local/bin/
    ```

### 2. Configuration

Create a Tower access token using the [Tower](https://tower.nf/) web interface via the **Your Tokens** page in your profile.

Providing `tw` access to Tower with your access token can be achieved in several ways:

1. Export it directly into your terminal:

    ```bash
    export TOWER_ACCESS_TOKEN=<your access token>
    ```

2. Add the above `export` command to a file such as `.bashrc` to be automatically added into your environment.

3. Specify your token as a parameter when running `tw`:

    ```bash
    tw --access-token=<your access token> <other options>
    ```

If required, you can configure the following non-mandatory environment variables using the same methods above:

- `TOWER_WORKSPACE_ID`: Workspace id. Default: The user workspace.
- `TOWER_API_ENDPOINT`: Tower API URL. Default: `api.tower.nf`.

> You can find the `TOWER_WORKSPACE_ID` for a given organisation in its **Workspaces** page as shown in [this image](assets/img/workspace_id.png).

### 3. Health check

Confirm the installation, configuration and connection is working as expected:

```console
$ tw info

    System health status
    ---------------------------------------+------------------
     Remote API server connection check    | OK
     Tower API version check               | OK
     Authentication API credential's token | OK
```

## Using the `tw` CLI

For detailed instructions on how to use the `tw` CLI to interact with Tower please refer to the [usage docs](./USAGE.md).

## Activate autocompletion

You can activate option autocompletion in your current session with the command below:

```bash
source <(tw generate-completion)
```

## Custom SSL certificate authority store

If you are using an SSL certificate that it is not accepted by the default Java certificate authorities you
can [customize](https://www.baeldung.com/jvm-certificate-store-errors) a `cacerts` store and use it like:

```bash
tw -Djavax.net.ssl.trustStore=/absolute/path/to/cacerts info
```

To avoid typing it everytime we recommend to rename the binary to `tw-binary` and create a `tw` script similar
to this:

```bash
#!/usr/bin/env bash
tw-binary -Djavax.net.ssl.trustStore=/absolute/path/to/cacerts $@
```

## Build binary development versions

The Tower CLI is a platform binary executable created by a native compilation from Java GraalVM.

1. Install [SDKMan!](https://sdkman.io/)

2. Install required GraalVM:

    ```bash
    sdk env install
    ```

3. Install `native-image`:

    ```bash
    gu install native-image
    ```
4. Export Github credentials. Even for public packages Github requires authentication (the token only requires `read:packages` scope):

    ```
    export GITHUB_USERNAME=...
    export GITHUB_TOKEN=...
    ```
   
5. Create the native client:

    ```bash
    ./gradlew nativeCompile
    ```

6. Run `tw`:

    ```bash
    ./build/graal/tw
    ```

## Using non-binary development versions

You can run a non-binary development version by executing the [`./tw`](tw) script in the root of this repository.

## License

[Mozilla Public License v2.0](https://github.com/seqeralabs/tower-cli/blob/master/LICENSE.txt)
