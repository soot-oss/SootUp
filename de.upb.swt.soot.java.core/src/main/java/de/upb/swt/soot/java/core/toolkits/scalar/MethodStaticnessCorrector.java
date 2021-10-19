package de.upb.swt.soot.java.core.toolkits.scalar;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JStaticInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.Type;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Transformer that checks whether an instance method is used like a static method, and can easily be made static, i.e., does
 * not reference any field or method in the "this" object. In this case, we make the method static, so that it complies with
 * the invocations.
 *
 * Attention: This is not really a body transformer. It checks the current body, but modifies the invocation target.
 *
 * @author Steven Arzt
 */
public class MethodStaticnessCorrector extends AbstractStaticnessCorrector {
  private static final Logger logger = LoggerFactory.getLogger(MethodStaticnessCorrector.class);

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder bodyBuilder) {
    for (Stmt stmt : bodyBuilder.getStmts()) {
        if (stmt.containsInvokeExpr()) {
          AbstractInvokeExpr iexpr = stmt.getInvokeExpr();
          if (iexpr instanceof JStaticInvokeExpr) {
            Type type = iexpr.getMethodSignature().getType();
            if (isClassLoaded(type.getClass())) {
              SootMethod target = Scene.v().grabMethod(type.getSignature());
              if (target != null && !target.isStatic()) {
                if (canBeMadeStatic(target)) {
                  // Remove the this-assignment to prevent
                  // 'this-assignment in a static method!' exception
                  Body targetBody = target.getActiveBody();
                  targetBody.getUnits().remove(targetBody.getThisUnit());
                  target.setModifiers(target.getModifiers() | Modifier.STATIC);
                  logger.warn(target.getName() + " changed into a static method");
                }
              }
            }
          }
      }
    }
  }

  /**
   * Checks whether the given method can be made static, i.e., does not reference the "this" object
   *
   * @param target
   *          The method to check
   * @return True if the given method can be made static, otherwise false
   */
  private boolean canBeMadeStatic(SootMethod target) {
    if (!target.hasActiveBody()) {
      return false;
    }
    Body body = target.getActiveBody();
    Value thisLocal = body.getThisLocal();
    for (Unit u : body.getUnits()) {
      for (ValueBox vb : u.getUseBoxes()) {
        if (vb.getValue() == thisLocal) {
          return false;
        }
      }
    }
    return true;
  }

}
