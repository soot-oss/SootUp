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
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * An {@link ImplicitCallModel} defined by {@link ImplicitCall} rules.
 *
 * <p>A call with the subsignature of a rule's source fires the rule if the analysis would not see
 * the implicit call otherwise. The call is resolved with the {@link ImplicitCallContext}'s method
 * lookup:
 *
 * <ul>
 *   <li>If it resolves to the source, the rule fires: the source is modeled, e.g. {@code
 *       Thread.start()}, whose body calls {@code run()} only through the virtual machine. This also
 *       holds for {@code super.start()} and for a static source called through a subclass.
 *   <li>If it resolves to another instance method and the class that the call names is a subtype of
 *       the source's class, the method implements the source for that class, e.g. the {@code
 *       execute(Runnable)} of an {@link java.util.concurrent.Executor}. The rule fires only if the
 *       analysis does not follow calls into that method; otherwise its body shows what it does.
 *   <li>If it does not resolve, the rule fires if the class that the call names is not in the view,
 *       or is a subtype of the source's class.
 * </ul>
 *
 * <p>An implicit invocation names the class in which its method is looked up: the target's {@link
 * ImplicitCallTarget.Direct#declaringClass()}, or else the type that the call gives the value, i.e.
 * the parameter type for an argument and the class that the call names for the receiver. The static
 * type of the value's local replaces it if it is a subtype, so that a local typed more precisely
 * narrows the dispatch, while a local typed more widely, e.g. {@code Object} for a value passed to
 * an interface parameter, does not lose the call.
 */
public class RuleBasedImplicitCallModel implements ImplicitCallModel {

  private final Map<MethodSubSignature, List<ImplicitCall>> instanceRules = new HashMap<>();
  private final Map<MethodSubSignature, List<ImplicitCall>> staticRules = new HashMap<>();

  public RuleBasedImplicitCallModel(@NonNull Collection<ImplicitCall> rules) {
    for (ImplicitCall rule : rules) {
      (rule.staticSource() ? staticRules : instanceRules)
          .computeIfAbsent(rule.source().getSubSignature(), k -> new ArrayList<>())
          .add(rule);
    }
  }

  @Override
  @NonNull
  public List<AbstractInstanceInvokeExpr> resolve(
      @NonNull ImplicitCallContext context, @NonNull Body body, @NonNull InvokableStmt stmt) {
    Optional<AbstractInvokeExpr> invokeExpr = stmt.getInvokeExpr();
    if (!invokeExpr.isPresent()) {
      return List.of();
    }
    List<ImplicitCall> rules = matchingRules(context, invokeExpr.get());
    if (rules.isEmpty()) {
      return List.of();
    }
    List<AbstractInstanceInvokeExpr> invokes = new ArrayList<>();
    for (ImplicitCall rule : rules) {
      for (ImplicitCallTarget target : rule.targets()) {
        if (target instanceof ImplicitCallTarget.Direct direct) {
          addInvoke(context, invokeExpr.get(), invokeExpr.get(), direct, invokes);
        } else if (target instanceof ImplicitCallTarget.Indirect indirect) {
          Local value = valueAt(invokeExpr.get(), indirect.position());
          if (value == null) {
            continue;
          }
          for (AbstractInvokeExpr earlierCall : callsOn(body, value, indirect.callSubSignature())) {
            for (ImplicitCallTarget.Direct direct : indirect.targets()) {
              addInvoke(context, invokeExpr.get(), earlierCall, direct, invokes);
            }
          }
        }
      }
    }
    return invokes;
  }

  private List<ImplicitCall> matchingRules(
      ImplicitCallContext context, AbstractInvokeExpr invokeExpr) {
    boolean staticCall = invokeExpr instanceof JStaticInvokeExpr;
    if (!staticCall && !(invokeExpr instanceof AbstractInstanceInvokeExpr)) {
      return List.of();
    }
    MethodSignature invoked = invokeExpr.getMethodSignature();
    List<ImplicitCall> candidates =
        (staticCall ? staticRules : instanceRules).get(invoked.getSubSignature());
    if (candidates == null) {
      return List.of();
    }
    Optional<SootMethod> resolved = context.methodLookup().find(invoked);
    List<ImplicitCall> rules = new ArrayList<>();
    for (ImplicitCall rule : candidates) {
      if (fires(context, rule, invoked, resolved)) {
        rules.add(rule);
      }
    }
    return rules;
  }

  /**
   * Whether a call of {@code invoked}, which resolves to {@code resolved}, fires the rule, see the
   * class documentation
   */
  private static boolean fires(
      ImplicitCallContext context,
      ImplicitCall rule,
      MethodSignature invoked,
      Optional<SootMethod> resolved) {
    MethodSignature source = rule.source();
    if (resolved.isPresent()) {
      SootMethod method = resolved.get();
      if (method.getSignature().equals(source)) {
        return true;
      }
      // a static method that hides the source is a method of its own
      return !rule.staticSource()
          && !method.isStatic()
          && namesSubtypeOf(context, invoked.getDeclClassType(), source.getDeclClassType())
          && !context.isAnalyzed(method);
    }
    if (rule.staticSource()) {
      return invoked.equals(source);
    }
    return !context.view().getTypeHierarchy().contains(invoked.getDeclClassType())
        || namesSubtypeOf(context, invoked.getDeclClassType(), source.getDeclClassType());
  }

  /**
   * Whether the class that a call names is the source's class or a subtype of it; also true if the
   * type hierarchy does not know the source's class
   */
  private static boolean namesSubtypeOf(
      ImplicitCallContext context, ClassType named, ClassType sourceClass) {
    TypeHierarchy hierarchy = context.view().getTypeHierarchy();
    return named.equals(sourceClass)
        || !hierarchy.contains(sourceClass)
        || (hierarchy.contains(named) && hierarchy.isSubtype(sourceClass, named));
  }

  /**
   * Adds the invocation of the given target. The receiver is taken from the owner call of the
   * target, the arguments from the source call.
   */
  private static void addInvoke(
      ImplicitCallContext context,
      AbstractInvokeExpr sourceCall,
      AbstractInvokeExpr ownerCall,
      ImplicitCallTarget.Direct target,
      List<AbstractInstanceInvokeExpr> invokes) {
    Local receiver = valueAt(ownerCall, target.position());
    if (receiver == null) {
      return;
    }
    ClassType declaringClass = lookupClass(context, ownerCall, receiver, target);
    if (declaringClass == null) {
      return;
    }
    View view = context.view();
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

  /**
   * The class in which the method of an implicit invocation on {@code value} is looked up: the
   * static type of the local if it is a subtype of the bound, else the bound. The bound is the
   * target's declaring class, or else the type that the owner call gives the value. Falls back to
   * the local's type if the method cannot be found in the bound. Null if there is no class type.
   */
  @Nullable
  private static ClassType lookupClass(
      ImplicitCallContext context,
      AbstractInvokeExpr ownerCall,
      Local value,
      ImplicitCallTarget.Direct target) {
    ClassType localType = value.getType() instanceof ClassType type ? type : null;
    ClassType bound = target.declaringClass();
    if (bound == null) {
      bound = typeAt(ownerCall, target.position());
    }
    if (bound == null) {
      return localType;
    }
    TypeHierarchy hierarchy = context.view().getTypeHierarchy();
    if (localType != null
        && (localType.equals(bound)
            || (hierarchy.contains(localType) && hierarchy.isSubtype(bound, localType)))) {
      return localType;
    }
    if (localType != null
        && context
            .methodLookup()
            .find(
                context
                    .view()
                    .getIdentifierFactory()
                    .getMethodSignature(bound, target.subSignature()))
            .isEmpty()) {
      return localType;
    }
    return bound;
  }

  /**
   * The type that a call gives the value at the given position: the class that it names for the
   * receiver, the parameter type for an argument; null if that is not a class type
   */
  @Nullable
  private static ClassType typeAt(AbstractInvokeExpr call, int position) {
    MethodSignature signature = call.getMethodSignature();
    if (position == ImplicitCallTarget.RECEIVER) {
      return signature.getDeclClassType();
    }
    if (position >= 0 && position < signature.getParameterTypes().size()) {
      return signature.getParameterTypes().get(position) instanceof ClassType type ? type : null;
    }
    return null;
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
