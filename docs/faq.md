# Frequently Asked Questions

### How to setup the soot dependency (in Maven, Gradle)?
See [Installation](installation.md).

### How to retreive a SootClass or SootMethod?
See [Getting Started](getting-started.md).

### Is there a github template to quickstart into development with SootUp?
Not yet.

### Exception: Provider "jrt" not found

=== "Shell"

    ```shell
    java.lang.ExceptionInInitializerError
        at inputlocation.sootup.java.bytecode.frontend.JrtFileSystemAnalysisInputLocationTest.getClassSource(JrtFileSystemAnalysisInputLocationTest.java:28)
        ...
    Caused by: java.nio.file.ProviderNotFoundException: Provider "jrt" not found
    ```

To execute SootUp with JavaModules support please make sure you run the code at least the Java9 Runtime.


### How to solve a ConcurrentModificationException?
Copy the Iterator into an intermediate Collection.

=== "Java"

    ```java
    final ControlFlowGraph<?> controlFlowGraph = builder.getControlFlowGraph();
        for (Stmt stmt : Lists.newArrayList(controlFlowGraph)){
            ...
        }
    }
    ```

### How can I visualize a ControlFlowGraph?
There exists a tool, that converts a ControlFlowGraph to the Graphviz Dot Language.

=== "Java"

    ```java
        DotExporter.buildGraph( controlFlowGraph );
    ```
or create a convenient link with the exported controlFlowGraph as HTTP GET Parameter

=== "Java"

    ```java
        DotExporter.createUrlToWebeditor( controlFlowGraph );
    ```

### Is there a way to use code exploration and syntax highlighting features in my IDE for .jimple files?
Try [JimpeLsp](https://github.com/swissiety/JimpleLsp) or the [vscode plugin](https://marketplace.visualstudio.com/items?itemName=swissiety.jimplelsp)

### Is there a way to use syntax highlighting of .jimple in my paper, thesis, ...?
Have a look at [LspLexer4Pygments](https://github.com/swissiety/LspLexer4Pygments). Its the same syntax highlighting you see here in the documentation. You can export it to LaTex as well.

### How to ... add an entry in this list? i.e. Your question is not answered here?
Feel free to start a [Discussion](https://github.com/soot-oss/SootUp/discussions). 
    