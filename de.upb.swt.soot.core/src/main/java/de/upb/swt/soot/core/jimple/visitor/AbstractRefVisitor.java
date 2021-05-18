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

import de.upb.swt.soot.core.jimple.common.ref.*;
import javax.annotation.Nullable;

public abstract class AbstractRefVisitor<V> implements RefVisitor {
  private V result;

  @Override
  public void caseStaticFieldRef(JStaticFieldRef v) {
    defaultCase(v);
  }

  @Override
  public void caseInstanceFieldRef(JInstanceFieldRef v) {
    defaultCase(v);
  }

  @Override
  public void caseArrayRef(JArrayRef v) {
    defaultCase(v);
  }

  @Override
  public void caseParameterRef(JParameterRef v) {
    defaultCase(v);
  }

  @Override
  public void caseCaughtExceptionRef(JCaughtExceptionRef v) {
    defaultCase(v);
  }

  @Override
  public void caseThisRef(JThisRef v) {
    defaultCase(v);
  }

  @Override
  public void defaultCase(Ref v) {}

  protected void setResult(V result) {
    this.result = result;
  }

  @Nullable
  public V getResult() {
    return result;
  }
}
