//
// (c) 2012 University of Luxembourg - Interdisciplinary Centre for
// Security Reliability and Trust (SnT) - All rights reserved
//
// Author: Alexandre Bartel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.swt.soot.core.analysis.SimpleLocalDefs;
import de.upb.swt.soot.core.analysis.SimpleLocalUses;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.*;
import de.upb.swt.soot.core.jimple.tag.Tag;
import de.upb.swt.soot.core.jimple.visitor.AbstractStmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.DoubleOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.FloatOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.IntOpTag;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.LongOpTag;
import de.upb.swt.soot.java.core.types.JavaClassType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class DalvikTyper implements IDalvikTyper {

  private static DalvikTyper dt = null;

  private Set<Constraint> constraints = new HashSet<Constraint>();
  private Map<Value, Type> typed = new HashMap<>();
  private Map<Local, Type> localTyped = new HashMap<Local, Type>();
  private Set<Local> localTemp = new HashSet<Local>();
  private List<LocalObj> localObjList = new ArrayList<LocalObj>();
  private Map<Local, List<LocalObj>> local2Obj = new HashMap<Local, List<LocalObj>>();

  private DalvikTyper() {
  }

  public static DalvikTyper v() {
    if (dt == null) {
      dt = new DalvikTyper();
    }
    return dt;
  }

  public void clear() {
    constraints.clear();
    typed.clear();
    localTyped.clear();
    localTemp.clear();
    localObjList.clear();
    local2Obj.clear();
  }

  @Override
  public void setType(Value value, Type t, boolean isUse) {
    if (IDalvikTyper.DEBUG) {
      if (t instanceof UnknownType) {

        throw new RuntimeException("error: expected concreted type. Got " + t);
      }
    }

    if (value instanceof Local) {
      LocalObj lb = new LocalObj(value, t, isUse);
      localObjList.add(lb);
      Local k = (Local) value;
      if (!local2Obj.containsKey(k)) {
        local2Obj.put(k, new ArrayList<>());
      }
      local2Obj.get(k).add(lb);
    } else {
      // Debug.printDbg(IDalvikTyper.DEBUG, "not instance of local: vb: ", vb, " value: ", vb.getValue(), " class: ",
      // vb.getValue().getClass());
    }
  }

  @Override
  public void addConstraint(Value l, Value r) {
    if (IDalvikTyper.DEBUG) {
      // Debug.printDbg(IDalvikTyper.DEBUG, " [addConstraint] ", l, " < ", r);
      constraints.add(new Constraint(l, r));
    }
  }

  @Override
  public void assignType(final Body.BodyBuilder bodyBuilder) {

    // Debug.printDbg("assignTypes: before: \n", bodyBuilder);

    constraints.clear();
    localObjList.clear();

    final Set<Stmt> todoStmts = new HashSet<>();

    // put constraints:
    for (Stmt stmt : bodyBuilder.getStmts()) {
      StmtVisitor ss = new StmtVisitor() {

        @Override
        public void caseBreakpointStmt(JBreakpointStmt stmt) {
          // nothing
        }

        @Override
        public void caseInvokeStmt(JInvokeStmt stmt) {
          // add constraint
          DalvikTyper.v().setInvokeType(stmt.getInvokeExpr());
        }

        @Override
        public void caseAssignStmt(JAssignStmt stmt) {
          // add constraint
          Value l = stmt.getLeftOp();
          Value r = stmt.getRightOp();

          // size in new array expression is of tye integer
          if (r instanceof JNewArrayExpr) {
            JNewArrayExpr nae = (JNewArrayExpr) r;
            Value sb = nae.getSize();
            if (sb instanceof Local) {
              DalvikTyper.v().setType(sb, PrimitiveType.IntType.getInstance(), true);
            }
          }

          // array index is of type integer
          if (stmt.containsArrayRef()) {
            JArrayRef ar = stmt.getArrayRef();
            Value sb = ar.getIndex();
            if (sb instanceof Local) {
              DalvikTyper.v().setType(sb, PrimitiveType.IntType.getInstance(), true);
            }
          }

          if (l instanceof Local && r instanceof Local) {
            DalvikTyper.v().addConstraint(stmt.getLeftOp(), stmt.getRightOp());
            return;
          }

          if (stmt.containsInvokeExpr()) {
            DalvikTyper.v().setInvokeType(stmt.getInvokeExpr());
          }

          if (r instanceof Local) { // l NOT local
            Type leftType = stmt.getLeftOp().getType();
            if (l instanceof JArrayRef && leftType instanceof UnknownType) {
              // find type later
              todoStmts.add(stmt);
              return;
            }
            DalvikTyper.v().setType(stmt.getRightOp(), leftType, true);
            return;
          }

          if (l instanceof Local) { // r NOT local

            if (r instanceof UntypedConstant) {
              return;
            }


            for (Tag t : stmt.getTags()) {

              if (r instanceof JCastExpr) {
                // do not check tag, since the tag is for the operand of the cast
                break;
              }

              // Debug.printDbg("assign stmt tag: ", stmt, t);
              if (t instanceof IntOpTag) {
                checkExpr(r, PrimitiveType.IntType.getInstance());
                DalvikTyper.v().setType(stmt.getLeftOp(), PrimitiveType.IntType.getInstance(), false);
                return;
              } else if (t instanceof FloatOpTag) {
                checkExpr(r, PrimitiveType.FloatType.getInstance());
                DalvikTyper.v().setType(stmt.getLeftOp(), PrimitiveType.FloatType.getInstance(), false);
                return;
              } else if (t instanceof DoubleOpTag) {
                checkExpr(r, PrimitiveType.DoubleType.getInstance());
                DalvikTyper.v().setType(stmt.getLeftOp(), PrimitiveType.DoubleType.getInstance(), false);
                return;
              } else if (t instanceof LongOpTag) {
                checkExpr(r, PrimitiveType.LongType.getInstance());
                DalvikTyper.v().setType(stmt.getLeftOp(), PrimitiveType.LongType.getInstance(), false);
                return;
              }
            }
            Type rightType = stmt.getRightOp().getType();
            if (r instanceof JArrayRef && rightType instanceof UnknownType) {
              // find type later
              todoStmts.add(stmt);
              return;
            } else if (r instanceof JCastExpr) {
              JCastExpr ce = (JCastExpr) r;
              Type castType = ce.getType();
              if (castType instanceof PrimitiveType) {
                // check incoming primitive type
                for (Tag t : stmt.getTags()) {
                  // Debug.printDbg("assign primitive type from stmt tag: ", stmt, t);
                  if (t instanceof IntOpTag) {
                    DalvikTyper.v().setType(ce.getOp(), PrimitiveType.IntType.getInstance(), false);
                    return;
                  } else if (t instanceof FloatOpTag) {
                    DalvikTyper.v().setType(ce.getOp(), PrimitiveType.FloatType.getInstance(), false);
                    return;
                  } else if (t instanceof DoubleOpTag) {
                    DalvikTyper.v().setType(ce.getOp(), PrimitiveType.DoubleType.getInstance(), false);
                    return;
                  } else if (t instanceof LongOpTag) {
                    DalvikTyper.v().setType(ce.getOp(), PrimitiveType.LongType.getInstance(), false);
                    return;
                  }
                }
              } else {
                // incoming type is object
                DalvikTyper.v().setType(ce.getOp(), new JavaClassType("java.lang.Object"), false);
              }
            }
            DalvikTyper.v().setType(stmt.getLeftOp(), rightType, false);
            return;
          }

        }

        @Override
        public void caseIdentityStmt(JIdentityStmt stmt) {
          DalvikTyper.v().setType(stmt.getLeftOp(), stmt.getRightOp().getType(), false);

        }

        @Override
        public void caseEnterMonitorStmt(JEnterMonitorStmt stmt) {
          // add constraint
          DalvikTyper.v().setType(stmt.getOp(), new JavaClassType("java.lang.Object"), true);

        }

        @Override
        public void caseExitMonitorStmt(JExitMonitorStmt stmt) {
          // add constraint
          DalvikTyper.v().setType(stmt.getOp(), new JavaClassType("java.lang.Object"), true);
        }

        @Override
        public void caseGotoStmt(JGotoStmt stmt) {
          // nothing

        }

        @Override
        public void caseIfStmt(JIfStmt stmt) {
          // add constraint
          Value c = stmt.getCondition();
          if (c instanceof AbstractBinopExpr) {
            AbstractBinopExpr bo = (AbstractBinopExpr) c;
            Value op1 = bo.getOp1();
            Value op2 = bo.getOp2();
            if (op1 instanceof Local && op2 instanceof Local) {
              DalvikTyper.v().addConstraint(bo.getOp1(), bo.getOp2());
            }
          }

        }

        @Override
        public void caseSwitchStmt(JSwitchStmt stmt) {
          // add constraint
          DalvikTyper.v().setType(stmt.getKey(), PrimitiveType.IntType.getInstance(), true);

        }

        @Override
        public void caseNopStmt(JNopStmt stmt) {
          // nothing

        }

        @Override
        public void caseRetStmt(JRetStmt stmt) {
          // nothing

        }

        @Override
        public void caseReturnStmt(JReturnStmt stmt) {
          // add constraint
          DalvikTyper.v().setType(stmt.getOp(), bodyBuilder.getMethodSignature().getType(), true);

        }

        @Override
        public void caseReturnVoidStmt(JReturnVoidStmt stmt) {
          // nothing

        }

        @Override
        public void caseThrowStmt(JThrowStmt stmt) {
          // add constraint
          DalvikTyper.v().setType(stmt.getOp(), new JavaClassType("java.lang.Object"), true);

        }

        @Override
        public void defaultCaseStmt(Stmt stmt) {
          throw new RuntimeException("error: unknown statement: " + stmt);

        }

      };
      stmt.accept(ss);
    }

    // print todo list:
    // <com.admob.android.ads.q: void a(android.os.Bundle,java.lang.String,java.lang.Object)>
    if (!todoStmts.isEmpty()) {

      // propagate array types
      StmtGraph stmtGraph = bodyBuilder.getStmtGraph();
      SimpleLocalDefs sld = new SimpleLocalDefs(bodyBuilder);
      SimpleLocalUses slu = new SimpleLocalUses(bodyBuilder, sld);

      for (Stmt s : bodyBuilder.getStmts()) {
        if (s instanceof AbstractDefinitionStmt) {
          // Debug.printDbg("U: ", u);
          AbstractDefinitionStmt ass = (AbstractDefinitionStmt) s;
          Value r = ass.getRightOp();

          if (r instanceof UntypedConstant) {
            continue;
          }

          Type rType = r.getType();
          if (rType instanceof ArrayType && ass.getLeftOp() instanceof Local) {
            // Debug.printDbg("propagate-array: checking ", u);
            // propagate array type through aliases
            Set<Stmt> done = new HashSet<Stmt>();
            Set<AbstractDefinitionStmt> toDo = new HashSet<>();
            toDo.add(ass);
            while (!toDo.isEmpty()) {
              AbstractDefinitionStmt currentUnit = toDo.iterator().next();
              if (done.contains(currentUnit)) {
                toDo.remove(currentUnit);
                continue;
              }
              done.add(currentUnit);

              for (Pair<Stmt, Value> uvbp : slu.getUsesOf(currentUnit)) {
                Stmt use = uvbp.getKey();
                Value l2;
                Value r2;
                if (use instanceof JAssignStmt) {
                  JAssignStmt ass2 = (JAssignStmt) use;
                  l2 = ass2.getLeftOp();
                  r2 = ass2.getRightOp();
                  if (!(l2 instanceof Local) || !(r2 instanceof Local || r2 instanceof JArrayRef)) {
                    // Debug.printDbg("propagate-array: skipping ", use);
                    continue;
                  }

                  Type newType;
                  if (r2 instanceof Local) {
                    List<LocalObj> lobjs = local2Obj.get(r2);
                    newType = lobjs.get(0).t;

                  } else if (r2 instanceof JArrayRef) {

                    JArrayRef ar = (JArrayRef) r2;
                    // skip if use is in index
                    if (ar.getIndex() == currentUnit.getLeftOp()) {
                      // Debug.printDbg("skipping since local is used as index...");
                      continue;
                    }

                    Local arBase = ar.getBase();
                    List<LocalObj> lobjs = local2Obj.get(arBase);
                    Type baseT = lobjs.get(0).t;
                    if (baseT.toString().equals(("java.lang.Object"))) {
                      // look for an array type, because an TTT[] is also an Object...
                      ArrayType aTypeOtherThanObject = null;
                      for (LocalObj lo : local2Obj.get(arBase)) {
                        if (lo.t instanceof ArrayType) {
                          aTypeOtherThanObject = (ArrayType) lo.t;
                        }
                      }
                      if (aTypeOtherThanObject == null) {
                        throw new RuntimeException(
                            "error: did not found array type for base " + arBase + " " + local2Obj.get(arBase) + " \n " + bodyBuilder);
                      }
                      baseT = aTypeOtherThanObject;
                    }

                    ArrayType at = (ArrayType) baseT;
                    newType = at.getBaseType();
                  } else {
                    throw new RuntimeException("error: expected Local or ArrayRef. Got " + r2);
                  }

                  toDo.add((AbstractDefinitionStmt) use);
                  DalvikTyper.v().setType(ass2.getLeftOp(), newType, true);
                }
              }
            }

          }
        }
      }

      while (!todoStmts.isEmpty()) {
        Stmt s = todoStmts.iterator().next();
        if (!(s instanceof JAssignStmt)) {
          throw new RuntimeException("error: expecting assign stmt. Got " + s);
        }
        JAssignStmt ass = (JAssignStmt) s;
        Value l = ass.getLeftOp();
        Value r = ass.getRightOp();
        JArrayRef ar;
        Local loc = null;
        if (l instanceof JArrayRef) {
          ar = (JArrayRef) l;
          loc = (Local) r;
        } else if (r instanceof JArrayRef) {
          ar = (JArrayRef) r;
          loc = (Local) l;
        } else {
          throw new RuntimeException("error: expecting an array ref. Got " + s);
        }

        Local baselocal = ar.getBase();
        if (!local2Obj.containsKey(baselocal)) {
          // Debug.printDbg("oups no baselocal! for ", u);
          // Debug.printDbg("bodyBuilder: ", bodyBuilder.getMethod(), " \n", bodyBuilder);
          throw new RuntimeException("oups");
        }

        Type baseT = local2Obj.get(baselocal).get(0).t;
        if (baseT.toString().equals(("java.lang.Object"))) {
          // look for an array type, because an TTT[] is also an Object...
          ArrayType aTypeOtherThanObject = null;
          for (LocalObj lo : local2Obj.get(baselocal)) {
            if (lo.t instanceof ArrayType) {
              aTypeOtherThanObject = (ArrayType) lo.t;
            }
          }
          if (aTypeOtherThanObject == null) {
            throw new RuntimeException(
                "did not found array type for base " + baselocal + " " + local2Obj.get(baselocal) + " \n " + bodyBuilder);
          }
          baseT = aTypeOtherThanObject;
        }
        ArrayType basetype = (ArrayType) baseT;

        // Debug.printDbg("v: ", ar, " base:", ar.getBase(), " base type: ", basetype, " type: ", ar.getType());

        Type t = basetype.getBaseType();
        if (t instanceof UnknownType) {
          todoStmts.add(s);
          continue;
        } else {
          DalvikTyper.v().setType(ar == l ? ass.getRightOp() : ass.getLeftOp(), t, true);
          todoStmts.remove(s);
        }

      }

      // throw new RuntimeException("ouppppp");
    }

    // Debug.printDbg(IDalvikTyper.DEBUG, "list of constraints:");
    List<Value> usesAndDefs = new ArrayList<>();
    usesAndDefs.addAll(bodyBuilder.getUses());
    usesAndDefs.addAll(bodyBuilder.getDefs());

    // clear constraints after local splitting and dead code eliminator
    List<Constraint> toRemove = new ArrayList<Constraint>();
    for (Constraint c : constraints) {
      if (!usesAndDefs.contains(c.l)) {
        // Debug.printDbg(IDalvikTyper.DEBUG, "warning: ", c.l, " not in locals! removing...");
        toRemove.add(c);
        continue;
      }
      if (!usesAndDefs.contains(c.r)) {
        // Debug.printDbg(IDalvikTyper.DEBUG, "warning: ", c.r, " not in locals! removing...");
        toRemove.add(c);
        continue;
      }
    }
    for (Constraint c : toRemove) {
      constraints.remove(c);
    }

    // keep only valid locals
    for (LocalObj lo : localObjList) {
      if (!usesAndDefs.contains(lo)) {
        // Debug.printDbg(IDalvikTyper.DEBUG, " -- removing vb: ", lo.vb, " with type ", lo.t);
        continue;
      }
      Local l = lo.getLocal();
      Type t = lo.t;
      if (localTemp.contains(l) && lo.isUse) {
        // Debug.printDbg(IDalvikTyper.DEBUG, " /!\\ def already added for local ", l, "! for vb: ", lo.vb);
      } else {
        // Debug.printDbg(IDalvikTyper.DEBUG, " * add type ", t, " to local ", l, " for vb: ", lo.vb);
        localTemp.add(l);
        typed.put(lo.value, t);
      }
    }
    for (Value vb : typed.keySet()) {
      if (vb instanceof Local) {
        Local l = (Local) vb;
        localTyped.put(l, typed.get(vb));
      }
    }

    for (Constraint c : constraints) {
      // Debug.printDbg(IDalvikTyper.DEBUG, " -> constraint: ", c);
      for (Value vb : typed.keySet()) {
        // Debug.printDbg(IDalvikTyper.DEBUG, " typed: ", vb, " -> ", typed.get(vb));
      }
    }
    for (Local l : localTyped.keySet()) {
      // Debug.printDbg(IDalvikTyper.DEBUG, " localTyped: ", l, " -> ", localTyped.get(l));
    }

    while (!constraints.isEmpty()) {
      boolean update = false;
      for (Constraint constraint : constraints) {
        // Debug.printDbg(IDalvikTyper.DEBUG, "current constraint: ", c);
        Value l = constraint.l;
        Value r = constraint.r;
        if (l instanceof Local && r instanceof Constant) {
          Constant cst = (Constant) r;
          if (!localTyped.containsKey(l)) {
            continue;
          }
          Type lt = localTyped.get(l);
          // Debug.printDbg(IDalvikTyper.DEBUG, "would like to set type ", lt, " to constant: ", c);
          Value newValue = null;
          if (lt instanceof PrimitiveType.IntType || lt instanceof PrimitiveType.BooleanType || lt instanceof PrimitiveType.ShortType || lt instanceof PrimitiveType.CharType
              || lt instanceof PrimitiveType.ByteType) {
            UntypedIntOrFloatConstant uf = (UntypedIntOrFloatConstant) cst;
            newValue = uf.toIntConstant();
          } else if (lt instanceof PrimitiveType.FloatType) {
            UntypedIntOrFloatConstant uf = (UntypedIntOrFloatConstant) cst;
            newValue = uf.toFloatConstant();
          } else if (lt instanceof PrimitiveType.DoubleType) {
            UntypedLongOrDoubleConstant ud = (UntypedLongOrDoubleConstant) cst;
            newValue = ud.toDoubleConstant();
          } else if (lt instanceof PrimitiveType.LongType) {
            UntypedLongOrDoubleConstant ud = (UntypedLongOrDoubleConstant) cst;
            newValue = ud.toLongConstant();
          } else {
            if (cst instanceof UntypedIntOrFloatConstant && ((UntypedIntOrFloatConstant) cst).value == 0) {
              newValue = NullConstant.getInstance();
              // Debug.printDbg("new null constant for constraint ", c, " with l type: ", localTyped.get(l));
            } else {
              throw new RuntimeException("unknow type for constance: " + lt);
            }
          }
          constraint.r = newValue;

          // Debug.printDbg(IDalvikTyper.DEBUG, "remove constraint: ", c);
          constraints.remove(constraint);
          update = true;
          break;
        } else if (l instanceof Local && r instanceof Local) {
          Local leftLocal = (Local) l;
          Local rightLocal = (Local) r;
          if (localTyped.containsKey(leftLocal)) {
            Type leftLocalType = localTyped.get(leftLocal);
            if (!localTyped.containsKey(rightLocal)) {
              // Debug.printDbg(IDalvikTyper.DEBUG, "set type ", leftLocalType, " to local ", rightLocal);
              rightLocal = rightLocal.withType(leftLocalType);
              setLocalTyped(rightLocal, leftLocalType);
            }
            // Debug.printDbg(IDalvikTyper.DEBUG, "remove constraint: ", c);
            constraints.remove(constraint);
            update = true;
            break;
          } else if (localTyped.containsKey(rightLocal)) {
            Type rightLocalType = localTyped.get(rightLocal);
            if (!localTyped.containsKey(leftLocal)) {
              // Debug.printDbg(IDalvikTyper.DEBUG, "set type ", rightLocalType, " to local ", leftLocal);
              leftLocal = leftLocal.withType(rightLocalType);
              setLocalTyped(leftLocal, rightLocalType);
            }
            // Debug.printDbg(IDalvikTyper.DEBUG, "remove constraint: ", c);
            constraints.remove(constraint);
            update = true;
            break;
          }
        } else if (l instanceof JArrayRef && r instanceof Local) {
          Local rightLocal = (Local) r;
          JArrayRef ar = (JArrayRef) l;
          Local base = (Local) ar.getBase();
          // Debug.printDbg(IDalvikTyper.DEBUG, "base: ", base);
          // Debug.printDbg(IDalvikTyper.DEBUG, "index: ", ar.getIndex());
          if (localTyped.containsKey(base)) {
            Type t = localTyped.get(base);

            // Debug.printDbg(IDalvikTyper.DEBUG, "type of local1: ", t, " ", t.getClass());
            Type elementType = null;
            if (t instanceof ArrayType) {
              ArrayType at = (ArrayType) t;
              elementType = at.getBaseType();
            } else {
              continue;
            }

            if (!localTyped.containsKey(rightLocal)) {
              // Debug.printDbg(IDalvikTyper.DEBUG, "set type ", elementType, " to local ", r);
              rightLocal = rightLocal.withType(elementType);
              setLocalTyped(rightLocal, elementType);
            }
            // Debug.printDbg(IDalvikTyper.DEBUG, "remove constraint: ", c);
            constraints.remove(constraint);
            update = true;
            break;
          }
        } else if (l instanceof Local && r instanceof JArrayRef) {
          Local leftLocal = (Local) l;
          JArrayRef ar = (JArrayRef) r;
          Local base = (Local) ar.getBase();
          if (localTyped.containsKey(base)) {
            Type t = localTyped.get(base);

            // Debug.printDbg(IDalvikTyper.DEBUG, "type of local2: ", t, " ", t.getClass());
            Type elementType = null;
            if (t instanceof ArrayType) {
              ArrayType at = (ArrayType) t;
              elementType = at.getBaseType();
            } else {
              continue;
            }

            if (!localTyped.containsKey(leftLocal)) {
              // Debug.printDbg(IDalvikTyper.DEBUG, "set type ", elementType, " to local ", l);
              leftLocal = leftLocal.withType(elementType);
              setLocalTyped(leftLocal, elementType);
            }
            // Debug.printDbg(IDalvikTyper.DEBUG, "remove constraint: ", c);
            constraints.remove(constraint);
            update = true;
            break;
          }
        } else {
          throw new RuntimeException("error: do not handling this kind of constraint: " + constraint);
        }
      }
      if (!update) {
        break;
      }
    }

    List<Stmt> stmts = bodyBuilder.getStmts();
    for(int i=0; i<stmts.size(); i++){
      Stmt s = stmts.get(i);
      if (!(s instanceof JAssignStmt)) {
        continue;
      }
      JAssignStmt ass = (JAssignStmt) s;
      if (!(ass.getLeftOp() instanceof Local)) {
        continue;
      }
      if (!(ass.getRightOp() instanceof UntypedConstant)) {
        continue;
      }
      UntypedConstant uc = (UntypedConstant) ass.getRightOp();
      ass = ass.withRValue(uc.defineType(localTyped.get(ass.getLeftOp())));
      // TODO: is this the correct way to update a stmt in body?
      stmts.set(i, ass);
    }

    // At this point some constants may be untyped.
    // (for instance if it is only use in a if condition).
    // We assume type in integer.
    //
    for (Constraint constraint : constraints) {
      // Debug.printDbg(IDalvikTyper.DEBUG, "current constraint: ", c);
      Value l = constraint.l;
      Value r = constraint.r;
      if (l instanceof Local && r instanceof Constant) {
        if (r instanceof UntypedIntOrFloatConstant) {
          UntypedIntOrFloatConstant cst = (UntypedIntOrFloatConstant) r;
          Value newValue = null;
          if (cst.value != 0) {
            // Debug.printDbg(IDalvikTyper.DEBUG, "[untyped constaints] set type int to non zero constant: ", c, " = ",
            // cst.value);
            newValue = cst.toIntConstant();
          } else { // check if used in cast, just in case...
            for (Stmt s : bodyBuilder.getStmts()) {
              for (Value v1 : s.getUses()) {
                if (v1 == l) {
                  // Debug.printDbg("local used in ", u);
                  if (s instanceof JAssignStmt) {
                    JAssignStmt a = (JAssignStmt) s;
                    Value right = a.getRightOp();
                    if (right instanceof JCastExpr) {
                      newValue = NullConstant.getInstance();
                    } else {
                      newValue = cst.toIntConstant();
                    }
                  } else if (s instanceof JIfStmt) {
                    newValue = cst.toIntConstant();// TODO check this better
                  }
                }
              }
            }
          }
          if (newValue == null) {
            throw new RuntimeException("error: no type found for local: " + l);
          }
          constraint.r = newValue;
        } else if (r instanceof UntypedLongOrDoubleConstant) {
          // Debug.printDbg(IDalvikTyper.DEBUG, "[untyped constaints] set type long to constant: ", c);
          Value newValue = ((UntypedLongOrDoubleConstant) r).toLongConstant();
          constraint.r = newValue;
        }
      }
    }

    // fix untypedconstants which have flown to an array index
    List<Stmt> stmtList = bodyBuilder.getStmts();
    for (int i=0; i<stmtList.size(); i++) {
      Stmt stmt = stmtList.get(i);
      AbstractStmtVisitor<Stmt> sw = new AbstractStmtVisitor() {
        @Override
        public void caseInvokeStmt(JInvokeStmt stmt) {
          AbstractInvokeExpr invoke = changeUntypedConstantsInInvoke(stmt.getInvokeExpr());
          setResult(stmt.withInvokeExpr(invoke));
        }

        @Override
        public void caseAssignStmt(JAssignStmt stmt) {
          if (stmt.getRightOp() instanceof JNewArrayExpr) {
            JNewArrayExpr nae = (JNewArrayExpr) stmt.getRightOp();
            if (nae.getSize() instanceof UntypedConstant) {
              UntypedIntOrFloatConstant uc = (UntypedIntOrFloatConstant) nae.getSize();
              nae = nae.withSize(uc.defineType(PrimitiveType.IntType.getInstance()));
              setResult(stmt.withRValue(nae));
            }
          } else if (stmt.getRightOp() instanceof UntypedConstant) {
            UntypedConstant uc = (UntypedConstant) stmt.getRightOp();
            Value l = stmt.getLeftOp();
            Type lType = null;
            if (l instanceof JArrayRef) {
              JArrayRef ar = (JArrayRef) l;
              Local baseLocal = (Local) ar.getBase();
              ArrayType arrayType = (ArrayType) localTyped.get(baseLocal);
              lType = arrayType.getBaseType();
            } else {
              lType = l.getType();
            }
            stmt = stmt.withRValue(uc.defineType(lType));
            setResult(stmt);
          } else if (stmt.getRightOp() instanceof AbstractInvokeExpr) {
            AbstractInvokeExpr invoke = changeUntypedConstantsInInvoke((AbstractInvokeExpr) stmt.getRightOp());
            stmt = stmt.withRValue(invoke);
            setResult(stmt);
          }
          if (!stmt.containsArrayRef()) {
            return;
          }
          JArrayRef ar = stmt.getArrayRef();
          if ((ar.getIndex() instanceof UntypedConstant)) {
            UntypedIntOrFloatConstant uc = (UntypedIntOrFloatConstant) ar.getIndex();
            ar = ar.withIndex(uc.toIntConstant());
            setResult(stmt.withArrayRef(ar));
          }

          if (stmt.getLeftOp() instanceof JArrayRef && stmt.getRightOp() instanceof UntypedConstant) {
            UntypedConstant uc = (UntypedConstant) stmt.getRightOp();
            Local baseLocal = (Local) stmt.getArrayRef().getBase();
            ArrayType lType = (ArrayType) localTyped.get(baseLocal);
            Type elemType = lType.getBaseType();
            setResult(stmt.withRValue(uc.defineType(elemType)));
          }

        }

        @Override
        public void caseIfStmt(JIfStmt stmt) {
          Value c = stmt.getCondition();
          if (c instanceof AbstractBinopExpr) {
            AbstractBinopExpr be = (AbstractBinopExpr) c;
            Value op1 = be.getOp1();
            Value op2 = be.getOp2();
            if (op1 instanceof UntypedConstant || op2 instanceof UntypedConstant) {
              // Debug.printDbg("if to handle: ", stmt);

              if (op1 instanceof Local) {
                Type t = localTyped.get(op1);
                // Debug.printDbg("if op1 type: ", t);
                UntypedConstant uc = (UntypedConstant) op2;
                be = be.withOp2(uc.defineType(t));
                setResult(stmt.withCondition((AbstractConditionExpr) be));
              } else if (op2 instanceof Local) {
                Type t = localTyped.get(op2);
                // Debug.printDbg("if op2 type: ", t);
                UntypedConstant uc = (UntypedConstant) op1;
                be = be.withOp1(uc.defineType(t));
                setResult(stmt.withCondition((AbstractConditionExpr) be));
              } else if (op1 instanceof UntypedConstant && op2 instanceof UntypedConstant) {
                if (op1 instanceof UntypedIntOrFloatConstant && op2 instanceof UntypedIntOrFloatConstant) {
                  UntypedIntOrFloatConstant uc1 = (UntypedIntOrFloatConstant) op1;
                  UntypedIntOrFloatConstant uc2 = (UntypedIntOrFloatConstant) op2;
                  be = be.withOp1(uc1.toIntConstant()); // to int or float, it does not matter
                  be = be.withOp2(uc2.toIntConstant());
                  setResult(stmt.withCondition((AbstractConditionExpr) be));
                } else if (op1 instanceof UntypedLongOrDoubleConstant && op2 instanceof UntypedLongOrDoubleConstant) {
                  UntypedLongOrDoubleConstant uc1 = (UntypedLongOrDoubleConstant) op1;
                  UntypedLongOrDoubleConstant uc2 = (UntypedLongOrDoubleConstant) op2;
                  be = be.withOp1(uc1.toLongConstant()); // to long or double, it does not matter
                  be = be.withOp2(uc2.toLongConstant());
                  setResult(stmt.withCondition((AbstractConditionExpr) be));
                } else {
                  throw new RuntimeException("error: expected same type of untyped constants. Got " + stmt);
                }
              } else if (op1 instanceof UntypedConstant || op2 instanceof UntypedConstant) {
                if (op1 instanceof UntypedConstant) {
                  UntypedConstant uc = (UntypedConstant) op1;
                  be = be.withOp1(uc.defineType(op2.getType()));
                  setResult(stmt.withCondition((AbstractConditionExpr) be));
                } else if (op2 instanceof UntypedConstant) {
                  UntypedConstant uc = (UntypedConstant) op2;
                  be = be.withOp2(uc.defineType(op1.getType()));
                  setResult(stmt.withCondition((AbstractConditionExpr) be));
                }
              } else {
                throw new RuntimeException("error: expected local/untyped untyped/local or untyped/untyped. Got " + stmt);
              }
            }
          } else if (c instanceof AbstractUnopExpr) {

          } else {
            throw new RuntimeException("error: expected binop or unop. Got " + stmt);
          }

        }

        @Override
        public void caseReturnStmt(JReturnStmt stmt) {
          if (stmt.getOp() instanceof UntypedConstant) {
            UntypedConstant uc = (UntypedConstant) stmt.getOp();
            Type type = bodyBuilder.getMethodSignature().getType();
            stmt = stmt.withReturnValue((Immediate) uc.defineType(type));
            setResult(stmt);
          }
        }
      };
      stmt.accept(sw);
      // TODO: setting stmt correct?
      stmtList.set(i, sw.getResult());
    }

    // fix untyped constants remaining

    // Debug.printDbg("assignTypes: after: \n", bodyBuilder);

  }

  private AbstractInvokeExpr changeUntypedConstantsInInvoke(AbstractInvokeExpr invokeExpr) {
    List<Immediate> args;
    if(invokeExpr.getArgCount()>0){
      args = new ArrayList<>();
      for (int i = 0; i < invokeExpr.getArgCount(); i++) {
        Immediate v = invokeExpr.getArg(i);
        if (!(v instanceof UntypedConstant)) {
          args.add(v);
          continue;
        }
        Type t = invokeExpr.getMethodSignature().getParameterTypes().get(i);
        UntypedConstant uc = (UntypedConstant) v;
        args.add(uc.defineType(t));
      }
      return invokeExpr.withArgs(args);
    }
    return invokeExpr;
  }

  protected void checkExpr(Value v, Type t) {
    List<Value> uses = v.getUses();
    for (int i=0; i<uses.size(); i++) {
      Value useValue = uses.get(i);
      if (useValue instanceof Local) {
        // special case where the second operand is always of type integer
        if ((v instanceof JShrExpr || v instanceof JShlExpr || v instanceof JUshrExpr) && ((AbstractBinopExpr) v).getOp2() == useValue) {
          // Debug.printDbg("setting type of operand two of shift expression to integer", value);
          DalvikTyper.v().setType(useValue, PrimitiveType.IntType.getInstance(), true);
          continue;
        }
        DalvikTyper.v().setType(useValue, t, true);
      } else if (useValue instanceof UntypedConstant) {

        UntypedConstant uc = (UntypedConstant) useValue;

        // special case where the second operand is always of type integer
        if ((v instanceof JShrExpr || v instanceof JShlExpr || v instanceof JUshrExpr) && ((AbstractBinopExpr) v).getOp2() == useValue) {
          UntypedIntOrFloatConstant ui = (UntypedIntOrFloatConstant) uc;
          useValue = ui.toIntConstant();
          uses.set(i, useValue);
          continue;
        }
        useValue = uc.defineType(t);
        uses.set(i, useValue);
      }
    }
  }

  protected void setInvokeType(AbstractInvokeExpr invokeExpr) {
    for (int i = 0; i < invokeExpr.getArgCount(); i++) {
      Value v = invokeExpr.getArg(i);
      if (!(v instanceof Local)) {
        continue;
      }
      Type t = invokeExpr.getMethodSignature().getParameterTypes().get(i);
      DalvikTyper.v().setType(invokeExpr.getArg(i), t, true);
    }
    if (invokeExpr instanceof JStaticInvokeExpr) {
      // nothing to do
    } else if (invokeExpr instanceof AbstractInstanceInvokeExpr) {
      AbstractInstanceInvokeExpr iie = (AbstractInstanceInvokeExpr) invokeExpr;
      DalvikTyper.v().setType(iie.getBase(), new JavaClassType("java.lang.Object"), true);
    } else if (invokeExpr instanceof JDynamicInvokeExpr) {
      JDynamicInvokeExpr die = (JDynamicInvokeExpr) invokeExpr;
      // ?
    } else {
      throw new RuntimeException("error: unhandled invoke expression: " + invokeExpr + " " + invokeExpr.getClass());
    }

  }

  private void setLocalTyped(Local l, Type t) {
    localTyped.put(l, t);
  }

  class LocalObj {
    Value value;
    Type t;
    // private Local l;
    boolean isUse;

    public LocalObj(Value value, Type t, boolean isUse) {
      this.value = value;
      // this.l = (Local)vb.getValue();
      this.t = t;
      this.isUse = isUse;
    }

    public Local getLocal() {
      return (Local) value;
    }

  }

  class Constraint {
    Value l;
    Value r;

    public Constraint(Value l, Value r) {
      this.l = l;
      this.r = r;
    }

    @Override
    public String toString() {
      return l + " < " + r;
    }
  }

  // this is needed because UnuesedStatementTransformer checks types in the div expressions
  public void typeUntypedConstrantInDiv(final Body.BodyBuilder bodyBuilder) {
    List<Stmt> stmts = bodyBuilder.getStmts();
    for (int i=0; i<stmts.size(); i++) {
      Stmt s = stmts.get(i);
      AbstractStmtVisitor<Stmt> sw = new AbstractStmtVisitor<Stmt>() {
        @Override
        public void caseInvokeStmt(JInvokeStmt stmt) {
          AbstractInvokeExpr invoke = changeUntypedConstantsInInvoke(stmt.getInvokeExpr());
          setResult(stmt.withInvokeExpr(invoke));
        }

        @Override
        public void caseAssignStmt(JAssignStmt stmt) {
          if (stmt.getRightOp() instanceof JNewArrayExpr) {
            JNewArrayExpr nae = (JNewArrayExpr) stmt.getRightOp();
            if (nae.getSize() instanceof UntypedConstant) {
              UntypedIntOrFloatConstant uc = (UntypedIntOrFloatConstant) nae.getSize();
              nae = nae.withSize(uc.defineType(PrimitiveType.IntType.getInstance()));
              setResult(stmt.withRValue(nae));
            }
          } else if (stmt.getRightOp() instanceof AbstractInvokeExpr) {
            AbstractInvokeExpr invoke = changeUntypedConstantsInInvoke((AbstractInvokeExpr) stmt.getRightOp());
            setResult(stmt.withRValue(invoke));
          } else if (stmt.getRightOp() instanceof JCastExpr) {
            JCastExpr ce = (JCastExpr) stmt.getRightOp();
            if (ce.getOp() instanceof UntypedConstant) {
              UntypedConstant uc = (UntypedConstant) ce.getOp();
              // check incoming primitive type
              for (Tag t : stmt.getTags()) {
                // Debug.printDbg("assign primitive type from stmt tag: ", stmt, t);
                if (t instanceof IntOpTag) {
                  ce = ce.withOp(uc.defineType(PrimitiveType.IntType.getInstance()));
                  setResult(stmt.withRValue(ce));
                  return;
                } else if (t instanceof FloatOpTag) {
                  ce = ce.withOp(uc.defineType(PrimitiveType.FloatType.getInstance()));
                  setResult(stmt.withRValue(ce));
                  return;
                } else if (t instanceof DoubleOpTag) {
                  ce = ce.withOp(uc.defineType(PrimitiveType.DoubleType.getInstance()));
                  setResult(stmt.withRValue(ce));
                  return;
                } else if (t instanceof LongOpTag) {
                  ce = ce.withOp(uc.defineType(PrimitiveType.LongType.getInstance()));
                  setResult(stmt.withRValue(ce));
                  return;
                }
              }

              // 0 -> null
              ce = ce.withOp(uc.defineType(new JavaClassType("java.lang.Object")));
              setResult(stmt.withRValue(ce));
            }
          }

          if (stmt.containsArrayRef()) {
            JArrayRef ar = stmt.getArrayRef();
            if ((ar.getIndex() instanceof UntypedConstant)) {
              UntypedIntOrFloatConstant uc = (UntypedIntOrFloatConstant) ar.getIndex();
              ar = ar.withIndex(uc.toIntConstant());
              setResult(stmt.withArrayRef(ar));
            }
          }

          Value r = stmt.getRightOp();
          if (r instanceof JDivExpr || r instanceof JRemExpr) {
            // DivExpr de = (DivExpr) r;

            for (Tag t : stmt.getTags()) {
              // Debug.printDbg("div stmt tag: ", stmt, t);
              if (t instanceof IntOpTag) {
                checkExpr(r, PrimitiveType.IntType.getInstance());
                return;
              } else if (t instanceof FloatOpTag) {
                checkExpr(r, PrimitiveType.FloatType.getInstance());
                return;
              } else if (t instanceof DoubleOpTag) {
                checkExpr(r, PrimitiveType.DoubleType.getInstance());
                return;
              } else if (t instanceof LongOpTag) {
                checkExpr(r, PrimitiveType.LongType.getInstance());
                return;
              }
            }
          }
        }

        @Override
        public void caseReturnStmt(JReturnStmt stmt) {
          if (stmt.getOp() instanceof UntypedConstant) {
            UntypedConstant uc = (UntypedConstant) stmt.getOp();
            Type type = bodyBuilder.getMethodSignature().getType();
            Immediate defineType = uc.defineType(type);
            setResult(stmt.withReturnValue(defineType));
          }
        }

        @Override
        public void caseThrowStmt(JThrowStmt stmt) {
          if (stmt.getOp() instanceof UntypedConstant) {
            UntypedConstant uc = (UntypedConstant) stmt.getOp();
            Immediate defineType = uc.defineType(new JavaClassType("java.lang.Object"));
            stmt = stmt.withOp(defineType);
            setResult(stmt);
          }
        }
      };
      s.accept(sw);
      stmts.set(i, sw.getResult());
    }
  }

}
