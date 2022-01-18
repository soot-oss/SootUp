# Call Graph Construction
A call graph shows the method calling relationship of a program. It is a directed graph, whose nodes represent different **methods**, and edges represent **caller -> callee** relationship.

FutureSoot contains several call graph construction algorithms. Below, we show how you can use each of these.

## Creating the Type Hierarchy
All the call graph construction algorithms require a type hierarchy for resolving method calls based of sub typing relationship.
Below, we show how to create a type hierarchy:

=== "FutureSoot"

    ```java
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JavaClassPathAnalysisInputLocation(
                "src/test/resources/Callgraph/binary");

    JavaLanguage language = new JavaLanguage(8);

    JavaProject project =
        JavaProject.builder(language)
            .addInputLocation(inputLocation)
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .build();

    JavaView view = project.createFullView();
    
    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);    
    ```
    
=== "Soot"

    ```java
    // TODO: add old Soot equivalent
    
    G.reset();
        String userdir = System.getProperty("user.dir");
        String sootCp = 
                userdir 
                + File.separator 
                + "target" 
                + File.separator 
                + "test-classes"
                + File.pathSeparator + "lib"+File.separator+"rt.jar";
    ```

## Defining an Entry Method
All the call graph construction algorithm require an entry method to start with. In java application, you usually define the main method. However, it is possible to define arbitrary entry methods depending on your needs. Below, we show how to define such an entry method:

=== "FutureSoot"

    ```java
    ClassType classTypeA = project.getIdentifierFactory().getClassType("A");
    
    MethodSignature entryMethodSignature =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                classTypeA,
                JavaIdentifierFactory.getInstance()
                    .getMethodSubSignature(
                        "calc",
                        VoidType.getInstance(),
                        Collections.singletonList(classTypeA)));    
    ```
    
=== "Soot"

    ```java
    // TODO: add old Soot equivalent
    
    G.reset();
    String userdir = System.getProperty("user.dir");
    String sootCp = 
            userdir 
            + File.separator 
            + "target" 
            + File.separator 
            + "test-classes"
            + File.pathSeparator + "lib"+File.separator+"rt.jar";
            
    ```

## Class Hierarchy Analysis
Class Hierarchy Analysis (CHA) algorithm is the most sound call graph construction algorithm available in FutureSoot. It soundly includes all implementers of an interface, when resolving a method call on an interface.
You can construct a call graph with CHA as follows:

=== "FutureSoot"

    ```java
    CallGraphAlgorithm cha = 
            new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    
    CallGraph cg = 
            cha.initialize(Collections.singletonList(entryMethodSignature));

    cg.callsFrom(entryMethodSignature).forEach(System.out::println);
    ```
    
=== "Soot"

    ```java
    // TODO: add old Soot equivalent
        
        G.reset();
            String userdir = System.getProperty("user.dir");
            String sootCp = 
                    userdir 
                    + File.separator 
                    + "target" 
                    + File.separator 
                    + "test-classes"
                    + File.pathSeparator + "lib"+File.separator+"rt.jar";
    ```

## Rapid Type Analysis
Rapid Type Analysis (RTA) algorithm constructs a rather precise version of the call graph that the CHA constructs. It refines CHA by considering only the instantiated implementers of an interface, when resolving a method call on an interface.
You can construct a call graph with RTA as follows:

=== "FutureSoot"

    ```java
    CallGraphAlgorithm cha = 
            new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
    
    CallGraph cg = 
            cha.initialize(Collections.singletonList(entryMethodSignature));

    cg.callsFrom(entryMethodSignature).forEach(System.out::println);
    ```
    
=== "Soot"

    ```java
    // TODO: add old Soot equivalent
        
        G.reset();
            String userdir = System.getProperty("user.dir");
            String sootCp = 
                    userdir 
                    + File.separator 
                    + "target" 
                    + File.separator 
                    + "test-classes"
                    + File.pathSeparator + "lib"+File.separator+"rt.jar";   
    ```

## Variable Type Analysis
Variable Type Analysis (VTA) algorithm further refines the call graph that the RTA constructs. It refines RTA by considering only the assigned instantiations of the implementers of an interface, when resolving a method call on an interface.
When considering assignments, we usually need to consider **pointer** (points-to) relationship.

!!! info

    VTA algorithm was implemented using the [Spark](https://plg.uwaterloo.ca/~olhotak/pubs/thesis-olhotak-msc.pdf) pointer analysis framework.

Spark requires an initial call graph to begin with. You can use one of the call graphs that we have constructed above. You can construct a call graph with VTA as follows:

=== "FutureSoot"

    ```java
    Spark spark = new Spark.Builder(view, callGraph).vta(true).build();
    spark.analyze();
    CallGraph vtaCAllGraph = spark.getCallGraph();
    ```
    
=== "Soot"

    ```java
    // TODO: add old Soot equivalent
        
        G.reset();
            String userdir = System.getProperty("user.dir");
            String sootCp = 
                    userdir 
                    + File.separator 
                    + "target" 
                    + File.separator 
                    + "test-classes"
                    + File.pathSeparator + "lib"+File.separator+"rt.jar";    
    ```