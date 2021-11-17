# Getting Started
This new version enables Soot users to use Soot in a much more flexible manner. Particularly, we introduce the following major, breaking changes:

- Library by default. This version of Soot assumes that it’s embedded in a client application that owns the thread of control. This analysis client can use off Soot whatever is needed. We also plan to implement a command-line client which implements a default control thread to load into Soot certain code projects, have them analyzed/transformed, similarly to how old Soot was used.

- No singleton such the `Scene` class in the old Soot any more. A much more decoupled, modular design allows users to parallelize analysis of multiple projects.

- We use WALA's source code front ends to support more languages. This will not only allow Soot to analyze Java source code, but also JavaScript and Python.

- Jimple is the only intermediate representation (IR) in this new version and it undergos slight changes to accommodate different programming languages. 

- Immutable by design. This allows users to have multiple views on the code. Jimple-modification is still possible.

## Core Data Structures
Before you use Soot as a library, you need understand the following data structures at first: 

- `Project`: defines the outlines of an analysis. Soot users should first create a `Project` instance. It is the starting point for all operations. 
  You can define multiple instances of `Project` at the same time and there are no information shared between them. All caches are always at the project level.

- `Language`: represents the programming language of the analyzed code. 

- `AnalysisInputLocation`: defines where to find the analyzed code. It is like the `cp` option in the old Soot, which specifies the classpath for Soot to find classes to be analyzed.

- `View`: presents the code/classes under analysis. It can been seen as a replacement of the `Scene` class, but it is not a singletion class.

- `Scope`: defines the scope of the `View`. By default, the `View` is created with all code found on the `AnalysisInputLocation` specified for the `Project` instance.

- `SootClass`: represents a class loaded into the `View`.

- `SootMethod`: represents a method of a class.

- `SootField`: represents a field of a class.

- `Body`: represents a method body in Jimpe.

- `StmtGraph`: represents the control flow graph of a method body in Jimple statements.

## Add Soot as Library Dependency to Your Project
1. Maven

TODO

2. Gradle

TODO

3. Eclipse

TODO

## Basic Project Setup
1. Create a project to analyze Java bytecode. 
~~~
Path pathToBinary = Paths.get("src/test/resources/BasicSetup/binary");
AnalysisInputLocation<JavaSootClass> inputLocation = PathBasedAnalysisInputLocation.createForClassContainer(pathToBinary;
Language language = new JavaLanguage(8);
Project project = JavaProject.builder((JavaLanguage) language).addInputLocation(inputLocation).build();
~~~

2. Create a project to analyze Java source code.
~~~
Path pathToSource = Paths.get("src/test/resources/BasicSetup/source");
AnalysisInputLocation<JavaSootClass> inputLocation = new JavaSourcePathAnalysisInputLocation(pathToSource.toString());
Language language = new JavaLanguage(8);
Project project = JavaProject.builder((JavaLanguage) language).addInputLocation(inputLocation).build();
~~~

3. Create a project to analyze Android APK. 

   TODO

4. Create a project to analyze Jimple code.
~~~
Path pathToJimple = Paths.get("src/test/resources/BasicSetup/jimple");
AnalysisInputLocation<JavaSootClass> inputLocation = new JimpleAnalysisInputLocation(pathToJimple);
Project project = new JimpleProject(inputLocation);
~~~

## Create Different Views
1. Create a full view of all classes found in given analysis input location. 
~~~
  project.createFullView();
~~~  
2. Create a on-demand view. An on-demand view does not load all classes into the view, but only classes that are specified and their transitive closure. 

   TODO

3. Create a view based on a defined scope. 
   TODO

## Perform an Intra-procedural Analysis

## Construct Call Graph

## Perform an Inter-procedural Analysis

```
{{ include('basicSetup/BasicSetup.java') }}
```
