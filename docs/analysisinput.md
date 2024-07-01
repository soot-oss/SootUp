# Analysis Input
i.e. What should be analyzed - an `AnalysisInputLocation` points to code input SootUp can analyze.
We ship multiple Subclasses that can handle different code input.

You can specify a SourceType - which e.g. determines how far an analysis will go.
Additionally you can specify a List of [BodyInterceptors](bodyinterceptors.md), which will optimize the raw Jimple IR that was transformed from the input.

### Java Runtime
points to the runtime library of the executing JVM.
 
- Java <=8: `DefaultRTJaAnalysisInputLocation` points to the rt.jar of the executing JVM.
- To include a different Java Runtime library point to any rt.jar via a `JavaClassPathAnalysisInputLocation` as its a usual .jar file.
- Java >=9: `JRTFilesystemAnalysisInputLocation` points to the jigsawed java runtime of the executing jvm.  

If you have errors like Java.lang.String, Java.lang.Object, ... you are most likely missing this AnalysisInput.

### Java Bytecode
File-Extensions: `.class, .jar, .war`

- `JavaClassPathAnalysisInputLocation` - its the equivalent of the classpath you would pass to the java executable i.e. point to root(s) of package(s).

### Java Sourcecode
File-Extensions: `.java`

- `OTFCompileAnalysisInputLocation` - you can point directly to .java files or pass a String with Java sourcecode, SootUp delegates to the `JavaCompiler` and transform the bytecode from the compiler to Jimple
- `JavaSourcePathInputLocation` [***experimental!***]{Has huge problems with exceptional flow!} - points to a directory that is the root source directory (containing the package directory structure).

### Jimple
File-Extensions: `.jimple`

- `JimpleAnalysisInputLocation` - needs a Path to a .jimple file or a directory.

### Android Bytecode
File-Extensions: `.apk`

- `ApkAnalysisInputLocation` - currently uses dex2jar internally - A SootUp solution to directly generate Jimple is WIP!


### Java cli arguments to configure SootUp
We created a [Utility](tool_setup.md) that parses a String of java command line arguments and configures SootUp respectively.