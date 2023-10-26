package sootup.java.bytecode.interceptors.typeresolving;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Zun Wang
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

import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.bytecode.interceptors.typeresolving.types.AugIntegerTypes;
import sootup.java.bytecode.interceptors.typeresolving.types.BottomType;

/** @author Zun Wang */
public class AugEvalFunction {

  private final ImmutableSet<ClassType> evalClassTypes;
  private final ClassType stringClassType;
  private final ClassType classClassType;
  private final ClassType methodHandleClassType;
  private final ClassType methodTypeClassType;
  private final ClassType throwableClassType;

  View<? extends SootClass<?>> view;

  public AugEvalFunction(View<? extends SootClass<?>> view) {
    this.view = view;

    // one time setup
    final IdentifierFactory identifierFactory = view.getIdentifierFactory();
    evalClassTypes =
        ImmutableSet.of(
            identifierFactory.getClassType("java.lang.Object"),
            identifierFactory.getClassType("java.lang.Cloneable"),
            identifierFactory.getClassType("java.io.Serializable"));

    stringClassType = identifierFactory.getClassType("java.lang.String");
    classClassType = identifierFactory.getClassType("java.lang.Class");
    methodHandleClassType = identifierFactory.getClassType("java.lang.MethodHandle");
    methodTypeClassType = identifierFactory.getClassType("java.lang.MethodType");
    throwableClassType = identifierFactory.getClassType("java.lang.Throwable");
  }

  /**
   * This method is used to evaluate the type of the given value which the given stmt and body
   * belongs to.
   */
  public Type evaluate(
      @Nonnull Typing typing,
      @Nonnull Value value,
      @Nonnull Stmt stmt,
      @Nonnull StmtGraph<?> graph) {

    // TODO: [ms] make use of the ValueVisitor..

    if (value instanceof Immediate) {
      if (value instanceof Local) {
        return typing.getType((Local) value);
        // if value instanceof Constant
      } else {
        if (value.getClass() == IntConstant.class) {
          int val = ((IntConstant) value).getValue();
          if (val >= 0 && val < 2) {
            return AugIntegerTypes.getInteger1();
          } else if (val >= 2 && val < 128) {
            return AugIntegerTypes.getInteger127();
          } else if (val >= -128 && val < 0) {
            return PrimitiveType.getByte();
          } else if (val >= 128 && val < 32768) {
            return AugIntegerTypes.getInteger32767();
          } else if (val >= -32768 && val < -128) {
            return PrimitiveType.getShort();
          } else if (val >= 32768 && val < 65536) {
            return PrimitiveType.getChar();
          } else {
            return PrimitiveType.getInt();
          }
        } else if (value.getClass() == LongConstant.class
            || value.getClass() == FloatConstant.class
            || value.getClass() == DoubleConstant.class
            || value.getClass() == NullConstant.class
            || value.getClass() == EnumConstant.class) {
          return value.getType();
        } else if (value.getClass() == StringConstant.class) {
          return stringClassType;
        } else if (value.getClass() == ClassConstant.class) {
          return classClassType;
        } else if (value.getClass() == MethodHandle.class) {
          return methodHandleClassType;
        } else if (value.getClass() == MethodType.class) {
          return methodTypeClassType;
        } else {
          throw new RuntimeException("Invaluable constant in AugEvalFunction '" + value + "'.");
        }
      }
    } else if (value instanceof Expr) {
      if (value instanceof AbstractBinopExpr) {
        Type tl = evaluate(typing, ((AbstractBinopExpr) value).getOp1(), stmt, graph);
        Type tr = evaluate(typing, ((AbstractBinopExpr) value).getOp2(), stmt, graph);
        if (value instanceof AbstractIntBinopExpr) {
          if (value instanceof AbstractConditionExpr) {
            return PrimitiveType.getBoolean();
          } else {
            return PrimitiveType.getByte();
          }
        } else if (value instanceof AbstractIntLongBinopExpr) {
          if (value instanceof JShlExpr
              || value instanceof JShrExpr
              || value instanceof JUshrExpr) {
            return (tl instanceof PrimitiveType.IntType) ? PrimitiveType.getInt() : tl;
          } else {
            if (tl instanceof PrimitiveType.IntType && tr instanceof PrimitiveType.IntType) {

              if (tl.getClass() == PrimitiveType.BooleanType.class) {
                return (tr.getClass() == PrimitiveType.BooleanType.class)
                    ? PrimitiveType.getBoolean()
                    : tr;
              } else if (tr.getClass() == PrimitiveType.BooleanType.class) {
                return tl;
              } else {
                Collection<Type> lca = PrimitiveHierarchy.getLeastCommonAncestor(tl, tr);
                if (lca.isEmpty()) {
                  throw new RuntimeException(
                      "Invaluable expression by using AugEvalFunction '" + value + "'.");
                }
                return lca.iterator().next();
              }
            } else {
              return (tl.getClass() == PrimitiveType.LongType.class) ? PrimitiveType.getLong() : tr;
            }
          }
        } else if (value instanceof AbstractFloatBinopExpr) {
          return (tl instanceof PrimitiveType.IntType) ? PrimitiveType.getInt() : tl;
        }
      } else if (value instanceof AbstractUnopExpr) {
        if (value instanceof JLengthExpr) {
          return PrimitiveType.getInt();
        } else {
          Type opt = evaluate(typing, ((AbstractUnopExpr) value).getOp(), stmt, graph);
          return (opt instanceof PrimitiveType.IntType) ? PrimitiveType.getInt() : opt;
        }
      } else {
        return value.getType();
      }
    } else if (value instanceof Ref) {
      if (value instanceof JCaughtExceptionRef) {
        Set<ClassType> exceptionTypes = getExceptionTypeCandidates(stmt, graph);
        ClassType type = null;
        for (ClassType exceptionType : exceptionTypes) {
          Optional<?> exceptionClassOp = view.getClass(exceptionType);
          SootClass<?> exceptionClass;
          if (exceptionClassOp.isPresent()) {
            exceptionClass = (SootClass<?>) exceptionClassOp.get();
          } else {
            throw new RuntimeException("ExceptionType '" + exceptionType + "' is not in the view");
          }
          if (exceptionClass.isPhantomClass()) {
            return throwableClassType;
          } else if (type == null) {
            type = exceptionType;
          } else {
            type = getLeastCommonExceptionType(type, exceptionType);
          }
        }
        if (type == null) {
          throw new RuntimeException("Invaluable reference in AugEvalFunction '" + value + "'.");
        }
        return type;
      } else if (value instanceof JArrayRef) {
        Type type = typing.getType(((JArrayRef) value).getBase());
        if (type instanceof ArrayType) {
          return ((ArrayType) type).getElementType();
          // Because Object, Serializable and Cloneable are super types of any ArrayType, thus the
          // base type of ArrayRef could be one of this three types
        } else if (type instanceof ClassType) {
          return evalClassTypes.contains(type) ? type : BottomType.getInstance();
        } else {
          return BottomType.getInstance();
        }
      } else if (value.getClass() == JThisRef.class
          || value.getClass() == JParameterRef.class
          || value instanceof JFieldRef) {
        return value.getType();
      } else {
        throw new RuntimeException("Invaluable reference in AugEvalFunction '" + value + "'.");
      }
    }
    return null;
  }

  /**
   * This function is used to get all exception types for the traps handled by the given handle
   * statement in body.
   */
  private Set<ClassType> getExceptionTypeCandidates(
      @Nonnull Stmt handlerStmt, @Nonnull StmtGraph<?> graph) {
    return graph.getBlockOf(handlerStmt).getExceptionalPredecessors().keySet();
  }

  /**
   * This function is used to retrieve the path from the type "Throwable" to the given exception
   * type
   */
  private Deque<ClassType> getExceptionPath(@Nonnull ClassType exceptionType) {
    Deque<ClassType> path = new ArrayDeque<>();
    path.push(exceptionType);

    while (exceptionType != throwableClassType) {
      final Optional<? extends ClassType> superclassOpt =
          view.getClass(exceptionType).flatMap(SootClass::getSuperclass);
      if (!superclassOpt.isPresent()) {
        throw new IllegalStateException(
            "The path from '" + exceptionType + "' to java.lang.Throwable cannot be found!");
      }

      ClassType superType = superclassOpt.get();
      path.push(superType);
      exceptionType = superType;
    }
    return path;
  }

  /**
   * This function is used to get the least common type for two exception types
   *
   * @param a an exception type
   * @param b an exception type
   */
  private ClassType getLeastCommonExceptionType(@Nonnull ClassType a, @Nonnull ClassType b) {
    if (a == b) {
      return a;
    }
    ClassType commonType = null;
    Deque<ClassType> pathA = getExceptionPath(a);
    Deque<ClassType> pathB = getExceptionPath(b);
    while (!pathA.isEmpty() && !pathB.isEmpty() && pathA.peekFirst().equals(pathB.peekFirst())) {
      commonType = pathA.removeFirst();
      pathB.removeFirst();
    }
    return commonType;
  }
}
