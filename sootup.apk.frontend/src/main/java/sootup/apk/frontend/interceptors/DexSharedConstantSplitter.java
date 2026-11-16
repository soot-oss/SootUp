package sootup.apk.frontend.interceptors;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2026 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.views.View;

/**
 * Gives each statement that uses a shared constant its own copy of the constant, like Soot's
 * dexpler does with its {@code SharedInitializationLocalSplitter}.
 *
 * <p>Dex constants have no type: {@code const/4 v0, 0} can be the int 0, {@code false}, the float
 * 0.0 or {@code null}, and the verifier checks each use on its own, so one register can feed uses
 * of different types:
 *
 * <pre>
 * l = 0;
 * useInt(l);
 * useObject(l);
 * </pre>
 *
 * The number and null transformers decide per local, so one of the uses would get a constant of the
 * wrong type. After this interceptor, each using statement has its own local:
 *
 * <pre>
 * l = 0;
 * useInt(l);
 * l#c1 = 0;
 * useObject(l#c1);
 * </pre>
 *
 * <p>It handles locals with a single definition that assigns an int or long constant, so it runs
 * after a {@code LocalSplitter} and before the dex transformers. The copy is assigned right before
 * the statement that uses it.
 */
public class DexSharedConstantSplitter implements BodyInterceptor {

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    List<Stmt> stmts = builder.getStmts();

    // the single definition of each local that is defined once, if it assigns an int or long
    Map<Local, JAssignStmt> constantDefs = new HashMap<>();
    Set<Local> definedMoreThanOnce = new HashSet<>();
    for (Stmt stmt : stmts) {
      if (!(stmt instanceof AbstractDefinitionStmt)
          || !(((AbstractDefinitionStmt) stmt).getLeftOp() instanceof Local)) {
        continue;
      }
      Local local = (Local) ((AbstractDefinitionStmt) stmt).getLeftOp();
      if (definedMoreThanOnce.contains(local)) {
        continue;
      }
      if (constantDefs.containsKey(local)) {
        constantDefs.remove(local);
        definedMoreThanOnce.add(local);
      } else if (stmt instanceof JAssignStmt && isDexConstant(((JAssignStmt) stmt).getRightOp())) {
        constantDefs.put(local, (JAssignStmt) stmt);
      } else {
        definedMoreThanOnce.add(local);
      }
    }
    if (constantDefs.isEmpty()) {
      return;
    }

    // the statements that use each of these locals, in the order of the statements
    Map<Local, List<Stmt>> usingStmts = new LinkedHashMap<>();
    for (Stmt stmt : stmts) {
      for (Value use : stmt.getUses()) {
        if (use instanceof Local && constantDefs.containsKey(use)) {
          List<Stmt> users = usingStmts.computeIfAbsent((Local) use, l -> new ArrayList<>());
          if (users.isEmpty() || users.get(users.size() - 1) != stmt) {
            users.add(stmt);
          }
        }
      }
    }

    // a statement that uses two shared constants is replaced twice
    Map<Stmt, Stmt> replaced = new HashMap<>();
    Set<Local> locals = new HashSet<>(builder.getLocals());
    Set<String> names = new HashSet<>();
    locals.forEach(l -> names.add(l.getName()));
    for (Map.Entry<Local, List<Stmt>> entry : usingStmts.entrySet()) {
      Local local = entry.getKey();
      List<Stmt> users = entry.getValue();
      JAssignStmt def = constantDefs.get(local);
      // the first use keeps the local
      for (int i = 1; i < users.size(); i++) {
        Stmt user = replaced.getOrDefault(users.get(i), users.get(i));
        Local copy = local.withName(freshName(local.getName(), names));
        locals.add(copy);
        graph.insertBefore(
            user, Jimple.newAssignStmt(copy, def.getRightOp(), def.getPositionInfo()));
        Stmt newUser = user.withNewUse(local, copy);
        graph.replaceNode(user, newUser);
        replaced.put(users.get(i), newUser);
      }
    }
    builder.setLocals(locals);
  }

  private static boolean isDexConstant(Value value) {
    return value instanceof IntConstant || value instanceof LongConstant;
  }

  private static String freshName(String name, Set<String> names) {
    for (int i = 1; ; i++) {
      String candidate = name + "#c" + i;
      if (names.add(candidate)) {
        return candidate;
      }
    }
  }
}
