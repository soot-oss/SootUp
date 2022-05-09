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
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.views.View;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Transformer that checks whether a static field is used like an instance field. If this is the
 * case, all instance references are replaced by static field references.
 *
 * @author Steven Arzt
 */
public class FieldStaticnessCorrector implements BodyInterceptor {

  private View<SootClass> view;

  protected FieldStaticnessCorrector(View view) {
    this.view = view;
  }

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder bodyBuilder) {
    // Some apps reference static fields as instance fields. We need to fix
    // this for not breaking the client analysis.
    for (Stmt stmt : bodyBuilder.getStmts()) {
      if (stmt instanceof JAssignStmt) {
        JAssignStmt assignStmt = (JAssignStmt) stmt;
        if (assignStmt.containsFieldRef()) {
          JFieldRef ref = assignStmt.getFieldRef();
          // Make sure that the target class has already been loaded

          if (ref instanceof JInstanceFieldRef) {
            Optional<? extends SootField> field = view.getField(ref.getFieldSignature());
            if (field.isPresent()) {
              SootField fld = field.get();
              if (fld.isStatic()) {
                if (assignStmt.getLeftOp() == ref) {
                  assignStmt.withLeftOp(Jimple.newStaticFieldRef(fld.getSignature()));
                } else if (assignStmt.getRightOp() == ref) {
                  assignStmt.withRightOp(Jimple.newStaticFieldRef(fld.getSignature()));
                }
              }
            }
          }
        }
      }
    }
  }
}
