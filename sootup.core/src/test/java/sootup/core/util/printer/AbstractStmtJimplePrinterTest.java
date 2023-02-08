package sootup.core.util.printer;

import static org.junit.Assert.*;

import categories.Java8Test;
import com.google.common.base.Objects;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;

@Category(Java8Test.class)
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
      public boolean isBuiltInClass() {
        return false;
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
