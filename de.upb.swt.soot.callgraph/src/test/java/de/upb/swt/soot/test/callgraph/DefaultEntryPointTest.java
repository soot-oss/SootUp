package de.upb.swt.soot.test.callgraph;

import static junit.framework.TestCase.*;

import de.upb.swt.soot.callgraph.CallGraph;
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

  @Test
  public void CHADefaultEntryPoint() {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }
    String classPath = "src/test/resources/callgraph/DefaultEntryPoint";
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar", SourceType.Library))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();
    JavaView view = javaProject.createOnDemandView();

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    JavaClassType mainClassSignature = identifierFactory.getClassType("example1.Example");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    ClassHierarchyAnalysisAlgorithm algorithm =
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

  @Test
  public void CHAMultipleMainMethod() {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }
    String classPath = "src/test/resources/callgraph/Misc";
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar", SourceType.Library))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();
    JavaView view = javaProject.createOnDemandView();

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

  @Test
  public void CHANoMainMethod() {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }
    String classPath = "src/test/resources/callgraph/NoMainMethod";
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar", SourceType.Library))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();
    JavaView view = javaProject.createOnDemandView();

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

  @Test
  public void RTADefaultEntryPoint() {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }
    String classPath = "src/test/resources/callgraph/DefaultEntryPoint";
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar", SourceType.Library))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();
    JavaView view = javaProject.createOnDemandView();

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    JavaClassType mainClassSignature = identifierFactory.getClassType("example1.Example");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    RapidTypeAnalysisAlgorithm algorithm = new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
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
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }
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
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

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

  @Test
  public void specifyBuiltInInputJimplePath() {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }
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
