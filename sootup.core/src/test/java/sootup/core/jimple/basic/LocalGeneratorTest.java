package sootup.core.jimple.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;

public class LocalGeneratorTest {

  @Test
  public void testGenerate() {
    final LocalGenerator localGenerator = new LocalGenerator(new HashSet<>());
    final Local i0 = localGenerator.generateLocal(PrimitiveType.IntType.getInstance());
    final Local i1 = localGenerator.generateLocal(PrimitiveType.IntType.getInstance());
    final Local s0 = localGenerator.generateLocal(PrimitiveType.ShortType.getInstance());
    final Local r0 =
        localGenerator.generateLocal(
            new ClassType() {

              @Override
              protected boolean isPrimitiveType() {
                return false;
              }

              @Override
              protected boolean isReferenceType() {
                return false;
              }

              @Override
              protected boolean isBottomType() {
                return false;
              }

              @Override
              protected boolean isTopType() {
                return false;
              }

              @Override
              protected boolean isUnknownType() {
                return false;
              }

              @Override
              protected boolean isVoidType() {
                return false;
              }

              @Override
              protected PrimitiveType asPrimitiveType() {
                return null;
              }

              @Override
              protected ReferenceType asReferenceType() {
                return null;
              }

              @Override
              protected Type asBottomType() {
                return null;
              }

              @Override
              protected Type asTopType() {
                return null;
              }

              @Override
              protected UnknownType asUnknownType() {
                return null;
              }

              @Override
              protected VoidType asVoidType() {
                return null;
              }

              @Override
              protected Optional<PrimitiveType> toPrimitiveType() {
                return Optional.empty();
              }

              @Override
              protected Optional<ReferenceType> toReferenceType() {
                return Optional.empty();
              }

              @Override
              protected Optional<Type> toBottomType() {
                return Optional.empty();
              }

              @Override
              protected Optional<Type> toTopType() {
                return Optional.empty();
              }

              @Override
              protected Optional<UnknownType> toUnknownType() {
                return Optional.empty();
              }

              @Override
              protected Optional<VoidType> toVoidType() {
                return Optional.empty();
              }

              @Override
              protected boolean isClassType() {
                return false;
              }

              @Override
              protected boolean isArrayType() {
                return false;
              }

              @Override
              protected boolean isNullType() {
                return false;
              }

              @Override
              protected ClassType asClassType() {
                return null;
              }

              @Override
              protected ArrayType asArrayType() {
                return null;
              }

              @Override
              protected NullType asNullType() {
                return null;
              }

              @Override
              protected Optional<ClassType> toClassType() {
                return Optional.empty();
              }

              @Override
              protected Optional<ArrayType> toArrayType() {
                return Optional.empty();
              }

              @Override
              protected Optional<NullType> toNullType() {
                return Optional.empty();
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

              @Override
              protected boolean isJavaClassType() {
                return false;
              }

              @Override
              protected boolean isModuleJavaClassType() {
                return false;
              }

              @Override
              protected boolean isWeakObjectType() {
                return false;
              }

              @Override
              protected Type asJavaClassType() {
                return null;
              }

              @Override
              protected Type asModuleJavaClassType() {
                return null;
              }

              @Override
              protected Type asWeakObjectType() {
                return null;
              }

              @Override
              protected Optional<Type> toJavaClassType() {
                return Optional.empty();
              }

              @Override
              protected Optional<Type> toModuleJavaClassType() {
                return Optional.empty();
              }

              @Override
              protected Optional<Type> toWeakObjectType() {
                return Optional.empty();
              }
            });

    assertEquals("i0", i0.toString());
    assertEquals("i1", i1.toString());
    assertEquals("s0", s0.toString());
    assertEquals("r0", r0.toString());

    final Local di0 = localGenerator.generateLocal(PrimitiveType.BooleanType.getInstance());
    final Local di1 = localGenerator.generateLocal(PrimitiveType.BooleanType.getInstance());
    final Local ds0 = localGenerator.generateLocal(PrimitiveType.LongType.getInstance());

    assertEquals("z0", di0.toString());
    assertEquals("z1", di1.toString());
    assertEquals("l0", ds0.toString());
  }

  @Disabled
  public void testGenerateLocalCollisionHandling() {
    final LocalGenerator localGenerator = new LocalGenerator(new HashSet<>());
    final Local di0 = localGenerator.generateLocal(PrimitiveType.IntType.getInstance());

    final LocalGenerator localGenerator2 = new LocalGenerator(localGenerator.getLocals());
    final Local di1 = localGenerator2.generateLocal(PrimitiveType.IntType.getInstance());
    final Local i2 = localGenerator2.generateLocal(PrimitiveType.IntType.getInstance());

    assertEquals("i0", di0.toString());
    assertEquals("i1", di1.toString());
    assertEquals("i2", i2.toString());
    // "old" LocalGenerator
    assertEquals(
        "i3", localGenerator.generateLocal(PrimitiveType.IntType.getInstance()).toString());
    assertEquals(
        "i4", localGenerator.generateLocal(PrimitiveType.IntType.getInstance()).toString());

    // "new" LocalGenerator
    assertEquals(
        "i5", localGenerator2.generateLocal(PrimitiveType.IntType.getInstance()).toString());
    assertEquals(
        "i6", localGenerator2.generateLocal(PrimitiveType.IntType.getInstance()).toString());
    // "old" Localgenerator
    assertEquals(
        "i7", localGenerator.generateLocal(PrimitiveType.IntType.getInstance()).toString());
    // ->no collision but not necessarily a continuous increment by 1 if you intertwine multiple
    // LocalGenerators
  }
}
