package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A BodyTransformer that removes all unused local variables from a given Body.
 *
 * @author Marcus Nachtigall
 */
public class UnusedLocalEliminator implements BodyInterceptor {

  /**
   * Removes unused local variables from the List of Stmts of the given {@link Body}. Complexity is
   * linear with respect to the statements.
   *
   * @param originalBody The current body before interception.
   * @return The transformed body.
   */
  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {

    Set<Local> locals = new HashSet<>();

    // Traverse statements copying all used uses and defs
    for (Stmt stmt : originalBody.getStmts()) {
      for (Value value : stmt.getUsesAndDefs()) {
        if (value instanceof Local) {
          Local local = (Local) value;
          locals.add(local);
        }
      }
    }

    return originalBody.withLocals(locals);
  }
}
