package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.validation.BodyValidator;
import sootup.core.validation.ReturnStatementsValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public class ReturnStatementsValidatorTest {

  @Test
  public void testValidateMissingReturnAndThrowStatement() {
    JavaView view = Mockito.mock(JavaView.class);
    JavaSootMethod method = Mockito.mock(JavaSootMethod.class);
    Body body = Mockito.mock(Body.class);
    Stmt stmt = Jimple.newNopStmt(StmtPositionInfo.getNoStmtPositionInfo());

    when(body.getStmts()).thenReturn(List.of(stmt));

    when(method.getBody()).thenReturn(body);

    when(view.getMethod(any())).thenReturn(Optional.of(method));

    BodyValidator validator = new ReturnStatementsValidator();
    List<ValidationException> validationExceptions = validator.validate(body, view);
    assertEquals(1, validationExceptions.size());
  }

  @Test
  public void testNoReturnButThrowStatement() {
    JavaView view = Mockito.mock(JavaView.class);
    JavaSootMethod method = Mockito.mock(JavaSootMethod.class);
    Body body = Mockito.mock(Body.class);
    Stmt stmt = Jimple.newThrowStmt(null, StmtPositionInfo.getNoStmtPositionInfo());

    when(body.getStmts()).thenReturn(List.of(stmt));

    when(method.getBody()).thenReturn(body);

    when(view.getMethod(any())).thenReturn(Optional.of(method));

    BodyValidator validator = new ReturnStatementsValidator();
    List<ValidationException> validationExceptions = validator.validate(body, view);
    assertEquals(0, validationExceptions.size());
  }
}
