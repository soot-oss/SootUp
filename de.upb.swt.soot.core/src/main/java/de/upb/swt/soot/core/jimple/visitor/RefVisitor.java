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

public interface RefVisitor extends Visitor {

  void caseStaticFieldRef(JStaticFieldRef v);

  void caseInstanceFieldRef(JInstanceFieldRef v);

  void caseArrayRef(JArrayRef v);

  void caseParameterRef(JParameterRef v);

  void caseCaughtExceptionRef(JCaughtExceptionRef v);

  void caseThisRef(JThisRef v);

  void defaultCase(Object obj);
}
