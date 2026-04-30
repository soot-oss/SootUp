package sootup.apk.frontend;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;

public class StaticInvokeArgsTest {

  public static JavaView view;
  static String cryptoClassName = "com.example.CryptoWeakness";

  @BeforeAll
  public static void createView() {
    String apk_path_string = "resources/Crypto.apk";
    Path apkPath = Paths.get(apk_path_string);
    String androidPlatformsPath = "";
    ApkAnalysisInputLocation sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation(
            apkPath, androidPlatformsPath, DexBodyInterceptors.Default.bodyInterceptors());
    view = new JavaView(sootClassApkAnalysisInputLocation);
  }

  @Test
  public void testStaticInvokesContainArguments() {
    MethodSignature generateKeyWithWeakRandomSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                cryptoClassName, "generateKeyWithWeakRandom", "javax.crypto.SecretKey", List.of());

    int argCount =
        view.getMethod(generateKeyWithWeakRandomSignature).get().getBody().getStmts().stream()
            .filter(Stmt::isInvokableStmt)
            .map(Stmt::asInvokableStmt)
            .filter(invokableStmt -> invokableStmt.getInvokeExpr().isPresent())
            .filter(
                invokableStmt ->
                    invokableStmt
                        .getInvokeExpr()
                        .get()
                        .getMethodSignature()
                        .toString()
                        .contains("getInstance"))
            .findFirst()
            .flatMap(InvokableStmt::getInvokeExpr)
            .get()
            .getArgs()
            .size();
    // There should be at least one static invoke with arguments present in the APK
    Assertions.assertEquals(1, argCount);
  }
}
