package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.validation.InvokeArgumentValidator;
import sootup.core.validation.ValidationException;
import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleView;

public class InvokeArgumentValidatorTest {
  InvokeArgumentValidator invokeArgumentValidator = new InvokeArgumentValidator();
  static JimpleView jimpleView;

  @BeforeAll
  public static void setup() {

    String classPath = "src/test/resources/validator/jimple";
    JimpleAnalysisInputLocation jimpleInputLocation =
        new JimpleAnalysisInputLocation(Paths.get(classPath), SourceType.Application);

    // rt.jar is required since the validator uses typeHierarchy
    DefaultRTJarAnalysisInputLocation defaultRTJarAnalysisInputLocation =
        new DefaultRTJarAnalysisInputLocation();
    jimpleView =
        new JimpleView(Arrays.asList(jimpleInputLocation, defaultRTJarAnalysisInputLocation));

    // Speed up the class search process by limiting the search scope within application classes
    final Optional<SootClass> classSource1 =
        jimpleView.getClasses().stream()
            .filter(c -> c.getType().toString().equals("jimple.InvokeArgumentValidator"))
            .findFirst();
    assertFalse(classSource1.isPresent());
  }

  @Test
  public void invokeArgumentValidator_success() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        invokeArgumentValidator.validate(
            jimpleView
                .getMethod(
                    jimpleView
                        .getIdentifierFactory()
                        .parseMethodSignature(
                            "<InvokeArgumentValidator: void invokeArgumentValidator_success()>"))
                .get()
                .getBody(),
            jimpleView);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testArgumentNumber_fail() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        invokeArgumentValidator.validate(
            jimpleView
                .getMethod(
                    jimpleView
                        .getIdentifierFactory()
                        .parseMethodSignature(
                            "<InvokeArgumentValidator: void testArgumentNumber_fail()>"))
                .get()
                .getBody(),
            jimpleView);
    assertEquals(1, validationExceptions_success.size());
  }

  @Test
  public void testArgumentType_fail() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        invokeArgumentValidator.validate(
            jimpleView
                .getMethod(
                    jimpleView
                        .getIdentifierFactory()
                        .parseMethodSignature(
                            "<InvokeArgumentValidator: void testArgumentType_fail()>"))
                .get()
                .getBody(),
            jimpleView);
    assertEquals(2, validationExceptions_success.size());
  }

  @Test
  public void testPrimitiveTypeConversion_success() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        invokeArgumentValidator.validate(
            jimpleView
                .getMethod(
                    jimpleView
                        .getIdentifierFactory()
                        .parseMethodSignature(
                            "<InvokeArgumentValidator: void testPrimitiveTypeConversion_success()>"))
                .get()
                .getBody(),
            jimpleView);
    assertEquals(0, validationExceptions_success.size());
  }
}
