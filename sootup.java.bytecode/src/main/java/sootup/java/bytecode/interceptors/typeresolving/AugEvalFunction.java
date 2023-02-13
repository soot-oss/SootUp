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
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.bytecode.interceptors.typeresolving.types.AugmentIntegerTypes;
import sootup.java.bytecode.interceptors.typeresolving.types.BottomType;
import sootup.java.core.JavaIdentifierFactory;

/** @author Zun Wang */
public class AugEvalFunction {
  IdentifierFactory factory = JavaIdentifierFactory.getInstance();
  View<?> view;
  PrimitiveHierarchy primitiveHierarchy = new PrimitiveHierarchy();

  public AugEvalFunction(View<?> view) {
    this.view = view;
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
    if (value instanceof Immediate) {
      if (value instanceof Local) {
        return typing.getType((Local) value);
        // if value instanceof Constant
      } else {
        if (value instanceof IntConstant) {
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
        } else if (value instanceof LongConstant
            || value instanceof FloatConstant
            || value instanceof DoubleConstant
            || value instanceof NullConstant
            || value instanceof EnumConstant) {
          return value.getType();
        } else if (value instanceof StringConstant) {
          return factory.getClassType("java.lang.String");
        } else if (value instanceof ClassConstant) {
          return factory.getClassType("java.lang.Class");
        } else if (value instanceof MethodHandle) {
          return factory.getClassType("java.lang.MethodHandle");
        } else if (value instanceof MethodType) {
          return factory.getClassType("java.lang.MethodType");
        } else {
          throw new RuntimeException("Invaluable constant in AugEvalFunction: " + value);
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
              if (tl instanceof PrimitiveType.BooleanType) {
                return (tr instanceof PrimitiveType.BooleanType) ? PrimitiveType.getBoolean() : tr;
              } else if (tr instanceof PrimitiveType.BooleanType) {
                return tl;
              } else {
                Collection<Type> set = primitiveHierarchy.getLeastCommonAncestor(tl, tr);
                if (set.isEmpty()) {
                  throw new RuntimeException(
                      "Invaluable expression by using AugEvalFunction: " + value);
                }
                return set.iterator().next();
              }
            } else {
              return (tl instanceof PrimitiveType.LongType) ? PrimitiveType.getLong() : tr;
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
        ClassType throwable = factory.getClassType("java.lang.Throwable");
        ClassType type = null;
        for (ClassType exceptionType : exceptionTypes) {
          Optional<?> exceptionClassOp = view.getClass(exceptionType);
          SootClass<?> exceptionClass;
          if (exceptionClassOp.isPresent()) {
            exceptionClass = (SootClass<?>) exceptionClassOp.get();
          } else {
            throw new RuntimeException(
                "ExceptionType: \"" + exceptionType + "\" is not in the view");
          }
          if (exceptionClass.isPhantomClass()) {
            return throwable;
          } else if (type == null) {
            type = exceptionType;
          } else {
            type = getLeastCommonExceptionType(type, exceptionType);
          }
        }
        if (type == null) {
          throw new RuntimeException("Invaluable reference in AugEvalFunction: " + value);
        }
        return type;
      } else if (value instanceof JArrayRef) {
        Type type = typing.getType(((JArrayRef) value).getBase());
        if (type instanceof ArrayType) {
          return ((ArrayType) type).getElementType();
          // Because Object, Serializable and Cloneable are super types of any ArrayType, thus the
          // base type of ArrayRef could be one of this three types
        } else if (type instanceof ClassType) {
          String name = ((ClassType) type).getFullyQualifiedName();
          Type retType;
          switch (name) {
            case "java.lang.Object":
              retType = factory.getClassType("java.lang.Object");
              break;
            case "java.lang.Cloneable":
              retType = factory.getClassType("java.lang.Cloneable");
              break;
            case "java.io.Serializable":
              retType = factory.getClassType("java.io.Serializable");
              break;
            default:
              retType = BottomType.getInstance();
          }
          return retType;
        } else {
          return BottomType.getInstance();
        }
      } else if (value instanceof JThisRef
          || value instanceof JParameterRef
          || value instanceof JFieldRef) {
        return value.getType();
      } else {
        throw new RuntimeException("Invaluable reference in AugEvalFunction: " + value);
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
    ViewTypeHierarchy hierarchy = new ViewTypeHierarchy(view);
    ClassType throwable = factory.getClassType("java.lang.Throwable");
    Deque<ClassType> path = new ArrayDeque<>();
    path.push(exceptionType);

    while (!exceptionType.equals(throwable)) {
      ClassType superType = hierarchy.directlySuperClassOf(exceptionType);
      if (superType != null) {
        path.push(superType);
        exceptionType = superType;
      } else {
        throw new RuntimeException(
            "The path from " + exceptionType + " to java.lang.Throwable cannot be found!");
      }
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
    if (a.equals(b)) {
      return a;
    }
    ClassType commonType = null;
    Deque<ClassType> pathA = getExceptionPath(a);
    Deque<ClassType> pathB = getExceptionPath(b);
    while (!pathA.isEmpty() && !pathB.isEmpty() && pathA.getFirst().equals(pathB.getFirst())) {
      commonType = pathA.removeFirst();
      pathB.removeFirst();
    }
    return commonType;
  }
}
