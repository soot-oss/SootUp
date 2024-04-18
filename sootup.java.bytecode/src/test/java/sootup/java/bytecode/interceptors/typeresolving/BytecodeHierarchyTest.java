package sootup.java.bytecode.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import categories.TestCategories;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.*;
import sootup.core.util.ImmutableUtils;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.interceptors.typeresolving.BytecodeHierarchy;
import sootup.java.core.interceptors.typeresolving.types.BottomType;
import sootup.java.core.interceptors.typeresolving.types.TopType;
import sootup.java.core.views.JavaView;

/** @author Zun Wang */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class BytecodeHierarchyTest {

  private JavaView view;

  private ClassType rootInterface1,
      rootInterface2,
      class1,
      class2,
      class3,
      class4,
      double_class1,
      double_class2,
      int_class,
      object,
      serializable,
      cloneable,
      number,
      comparable;

  private Type double_prim = PrimitiveType.getDouble();
  private Type int_prim = PrimitiveType.getInt();
  private Type short_prim = PrimitiveType.getShort();
  private Type byte_prim = PrimitiveType.getByte();
  private Type bt = BottomType.getInstance();
  private Type nullType = NullType.getInstance();

  private ArrayType objArr,
      seriArr,
      doubleArr,
      class1AArr,
      class2Arr,
      class2AArr,
      class3Arr,
      class4Arr,
      doubleArr_prim,
      intArr_prim,
      shortArr,
      byteArr;

  public void setUp() {
    String jarFile =
        "../shared-test-resources/TypeResolverTestSuite/ByteCodeHierarchyTest/MiniHierarchy.jar";
    String currentClassPath =
        System.getProperty("java.class.path")
            + File.pathSeparator
            + ManagementFactory.getRuntimeMXBean().getBootClassPath();
    String rtJarClassPath =
        Arrays.stream(currentClassPath.split(File.pathSeparator))
            .filter(pathEntry -> pathEntry.endsWith(File.separator + "rt.jar"))
            .distinct()
            .collect(Collectors.joining(File.pathSeparator));
    JavaClassPathAnalysisInputLocation analysisInputLocation =
        new JavaClassPathAnalysisInputLocation(jarFile + File.pathSeparator + rtJarClassPath);

    view = new JavaView(analysisInputLocation);
    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);

    // create types
    IdentifierFactory factory = view.getIdentifierFactory();
    rootInterface1 = factory.getClassType("RootInterface1");
    rootInterface2 = factory.getClassType("RootInterface2");
    class1 = factory.getClassType("Class1");
    class2 = factory.getClassType("Class2");
    class3 = factory.getClassType("Class3");
    class4 = factory.getClassType("Class4");
    double_class1 = factory.getClassType("java.lang.Double");
    double_class2 = factory.getClassType("java.lang.Double");
    int_class = factory.getClassType("java.lang.Integer");
    object = factory.getClassType("java.lang.Object");
    serializable = factory.getClassType("java.io.Serializable");
    cloneable = factory.getClassType("java.lang.Cloneable");
    number = factory.getClassType("java.lang.Number");
    comparable = factory.getClassType("java.lang.Comparable");

    objArr = factory.getArrayType(object, 1);
    seriArr = factory.getArrayType(serializable, 1);
    doubleArr = factory.getArrayType(double_class1, 1);
    doubleArr_prim = factory.getArrayType(double_prim, 1);
    intArr_prim = factory.getArrayType(int_prim, 1);
    class1AArr = factory.getArrayType(class1, 2);
    class2AArr = factory.getArrayType(class2, 2);
    class2Arr = factory.getArrayType(class2, 1);
    class3Arr = factory.getArrayType(class3, 1);
    class4Arr = factory.getArrayType(class4, 1);
    shortArr = factory.getArrayType(short_prim, 1);
    byteArr = factory.getArrayType(byte_prim, 1);
  }

  @Test
  public void testIsAncestor() {
    // setup view and ViewTypeHierarchy
    setUp();
    BytecodeHierarchy hierarchy = new BytecodeHierarchy(view);

    // tests
    assertTrue(hierarchy.isAncestor(double_class1, double_class2));
    assertFalse(hierarchy.isAncestor(double_class1, double_prim));
    assertTrue(hierarchy.isAncestor(int_prim, bt));
    assertFalse(hierarchy.isAncestor(double_prim, int_prim));
    assertFalse(hierarchy.isAncestor(bt, double_prim));
    assertFalse(hierarchy.isAncestor(bt, double_class1));
    assertTrue(hierarchy.isAncestor(nullType, bt));
    assertFalse(hierarchy.isAncestor(bt, nullType));
    assertTrue(hierarchy.isAncestor(int_class, nullType));
    assertFalse(hierarchy.isAncestor(object, double_prim));
    assertTrue(hierarchy.isAncestor(rootInterface1, class1));
    assertTrue(hierarchy.isAncestor(rootInterface1, class2));
    assertTrue(hierarchy.isAncestor(rootInterface2, class4));
    assertFalse(hierarchy.isAncestor(rootInterface2, class1));
    assertTrue(hierarchy.isAncestor(object, rootInterface1));
    assertTrue(hierarchy.isAncestor(object, class1AArr));
    assertTrue(hierarchy.isAncestor(serializable, class1AArr));
    assertTrue(hierarchy.isAncestor(cloneable, class1AArr));
    assertFalse(hierarchy.isAncestor(class1AArr, object));
    assertTrue(hierarchy.isAncestor(seriArr, doubleArr));
    assertTrue(hierarchy.isAncestor(class1AArr, class2AArr));
    assertFalse(hierarchy.isAncestor(class2AArr, class1AArr));
    assertTrue(hierarchy.isAncestor(seriArr, class1AArr));
    assertTrue(hierarchy.isAncestor(objArr, class2AArr));
    assertFalse(hierarchy.isAncestor(seriArr, class3Arr));
  }

  @Test
  public void testLCA() {
    // setup view and ViewTypeHierarchy
    setUp();
    BytecodeHierarchy hierarchy = new BytecodeHierarchy(view);

    // tests
    Collection<Type> actualSet = hierarchy.getLeastCommonAncestor(double_prim, int_prim);
    Collection<Type> expectedSet = Collections.singleton(TopType.getInstance());
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(rootInterface1, class1);
    expectedSet = Collections.singleton(rootInterface1);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(double_class1, int_class);
    expectedSet = ImmutableUtils.immutableSet(number, comparable);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(rootInterface1, rootInterface2);
    expectedSet = Collections.singleton(object);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(intArr_prim, doubleArr_prim);
    expectedSet = ImmutableUtils.immutableSet(object, serializable, cloneable);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class1AArr, class3Arr);
    expectedSet = ImmutableUtils.immutableSet(objArr);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class1AArr, class2AArr);
    expectedSet = ImmutableUtils.immutableSet(class1AArr);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class3Arr, class4Arr);
    expectedSet = Collections.singleton(class2Arr);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class4, class4Arr);
    expectedSet = ImmutableUtils.immutableSet(serializable, cloneable);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class2, class4Arr);
    expectedSet = Collections.singleton(object);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class2, class4Arr);
    expectedSet = Collections.singleton(object);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(short_prim, byte_prim);
    expectedSet = Collections.singleton(short_prim);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(shortArr, byteArr);
    expectedSet = ImmutableUtils.immutableSet(object, serializable, cloneable);
    assertEquals(expectedSet, actualSet);
  }
}
