package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * A BodyTransformer that removes all unused local variables from a given Body. Implemented as a
 * singleton.
 *
 * @author Marcus Nachtigall
 */
public class UnusedLocalEliminator implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {

    Set<Local> originalLocals = originalBody.getLocals();
    Set<Local> locals = new HashSet<>();

    for (Stmt stmt : originalBody.getStmts()) {
      for (ValueBox valueBox : stmt.getUseBoxes()) {
        Value value = valueBox.getValue();
        if (value instanceof Local) {
          Local local = (Local) value;
          assert originalLocals.contains(local);
          locals.add(local);
        }
      }
      for (ValueBox valueBox : stmt.getDefBoxes()) {
        Value value = valueBox.getValue();
        if (value instanceof Local) {
          Local local = (Local) value;
          assert originalLocals.contains(local);
          locals.add(local);
        }
      }
    }

    return originalBody.withLocals(locals);
  }
}