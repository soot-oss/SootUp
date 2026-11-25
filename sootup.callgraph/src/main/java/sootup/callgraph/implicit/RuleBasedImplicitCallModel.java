package sootup.callgraph.implicit;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Hazem Gamal and others
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * An {@link ImplicitCallModel} defined by {@link ImplicitCall} rules. Instance rules match virtual
 * and interface calls whose referenced class is the declaring class of the rule's source or a
 * subtype of it; static rules match calls of exactly their source.
 */
public class RuleBasedImplicitCallModel implements ImplicitCallModel {

  private final Map<MethodSubSignature, List<ImplicitCall>> instanceRules = new HashMap<>();
  private final Map<MethodSignature, List<ImplicitCall>> staticRules = new HashMap<>();

  public RuleBasedImplicitCallModel(@NonNull Collection<ImplicitCall> rules) {
    for (ImplicitCall rule : rules) {
      if (rule.staticSource()) {
        staticRules.computeIfAbsent(rule.source(), k -> new ArrayList<>()).add(rule);
      } else {
        instanceRules
            .computeIfAbsent(rule.source().getSubSignature(), k -> new ArrayList<>())
            .add(rule);
      }
    }
  }

  @Override
  @NonNull
  public List<AbstractInstanceInvokeExpr> resolve(
      @NonNull View view, @NonNull Body body, @NonNull InvokableStmt stmt) {
    Optional<AbstractInvokeExpr> invokeExpr = stmt.getInvokeExpr();
    if (!invokeExpr.isPresent()) {
      return List.of();
    }
    List<ImplicitCall> rules = matchingRules(view, invokeExpr.get());
    if (rules.isEmpty()) {
      return List.of();
    }
    List<AbstractInstanceInvokeExpr> invokes = new ArrayList<>();
    for (ImplicitCall rule : rules) {
      for (ImplicitCallTarget target : rule.targets()) {
        if (target instanceof ImplicitCallTarget.Direct direct) {
          addInvoke(view, invokeExpr.get(), invokeExpr.get(), direct, invokes);
        } else if (target instanceof ImplicitCallTarget.Indirect indirect) {
          Local value = valueAt(invokeExpr.get(), indirect.position());
          if (value == null) {
            continue;
          }
          for (AbstractInvokeExpr earlierCall : callsOn(body, value, indirect.callSubSignature())) {
            for (ImplicitCallTarget.Direct direct : indirect.targets()) {
              addInvoke(view, invokeExpr.get(), earlierCall, direct, invokes);
            }
          }
        }
      }
    }
    return invokes;
  }

  private List<ImplicitCall> matchingRules(View view, AbstractInvokeExpr invokeExpr) {
    MethodSignature invoked = invokeExpr.getMethodSignature();
    if (invokeExpr instanceof JStaticInvokeExpr) {
      return staticRules.getOrDefault(invoked, List.of());
    }
    if (!(invokeExpr instanceof AbstractInstanceInvokeExpr)
        || invokeExpr instanceof JSpecialInvokeExpr) {
      return List.of();
    }
    List<ImplicitCall> candidates = instanceRules.get(invoked.getSubSignature());
    if (candidates == null) {
      return List.of();
    }
    TypeHierarchy hierarchy = view.getTypeHierarchy();
    ClassType referenced = invoked.getDeclClassType();
    List<ImplicitCall> rules = new ArrayList<>();
    for (ImplicitCall rule : candidates) {
      ClassType declaring = rule.source().getDeclClassType();
      if (declaring.equals(referenced)
          || (hierarchy.contains(referenced) && hierarchy.isSubtype(declaring, referenced))) {
        rules.add(rule);
      }
    }
    return rules;
  }

  /**
   * Adds the invocation of the given target. The receiver is taken from the owner call of the
   * target, the arguments from the source call.
   */
  private static void addInvoke(
      View view,
      AbstractInvokeExpr sourceCall,
      AbstractInvokeExpr ownerCall,
      ImplicitCallTarget.Direct target,
      List<AbstractInstanceInvokeExpr> invokes) {
    Local receiver = valueAt(ownerCall, target.position());
    if (receiver == null) {
      return;
    }
    ClassType declaringClass = target.declaringClass();
    if (declaringClass == null) {
      if (!(receiver.getType() instanceof ClassType receiverType)) {
        return;
      }
      declaringClass = receiverType;
    }
    MethodSignature method =
        view.getIdentifierFactory().getMethodSignature(declaringClass, target.subSignature());

    int parameterCount = target.subSignature().getParameterTypes().size();
    List<Immediate> args = new ArrayList<>(parameterCount);
    for (int i = 0; i < parameterCount; i++) {
      args.add(NullConstant.getInstance());
    }
    target
        .parameterMapping()
        .forEach(
            (argIndex, paramIndex) -> {
              if (argIndex < sourceCall.getArgCount() && paramIndex < parameterCount) {
                args.set(paramIndex, sourceCall.getArg(argIndex));
              }
            });

    boolean isInterface = view.getClass(declaringClass).map(SootClass::isInterface).orElse(false);
    invokes.add(
        isInterface
            ? new JInterfaceInvokeExpr(receiver, method, args)
            : new JVirtualInvokeExpr(receiver, method, args));
  }

  /** Returns the local at the given position of the call, or null if it is not a local */
  @Nullable
  private static Local valueAt(AbstractInvokeExpr call, int position) {
    Object value;
    if (position == ImplicitCallTarget.RECEIVER) {
      if (!(call instanceof AbstractInstanceInvokeExpr instanceCall)) {
        return null;
      }
      value = instanceCall.getBase();
    } else if (position >= 0 && position < call.getArgCount()) {
      value = call.getArg(position);
    } else {
      return null;
    }
    return value instanceof Local local ? local : null;
  }

  /**
   * Returns the calls with the given subsignature on the given local in the body, or on a local
   * that it is copied from or to
   */
  private static List<AbstractInvokeExpr> callsOn(
      Body body, Local local, MethodSubSignature subSignature) {
    Set<Local> copies = copiesOf(body, local);
    List<AbstractInvokeExpr> calls = new ArrayList<>();
    for (Stmt stmt : body.getStmts()) {
      if (!stmt.isInvokableStmt()) {
        continue;
      }
      stmt.asInvokableStmt()
          .getInvokeExpr()
          .filter(e -> e instanceof AbstractInstanceInvokeExpr)
          .map(e -> (AbstractInstanceInvokeExpr) e)
          .filter(e -> copies.contains(e.getBase()))
          .filter(e -> e.getMethodSignature().getSubSignature().equals(subSignature))
          .ifPresent(calls::add);
    }
    return calls;
  }

  /** Returns the locals that are connected to the given local by copy assignments */
  private static Set<Local> copiesOf(Body body, Local local) {
    Set<Local> copies = new HashSet<>();
    copies.add(local);
    boolean changed = true;
    while (changed) {
      changed = false;
      for (Stmt stmt : body.getStmts()) {
        if (stmt instanceof JAssignStmt assign
            && assign.getLeftOp() instanceof Local left
            && assign.getRightOp() instanceof Local right
            && copies.contains(left) != copies.contains(right)) {
          copies.add(left);
          copies.add(right);
          changed = true;
        }
      }
    }
    return copies;
  }
}
