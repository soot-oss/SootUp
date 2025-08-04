package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;
import sootup.core.validation.IdentityStmtsValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import sootup.jimple.frontend.JimpleAnalysisInputLocation;

public class IdentityStmtsValidatorTest {

  IdentityStmtsValidator identityStmtsValidator;
  JavaView jimpleView;

  @BeforeEach
  public void Setup() {

    identityStmtsValidator = new IdentityStmtsValidator();

    ClassType classTypeFieldRefValidator =
        new ClassType() {
          @Override
          public String getFullyQualifiedName() {
            return "IdentityStmtsValidator";
          }

          @Override
          public String getClassName() {
            return "IdentityStmtsValidator";
          }

          @Override
          public PackageName getPackageName() {
            return new PackageName("");
          }
        };

    String classPath = "src/test/resources/validator/jimple";
    JimpleAnalysisInputLocation jimpleInputLocation =
        new JimpleAnalysisInputLocation(Paths.get(classPath), SourceType.Application);

    jimpleView = new JavaView(jimpleInputLocation);
    final Optional<JavaSootClass> scOpt = jimpleView.getClass(classTypeFieldRefValidator);
    assertTrue(scOpt.isPresent());
  }

  @Test
  public void testThisRefSuccess() {
    List<ValidationException> validationExceptions_success =
        identityStmtsValidator.validate(
            getBody("<IdentityStmtsValidator: void <init>()>"), jimpleView);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testParameterRefSuccess() {
    List<ValidationException> validationExceptions_success =
        identityStmtsValidator.validate(
            getBody("<IdentityStmtsValidator: void testParameterRefSuccess(int)>"), jimpleView);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testNoThisrRef() {
    List<ValidationException> validationExceptions_success =
        identityStmtsValidator.validate(
            getBody("<IdentityStmtsValidator: void testNoThisrRef(int)>"), jimpleView);

    assertEquals(1, validationExceptions_success.size());
  }

  @Test
  public void testParameterRefMultiLocals() {
    List<ValidationException> validationExceptions_success =
        identityStmtsValidator.validate(
            getBody("<IdentityStmtsValidator: void testParameterRefMultiLocals(int)>"), jimpleView);

    assertEquals(1, validationExceptions_success.size());
  }

  @Test
  public void testParameterRefNoLocal() {
    List<ValidationException> validationExceptions_success =
        identityStmtsValidator.validate(
            getBody("<IdentityStmtsValidator: void testParameterRefNoLocal(int)>"), jimpleView);

    assertEquals(1, validationExceptions_success.size());
  }

  /*
     Unable to test failure cases for out-of-index parameterRef (including the special case where no parameterRef is allowed).
     Attempting to access parameterRef out of bounds may result in Jimple load failure.

     Example:
            public void testNoParameterRefAllowed()
             {
                 LocalsValidator l0;
                 unknown l1;

                 l0 := @this: IdentityStmtsValidator;
                 l1 = @parameter0: int;

                 return;
             }
  */

  Body getBody(String methodSignature) {
    Optional<JavaSootMethod> optionalSootMethod =
        jimpleView.getMethod(
            jimpleView.getIdentifierFactory().parseMethodSignature(methodSignature));
    assertTrue(optionalSootMethod.isPresent());
    return optionalSootMethod.get().getBody();
  }
}
