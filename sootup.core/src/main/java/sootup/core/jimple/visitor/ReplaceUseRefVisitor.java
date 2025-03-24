package sootup.core.jimple.visitor;

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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.Ref;

/**
 * Replace old use of a Ref with a new use
 *
 * @author Zun Wang
 */
public class ReplaceUseRefVisitor extends AbstractRefVisitor {

  protected Value oldUse;
  protected Value newUse;
  protected Ref result = null;

  public ReplaceUseRefVisitor() {}

  public void init(@NonNull Value oldUse, @NonNull Value newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  @Override
  public void caseInstanceFieldRef(@NonNull JInstanceFieldRef ref) {
    if (ref.getBase() == oldUse) {
      setResult(ref.withBase((Local) newUse));
    } else {
      setResult(ref);
    }
  }

  @Override
  public void caseArrayRef(@NonNull JArrayRef ref) {
    if (ref.getBase() == oldUse) {
      setResult(ref.withBase((Local) newUse));
    } else if (ref.getIndex() == oldUse) {
      setResult(ref.withIndex((Immediate) newUse));
    } else {
      setResult(ref);
    }
  }

  @Override
  public void defaultCaseRef(@NonNull Ref ref) {
    setResult(ref);
  }

  public Ref getResult() {
    return result;
  }

  protected void setResult(Ref result) {
    this.result = result;
  }
}
