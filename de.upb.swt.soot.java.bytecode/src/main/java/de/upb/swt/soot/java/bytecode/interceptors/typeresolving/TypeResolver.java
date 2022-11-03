package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;
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
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.BodyUtils;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.*;

/** @author Zun Wang Algorithm: see 'Efficient Local Type Inference' at OOPSLA 08 */
public class TypeResolver {
  private Body body;
  private Map<Integer, AbstractDefinitionStmt> id2assignments = new HashMap<>();
  private final Map<Local, BitSet> depends = new HashMap<>();
  private final JavaView view;
  private int castCount;

  public TypeResolver(Body.BodyBuilder builder, JavaView view) {
    this.body = builder.build();
    this.view = view;
    init();
  }

  public Body getBody() {
    Typing typing = inferTypes();
    Body.BodyBuilder builder = new Body.BodyBuilder(this.body, Collections.emptySet());
    for (Local local : this.body.getLocals()) {
      Type oldType = local.getType();
      Type newType = typing.getType(local);
      if (oldType.equals(newType)) {
        continue;
      }
      Local newLocal = local.withType(newType);
      BodyUtils.replaceLocalInBuilder(builder, local, newLocal);
    }
    return builder.build();
  }

  public Typing inferTypes() {

    BytecodeHierarchy hierarchy = new BytecodeHierarchy(view);
    AugEvalFunction evalFunction = new AugEvalFunction(view);
    Typing iniTyping = new Typing(body.getLocals());
    Collection<Typing> typings = applyAssignmentConstraint(iniTyping, evalFunction, hierarchy);
    if (typings.isEmpty()) {
      // todo: use another type resolver solution!
      return null;
    }
    Typing typing = getMinCastsTyping(typings, evalFunction, hierarchy);

    if (this.castCount != 0) {
      CastCounter castCounter = new CastCounter(body, evalFunction, hierarchy);
      this.body = castCounter.insertCastStmts(typing);
    }
    TypePromotionVisitor promotionVisitor =
        new TypePromotionVisitor(this.body, evalFunction, hierarchy);
    Typing promotedTyping = promotionVisitor.getPromotedTyping(typing);
    if (promotedTyping == null) {
      // todo: use another type resolver solution!
      return null;
    } else {
      for (Local local : this.body.getLocals()) {
        Type convertedType = convertType(promotedTyping.getType(local));
        if (convertedType != null) {
          promotedTyping.set(local, convertedType);
        }
      }
      return promotedTyping;
    }
  }

  /** observe all definition assignments, add all locals at right-hand-side into the map depends */
  private void init() {
    int assignID = 0;
    for (Stmt stmt : body.getStmts()) {
      if (stmt instanceof AbstractDefinitionStmt) {
        AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) stmt;
        Value lhs = defStmt.getLeftOp();
        if (lhs instanceof Local || lhs instanceof JArrayRef) {
          this.id2assignments.put(assignID, defStmt);
          addDependsForRHS(defStmt.getRightOp(), assignID);
          assignID++;
        }
      }
    }
  }

  private void addDependsForRHS(Value rhs, int id) {
    if (rhs instanceof Local) {
      addDepend((Local) rhs, id);
    } else if (rhs instanceof AbstractBinopExpr) {
      Immediate op1 = ((AbstractBinopExpr) rhs).getOp1();
      Immediate op2 = ((AbstractBinopExpr) rhs).getOp2();
      if (op1 instanceof Local) {
        addDepend((Local) op1, id);
      }
      if (op2 instanceof Local) {
        addDepend((Local) op2, id);
      }
    } else if (rhs instanceof JNegExpr) {
      Immediate op = ((JNegExpr) rhs).getOp();
      if (op instanceof Local) {
        addDepend((Local) op, id);
      }
    } else if (rhs instanceof JCastExpr) {
      Immediate op = ((JCastExpr) rhs).getOp();
      if (op instanceof Local) {
        addDepend((Local) op, id);
      }
    } else if (rhs instanceof JArrayRef) {
      Local base = ((JArrayRef) rhs).getBase();
      addDepend(base, id);
    }
  }

  private void addDepend(Local local, int id) {
    BitSet bitSet = this.depends.get(local);
    if (bitSet == null) {
      bitSet = new BitSet();
      this.depends.put(local, bitSet);
    }
    bitSet.set(id);
  }

  private Collection<Typing> applyAssignmentConstraint(
      Typing typing, AugEvalFunction evalFunction, BytecodeHierarchy hierarchy) {
    int numOfAssigns = this.id2assignments.size();
    if (numOfAssigns == 0) {
      return Collections.emptyList();
    }
    Deque<Typing> workQueue = new ArrayDeque<>();
    List<Typing> ret = new ArrayList<>();

    BitSet stmtsList = new BitSet(numOfAssigns);
    stmtsList.set(0, numOfAssigns);
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
      } else {
        actualSL.clear(stmtId);
        AbstractDefinitionStmt defStmt = this.id2assignments.get(stmtId);
        Value lhs = defStmt.getLeftOp();
        Local local = (lhs instanceof Local) ? (Local) lhs : ((JArrayRef) lhs).getBase();
        Type t_old = actualTyping.getType(local);
        Type t_right = evalFunction.evaluate(actualTyping, defStmt.getRightOp(), defStmt, body);
        if (lhs instanceof JArrayRef) {
          t_right = TypeUtils.makeArrayType(t_right, 1);
        }

        boolean isFirstType = true;
        Collection<Type> leastCommonAncestors = hierarchy.getLeastCommonAncestor(t_old, t_right);
        for (Type type : leastCommonAncestors) {
          if (!type.equals(t_old)) {
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
    }
    minimize(ret, hierarchy);
    return ret;
  }

  /** This method is used to remove the more general typings. */
  private void minimize(List<Typing> typings, BytecodeHierarchy hierarchy) {
    Set<Type> objectLikeTypes = new HashSet<>();
    IdentifierFactory factory = view.getIdentifierFactory();
    ClassType obj = factory.getClassType("java.lang.Object");
    ClassType ser = factory.getClassType("java.io.Serializable");
    ClassType clo = factory.getClassType("java.lang.Cloneable");
    objectLikeTypes.add(obj);
    objectLikeTypes.add(ser);
    objectLikeTypes.add(clo);

    // collect all locals whose types are object, serializable, cloneable
    Set<Local> objectLikeLocals = new HashSet<>();
    Map<Local, Set<Type>> local2Types = getLocal2Types(typings);
    for (Local local : local2Types.keySet()) {
      if (local2Types.get(local).equals(objectLikeTypes)) {
        objectLikeLocals.add(local);
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

  private Map<Local, Set<Type>> getLocal2Types(List<Typing> typings) {
    Map<Local, Set<Type>> map = new HashMap<>();
    for (Typing typing : typings) {
      for (Local local : typing.getLocals()) {
        if (!map.containsKey(local)) {
          map.put(local, new HashSet<>());
        }
        map.get(local).add(typing.getType(local));
      }
    }
    return map;
  }

  private Typing getMinCastsTyping(
      Collection<Typing> typings, AugEvalFunction evalFunction, BytecodeHierarchy hierarchy) {
    CastCounter castCounter = new CastCounter(body, evalFunction, hierarchy);
    Iterator<Typing> typingIterator = typings.iterator();
    Typing ret = null;
    int min = Integer.MAX_VALUE;
    while (typingIterator.hasNext()) {
      Typing typing = typingIterator.next();
      int castCount = castCounter.getCastCount(typing);
      if (castCount < min) {
        min = castCount;
        ret = typing;
      }
    }
    this.castCount = min;
    return ret;
  }

  private Type convertType(Type type) {
    if (type instanceof PrimitiveType.Integer1Type) {
      return PrimitiveType.getBoolean();
    } else if (type instanceof PrimitiveType.Integer127Type) {
      return PrimitiveType.getByte();
    } else if (type instanceof PrimitiveType.Integer32767Type) {
      return PrimitiveType.getShort();
    } else if (type instanceof ArrayType) {
      Type eleType = convertType(((ArrayType) type).getElementType());
      if (eleType != null) {
        return TypeUtils.makeArrayType(eleType, 1);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
}
