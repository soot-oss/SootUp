# General Usage of SootUp
This page walks you through the core data structures, as well as shows how to get started with SootUp.

## The core datastructures
Before you get started with the SootUp library, it helps to learn about the following core data structures:

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

!!! example "Create a project to analyze jimple code"

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
    Optional<JavaSootMethod> opt = sootClass.getMethod(methodSignature.getSubSignature());
    
    if(opt.isPresent()){
      JavaSootMethod method = opt.get();
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

    JavaView view = new JavaView(inputLocation);

    JavaClassType classType = 
            view.getIdentifierFactory().getClassType("HelloWorld");

    MethodSignature methodSignature =
        view
            .getIdentifierFactory()
            .getMethodSignature(
                "main", classType, "void",
                Collections.singletonList("java.lang.String[]"));

    JavaSootClass sootClass = view.getClass(classType).get();

    JavaSootMethod sootMethod =  sootClass.getMethod(methodSignature.getSubSignature()).get();
    
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

# SootUp-Examples
Example code to help getting start with SootUp


1) Here we will provide some examples that uses SootUp to provide insights about a Java program. The repository that contains the examples can be found in [https://github.com/soot-oss/SootUp-Examples.git].
2) There are mainly 5 projects to be considered under SootUp.
   a) *BasicSetupExample*
   b) *BodyInterceptorExample*
   c) *CallGraphExample*
   d) *ClassHierarchyExample*
   e) *MutatingSootClassExample* <br>

3) We have included all the five projects in 5 different branches under SootUp-Examples with detailed explanation about the project.

     a) BasicSetupExample 
           1) package sootup.examples; - defines the package name for the Java class.
           2) import statement - defines various classes and interfaces from different packages that the program uses.
           3) public class BasicSetup - declares a public class named 'BasicSetup' which is the main class for this program.
           4) Then we have created a main method which is the entry point of the progrram
           5) Path pathToBinary object pointing to a directory that contains the binary files ie class files to be analyzed and Paths.get is a static method that converts string path to a 'Path' object.
           6) AnalysisInputLocation object specifying where SootUp should look for classes to analyze.
           7) View object is created for the project allowing the retrieal of classes from the specified input location. JavaView is specific implementation of View tailed for Java projects.
           8) The ClassType object is created for the class name 'HelloWorld'. This object represents the type of class to be analyzed.
           9) A MethodSignature object is created for the main method of the HelloWorld class. This signature specifies the method's return type (void) and its parameter types (a single parameter of type String[]).
           10) The if statment checks for the presences of the class 'HelloWorld'. If not it prints "Class not ffound!" and exits the program.
           11) Then it retrieves the SootClass object representing the HelloWorld class, assuming it is present.
           12) view.getMethod(methodSignature); - Attempts to retrieve the specified method from the project.
           13) The if statment after this, checks if the main method is present in the HelloWorld class. If not, it prints "Method not found!" and exits.
           14) Then the next statment retrieves the SootMethod object for the main method and prints its body, which is in Jimple, a simplified version of Java bytecode used by Soot for analysis and transformation.
           15) Then the next if condition checks if the method containts a specific statement called 'Hello World!'.

   b) BodyInterceptor 
           1) package sootup.examples; - defines the package name for the Java class.
           2) import statement - defines various classes and interfaces from different packages that the program uses.
           3) public class BodyInterceptor - declares a public class named "BodyInterceptor".
           4) Then we have created a main method in which is the entry point for the code.
           5) Then we have created an AnalysisInputLocation pointing to a directory with class files to be loaded. It specifies that the DeadAssignmentEliminator interceptor should be applied to these classes.
           6) Then created a View that initializes a JavaView with the specified inputLocation, allowing interaction with the classes for analysis.
           7) Then have created a ClassType and MethodSignature which is used for analysis. The signature contains method name, return type and parameters.
           8)  Then we check for the existence of the class and method in the given view.
           9) If they exist, a SootClass and SootMethod objects are used to retrieve the same.
           10) Then prints the body of the SootMethod object.
           11) Then we check if the interceptor worked.  ie here we check if the DeadAssignmentEliminator interceptor has successfully removed a specific assignment (l1 = 3) from the method's body. It does this by looking through all statements (JAssignStmt) in the method body and checking if the assignment is not                     present.
           12) Then it prints the result of the interceptor check.

   
   c) CallGraphExample 
           1) package sootup.examples; - defines the package name for the Java class.
           2) import statement - defines various classes and interfaces from different packages that the program uses.
           3) public class CallgraphExample  - declares a public class named "CallGraphExample".
           4) Then we have created a main method in which is the entry point for the code.
           5) List<AnalysisInputLocation> inputLocations creates a list of AnalysisInputLocation objects. These specify where Soot should look for Java class files for analysis.
           6) Then we have provided towo inputLocations.add() - one for the project's class file directory and another for Java's runtime library (rt.jar).
           7) Then we have created a JavaView which is used for analysing the Java program.
           8) Then we have created two ClassType for two classes ie 'A' and 'B'. They are used to create a MethodSignature for a method that will be analysed.
           9) ViewTypeHierarchy  - then we have set up a type hierarchy from the provided view and prints the subclasses of class 'A'.
           10) Initializes a CallGraphAlgorithm using the ClassHierarchyAnalysisAlgorithm, which is a method for constructing call graphs.
           11) Then we creates a call graph by initialising the Class Hierarchy Analysis (cha) with the entry method signature.
           12) Prints information about calls from the entry method in the call graph.

   d) ClassHierarchyExample
           1) package sootup.examples; - defines the package name for the Java class.
           2) import statement - defines various classes and interfaces from different packages that the program uses.
           3) public class ClassHierarchy - declares a public class named "ClassHierarchy".
           4) Then we have created a main method in which is the entry point for the code.
           5) Then creates a list of AnalysisInputLocation objects. These specify where Soot should look for Java class files for analysis. Two locations are added: one for the project's binary directory and another for the default Java runtime library (rt.jar).
           6) Initializes a JavaView object with the previously created input locations. 
           7) Initializes a ViewTypeHierarchy object using the view. This object will be used to analyze the class hierarchy.
           8) Then we have created two ClassTypes. These lines get JavaClassType objects for classes "A" and "C". These types are used for further hierarchy analysis.
           9) Checks the direct subclasses of class "C". It verifies if all direct subclasses are "D" using two different methods: comparing class names and fully qualified names.
           10) Then prints a message based on whether all direct subtypes of "C" are correctly identified as "D".
           11) Retrieves and checks the superclasses of class "C". It then verifies if these superclasses include class "A" and java.lang.Object, printing a message based on the result.

    e) MutatingSootClassExample
           
           1) package sootup.examples; - defines the package name for the Java class.
           2) import statement - defines various classes and interfaces from different packages that the program uses.
           3) public class MutatingSootClass - declares a public class named "MutatingSootClass".
           4) Then we have created a main method in which is the entry point for the code.
           5) First we have created an 'AnalysisInputLocation' which points to a directory which contains the class files to be analysed.
           6) Then we have created a JavaView which allos us to retrievet the classes.
           7) And also created a ClassType to get the class 'HelloWorld' and a method within that class ie main for analysis using MethodSignature.
           8) THen we are checking and retrieving the class and method.
           9) Then we retrives the existing body of the method and prints it. Then we create a new local variable to add it copy to the method body.
           10) Then we are overriding the method body and class. ie this lines creates new sources that overrides teh original method body and class. It replaces the old method in the class with the new method having the modified body.
           11) Prints the modified method body and checks if the new local variable (newLocal) exists in the modified method. Depending on the result, it prints a corresponding message.
