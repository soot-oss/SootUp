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
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.main.DexCastExpr;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.model.Body;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.views.View;

public class DexNumberTranformer extends DexTransformer {

  /* This default type is "int" for registers whose size is  less or equal to 32bits and "long" to registers whose size is 64bits.
   *  The problem is that 32bits registers could be either
   * "int" or "float" and 64bits registers "long" or "double". If the analysis concludes that an "int" has to be changed to a
   * "float", rightValue has to change from IntConstant.v(literal) to Float.intBitsToFloat((int) literal). If the analysis
   * concludes that a "long" has to be changed to a "double, rightValue has to change from LongConstant.v(literal) to
   * DoubleConstant.v(Double.longBitsToDouble(literal)).*/

  private boolean usedAsFloatingPoint;

  private boolean doBreak = false;

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    // The fields hold the state of one body. The interceptors of an input location are shared by
    // its bodies, which may be resolved concurrently, so each body gets its own instance.
    new DexNumberTranformer().transform(builder, view);
  }

  private void transform(Body.@NonNull BodyBuilder builder, @NonNull View view) {

    final DexDefUseAnalysis localDefs = new DexDefUseAnalysis(builder);
    // original statement -> its current version
    final Map<Stmt, Stmt> replacements = new LinkedHashMap<>();
    final Set<Local> floatingPointLocals = floatingPointLocals(builder);

    for (Local local : getNumCandidates(builder)) {
      usedAsFloatingPoint = false;
      Set<Stmt> defs = localDefs.collectDefinitionsWithAliases(local);

      doBreak = false;
      for (Stmt stmt : defs) {
        // put correct local in l
        final Local l =
            stmt instanceof AbstractDefinitionStmt
                ? (Local) ((AbstractDefinitionStmt) stmt).getLeftOp()
                : null;
        stmt.accept(
            new AbstractStmtVisitor() {
              @Override
              public void caseAssignStmt(@NonNull JAssignStmt stmt) {
                {
                  Value rightOp = stmt.getRightOp();
                  if (rightOp instanceof JFieldRef) {
                    usedAsFloatingPoint = isFloatingPointLike(rightOp.getType());
                    doBreak = true;
                  } else if (rightOp instanceof JNewArrayExpr) {
                    JNewArrayExpr nae = (JNewArrayExpr) rightOp;
                    Type t = nae.getType();
                    usedAsFloatingPoint = isFloatingPointLike(t);
                    doBreak = true;
                  } else if (rightOp instanceof JArrayRef) {
                    JArrayRef ar = (JArrayRef) rightOp;
                    Type arType = ar.getType();
                    if (arType instanceof UnknownType) {
                      Type t = findArrayType(localDefs, stmt, 0, Collections.emptySet());
                      usedAsFloatingPoint = isFloatingPointLike(t);
                    } else {
                      usedAsFloatingPoint = isFloatingPointLike(ar.getType());
                    }
                    doBreak = true;
                  } else if (rightOp instanceof JCastExpr) {
                    usedAsFloatingPoint = isFloatingPointLike(rightOp.getType());
                    doBreak = true;
                  } else if (rightOp instanceof AbstractInvokeExpr) {
                    usedAsFloatingPoint = isFloatingPointLike(rightOp.getType());
                    doBreak = true;
                  } else if (rightOp instanceof JLengthExpr) {
                    usedAsFloatingPoint = false;
                    doBreak = true;
                  }
                }
              }

              @Override
              public void caseIdentityStmt(@NonNull JIdentityStmt stmt) {
                if (stmt.getLeftOp() == l) {
                  usedAsFloatingPoint = isFloatingPointLike(stmt.getRightOp().getType());
                  doBreak = true;
                }
              }
            });

        if (doBreak) {
          break;
        }

        // check uses
        for (Stmt use : localDefs.getUsesOf(l)) {
          use.accept(
              new AbstractStmtVisitor() {
                @Override
                public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
                  AbstractInvokeExpr e = stmt.getInvokeExpr().get();
                  usedAsFloatingPoint = examineInvokeExpr(e, l);
                }

                @Override
                public void caseReturnStmt(@NonNull JReturnStmt stmt) {
                  usedAsFloatingPoint =
                      stmt.getOp() == l
                          && isFloatingPointLike(
                              Objects.requireNonNull(builder.getMethodSignature()).getType());
                  doBreak = true;
                }

                @Override
                public void caseAssignStmt(@NonNull JAssignStmt stmt) {
                  {
                    // only case where 'l' could be on the left side is
                    // arrayRef with 'l' as the index
                    JAssignStmt jAssignStmt = (JAssignStmt) use;
                    Value left = jAssignStmt.getLeftOp();
                    if (left instanceof JArrayRef) {
                      JArrayRef ar = (JArrayRef) left;
                      if (ar.getIndex() == l) {
                        doBreak = true;
                        return;
                      }
                    }

                    // from this point, we only check the right hand
                    // side of the assignment
                    Value r = jAssignStmt.getRightOp();
                    if (r instanceof JArrayRef) {
                      if (((JArrayRef) r).getIndex() == l) {
                        doBreak = true;
                      }
                    } else if (r instanceof AbstractInvokeExpr) {
                      usedAsFloatingPoint = examineInvokeExpr((AbstractInvokeExpr) r, l);
                      doBreak = true;
                    } else if (r instanceof AbstractBinopExpr) {
                      // Soot's dexpler tags the operations of float and double instructions;
                      // without tags the operand types follow from the arithmetic
                      usedAsFloatingPoint = floatingPointLocals.contains(l);
                      doBreak = true;
                    } else if (r instanceof JCastExpr) {
                      // Soot's dexpler: stmt.hasTag(FloatOpTag.NAME) ||
                      // stmt.hasTag(DoubleOpTag.NAME)
                      usedAsFloatingPoint = floatingPointLocals.contains(l);
                      doBreak = true;
                    } else if (r instanceof Local && r == l) {
                      if (left instanceof JFieldRef) {
                        JFieldRef fr = (JFieldRef) left;
                        if (isFloatingPointLike(fr.getType())) {
                          usedAsFloatingPoint = true;
                        }
                        doBreak = true;
                      } else if (left instanceof JArrayRef) {
                        JArrayRef jArrayRef = (JArrayRef) left;
                        Type arType = jArrayRef.getType();
                        if (arType instanceof UnknownType) {
                          arType = findArrayType(localDefs, stmt, 0, Collections.emptySet());
                        }
                        usedAsFloatingPoint = isFloatingPointLike(arType);
                        doBreak = true;
                      }
                    }
                  }
                }
              });
          if (doBreak) {
            break;
          }
        }
        if (doBreak) {
          break;
        }
      }

      if (usedAsFloatingPoint || floatingPointLocals.contains(local)) {
        for (Stmt defStmt : defs) {
          Stmt current = replacements.getOrDefault(defStmt, defStmt);
          Stmt changed = withFloatingPoint(current);
          if (changed != current) {
            replacements.put(defStmt, changed);
          }
        }
      }
    }

    // Statements are immutable: put the changed ones into the body. The analysis above refers to
    // the original statements, so they are replaced at the end.
    for (Map.Entry<Stmt, Stmt> replacement : replacements.entrySet()) {
      builder.getControlFlowGraph().replaceNode(replacement.getKey(), replacement.getValue());
    }
  }

  /**
   * Returns the statement with its int or long constant replaced by the float or double with the
   * same bits, or the statement itself if it assigns no such constant.
   */
  private static Stmt withFloatingPoint(Stmt stmt) {
    if (stmt instanceof JAssignStmt) {
      JAssignStmt s = (JAssignStmt) stmt;
      Value v = s.getRightOp();
      if ((v instanceof IntConstant)) {
        int vVal = ((IntConstant) v).getValue();
        return s.withRValue(FloatConstant.getInstance(Float.intBitsToFloat(vVal)));
      } else if (v instanceof LongConstant) {
        long vVal = ((LongConstant) v).getValue();
        return s.withRValue(DoubleConstant.getInstance(Double.longBitsToDouble(vVal)));
      }
    }
    return stmt;
  }

  /**
   * The locals that hold floats or doubles as far as arithmetic shows. The operands of {@code cmpl}
   * and {@code cmpg} are floats or doubles, and so are locals that a statement uses or defines as
   * one: a field, a parameter of the method or of a call, a return value or a cast of such a type,
   * and the operand of a dex conversion from float or double ({@link DexCastExpr}). The operands
   * and the result of {@code + - * / %}, negation and copies have one type, so they are floating
   * point together.
   */
  private Set<Local> floatingPointLocals(Body.BodyBuilder bodyBuilder) {
    Map<Local, List<Local>> sameType = new HashMap<>();
    Deque<Local> work = new ArrayDeque<>();
    for (Stmt stmt : bodyBuilder.getStmts()) {
      Optional<AbstractInvokeExpr> invokeExpr =
          stmt instanceof InvokableStmt ? ((InvokableStmt) stmt).getInvokeExpr() : Optional.empty();
      if (invokeExpr.isPresent()) {
        AbstractInvokeExpr invoke = invokeExpr.get();
        List<Immediate> args = invoke.getArgs();
        List<Type> parameterTypes = invoke.getMethodSignature().getParameterTypes();
        for (int i = 0; i < args.size() && i < parameterTypes.size(); i++) {
          if (args.get(i) instanceof Local && isFloatingPointLike(parameterTypes.get(i))) {
            work.add((Local) args.get(i));
          }
        }
      }
      if (stmt instanceof JReturnStmt) {
        Value op = ((JReturnStmt) stmt).getOp();
        if (op instanceof Local
            && isFloatingPointLike(
                Objects.requireNonNull(bodyBuilder.getMethodSignature()).getType())) {
          work.add((Local) op);
        }
      }
      if (stmt instanceof JIdentityStmt) {
        JIdentityStmt identity = (JIdentityStmt) stmt;
        if (identity.getRightOp() instanceof JParameterRef
            && isFloatingPointLike(identity.getRightOp().getType())) {
          work.add(identity.getLeftOp());
        }
        continue;
      }
      if (!(stmt instanceof JAssignStmt)) {
        continue;
      }
      JAssignStmt assign = (JAssignStmt) stmt;
      Value left = assign.getLeftOp();
      Value right = assign.getRightOp();
      if (left instanceof JFieldRef
          && right instanceof Local
          && isFloatingPointLike(left.getType())) {
        work.add((Local) right);
      }
      if (!(left instanceof Local)) {
        continue;
      }
      Local result = (Local) left;
      if (right instanceof DexCastExpr
          && ((DexCastExpr) right).getOp() instanceof Local
          && isFloatingPointLike(((DexCastExpr) right).getSourceType())) {
        // A conversion from float or double, e.g. float-to-int: its operand is floating point
        work.add((Local) ((DexCastExpr) right).getOp());
      }
      if (right instanceof JCmplExpr || right instanceof JCmpgExpr) {
        for (Value op :
            List.of(((AbstractBinopExpr) right).getOp1(), ((AbstractBinopExpr) right).getOp2())) {
          if (op instanceof Local) {
            work.add((Local) op);
          }
        }
      } else if (right instanceof JAddExpr
          || right instanceof JSubExpr
          || right instanceof JMulExpr
          || right instanceof JDivExpr
          || right instanceof JRemExpr) {
        AbstractBinopExpr binop = (AbstractBinopExpr) right;
        for (Value op : List.of(binop.getOp1(), binop.getOp2())) {
          if (op instanceof Local) {
            link(sameType, result, (Local) op);
          }
        }
      } else if (right instanceof JNegExpr) {
        if (((JNegExpr) right).getOp() instanceof Local) {
          link(sameType, result, (Local) ((JNegExpr) right).getOp());
        }
      } else if (right instanceof Local) {
        link(sameType, result, (Local) right);
      } else if ((right instanceof JFieldRef
              || right instanceof JCastExpr
              || right instanceof AbstractInvokeExpr)
          && isFloatingPointLike(right.getType())) {
        work.add(result);
      }
    }

    Set<Local> floatingPoint = new HashSet<>();
    while (!work.isEmpty()) {
      Local local = work.poll();
      if (floatingPoint.add(local)) {
        work.addAll(sameType.getOrDefault(local, Collections.emptyList()));
      }
    }
    return floatingPoint;
  }

  private static void link(Map<Local, List<Local>> sameType, Local a, Local b) {
    sameType.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
    sameType.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
  }

  /**
   * Collect all the locals which are assigned a IntConstant(0) or are used within a zero
   * comparison.
   *
   * @param bodyBuilder the bodyBuilder to analyze
   */
  private Set<Local> getNumCandidates(Body.BodyBuilder bodyBuilder) {
    Set<Local> candidates = new LinkedHashSet<>();
    for (Stmt u : bodyBuilder.getStmts()) {
      if (u instanceof JAssignStmt) {
        JAssignStmt a = (JAssignStmt) u;
        if (!(a.getLeftOp() instanceof Local)) {
          continue;
        }
        Local l = (Local) a.getLeftOp();
        Value rightOp = a.getRightOp();
        if ((rightOp instanceof IntConstant || rightOp instanceof LongConstant)) {
          candidates.add(l);
        }
      }
    }

    return candidates;
  }
}
