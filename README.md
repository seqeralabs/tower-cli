Nextflow Tower CLI
==================

`tw` is Nextflow Tower on the command line. It brings Tower concepts including Pipelines, Actions and Compute Environments to the terminal.

![tw](/tw-screenshot.png)

Nextflow Tower is a full-stack application for the management of data pipelines and compute resources. It enables collaborative data analysis at scale, on-premises or in any cloud. 

The Tower CLI interacts with instances of Tower, providing an interface to launch pipelines, manage cloud resources and administer the application.

The key features of the Tower CLI are:

- **A Nextflow-like experience**: Tower CLI provides a developer friendly environment. Workflows can be launched similar to Nextflow but with the benefits of Tower such as monitoring, logging, resource provisioning, dataset management and collaborative sharing.

- **Infrastructure as Code**: All Tower resources including Pipeines and Compute Environments can be described in a declarative manner. This allows a complete definition of an analysis environment that can be versioned and treated as code. It greatly simplifies sharing, re-using and administration.

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
This guide covers how to get started with `tw`. We will start configure the CLI, provision cloud infrastructure and launch workflows into AWS Batch.

**1. Install** 

Install `tw` as described above. 

**2. Access token**

Create an access token using the [Tower](https://tower.nf/) web interface under the **Profile** / **Your Tokens** page.

**3. Configuration**

In a terminal, export your access token.

    $ export TOWER_ACCESS_TOKEN=[your access token]

**4. Health check**

Confirm the installation, configuration and connection are working correctly.

    $ tw health
    
        System health status
        ---------------------------------------+------
         Remote API server connection check    | OK 
         Tower API version check               | OK 
         Authentication API credential's token | OK 

**5. View available commands**

Use the `-h` or `--help` option to see command and options.

    $ tw -h

**6. Add Credentials**

To launch pipelines in AWS Batch, first add some credentials to the Tower workspace.

*See the [IAM policy](https://github.com/seqeralabs/nf-tower-aws/tree/master/forge) for Tower Forge for recommendations on AWS Batch permissions.*

    $ tw credentials create aws -n aws -a <aws access key> -s <aws secret key>

**7. List Credentials**

List the credentials available in the workspace.

    $ tw credentials list

**8. Provision Compute Environment**

Create a Compute Environment for AWS Batch with automatic provisioning of cloud compute resources. 

    $ tw compute-envs create aws -n aws-ce -r eu-west-1 --max-cpus=256 -w s3://<bucket-name>

* This create all the required AWS Batch resources in the AWS Ireland region (eu-west-1) with a maximum total of 256 CPUs. 

* An existing S3 bucket will be used as a work directory. 

* See the [user documentation](https://help.tower.nf/compute-envs/aws-batch/#forge) for complete details of Tower Forge.

**9. Create a Pipeline**

Add a pre-configured workflow that can be reused. 

    $ tw pipelines create -n sleepy-flow --params=<(echo 'timeout: 60') https://github.com/pditommaso/nf-sleep

*The `params` option should be a YAML or JSON file. Here we use a Bash pipe to convert a command into a YAML file automatically.*

Pipelines consists of a workflow repository, launch parameters, and a Compute Environment. When no Compute Environment is specified, the primary CE is used.

**10. Launch it!**

    $ tw launch sleepy-flow

**11. Change launch parameters**

Launch the workflow with different parameters.

    $ tw launch sleepy-flow --params=<(echo 'timeout: 30') 

**12. Update a Pipeline**

Pipelines can be modified with new launch parameters using the `update` command. 

    $ tw pipelines update -n sleepy-flow --params=<(echo 'timeout: 30')

**13. Launch a workflow directly**

It possible to directly launch Git workflows that have not been added to a workspace as Pipelines.

    $ tw launch nextflow-io/hello


Launch Examples
---------------

The `tw launch` command provides a similar user experience to `nextflow run` with the benefits of using Tower.

1. Run a workspace Pipeline with a custom parameters file.

    ```
    $ tw launch sarek --params ./myparams.yaml
    ```

2. Run any Nextflow workflow using the primary Compute Environment.

    ```
    $ tw launch nextflow-io/hello
    ```

3. Run any Nextflow pipeline setting a profile.

    ```
    $ tw launch nf-core/sarek --profile test,docker --params ./myparams.yaml
    ```

4. Select a specific Compute Environment.

    ```
    $ tw launch nf-core/sarek --compute-env "aws-ce" --profile test,docker
    

Activate autocomplete
---------------------

It is possible to activate autocomplete in your current session.

    $ source <(tw generate-completion)



Build binary development versions
--------------------------------

Tower CLI is based on a Java GraalVM native compilation to produce a platform binary executable.

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


Using non-binary development versions
-------------------------------------

You can run a non-binary development version running the `./tw` script in the root of this repository.


License
-------

[Mozilla Public License v2.0](https://github.com/seqeralabs/tower-cli/blob/master/LICENSE.txt)
