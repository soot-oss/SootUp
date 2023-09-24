package sootup.java.core.jimple.common.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import sootup.core.IdentifierFactory;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootField;
import sootup.core.model.SourceType;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class JFieldRefTest {

  JavaView view;

  @Before
  public void setUp() {
    JavaProject project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(new EagerInputLocation()).build();
    view = project.createView();
  }

  @Ignore
  public void testJStaticFieldRef() {
    IdentifierFactory fact = view.getIdentifierFactory();
    ClassType declaringClassSignature =
        JavaIdentifierFactory.getInstance().getClassType("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field =
        new SootField(
            fieldSig, EnumSet.of(FieldModifier.FINAL), NoPositionInformation.getInstance());

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
                EnumSet.of(ClassModifier.PUBLIC),
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
    assertEquals(EnumSet.of(FieldModifier.FINAL), field1.get().getModifiers());
  }

  @Ignore
  public void testJInstanceFieldRef() {
    IdentifierFactory fact = view.getIdentifierFactory();
    ClassType declaringClassSignature =
        JavaIdentifierFactory.getInstance().getClassType("dummyMainClass");
    FieldSignature fieldSig = fact.getFieldSignature("dummyField", declaringClassSignature, "int");
    SootField field =
        new SootField(
            fieldSig, EnumSet.of(FieldModifier.FINAL), NoPositionInformation.getInstance());

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
                EnumSet.of(ClassModifier.PUBLIC),
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
    assertEquals(EnumSet.of(FieldModifier.FINAL), field1.get().getModifiers());
  }
}
