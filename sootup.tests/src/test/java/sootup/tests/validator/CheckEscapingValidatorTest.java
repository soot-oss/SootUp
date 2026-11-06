package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.validation.CheckEscapingValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.types.JavaClassType;
import sootup.tests.util.TestUtil;

public class CheckEscapingValidatorTest {

  @Test
  public void testValidateMethodSignature() {
    Body body = Mockito.mock(Body.class);
    Stmt stmt = Mockito.mock(Stmt.class);
    InvokableStmt invokableStmt = Mockito.mock(InvokableStmt.class);
    AbstractInvokeExpr invokeExpr = Mockito.mock(AbstractInvokeExpr.class);
    MethodSignature sig =
        new MethodSignature(
            TestUtil.createDummyClassType(),
            "'notescaped",
            Collections.emptyList(),
            TestUtil.createDummyClassType());

    when(body.getStmts()).thenReturn(List.of(stmt));

    when(stmt.isInvokableStmt()).thenReturn(true);
    when(stmt.asInvokableStmt()).thenReturn(invokableStmt);

    when(invokableStmt.getInvokeExpr()).thenReturn(Optional.of(invokeExpr));

    when(invokeExpr.getMethodSignature()).thenReturn(sig);

    CheckEscapingValidator validator = new CheckEscapingValidator();
    List<ValidationException> validationExceptions = validator.validate(body, null);
    assertEquals(1, validationExceptions.size());
  }

  @Test
  public void testValidateMethodType() {
    Body body = Mockito.mock(Body.class);
    Stmt stmt = Mockito.mock(Stmt.class);
    InvokableStmt invokableStmt = Mockito.mock(InvokableStmt.class);
    AbstractInvokeExpr invokeExpr = Mockito.mock(AbstractInvokeExpr.class);
    MethodSignature sig =
        new MethodSignature(
            TestUtil.createDummyClassType(),
            "correct",
            List.of(new JavaClassType("'dummy", new PackageName("'dummy'"))),
            TestUtil.createDummyClassType());

    when(body.getStmts()).thenReturn(List.of(stmt));

    when(stmt.isInvokableStmt()).thenReturn(true);
    when(stmt.asInvokableStmt()).thenReturn(invokableStmt);

    when(invokableStmt.getInvokeExpr()).thenReturn(Optional.of(invokeExpr));

    when(invokeExpr.getMethodSignature()).thenReturn(sig);

    CheckEscapingValidator validator = new CheckEscapingValidator();
    List<ValidationException> validationExceptions = validator.validate(body, null);
    assertEquals(1, validationExceptions.size());
  }
}
