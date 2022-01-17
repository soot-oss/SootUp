# Getting Started

This page walks you through the core data structures, as well as shows how to get started with FutureSoot.

## Core Data Structures
Before you get started with the FutureSoot library, it helps to learn about the following core data structures: 

- `Project`: defines the outlines of an analysis. Soot users should first create a `Project` instance. It is the starting point for all operations. 
  You can define multiple instances of `Project` at the same time and there are no information shared between them. All caches are always at the project level.

- `Language`: represents the programming language of the analyzed code. 

- `AnalysisInputLocation`: points to the target code to be analyzed.

!!! info "Soot Equivalent"

    It corresponds to the `cp` option, which specifies the classpath for Soot to find classes to be analyzed.

- `View`: presents the code/classes under analysis.

!!! info "Soot Equivalent"

    It corresponds to the `Scene` class, but it is not a singletion.

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
    Path pathToBinary = Paths.get("src/test/resources/BasicSetup/binary");
    
    AnalysisInputLocation<JavaSootClass> inputLocation = 
            PathBasedAnalysisInputLocation.createForClassContainer(pathToBinary);
            
    JavaLanguage language = new JavaLanguage(8);
    
    Project project = 
            JavaProject.builder(language).addInputLocation(inputLocation).build();
    ~~~

If you have access to the source code, it is also possible to create a project for analyzing source code. Following example shows how to create project for analyzing Java source code.

!!! example "Create a project to analyze Java source code"

    ~~~java
    Path pathToSource = Paths.get("src/test/resources/BasicSetup/source");
    
    AnalysisInputLocation<JavaSootClass> inputLocation = 
            new JavaSourcePathAnalysisInputLocation(pathToSource.toString());
            
    JavaLanguage language = new JavaLanguage(8);
    
    Project project = 
            JavaProject.builder(language).addInputLocation(inputLocation).build();
    ~~~

If you have a [Jimple](../jimple) file, you can create a project for analyzing jimple code directly. Following example shows how to create project for analyzing jimple code.

!!! example "Create a project to analyze jimple code"

    ~~~java
    Path pathToJimple = Paths.get("src/test/resources/BasicSetup/jimple");
    
    AnalysisInputLocation<JavaSootClass> inputLocation = new JimpleAnalysisInputLocation(pathToJimple);
    
    Project project = new JimpleProject(inputLocation);
    ~~~

<!---
3. Create a project to analyze Android APK. 

   TODO: add code
--->

## Creating a View

It is possible to create different views based on your needs. You can prefer creating a full view, when you are interested in the whole program including all the application code, and the library code; or you can create an on-demand view, when you want to limit your analysis' scope.
You can call different view creation methods on the `project` object.

```java
project.createFullView();
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
    
      public static void main(String[] var0) {
      
        System.out.println("Hello World!");
        
      }
      
    }
    ```

Then, we could define the `ClassType` of the `HelloWorld` class as follows:

!!! example "Defining a ClassType"

    ```java
    ClassType classType = 
            project.getIdentifierFactory().getClassType("example.HelloWorld");
    ```

Once we have a `ClassType` which identifies the `HelloWorld` class, we can use it to retrieve the corresponding `SootClass` object from the `view` as shown below.

!!! example "Retrieving a SootClass"

    ```java
    SootClass<JavaSootClassSource> sootClass =
            (SootClass<JavaSootClassSource>) view.getClass(classType).get();
    ```


<!---
## Creating Different Views
1. Create a full view of all classes found in given analysis input location. 
~~~java
  project.createFullView();
~~~  
2. Create a on-demand view. An on-demand view does not load all classes into the view, but only classes that are specified and their transitive closure. 

   TODO: add code 

3. Create a view based on a defined scope. 
   TODO: add code
   
--->

## Perform an Intra-procedural Analysis

## Construct Call Graph

## Perform an Inter-procedural Analysis


## All Code Used Aboves
```java
{{ include('basicSetup/BasicSetup.java') }}
```
