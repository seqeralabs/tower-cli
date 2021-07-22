# Tower CLI app

Proof of concept of Tower command line client app based on Java native compilation, 
Micronaut HTTP declarative client and Tower domain classes  

## Get started 

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

## Configuration

To use the command line you need to define these environment variables:

 1. `TOWER_ACCESS_TOKEN`: (mandatory) the user access token 
 2. `TOWER_WORKSPACE_ID`: (optional) the workspace id. Defaults to user workspace.
 3. `TOWER_URL`: (optional) the Tower API URL. Defaults to `tower.nf` API. 

## Autocomplete

To install the autocomplete only for the current session run:
```
source <(./towr generate-completion) 
```

## Usage examples

Run a workspace defined pipeline with custom parameters file:
```
./towr run sarek -params-file ./myparams.yaml
```

Run any Nextflow pipeline using the primary compute environment:
```
./towr run nextflow-io/hello 
```

Run any Nextflow pipeline setting a profile.
```
./towr run nf-core/sarek -profile test,docker -params-file ./myparams.yaml
```

Select the compute environment that you want to use:
```
./towr run nf-core/sarek -compute-env "aws seqera" -profile test,docker
```



### Credits & Links 

* [Szymon Stepniak YT tutorial](https://www.youtube.com/watch?v=RPdugI8eZgo)
* [Szymon Stepniak example](https://github.com/wololock/gttp)
* [Mitch Seymour's blog post](https://medium.com/@mitch.seymour/building-native-java-clis-with-graalvm-picocli-and-gradle-2e8a8388d70d)
* [CLI applications with GraalVM Native Image](https://medium.com/graalvm/cli-applications-with-graalvm-native-image-d629a40aa0be)
