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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;

/** The built-in implicit call models */
final class ImplicitCalls {

  private ImplicitCalls() {}

  static final ImplicitCallModel THREAD_START =
      new FactoryRules(factory -> List.of(threadStart(factory, false)));

  static final ImplicitCallModel JDK = new FactoryRules(ImplicitCalls::jdkRules);

  /**
   * A model whose rules are created with the identifier factory of the view, so that their
   * signatures match the signatures of the analyzed code
   */
  private static final class FactoryRules implements ImplicitCallModel {

    private final Function<IdentifierFactory, List<ImplicitCall>> rules;
    private final Map<IdentifierFactory, ImplicitCallModel> models = new ConcurrentHashMap<>();

    FactoryRules(Function<IdentifierFactory, List<ImplicitCall>> rules) {
      this.rules = rules;
    }

    @Override
    @NonNull
    public List<AbstractInstanceInvokeExpr> resolve(
        @NonNull ImplicitCallContext context, @NonNull Body body, @NonNull InvokableStmt stmt) {
      return models
          .computeIfAbsent(
              context.view().getIdentifierFactory(),
              factory -> new RuleBasedImplicitCallModel(rules.apply(factory)))
          .resolve(context, body, stmt);
    }
  }

  /**
   * {@code Thread.start()} calls {@code run()} on the thread and, if requested, on the {@link
   * Runnable} passed to the thread's constructor
   */
  private static ImplicitCall threadStart(
      IdentifierFactory factory, boolean withConstructorRunnable) {
    ClassType thread = factory.getClassType("java.lang.Thread");
    MethodSubSignature run = factory.parseMethodSubSignature("void run()");
    List<ImplicitCallTarget> targets = new ArrayList<>();
    targets.add(new ImplicitCallTarget.Direct(ImplicitCallTarget.RECEIVER, run));
    if (withConstructorRunnable) {
      String[][] constructors = {
        {"java.lang.Runnable", "0"},
        {"java.lang.Runnable,java.lang.String", "0"},
        {"java.lang.ThreadGroup,java.lang.Runnable", "1"},
        {"java.lang.ThreadGroup,java.lang.Runnable,java.lang.String", "1"},
        {"java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long", "1"}
      };
      for (String[] constructor : constructors) {
        targets.add(
            new ImplicitCallTarget.Indirect(
                ImplicitCallTarget.RECEIVER,
                factory.parseMethodSubSignature("void <init>(" + constructor[0] + ")"),
                List.of(new ImplicitCallTarget.Direct(Integer.parseInt(constructor[1]), run))));
      }
    }
    return new ImplicitCall(
        factory.getMethodSignature(thread, factory.parseMethodSubSignature("void start()")),
        false,
        targets);
  }

  private static List<ImplicitCall> jdkRules(IdentifierFactory factory) {
    MethodSubSignature run = factory.parseMethodSubSignature("void run()");
    List<ImplicitCall> rules = new ArrayList<>();
    rules.add(threadStart(factory, true));
    rules.add(
        new ImplicitCall(
            factory.parseMethodSignature(
                "<java.util.concurrent.Executor: void execute(java.lang.Runnable)>"),
            false,
            List.of(new ImplicitCallTarget.Direct(0, run))));
    for (String action :
        new String[] {
          "java.security.PrivilegedAction", "java.security.PrivilegedExceptionAction"
        }) {
      MethodSubSignature actionRun = factory.parseMethodSubSignature("java.lang.Object run()");
      for (String suffix : new String[] {"", ",java.security.AccessControlContext"}) {
        rules.add(
            new ImplicitCall(
                factory.parseMethodSignature(
                    "<java.security.AccessController: java.lang.Object doPrivileged("
                        + action
                        + suffix
                        + ")>"),
                true,
                List.of(new ImplicitCallTarget.Direct(0, actionRun))));
      }
    }
    return rules;
  }
}
