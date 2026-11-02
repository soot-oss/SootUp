package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

/**
 * {@code SharedZero.apk} holds one class, generated with dexlib2, whose methods load one zero and
 * pass it to two methods with different parameter types:
 *
 * <pre>
 * intAndObject:  const/4 v0, 0; invoke-static {v0}, useInt(I)V; invoke-static {v0}, useObject(Ljava/lang/Object;)V; return-void
 * </pre>
 */
public class DexSharedConstantTest {

  private static List<String> body(String method) {
    Path apk = Paths.get("src/test/resources/SharedZero.apk");
    JavaView view =
        new JavaView(
            new ApkAnalysisInputLocation(
                apk,
                new AndroidVersionInfo(apk, ""),
                DexBodyInterceptors.Default.bodyInterceptors()));
    JavaSootClass clazz =
        view.getClass(view.getIdentifierFactory().getClassType("example.SharedZero")).get();
    SootMethod m =
        clazz.getMethods().stream().filter(x -> x.getName().equals(method)).findFirst().get();
    return m.getBody().getStmts().stream().map(Stmt::toString).collect(Collectors.toList());
  }

  @Test
  void zeroSharedByAnIntAndAnObjectParameter() {
    List<String> stmts = body("intAndObject");
    assertEquals(
        List.of(
            "$u0 = 0",
            "staticinvoke <example.SharedZero: void useInt(int)>($u0)",
            "$u0#c1 = null",
            "staticinvoke <example.SharedZero: void useObject(java.lang.Object)>($u0#c1)",
            "return"),
        stmts);
  }
}
