package de.upb.swt.soot.core.util.printer;

import static org.junit.Assert.*;

import com.google.common.base.Objects;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import org.junit.Test;

public class AbstractStmtPrinterTest {

  @Test
  public void addImportTest() {
    NormalStmtPrinter p = new NormalStmtPrinter();
    p.enableImports(true);

    PackageName abc = new PackageName("a.b.c");
    PackageName def = new PackageName("d.e.f");
    PackageName anotherAbc = new PackageName("a.b.c");

    ClassType classOneFromAbc =
        new ClassType() {
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
            return "ClassOne";
          }

          @Override
          public PackageName getPackageName() {
            return abc;
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

    ClassType classOneFromDef =
        new ClassType() {
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
            return "ClassOne";
          }

          @Override
          public PackageName getPackageName() {
            return def;
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

    ClassType anotherRefToClassOneFromAbc =
        new ClassType() {
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
            return "ClassOne";
          }

          @Override
          public PackageName getPackageName() {
            return anotherAbc;
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

    ClassType classTwoFromAbc =
        new ClassType() {
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
            return "ClassTwo";
          }

          @Override
          public PackageName getPackageName() {
            return abc;
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
}
