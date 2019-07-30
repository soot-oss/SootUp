package de.upb.soot.typehierarchy;

import static de.upb.soot.util.ImmutableUtils.immutableList;
import static de.upb.soot.util.ImmutableUtils.immutableSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.IdentifierFactory;
import de.upb.soot.Project;
import de.upb.soot.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.soot.types.ArrayType;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;
import de.upb.soot.util.ImmutableUtils;
import de.upb.soot.views.View;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
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

    // Test with an interface that extends another one, i.e. List extends Collection
    JavaClassType arrayList = factory.getClassType("ArrayList", "java.util");
    JavaClassType collection = factory.getClassType("Collection", "java.util");
    JavaClassType list = factory.getClassType("List", "java.util");
    Set<JavaClassType> implementedInterfacesOfArrayList =
        typeHierarchy.implementedInterfacesOf(arrayList);
    assertTrue(
        "ArrayList implements Collection", implementedInterfacesOfArrayList.contains(collection));
    assertTrue("ArrayList implements List", implementedInterfacesOfArrayList.contains(list));
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

  @Test
  public void javaClassTypeSubtyping() {}

  @Test
  public void primitiveTypeSubtyping() {}

  @Test
  public void arraySubtyping() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ArrayType stringArrayDim1Type =
        factory.getArrayType(factory.getClassType("java.lang.String"), 1);
    ArrayType stringArrayDim3Type =
        factory.getArrayType(factory.getClassType("java.lang.String"), 3);

    ArrayType objectArrayDim1Type =
        factory.getArrayType(factory.getClassType("java.lang.Object"), 1);
    ArrayType objectArrayDim2Type =
        factory.getArrayType(factory.getClassType("java.lang.Object"), 2);
    ArrayType objectArrayDim3Type =
        factory.getArrayType(factory.getClassType("java.lang.Object"), 3);

    ArrayType doubleArrayDim1Type = factory.getArrayType(PrimitiveType.getDouble(), 1);
    ArrayType doubleArrayDim2Type = factory.getArrayType(PrimitiveType.getDouble(), 2);

    JavaClassType objectType = factory.getClassType("java.lang.Object");

    // We don't consider types to be subtypes of itself
    Stream.of(
            stringArrayDim1Type,
            stringArrayDim3Type,
            objectArrayDim1Type,
            objectArrayDim2Type,
            objectArrayDim3Type,
            doubleArrayDim2Type,
            doubleArrayDim1Type)
        .forEach(type -> assertFalse(typeHierarchy.isSubtype(type, type)));

    Set<Pair<Type, Type>> subtypes =
        ImmutableUtils.immutableSet(
            Pair.of(objectArrayDim1Type, stringArrayDim1Type),
            Pair.of(objectArrayDim1Type, stringArrayDim3Type),
            Pair.of(objectArrayDim2Type, stringArrayDim3Type),
            Pair.of(objectArrayDim3Type, stringArrayDim3Type),
            Pair.of(objectType, objectArrayDim1Type),
            Pair.of(objectType, objectArrayDim2Type),
            Pair.of(objectType, doubleArrayDim1Type),
            Pair.of(objectArrayDim1Type, doubleArrayDim2Type));

    subtypes.forEach(
        subtypePair -> {
          assertTrue(
              subtypePair.getRight() + " should be a subtype of " + subtypePair.getLeft(),
              typeHierarchy.isSubtype(subtypePair.getLeft(), subtypePair.getRight()));
          assertFalse(
              subtypePair.getLeft() + " should not be a subtype of " + subtypePair.getRight(),
              typeHierarchy.isSubtype(subtypePair.getRight(), subtypePair.getLeft()));
        });

    assertFalse(
        doubleArrayDim1Type + " should not be a subtype of " + objectArrayDim1Type,
        typeHierarchy.isSubtype(objectArrayDim1Type, doubleArrayDim1Type));
    assertFalse(
        stringArrayDim1Type + " should not be a subtype of " + objectArrayDim1Type,
        typeHierarchy.isSubtype(objectArrayDim2Type, stringArrayDim1Type));
  }
}
