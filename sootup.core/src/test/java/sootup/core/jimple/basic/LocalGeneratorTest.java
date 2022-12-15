package sootup.core.jimple.basic;

import java.util.HashSet;
import junit.framework.TestCase;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;

public class LocalGeneratorTest extends TestCase {

  public void testGenerate() {
    final LocalGenerator localGenerator = new LocalGenerator(new HashSet<>());
    final Local i0 = localGenerator.generateField(PrimitiveType.IntType.getInstance());
    final Local i1 = localGenerator.generateField(PrimitiveType.IntType.getInstance());
    final Local s0 = localGenerator.generateField(PrimitiveType.ShortType.getInstance());
    final Local r0 =
        localGenerator.generateField(
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
}
