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

package qilin.core.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import qilin.core.builder.callgraph.Edge;
import qilin.core.pag.ContextMethod;
import sootup.callgraph.MethodLookup;
import sootup.callgraph.implicit.ImplicitCallContext;
import sootup.callgraph.implicit.ImplicitCallModel;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;

/**
 * The calls that methods perform implicitly, from an {@link ImplicitCallModel}. Each implicit
 * invocation is a synthetic statement that the solver dispatches like a virtual call on the
 * points-to set of its receiver; edges from it are attributed to the causing statement in the
 * exported call graph ({@link #callSiteOf}). A virtual or interface call asks the model again
 * whenever it gets a new target, since rules fire by the targets of the call.
 */
public class ImplicitCallSites {

  private final ImplicitCallModel model;
  private final ImplicitCallContext context;

  /** Registers a synthetic statement of a method (in a context) as a call site. */
  private BiConsumer<ContextMethod, InvokableStmt> register = (m, s) -> {};

  /** Causing call statement -> its state. */
  private final Map<InvokableStmt, Site> sites = new IdentityHashMap<>();

  /** Call statements without implicit invocations that cannot get any. */
  private final Set<InvokableStmt> none = Collections.newSetFromMap(new IdentityHashMap<>());

  /** Synthetic statement -> causing call statement. */
  private final Map<InvokableStmt, InvokableStmt> causes = new IdentityHashMap<>();

  private static final class Site {
    final Body body;
    final Set<ContextMethod> contexts = new LinkedHashSet<>();
    Set<MethodSignature> targets;

    /** Synthetic statements by the text of their invocation; invocations equal by identity only */
    final Map<String, InvokableStmt> invokes = new LinkedHashMap<>();

    Site(Body body) {
      this.body = body;
    }
  }

  public ImplicitCallSites(ImplicitCallModel model, MethodLookup methodLookup) {
    this.model = model;
    this.context =
        ImplicitCallContext.of(
            methodLookup,
            SootMethod::isConcrete,
            (caller, stmt) -> {
              Site site = sites.get(stmt);
              return Optional.of(site == null || site.targets == null ? Set.of() : site.targets);
            });
  }

  public void setRegister(BiConsumer<ContextMethod, InvokableStmt> register) {
    this.register = register;
  }

  /** The statement that causes {@code stmt}, or {@code stmt} if it is not synthetic. */
  public InvokableStmt callSiteOf(InvokableStmt stmt) {
    return causes.getOrDefault(stmt, stmt);
  }

  /** Registers the implicit invocations of the call statements of a newly reached method. */
  public void record(ContextMethod m, Body body, Iterable<InvokableStmt> stmts) {
    for (InvokableStmt stmt : stmts) {
      if (stmt.getInvokeExpr().isEmpty() || causes.containsKey(stmt) || none.contains(stmt)) {
        continue;
      }
      Site site = sites.get(stmt);
      if (site == null) {
        site = new Site(body);
        site.contexts.add(m);
        List<AbstractInstanceInvokeExpr> invokes = model.resolve(context, body, stmt);
        if (invokes.isEmpty() && !isVirtual(stmt)) {
          // only virtual calls get new targets that can fire a rule
          none.add(stmt);
          continue;
        }
        sites.put(stmt, site);
        addInvokes(stmt, site, invokes);
      } else if (site.contexts.add(m)) {
        for (InvokableStmt synthetic : site.invokes.values()) {
          register.accept(m, synthetic);
        }
      }
    }
  }

  /** Asks the model again when a virtual call gets a new target. */
  public void onEdge(Edge edge) {
    InvokableStmt stmt = edge.srcUnit();
    if (stmt == null) return;
    Site site = sites.get(stmt);
    if (site == null || !isVirtual(stmt)) return;
    if (site.targets == null) site.targets = new HashSet<>();
    if (site.targets.add(edge.tgt().getSignature())) {
      addInvokes(stmt, site, model.resolve(context, site.body, stmt));
    }
  }

  private void addInvokes(InvokableStmt stmt, Site site, List<AbstractInstanceInvokeExpr> invokes) {
    List<InvokableStmt> added = new ArrayList<>();
    for (AbstractInstanceInvokeExpr invoke : invokes) {
      site.invokes.computeIfAbsent(
          invoke.toString(),
          k -> {
            InvokableStmt synthetic = new JInvokeStmt(invoke, stmt.getPositionInfo());
            causes.put(synthetic, stmt);
            added.add(synthetic);
            return synthetic;
          });
    }
    for (InvokableStmt synthetic : added) {
      for (ContextMethod m : site.contexts) {
        register.accept(m, synthetic);
      }
    }
  }

  private static boolean isVirtual(InvokableStmt stmt) {
    AbstractInvokeExpr expr = stmt.getInvokeExpr().orElseThrow();
    return expr instanceof JVirtualInvokeExpr || expr instanceof JInterfaceInvokeExpr;
  }
}
