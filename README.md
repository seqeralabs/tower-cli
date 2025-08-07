# tw CLI

`tw` is [Seqera Platform](https://cloud.seqera.io/) on the command line. It brings Seqera concepts like pipelines, actions, and compute environments to the terminal.

Seqera Platform is a full-stack application for the management of data pipelines and compute resources. It enables collaborative data analysis at scale, on-premises or in any cloud.

The CLI interacts with Seqera to provide an interface to launch pipelines, manage cloud resources, and administer your analysis.

![tw](assets/img/tw-screenshot.png)

The key features are:

- **A Nextflow-like experience**: tw CLI provides a developer-friendly environment. Pipelines can be launched with the CLI similarly to Nextflow but with the Seqera benefits of monitoring, logging, resource provisioning, dataset management, and collaborative sharing.

- **Infrastructure as Code**: All Seqera resources, including pipelines and compute environments, can be described in a declarative manner. This enables a complete definition of an analysis environment that can be versioned and treated as code. It greatly simplifies configuration sharing and routine administration.

- **Built on OpenAPI**: tw CLI interacts with Seqera via the [Seqera Platform API](https://cloud.seqera.io/openapi/index.html) which uses the OpenAPI 3.0 specification. The CLI provides full control of the Seqera application, allowing users to get maximum insights into pipeline submissions and execution environments.

See the [Seqera Platform documentation](https://docs.seqera.io/platform/latest) to learn more about the application.

## Availability

tw CLI can be installed on macOS, Windows, and Linux.

It is compatible with [Seqera Cloud](https://cloud.seqera.io/) and Enterprise versions 21.08 and later.

## Getting Started

This guide covers the installation and configuration of the CLI, cloud infrastructure provisioning, and launching pipelines into an AWS Batch compute environment.

### 1. Installation

1. Download the latest version for your OS from the assets on the [releases](https://github.com/seqeralabs/tower-cli/releases) page.

2. Rename and make the file executable:

    ```bash
    mv tw-* tw
    chmod +x ./tw
    ```

3. Move the file to a directory accessible to your `$PATH` variable:

    ```bash
    sudo mv tw /usr/local/bin/
    ```

## Alternative: Homebrew (Linux and macOS)

tw-cli is published to the Seqera [Homebrew](https://brew.sh/) tap and can be installed like this:

```bash
brew install seqeralabs/tap/tw
```

### 2. Configuration

You need an access token for the CLI to interact with your Seqera instance. Select **User tokens** from the user menu in the [Seqera UI](https://cloud.seqera.io), then select **Add token** to create a new token.

Copy the access token value and use it with the CLI in one of two ways:

- **Environment variable**:

    1. Export the token as a shell variable directly into your terminal:

        ```bash
        export TOWER_ACCESS_TOKEN=<your access token>
        ```

    2. Add the `export` command to your `.bashrc`, `.zshrc`, or `.bash_profile` file for it to be permanently added to your environment.

- **tw command flag**:

    Provide the access token directly in your `tw` command with `--access-token`:

    ```bash
    tw --access-token=<your access token> <other options>
    ```

If required, configure the following non-mandatory environment variables using the same methods above:

- `TOWER_WORKSPACE_ID`: Workspace ID. Default: The user workspace.
- `TOWER_API_ENDPOINT`: Seqera API URL. Default: `api.cloud.seqera.io`.

> You can find your `TOWER_WORKSPACE_ID` from the **Workspaces** tab on your organization page. Alternatively, you can list all the workspaces your token can access with `tw workspaces list` and copy the workspace ID from the command output.

### 3. Health check

Confirm the installation, configuration, and connection:

```console
$ tw info

    System health status
    ---------------------------------------+------------------
     Remote API server connection check    | OK
     Tower API version check               | OK
     Authentication API credential's token | OK
```

## `tw` CLI commands

See [Usage](./USAGE.md) for detailed instructions to use the CLI.

## Autocompletion

Activate autocompletion in your current session with this command:

```bash
source <(tw generate-completion)
```

## Custom SSL certificate authority store

If you are using a Private CA SSL certificate not recognized by the default Java certificate authorities, use a [custom](https://www.baeldung.com/jvm-certificate-store-errors) `cacerts` store:

```bash
tw -Djavax.net.ssl.trustStore=/absolute/path/to/cacerts info
```

You can rename the binary to `tw-binary` and create a `tw` script to automatically include the custom `cacerts` store in every session:

```bash
#!/usr/bin/env bash
tw-binary -Djavax.net.ssl.trustStore=/absolute/path/to/cacerts $@
```

## Build binary development versions

tw CLI is a platform binary executable created by a native compilation from Java GraalVM. To compile and build a development version of the binary:

1. If necessary, install [SDKMan!](https://sdkman.io/)

1. From the root of the tower-cli project, install GraalVM:

    ```bash
    sdk env install
    ```

    This ensures that SDKMan uses the tower-cli project-specific `.sdkmanrc` configuration.

1. Export your Github credentials. Github requires authentication for public packages (the token only requires the `read:packages` scope):

    ```bash
    export GITHUB_USERNAME=...
    export GITHUB_TOKEN=...
    ```

1. Update reflection metadata config for new Tower SDK version (optional)

    This step is only needed if you are updating to a newer version of Tower SDK (io.seqera.tower:tower-java-sdk).

    ```bash
    ./gradlew runReflectionConfigGenerator
    ```

    The GraalVM reflection metadata generation relies on code paths being executed with [a tracing agent](https://www.graalvm.org/jdk21/reference-manual/native-image/metadata/AutomaticMetadataCollection/#tracing-agent) to dynamically capture various execution paths and update the reflect-config.json.

    This is a utility task that uses reflection to make calls to all classes in the Tower SDK package,
    in order to capture and generate methods reflection metadata of any new fields/classes in Tower SDK and update resource-config.json

1. Create the native client:

    ```bash
    ./gradlew nativeCompile
    ```

    This will install a locally compiled version of `tw` in the nativeCompile directory:

    ```console
    Produced artifacts:
     <tower-cli-repository-root>/build/native/nativeCompile/tw (executable)
    ========================================================================================================================
    Finished generating 'tw' in 1m 6s.
    [native-image-plugin] Native Image written to: <tower-cli-repository-root>/build/native/nativeCompile
    
    BUILD SUCCESSFUL in 1m 8s
    6 actionable tasks: 2 executed, 4 up-to-date
    ```

1. Run `tw`:

    ```bash
    ./build/native/nativeCompile/tw
    ```

## Non-binary development versions

Run a non-binary development version by executing the [`./tw`](tw) script in the root of this repository.

## License

[Apache 2.0](https://github.com/seqeralabs/tower-cli/blob/master/LICENSE.txt)
