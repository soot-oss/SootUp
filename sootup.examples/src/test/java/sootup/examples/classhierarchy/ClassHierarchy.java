package sootup.examples.classhierarchy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
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
@Tag("Java8")
public class ClassHierarchy {

  @Test
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation("src/test/resources/ClassHierarchy/binary"));
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation()); // add rt.jar

    JavaView view = new JavaView(inputLocations);

    // Create type hierarchy
    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);

    // Specify class types we want to receive information about
    JavaClassType clazzTypeA = JavaIdentifierFactory.getInstance().getClassType("A");
    JavaClassType clazzTypeC = JavaIdentifierFactory.getInstance().getClassType("C");

    // Check direct subtypes
    Set<ClassType> subtypes =
        typeHierarchy.directSubtypesOf(clazzTypeC).collect(Collectors.toSet());
    assertTrue(subtypes.stream().allMatch(type -> type.getClassName().equals("D")));
    assertTrue(subtypes.stream().allMatch(type -> type.getFullyQualifiedName().equals("D")));

    // Examine super types
    List<ClassType> superClasses =
        typeHierarchy.superClassesOf(clazzTypeC).collect(Collectors.toList());
    assertEquals(
        superClasses,
        Arrays.asList(
            clazzTypeA, JavaIdentifierFactory.getInstance().getClassType("java.lang.Object")));
  }
}
