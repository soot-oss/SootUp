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

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.upb.soot.core.SootFieldRef;
import de.upb.soot.core.SootMethodRef;
import de.upb.soot.core.Unit;
import de.upb.soot.jimple.constant.IntConstant;
import de.upb.soot.jimple.expr.AddExpr;
import de.upb.soot.jimple.expr.AndExpr;
import de.upb.soot.jimple.expr.CastExpr;
import de.upb.soot.jimple.expr.CmpExpr;
import de.upb.soot.jimple.expr.CmpgExpr;
import de.upb.soot.jimple.expr.CmplExpr;
import de.upb.soot.jimple.expr.DivExpr;
import de.upb.soot.jimple.expr.DynamicInvokeExpr;
import de.upb.soot.jimple.expr.EqExpr;
import de.upb.soot.jimple.expr.GeExpr;
import de.upb.soot.jimple.expr.GtExpr;
import de.upb.soot.jimple.expr.InstanceOfExpr;
import de.upb.soot.jimple.expr.InterfaceInvokeExpr;
import de.upb.soot.jimple.expr.LeExpr;
import de.upb.soot.jimple.expr.LengthExpr;
import de.upb.soot.jimple.expr.LtExpr;
import de.upb.soot.jimple.expr.MulExpr;
import de.upb.soot.jimple.expr.NeExpr;
import de.upb.soot.jimple.expr.NegExpr;
import de.upb.soot.jimple.expr.NewArrayExpr;
import de.upb.soot.jimple.expr.NewExpr;
import de.upb.soot.jimple.expr.OrExpr;
import de.upb.soot.jimple.expr.RemExpr;
import de.upb.soot.jimple.expr.ShlExpr;
import de.upb.soot.jimple.expr.ShrExpr;
import de.upb.soot.jimple.expr.SpecialInvokeExpr;
import de.upb.soot.jimple.expr.StaticInvokeExpr;
import de.upb.soot.jimple.expr.SubExpr;
import de.upb.soot.jimple.expr.UshrExpr;
import de.upb.soot.jimple.expr.VirtualInvokeExpr;
import de.upb.soot.jimple.expr.XorExpr;
import de.upb.soot.jimple.internal.JAddExpr;
import de.upb.soot.jimple.internal.JAndExpr;
import de.upb.soot.jimple.internal.JAssignStmt;
import de.upb.soot.jimple.internal.JBreakpointStmt;
import de.upb.soot.jimple.internal.JCastExpr;
import de.upb.soot.jimple.internal.JCaughtExceptionRef;
import de.upb.soot.jimple.internal.JCmpExpr;
import de.upb.soot.jimple.internal.JCmpgExpr;
import de.upb.soot.jimple.internal.JCmplExpr;
import de.upb.soot.jimple.internal.JDivExpr;
import de.upb.soot.jimple.internal.JDynamicInvokeExpr;
import de.upb.soot.jimple.internal.JEnterMonitorStmt;
import de.upb.soot.jimple.internal.JEqExpr;
import de.upb.soot.jimple.internal.JExitMonitorStmt;
import de.upb.soot.jimple.internal.JGeExpr;
import de.upb.soot.jimple.internal.JGotoStmt;
import de.upb.soot.jimple.internal.JGtExpr;
import de.upb.soot.jimple.internal.JIdentityStmt;
import de.upb.soot.jimple.internal.JIfStmt;
import de.upb.soot.jimple.internal.JInstanceFieldRef;
import de.upb.soot.jimple.internal.JInstanceOfExpr;
import de.upb.soot.jimple.internal.JInterfaceInvokeExpr;
import de.upb.soot.jimple.internal.JInvokeStmt;
import de.upb.soot.jimple.internal.JLeExpr;
import de.upb.soot.jimple.internal.JLengthExpr;
import de.upb.soot.jimple.internal.JLtExpr;
import de.upb.soot.jimple.internal.JMulExpr;
import de.upb.soot.jimple.internal.JNeExpr;
import de.upb.soot.jimple.internal.JNegExpr;
import de.upb.soot.jimple.internal.JNewArrayExpr;
import de.upb.soot.jimple.internal.JNewExpr;
import de.upb.soot.jimple.internal.JNopStmt;
import de.upb.soot.jimple.internal.JOrExpr;
import de.upb.soot.jimple.internal.JRemExpr;
import de.upb.soot.jimple.internal.JRetStmt;
import de.upb.soot.jimple.internal.JReturnStmt;
import de.upb.soot.jimple.internal.JReturnVoidStmt;
import de.upb.soot.jimple.internal.JShlExpr;
import de.upb.soot.jimple.internal.JShrExpr;
import de.upb.soot.jimple.internal.JSpecialInvokeExpr;
import de.upb.soot.jimple.internal.JStaticInvokeExpr;
import de.upb.soot.jimple.internal.JSubExpr;
import de.upb.soot.jimple.internal.JTableSwitchStmt;
import de.upb.soot.jimple.internal.JThrowStmt;
import de.upb.soot.jimple.internal.JUshrExpr;
import de.upb.soot.jimple.internal.JVirtualInvokeExpr;
import de.upb.soot.jimple.internal.JXorExpr;
import de.upb.soot.jimple.internal.JimpleLocal;
import de.upb.soot.jimple.stmt.AssignStmt;
import de.upb.soot.jimple.stmt.BreakpointStmt;
import de.upb.soot.jimple.stmt.EnterMonitorStmt;
import de.upb.soot.jimple.stmt.ExitMonitorStmt;
import de.upb.soot.jimple.stmt.GotoStmt;
import de.upb.soot.jimple.stmt.IdentityStmt;
import de.upb.soot.jimple.stmt.IfStmt;
import de.upb.soot.jimple.stmt.InvokeStmt;
import de.upb.soot.jimple.stmt.LookupSwitchStmt;
import de.upb.soot.jimple.stmt.NopStmt;
import de.upb.soot.jimple.stmt.RetStmt;
import de.upb.soot.jimple.stmt.ReturnStmt;
import de.upb.soot.jimple.stmt.ReturnVoidStmt;
import de.upb.soot.jimple.stmt.TableSwitchStmt;
import de.upb.soot.jimple.stmt.ThrowStmt;
import de.upb.soot.jimple.type.RefType;
import de.upb.soot.jimple.type.Type;
/**
 * The Jimple class contains all the constructors for the components of the Jimple grammar for the
 * Jimple body. <br>
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
    Collections.addAll(l, NEWARRAY, NEWMULTIARRAY, NOP, RET, SPECIALINVOKE, STATICINVOKE,
        TABLESWITCH, VIRTUALINVOKE, NULL_TYPE, UNKNOWN, CMP, CMPG, CMPL, ENTERMONITOR, EXITMONITOR,
        INTERFACEINVOKE, LENGTHOF, LOOKUPSWITCH, NEG, IF, ABSTRACT, BOOLEAN, BREAK, BYTE, CASE,
        CATCH, CHAR, CLASS, FINAL, NATIVE, PUBLIC, PROTECTED, PRIVATE, STATIC, SYNCHRONIZED,
        TRANSIENT, VOLATILE, STRICTFP, ENUM, ANNOTATION, INTERFACE, VOID, SHORT, INT, LONG, FLOAT,
        DOUBLE, EXTENDS, IMPLEMENTS, BREAKPOINT, DEFAULT, GOTO, INSTANCEOF, NEW, RETURN, THROW,
        THROWS, NULL, FROM, TO, WITH, CLS, TRUE, FALSE);
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
  public XorExpr newXorExpr(Value op1, Value op2) {
    return new JXorExpr(op1, op2);
  }

  /**
   * Constructs a UshrExpr(Immediate, Immediate) grammar chunk.
   */
  public UshrExpr newUshrExpr(Value op1, Value op2) {
    return new JUshrExpr(op1, op2);
  }

  /**
   * Constructs a SubExpr(Immediate, Immediate) grammar chunk.
   */
  public SubExpr newSubExpr(Value op1, Value op2) {
    return new JSubExpr(op1, op2);
  }

  /**
   * Constructs a ShrExpr(Immediate, Immediate) grammar chunk.
   */
  public ShrExpr newShrExpr(Value op1, Value op2) {
    return new JShrExpr(op1, op2);
  }

  /**
   * Constructs a ShlExpr(Immediate, Immediate) grammar chunk.
   */
  public ShlExpr newShlExpr(Value op1, Value op2) {
    return new JShlExpr(op1, op2);
  }

  /**
   * Constructs a RemExpr(Immediate, Immediate) grammar chunk.
   */
  public RemExpr newRemExpr(Value op1, Value op2) {
    return new JRemExpr(op1, op2);
  }

  /**
   * Constructs a OrExpr(Immediate, Immediate) grammar chunk.
   */
  public OrExpr newOrExpr(Value op1, Value op2) {
    return new JOrExpr(op1, op2);
  }

  /**
   * Constructs a NeExpr(Immediate, Immediate) grammar chunk.
   */
  public NeExpr newNeExpr(Value op1, Value op2) {
    return new JNeExpr(op1, op2);
  }

  /**
   * Constructs a MulExpr(Immediate, Immediate) grammar chunk.
   */
  public MulExpr newMulExpr(Value op1, Value op2) {
    return new JMulExpr(op1, op2);
  }

  /**
   * Constructs a LeExpr(Immediate, Immediate) grammar chunk.
   */
  public LeExpr newLeExpr(Value op1, Value op2) {
    return new JLeExpr(op1, op2);
  }

  /**
   * Constructs a GeExpr(Immediate, Immediate) grammar chunk.
   */
  public GeExpr newGeExpr(Value op1, Value op2) {
    return new JGeExpr(op1, op2);
  }

  /**
   * Constructs a EqExpr(Immediate, Immediate) grammar chunk.
   */
  public EqExpr newEqExpr(Value op1, Value op2) {
    return new JEqExpr(op1, op2);
  }

  /**
   * Constructs a DivExpr(Immediate, Immediate) grammar chunk.
   */
  public DivExpr newDivExpr(Value op1, Value op2) {
    return new JDivExpr(op1, op2);
  }

  /**
   * Constructs a CmplExpr(Immediate, Immediate) grammar chunk.
   */
  public CmplExpr newCmplExpr(Value op1, Value op2) {
    return new JCmplExpr(op1, op2);
  }

  /**
   * Constructs a CmpgExpr(Immediate, Immediate) grammar chunk.
   */
  public CmpgExpr newCmpgExpr(Value op1, Value op2) {
    return new JCmpgExpr(op1, op2);
  }

  /**
   * Constructs a CmpExpr(Immediate, Immediate) grammar chunk.
   */
  public CmpExpr newCmpExpr(Value op1, Value op2) {
    return new JCmpExpr(op1, op2);
  }

  /**
   * Constructs a GtExpr(Immediate, Immediate) grammar chunk.
   */
  public GtExpr newGtExpr(Value op1, Value op2) {
    return new JGtExpr(op1, op2);
  }

  /**
   * Constructs a LtExpr(Immediate, Immediate) grammar chunk.
   */
  public LtExpr newLtExpr(Value op1, Value op2) {
    return new JLtExpr(op1, op2);
  }

  /**
   * Constructs a AddExpr(Immediate, Immediate) grammar chunk.
   */
  public AddExpr newAddExpr(Value op1, Value op2) {
    return new JAddExpr(op1, op2);
  }

  /**
   * Constructs a AndExpr(Immediate, Immediate) grammar chunk.
   */
  public AndExpr newAndExpr(Value op1, Value op2) {
    return new JAndExpr(op1, op2);
  }

  /**
   * Constructs a NegExpr(Immediate, Immediate) grammar chunk.
   */
  public NegExpr newNegExpr(Value op) {
    return new JNegExpr(op);
  }

  /**
   * Constructs a LengthExpr(Immediate) grammar chunk.
   */
  public LengthExpr newLengthExpr(Value op) {
    return new JLengthExpr(op);
  }

  /**
   * Constructs a CastExpr(Immediate, Type) grammar chunk.
   */
  public CastExpr newCastExpr(Value op1, Type t) {
    return new JCastExpr(op1, t);
  }

  /**
   * Constructs a InstanceOfExpr(Immediate, Type) grammar chunk.
   */
  public InstanceOfExpr newInstanceOfExpr(Value op1, Type t) {
    return new JInstanceOfExpr(op1, t);
  }

  /**
   * Constructs a NewExpr(RefType) grammar chunk.
   */
  public NewExpr newNewExpr(RefType type) {
    return new JNewExpr(type);
  }

  /**
   * Constructs a NewArrayExpr(Type, Immediate) grammar chunk.
   */
  public NewArrayExpr newNewArrayExpr(Type type, Value size) {
    return new JNewArrayExpr(type, size);
  }

  /**
   * Constructs a NewStaticInvokeExpr(ArrayType, List of Immediate) grammar chunk.
   */
  public StaticInvokeExpr newStaticInvokeExpr(SootMethodRef method, List<? extends Value> args) {
    return new JStaticInvokeExpr(method, args);
  }

  public StaticInvokeExpr newStaticInvokeExpr(SootMethodRef method, Value... args) {
    return newStaticInvokeExpr(method, Arrays.asList(args));
  }

  public StaticInvokeExpr newStaticInvokeExpr(SootMethodRef method, Value arg) {
    return newStaticInvokeExpr(method, Collections.singletonList(arg));
  }

  public StaticInvokeExpr newStaticInvokeExpr(SootMethodRef method) {
    return newStaticInvokeExpr(method, Collections.<Value>emptyList());
  }

  /**
   * Constructs a NewSpecialInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar
   * chunk.
   */
  public SpecialInvokeExpr newSpecialInvokeExpr(Local base, SootMethodRef method,
      List<? extends Value> args) {
    return new JSpecialInvokeExpr(base, method, args);
  }

  /**
   * Constructs a NewSpecialInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar
   * chunk.
   */
  public SpecialInvokeExpr newSpecialInvokeExpr(Local base, SootMethodRef method, Value... args) {
    return newSpecialInvokeExpr(base, method, Arrays.asList(args));
  }

  public SpecialInvokeExpr newSpecialInvokeExpr(Local base, SootMethodRef method, Value arg) {
    return newSpecialInvokeExpr(base, method, Collections.<Value>singletonList(arg));
  }

  public SpecialInvokeExpr newSpecialInvokeExpr(Local base, SootMethodRef method) {
    return newSpecialInvokeExpr(base, method, Collections.<Value>emptyList());
  }

  /**
   * Constructs a NewDynamicInvokeExpr(SootMethodRef bootstrapMethodRef, List bootstrapArgs,
   * SootMethodRef methodRef, List args) grammar chunk.
   */
  public DynamicInvokeExpr newDynamicInvokeExpr(SootMethodRef bootstrapMethodRef,
      List<? extends Value> bootstrapArgs, SootMethodRef methodRef, List<? extends Value> args) {
    return new JDynamicInvokeExpr(bootstrapMethodRef, bootstrapArgs, methodRef, args);
  }

  /**
   * Constructs a NewDynamicInvokeExpr(SootMethodRef bootstrapMethodRef, List bootstrapArgs,
   * SootMethodRef methodRef, List args) grammar chunk.
   */
  public DynamicInvokeExpr newDynamicInvokeExpr(SootMethodRef bootstrapMethodRef,
      List<? extends Value> bootstrapArgs, SootMethodRef methodRef, int tag,
      List<? extends Value> args) {
    return new JDynamicInvokeExpr(bootstrapMethodRef, bootstrapArgs, methodRef, tag, args);
  }

  /**
   * Constructs a NewVirtualInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar
   * chunk.
   */
  public VirtualInvokeExpr newVirtualInvokeExpr(Local base, SootMethodRef method,
      List<? extends Value> args) {
    return new JVirtualInvokeExpr(base, method, args);
  }

  /**
   * Constructs a NewVirtualInvokeExpr(Local base, SootMethodRef method, List of Immediate) grammar
   * chunk.
   */
  public VirtualInvokeExpr newVirtualInvokeExpr(Local base, SootMethodRef method, Value... args) {
    return newVirtualInvokeExpr(base, method, Arrays.asList(args));
  }

  public VirtualInvokeExpr newVirtualInvokeExpr(Local base, SootMethodRef method, Value arg) {
    return newVirtualInvokeExpr(base, method, Collections.<Value>singletonList(arg));
  }

  public VirtualInvokeExpr newVirtualInvokeExpr(Local base, SootMethodRef method) {
    return newVirtualInvokeExpr(base, method, Collections.<Value>emptyList());
  }

  /**
   * Constructs a NewInterfaceInvokeExpr(Local base, SootMethodRef method, List of Immediate)
   * grammar chunk.
   */
  public InterfaceInvokeExpr newInterfaceInvokeExpr(Local base, SootMethodRef method,
      List<? extends Value> args) {
    return new JInterfaceInvokeExpr(base, method, args);
  }

  /**
   * Constructs a NewInterfaceInvokeExpr(Local base, SootMethodRef method, List of Immediate)
   * grammar chunk.
   */
  public InterfaceInvokeExpr newInterfaceInvokeExpr(Local base, SootMethodRef method,
      Value... args) {
    return newInterfaceInvokeExpr(base, method, Arrays.asList(args));
  }

  public InterfaceInvokeExpr newInterfaceInvokeExpr(Local base, SootMethodRef method, Value arg) {
    return newInterfaceInvokeExpr(base, method, Collections.<Value>singletonList(arg));
  }

  public InterfaceInvokeExpr newInterfaceInvokeExpr(Local base, SootMethodRef method) {
    return newInterfaceInvokeExpr(base, method, Collections.<Value>emptyList());
  }

  /**
   * Constructs a ThrowStmt(Immediate) grammar chunk.
   */
  public ThrowStmt newThrowStmt(Value op) {
    return new JThrowStmt(op);
  }

  /**
   * Constructs a ExitMonitorStmt(Immediate) grammar chunk
   */
  public ExitMonitorStmt newExitMonitorStmt(Value op) {
    return new JExitMonitorStmt(op);
  }

  /**
   * Constructs a EnterMonitorStmt(Immediate) grammar chunk.
   */
  public EnterMonitorStmt newEnterMonitorStmt(Value op) {
    return new JEnterMonitorStmt(op);
  }

  /**
   * Constructs a BreakpointStmt() grammar chunk.
   */
  public BreakpointStmt newBreakpointStmt() {
    return new JBreakpointStmt();
  }

  /**
   * Constructs a GotoStmt(Stmt) grammar chunk.
   */
  public GotoStmt newGotoStmt(Unit target) {
    return new JGotoStmt(target);
  }

  public GotoStmt newGotoStmt(UnitBox stmtBox) {
    return new JGotoStmt(stmtBox);
  }

  /**
   * Constructs a NopStmt() grammar chunk.
   */
  public NopStmt newNopStmt() {
    return new JNopStmt();
  }

  /**
   * Constructs a ReturnVoidStmt() grammar chunk.
   */
  public ReturnVoidStmt newReturnVoidStmt() {
    return new JReturnVoidStmt();
  }

  /**
   * Constructs a ReturnStmt(Immediate) grammar chunk.
   */
  public ReturnStmt newReturnStmt(Value op) {
    return new JReturnStmt(op);
  }

  /**
   * Constructs a RetStmt(Local) grammar chunk.
   */
  public RetStmt newRetStmt(Value stmtAddress) {
    return new JRetStmt(stmtAddress);
  }

  /**
   * Constructs a IfStmt(Condition, Stmt) grammar chunk.
   */
  public IfStmt newIfStmt(Value condition, Unit target) {
    return new JIfStmt(condition, target);
  }

  /**
   * Constructs a IfStmt(Condition, UnitBox) grammar chunk.
   */
  public IfStmt newIfStmt(Value condition, UnitBox target) {
    return new JIfStmt(condition, target);
  }

  /**
   * Constructs a IdentityStmt(Local, IdentityRef) grammar chunk.
   */
  public IdentityStmt newIdentityStmt(Value local, Value identityRef) {
    return new JIdentityStmt(local, identityRef);
  }

  /**
   * Constructs a AssignStmt(Variable, RValue) grammar chunk.
   */
  public AssignStmt newAssignStmt(Value variable, Value rvalue) {
    return new JAssignStmt(variable, rvalue);
  }

  /**
   * Constructs a InvokeStmt(InvokeExpr) grammar chunk.
   */
  public InvokeStmt newInvokeStmt(Value op) {
    return new JInvokeStmt(op);
  }

  /**
   * Constructs a TableSwitchStmt(Immediate, int, int, List of Unit, Stmt) grammar chunk.
   */
  public TableSwitchStmt newTableSwitchStmt(Value key, int lowIndex, int highIndex,
      List<? extends Unit> targets, Unit defaultTarget) {
    return new JTableSwitchStmt(key, lowIndex, highIndex, targets, defaultTarget);
  }

  public TableSwitchStmt newTableSwitchStmt(Value key, int lowIndex, int highIndex,
      List<? extends UnitBox> targets, UnitBox defaultTarget) {
    return new JTableSwitchStmt(key, lowIndex, highIndex, targets, defaultTarget);
  }

  /**
   * Constructs a LookupSwitchStmt(Immediate, List of Immediate, List of Unit, Stmt) grammar chunk.
   */
  public LookupSwitchStmt newLookupSwitchStmt(Value key, List<IntConstant> lookupValues,
      List<? extends Unit> targets, Unit defaultTarget) {
    return null;
  }

  public LookupSwitchStmt newLookupSwitchStmt(Value key, List<IntConstant> lookupValues,
      List<? extends UnitBox> targets, UnitBox defaultTarget) {
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
  public InstanceFieldRef newInstanceFieldRef(Value base, SootFieldRef f) {
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

  public UnitBox newStmtBox(Unit target) {
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
