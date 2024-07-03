# From Prototype to an intuitive Tool
**How was the parameter order again?**
For a lot of cli tools we see an arbitrary order of cli parameters, different options for giving a working directory etc..
So in the wild you can see a lot from run.sh/run.bat to make files just to reorder arguments to execute a tool.

In SootUp we thought we could help on improving this madness while saving your time.

The command line parser mimics the options the java executable accepts - at least for what is supported by SootUp.
This makes it very simple to just copy the execution paramaters you use for execution, to use them more or less as is four the analysis tool.

### Dependencies
=== "Maven"
    ```maven
    <dependency>
        <groupId>commons-cli</groupId>
        <artifactId>commons-cli</artifactId>
        <version>1.8.0</version>
    </dependency>
    ```

=== "Gradle"
    ```groovy
        implementation("commons-cli:commons-cli:1.8.0")
    ```

### Java Code

    ```java
    
    class SootUpConfiguration{
        // TODO incorporate from downstream
    }
    
    ```

We are happy if you steal the following code to create a tool where the setup is just simple.
