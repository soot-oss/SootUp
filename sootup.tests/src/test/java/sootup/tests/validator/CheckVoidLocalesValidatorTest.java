package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sootup.core.jimple.common.Local;
import sootup.core.model.Body;
import sootup.core.types.VoidType;
import sootup.core.validation.CheckVoidLocalesValidator;
import sootup.core.validation.ValidationException;

public class CheckVoidLocalesValidatorTest {

  @Test
  public void testValidate() {
    Body body = Mockito.mock(Body.class);
    Local l = Mockito.mock(Local.class);

    when(l.getType()).thenReturn(VoidType.getInstance());

    when(body.getLocals()).thenReturn(Set.of(l));

    CheckVoidLocalesValidator validator = new CheckVoidLocalesValidator();
    List<ValidationException> validationExceptions = validator.validate(body, null);
    assertEquals(1, validationExceptions.size());
  }
}
