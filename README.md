Nextflow Tower CLI
==================

`tw` is Nextflow Tower on the command line. It brings Tower concepts including Pipelines, Actions and Compute Environments to the terminal.

![tw](/tw-screenshot.png)

Nextflow Tower is a full-stack application for the management of data pipelines and compute resources. It enables collaborative data analysis at scale, on-premises or in any cloud. 

The Tower CLI interacts with instances of Tower, providing an interface to launch pipelines, manage cloud resources and administer the application.

The key features of the Tower CLI are:

- **A Nextflow-like experience**: Tower CLI provides a developer friendly environment. Workflows can be launched similar to Nextflow but with the benefits of Tower such as monitoring, logging, resource provisioning, dataset management and collaborative sharing.

- **Infrastructure as Code**: All Tower resources including Workspaces and Compute Environments can be described in a declarative manner using YAML or JSON. This allows a complete definition of an analysis environment that can be versioned and treated as code. It greatly simplifies sharing, re-using and administration.

- **Built on OpenAPI**: Tower CLI interacts using the [Tower API](https://tower.nf/openapi/index.html) which is created using the latest OpenAPI 3.0 specification. Tower CLI provides full control of the application allowing users can get maximum insights into their workflows. 

For more information on Tower, see the [user documentation](https://help.tower.nf) on the Tower Cloud website.

Availability
------------

Tower CLI can be installed on macOS, Windows, and Linux.

It is compatible with [Tower Cloud](https://tower.nf/) and Tower Enterprise versions 21.08 and later. 

Installation
------------

1. Download the version for your OS from the assets in the latest [release](https://github.com/seqeralabs/tower-cli/releases) page.
2. Unzip the binary file.
3. Make the file executable `chmod +x ./tw`.
4. Move the file to a directory on your path `sudo mv tw /usr/local/bin/`.

Configuration
-------------

Define the following environment variables:

* `TOWER_ACCESS_TOKEN`: User access token (mandatory). 
* `TOWER_WORKSPACE_ID`: Workspace id (optional). Defaults to the user workspace.
* `TOWER_API_ENDPOINT`: Tower API URL (optional). Default `api.tower.nf`.

These options can be also be provided using command line options.

*An organization's `TOWER_WORKSPACE_ID`'s are listed as `Ids` on its **Workspaces** page.* 

Getting Started
---------------
This guide covers how to get started with `tw` configuration, provision cloud infrastructure and launch workflows into AWS Batch.

### 1. Install 

Install `tw` as described above. 

### 2. Access token
Create an access token using the [Tower](https://tower.nf/) web interface under the **Profile** / **Your Tokens** page.

### 3. Configuration

In a terminal, export your access token.

```
$ export TOWER_ACCESS_TOKEN=[your access token]
```

### 4. Health check

Confirm the installation, configuration and connection are working correctly.

```
$ tw health

    System health status
    ---------------------------------------+------
     Remote API server connection check    | OK 
     Tower API version check               | OK 
     Authentication API credential's token | OK 
```

### 5. View available commands

Use the `-h` or `--help` option to see command and options.

```
$ tw -h
```

### 5. Add Credentials

To launch pipelines in AWS Batch, first add some credentials to the Tower workspace.

*See the [IAM policy](https://github.com/seqeralabs/nf-tower-aws/tree/master/forge) for Tower Forge for recommendations on AWS Batch permissions.*

```
$ tw credentials create aws -n aws -a <aws access key> -s <aws secret key>
```

### 6. List Credentials

List the credentials available in the workspace.

```
$ tw credentials list
```

### 7. Provision Compute Environment

Create a Compute Environment for AWS Batch with automatic provisioning of cloud compute resources. 

```
$ tw compute-envs create aws -n aws-ce -r eu-west-1 --max-cpus=256 -w s3://<bucket-name> 
```

* This create all the required AWS Batch resources in the AWS Ireland region (eu-west-1) with a maximum total of 256 CPUs. 

* An existing S3 bucket will be used as a work directory. 

* See the [user documentation](https://help.tower.nf/compute-envs/aws-batch/#forge) for complete details of Tower Forge.


### 8. Create a Pipeline

Add a pre-configured workflow that can be reused. 

```
$ tw pipelines create -n sleepy-flow --params=<(echo 'timeout: 60') https://github.com/pditommaso/nf-sleep
```

*The `params` option should be a YAML or JSON file. Here we use a Bash pipe to convert a command into a YAML file automatically.*

Pipelines consists of a workflow repository, launch parameters, and a Compute Environment. When no Compute Environment is specified, the primary CE is used.


### 9. Launch it! :rocket:

Run the workflow.

```
$ tw launch sleepy-flow
```

### 10. Change launch parameters

Launch the workflow with different parameters.

```
$ tw launch sleepy-flow --params=<(echo 'timeout: 30')
```

### 11. Update a Pipeline

Pipelines can be modified with new launch parameters using the `update` command. 

```
$ tw pipelines update -n sleepy-flow --params=<(echo 'timeout: 30')
```

### 12. Launch a workflow directly

It possible to directly launch Git workflows that have not been added to a workspace as Pipelines.

```
$ tw launch nextflow-io/hello
```

### :gift: Bonus: Activate autocomplete 

Install autocomplete in your current session, type `tw pi` then press tab twice.

```
$source <(tw generate-completion)
```

Launch Examples
---------------

The `tw launch` command provides a similar user experience to `nextflow run` with the benefits of using Tower.

### 1. Run a workspace Pipeline with a custom parameters file

```
tw launch sarek --params ./myparams.yaml
```

### 2. Run any Nextflow workflow using the primary Compute Environment

```
$ tw launch nextflow-io/hello 
```

### 3. Run any Nextflow pipeline setting a profile

```
$ tw launch nf-core/sarek --profile test,docker --params ./myparams.yaml
```

### 4. Select a specific Compute Environment

```
$ tw launch nf-core/sarek --compute-env "aws-ce" --profile test,docker
```


Build binary development version
--------------------------------

The Tower CLI is based on a Java native compilation, Micronaut HTTP declarative client and Tower domain classes.

1. Download GraalVM (Java 11 version) from [this link](https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-20.2.0).

2. Install `native-image` tool:

    ```
    $ gu install native-image
    ``` 

3. Create the native client:

    ```
    $ ./gradlew nativeImage
    ```

4. then run

    ```
    $ ./build/graal/tw
    ```

Development
-----------

To force reload SNAPSHOT dependencies run:

```
./gradlew clean build --refresh-dependencies
```

Using non-binary development versions
-------------------------------------

You can run a non-binary development version running the `./tw` script in the root of this repository.

License
-------

[Mozilla Public License v2.0](https://github.com/seqeralabs/tower-cli/blob/master/LICENSE.txt)

### Credits & Links

Thanks to the creators of the following resources for helping in the development of this software.

* [Szymon Stepniak YT tutorial](https://www.youtube.com/watch?v=RPdugI8eZgo)
* [Szymon Stepniak example](https://github.com/wololock/gttp)
* [Mitch Seymour's blog post](https://medium.com/@mitch.seymour/building-native-java-clis-with-graalvm-picocli-and-gradle-2e8a8388d70d)
* [CLI applications with GraalVM Native Image](https://medium.com/graalvm/cli-applications-with-graalvm-native-image-d629a40aa0be)
