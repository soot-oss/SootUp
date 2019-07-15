package de.upb.soot.typehierarchy;

import static de.upb.soot.util.ImmutableUtils.immutableList;
import static de.upb.soot.util.ImmutableUtils.immutableSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.IdentifierFactory;
import de.upb.soot.Project;
import de.upb.soot.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.View;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class ViewTypeHierarchyTest {

  private static View view;
  private static ViewTypeHierarchy typeHierarchy;

  @BeforeClass
  public static void setup() {
    String jarFile = "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar";
    assertTrue(new File(jarFile).exists());
    String currentClassPath =
        System.getProperty("java.class.path")
            + File.pathSeparator
            + ManagementFactory.getRuntimeMXBean().getBootClassPath();
    String rtJarClassPath =
        Arrays.stream(currentClassPath.split(File.pathSeparator))
            .filter(pathEntry -> pathEntry.endsWith(File.separator + "rt.jar"))
            .distinct()
            .collect(Collectors.joining(File.pathSeparator));
    Project<JavaClassPathAnalysisInputLocation> p =
        new Project<>(
            new JavaClassPathAnalysisInputLocation(jarFile + File.pathSeparator + rtJarClassPath));
    view = p.createOnDemandView();
    typeHierarchy = new ViewTypeHierarchy(view);
  }

  @Test
  public void implementersOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType iNamespace = factory.getClassType("INamespace", "de.upb.soot.namespaces");
    Set<JavaClassType> implementers = typeHierarchy.implementersOf(iNamespace);
    assertEquals(
        immutableSet(
            factory.getClassType("de.upb.soot.namespaces.PathBasedNamespace$ArchiveBasedNamespace"),
            factory.getClassType("de.upb.soot.namespaces.PathBasedNamespace"),
            factory.getClassType("de.upb.soot.namespaces.AbstractNamespace"),
            factory.getClassType("de.upb.soot.namespaces.JavaClassPathNamespace"),
            factory.getClassType(
                "de.upb.soot.namespaces.PathBasedNamespace$DirectoryBasedNamespace")),
        implementers);
  }

  @Test
  public void subclassesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType abstractNamespace =
        factory.getClassType("AbstractNamespace", "de.upb.soot.namespaces");
    Set<JavaClassType> subclasses = typeHierarchy.subclassesOf(abstractNamespace);
    assertEquals(
        immutableSet(
            factory.getClassType("de.upb.soot.namespaces.PathBasedNamespace$ArchiveBasedNamespace"),
            factory.getClassType("de.upb.soot.namespaces.PathBasedNamespace"),
            factory.getClassType("de.upb.soot.namespaces.JavaClassPathNamespace"),
            factory.getClassType(
                "de.upb.soot.namespaces.PathBasedNamespace$DirectoryBasedNamespace")),
        subclasses);
  }

  @Test
  public void implementedInterfacesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType javaClassPathNamespace =
        factory.getClassType("JavaClassPathNamespace", "de.upb.soot.namespaces");
    Set<JavaClassType> implementedInterfaces =
        typeHierarchy.implementedInterfacesOf(javaClassPathNamespace);
    assertEquals(
        immutableSet(factory.getClassType("de.upb.soot.namespaces.INamespace")),
        implementedInterfaces);
  }

  @Test
  public void superClassOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType javaClassPathNamespace =
        factory.getClassType("JavaClassPathNamespace", "de.upb.soot.namespaces");
    JavaClassType superClass = typeHierarchy.superClassOf(javaClassPathNamespace);
    assertEquals(factory.getClassType("de.upb.soot.namespaces.AbstractNamespace"), superClass);
  }

  @Test
  public void superClassesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType javaClassPathNamespace =
        factory.getClassType("JavaClassPathNamespace", "de.upb.soot.namespaces");
    List<JavaClassType> superClasses = typeHierarchy.superClassesOf(javaClassPathNamespace);
    assertEquals(
        immutableList(
            factory.getClassType("de.upb.soot.namespaces.AbstractNamespace"),
            factory.getClassType("java.lang.Object")),
        superClasses);
  }
}
