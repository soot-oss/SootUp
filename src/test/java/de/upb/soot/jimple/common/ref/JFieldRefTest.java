package de.upb.soot.jimple.common.ref;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.IdentifierFactory;
import de.upb.soot.Project;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SourceType;
import de.upb.soot.frontends.java.EagerJavaClassSource;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.namespaces.JavaSourcePathAnalysisInputLocation;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class JFieldRefTest {

  @Test
  public void testJStaticFieldRef() {
    IView view = new JavaView(new Project(null, DefaultIdentifierFactory.getInstance()));
    IdentifierFactory fact = view.getIdentifierFactory();
    JavaClassType declaringClassSignature =
        DefaultIdentifierFactory.getInstance().getClassType("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field = new SootField(fieldSig, EnumSet.of(Modifier.FINAL));

    // FIXME: [JMP] This instance is never used.
    SootClass mainClass =
        new SootClass(
            new EagerJavaClassSource(
                new JavaSourcePathAnalysisInputLocation(Collections.emptySet()),
                null,
                declaringClassSignature,
                null,
                Collections.emptySet(),
                null,
                Collections.singleton(field),
                Collections.emptySet(),
                null,
                EnumSet.of(Modifier.PUBLIC)),
            SourceType.Application);
    JStaticFieldRef ref = Jimple.newStaticFieldRef(fieldSig);
    assertEquals("<dummyMainClass: int dummyField>", ref.toString());

    // FIXME: [JMP] This assert always fails, because the view does not contain any class.
    // assertTrue(ref.getField(view).isPresent());
    // assertEquals(field, ref.getField(view).get());
    // assertEquals(EnumSet.of(Modifier.FINAL), ref.getField(view).get().getModifiers());
  }

  @Test
  public void testJInstanceFieldRef() {
    IView view = new JavaView(new Project(null, DefaultIdentifierFactory.getInstance()));
    IdentifierFactory fact = view.getIdentifierFactory();
    JavaClassType declaringClassSignature =
        DefaultIdentifierFactory.getInstance().getClassType("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field = new SootField(fieldSig, EnumSet.of(Modifier.FINAL));

    // FIXME: [JMP] This instance is never used.
    SootClass mainClass =
        new SootClass(
            new EagerJavaClassSource(
                new JavaSourcePathAnalysisInputLocation(Collections.emptySet()),
                null,
                declaringClassSignature,
                null,
                Collections.emptySet(),
                null,
                Collections.singleton(field),
                Collections.emptySet(),
                null,
                EnumSet.of(Modifier.PUBLIC)),
            SourceType.Application);
    Local base = new Local("obj", declaringClassSignature);
    JInstanceFieldRef ref = Jimple.newInstanceFieldRef(base, fieldSig);
    assertEquals("obj.<dummyMainClass: int dummyField>", ref.toString());

    // FIXME: [JMP] This assert always fails, because the view does not contain any class.
    // assertTrue(ref.getField(view).isPresent());
    // assertEquals(fieldSig, ref.getField(view).get().getSignature());
    // assertEquals(EnumSet.of(Modifier.FINAL), ref.getField(view).get().getModifiers());
  }
}
