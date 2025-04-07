package sootup.core.util.printer;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.base.Objects;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;

public class AbstractStmtJimplePrinterTest {

  @Test
  public void addImportTest() {

    PackageName abc = new PackageName("a.b.c");
    PackageName def = new PackageName("d.e.f");
    PackageName anotherAbc = new PackageName("a.b.c");

    ClassType classOneFromAbc = generateClass("ClassOne", abc);
    ClassType classOneFromDef = generateClass("ClassOne", def);
    ClassType anotherRefToClassOneFromAbc = generateClass("ClassOne", abc);
    ClassType classTwoFromAbc = generateClass("ClassTwo", abc);

    MethodSignature ms =
        new MethodSignature(
            classOneFromAbc,
            new MethodSubSignature("banana", Collections.emptyList(), VoidType.getInstance()));
    final Body body =
        Body.builder().setModifiers(Collections.emptySet()).setMethodSignature(ms).build();
    NormalStmtPrinter p = new NormalStmtPrinter();
    p.enableImports(true);

    // basic sanity checks
    assertEquals(classOneFromAbc.hashCode(), anotherRefToClassOneFromAbc.hashCode());
    assertNotEquals(classOneFromAbc.hashCode(), classOneFromDef.hashCode());

    assertEquals(classOneFromAbc, anotherRefToClassOneFromAbc);
    assertNotEquals(classOneFromAbc, classOneFromDef);

    assertTrue(p.addImport(classOneFromAbc)); // check non colliding with empty imports
    assertTrue(p.addImport(classOneFromAbc)); // test subsequent call is fine too
    assertTrue(p.addImport(anotherRefToClassOneFromAbc));

    assertFalse(p.addImport(classOneFromDef)); // check collision
    assertTrue(p.addImport(classTwoFromAbc));
  }

  private ClassType generateClass(String name, PackageName pckg) {
    return new ClassType() {

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
        return getPackageName().toString() + "." + getClassName();
      }

      @Override
      public String getClassName() {
        return name;
      }

      @Override
      public PackageName getPackageName() {
        return pckg;
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

      @Override
      public int hashCode() {
        return Objects.hashCode(getPackageName(), getClassName());
      }

      @Override
      public boolean equals(Object o) {
        ClassType that = (ClassType) o;
        return Objects.equal(getPackageName(), that.getPackageName())
            && Objects.equal(getClassName(), that.getClassName());
      }
    };
  }
}
