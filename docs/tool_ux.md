# Prototype to user friendly tool

`How was the parameter order again?`
For a lot of cli tools we see an arbitrary order of cli parameters, different options for giving a working directory etc..
So in the wild you can see a lot from run.sh/run.bat to make files just to reorder arguments and calling a tool.

In SootUp we thought we could help on improving this madness while saving your time.

The command line parser mimics the options the java executable accepts - at least for what is supported by SootUp.

Dependencies:
```maven
<dependency>
    <groupId>commons-cli</groupId>
    <artifactId>commons-cli</artifactId>
    <version>1.8.0</version>
</dependency>

```

```gradle
    implementation("commons-cli:commons-cli:1.8.0")
```

```java

class SootUpConfiguration{
    // TODO incorporate
}

```

We are happy if you steal the following code to create a tool where the setup is just simple.
