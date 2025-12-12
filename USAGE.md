# Seqera CLI Usage

> **Note**: The CLI performs operations in the user workspace context by default. Use the `SEQERA_WORKSPACE` environment variable or the `--workspace` parameter to specify an organization workspace.

Use `-h` or `--help` to list available commands and options.

```bash
seqera --help
seqera credentials add --help
```

## Configuration

The CLI loads configuration from multiple sources with the following precedence (highest to lowest):

1. **Command-line options** (`--access-token`, `--url`, etc.)
2. **Environment variables** (`SEQERA_ACCESS_TOKEN`, `TOWER_ACCESS_TOKEN`, etc.)
3. **TOML config file** (`$XDG_CONFIG_HOME/seqera/config.toml` or `~/.config/seqera/config.toml`)
4. **Nextflow auth config** (`$NXF_HOME/seqera-auth.config` or `~/.nextflow/seqera-auth.config`)

Each configuration value is resolved independently, so you can mix sources (e.g., token from environment, workspace from config file).

### Environment variables

| Variable | Description |
|----------|-------------|
| `SEQERA_ACCESS_TOKEN` | Seqera Platform access token |
| `TOWER_ACCESS_TOKEN` | Alternative access token variable (legacy) |
| `SEQERA_API_ENDPOINT` | API endpoint URL |
| `TOWER_API_ENDPOINT` | Alternative API endpoint variable (legacy) |
| `SEQERA_WORKSPACE` | Default workspace ID |
| `TOWER_WORKSPACE_ID` | Alternative workspace variable (legacy) |
| `SEQERA_COMPUTE_ENV` | Default compute environment ID |

### TOML config file

Create a config file at `~/.config/seqera/config.toml` (or `$XDG_CONFIG_HOME/seqera/config.toml`):

```toml
access_token = "your-access-token"
url = "https://api.cloud.seqera.io"
workspace_id = "123456789"
compute_env_id = "abc123def456"
```

### Nextflow auth config

The CLI can also read credentials from a Nextflow-style auth config file at `~/.nextflow/seqera-auth.config` (or `$NXF_HOME/seqera-auth.config`):

```groovy
tower {
    accessToken = 'your-access-token'
    endpoint = 'https://api.cloud.seqera.io'
    workspaceId = '123456789'
    computeEnvId = 'abc123def456'
}
```

This allows sharing authentication configuration between Nextflow and the Seqera CLI.

### Debugging configuration

Use the `-v/--verbose` flag to see where each configuration value was loaded from:

```console
$ seqera -v info
[config] access_token from: ~/.config/seqera/config.toml
[config] url from: environment
...
```

## Version

Display the CLI version:

```console
$ seqera --version

seqera 0.1.0
```

## Shell completion

Generate shell completion scripts for bash, zsh, or fish:

```bash
# Bash
eval "$(seqera generate-completion bash)"

# Zsh
eval "$(seqera generate-completion zsh)"

# Fish
seqera generate-completion fish > ~/.config/fish/completions/seqera.fish
```

> **Tip**: Use `seqera --output=json <command>` to output JSON for scripting.
>
> **Tip**: Use `seqera --output=json <command> | jq -r '.key'` to extract specific values.

## Credentials

To launch pipelines in a Seqera workspace, you need [credentials][credentials] for:

1. Compute environments
2. Pipeline repository Git providers
3. (Optional) [Seqera agent][tower-agent] — used with HPC clusters
4. (Optional) Container registries, such as docker.io

### Add credentials

Run `seqera credentials add -h` to view a list of providers.
Run `seqera credentials add <provider> -h` to view the required fields for your provider.

> **Note**: You can add multiple credentials from the same provider in the same workspace.

#### Compute environment credentials

Seqera requires credentials to access your cloud compute environments. See the [compute environment page][compute-envs] for your cloud provider for more information.

  ```console
  $ seqera credentials add aws --name=my_aws_creds --access-key=<aws access key> --secret-key=<aws secret key>

    New AWS credentials 'my_aws_creds (1sxCxvxfx8xnxdxGxQxqxH)' added at user workspace
  ```

#### Git credentials

Seqera requires access credentials to interact with pipeline Git repositories. See [Git integration][git-integration] for more information.

  ```console
  $ seqera credentials add github -n=my_GH_creds -u=<GitHub username> -p=<GitHub access token>

    New GITHUB credentials 'my_GH_creds (xxxxx3prfGlpxxxvR2xxxxo7ow)' added at user workspace
  ```

#### Container registry credentials

Configure credentials for the Nextflow Wave container service to authenticate to private and public container registries. See the **Container registry credentials** section under [Credentials][credentials] for registry-specific instructions.

> **Note**: Container registry credentials are only used by the Wave container service. See [Wave containers][wave-docs] for more information.

### List credentials

```console
$ seqera credentials list

  Credentials at user workspace:

     ID                     | Provider  | Name                               | Last activity
    ------------------------+-----------+------------------------------------+-------------------------------
     1x1HxFxzxNxptxlx4xO7Gx | aws       | my_aws_creds_1                     | Wed, 6 Apr 2022 08:40:49 GMT
     1sxCxvxfx8xnxdxGxQxqxH | aws       | my_aws_creds_2                     | Wed, 9 Apr 2022 08:40:49 GMT
     2x7xNsf2xkxxUIxXKxsTCx | ssh       | my_ssh_key                         | Thu, 8 Jul 2021 07:09:46 GMT
     4xxxIeUx7xex1xqx1xxesk | github    | my_github_cred                     | Wed, 22 Jun 2022 09:18:05 GMT
```

### Delete credentials

```console
$ seqera credentials delete --name=my_aws_creds

  Credentials '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
```

## Compute environments

Compute environments in Seqera define the execution platform where a pipeline runs. A compute environment is composed of the credentials, configuration, and storage options related to a particular computing platform.  See [Seqera Platform compute environments][compute-envs] for more information on supported compute environments.

### Add a compute environment

Run `seqera compute-envs add -h` to view the list of supported platforms.
Run `seqera compute-envs add <platform> -h` to view the required and optional fields for your platform.

You must add the credentials for your provider before creating your compute environment.

```console
$ seqera compute-envs add aws-batch forge --name=my_aws_ce --credentials=<my_aws_creds_1> --region=eu-west-1 --max-cpus=256 --work-dir=s3://<bucket name> --wait=AVAILABLE

  New AWS-BATCH compute environment 'my_aws_ce' added at user workspace
```

This command will:

- Use **Batch Forge** to automatically manage the AWS Batch resource lifecycle (`forge`)
- Use the credentials previously added to the workspace (`--credentials`)
- Create the required AWS Batch resources in the AWS Ireland (`eu-west-1`) region
- Provision a maximum of 256 CPUs in the compute environment (`--max-cpus`)
- Use an existing S3 bucket to store the Nextflow work directory (`--work-dir`)
- Wait until the compute environment has been successfully created and is ready to use (`--wait`)

See the [compute environment page][compute-envs] for your provider for detailed information on Batch Forge and manual compute environment creation.

### Delete a compute environment

```console
$ seqera compute-envs delete --name=my_aws_ce

  Compute environment '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
```

### Default compute environment

Select a **primary** compute environment to be used by default in a workspace. You can override the workspace primary compute environment by explicitly specifying an alternative compute environment when you create or launch a pipeline.

```console
$ seqera compute-envs primary set --name=my_aws_ce

  Primary compute environment for workspace 'user' was set to 'my_aws_ce (1sxCxvxfx8xnxdxGxQxqxH)'
```

### Import and export a compute environment

Export the configuration details of a compute environment in JSON format for scripting and reproducibility purposes.

```console
$ seqera compute-envs export --name=my_aws_ce my_aws_ce_v1.json

  Compute environment exported into 'my_aws_ce_v1.json'
```

Similarly, a compute environment can be imported to a workspace from a previously exported JSON file.

```console
$ seqera compute-envs import --name=my_aws_ce_v1 ./my_aws_ce_v1.json

  New AWS-BATCH compute environment 'my_aws_ce_v1' added at user workspace
```

### Update a compute environment

Update settings for an existing compute environment (e.g., rename it).

```console
$ seqera compute-envs update --name=my_aws_ce --new-name=my_aws_ce_renamed

  Compute environment 'my_aws_ce_renamed' updated at user workspace
```

## Pipelines

Pipelines define pre-configured workflows in a workspace. A pipeline consists of a workflow repository, launch parameters, and a compute environment.

Run `seqera pipelines -h` to view the list of supported operations.
Run `seqera pipelines add -h` to view the required and optional fields for adding your pipeline.

### Add a pipeline

Add a pre-configured pipeline to the Launchpad:

```console
$ seqera pipelines add --name=my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params.yaml https://github.com/nextflow-io/rnaseq-nf

 New pipeline 'my_rnaseq_nf_pipeline' added at user workspace
```

The optional `--params-file` flag is used to pass a set of default parameters that will be associated with the pipeline in the Launchpad.

> **Note**: The `params-file` must be a YAML or JSON file using [Nextflow configuration file][nextflow-config] syntax.

### Import and export a pipeline

Export the configuration details of a pipeline in JSON format for scripting and reproducibility purposes.

```console
$ seqera pipelines export --name=my_rnaseq_nf_pipeline my_rnaseq_nf_pipeline_v1.json

  Pipeline exported into 'my_rnaseq_nf_pipeline_v1.json'
```

Similarly, a pipeline can be imported to a workspace from a previously exported JSON file.

```console
$ seqera pipelines import --name=my_rnaseq_nf_pipeline_v1 ./my_rnaseq_nf_pipeline_v1.json

  New pipeline 'my_rnaseq_nf_pipeline_v1' added at user workspace
```

### Update a pipeline

The default launch parameters can be changed with the `update` command:

```console
seqera pipelines update --name=my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params_2.yaml
```

## Launch pipelines

Run `seqera launch -h` to view supported launch options.

### Launch a preconfigured pipeline

If no custom parameters are passed via the CLI during launch, the defaults set for the pipeline in the Launchpad will be used.

> **Note**: seqeraCLI users are bound to the same user permissions that apply in the Platform UI. Launch users can launch pre-configured pipelines in the workspaces they have access to, but they cannot add or run new pipelines.

```console
$ seqera launch my_rnaseq_nf_pipeline

  Workflow 1XCXxX0vCX8xhx submitted at user workspace.

    https://cloud.seqera.io/user/abhinav/watch/1XCXxX0vCX8xhx
```

When using `--wait`, `tw` can exit with one of two exit codes:

- `0`: When the run reaches the desired state.
- `1`: When the run reaches a state that makes it impossible to reach the desired state.

> **Tip**: Use `--wait=SUCCEEDED` if you want the command to wait until the pipeline execution is complete.

### Launch a pipeline with custom parameters

To specify custom parameters during pipeline launch, specify a custom `--params-file`:

```console
$ seqera launch my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params_2.yaml

  Workflow 2XDXxX0vCX8xhx submitted at user workspace.

    https://cloud.seqera.io/user/abhinav/watch/2XDXxX0vCX8xhx
```

See [Nextflow configuration][nextflow-config] for more information.

### Launch an unsaved pipeline

The CLI can directly launch pipelines that have not been added to the Launchpad in a Seqera workspace by using the full pipeline repository URL:

```console
$ seqera launch https://github.com/nf-core/rnaseq --params-file=./custom_rnaseq_params.yaml --compute-env=my_aws_ce --revision 3.8.1 --profile=test,docker

  Workflow 2XDXxX0vCX8xhx submitted at user workspace.

    https://cloud.seqera.io/user/abhinav/watch/2XDXxX0vCX8xhx
```

- Pipeline parameters are defined within the `custom_rnaseq_params.yaml` file.
- Other parameters such as `--profile` and `--revision` can also be specified.
- A non-primary compute environment can be used to launch the pipeline. Omit `--compute-env` to launch with the workspace default compute environment.

> **Note**: CLI users are bound to the same user permissions that apply in the Platform UI. Launch users can launch pre-configured pipelines in the workspaces they have access to, but they cannot add or run new pipelines.

### Additional launch options

The `launch` command supports additional options for advanced use cases:

```console
$ seqera launch my_pipeline \
    --launch-container="quay.io/seqeralabs/nf-launcher:j17" \
    --user-secrets="MY_SECRET,OTHER_SECRET" \
    --workspace-secrets="SHARED_SECRET"
```

- `--launch-container`: Specify a custom container image for the Nextflow head job (BETA feature)
- `--user-secrets`: Comma-separated list of user secrets to make available during pipeline execution
- `--workspace-secrets`: Comma-separated list of workspace secrets to make available during pipeline execution

## Runs

Run `seqera runs -h` to view supported runs operations.

Runs display all the current and previous pipeline runs in the specified workspace. Each new or resumed run is given a random name such as _grave_williams_ by default, which can be overridden with a custom value at launch. See [Runs](https://docs.seqera.io/platform/latest/monitoring/overview) for more information. As a run executes, it can transition through the following states:

- `submitted`: Pending execution
- `running`: Running
- `succeeded`: Completed successfully
- `failed`: Successfully executed, where at least one task failed with a terminate [error strategy](https://www.nextflow.io/docs/latest/process.html#errorstrategy)
- `cancelled`: Stopped manually during execution
- `unknown`: Indeterminate status

### View pipeline's runs

Run `seqera runs view -h` to view all the required and optional fields for viewing a pipeline's runs.

```console
$ seqera runs view -i 2vFUbBx63cfsBY -w seqeralabs/showcase

  Run at [seqeralabs / showcase] workspace:


    General
    ---------------------+-------------------------------------------------
     ID                  | 2vFUbBx63cfsBY
     Operation ID        | b5d55384-734e-4af0-8e47-0d3abec71264
     Run name            | adoring_brown
     Status              | SUCCEEDED
     Starting date       | Fri, 31 May 2024 10:38:30 GMT
     Commit ID           | b89fac32650aacc86fcda9ee77e00612a1d77066
     Session ID          | 9365c6f4-6d79-4ca9-b6e1-2425f4d957fe
     Username            | drpatelhh
     Workdir             | s3://seqeralabs-showcase/scratch/2vFUbBx63cfsBY
     Container           | No container was reported
     Executors           | awsbatch
     Compute Environment | seqera_aws_ireland_fusionv2_nvme
     Nextflow Version    | 23.10.1
     Labels              | star_salmon,yeast
```

### List runs

Run `seqera runs list -h` to view all the required and optional fields for listing runs in a workspace.

```console
$ seqera runs list

  Pipeline runs at [seqeralabs / testing] workspace:

     ID             | Status    | Project Name               | Run Name                        | Username              | Submit Date
    ----------------+-----------+----------------------------+---------------------------------+-----------------------+-------------------------------
     49Gb5XVMud2e7H | FAILED    | seqeralabs/nf-aggregate    | distraught_archimedes           | adrian-navarro-seqera | Fri, 31 May 2024 16:22:10 GMT
     4anNFvTUwRFDp  | SUCCEEDED | nextflow-io/rnaseq-nf      | nasty_kilby                     | mattia-bosio-seqera   | Fri, 31 May 2024 15:23:12 GMT
     3wo3Kfni6Kl3hO | SUCCEEDED | nf-core/proteinfold        | reverent_linnaeus               | mattia-bosio-seqera   | Fri, 31 May 2024 15:22:38 GMT

<snip>

     4fIRrFgZV3eDb1 | FAILED    | nextflow-io/hello          | gigantic_lichterman             | pedro-geadas          | Mon, 29 Apr 2024 08:44:47 GMT
     cHEdKBXmdoQQM  | FAILED    | mathysgrapotte/stimulus    | mighty_poitras                  | evanfloden            | Mon, 29 Apr 2024 08:08:52 GMT
```

Use the optional `--filter` flag to filter the list of runs returned by one or more `keyword:value` entries:

- `status`
- `label`
- `workflowId`
- `runName`
- `username`
- `projectName`
- `after`
- `before`
- `sessionId`
- `is:starred`

If no `keyword` is defined, the filtering is applied to the `runName`, `projectName` (the pipeline name), and `username`.

> Note: The `after` and `before` flags require an [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) timestamp with UTC timezone (`YYYY-MM-DDThh:mm:ss.sssZ`)

```console
$ seqera runs list --filter hello_slurm_20240530

  Pipeline runs at [seqeralabs / showcase] workspace:

     ID            | Status    | Project Name      | Run Name                             | Username   | Submit Date
    ---------------+-----------+-------------------+--------------------------------------+------------+-------------------------------
     pZeJBOLtIvP7R | SUCCEEDED | nextflow-io/hello | hello_slurm_20240530_e75584566f774e7 | adamtalbot | Thu, 30 May 2024 09:12:51 GMT
```

Multiple filter criteria can be defined:

```console
$ seqera runs list --filter="after:2024-05-29T00:00:00.000Z before:2024-05-30T00:00:00.000Z username:mark-panganiban"

  Pipeline runs at [seqeralabs / testing] workspace:

     ID             | Status    | Project Name          | Run Name           | Username              | Submit Date
    ----------------+-----------+-----------------------+--------------------+-----------------------+-------------------------------
     xJvK95W6YUmEz  | SUCCEEDED | nextflow-io/rnaseq-nf | ondemand2          | mark-panganiban       | Wed, 29 May 2024 20:35:28 GMT
     1c1ckn9a3j0xF0 | SUCCEEDED | nextflow-io/rnaseq-nf | fargate            | mark-panganiban       | Wed, 29 May 2024 20:28:02 GMT
     3sYX1acJ01T7rL | SUCCEEDED | nextflow-io/rnaseq-nf | min1vpcu-spot      | mark-panganiban       | Wed, 29 May 2024 20:27:47 GMT
     4ZYJGWJCttXqXq | SUCCEEDED | nextflow-io/rnaseq-nf | min1cpu-ondemand   | mark-panganiban       | Wed, 29 May 2024 20:25:21 GMT
     4LCxsffTqf3ysT | SUCCEEDED | nextflow-io/rnaseq-nf | lonely_northcutt   | mark-panganiban       | Wed, 29 May 2024 20:09:51 GMT
     4Y8EcyopNiYBlJ | SUCCEEDED | nextflow-io/rnaseq-nf | fargate            | mark-panganiban       | Wed, 29 May 2024 18:53:47 GMT
     dyKevNwxK50XX  | SUCCEEDED | mark814/nr-test       | cheeky_cuvier      | mark-panganiban       | Wed, 29 May 2024 12:21:10 GMT
     eS6sVB5A387aR  | SUCCEEDED | mark814/nr-test       | evil_murdock       | mark-panganiban       | Wed, 29 May 2024 12:11:08 GMT
```

A leading and trailing `*` wildcard character is supported:

```console
$ seqera runs list --filter="*man/rnaseq-*"

  Pipeline runs at [seqeralabs / testing] workspace:

     ID             | Status    | Project Name        | Run Name            | Username       | Submit Date
    ----------------+-----------+---------------------+---------------------+----------------+-------------------------------
     5z4AMshti4g0GK | SUCCEEDED | robnewman/rnaseq-nf | admiring_darwin     | rob-newman     | Tue, 16 Jan 2024 19:56:29 GMT
     62LqiS4O4FatSy | SUCCEEDED | robnewman/rnaseq-nf | cheeky_yonath       | joaquim-gamero | Wed, 3 Jan 2024 12:36:09 GMT
     3k2nu8ZmcBFSGv | SUCCEEDED | robnewman/rnaseq-nf | compassionate_jones | pedro-geadas   | Tue, 2 Jan 2024 16:22:26 GMT
     3zG2ggf5JsniNW | SUCCEEDED | robnewman/rnaseq-nf | fervent_payne       | rob-newman     | Wed, 20 Dec 2023 23:55:17 GMT
     1SNIcSXRuJMSNZ | SUCCEEDED | robnewman/rnaseq-nf | curious_babbage     | rob-newman     | Thu, 28 Sep 2023 17:48:04 GMT
     5lI2fZUZfiokBI | SUCCEEDED | robnewman/rnaseq-nf | boring_heisenberg   | rob-newman     | Thu, 28 Sep 2023 12:29:27 GMT
     5I4lsRXIHVEjNB | SUCCEEDED | robnewman/rnaseq-nf | ecstatic_ptolemy    | rob-newman     | Wed, 27 Sep 2023 22:06:19 GMT
```

### Relaunch run

Run `seqera runs relaunch -h` to view all the required and optional fields for relaunching a run in a workspace.

```console
$ seqera runs relaunch -i 5z4AMshti4g0GK

  Workflow 6ABCxX0vCX8xhx submitted at user workspace.

    https://cloud.seqera.io/user/abhinav/watch/6ABCxX0vCX8xhx
```

Additional options for relaunch:

```console
$ seqera runs relaunch -i 5z4AMshti4g0GK \
    --no-resume \
    --pull-latest \
    --launch-container="quay.io/seqeralabs/nf-launcher:j17" \
    --main-script="custom_main.nf" \
    --entry-name="my_workflow" \
    --user-secrets="MY_SECRET" \
    --workspace-secrets="SHARED_SECRET"
```

- `--no-resume`: Start fresh instead of resuming from the previous run
- `--pull-latest`: Pull the latest pipeline code before running
- `--stub-run`: Execute in stub/dry-run mode
- `--launch-container`: Custom container for the Nextflow head job (BETA)
- `--main-script`: Main script file if different from main.nf
- `--entry-name`: Main workflow name for DLS2 syntax
- `--schema-name`: Schema name for the pipeline
- `--user-secrets`: Comma-separated user secrets for the execution
- `--workspace-secrets`: Comma-separated workspace secrets for the execution

### Cancel a run

Run `seqera runs cancel -h` to view all the required and optional fields for canceling a run in a workspace.

### Manage labels for runs

Run `seqera runs labels -h` to view all the required and optional fields for managing labels for runs in a workspace.

In the example below, we add the labels `test` and `rnaseq-demo` to the run with ID `5z4AMshti4g0GK`:

```console
$ seqera runs labels -i 5z4AMshti4g0GK test,rnaseq-demo

 'set' labels on 'run' with id '5z4AMshti4g0GK' at 34830707738561 workspace
```

### Delete a run

Run `seqera runs delete -h` to view all the required and optional fields for deleting a run in a workspace.

### Dump all logs and details of a run

Run `seqera runs dump -h` to view all the required and optional fields for dumping all logs and details of a run in a workspace. The supported formats are `.tar.xz` and `.tar.gz`. In the example below, we dump all the logs and details for the run with ID `5z4AMshti4g0GK` to the output file `file.tar.gz`.

```console
$ seqera runs dump -i 5z4AMshti4g0GK -o file.tar.gz
- Seqera info
- Workflow details
- Task details

  Pipeline run '5z4AMshti4g0GK' at [seqeralabs / testing] workspace details dump at 'file.tar.gz'
```

### View a single task

Run `seqera runs task -h` to view all the required and optional fields for viewing a single task in a workflow run.

```console
$ seqera runs task -i 5z4AMshti4g0GK -t 1

  Task details for run 5z4AMshti4g0GK:

    Task ID:    1
    Process:    NFCORE_RNASEQ:RNASEQ:FASTQC
    Tag:        -
    Status:     COMPLETED
    Exit code:  0
    Container:  quay.io/biocontainers/fastqc:0.11.9
    Executor:   awsbatch
    Work dir:   s3://bucket/work/a1/b2c3d4
```

### Download log files

Download workflow or task log files from a run. Available file types:

- **stdout** - Standard output (workflow: `nf-{id}.txt`, task: `.command.out`)
- **log** - Log file (workflow: `nf-{id}.log`, task: `.command.log`)
- **stderr** - Standard error (task only: `.command.err`)
- **timeline** - Timeline HTML (workflow only: `timeline-{id}.html`)

```console
$ seqera runs download -i 5z4AMshti4g0GK --type=stdout

  Downloaded stdout file for run 5z4AMshti4g0GK to 'nf-5z4AMshti4g0GK.txt'
```

Download a task's log file:

```console
$ seqera runs download -i 5z4AMshti4g0GK --type=log -t 42 -o task-42.log

  Downloaded log file for task 42 to 'task-42.log'
```

## Workspaces

Run `seqera workspaces -h` to view supported workspace operations.
Run `seqera workspaces add -h` to view the required and optional fields for adding your workspace.

Workspaces provide the context in which a user launches workflow executions, defines the available resources, and manages who can access those resources. Workspaces contain pipelines, runs, actions, compute environments, credentials, datasets, data links, and secrets. Access permissions are controlled with participants, collaborators, and teams.

See [User workspaces][user-workspaces] for more information.

> **Note**: This section assumes that you already have access to an organization in Seqera Platform.

### Create a workspace

In the example below, we create a shared workspace to be used for sharing pipelines with other private workspaces. See [Shared workspaces][shared-workspaces] for more information.

```console
$ seqera workspaces add --name=shared-workspace --full-name=shared-workspace-for-all  --org=my-seqera-org --visibility=SHARED

  A 'SHARED' workspace 'shared-workspace' added for 'my-seqera-org' organization
```

> **Note**: By default, a workspace is set to private when created.

### List workspaces

List all the workspaces in which you are a participant:

```console
$ seqera workspaces list

  Workspaces for abhinav user:

     Workspace ID    | Workspace Name   | Organization Name | Organization ID
    -----------------+------------------+-------------------+-----------------
     26002603030407  | shared-workspace | my-seqera-org      | 04303000612070
```

## Participants

Run `seqera participants -h` to view supported participant operations.
Run `seqera participants add -h` to view the required and optional fields for adding a participant.

### List participants

```console
$ seqera participants list

  Participants for 'my-seqera-org/shared-workspace' workspace:

     ID             | Participant Type | Name                        | Workspace Role
    ----------------+------------------+-----------------------------+----------------
     45678460861822 | MEMBER           | abhinav (abhinav@mydomain.com) | owner
```

### Add participants

To add a new _collaborator_ to the workspace, use the `add` subcommand. The default role assigned to a _collaborator_ is `Launch`.

See [Participant roles][participant-roles] for more information.

```console
$ seqera participants add --name=collaborator@mydomain.com --type=MEMBER

  User 'collaborator' was added as participant to 'shared-workspace' workspace with role 'launch'
```

### Update participant roles

To update the role of a _Collaborator_ to `ADMIN` or `MAINTAIN`, use the `update` subcommand:

```console
$ seqera participants update --name=collaborator@mydomain.com --type=COLLABORATOR --role=MAINTAIN

  Participant 'collaborator@mydomain.com' has now role 'maintain' for workspace 'shared-workspace'
```

## Data Links

Run `seqera data-links -h` to view supported data link operations.

Data links allow you to work with public and private cloud storage buckets in Data Explorer in the specified workspace. See [Data Explorer][data-explorer] for more information. AWS S3, Azure Blob Storage, and Google Cloud Storage are supported. The full list of operations are:

- `list`: List data links in a workspace
- `add`: Add a custom data link to a workspace
- `update`: Update a custom data link in a workspace
- `delete`: Delete a custom data link from a workspace
- `browse`: Browse the contents of a data link in a workspace

### List data links

Run `seqera data-links list -h` to view all the optional fields for listing data links in a workspace. If a workspace is not defined, the `SEQERA_WORKSPACE_ID` workspace is used by default. Data links can be one of two types:

- `v1-cloud-<id>`: **cloud** data links auto-discovered using credentials attached to the workspace
- `v1-user-<id>`: **custom** data links created by users

```console
$ seqera data-links list -w seqeralabs/showcase

  Data links at [seqeralabs / showcase] workspace:

 ID                                        | Provider | Name                           | Resource ref                                                    | Region
-------------------------------------------+----------+--------------------------------+-----------------------------------------------------------------+-----------
 v1-cloud-833bb845bd9ec1970c4a7b0bb7b8c4ad | aws      | e2e-data-explorer-tests-aws    | s3://e2e-data-explorer-tests-aws                                | eu-west-2
 v1-cloud-60700a33ec3fae68d424cf948fa8d10c | aws      | nf-seqera-bucket                | s3://nf-seqera-bucket                                            | eu-west-1
 v1-user-09705781697816b62f9454bc4b9434b4  | aws      | vscode-analysis-demo           | s3://seqera-development-permanent-bucket/studios-demo/vscode/   | eu-west-2
 v1-user-0dede00fabbc4b9e2610261822a2d6ae  | aws      | seqeralabs-showcase            | s3://seqeralabs-showcase                                        | eu-west-1
 v1-user-171aa8801cabe4af71500335f193d649  | aws      | projectA-rnaseq-analysis       | s3://seqeralabs-showcase/demo/nf-core-rnaseq/                   | eu-west-1

<snip>

 v1-user-bb4fa9625a44721510c47ac1cb97905b  | aws      | genome-in-a-bottle             | s3://giab                                                       | us-east-1
 v1-user-e7bf26921ba74032bd6ae1870df381fc  | aws      | NCBI_Sequence_Read_Archive_SRA | s3://sra-pub-src-1/                                             | us-east-1

  Showing from 0 to 99 from a total of 16 entries.
```

### Add a custom data link

Run `seqera data-links add -h` to view all the required and optional fields for adding a custom data link to a workspace. Users with the `MAINTAIN` role and above for a workspace can add custom data links. The data link `name`, `uri` and `provider` (one of `aws`, `azure`, or `google`) fields are required. If adding a custom data link for a private bucket, the `credentials` identifier field is also required. Adding a custom data link for a public bucket doesn't require credentials.

```console
$ seqera data-links add -w seqeralabs/showcase -n FOO -u az://seqeralabs.azure-benchmarking -p azure -c seqera_azure_credentials

  Data link created:

 ID                                       | Provider | Name | Resource ref                       | Region
------------------------------------------+----------+------+------------------------------------+--------
 v1-user-152116183ee325463901430bb9efb8c9 | azure    | FOO  | az://seqeralabs.azure-benchmarking |
```

### Update a custom data link

Run `seqera data-links update -h` to view all the required and optional fields for updating a custom data link in a workspace. Users with the `MAINTAIN` role and above for a workspace can update custom data links.

```console
$ seqera data-links update -w seqeralabs/showcase -i v1-user-152116183ee325463901430bb9efb8c9 -n BAR

  Data link updated:

 ID                                       | Provider | Name | Resource ref                       | Region
------------------------------------------+----------+------+------------------------------------+--------
 v1-user-152116183ee325463901430bb9efb8c9 | azure    | BAR  | az://seqeralabs.azure-benchmarking |
```

### Delete a custom data link

Run `seqera data-links delete -h` to view all the required and optional fields for deleting a custom data link from a workspace. Users with the `MAINTAIN` role and above for a workspace can delete custom data links.

```console
$ seqera data-links delete -w seqeralabs/showcase -i v1-user-152116183ee325463901430bb9efb8c9

  Data link 'v1-user-152116183ee325463901430bb9efb8c9' deleted at '138659136604200' workspace.
```

### Browse the contents of a data link

Run `seqera data-links browse -h` to view all the required and optional fields for browsing a data link in a workspace. Define the data link id using the required `-i` or `--id` argument, which can be found by first using the `list` operation for a workspace. In the example below, a `name` is optionally defined to only retrieve data links with names that start with the given word:

```console
$ seqera data-links list -w seqeralabs/showcase -n 1000genomes

  Data links at [seqeralabs / showcase] workspace:

 ID                                       | Provider | Name        | Resource ref     | Region
------------------------------------------+----------+-------------+------------------+-----------
 v1-user-6d8f44c239e2a098b3e02e918612452a | aws      | 1000genomes | s3://1000genomes | us-east-1

  Showing from 0 to 99 from a total of 1 entries.

$ seqera data-links browse -w seqeralabs/showcase -i v1-user-6d8f44c239e2a098b3e02e918612452a

  Content of 's3://1000genomes' and path 'null':

 Type   | Name                                       | Size
--------+--------------------------------------------+----------
 FILE   | 20131219.populations.tsv                   | 1663
 FILE   | 20131219.superpopulations.tsv              | 97
 FILE   | CHANGELOG                                  | 257098
 FILE   | README.alignment_data                      | 15977
 FILE   | README.analysis_history                    | 5289
 FILE   | README.complete_genomics_data              | 5967
 FILE   | README.crams                               | 563
 FILE   | README.ebi_aspera_info                     | 935
 FILE   | README.ftp_structure                       | 8408
 FILE   | README.pilot_data                          | 2082
 FILE   | README.populations                         | 1938
 FILE   | README.sequence_data                       | 7857
 FILE   | README_missing_files_20150612              | 672
 FILE   | README_phase3_alignments_sequence_20150526 | 136
 FILE   | README_phase3_data_move_20150612           | 273
 FILE   | alignment.index                            | 3579471
 FILE   | analysis.sequence.index                    | 54743580
 FILE   | exome.alignment.index                      | 3549051
 FILE   | sequence.index                             | 67069489
 FOLDER | 1000G_2504_high_coverage/                  | 0
 FOLDER | alignment_indices/                         | 0
 FOLDER | changelog_details/                         | 0
 FOLDER | complete_genomics_indices/                 | 0
 FOLDER | data/                                      | 0
 FOLDER | hgsv_sv_discovery/                         | 0
 FOLDER | phase1/                                    | 0
 FOLDER | phase3/                                    | 0
 FOLDER | pilot_data/                                | 0
 FOLDER | release/                                   | 0
 FOLDER | sequence_indices/                          | 0
 FOLDER | technical/                                 | 0
```

### Download files from a data link

Run `seqera data-links download -h` to view all the required and optional fields for downloading files. You can identify the data link by ID, name, or URI:

```console
# By ID
$ seqera data-links download -i v1-user-abc123 -c my-creds -p data/sample.csv -o ./sample.csv

# By name
$ seqera data-links download -n my-bucket -c my-creds -p data/sample.csv -o ./sample.csv

# By URI
$ seqera data-links download --uri s3://my-bucket -c my-creds -p data/sample.csv -o ./sample.csv
```

### Upload files to a data link

Run `seqera data-links upload -h` to view all the required and optional fields for uploading files. You can identify the data link by ID, name, or URI:

```console
# By ID
$ seqera data-links upload -i v1-user-abc123 -c my-creds -p data/results.csv -f ./results.csv

# By name
$ seqera data-links upload -n my-bucket -c my-creds -p data/results.csv -f ./results.csv

# By URI
$ seqera data-links upload --uri s3://my-bucket -c my-creds -p data/results.csv -f ./results.csv
```

## Labels

Run `seqera labels -h` to view supported label operations.

Labels allow you to organize and categorize resources in a workspace. There are two types of labels:

- **Simple labels**: Just a name (e.g., `production`, `test`)
- **Resource labels**: A key-value pair (e.g., `env=production`, `project=rnaseq`)

### List labels

Run `seqera labels list -h` to view all the optional fields for listing labels in a workspace.

```console
$ seqera labels list -w seqeralabs/showcase

  Labels at [seqeralabs / showcase] workspace:

     ID              | Name         | Value    | Type
    -----------------+--------------+----------+----------
     97027588903667  | awesome-label|          | simple
     250670995082875 | new-label    |          | simple
     232243090533688 | res-label    | aaaa     | resource

  Showing from 0 to 99 from a total of 3 entries.
```

### Add a label

Run `seqera labels add -h` to view all the required and optional fields for adding a label to a workspace.

```console
$ seqera labels add -n my-label -w seqeralabs/showcase

  Label 'my-label' (id: 12345) added at [seqeralabs / showcase] workspace
```

To add a resource label with a value:

```console
$ seqera labels add -n env -v production -w seqeralabs/showcase

  Label 'env=production' (id: 12346) added at [seqeralabs / showcase] workspace
```

### Update a label

Run `seqera labels update -h` to view all the required and optional fields for updating a label in a workspace.

```console
$ seqera labels update -i 12345 -n renamed-label -w seqeralabs/showcase

  Label 'renamed-label' updated at [seqeralabs / showcase] workspace
```

### Delete a label

Run `seqera labels delete -h` to view all the required and optional fields for deleting a label from a workspace.

```console
$ seqera labels delete -i 12345 -w seqeralabs/showcase

  Label '12345' deleted at [seqeralabs / showcase] workspace
```

## Studios

Run `seqera studios -h` to view supported Data Studios operations.

Data Studios provide interactive analysis environments in the Seqera Platform. See [Data Studios][data-studios] for more information.

### List studios

Run `seqera studios list -h` to view all the optional fields for listing studios in a workspace.

```console
$ seqera studios list -w seqeralabs/showcase

  Studios at [seqeralabs / showcase] workspace:

     ID       | Name              | Status   | Template
    ----------+-------------------+----------+------------------
     abc123   | my-jupyter        | running  | Jupyter Notebook
     def456   | my-rstudio        | stopped  | RStudio
```

### View a studio

Run `seqera studios view -h` to view all the required and optional fields for viewing a studio.

```console
$ seqera studios view -i abc123 -w seqeralabs/showcase

  Studio details at [seqeralabs / showcase] workspace:

    ID:         abc123
    Name:       my-jupyter
    Status:     running
    Template:   Jupyter Notebook
    URL:        https://cloud.seqera.io/studios/abc123
```

### Start and stop a studio

Run `seqera studios start -h` or `seqera studios stop -h` to view the fields for starting or stopping a studio.

```console
$ seqera studios start -i abc123 -w seqeralabs/showcase

  Studio 'abc123' starting at [seqeralabs / showcase] workspace
```

### List studio templates

Run `seqera studios templates -h` to view all the optional fields for listing available studio templates in a workspace.

```console
$ seqera studios templates -w seqeralabs/showcase

  Studio templates at [seqeralabs / showcase]:

    ID           Name                           Container
    ----------------------------------------------------------------------
    jupyter      Jupyter Notebook               cr.seqera.io/public/data-studio-jupyter
    vscode       VS Code                        cr.seqera.io/public/data-studio-vscode
    rstudio      RStudio                        cr.seqera.io/public/data-studio-rstudio
```

### Add a studio

Create a new studio session with a template and compute environment.

```console
$ seqera studios add -n my-jupyter-studio -c my-compute-env --template Jupyter -w 12345

  Studio 'abc123' created at [myorg / myworkspace] workspace
```

With additional options:

```console
$ seqera studios add -n my-studio -c my-compute-env --template Jupyter \
    --cpu 4 --memory 16384 --gpu 1 \
    --description "My analysis studio" \
    --labels "project=myproject" \
    --auto-start \
    --lifespan 24 \
    --mount-data-ids v1-user-abc123 \
    --wait RUNNING \
    -w 12345

  Studio 'abc123' created and started at [myorg / myworkspace] workspace
```

Additional studio options:

- `--lifespan`: Hours until the studio automatically stops
- `--mount-data-ids`: Data link IDs to mount (can be specified multiple times)
- `--mount-data`: Data link names to mount (can be specified multiple times)
- `--mount-data-uris`: Data link URIs to mount (can be specified multiple times)
- `--wait`: Wait for the studio to reach a specific status (e.g., `RUNNING`)

### Add a studio from existing

Create a new studio from an existing studio's checkpoint (preserving state/data).

```console
$ seqera studios add-as-new -n cloned-studio -p parent-studio-id -w 12345

  Studio 'def456' created at [myorg / myworkspace] workspace
```

Using a specific checkpoint:

```console
$ seqera studios add-as-new -n cloned-studio --parent-name "My Studio" --parent-checkpoint-id checkpoint-123 -w 12345

  Studio 'def456' created at [myorg / myworkspace] workspace
```

With additional options:

```console
$ seqera studios add-as-new -n cloned-studio -p parent-studio-id \
    --lifespan 48 \
    --mount-data-ids v1-user-abc123 \
    --auto-start \
    --wait RUNNING \
    -w 12345

  Studio 'def456' created and started at [myorg / myworkspace] workspace
```

- `--lifespan`: Hours until the studio automatically stops
- `--mount-data-ids`: Data link IDs to mount (can be specified multiple times)
- `--mount-data`: Data link names to mount (can be specified multiple times)
- `--mount-data-uris`: Data link URIs to mount (can be specified multiple times)
- `--wait`: Wait for the studio to reach a specific status (e.g., `RUNNING`)

### Delete a studio

Run `seqera studios delete -h` to view all the required and optional fields for deleting a studio.

```console
$ seqera studios delete -i abc123 -w seqeralabs/showcase

  Studio 'abc123' deleted at [seqeralabs / showcase] workspace
```

[compute-envs]: https://docs.seqera.io/platform/latest/compute-envs/overview
[credentials]: https://docs.seqera.io/platform/latest/credentials/overview
[git-integration]: https://docs.seqera.io/platform/latest/git/overview
[nextflow-config]: https://www.nextflow.io/docs/latest/config.html#config-syntax
[participant-roles]: https://docs.seqera.io/platform/latest/orgs-and-teams/workspace-management#participant-roles
[shared-workspaces]: https://docs.seqera.io/platform/latest/orgs-and-teams/shared-workspaces
[tower-agent]: https://github.com/seqeralabs/tower-agent
[user-workspaces]: https://docs.seqera.io/platform/latest/orgs-and-teams/workspace-management
[wave-docs]: https://www.nextflow.io/docs/latest/wave.html
[data-explorer]: https://docs.seqera.io/platform/latest/data/data-explorer
[data-studios]: https://docs.seqera.io/platform/latest/data-studios/overview
