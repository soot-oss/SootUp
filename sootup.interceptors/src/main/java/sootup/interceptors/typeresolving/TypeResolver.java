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
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.IdentifierFactory;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNegExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ArrayType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.interceptors.typeresolving.types.AugmentIntegerTypes;
import sootup.interceptors.typeresolving.types.BottomType;
import sootup.interceptors.typeresolving.types.TopType;
import sootup.java.core.views.JavaView;

/** @author Zun Wang Algorithm: see 'Efficient Local Type Inference' at OOPSLA 08 */
public class TypeResolver {
  private final ArrayList<AbstractDefinitionStmt> assignments = new ArrayList<>();
  private final Map<Local, BitSet> depends = new HashMap<>();
  private final JavaView view;

  private final Type objectType;

  private static final Logger logger = LoggerFactory.getLogger(TypeResolver.class);

  public TypeResolver(@Nonnull JavaView view) {
    this.view = view;
    objectType = view.getIdentifierFactory().getClassType("java.lang.Object");
  }

  public boolean resolve(@Nonnull Body.BodyBuilder builder) {
    init(builder);
    BytecodeHierarchy hierarchy = new BytecodeHierarchy(view);
    AugEvalFunction evalFunction = new AugEvalFunction(view);
    final Collection<Local> locals = Lists.newArrayList(builder.getLocals());
    Typing iniTyping = new Typing(locals);
    Collection<Typing> typings =
        applyAssignmentConstraint(builder.getStmtGraph(), iniTyping, evalFunction, hierarchy);
    if (typings.isEmpty()) {
      return false;
    }

    TypePromotionVisitor promotionVisitor =
        new TypePromotionVisitor(builder, evalFunction, hierarchy);
    typings = typings.stream().map(promotionVisitor::getPromotedTyping).collect(Collectors.toSet());

    // Promote `null`/`BottomType`/'TopType' types to `Object`, and other types which have
    // UnsupportedOperation in TypePromotionVisitor.
    for (Typing typing : typings) {
      for (Local local : locals) {
        typing.set(local, convertUnderspecifiedType(typing.getType(local)));
      }
    }

    CastCounter minCastsCounter = getMinCastsCounter(builder, typings, evalFunction, hierarchy);
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

    locals.stream()
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
      if (lhs instanceof Local) {
        local = (Local) lhs;
      } else if (lhs instanceof JArrayRef) {
        local = ((JArrayRef) lhs).getBase();
      } else if (lhs instanceof JInstanceFieldRef) {
        // local = ((JInstanceFieldRef) lhs).getBase();
        continue; // assigment to a field is independent of the base type.
      } else {
        // Only `Local`s and `JArrayRef`s as the left-hand side are relevant for type inference.
        // The statements get filtered to only contain those assignments in the `init` method,
        // so this branch shouldn't happen.
        throw new IllegalStateException("can not handle " + lhs.getClass());
      }

      Type rhsType = evalFunction.evaluate(actualTyping, defStmt.getRightOp(), defStmt, graph);
      if (rhsType == null) {
        workQueue.removeFirst();
        continue;
      }

      Type oldType = actualTyping.getType(local);
      if (oldType == null) {
        // Body.getLocals() contains Locals that are not in Stmts (anymore?)
        logger.info("Body.locals do not match the Locals occurring in the Stmts.");
        continue;
      }
      Collection<Type> leastCommonAncestors;
      if (lhs instanceof JArrayRef) {
        // `local[index] = rhs` -> `local` should have the type `[rhs][]`
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

          // when `local` has an array type, the type of `rhs` needs to be assignable as an element
          // of that array
          Collection<Type> leastCommonAncestorsElement =
              hierarchy.getLeastCommonAncestors(elementType, rhsType);
          leastCommonAncestors =
              leastCommonAncestorsElement.stream()
                  .map(type -> Type.createArrayType(type, 1))
                  .collect(Collectors.toSet());
        } else {
          // when `local` isn't an array type, but is used as an array, its type has to be
          // compatible with `[rhs][]`
          leastCommonAncestors =
              hierarchy.getLeastCommonAncestors(oldType, Type.createArrayType(rhsType, 1));
        }
      } else {
        leastCommonAncestors = hierarchy.getLeastCommonAncestors(oldType, rhsType);
      }

      assert !leastCommonAncestors.isEmpty();

      boolean isFirstType = true;
      for (Type type : leastCommonAncestors) {
        if (!type.equals(oldType)) {
          BitSet dependStmtList = this.depends.get(local);
          // Up to now there's no ambiguity of types
          if (isFirstType) {
            isFirstType = false;
          } else {
            // Ambiguity handling: create new Typing and add it into workQueue
            actualTyping = new Typing(actualTyping, (BitSet) actualSL.clone());
            workQueue.add(actualTyping);
            actualSL = actualTyping.getStmtsIDList();
          }

          actualTyping.set(local, type);

          // Type is changed, the associated definition stmts are necessary handled again
          if (dependStmtList != null) {
            actualSL.or(dependStmtList);
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

  private CastCounter getMinCastsCounter(
      @Nonnull Body.BodyBuilder builder,
      @Nonnull Collection<Typing> typings,
      @Nonnull AugEvalFunction evalFunction,
      @Nonnull BytecodeHierarchy hierarchy) {
    return typings.stream()
        .map(typing -> new CastCounter(builder, evalFunction, hierarchy, typing))
        .min(Comparator.comparingInt(CastCounter::getCastCount))
        .get();
  }

  private Type convertUnderspecifiedType(@Nonnull Type type) {
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
