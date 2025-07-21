package sootup.tests.exceptions;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import sootup.core.graph.*;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaEagerView;
import sootup.java.core.views.JavaView;

public class RemoveExceptionalEdgeTest {

  static class ExceptionalEdgeInterceptor implements BodyInterceptor {

    @Override
    public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
      MethodSignature methodSignature =
          view.getIdentifierFactory()
              .parseMethodSignature("<org.atmosphere.util.Version: void <clinit>()>");
      if (!builder.getMethodSignature().equals(methodSignature)) return;

      MutableStmtGraph stmtGraph = builder.getStmtGraph();
      List<? extends BasicBlock<?>> blocks = stmtGraph.getBlocks().stream().toList();
      BasicBlock<?> basicBlock = blocks.get(11);
      Stmt head = basicBlock.getHead();

      stmtGraph.removeExceptionalEdge(
          head, new JavaClassType("Throwable", new PackageName("java.lang")));
      if (basicBlock.getSuccessors().size() == 0) {
        stmtGraph.removeBlock(basicBlock);
      }
    }
  }

  @Test
  public void testRemoveExceptionalEdgeAndMerge() {
    List<BodyInterceptor> interceptors =
        new ArrayList<>(BytecodeBodyInterceptors.Default.getBodyInterceptors());
    interceptors.add(new ExceptionalEdgeInterceptor());

    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/exception/removeExceptionalEdge/",
            SourceType.Application,
            interceptors);
    JavaView view = new JavaEagerView(inputLocation);
  }
}
