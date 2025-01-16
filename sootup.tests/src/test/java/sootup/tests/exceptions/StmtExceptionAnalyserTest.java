package sootup.tests.exceptions;

import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.interceptors.*;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.exceptions.ExceptionInferResult;
import sootup.java.core.views.JavaView;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StmtExceptionAnalyserTest {

    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    ClassType clazzType = factory.getClassType("ArrayExceptions");
    String location =
            Paths.get(System.getProperty("user.dir")).getParent()
                    + File.separator
                    + "shared-test-resources/exceptions/";
    MethodSignature methodSignature =
            factory.getMethodSignature(clazzType, "testException", "void", Collections.emptyList());
    final Path path = Paths.get(location + "ArrayExceptions.class");
    List<BodyInterceptor> interceptors = Arrays.asList(new EmptySwitchEliminator(),
            new CastAndReturnInliner(),
            new Aggregator(),
            new LocalSplitter(),
            new CopyPropagator(),
            new ConstantPropagatorAndFolder(),
            new TypeAssigner());
    PathBasedAnalysisInputLocation inputLocation =
            new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
                    path, "", SourceType.Application, interceptors);
    JavaClassPathAnalysisInputLocation javaInputLocation = new JavaClassPathAnalysisInputLocation(
            System.getProperty("java.home") + "/lib/rt.jar");
    JavaView view = new JavaView(Arrays.asList(inputLocation, javaInputLocation));
    Body body = view.getMethod(methodSignature).get().getBody();

    @Test
    public void testMightThrowExplicitly(){
        MutableBlockStmtGraph graph = new MutableBlockStmtGraph(body.getStmtGraph());
        Body.BodyBuilder builder = Body.builder(graph);
        List<Stmt> stmts =  graph.getStmts();
        TypeHierarchy hierarchy = new ViewTypeHierarchy(view);
        System.out.println(hierarchy.superClassOf(ExceptionInferResult.ErrorType.INTERNAL_ERROR));
        /*System.out.println(body.toString());
        Stmt target = stmts.stream().filter(stmt -> stmt.toString().equals("l1[l2#0] = $stack8")).collect(Collectors.toList()).get(0);
        System.out.println((((JAssignStmt) target).getRightOp()).getType());*/
    }

}
