package sootup.interceptors.typeresolving;

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

import com.google.common.collect.Lists;
import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.types.*;
import sootup.core.views.View;
import sootup.interceptors.typeresolving.types.AugmentIntegerTypes;
import sootup.interceptors.typeresolving.types.BottomType;
import sootup.interceptors.typeresolving.types.TopType;

/**
 * @author Zun Wang Algorithm started on 'Efficient Local Type Inference' with later inspiration by
 *     'Two Approaches to Fast Bytecode Frontend for Static Analysis'
 */
public class TypeResolver {
  private final ArrayList<AbstractDefinitionStmt> assignments = new ArrayList<>();
  private final Map<Local, BitSet> depends = new HashMap<>();
  private final Map<Local, Set<Type>> useConstraints = new HashMap<>();
  private final View view;

  private final Type objectType;

  private static final Logger logger = LoggerFactory.getLogger(TypeResolver.class);

  public TypeResolver(@NonNull View view) {
    this.view = view;
    objectType = view.getIdentifierFactory().getClassType("java.lang.Object");
  }

  public boolean resolve(Body.@NonNull BodyBuilder builder) {
    init(builder);
    BytecodeHierarchy hierarchy = new BytecodeHierarchy(view);
    AugEvalFunction evalFunction = new AugEvalFunction(view);
    collectUseConstraints(builder);
    final Collection<Local> locals = Lists.newArrayList(builder.getLocals());
    Typing iniTyping = new Typing(locals);
    Typing typing =
        applyAssignmentConstraint(
            builder.getControlFlowGraph(), iniTyping, evalFunction, hierarchy);
    if (typing == null) {
      return false;
    }

    TypePromotionVisitor promotionVisitor =
        new TypePromotionVisitor(builder, evalFunction, hierarchy);
    typing = promotionVisitor.getPromotedTyping(typing);

    // Promote `null`/`BottomType`/'TopType' types to `Object`, and other types which have
    // UnsupportedOperation in TypePromotionVisitor.
    for (Local local : locals) {
      typing.set(local, convertUnderspecifiedType(typing.getType(local)));
    }

    CastCounter minCastsCounter = new CastCounter(builder, evalFunction, hierarchy, typing);
    minCastsCounter.insertCastStmts();
    Typing minCastsTyping = minCastsCounter.getTyping();

    for (Local local : locals) {
      final Type type = minCastsTyping.getType(local);
      if (type == null) {
        continue;
      }
      Type convertedType = convertType(type);
      if (convertedType != null) {
        minCastsTyping.set(local, convertedType);
      }
    }

    locals
        .forEach(
            local -> {
              Type oldType = local.getType();
              Type type = minCastsTyping.getMap().getOrDefault(local, oldType);
              if (type != oldType) {
                Local newLocal = local.withType(type);
                builder.replaceLocal(local, newLocal);
              }
            });
    return true;
  }

  /** find all definition assignments, add all locals at right-hand-side into the map depends */
  private void init(Body.BodyBuilder builder) {
    for (Stmt stmt : builder.getControlFlowGraph()) {
      if (!(stmt instanceof AbstractDefinitionStmt defStmt)) {
        continue;
      }
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

  private void addDependency(@NonNull Local local, int id) {
    BitSet bitSet = depends.computeIfAbsent(local, k -> new BitSet());
    bitSet.set(id);
  }

  private Typing applyAssignmentConstraint(
      @NonNull ControlFlowGraph<?> graph,
      @NonNull Typing typing,
      @NonNull AugEvalFunction evalFunction,
      @NonNull BytecodeHierarchy hierarchy) {

    final int numOfAssignments = assignments.size();
    if (numOfAssignments == 0) {
      return null;
    }

    BitSet pending = new BitSet(numOfAssignments);
    pending.set(0, numOfAssignments);
    typing.setStmtsIDList(pending);

    while (true) {
      int stmtId = pending.nextSetBit(0);
      if (stmtId == -1) break;
      pending.clear(stmtId);

      AbstractDefinitionStmt defStmt = this.assignments.get(stmtId);
      Value lhs = defStmt.getLeftOp();
      Local local;
      if (lhs instanceof Local) {
        local = (Local) lhs;
      } else if (lhs instanceof JArrayRef) {
        local = ((JArrayRef) lhs).getBase();
      } else if (lhs instanceof JInstanceFieldRef) {
        continue; // assignment to a field is independent of the base type
      } else {
        throw new IllegalStateException("can not handle " + lhs.getClass());
      }

      Type rhsType = evalFunction.evaluate(typing, defStmt.getRightOp(), defStmt, graph);
      if (rhsType == null) {
        // RHS type not yet determinable; will be re-queued when dependencies resolve
        continue;
      }

      Type oldType = typing.getType(local);
      if (oldType == null) {
        logger.info("Body.locals do not match the Locals occurring in the Stmts.");
        continue;
      }

      Collection<Type> leastCommonAncestors;
      if (lhs instanceof JArrayRef) {
        // `local[index] = rhs` -> `local` should have the type `rhs[]`
        if (oldType instanceof ArrayType) {
          Type elementType = ((ArrayType) oldType).getElementType();
          if (elementType instanceof PrimitiveType) {
            // Can't always change the type of the array when it is a primitive array.
            // Take the following example: `l1 = newarray (byte)[1]; l1[0] = l0;`, with `l0` being
            // an `int` (see `testMixedPrimitiveArray`).
            // At the `l1[0] = l0` statement, `l1` has to stay as a `byte[]` and can't be upgraded
            // to an `int[]` because otherwise the first statement becomes invalid.
            continue;
          }
          Collection<Type> lcaElement = hierarchy.getLeastCommonAncestors(elementType, rhsType);
          leastCommonAncestors =
              lcaElement.stream()
                  .map(type -> Type.createArrayType(type, 1))
                  .collect(Collectors.toSet());
        } else {
          leastCommonAncestors =
              hierarchy.getLeastCommonAncestors(oldType, Type.createArrayType(rhsType, 1));
        }
      } else {
        leastCommonAncestors = hierarchy.getLeastCommonAncestors(oldType, rhsType);
      }

      assert !leastCommonAncestors.isEmpty();

      // Pick the best single type — no branching
      Type selectedType = selectType(leastCommonAncestors, local, hierarchy);
      if (!selectedType.equals(oldType)) {
        typing.set(local, selectedType);
        BitSet dependStmtList = this.depends.get(local);
        if (dependStmtList != null) {
          pending.or(dependStmtList);
        }
      }
    }

    return typing;
  }

  /**
   * Selects the best type from LCA candidates using pre-collected use constraints.
   *
   * <p>Filters candidates to those that satisfy all use constraints (i.e., are subtypes of every
   * required type at each use site). If multiple candidates survive, returns the first; if none
   * survive, falls back to the first candidate.
   */
  private Type selectType(Collection<Type> candidates, Local local, BytecodeHierarchy hierarchy) {
    if (candidates.size() == 1) {
      return candidates.iterator().next();
    }

    Set<Type> constraints = useConstraints.getOrDefault(local, Collections.emptySet());
    if (!constraints.isEmpty()) {
      for (Type candidate : candidates) {
        boolean satisfiesAll = true;
        for (Type required : constraints) {
          if (!hierarchy.isAncestor(required, candidate)) {
            satisfiesAll = false;
            break;
          }
        }
        if (satisfiesAll) {
          return candidate;
        }
      }
    }

    return candidates.iterator().next();
  }

  /**
   * Pre-collects use constraints: for each local, the set of types it must be a subtype of at its
   * use sites. This avoids the exponential branching that would otherwise arise when the LCA of two
   * types has multiple minimal candidates.
   */
  private void collectUseConstraints(Body.BodyBuilder builder) {
    for (Stmt stmt : builder.getControlFlowGraph()) {
      if (stmt instanceof JInvokeStmt) {
        collectInvokeConstraints(((JInvokeStmt) stmt).getInvokeExpr().get());
      } else if (stmt instanceof JAssignStmt assignStmt) {
        Value rhs = assignStmt.getRightOp();
        if (rhs instanceof AbstractInvokeExpr) {
          collectInvokeConstraints((AbstractInvokeExpr) rhs);
        }
        collectFieldConstraint(assignStmt.getLeftOp());
        collectFieldConstraint(assignStmt.getRightOp());
      } else if (stmt instanceof JReturnStmt) {
        Value op = ((JReturnStmt) stmt).getOp();
        if (op instanceof Local) {
          addUseConstraint((Local) op, builder.getMethodSignature().getType());
        }
      }
    }
  }

  private void collectInvokeConstraints(AbstractInvokeExpr invoke) {
    if (invoke instanceof AbstractInstanceInvokeExpr) {
      Local base = ((AbstractInstanceInvokeExpr) invoke).getBase();
      addUseConstraint(base, invoke.getMethodSignature().getDeclClassType());
    }
    List<Type> paramTypes = invoke.getMethodSignature().getParameterTypes();
    for (int i = 0; i < invoke.getArgCount(); i++) {
      Value arg = invoke.getArg(i);
      if (arg instanceof Local) {
        addUseConstraint((Local) arg, paramTypes.get(i));
      }
    }
  }

  private void collectFieldConstraint(Value value) {
    if (value instanceof JInstanceFieldRef instanceFieldRef) {
      Local base = instanceFieldRef.getBase();
      addUseConstraint(base, instanceFieldRef.getFieldSignature().getDeclClassType());
    }
  }

  private void addUseConstraint(Local local, Type requiredType) {
    if (requiredType instanceof ClassType || requiredType instanceof ArrayType) {
      useConstraints.computeIfAbsent(local, k -> new HashSet<>()).add(requiredType);
    }
  }

  private Type convertUnderspecifiedType(@NonNull Type type) {
    if (type instanceof ArrayType) {
      Type elementType = convertUnderspecifiedType(((ArrayType) type).getElementType());
      return Type.createArrayType(elementType, 1);
    } else if (type instanceof NullType || type instanceof BottomType || type instanceof TopType) {
      // Convert `null`/`BottomType`/`TopType` types to `java.lang.Object`.
      // Top Type can show up when in a simple try-catch block, least common ancestor is determined
      // as TopType for int and ArithmeticException
      // `null` can show up when a variable never gets a non-null value assigned to it.
      // `BottomType` can show up when a variable only every gets assigned from "impossible"
      // operations, e.g., indexing into `null`, or never gets assigned at all.
      // Choosing `java.lang.Object` is an arbitrary choice.
      // It is probably possible to use the debug information to choose a type here, but that
      // complexity is not worth it for such an edge case.
      return objectType;
    } else if (type instanceof AugmentIntegerTypes.Integer1Type) {
      return PrimitiveType.getBoolean();
    } else if (type instanceof AugmentIntegerTypes.Integer127Type) {
      return PrimitiveType.getByte();
    } else if (type instanceof AugmentIntegerTypes.Integer32767Type) {
      return PrimitiveType.getShort();
    } else {
      return type;
    }
  }

  private Type convertType(@NonNull Type type) {
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
