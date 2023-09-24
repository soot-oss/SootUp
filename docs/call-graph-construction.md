# Call Graph Construction
A call graph shows the method calling relationship of a program. It is a directed graph, whose nodes represent different **methods**, and edges represent **caller -> callee** relationship.

SootUp contains several call graph construction algorithms. Below, we show how you can use each of these.

## Creating the Type Hierarchy
All the call graph construction algorithms require the view to access the type hierarchy for resolving method calls based of sub typing relationship.
Below, we show how to create a type hierarchy:

=== "SootUp"

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

    JavaView view = project.createView();
    ```
    
=== "Soot"

    ```java
    String targetTestClassName = target.exercise1.Hierarchy.class.getName();
    G.reset();
    String userdir = System.getProperty("user.dir");
    String sootCp = userdir + File.separator + "target" + File.separator + "test-classes"+ File.pathSeparator + "lib"+File.separator+"rt.jar";
    Options.v().set_whole_program(true);
    Options.v().set_soot_classpath(sootCp);
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().process_dir();
    Options.v().set_allow_phantom_refs(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().set_prepend_classpath(false);
    SootClass c = Scene.v().forceResolve(targetTestClassName, SootClass.BODIES);
    if (c != null)
	    c.setApplicationClass();
    Scene.v().loadNecessaryClasses();

    Hierarchy hierarchy = new Hierarchy();

    ```

## Defining an Entry Method
All the call graph construction algorithms require an entry method to start with. In java application, you usually define the main method. However, it is possible to define arbitrary entry methods depending on your needs. Below, we show how to define such an entry method:

=== "SootUp"

    ```java
    ClassType classTypeA = project.getIdentifierFactory().getClassType("A");

    MethodSignature entryMethodSignature =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                classTypeA,
                JavaIdentifierFactory.getInstance()
                    .getMethodSubSignature(
                        "calc", VoidType.getInstance(), Collections.singletonList(classTypeA)));    
    ```
    
=== "Soot"

    ```java
    SootMethod src = Scene.v().getSootClass(targetTestClassName).getMethodByName("doStuff");     
   
    ```

## Class Hierarchy Analysis
Class Hierarchy Analysis (CHA) algorithm is the most sound call graph construction algorithm available in SootUp. It soundly includes all implementers of an interface, when resolving a method call on an interface.
You can construct a call graph with CHA as follows:

=== "SootUp"

    ```java
    CallGraphAlgorithm cha = 
            new ClassHierarchyAnalysisAlgorithm(view);
    
    CallGraph cg = 
            cha.initialize(Collections.singletonList(entryMethodSignature));

    System.out.println(cg);
    ```
    
=== "Soot"

    ```java
    CHATransformer.v().transform();
    SootMethod src = Scene.v().getSootClass(targetTestClassName).getMethodByName("doStuff");
    CallGraph cg = Scene.v().getCallGraph();
    Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
    while (targets.hasNext()) {
	    SootMethod tgt = (SootMethod)targets.next();
	    System.out.println(src + " may call " + tgt);
	    }
    ```

## Rapid Type Analysis
Rapid Type Analysis (RTA) algorithm constructs a rather precise version of the call graph that the CHA constructs. It refines CHA by considering only the instantiated implementers of an interface, when resolving a method call on an interface.
You can construct a call graph with RTA as follows:

=== "SootUp"

    ```java
    CallGraphAlgorithm rta = 
            new RapidTypeAnalysisAlgorithm(view);
    
    CallGraph cg = 
            rta.initialize(Collections.singletonList(entryMethodSignature));

    System.out.println(cg);
    ```
    
=== "Soot"

    ```java
    Transform sparkConfig = new Transform("cg.spark", null);
    PhaseOptions.v().setPhaseOption(sparkConfig, "enabled:true");
    PhaseOptions.v().setPhaseOption(sparkConfig, "rta:true");
    PhaseOptions.v().setPhaseOption(sparkConfig, "on-fly-cg:false");
    Map phaseOptions = PhaseOptions.v().getPhaseOptions(sparkConfig);
    SparkTransformer.v().transform(sparkConfig.getPhaseName(), phaseOptions);
    SootMethod src = Scene.v().getSootClass(targetTestClassName).getMethodByName("doStuff");
    CallGraph cg = Scene.v().getCallGraph();
    Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
    while (targets.hasNext()) {
	    SootMethod tgt = (SootMethod)targets.next();
        System.out.println(src + " may call " + tgt);
    }  
    ```

## Variable Type Analysis
Variable Type Analysis (VTA) algorithm further refines the call graph that the RTA constructs. It refines RTA by considering only the assigned instantiations of the implementers of an interface, when resolving a method call on an interface.
When considering assignments, we usually need to consider **pointer** (points-to) relationship.

!!! info

    VTA algorithm was implemented using the [Spark](https://plg.uwaterloo.ca/~olhotak/pubs/thesis-olhotak-msc.pdf) pointer analysis framework.
    A reimplementation of Spark in SootUp is currently under development.

Spark requires an initial call graph to begin with. You can use one of the call graphs that we have constructed above. You can construct a call graph with VTA as follows:

=== "SootUp"

    ```java
    Spark spark = new Spark.Builder(view, callGraph).vta(true).build();
    spark.analyze();
    CallGraph vtaCAllGraph = spark.getCallGraph();
    ```
    
=== "Soot"

    ```java
    Transform sparkConfig = new Transform("cg.spark", null);
    PhaseOptions.v().setPhaseOption(sparkConfig, "enabled:true");
    PhaseOptions.v().setPhaseOption(sparkConfig, "vta:true");
    PhaseOptions.v().setPhaseOption(sparkConfig, "on-fly-cg:false");
    Map phaseOptions = PhaseOptions.v().getPhaseOptions(sparkConfig);
    SparkTransformer.v().transform(sparkConfig.getPhaseName(), phaseOptions);
    SootMethod src = Scene.v().getSootClass(targetTestClassName).getMethodByName("doStuff");
    CallGraph cg = Scene.v().getCallGraph();
    Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
    while (targets.hasNext()) {
	    SootMethod tgt = (SootMethod)targets.next();
        System.out.println(src + " may call " + tgt);
    }    
    ```

