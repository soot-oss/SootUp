package sootup.analysis.interprocedural.ifds;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import junit.framework.TestCase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.analysis.interprocedural.icfg.CGEdgeUtil;
import sootup.analysis.interprocedural.icfg.CGEdgeUtil.CallGraphEdgeType;
import sootup.analysis.interprocedural.icfg.CalleeMethodSignature;
import sootup.callgraph.AbstractCallGraphAlgorithm;
import sootup.callgraph.CallGraph;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

@Category(Java8Test.class)
public class CGEdgeUtilTest {

  @Test
  public void testFindCallGraphEdgeType() {
    Type returnType = PrimitiveType.getInt();

    Local testLocal = new Local("a", returnType);
    MethodSignature testMethod =
        new MethodSignature(
            new JavaClassType("Object", new PackageName("java.lang")),
            "test",
            Collections.singletonList(PrimitiveType.getInt()),
            returnType);
    List<Immediate> testParameterList = Collections.singletonList(IntConstant.getInstance(2));

    JVirtualInvokeExpr jVirtualInvokeExpr =
        new JVirtualInvokeExpr(testLocal, testMethod, testParameterList);
    assertEquals(CGEdgeUtil.findCallGraphEdgeType(jVirtualInvokeExpr), CallGraphEdgeType.VIRTUAL);

    JSpecialInvokeExpr jSpecialInvokeExpr =
        new JSpecialInvokeExpr(testLocal, testMethod, testParameterList);
    assertEquals(CGEdgeUtil.findCallGraphEdgeType(jSpecialInvokeExpr), CallGraphEdgeType.SPECIAL);

    JInterfaceInvokeExpr jInterfaceInvokeExpr =
        new JInterfaceInvokeExpr(testLocal, testMethod, testParameterList);
    assertEquals(
        CGEdgeUtil.findCallGraphEdgeType(jInterfaceInvokeExpr), CallGraphEdgeType.INTERFACE);

    JStaticInvokeExpr jStaticInvokeExpr = new JStaticInvokeExpr(testMethod, testParameterList);
    assertEquals(CGEdgeUtil.findCallGraphEdgeType(jStaticInvokeExpr), CallGraphEdgeType.STATIC);

    MethodSignature testDynamicMethod =
        new MethodSignature(
            new JavaClassType(
                JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.substring(
                    JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.lastIndexOf(".") + 1),
                new PackageName(
                    JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.substring(
                        0, JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.lastIndexOf(".")))),
            "test",
            Collections.singletonList(PrimitiveType.getInt()),
            returnType);
    JDynamicInvokeExpr jDynamicInvokeExpr =
        new JDynamicInvokeExpr(testMethod, testParameterList, testDynamicMethod, testParameterList);
    assertEquals(CGEdgeUtil.findCallGraphEdgeType(jDynamicInvokeExpr), CallGraphEdgeType.DYNAMIC);
  }

  @Test(expected = RuntimeException.class)
  public void testFindCallGraphEdgeTypeError() {
    MethodSignature testMethod =
        new MethodSignature(
            new JavaClassType("Object", new PackageName("java.lang")),
            "test",
            Collections.singletonList(PrimitiveType.getInt()),
            PrimitiveType.getInt());
    Immediate[] testParameterList = {IntConstant.getInstance(2)};

    AbstractInvokeExpr jNewInvoke =
        new AbstractInvokeExpr(testMethod, testParameterList) {
          @Override
          public void toString(@Nonnull StmtPrinter up) {}

          @Override
          public int equivHashCode() {
            return 0;
          }

          @Override
          public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
            return false;
          }

          @Override
          public void accept(@Nonnull ExprVisitor exprVisitor) {}
        };
    CGEdgeUtil.findCallGraphEdgeType(jNewInvoke);
  }

  @Test
  public void testGetCallEdges() {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    JavaView view =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addInputLocation(
                new JavaSourcePathAnalysisInputLocation("src/test/resources/callgraph/"))
            .build()
            .createView();

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature = identifierFactory.getClassType("example1.Example");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, "main", "void", Collections.singletonList("java.lang.String[]"));

    SootClass<?> sc = view.getClass(mainClassSignature).orElse(null);
    assertNotNull(sc);
    SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
    assertNotNull(mainMethodSignature + " not found in classloader", m);

    AbstractCallGraphAlgorithm algorithm = new RapidTypeAnalysisAlgorithm(view);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

    assertNotNull(cg);
    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));

    Set<Pair<MethodSignature, CalleeMethodSignature>> results = CGEdgeUtil.getCallEdges(view, cg);
    assertEquals(results.size(), 6);

    List<Stmt> invokesStmts =
        m.getBody().getStmts().stream()
            .filter(Stmt::containsInvokeExpr)
            .collect(Collectors.toList());
    assertEquals(invokesStmts.size(), 3);
    MethodSignature constructorMethodSignature =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "<init>",
            "void",
            Collections.emptyList());
    checkPair(
        results,
        invokesStmts,
        mainMethodSignature,
        constructorMethodSignature,
        CallGraphEdgeType.SPECIAL,
        JSpecialInvokeExpr.class);
    MethodSignature staticMethodSignature =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "staticDispatch",
            "void",
            Collections.emptyList());
    checkPair(
        results,
        invokesStmts,
        mainMethodSignature,
        staticMethodSignature,
        CallGraphEdgeType.STATIC,
        JStaticInvokeExpr.class);
    MethodSignature virtualMethodSignature =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "virtualDispatch",
            "void",
            Collections.emptyList());
    checkPair(
        results,
        invokesStmts,
        mainMethodSignature,
        virtualMethodSignature,
        CallGraphEdgeType.VIRTUAL,
        JVirtualInvokeExpr.class);
  }

  private void checkPair(
      Set<Pair<MethodSignature, CalleeMethodSignature>> results,
      List<Stmt> invokesStmts,
      MethodSignature key,
      MethodSignature valueMethod,
      CallGraphEdgeType type,
      Class<?> invokeClass) {
    Pair<MethodSignature, CalleeMethodSignature> virtualCall =
        results.stream()
            .filter(pair -> pair.getKey().equals(key) && pair.getValue().getEdgeType().equals(type))
            .findAny()
            .orElse(null);
    assertNotNull(virtualCall);
    Stmt virtualStmt =
        invokesStmts.stream()
            .filter(stmt -> stmt.getInvokeExpr().getClass() == invokeClass)
            .findAny()
            .orElse(null);
    assertNotNull(virtualStmt);
    TestCase.assertEquals(virtualCall.getValue().getMethodSignature(), valueMethod);
    TestCase.assertEquals(virtualCall.getValue().getSourceStmt(), virtualStmt);
  }

  @Test
  public void testCallGraphEdgeType() {
    CallGraphEdgeType invalidEnum = CallGraphEdgeType.INVALID;
    CallGraphEdgeType staticEnum = CallGraphEdgeType.STATIC;
    CallGraphEdgeType virtualEnum = CallGraphEdgeType.VIRTUAL;
    CallGraphEdgeType interfaceEnum = CallGraphEdgeType.INTERFACE;
    CallGraphEdgeType specialEnum = CallGraphEdgeType.SPECIAL;
    CallGraphEdgeType dynamicEnum = CallGraphEdgeType.DYNAMIC;
    CallGraphEdgeType clinitEnum = CallGraphEdgeType.CLINIT;
    CallGraphEdgeType genericFakeEnum = CallGraphEdgeType.GENERIC_FAKE;
    CallGraphEdgeType threadEnum = CallGraphEdgeType.THREAD;
    CallGraphEdgeType executorEnum = CallGraphEdgeType.EXECUTOR;
    CallGraphEdgeType asynctaskEnum = CallGraphEdgeType.ASYNCTASK;
    CallGraphEdgeType finalizeEnum = CallGraphEdgeType.FINALIZE;
    CallGraphEdgeType handlerEnum = CallGraphEdgeType.HANDLER;
    CallGraphEdgeType invokeFinalizeEnum = CallGraphEdgeType.INVOKE_FINALIZE;
    CallGraphEdgeType privilegedEnum = CallGraphEdgeType.PRIVILEGED;
    CallGraphEdgeType reflInvokeEnum = CallGraphEdgeType.REFL_INVOKE;
    CallGraphEdgeType newinstanceEnum = CallGraphEdgeType.NEWINSTANCE;
    CallGraphEdgeType refConstrNewinstanceEnum = CallGraphEdgeType.REFL_CONSTR_NEWINSTANCE;
    CallGraphEdgeType refClassNewinstanceEnum = CallGraphEdgeType.REFL_CLASS_NEWINSTANCE;

    assertFalse(invalidEnum.isAsyncTask());
    assertFalse(staticEnum.isAsyncTask());
    assertFalse(virtualEnum.isAsyncTask());
    assertFalse(interfaceEnum.isAsyncTask());
    assertFalse(specialEnum.isAsyncTask());
    assertFalse(dynamicEnum.isAsyncTask());
    assertFalse(clinitEnum.isAsyncTask());
    assertFalse(genericFakeEnum.isAsyncTask());
    assertFalse(threadEnum.isAsyncTask());
    assertFalse(executorEnum.isAsyncTask());
    assertTrue(asynctaskEnum.isAsyncTask());
    assertFalse(finalizeEnum.isAsyncTask());
    assertFalse(handlerEnum.isAsyncTask());
    assertFalse(invokeFinalizeEnum.isAsyncTask());
    assertFalse(privilegedEnum.isAsyncTask());
    assertFalse(reflInvokeEnum.isAsyncTask());
    assertFalse(newinstanceEnum.isAsyncTask());
    assertFalse(refConstrNewinstanceEnum.isAsyncTask());
    assertFalse(refClassNewinstanceEnum.isAsyncTask());

    assertFalse(invalidEnum.isClinit());
    assertFalse(staticEnum.isClinit());
    assertFalse(virtualEnum.isClinit());
    assertFalse(interfaceEnum.isClinit());
    assertFalse(specialEnum.isClinit());
    assertFalse(dynamicEnum.isClinit());
    assertTrue(clinitEnum.isClinit());
    assertFalse(genericFakeEnum.isClinit());
    assertFalse(threadEnum.isClinit());
    assertFalse(executorEnum.isClinit());
    assertFalse(asynctaskEnum.isClinit());
    assertFalse(finalizeEnum.isClinit());
    assertFalse(handlerEnum.isClinit());
    assertFalse(invokeFinalizeEnum.isClinit());
    assertFalse(privilegedEnum.isClinit());
    assertFalse(reflInvokeEnum.isClinit());
    assertFalse(newinstanceEnum.isClinit());
    assertFalse(refConstrNewinstanceEnum.isClinit());
    assertFalse(refClassNewinstanceEnum.isClinit());

    assertFalse(invalidEnum.isDynamic());
    assertFalse(staticEnum.isDynamic());
    assertFalse(virtualEnum.isDynamic());
    assertFalse(interfaceEnum.isDynamic());
    assertFalse(specialEnum.isDynamic());
    assertTrue(dynamicEnum.isDynamic());
    assertFalse(clinitEnum.isDynamic());
    assertFalse(genericFakeEnum.isDynamic());
    assertFalse(threadEnum.isDynamic());
    assertFalse(executorEnum.isDynamic());
    assertFalse(asynctaskEnum.isDynamic());
    assertFalse(finalizeEnum.isDynamic());
    assertFalse(handlerEnum.isDynamic());
    assertFalse(invokeFinalizeEnum.isDynamic());
    assertFalse(privilegedEnum.isDynamic());
    assertFalse(reflInvokeEnum.isDynamic());
    assertFalse(newinstanceEnum.isDynamic());
    assertFalse(refConstrNewinstanceEnum.isDynamic());
    assertFalse(refClassNewinstanceEnum.isDynamic());

    assertFalse(invalidEnum.isFake());
    assertFalse(staticEnum.isFake());
    assertFalse(virtualEnum.isFake());
    assertFalse(interfaceEnum.isFake());
    assertFalse(specialEnum.isFake());
    assertFalse(dynamicEnum.isFake());
    assertFalse(clinitEnum.isFake());
    assertTrue(genericFakeEnum.isFake());
    assertTrue(threadEnum.isFake());
    assertTrue(executorEnum.isFake());
    assertTrue(asynctaskEnum.isFake());
    assertFalse(finalizeEnum.isFake());
    assertTrue(handlerEnum.isFake());
    assertFalse(invokeFinalizeEnum.isFake());
    assertTrue(privilegedEnum.isFake());
    assertFalse(reflInvokeEnum.isFake());
    assertFalse(newinstanceEnum.isFake());
    assertFalse(refConstrNewinstanceEnum.isFake());
    assertFalse(refClassNewinstanceEnum.isFake());

    assertFalse(invalidEnum.isExecutor());
    assertFalse(staticEnum.isExecutor());
    assertFalse(virtualEnum.isExecutor());
    assertFalse(interfaceEnum.isExecutor());
    assertFalse(specialEnum.isExecutor());
    assertFalse(dynamicEnum.isExecutor());
    assertFalse(clinitEnum.isExecutor());
    assertFalse(genericFakeEnum.isExecutor());
    assertFalse(threadEnum.isExecutor());
    assertTrue(executorEnum.isExecutor());
    assertFalse(asynctaskEnum.isExecutor());
    assertFalse(finalizeEnum.isExecutor());
    assertFalse(handlerEnum.isExecutor());
    assertFalse(invokeFinalizeEnum.isExecutor());
    assertFalse(privilegedEnum.isExecutor());
    assertFalse(reflInvokeEnum.isExecutor());
    assertFalse(newinstanceEnum.isExecutor());
    assertFalse(refConstrNewinstanceEnum.isExecutor());
    assertFalse(refClassNewinstanceEnum.isExecutor());

    assertFalse(invalidEnum.passesParameters());
    assertTrue(staticEnum.passesParameters());
    assertTrue(virtualEnum.passesParameters());
    assertTrue(interfaceEnum.passesParameters());
    assertTrue(specialEnum.passesParameters());
    assertFalse(dynamicEnum.passesParameters());
    assertFalse(clinitEnum.passesParameters());
    assertFalse(genericFakeEnum.passesParameters());
    assertTrue(threadEnum.passesParameters());
    assertTrue(executorEnum.passesParameters());
    assertTrue(asynctaskEnum.passesParameters());
    assertTrue(finalizeEnum.passesParameters());
    assertFalse(handlerEnum.passesParameters());
    assertTrue(invokeFinalizeEnum.passesParameters());
    assertTrue(privilegedEnum.passesParameters());
    assertTrue(reflInvokeEnum.passesParameters());
    assertTrue(newinstanceEnum.passesParameters());
    assertTrue(refConstrNewinstanceEnum.passesParameters());
    assertTrue(refClassNewinstanceEnum.passesParameters());

    assertFalse(invalidEnum.isExplicit());
    assertTrue(staticEnum.isExplicit());
    assertTrue(virtualEnum.isExplicit());
    assertTrue(interfaceEnum.isExplicit());
    assertTrue(specialEnum.isExplicit());
    assertFalse(dynamicEnum.isExplicit());
    assertFalse(clinitEnum.isExplicit());
    assertFalse(genericFakeEnum.isExplicit());
    assertFalse(threadEnum.isExplicit());
    assertFalse(executorEnum.isExplicit());
    assertFalse(asynctaskEnum.isExplicit());
    assertFalse(finalizeEnum.isExplicit());
    assertFalse(handlerEnum.isExplicit());
    assertFalse(invokeFinalizeEnum.isExplicit());
    assertFalse(privilegedEnum.isExplicit());
    assertFalse(reflInvokeEnum.isExplicit());
    assertFalse(newinstanceEnum.isExplicit());
    assertFalse(refConstrNewinstanceEnum.isExplicit());
    assertFalse(refClassNewinstanceEnum.isExplicit());

    assertFalse(invalidEnum.isInstance());
    assertFalse(staticEnum.isInstance());
    assertTrue(virtualEnum.isInstance());
    assertTrue(interfaceEnum.isInstance());
    assertTrue(specialEnum.isInstance());
    assertFalse(dynamicEnum.isInstance());
    assertFalse(clinitEnum.isInstance());
    assertFalse(genericFakeEnum.isInstance());
    assertFalse(threadEnum.isInstance());
    assertFalse(executorEnum.isInstance());
    assertFalse(asynctaskEnum.isInstance());
    assertFalse(finalizeEnum.isInstance());
    assertFalse(handlerEnum.isInstance());
    assertFalse(invokeFinalizeEnum.isInstance());
    assertFalse(privilegedEnum.isInstance());
    assertFalse(reflInvokeEnum.isInstance());
    assertFalse(newinstanceEnum.isInstance());
    assertFalse(refConstrNewinstanceEnum.isInstance());
    assertFalse(refClassNewinstanceEnum.isInstance());

    assertFalse(invalidEnum.isVirtual());
    assertFalse(staticEnum.isVirtual());
    assertTrue(virtualEnum.isVirtual());
    assertFalse(interfaceEnum.isVirtual());
    assertFalse(specialEnum.isVirtual());
    assertFalse(dynamicEnum.isVirtual());
    assertFalse(clinitEnum.isVirtual());
    assertFalse(genericFakeEnum.isVirtual());
    assertFalse(threadEnum.isVirtual());
    assertFalse(executorEnum.isVirtual());
    assertFalse(asynctaskEnum.isVirtual());
    assertFalse(finalizeEnum.isVirtual());
    assertFalse(handlerEnum.isVirtual());
    assertFalse(invokeFinalizeEnum.isVirtual());
    assertFalse(privilegedEnum.isVirtual());
    assertFalse(reflInvokeEnum.isVirtual());
    assertFalse(newinstanceEnum.isVirtual());
    assertFalse(refConstrNewinstanceEnum.isVirtual());
    assertFalse(refClassNewinstanceEnum.isVirtual());

    assertFalse(invalidEnum.isSpecial());
    assertFalse(staticEnum.isSpecial());
    assertFalse(virtualEnum.isSpecial());
    assertFalse(interfaceEnum.isSpecial());
    assertTrue(specialEnum.isSpecial());
    assertFalse(dynamicEnum.isSpecial());
    assertFalse(clinitEnum.isSpecial());
    assertFalse(genericFakeEnum.isSpecial());
    assertFalse(threadEnum.isSpecial());
    assertFalse(executorEnum.isSpecial());
    assertFalse(asynctaskEnum.isSpecial());
    assertFalse(finalizeEnum.isSpecial());
    assertFalse(handlerEnum.isSpecial());
    assertFalse(invokeFinalizeEnum.isSpecial());
    assertFalse(privilegedEnum.isSpecial());
    assertFalse(reflInvokeEnum.isSpecial());
    assertFalse(newinstanceEnum.isSpecial());
    assertFalse(refConstrNewinstanceEnum.isSpecial());
    assertFalse(refClassNewinstanceEnum.isSpecial());

    assertFalse(invalidEnum.isStatic());
    assertTrue(staticEnum.isStatic());
    assertFalse(virtualEnum.isStatic());
    assertFalse(interfaceEnum.isStatic());
    assertFalse(specialEnum.isStatic());
    assertFalse(dynamicEnum.isStatic());
    assertFalse(clinitEnum.isStatic());
    assertFalse(genericFakeEnum.isStatic());
    assertFalse(threadEnum.isStatic());
    assertFalse(executorEnum.isStatic());
    assertFalse(asynctaskEnum.isStatic());
    assertFalse(finalizeEnum.isStatic());
    assertFalse(handlerEnum.isStatic());
    assertFalse(invokeFinalizeEnum.isStatic());
    assertFalse(privilegedEnum.isStatic());
    assertFalse(reflInvokeEnum.isStatic());
    assertFalse(newinstanceEnum.isStatic());
    assertFalse(refConstrNewinstanceEnum.isStatic());
    assertFalse(refClassNewinstanceEnum.isStatic());

    assertFalse(invalidEnum.isThread());
    assertFalse(staticEnum.isThread());
    assertFalse(virtualEnum.isThread());
    assertFalse(interfaceEnum.isThread());
    assertFalse(specialEnum.isThread());
    assertFalse(dynamicEnum.isThread());
    assertFalse(clinitEnum.isThread());
    assertFalse(genericFakeEnum.isThread());
    assertTrue(threadEnum.isThread());
    assertFalse(executorEnum.isThread());
    assertFalse(asynctaskEnum.isThread());
    assertFalse(finalizeEnum.isThread());
    assertFalse(handlerEnum.isThread());
    assertFalse(invokeFinalizeEnum.isThread());
    assertFalse(privilegedEnum.isThread());
    assertFalse(reflInvokeEnum.isThread());
    assertFalse(newinstanceEnum.isThread());
    assertFalse(refConstrNewinstanceEnum.isThread());
    assertFalse(refClassNewinstanceEnum.isThread());

    assertFalse(invalidEnum.isPrivileged());
    assertFalse(staticEnum.isPrivileged());
    assertFalse(virtualEnum.isPrivileged());
    assertFalse(interfaceEnum.isPrivileged());
    assertFalse(specialEnum.isPrivileged());
    assertFalse(dynamicEnum.isPrivileged());
    assertFalse(clinitEnum.isPrivileged());
    assertFalse(genericFakeEnum.isPrivileged());
    assertFalse(threadEnum.isPrivileged());
    assertFalse(executorEnum.isPrivileged());
    assertFalse(asynctaskEnum.isPrivileged());
    assertFalse(finalizeEnum.isPrivileged());
    assertFalse(handlerEnum.isPrivileged());
    assertFalse(invokeFinalizeEnum.isPrivileged());
    assertTrue(privilegedEnum.isPrivileged());
    assertFalse(reflInvokeEnum.isPrivileged());
    assertFalse(newinstanceEnum.isPrivileged());
    assertFalse(refConstrNewinstanceEnum.isPrivileged());
    assertFalse(refClassNewinstanceEnum.isPrivileged());

    assertFalse(invalidEnum.isReflection());
    assertFalse(staticEnum.isReflection());
    assertFalse(virtualEnum.isReflection());
    assertFalse(interfaceEnum.isReflection());
    assertFalse(specialEnum.isReflection());
    assertFalse(dynamicEnum.isReflection());
    assertFalse(clinitEnum.isReflection());
    assertFalse(genericFakeEnum.isReflection());
    assertFalse(threadEnum.isReflection());
    assertFalse(executorEnum.isReflection());
    assertFalse(asynctaskEnum.isReflection());
    assertFalse(finalizeEnum.isReflection());
    assertFalse(handlerEnum.isReflection());
    assertFalse(invokeFinalizeEnum.isReflection());
    assertFalse(privilegedEnum.isReflection());
    assertTrue(reflInvokeEnum.isReflection());
    assertFalse(newinstanceEnum.isReflection());
    assertTrue(refConstrNewinstanceEnum.isReflection());
    assertTrue(refClassNewinstanceEnum.isReflection());

    assertFalse(invalidEnum.isReflInvoke());
    assertFalse(staticEnum.isReflInvoke());
    assertFalse(virtualEnum.isReflInvoke());
    assertFalse(interfaceEnum.isReflInvoke());
    assertFalse(specialEnum.isReflInvoke());
    assertFalse(dynamicEnum.isReflInvoke());
    assertFalse(clinitEnum.isReflInvoke());
    assertFalse(genericFakeEnum.isReflInvoke());
    assertFalse(threadEnum.isReflInvoke());
    assertFalse(executorEnum.isReflInvoke());
    assertFalse(asynctaskEnum.isReflInvoke());
    assertFalse(finalizeEnum.isReflInvoke());
    assertFalse(handlerEnum.isReflInvoke());
    assertFalse(invokeFinalizeEnum.isReflInvoke());
    assertFalse(privilegedEnum.isReflInvoke());
    assertTrue(reflInvokeEnum.isReflInvoke());
    assertFalse(newinstanceEnum.isReflInvoke());
    assertFalse(refConstrNewinstanceEnum.isReflInvoke());
    assertFalse(refClassNewinstanceEnum.isReflInvoke());
  }
}
