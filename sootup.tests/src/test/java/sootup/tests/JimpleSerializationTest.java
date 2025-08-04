package sootup.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public class JimpleSerializationTest {

  @Test
  public void testTrapSerialization() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/bugs/1119_trap-serialization",
            SourceType.Application,
            Collections.emptyList());
    JavaView view = new JavaView(inputLocation);

    Optional<JavaSootMethod> methodOpt =
        view.getMethod(
            view.getIdentifierFactory()
                .parseMethodSignature(
                    "<com.linecorp.centraldogma.server.internal.storage.repository.git.GitRepository: java.util.Map blockingFind(com.linecorp.centraldogma.common.Revision,java.lang.String,java.util.Map)>"));
    assertTrue(methodOpt.isPresent());
    JavaSootMethod method = methodOpt.get();
    method.getBody().toString();
  }

  @Test
  public void testBasicTrapSerialization() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/bugs/1119_trap-serialization",
            SourceType.Application,
            Collections.emptyList());
    JavaView javaView = new JavaView(inputLocation);
    Optional<JavaSootMethod> nestedTrap =
        javaView.getMethod(
            javaView
                .getIdentifierFactory()
                .parseMethodSignature(
                    "<com.linecorp.centraldogma.server.internal.storage.repository.git.TrapSerialization: java.lang.Integer processWithExplicitCasting(java.lang.String,java.lang.String)>"));

    assertTrue(nestedTrap.isPresent());
    JavaSootMethod nestedTrapMethod = nestedTrap.get();
    nestedTrapMethod.getBody().getStmtGraph().toString();
  }
}
