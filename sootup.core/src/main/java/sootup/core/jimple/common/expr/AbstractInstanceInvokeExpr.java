package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Zun Wang and others
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
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.signatures.MethodSignature;

public abstract class AbstractInstanceInvokeExpr extends AbstractInvokeExpr {

  @NonNull private final Local base;

  AbstractInstanceInvokeExpr(
      @NonNull Local base, @NonNull MethodSignature methodSig, @NonNull Immediate[] args) {
    super(methodSig, args);
    this.base = base;
  }

  @NonNull
  public Local getBase() {
    return base;
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.concat(
        Stream.concat(
            Stream.concat(getArgs().stream(), getArgs().stream().flatMap(Value::getUses)),
            base.getUses()),
        Stream.of(base));
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return base.equivHashCode() * 101 + getMethodSignature().hashCode() * 17;
  }

  @NonNull
  public abstract AbstractInvokeExpr withBase(@NonNull Local base);

  @NonNull
  public abstract AbstractInvokeExpr withMethodSignature(@NonNull MethodSignature methodSignature);

  @NonNull
  public abstract AbstractInvokeExpr withArgs(@NonNull List<Immediate> args);
}
