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


import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootField;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Transformer that checks whether a static field is used like an instance field. If this is the case, all instance
 * references are replaced by static field references.
 *
 * @author Steven Arzt
 */
public class FieldStaticnessCorrector extends AbstractStaticnessCorrector {


  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder bodyBuilder) {
    // Some apps reference static fields as instance fields. We need to fix
    // this for not breaking the client analysis.
    for (Unit u : bodyBuilder.getUnits()) {
      if (u instanceof AssignStmt) {
        AssignStmt assignStmt = (AssignStmt) u;
        if (assignStmt.containsFieldRef()) {
          FieldRef ref = assignStmt.getFieldRef();
          // Make sure that the target class has already been loaded
          if (isTypeLoaded(ref.getFieldRef().type())) {
            try {
              if (ref instanceof InstanceFieldRef) {
                SootField fld = ref.getField();
                if (fld != null && fld.isStatic()) {
                  if (assignStmt.getLeftOp() == ref) {
                    assignStmt.setLeftOp(Jimple.v().newStaticFieldRef(ref.getField().makeRef()));
                  } else if (assignStmt.getRightOp() == ref) {
                    assignStmt.setRightOp(Jimple.v().newStaticFieldRef(ref.getField().makeRef()));
                  }
                }
              }
            } catch (ConflictingFieldRefException ex) {
              // That field is broken, just don't touch it
            }
          }
        }
      }
    }
  }

}
