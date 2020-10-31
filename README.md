# Tower CLI app

Proof of concept of Tower command line client app based on Java native compilation, 
Micronaut HTTP declarative client and Tower domain classes  

## Get started 

1. Download GraalVM (Java 11 version) from [this link](https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-20.2.0). 

2. Install `native-image` tool:

    ```
    gu install native-image
    ``` 

3. Compile and run to record execution analysis with `native-image-agent`: 

    ``` 
    ./gradlew run
    ```

4. Create the native client: 

    ```
     ./gradlew nativeImage
    ```

5. then run 

    ```
    ./build/graal/towr
    ```


### Credits & Links 

* [Szymon Stepniak YT tutorial](https://www.youtube.com/watch?v=RPdugI8eZgo)
* [Szymon Stepniak example](https://github.com/wololock/gttp)
* [Mitch Seymour's blog post](https://medium.com/@mitch.seymour/building-native-java-clis-with-graalvm-picocli-and-gradle-2e8a8388d70d)
