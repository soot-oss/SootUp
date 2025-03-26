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

import java.util.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

public abstract class TypeChecker extends AbstractStmtVisitor {

  private final AugEvalFunction evalFunction;
  private final BytecodeHierarchy hierarchy;
  protected Stmt result = null;
  private Typing typing;
  protected final Body.BodyBuilder builder;

  protected final MutableStmtGraph graph;

  private static final Logger logger = LoggerFactory.getLogger(TypeChecker.class);

  public TypeChecker(
      Body.@NonNull BodyBuilder builder,
      @NonNull AugEvalFunction evalFunction,
      @NonNull BytecodeHierarchy hierarchy) {
    this.builder = builder;
    this.graph = builder.getStmtGraph();
    this.evalFunction = evalFunction;
    this.hierarchy = hierarchy;
  }

  public abstract void visit(@NonNull Value value, @NonNull Type stdType, @NonNull Stmt stmt);

  @Override
  public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
    handleInvokeExpr(stmt.getInvokeExpr().get(), stmt);
  }

  @Override
  public void caseAssignStmt(@NonNull JAssignStmt stmt) {
    LValue lhs = stmt.getLeftOp();
    Value rhs = stmt.getRightOp();
    Type type_lhs = null;
    if (lhs instanceof Local) {
      type_lhs = typing.getType((Local) lhs);
      if (type_lhs == null) {
        // body.getLocals() is out of sync with Locals used in the Stmts
        logger.info("Body.locals do not match the Locals occurring in the Stmts.");
        return;
      }
    } else if (lhs instanceof JArrayRef) {
      visit(((JArrayRef) lhs).getIndex(), PrimitiveType.getInt(), stmt);
      ArrayType arrayType = null;
      Local base = ((JArrayRef) lhs).getBase();
      Type type_base = typing.getType(base);
      if (type_base == null) {
        // body.getLocals() is out of sync with Locals used in the Stmts
        logger.info("Body.locals do not match the Locals occurring in the Stmts.");
        return;
      }
      if (type_base instanceof ArrayType) {
        arrayType = (ArrayType) type_base;
      } else {
        Type type_rhs = evalFunction.evaluate(typing, rhs, stmt, graph);
        // if base type of lhs is an object-like-type, retrieve its base type from array
        // allocation site.
        if (Type.isObjectLikeType(type_base)
            || (Type.isObject(type_base) && type_rhs instanceof PrimitiveType)) {
          Map<LValue, Collection<Stmt>> defs = Body.collectDefs(graph.getNodes());
          Collection<Stmt> defStmts = defs.get(base);
          boolean findDef = false;
          if (defStmts != null) {
            for (Stmt defStmt : defStmts) {
              if (defStmt instanceof JAssignStmt) {
                Value arrExpr = ((JAssignStmt) defStmt).getRightOp();
                if (arrExpr instanceof JNewArrayExpr) {
                  arrayType = (ArrayType) arrExpr.getType();
                  findDef = true;
                  break;
                } else if (arrExpr instanceof JNewMultiArrayExpr) {
                  arrayType = ((JNewMultiArrayExpr) arrExpr).getBaseType();
                  findDef = true;
                  break;
                }
              }
            }
          }
          if (!findDef && type_rhs != null) {
            arrayType = Type.createArrayType(type_rhs, 1);
          }
        }
        if (arrayType == null) {
          arrayType = Type.createArrayType(type_base, 1);
        }
      }

      type_lhs = arrayType.getElementType();
      visit(base, arrayType, stmt);
      visit(lhs, type_lhs, stmt);

    } else if (lhs instanceof JFieldRef) {
      if (lhs instanceof JInstanceFieldRef) {
        visit(
            ((JInstanceFieldRef) lhs).getBase(),
            ((JInstanceFieldRef) lhs).getFieldSignature().getDeclClassType(),
            stmt);
      }
      type_lhs = lhs.getType();
    }

    if (type_lhs == null) {
      logger.info("Lhs was is not set!");
      return;
    }

    if (rhs instanceof Local) {
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JArrayRef) {
      visit(((JArrayRef) rhs).getIndex(), PrimitiveType.getInt(), stmt);
      Local base = ((JArrayRef) rhs).getBase();
      ArrayType arrayType = null;
      Type type_base = typing.getType(base);
      if (type_base == null) {
        // body.getLocals() is out of sync with Locals used in the Stmts
        logger.info("Body.locals do not match the Locals occurring in the Stmts.");
        return;
      }
      if (type_base instanceof ArrayType) {
        arrayType = (ArrayType) type_base;
      } else {
        if (type_base instanceof NullType || Type.isObjectLikeType(type_base)) {
          Map<LValue, Collection<Stmt>> defs = Body.collectDefs(graph.getNodes());
          Deque<StmtLocalPair> worklist = new ArrayDeque<>();
          Set<StmtLocalPair> visited = new HashSet<>();
          worklist.add(new StmtLocalPair(stmt, base));
          Type sel = null;
          while (!worklist.isEmpty()) {
            StmtLocalPair pair = worklist.removeFirst();
            if (!visited.add(pair)) {
              continue;
            }
            Collection<Stmt> stmts = defs.get(pair.getLocal());
            for (Stmt s : stmts) {
              if (s instanceof JAssignStmt) {
                Value value = ((JAssignStmt) s).getRightOp();
                if (value instanceof JNewArrayExpr) {
                  sel = selectArrayType(sel, ((JNewArrayExpr) value).getBaseType(), s);
                } else if (value instanceof JNewMultiArrayExpr) {
                  sel = selectArrayType(sel, ((JNewMultiArrayExpr) value).getBaseType(), s);
                } else if (value instanceof Local) {
                  worklist.add(new StmtLocalPair(s, (Local) value));
                } else if (value instanceof JCastExpr) {
                  worklist.add(new StmtLocalPair(s, (Local) ((JCastExpr) value).getOp()));
                }
              }
            }
          }
          if (sel == null) {
            sel = type_base;
          }
          arrayType = Type.createArrayType(sel, 1);
        }
      }
      if (arrayType != null) {
        Type type_rhs = arrayType.getElementType();
        visit(base, arrayType, stmt);
        visit(rhs, type_rhs, stmt);
        visit(rhs, type_lhs, stmt);
      }
    } else if (rhs instanceof JInstanceFieldRef) {
      visit(
          ((JInstanceFieldRef) rhs).getBase(),
          ((JInstanceFieldRef) rhs).getFieldSignature().getDeclClassType(),
          stmt);
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof AbstractBinopExpr) {
      handleBinopExpr((AbstractBinopExpr) rhs, type_lhs, stmt);
    } else if (rhs instanceof AbstractInvokeExpr) {
      handleInvokeExpr((AbstractInvokeExpr) rhs, stmt);
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JCastExpr) {
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JInstanceOfExpr) {
      visit(((JInstanceOfExpr) rhs).getOp(), hierarchy.objectClassType, stmt);
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JNewArrayExpr) {
      visit(((JNewArrayExpr) rhs).getSize(), PrimitiveType.getInt(), stmt);
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JNewMultiArrayExpr) {
      for (int i = 0; i < ((JNewMultiArrayExpr) rhs).getSizeCount(); i++) {
        visit(((JNewMultiArrayExpr) rhs).getSize(i), PrimitiveType.getInt(), stmt);
      }
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JLengthExpr) {
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JNegExpr) {
      visit(((JNegExpr) rhs).getOp(), type_lhs, stmt);
    } else if (rhs instanceof JNewExpr) {
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof Constant) {
      if (!(rhs instanceof NullConstant)) {
        visit(rhs, type_lhs, stmt);
      }
    }
  }

  @Override
  public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
    visit(stmt.getOp(), hierarchy.objectClassType, stmt);
  }

  @Override
  public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
    visit(stmt.getOp(), hierarchy.objectClassType, stmt);
  }

  @Override
  public void caseIfStmt(@NonNull JIfStmt stmt) {
    handleBinopExpr(stmt.getCondition(), PrimitiveType.getBoolean(), stmt);
  }

  @Override
  public void caseSwitchStmt(@NonNull JSwitchStmt stmt) {
    visit(stmt.getKey(), PrimitiveType.getInt(), stmt);
  }

  @Override
  public void caseReturnStmt(@NonNull JReturnStmt stmt) {
    visit(stmt.getOp(), builder.getMethodSignature().getType(), stmt);
  }

  @Override
  public void caseThrowStmt(@NonNull JThrowStmt stmt) {
    visit(stmt.getOp(), hierarchy.throwableClassType, stmt);
  }

  public AugEvalFunction getFuntion() {
    return evalFunction;
  }

  public BytecodeHierarchy getHierarchy() {
    return hierarchy;
  }

  public Typing getTyping() {
    return typing;
  }

  public void setTyping(Typing typing) {
    this.typing = typing;
  }

  private void handleInvokeExpr(AbstractInvokeExpr expr, Stmt stmt) {
    MethodSignature signature = expr.getMethodSignature();
    if (expr instanceof AbstractInstanceInvokeExpr) {
      visit(((AbstractInstanceInvokeExpr) expr).getBase(), signature.getDeclClassType(), stmt);
    }
    for (int i = 0; i < expr.getArgCount(); i++) {
      visit(expr.getArg(i), signature.getParameterTypes().get(i), stmt);
    }
  }

  private void handleBinopExpr(AbstractBinopExpr expr, Type type, Stmt stmt) {
    Value op1 = expr.getOp1();
    Value op2 = expr.getOp2();
    Type t1 = evalFunction.evaluate(typing, op1, stmt, graph);
    Type t2 = evalFunction.evaluate(typing, op2, stmt, graph);
    if (expr instanceof AbstractConditionExpr
        || expr instanceof AbstractFloatBinopExpr
        || expr instanceof JShlExpr
        || expr instanceof JShrExpr
        || expr instanceof JUshrExpr) {
      if (expr instanceof JEqExpr || expr instanceof JNeExpr) {
        if (!(t1 instanceof PrimitiveType.BooleanType && t2 instanceof PrimitiveType.BooleanType)
            && t1 instanceof PrimitiveType.IntType) {
          visit(op1, PrimitiveType.getInt(), stmt);
          visit(op2, PrimitiveType.getInt(), stmt);
        }
      } else {
        if (type instanceof PrimitiveType.IntType) {
          visit(op1, PrimitiveType.getInt(), stmt);
          visit(op2, PrimitiveType.getInt(), stmt);
        }
      }
    } else if (expr instanceof JXorExpr || expr instanceof JOrExpr || expr instanceof JAndExpr) {
      visit(op1, type, stmt);
      visit(op2, type, stmt);
    }
  }

  // select the type with bigger bit size
  public Type selectArrayType(@Nullable Type preType, @NonNull Type newType, @NonNull Stmt stmt) {
    if (preType == null || preType.equals(newType)) {
      return newType;
    }
    Type sel = Type.getValueBitSize(newType) > Type.getValueBitSize(preType) ? newType : preType;

    logger.warn(
        "Conflicting array types at "
            + stmt
            + " in "
            + builder.getMethodSignature()
            + ". Its base type may be "
            + preType
            + " or "
            + newType
            + ". Selecting: "
            + sel);
    return sel;
  }

  public Stmt getResult() {
    return result;
  }

  protected void setResult(Stmt result) {
    this.result = result;
  }
}
