package sootup.java.bytecode.minimaltestsuite.java6.ValidatorTests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import sootup.core.validation.NewValidator;
import sootup.core.validation.ValidationException;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;

public class NewKeywordValidatorTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void testNewValidator() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());

    List<? extends JavaSootMethod> main =
        sootClass.getMethods().stream()
            .filter(javaSootMethod -> javaSootMethod.getName().contains("main"))
            .collect(Collectors.toList());

    List<ValidationException> validationExceptions = new ArrayList<>();

    new NewValidator().validate(main.get(0).getBody(), validationExceptions);

    assertEquals(0, validationExceptions.size());
  }
}
