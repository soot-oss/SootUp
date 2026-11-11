package sootup.java.bytecode.frontend.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.Type;
import sootup.interceptors.typeresolving.AugEvalFunction;
import sootup.interceptors.typeresolving.BytecodeHierarchy;
import sootup.interceptors.typeresolving.CastCounter;
import sootup.interceptors.typeresolving.Typing;

/**
 * A local that is not typed as an array but is assigned arrays of two element types: the type
 * checker takes the array type from the first allocation in statement order, as Soot does.
 */
public class TypeCheckerArrayDefsTest extends TypeAssignerTestSuite {

  AugEvalFunction function;
  BytecodeHierarchy hierarchy;

  @BeforeEach
  public void setup() {
    buildView("src/test/resources/TypeResolverTestSuite/TypeCheckerTest/", "ArrayDefsDemos");
    function = new AugEvalFunction(view);
    hierarchy = new BytecodeHierarchy(view);
  }

  // repeated: each run has new statements, so a hash order would show as a different result
  @RepeatedTest(20)
  public void arrayStoreTakesTheFirstAllocation() {
    assertCastToFirstAllocation(createMethodsBuilder("storeIntoArrayOfTwoTypes", "void"));
  }

  @RepeatedTest(20)
  public void arrayLoadTakesTheFirstAllocation() {
    assertCastToFirstAllocation(createMethodsBuilder("loadFromArrayOfTwoTypes", "void"));
  }

  /** Types the array local as Object, so the array type has to come from its allocations. */
  private void assertCastToFirstAllocation(Body.BodyBuilder builder) {
    List<Stmt> stmts = builder.getStmts();
    Local array =
        stmts.stream()
            .flatMap(s -> s.getUsesAndDefs().stream())
            .filter(JArrayRef.class::isInstance)
            .map(ref -> ((JArrayRef) ref).getBase())
            .findFirst()
            .get();
    List<Type> allocations =
        stmts.stream()
            .filter(s -> s instanceof JAssignStmt && ((JAssignStmt) s).getLeftOp().equals(array))
            .map(s -> ((JAssignStmt) s).getRightOp())
            .filter(JNewArrayExpr.class::isInstance)
            .map(v -> ((JNewArrayExpr) v).getType())
            .collect(Collectors.toList());
    assertEquals(2, allocations.size());

    Typing typing = new Typing(builder.getLocals());
    for (Local local : builder.getLocals()) {
      typing.set(local, local.getType());
    }
    typing.set(array, view.getIdentifierFactory().getClassType("java.lang.Object"));
    new CastCounter(builder, function, hierarchy, typing).insertCastStmts();

    List<Type> casts =
        builder.getStmts().stream()
            .filter(s -> s instanceof JAssignStmt)
            .map(s -> ((JAssignStmt) s).getRightOp())
            .filter(v -> v instanceof JCastExpr && ((JCastExpr) v).getOp().equals(array))
            .map(v -> ((JCastExpr) v).getType())
            .distinct()
            .collect(Collectors.toList());
    assertEquals(List.of(allocations.get(0)), casts);
  }
}
