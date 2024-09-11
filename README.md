# tw CLI

`tw` is [Seqera Platform](https://cloud.seqera.io/) on the command line. It brings Seqera concepts like pipelines, actions, and compute environments to the terminal.

Seqera Platform is a full-stack application for the management of data pipelines and compute resources. It enables collaborative data analysis at scale, on-premises or in any cloud.

The CLI interacts with Platform to provide an interface to launch pipelines, manage cloud resources, and administer your analysis.

![tw](assets/img/tw-screenshot.png)

See the [Platform CLI documentation](https://docs.seqera.io/platform/24.1/cli/overview).

### Installation

1. Download the latest [version](https://github.com/seqeralabs/tower-cli/releases) for your OS.
1. Rename the file and and make it executable:

    ```bash
    mv tw-* tw
    chmod +x ./tw
    ```

1. Move the file to a directory accessible to your `$PATH` variable:

    ```bash
    sudo mv tw /usr/local/bin/
    ```

### Configuration

The CLI requires an access token to interact with your Seqera Platform instance. Select **User tokens** from the user menu in the [Platform UI](https://cloud.seqera.io), then select **Add token** to create a new token.

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

If required, configure the following optional environment variables using the same methods above:

- `TOWER_WORKSPACE_ID`: Workspace ID. Default: Your user workspace.
- `TOWER_API_ENDPOINT`: Seqera Platform API URL. Default: `api.cloud.seqera.io`.

:::tip
Find your `TOWER_WORKSPACE_ID` from the **Workspaces** tab on your organization page. Alternatively, list all the workspaces your token can access with `tw workspaces list` and copy the workspace ID from the command output.
:::

### Health check

Confirm the installation, configuration, and connection:

```console
tw info

    Details
    -------------------------+----------------------
     Tower API endpoint      | <TOWER_API_ENDPOINT>
     Tower API version       | 1.25.0               
     Tower version           | 24.2.0_cycle22       
     CLI version             | 0.9.4 (f3e846e)      
     CLI minimum API version | 1.15                 
     Authenticated user      | <username>  
     
    System health status
    ---------------------------------------+------------------
     Remote API server connection check    | OK
     Tower API version check               | OK
     Authentication API credential's token | OK
```

### Commands

See [Commands](https://docs.seqera.io/platform/24.1/cli/commands) for detailed instructions.

### Build binary development versions 

See the [Installation](https://docs.seqera.io/platform/24.1/cli/installation#build-binary-development-versions) docs for more information on building binary development versions of the CLI. 

### Non-binary development versions

Run a non-binary development version by executing the [./tw](https://github.com/seqeralabs/tower-cli/blob/master/tw) script in the root of this repository.

## License

[Apache 2.0](https://github.com/seqeralabs/tower-cli/blob/master/LICENSE.txt)