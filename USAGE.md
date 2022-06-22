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

### Credentials

To launch pipelines on AWS Batch, you will need to add credentials to the appropriate Tower Workspace.

**NOTE**: The default workspace is the user workspace, which could be overridden by the `TOWER_WORKSPACE_ID` env variable or the `--workspace` argument for various commands.

#### Adding credentials to a workspace

```bash
$ tw credentials add aws --name=my_aws_creds --access-key=<aws access key> --secret-key=<aws secret key>

  New AWS credentials 'my_aws_creds (1sxCxvxfx8xnxdxGxQxqxH)' added at user workspace
```
> See the [IAM policy](https://github.com/seqeralabs/nf-tower-aws/tree/master/forge) for Tower Forge for recommendations on AWS Batch permissions.

#### Deleting credentials from a workspace

```bash
$ tw credentials delete --name=my_aws_creds

  Credentials '1sxCxvxfx8xnxdxGxQxqxH' deleted at user workspace


### 6. List credentials

List the credentials available in the workspace.

```bash
tw credentials list
```

### 7. Provision a Compute Environment

Create a Compute Environment for AWS Batch with automatic provisioning of cloud computing resources:

```bash
tw compute-envs add aws-batch forge --name=my_aws_ce --region=eu-west-1 --max-cpus=256 --work-dir=s3://<bucket name> --wait=AVAILABLE
```

The above command will create all of the required AWS Batch resources in the AWS Ireland (`eu-west-1`) region with a total of 256 CPUs provisioned in the compute environment. An existing S3 bucket will be used as the work directory when running Nextflow. Also, it will wait until all the resources are AVAILABLE and ready to use.

Comprehensive details about Tower Forge are available in the [user documentation](https://help.tower.nf/compute-envs/aws-batch/#forge).

> If you have multiple credentials matching the same compute environment, you will need to provide the `--credentials-id` obtained by running `tw credentials list`.

### 8. Add a pipeline

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

