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
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
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

    final DexDefUseAnalysis localDefs = new DexDefUseAnalysis(builder);

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
                      //                                usedAsFloatingPoint =
                      // examineBinopExpr(stmt);
                      doBreak = true;
                    } else if (r instanceof JCastExpr) {
                      //                                usedAsFloatingPoint =
                      // stmt.hasTag(FloatOpTag.NAME) || stmt.hasTag(DoubleOpTag.NAME);
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

      if (usedAsFloatingPoint) {
        for (Stmt defStmt : defs) {
          replaceWithFloatingPoint(defStmt);
        }
      }
    }
  }

  /**
   * Replace 0 with null in the given unit.
   *
   * @param stmt the stmt where 0 will be replaced with null.
   */
  private void replaceWithFloatingPoint(Stmt stmt) {
    if (stmt instanceof JAssignStmt) {
      JAssignStmt s = (JAssignStmt) stmt;
      Value v = s.getRightOp();
      if ((v instanceof IntConstant)) {
        int vVal = ((IntConstant) v).getValue();
        s.withRValue(FloatConstant.getInstance(Float.intBitsToFloat(vVal)));
      } else if (v instanceof LongConstant) {
        long vVal = ((LongConstant) v).getValue();
        s.withRValue(DoubleConstant.getInstance(Double.longBitsToDouble(vVal)));
      }
    }
  }

  /**
   * Collect all the locals which are assigned a IntConstant(0) or are used within a zero
   * comparison.
   *
   * @param bodyBuilder the bodyBuilder to analyze
   */
  private Set<Local> getNumCandidates(Body.BodyBuilder bodyBuilder) {
    Set<Local> candidates = new HashSet<>();
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
