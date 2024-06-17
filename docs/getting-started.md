# First Steps with SootUp
Before you get started with the SootUp library, it helps to learn about the following core data structures:

- [`AnalysisInputLocation`]{It corresponds to the `cp` option, which specifies the classpath for Soot to find classes to be analyzed.}
  : points to the target code to be analyzed.

- [`View`]{Corresponds to the `Scene` class, but it is not a singleton. So it is possible to instantiate multiple views simultaneously.}: presents the code/classes under analysis. 

- `SootClass`: represents a class loaded into the `View`.

- `SootMethod`: represents a method of a class.

- `SootField`: represents a field of a class.

- `Body`: represents a method body in Jimple.

- `StmtGraph`: represents the control flow graph of a method body in Jimple statements.

## Creating a View

You can use bytecode analysis typically when you do not have access to the source code of the target program. Following example shows how to create a view for analyzing Java bytecode.

!!! example "Create a view to analyze Java bytecode"

    ~~~java
    AnalysisInputLocation inputLocation = 
            new JavaClassPathAnalysisInputLocation("path2Binary");
            
    JavaView view = new JavaView(inputLocation);
    ~~~

If you have access to the source code, it is also possible to create a view for analyzing source code. Following example shows how to create view for analyzing Java source code.

!!! info "Experimental"

    The source code frontend is experimental and should only be used for testing purposes. You should compile the code for analysis first and use the bytecode frontend instead.  

!!! example "Create a view to analyze Java source code"

    ~~~java
    AnalysisInputLocation inputLocation = 
            new JavaSourcePathAnalysisInputLocation("path2Source");
            
    JavaView view = new JavaView(inputLocation);
    ~~~

If you have a [Jimple](../jimple) file, you can create a view for analyzing jimple code directly. Following example shows how to create a view for analyzing jimple code.

!!! example "Create a view to analyze jimple code"

    ~~~java
    Path pathToJimple = Paths.get("path2Jimple");
    
    AnalysisInputLocation inputLocation = 
            new JimpleAnalysisInputLocation(pathToJimple);
    
    JimpleView view = new JimpleView(inputLocation);
    ~~~

<!---
3. Create a project to analyze Android APK. 

   TODO: add code
--->

By default, whenever a class is retrieved, it will be permanently stored in a cache.
If you do not want retrieved classes to be stored indefinetly, you can instead provide a different `CacheProvider` to the created view.
To for example use an `LRUCache` instead, which stores at most 50 classes, and always replaces the least recently used class by a newly retrieved one, use the following call:

```java
JavaView view = new JavaView(Collections.singletonList(inputLocation), new LRUCacheProvider(50));
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
    JavaClassType classType = 
            view.getIdentifierFactory().getClassType("example.HelloWorld");
    ```

Once we have a `ClassType` that identifies the `HelloWorld` class, we can use it to retrieve the corresponding `SootClass` object from the `view` as shown below:

!!! example "Retrieving a SootClass"

    ```java
    JavaSootClass sootClass = view.getClass(classType).get();
    ```

## Retrieving a Method
Like the classes, methods also have an identifier which we call `MethodSignature`. For instance, we can define the method signature for identifying the `main` method of the `HelloWorld` class as follows:

!!! example "Defining a MethodSignature"
    === "Pure"
        ```java
        MethodSignature methodSignature =
            view
                .getIdentifierFactory()
                .getMethodSignature(
                    "main", // method name
                    classType,
                    "void", // return type
                    Collections.singletonList("java.lang.String[]")); // args
        ```
    === "Parse from String"
        ```java
        MethodSignature methodSignature =
            view
                .getIdentifierFactory()
                .parseMethodSignature(
                    "<packageName.classType: void main(java.lang.String[])>");
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
    MethodSubSignature mss = methodSignature.getSubSignature()
    Optional<JavaSootMethod> opt = sootClass.getMethod(mss);
    
    if(opt.isPresent()){
      JavaSootMethod method = opt.get();
    }
    ```

## Retrieving the Control-Flow Graph of a Method

Each `SootMethod` contains a Control-Flow Graph (CFG) which is represented via the `StmtGraph`. This structure is usually used for program analysis. You can retrieve the CFG of a `SootMethod` as follows:

!!! example "Retrieving the CFG of a SootMethod"

    ```java
    sootMethod.getBody().getStmtGraph();
    ```



!!! info "Access a complete example of the code used above"

    Download [BasicSetup.java](https://github.com/secure-software-engineering/soot-reloaded/blob/develop/sootup.examples/src/test/java/sootup/examples/basicSetup/BasicSetup.java)
