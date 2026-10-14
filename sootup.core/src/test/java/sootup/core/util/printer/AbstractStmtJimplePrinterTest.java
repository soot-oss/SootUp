package sootup.core.util.printer;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.base.Objects;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.signatures.SignatureInterner;
import sootup.core.types.*;

public class AbstractStmtJimplePrinterTest {

  @Test
  public void addImportTest() {

    PackageName abc = SignatureInterner.getPackageName("a.b.c");
    PackageName def = SignatureInterner.getPackageName("d.e.f");
    PackageName anotherAbc = SignatureInterner.getPackageName("a.b.c");

    ClassType classOneFromAbc = generateClass("ClassOne", abc);
    ClassType classOneFromDef = generateClass("ClassOne", def);
    ClassType anotherRefToClassOneFromAbc = generateClass("ClassOne", abc);
    ClassType classTwoFromAbc = generateClass("ClassTwo", abc);

    MethodSignature ms =
        SignatureInterner.getMethodSignature(
            classOneFromAbc,
            SignatureInterner.getMethodSubSignature(
                "banana", VoidType.getInstance(), Collections.emptyList()));
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
