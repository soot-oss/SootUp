package sootup.interceptors.typeresolving;

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

import java.util.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.interceptors.typeresolving.types.AugmentIntegerTypes;
import sootup.interceptors.typeresolving.types.BottomType;
import sootup.interceptors.typeresolving.types.TopType;

/**
 * @author Zun Wang
 */
public class AugEvalFunction {

  private static final Logger logger = LoggerFactory.getLogger(AugEvalFunction.class);

  private final ClassType stringClassType;
  private final ClassType classClassType;
  private final ClassType methodHandleClassType;
  private final ClassType methodTypeClassType;
  private final ClassType throwableClassType;

  View view;

  public AugEvalFunction(View view) {
    this.view = view;

    // one time setup
    final IdentifierFactory identifierFactory = view.getIdentifierFactory();

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
  @Nullable
  public Type evaluate(
      @NonNull Typing typing,
      @NonNull Value value,
      @NonNull Stmt stmt,
      @NonNull StmtGraph<?> graph) {

    // TODO: [ms] make use of the ValueVisitor

    if (value instanceof Immediate) {
      if (value instanceof Local) {
        return typing.getType((Local) value);
      } else if (value instanceof Constant) {
        if (value.getClass() == IntConstant.class) {
          int val = ((IntConstant) value).getValue();
          if (val >= 0 && val < 2) {
            return AugmentIntegerTypes.getInteger1();
          } else if (val >= 2 && val < 128) {
            return AugmentIntegerTypes.getInteger127();
          } else if (val >= -128 && val < 0) {
            return PrimitiveType.getByte();
          } else if (val >= 128 && val < 32768) {
            return AugmentIntegerTypes.getInteger32767();
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
          throw new IllegalStateException("can't evaluate this type of Constant '" + value + "'.");
        }
      }
    } else if (value instanceof Expr) {
      if (value instanceof AbstractBinopExpr) {
        Type tl = evaluate(typing, ((AbstractBinopExpr) value).getOp1(), stmt, graph);
        if (tl == null) {
          return null;
          // throw new RuntimeException("can't evaluatable constant in AugEvalFunction '" + value +
          // "'.");
        }
        Type tr = evaluate(typing, ((AbstractBinopExpr) value).getOp2(), stmt, graph);
        if (tr == null) {
          return null;
          // throw new RuntimeException("can't evaluatable constant in AugEvalFunction '" + value +
          // "'.");
        }
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
                  // throw new RuntimeException("can't evaluate expression by using AugEvalFunction
                  // '" + value + "'.");
                  return null;
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
          Optional<?> exceptionClassOpt = view.getClass(exceptionType);
          if (exceptionClassOpt.isEmpty()) {
            return throwableClassType;
          }
          if (type == null) {
            type = exceptionType;
          } else {
            type = getLeastCommonExceptionType(type, exceptionType);
          }
        }
        return type;
      } else if (value instanceof JArrayRef) {
        Type type = typing.getType(((JArrayRef) value).getBase());
        if (type instanceof ArrayType) {
          return ((ArrayType) type).getElementType();
        } else if (type instanceof NullType) {
          // This is an expression like `null[index]`. That means the type of the array variable has
          // not been determined yet, but because this statement is dependent on whatever will
          // calculate the type of the array, the fixpoint iteration will call this again with the
          // correct type. (Or it won't get called again, in which case the `null` type will get
          // promoted to `Object`)
          return BottomType.getInstance();
        } else {
          // When the type is not an array type, it can't be known what the type of the array ref
          // expression is. Because the result of the array access could be an object or a
          // primitive, the top type has to be chosen here.
          return TopType.getInstance();
        }
      } else if (value.getClass() == JThisRef.class
          || value.getClass() == JParameterRef.class
          || value instanceof JFieldRef) {
        return value.getType();
      } else {
        return null;
        // throw new RuntimeException("can't evaluatable reference in AugEvalFunction '" + value +
        // "'.");
      }
    }
    return null;
  }

  /**
   * This function is used to get all exception types for the traps handled by the given handle
   * statement in body.
   */
  private Set<ClassType> getExceptionTypeCandidates(
      @NonNull Stmt handlerStmt, @NonNull StmtGraph<?> graph) {
    return graph.getBlockOf(handlerStmt).getExceptionalPredecessors().keySet();
  }

  /**
   * This function is used to retrieve the path from the type "Throwable" to the given exception
   * type
   */
  // TODO: ms: simplify - use the typehiararchy directly!
  private Deque<ClassType> getExceptionPath(@NonNull ClassType exceptionType) {
    Deque<ClassType> path = new ArrayDeque<>();
    path.push(exceptionType);

    while (exceptionType != throwableClassType) {
      final Optional<? extends ClassType> superclassOpt =
          view.getClass(exceptionType).flatMap(SootClass::getSuperclass);
      if (superclassOpt.isEmpty()) {
        // Note: We have progressed as far as the available information allows.
        logger.warn(
            "The path from '"
                + exceptionType
                + "' to java.lang.Throwable cannot be found! Are you certain you don't want to include rt.jar? Including rt.jar could provide additional classes and resources that might be necessary for full functionality.");
        break;
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
  private ClassType getLeastCommonExceptionType(@NonNull ClassType a, @NonNull ClassType b) {
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
