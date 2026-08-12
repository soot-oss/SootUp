/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core.pag;

import java.util.Objects;
import sootup.core.model.SootMethod;

/**
 * Represents a synthetic pointer-assignment-graph variable for one of a method's special
 * argument-passing slots: an ordinary parameter at a given index (the base case, this class
 * itself), or the receiver ({@code this}), the return value, or the thrown exception (the {@link
 * This}, {@link Return} and {@link Throw} subclasses). Use the {@code of*} factory methods to
 * build one, and {@link #isThis()}/{@link #isReturn()}/{@link #isThrowRet()} to tell which kind
 * you have, rather than comparing against a magic sentinel index.
 *
 * @author Ondrej Lhotak
 */
public class MethodParameter {
  private final int index;
  private final SootMethod method;

  private MethodParameter(SootMethod m, int index) {
    this.method = m;
    this.index = index;
  }

  public static MethodParameter ofOrdinary(SootMethod m, int index) {
    if (index < 0) {
      throw new IllegalArgumentException("ordinary parameter index must be >= 0, got " + index);
    }
    return new MethodParameter(m, index);
  }

  public static MethodParameter ofThis(SootMethod m) {
    return new This(m);
  }

  public static MethodParameter ofReturn(SootMethod m) {
    return new Return(m);
  }

  public static MethodParameter ofThrow(SootMethod m) {
    return new Throw(m);
  }

  public String toString() {
    return "Parm " + index + " to " + method;
  }

  /**
   * Returns the ordinary-parameter index. Only valid on a plain (non-this/return/throw)
   * instance; throws otherwise.
   */
  public int getIndex() {
    return index;
  }

  public boolean isThis() {
    return false;
  }

  public boolean isReturn() {
    return false;
  }

  public boolean isThrowRet() {
    return false;
  }

  public SootMethod method() {
    return method;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MethodParameter that = (MethodParameter) o;
    return index == that.index && method.equals(that.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), index, method);
  }

  /** Represents the receiver ({@code this}) of an instance method. */
  private static final class This extends MethodParameter {
    private This(SootMethod m) {
      super(m, -1);
    }

    @Override
    public String toString() {
      return "Parm THIS_NODE to " + method();
    }

    @Override
    public boolean isThis() {
      return true;
    }

    @Override
    public int getIndex() {
      throw new IllegalStateException("getIndex() is not valid for the THIS parameter");
    }
  }

  /** Represents the return value of a method. */
  private static final class Return extends MethodParameter {
    private Return(SootMethod m) {
      super(m, -1);
    }

    @Override
    public String toString() {
      return "Parm RETURN to " + method();
    }

    @Override
    public boolean isReturn() {
      return true;
    }

    @Override
    public int getIndex() {
      throw new IllegalStateException("getIndex() is not valid for the RETURN parameter");
    }
  }

  /** Represents the exception thrown out of a method. */
  private static final class Throw extends MethodParameter {
    private Throw(SootMethod m) {
      super(m, -1);
    }

    @Override
    public String toString() {
      return "Parm THROW to " + method();
    }

    @Override
    public boolean isThrowRet() {
      return true;
    }

    @Override
    public int getIndex() {
      throw new IllegalStateException("getIndex() is not valid for the THROW parameter");
    }
  }
}
