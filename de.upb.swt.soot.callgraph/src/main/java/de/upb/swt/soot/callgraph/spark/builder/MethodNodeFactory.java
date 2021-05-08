package de.upb.swt.soot.callgraph.spark.builder;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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

import de.upb.swt.soot.callgraph.spark.pag.IntraproceduralPointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import de.upb.swt.soot.callgraph.spark.pointsto.PointsToAnalysis;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.constant.StringConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.*;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.visitor.AbstractJimpleValueVisitor;
import de.upb.swt.soot.core.jimple.visitor.AbstractStmtVisitor;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class MethodNodeFactory extends AbstractJimpleValueVisitor<Node> {
  private SootMethod method;
  private IntraproceduralPointerAssignmentGraph intraPag;
  private PointerAssignmentGraph pag;
  private View<? extends SootClass> view;

  protected final ReferenceType rtClass;
  protected final ReferenceType rtObject;
  protected final ReferenceType rtStringType;
  protected final ReferenceType rtHashSet;
  protected final ReferenceType rtHashMap;
  protected final ReferenceType rtLinkedList;
  protected final ReferenceType rtHashtableEmptyIterator;
  protected final ReferenceType rtHashtableEmptyEnumerator;

  public MethodNodeFactory(IntraproceduralPointerAssignmentGraph intraPag) {
    this.intraPag = intraPag;
    this.pag = intraPag.getPointerAssignmentGraph();
    this.view = pag.getView();
    setMethod(intraPag.getMethod());

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    rtClass = identifierFactory.getClassType(NodeConstants.CLASS);
    rtObject = identifierFactory.getClassType(NodeConstants.OBJECT);
    rtStringType = identifierFactory.getClassType(NodeConstants.STRING);
    rtHashSet = identifierFactory.getClassType(NodeConstants.HASH_SET);
    rtHashMap = identifierFactory.getClassType(NodeConstants.HASH_MAP);
    rtLinkedList = identifierFactory.getClassType(NodeConstants.LINKED_LIST);
    rtHashtableEmptyIterator =
        identifierFactory.getClassType(NodeConstants.HASH_TABLE_EMPTY_ITERATOR);
    rtHashtableEmptyEnumerator =
        identifierFactory.getClassType(NodeConstants.HASH_TABLE_EMPTY_NUMERATOR);
  }

  /** Sets the method for which a graph is currently being built. */
  private void setMethod(SootMethod m) {
    method = m;
    if (!m.isStatic()) {
      view.getClassOrThrow(m.getDeclaringClassType());
      caseThis();
    }
    for (int i = 0; i < m.getParameterCount(); i++) {
      if (m.getParameterType(i) instanceof ReferenceType) {
        caseParameter(i);
      }
    }
    Type retType = m.getReturnTypeSignature();
    if (retType instanceof ReferenceType) {
      caseReturn();
    }
  }

  public Node getNode(Value v) {
    v.accept(this);
    return getNode();
  }

  public final Node getNode() {
    return getResult();
  }

  public void processStmt(Stmt stmt) {
    if (!canProcess(stmt)) {
      return;
    }
    stmt.accept(
        new AbstractStmtVisitor() {
          @Override
          public final void caseAssignStmt(JAssignStmt stmt) {
            Value leftOp = stmt.getLeftOp();
            Value rightOp = stmt.getRightOp();
            if (!(leftOp.getType() instanceof ReferenceType)) {
              return;
            }
            if (!(rightOp.getType() instanceof ReferenceType)) {
              throw new AssertionError(
                  "Type mismatch in assignment " + stmt + " in method " + method.getSignature());
            }
            leftOp.accept(MethodNodeFactory.this);
            Node target = getNode();
            rightOp.accept(MethodNodeFactory.this);
            Node source = getNode();
            if (leftOp instanceof JInstanceFieldRef) {
              ((JInstanceFieldRef) leftOp).getBase().accept(MethodNodeFactory.this);
              pag.addDereference((VariableNode) getNode());
            }
            if (rightOp instanceof JInstanceFieldRef) {
              ((JInstanceFieldRef) rightOp).getBase().accept(MethodNodeFactory.this);
              pag.addDereference((VariableNode) getNode());
            } else if (rightOp instanceof JStaticFieldRef) {
              JStaticFieldRef staticFieldRef = (JStaticFieldRef) rightOp;
              staticFieldRef.getFieldSignature();
              if(pag.getSparkOptions().isEmptiesAsAllocs()){
                String className = staticFieldRef.getFieldSignature().getDeclClassType().getClassName();
                if(className.equals("java.util.Collections")){
                  // if (s.name().equals("EMPTY_SET")) {
                  //                src = pag.makeAllocNode(rtHashSet, rtHashSet, method);
                  //              } else if (s.name().equals("EMPTY_MAP")) {
                  //                src = pag.makeAllocNode(rtHashMap, rtHashMap, method);
                  //              } else if (s.name().equals("EMPTY_LIST")) {
                  //                src = pag.makeAllocNode(rtLinkedList, rtLinkedList, method);
                  //              }
                  if(staticFieldRef.getFieldSignature().getName().equals("EMPTY_SET")){
                    source = pag.getOrCreateAllocationNode(rtHashSet, rtHashSet, method);
                  } else if(staticFieldRef.getFieldSignature().getName().equals("EMPTY_MAP")){
                    source = pag.getOrCreateAllocationNode(rtHashMap, rtHashMap, method);
                  } else if(staticFieldRef.getFieldSignature().getName().equals("EMPTY_LIST")){
                    source = pag.getOrCreateAllocationNode(rtLinkedList, rtLinkedList, method);
                  }
                } else if(className.equals("java.util.Hashtable")){
                  if(staticFieldRef.getFieldSignature().getName().equals("emptyIterator")){
                    source = pag.getOrCreateAllocationNode(rtHashtableEmptyIterator, rtHashtableEmptyIterator, method);
                  } else if(staticFieldRef.getFieldSignature().getName().equals("emptyEnumerator")){
                    source = pag.getOrCreateAllocationNode(rtHashtableEmptyEnumerator, rtHashtableEmptyEnumerator, method);
                  }
                }
              }
            }
            intraPag.addInternalEdge(source, target);
          }

          @Override
          public void caseReturnStmt(JReturnStmt returnStmt) {
            if (!(returnStmt.getOp().getType() instanceof ReferenceType)) {
              return;
            }
            returnStmt.getOp().accept(MethodNodeFactory.this);
            Node returnNode = getNode();
            intraPag.addInternalEdge(returnNode, caseReturn());
          }

          @Override
          public void caseIdentityStmt(JIdentityStmt identityStmt) {
            if (!(identityStmt.getLeftOp().getType() instanceof ReferenceType)) {
              // TODO: why is Object <init> l0 unknown?
              if (!identityStmt.getRightOp().getType().toString().equals("java.lang.Object")) {
                return;
              }
            }
            Value leftOp = identityStmt.getLeftOp();
            Value rightOp = identityStmt.getRightOp();
            leftOp.accept(MethodNodeFactory.this);
            Node target = getNode();
            rightOp.accept(MethodNodeFactory.this);
            Node source = getNode();
            intraPag.addInternalEdge(source, target);

            // TODO: SPARK_OPT library_disabled

          }

          @Override
          public void caseThrowStmt(JThrowStmt throwStmt) {
            throwStmt.getOp().accept(MethodNodeFactory.this);
            intraPag.addOutEdge(getNode(), pag.getNodeFactory().caseThrow());
          }
        });
  }

  private boolean canProcess(Stmt stmt) {
    // TODO: SPARK_OPT types-for-invoke
    if (stmt.containsInvokeExpr()) {
      AbstractInvokeExpr invokeExpr = stmt.getInvokeExpr();
      if (!isReflectionNewInstance(invokeExpr) || !(invokeExpr instanceof JStaticInvokeExpr)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether the given invocation is for Class.newInstance()
   *
   * @param invokeExpr
   * @return
   */
  private boolean isReflectionNewInstance(AbstractInvokeExpr invokeExpr) {
    // TODO: put this in a utility class?
    if (invokeExpr instanceof JVirtualInvokeExpr) {
      JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) invokeExpr;
      if (virtualInvokeExpr.getBase().getType() instanceof JavaClassType) {
        JavaClassType rt = (JavaClassType) virtualInvokeExpr.getBase().getType();
        if (rt.getFullyQualifiedName().equals("java.lang.class")) {
          MethodSignature signature = virtualInvokeExpr.getMethodSignature();
          if (signature.getName().equals("newInstance")
              && signature.getParameterTypes().isEmpty()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public Node caseThis() {
    VariableNode node =
        pag.getOrCreateLocalVariableNode(
            new ImmutablePair<SootMethod, String>(method, PointsToAnalysis.THIS_NODE),
            method.getDeclaringClassType(),
            method);
    // TODO: setInterProcTarget
    return node;
  }

  public Node caseParameter(int index) {
    VariableNode node =
        pag.getOrCreateLocalVariableNode(
            new ImmutablePair<SootMethod, Integer>(method, index),
            method.getParameterType(index),
            method);
    // TODO: setInterProcTarget
    return node;
  }

  public Node caseReturn() {
    VariableNode node =
        pag.getOrCreateLocalVariableNode(
            new ImmutablePair<SootMethod, String>(method, PointsToAnalysis.RETURN_NODE),
            method.getReturnTypeSignature(),
            method);
    // TODO: setInterProcTarget
    return node;
  }

  public Node caseArray(VariableNode base) {
    return pag.getOrCreateFieldReferenceNode(base, new ArrayElement());
  }

  @Override
  public void caseArrayRef(JArrayRef ref) {
    caseLocal((Local) ref.getBase());
    setResult(caseArray((VariableNode) getNode()));
  }

  @Override
  public void caseCastExpr(JCastExpr castExpr) {
    Pair<JCastExpr, String> castPair = new ImmutablePair<>(castExpr, PointsToAnalysis.CAST_NODE);
    castExpr.getOp().accept(this);
    Node opNode = getNode();
    Node castNode = pag.getOrCreateLocalVariableNode(castPair, castExpr.getType(), method);
    intraPag.addInternalEdge(opNode, castNode);
    setResult(castNode);
  }

  @Override
  public void caseCaughtExceptionRef(JCaughtExceptionRef ref) {
    setResult(pag.getNodeFactory().caseThrow());
  }

  @Override
  public void caseInstanceFieldRef(JInstanceFieldRef ref) {
    Optional<SootField> field = ref.getField(view);
    if (field.isPresent()) {
      if(pag.getSparkOptions().isFieldBased() || pag.getSparkOptions().isVta()){
        setResult(pag.getOrCreateGlobalVariableNode(field.get(), field.get().getType()));
      } else {
        setResult(
                pag.getOrCreateLocalFieldReferenceNode(
                        ref.getBase(), ref.getBase().getType(), field.get(), method));
      }
    } else {
      throw new RuntimeException("Field not present on ref:" + ref);
    }
  }

  @Override
  public void caseLocal(Local local) {
    setResult(pag.getOrCreateLocalVariableNode(local, local.getType(), method));
  }

  @Override
  public void caseNewArrayExpr(JNewArrayExpr expr) {
    setResult(pag.getOrCreateAllocationNode(expr, expr.getType(), method));
  }

  private boolean isStringBuffer(Type type) {
    if (type instanceof JavaClassType) {
      JavaClassType refType = (JavaClassType) type;
      String fullName = refType.getFullyQualifiedName();
      if (fullName.equals("java.lang.StringBuffer") || fullName.equals("java.lang.StringBuilder")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void caseNewExpr(JNewExpr expr) {
    if(pag.getSparkOptions().isMergeStringBuffer() && isStringBuffer(expr.getType())){
      setResult(pag.getOrCreateAllocationNode(expr.getType(), expr.getType(), null));
    }
    setResult(pag.getOrCreateAllocationNode(expr, expr.getType(), method));
  }

  @Override
  public void caseNewMultiArrayExpr(JNewMultiArrayExpr expr) {
    ArrayType type = (ArrayType) expr.getType();
    AllocationNode prevAllocationNode =
        pag.getOrCreateAllocationNode(
            new ImmutablePair<Expr, Integer>(expr, type.getDimension()), type, method);
    VariableNode prevVariableNode =
        pag.getOrCreateLocalVariableNode(prevAllocationNode, prevAllocationNode.getType(), method);
    intraPag.addInternalEdge(prevAllocationNode, prevVariableNode);
    setResult(prevAllocationNode);
    // TODO: do we need to handle elementType?
  }

  @Override
  public void caseParameterRef(JParameterRef ref) {
    setResult(caseParameter(ref.getIndex()));
  }

  @Override
  public void caseStaticFieldRef(JStaticFieldRef ref) {
    Optional<SootField> field = ref.getField(view);
    if (field.isPresent()) {
      setResult(pag.getOrCreateGlobalVariableNode(ref.getField(view), field.get().getType()));
    } else {
      throw new RuntimeException("Field not present on ref:" + ref);
    }
  }

  @Override
  public void caseStringConstant(StringConstant sc) {
    AllocationNode strConstant;
    if(pag.getSparkOptions().isStringConstants() || (sc.getValue().length()>0 && sc.getValue().charAt(0) == '[')){
      strConstant = pag.getOrCreateStringConstantNode(sc.getValue());
    }else{
      strConstant = pag.getOrCreateAllocationNode(PointsToAnalysis.STRING_NODE, rtStringType, null);
    }
    VariableNode strConstantLocal = pag.getOrCreateGlobalVariableNode(strConstant, rtStringType);
    pag.addEdge(strConstant, strConstantLocal);
    setResult(strConstantLocal);
  }

  @Override
  public void caseThisRef(JThisRef ref) {
    setResult(caseThis());
  }

  public void caseNullConstant(NullConstant nullConstant) {
    setResult(null);
  }

  @Override
  public void caseClassConstant(ClassConstant cc) {
    AllocationNode classConstant = pag.getOrCreateClassConstantNode(cc);
    VariableNode classConstantLocal = pag.getOrCreateGlobalVariableNode(classConstant, rtClass);
    pag.addEdge(classConstant, classConstantLocal);
    setResult(classConstantLocal);
  }

  @Override
  public void defaultCase(Object obj) {
    throw new RuntimeException("failed to handle " + obj);
  }

  @Override
  public void caseStaticInvokeExpr(JStaticInvokeExpr expr) {
    MethodSignature methodSignature = expr.getMethodSignature();
    if (expr.getArgCount() == 1
        && expr.getArg(0) instanceof StringConstant
        && methodSignature.getName().equals("forName")
        && methodSignature.getDeclClassType().getFullyQualifiedName().equals("java.lang.Class")
        && methodSignature.getParameterTypes().size() == 1) {
      // This is a call to Class.forName
      StringConstant classNameConst = (StringConstant) expr.getArg(0);
      String constStr = "L" + classNameConst.getValue().replace("\\.", "/") + ";";
      caseClassConstant(new ClassConstant(constStr, null)); // [KK] type not needed in old soot
    }
  }

  @Override
  public void caseVirtualInvokeExpr(JVirtualInvokeExpr expr) {
    if (isReflectionNewInstance(expr)) {
      NewInstanceNode newInstanceNode = pag.getOrCreateNewInstanceNode(expr, rtObject, method);
      expr.getBase().accept(this);
      Node srcNode = getNode();
      intraPag.addInternalEdge(srcNode, newInstanceNode);
      setResult(newInstanceNode);
    } else {
      throw new RuntimeException("Unhandled case of JVirtualInvokeExpr");
    }
  }
}
