# Tower CLI app

Proof of concept of Tower command line client app based on Java native compilation, Micronaut HTTP declarative client and Tower domain classes

## Installation

From the last Github action at artifacts section, download the binary for your OS. Unzip it, give it execution permissions with `chmod +x ./towr` and move it into a folder that it's in your path (ex: `sudo mv ./towr /usr/local/bin/towr`)

## Build binary development version

1. Download GraalVM (Java 11 version) from [this link](https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-20.2.0).

2. Install `native-image` tool:

    ```
    gu install native-image
    ``` 

3. Create the native client:

    ```
     ./gradlew nativeImage
    ```

4. then run

    ```
    ./build/graal/towr
    ```

## Run non-binary development version

You can run a non-binary development version running the script `./towr` at the root of this repository.

## Configuration

To use the command line you need to define these environment variables (they also can be provided using command line options):

1. `TOWER_ACCESS_TOKEN`: (mandatory) the user access token
2. `TOWER_WORKSPACE_ID`: (optional) the workspace id. Defaults to user workspace.
3. `TOWER_API_ENDPOINT`: (optional) the Tower API URL. Defaults to `api.tower.nf` API.

## Autocomplete

To install the autocomplete only for the current session run:

```
source <(towr generate-completion) 
```

## Tutorial

Define Tower token and server URL (you can add this variable at your `.bashrc` profile if you do not want to set them on each session):

```
export TOWER_ACCESS_TOKEN=[your personal token]
export TOWER_SERVER_URL=https://scratch.staging-tower.xyz/api 
```

_NOTE: If you don't set the `TOWER_WORKSPACE_ID` the user workspace will be used._

First test that it is working

```
towr -h
```

Install the autocomplete on your current session

```
source <(towr generate-completion)
```

Check available create credentials commands

```
towr credentials create -h
```

Check help and create AWS credentials

```
towr credentials create aws -h
towr credentials create aws -n aws -a <your aws access key> -s <your aws secret key> 
towr credentials list
```

Create an AWS compute environment with automatic provisioning of compute resources

```
towr compute-envs create aws -n demo -r eu-west-1 -w s3://nextflow-ci/jordeu --max-cpus=123 --fusion
```

Create a new pipeline at launchpad
_NOTE: the params option has to be a file. Here we are using Bash pipes to convert a command output into a file automatically._

```
towr pipelines create -n sleep_one_minute --params=<(echo 'timeout: 60') https://github.com/pditommaso/nf-sleep
```

Run it!

```
towr launch sleep_one_minute
```

Run it with different parameters

```
towr launch sleep_one_minute --params=<(echo 'timeout: 30')
```

Directly launch a Github pipeline not defined at launchpad

```
towr launch nextflow-io/hello
```

Update a launchpad pipeline

```
towr pipelines update -n sleep_one_minute --params=<(echo 'timeout: 30')
```

## Launch usage examples

Run a workspace defined pipeline with custom parameters file:

```
towr launch sarek --params ./myparams.yaml
```

Run any Nextflow pipeline using the primary compute environment:

```
towr launch nextflow-io/hello 
```

Run any Nextflow pipeline setting a profile.

```
towr launch nf-core/sarek --profile test,docker --params ./myparams.yaml
```

Select the compute environment that you want to use:

```
towr launch nf-core/sarek --compute-env "aws seqera" --profile test,docker
```

### Development

To force reload SNAPSHOT dependencies run:

```
./gradlew clean build --refresh-dependencies
```

### Credits & Links

* [Szymon Stepniak YT tutorial](https://www.youtube.com/watch?v=RPdugI8eZgo)
* [Szymon Stepniak example](https://github.com/wololock/gttp)
* [Mitch Seymour's blog post](https://medium.com/@mitch.seymour/building-native-java-clis-with-graalvm-picocli-and-gradle-2e8a8388d70d)
* [CLI applications with GraalVM Native Image](https://medium.com/graalvm/cli-applications-with-graalvm-native-image-d629a40aa0be)
