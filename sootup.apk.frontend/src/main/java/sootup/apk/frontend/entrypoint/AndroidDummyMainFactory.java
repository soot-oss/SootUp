package sootup.apk.frontend.entrypoint;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.ClassModifier;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.types.JavaClassType;

/**
 * Builds a synthetic {@code dummyMain} method with a real Jimple {@link Body} that unconditionally
 * calls every entry point in a given list, the classic Soot/FlowDroid shape (see {@code
 * ANDROID_CALL_GRAPH_PLAN.md} step 5's "no synthesized dummy-main body" design note, which flagged
 * this as an additive follow-up for callers that need one — e.g. a control-flow-sensitive analysis
 * built on top of the call graph, or any downstream tool written against the classic
 * single-entry-point convention rather than {@code CallGraphAlgorithm.initialize(List)}'s flat
 * entry-point list).
 *
 * <p>Since {@code CallGraphAlgorithm.initialize} already treats every element of a flat entry-point
 * list as an independent root (see {@link AndroidEntryPointCreator}'s own class doc), the body built
 * here doesn't need to model the OS's unpredictable invocation order the way Soot's classic
 * dummy-main does (loops/conditionals around every call): for call-graph reachability, a
 * straight-line sequence that calls each entry point exactly once is equivalent. One receiver local
 * is allocated per distinct declaring class (via {@code new}, plus a {@code specialinvoke} of that
 * class's own {@code <init>} entry point if one is present in the list) and reused across every
 * other entry point declared on that same class, matching how the OS really does invoke lifecycle
 * callbacks — repeatedly, on the one instance it constructed.
 */
public final class AndroidDummyMainFactory {

  private AndroidDummyMainFactory() {}

  private static final String DUMMY_MAIN_CLASS_NAME = "sootup.apk.frontend.dummyMain.AndroidDummyMain";
  private static final String DUMMY_MAIN_METHOD_NAME = "dummyMain";

  /** The signature {@link #createDummyMainClass} always gives the synthetic method it builds. */
  @NonNull
  public static MethodSignature getDummyMainSignature(@NonNull IdentifierFactory identifierFactory) {
    return identifierFactory.getMethodSignature(
        DUMMY_MAIN_CLASS_NAME, DUMMY_MAIN_METHOD_NAME, "void", Collections.emptyList());
  }

  /**
   * Builds a synthetic class declaring exactly one static {@code void dummyMain()} method whose
   * body calls every method in {@code entryPoints} once. The caller is responsible for registering
   * the returned class with a {@code MutableJavaView} (via {@code addClass}) so the method becomes
   * resolvable through the view for call-graph construction.
   *
   * @param view resolved against to distinguish static/instance/interface methods (needed to choose
   *     the right Jimple invoke-expression kind for each entry point) and to build placeholder
   *     receivers of the right declared type.
   * @param entryPoints the combined entry-point list to call from the dummy main's body — e.g. the
   *     result of {@code AndroidApkAnalysis.getEntryPoints()}.
   */
  @NonNull
  public static JavaSootClass createDummyMainClass(
      @NonNull View view, @NonNull List<MethodSignature> entryPoints) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    MethodSignature dummyMainSignature = getDummyMainSignature(identifierFactory);

    Body body = buildDummyMainBody(view, dummyMainSignature, entryPoints);
    JavaSootMethod dummyMainMethod =
        new JavaSootMethod(
            new OverridingBodySource(dummyMainSignature, body),
            dummyMainSignature,
            EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            Collections.emptyList(),
            NoPositionInformation.getInstance());

    JavaClassType objectType = (JavaClassType) identifierFactory.getClassType("java.lang.Object");
    return new JavaSootClass(
        new OverridingJavaClassSource(
            Collections.singleton(dummyMainMethod),
            Collections.emptySet(),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.emptySet(),
            objectType,
            null,
            NoPositionInformation.getInstance(),
            null,
            identifierFactory.getClassType(DUMMY_MAIN_CLASS_NAME),
            new EagerInputLocation()),
        SourceType.Application);
  }

  @NonNull
  private static Body buildDummyMainBody(
      @NonNull View view,
      @NonNull MethodSignature dummyMainSignature,
      @NonNull List<MethodSignature> entryPoints) {
    Body.BodyBuilder bodyBuilder = Body.builder();
    List<Stmt> stmtList = new ArrayList<>();
    Map<ClassType, Local> receiverForClass = new LinkedHashMap<>();
    int[] nextLocalIndex = {0};

    for (MethodSignature entryPoint : entryPoints) {
      Optional<? extends SootMethod> resolved = view.getMethod(entryPoint);
      if (!resolved.isPresent()) {
        // The combined entry-point list can outlive the exact view it was computed against (e.g.
        // a caller merges lists from two analyses) - silently skip anything not actually
        // resolvable rather than fail the whole dummy main.
        continue;
      }
      SootMethod method = resolved.get();

      if (method.isStatic()) {
        List<Immediate> args = placeholderArgs(entryPoint.getParameterTypes());
        addInvokeStmt(stmtList, Jimple.newStaticInvokeExpr(entryPoint, args));
        continue;
      }

      ClassType declaringClass = entryPoint.getDeclClassType();
      Local receiver =
          receiverForClass.computeIfAbsent(
              declaringClass,
              type -> allocateReceiver(bodyBuilder, stmtList, type, nextLocalIndex));

      List<Immediate> args = placeholderArgs(entryPoint.getParameterTypes());
      if (entryPoint.getName().equals("<init>")) {
        addInvokeStmt(stmtList, Jimple.newSpecialInvokeExpr(receiver, entryPoint, args));
        continue;
      }

      boolean isInterfaceMethod =
          view.getClass(declaringClass).map(SootClass::isInterface).orElse(false);
      addInvokeStmt(
          stmtList,
          isInterfaceMethod
              ? Jimple.newInterfaceInvokeExpr(receiver, entryPoint, args)
              : Jimple.newVirtualInvokeExpr(receiver, entryPoint, args));
    }

    StmtPositionInfo noPosInfo = StmtPositionInfo.getNoStmtPositionInfo();
    Stmt returnVoidStmt = new JReturnVoidStmt(noPosInfo);
    MutableControlFlowGraph cfg = bodyBuilder.getControlFlowGraph();
    if (stmtList.isEmpty()) {
      cfg.addBlock(Collections.singletonList(returnVoidStmt));
      cfg.setStartingStmt(returnVoidStmt);
    } else {
      cfg.addBlock(stmtList);
      cfg.setStartingStmt(stmtList.get(0));
      cfg.putEdge((FallsThroughStmt) stmtList.get(stmtList.size() - 1), returnVoidStmt);
    }

    return bodyBuilder
        .setMethodSignature(dummyMainSignature)
        .setPosition(NoPositionInformation.getInstance())
        .build();
  }

  @NonNull
  private static Local allocateReceiver(
      Body.@NonNull BodyBuilder bodyBuilder,
      @NonNull List<Stmt> stmtList,
      @NonNull ClassType type,
      @NonNull int[] nextLocalIndex) {
    Local receiver = Jimple.newLocal("r" + nextLocalIndex[0]++, type);
    bodyBuilder.addLocal(receiver);
    addAssignStmt(stmtList, receiver, new JNewExpr(type));
    return receiver;
  }

  private static void addInvokeStmt(
      @NonNull List<Stmt> stmtList, @NonNull AbstractInvokeExpr invokeExpr) {
    stmtList.add(Jimple.newInvokeStmt(invokeExpr, StmtPositionInfo.getNoStmtPositionInfo()));
  }

  private static void addAssignStmt(
      @NonNull List<Stmt> stmtList, @NonNull LValue lValue, @NonNull Value rValue) {
    stmtList.add(Jimple.newAssignStmt(lValue, rValue, StmtPositionInfo.getNoStmtPositionInfo()));
  }

  /**
   * One placeholder {@link Immediate} per parameter type: {@code null} for reference/array types,
   * zero for primitives. The dummy main's body is never executed - only ever traversed statically
   * by call-graph construction - so the actual values are irrelevant; only well-typed arguments of
   * the right arity are needed for the invoke expression to be valid Jimple.
   */
  @NonNull
  private static List<Immediate> placeholderArgs(@NonNull List<Type> parameterTypes) {
    List<Immediate> args = new ArrayList<>(parameterTypes.size());
    for (Type type : parameterTypes) {
      args.add(placeholderValue(type));
    }
    return args;
  }

  @NonNull
  private static Immediate placeholderValue(@NonNull Type type) {
    if (type instanceof PrimitiveType.LongType) {
      return LongConstant.getInstance(0L);
    }
    if (type instanceof PrimitiveType.FloatType) {
      return FloatConstant.getInstance(0f);
    }
    if (type instanceof PrimitiveType.DoubleType) {
      return DoubleConstant.getInstance(0d);
    }
    if (type instanceof PrimitiveType.IntType) {
      // Covers int, byte, short, char and boolean - all represented as 0 at the Jimple level,
      // the same convention plain bytecode/Soot use.
      return IntConstant.getInstance(0);
    }
    return NullConstant.getInstance();
  }
}
