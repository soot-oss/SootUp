package bugs;

import categories.Java8Test;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.DotExporter;
import sootup.java.bytecode.inputlocation.BytecodeClassLoadingOptions;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class Issue698Test {
  @Test
  public void testJar_missing_if_flows() {

    // https://repo1.maven.org/maven2/cn/hutool/hutool-db/5.7.18/hutool-db-5.7.18.jar.
    JavaProject applicationProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    "src/test/resources/bugs/698_hutool/hutool-db-5.7.18.jar",
                    SourceType.Application))
            .build();

    JavaView view = applicationProject.createMutableView();
    view.configBodyInterceptors(analysisInputLocation -> BytecodeClassLoadingOptions.Default);

    /*
        {
        final MethodSignature methodSignature =
                view.getIdentifierFactory()
                        .parseMethodSignature(
                                "<cn.hutool.db.sql.SqlBuilder: cn.hutool.db.sql.SqlBuilder insert(cn.hutool.db.Entity,java.lang.String)>");
        final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
        Assert.assertTrue(methodOpt.isPresent());

        final StmtGraph<?> stmtGraph = methodOpt.get().getBody().getStmtGraph();

        System.out.println(DotExporter.createUrlToWebeditor(stmtGraph));
        System.out.println(stmtGraph);

        final boolean findingTableName = stmtGraph.getStmts().stream()
                .anyMatch(
                        stmt ->
                                stmt instanceof JAssignStmt
                                        && ((JAssignStmt<?, ?>) stmt).getLeftOp().toString().equals("tableName"));
         Assert.assertTrue(findingTableName);
      }
    */
    {
      /// missing assignment of: password = $stack20
      final MethodSignature methodSignature =
          view.getIdentifierFactory()
              .parseMethodSignature(
                  "<cn.hutool.db.ds.pooled.PooledConnection: void <init>(cn.hutool.db.ds.pooled.PooledDataSource)>");
      final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
      Assert.assertTrue(methodOpt.isPresent());

      final StmtGraph<?> stmtGraph = methodOpt.get().getBody().getStmtGraph();

      final boolean findingPasswordLocal =
          stmtGraph.getStmts().stream()
              .anyMatch(
                  stmt ->
                      stmt instanceof JAssignStmt
                          && ((JAssignStmt<?, ?>) stmt).getLeftOp().toString().equals("password"));

      System.out.println(DotExporter.createUrlToWebeditor(stmtGraph));
      System.out.println(stmtGraph);

      Assert.assertTrue(findingPasswordLocal);
    }
  }
}
