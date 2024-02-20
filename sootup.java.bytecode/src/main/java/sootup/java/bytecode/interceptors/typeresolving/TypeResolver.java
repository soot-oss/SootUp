package sootup.java.bytecode.interceptors.typeresolving;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Zun Wang
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
import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNegExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ArrayType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.bytecode.interceptors.typeresolving.types.AugmentIntegerTypes;
import sootup.java.core.views.JavaView;

/** @author Zun Wang Algorithm: see 'Efficient Local Type Inference' at OOPSLA 08 */
public class TypeResolver {
  private final ArrayList<AbstractDefinitionStmt> assignments = new ArrayList<>();
  private final Map<Local, BitSet> depends = new HashMap<>();
  private final JavaView view;
  private int castCount;

  public TypeResolver(@Nonnull JavaView view) {
    this.view = view;
  }

  public boolean resolve(@Nonnull Body.BodyBuilder builder) {
    init(builder);
    BytecodeHierarchy hierarchy = new BytecodeHierarchy(view);
    AugEvalFunction evalFunction = new AugEvalFunction(view);
    Typing iniTyping = new Typing(builder.getLocals());
    Collection<Typing> typings =
        applyAssignmentConstraint(builder.getStmtGraph(), iniTyping, evalFunction, hierarchy);
    if (typings.isEmpty()) {
      return false;
    }

    Typing minCastsTyping = getMinCastsTyping(builder, typings, evalFunction, hierarchy);
    if (this.castCount > 0) {
      CastCounter castCounter = new CastCounter(builder, evalFunction, hierarchy);
      castCounter.insertCastStmts(minCastsTyping);
    }

    TypePromotionVisitor promotionVisitor =
        new TypePromotionVisitor(builder, evalFunction, hierarchy);
    Typing promotedTyping = promotionVisitor.getPromotedTyping(minCastsTyping);
    if (promotedTyping == null) {
      return false;
    }

    for (Local local : builder.getLocals()) {
      final Type type = promotedTyping.getType(local);
      if (type == null) {
        continue;
      }
      Type convertedType = convertType(type);
      if (convertedType != null) {
        promotedTyping.set(local, convertedType);
      }
    }

    builder.setLocals(
        builder.getLocals().stream()
            .map(
                local -> {
                  Type type = promotedTyping.getMap().getOrDefault(local, local.getType());
                  return local.withType(type);
                })
            .collect(Collectors.toSet()));
    return true;
  }

  /** find all definition assignments, add all locals at right-hand-side into the map depends */
  private void init(Body.BodyBuilder builder) {
    for (Stmt stmt : builder.getStmtGraph()) {
      if (!(stmt instanceof AbstractDefinitionStmt)) {
        continue;
      }
      AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) stmt;
      Value lhs = defStmt.getLeftOp();
      if (lhs instanceof Local || lhs instanceof JArrayRef) {
        final int defStmtId = assignments.size();
        assignments.add(defStmt);
        addDependsForRHS(defStmt.getRightOp(), defStmtId);
      }
    }
  }

  private void addDependsForRHS(Value rhs, int id) {
    if (rhs instanceof Local) {
      addDependency((Local) rhs, id);
    } else if (rhs instanceof AbstractBinopExpr) {
      Immediate op1 = ((AbstractBinopExpr) rhs).getOp1();
      Immediate op2 = ((AbstractBinopExpr) rhs).getOp2();
      if (op1 instanceof Local) {
        addDependency((Local) op1, id);
      }
      if (op2 instanceof Local) {
        addDependency((Local) op2, id);
      }
    } else if (rhs instanceof JNegExpr) {
      Immediate op = ((JNegExpr) rhs).getOp();
      if (op instanceof Local) {
        addDependency((Local) op, id);
      }
    } else if (rhs instanceof JCastExpr) {
      Immediate op = ((JCastExpr) rhs).getOp();
      if (op instanceof Local) {
        addDependency((Local) op, id);
      }
    } else if (rhs instanceof JArrayRef) {
      Local base = ((JArrayRef) rhs).getBase();
      addDependency(base, id);
    }
  }

  private void addDependency(@Nonnull Local local, int id) {
    BitSet bitSet = depends.computeIfAbsent(local, k -> new BitSet());
    bitSet.set(id);
  }

  private Collection<Typing> applyAssignmentConstraint(
      @Nonnull StmtGraph<?> graph,
      @Nonnull Typing typing,
      @Nonnull AugEvalFunction evalFunction,
      @Nonnull BytecodeHierarchy hierarchy) {

    final int numOfAssignments = assignments.size();
    if (numOfAssignments == 0) {
      return Collections.emptyList();
    }

    Deque<Typing> workQueue = new ArrayDeque<>();
    List<Typing> ret = new ArrayList<>();

    BitSet stmtsList = new BitSet(numOfAssignments);
    stmtsList.set(0, numOfAssignments);
    typing.setStmtsIDList(stmtsList);
    workQueue.add(typing);

    while (!workQueue.isEmpty()) {
      Typing actualTyping = workQueue.getFirst();
      BitSet actualSL = actualTyping.getStmtsIDList();
      int stmtId = actualSL.nextSetBit(0);
      // all definition stmts are handled
      if (stmtId == -1) {
        ret.add(actualTyping);
        workQueue.removeFirst();
        continue;
      }

      actualSL.clear(stmtId);
      AbstractDefinitionStmt defStmt = this.assignments.get(stmtId);
      Value lhs = defStmt.getLeftOp();
      Local local;
      Type oldType;
      if (lhs instanceof Local) {
        local = (Local) lhs;
        oldType = typing.getType(local);
      } else if (lhs instanceof JArrayRef) {
        local = ((JArrayRef) lhs).getBase();
        oldType = typing.getType(local);
        if (oldType instanceof ArrayType) {
          oldType = ((ArrayType) oldType).getElementType();
        } else {
          assert oldType == NullType.getInstance();
        }
      } else {
        // Only `Local`s and `JArrayRef`s as the left-hand side are relevant for type inference.
        // The statements get filtered to only contain those assignments in the `init` method,
        // so this branch shouldn't happen.
        throw new IllegalStateException("can not handle " + lhs.getClass());
      }

      Type rightOpDerivedType =
          evalFunction.evaluate(actualTyping, defStmt.getRightOp(), defStmt, graph);
      if (rightOpDerivedType == null) {
        workQueue.removeFirst();
        continue;
      }

      boolean isFirstType = true;
      Collection<Type> leastCommonAncestors =
          hierarchy.getLeastCommonAncestor(oldType, rightOpDerivedType);

      if (lhs instanceof JArrayRef) {
        // To find the correct type of `local` in an assignment like `local[index] = rhs`,
        // the type of the right-hand side and the element type of `local` are used above,
        // and changed back into the actual array type here.
        leastCommonAncestors =
            leastCommonAncestors.stream()
                .map(
                    type -> {
                      if (type == NullType.getInstance()) {
                        // prevent a `null[]` and use an `Object[]` instead
                        return hierarchy.objectClassType;
                      } else {
                        return type;
                      }
                    })
                .map(type -> Type.createArrayType(type, 1))
                .collect(Collectors.toSet());
      }

      assert !leastCommonAncestors.isEmpty();
      for (Type type : leastCommonAncestors) {
        if (!type.equals(oldType)) {
          BitSet dependStmtList = this.depends.get(local);
          // Up to now there's no ambiguity of types
          if (isFirstType) {
            actualTyping.set(local, type);
            // Type is changed, the associated definition stmts are necessary handled again
            if (dependStmtList != null) {
              actualSL.or(dependStmtList);
            }
            isFirstType = false;
          } else {
            // Ambiguity handling: create new Typing and add it into workQueue
            Typing newTyping = new Typing(actualTyping, (BitSet) actualSL.clone());
            workQueue.add(newTyping);

            BitSet newSL = newTyping.getStmtsIDList();
            newTyping.set(local, type);
            if (dependStmtList != null) {
              newSL.or(dependStmtList);
            }
          }
        }
      }
    }
    minimize(ret, hierarchy);
    return ret;
  }

  /** This method is used to remove the more general typings. */
  private void minimize(@Nonnull List<Typing> typings, @Nonnull BytecodeHierarchy hierarchy) {
    Set<Type> objectLikeTypes = new HashSet<>();
    // FIXME: [ms] handle java modules as well!
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    objectLikeTypes.add(identifierFactory.getClassType("java.lang.Object"));
    objectLikeTypes.add(identifierFactory.getClassType("java.io.Serializable"));
    objectLikeTypes.add(identifierFactory.getClassType("java.lang.Cloneable"));

    // collect all locals whose types are object, serializable, cloneable
    Set<Local> objectLikeLocals = new HashSet<>();
    Map<Local, Set<Type>> local2Types = getLocal2Types(typings);
    for (Map.Entry<Local, Set<Type>> local : local2Types.entrySet()) {
      if (local.getValue().equals(objectLikeTypes)) {
        objectLikeLocals.add(local.getKey());
      }
    }
    // if one typing is more general als another typing, it should be removed.
    List<Typing> typings_clo = new ArrayList<>(typings);
    for (Typing tpi : typings_clo) {
      for (Typing tpj : typings_clo) {
        if (tpi.compare(tpj, hierarchy, objectLikeLocals) == 1) {
          typings.remove(tpi);
          break;
        }
      }
    }
  }

  private Map<Local, Set<Type>> getLocal2Types(@Nonnull List<Typing> typings) {
    Map<Local, Set<Type>> map = new HashMap<>();
    for (Typing typing : typings) {
      for (Local local : typing.getLocals()) {
        Set<Type> types = map.computeIfAbsent(local, k -> new HashSet<>());
        types.add(typing.getType(local));
      }
    }
    return map;
  }

  private Typing getMinCastsTyping(
      @Nonnull Body.BodyBuilder builder,
      @Nonnull Collection<Typing> typings,
      @Nonnull AugEvalFunction evalFunction,
      @Nonnull BytecodeHierarchy hierarchy) {
    CastCounter castCounter = new CastCounter(builder, evalFunction, hierarchy);
    Typing minTyping =
        typings.stream().min(Comparator.comparingInt(castCounter::getCastCount)).get();
    this.castCount = castCounter.getCastCount(minTyping);
    return minTyping;
  }

  private Type convertType(@Nonnull Type type) {
    if (type instanceof AugmentIntegerTypes.Integer1Type) {
      return PrimitiveType.getBoolean();
    } else if (type instanceof AugmentIntegerTypes.Integer127Type) {
      return PrimitiveType.getByte();
    } else if (type instanceof AugmentIntegerTypes.Integer32767Type) {
      return PrimitiveType.getShort();
    } else if (type instanceof ArrayType) {
      Type eleType = convertType(((ArrayType) type).getElementType());
      if (eleType != null) {
        return Type.createArrayType(eleType, 1);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
}
