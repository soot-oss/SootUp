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
import de.upb.swt.soot.core.jimple.common.ref.*;
import javax.annotation.Nonnull;

/**
 * Replace old use of a Ref with a new use
 *
 * @author Zun Wang
 */
public class ReplaceUseRefVisitor extends AbstractRefVisitor<Ref> {

  private Immediate oldUse;
  private Immediate newUse;

  public ReplaceUseRefVisitor() {}

  public void init(@Nonnull Immediate oldUse, @Nonnull Immediate newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  @Override
  public void caseStaticFieldRef(@Nonnull JStaticFieldRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef ref) {
    if (ref.getBase() == oldUse) {
      setResult(ref.withBase((Local) newUse));
    } else {
      throw new IllegalArgumentException(
          "given oldUse is not a current use of JInstancFieldRef " + ref + "!");
    }
  }

  @Override
  public void caseArrayRef(@Nonnull JArrayRef ref) {
    if (ref.getBase() == oldUse) {
      setResult(ref.withBase((Local) newUse));
    } else if (ref.getIndex() == oldUse) {
      setResult(ref.withIndex(newUse));
    } else {
      throw new IllegalArgumentException(
          "given oldUse is not a current use of JArrayRef " + ref + "!");
    }
  }

  @Override
  public void caseParameterRef(@Nonnull JParameterRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseCaughtExceptionRef(@Nonnull JCaughtExceptionRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void caseThisRef(@Nonnull JThisRef ref) {
    defaultCaseRef(ref);
  }

  @Override
  public void defaultCaseRef(@Nonnull Ref ref) {
    setResult(ref);
  }
}
