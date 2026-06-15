package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.validation.FieldModifiersValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.*;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class FieldModifiersValidatorTest {
  JavaView view;
  FieldModifiersValidator fieldModifiersValidator;

  @BeforeEach
  public void setUp() {
    view = new JavaView(Collections.singletonList(new EagerInputLocation()));
    fieldModifiersValidator = new FieldModifiersValidator();
  }

  public JavaSootClass testClassCreatorWithModifiers(
      EnumSet<ClassModifier> classModifierEnumSet, EnumSet<FieldModifier> fieldModifierEnumSet) {

    JavaView view = new JavaView(Collections.singletonList(new EagerInputLocation()));
    ClassType type = view.getIdentifierFactory().getClassType("java.lang.String");

    LocalGenerator generator = new LocalGenerator(new HashSet<>());
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("dummyMain", "main", "void", Collections.emptyList());
    Body.BodyBuilder bodyBuilder = Body.builder();

    final JIdentityStmt firstStmt =
        Jimple.newIdentityStmt(
            generator.generateLocal(type),
            Jimple.newParameterRef(type, 0),
            StmtPositionInfo.getNoStmtPositionInfo());
    final JReturnVoidStmt returnVoidStmt =
        new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    MutableControlFlowGraph controlFlowGraph = bodyBuilder.getControlFlowGraph();
    controlFlowGraph.setStartingStmt(firstStmt);
    controlFlowGraph.putEdge(firstStmt, returnVoidStmt);

    Body body =
        bodyBuilder.setMethodSignature(methodSignature).setLocals(generator.getLocals()).build();
    assertEquals(1, body.getLocalCount());

    FieldSignature fieldSignature =
        new FieldSignature(
            new JavaClassType("FieldModifiersValidator", PackageName.DEFAULT_PACKAGE),
            "i",
            PrimitiveType.IntType.getInstance());
    JavaSootField dummyField =
        JavaSootField.JavaSootFieldBuilder.builder()
            .withSignature(fieldSignature)
            .withModifier(fieldModifierEnumSet)
            .withAnnotation(Collections.emptyList())
            .withPosition(NoPositionInformation.getInstance())
            .build();

    JavaSootMethod dummyMainMethod =
        new JavaSootMethod(
            new OverridingBodySource(methodSignature, body),
            methodSignature,
            EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            Collections.emptyList(),
            Collections.emptyList(),
            NoPositionInformation.getInstance());

    JavaSootClass mainClass =
        new JavaSootClass(
            new InMemoryOverridingJavaClassSource(
                new EagerInputLocation(),
                null,
                view.getIdentifierFactory().getClassType("dummyMain"),
                null,
                Collections.emptySet(),
                null,
                Collections.singleton(dummyField),
                Collections.singleton(dummyMainMethod),
                NoPositionInformation.getInstance(),
                classModifierEnumSet,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()),
            SourceType.Application);
    assertEquals(mainClass.getMethods().size(), 1);

    return mainClass;
  }

  @Test
  public void testFieldModifiersValidator_success() {
    List<ValidationException> validationExceptions_success = new ArrayList<>();

    JavaSootClass javaSootClass =
        testClassCreatorWithModifiers(
            EnumSet.of(ClassModifier.INTERFACE),
            EnumSet.of(FieldModifier.PUBLIC, FieldModifier.STATIC, FieldModifier.FINAL));

    fieldModifiersValidator.validate(javaSootClass, validationExceptions_success);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testFieldModifiersValidator_fail1() {
    List<ValidationException> validationExceptions_fail1 = new ArrayList<>();

    JavaSootClass javaSootClass =
        testClassCreatorWithModifiers(
            EnumSet.of(ClassModifier.PUBLIC),
            EnumSet.of(FieldModifier.PUBLIC, FieldModifier.PRIVATE));

    fieldModifiersValidator.validate(javaSootClass, validationExceptions_fail1);

    assertEquals(1, validationExceptions_fail1.size());
  }

  @Test
  public void testFieldModifiersValidator_fail2() {
    List<ValidationException> validationExceptions_fail1 = new ArrayList<>();

    JavaSootClass javaSootClass =
        testClassCreatorWithModifiers(
            EnumSet.of(ClassModifier.INTERFACE), EnumSet.of(FieldModifier.PRIVATE));

    fieldModifiersValidator.validate(javaSootClass, validationExceptions_fail1);

    assertEquals(3, validationExceptions_fail1.size());
  }
}
