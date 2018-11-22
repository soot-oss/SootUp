package de.upb.soot.jimple.common.ref;

import static org.junit.Assert.assertEquals;

import de.upb.soot.core.ClassType;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.namespaces.JavaSourcePathNamespace;
import de.upb.soot.namespaces.classprovider.java.JavaClassSource;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 *
 * @author Linghui Luo
 *
 */
@Category(Java8Test.class)
public class JFieldRefTest {

  @Test
  public void testJStaticFieldRef() {
    IView view = new JavaView(null);
    SignatureFactory fact = view.getSignatureFactory();
    JavaClassSignature declaringClassSignature = fact.getClassSignature("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field
        = new SootField(view, declaringClassSignature, fieldSig, fact.getTypeSignature("int"), EnumSet.of(Modifier.FINAL));

    SootClass mainClass = new SootClass(view, ResolvingLevel.BODIES,
        new JavaClassSource(new JavaSourcePathNamespace(""), null, declaringClassSignature), ClassType.Application,
        Optional.empty(), Collections.emptySet(), Optional.empty(), Collections.singleton(field), Collections.emptySet(),
        null,
        EnumSet.of(Modifier.PUBLIC));
    JStaticFieldRef ref = Jimple.newStaticFieldRef(view, fieldSig);
    assertEquals("<dummyMainClass: int dummyField>", ref.toString());
    assertEquals(true, ref.getField().isPresent());
    assertEquals(field, ref.getField().get());
    assertEquals(EnumSet.of(Modifier.FINAL), ref.getField().get().getModifiers());
  }

  @Test
  public void testJInstanceFieldRef() {
    IView view = new JavaView(null);
    SignatureFactory fact = view.getSignatureFactory();
    JavaClassSignature declaringClassSignature = fact.getClassSignature("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field
        = new SootField(view, declaringClassSignature, fieldSig, fact.getTypeSignature("int"), EnumSet.of(Modifier.FINAL));
    SootClass mainClass = new SootClass(view, ResolvingLevel.BODIES,
        new JavaClassSource(new JavaSourcePathNamespace(""), null, declaringClassSignature), ClassType.Application,
        Optional.empty(), Collections.emptySet(), Optional.empty(), Collections.singleton(field), Collections.emptySet(),
        null, EnumSet.of(Modifier.PUBLIC));
    Local base = new Local("obj", RefType.getInstance(mainClass));
    JInstanceFieldRef ref = Jimple.newInstanceFieldRef(view, base, fieldSig);
    assertEquals("obj.<dummyMainClass: int dummyField>", ref.toString());
    assertEquals(true, ref.getField().isPresent());
    assertEquals(field, ref.getField().get());
    assertEquals(EnumSet.of(Modifier.FINAL), ref.getField().get().getModifiers());
  }
}
