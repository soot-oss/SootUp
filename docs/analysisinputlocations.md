# AnalysisInputLocations
An AnalysisInputLocation tells SootUp what code input it should analyze.

### Java Runtime
- Java <9: DefaultRTJaAnalysisInputLocation current rt.jar (or point to any rt.jar as its just a usual .jar file)
- Java >8: JRTFilesystemAnalysisInputLocation

If you have errors like Java.lang.String, Java.lang.Object, ... you are most likely missing this AnalysisInput.

### Java Bytecode .class, .jar, .war
- JavaClassPathAnalysisInputLocation - its the equivalent of the classpath you would pass to the java executable i.e. point to root(s) of package(s).

### Java Sourcecode .java
- OTFCompileAnalysisInputLocation - you can point directly to .java files or pass a String with Java sourcecode
- JavaSourcePathInputLocation - points to a directory that is the root source directory (containing the package directory structure)

### Jimple .jimple
- JimpleAnalysisInputLocation - needs a Path to a .jimple file or a directory.

### Android Bytecode .dex
- ApkAnalysisInputLocation - currenlty uses dex2jar internally - SootUp solution is WIP!


### Java cli arguments to configure SootUp
We created a [Utility](tool_ux.md) that parses a String of java command line arguments and configures SootUp respectively.