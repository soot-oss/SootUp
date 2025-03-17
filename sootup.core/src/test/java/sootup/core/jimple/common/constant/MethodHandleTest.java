package sootup.core.jimple.common.constant;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.constant.MethodHandle.Kind;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType.IntType;
import sootup.core.types.VoidType;

public class MethodHandleTest {

  @Test
  public void testMethodHandle() {
    assertEquals(Kind.REF_GET_FIELD.toString(), "REF_GET_FIELD");
    assertEquals(Kind.REF_GET_FIELD.getValueName(), "REF_GET_FIELD");
    assertEquals(Kind.REF_GET_FIELD.getValue(), 1);

    for (Kind currentKind : Kind.values()) {
      assertEquals(currentKind, Kind.getKind(currentKind.getValueName()));
      assertEquals(currentKind, Kind.getKind(currentKind.getValue()));
    }
    // not valid kinds
    assertThrows(RuntimeException.class, () -> Kind.getKind(0));
    assertThrows(RuntimeException.class, () -> Kind.getKind("invalid"));

    assertTrue(MethodHandle.isMethodRef(Kind.REF_INVOKE_VIRTUAL.getValue()));
    assertTrue(MethodHandle.isMethodRef(Kind.REF_INVOKE_STATIC.getValue()));
    assertTrue(MethodHandle.isMethodRef(Kind.REF_INVOKE_SPECIAL.getValue()));
    assertTrue(MethodHandle.isMethodRef(Kind.REF_INVOKE_CONSTRUCTOR.getValue()));
    assertTrue(MethodHandle.isMethodRef(Kind.REF_INVOKE_INTERFACE.getValue()));
    assertFalse(MethodHandle.isMethodRef(Kind.REF_GET_FIELD.getValue()));
    assertFalse(MethodHandle.isMethodRef(Kind.REF_PUT_FIELD.getValue()));
    assertFalse(MethodHandle.isMethodRef(Kind.REF_PUT_FIELD_STATIC.getValue()));
    assertFalse(MethodHandle.isMethodRef(Kind.REF_GET_FIELD_STATIC.getValue()));

    assertFalse(MethodHandle.isFieldRef(Kind.REF_INVOKE_VIRTUAL.getValue()));
    assertFalse(MethodHandle.isFieldRef(Kind.REF_INVOKE_STATIC.getValue()));
    assertFalse(MethodHandle.isFieldRef(Kind.REF_INVOKE_SPECIAL.getValue()));
    assertFalse(MethodHandle.isFieldRef(Kind.REF_INVOKE_CONSTRUCTOR.getValue()));
    assertFalse(MethodHandle.isFieldRef(Kind.REF_INVOKE_INTERFACE.getValue()));
    assertTrue(MethodHandle.isFieldRef(Kind.REF_GET_FIELD.getValue()));
    assertTrue(MethodHandle.isFieldRef(Kind.REF_PUT_FIELD.getValue()));
    assertTrue(MethodHandle.isFieldRef(Kind.REF_PUT_FIELD_STATIC.getValue()));
    assertTrue(MethodHandle.isFieldRef(Kind.REF_GET_FIELD_STATIC.getValue()));

    ClassType classType =
        new ClassType() {

          @Override
          public String getFullyQualifiedName() {
            return "test.A";
          }

          @Override
          public String getClassName() {
            return "A";
          }

          @Override
          public PackageName getPackageName() {
            return new PackageName("test");
          }
        };
    MethodSignature ms =
        new MethodSignature(classType, "m1", Collections.emptyList(), VoidType.getInstance());
    FieldSignature fs = new FieldSignature(classType, "f", IntType.getInstance());

    MethodHandle mhms = new MethodHandle(ms, Kind.REF_INVOKE_VIRTUAL.getValue(), classType);
    MethodHandle mhfs = new MethodHandle(fs, Kind.REF_GET_FIELD, classType);

    // not valid Method handles
    assertThrows(
        IllegalArgumentException.class,
        () -> new MethodHandle(fs, Kind.REF_INVOKE_CONSTRUCTOR, classType));
    assertThrows(
        IllegalArgumentException.class, () -> new MethodHandle(ms, Kind.REF_GET_FIELD, classType));

    assertTrue(mhms.isMethodRef());
    assertFalse(mhms.isFieldRef());

    assertFalse(mhfs.isMethodRef());
    assertTrue(mhfs.isFieldRef());

    assertEquals(mhfs.getType(), classType);
    assertEquals(
        mhfs.toString(),
        "methodhandle: \"" + mhfs.getKind() + "\" " + mhfs.getReferenceSignature());

    MethodHandle mhms2 = new MethodHandle(ms, Kind.REF_INVOKE_VIRTUAL.getValue(), classType);
    assertTrue(mhfs.equals(mhfs));
    assertFalse(mhfs.equals(mhms));
    assertFalse(mhfs.equals(null));
    assertFalse(mhfs.equals(classType));
    assertFalse(mhfs.equals(mhms2));

    assertEquals(mhfs.hashCode(), mhfs.hashCode());
    assertEquals(mhms.hashCode(), mhms2.hashCode());
    assertNotEquals(mhfs.hashCode(), mhms.hashCode());
  }
}
