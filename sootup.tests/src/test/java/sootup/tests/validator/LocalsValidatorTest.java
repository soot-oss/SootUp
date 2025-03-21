package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
import sootup.core.validation.LocalsValidator;
import sootup.core.validation.ValidationException;
import sootup.jimple.frontend.JimpleAnalysisInputLocation;
import sootup.jimple.frontend.JimpleView;

public class LocalsValidatorTest {
  LocalsValidator localsValidator;
  JimpleView jimpleView;
  Collection<SootClass> classes;

  @BeforeEach
  public void Setup() {

    localsValidator = new LocalsValidator();

    ClassType classTypeCheckInitValidator =
        new ClassType() {

          @Override
          public String getFullyQualifiedName() {
            return "jimple.LocalsValidator";
          }

          @Override
          public String getClassName() {
            return "LocalsValidator";
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

    classes = new HashSet<>(); // Set to track the classes to check

    jimpleView.getClasses().forEach(classes::add);
  }

  @Test
  public void testCheckInitValidatorSuccess() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        localsValidator.validate(
            classes.stream()
                .filter(c -> c.getType().getClassName().equals("LocalsValidator"))
                .findFirst()
                .get()
                .getMethods()
                .stream()
                .filter(m -> m.getName().equals("localsValidator_success"))
                .map(SootMethod::getBody)
                .findFirst()
                .get(),
            jimpleView);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testCheckInitValidatorFail() {
    List<ValidationException> validationExceptions_success;

    Body body =
        classes.stream()
            .filter(c -> c.getType().getClassName().equals("LocalsValidator"))
            .findFirst()
            .get()
            .getMethods()
            .stream()
            .filter(m -> m.getName().equals("localsValidator_fail"))
            .map(SootMethod::getBody)
            .findFirst()
            .get();

    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    builder.setLocals(new HashSet<>());

    validationExceptions_success = localsValidator.validate(builder.build(), jimpleView);

    assertEquals(3, validationExceptions_success.size());
  }
}
