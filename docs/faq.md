# Frequently Asked Questions

### How to setup the soot dependency (in Maven, Gradle)?
See [Installation](installation.md).

### How to retreive a SootClass or SootMethod?
See [Getting Started](getting-started.md).

### Is there a github template to quickstart into development with SootUp?
Not yet.

### Error while using the Sourcecodefrontend
Please make sure to use Java8 to execute. 

### Exception: Provider "jrt" not found
```shell
java.lang.ExceptionInInitializerError
	at sootup.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocationTest.getClassSource(JrtFileSystemAnalysisInputLocationTest.java:28)
	...
Caused by: java.nio.file.ProviderNotFoundException: Provider "jrt" not found
```
To execute SootUp with JavaModules support please make sure you run the code at least the Java9 Runtime.


### How to solve a ConcurrentModificationException?
Copy the Iterator into an intermediate Collection. 
```java
final StmtGraph<?> stmtGraph = builder.getStmtGraph();
    for (Stmt stmt : Lists.newArrayList(stmtGraph)){
        ...
    }
}
```

### How can I visualize a StmtGraph?
There exists a tool, that converts a StmtGraph to the Graphviz Dot Language.
```java
DotExporter.buildGraph( stmtgraph );
```
or create a convenient link with the exported stmtgraph as HTTP GET Parameter
```java
DotExporter.createUrlToWebeditor( stmtgraph );
```


### Is there a way to use code exploration and syntax highlighting features in my IDE for .jimple files?
Try [JimpeLsp](https://github.com/swissiety/JimpleLsp).

### Is there a way to use syntax highlighting of .jimple in my paper, thesis, ...?
Have a look at [LspLexer4Pygments](https://github.com/swissiety/LspLexer4Pygments).

### How to ... add an entry in this list? i.e. Your question is not answered here?
Feel free to start a [Discussion](https://github.com/soot-oss/SootUp/discussions). 
    