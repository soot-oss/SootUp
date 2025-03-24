package sootup.apk.frontend.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Markus Schmidt and others
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
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.views.View;

/**
 * BodyTransformer to find and change IntConstant(0) to NullConstant where locals are used as
 * objects.
 *
 * @author Palaniappan Muthuraman
 */
public class DexNullTransformer extends AbstractNullTransformer {
  boolean usedAsObject;

  boolean doBreak = false;

  private Local l = null;

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    final DexDefUseAnalysis localDefs = new DexDefUseAnalysis(builder);

    AbstractStmtVisitor checkDef =
        new AbstractStmtVisitor() {
          @Override
          public void caseAssignStmt(@NonNull JAssignStmt stmt) {
            Value r = stmt.getRightOp();
            if (r instanceof JFieldRef) {
              usedAsObject = isObject(r.getType());
              doBreak = true;
            } else if (r instanceof JArrayRef) {
              JArrayRef ar = (JArrayRef) r;
              if (ar.getType() instanceof UnknownType) {
              } else {
                usedAsObject = isObject(ar.getType());
              }
              doBreak = true;
            } else if (r instanceof StringConstant
                || r instanceof JNewExpr
                || r instanceof JNewArrayExpr
                || r instanceof ClassConstant) {
              usedAsObject = true;
              doBreak = true;
            } else if (r instanceof JCastExpr) {
              usedAsObject = isObject(r.getType());
              doBreak = true;
            } else if (r instanceof AbstractInvokeExpr) {
              usedAsObject = isObject(r.getType());
              doBreak = true;
            } else if (r instanceof JLengthExpr) {
              usedAsObject = false;
              doBreak = true;
              // introduces alias
            }
          }

          @Override
          public void caseIdentityStmt(@NonNull JIdentityStmt stmt) {
            if (stmt.getLeftOp() == l) {
              usedAsObject = isObject(stmt.getRightOp().getType());
              doBreak = true;
            }
          }
        };

    AbstractStmtVisitor checkUse =
        new AbstractStmtVisitor() {

          private boolean examineInvokeExpr(AbstractInvokeExpr e) {
            List<Immediate> args = e.getArgs();
            List<Type> argTypes = e.getMethodSignature().getParameterTypes();
            assert args.size() == argTypes.size();
            for (int i = 0; i < args.size(); i++) {
              if (args.get(i) == l && isObject(argTypes.get(i))) {
                return true;
              }
            }
            // check for base

            if (!MethodModifier.isStatic(builder.getModifiers())) {
              AbstractInstanceInvokeExpr aiiexpr = (AbstractInstanceInvokeExpr) e;
              Value b = aiiexpr.getBase();
              return b == l;
            }
            return false;
          }

          @Override
          public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
            AbstractInvokeExpr e = stmt.getInvokeExpr().get();
            usedAsObject = examineInvokeExpr(e);
            doBreak = true;
          }

          @Override
          public void caseAssignStmt(@NonNull JAssignStmt stmt) {
            Value left = stmt.getLeftOp();
            Value r = stmt.getRightOp();

            if (left instanceof JArrayRef) {
              JArrayRef ar = (JArrayRef) left;
              if (ar.getIndex() == l) {
                doBreak = true;
                return;
              } else if (ar.getBase() == l) {
                usedAsObject = true;
                doBreak = true;
                return;
              }
            }

            if (left instanceof JInstanceFieldRef) {
              JInstanceFieldRef ifr = (JInstanceFieldRef) left;
              if (ifr.getBase() == l) {
                usedAsObject = true;
                doBreak = true;
                return;
              }
            }

            // used to assign
            if (stmt.getRightOp() == l) {
              Value l = stmt.getLeftOp();
              if (l instanceof JStaticFieldRef && isObject(l.getType())) {
                usedAsObject = true;
                doBreak = true;
                return;
              } else if (l instanceof JInstanceFieldRef && isObject(l.getType())) {
                usedAsObject = true;
                doBreak = true;
                return;
              } else if (l instanceof JArrayRef) {
                Type aType = l.getType();
                if (aType instanceof UnknownType) {
                } else {
                  usedAsObject = isObject(aType);
                }
                doBreak = true;
                return;
              }
            }

            // is used as value (does not exclude assignment)
            if (r instanceof JFieldRef) {
              usedAsObject = true; // isObject(((FieldRef)
              // r).getFieldRef().type());
              doBreak = true;
            } else if (r instanceof JArrayRef) {
              JArrayRef ar = (JArrayRef) r;
              // used as index
              usedAsObject = ar.getBase() == l;
              doBreak = true;
            } else if (r instanceof StringConstant || r instanceof JNewExpr) {
              throw new RuntimeException("NOT POSSIBLE StringConstant or NewExpr at " + stmt);
            } else if (r instanceof JNewArrayExpr) {
              usedAsObject = false;
              doBreak = true;
            } else if (r instanceof JCastExpr) {
              usedAsObject = isObject(r.getType());
              doBreak = true;
            } else if (r instanceof AbstractInvokeExpr) {
              usedAsObject = examineInvokeExpr((AbstractInvokeExpr) stmt.getRightOp());
              doBreak = true;
            } else if (r instanceof JLengthExpr) {
              usedAsObject = true;
              doBreak = true;
            } else if (r instanceof AbstractBinopExpr) {
              usedAsObject = false;
              doBreak = true;
            }
          }

          @Override
          public void caseIdentityStmt(@NonNull JIdentityStmt stmt) {
            if (stmt.getLeftOp() == l) {
              throw new RuntimeException("IMPOSSIBLE 0");
            }
          }

          @Override
          public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
            usedAsObject = stmt.getOp() == l;
            doBreak = true;
          }

          @Override
          public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
            usedAsObject = stmt.getOp() == l;
            doBreak = true;
          }

          @Override
          public void caseReturnStmt(@NonNull JReturnStmt stmt) {
            usedAsObject =
                stmt.getOp() == l
                    && isObject(Objects.requireNonNull(builder.getMethodSignature()).getType());
            doBreak = true;
          }

          @Override
          public void caseThrowStmt(@NonNull JThrowStmt stmt) {
            usedAsObject = stmt.getOp() == l;
            doBreak = true;
          }
        };

    for (Local loc : getNullCandidates(builder)) {
      usedAsObject = false;
      Set<Stmt> defs = localDefs.collectDefinitionsWithAliases(loc);
      // process normally
      doBreak = false;
      for (Stmt stmt : defs) {
        // put correct local in l
        if (stmt instanceof AbstractDefinitionStmt) {
          l = (Local) ((AbstractDefinitionStmt) stmt).getLeftOp();
        } else if (stmt instanceof JIfStmt) {
          throw new RuntimeException(
              "ERROR: def can not be something else than Assign or Identity statement! (def: "
                  + stmt
                  + " class: "
                  + stmt.getClass());
        }

        // check defs
        stmt.accept(checkDef);
        if (doBreak) {
          break;
        }

        // check uses
        for (Stmt use : localDefs.getUsesOf(l)) {
          use.accept(checkUse);

          if (doBreak) {
            break;
          }
        } // for uses
        if (doBreak) {
          break;
        }
      } // for defs

      // change values
      if (usedAsObject) {
        for (Stmt u : defs) {
          replaceWithNull(u);
          Set<Value> defLocals = u.getUsesAndDefs().collect(Collectors.toSet());

          Local l = (Local) ((AbstractDefinitionStmt) u).getLeftOp();
          for (Stmt uuse : localDefs.getUsesOf(l)) {
            // If we have a[x] = 0 and a is an object, we may not conclude 0 -> null
            if (!((Stmt) uuse).containsArrayRef()
                || !defLocals.contains(((Stmt) uuse).getArrayRef().getBase())) {
              replaceWithNull((Stmt) uuse);
            }
          }
        }
      } // end if
    }

    // Check for inlined zero values

    AbstractStmtVisitor inlinedZeroValues =
        new AbstractStmtVisitor() {

          final NullConstant nullConstant = NullConstant.getInstance();
          Set<Value> objects = null;

          @Override
          public void caseAssignStmt(@NonNull JAssignStmt stmt) {
            if (isObject(stmt.getLeftOp().getType()) && isConstZero(stmt.getRightOp())) {
              stmt.withRValue(nullConstant);
              return;
            }

            // Case a = (Object) 0
            if (stmt.getRightOp() instanceof JCastExpr) {
              JCastExpr ce = (JCastExpr) stmt.getRightOp();
              if (isObject(ce.getType()) && isConstZero(ce.getOp())) {
                stmt.withRValue(nullConstant);
              }
            }

            // Case a[0] = 0
            if (stmt.getLeftOp() instanceof JArrayRef && isConstZero(stmt.getRightOp())) {
              JArrayRef ar = (JArrayRef) stmt.getLeftOp();
              if (objects == null) {
                objects = getObjectArray(builder);
              }
              if (objects.contains(ar.getBase())) {
                stmt.withRValue(nullConstant);
              }
            }
          }

          private boolean isConstZero(Value rightOp) {
            return (rightOp instanceof IntConstant && ((IntConstant) rightOp).getValue() == 0)
                || (rightOp instanceof LongConstant && ((LongConstant) rightOp).getValue() == 0);
          }

          @Override
          public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
            if (stmt.getOp() instanceof IntConstant
                && ((IntConstant) stmt.getOp()).getValue() == 0) {
              stmt.withOp(nullConstant);
            }
          }

          @Override
          public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
            if (stmt.getOp() instanceof IntConstant
                && ((IntConstant) stmt.getOp()).getValue() == 0) {
              stmt.withOp(nullConstant);
            }
          }

          @Override
          public void caseReturnStmt(@NonNull JReturnStmt stmt) {
            if (stmt.getOp() instanceof IntConstant) {
              assert builder.getMethodSignature() != null;
              if (isObject(builder.getMethodSignature().getType())) {
                IntConstant iconst = (IntConstant) stmt.getOp();
                assert iconst.getValue() == 0;
                stmt.withReturnValue(nullConstant);
              }
            }
          }
        };

    final NullConstant nullConstant = NullConstant.getInstance();
    for (Stmt stmt : builder.getStmts()) {
      stmt.accept(inlinedZeroValues);
      if (stmt.isInvokableStmt()) {
        InvokableStmt invokableStmt = stmt.asInvokableStmt();
        if (invokableStmt.containsInvokeExpr()) {
          AbstractInvokeExpr invExpr = invokableStmt.getInvokeExpr().get();
          for (int i = 0; i < invExpr.getArgCount(); i++) {
            if (isObject(invExpr.getMethodSignature().getParameterTypes().get(i))) {
              if (invExpr.getArg(i) instanceof IntConstant) {
                IntConstant iconst = (IntConstant) invExpr.getArg(i);
                assert iconst.getValue() == 0;
                if (invExpr instanceof AbstractInstanceInvokeExpr) {
                  invExpr =
                      ((AbstractInstanceInvokeExpr) invExpr)
                          .withArgs(Collections.singletonList(nullConstant));
                }
              }
            }
          }
        }
      }
    }
  }

  private static Set<Value> getObjectArray(Body.BodyBuilder bodyBuilder) {
    Set<Value> objArrays = new HashSet<Value>();
    for (Stmt u : bodyBuilder.getStmts()) {
      if (u instanceof JAssignStmt) {
        JAssignStmt assign = (JAssignStmt) u;
        if (assign.getRightOp() instanceof JNewArrayExpr) {
          JNewArrayExpr nea = (JNewArrayExpr) assign.getRightOp();
          if (isObject(nea.getBaseType())) {
            objArrays.add(assign.getLeftOp());
          }
        } else if (assign.getRightOp() instanceof JFieldRef) {
          JFieldRef fr = (JFieldRef) assign.getRightOp();
          if (fr.getType() instanceof ArrayType) {
            if (isObject(((ArrayType) fr.getType()).getElementType())) {
              objArrays.add(assign.getLeftOp());
            }
          }
        }
      }
    }
    return objArrays;
  }

  /**
   * Collect all the locals which are assigned a IntConstant(0) or are used within a zero
   * comparison.
   *
   * @param bodyBuilder the body to analyze
   */
  private Set<Local> getNullCandidates(Body.BodyBuilder bodyBuilder) {
    Set<Local> candidates = null;
    for (Stmt stmt : bodyBuilder.getStmts()) {
      if (stmt instanceof JAssignStmt) {
        JAssignStmt a = (JAssignStmt) stmt;
        if (!(a.getLeftOp() instanceof Local)) {
          continue;
        }
        Local l = (Local) a.getLeftOp();
        Value r = a.getRightOp();
        if ((r instanceof IntConstant && ((IntConstant) r).getValue() == 0)
            || (r instanceof LongConstant && ((LongConstant) r).getValue() == 0)) {
          if (candidates == null) {
            candidates = new HashSet<Local>();
          }
          candidates.add(l);
        }
      } else if (stmt instanceof JIfStmt) {
        AbstractConditionExpr expr = ((JIfStmt) stmt).getCondition();
        if (isZeroComparison(expr) && expr.getOp1() instanceof Local) {
          if (candidates == null) {
            candidates = new HashSet<Local>();
          }
          candidates.add((Local) expr.getOp1());
        }
      }
    }

    return candidates == null ? Collections.<Local>emptySet() : candidates;
  }
}
