package de.upb.swt.soot.test.core.jimple.common.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.EagerJavaClassSource;
import de.upb.swt.soot.core.inputlocation.EagerInputLocation;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JStaticFieldRef;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.DefaultIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaJimple;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class JFieldRefTest {

  View view;

  @Before
  public void setUp() {
    Project project = JavaProject.builder().addClassPath(new EagerInputLocation()).make();
    view = project.createOnDemandView();
  }

  @Ignore
  public void testJStaticFieldRef() {
    IdentifierFactory fact = view.getIdentifierFactory();
    ClassType declaringClassSignature =
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
    JStaticFieldRef ref = JavaJimple.getInstance().newStaticFieldRef(fieldSig);
    assertEquals("<dummyMainClass: int dummyField>", ref.toString());

    // FIXME: [JMP] This assert always fails, because the view does not contain any class.
    assertTrue(ref.getField(view).isPresent());
    assertEquals(field, ref.getField(view).get());
    assertEquals(EnumSet.of(Modifier.FINAL), ref.getField(view).get().getModifiers());
  }

  @Ignore
  public void testJInstanceFieldRef() {
    IdentifierFactory fact = view.getIdentifierFactory();
    ClassType declaringClassSignature =
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
    JInstanceFieldRef ref = JavaJimple.getInstance().newInstanceFieldRef(base, fieldSig);
    assertEquals("obj.<dummyMainClass: int dummyField>", ref.toString());

    // FIXME: [JMP] This assert always fails, because the view does not contain any class.
    assertTrue(ref.getField(view).isPresent());
    assertEquals(fieldSig, ref.getField(view).get().getSignature());
    assertEquals(EnumSet.of(Modifier.FINAL), ref.getField(view).get().getModifiers());
  }
}
