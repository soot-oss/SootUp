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
import sootup.core.validation.CheckInitValidator;
import sootup.core.validation.ValidationException;
import sootup.jimple.frontend.JimpleAnalysisInputLocation;
import sootup.jimple.frontend.JimpleView;

public class CheckInitValidatorTest {
  CheckInitValidator checkInitValidator;
  JimpleView jimpleView;

  Collection<SootClass> classes;

  @BeforeEach
  public void Setup() {

    checkInitValidator = new CheckInitValidator();

    ClassType classTypeCheckInitValidator =
        new ClassType() {

          @Override
          public String getFullyQualifiedName() {
            return "jimple.CheckInitValidator";
          }

          @Override
          public String getClassName() {
            return "CheckInitValidator";
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
        checkInitValidator.validate(
            classes.stream()
                .filter(c -> c.getType().getClassName().equals("CheckInitValidator"))
                .findFirst()
                .get()
                .getMethods()
                .stream()
                .filter(m -> m.getName().equals("checkInitValidator_success"))
                .map(SootMethod::getBody)
                .findFirst()
                .get(),
            jimpleView);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testCheckInitValidatorFail() {
    List<ValidationException> validationExceptions_fail;

    Body sootMethodbody =
        classes.stream()
            .filter(c -> c.getType().getClassName().equals("CheckInitValidator"))
            .findFirst()
            .get()
            .getMethods()
            .stream()
            .filter(m -> m.getName().equals("checkInitValidator_fail"))
            .map(SootMethod::getBody)
            .findFirst()
            .get();

    Body.BodyBuilder builder = Body.builder(sootMethodbody, Collections.emptySet());

    validationExceptions_fail =
        checkInitValidator.validate(builder.setLocals(Collections.emptySet()).build(), jimpleView);

    assertEquals(1, validationExceptions_fail.size());
  }
}
