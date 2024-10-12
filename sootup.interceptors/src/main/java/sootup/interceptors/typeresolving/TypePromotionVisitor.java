package sootup.interceptors.typeresolving;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2023 Raja Vallée-Rai and others
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

import java.util.Collection;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.interceptors.typeresolving.types.AugmentIntegerTypes;
import sootup.interceptors.typeresolving.types.TopType;

public class TypePromotionVisitor extends TypeChecker {
  public TypePromotionVisitor(
      @Nonnull Body.BodyBuilder builder,
      @Nonnull AugEvalFunction evalFunction,
      @Nonnull BytecodeHierarchy hierarchy) {
    super(builder, evalFunction, hierarchy);
  }

  public Typing getPromotedTyping(Typing typing) {
    setTyping(typing);
    for (Stmt stmt : builder.getStmts()) {
      stmt.accept(this);
    }
    return getTyping();
  }

  public static boolean isIntermediateType(Type type) {
    return type.equals(AugmentIntegerTypes.getInteger1())
        || type.equals(AugmentIntegerTypes.getInteger127())
        || type.equals(AugmentIntegerTypes.getInteger32767());
  }

  public void visit(@Nonnull Value value, @Nonnull Type stdType, @Nonnull Stmt stmt) {

    /* Note: When visiting function parameters, we may encounter constant values such as strings ("abc") or integers (2).
      These constants are not instances of the Local class and should be handled accordingly.
      Skipping non-Local instances may lead to over-approximation and potential inaccuracies; revisit if issues arise.
    */
    if (!(value instanceof Local)) {
      return;
    }
    AugEvalFunction evalFunction = getFuntion();
    BytecodeHierarchy hierarchy = getHierarchy();
    Typing typing = getTyping();
    Type evaType = evalFunction.evaluate(typing, value, stmt, graph);
    if (evaType == null || evaType.equals(stdType)) {
      return;
    }
    if (!hierarchy.isAncestor(stdType, evaType)) {
      if (!hierarchy.isAncestor(evaType, stdType)) {
        assert value instanceof Local;
        // The type of the local and the type that is required in the statement are incompatible,
        // so the type of the local needs to be upgraded to a common ancestor.
        Collection<Type> lca = hierarchy.getLeastCommonAncestors(evaType, stdType);
        assert !lca.isEmpty();
        // Only use the first of the common ancestors, because this is an edge case.
        // The proper way to do this would be to create a completely new visitor that can yield
        // multiple possible typings, or to not only use assignments for the initial typing.
        typing.set((Local) value, lca.iterator().next());
      }
    } else if (value instanceof Local && isIntermediateType(evaType)) {
      Local local = (Local) value;
      Type promotedType = promote(evaType, stdType);
      if (promotedType != null && !promotedType.equals(evaType)) {
        typing.set(local, promotedType);
      }
    }
  }

  private Type promote(Type low, Type high) {
    Class<?> lowClass = low.getClass();
    Class<?> highClass = high.getClass();

    if (highClass == TopType.class || lowClass == highClass) {
      return low;
    }

    if (lowClass == AugmentIntegerTypes.Integer1Type.class) {
      if (highClass == PrimitiveType.IntType.class) {
        return AugmentIntegerTypes.getInteger127();
      } else if (highClass == PrimitiveType.ShortType.class) {
        return PrimitiveType.getByte();
      } else if (highClass == PrimitiveType.BooleanType.class
          || highClass == PrimitiveType.ByteType.class
          || highClass == PrimitiveType.CharType.class
          || highClass == AugmentIntegerTypes.Integer127Type.class
          || highClass == AugmentIntegerTypes.Integer32767Type.class) {
        return high;
      } else {
        throw new IllegalArgumentException(
            low + " cannot be promoted with the supertype " + high + "!");
      }
    } else if (lowClass == AugmentIntegerTypes.Integer127Type.class) {
      if (highClass == PrimitiveType.ShortType.class) {
        return PrimitiveType.getByte();
      } else if (highClass == PrimitiveType.IntType.class) {
        return AugmentIntegerTypes.getInteger127();
      } else if (highClass == PrimitiveType.ByteType.class
          || highClass == PrimitiveType.CharType.class
          || highClass == AugmentIntegerTypes.Integer32767Type.class) {
        return high;
      } else {
        throw new IllegalArgumentException(
            low + " cannot be promoted with the supertype " + high + "!");
      }
    } else if (lowClass == AugmentIntegerTypes.Integer32767Type.class) {
      if (highClass == PrimitiveType.IntType.class) {
        return AugmentIntegerTypes.getInteger32767();
      } else if (highClass == PrimitiveType.ShortType.class
          || highClass == PrimitiveType.CharType.class) {
        return high;
      } else {
        throw new IllegalArgumentException(
            low + " cannot be promoted with the supertype " + high + "!");
      }
    } else {
      return low;
    }
  }
}
