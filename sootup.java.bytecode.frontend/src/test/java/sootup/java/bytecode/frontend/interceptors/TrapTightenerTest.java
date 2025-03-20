package sootup.java.bytecode.frontend.interceptors;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.Position;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.interceptors.*;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import static sootup.core.model.Body.builder;

/** @author Zun Wang */
public class TrapTightenerTest {

  final JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  final String location = Paths.get(System.getProperty("user.dir")).getParent() + File.separator + "shared-test-resources/interceptors/";
  final Path path = Paths.get(location + "TrapTightenerExamples.class");
  List<BodyInterceptor> interceptors =
          Arrays.asList(
                  new EmptySwitchEliminator(),
                  new CastAndReturnInliner(),
                  new Aggregator(),
                  new TypeAssigner());
  List<BodyInterceptor> interceptorsWithTT =
          Arrays.asList(
                  new EmptySwitchEliminator(),
                  new CastAndReturnInliner(),
                  new Aggregator(),
                  new TypeAssigner(),
                  new TrapTightener());
  public final BriefStmtPrinter briefStmtPrinter = new BriefStmtPrinter();
  PathBasedAnalysisInputLocation inputLocation =
          new ClassFileBasedAnalysisInputLocation(path, "", SourceType.Application, interceptors);
  PathBasedAnalysisInputLocation inputLocationWithTT =
          new ClassFileBasedAnalysisInputLocation(
                  path,
                  "",
                  SourceType.Application,
                  interceptorsWithTT);
  AnalysisInputLocation javaInputLocation = new DefaultRuntimeAnalysisInputLocation();
  JavaView view = new JavaView(Arrays.asList(inputLocation, javaInputLocation ));
  JavaView viewTT = new JavaView(Arrays.asList(inputLocationWithTT, javaInputLocation));
  ClassType clazzType = factory.getClassType("TrapTightenerExamples");


  /**
   * <pre>
   *    l0 := @this Test;
   *  label1:
   *    l1 = 1;
   *    l2 = 2;
   *    l2 = 3;
   *  label2:
   *    goto label4;
   *  label3:
   *    l3 := @caughtexception;
   *    l2 = 4;
   *    throw l3;
   *  label4:
   *    return;
   *  catch Exception from label1 to label2 with label3;
   * </pre>
   *
   * after run trapTightener
   *
   * <pre>
   *    l0 := @this Test;
   *    l1 = 1;
   *  label1:
   *    l2 = 2;
   *  label2:
   *    l2 = 3;
   *    goto label4;
   *  label3:
   *    l3 := @caughtexception;
   *    l2 = 4;
   *    throw l3;
   *  label4:
   *    return;
   *  catch Exception from label1 to label2 with label3;
   * </pre>
   */
  @Test
  public void testExample1() {
    MethodSignature methodSignature = factory.getMethodSignature(clazzType, "example1", "int", Collections.emptyList());
    Body body = view.getMethod(methodSignature).get().getBody();
    System.out.println(body);
    Body bodyTT = viewTT.getMethod(methodSignature).get().getBody();
    System.out.println(bodyTT);
  }
}
