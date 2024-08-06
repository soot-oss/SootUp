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

package qilin.core.reflection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import qilin.core.PTAScene;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;

public abstract class ReflectionModel {
  protected final PTAScene ptaScene;
  protected final String sigForName =
      "<java.lang.Class: java.lang.Class forName(java.lang.String)>";
  protected final String sigForName2 =
      "<java.lang.Class: java.lang.Class forName(java.lang.String,boolean,java.lang.ClassLoader)>";
  protected final String sigClassNewInstance = "<java.lang.Class: java.lang.Object newInstance()>";
  protected final String sigConstructorNewInstance =
      "<java.lang.reflect.Constructor: java.lang.Object newInstance(java.lang.Object[])>";
  protected final String sigMethodInvoke =
      "<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>";
  protected final String sigFieldSet =
      "<java.lang.reflect.Field: void set(java.lang.Object,java.lang.Object)>";
  protected final String sigFieldGet =
      "<java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>";
  protected final String sigArrayNewInstance =
      "<java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>";
  protected final String sigArrayGet =
      "<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>";
  protected final String sigArraySet =
      "<java.lang.reflect.Array: void set(java.lang.Object,int,java.lang.Object)>";
  protected final String sigReifiedField =
      "<java.lang.Class: java.lang.reflect.Field getField(java.lang.String)>";
  protected final String sigReifiedDeclaredField =
      "<java.lang.Class: java.lang.reflect.Field getDeclaredField(java.lang.String)>";
  protected final String sigReifiedFieldArray =
      "<java.lang.Class: java.lang.reflect.Field[] getFields()>";
  protected final String sigReifiedDeclaredFieldArray =
      "<java.lang.Class: java.lang.reflect.Field[] getDeclaredFields()>";
  protected final String sigReifiedMethod =
      "<java.lang.Class: java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])>";
  protected final String sigReifiedDeclaredMethod =
      "<java.lang.Class: java.lang.reflect.Method getDeclaredMethod(java.lang.String,java.lang.Class[])>";
  protected final String sigReifiedMethodArray =
      "<java.lang.Class: java.lang.reflect.Method[] getMethods()>";
  protected final String sigReifiedDeclaredMethodArray =
      "<java.lang.Class: java.lang.reflect.Method[] getDeclaredMethods()>";

  protected ReflectionModel(PTAScene ptaScene) {
    this.ptaScene = ptaScene;
  }

  private Collection<Stmt> transform(InvokableStmt s) {
    AbstractInvokeExpr ie = s.getInvokeExpr().get();
    switch (ie.getMethodSignature().toString()) {
      case sigForName:
      case sigForName2:
        return transformClassForName(s);
      case sigClassNewInstance:
        return transformClassNewInstance(s);
      case sigConstructorNewInstance:
        return transformConstructorNewInstance(s);
      case sigMethodInvoke:
        return transformMethodInvoke(s);
      case sigFieldSet:
        return transformFieldSet(s);
      case sigFieldGet:
        return transformFieldGet(s);
      case sigArrayNewInstance:
        return transformArrayNewInstance(s);
      case sigArrayGet:
        return transformArrayGet(s);
      case sigArraySet:
        return transformArraySet(s);
      default:
        return Collections.emptySet();
    }
  }

  /** replace reflection call with appropriate statements */
  public void buildReflection(SootMethod m) {
    if (!ptaScene.reflectionBuilt.add(m)) {
      return;
    }
    Map<Stmt, Collection<Stmt>> newUnits = DataFactory.createMap();
    Body body = PTAUtils.getMethodBody(m);
    List<Stmt> units = body.getStmts();
    for (final Stmt u : units) {
      if (u.isInvokableStmt() && u.asInvokableStmt().containsInvokeExpr()) {
        newUnits.put(u, transform(u.asInvokableStmt()));
      }
    }
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    for (Stmt unit : newUnits.keySet()) {
      for (Stmt succ : newUnits.get(unit)) {
        if (succ instanceof JAssignStmt) {
          JAssignStmt assign = (JAssignStmt) succ;
          stmtGraph.insertBefore(unit, assign);
        } else if (succ instanceof JInvokeStmt) {
          JInvokeStmt invoke = (JInvokeStmt) succ;
          stmtGraph.insertBefore(unit, invoke);
        } else {
          System.out.println("unit:" + unit);
          System.out.println("succ:" + succ.getClass());
          stmtGraph.putEdge((FallsThroughStmt) unit, succ);
        }
      }
    }
    PTAUtils.updateMethodBody(m, builder.build());
  }

  abstract Collection<Stmt> transformClassForName(InvokableStmt s);

  abstract Collection<Stmt> transformClassNewInstance(InvokableStmt s);

  abstract Collection<Stmt> transformConstructorNewInstance(InvokableStmt s);

  abstract Collection<Stmt> transformMethodInvoke(InvokableStmt s);

  abstract Collection<Stmt> transformFieldSet(InvokableStmt s);

  abstract Collection<Stmt> transformFieldGet(InvokableStmt s);

  abstract Collection<Stmt> transformArrayNewInstance(InvokableStmt s);

  abstract Collection<Stmt> transformArrayGet(InvokableStmt s);

  abstract Collection<Stmt> transformArraySet(InvokableStmt s);
}
