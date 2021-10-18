package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
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

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JStaticFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import de.upb.swt.soot.core.jimple.visitor.AbstractStmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.ObjectOpTag;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BodyTransformer to find and change IntConstant(0) to NullConstant where locals are used as objects.
 *
 * @author Michael Markert
 */
public class DexNullTransformer extends AbstractNullTransformer {
  // Note: we need an instance variable for inner class access, treat this as
  // a local variable (including initialization before use)

  private boolean usedAsObject;
  private boolean doBreak = false;

  public static DexNullTransformer v() {
    return new DexNullTransformer();
  }

  private Local l = null;

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder bodyBuilder) {
    final DexDefUseAnalysis localDefs = new DexDefUseAnalysis(bodyBuilder);
    AbstractStmtVisitor checkDef = new AbstractStmtVisitor() { // Alex: should also end as
      // soon as detected as not
      // used as an object
      @Override
      public void caseAssignStmt(JAssignStmt stmt) {
        Value r = stmt.getRightOp();
        if (r instanceof JFieldRef) {
          usedAsObject = isObject(((JFieldRef) r).getFieldSignature().getType());
          doBreak = true;
          return;
        } else if (r instanceof JArrayRef) {
          JArrayRef ar = (JArrayRef) r;
          if (ar.getType() instanceof UnknownType) {
            usedAsObject = stmt.hasTag(new ObjectOpTag().getName()); // isObject
            // (findArrayType
            // (g,
            // localDefs,
            // localUses,
            // stmt));
          } else {
            usedAsObject = isObject(ar.getType());
          }
          doBreak = true;
          return;
        } else if (r instanceof StringConstant || r instanceof JNewExpr || r instanceof JNewArrayExpr
            || r instanceof ClassConstant) {
          usedAsObject = true;
          doBreak = true;
          return;
        } else if (r instanceof JCastExpr) {
          usedAsObject = isObject(r.getType());
          doBreak = true;
          return;
        } else if (r instanceof AbstractInvokeExpr) {
          usedAsObject = isObject(r.getType());
          doBreak = true;
          return;
        } else if (r instanceof JLengthExpr) {
          usedAsObject = false;
          doBreak = true;
          return;
          // introduces alias
        }

      }

      @Override
      public void caseIdentityStmt(JIdentityStmt stmt) {
        if (stmt.getLeftOp() == l) {
          usedAsObject = isObject(stmt.getRightOp().getType());
          doBreak = true;
          return;
        }
      }
    };
    AbstractStmtVisitor checkUse = new AbstractStmtVisitor() {
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
        // Type sm = e.getMethodSignature().getType();
          if (e instanceof AbstractInstanceInvokeExpr) {
            AbstractInstanceInvokeExpr aiiexpr = (AbstractInstanceInvokeExpr) e;
            Value b = aiiexpr.getBase();
            if (b == l) {
              return true;
            }
          }

        return false;
      }

      @Override
      public void caseInvokeStmt(JInvokeStmt stmt) {
        AbstractInvokeExpr e = stmt.getInvokeExpr();
        usedAsObject = examineInvokeExpr(e);
        doBreak = true;
        return;
      }

      @Override
      public void caseAssignStmt(JAssignStmt stmt) {
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
          if (l instanceof JStaticFieldRef && isObject(((JStaticFieldRef) l).getFieldSignature().getType())) {
            usedAsObject = true;
            doBreak = true;
            return;
          } else if (l instanceof JInstanceFieldRef && isObject(((JInstanceFieldRef) l).getFieldSignature().getType())) {
            usedAsObject = true;
            doBreak = true;
            return;
          } else if (l instanceof JArrayRef) {
            Type aType = ((JArrayRef) l).getType();
            if (aType instanceof UnknownType) {
              usedAsObject = stmt.hasTag(new ObjectOpTag().getName()); // isObject(
              // findArrayType(g,
              // localDefs,
              // localUses,
              // stmt));
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
          return;
        } else if (r instanceof JArrayRef) {
          JArrayRef ar = (JArrayRef) r;
          if (ar.getBase() == l) {
            usedAsObject = true;
          } else { // used as index
            usedAsObject = false;
          }
          doBreak = true;
          return;
        } else if (r instanceof StringConstant || r instanceof JNewExpr) {
          throw new RuntimeException("NOT POSSIBLE StringConstant or NewExpr at " + stmt);
        } else if (r instanceof JNewArrayExpr) {
          usedAsObject = false;
          doBreak = true;
          return;
        } else if (r instanceof JCastExpr) {
          usedAsObject = isObject(r.getType());
          doBreak = true;
          return;
        } else if (r instanceof AbstractInvokeExpr) {
          usedAsObject = examineInvokeExpr((AbstractInvokeExpr) stmt.getRightOp());
          doBreak = true;
          return;
        } else if (r instanceof JLengthExpr) {
          usedAsObject = true;
          doBreak = true;
          return;
        } else if (r instanceof AbstractBinopExpr) {
          usedAsObject = false;
          doBreak = true;
          return;
        }
      }

      @Override
      public void caseIdentityStmt(JIdentityStmt stmt) {
        if (stmt.getLeftOp() == l) {
          throw new RuntimeException("IMPOSSIBLE 0");
        }
      }

      @Override
      public void caseEnterMonitorStmt(JEnterMonitorStmt stmt) {
        usedAsObject = stmt.getOp() == l;
        doBreak = true;
        return;
      }

      @Override
      public void caseExitMonitorStmt(JExitMonitorStmt stmt) {
        usedAsObject = stmt.getOp() == l;
        doBreak = true;
        return;
      }

      @Override
      public void caseReturnStmt(JReturnStmt stmt) {
        usedAsObject = stmt.getOp() == l && isObject(bodyBuilder.getMethodSignature().getType());
        doBreak = true;
        return;
      }

      @Override
      public void caseThrowStmt(JThrowStmt stmt) {
        usedAsObject = stmt.getOp() == l;
        doBreak = true;
        return;
      }
    };

    for (Local loc : getNullCandidates(bodyBuilder)) {
      usedAsObject = false;
      Set<Stmt> defs = localDefs.collectDefinitionsWithAliases(loc);
      // process normally
      doBreak = false;
      for (Stmt stmt : defs) {
        // put correct local in l
        if (stmt instanceof AbstractDefinitionStmt) {
          l = (Local) ((AbstractDefinitionStmt) stmt).getLeftOp();
        } else if (stmt instanceof JIfStmt) {
          throw new RuntimeException("ERROR: def can not be something else than Assign or Identity statement! (def: " + stmt
              + " class: " + stmt.getClass() + "");
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
          Set<Value> defLocals = new HashSet<Value>();
          for (Value vb : u.getDefs()) {
            defLocals.add(vb);
          }

          Local l = (Local) ((AbstractDefinitionStmt) u).getLeftOp();
          for (Stmt uuse : localDefs.getUsesOf(l)) {
            Stmt use = (Stmt) uuse;
            // If we have a[x] = 0 and a is an object, we may not conclude 0 -> null
            if (!use.containsArrayRef() || !defLocals.contains(use.getArrayRef().getBase())) {
              replaceWithNull(use);
            }
          }
        }
      } // end if
    }

    // Check for inlined zero values
    AbstractStmtVisitor inlinedZeroValues = new AbstractStmtVisitor() {
      final NullConstant nullConstant = NullConstant.getInstance();
      Set<Value> objects = null;

      @Override
      public void caseAssignStmt(JAssignStmt stmt) {
        // Case a = 0 with a being an object
        if (isObject(stmt.getLeftOp().getType()) && isConstZero(stmt.getRightOp())) {
          stmt.withRightOp(nullConstant);
          return;
        }

        // Case a = (Object) 0
        if (stmt.getRightOp() instanceof JCastExpr) {
          JCastExpr ce = (JCastExpr) stmt.getRightOp();
          if (isObject(ce.getType()) && isConstZero(ce.getOp())) {
            stmt.withRightOp(nullConstant);
          }
        }

        // Case a[0] = 0
        if (stmt.getLeftOp() instanceof JArrayRef && isConstZero(stmt.getRightOp())) {
          JArrayRef ar = (JArrayRef) stmt.getLeftOp();
          if (objects == null) {
            objects = getObjectArray(bodyBuilder);
          }
          if (objects.contains(ar.getBase()) || stmt.hasTag(new ObjectOpTag().getName())) {
            stmt.withRightOp(nullConstant);
          }
        }
      }

      private boolean isConstZero(Value rightOp) {
        if (rightOp instanceof IntConstant && ((IntConstant) rightOp).getValue() == 0) {
          return true;
        }
        if (rightOp instanceof LongConstant && ((LongConstant) rightOp).getValue() == 0) {
          return true;
        }
        return false;
      }

      @Override
      public void caseReturnStmt(JReturnStmt stmt) {
        if (stmt.getOp() instanceof IntConstant && isObject(bodyBuilder.getMethodSignature().getType())) {
          IntConstant iconst = (IntConstant) stmt.getOp();
          assert iconst.getValue() == 0;
          stmt.withReturnValue(nullConstant);
        }
      }

      @Override
      public void caseEnterMonitorStmt(JEnterMonitorStmt stmt) {
        if (stmt.getOp() instanceof IntConstant && ((IntConstant) stmt.getOp()).getValue() == 0) {
          stmt.withOp(nullConstant);
        }
      }

      @Override
      public void caseExitMonitorStmt(JExitMonitorStmt stmt) {
        if (stmt.getOp() instanceof IntConstant && ((IntConstant) stmt.getOp()).getValue() == 0) {
          stmt.withOp(nullConstant);
        }
      }

    };

    final NullConstant nullConstant = NullConstant.getInstance();
    for (Stmt u : bodyBuilder.getStmts()) {
      u.accept(inlinedZeroValues);
      if (u instanceof Stmt) {
        Stmt stmt = (Stmt) u;
        if (stmt.containsInvokeExpr()) {
          AbstractInvokeExpr invExpr = stmt.getInvokeExpr();
          for (int i = 0; i < invExpr.getArgCount(); i++) {
            if (isObject(invExpr.getMethodSignature().getParameterTypes().get(i))) {
              if (invExpr.getArg(i) instanceof IntConstant) {
                IntConstant iconst = (IntConstant) invExpr.getArg(i);
                assert iconst.getValue() == 0;
                // FIXME - find a way to update unmodifiableList
                invExpr.setArg(i, nullConstant);// invExpr = invExpr.getArgs().set(i, nullConstant);
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
            if (isObject(((ArrayType) fr.getType()).getBaseType())) {
              objArrays.add(assign.getLeftOp());
            }
          }
        }

      }
    }
    return objArrays;
  }

  /**
   * Collect all the locals which are assigned a IntConstant(0) or are used within a zero comparison.
   *
   * @param bodyBuilder
   *          the bodyBuilder to analyze
   */
  private Set<Local> getNullCandidates(Body.BodyBuilder bodyBuilder) {
    Set<Local> candidates = null;
    for (Stmt u : bodyBuilder.getStmts()) {
      if (u instanceof JAssignStmt) {
        JAssignStmt a = (JAssignStmt) u;
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
      } else if (u instanceof JIfStmt) {
        AbstractConditionExpr expr = (AbstractConditionExpr) ((JIfStmt) u).getCondition();
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
