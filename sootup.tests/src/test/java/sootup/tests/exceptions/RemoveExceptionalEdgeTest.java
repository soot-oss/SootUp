package sootup.tests.exceptions;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sootup.core.graph.*;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
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

      MutableControlFlowGraph controlFlowGraph = builder.getControlFlowGraph();
      ClassType throwable = view.getIdentifierFactory().getClassType("java.lang.Throwable");
      // a block with a Throwable trap; do not rely on the iteration order of getBlocks()
      BasicBlock<?> basicBlock =
          controlFlowGraph.getBlocks().stream()
              .filter(b -> b.getExceptionalSuccessors().containsKey(throwable))
              .findFirst()
              .orElseThrow();
      Stmt head = basicBlock.getHead();

      controlFlowGraph.removeExceptionalEdge(head, throwable);
      Assertions.assertFalse(controlFlowGraph.exceptionalSuccessors(head).containsKey(throwable));
      // the block may have been merged with a neighbour: look it up again
      BasicBlock<?> blockOfHead = controlFlowGraph.getBlockOf(head);
      if (blockOfHead.getSuccessors().isEmpty()) {
        controlFlowGraph.removeBlock(blockOfHead);
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
