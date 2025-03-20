package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.validation.ClassModifiersValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.*;
import sootup.java.core.views.JavaView;

public class ClassModifiersValidatorTest {
  static JavaView view;
  static ClassModifiersValidator classModifiersValidator;

  @BeforeAll
  public static void setUp() {
    view = new JavaView(Collections.singletonList(new EagerInputLocation()));
    classModifiersValidator = new ClassModifiersValidator();
  }

  public JavaSootClass testClassCreatorWithModifiers(EnumSet<ClassModifier> modifierEnumSet) {

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

    MutableStmtGraph stmtGraph = bodyBuilder.getStmtGraph();
    stmtGraph.setStartingStmt(firstStmt);
    stmtGraph.putEdge(firstStmt, returnVoidStmt);

    Body body =
        bodyBuilder.setMethodSignature(methodSignature).setLocals(generator.getLocals()).build();
    assertEquals(1, body.getLocalCount());

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
            new OverridingJavaClassSource(
                new EagerInputLocation(),
                null,
                view.getIdentifierFactory().getClassType("dummyMain"),
                null,
                Collections.emptySet(),
                null,
                Collections.emptySet(),
                Collections.singleton(dummyMainMethod),
                NoPositionInformation.getInstance(),
                modifierEnumSet,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()),
            SourceType.Application);
    assertEquals(mainClass.getMethods().size(), 1);

    return mainClass;
  }

  @Test
  public void testClassModifiersValidator_success() {
    List<ValidationException> validationExceptions_success = new ArrayList<>();

    JavaSootClass javaSootClass =
        testClassCreatorWithModifiers(
            EnumSet.of(ClassModifier.ANNOTATION, ClassModifier.INTERFACE, ClassModifier.ABSTRACT));

    classModifiersValidator.validate(javaSootClass, validationExceptions_success);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testClassModifiersValidator_fail1() {
    List<ValidationException> validationExceptions_fail1 = new ArrayList<>();

    JavaSootClass javaSootClass =
        testClassCreatorWithModifiers(
            EnumSet.of(
                ClassModifier.INTERFACE,
                ClassModifier.ENUM,
                ClassModifier.FINAL,
                ClassModifier.SUPER));

    classModifiersValidator.validate(javaSootClass, validationExceptions_fail1);

    assertEquals(4, validationExceptions_fail1.size());
  }

  @Test
  public void testClassModifiersValidator_fail2() {
    List<ValidationException> validationExceptions_fail2 = new ArrayList<>();

    JavaSootClass javaSootClass =
        testClassCreatorWithModifiers(EnumSet.of(ClassModifier.ANNOTATION));

    classModifiersValidator.validate(javaSootClass, validationExceptions_fail2);

    assertEquals(1, validationExceptions_fail2.size());
  }
}
