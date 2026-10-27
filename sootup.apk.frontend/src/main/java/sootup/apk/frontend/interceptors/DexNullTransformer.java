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
import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
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
    // Statements are immutable: collect the changed statements of the body and replace them at
    // the end, after all analyses of the original statements are done
    final Map<Stmt, Stmt> replacements = new LinkedHashMap<>();

    AbstractStmtVisitor checkDef =
        new AbstractStmtVisitor() {
          @Override
          public void caseAssignStmt(@NonNull JAssignStmt stmt) {
            Value r = stmt.getRightOp();
            if (r instanceof JFieldRef) {
              usedAsObject = isObject(r.getType());
              doBreak = true;
            } else if (r instanceof JArrayRef ar) {
              if (!(ar.getType() instanceof UnknownType)) {
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

            if (!MethodModifier.isStatic(builder.getModifiers())
                && e instanceof AbstractInstanceInvokeExpr) {
              AbstractInstanceInvokeExpr aiiexpr = (AbstractInstanceInvokeExpr) e;
              Value b = aiiexpr.getBase();
              return b == l;
            }
            return false;
          }

          @Override
          public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
            if (stmt.getInvokeExpr().isEmpty()) return;
            AbstractInvokeExpr e = stmt.getInvokeExpr().get();
            usedAsObject = examineInvokeExpr(e);
            doBreak = true;
          }

          @Override
          public void caseAssignStmt(@NonNull JAssignStmt stmt) {
            Value left = stmt.getLeftOp();
            Value r = stmt.getRightOp();

            if (left instanceof JArrayRef ar) {
              if (ar.getIndex() == l) {
                doBreak = true;
                return;
              } else if (ar.getBase() == l) {
                usedAsObject = true;
                doBreak = true;
                return;
              }
            }

            if (left instanceof JInstanceFieldRef ifr) {
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
                if (!(aType instanceof UnknownType)) {
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
            } else if (r instanceof JArrayRef ar) {
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
          replace(replacements, u, this::replaceWithNull);
          Set<Value> defLocals = new HashSet<>(u.getUsesAndDefs());

          Local l = (Local) ((AbstractDefinitionStmt) u).getLeftOp();
          for (Stmt uuse : localDefs.getUsesOf(l)) {
            // If we have a[x] = 0 and a is an object, we may not conclude 0 -> null
            if (!uuse.containsArrayRef() || !defLocals.contains(uuse.getArrayRef().getBase())) {
              replace(replacements, uuse, this::replaceWithNull);
            }
          }
        }
      } // end if
    }

    // Check for inlined zero values
    Set<Value> objects = getObjectArray(builder);
    for (Stmt stmt : builder.getStmts()) {
      replace(replacements, stmt, s -> replaceInlinedZeros(s, builder, objects));
    }

    for (Map.Entry<Stmt, Stmt> replacement : replacements.entrySet()) {
      if (replacement.getKey() != replacement.getValue()) {
        builder.getControlFlowGraph().replaceNode(replacement.getKey(), replacement.getValue());
      }
    }
  }

  /**
   * Applies the given change to the current version of the given statement of the body and records
   * the result
   */
  private static void replace(
      Map<Stmt, Stmt> replacements, Stmt original, UnaryOperator<Stmt> change) {
    Stmt current = replacements.getOrDefault(original, original);
    Stmt changed = change.apply(current);
    if (changed != current) {
      replacements.put(original, changed);
    }
  }

  private static boolean isConstZero(Value value) {
    return (value instanceof IntConstant && ((IntConstant) value).getValue() == 0)
        || (value instanceof LongConstant && ((LongConstant) value).getValue() == 0);
  }

  /**
   * Replaces the zeros that are used as objects without being stored in a local: assigned to an
   * object, cast to an object, stored in an object array, used as monitor, returned from a method
   * that returns an object, or passed for an object parameter
   *
   * @return the statement with null instead of these zeros, or the given statement
   */
  private static Stmt replaceInlinedZeros(
      Stmt stmt, Body.BodyBuilder builder, Set<Value> objectArrays) {
    final NullConstant nullConstant = NullConstant.getInstance();
    Stmt result = stmt;
    if (stmt instanceof JAssignStmt assign) {
      Value right = assign.getRightOp();
      if ((isObject(assign.getLeftOp().getType()) && isConstZero(right))
          // Case a = (Object) 0
          || (right instanceof JCastExpr ce && isObject(ce.getType()) && isConstZero(ce.getOp()))
          // Case a[0] = 0
          || (assign.getLeftOp() instanceof JArrayRef ar
              && isConstZero(right)
              && objectArrays.contains(ar.getBase()))) {
        result = assign.withRValue(nullConstant);
      }
    } else if (stmt instanceof JEnterMonitorStmt monitor && isConstZero(monitor.getOp())) {
      result = monitor.withOp(nullConstant);
    } else if (stmt instanceof JExitMonitorStmt monitor && isConstZero(monitor.getOp())) {
      result = monitor.withOp(nullConstant);
    } else if (stmt instanceof JReturnStmt ret
        && isConstZero(ret.getOp())
        && builder.getMethodSignature() != null
        && isObject(builder.getMethodSignature().getType())) {
      result = ret.withReturnValue(nullConstant);
    }

    // Zeros that are passed for object parameters
    if (result.isInvokableStmt() && result.asInvokableStmt().getInvokeExpr().isPresent()) {
      AbstractInvokeExpr invExpr = result.asInvokableStmt().getInvokeExpr().get();
      List<Type> parameterTypes = invExpr.getMethodSignature().getParameterTypes();
      List<Immediate> args = new ArrayList<>(invExpr.getArgs());
      boolean changed = false;
      for (int i = 0; i < args.size(); i++) {
        if (isObject(parameterTypes.get(i)) && isConstZero(args.get(i))) {
          args.set(i, nullConstant);
          changed = true;
        }
      }
      if (changed) {
        AbstractInvokeExpr newExpr;
        if (invExpr instanceof AbstractInstanceInvokeExpr) {
          newExpr = ((AbstractInstanceInvokeExpr) invExpr).withArgs(args);
        } else if (invExpr instanceof JStaticInvokeExpr) {
          newExpr = ((JStaticInvokeExpr) invExpr).withArgs(args);
        } else if (invExpr instanceof JDynamicInvokeExpr) {
          newExpr = ((JDynamicInvokeExpr) invExpr).withMethodArgs(args);
        } else {
          return result;
        }
        if (result instanceof JInvokeStmt) {
          result = ((JInvokeStmt) result).withInvokeExpr(newExpr);
        } else if (result instanceof JAssignStmt) {
          result = ((JAssignStmt) result).withRValue(newExpr);
        }
      }
    }
    return result;
  }

  private static Set<Value> getObjectArray(Body.BodyBuilder bodyBuilder) {
    Set<Value> objArrays = new HashSet<>();
    for (Stmt u : bodyBuilder.getStmts()) {
      if (u instanceof JAssignStmt assign) {
        if (assign.getRightOp() instanceof JNewArrayExpr nea) {
          if (isObject(nea.getBaseType())) {
            objArrays.add(assign.getLeftOp());
          }
        } else if (assign.getRightOp() instanceof JFieldRef fr) {
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
      if (stmt instanceof JAssignStmt a) {
        if (!(a.getLeftOp() instanceof Local l)) {
          continue;
        }
        Value r = a.getRightOp();
        if ((r instanceof IntConstant && ((IntConstant) r).getValue() == 0)
            || (r instanceof LongConstant && ((LongConstant) r).getValue() == 0)) {
          if (candidates == null) {
            candidates = new HashSet<>();
          }
          candidates.add(l);
        }
      } else if (stmt instanceof JIfStmt) {
        AbstractConditionExpr expr = ((JIfStmt) stmt).getCondition();
        if (isZeroComparison(expr) && expr.getOp1() instanceof Local) {
          if (candidates == null) {
            candidates = new HashSet<>();
          }
          candidates.add((Local) expr.getOp1());
        }
      }
    }

    return candidates == null ? Collections.emptySet() : candidates;
  }
}
