package sootup.apk.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.DexBodyInterceptors;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.UnknownType;
import sootup.core.views.View;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * The interceptors of an input location are shared by its bodies, which may be resolved
 * concurrently: the dex transformers must not keep the state of a body in their fields.
 */
class DexTransformersConcurrencyTest {

  private static final StmtPositionInfo POS = StmtPositionInfo.getNoStmtPositionInfo();
  private static final JavaIdentifierFactory FACTORY = new JavaIdentifierFactory();
  private static final MethodSignature USE_OBJECT =
      FACTORY.parseMethodSignature("<example.Test: void use(java.lang.Object)>");
  private static final MethodSignature USE_INT =
      FACTORY.parseMethodSignature("<example.Test: void use(int)>");
  private static final View VIEW = new JavaView(Collections.emptyList());

  /** {@code l = 0; use(l); return;} with an object or an int parameter */
  private static Body.BodyBuilder body(boolean usedAsObject) {
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    FallsThroughStmt zero = Jimple.newAssignStmt(l, IntConstant.getInstance(0), POS);
    FallsThroughStmt use =
        Jimple.newInvokeStmt(
            Jimple.newStaticInvokeExpr(usedAsObject ? USE_OBJECT : USE_INT, l), POS);
    Stmt ret = Jimple.newReturnVoidStmt(POS);
    var graph = new MutableBlockControlFlowGraph();
    graph.putEdge(zero, use);
    graph.putEdge(use, ret);
    graph.setStartingStmt(zero);
    return Body.builder(graph)
        .setLocals(new LinkedHashSet<>(List.of(l)))
        .setMethodSignature(FACTORY.parseMethodSignature("<example.Test: void test()>"));
  }

  private static Constant zeroAfterTransforming(boolean usedAsObject) {
    Body.BodyBuilder builder = body(usedAsObject);
    for (BodyInterceptor interceptor : DexBodyInterceptors.Default.bodyInterceptors()) {
      interceptor.interceptBody(builder, VIEW);
    }
    return (Constant) ((JAssignStmt) builder.getStmts().get(0)).getRightOp();
  }

  @Test
  void bodiesCanBeTransformedConcurrently() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(8);
    try {
      List<Future<Constant>> results =
          IntStream.range(0, 20000)
              .mapToObj(i -> executor.submit(() -> zeroAfterTransforming(i % 2 == 0)))
              .collect(Collectors.toList());
      for (int i = 0; i < results.size(); i++) {
        Constant expected = i % 2 == 0 ? NullConstant.getInstance() : IntConstant.getInstance(0);
        assertEquals(expected, results.get(i).get(), "body " + i);
      }
    } finally {
      executor.shutdownNow();
      executor.awaitTermination(1, TimeUnit.MINUTES);
    }
  }
}
