package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing;

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


import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.visitor.ValueVisitor;
import de.upb.swt.soot.core.types.Type;

import javax.annotation.Nonnull;

public abstract class UntypedConstant implements Constant {
  /**
  * 
  */
  private static final long serialVersionUID = -742448859930407635L;

  @Override
  public Type getType() {
    throw new RuntimeException("no type yet!");
  }


  public abstract Immediate defineType(Type type);

  @Override
  public void accept(@Nonnull ValueVisitor v) {

  }
}
