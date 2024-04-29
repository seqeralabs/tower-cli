# tw CLI commands

> **Note**: The CLI performs operations in the user workspace context by default. Use the `TOWER_WORKSPACE_ID` environment variable or the `--workspace` parameter to specify an organization workspace ID.

Use the `-h` or `--help` parameter to list the available commands and their associated options.

![`tw --help`](./assets/img/rich_codex/tw-info.svg)

For help with a specific subcommand, run the command with `-h` or `--help` appended. For example, `tw credentials add google -h`. 

> **Tip**: Use `tw --output=json <command>` to dump and store Seqera Platform entities in JSON format.
>
> **Tip**: Use `tw --output=json <command> | jq -r '.[].<key>'`  to pipe the command to jq to retrieve specific values in the JSON output. For example, `tw --output=json workspaces list | jq -r '.workspaces[].orgId'` returns the organization ID for each workspace listed.

## Credentials

To launch pipelines in a Seqera workspace, you need [credentials](https://docs.seqera.io/platform/latest/credentials/overview) for:

1. Compute environments
2. Pipeline repository Git providers
3. (Optional) [Tower agent](https://github.com/seqeralabs/tower-agent) — used with HPC clusters
4. (Optional) Container registries, such as docker.io

### Add credentials

Run `tw credentials add -h` to view a list of providers.
Run `tw credentials add <provider> -h` to view the required fields for your provider.

#### Compute environment credentials

Seqera requires credentials to access your cloud compute environments. See the [compute environment page](https://docs.seqera.io/platform/latest/compute-envs/overview) for your cloud provider for more information.

  ```console
  $ tw credentials add aws --name=my_aws_creds --access-key=<aws access key> --secret-key=<aws secret key>

    New AWS credentials 'my_aws_creds (1sxCxvxfx8xnxdxGxQxqxH)' added at user workspace
  ```

#### Git credentials

Seqera requires access credentials to interact with pipeline Git repositories. See [Git integration](https://docs.seqera.io/platform/latest/git/overview) for more information.

  ```console
  $ tw credentials add github -n=my_GH_creds -u=<GitHub username> -p=<GitHub access token>

    New GITHUB credentials 'my_GH_creds (xxxxx3prfGlpxxxvR2xxxxo7ow)' added at user workspace
  ```

#### Container registry credentials

Configure credentials for the Nextflow Wave container service to authenticate to private and public container registries. See the **Container registry credentials** section under [Credentials](https://docs.seqera.io/platform/latest/credentials/overview) for registry-specific instructions. 

> **Note**: Container registry credentials are only used by the Wave container service. See [Wave containers](https://www.nextflow.io/docs/latest/wave.html) for more information.

### List credentials

```console
$ tw credentials list

  Credentials at user workspace:

     ID                     | Provider  | Name                               | Last activity                 
    ------------------------+-----------+------------------------------------+-------------------------------
     1x1HxFxzxNxptxlx4xO7Gx | aws       | my_aws_creds_1                     | Wed, 6 Apr 2022 08:40:49 GMT  
     1sxCxvxfx8xnxdxGxQxqxH | aws       | my_aws_creds_2                     | Wed, 9 Apr 2022 08:40:49 GMT  
     2x7xNsf2xkxxUIxXKxsTCx | ssh       | my_ssh_key                         | Thu, 8 Jul 2021 07:09:46 GMT  
     4xxxIeUx7xex1xqx1xxesk | github    | my_github_cred                     | Wed, 22 Jun 2022 09:18:05 GMT 
```

> **Note**: You can add multiple credentials from the same provider in the same workspace.

### Delete credentials

```console
$ tw credentials delete --name=my_aws_creds

  Credentials '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
```

## Compute environments

Compute environments in Seqera define the execution platform where a pipeline will run. A compute environment is composed of the credentials, configuration, and storage options related to a particular computing platform.  See [Seqera Platform compute environments](https://docs.seqera.io/platform/latest/compute-envs/overview) for more information on supported compute environments.

### Add a compute environment

Run `tw compute-envs add -h` to view the list of supported platforms.
Run `tw compute-envs add <platform> -h` to view the required and optional fields for your platform.

You must add the credentials for your provider before creating your compute environment. 

```console
$ tw compute-envs add aws-batch forge --name=my_aws_ce --credentials=<my_aws_creds_1> --region=eu-west-1 --max-cpus=256 --work-dir=s3://<bucket name> --wait=AVAILABLE

  New AWS-BATCH compute environment 'my_aws_ce' added at user workspace
```

This command will:

- Use **Batch Forge** to automatically manage the AWS Batch resource lifecycle (`forge`)
- Use the credentials previously added to the workspace (`--credentials`)
- Create the required AWS Batch resources in the AWS Ireland (`eu-west-1`) region
- Provision a maximum of 256 CPUs in the compute environment (`--max-cpus`)
- Use an existing S3 bucket to store the Nextflow work directory (`--work-dir`)
- Wait until the compute environment has been successfully created and is ready to use (`--wait`)

See the [compute environment page](https://docs.seqera.io/platform/latest/compute-envs/overview) for your provider for detailed information on Batch Forge and manual compute environment creation.

### Delete a compute environment

```console
$ tw compute-envs delete --name=my_aws_ce

  Compute environment '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
```

### Default compute environment

Select a **primary** compute environment to be used by default in a workspace. You can override the workspace primary compute environment by explicitly specifying an alternative compute environment when you create or launch a pipeline.

```console
$ tw compute-envs primary set --name=my_aws_ce

  Primary compute environment for workspace 'user' was set to 'my_aws_ce (1sxCxvxfx8xnxdxGxQxqxH)'  
```

### Import and export a compute environment

Export the configuration details of a compute environment in JSON format for scripting and reproducibility purposes.

```console
$ tw compute-envs export --name=my_aws_ce my_aws_ce_v1.json

  Compute environment exported into 'my_aws_ce_v1.json' 
```

Similarly, a compute environment can be imported to a workspace from a previously exported JSON file.

```console
$ tw compute-envs import --name=my_aws_ce_v1 ./my_aws_ce_v1.json

  New AWS-BATCH compute environment 'my_aws_ce_v1' added at user workspace
```

## Pipelines

Pipelines define pre-configured workflows in a workspace. A pipeline consists of a workflow repository, launch parameters, and a compute environment. 

### Add a pipeline

Add a pre-configured pipeline to the Launchpad:

```console
$ tw pipelines add --name=my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params.yaml https://github.com/nextflow-io/rnaseq-nf

 New pipeline 'my_rnaseq_nf_pipeline' added at user workspace
```

The optional `--params-file` flag is used to pass a set of default parameters that will be associated with the pipeline in the Launchpad.

> **Note**: The `params-file` must be a YAML or JSON file using [Nextflow configuration file](https://www.nextflow.io/docs/latest/config.html#config-syntax) syntax.

### Import and export a pipeline

Export the configuration details of a pipeline in JSON format for scripting and reproducibility purposes.

```console
$ tw pipelines export --name=my_rnaseq_nf_pipeline my_rnaseq_nf_pipeline_v1.json

  Pipeline exported into 'my_rnaseq_nf_pipeline_v1.json' 
```

Similarly, a pipeline can be imported to a workspace from a previously exported JSON file.

```console
$ tw pipelines import --name=my_rnaseq_nf_pipeline_v1 ./my_rnaseq_nf_pipeline_v1.json

  New pipeline 'my_rnaseq_nf_pipeline_v1' added at user workspace
```

### Update a pipeline

The default launch parameters can be changed with the `update` command:

```console
tw pipelines update --name=my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params_2.yaml
```

## Launch pipelines

### Launch a preconfigured pipeline

If no custom parameters are passed via the CLI during launch, the defaults set for the pipeline in the Launchpad will be used.

> **Note**: tw CLI users are bound to the same user permissions that apply in the Platform UI. Launch users can launch pre-configured pipelines in the workspaces they have access to, but they cannot add or run new pipelines.

```console
$ tw launch my_rnaseq_nf_pipeline 

  Workflow 1XCXxX0vCX8xhx submitted at user workspace.

    https://tower.nf/user/abhinav/watch/1XCXxX0vCX8xhx
```

When using `--wait`, `tw` can exit with one of two exit codes:

- `0`: When the run reaches the desired state.
- `1`: When the run reaches a state that makes it impossible to reach the desired state.

> **Tip**: Use `--wait=SUCCEEDED` if you want the command to wait until the pipeline execution is complete.

### Launch a pipeline with custom parameters

To specify custom parameters during pipeline launch, specify a custom `--params-file`:

```console
$ tw launch my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params_2.yaml

  Workflow 2XDXxX0vCX8xhx submitted at user workspace.

    https://tower.nf/user/abhinav/watch/2XDXxX0vCX8xhx
```

See [Nextflow configuration](https://www.nextflow.io/docs/latest/config.html#config-syntax) for more information.

### Launch an unconfigured pipeline

The CLI can directly launch pipelines that have not been added to the Launchpad in a Seqera workspace by using the full pipeline repository URL:

```console
$ tw launch https://github.com/nf-core/rnaseq --params-file=./custom_rnaseq_params.yaml --compute-env=my_aws_ce --revision 3.8.1 --profile=test,docker  

  Workflow 2XDXxX0vCX8xhx submitted at user workspace.

    https://tower.nf/user/abhinav/watch/2XDXxX0vCX8xhx
```

- Pipeline parameters are defined within the `custom_rnaseq_params.yaml` file
- Other parameters such as `--profile` and `--revision` can also be specified
- A non-primary compute environment has been used to launch the pipeline. Omit `--compute-env` to launch with the workspace default compute environment.

> **Note**: CLI users are bound to the same user permissions that apply in the Platform UI. Launch users can launch pre-configured pipelines in the workspaces they have access to, but they cannot add or run new pipelines.

## Workspaces

Workspaces provide the context in which a user launches workflow executions, defines the available resources, and manages who can access those resources. Workspaces contain pipelines, runs, actions, datasets, compute environments, and credentials. Access permissions are controlled with participants, collaborators, and teams.

See [User workspaces](https://docs.seqera.io/platform/latest/orgs-and-teams/workspace-management) for more information.

> **Note**: This section assumes that you already have access to an organization in Seqera Platform.

### Create a workspace

In the example below, we create a shared workspace to be used for sharing pipelines with other private workspaces. See [Shared workspaces](https://docs.seqera.io/platform/latest/orgs-and-teams/shared-workspaces) for more information.

```console
$ tw workspaces add --name=shared-workspace --full-name=shared-workspace-for-all  --org=my-tower-org --visibility=SHARED

  A 'SHARED' workspace 'shared-workspace' added for 'my-tower-org' organization
```

> **Note**: By default, a workspace is set to private when created.

### List workspaces

List all the workspaces in which you are a participant:

```console
$ tw workspaces list                      

  Workspaces for abhinav user:

     Workspace ID    | Workspace Name   | Organization Name | Organization ID 
    -----------------+------------------+-------------------+-----------------
     26002603030407  | shared-workspace | my-tower-org      | 04303000612070  
```

## Participants

### List participants

```console
$ tw participants list

  Participants for 'my-tower-org/shared-workspace' workspace:

     ID             | Participant Type | Name                        | Workspace Role 
    ----------------+------------------+-----------------------------+----------------
     45678460861822 | MEMBER           | abhinav (abhinav@mydomain.com) | owner          
```

### Add participants

To add a new _collaborator_ to the workspace, use the `add` subcommand. The default role assigned to a _collaborator_ is `Launch`.

See [Participant roles](https://docs.seqera.io/platform/latest/orgs-and-teams/workspace-management#participant-roles) for more information.

```console
$ tw participants add --name=collaborator@mydomain.com --type=MEMBER                           

  User 'collaborator' was added as participant to 'shared-workspace' workspace with role 'launch'
```

### Update participant roles

To update the role of a _Collaborator_ to `ADMIN` or `MAINTAIN`, use the `update` subcommand:

```console
$ tw  participants update --name=collaborator@mydomain.com --type=COLLABORATOR --role=MAINTAIN

  Participant 'collaborator@mydomain.com' has now role 'maintain' for workspace 'shared-workspace'
```
