# Usage examples

## View available commands

Use the `-h` or `--help` parameter to list the available commands and their associated options.

![`tw --help`](./assets/img/rich_codex/tw-info.svg)

> **TIP**: Use `tw --output=json <command>` to dump and store Tower entities in JSON format.
>
> **TIP**: Use `tw --output=json <command> | jq -r '.[].<key>'`  pipe the command to jq to get specific entries within the JSON output.

## Credentials

To launch Pipelines in a Tower Workspace you will need to add Credentials for:

1. Any Compute Environment(s) you would like to use
2. Git provider e.g. Github
3. (Optional) [Tower agent](https://github.com/seqeralabs/tower-agent) if using a HPC cluster
4. (Optional) Container registries e.g. docker.io

All of these can be added with the `tw credentials add <provider>` command as highlighted in the next section.

> **NOTE**: The default Workspace used by the CLI is the user Workspace. Use the `TOWER_WORKSPACE_ID` environment variable or the `--workspace` parameter to override this behaviour.

### Adding Credentials

```console
$ tw credentials add aws --name=my_aws_creds --access-key=<aws access key> --secret-key=<aws secret key>

  New AWS credentials 'my_aws_creds (1sxCxvxfx8xnxdxGxQxqxH)' added at user workspace
```

### Listing Credentials

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

> **NOTE**: You can add multiple Credentials from the same "provider". For example, for AWS Batch you can add `my_aws_creds_1` as well as `my_aws_creds_2` in the same Workspace.

### Deleting Credentials

```console
$ tw credentials delete --name=my_aws_creds

  Credentials '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
```

## Compute Environments

Tower uses the concept of Compute Environments to define the execution platform where a Pipeline will run. A Compute Environment is composed of Credentials and configuration and storage options related to a particular computing platform.  Comprehensive details for supported Compute Environments can be obtained from the [Tower Usage docs](https://help.tower.nf/22.1/compute-envs/overview/#introduction).

### Adding a Compute Environment

Once Credentials have been added to a Workspace, a Compute Environment (e.g AWS Batch) can be created using those Credentials with automatic provisioning of cloud computing resources via **Tower Forge**:

```console
$ tw compute-envs add aws-batch forge --name=my_aws_ce --credentials=<my_aws_creds_1> --region=eu-west-1 --max-cpus=256 --work-dir=s3://<bucket name> --wait=AVAILABLE

  New AWS-BATCH compute environment 'my_aws_ce' added at user workspace
```

The command above will:

- Use the **Tower Forge** mechanism to automatically manage the AWS Batch resource lifecycle (`forge`)
- Use the Credentials added to the Workspace in the previous section (`--credentials`)
- Create all of the required AWS Batch resources in the AWS Ireland (`eu-west-1`) region
- Provision a maximum of 256 CPUs in the Compute Environment (`--max-cpus`)
- Use an existing S3 bucket to store the work directory when running Nextflow (`--work-dir`)
- Wait until the Compute Environment has been successfully created and is ready to use (`--wait`)

Comprehensive details about the Tower Forge feature are available in the [user documentation](https://help.tower.nf/compute-envs/aws-batch/#forge).

> **NOTE**: Compute Environment creation will fail if you haven't set the appropriate [IAM policies](https://github.com/seqeralabs/nf-tower-aws/tree/master/forge) for Tower Forge when using AWS Batch.

### Deleting a Compute Environment

```console
$ tw compute-envs delete --name=my_aws_ce

  Compute environment '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
```

### Default Compute Environment

It is possible to select a **primary** Compute Environment within a Workspace which will be used by default unless a different Compute Environment is explicitly specified when creating or launching a Pipeline.

```console
$ tw compute-envs primary set --name=my_aws_ce

  Primary compute environment for workspace 'user' was set to 'my_aws_ce (1sxCxvxfx8xnxdxGxQxqxH)'  
```

### Importing/Exporting a Compute Environment

It is possible to export the configuration details for a Compute Environment in JSON format for scripting and reproducibility purposes.

```console
$ tw compute-envs export --name=my_aws_ce my_aws_ce_v1.json

  Compute environment exported into 'my_aws_ce_v1.json' 
```

Similarly, a Compute Environment can easily be imported into a Workspace from a previously exported JSON file.

```console
$ tw compute-envs import --name=my_aws_ce_v1 ./my_aws_ce_v1.json

  New AWS-BATCH compute environment 'my_aws_ce_v1' added at user workspace
```

## Pipelines

A pipeline consists of a workflow repository, launch parameters, and a Compute Environment. Pipelines are used to define pre-configured workflows in a Workspace.

### Adding a Pipeline

Add a pre-configured pipeline to the Launchpad that can be re-used later:

```console
$ tw pipelines add --name=my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params.yaml https://github.com/nextflow-io/rnaseq-nf

 New pipeline 'my_rnaseq_nf_pipeline' added at user workspace
```

The `--params-file` option was used to pass a set of default parameters that will be associated with the pipeline in the Launchpad.

> **NOTE**: The `params-file` option should be a YAML or JSON file.

### Importing/exporting a pipeline

You can export the configuration details of a pipeline in JSON format for scripting and reproducibility purposes.

```console
$ tw pipelines export --name=my_rnaseq_nf_pipeline my_rnaseq_nf_pipeline_v1.json

  Pipeline exported into 'my_rnaseq_nf_pipeline_v1.json' 
```

Similarly, a pipeline can easily be imported into a Workspace from a previously exported JSON file.

```console
$ tw pipelines import --name=my_rnaseq_nf_pipeline_v1 ./my_rnaseq_nf_pipeline_v1.json

  New pipeline 'my_rnaseq_nf_pipeline_v1' added at user workspace
```

### Updating a pipeline

The default launch parameters can be changed using the `update` command:

```console
tw pipelines update --name=my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params_2.yaml
```

## Launching pipelines

### Launching a preconfigured pipeline

When launching a pipeline from the Launchpad, if no custom parameters are passed via the CLI then the defaults set for the pipeline in the Launchpad will be used.

> **NOTE**: Platform CLI users are bound to the same user permissions that apply in the platform UI. Launchpad users can launch pre-configured pipelines in the workspaces they have access to, but they cannot add or run new pipelines. 

```console
$ tw launch my_rnaseq_nf_pipeline 

  Workflow 1XCXxX0vCX8xhx submitted at user workspace.

    https://tower.nf/user/abhinav/watch/1XCXxX0vCX8xhx
```

When using `--wait`, `tw` can exit with one of two exit codes:

- `0`: When the run reaches the desired state.
- `1`: When the run reaches a state that makes it impossible to reach the desired state.

> **TIP**: Use `--wait=SUCCEEDED` if you want the command to wait until the Pipeline execution is complete.

### Launching a pipeline with custom parameters

To launch the pipeline with different parameters:

```console
$ tw launch my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params_2.yaml

  Workflow 2XDXxX0vCX8xhx submitted at user workspace.

    https://tower.nf/user/abhinav/watch/2XDXxX0vCX8xhx
```

### Launching any pipeline

Platform CLI can directly launch pipelines that have not been explicitly added to the Launchpad in a platform workspace by using the full pipeline repository URL:

```console
$ tw launch https://github.com/nf-core/rnaseq --params-file=./custom_rnaseq_params.yaml --compute-env=my_aws_ce --revision 3.8.1 --profile=test,docker  

  Workflow 2XDXxX0vCX8xhx submitted at user workspace.

    https://tower.nf/user/abhinav/watch/2XDXxX0vCX8xhx
```

> **NOTE**: Platform CLI users are bound to the same user permissions that apply in the platform UI. Launch users can launch pre-configured pipelines in the workspaces they have access to, but they cannot add or run new pipelines. 

In the above command:

- Pipeline level parameters are defined within the `custom_rnaseq_params.yaml` file
- Other parameters such as `--profile` and `--revision` can also be specified
- A non-primary Compute Environment has been used to launch the pipeline

## Workspaces

Workspaces provide the context in which a user operates, i.e. to launch workflow executions, define the available resources and to manage who can access/operate on those resources. Workspaces are composed of Pipelines, Runs, Actions, Datasets, Compute Environments and Credentials. Access permissions are controlled through Participants, Collaborators, and Teams.

Comprehensive details about [Users and Workspaces](https://help.tower.nf/22.1/orgs-and-teams/overview/) are available in the Tower Usage docs.

> **NOTE**: This section assumes that you already have access to an organization within Tower.

### Creating Workspaces

In the example below, we create a shared Workspace which can be used for sharing Pipelines across other private Workspaces. Please refer to the Tower usage docs for detailed information about [shared Workspaces](https://help.tower.nf/22.1/orgs-and-teams/shared-workspaces/).

```console
$ tw workspaces add --name=shared-workspace --full-name=shared-workspace-for-all  --org=my-tower-org --visibility=SHARED

  A 'SHARED' workspace 'shared-workspace' added for 'my-tower-org' organization
```

> **NOTE**: By default, a Workspace is set to private when created.

### Listing Workspaces

It is possible to list all the Workspaces in which you are participating

```console
$ tw workspaces list                      

  Workspaces for abhinav user:

     Workspace ID    | Workspace Name   | Organization Name | Organization ID 
    -----------------+------------------+-------------------+-----------------
     26002603030407  | shared-workspace | my-tower-org      | 04303000612070  
```

## Participants

### Listing Participants

```console
$ tw participants list

  Participants for 'my-tower-org/shared-workspace' workspace:

     ID             | Participant Type | Name                        | Workspace Role 
    ----------------+------------------+-----------------------------+----------------
     45678460861822 | MEMBER           | abhinav (abhinav@mydomain.com) | owner          
```

### Adding Participants

To add a new _Collaborator_ to the Workspace, you can use the `add` subcommand, the default role assigned to a _Collaborator_ is `Launch`.

Please refer to the Tower usage docs for detailed information about [collaborators and members](https://help.tower.nf/22.1/orgs-and-teams/workspace-management/).

```console
$ tw participants add --name=collaborator@mydomain.com --type=MEMBER                           

  User 'collaborator' was added as participant to 'shared-workspace' workspace with role 'launch'
```

### Updating Participants

If you would like to update the role of a _Collaborator_, to `ADMIN` or `MAINTAIN`, you can use the `update` subcommand.

```console
$ tw  participants update --name=collaborator@mydomain.com --type=COLLABORATOR --role=MAINTAIN

  Participant 'collaborator@mydomain.com' has now role 'maintain' for workspace 'shared-workspace'
```
