# Analysis Input
i.e. What should be analyzed. An `AnalysisInputLocation` points to code input SootUp can analyze.
We ship multiple Implementations that can handle different input.

Additionally you can specify a SourceType. This determines what is considered e.g. in the CallGraphs generation.
Further you can specify a List of [BodyInterceptors](bodyinterceptors.md), which will optimize the raw Jimple IR that was transformed from the input.

### Java Runtime
#### Java <=8
The `DefaultRTJaAnalysisInputLocation` points to the rt.jar of the executing JVM.


```java
AnalysisInputLocation inputLocation = new DefaultRTJaAnalysisInputLocation();
JavaView view = new JavaView(inputLocation);
```

To include a different Java Runtime library point to any rt.jar via a `JavaClassPathAnalysisInputLocation` as its a usual .jar file.

#### Java >=9
The `JRTFilesystemAnalysisInputLocation` points to the jigsawed java runtime of the executing JVM.  

```java
AnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation(); 
JavaView view = new JavaView(inputLocation);
```


!!! info  "If you have errors like Java.lang.String, Java.lang.Object, ... you are most likely missing this AnalysisInputLocation."

### Java Bytecode
File-Extensions: `.class, .jar, .war`

The `JavaClassPathAnalysisInputLocation` is the equivalent of the classpath you would pass to the java executable i.e. point to root(s) of package(s).

=== "Directory"
    ```java
    AnalysisInputLocation inputLocation =
            new JavaClassPathAnalysisInputLocation("target/");  // points to
    JavaView view = new JavaView(inputLocation);
    ```

=== ".jar File"
    ```java
    AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation("myCode.jar");
    JavaView view1 = new JavaView(inputLocation);
    
    // if you want to analyze a specific language level of a multi release jar
    AnalysisInputLocation inputLocation =
            new MultiReleaseJarAnalysisInputLocation("myCode.jar", new JavaLanguage(10) );
    JavaView view2 = new JavaView(inputLocation);
    ```

=== ".class File"
    ```java
    // if you omit the package structure while pointing to a file,
    // you have to pass the omitted directories as a parameter
    AnalysisInputLocation inputLocation = new PathBasedAnalysisInputLocation.
                ClassFileBasedAnalysisInputLocation("Banana.class", "packageName.subPackage", SourceType.Application);
    JavaView view = new JavaView(inputLocation);
    ```

=== "Complete class path"
    ```java
    String cp = "myCode.jar" + File.pathSeparator + "dependency.jar" + File.pathSeparator + "target/classes/";
    AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(cp);
    JavaView view = new JavaView(inputLocation);
    ```

### Java Sourcecode
File-Extensions: `.java`

With the `OTFCompileAnalysisInputLocation` you can point directly to .java files or pass a String with Java sourcecode.
The AnalysisInputLocation delegates the data to the `JavaCompiler` and transform the bytecode from the compiler to Jimple.

=== "Single File"
    ```java
    AnalysisInputLocation inputLocation = new OTFCompileAnalysisInputLocation("Banana.java");
    JavaView view = new JavaView(inputLocation);
    ```

=== "Multiple Files"
    ```java
    List<Path> files = Arrays.asList(Paths.get("Apple.java"), Paths.get("Banana.java"));
    AnalysisInputLocation inputLocation = new OTFCompileAnalysisInputLocation(files);
    JavaView view = new JavaView(inputLocation);
    ```
=== "File as String"
    ```java
    String content = "public class B{ }";
    AnalysisInputLocation location = new OTFCompileAnalysisInputLocation("B.java", content );
    JavaView view = new JavaView(location);
    ```

`JavaSourcePathInputLocation` [***experimental!***]{Has huge problems with exceptional flow!} - points to a directory that is the root source directory (containing the package directory structure).

### Jimple
File-Extensions: `.jimple`

The `JimpleAnalysisInputLocation` needs a Path to a .jimple file or a directory.

```java
Path path = Paths.get("Banana.java");
AnalysisInputLocation jimpleLocation = new JimpleAnalysisInputLocation(path);
JavaView view = new JavaView(jimpleLocation);
```

### Android Bytecode
File-Extensions: `.apk`

The `ApkAnalysisInputLocation` is the APK frontend written for Sootup

```java
Path path = Paths.get("Banana.apk");
AnalysisInputLocation inputLocation = new ApkAnalysisInputLocation(path, "", DexBodyInterceptors.Default.bodyInterceptors());
JavaView view = new JavaView(inputLocation);
```
### Android Bytecode with Dex2Jar
File-Extensions: `.apk`

If you prefer to use dex2jar as a base to transform android apps to jimple, you can add the code below to create your own analysis input location.
We used the dependency de.femtopedia.dex2jar:dex2jar:2.4.22 in the given example.
We recommend to use ApkAnalysisInputLocation

```java
Path path = Paths.get("Banana.apk");
AnalysisInputLocation inputLocation = new Dex2JarAnalysisInputLocation(path);
JavaView view = new JavaView(inputLocation);

```

```java
public class Dex2JarAnalysisInputLocation extends ArchiveBasedAnalysisInputLocation {

    public Dex2JarAnalysisInputLocation(@NonNull  Path path, @Nullable SourceType srcType) {
        super(path, srcType);
        String jarPath = dex2jar(path);
        this.path = Paths.get(jarPath);
    }

    private String dex2jar(Path path) {
        String apkPath = path.toAbsolutePath().toString();
        String outDir = "./tmp/";
        int start = apkPath.lastIndexOf(File.separator);
        int end = apkPath.lastIndexOf(".apk");
        String outputFile = outDir + apkPath.substring(start + 1, end) + ".jar";
        Dex2jarCmd.main("-f", apkPath, "-o", outputFile);
        return outputFile;
    }
}
```

### Combining Multiple AnalysisInputLocations
But what if I want to point to multiple AnalysisInputLocations?

```java
AnalysisInputLocation mainJar = new JavaClassPathAnalysisInputLocation("myCode.jar");
AnalysisInputLocation jarA = new JavaClassPathAnalysisInputLocation("dependencyA.jar");
AnalysisInputLocation jarB = new JavaClassPathAnalysisInputLocation("dependencyB.jar");

List<AnalysisInputLocation> inputlocationList = Arrays.asList(mainJar, jarA, jarB);
            
JavaView view = new JavaView(inputlocationList);
```
!!! note "Of course you can combine different types of `AnalysisInputLocation`s as well!"


### Maven Project as Analysis Input in SootUp
This uses `#!shell mvn compile` + `JavaClassPathAnalysisInputLocation` under the hood to include a maven project.
```java
    TODO: let the code sail with the upstream boat to this doc.
```

Unfortunately its harder to extract the path of the binary result of Gradle projects in a unified way for all kinds of models - If you have a solution are looking forward to merge your contribution :-). 

### Java cli arguments to configure SootUp
We created a [Utility](tool_setup.md) that parses a String of java command line arguments and configures SootUp respectively.