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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.layout.AndroidLayoutParser;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.ManifestComponent;
import sootup.apk.frontend.resources.AndroidResourceTableParser;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Derives call-graph entry points from {@code android:onClick} method names extracted by {@link
 * AndroidLayoutParser} (step 4): for every method name found in a layout, checks whether a
 * manifest-declared {@code Activity} (or an app-defined class in its superclass chain) defines the
 * matching {@code void <name>(android.view.View)} callback the framework would invoke via
 * reflection once such a layout is inflated.
 *
 * <p>Scoped to activities only, since {@code android:onClick} is resolved against the Context that
 * inflates the layout, which for a manifest-declared component is always an Activity (a Service,
 * BroadcastReceiver or ContentProvider never inflates a view hierarchy).
 *
 * <h2>Precise resolution, with a sound fallback</h2>
 *
 * A layout file's {@code android:onClick} names are only checked against the specific Activity(s)
 * that actually inflate it, resolved by tracing that Activity's own {@code setContentView(int)}
 * call site(s) back to a resource ID (a bounded, single-class constant scan — see {@link
 * #resolveOwnLayoutResourceIds}) and then to a file name via {@link AndroidResourceTableParser}.
 * This can fail for any number of reasons (no {@code resources.arsc}, an
 * unresolvable/dynamically-computed resource ID, a layout inflated some other way — {@code
 * LayoutInflater#inflate}, a Fragment, {@code setContentView(View)} — none of which are traced): if
 * <em>any</em> part of that chain doesn't resolve for a given Activity, that Activity falls back to
 * the old behavior of being checked against every {@code android:onClick} name found anywhere in
 * the APK, not just the ones from a layout it's confirmed to use. This keeps the overall result
 * sound — precision is opportunistic, never at the cost of missing a real entry point — the same
 * way steps 3 and 7 stay sound by over-approximating whatever they can't precisely resolve.
 */
public final class AndroidLayoutEntryPointCreator {

  private static final String ON_CLICK_RETURN_TYPE = "void";
  private static final List<String> ON_CLICK_PARAMETER_TYPES =
      Collections.singletonList("android.view.View");
  private static final String SET_CONTENT_VIEW = "setContentView";
  private static final List<String> SET_CONTENT_VIEW_PARAMETER_TYPES =
      Collections.singletonList("int");

  private AndroidLayoutEntryPointCreator() {}

  /**
   * @param onClickMethodNamesByLayoutFile every {@code android:onClick} method name found, keyed by
   *     the layout zip entry name it was declared in — see {@link
   *     AndroidLayoutParser#parseOnClickMethodNamesByFileFromApk}.
   * @param layoutFileNamesByResourceId {@code layout}-type resource IDs resolved to the zip entry
   *     name(s) they point at — see {@link
   *     AndroidResourceTableParser#parseFileNamesByResourceIdFromApk}. Pass an empty map (e.g. no
   *     {@code resources.arsc} could be parsed) to fall back to the old blanket behavior for every
   *     activity.
   * @param appClassNames the fully qualified names of classes actually declared in the APK's dex
   *     (see {@link sootup.apk.frontend.ApkAnalysisInputLocation#getApplicationClassNames()}).
   */
  @NonNull
  public static List<MethodSignature> getOnClickEntryPoints(
      @NonNull View view,
      @NonNull AndroidManifest manifest,
      @NonNull Set<String> appClassNames,
      @NonNull Map<String, Set<String>> onClickMethodNamesByLayoutFile,
      @NonNull Map<Integer, Set<String>> layoutFileNamesByResourceId) {
    if (onClickMethodNamesByLayoutFile.isEmpty()) {
      return Collections.emptyList();
    }

    Set<String> allOnClickMethodNames = new LinkedHashSet<>();
    for (Set<String> namesInFile : onClickMethodNamesByLayoutFile.values()) {
      allOnClickMethodNames.addAll(namesInFile);
    }

    Set<MethodSignature> entryPoints = new LinkedHashSet<>();
    IdentifierFactory identifierFactory = view.getIdentifierFactory();

    for (ManifestComponent activity : manifest.getComponents(AndroidComponentType.ACTIVITY)) {
      ClassType activityType = identifierFactory.getClassType(activity.getClassName());
      Set<String> onClickMethodNames =
          resolveOnClickNamesForActivity(
                  view, activityType, onClickMethodNamesByLayoutFile, layoutFileNamesByResourceId)
              .orElse(allOnClickMethodNames);

      for (String onClickMethodName : onClickMethodNames) {
        LifecycleMethod onClickMethod =
            new LifecycleMethod(onClickMethodName, ON_CLICK_RETURN_TYPE, ON_CLICK_PARAMETER_TYPES);
        AndroidEntryPointCreator.resolveOverride(
                view, identifierFactory, appClassNames, activityType, onClickMethod)
            .ifPresent(entryPoints::add);
      }
    }

    return new ArrayList<>(entryPoints);
  }

  /**
   * The precise per-activity {@code android:onClick} names, or empty if any part of the resolution
   * chain (own-class {@code setContentView(int)} call sites → resource ID → file name) didn't fully
   * resolve — signaling the caller to fall back to the blanket set instead.
   */
  @NonNull
  private static Optional<Set<String>> resolveOnClickNamesForActivity(
      @NonNull View view,
      @NonNull ClassType activityType,
      @NonNull Map<String, Set<String>> onClickMethodNamesByLayoutFile,
      @NonNull Map<Integer, Set<String>> layoutFileNamesByResourceId) {
    Optional<Set<Integer>> resourceIds = resolveOwnLayoutResourceIds(view, activityType);
    if (!resourceIds.isPresent()) {
      return Optional.empty();
    }

    Set<String> onClickMethodNames = new LinkedHashSet<>();
    for (int resourceId : resourceIds.get()) {
      Set<String> fileNames = layoutFileNamesByResourceId.get(resourceId);
      if (fileNames == null) {
        // A resource ID we traced doesn't appear in the parsed resource table at all (no
        // resources.arsc, an aliased/non-layout resource, or an ID resources.arsc and the dex
        // disagree on - see AndroidResourceTableParser's class doc) - can't vouch for
        // completeness anymore, so the whole activity falls back rather than silently
        // undercounting.
        return Optional.empty();
      }
      for (String fileName : fileNames) {
        onClickMethodNames.addAll(
            onClickMethodNamesByLayoutFile.getOrDefault(fileName, Collections.emptySet()));
      }
    }
    return Optional.of(onClickMethodNames);
  }

  /**
   * Scans every method declared directly on {@code activityType}'s own class (not its superclass
   * chain - {@code setContentView} is called by the concrete Activity itself, not inherited
   * boilerplate, so unlike lifecycle-method resolution there's no shared-base-class case to walk up
   * for) for {@code setContentView(int)} call sites, and traces each one's single argument back to
   * a constant resource ID.
   *
   * <p>Matches by method name and parameter shape only (not declaring class): real dex bytecode
   * encodes a {@code this.setContentView(id)} call site's invoke target against {@code this}'s
   * static type (the concrete Activity subclass), not necessarily the class that actually defines
   * the method (almost never overridden) - confirmed directly against this module's own checked-in
   * sample APKs, e.g. {@code virtualinvoke this.<de.ecspride.LocationLeak1: void
   * setContentView(int)>(...)} even though {@code LocationLeak1} never overrides it.
   *
   * <p>Returns empty (signaling the caller to fall back) if the class isn't in the view, no {@code
   * setContentView(int)} call site is found at all, or <em>any</em> call site's argument can't be
   * traced to a constant - partial resolution isn't good enough to drop the sound fallback for this
   * activity (see the class doc).
   */
  @NonNull
  private static Optional<Set<Integer>> resolveOwnLayoutResourceIds(
      @NonNull View view, @NonNull ClassType activityType) {
    Optional<? extends SootClass> sootClass = view.getClass(activityType);
    if (!sootClass.isPresent()) {
      return Optional.empty();
    }

    Set<Integer> resourceIds = new LinkedHashSet<>();
    boolean foundAnyCallSite = false;
    for (SootMethod method : sootClass.get().getMethods()) {
      if (!method.hasBody()) {
        continue;
      }
      Map<Local, Integer> intConstantByLocal = collectIntConstantLocals(method);
      for (Stmt stmt : method.getBody().getStmts()) {
        if (!stmt.isInvokableStmt()) {
          continue;
        }
        Optional<AbstractInvokeExpr> exprOpt = stmt.asInvokableStmt().getInvokeExpr();
        if (!exprOpt.isPresent() || !isSetContentViewWithIntArg(exprOpt.get())) {
          continue;
        }
        foundAnyCallSite = true;
        Optional<Integer> resourceId =
            resolveIntConstant(exprOpt.get().getArg(0), intConstantByLocal);
        if (!resourceId.isPresent()) {
          return Optional.empty();
        }
        resourceIds.add(resourceId.get());
      }
    }
    return foundAnyCallSite ? Optional.of(resourceIds) : Optional.empty();
  }

  private static boolean isSetContentViewWithIntArg(@NonNull AbstractInvokeExpr expr) {
    MethodSignature invoked = expr.getMethodSignature();
    return SET_CONTENT_VIEW.equals(invoked.getName())
        && invoked.getParameterTypes().size() == 1
        && SET_CONTENT_VIEW_PARAMETER_TYPES
            .get(0)
            .equals(invoked.getParameterTypes().get(0).toString());
  }

  @NonNull
  private static Optional<Integer> resolveIntConstant(
      @NonNull Immediate value, @NonNull Map<Local, Integer> intConstantByLocal) {
    if (value instanceof IntConstant) {
      return Optional.of(((IntConstant) value).getValue());
    }
    if (value instanceof Local && intConstantByLocal.containsKey(value)) {
      return Optional.of(intConstantByLocal.get(value));
    }
    return Optional.empty();
  }

  /**
   * Builds a {@code Local -> int constant} map for a method body, resolving through {@code (Type)
   * local}/{@code (Type) constant} cast chains, not just direct {@code local = 123} assignments:
   * real dex-derived Jimple for a literal passed to {@code setContentView(int)} showed up as {@code
   * $u1 = (Object) 2130903040; local = (int) $u1} (an autoboxing-shaped double cast) rather than a
   * single direct assignment, confirmed against this module's own checked-in sample APKs. A small
   * fixed-point loop over the method's assignments (bounded by statement count, cheap for a single
   * method body) resolves such chains regardless of how many hops or what statement order they
   * appear in.
   */
  @NonNull
  private static Map<Local, Integer> collectIntConstantLocals(@NonNull SootMethod method) {
    Map<Local, Integer> intConstantByLocal = new HashMap<>();
    boolean changed = true;
    while (changed) {
      changed = false;
      for (Stmt stmt : method.getBody().getStmts()) {
        if (!(stmt instanceof JAssignStmt)) {
          continue;
        }
        JAssignStmt assignStmt = (JAssignStmt) stmt;
        if (!(assignStmt.getLeftOp() instanceof Local)) {
          continue;
        }
        Local left = (Local) assignStmt.getLeftOp();
        if (intConstantByLocal.containsKey(left)) {
          continue;
        }
        Optional<Integer> resolved = resolveAssignedIntConstant(assignStmt, intConstantByLocal);
        if (resolved.isPresent()) {
          intConstantByLocal.put(left, resolved.get());
          changed = true;
        }
      }
    }
    return intConstantByLocal;
  }

  @NonNull
  private static Optional<Integer> resolveAssignedIntConstant(
      @NonNull JAssignStmt assignStmt, @NonNull Map<Local, Integer> intConstantByLocal) {
    Object rightOp = assignStmt.getRightOp();
    if (rightOp instanceof IntConstant) {
      return Optional.of(((IntConstant) rightOp).getValue());
    }
    if (rightOp instanceof JCastExpr) {
      Immediate castOperand = ((JCastExpr) rightOp).getOp();
      return resolveIntConstant(castOperand, intConstantByLocal);
    }
    return Optional.empty();
  }
}
