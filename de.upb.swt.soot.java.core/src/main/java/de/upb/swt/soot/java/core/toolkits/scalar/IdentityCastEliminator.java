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

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Transformer that removes unnecessary identity casts such as
 * 
 * $i3 = (int) $i3
 * 
 * when $i3 is already of type "int".
 * 
 * @author Steven Arzt
 */
public class IdentityCastEliminator implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder bodyBuilder) {
    for (Iterator<Unit> unitIt = bodyBuilder.getUnits().iterator(); unitIt.hasNext();) {
      Unit curUnit = unitIt.next();
      if (curUnit instanceof AssignStmt) {
        final AssignStmt assignStmt = (AssignStmt) curUnit;
        final Value leftOp = assignStmt.getLeftOp();
        final Value rightOp = assignStmt.getRightOp();
        if (leftOp instanceof Local && rightOp instanceof CastExpr) {
          final CastExpr ce = (CastExpr) rightOp;
          final Value castOp = ce.getOp();

          // If this a cast such as a = (X) a, we can remove the whole line.
          // Otherwise, if only the types match, we can replace the typecast
          // with a normal assignment.
          if (castOp.getType() == ce.getCastType()) {
            if (leftOp == castOp) {
              unitIt.remove();
            } else {
              assignStmt.setRightOp(castOp);
            }
          }
        }
      }
    }
  }

 }
