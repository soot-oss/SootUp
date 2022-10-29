package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.jimple.visitor.AbstractStmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.BodyUtils;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import java.util.*;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CastCounter extends AbstractStmtVisitor<Stmt> {

  private final AugEvalFunction evalFunction;
  private final BytecodeHierarchy hierarchy;
  private Typing typing;
  private Body body;
  private final IdentifierFactory factory = JavaIdentifierFactory.getInstance();
  private int castCount = 0;
  private boolean countOnly;
  private Map<Stmt, Map<Value, Value>> changedValues = new HashMap<>();
  public LinkedHashSet<Local> newLocals = new LinkedHashSet<>();
  private int newLocalsCount = 0;
  public Map<Stmt, Stmt> stmt2NewStmt = new HashMap<>();

  private static final Logger logger = LoggerFactory.getLogger(CastCounter.class);

  public CastCounter(Body body, AugEvalFunction evalFunction, BytecodeHierarchy hierarchy) {
    this.body = body;
    this.evalFunction = evalFunction;
    this.hierarchy = hierarchy;
  }

  public int getCastCount(Typing typing) {
    this.castCount = 0;
    this.countOnly = true;
    this.typing = typing;
    for (Stmt stmt : body.getStmts()) {
      stmt.accept(this);
    }
    return this.castCount;
  }

  public int getCastCount() {
    return this.castCount;
  }

  public Body insertCastStmts(Typing typing) {
    this.castCount = 0;
    this.countOnly = false;
    // TODO: modifiers later must be added
    // builder = Body.builder(body, Collections.emptySet());
    this.typing = typing;
    List<Stmt> stmts = new ArrayList<>(body.getStmts());
    int size = stmts.size();
    for (int i = 0; i < size; i++) {
      stmts.get(i).accept(this);
    }
    return this.body;
  }

  public LinkedHashSet<Local> getNewLocals() {
    return this.newLocals;
  }

  @Override
  public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
    this.handleInvokeExpr(stmt.getInvokeExpr(), stmt);
  }

  @Override
  public void caseAssignStmt(@Nonnull JAssignStmt stmt) {
    Value lhs = stmt.getLeftOp();
    Value rhs = stmt.getRightOp();
    Type type_lhs = null;
    if (lhs instanceof Local) {
      type_lhs = this.typing.getType((Local) lhs);
    } else if (lhs instanceof JArrayRef) {
      count(((JArrayRef) lhs).getIndex(), PrimitiveType.getInt(), stmt);
      ArrayType arrayType = null;
      Local base = ((JArrayRef) lhs).getBase();
      Type type_base = this.typing.getType(base);
      if (type_base instanceof ArrayType) {
        arrayType = (ArrayType) type_base;
      } else {
        if (rhs instanceof Local) {
          Type type_rhs = this.typing.getType((Local) rhs);
          // if base type of lhs is an object-like-type, retrieve its base type from array
          // allocation site.
          if (TypeUtils.isObjectLikeType(type_base)
              || (TypeUtils.isObject(type_base) && type_rhs instanceof PrimitiveType)) {
            Map<Local, List<Stmt>> defs = BodyUtils.collectDefs(body.getStmts());
            List<Stmt> defStmts = defs.get(base);
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
            if (!findDef) {
              arrayType = TypeUtils.makeArrayType(type_rhs, 1);
            }
          }
        }
        if (arrayType == null) {
          arrayType = TypeUtils.makeArrayType(type_base, 1);
        }
      }
      type_lhs = arrayType.getElementType();
      count(base, arrayType, stmt);
      count(lhs, type_lhs, stmt);
    } else if (lhs instanceof JFieldRef) {
      if (lhs instanceof JInstanceFieldRef) {
        count(
            ((JInstanceFieldRef) lhs).getBase(),
            ((JInstanceFieldRef) lhs).getFieldSignature().getDeclClassType(),
            stmt);
      }
      type_lhs = lhs.getType();
    }

    if (rhs instanceof Local) {
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof JArrayRef) {
      count(((JArrayRef) rhs).getIndex(), PrimitiveType.getInt(), stmt);
      Local base = ((JArrayRef) rhs).getBase();
      ArrayType arrayType = null;
      Type type_base = typing.getType(base);
      if (type_base instanceof ArrayType) {
        arrayType = (ArrayType) type_base;
      } else {
        if (type_base instanceof NullType || TypeUtils.isObjectLikeType(type_base)) {
          Map<Local, List<Stmt>> defs = BodyUtils.collectDefs(body.getStmts());
          Deque<StmtLocalPair> worklist = new ArrayDeque<>();
          Set<StmtLocalPair> visited = new HashSet<>();
          worklist.add(new StmtLocalPair(stmt, base));
          Type sel = null;
          while (!worklist.isEmpty()) {
            StmtLocalPair pair = worklist.removeFirst();
            if (!visited.add(pair)) {
              continue;
            }
            List<Stmt> stmts = defs.get(pair.local);
            for (Stmt s : stmts) {
              if (s instanceof JAssignStmt) {
                Value value = ((JAssignStmt) s).getRightOp();
                if (value instanceof JNewArrayExpr) {
                  sel = selectType(sel, ((JNewArrayExpr) value).getBaseType(), s);
                } else if (value instanceof JNewMultiArrayExpr) {
                  sel = selectType(sel, ((JNewMultiArrayExpr) value).getBaseType(), s);
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
          arrayType = TypeUtils.makeArrayType(sel, 1);
        }
      }
      Type type_rhs = arrayType.getElementType();
      count(base, arrayType, stmt);
      count(rhs, type_rhs, stmt);
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof JInstanceFieldRef) {
      count(
          ((JInstanceFieldRef) rhs).getBase(),
          ((JInstanceFieldRef) rhs).getFieldSignature().getDeclClassType(),
          stmt);
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof AbstractBinopExpr) {
      this.handleBinopExpr((AbstractBinopExpr) rhs, type_lhs, stmt);
    } else if (rhs instanceof AbstractInvokeExpr) {
      this.handleInvokeExpr((AbstractInvokeExpr) rhs, stmt);
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof JCastExpr) {
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof JInstanceOfExpr) {
      count(((JInstanceOfExpr) rhs).getOp(), factory.getType("java.lang.Object"), stmt);
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof JNewArrayExpr) {
      count(((JNewArrayExpr) rhs).getSize(), PrimitiveType.getInt(), stmt);
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof JNewMultiArrayExpr) {
      for (int i = 0; i < ((JNewMultiArrayExpr) rhs).getSizeCount(); i++) {
        count(((JNewMultiArrayExpr) rhs).getSize(i), PrimitiveType.getInt(), stmt);
      }
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof JLengthExpr) {
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof JNegExpr) {
      count(((JNegExpr) rhs).getOp(), type_lhs, stmt);
    } else if (rhs instanceof JNewExpr) {
      count(rhs, type_lhs, stmt);
    } else if (rhs instanceof Constant) {
      if (!(rhs instanceof NullConstant)) {
        count(rhs, type_lhs, stmt);
      }
    }
  }

  @Override
  public void caseEnterMonitorStmt(@Nonnull JEnterMonitorStmt stmt) {
    count(stmt.getOp(), factory.getType("java.lang.Object"), stmt);
  }

  @Override
  public void caseExitMonitorStmt(@Nonnull JExitMonitorStmt stmt) {
    count(stmt.getOp(), factory.getType("java.lang.Object"), stmt);
  }

  @Override
  public void caseIfStmt(@Nonnull JIfStmt stmt) {
    handleBinopExpr(stmt.getCondition(), PrimitiveType.getBoolean(), stmt);
  }

  @Override
  public void caseSwitchStmt(@Nonnull JSwitchStmt stmt) {
    count(stmt.getKey(), PrimitiveType.getInt(), stmt);
  }

  @Override
  public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
    count(stmt.getOp(), body.getMethodSignature().getType(), stmt);
  }

  @Override
  public void caseThrowStmt(@Nonnull JThrowStmt stmt) {
    count(stmt.getOp(), factory.getType("java.lang.Throwable"), stmt);
  }

  /** This method is used to check weather a value in a stmt need a cast. */
  public void count(Value value, Type stdType, Stmt stmt) {
    if (countOnly) {
      Type evaType = evalFunction.evaluate(typing, value, stmt, body);
      if (hierarchy.isAncestor(stdType, evaType)) {
        return;
      }
      this.castCount++;
    } else {
      Stmt oriStmt = stmt;
      Value oriValue = value;
      Stmt updatedStmt = stmt2NewStmt.get(stmt);
      if (updatedStmt != null) {
        stmt = stmt2NewStmt.get(stmt);
      }
      Map<Value, Value> m = changedValues.get(oriStmt);
      if (m != null) {
        Value updatedValue = m.get(value);
        if (updatedValue != null) {
          value = updatedValue;
        }
      }
      Type evaType = evalFunction.evaluate(typing, value, stmt, body);
      if (hierarchy.isAncestor(stdType, evaType)) {
        return;
      }
      this.castCount++;
      Body.BodyBuilder builder = new Body.BodyBuilder(body, Collections.emptySet());

      Local old_local;
      if (value instanceof Local) {
        old_local = (Local) value;
      } else {
        old_local = generateTempLocal(evaType);
        builder.addLocal(old_local);
        this.typing.set(old_local, evaType);
        // todo: later position info should be adjusted
        JAssignStmt newAssign = JavaJimple.newAssignStmt(old_local, value, stmt.getPositionInfo());
        builder.insertStmt(newAssign, stmt);
      }
      Local new_local = generateTempLocal(stdType);
      builder.addLocal(new_local);
      this.typing.set(new_local, stdType);
      addUpdatedValue(oriValue, new_local, oriStmt);
      // todo: later position info should be adjusted
      JAssignStmt newCast =
          JavaJimple.newAssignStmt(
              new_local, JavaJimple.newCastExpr(old_local, stdType), stmt.getPositionInfo());
      builder.insertStmt(newCast, stmt);

      Stmt newStmt;
      if (stmt.getUses().contains(value)) {
        newStmt = BodyUtils.withNewUse(stmt, value, new_local);
      } else {
        newStmt = BodyUtils.withNewDef(stmt, new_local);
      }
      builder.replaceStmt(stmt, newStmt);
      this.stmt2NewStmt.put(oriStmt, newStmt);
      this.body = builder.build();
    }
  }

  private class StmtLocalPair {
    final Stmt stmt;
    final Local local;

    StmtLocalPair(Stmt stmt, Local local) {
      this.stmt = stmt;
      this.local = local;
    }
  }

  // select the type with bigger bit size
  private Type selectType(Type preType, Type newType, Stmt stmt) {
    if (preType == null || preType.equals(newType)) {
      return newType;
    }
    Type sel;
    if (TypeUtils.getValueBitSize(newType) > TypeUtils.getValueBitSize(preType)) {
      sel = newType;
    } else {
      sel = preType;
    }
    logger.warn(
        "Conflicting array types at "
            + stmt
            + " in "
            + body.getMethodSignature()
            + ". Its base type may be "
            + preType
            + " or "
            + newType
            + ". Select: "
            + sel);
    return sel;
  }

  private void handleInvokeExpr(AbstractInvokeExpr expr, Stmt stmt) {
    MethodSignature signature = expr.getMethodSignature();
    if (expr instanceof AbstractInstanceInvokeExpr) {
      count(((AbstractInstanceInvokeExpr) expr).getBase(), signature.getDeclClassType(), stmt);
    }
    for (int i = 0; i < expr.getArgCount(); i++) {
      count(expr.getArg(i), signature.getParameterTypes().get(i), stmt);
    }
  }

  private void handleBinopExpr(AbstractBinopExpr expr, Type type, Stmt stmt) {
    Value op1 = expr.getOp1();
    Value op2 = expr.getOp2();
    Type t1 = evalFunction.evaluate(typing, op1, stmt, body);
    Type t2 = evalFunction.evaluate(typing, op2, stmt, body);
    if (expr instanceof AbstractConditionExpr
        || expr instanceof AbstractFloatBinopExpr
        || expr instanceof JShlExpr
        || expr instanceof JShrExpr
        || expr instanceof JUshrExpr) {
      if (expr instanceof JEqExpr || expr instanceof JNeExpr) {
        if (!(t1 instanceof PrimitiveType.BooleanType && t2 instanceof PrimitiveType.BooleanType)
            && t1 instanceof IntegerType) {
          count(op1, PrimitiveType.getInt(), stmt);
          count(op2, PrimitiveType.getInt(), stmt);
        }
      } else {
        if (type instanceof IntegerType) {
          count(op1, PrimitiveType.getInt(), stmt);
          count(op2, PrimitiveType.getInt(), stmt);
        }
      }
    } else if (expr instanceof JXorExpr || expr instanceof JOrExpr || expr instanceof JAndExpr) {
      count(op1, type, stmt);
      count(op2, type, stmt);
    }
  }

  private void addUpdatedValue(Value oldValue, Value newValue, Stmt stmt) {
    Map<Value, Value> map;
    if (!this.changedValues.containsKey(stmt)) {
      map = new HashMap<>();
      this.changedValues.put(stmt, map);
    } else {
      map = this.changedValues.get(stmt);
    }
    map.put(oldValue, newValue);
    if (stmt instanceof JAssignStmt && stmt.containsArrayRef()) {
      Value leftOp = ((JAssignStmt) stmt).getLeftOp();
      Value rightOp = ((JAssignStmt) stmt).getRightOp();
      if (leftOp instanceof JArrayRef) {
        if (oldValue == leftOp) {
          Local base = ((JArrayRef) oldValue).getBase();
          Local nBase = ((JArrayRef) newValue).getBase();
          map.put(base, nBase);
        } else if (leftOp.getUses().contains(oldValue)) {
          JArrayRef nArrRef = ((JArrayRef) leftOp).withBase((Local) newValue);
          map.put(leftOp, nArrRef);
        }
      } else if (rightOp instanceof JArrayRef) {
        if (oldValue == rightOp) {
          Local base = ((JArrayRef) oldValue).getBase();
          Local nBase = ((JArrayRef) newValue).getBase();
          map.put(base, nBase);
        } else if (rightOp.getUses().contains(oldValue)) {
          JArrayRef nArrRef = ((JArrayRef) rightOp).withBase((Local) newValue);
          map.put(rightOp, nArrRef);
        }
      }
    }
  }

  private String generateLocalTempName() {
    StringBuilder name = new StringBuilder();
    name.append("#l");
    name.append(this.newLocalsCount);
    newLocalsCount++;
    return name.toString();
  }

  private Local generateTempLocal(Type type) {
    String name = generateLocalTempName();
    return JavaJimple.newLocal(name, type);
  }
}
