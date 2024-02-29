package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.validation.MethodValidator;
import sootup.core.validation.ValidationException;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleView;

public class MethodValidatorTest {
  MethodValidator methodValidator;
  JimpleView jimpleView;

  @BeforeEach
  public void Setup() {

    methodValidator = new MethodValidator();

    ClassType classTypeCheckInitValidator =
        new ClassType() {
          @Override
          public boolean isBuiltInClass() {
            return false;
          }

          @Override
          public String getFullyQualifiedName() {
            return "jimple.MethodValidator";
          }

          @Override
          public String getClassName() {
            return "MethodValidator";
          }

          @Override
          public PackageName getPackageName() {
            return new PackageName("jimple");
          }
        };

    String classPath = "src/test/resources/validator/jimple";
    JimpleAnalysisInputLocation jimpleInputLocation =
        new JimpleAnalysisInputLocation(Paths.get(classPath), SourceType.Application);

    jimpleView = new JimpleView(jimpleInputLocation);
    final Optional<SootClass> classSource1 = jimpleView.getClass(classTypeCheckInitValidator);
    assertFalse(classSource1.isPresent());
  }

  @Test
  public void testCheckInitValidatorSuccess() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        methodValidator.validate(getBody("<MethodValidator: void <init>()>"), jimpleView);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testCheckInitValidatorFailure() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        methodValidator.validate(getBody("<MethodValidator: void <clinit>()>"), jimpleView);

    assertEquals(1, validationExceptions_success.size());
  }

  Body getBody(String methodSignature) {
    Optional<? extends SootMethod> optMethod =
        jimpleView.getMethod(
            jimpleView.getIdentifierFactory().parseMethodSignature(methodSignature));
    assertTrue(optMethod.isPresent());
    return optMethod.get().getBody();
  }
}
