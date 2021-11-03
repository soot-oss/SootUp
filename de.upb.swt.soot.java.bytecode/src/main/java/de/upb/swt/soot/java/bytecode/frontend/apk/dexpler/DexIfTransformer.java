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
import de.upb.swt.soot.core.jimple.common.constant.StringConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JStaticFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import de.upb.swt.soot.core.jimple.visitor.AbstractStmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.tags.ObjectOpTag;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * BodyTransformer to find and change definition of locals used within an if which contains a condition involving two locals
 * ( and not only one local as in DexNullTransformer).
 *
 * It this case, if any of the two locals leads to an object being def or used, all the appropriate defs of the two locals
 * are updated to reflect the use of objects (i.e: 0s are replaced by nulls).
 */
public class DexIfTransformer extends AbstractNullTransformer {
  // Note: we need an instance variable for inner class access, treat this as
  // a local variable (including initialization before use)

  private boolean usedAsObject;
  private boolean doBreak = false;

  public static DexIfTransformer getInstance() {
    return new DexIfTransformer();
  }

  Local l = null;

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    final DexDefUseAnalysis localDefs = new DexDefUseAnalysis(builder);

    Set<JIfStmt  > ifSet = getNullIfCandidates(builder);
    for (JIfStmt    ifs : ifSet) {
       AbstractConditionExpr ifCondition = ifs.getCondition();
      Local[] twoIfLocals = new Local[] { (Local) ifCondition.getOp1(), (Local) ifCondition.getOp2() };
      usedAsObject = false;
      for (Local loc : twoIfLocals) {
        Set<Stmt> defs = localDefs.collectDefinitionsWithAliases(loc);

        // process normally
        doBreak = false;
        for (Stmt stmt : defs) {

          // put correct local in l
          if (stmt instanceof AbstractDefinitionStmt) {
            l = (Local) ((AbstractDefinitionStmt) stmt).getLeftOp();
          } else {
            throw new RuntimeException("ERROR: def can not be something else than Assign or Identity statement! (def: " + stmt
                + " class: " + stmt.getClass() + "");
          }

          // check defs
          stmt.accept(new AbstractStmtVisitor() { // Alex: should also end
            // as soon as detected
            // as not used as an
            // object
            @Override
            public void caseAssignStmt(JAssignStmt stmt) {
              Value r = stmt.getRightOp();
              if (r instanceof Type) {
                usedAsObject = isObject(r.getType());
                if (usedAsObject) {
                  doBreak = true;
                }
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
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              } else if (r instanceof StringConstant || r instanceof JNewExpr || r instanceof JNewArrayExpr) {
                usedAsObject = true;
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              } else if (r instanceof JCastExpr) {
                usedAsObject = isObject(r.getType());
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              } else if (r instanceof AbstractInvokeExpr) {
                usedAsObject = isObject(r.getType());
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              } else if (r instanceof JLengthExpr) {
                usedAsObject = false;
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              }

            }

            @Override
            public void caseIdentityStmt(JIdentityStmt stmt) {
              if (stmt.getLeftOp() == l) {
                usedAsObject = isObject(stmt.getRightOp().getType());
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              }
            }
          });
          if (doBreak) {
            break;
          }

          // check uses
          for (Stmt use : localDefs.getUsesOf(l)) {
            use.accept(new AbstractStmtVisitor() {
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
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              }

              @Override
              public void caseAssignStmt(JAssignStmt stmt) {
                Value left = stmt.getLeftOp();
                Value r = stmt.getRightOp();

                if (left instanceof JArrayRef) {
                  if (((JArrayRef) left).getIndex() == l) {
                    // doBreak = true;
                    return;
                  }
                }

                // IMPOSSIBLE! WOULD BE DEF!
                // // gets value assigned
                // if (stmt.getLeftOp() == l) {
                // if (r instanceof Type)
                // usedAsObject = isObject(((FieldRef)
                // r).getFieldRef().type());
                // else if (r instanceof JArrayRef)
                // usedAsObject = isObject(((JArrayRef)
                // r).getType());
                // else if (r instanceof StringConstant || r
                // instanceof NewExpr || r instanceof
                // NewArrayExpr)
                // usedAsObject = true;
                // else if (r instanceof CastExpr)
                // usedAsObject = isObject
                // (((CastExpr)r).getCastType());
                // else if (r instanceof InvokeExpr)
                // usedAsObject = isObject(((InvokeExpr)
                // r).getType());
                // // introduces alias
                // else if (r instanceof Local) {}
                //
                // }
                // used to assign
                if (stmt.getRightOp() == l) {
                  Value l = stmt.getLeftOp();
                  if (l instanceof JStaticFieldRef && isObject(((JStaticFieldRef) l).getFieldSignature().getType())) {
                    usedAsObject = true;
                    if (usedAsObject) {
                      doBreak = true;
                    }
                    return;
                  } else if (l instanceof JInstanceFieldRef && isObject(((JInstanceFieldRef) l).getFieldSignature().getType())) {
                    usedAsObject = true;
                    if (usedAsObject) {
                      doBreak = true;
                    }
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
                    if (usedAsObject) {
                      doBreak = true;
                    }
                    return;
                  }
                }

                // is used as value (does not exclude
                // assignment)
                if (r instanceof Type) {
                  usedAsObject = true; // isObject(((FieldRef)
                  // r).getFieldRef().type());
                  if (usedAsObject) {
                    doBreak = true;
                  }
                  return;
                } else if (r instanceof JArrayRef) {
                  JArrayRef ar = (JArrayRef) r;
                  if (ar.getBase() == l) {
                    usedAsObject = true;
                  } else { // used as index
                    usedAsObject = false;
                  }
                  if (usedAsObject) {
                    doBreak = true;
                  }
                  return;
                } else if (r instanceof StringConstant || r instanceof JNewExpr) {
                  throw new RuntimeException("NOT POSSIBLE StringConstant or NewExpr at " + stmt);
                } else if (r instanceof JNewArrayExpr) {
                  usedAsObject = false;
                  if (usedAsObject) {
                    doBreak = true;
                  }
                  return;
                } else if (r instanceof JCastExpr) {
                  usedAsObject = isObject(((JCastExpr) r).getType());
                  if (usedAsObject) {
                    doBreak = true;
                  }
                  return;
                } else if (r instanceof AbstractInvokeExpr) {
                  usedAsObject = examineInvokeExpr((AbstractInvokeExpr) stmt.getRightOp());
                  if (usedAsObject) {
                    doBreak = true;
                  }
                  return;
                } else if (r instanceof JLengthExpr) {
                  usedAsObject = true;
                  if (usedAsObject) {
                    doBreak = true;
                  }
                  return;
                } else if (r instanceof AbstractBinopExpr) {
                  usedAsObject = false;
                  if (usedAsObject) {
                    doBreak = true;
                  }
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
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              }

              @Override
              public void caseExitMonitorStmt(JExitMonitorStmt stmt) {
                usedAsObject = stmt.getOp() == l;
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              }

              @Override
              public void caseReturnStmt(JReturnStmt stmt) {
                usedAsObject = stmt.getOp() == l && isObject(builder.getMethodSignature().getType());
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              }

              @Override
              public void caseThrowStmt(JThrowStmt stmt) {
                usedAsObject = stmt.getOp() == l;
                if (usedAsObject) {
                  doBreak = true;
                }
                return;
              }
            });

            if (doBreak) {
              break;
            }

          } // for uses
          if (doBreak) {
            break;
          }
        } // for defs

        if (doBreak) {
          // all defs from the two locals in the if must
          // be updated
          break;
        }

      } // for two locals in if

      // change values
      if (usedAsObject) {
        Set<Stmt> defsOp1 = localDefs.collectDefinitionsWithAliases(twoIfLocals[0]);
        Set<Stmt> defsOp2 = localDefs.collectDefinitionsWithAliases(twoIfLocals[1]);
        defsOp1.addAll(defsOp2);
        for (Stmt u : defsOp1) {
          Stmt s = (Stmt) u;
          // If we have a[x] = 0 and a is an object, we may not conclude 0 -> null
          if (!s.containsArrayRef()
              || (!defsOp1.contains(s.getArrayRef().getBase()) && !defsOp2.contains(s.getArrayRef().getBase()))) {
            replaceWithNull(u);
          }

          Local l = (Local) ((AbstractDefinitionStmt) u).getLeftOp();
          for (Stmt uuse : localDefs.getUsesOf(l)) {
            Stmt use = (Stmt) uuse;
            // If we have a[x] = 0 and a is an object, we may not conclude 0 -> null
            if (!use.containsArrayRef()
                || (twoIfLocals[0] != use.getArrayRef().getBase()) && twoIfLocals[1] != use.getArrayRef().getBase()) {
              replaceWithNull(use);
            }
          }
        }
      } // end if

    } // for if statements
  }

  /**
   * Collect all the if statements comparing two locals with an Eq or Ne expression
   *
   * @param bodyBuilder
   *          the bodyBuilder to analyze
   */
  private Set<JIfStmt   > getNullIfCandidates(Body.BodyBuilder bodyBuilder) {
    Set<JIfStmt   > candidates = new HashSet<JIfStmt   >();
    Iterator<Stmt> i = bodyBuilder.getStmts().iterator();
    while (i.hasNext()) {
      Stmt u = i.next();
      if (u instanceof JIfStmt   ) {
         AbstractConditionExpr expr = ( AbstractConditionExpr) ((JIfStmt   ) u).getCondition();
        boolean isTargetIf = false;
        if (((expr instanceof JEqExpr) || (expr instanceof JNeExpr))) {
          if (expr.getOp1() instanceof Local && expr.getOp2() instanceof Local) {
            isTargetIf = true;
          }
        }
        if (isTargetIf) {
          candidates.add((JIfStmt   ) u);
        }

      }
    }

    return candidates;
  }

}
