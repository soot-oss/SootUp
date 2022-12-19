package sootup.core.jimple.basic;

import java.util.HashSet;
import junit.framework.TestCase;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;

public class LocalGeneratorTest extends TestCase {

  public void testGenerate() {
    final LocalGenerator localGenerator = new LocalGenerator(new HashSet<>());
    final Local i0 = localGenerator.generateFieldLocal(PrimitiveType.IntType.getInstance());
    final Local i1 = localGenerator.generateFieldLocal(PrimitiveType.IntType.getInstance());
    final Local s0 = localGenerator.generateFieldLocal(PrimitiveType.ShortType.getInstance());
    final Local r0 =
        localGenerator.generateFieldLocal(
            new ClassType() {
              @Override
              public boolean isBuiltInClass() {
                return false;
              }

              @Override
              public String getFullyQualifiedName() {
                return "Fruit.Banana";
              }

              @Override
              public String getClassName() {
                return "Banana";
              }

              @Override
              public PackageName getPackageName() {
                return new PackageName("Fruit");
              }
            });

    assertEquals("i0", i0.toString());
    assertEquals("i1", i1.toString());
    assertEquals("s0", s0.toString());
    assertEquals("r0", r0.toString());

    final Local di0 = localGenerator.generateLocal(PrimitiveType.BooleanType.getInstance());
    final Local di1 = localGenerator.generateLocal(PrimitiveType.BooleanType.getInstance());
    final Local ds0 = localGenerator.generateLocal(PrimitiveType.LongType.getInstance());

    assertEquals("$z0", di0.toString());
    assertEquals("$z1", di1.toString());
    assertEquals("$l0", ds0.toString());
  }

  public void testGenerateFieldCollisionHandling() {
    final LocalGenerator localGenerator = new LocalGenerator(new HashSet<>());
    final Local i0 = localGenerator.generateFieldLocal(PrimitiveType.IntType.getInstance());

    final LocalGenerator localGenerator2 = new LocalGenerator(localGenerator.getLocals());
    final Local i1 = localGenerator2.generateFieldLocal(PrimitiveType.IntType.getInstance());
    final Local di2 = localGenerator2.generateLocal(PrimitiveType.IntType.getInstance());

    assertEquals("i0", i0.toString());
    assertEquals("i1", i1.toString());
    assertEquals("$i2", di2.toString());
  }

  public void testGenerateLocalCollisionHandling() {
    final LocalGenerator localGenerator = new LocalGenerator(new HashSet<>());
    final Local di0 = localGenerator.generateLocal(PrimitiveType.IntType.getInstance());

    final LocalGenerator localGenerator2 = new LocalGenerator(localGenerator.getLocals());
    final Local di1 = localGenerator2.generateLocal(PrimitiveType.IntType.getInstance());
    final Local i2 = localGenerator2.generateFieldLocal(PrimitiveType.IntType.getInstance());

    assertEquals("$i0", di0.toString());
    assertEquals("$i1", di1.toString());
    assertEquals("i2", i2.toString());
    // "old" LocalGenerator
    assertEquals(
        "$i2", localGenerator.generateLocal(PrimitiveType.IntType.getInstance()).toString());
    assertEquals(
        "$i3", localGenerator.generateLocal(PrimitiveType.IntType.getInstance()).toString());
    // "new" LocalGenerator
    assertEquals(
        "$i4", localGenerator2.generateLocal(PrimitiveType.IntType.getInstance()).toString());
    assertEquals(
        "$i5", localGenerator2.generateLocal(PrimitiveType.IntType.getInstance()).toString());
    // "old" Localgenerator
    assertEquals(
        "i4", localGenerator.generateFieldLocal(PrimitiveType.IntType.getInstance()).toString());
    // ->no collision but not necessarily a continuous increment by 1 if you intertwine multiple
    // LocalGenerators
  }
}
