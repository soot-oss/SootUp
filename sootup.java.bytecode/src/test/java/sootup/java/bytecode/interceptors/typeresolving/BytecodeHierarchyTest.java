package sootup.java.bytecode.interceptors.typeresolving;

import categories.Java8Test;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.IdentifierFactory;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.*;
import sootup.core.util.ImmutableUtils;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.interceptors.typeresolving.types.BottomType;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/** @author Zun Wang */
@Category(Java8Test.class)
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
    JavaProject p =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(analysisInputLocation).build();
    view = p.createOnDemandView();
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
    Assert.assertTrue(hierarchy.isAncestor(double_class1, double_class2));
    Assert.assertFalse(hierarchy.isAncestor(double_class1, double_prim));
    Assert.assertTrue(hierarchy.isAncestor(int_prim, bt));
    Assert.assertFalse(hierarchy.isAncestor(double_prim, int_prim));
    Assert.assertFalse(hierarchy.isAncestor(bt, double_prim));
    Assert.assertFalse(hierarchy.isAncestor(bt, double_class1));
    Assert.assertTrue(hierarchy.isAncestor(nullType, bt));
    Assert.assertFalse(hierarchy.isAncestor(bt, nullType));
    Assert.assertTrue(hierarchy.isAncestor(int_class, nullType));
    Assert.assertFalse(hierarchy.isAncestor(object, double_prim));
    Assert.assertTrue(hierarchy.isAncestor(rootInterface1, class1));
    Assert.assertTrue(hierarchy.isAncestor(rootInterface1, class2));
    Assert.assertTrue(hierarchy.isAncestor(rootInterface2, class4));
    Assert.assertFalse(hierarchy.isAncestor(rootInterface2, class1));
    Assert.assertTrue(hierarchy.isAncestor(object, rootInterface1));
    Assert.assertTrue(hierarchy.isAncestor(object, class1AArr));
    Assert.assertTrue(hierarchy.isAncestor(serializable, class1AArr));
    Assert.assertTrue(hierarchy.isAncestor(cloneable, class1AArr));
    Assert.assertFalse(hierarchy.isAncestor(class1AArr, object));
    Assert.assertTrue(hierarchy.isAncestor(seriArr, doubleArr));
    Assert.assertTrue(hierarchy.isAncestor(class1AArr, class2AArr));
    Assert.assertFalse(hierarchy.isAncestor(class2AArr, class1AArr));
    Assert.assertTrue(hierarchy.isAncestor(seriArr, class1AArr));
    Assert.assertTrue(hierarchy.isAncestor(objArr, class2AArr));
    Assert.assertFalse(hierarchy.isAncestor(seriArr, class3Arr));
  }

  @Test
  public void testLCA() {
    // setup view and ViewTypeHierarchy
    setUp();
    BytecodeHierarchy hierarchy = new BytecodeHierarchy(view);

    // tests
    Collection<Type> actualSet = hierarchy.getLeastCommonAncestor(double_prim, int_prim);
    Collection<Type> expectedSet = Collections.emptySet();
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(rootInterface1, class1);
    expectedSet = Collections.singleton(rootInterface1);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(double_class1, int_class);
    expectedSet = ImmutableUtils.immutableSet(number, comparable);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(rootInterface1, rootInterface2);
    expectedSet = Collections.singleton(object);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(intArr_prim, doubleArr_prim);
    expectedSet = ImmutableUtils.immutableSet(object, serializable, cloneable);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class1AArr, class3Arr);
    expectedSet = ImmutableUtils.immutableSet(objArr);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class1AArr, class2AArr);
    expectedSet = ImmutableUtils.immutableSet(class1AArr);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class3Arr, class4Arr);
    expectedSet = Collections.singleton(class2Arr);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class4, class4Arr);
    expectedSet = ImmutableUtils.immutableSet(serializable, cloneable);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class2, class4Arr);
    expectedSet = Collections.singleton(object);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(class2, class4Arr);
    expectedSet = Collections.singleton(object);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(short_prim, byte_prim);
    expectedSet = Collections.singleton(int_prim);
    Assert.assertEquals(expectedSet, actualSet);

    actualSet = hierarchy.getLeastCommonAncestor(shortArr, byteArr);
    expectedSet = ImmutableUtils.immutableSet(object, serializable, cloneable);
    Assert.assertEquals(expectedSet, actualSet);
  }
}
