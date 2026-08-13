/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import qilin.core.builder.FakeMainFactory;
import qilin.core.builder.callgraph.OnFlyCallGraph;
import qilin.core.config.PointerAnalysisConfig;
import qilin.util.CallDetails;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public class PTAScene {
  private final View view;
  private final ClassType mainClass;
  private final PointerAnalysisConfig config;
  private OnFlyCallGraph callgraph;
  private final FakeMainFactory fakeMainFactory;
  private final CallDetails callDetails;

  // Thread-safe: guard the corresponding effect model's one-time-per-method work
  // (PAG.getMethodPAG()'s effect-model dispatch and arraycopy handling), which is reached from
  // toolkit parallelStream() passes run after the main solve (e.g.
  // qilin.pta.toolkits.conch.AbstractPAG, qilin.pta.toolkits.debloaterx.XPAG), not just the
  // single-threaded Solver. add()'s atomicity is what makes "build it once" hold under that.
  public final Set<SootMethod> nativeBuilt;
  public final Set<SootMethod> reflectionBuilt;
  public final Set<SootMethod> arraycopyBuilt;
  public final Set<SootMethod> dynamicInvokeBuilt;

  public PTAScene(View view, ClassType mainClass, PointerAnalysisConfig config) {
    this.nativeBuilt = ConcurrentHashMap.newKeySet();
    this.reflectionBuilt = ConcurrentHashMap.newKeySet();
    this.arraycopyBuilt = ConcurrentHashMap.newKeySet();
    this.dynamicInvokeBuilt = ConcurrentHashMap.newKeySet();
    this.view = view;
    this.mainClass = mainClass;
    this.config = config;
    SootClass mainSootClass =
        view.getClass(mainClass)
            .orElseThrow(
                () -> new IllegalArgumentException("Main class not found in view: " + mainClass));
    // setup fakemain
    this.fakeMainFactory = new FakeMainFactory(view, mainSootClass, config);
    this.callDetails = new CallDetails();
  }

  public PointerAnalysisConfig getConfig() {
    return config;
  }

  public ClassType getMainClass() {
    return mainClass;
  }

  public FakeMainFactory getFakeMainFactory() {
    return fakeMainFactory;
  }

  public CallDetails getCallDetails() {
    return callDetails;
  }

  /*
   * wrapper methods for FakeMain.
   * */
  public SootMethod getFakeMainMethod() {
    return this.fakeMainFactory.getFakeMain();
  }

  public JStaticFieldRef getFieldCurrentThread() {
    return this.fakeMainFactory.getFieldCurrentThread();
  }

  public Value getFieldGlobalThrow() {
    return this.fakeMainFactory.getFieldGlobalThrow();
  }

  /*
   * getView() is the escape hatch to the underlying sootup View for everything View already does
   * directly (getClasses(), getMethod(...).isPresent(), ...). The methods below only exist because
   * they add something View doesn't: getMethod()/getSootClass() turn a raw Optional.get() into a
   * descriptive exception, and isApplicationMethod() is qilin-specific derived logic, not a plain
   * View lookup.
   * */
  public void setCallGraph(OnFlyCallGraph cg) {
    this.callgraph = cg;
  }

  public View getView() {
    return view;
  }

  public OnFlyCallGraph getCallGraph() {
    return this.callgraph;
  }

  public SootMethod getMethod(MethodSignature methodSignature) {
    return view.getMethod(methodSignature)
        .orElseThrow(
            () -> new IllegalArgumentException("Method not found in view: " + methodSignature));
  }

  public Collection<SootClass> getPhantomClasses() {
    return Collections.emptySet();
  }

  public SootClass getSootClass(ClassType classType) {
    return view.getClass(classType)
        .orElseThrow(() -> new IllegalArgumentException("Class not found in view: " + classType));
  }

  public boolean isApplicationMethod(SootMethod sm) {
    ClassType classType = sm.getDeclaringClassType();
    Optional<? extends SootClass> osc = view.getClass(classType);
    return osc.map(SootClass::isApplicationClass).orElse(false);
  }
}
