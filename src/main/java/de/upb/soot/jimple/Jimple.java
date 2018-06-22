package de.upb.soot.jimple;
/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.jimple.common.expr.JAndExpr;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.expr.JCaughtExceptionRef;
import de.upb.soot.jimple.common.expr.JCmpExpr;
import de.upb.soot.jimple.common.expr.JCmpgExpr;
import de.upb.soot.jimple.common.expr.JCmplExpr;
import de.upb.soot.jimple.common.expr.JDivExpr;
import de.upb.soot.jimple.common.expr.JDynamicInvokeExpr;
import de.upb.soot.jimple.common.expr.JEqExpr;
import de.upb.soot.jimple.common.expr.JGeExpr;
import de.upb.soot.jimple.common.expr.JGtExpr;
import de.upb.soot.jimple.common.expr.JInstanceOfExpr;
import de.upb.soot.jimple.common.expr.JInterfaceInvokeExpr;
import de.upb.soot.jimple.common.expr.JLeExpr;
import de.upb.soot.jimple.common.expr.JLengthExpr;
import de.upb.soot.jimple.common.expr.JLtExpr;
import de.upb.soot.jimple.common.expr.JMulExpr;
import de.upb.soot.jimple.common.expr.JNeExpr;
import de.upb.soot.jimple.common.expr.JNegExpr;
import de.upb.soot.jimple.common.expr.JNewArrayExpr;
import de.upb.soot.jimple.common.expr.JNewExpr;
import de.upb.soot.jimple.common.expr.JOrExpr;
import de.upb.soot.jimple.common.expr.JRemExpr;
import de.upb.soot.jimple.common.expr.JShlExpr;
import de.upb.soot.jimple.common.expr.JShrExpr;
import de.upb.soot.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.soot.jimple.common.expr.JStaticInvokeExpr;
import de.upb.soot.jimple.common.expr.JSubExpr;
import de.upb.soot.jimple.common.expr.JUshrExpr;
import de.upb.soot.jimple.common.expr.JVirtualInvokeExpr;
import de.upb.soot.jimple.common.expr.JXorExpr;
import de.upb.soot.jimple.common.ref.CaughtExceptionRef;
import de.upb.soot.jimple.common.ref.JInstanceFieldRef;
import de.upb.soot.jimple.common.ref.ParameterRef;
import de.upb.soot.jimple.common.ref.SootFieldRef;
import de.upb.soot.jimple.common.ref.SootMethodRef;
import de.upb.soot.jimple.common.ref.StaticFieldRef;
import de.upb.soot.jimple.common.ref.ThisRef;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.JIfStmt;
import de.upb.soot.jimple.common.stmt.JInvokeStmt;
import de.upb.soot.jimple.common.stmt.JNopStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.common.stmt.JReturnVoidStmt;
import de.upb.soot.jimple.common.stmt.JThrowStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.javabyte.JBreakpointStmt;
import de.upb.soot.jimple.javabyte.JEnterMonitorStmt;
import de.upb.soot.jimple.javabyte.JExitMonitorStmt;
import de.upb.soot.jimple.javabyte.JLookupSwitchStmt;
import de.upb.soot.jimple.javabyte.JRetStmt;
import de.upb.soot.jimple.javabyte.JTableSwitchStmt;

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The Jimple class contains all the constructors for the components of the Jimple grammar for the Jimple body. <br>
 * <br>
 * 
 * Immediate -> Local | Constant <br>
 * RValue -> Local | Constant | ConcreteRef | Expr<br>
 * Variable -> Local | ArrayRef | InstanceFieldRef | StaticFieldRef <br>
 */

public class Jimple {
  public Jimple() {
  }

  public static Jimple v() {
    return null;
  }

  public final static String NEWARRAY = "newarray";
  public final static String NEWMULTIARRAY = "newmultiarray";
  public final static String NOP = "nop";
  public final static String RET = "ret";
  public final static String SPECIALINVOKE = "specialinvoke";
  public final static String DYNAMICINVOKE = "dynamicinvoke";
  public final static String STATICINVOKE = "staticinvoke";
  public final static String TABLESWITCH = "tableswitch";
  public final static String VIRTUALINVOKE = "virtualinvoke";
  public final static String NULL_TYPE = "null_type";
  public final static String UNKNOWN = "unknown";
  public final static String CMP = "cmp";
  public final static String CMPG = "cmpg";
  public final static String CMPL = "cmpl";
  public final static String ENTERMONITOR = "entermonitor";
  public final static String EXITMONITOR = "exitmonitor";
  public final static String INTERFACEINVOKE = "interfaceinvoke";
  public final static String LENGTHOF = "lengthof";
  public final static String LOOKUPSWITCH = "lookupswitch";
  public final static String NEG = "neg";
  public final static String IF = "if";
  public final static String ABSTRACT = "abstract";
  public final static String BOOLEAN = "boolean";
  public final static String BREAK = "break";
  public final static String BYTE = "byte";
  public final static String CASE = "case";
  public final static String CATCH = "catch";
  public final static String CHAR = "char";
  public final static String CLASS = "class";
  public final static String FINAL = "final";
  public final static String NATIVE = "native";
  public final static String PUBLIC = "public";
  public final static String PROTECTED = "protected";
  public final static String PRIVATE = "private";
  public final static String STATIC = "static";
  public final static String SYNCHRONIZED = "synchronized";
  public final static String TRANSIENT = "transient";
  public final static String VOLATILE = "volatile";
  public final static String STRICTFP = "strictfp";
  public final static String ENUM = "enum";
  public final static String ANNOTATION = "annotation";
  public final static String INTERFACE = "interface";
  public final static String VOID = "void";
  public final static String SHORT = "short";
  public final static String INT = "int";
  public final static String LONG = "long";
  public final static String FLOAT = "float";
  public final static String DOUBLE = "double";
  public final static String EXTENDS = "extends";
  public final static String IMPLEMENTS = "implements";
  public final static String BREAKPOINT = "breakpoint";
  public final static String DEFAULT = "default";
  public final static String GOTO = "goto";
  public final static String INSTANCEOF = "instanceof";
  public final static String NEW = "new";
  public final static String RETURN = "return";
  public final static String THROW = "throw";
  public final static String THROWS = "throws";
  public final static String NULL = "null";
  public final static String FROM = "from";
  public final static String TO = "to";
  public final static String WITH = "with";
  public final static String CLS = "cls";
  public final static String TRUE = "true";
  public final static String FALSE = "false";

  public static List<String> jimpleKeywordList() {
    List<String> l = new LinkedList<String>();
    Collections.addAll(l, NEWARRAY, NEWMULTIARRAY, NOP, RET, SPECIALINVOKE, STATICINVOKE, TABLESWITCH, VIRTUALINVOKE,
        NULL_TYPE, UNKNOWN, CMP, CMPG, CMPL, ENTERMONITOR, EXITMONITOR, INTERFACEINVOKE, LENGTHOF, LOOKUPSWITCH, NEG, IF,
        ABSTRACT, BOOLEAN, BREAK, BYTE, CASE, CATCH, CHAR, CLASS, FINAL, NATIVE, PUBLIC, PROTECTED, PRIVATE, STATIC,
        SYNCHRONIZED, TRANSIENT, VOLATILE, STRICTFP, ENUM, ANNOTATION, INTERFACE, VOID, SHORT, INT, LONG, FLOAT, DOUBLE,
        EXTENDS, IMPLEMENTS, BREAKPOINT, DEFAULT, GOTO, INSTANCEOF, NEW, RETURN, THROW, THROWS, NULL, FROM, TO, WITH, CLS,
        TRUE, FALSE);
    return l;
  }

  public static boolean isJavaKeywordType(Type t) {
    return false;
  }

  public static Value cloneIfNecessary(Value val) {
    return null;
  }

  /**
   * Constructs a XorExpr(Immediate, Immediate) grammar chunk.
   */
  public JXorExpr newXorExpr(Value op1, Value op2) {
    return new JXorExpr(op1, op2);
  }

  /**
   * Constructs a UshrExpr(Immediate, Immediate) grammar chunk.
   */
  public JUshrExpr newUshrExpr(Value op1, Value op2) {
    return new JUshrExpr(op1, op2);
  }

  /**
   * Constructs a SubExpr(Immediate, Immediate) grammar chunk.
   */
  public JSubExpr newSubExpr(Value op1, Value op2) {
    return new JSubExpr(op1, op2);
  }

  /**
   * Constructs a ShrExpr(Immediate, Immediate) grammar chunk.
   */
  public JShrExpr newShrExpr(Value op1, Value op2) {
    return new JShrExpr(op1, op2);
  }

  /**
   * Constructs a ShlExpr(Immediate, Immediate) grammar chunk.
   */
  public JShlExpr newShlExpr(Value op1, Value op2) {
    return new JShlExpr(op1, op2);
  }

  /**
   * Constructs a RemExpr(Immediate, Immediate) grammar chunk.
   */
  public JRemExpr newRemExpr(Value op1, Value op2) {
    return new JRemExpr(op1, op2);
  }

  /**
   * Constructs a OrExpr(Immediate, Immediate) grammar chunk.
   */
  public JOrExpr newOrExpr(Value op1, Value op2) {
    return new JOrExpr(op1, op2);
  }

  /**
   * Constructs a NeExpr(Immediate, Immediate) grammar chunk.
   */
  public JNeExpr newNeExpr(Value op1, Value op2) {
    return new JNeExpr(op1, op2);
  }

  /**
   * Constructs a MulExpr(Immediate, Immediate) grammar chunk.
   */
  public JMulExpr newMulExpr(Value op1, Value op2) {
    return new JMulExpr(op1, op2);
  }

  /**
   * Constructs a LeExpr(Immediate, Immediate) grammar chunk.
   */
  public JLeExpr newLeExpr(Value op1, Value op2) {
    return new JLeExpr(op1, op2);
  }

  /**
   * Constructs a GeExpr(Immediate, Immediate) grammar chunk.
   */
  public JGeExpr newGeExpr(Value op1, Value op2) {
    return new JGeExpr(op1, op2);
  }

  /**
   * Constructs a EqExpr(Immediate, Immediate) grammar chunk.
   */
  public JEqExpr newEqExpr(Value op1, Value op2) {
    return new JEqExpr(op1, op2);
  }

  /**
   * Constructs a DivExpr(Immediate, Immediate) grammar chunk.
   */
  public JDivExpr newDivExpr(Value op1, Value op2) {
    return new JDivExpr(op1, op2);
  }

  /**
   * Constructs a CmplExpr(Immediate, Immediate) grammar chunk.
   */
  public JCmplExpr newCmplExpr(Value op1, Value op2) {
    return new JCmplExpr(op1, op2);
  }

  /**
   * Constructs a CmpgExpr(Immediate, Immediate) grammar chunk.
   */
  public JCmpgExpr newCmpgExpr(Value op1, Value op2) {
    return new JCmpgExpr(op1, op2);
  }

  /**
   * Constructs a CmpExpr(Immediate, Immediate) grammar chunk.
   */
  public JCmpExpr newCmpExpr(Value op1, Value op2) {
    return new JCmpExpr(op1, op2);
  }

  /**
   * Constructs a GtExpr(Immediate, Immediate) grammar chunk.
   */
  public JGtExpr newGtExpr(Value op1, Value op2) {
    return new JGtExpr(op1, op2);
  }

  /**
   * Constructs a LtExpr(Immediate, Immediate) grammar chunk.
   */
  public JLtExpr newLtExpr(Value op1, Value op2) {
    return new JLtExpr(op1, op2);
  }

  /**
   * Constructs a AddExpr(Immediate, Immediate) grammar chunk.
   */
  public JAddExpr newAddExpr(Value op1, Value op2) {
    return new JAddExpr(op1, op2);
  }

  /**
   * Constructs a AndExpr(Immediate, Immediate) grammar chunk.
   */
  public JAndExpr newAndExpr(Value op1, Value op2) {
    return new JAndExpr(op1, op2);
  }

  /**
   * Constructs a NegExpr(Immediate, Immediate) grammar chunk.
   */
  public JNegExpr newNegExpr(Value op) {
    return new JNegExpr(op);
  }

  /**
   * Constructs a LengthExpr(Immediate) grammar chunk.
   */
  public JLengthExpr newLengthExpr(Value op) {
    return new JLengthExpr(op);
  }

  /**
   * Constructs a CastExpr(Immediate, Type) grammar chunk.
   */
  public JCastExpr newCastExpr(Value op1, Type t) {
    return new JCastExpr(op1, t);
  }

  /**
   * Constructs a InstanceOfExpr(Immediate, Type) grammar chunk.
   */
  public JInstanceOfExpr newInstanceOfExpr(Value op1, Type t) {
    return new JInstanceOfExpr(op1, t);
  }

  /**
   * Constructs a NewExpr(RefType) grammar chunk.
   */
  public JNewExpr newNewExpr(RefType type) {
    return new JNewExpr(type);
  }

  /**
   * Constructs a NewArrayExpr(Type, Immediate) grammar chunk.
   */
  public JNewArrayExpr newNewArrayExpr(Type type, Value size) {
    return new JNewArrayExpr(type, size);
  }

  /**
   * Constructs a NewStaticInvokeExpr(ArrayType, List of Immediate) grammar chunk.
   */
  public JStaticInvokeExpr newStaticInvokeExpr(SootMethodRef method, List<? extends Value> args) {
    return new JStaticInvokeExpr(method, args);
  }

  public JStaticInvokeExpr newStaticInvokeExpr(SootMethodRef method, Value... args) {
    return newStaticInvokeExpr(method, Arrays.asList(args));
  }

  public JStaticInvokeExpr newStaticInvokeExpr(SootMethodRef method, Value arg) {
    return newStaticInvokeExpr(method, Collections.singletonList(arg));
  }

  public JStaticInvokeExpr newStaticInvokeExpr(SootMethodRef method) {
    return newStaticInvokeExpr(method, Collections.<Value>emptyList());
  }

  /**
   * Constructs a NewSpecialInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar chunk.
   */
  public JSpecialInvokeExpr newSpecialInvokeExpr(Local base, SootMethodRef method, List<? extends Value> args) {
    return new JSpecialInvokeExpr(base, method, args);
  }

  /**
   * Constructs a NewSpecialInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar chunk.
   */
  public JSpecialInvokeExpr newSpecialInvokeExpr(Local base, SootMethodRef method, Value... args) {
    return newSpecialInvokeExpr(base, method, Arrays.asList(args));
  }

  public JSpecialInvokeExpr newSpecialInvokeExpr(Local base, SootMethodRef method, Value arg) {
    return newSpecialInvokeExpr(base, method, Collections.<Value>singletonList(arg));
  }

  public JSpecialInvokeExpr newSpecialInvokeExpr(Local base, SootMethodRef method) {
    return newSpecialInvokeExpr(base, method, Collections.<Value>emptyList());
  }

  /**
   * Constructs a NewDynamicInvokeExpr(SootMethodRef bootstrapMethodRef, List bootstrapArgs, SootMethodRef methodRef, List
   * args) grammar chunk.
   */
  public JDynamicInvokeExpr newDynamicInvokeExpr(SootMethodRef bootstrapMethodRef, List<? extends Value> bootstrapArgs,
      SootMethodRef methodRef, List<? extends Value> args) {
    return new JDynamicInvokeExpr(bootstrapMethodRef, bootstrapArgs, methodRef, args);
  }

  /**
   * Constructs a NewDynamicInvokeExpr(SootMethodRef bootstrapMethodRef, List bootstrapArgs, SootMethodRef methodRef, List
   * args) grammar chunk.
   */
  public JDynamicInvokeExpr newDynamicInvokeExpr(SootMethodRef bootstrapMethodRef, List<? extends Value> bootstrapArgs,
      SootMethodRef methodRef, int tag, List<? extends Value> args) {
    return new JDynamicInvokeExpr(bootstrapMethodRef, bootstrapArgs, methodRef, tag, args);
  }

  /**
   * Constructs a NewVirtualInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar chunk.
   */
  public JVirtualInvokeExpr newVirtualInvokeExpr(Local base, SootMethodRef method, List<? extends Value> args) {
    return new JVirtualInvokeExpr(base, method, args);
  }

  /**
   * Constructs a NewVirtualInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar chunk.
   */
  public JVirtualInvokeExpr newVirtualInvokeExpr(Local base, SootMethodRef method, Value... args) {
    return newVirtualInvokeExpr(base, method, Arrays.asList(args));
  }

  public JVirtualInvokeExpr newVirtualInvokeExpr(Local base, SootMethodRef method, Value arg) {
    return newVirtualInvokeExpr(base, method, Collections.<Value>singletonList(arg));
  }

  public JVirtualInvokeExpr newVirtualInvokeExpr(Local base, SootMethodRef method) {
    return newVirtualInvokeExpr(base, method, Collections.<Value>emptyList());
  }

  /**
   * Constructs a NewInterfaceInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar chunk.
   */
  public JInterfaceInvokeExpr newInterfaceInvokeExpr(Local base, SootMethodRef method, List<? extends Value> args) {
    return new JInterfaceInvokeExpr(base, method, args);
  }

  /**
   * Constructs a NewInterfaceInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar chunk.
   */
  public JInterfaceInvokeExpr newInterfaceInvokeExpr(Local base, SootMethodRef method, Value... args) {
    return newInterfaceInvokeExpr(base, method, Arrays.asList(args));
  }

  public JInterfaceInvokeExpr newInterfaceInvokeExpr(Local base, SootMethodRef method, Value arg) {
    return newInterfaceInvokeExpr(base, method, Collections.<Value>singletonList(arg));
  }

  public JInterfaceInvokeExpr newInterfaceInvokeExpr(Local base, SootMethodRef method) {
    return newInterfaceInvokeExpr(base, method, Collections.<Value>emptyList());
  }

  /**
   * Constructs a ThrowStmt(Immediate) grammar chunk.
   */
  public JThrowStmt newThrowStmt(Value op) {
    return new JThrowStmt(op);
  }

  /**
   * Constructs a ExitMonitorStmt(Immediate) grammar chunk
   */
  public JExitMonitorStmt newExitMonitorStmt(Value op) {
    return new JExitMonitorStmt(op);
  }

  /**
   * Constructs a EnterMonitorStmt(Immediate) grammar chunk.
   */
  public JEnterMonitorStmt newEnterMonitorStmt(Value op) {
    return new JEnterMonitorStmt(op);
  }

  /**
   * Constructs a BreakpointStmt() grammar chunk.
   */
  public JBreakpointStmt newBreakpointStmt() {
    return new JBreakpointStmt();
  }

  /**
   * Constructs a GotoStmt(Stmt) grammar chunk.
   */
  public JGotoStmt newGotoStmt(Stmt target) {
    return new JGotoStmt(target);
  }

  public JGotoStmt newGotoStmt(StmtBox stmtBox) {
    return new JGotoStmt(stmtBox);
  }

  /**
   * Constructs a NopStmt() grammar chunk.
   */
  public JNopStmt newNopStmt() {
    return new JNopStmt();
  }

  /**
   * Constructs a ReturnVoidStmt() grammar chunk.
   */
  public JReturnVoidStmt newReturnVoidStmt() {
    return new JReturnVoidStmt();
  }

  /**
   * Constructs a ReturnStmt(Immediate) grammar chunk.
   */
  public JReturnStmt newReturnStmt(Value op) {
    return new JReturnStmt(op);
  }

  /**
   * Constructs a RetStmt(Local) grammar chunk.
   */
  public JRetStmt newRetStmt(Value stmtAddress) {
    return new JRetStmt(stmtAddress);
  }

  /**
   * Constructs a IfStmt(Condition, Stmt) grammar chunk.
   */
  public JIfStmt newIfStmt(Value condition, Stmt target) {
    return new JIfStmt(condition, target);
  }

  /**
   * Constructs a IfStmt(Condition, UnitBox) grammar chunk.
   */
  public JIfStmt newIfStmt(Value condition, StmtBox target) {
    return new JIfStmt(condition, target);
  }

  /**
   * Constructs a IdentityStmt(Local, IdentityRef) grammar chunk.
   */
  public JIdentityStmt newIdentityStmt(Value local, Value identityRef) {
    return new JIdentityStmt(local, identityRef);
  }

  /**
   * Constructs a AssignStmt(Variable, RValue) grammar chunk.
   */
  public JAssignStmt newAssignStmt(Value variable, Value rvalue) {
    return new JAssignStmt(variable, rvalue);
  }

  /**
   * Constructs a InvokeStmt(InvokeExpr) grammar chunk.
   */
  public JInvokeStmt newInvokeStmt(Value op) {
    return new JInvokeStmt(op);
  }

  /**
   * Constructs a TableSwitchStmt(Immediate, int, int, List of Unit, Stmt) grammar chunk.
   */
  public JTableSwitchStmt newTableSwitchStmt(Value key, int lowIndex, int highIndex, List<? extends Stmt> targets,
      Stmt defaultTarget) {
    return new JTableSwitchStmt(key, lowIndex, highIndex, targets, defaultTarget);
  }

  public JTableSwitchStmt newTableSwitchStmt(Value key, int lowIndex, int highIndex, List<? extends StmtBox> targets,
      StmtBox defaultTarget) {
    return new JTableSwitchStmt(key, lowIndex, highIndex, targets, defaultTarget);
  }

  /**
   * Constructs a LookupSwitchStmt(Immediate, List of Immediate, List of Unit, Stmt) grammar chunk.
   */
  public JLookupSwitchStmt newLookupSwitchStmt(Value key, List<IntConstant> lookupValues, List<? extends Stmt> targets,
      Stmt defaultTarget) {
    return null;
  }

  public JLookupSwitchStmt newLookupSwitchStmt(Value key, List<IntConstant> lookupValues, List<? extends StmtBox> targets,
      StmtBox defaultTarget) {
    return null;
  }

  /**
   * Constructs a Local with the given name and type.
   */
  public Local newLocal(String name, Type t) {
    return new JimpleLocal(name, t);
  }

  /**
   * Constructs a StaticFieldRef(SootFieldRef) grammar chunk.
   */
  public StaticFieldRef newStaticFieldRef(SootFieldRef f) {
    return new StaticFieldRef(f);
  }

  /**
   * Constructs a ThisRef(RefType) grammar chunk.
   */
  public ThisRef newThisRef(RefType t) {
    return new ThisRef(t);
  }

  /**
   * Constructs a ParameterRef(SootMethod, int) grammar chunk.
   */
  public ParameterRef newParameterRef(Type paramType, int number) {
    return new ParameterRef(paramType, number);
  }

  /**
   * Constructs a InstanceFieldRef(Local, SootFieldRef) grammar chunk.
   */
  public JInstanceFieldRef newInstanceFieldRef(Value base, SootFieldRef f) {
    return new JInstanceFieldRef(base, f);
  }

  /**
   * Constructs a CaughtExceptionRef() grammar chunk.
   */
  public CaughtExceptionRef newCaughtExceptionRef() {
    return new JCaughtExceptionRef();
  }

  public ValueBox newArgBox(Value op1) {
    // TODO Auto-generated method stub
    return null;
  }

  public ValueBox newImmediateBox(Value value) {
    // TODO Auto-generated method stub
    return null;
  }

  public StmtBox newStmtBox(Stmt target) {
    // TODO Auto-generated method stub
    return null;
  }

  public ValueBox newLocalBox(Value local) {
    // TODO Auto-generated method stub
    return null;
  }

  public ValueBox newIdentityRefBox(Value identityValue) {
    // TODO Auto-generated method stub
    return null;
  }

  public ValueBox newConditionExprBox(Value condition) {
    // TODO Auto-generated method stub
    return null;
  }

  public ValueBox newInvokeExprBox(Value c) {
    // TODO Auto-generated method stub
    return null;
  }

}
