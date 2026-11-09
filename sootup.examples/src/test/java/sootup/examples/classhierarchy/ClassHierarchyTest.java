package sootup.examples.classhierarchy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class ClassHierarchyTest {

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
    JavaIdentifierFactory identifierFactory = view.getIdentifierFactory();
    JavaClassType clazzTypeA = identifierFactory.getClassType("A");
    JavaClassType clazzTypeC = identifierFactory.getClassType("C");

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
        Arrays.asList(clazzTypeA, identifierFactory.getClassType("java.lang.Object")));
  }
}
