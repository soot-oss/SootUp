package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;
import sootup.core.validation.JimpleTrapValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;
import sootup.jimple.frontend.JimpleAnalysisInputLocation;

public class JimpleTrapValidatorTest {

  JimpleTrapValidator jimpleTrapValidator;
  JavaView jimpleView;

  @BeforeEach
  public void Setup() {

    jimpleTrapValidator = new JimpleTrapValidator();

    ClassType classTypeJimpleTrapValidator =
        new ClassType() {
          @Override
          public String getFullyQualifiedName() {
            return "jimple.TrapsValidator";
          }

          @Override
          public String getClassName() {
            return "TrapsValidator";
          }

          @Override
          public PackageName getPackageName() {
            return new PackageName("jimple");
          }
        };

    String classPath = "src/test/resources/validator/jimple";
    JimpleAnalysisInputLocation jimpleInputLocation =
        new JimpleAnalysisInputLocation(Paths.get(classPath), SourceType.Application);

    jimpleView = new JavaView(jimpleInputLocation);
    final Optional<JavaSootClass> classSource1 = jimpleView.getClass(classTypeJimpleTrapValidator);
    assertFalse(classSource1.isPresent());
  }

  Body getBody(String methodSignature) {
    return jimpleView
        .getMethod(jimpleView.getIdentifierFactory().parseMethodSignature(methodSignature))
        .get()
        .getBody();
  }

  @Test
  public void trapsValidator_success() {
    List<ValidationException> validationExceptions_success =
        jimpleTrapValidator.validate(
            getBody("<TrapsValidator: void trapsValidator_success()>"), jimpleView);

    assertEquals(0, validationExceptions_success.size());
  }
}
