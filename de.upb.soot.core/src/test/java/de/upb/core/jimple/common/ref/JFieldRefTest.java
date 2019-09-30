package de.upb.core.jimple.common.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.core.DefaultIdentifierFactory;
import de.upb.soot.core.IdentifierFactory;
import de.upb.soot.core.Project;
import de.upb.soot.core.frontend.EagerJavaClassSource;
import de.upb.soot.core.inputlocation.EagerInputLocation;
import de.upb.soot.core.jimple.Jimple;
import de.upb.soot.core.jimple.basic.Local;
import de.upb.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.soot.core.jimple.common.ref.JStaticFieldRef;
import de.upb.soot.core.model.Modifier;
import de.upb.soot.core.model.SootClass;
import de.upb.soot.core.model.SootField;
import de.upb.soot.core.model.SourceType;
import de.upb.soot.core.signatures.FieldSignature;
import de.upb.soot.core.types.JavaClassType;
import de.upb.soot.core.views.JavaView;
import de.upb.soot.core.views.View;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class JFieldRefTest {

  @Ignore
  public void testJStaticFieldRef() {
    View view = new JavaView<>(new Project<>(null, DefaultIdentifierFactory.getInstance()));
    IdentifierFactory fact = view.getIdentifierFactory();
    JavaClassType declaringClassSignature =
        DefaultIdentifierFactory.getInstance().getClassType("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field = new SootField(fieldSig, EnumSet.of(Modifier.FINAL));

    SootClass mainClass =
        new SootClass(
            new EagerJavaClassSource(
                new EagerInputLocation(),
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
    assertTrue(ref.getField(view).isPresent());
    assertEquals(field, ref.getField(view).get());
    assertEquals(EnumSet.of(Modifier.FINAL), ref.getField(view).get().getModifiers());
  }

  @Ignore
  public void testJInstanceFieldRef() {
    View view = new JavaView<>(new Project<>(null, DefaultIdentifierFactory.getInstance()));
    IdentifierFactory fact = view.getIdentifierFactory();
    JavaClassType declaringClassSignature =
        DefaultIdentifierFactory.getInstance().getClassType("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field = new SootField(fieldSig, EnumSet.of(Modifier.FINAL));

    SootClass mainClass =
        new SootClass(
            new EagerJavaClassSource(
                new EagerInputLocation(),
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
    assertTrue(ref.getField(view).isPresent());
    assertEquals(fieldSig, ref.getField(view).get().getSignature());
    assertEquals(EnumSet.of(Modifier.FINAL), ref.getField(view).get().getModifiers());
  }
}
