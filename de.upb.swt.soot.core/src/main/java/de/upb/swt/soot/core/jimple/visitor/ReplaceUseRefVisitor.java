package de.upb.swt.soot.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Zun Wang
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

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.JPhiExpr;
import de.upb.swt.soot.core.jimple.common.ref.*;
import javax.annotation.Nonnull;

/**
 * Replace old use of a Ref with a new use
 *
 * @author Zun Wang
 */
public class ReplaceUseRefVisitor extends AbstractRefVisitor {

  Value oldUse;
  Value newUse;
  Ref newRef;

  public ReplaceUseRefVisitor(@Nonnull Value oldUse, @Nonnull Value newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  @Nonnull
  @Override
  public void caseStaticFieldRef(@Nonnull JStaticFieldRef v) {
    defaultCase(v);
  }

  @Nonnull
  @Override
  public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef v) {
    if ((newUse instanceof Local || newUse instanceof JPhiExpr) &&  v.getBase().equivTo(oldUse)) {
      newRef = v.withBase(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseArrayRef(@Nonnull JArrayRef v) {
    if ((newUse instanceof Local || newUse instanceof JPhiExpr) && v.getBase().equivTo(oldUse) && newUse.getType().equals(oldUse.getType())) {
      newRef = v.withBase(newUse);
    } else if ((newUse instanceof Immediate || newUse instanceof JPhiExpr)&& v.getIndex().equivTo(oldUse)) {
      newRef = v.withIndex(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseParameterRef(@Nonnull JParameterRef v) {
    defaultCase(v);
  }

  @Nonnull
  @Override
  public void caseCaughtExceptionRef(@Nonnull JCaughtExceptionRef v) {
    defaultCase(v);
  }

  @Nonnull
  @Override
  public void caseThisRef(@Nonnull JThisRef v) {
    defaultCase(v);
  }

  @Nonnull
  @Override
  public void defaultCase(@Nonnull Object obj) {
    newRef = (Ref) obj;
  }

  @Nonnull
  public Ref getNewRef() {
    return newRef;
  }
}
