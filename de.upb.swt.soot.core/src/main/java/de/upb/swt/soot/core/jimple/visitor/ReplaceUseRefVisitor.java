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
import de.upb.swt.soot.core.jimple.common.ref.*;
import javax.annotation.Nonnull;

/**
 * Replace old use of a Ref with a new use
 *
 * @author Zun Wang
 */
public class ReplaceUseRefVisitor extends AbstractRefVisitor<Ref> {

  Value oldUse;
  Value newUse;

  public ReplaceUseRefVisitor(@Nonnull Value oldUse, @Nonnull Value newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  @Override
  public void caseStaticFieldRef(@Nonnull JStaticFieldRef ref) {
    this.defaultCaseRef(ref);
  }

  @Override
  public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef ref) {
    if (newUse instanceof Local && ref.getBase().equivTo(oldUse)) {
      setResult(ref.withBase((Local) newUse));
    } else {
      this.defaultCaseRef(ref);
    }
  }

  @Override
  public void caseArrayRef(@Nonnull JArrayRef ref) {
    if (newUse instanceof Local && ref.getBase().equivTo(oldUse)) {
      setResult(ref.withBase((Local) newUse));
    } else if (newUse instanceof Immediate && ref.getIndex().equivTo(oldUse)) {
      setResult(ref.withIndex(newUse));
    } else {
      this.defaultCaseRef(ref);
    }
  }

  @Override
  public void caseParameterRef(@Nonnull JParameterRef ref) {
    this.defaultCaseRef(ref);
  }

  @Override
  public void caseCaughtExceptionRef(@Nonnull JCaughtExceptionRef ref) {
    this.defaultCaseRef(ref);
  }

  @Override
  public void caseThisRef(@Nonnull JThisRef ref) {
    this.defaultCaseRef(ref);
  }

  @Override
  public void defaultCaseRef(@Nonnull Ref ref) {
    setResult(ref);
  }
}
