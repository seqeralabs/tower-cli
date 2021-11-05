## Tower CLI

Tower command line client allows to manage all the resources available at Tower. 

### Installation

Tower CLI is distributed as a single binary exacutable file. Just execute it after downloading your platform binary.

1. Download the latest release binary from [here](https://github.com/seqeralabs/tower-cli/releases)
2. Enable execution permissions (`chmod +x ./tw` on Mac and Linux platforms)
3. (OPTIONAL) Move it into a folder that is in your path.

### Quick start

Before running the CLI:
- You need to create a **personal access token** at Tower. See [here](https://help.tower.nf/api/overview/#authentication).

Running the CLI using your token:
```
./tw -t <your personal access token> ...
```

### Tips

- To prevent passing your personal access token at every execution it is possible to define a `TOWER_ACCESS_TOKEN` environment variable.
- If you are using an on premises Tower installation you can set the API url using `TOWER_API_ENDPOINT` environment variable or the `--url` option.
- You can set the workspace using the `TOWER_WORKSPACE_ID` environment variable.

### Autocomplete

To install the autocomplete only for the current session run:

```
source <(tw generate-completion) 
```

To permanently add the autocomplete add the previous line to the init script of your shell manager (ie: `~/.bashrc`, `~/.zshrc` ...) 

## Usage

### Step by step

Define Tower token and server URL (you can add this variable at your `.bashrc` profile if you do not want to set them on each session):

```
export TOWER_ACCESS_TOKEN=[your personal token]
```

First test that it is working

```
tw -h
```

Run a health check to verify that the CLI can connect to Tower

```
tw health
```

Install the autocomplete on your current session

```
source <(tw generate-completion)
```

Check available create credentials commands

```
tw credentials create -h
```

Check help and create AWS credentials

```
tw credentials create aws -h
tw credentials create aws -n aws -a <your aws access key> -s <your aws secret key> 
tw credentials list
```

Create an AWS compute environment with automatic provisioning of compute resources

```
tw compute-envs create aws -n demo -r eu-west-1 -w s3://nextflow-ci/jordeu --max-cpus=123 --fusion
```

Create a new pipeline at launchpad
_NOTE: the params option has to be a file. Here we are using Bash pipes to convert a command output into a file automatically._

```
tw pipelines create -n sleep_one_minute --params=<(echo 'timeout: 60') https://github.com/pditommaso/nf-sleep
```

Run it!

```
tw launch sleep_one_minute
```

Run it with different parameters

```
tw launch sleep_one_minute --params=<(echo 'timeout: 30')
```

Directly launch a Github pipeline not defined at launchpad

```
tw launch nextflow-io/hello
```

Update a launchpad pipeline

```
tw pipelines update -n sleep_one_minute --params=<(echo 'timeout: 30')
```

### Launch command

Run a workspace defined pipeline with custom parameters file:

```
tw launch sarek --params ./myparams.yaml
```

Run any Nextflow pipeline using the primary compute environment:

```
tw launch nextflow-io/hello 
```

Run any Nextflow pipeline setting a profile.

```
tw launch nf-core/sarek --profile test,docker --params ./myparams.yaml
```

Select a compute environment that you want to use:

```
tw launch nf-core/sarek --compute-env "aws seqera" --profile test,docker
```

