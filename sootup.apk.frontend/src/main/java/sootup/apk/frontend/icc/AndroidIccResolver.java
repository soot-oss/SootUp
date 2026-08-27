package sootup.apk.frontend.icc;

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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.entrypoint.AndroidEntryPointConstants;
import sootup.apk.frontend.entrypoint.AndroidEntryPointCreator;
import sootup.apk.frontend.entrypoint.LifecycleMethod;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.ManifestComponent;
import sootup.callgraph.MutableCallGraph;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Adds inter-component communication (ICC) edges to an already-built {@link MutableCallGraph}:
 * edges from an {@code Intent}-based call site ({@code startActivity}, {@code startService}, {@code
 * bindService}, {@code sendBroadcast}, ...) to the lifecycle callback of whichever
 * manifest-declared component the {@code Intent} targets.
 *
 * <p><b>Assumes the graph was already built from the combined entry points of steps 1/2/3/4/5</b>
 * (manifest lifecycle callbacks, discovered listeners, {@code android:onClick} targets). Under that
 * assumption, a valid ICC target's lifecycle method is always already a node in {@code cg} (step 5
 * unconditionally probes every manifest component's lifecycle callbacks, independent of any ICC
 * evidence), so this only needs to add the missing edge connecting a specific call site to that
 * already-expanded node — not re-run CHA/RTA or expand a fresh subtree. If a target isn't already
 * present (e.g. a caller only ran a subset of the earlier steps), the target is still added as a
 * bare node so the edge is well-formed, but its own body won't have been explored.
 *
 * <h2>How a target Intent is resolved</h2>
 *
 * For each {@code Intent} local reachable from an ICC call site's first argument, this looks
 * (within the same method body only — no interprocedural or points-to analysis) for:
 *
 * <ul>
 *   <li><b>Explicit</b>: a {@code new Intent(Context, Class)} constructor call, or a later {@code
 *       setClass(Context, Class)}/{@code setClassName(...)} call on the same local — resolved
 *       against the manifest components matching that exact class name.
 *   <li><b>Implicit</b>: {@code setAction(String)}/constructor-with-action-String calls on the same
 *       local — resolved against manifest components with a matching {@code <intent-filter>} action
 *       (category matching is not modeled; any action match is treated as a match,
 *       over-approximating the same way steps 3/4 do).
 * </ul>
 *
 * An Intent built across a conditional/loop, passed in as a parameter, returned from a helper
 * method, or targeted via {@code setComponent(ComponentName)} isn't resolved — these fall outside
 * this intentionally bounded, whole-method-body scan, and no edge is added for that call site
 * (sound but incomplete, consistent with the rest of this module).
 *
 * <p>Only manifest-declared components (dynamically-registered {@code BroadcastReceiver}s via
 * {@code registerReceiver(...)} are not modeled; see {@code ANDROID_CALL_GRAPH_PLAN.md}) and only
 * {@code startActivity}/{@code startService}/{@code bindService}/{@code sendBroadcast} and their
 * common overloads are handled; {@code ContentProvider} URI-based ICC is not modeled.
 */
public final class AndroidIccResolver {

  private static final String INTENT_CLASS = "android.content.Intent";

  private AndroidIccResolver() {}

  /**
   * Scans every method already in {@code cg} for ICC call sites and adds the resolved edges (and,
   * if missing, target method nodes) directly to {@code cg}.
   *
   * @param appClassNames the fully qualified names of classes actually declared in the APK's dex
   *     (see {@link sootup.apk.frontend.ApkAnalysisInputLocation#getApplicationClassNames()}).
   */
  public static void addIccEdges(
      @NonNull MutableCallGraph cg,
      @NonNull View view,
      @NonNull AndroidManifest manifest,
      @NonNull Set<String> appClassNames) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();

    // Snapshot: cg.getMethodSignatures() may be backed by a mutable collection that addMethod()
    // below would otherwise modify while this loop iterates it.
    for (MethodSignature sourceSig : new ArrayList<>(cg.getMethodSignatures())) {
      Optional<? extends SootMethod> sourceMethodOpt = view.getMethod(sourceSig);
      if (!sourceMethodOpt.isPresent() || !sourceMethodOpt.get().hasBody()) {
        continue;
      }
      SootMethod sourceMethod = sourceMethodOpt.get();
      Map<Local, IntentInfo> intentInfoByLocal = collectIntentInfo(sourceMethod);

      for (Stmt stmt : sourceMethod.getBody().getStmts()) {
        if (!stmt.isInvokableStmt()) {
          continue;
        }
        InvokableStmt invokableStmt = stmt.asInvokableStmt();
        Optional<AbstractInvokeExpr> exprOpt = invokableStmt.getInvokeExpr();
        if (!exprOpt.isPresent()) {
          continue;
        }
        AbstractInvokeExpr expr = exprOpt.get();
        IccCallType callType = classifyIccCall(expr.getMethodSignature());
        if (callType == null || expr.getArgCount() == 0) {
          continue;
        }
        Immediate intentArg = expr.getArg(0);
        if (!(intentArg instanceof Local)) {
          continue;
        }
        IntentInfo info = intentInfoByLocal.get(intentArg);
        if (info == null) {
          continue;
        }

        for (String targetClassName : resolveTargetClassNames(manifest, callType, info)) {
          ClassType targetClassType = identifierFactory.getClassType(targetClassName);
          for (LifecycleMethod targetLifecycleMethod : targetMethodsFor(callType)) {
            AndroidEntryPointCreator.resolveOverride(
                    view, identifierFactory, appClassNames, targetClassType, targetLifecycleMethod)
                .ifPresent(
                    target -> {
                      if (!cg.containsMethod(target)) {
                        cg.addMethod(target);
                      }
                      if (!cg.containsCall(sourceSig, target, invokableStmt)) {
                        cg.addCall(sourceSig, target, invokableStmt);
                      }
                    });
          }
        }
      }
    }
  }

  private enum IccCallType {
    ACTIVITY,
    SERVICE_START,
    SERVICE_BIND,
    BROADCAST
  }

  @Nullable
  private static IccCallType classifyIccCall(@NonNull MethodSignature invoked) {
    if (invoked.getParameterTypes().isEmpty()
        || !invoked.getParameterTypes().get(0).toString().equals(INTENT_CLASS)) {
      return null;
    }
    switch (invoked.getName()) {
      case "startActivity":
      case "startActivityForResult":
      case "startActivityFromChild":
      case "startActivityFromFragment":
        return IccCallType.ACTIVITY;
      case "startService":
      case "startForegroundService":
        return IccCallType.SERVICE_START;
      case "bindService":
        return IccCallType.SERVICE_BIND;
      case "sendBroadcast":
      case "sendOrderedBroadcast":
      case "sendStickyBroadcast":
      case "sendStickyOrderedBroadcast":
        return IccCallType.BROADCAST;
      default:
        return null;
    }
  }

  @NonNull
  private static AndroidComponentType componentTypeFor(@NonNull IccCallType callType) {
    switch (callType) {
      case ACTIVITY:
        return AndroidComponentType.ACTIVITY;
      case SERVICE_START:
      case SERVICE_BIND:
        return AndroidComponentType.SERVICE;
      case BROADCAST:
      default:
        return AndroidComponentType.BROADCAST_RECEIVER;
    }
  }

  @NonNull
  private static List<LifecycleMethod> targetMethodsFor(@NonNull IccCallType callType) {
    switch (callType) {
      case ACTIVITY:
        return optionalToList(
            AndroidEntryPointConstants.getLifecycleMethod(
                AndroidComponentType.ACTIVITY, "onCreate"));
      case SERVICE_START:
        return optionalsToList(
            AndroidEntryPointConstants.getLifecycleMethod(AndroidComponentType.SERVICE, "onCreate"),
            AndroidEntryPointConstants.getLifecycleMethod(
                AndroidComponentType.SERVICE, "onStartCommand"));
      case SERVICE_BIND:
        return optionalsToList(
            AndroidEntryPointConstants.getLifecycleMethod(AndroidComponentType.SERVICE, "onCreate"),
            AndroidEntryPointConstants.getLifecycleMethod(AndroidComponentType.SERVICE, "onBind"));
      case BROADCAST:
      default:
        return optionalToList(
            AndroidEntryPointConstants.getLifecycleMethod(
                AndroidComponentType.BROADCAST_RECEIVER, "onReceive"));
    }
  }

  private static List<LifecycleMethod> optionalToList(Optional<LifecycleMethod> m) {
    return m.isPresent() ? Collections.singletonList(m.get()) : Collections.emptyList();
  }

  @SafeVarargs
  private static List<LifecycleMethod> optionalsToList(Optional<LifecycleMethod>... methods) {
    List<LifecycleMethod> result = new ArrayList<>();
    for (Optional<LifecycleMethod> m : methods) {
      m.ifPresent(result::add);
    }
    return result;
  }

  @NonNull
  private static List<String> resolveTargetClassNames(
      @NonNull AndroidManifest manifest, @NonNull IccCallType callType, @NonNull IntentInfo info) {
    List<ManifestComponent> candidates = manifest.getComponents(componentTypeFor(callType));

    if (info.explicitClassName != null) {
      List<String> result = new ArrayList<>();
      for (ManifestComponent candidate : candidates) {
        if (candidate.getClassName().equals(info.explicitClassName)) {
          result.add(candidate.getClassName());
        }
      }
      return result;
    }

    if (!info.actions.isEmpty()) {
      List<String> result = new ArrayList<>();
      for (ManifestComponent candidate : candidates) {
        boolean actionMatches =
            candidate.getIntentFilters().stream()
                .anyMatch(filter -> !Collections.disjoint(filter.getActions(), info.actions));
        if (actionMatches) {
          result.add(candidate.getClassName());
        }
      }
      return result;
    }

    return Collections.emptyList();
  }

  /** Per-{@code Intent}-local targeting info collected from a single method body. */
  private static final class IntentInfo {
    @Nullable String explicitClassName;
    @NonNull final Set<String> actions = new LinkedHashSet<>();
  }

  /**
   * Single whole-body pass: seeds an {@link IntentInfo} for every {@code new Intent} assignment,
   * then folds every subsequent call on that same local (constructor, {@code setClass}, {@code
   * setClassName}, {@code setAction}) into it. Deliberately not backward-from-the-call-site or
   * control-flow-sensitive — see the class doc for what that trades away.
   */
  @NonNull
  private static Map<Local, IntentInfo> collectIntentInfo(@NonNull SootMethod method) {
    Map<Local, IntentInfo> infoByLocal = new HashMap<>();

    for (Stmt stmt : method.getBody().getStmts()) {
      if (stmt instanceof JAssignStmt) {
        Object rightOp = ((JAssignStmt) stmt).getRightOp();
        Object leftOp = ((JAssignStmt) stmt).getLeftOp();
        if (rightOp instanceof JNewExpr
            && ((JNewExpr) rightOp).getType().getFullyQualifiedName().equals(INTENT_CLASS)
            && leftOp instanceof Local) {
          infoByLocal.put((Local) leftOp, new IntentInfo());
        }
      }

      if (!stmt.isInvokableStmt()) {
        continue;
      }
      Optional<AbstractInvokeExpr> exprOpt = stmt.asInvokableStmt().getInvokeExpr();
      if (!exprOpt.isPresent() || !(exprOpt.get() instanceof AbstractInstanceInvokeExpr)) {
        continue;
      }
      AbstractInstanceInvokeExpr expr = (AbstractInstanceInvokeExpr) exprOpt.get();
      MethodSignature invoked = expr.getMethodSignature();
      if (!invoked.getDeclClassType().getFullyQualifiedName().equals(INTENT_CLASS)) {
        continue;
      }
      IntentInfo info = infoByLocal.get(expr.getBase());
      if (info == null) {
        continue;
      }
      applyIntentMethod(info, invoked.getName(), expr.getArgs());
    }

    return infoByLocal;
  }

  private static void applyIntentMethod(
      @NonNull IntentInfo info, @NonNull String methodName, @NonNull List<Immediate> args) {
    switch (methodName) {
      case "<init>":
        classArgClassName(args).ifPresent(name -> info.explicitClassName = name);
        firstArgIfString(args).ifPresent(info.actions::add);
        break;
      case "setClass":
        classArgClassName(args).ifPresent(name -> info.explicitClassName = name);
        break;
      case "setClassName":
        lastArgIfString(args).ifPresent(name -> info.explicitClassName = name);
        break;
      case "setAction":
        firstArgIfString(args).ifPresent(info.actions::add);
        break;
      default:
        break;
    }
  }

  @NonNull
  private static Optional<String> classArgClassName(@NonNull List<Immediate> args) {
    for (Immediate arg : args) {
      if (arg instanceof ClassConstant) {
        String raw = ((ClassConstant) arg).getValue();
        return Optional.of(DexUtil.isByteCodeClassName(raw) ? DexUtil.dottedClassName(raw) : raw);
      }
    }
    return Optional.empty();
  }

  @NonNull
  private static Optional<String> firstArgIfString(@NonNull List<Immediate> args) {
    return args.isEmpty() ? Optional.empty() : asString(args.get(0));
  }

  @NonNull
  private static Optional<String> lastArgIfString(@NonNull List<Immediate> args) {
    return args.isEmpty() ? Optional.empty() : asString(args.get(args.size() - 1));
  }

  @NonNull
  private static Optional<String> asString(@NonNull Immediate value) {
    return value instanceof StringConstant
        ? Optional.of(((StringConstant) value).getValue())
        : Optional.empty();
  }
}
