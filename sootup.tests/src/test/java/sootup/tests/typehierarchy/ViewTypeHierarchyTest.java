package sootup.tests.typehierarchy;

import static org.junit.jupiter.api.Assertions.*;
import static sootup.core.util.ImmutableUtils.immutableList;
import static sootup.core.util.ImmutableUtils.immutableSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.*;
import sootup.core.util.ImmutableUtils;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.views.JavaView;

/**
 * @author Kaustubh Kelkar update on 22.04.2020
 */
public class ViewTypeHierarchyTest {

  private JavaView view;
  private ViewTypeHierarchy typeHierarchy;
  private JavaClassPathAnalysisInputLocation analysisInputLocation;

  @BeforeEach
  public void setup() {

    String jarFile = "../shared-test-resources/java-miniapps/MiniApp.jar";
    assertTrue(new File(jarFile).exists(), "File " + jarFile + " not found.");
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    analysisInputLocation = new JavaClassPathAnalysisInputLocation(jarFile);
    inputLocations.add(analysisInputLocation);
    view = new JavaView(inputLocations);
    typeHierarchy = new ViewTypeHierarchy(view);
  }

  @Test
  public void implementersOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType iNamespace = factory.getClassType("IFaceOperations", "utils");
    List<ClassType> implementers =
        typeHierarchy.implementersOf(iNamespace).collect(Collectors.toList());
    ClassType expectedImplementer = factory.getClassType("utils.Operations");
    assertEquals(expectedImplementer, implementers.get(0));
    assertTrue(typeHierarchy.isSubtype(iNamespace, expectedImplementer));
  }

  @Test
  public void subclassesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType abstractNamespace = factory.getClassType("AbstractDataStrcture", "ds");
    Set<ClassType> subclasses =
        typeHierarchy.subclassesOf(abstractNamespace).collect(Collectors.toSet());
    ImmutableSet<ClassType> expectedSubclasses =
        immutableSet(factory.getClassType("ds.Employee"), factory.getClassType("ds.Department"));
    assertEquals(expectedSubclasses, subclasses);
    assertFalse(
        subclasses.contains(abstractNamespace), "A class should not be a subclass of itself");
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
        typeHierarchy.implementedInterfacesOf(javaClassPathNamespace).collect(Collectors.toSet());
    assertEquals(immutableSet(iNamespace), implementedInterfaces);
    assertTrue(typeHierarchy.isSubtype(iNamespace, javaClassPathNamespace));

    // Test with an interface that extends another one, i.e. List extends Collection
    ClassType arrayList = factory.getClassType("ArrayList", "java.util");
    ClassType collection = factory.getClassType("Collection", "java.util");
    ClassType list = factory.getClassType("List", "java.util");
    Set<ClassType> implementedInterfacesOfArrayList =
        typeHierarchy.implementedInterfacesOf(arrayList).collect(Collectors.toSet());
    assertTrue(
        implementedInterfacesOfArrayList.contains(collection), "ArrayList implements Collection");
    assertTrue(typeHierarchy.isSubtype(collection, arrayList), "ArrayList implements Collection");
    assertTrue(implementedInterfacesOfArrayList.contains(list), "ArrayList implements List");
    assertTrue(typeHierarchy.isSubtype(list, arrayList), "ArrayList implements List");
    assertTrue(
        typeHierarchy.implementersOf(collection).collect(Collectors.toSet()).contains(list),
        "List is an implementer of Collection");
  }

  @Test
  public void superClassOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType javaClassPathNamespace = factory.getClassType("Employee", "ds");
    ClassType superClass = typeHierarchy.superClassOf(javaClassPathNamespace).get();
    assertEquals(factory.getClassType("ds.AbstractDataStrcture"), superClass);
    assertFalse(
        typeHierarchy.superClassOf(factory.getClassType("java.lang.Object")).isPresent(),
        "java.lang.Object should not have a superclass");
    assertEquals(
        factory.getClassType("java.lang.Object"),
        typeHierarchy.superClassOf(factory.getClassType("java.lang.Iterable")).get(),
        "In Soot, interfaces should have java.lang.Object as the superclass");
  }

  @Test
  public void superClassesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType javaClassPathNamespace = factory.getClassType("Employee", "ds");
    List<ClassType> superClasses =
        typeHierarchy.superClassesOf(javaClassPathNamespace).collect(Collectors.toList());
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
        typeHierarchy.isSubtype(PrimitiveType.getInt(), PrimitiveType.getInt()),
        "Primitive types should not have subtype relations");
    assertFalse(
        typeHierarchy.isSubtype(PrimitiveType.getDouble(), PrimitiveType.getInt()),
        "Primitive types should not have subtype relations");
  }

  @Test
  public void nullTypeSubtyping() {
    IdentifierFactory factory = view.getIdentifierFactory();
    assertTrue(
        typeHierarchy.isSubtype(factory.getClassType("java.lang.Object"), NullType.getInstance()),
        "null should be valid value for all reference types");
    assertTrue(
        typeHierarchy.isSubtype(factory.getClassType("java.lang.String"), NullType.getInstance()),
        "null should be valid value for all reference types");
    assertTrue(
        typeHierarchy.isSubtype(
            factory.getClassType("JavaClassPathNamespace", "de.upb.sootup.namespaces"),
            NullType.getInstance()),
        "null should be valid value for all reference types");
    assertFalse(
        typeHierarchy.isSubtype(PrimitiveType.getInt(), NullType.getInstance()),
        "null should not be a valid value for primitive types");
    assertFalse(
        typeHierarchy.isSubtype(PrimitiveType.getDouble(), NullType.getInstance()),
        "null should not be a valid value for primitive types");
  }

  @Test
  public void addType() {
    JavaIdentifierFactory factory = view.getIdentifierFactory();
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
        typeHierarchy
            .subclassesOf(factory.getClassType("ds.AbstractDataStrcture"))
            .collect(Collectors.toSet())
            .contains(sootClass.getType()),
        "Newly added type must be detected as a subtype");
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
              typeHierarchy.isSubtype(subtypePair.getLeft(), subtypePair.getRight()),
              subtypePair.getRight() + " should be a subtype of " + subtypePair.getLeft());
          assertFalse(
              typeHierarchy.isSubtype(subtypePair.getRight(), subtypePair.getLeft()),
              subtypePair.getLeft() + " should not be a subtype of " + subtypePair.getRight());
        });

    assertFalse(
        typeHierarchy.isSubtype(objectArrayDim1Type, doubleArrayDim1Type),
        doubleArrayDim1Type + " should not be a subtype of " + objectArrayDim1Type);
    assertFalse(
        typeHierarchy.isSubtype(objectArrayDim2Type, stringArrayDim1Type),
        stringArrayDim1Type + " should not be a subtype of " + objectArrayDim1Type);

    assertFalse(
        typeHierarchy.isSubtype(serializableDim1Type, objectArrayDim1Type),
        objectArrayDim1Type + " should not be a subtype of " + serializableDim1Type);
    assertFalse(
        typeHierarchy.isSubtype(cloneableDim1Type, objectArrayDim1Type),
        objectArrayDim1Type + " should not be a subtype of " + cloneableDim1Type);
    assertFalse(
        typeHierarchy.isSubtype(serializableDim2Type, objectArrayDim2Type),
        objectArrayDim2Type + " should not be a subtype of " + serializableDim2Type);
    assertFalse(
        typeHierarchy.isSubtype(collectionArrayDim1Type, arrayListDim2Type),
        arrayListDim2Type + " should not be a subtype of " + collectionArrayDim1Type);

    assertTrue(
        typeHierarchy.isSubtype(objectArrayDim1Type, collectionArrayDim1Type),
        "Collection[] should be a subtype of Object[]");
  }
}
