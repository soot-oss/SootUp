package sootup.java.bytecode.interceptors.defuse;

import categories.Java8Test;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.*;

import static org.junit.Assert.assertTrue;

@Category(Java8Test.class)
public class DefUseHelperTest {

    JavaView view;

    @Before
    public void Setup() {
        String classPath = "src/test/java/resources/interceptors";
        JavaClassPathAnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);
        view = new JavaView(inputLocation);
    }

    private Body getBody(String methodName, Type returnType) {
        ClassType type = new JavaClassType("LocalSplitterTarget", PackageName.DEFAULT_PACKAGE);
        MethodSignature sig = new MethodSignature(type, new MethodSubSignature(methodName, Collections.EMPTY_LIST, returnType));
        SootMethod sootMethod = view.getMethod(sig).get();
        Body originalBody = sootMethod.getBody();
        return originalBody;
    }

    private Stmt getStmt(Body body, String s){
        return body.getStmts().stream().filter(e -> e.toString().equals(s)).findFirst().get();
    }

    @Test
    public void testSimpleAssign(){
        Body originalBody = getBody("case0", VoidType.getInstance());
        Map<Local, Stmt> actual = DefUseHelper.getDefsInBlock(originalBody.getStmtGraph().getBlocksSorted().get(0));

        Stmt s1 = getStmt(originalBody, "$l1 = $l2 + 1");
        Local def1 = (Local) s1.getDefs().get(0);

        Stmt s2 = getStmt(originalBody, "$l2 = $l1 + 1");
        Local def2 = (Local) s2.getDefs().get(0);

        assertTrue(actual.get(def1).equals(s1));
        assertTrue(actual.get(def2).equals(s2));
    }

    @Test
    public void testSelfAssign(){
        Body originalBody = getBody("case1", VoidType.getInstance());
        Map<Local, Stmt> actual = DefUseHelper.getDefsInBlock(originalBody.getStmtGraph().getBlocksSorted().get(0));

        Stmt s1 = getStmt(originalBody, "$l1 = $l1 + 1");
        Local def1 = (Local) s1.getDefs().get(0);

        Stmt s2 = getStmt(originalBody, "$l2 = $l2 + 1");
        Local def2 = (Local) s2.getDefs().get(0);

        assertTrue(actual.get(def1).equals(s1));
        assertTrue(actual.get(def2).equals(s2));
    }

    @Test
    public void testBranch(){
        Body originalBody = getBody("case2", PrimitiveType.IntType.getInstance());
        Map<Local, Stmt> defsInBlock0 = DefUseHelper.getDefsInBlock(originalBody.getStmtGraph().getBlocksSorted().get(0));
        Stmt s0 = getStmt(originalBody, "$l1 = 0");
        Local def0 = (Local) s0.getDefs().get(0);
        assertTrue(defsInBlock0.get(def0).equals(s0));

        Map<Local, Stmt> defsInBlock1 = DefUseHelper.getDefsInBlock(originalBody.getStmtGraph().getBlocksSorted().get(1));
        Stmt s1 = getStmt(originalBody, "$l1 = $l1 + 1");
        Local def1 = (Local) s1.getDefs().get(0);
        assertTrue(defsInBlock1.get(def1).equals(s1));

        Map<Local, Stmt> defsInBlock2 = DefUseHelper.getDefsInBlock(originalBody.getStmtGraph().getBlocksSorted().get(2));
        Stmt s2 = getStmt(originalBody, "$l1 = $l1 + 2");
        Local def2 = (Local) s1.getDefs().get(0);
        assertTrue(defsInBlock2.get(def2).equals(s2));


        System.out.println(defsInBlock2);
        System.out.println(originalBody);

    }

    @Test
    public void testMultiBranch(){
        Body originalBody = getBody("case3", PrimitiveType.IntType.getInstance());
        Map<Local, Stmt> defsInBlock0 = DefUseHelper.getDefsInBlock(originalBody.getStmtGraph().getBlocksSorted().get(0));
        Stmt s0 = getStmt(originalBody, "$l1 = 0");
        Local def0 = (Local) s0.getDefs().get(0);
        assertTrue(defsInBlock0.get(def0).equals(s0));

        Map<Local, Stmt> defsInBlock1 = DefUseHelper.getDefsInBlock(originalBody.getStmtGraph().getBlocksSorted().get(1));
        Stmt s1 = getStmt(originalBody, "$l1 = $l1 + 3");
        Local def1 = (Local) s1.getDefs().get(0);
        assertTrue(defsInBlock1.get(def1).equals(s1));

        Map<Local, Stmt> defsInBlock2 = DefUseHelper.getDefsInBlock(originalBody.getStmtGraph().getBlocksSorted().get(2));
        Stmt s2 = getStmt(originalBody, "$l1 = $l1 - 3");
        Local def2 = (Local) s1.getDefs().get(0);
        assertTrue(defsInBlock2.get(def2).equals(s2));


        System.out.println(defsInBlock2);
        System.out.println(originalBody);

    }
}
