package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.BodyUtils;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.core.language.JavaJimple;
import java.util.*;

public class CastCounter extends TypeChecker {

  private int castCount = 0;
  private boolean countOnly;
  private Map<Stmt, Map<Value, Value>> changedValues = new HashMap<>();
  private int newLocalsCount = 0;
  public Map<Stmt, Stmt> stmt2NewStmt = new HashMap<>();

  public CastCounter(
      Body.BodyBuilder builder, AugEvalFunction evalFunction, BytecodeHierarchy hierarchy) {
    super(builder, evalFunction, hierarchy);
  }

  public int getCastCount(Typing typing) {
    this.castCount = 0;
    this.countOnly = true;
    setTyping(typing);
    for (Stmt stmt : getBuilder().getStmts()) {
      stmt.accept(this);
    }
    return this.castCount;
  }

  public int getCastCount() {
    return this.castCount;
  }

  public void insertCastStmts(Typing typing) {
    this.castCount = 0;
    this.countOnly = false;
    setTyping(typing);
    List<Stmt> stmts = new ArrayList<>(getBuilder().getStmts());
    int size = stmts.size();
    for (int i = 0; i < size; i++) {
      stmts.get(i).accept(this);
    }
  }

  /** This method is used to check weather a value in a stmt need a cast. */
  public void visit(Value value, Type stdType, Stmt stmt) {
    AugEvalFunction evalFunction = getFuntion();
    BytecodeHierarchy hierarchy = getHierarchy();
    Body body = getBody();
    Typing typing = getTyping();
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
      // TODO: modifiers later must be added

      Local old_local;
      if (value instanceof Local) {
        old_local = (Local) value;
      } else {
        old_local = generateTempLocal(evaType);
        getBuilder().addLocal(old_local);
        typing.set(old_local, evaType);
        // todo: later position info should be adjusted
        JAssignStmt newAssign = JavaJimple.newAssignStmt(old_local, value, stmt.getPositionInfo());
        getBuilder().insertStmt(newAssign, stmt);
      }
      Local new_local = generateTempLocal(stdType);
      getBuilder().addLocal(new_local);
      typing.set(new_local, stdType);
      addUpdatedValue(oriValue, new_local, oriStmt);
      // todo: later position info should be adjusted
      JAssignStmt newCast =
          JavaJimple.newAssignStmt(
              new_local, JavaJimple.newCastExpr(old_local, stdType), stmt.getPositionInfo());
      getBuilder().insertStmt(newCast, stmt);

      Stmt newStmt;
      if (stmt.getUses().contains(value)) {
        newStmt = BodyUtils.withNewUse(stmt, value, new_local);
      } else {
        newStmt = BodyUtils.withNewDef(stmt, new_local);
      }
      BodyUtils.replaceStmtInBuilder(getBuilder(), stmt, newStmt);
      this.stmt2NewStmt.put(oriStmt, newStmt);
      setBody(getBuilder().build());
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
