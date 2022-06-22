# Usage examples for `tw` CLI


## View available commands

Use the `-h` or `--help` parameter to list the available commands and their associated options.

```bash
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

**TIP**: `tw` CLI can be directed to use JSON format for the output using `tw --output=json <command>` option.

### Credentials

To launch pipelines on any compute environment (for example AWS Batch), you will need to add credentials to the appropriate Tower Workspace. Credentials for git provider (e.g. Github), [tower-agent](https://github.com/seqeralabs/tower-agent) or container registeries (e.g. docker.io) could also be added using the `tw credentials add <provider>` command.

**NOTE**: The default workspace is the user workspace, which could be overridden by the `TOWER_WORKSPACE_ID` env variable or the `--workspace` argument for various commands.

#### Adding credentials to a workspace

```bash
$ tw credentials add aws --name=my_aws_creds --access-key=<aws access key> --secret-key=<aws secret key>

  New AWS credentials 'my_aws_creds (1sxCxvxfx8xnxdxGxQxqxH)' added at user workspace
```

#### List credentials

List the credentials available in the workspace.

**NOTE**: You can add multiple credentials of the same "provider", for example you can add `my_aws_creds_1` as well as `my_aws_creds_2` in the workspace.

```bash
$ tw credentials list


  Credentials at user workspace:

     ID                     | Provider  | Name                               | Last activity                 
    ------------------------+-----------+------------------------------------+-------------------------------
     1x1HxFxzxNxptxlx4xO7Gx | aws       | my_aws_creds_1                     | Wed, 6 Apr 2022 08:40:49 GMT  
     1sxCxvxfx8xnxdxGxQxqxH | aws       | my_aws_creds_2                     | Wed, 9 Apr 2022 08:40:49 GMT  
     2x7xNsf2xkxxUIxXKxsTCx | ssh       | my_ssh_key                         | Thu, 8 Jul 2021 07:09:46 GMT  
     4xxxIeUx7xex1xqx1xxesk | github    | my_github_cred                     | Wed, 22 Jun 2022 09:18:05 GMT 



```


#### Deleting credentials from a workspace

```bash
$ tw credentials delete --name=my_aws_creds

  Credentials '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
```


### Compute Environments


Tower uses the concept of Compute Environments to define the execution platform where a pipeline will run, a Compute Environment is composed of credentials, configuration settings, and storage options related to a computing platform. They are used to configure and manage computing platforms where workflows are executed.

Comprehensive details on supported compute environments can be obtained from [Tower Usage docs](https://help.tower.nf/22.1/compute-envs/overview/#introduction).

#### Adding compute-env to a workspace

Once the credentials have been added to a workspace, a Compute Environment (e.g AWS Batch) can be created using those credentials with automatic provisioning of cloud computing resources via **Tower Forge**:

```bash
$ tw compute-envs add aws-batch forge --name=my_aws_ce --credentials=<my_aws_creds_1> --region=eu-west-1 --max-cpus=256 --work-dir=s3://<bucket name> --wait=AVAILABLE

  New AWS-BATCH compute environment 'my_aws_ce' added at user workspace
```

**NOTE**: See the [IAM policy](https://github.com/seqeralabs/nf-tower-aws/tree/master/forge) for Tower Forge for recommendations on AWS Batch permissions, without which the env creation would fail.

The above command will 
- Use the **Tower Forge** mechanism to automatically manage the AWS Batch resource lifesycle (`forge`)
- Use the credentials previously added to the workspace (`--credentials`)
- Create all of the required AWS Batch resources in the AWS Ireland (`eu-west-1`) region 
- A total of 256 CPUs will be provisined in the compute environment (`--max-cpus`)
- An existing S3 bucket will be used as the work directory when running Nextflow (`--work-dir`)
- Will wait until all the resources are AVAILABLE and ready to use (`--wait`)

Comprehensive details about Tower Forge are available in the [user documentation](https://help.tower.nf/compute-envs/aws-batch/#forge).


#### Deleting compute-env to a workspace

```bash
$ tw compute-envs delete --name my_aws_ce

  Compute environment '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace
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

Add a pre-configured pipeline that can be re-used later:

```bash
tw pipelines add --name=my_sleepy_pipeline --params-file=<(echo 'timeout: 60') https://github.com/pditommaso/nf-sleep
```

Pipelines consist of a pipeline repository, launch parameters, and a Compute Environment. When a Compute Environment is not specified the primary one is used.

> The `params-file` option should be a YAML or JSON file. Here we use a Bash pipe to convert a command into a YAML file automatically.

### 9. Launch it!

```bash
tw launch my_sleepy_pipeline
```

Add a `--wait=SUCCEEDED` if you want the command to wait until the pipeline execution is complete.

When using `--wait`, `tw` can exit with one of two exit codes:

- `0`: When the run reaches the desired state.
- `1`: When the run reaches a state that makes it impossible to reach the desired state.

### 10. Change launch parameters

Launch the pipeline with different parameters:

```bash
tw launch my_sleepy_pipeline --params-file=<(echo 'timeout: 30')
```

### 11. Update a pipeline

The default launch parameters can be changed using the `update` command:

```bash
tw pipelines update --name=my_sleepy_pipeline --params-file=<(echo 'timeout: 30')
```

### 12. Launch a pipeline directly

It is also possible to directly launch pipelines that have not been explicitly added to a Tower Workspace by using the pipeline repository URL:

```bash
tw launch https://github.com/nextflow-io/hello
```

## Launch Examples

The `tw launch` command provides a similar user experience to `nextflow run` with the benefits of using Tower.

1. Run a Pipeline pre-defined in a Tower Workspace with a custom parameters file:

    ```bash
    tw launch my_sleepy_pipeline --params-file=./my_params.yaml
    ```

2. Run any Nextflow pipeline using the primary Compute Environment:

    ```bash
    tw launch nf-core/rnaseq
    ```

3. Run any Nextflow pipeline on a specific Compute Environment:

    ```bash
    tw launch nf-core/rnaseq --compute-env=my_aws_ce
    ```

4. Run any Nextflow pipeline and adjust the default profile and parameters:

    ```bash
    tw launch nf-core/rnaseq --profile=test,docker --params-file=./my_params.yaml --compute-env=my_aws_ce
    ```

