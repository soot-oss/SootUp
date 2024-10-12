package sootup.java.bytecode.frontend.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.*;

import categories.TestCategories;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.types.*;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.typeresolving.BytecodeHierarchy;
import sootup.interceptors.typeresolving.types.BottomType;
import sootup.interceptors.typeresolving.types.TopType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
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
    JavaClassPathAnalysisInputLocation analysisInputLocation =
        new JavaClassPathAnalysisInputLocation(jarFile);
    view =
        new JavaView(
            Arrays.asList(new DefaultRuntimeAnalysisInputLocation(), analysisInputLocation));

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
    doubleArr_prim = factory.getArrayType(PrimitiveType.getDouble(), 1);
    intArr_prim = factory.getArrayType(PrimitiveType.getInt(), 1);
    class1AArr = factory.getArrayType(class1, 2);
    class2AArr = factory.getArrayType(class2, 2);
    class2Arr = factory.getArrayType(class2, 1);
    class3Arr = factory.getArrayType(class3, 1);
    class4Arr = factory.getArrayType(class4, 1);
    shortArr = factory.getArrayType(PrimitiveType.getShort(), 1);
    byteArr = factory.getArrayType(PrimitiveType.getByte(), 1);
  }

  @Test
  public void testIsAncestor() {
    // setup view and ViewTypeHierarchy
    setUp();
    BytecodeHierarchy hierarchy = new BytecodeHierarchy(view);

    // tests
    assertTrue(hierarchy.isAncestor(double_class1, double_class2));
    assertFalse(hierarchy.isAncestor(double_class1, PrimitiveType.getDouble()));
    assertTrue(hierarchy.isAncestor(PrimitiveType.getInt(), BottomType.getInstance()));
    assertFalse(hierarchy.isAncestor(PrimitiveType.getDouble(), PrimitiveType.getInt()));
    assertFalse(hierarchy.isAncestor(BottomType.getInstance(), PrimitiveType.getDouble()));
    assertFalse(hierarchy.isAncestor(BottomType.getInstance(), double_class1));
    assertTrue(hierarchy.isAncestor(NullType.getInstance(), BottomType.getInstance()));
    assertFalse(hierarchy.isAncestor(BottomType.getInstance(), NullType.getInstance()));
    assertTrue(hierarchy.isAncestor(int_class, NullType.getInstance()));
    assertFalse(hierarchy.isAncestor(object, PrimitiveType.getDouble()));
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
    Collection<Type> actualSet;
    Collection<Type> expectedSet;

    actualSet = hierarchy.getLeastCommonAncestors(double_class1, int_class);
    expectedSet = ImmutableUtils.immutableSet(number, comparable);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(rootInterface1, class1);
    expectedSet = Collections.singleton(rootInterface1);
    assertEquals(expectedSet, actualSet);

    actualSet =
        hierarchy.getLeastCommonAncestors(PrimitiveType.getDouble(), PrimitiveType.getInt());
    expectedSet = Collections.singleton(TopType.getInstance());
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(rootInterface1, rootInterface2);
    expectedSet = Collections.singleton(object);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(intArr_prim, doubleArr_prim);
    expectedSet = ImmutableUtils.immutableSet(object, serializable, cloneable);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(class1AArr, class3Arr);
    expectedSet = ImmutableUtils.immutableSet(objArr);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(class1AArr, class2AArr);
    expectedSet = ImmutableUtils.immutableSet(class1AArr);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(class3Arr, class4Arr);
    expectedSet = Collections.singleton(class2Arr);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(class4, class4Arr);
    expectedSet = ImmutableUtils.immutableSet(serializable, cloneable);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(class2, class4Arr);
    expectedSet = Collections.singleton(object);
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(class2, class4Arr);
    expectedSet = Collections.singleton(object);
    assertEquals(expectedSet, actualSet);

    actualSet =
        hierarchy.getLeastCommonAncestors(PrimitiveType.getShort(), PrimitiveType.getByte());
    expectedSet = Collections.singleton(PrimitiveType.getShort());
    assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestors(shortArr, byteArr);
    expectedSet = ImmutableUtils.immutableSet(object, serializable, cloneable);
    assertEquals(expectedSet, actualSet);
  }
}
