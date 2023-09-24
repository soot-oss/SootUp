# General Usage of SootUp
This page walks you through the core data structures, as well as shows how to get started with SootUp.

## The core datastructures
Before you get started with the SootUp library, it helps to learn about the following core data structures: 

- `Project`: defines the outlines of an analysis. SootUp users should first create a `Project` instance. It is the starting point for all operations. 
  You can define multiple instances of `Project` at the same time and there are no information shared between them. All caches are always at the project level.

- `Language`: represents the programming language of the analyzed code. 

- `AnalysisInputLocation`: points to the target code to be analyzed.

!!! info "Soot Equivalent"

    It corresponds to the `cp` option, which specifies the classpath for Soot to find classes to be analyzed.

- `View`: presents the code/classes under analysis.

!!! info "Soot Equivalent"

    It corresponds to the `Scene` class, but it is not a singleton. So it is possible to instantiate multiple views simultaneously.

- `Scope`: defines the scope of the `View`. By default, the `View` is created with all code found on the `AnalysisInputLocation` specified for the `Project` instance.

- `SootClass`: represents a class loaded into the `View`.

- `SootMethod`: represents a method of a class.

- `SootField`: represents a field of a class.

- `Body`: represents a method body in Jimpe.

- `StmtGraph`: represents the control flow graph of a method body in Jimple statements.

## Creating a Project

You can use bytecode analysis typically when you do not have access to the source code of the target program. Following example shows how to create project for analyzing Java bytecode.

!!! example "Create a project to analyze Java bytecode"

    ~~~java
    AnalysisInputLocation<JavaSootClass> inputLocation = 
            new JavaClassPathAnalysisInputLocation("path2Binary");
            
    JavaLanguage language = new JavaLanguage(8);
    
    Project project = 
            JavaProject.builder(language).addInputLocation(inputLocation).build();
    ~~~

If you have access to the source code, it is also possible to create a project for analyzing source code. Following example shows how to create project for analyzing Java source code.

!!! info "Experimental"

    The source code frontend is experimental and should only be used for testing purposes. You should compile the code for analysis first and use the bytecode frontend instead.  

!!! example "Create a project to analyze Java source code"

    ~~~java
    AnalysisInputLocation<JavaSootClass> inputLocation = 
            new JavaSourcePathAnalysisInputLocation("path2Source");
            
    JavaLanguage language = new JavaLanguage(8);
    
    Project project = 
            JavaProject.builder(language).addInputLocation(inputLocation).build();
    ~~~

If you have a [Jimple](../jimple) file, you can create a project for analyzing jimple code directly. Following example shows how to create project for analyzing jimple code.

!!! example "Create a project to analyze jimple code"

    ~~~java
    Path pathToJimple = Paths.get("path2Jimple");
    
    AnalysisInputLocation<JavaSootClass> inputLocation = 
            new JimpleAnalysisInputLocation(pathToJimple);
    
    Project project = new JimpleProject(inputLocation);
    ~~~

<!---
3. Create a project to analyze Android APK. 

   TODO: add code
--->

## Creating a View


To create an analysis view, you can call the `createView()` method on the `project` object:

```java
JavaView view = project.createView();
```

By default, whenever a class is retrieved, it will be permanently stored in a cache.
If you do not want retrieved classes to be stored indefinetly, you can instead provide a different `CacheProvider` to the created view.
To for example use an `LRUCache` instead, which stores at most 50 classes, and always replaces the least recently used class by a newly retrieved one, use the following call:

```java
JavaView view = project.createView(new LRUCacheProvider(50));
```

## Retrieving a Class

Each class is identified with a unique signature adhering to [Java identifier rules](https://www.geeksforgeeks.org/java-identifiers/), therefore you first need to specify the class signature (`ClassType`) as shown below.

Let's say the following is the target program that we want to analyze:

!!! example "Target Program"
    
    ```java
    package example;
    
    public class HelloWorld {
    
      public HelloWorld() {
      
      }
    
      public static void main(String[] args) {
        HelloWorld hw = new HelloWorld();
        hw.hello();
      }

      public void hello() {

      }
      
    }
    ```

Then, we could define the `ClassType` of the `HelloWorld` class as follows:

!!! example "Defining a ClassType"

    ```java
    ClassType classType = 
            project.getIdentifierFactory().getClassType("example.HelloWorld");
    ```

Once we have a `ClassType` that identifies the `HelloWorld` class, we can use it to retrieve the corresponding `SootClass` object from the `view` as shown below:

!!! example "Retrieving a SootClass"

    ```java
    SootClass<JavaSootClassSource> sootClass =
            (SootClass<JavaSootClassSource>) view.getClass(classType).get();
    ```

## Retrieving a Method
Like the classes, methods also have an identifier which we call `MethodSignature`. For instance, we can define the method signature for identifying the `main` method of the `HelloWorld` class as follows:

!!! example "Defining a MethodSignature"

    ```java
    MethodSignature methodSignature =
        project
            .getIdentifierFactory()
            .getMethodSignature(
                "main", // method name
                classType,
                "void", // return type
                Collections.singletonList("java.lang.String[]")); // args
    ```

Once we have a `MethodSignature` that identifies the `main` method of the `HelloWorld` class, we can use it to retrieve the corresponding `SootMethod` object from the `view` as shown below:

!!! example "Retrieving a SootMethod from the View"

    ```java
    Optional<SootMethod> opt = view.getMethod(methodSignature);
    
    if(opt.isPresent()){
      SootMethod method = opt.get();
    }
    ```
Alternatively, we can also retrieve a `SootMethod` from `SootClass` that contains it.

!!! example "Retrieving a SootMethod from a SootClass"

    ```java
    Optional<? extends SootMethod> opt = sootClass.getMethod(methodSignature.getSubSignature());
    
    if(opt.isPresent()){
      SootMethod method = opt.get();
    }
    ```

## Retrieving the Control-Flow Graph of a Method

Each `SootMethod` contains a Control-Flow Graph (CFG) which is represented via the `StmtGraph`. This structure is usually used for program analysis. You can retrieve the CFG of a `SootMethod` as follows:

!!! example "Retrieving the CFG of a SootMethod"

    ```java
    sootMethod.getBody().getStmts();
    ```



!!! info "Access or Download all of the code used above"

    [BasicSetup.java](https://github.com/secure-software-engineering/soot-reloaded/blob/develop/sootup.examples/src/test/java/sootup/examples/basicSetup/BasicSetup.java)

## SootUp vs Soot

Below we show a comparison of the code so far with the same functionality in sootup.

=== "SootUp"

    ``` java
    AnalysisInputLocation<JavaSootClass> inputLocation =
    new JavaClassPathAnalysisInputLocation("path2Binary");

    JavaLanguage language = new JavaLanguage(8);

    Project project =
        JavaProject.builder(language)
                .addInputLocation(inputLocation).build();

    ClassType classType = 
            project.getIdentifierFactory().getClassType("HelloWorld");

    MethodSignature methodSignature =
        project
            .getIdentifierFactory()
            .getMethodSignature(
                "main", classType, "void",
                Collections.singletonList("java.lang.String[]"));

    View view = project.createView();

    SootClass<JavaSootClassSource> sootClass =
        (SootClass<JavaSootClassSource>) view.getClass(classType).get();

    SootMethod sootMethod = 
            sootClass.getMethod(methodSignature.getSubSignature()).get();
    
    sootMethod.getBody().getStmts();
    ```

=== "Soot"

    ``` java
    G.reset();
    String userdir = System.getProperty("user.dir");
    String sootCp = 
            userdir 
            + File.separator 
            + "target" 
            + File.separator 
            + "test-classes"
            + File.pathSeparator + "lib"+File.separator+"rt.jar";
            
    Options.v().set_soot_classpath(sootCp);
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("cg.cha", "on");
    Options.v().setPhaseOption("cg", "all-reachable:true");
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().set_prepend_classpath(false);

    Scene.v().addBasicClass("java.lang.StringBuilder");
    SootClass c = 
        Scene.v().forceResolve(targetTestClassName, SootClass.BODIES);
    if (c != null) {
        c.setApplicationClass();
    }
    Scene.v().loadNecessaryClasses();
    
    SootMethod method;
    for (SootClass c : Scene.v().getApplicationClasses()) {
        if(c.getName().equals("example.HelloWorld")){
            for (SootMethod m : c.getMethods()) {
                if (!m.hasActiveBody()) {
                    continue;
                }
                if (m.getName().equals("entryPoint")) {
                    method = m;
                    break;
                }
            }
        }
    }
    
    method.getActiveBody().getUnits();
    ```


<!--- ## Perform an Intra-procedural Analysis --->

<!--- 
## Construct Call Graph

## Perform an Inter-procedural Analysis
--->



<!---
## Creating Different Views
1. Create a full view of all classes found in given analysis input location. 
~~~java
  project.createView();
~~~  
2. Create a on-demand view. An on-demand view does not load all classes into the view, but only classes that are specified and their transitive closure. 

   TODO: add code 

3. Create a view based on a defined scope. 
   TODO: add code
   
--->

