# Migration Help

### Version 1.3.0
- The Typehierarchy API is now returning `Stream<ClassType>` instead of `Collection<ClassType>`. The simplest fix to have the same behaviour as before would be to collect the Stream on your own ( e.g. via `.collect(Collectors.toList())` ).
- Default BytecodeBodyinterceptors are enabled to improve Jimple. To mitigate that adapt the List of BodyInterceptors to your needs.

### Version 1.2.0
- The (Java)Project structure was removed. You can configure the (Java)View directly.
- Bodyinterceptors are now passed as arguments into AnalysisInputLocations.

### From [Soot](https://github.com/soot-oss/Soot)
- The Scene singleton is dead. Long live the Scene. We have a central View object(!) now.
- Library first! No command line tool as primary goal. So you can configure parts of SootUp (near) where it is actually used. 
- t.b.c.

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

    MethodSubSignature mss = methodSignature.getSubSignature();
    JavaSootMethod sootMethod =  sootClass.getMethod(mss).get();
    
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
