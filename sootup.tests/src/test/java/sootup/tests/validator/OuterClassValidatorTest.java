package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sootup.core.signatures.PackageName;
import sootup.core.validation.ClassValidator;
import sootup.core.validation.OuterClassValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class OuterClassValidatorTest {

  @Test
  public void testOuterClassValidator() {
    JavaView view = Mockito.mock(JavaView.class);
    JavaSootClass sootClass = Mockito.mock(JavaSootClass.class);

    JavaClassType dummyClassType = new JavaClassType("dummy", new PackageName("dummy"));
    when(sootClass.hasOuterClass()).thenReturn(true);
    when(sootClass.getOuterClass()).thenReturn((Optional) Optional.of(dummyClassType));

    when(view.getClass(sootClass.getOuterClass().get())).thenReturn(Optional.of(sootClass));

    ClassValidator validator = new OuterClassValidator();
    List<ValidationException> exceptionList = new ArrayList<>();
    validator.validate(sootClass, exceptionList, view);
    assertEquals(1, exceptionList.size());
  }
}
