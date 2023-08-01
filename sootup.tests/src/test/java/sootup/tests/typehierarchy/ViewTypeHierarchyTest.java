package sootup.tests.typehierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static sootup.core.util.ImmutableUtils.immutableList;
import static sootup.core.util.ImmutableUtils.immutableSet;

import categories.Java8Test;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.IdentifierFactory;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.util.ImmutableUtils;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/** @author Kaustubh Kelkar update on 22.04.2020 */
@Category(Java8Test.class)
public class ViewTypeHierarchyTest {

  private JavaView view;
  private ViewTypeHierarchy typeHierarchy;
  private JavaClassPathAnalysisInputLocation analysisInputLocation;

  @Before
  public void setup() {

    String jarFile = MethodDispatchResolverTest.jarFile;
    assertTrue("File " + jarFile + " not found.", new File(jarFile).exists());
    String currentClassPath =
        System.getProperty("java.class.path")
            + File.pathSeparator
            + ManagementFactory.getRuntimeMXBean().getBootClassPath();
    String rtJarClassPath =
        Arrays.stream(currentClassPath.split(File.pathSeparator))
            .filter(pathEntry -> pathEntry.endsWith(File.separator + "rt.jar"))
            .distinct()
            .collect(Collectors.joining(File.pathSeparator));
    analysisInputLocation =
        new JavaClassPathAnalysisInputLocation(jarFile + File.pathSeparator + rtJarClassPath);
    JavaProject p =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(analysisInputLocation).build();
    view = p.createView();
    typeHierarchy = new ViewTypeHierarchy(view);
  }

  @Test
  public void implementersOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType iNamespace = factory.getClassType("IFaceOperations", "utils");
    Set<ClassType> implementers = typeHierarchy.implementersOf(iNamespace);
    ImmutableSet<ClassType> expectedImplementers =
        immutableSet(factory.getClassType("utils.Operations"));
    assertEquals(expectedImplementers, implementers);

    expectedImplementers.forEach(
        expectedImplementer ->
            assertTrue(typeHierarchy.isSubtype(iNamespace, expectedImplementer)));
  }

  @Test
  public void subclassesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType abstractNamespace = factory.getClassType("AbstractDataStrcture", "ds");
    Set<ClassType> subclasses = typeHierarchy.subclassesOf(abstractNamespace);
    ImmutableSet<ClassType> expectedSubclasses =
        immutableSet(factory.getClassType("ds.Employee"), factory.getClassType("ds.Department"));
    assertEquals(expectedSubclasses, subclasses);
    assertFalse(
        "A class should not be a subclass of itself", subclasses.contains(abstractNamespace));
    expectedSubclasses.forEach(
        expectedSubclass ->
            assertTrue(typeHierarchy.isSubtype(abstractNamespace, expectedSubclass)));
  }

  @Test
  public void implementedInterfacesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType javaClassPathNamespace = factory.getClassType("Operations", "utils");
    ClassType iNamespace = factory.getClassType("utils.IFaceOperations");
    Set<ClassType> implementedInterfaces =
        typeHierarchy.implementedInterfacesOf(javaClassPathNamespace);
    assertEquals(immutableSet(iNamespace), implementedInterfaces);
    assertTrue(typeHierarchy.isSubtype(iNamespace, javaClassPathNamespace));

    // Test with an interface that extends another one, i.e. List extends Collection
    ClassType arrayList = factory.getClassType("ArrayList", "java.util");
    ClassType collection = factory.getClassType("Collection", "java.util");
    ClassType list = factory.getClassType("List", "java.util");
    Set<ClassType> implementedInterfacesOfArrayList =
        typeHierarchy.implementedInterfacesOf(arrayList);
    assertTrue(
        "ArrayList implements Collection", implementedInterfacesOfArrayList.contains(collection));
    assertTrue("ArrayList implements Collection", typeHierarchy.isSubtype(collection, arrayList));
    assertTrue("ArrayList implements List", implementedInterfacesOfArrayList.contains(list));
    assertTrue("ArrayList implements List", typeHierarchy.isSubtype(list, arrayList));
    assertTrue(
        "List is an implementer of Collection",
        typeHierarchy.implementersOf(collection).contains(list));
  }

  @Test
  public void superClassOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType javaClassPathNamespace = factory.getClassType("Employee", "ds");
    ClassType superClass = typeHierarchy.superClassOf(javaClassPathNamespace);
    assertEquals(factory.getClassType("ds.AbstractDataStrcture"), superClass);
    assertNull(
        "java.lang.Object should not have a superclass",
        typeHierarchy.superClassOf(factory.getClassType("java.lang.Object")));
    assertEquals(
        "In Soot, interfaces should have java.lang.Object as the superclass",
        factory.getClassType("java.lang.Object"),
        typeHierarchy.superClassOf(factory.getClassType("java.util.Collection")));
  }

  @Test
  public void superClassesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType javaClassPathNamespace = factory.getClassType("Employee", "ds");
    List<ClassType> superClasses = typeHierarchy.superClassesOf(javaClassPathNamespace);
    ImmutableList<ClassType> expectedSuperClasses =
        immutableList(
            factory.getClassType("ds.AbstractDataStrcture"),
            factory.getClassType("java.lang.Object"));

    assertEquals(expectedSuperClasses, superClasses);
    expectedSuperClasses.forEach(
        expectedSuperClass ->
            assertTrue(typeHierarchy.isSubtype(expectedSuperClass, javaClassPathNamespace)));
  }

  @Test
  public void primitiveTypeSubtyping() {
    assertFalse(
        "Primitive types should not have subtype relations",
        typeHierarchy.isSubtype(PrimitiveType.getInt(), PrimitiveType.getInt()));
    assertFalse(
        "Primitive types should not have subtype relations",
        typeHierarchy.isSubtype(PrimitiveType.getDouble(), PrimitiveType.getInt()));
  }

  @Test
  public void nullTypeSubtyping() {
    IdentifierFactory factory = view.getIdentifierFactory();
    assertTrue(
        "null should be valid value for all reference types",
        typeHierarchy.isSubtype(factory.getClassType("java.lang.Object"), NullType.getInstance()));
    assertTrue(
        "null should be valid value for all reference types",
        typeHierarchy.isSubtype(factory.getClassType("java.lang.String"), NullType.getInstance()));
    assertTrue(
        "null should be valid value for all reference types",
        typeHierarchy.isSubtype(
            factory.getClassType("JavaClassPathNamespace", "de.upb.sootup.namespaces"),
            NullType.getInstance()));
    assertFalse(
        "null should not be a valid value for primitive types",
        typeHierarchy.isSubtype(PrimitiveType.getInt(), NullType.getInstance()));
    assertFalse(
        "null should not be a valid value for primitive types",
        typeHierarchy.isSubtype(PrimitiveType.getDouble(), NullType.getInstance()));
  }

  @Test
  public void addType() {
    IdentifierFactory factory = view.getIdentifierFactory();
    OverridingJavaClassSource classSource =
        new OverridingJavaClassSource(
            analysisInputLocation,
            null,
            factory.getClassType("adummytype.Type"),
            factory.getClassType("ds.Employee"),
            Collections.emptySet(),
            null,
            Collections.emptySet(),
            Collections.emptySet(),
            null,
            EnumSet.of(ClassModifier.FINAL),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList());
    SootClass sootClass = new SootClass(classSource, SourceType.Application);

    typeHierarchy.addType(sootClass);

    assertTrue(
        "Newly added type must be detected as a subtype",
        typeHierarchy
            .subclassesOf(factory.getClassType("ds.AbstractDataStrcture"))
            .contains(sootClass.getType()));
  }

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

    ArrayType collectionArrayDim1Type =
        factory.getArrayType(factory.getClassType("java.util.Collection"), 1);
    ArrayType arrayListDim1Type =
        factory.getArrayType(factory.getClassType("java.util.ArrayList"), 1);
    ArrayType arrayListDim2Type =
        factory.getArrayType(factory.getClassType("java.util.ArrayList"), 2);

    ArrayType serializableDim1Type =
        factory.getArrayType(factory.getClassType("java.io.Serializable"), 1);
    ArrayType serializableDim2Type =
        factory.getArrayType(factory.getClassType("java.io.Serializable"), 2);
    ArrayType cloneableDim1Type =
        factory.getArrayType(factory.getClassType("java.lang.Cloneable"), 1);

    ClassType objectType = factory.getClassType("java.lang.Object");
    ClassType stringType = factory.getClassType("java.lang.String");
    ClassType serializableType = factory.getClassType("java.io.Serializable");
    ClassType cloneableType = factory.getClassType("java.lang.Cloneable");
    ClassType arrayListType = factory.getClassType("java.util.ArrayList");

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
            Pair.of(serializableType, objectArrayDim1Type),
            Pair.of(serializableType, stringType),
            Pair.of(serializableDim1Type, stringArrayDim1Type),
            Pair.of(serializableDim1Type, stringArrayDim3Type),
            Pair.of(serializableDim1Type, objectArrayDim2Type),
            Pair.of(serializableDim2Type, objectArrayDim3Type),
            Pair.of(cloneableType, arrayListType),
            Pair.of(cloneableDim1Type, arrayListDim1Type),
            Pair.of(cloneableDim1Type, arrayListDim2Type),
            Pair.of(cloneableDim1Type, objectArrayDim2Type),
            Pair.of(objectArrayDim1Type, stringArrayDim1Type),
            Pair.of(objectArrayDim1Type, stringArrayDim3Type),
            Pair.of(objectArrayDim2Type, stringArrayDim3Type),
            Pair.of(objectArrayDim3Type, stringArrayDim3Type),
            Pair.of(objectType, objectArrayDim1Type),
            Pair.of(objectType, objectArrayDim2Type),
            Pair.of(objectType, doubleArrayDim1Type),
            Pair.of(serializableType, objectArrayDim1Type),
            Pair.of(serializableType, objectArrayDim2Type),
            Pair.of(serializableType, doubleArrayDim1Type),
            Pair.of(cloneableType, objectArrayDim1Type),
            Pair.of(cloneableType, objectArrayDim2Type),
            Pair.of(cloneableType, doubleArrayDim1Type),
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

    assertFalse(
        objectArrayDim1Type + " should not be a subtype of " + serializableDim1Type,
        typeHierarchy.isSubtype(serializableDim1Type, objectArrayDim1Type));
    assertFalse(
        objectArrayDim1Type + " should not be a subtype of " + cloneableDim1Type,
        typeHierarchy.isSubtype(cloneableDim1Type, objectArrayDim1Type));
    assertFalse(
        objectArrayDim2Type + " should not be a subtype of " + serializableDim2Type,
        typeHierarchy.isSubtype(serializableDim2Type, objectArrayDim2Type));
    assertFalse(
        arrayListDim2Type + " should not be a subtype of " + collectionArrayDim1Type,
        typeHierarchy.isSubtype(collectionArrayDim1Type, arrayListDim2Type));

    assertTrue(
        "Collection[] should be a subtype of Object[]",
        typeHierarchy.isSubtype(objectArrayDim1Type, collectionArrayDim1Type));
  }
}
