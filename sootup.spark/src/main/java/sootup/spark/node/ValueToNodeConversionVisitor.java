package sootup.spark.node;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2025 Ondrej Lhotak, Kadiray Karakaya and others
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

import java.util.Collections;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.EnumConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.constant.MethodType;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JAndExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JCmpExpr;
import sootup.core.jimple.common.expr.JCmpgExpr;
import sootup.core.jimple.common.expr.JCmplExpr;
import sootup.core.jimple.common.expr.JDivExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JGeExpr;
import sootup.core.jimple.common.expr.JGtExpr;
import sootup.core.jimple.common.expr.JInstanceOfExpr;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.expr.JLengthExpr;
import sootup.core.jimple.common.expr.JLtExpr;
import sootup.core.jimple.common.expr.JMulExpr;
import sootup.core.jimple.common.expr.JNeExpr;
import sootup.core.jimple.common.expr.JNegExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.expr.JOrExpr;
import sootup.core.jimple.common.expr.JPhiExpr;
import sootup.core.jimple.common.expr.JRemExpr;
import sootup.core.jimple.common.expr.JShlExpr;
import sootup.core.jimple.common.expr.JShrExpr;
import sootup.core.jimple.common.expr.JSubExpr;
import sootup.core.jimple.common.expr.JUshrExpr;
import sootup.core.jimple.common.expr.JXorExpr;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.visitor.AbstractValueVisitor;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.java.core.types.JavaClassType;
import sootup.spark.Engine;
import sootup.spark.SparkOptions;

/**
 * {@link Value} to {@link Node} converter. Supported nodes according to the Spark thesis:
 *
 * <ul>
 *   <li>AllocationNode
 *   <li>VariableNode
 *   <li>FieldRefNode
 * </ul>
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValueToNodeConversionVisitor extends AbstractValueVisitor {

  private static final MethodSignature GLOBAL_SCOPE =
      new MethodSignature(
          new JavaClassType("GLOBAL", new PackageName("sootup.global")),
          "GLOBAL_SCOPE",
          Collections.emptyList(),
          VoidType.getInstance());

  final MethodSignature containingMethodSig;
  final SparkOptions sparkOptions;
  Node node;

  public ValueToNodeConversionVisitor(
      MethodSignature containingMethodSig, SparkOptions sparkOptions) {
    this.containingMethodSig = containingMethodSig;
    this.sparkOptions = sparkOptions;
  }

  /**
   * returns a node as a result of the value to node conversion
   *
   * @return an optional PAG node
   */
  public Optional<Node> getResult() {
    return Optional.ofNullable(node);
  }

  @Override
  public void caseCastExpr(@NonNull JCastExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseNewArrayExpr(@NonNull JNewArrayExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseBooleanConstant(@NonNull BooleanConstant constant) {
    ignore(constant);
  }

  @Override
  public void caseClassConstant(@NonNull ClassConstant constant) {
    ignore(constant);
  }

  @Override
  public void caseDoubleConstant(@NonNull DoubleConstant constant) {
    ignore(constant);
  }

  @Override
  public void caseEnumConstant(@NonNull EnumConstant constant) {
    ignore(constant);
  }

  @Override
  public void caseFloatConstant(@NonNull FloatConstant constant) {
    ignore(constant);
  }

  @Override
  public void caseIntConstant(@NonNull IntConstant constant) {
    ignore(constant);
  }

  @Override
  public void caseLongConstant(@NonNull LongConstant constant) {
    ignore(constant);
  }

  @Override
  public void caseMethodHandle(@NonNull MethodHandle v) {
    ignore(v);
  }

  @Override
  public void caseMethodType(@NonNull MethodType v) {
    ignore(v);
  }

  @Override
  public void caseNullConstant(@NonNull NullConstant constant) {
    ignore(constant);
  }

  @Override
  public void caseAddExpr(@NonNull JAddExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseAndExpr(@NonNull JAndExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseCmpExpr(@NonNull JCmpExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseCmpgExpr(@NonNull JCmpgExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseCmplExpr(@NonNull JCmplExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseDivExpr(@NonNull JDivExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseEqExpr(@NonNull JEqExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseGeExpr(@NonNull JGeExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseGtExpr(@NonNull JGtExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseInstanceOfExpr(@NonNull JInstanceOfExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseLeExpr(@NonNull JLeExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseLengthExpr(@NonNull JLengthExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseLtExpr(@NonNull JLtExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseMulExpr(@NonNull JMulExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseNeExpr(@NonNull JNeExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseNegExpr(@NonNull JNegExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseOrExpr(@NonNull JOrExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseRemExpr(@NonNull JRemExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseShlExpr(@NonNull JShlExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseShrExpr(@NonNull JShrExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseSubExpr(@NonNull JSubExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseUshrExpr(@NonNull JUshrExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseXorExpr(@NonNull JXorExpr expr) {
    ignore(expr);
  }

  @Override
  public void casePhiExpr(JPhiExpr expr) {
    ignore(expr);
  }

  @Override
  public void caseCaughtExceptionRef(@NonNull JCaughtExceptionRef ref) {
    ignore(ref);
  }

  @Override
  public void caseNewMultiArrayExpr(@NonNull JNewMultiArrayExpr expr) {
    // TODO: wip
    defaultCaseValue(expr);
  }

  @Override
  public void caseNewExpr(@NonNull JNewExpr expr) {
    AllocationNode.AllocationNodeBuilder<?, ?> builder =
        AllocationNode.builder().type(expr.getType()).containingMethodSig(containingMethodSig);
    if (!sparkOptions.isTypesForSites()) {
      builder.allocationSite(Engine.incrementAndGetAllocCount());
    }
    this.node = builder.build();
  }

  @Override
  public void caseStaticFieldRef(@NonNull JStaticFieldRef ref) {
    this.node =
        StaticFieldRefNode.builder()
            .field(ref.getFieldSignature())
            .type(ref.getFieldSignature().getDeclClassType())
            .containingMethodSig(containingMethodSig)
            .build();
  }

  @Override
  public void caseInstanceFieldRef(@NonNull JInstanceFieldRef ref) {
    val fieldBasedScope = sparkOptions.isIgnoreBaseObjects();
    val baseMethodSig = fieldBasedScope ? GLOBAL_SCOPE : containingMethodSig;
    val baseName = fieldBasedScope ? ref.getBase().getType().toString() : ref.getBase().getName();
    val base =
        VariableNode.builder()
            .name(baseName)
            .type(ref.getBase().getType())
            .containingMethodSig(baseMethodSig)
            .build();
    this.node =
        InstanceFieldRefNode.builder()
            .base(base)
            .field(ref.getFieldSignature())
            .type(ref.getType())
            .containingMethodSig(baseMethodSig)
            .build();
  }

  @Override
  public void caseArrayRef(@NonNull JArrayRef ref) {
    val type = ref.getBase().getType();
    if (type instanceof ArrayType arrayType) {
      val baseType = arrayType.getBaseType();
      if (baseType instanceof ClassType declaringClassType) {
        val field =
            new FieldSignature(declaringClassType, String.valueOf(ref.getIndex()), ref.getType());
        val fieldBasedScope = sparkOptions.isIgnoreBaseObjects();
        val baseMethodSig = fieldBasedScope ? GLOBAL_SCOPE : containingMethodSig;
        val baseName =
            fieldBasedScope ? ref.getBase().getType().toString() : ref.getBase().getName();
        val base =
            VariableNode.builder()
                .name(baseName)
                .type(ref.getBase().getType())
                .containingMethodSig(baseMethodSig)
                .build();
        this.node =
            InstanceFieldRefNode.builder()
                .base(base)
                .field(field)
                .type(ref.getType())
                .containingMethodSig(baseMethodSig)
                .build();
      }
    }
  }

  @Override
  public void caseParameterRef(@NonNull JParameterRef ref) {
    this.node =
        VariableNode.builder()
            .name("@parameter" + ref.getIndex())
            .type(ref.getType())
            .containingMethodSig(containingMethodSig)
            .build();
  }

  @Override
  public void caseThisRef(@NonNull JThisRef ref) {
    this.node =
        VariableNode.builder()
            .name("@this")
            .type(ref.getType())
            .containingMethodSig(containingMethodSig)
            .build();
  }

  @Override
  public void caseLocal(@NonNull Local local) {
    this.node =
        VariableNode.builder()
            .type(local.getType())
            .name(local.getName())
            .containingMethodSig(containingMethodSig)
            .build();
  }

  @Override
  public void caseStringConstant(@NonNull StringConstant constant) {
    AllocationNode.AllocationNodeBuilder<?, ?> builder =
        AllocationNode.builder().type(constant.getType()).containingMethodSig(containingMethodSig);
    if (!sparkOptions.isTypesForSites()) {
      builder.allocationSite(Engine.incrementAndGetAllocCount());
    }
    this.node = builder.build();
  }

  @Override
  public void defaultCaseValue(@NonNull Value v) {
    log.warn("Unimplemented node conversion for value: {} of type: {}", v, v.getClass());
  }

  // For the cases that should safely be ignored for PTA
  public void ignore(@NonNull Value v) {}
}
