# Usage examples

## View available commands

Use the `-h` or `--help` parameter to list the available commands and their associated options.

```console
$ tw --help

Nextflow Tower CLI.

Options:
  -t, --access-token=<token>   Tower personal access token (TOWER_ACCESS_TOKEN).
  -u, --url=<url>              Tower server API endpoint URL (TOWER_API_ENDPOINT) [default: 'tower.nf'].
  -o, --output=<output>        Show output in defined format (only the 'json' option is available at the moment).
  -v, --verbose                Show HTTP request/response logs at stderr.
      --insecure               Explicitly allow to connect to a non-SSL secured Tower server (this is not recommended).
  -h, --help                   Show this help message and exit.
  -V, --version                Print version information and exit.

Commands:
  actions              Manage actions.
  collaborators        Manage organization collaborators.
  compute-envs         Manage workspace compute environments.
  credentials          Manage workspace credentials.
  datasets             Manage datasets.
  generate-completion  Generate bash/zsh completion script for tw.
  info                 System info and health status.
  launch               Launch a Nextflow pipeline execution.
  members              Manage organization members.
  organizations        Manage organizations.
  participants         Manage workspace participants.
  pipelines            Manage workspace pipeline launchpad.
  runs                 Manage workspace pipeline runs.
  teams                Manage organization teams.
  workspaces           Manage workspaces.
```

**TIP**: Use `tw --output=json <command>` to dump and store Tower entities in JSON format.

### Credentials

To launch pipelines in a Workspace you will need to add Credentials for:

1. Any Compute Environment(s) you would like to use
2. Git provider (e.g. Github)
3. (Optional) [Tower agent](https://github.com/seqeralabs/tower-agent) if using a HPC cluster
4. (Optional) Container registries (e.g. docker.io)

All of these can be added with the `tw credentials add <provider>` command as highlighted in the next section.

**NOTE**: The default Workspace used by the CLI is the user Workspace. Use the `TOWER_WORKSPACE_ID` environment variable or the `--workspace` parameter to override this behaviour.

#### Adding Credentials to a Workspace

```console
$ tw credentials add aws --name=my_aws_creds --access-key=<aws access key> --secret-key=<aws secret key>

  New AWS credentials 'my_aws_creds (1sxCxvxfx8xnxdxGxQxqxH)' added at user workspace
```

#### List Credentials



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

**NOTE**: You can add multiple Credentials from the same "provider". For example, you can add `my_aws_creds_1` as well as `my_aws_creds_2` in the same Workspace.

#### Deleting Credentials from a Workspace

```console
$ tw credentials delete --name=my_aws_creds

  Credentials '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
```

### Compute Environments

Tower uses the concept of Compute Environments to define the execution platform where a pipeline will run, a Compute Environment is composed of Credentials, configuration settings, and storage options related to a computing platform. They are used to configure and manage computing platforms where workflows are executed.

Comprehensive details for supported Compute Environments can be obtained from the [Tower Usage docs](https://help.tower.nf/22.1/compute-envs/overview/#introduction).

#### Adding a Compute Environment to a Workspace

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

Comprehensive details about Tower Forge are available in the [user documentation](https://help.tower.nf/compute-envs/aws-batch/#forge).

**NOTE**: Compute Environment creation will fail if you haven't set the appropriate [IAM policies](https://github.com/seqeralabs/nf-tower-aws/tree/master/forge) for Tower Forge when using AWS Batch.

#### Deleting a Compute Environment from a Workspace

```bash
$ tw compute-envs delete --name=my_aws_ce

  Compute environment '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
```

#### Default compute-env in a workspace

It is possible to select a **primary** compute-env within a workspace, which would be used by default if a different compute-env hasn't been specified.

```bash
$ tw compute-envs primary set --name=my_aws_ce

  Primary compute environment for workspace 'user' was set to 'my_aws_ce (1sxCxvxfx8xnxdxGxQxqxH)'  

```


#### Importing/Exporting a compute-env

Using the `tw` CLI, it is possible to export and import a compute-env for reproducibility and versioning purposes.

```bash
$ tw compute-envs export --name=my_aws_ce my_aws_ce_v1.json

  Compute environment exported into 'my_aws_ce_v1.json' 
```
Similarly, a compute-env can be imported into a workspace from a previously exported `JSON` file

```bash
$ tw compute-envs import --name=my_aws_ce_v1 ./my_aws_ce_v1.json

  New AWS-BATCH compute environment 'my_aws_ce_v1' added at user workspace
```

### Pipelines

A Pipeline is composed of a workflow repository, launch parameters, and a Compute Environment. Pipelines are used to define frequently used pre-configured workflows in a Workspace.

#### Adding a pipeline (with preset defaults) to launchpad 

Add a pre-configured pipeline to the launchpad that can be re-used later:

```bash
$ tw pipelines add --name=my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params.yaml https://github.com/nextflow-io/rnaseq-nf

 New pipeline 'my_rnaseq_nf_pipeline' added at user workspace
```

The `--params-file` option was used to pass the pipeline parameters and set those as default.

**NOTE**: The `params-file` option should be a YAML or JSON file.

#### Importing/Exporting a pipeline within a workspace

Using the `tw` CLI, it is possible to export and import a pipeline for reproducibility and versioning purposes.

```bash
$ tw pipelines export --name=my_rnaseq_nf_pipeline my_rnaseq_nf_pipeline_v1.json

  Pipeline exported into 'my_rnaseq_nf_pipeline_v1.json' 

```

Similarly, a pipeline can be imported into a workspace from a previously exported `JSON` file

```bash
$ tw pipelines import --name=my_rnaseq_nf_pipeline_v1 ./my_rnaseq_nf_pipeline_v1.json

  New pipeline 'my_rnaseq_nf_pipeline_v1' added at user workspace
```


#### Update the pipeline defaults

The default launch parameters can be changed using the `update` command:

```bash
$ tw pipelines update --name=my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params_2.yaml
```

### Launch


#### Launching a preset pipeline

While launching a launchpad pipeline, if no custom pipeline-parameters are passed then the preset defaults are used.

```bash
$ tw launch my_rnaseq_nf_pipeline 

  Workflow 1XCXxX0vCX8xhx submitted at user workspace.

    https://tower.nf/user/abhinav/watch/1XCXxX0vCX8xhx

```

**TIP**: Add a `--wait=SUCCEEDED` if you want the command to wait until the pipeline execution is complete.

When using `--wait`, `tw` can exit with one of two exit codes:

- `0`: When the run reaches the desired state.
- `1`: When the run reaches a state that makes it impossible to reach the desired state.

#### Launch a pipeline with custom parameters

Launch the pipeline with different parameters

```bash
$ tw launch my_rnaseq_nf_pipeline --params-file=my_rnaseq_nf_pipeline_params_2.yaml

  Workflow 2XDXxX0vCX8xhx submitted at user workspace.

    https://tower.nf/user/abhinav/watch/2XDXxX0vCX8xhx

```

#### Quicklaunch any pipeline 

It is also possible to directly launch pipelines that have not been explicitly added to the Launchapd in a Tower Workspace by using the full pipeline repository URL:

```bash
$ tw launch https://github.com/nf-core/rnaseq --params-file=./custom_rnaseq_params.yaml --compute-env=my_aws_ce --revision 3.8.1 --profile=test,docker  

  Workflow 2XDXxX0vCX8xhx submitted at user workspace.

    https://tower.nf/user/abhinav/watch/2XDXxX0vCX8xhx

```

In the above command:
- Pipeline level parameters are within the `custom_rnaseq_params.yaml` file
- Other parameters such as `--profile` and `--revision` can also be specified
- A non-primary compute-env has been used to launch the pipeline


### Workspaces

Workspaces provide the context in which a user operates, i.e. launch workflow executions, and defines what resources are available/accessible and who can access/operate on those resources. They are are composed of Pipelines, Runs, Actions, Datasets, Compute Environments and Credentials. Access permissions are controlled through Participants, Collaborators, and Teams.

Comprehensive details about [Users and Workspaces](https://help.tower.nf/22.1/orgs-and-teams/overview/) are available in the Tower Usage docs.

**NOTE**: This section assumes that you already have access to an organization within Tower.

#### Creating a new workspace

In the example below, we create a shared workspace which can be used for sharing pipelines across other private workspaces. For detailed information about [shared workspaces](https://help.tower.nf/22.1/orgs-and-teams/shared-workspaces/) please refer the Tower Usage docs.

**NOTE**: By default, a private workspace is created.


```bash
$ tw workspaces add --name=shared-workspace --full-name=shared-workspace-for-all  --org=my-tower-org --visibility=SHARED

  A 'SHARED' workspace 'shared-workspace' added for 'my-tower-org' organization

```



#### List all workspaces

It is possible to list all the workspaces in which you are participating

```bash

$ tw workspaces list                      


  Workspaces for abhinav user:

     Workspace ID    | Workspace Name   | Organization Name | Organization ID 
    -----------------+------------------+-------------------+-----------------
     26002603030407  | shared-workspace | my-tower-org      | 04303000612070  


```


### Participants

#### List the participants of a workspace

```bash
$ tw participants list

  Participants for 'my-tower-org/shared-workspace' workspace:

     ID             | Participant Type | Name                        | Workspace Role 
    ----------------+------------------+-----------------------------+----------------
     45678460861822 | MEMBER           | abhinav (abhinav@mydomain.com) | owner          

```


#### Add new participant to a workspace

To add a new _Collaborator_ to the workspace, you can use the `add` subcommand, the default role assigned to a _Collaborator_ is `Launch`.

For detailed information about [collaborators and members](https://help.tower.nf/22.1/orgs-and-teams/workspace-management/) please refer the Tower Usage docs.


```bash
$ tw participants add --name=collaborator@mydomain.com --type=MEMBER                           


  User 'collaborator' was added as participant to 'shared-workspace' workspace with role 'launch'


```

#### Update a participant role within the workspace

If you'd like to update the role of a _Collaborator_, to `ADMIN` or `MAINTAIN`, you can use the `update` subcommand.

```bash
$ tw  participants update --name=collaborator@mydomain.com --type=COLLABORATOR --role=MAINTAIN

  Participant 'collaborator@mydomain.com' has now role 'maintain' for workspace 'shared-workspace'

```

