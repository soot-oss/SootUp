package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Markus Schmidt and others
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

import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.ref.Ref;
import sootup.core.jimple.visitor.*;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

/**
 * Data used as, for instance, arguments to instructions; typical implementations are constants or
 * expressions.
 *
 * <p>Values are typed, clonable and must declare which other Values they use (contain).
 */
public interface Value extends EquivTo {

  /**
   * Returns a List of Locals,FieldRefs,ArrayRefs which are used by (ie contained within) this
   * Expression or Reference.
   *
   * @return
   */
  @Nonnull
  List<Value> getUses();

  /** Returns the Soot type of this Value. */
  @Nonnull
  Type getType();

  void toString(@Nonnull StmtPrinter up);

  default void accept(@Nonnull ValueVisitor v) {
    // [ms] find a way without casting and instanceof..
    if (this instanceof Local) {
      ((Local) this).accept((ImmediateVisitor) v);
    } else if (this instanceof Expr) {
      ((Expr) this).accept((ExprVisitor) v);
    } else if (this instanceof Constant) {
      ((Constant) this).accept((ConstantVisitor) v);
    } else if (this instanceof Ref) {
      ((Ref) this).accept((RefVisitor) v);
    } else {
      throw new RuntimeException("Unknown type of Value to switch on.");
    }
  }
}
