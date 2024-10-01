# TypeHierarchy
The TypeHierarchy models the relationship of Classes or Interfaces of a OOP program.

## Creating TypeHierarchy

=== "SootUp"

    ```java
    String cpString = "src/test/resources/Callgraph/binary";
    List<AnalysisInputLocation> inputLocations = new ArrayList();
    inputLocations.add(new JavaClassPathAnalysisInputLocation(cpStr));
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());

    JavaView view = new JavaView(inputLocations);
    TypeHierarchy typehierarchy = view.getTypeHierarchy();
    ```

=== "Soot"

    ```java
    String userdir = System.getProperty("user.dir");
    String sootCp = userdir + File.separator + "target" + File.separator + "test-classes"+ File.pathSeparator + "lib"+File.separator+"rt.jar";

    String targetTestClassName = target.exercise1.Hierarchy.class.getName();
    G.reset();
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

## Create a JavaClassType

=== "SootUp"

    ```java
    JavaClassType classTypeA = view.getIdentifierFactory().getClassType("packageName.A");
    JavaClassType classTypeB = view.getIdentifierFactory().getClassType("packageName.B");
    JavaClassType classTypeC = view.getIdentifierFactory().getClassType("packageName.C");

    ```
=== "Soot"

    ```java
    String targetTestClassName = "packageNameA.A";
    SootClass methodA = Scene.v().getSootClass(targetTestClassName);

    ```

## Query the TypeHierarchy
### Classes
```java
    // if the assertion fails, the following methods will throw an Exception (you don't have to call it - it's just to illustrate the assumption)
    assert typehierarchy.contains(classTypeA);

    typehierarchy.superclassOf(classTypeA);

    typehierarchy.subclassesOf(classTypeA);
    
    typehierarchy.isSubtypeOf(classTypeA, classTypeB);
    
```

### Interfaces

```java
    JavaClassType iterableInterface = view.getIdentifierFactory().getClassType("java.lang.Iterable");

    // if any of the assertions fail, the following methods will throw an Exception (you don't have to call these - it's just to illustrate the assumptions)
    assert typehierarchy.contains(iterableInterface);
    assert typehierarchy.isInterface(iterableInterface);
    
    // transitive relations as well
    typehierarchy.implementedInterfacesOf(iterableInterface);
    typehierarchy.implementersOf(iterableInterface);

    // only the direct related relations
    typehierarchy.directlyImplementedInterfacesOf(iterableInterface);
    typehierarchy.directlyExtendedInterfacesOf(iterableInterface);

```