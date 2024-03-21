package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.validation.ValidationException;
import sootup.core.validation.ValuesValidator;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleView;

@Tag("Java8")
public class ValuesValidatorTest {
  ValuesValidator valuesValidator;
  JimpleView jimpleView;
  Collection<SootClass> classes;

  @BeforeEach
  public void Setup() {

    valuesValidator = new ValuesValidator();

    ClassType classTypeValuesValidator =
        new ClassType() {
          @Override
          public boolean isBuiltInClass() {
            return false;
          }

          @Override
          public String getFullyQualifiedName() {
            return "jimple.ValuesValidator";
          }

          @Override
          public String getClassName() {
            return "ValuesValidator";
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
    final Optional<SootClass> classSource1 = jimpleView.getClass(classTypeValuesValidator);
    assertFalse(classSource1.isPresent());

    classes = new HashSet<>(); // Set to track the classes to check

    for (SootClass aClass : jimpleView.getClasses()) {
      if (!aClass.isLibraryClass()) {
        classes.add(aClass);
      }
    }
  }

  @Test
  public void testValuesValidatorSuccess() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        valuesValidator.validate(
            classes.stream()
                .filter(c -> c.getType().getClassName().equals("ValuesValidator"))
                .findFirst()
                .get()
                .getMethods()
                .stream()
                .filter(m -> m.getName().equals("valuesValidator_success"))
                .map(SootMethod::getBody)
                .findFirst()
                .get(),
            jimpleView);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testValuesValidatorFail() {
    List<ValidationException> validationExceptions_fail;

    validationExceptions_fail =
        valuesValidator.validate(
            classes.stream()
                .filter(c -> c.getType().getClassName().equals("ValuesValidator"))
                .findFirst()
                .get()
                .getMethods()
                .stream()
                .filter(m -> m.getName().equals("valuesValidator_fail"))
                .map(SootMethod::getBody)
                .findFirst()
                .get(),
            jimpleView);

    assertEquals(2, validationExceptions_fail.size());
  }
}
