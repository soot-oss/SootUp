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
import java.util.stream.Collectors;
import qilin.core.builder.FakeMainFactory;
import qilin.core.builder.callgraph.OnFlyCallGraph;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public class PTAScene {
  private final View view;
  private OnFlyCallGraph callgraph;
  private final FakeMainFactory fakeMainFactory;

  public final Set<SootMethod> nativeBuilt;
  public final Set<SootMethod> reflectionBuilt;
  public final Set<SootMethod> arraycopyBuilt;

  public PTAScene(View view, String mainClassSig) {
    this.nativeBuilt = DataFactory.createSet();
    this.reflectionBuilt = DataFactory.createSet();
    this.arraycopyBuilt = DataFactory.createSet();
    this.view = view;
    SootClass mainClass = getSootClass(mainClassSig);
    // setup fakemain
    this.fakeMainFactory = new FakeMainFactory(view, mainClass);
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
   *  wrapper methods of Soot Scene. Note, we do not allow you to use Soot Scene directly in qilin.qilin.pta subproject
   * to avoid confusing.
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

  public SootMethod getMethod(String methodSignature) {
    MethodSignature mthdSig = view.getIdentifierFactory().parseMethodSignature(methodSignature);
    return view.getMethod(mthdSig).get();
  }

  public Collection<SootClass> getApplicationClasses() {
    return view.getClasses().filter(SootClass::isApplicationClass).collect(Collectors.toSet());
  }

  public Collection<SootClass> getLibraryClasses() {
    return view.getClasses().filter(SootClass::isLibraryClass).collect(Collectors.toSet());
  }

  public boolean containsMethod(String methodSignature) {
    MethodSignature mthdSig = view.getIdentifierFactory().parseMethodSignature(methodSignature);
    return view.getMethod(mthdSig).isPresent();
  }

  public boolean containsField(String fieldSignature) {
    FieldSignature fieldSig = view.getIdentifierFactory().parseFieldSignature(fieldSignature);
    return view.getField(fieldSig).isPresent();
  }

  public Collection<? extends SootClass> getClasses() {
    return view.getClasses().collect(Collectors.toList());
  }

  public Collection<SootClass> getPhantomClasses() {
    return Collections.emptySet();
  }

  public SootClass getSootClass(String className) {
    ClassType classType = PTAUtils.getClassType(className);
    return view.getClass(classType).get();
  }

  public boolean containsClass(String className) {
    ClassType classType = PTAUtils.getClassType(className);
    Optional<? extends SootClass> oclazz = view.getClass(classType);
    return oclazz.isPresent();
  }

  public SootField getField(String fieldSignature) {
    FieldSignature fieldSig = view.getIdentifierFactory().parseFieldSignature(fieldSignature);
    return view.getField(fieldSig).get();
  }

  public boolean isApplicationMethod(SootMethod sm) {
    ClassType classType = sm.getDeclaringClassType();
    Optional<? extends SootClass> osc = view.getClass(classType);
    return osc.map(SootClass::isApplicationClass).orElse(false);
  }
}
