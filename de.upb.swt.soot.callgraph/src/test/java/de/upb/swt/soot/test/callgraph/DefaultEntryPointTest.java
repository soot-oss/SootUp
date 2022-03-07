package de.upb.swt.soot.test.callgraph;

import static junit.framework.TestCase.*;

import de.upb.swt.soot.callgraph.CallGraph;
import de.upb.swt.soot.callgraph.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.RapidTypeAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.*;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import de.upb.swt.soot.jimple.parser.JimpleAnalysisInputLocation;
import de.upb.swt.soot.jimple.parser.JimpleProject;
import de.upb.swt.soot.jimple.parser.JimpleView;
import java.nio.file.Paths;
import java.util.*;
import org.junit.Test;

public class DefaultEntryPointTest {

  /**
   * Method to check java version.
   */
  private void checkVersion(){
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }
  }

  /**
   * The method returns the view for input java source path and rt.jar file.
   * @param classPath - The location of java source files.
   * @return - Java view
   */
  private JavaView getView(String classPath){
    JavaProject javaProject =
            JavaProject.builder(new JavaLanguage(8))
                    .addInputLocation(
                            new JavaClassPathAnalysisInputLocation(
                                    System.getProperty("java.home") + "/lib/rt.jar", SourceType.Library))
                    .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
                    .build();
    return javaProject.createOnDemandView();
  }

  /**
   * Test to create call graph for CHA without specifying entry point. It uses main method present in input java files as entry point.
   */
  @Test
  public void CHADefaultEntryPoint() {
    checkVersion();

    JavaView view = getView("src/test/resources/callgraph/DefaultEntryPoint");

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    JavaClassType mainClassSignature = identifierFactory.getClassType("example1.Example");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm algorithm =
        new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    CallGraph cg = algorithm.initialize();
    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    assertNotNull(cg);

    MethodSignature constructorB =
        identifierFactory.getMethodSignature(
            "<init>",
            identifierFactory.getClassType("example1.B"),
            "void",
            Collections.emptyList());

    MethodSignature constructorC =
        identifierFactory.getMethodSignature(
            "<init>",
            identifierFactory.getClassType("example1.C"),
            "void",
            Collections.emptyList());

    MethodSignature methodA =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.A"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodB =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.B"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodC =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.C"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodD =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.D"),
            "void",
            Collections.singletonList("java.lang.Object"));

    assertTrue(cg.containsCall(mainMethodSignature, constructorB));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC));

    assertTrue(cg.containsCall(mainMethodSignature, methodA));
    assertTrue(cg.containsCall(mainMethodSignature, methodB));
    assertTrue(cg.containsCall(mainMethodSignature, methodC));
    assertTrue(cg.containsCall(mainMethodSignature, methodD));

    assertEquals(6, cg.callsFrom(mainMethodSignature).size());

    assertEquals(2, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(methodA).size());
    assertEquals(1, cg.callsTo(methodB).size());
    assertEquals(1, cg.callsTo(methodC).size());
    assertEquals(1, cg.callsTo(methodD).size());

    assertEquals(0, cg.callsFrom(methodA).size());
    assertEquals(0, cg.callsFrom(methodB).size());
    assertEquals(0, cg.callsFrom(methodC).size());
    assertEquals(0, cg.callsFrom(methodD).size());
  }

  /**
   * Test uses initialize() method to create call graph, but multiple main methods are present in input java source files.
   * Expected result is RuntimeException.
   */
  @Test
  public void CHAMultipleMainMethod() {
    checkVersion();

    JavaView view = getView("src/test/resources/callgraph/Misc");

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    ClassHierarchyAnalysisAlgorithm algorithm =
        new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    try {
      algorithm.initialize();
      fail("Runtime Exception not thrown, when multiple main methods are defined.");
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
      assertTrue(e.getMessage().startsWith("There are more than 1 main method present"));
    }
  }

  /**
   * Test uses initialize() method to create call graph, but no main method is present in input java source files.
   * Expected result is RuntimeException.
   */
  @Test
  public void CHANoMainMethod() {
    checkVersion();

    JavaView view = getView("src/test/resources/callgraph/NoMainMethod");

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    ClassHierarchyAnalysisAlgorithm algorithm =
        new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    try {
      algorithm.initialize();
      fail("Runtime Exception not thrown, when no main methods are defined.");
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
      assertEquals(
          e.getMessage(),
          "No main method is present in the input programs. initialize() method can be used if only one main method exists in the input program and that should be used as entry point for call graph. \n Please specify entry point as a parameter to initialize method.");
    }
  }

  /**
   * Test to create call graph for RTA without specifying entry point. It uses main method present in input java files as entry point.
   */
  @Test
  public void RTADefaultEntryPoint() {
    checkVersion();

    JavaView view = getView("src/test/resources/callgraph/DefaultEntryPoint");

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    JavaClassType mainClassSignature = identifierFactory.getClassType("example1.Example");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm algorithm = new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
    CallGraph cg = algorithm.initialize();
    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    assertNotNull(cg);

    MethodSignature constructorB =
        identifierFactory.getMethodSignature(
            "<init>",
            identifierFactory.getClassType("example1.B"),
            "void",
            Collections.emptyList());

    MethodSignature constructorC =
        identifierFactory.getMethodSignature(
            "<init>",
            identifierFactory.getClassType("example1.C"),
            "void",
            Collections.emptyList());

    MethodSignature methodA =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.A"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodB =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.B"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodC =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.C"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodD =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.D"),
            "void",
            Collections.singletonList("java.lang.Object"));

    assertTrue(cg.containsCall(mainMethodSignature, constructorB));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC));

    assertTrue(cg.containsCall(mainMethodSignature, methodA));
    assertTrue(cg.containsCall(mainMethodSignature, methodB));
    assertTrue(cg.containsCall(mainMethodSignature, methodC));
    assertFalse(cg.containsMethod(methodD));

    assertEquals(5, cg.callsFrom(mainMethodSignature).size());

    assertEquals(2, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(methodA).size());
    assertEquals(1, cg.callsTo(methodB).size());
    assertEquals(1, cg.callsTo(methodC).size());

    assertEquals(0, cg.callsFrom(methodA).size());
    assertEquals(0, cg.callsFrom(methodB).size());
    assertEquals(0, cg.callsFrom(methodC).size());
  }

  /**
   * Test for JavaSourcePathAnalysisInputLocation. Specifying all input source files with source
   * type as Library. Expected - All input classes are of source type Library.
   */
  @Test
  public void specifyBuiltInInputSourcePath() {
    checkVersion();

    String classPath = "src/test/resources/callgraph/DefaultEntryPoint";
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaSourcePathAnalysisInputLocation(SourceType.Library, classPath))
            .build();
    JavaView view = javaProject.createOnDemandView();

    Set<SootClass<JavaSootClassSource>> classes =
        new HashSet<>(); // Set to track the classes to check
    for (SootClass<JavaSootClassSource> aClass : view.getClasses()) {
      if (!aClass.isLibraryClass()) {
        System.out.println("Found user defined class " + aClass);
        classes.add(aClass);
      }
    }

    assertEquals("User Defined class found, expected none", 0, classes.size());
  }

  /**
   * Test for JavaClassPathAnalysisInputLocation. Specifying jar file with source type as Library.
   * Expected - All input classes are of source type Library.
   */
  @Test
  public void specifyBuiltInInputClassPath() {
    checkVersion();

    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar", SourceType.Library))
            .build();
    JavaView view = javaProject.createOnDemandView();

    Collection<SootClass<JavaSootClassSource>> classes =
        new HashSet<>(); // Set to track the classes to check

    for (SootClass<JavaSootClassSource> aClass : view.getClasses()) {
      // System.out.println(aClass.getClassSource().getClassType().isBuiltInClass());
      if (!aClass.isLibraryClass()) {
        System.out.println("Found user defined class " + aClass);
        classes.add(aClass);
      }
    }

    assertEquals("User Defined class found, expected none", 0, classes.size());
  }

  /**
   * Test for JimpleAnalysisInputLocation. Specifying jimple file with source type as Library.
   * Expected - All input classes are of source type Library.
   */
  @Test
  public void specifyBuiltInInputJimplePath() {
    checkVersion();

    String classPath = "src/test/resources/callgraph/jimple";
    AnalysisInputLocation<JavaSootClass> jimpleInputLocation =
        new JimpleAnalysisInputLocation<>(Paths.get(classPath), SourceType.Library);
    JimpleView view = new JimpleProject(jimpleInputLocation).createOnDemandView();

    Collection<SootClass<?>> classes = new HashSet<>(); // Set to track the classes to check

    for (SootClass<?> aClass : view.getClasses()) {
      // System.out.println(aClass.getClassSource().getClassType().isBuiltInClass());
      if (!aClass.isLibraryClass()) {
        System.out.println("Found user defined class " + aClass);
        classes.add(aClass);
      }
    }

    assertEquals("User Defined class found, expected none", 0, classes.size());
  }
}
