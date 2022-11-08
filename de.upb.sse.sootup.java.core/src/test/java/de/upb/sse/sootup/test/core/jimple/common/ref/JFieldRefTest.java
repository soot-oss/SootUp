package de.upb.sse.sootup.test.core.jimple.common.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.sse.sootup.core.IdentifierFactory;
import de.upb.sse.sootup.core.inputlocation.EagerInputLocation;
import de.upb.sse.sootup.core.jimple.Jimple;
import de.upb.sse.sootup.core.jimple.basic.Local;
import de.upb.sse.sootup.core.jimple.basic.NoPositionInformation;
import de.upb.sse.sootup.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.sse.sootup.core.jimple.common.ref.JStaticFieldRef;
import de.upb.sse.sootup.core.model.Modifier;
import de.upb.sse.sootup.core.model.SootField;
import de.upb.sse.sootup.core.model.SourceType;
import de.upb.sse.sootup.core.signatures.FieldSignature;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.java.core.*;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.OverridingJavaClassSource;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.views.JavaView;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class JFieldRefTest {

  JavaView view;

  @Before
  public void setUp() {
    JavaProject project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(new EagerInputLocation()).build();
    view = project.createOnDemandView();
  }

  @Ignore
  public void testJStaticFieldRef() {
    IdentifierFactory fact = view.getIdentifierFactory();
    ClassType declaringClassSignature =
        JavaIdentifierFactory.getInstance().getClassType("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field =
        new SootField(fieldSig, EnumSet.of(Modifier.FINAL), NoPositionInformation.getInstance());

    JavaSootClass mainClass =
        new JavaSootClass(
            new OverridingJavaClassSource(
                new EagerInputLocation(),
                null,
                declaringClassSignature,
                null,
                Collections.emptySet(),
                null,
                Collections.singleton(field),
                Collections.emptySet(),
                null,
                EnumSet.of(Modifier.PUBLIC),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()),
            SourceType.Application);
    JStaticFieldRef ref = Jimple.newStaticFieldRef(fieldSig);
    assertEquals("<dummyMainClass: int dummyField>", ref.toString());

    // FIXME: [JMP] This assert always fails, because the view does not contain any class.
    final Optional<? extends SootField> field1 = view.getField(ref.getFieldSignature());
    assertTrue(field1.isPresent());
    assertEquals(field, field1.get());
    assertEquals(EnumSet.of(Modifier.FINAL), field1.get().getModifiers());
  }

  @Ignore
  public void testJInstanceFieldRef() {
    IdentifierFactory fact = view.getIdentifierFactory();
    ClassType declaringClassSignature =
        JavaIdentifierFactory.getInstance().getClassType("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field =
        new SootField(fieldSig, EnumSet.of(Modifier.FINAL), NoPositionInformation.getInstance());

    JavaSootClass mainClass =
        new JavaSootClass(
            new OverridingJavaClassSource(
                new EagerInputLocation(),
                null,
                declaringClassSignature,
                null,
                Collections.emptySet(),
                null,
                Collections.singleton(field),
                Collections.emptySet(),
                null,
                EnumSet.of(Modifier.PUBLIC),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()),
            SourceType.Application);
    Local base = new Local("obj", declaringClassSignature);
    JInstanceFieldRef ref = Jimple.newInstanceFieldRef(base, fieldSig);
    assertEquals("obj.<dummyMainClass: int dummyField>", ref.toString());

    // FIXME: [JMP] This assert always fails, because the view does not contain any class.
    final Optional<? extends SootField> field1 = view.getField(ref.getFieldSignature());
    assertTrue(field1.isPresent());
    assertEquals(fieldSig, field1.get().getSignature());
    assertEquals(EnumSet.of(Modifier.FINAL), field1.get().getModifiers());
  }
}
