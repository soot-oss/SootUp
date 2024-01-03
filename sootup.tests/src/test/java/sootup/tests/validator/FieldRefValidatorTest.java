package sootup.tests.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import categories.Java8Test;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.validation.FieldRefValidator;
import sootup.core.validation.ValidationException;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleView;

@Category(Java8Test.class)
public class FieldRefValidatorTest {

  FieldRefValidator fieldRefValidator;
  JimpleView jimpleView;
  Collection<SootClass<?>> classes;

  @Before
  public void Setup() {

    fieldRefValidator = new FieldRefValidator();

    ClassType classTypeFieldRefValidator =
        new ClassType() {
          @Override
          public boolean isBuiltInClass() {
            return false;
          }

          @Override
          public String getFullyQualifiedName() {
            return "jimple.FieldRefValidator";
          }

          @Override
          public String getClassName() {
            return "FieldRefValidator";
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
    final Optional<SootClass<?>> classSource1 = jimpleView.getClass(classTypeFieldRefValidator);
    assertFalse(classSource1.isPresent());

    classes = new HashSet<>(); // Set to track the classes to check

    for (SootClass<?> aClass : jimpleView.getClasses()) {
      if (!aClass.isLibraryClass()) {
        classes.add(aClass);
      }
    }
  }

  @Test
  public void testFieldRefValidatorSuccess() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        fieldRefValidator.validate(
            classes.stream()
                .filter(c -> c.getType().getClassName().equals("FieldRefValidator"))
                .findFirst()
                .get()
                .getMethods()
                .stream()
                .filter(m -> m.getName().equals("testFieldRefVariable_pass"))
                .map(m -> m.getBody())
                .findFirst()
                .get(),
            jimpleView);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testFieldRefValidatorFailure() {
    List<ValidationException> validationExceptions_fail;

    validationExceptions_fail =
        fieldRefValidator.validate(
            classes.stream()
                .filter(c -> c.getType().getClassName().equals("FieldRefValidator"))
                .findFirst()
                .get()
                .getMethods()
                .stream()
                .filter(m -> m.getName().equals("testFieldRefVariable_fail"))
                .map(m -> m.getBody())
                .findFirst()
                .get(),
            jimpleView);

    assertEquals(2, validationExceptions_fail.size());
  }
}
