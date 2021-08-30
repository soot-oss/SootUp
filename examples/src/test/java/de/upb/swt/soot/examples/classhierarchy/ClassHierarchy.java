package de.upb.swt.soot.examples.classhierarchy;

import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 * The test files for this example consist of a class hierarchy as follows:
 *
 * <pre>
 *         |-- B
 *    A <--|
 *         |-- C <-- D
 *  </pre>
 *
 * This code example will show you how to build and examine a class hierarchy using Soot.
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

    JavaView view = project.createOnDemandView();

    // Create type hierarchy
    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);

    // Specify class types we want to receive information about
    JavaClassType clazzTypeA = JavaIdentifierFactory.getInstance().getClassType("ClassHierarchy.A");
    JavaClassType clazzTypeC = JavaIdentifierFactory.getInstance().getClassType("ClassHierarchy.C");

    // Check direct subtypes
    Set<ClassType> subtypes = typeHierarchy.directSubtypesOf(clazzTypeC);
    Assert.assertTrue(subtypes.stream().allMatch(type -> type.getClassName().equals("D")));

    // Examine super types
    List<ClassType> superClasses = typeHierarchy.superClassesOf(clazzTypeC);
    Assert.assertEquals(
        superClasses,
        Arrays.asList(
            clazzTypeA, JavaIdentifierFactory.getInstance().getClassType("java.lang.Object")));
  }
}
