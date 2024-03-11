package sootup.java.bytecode.interceptors.typeresolving.types;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2023 Zun Wang
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

import javax.annotation.Nonnull;
import sootup.core.jimple.visitor.TypeVisitor;
import sootup.core.types.PrimitiveType;

public abstract class AugIntegerTypes {

  @Nonnull
  public static Integer1Type getInteger1() {
    return Integer1Type.getInstance();
  }

  @Nonnull
  public static Integer127Type getInteger127() {
    return Integer127Type.getInstance();
  }

  @Nonnull
  public static Integer32767Type getInteger32767() {
    return Integer32767Type.getInstance();
  }

  /**
   * This type is intermediate type and used for determining the ancestor of an integer type. see:
   * AugmentHierarchy;
   */
  public static class Integer1Type extends PrimitiveType.IntType {
    // 2^0
    private static final Integer1Type INSTANCE = new Integer1Type();

    private Integer1Type() {
      super("integer1");
    }

    public static Integer1Type getInstance() {
      return INSTANCE;
    }

    @Override
    public void accept(@Nonnull TypeVisitor v) {
      throw new UnsupportedOperationException();
    }
  }

  /** This type is intermediate type and used for determining the ancestor of an integer type */
  public static class Integer127Type extends PrimitiveType.IntType {
    // 2^8
    private static final Integer127Type INSTANCE = new Integer127Type();

    private Integer127Type() {
      super("integer127");
    }

    public static Integer127Type getInstance() {
      return INSTANCE;
    }

    @Override
    public void accept(@Nonnull TypeVisitor v) {
      throw new UnsupportedOperationException();
    }
  }

  /** This type is intermediate type and used for determining the ancestor of an integer type */
  public static class Integer32767Type extends PrimitiveType.IntType {
    // 2^16
    private static final Integer32767Type INSTANCE = new Integer32767Type();

    private Integer32767Type() {
      super("integer32767");
    }

    public static Integer32767Type getInstance() {
      return INSTANCE;
    }

    @Override
    public void accept(@Nonnull TypeVisitor v) {
      throw new UnsupportedOperationException();
    }
  }
}
