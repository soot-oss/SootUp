package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.PrimitiveType;
import sootup.core.validation.BodyValidator;
import sootup.core.validation.InvokeArgumentValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.tests.util.TestUtil;

public class InvokeArgumentValidatorTest {

  @Test
  public void testInvalidInvokeArgument() {
    JavaView view = Mockito.mock(JavaView.class);

    JavaClassType dummyType = new JavaClassType("dummy", new PackageName("dummy"));
    MethodSignature dummyMethodSignature =
        new MethodSignature(
            dummyType,
            "dummyMethod",
            List.of(PrimitiveType.IntType.getInstance(), PrimitiveType.IntType.getInstance()),
            PrimitiveType.IntType.getInstance());

    JavaSootMethod dummyMethod =
        JavaSootMethod.JavaSootMethodBuilder.builder().withSignature(dummyMethodSignature).build();
    when(view.getMethod(any())).thenReturn(Optional.of(dummyMethod));

    JStaticInvokeExpr invokeExpr =
        Jimple.newStaticInvokeExpr(
            dummyMethodSignature,
            List.of(Jimple.newLocal("a", PrimitiveType.IntType.getInstance())));
    FallsThroughStmt illegalInvokeStmt =
        Jimple.newInvokeStmt(invokeExpr, StmtPositionInfo.getNoStmtPositionInfo());
    MutableBlockStmtGraph stmtGraph = new MutableBlockStmtGraph();

    stmtGraph.addNode(illegalInvokeStmt);
    stmtGraph.setStartingStmt(illegalInvokeStmt);
    stmtGraph.putEdge(
        illegalInvokeStmt, Jimple.newReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo()));
    Body body =
        Body.builder(stmtGraph).setMethodSignature(TestUtil.createDummyMethodSignature()).build();

    BodyValidator validator = new InvokeArgumentValidator();
    List<ValidationException> validationExceptions = validator.validate(body, view);
    assertEquals(1, validationExceptions.size());
  }
}
