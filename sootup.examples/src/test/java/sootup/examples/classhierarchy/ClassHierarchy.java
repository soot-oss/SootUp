package sootup.examples.classhierarchy;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * The test files for this example consist of a class hierarchy as follows:
 *
 * <pre>
 *         |-- B
 *    A <--|
 *         |-- C <-- D
 *  </pre>
 *
 * This code example will show you how to build and examine a class hierarchy using sootup.
 */
public class ClassHierarchy {

  @Test
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JavaClassPathAnalysisInputLocation("src/test/resources/ClassHierarchy/binary");

    // Specify the language of the JavaProject. This is especially relevant for Multi-release jars,
    // where classes are loaded depending on the language level of the analysis
    JavaLanguage language = new JavaLanguage(8);

    // Create a new JavaProject and view based on the input location
    JavaProject project =
        JavaProject.builder(language)
            .addInputLocation(inputLocation)
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar")) // add rt.jar
            .build();

    JavaView view = project.createView();

    // Create type hierarchy
    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);

    // Specify class types we want to receive information about
    JavaClassType clazzTypeA = JavaIdentifierFactory.getInstance().getClassType("ClassHierarchy.A");
    JavaClassType clazzTypeC = JavaIdentifierFactory.getInstance().getClassType("ClassHierarchy.C");

    // Check direct subtypes
    Set<ClassType> subtypes = typeHierarchy.directSubtypesOf(clazzTypeC);
    Assert.assertTrue(subtypes.stream().allMatch(type -> type.getClassName().equals("D")));
    Assert.assertTrue(
        subtypes.stream()
            .allMatch(type -> type.getFullyQualifiedName().equals("ClassHierarchy.D")));

    // Examine super types
    List<ClassType> superClasses = typeHierarchy.superClassesOf(clazzTypeC);
    Assert.assertEquals(
        superClasses,
        Arrays.asList(
            clazzTypeA, JavaIdentifierFactory.getInstance().getClassType("java.lang.Object")));
  }
}
