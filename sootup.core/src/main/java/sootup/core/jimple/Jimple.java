package sootup.core.jimple;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Markus Schmidt, Christian Brüggemann and others
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
import sootup.core.IdentifierFactory;
import sootup.core.graph.BasicBlock;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.util.StringTools;

/**
 * The Jimple class contains all the constructors for the components of the Jimple grammar for the
 * Jimple body. <br>
 * <br>
 *
 * <p>Immediate -&gt; Local | Constant <br>
 * RValue -&gt; Local | Constant | ConcreteRef | Expr<br>
 * Variable -&gt; Local | ArrayRef | InstanceFieldRef | StaticFieldRef <br>
 */
public abstract class Jimple {
  public static final String NEWARRAY = "newarray";
  public static final String NEWMULTIARRAY = "newmultiarray";
  public static final String NOP = "nop";
  public static final String RET = "ret";
  public static final String SPECIALINVOKE = "specialinvoke";
  public static final String DYNAMICINVOKE = "dynamicinvoke";
  public static final String STATICINVOKE = "staticinvoke";
  public static final String VIRTUALINVOKE = "virtualinvoke";
  public static final String CMP = "cmp";
  public static final String CMPG = "cmpg";
  public static final String CMPL = "cmpl";
  public static final String ENTERMONITOR = "entermonitor";
  public static final String EXITMONITOR = "exitmonitor";
  public static final String INTERFACEINVOKE = "interfaceinvoke";
  public static final String LENGTHOF = "lengthof";
  public static final String NEG = "neg";
  public static final String IF = "if";
  public static final String ABSTRACT = "abstract";
  public static final String CASE = "case";
  public static final String CATCH = "catch";
  public static final String CLASS = "class";
  public static final String FINAL = "final";
  public static final String NATIVE = "native";
  public static final String PUBLIC = "public";
  public static final String PROTECTED = "protected";
  public static final String PRIVATE = "private";
  public static final String STATIC = "static";
  public static final String SYNCHRONIZED = "synchronized";
  public static final String TRANSIENT = "transient";
  public static final String VOLATILE = "volatile";
  public static final String STRICTFP = "strictfp";
  public static final String ENUM = "enum";
  public static final String ANNOTATION = "annotation";
  public static final String INTERFACE = "interface";
  public static final String EXTENDS = "extends";
  public static final String IMPLEMENTS = "implements";
  public static final String BREAKPOINT = "breakpoint";
  public static final String DEFAULT = "default";
  public static final String GOTO = "goto";
  public static final String INSTANCEOF = "instanceof";
  public static final String NEW = "new";
  public static final String RETURN = "return";
  public static final String SWITCH = "switch";
  public static final String THROW = "throw";
  public static final String THROWS = "throws";
  public static final String NULL = "null";
  public static final String FROM = "from";
  public static final String TO = "to";
  public static final String WITH = "with";
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  public static final String PHI = "phi";

  /**
   * Returns a list of keywords for Jimple. This list has to be in sync with the tokens for the
   * jimple parser. This way StmtPrinter can escape reserved words while serializing if needed.
   */
  public static List<String> jimpleKeywordList() {
    List<String> l =
        Arrays.asList(
            NEWARRAY,
            NEWMULTIARRAY,
            NOP,
            RET,
            SPECIALINVOKE,
            STATICINVOKE,
            SWITCH,
            VIRTUALINVOKE,
            CMP,
            CMPG,
            CMPL,
            ENTERMONITOR,
            EXITMONITOR,
            INTERFACEINVOKE,
            LENGTHOF,
            NEG,
            IF,
            ABSTRACT,
            CASE,
            CATCH,
            CLASS,
            FINAL,
            NATIVE,
            PUBLIC,
            PROTECTED,
            PRIVATE,
            STATIC,
            SYNCHRONIZED,
            TRANSIENT,
            VOLATILE,
            STRICTFP,
            ENUM,
            ANNOTATION,
            INTERFACE,
            EXTENDS,
            IMPLEMENTS,
            BREAKPOINT,
            DEFAULT,
            GOTO,
            INSTANCEOF,
            NEW,
            RETURN,
            THROW,
            THROWS,
            NULL,
            FROM,
            TO,
            WITH,
            TRUE,
            FALSE,
            PHI);
    return l;
  }

  /** Escapes reserved Jimple keywords e.g. used in (Stmt)Printer, necessary in the JimpleParser */
  public static String escape(String str) {
    if (str.length() == 0) {
      return "\"\"";
    }
    return StringTools.getQuotedStringOf(str, jimpleKeywordList().contains(str));
  }

  public static String unescape(String str) {
    StringBuilder sb = new StringBuilder();

    // filter for only \ and not \\ preceeding a possible escapable char
    boolean lastWasRealEscape = false;
    int lastAppendedPos = 0;
    int openHyphenPos = -1;
    for (int i = 0; i < str.length(); i++) {
      if ((str.charAt(i) == '"' || str.charAt(i) == '\'') && !lastWasRealEscape) {
        if (openHyphenPos < 0) {
          if (lastAppendedPos < i) {
            sb.append(StringTools.getUnEscapedStringOf(str.substring(lastAppendedPos, i)));
          }
          openHyphenPos = i;
          lastAppendedPos = i;
        } else if (str.charAt(i) == str.charAt(openHyphenPos)) {
          sb.append(StringTools.getUnEscapedStringOf(str.substring(openHyphenPos + 1, i)));
          openHyphenPos = -1;
          lastAppendedPos = i + 1;
        }
      }
      lastWasRealEscape = !lastWasRealEscape && str.charAt(i) == '\\';
    }

    // if there has been nothing with hyphens etc.
    if (lastAppendedPos < str.length()) {
      sb.append(StringTools.getUnEscapedStringOf(str.substring(lastAppendedPos)));
    }

    return sb.toString();
  }

  public abstract IdentifierFactory getIdentifierFactory();

  /** Constructs a XorExpr(Immediate, Immediate) grammar chunk. */
  public static JXorExpr newXorExpr(Immediate op1, Immediate op2) {
    return new JXorExpr(op1, op2);
  }

  /** Constructs a UshrExpr(Immediate, Immediate) grammar chunk. */
  public static JUshrExpr newUshrExpr(Immediate op1, Immediate op2) {
    return new JUshrExpr(op1, op2);
  }

  /** Constructs a SubExpr(Immediate, Immediate) grammar chunk. */
  public static JSubExpr newSubExpr(Immediate op1, Immediate op2) {
    return new JSubExpr(op1, op2);
  }

  /** Constructs a ShrExpr(Immediate, Immediate) grammar chunk. */
  public static JShrExpr newShrExpr(Immediate op1, Immediate op2) {
    return new JShrExpr(op1, op2);
  }

  /** Constructs a ShlExpr(Immediate, Immediate) grammar chunk. */
  public static JShlExpr newShlExpr(Immediate op1, Immediate op2) {
    return new JShlExpr(op1, op2);
  }

  /** Constructs a RemExpr(Immediate, Immediate) grammar chunk. */
  public static JRemExpr newRemExpr(Immediate op1, Immediate op2) {
    return new JRemExpr(op1, op2);
  }

  /** Constructs a OrExpr(Immediate, Immediate) grammar chunk. */
  public static JOrExpr newOrExpr(Immediate op1, Immediate op2) {
    return new JOrExpr(op1, op2);
  }

  /** Constructs a NeExpr(Immediate, Immediate) grammar chunk. */
  public static JNeExpr newNeExpr(Immediate op1, Immediate op2) {
    return new JNeExpr(op1, op2);
  }

  /** Constructs a MulExpr(Immediate, Immediate) grammar chunk. */
  public static JMulExpr newMulExpr(Immediate op1, Immediate op2) {
    return new JMulExpr(op1, op2);
  }

  /** Constructs a LeExpr(Immediate, Immediate) grammar chunk. */
  public static JLeExpr newLeExpr(Immediate op1, Immediate op2) {
    return new JLeExpr(op1, op2);
  }

  /** Constructs a GeExpr(Immediate, Immediate) grammar chunk. */
  public static JGeExpr newGeExpr(Immediate op1, Immediate op2) {
    return new JGeExpr(op1, op2);
  }

  /** Constructs a EqExpr(Immediate, Immediate) grammar chunk. */
  public static JEqExpr newEqExpr(Immediate op1, Immediate op2) {
    return new JEqExpr(op1, op2);
  }

  /** Constructs a DivExpr(Immediate, Immediate) grammar chunk. */
  public static JDivExpr newDivExpr(Immediate op1, Immediate op2) {
    return new JDivExpr(op1, op2);
  }

  /** Constructs a CmplExpr(Immediate, Immediate) grammar chunk. */
  public static JCmplExpr newCmplExpr(Immediate op1, Immediate op2) {
    return new JCmplExpr(op1, op2);
  }

  /** Constructs a CmpgExpr(Immediate, Immediate) grammar chunk. */
  public static JCmpgExpr newCmpgExpr(Immediate op1, Immediate op2) {
    return new JCmpgExpr(op1, op2);
  }

  /** Constructs a CmpExpr(Immediate, Immediate) grammar chunk. */
  public static JCmpExpr newCmpExpr(Immediate op1, Immediate op2) {
    return new JCmpExpr(op1, op2);
  }

  /** Constructs a GtExpr(Immediate, Immediate) grammar chunk. */
  public static JGtExpr newGtExpr(Immediate op1, Immediate op2) {
    return new JGtExpr(op1, op2);
  }

  /** Constructs a LtExpr(Immediate, Immediate) grammar chunk. */
  public static JLtExpr newLtExpr(Immediate op1, Immediate op2) {
    return new JLtExpr(op1, op2);
  }

  /** Constructs a AddExpr(Immediate, Immediate) grammar chunk. */
  public static JAddExpr newAddExpr(Immediate op1, Immediate op2) {
    return new JAddExpr(op1, op2);
  }

  /** Constructs a AndExpr(Immediate, Immediate) grammar chunk. */
  public static JAndExpr newAndExpr(Immediate op1, Immediate op2) {
    return new JAndExpr(op1, op2);
  }

  /** Constructs a NegExpr(Immediate, Immediate) grammar chunk. */
  public static JNegExpr newNegExpr(Immediate op) {
    return new JNegExpr(op);
  }

  /** Constructs a LengthExpr(Immediate) grammar chunk. */
  public static JLengthExpr newLengthExpr(Immediate op) {
    return new JLengthExpr(op);
  }

  /** Constructs a CastExpr(Immediate, Type) grammar chunk. */
  public static JCastExpr newCastExpr(Immediate op1, Type t) {
    return new JCastExpr(op1, t);
  }

  /** Constructs a InstanceOfExpr(Immediate, Type) grammar chunk. */
  public static JInstanceOfExpr newInstanceOfExpr(Immediate op1, Type t) {
    return new JInstanceOfExpr(op1, t);
  }

  /** Constructs a NewArrayExpr(Type, Immediate) grammar chunk. */
  public JNewArrayExpr newNewArrayExpr(Type type, Immediate size) {
    return new JNewArrayExpr(type, size, getIdentifierFactory());
  }

  public static JPhiExpr newPhiExpr(List<Local> args, Map<Local, BasicBlock<?>> argToBlock) {
    return new JPhiExpr(args, argToBlock);
  }

  /** Constructs a NewStaticInvokeExpr(ArrayType, List of Immediate) grammar chunk. */
  public static JStaticInvokeExpr newStaticInvokeExpr(
      MethodSignature method, List<Immediate> args) {
    return new JStaticInvokeExpr(method, args);
  }

  public static JStaticInvokeExpr newStaticInvokeExpr(MethodSignature method, Immediate... args) {
    return newStaticInvokeExpr(method, Arrays.asList(args));
  }

  public static JStaticInvokeExpr newStaticInvokeExpr(MethodSignature method, Immediate arg) {
    return newStaticInvokeExpr(method, Collections.singletonList(arg));
  }

  public static JStaticInvokeExpr newStaticInvokeExpr(MethodSignature method) {
    return newStaticInvokeExpr(method, Collections.emptyList());
  }

  /**
   * Constructs a NewSpecialInvokeExpr(Local base, SootMethod method, List of Immediate) grammar
   * chunk.
   */
  public static JSpecialInvokeExpr newSpecialInvokeExpr(
      Local base, MethodSignature method, List<Immediate> args) {
    return new JSpecialInvokeExpr(base, method, args);
  }

  /**
   * Constructs a NewSpecialInvokeExpr(Local base, SootMethod method, List of Immediate) grammar
   * chunk.
   */
  public static JSpecialInvokeExpr newSpecialInvokeExpr(
      Local base, MethodSignature method, Immediate... args) {
    return newSpecialInvokeExpr(base, method, Arrays.asList(args));
  }

  public static JSpecialInvokeExpr newSpecialInvokeExpr(
      Local base, MethodSignature method, Immediate arg) {
    return newSpecialInvokeExpr(base, method, Collections.singletonList(arg));
  }

  public static JSpecialInvokeExpr newSpecialInvokeExpr(Local base, MethodSignature method) {
    return newSpecialInvokeExpr(base, method, Collections.emptyList());
  }

  /**
   * Constructs a NewDynamicInvokeExpr(SootMethod bootstrapMethodSignature, List bootstrapArgs,
   * SootMethod methodSignature, List args) grammar chunk.
   */
  public static JDynamicInvokeExpr newDynamicInvokeExpr(
      MethodSignature bootstrapMethodSignature,
      List<Immediate> bootstrapArgs,
      MethodSignature methodSignature,
      List<Immediate> args) {
    return new JDynamicInvokeExpr(bootstrapMethodSignature, bootstrapArgs, methodSignature, args);
  }

  /**
   * Constructs a NewDynamicInvokeExpr(SootMethod bootstrapMethodSignature, List bootstrapArgs,
   * SootMethod methodSignature, List args) grammar chunk.
   */
  public static JDynamicInvokeExpr newDynamicInvokeExpr(
      MethodSignature bootstrapMethodSignature,
      List<Immediate> bootstrapArgs,
      MethodSignature methodSignature,
      int tag,
      List<Immediate> args) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, bootstrapArgs, methodSignature, tag, args);
  }

  /**
   * Constructs a NewVirtualInvokeExpr(Local base, SootMethod method, List of Immediate) grammar
   * chunk.
   */
  public static JVirtualInvokeExpr newVirtualInvokeExpr(
      Local base, MethodSignature method, List<Immediate> args) {
    return new JVirtualInvokeExpr(base, method, args);
  }

  /**
   * Constructs a NewVirtualInvokeExpr(Local base, SootMethod method, List of Immediate) grammar
   * chunk.
   */
  public static JVirtualInvokeExpr newVirtualInvokeExpr(
      Local base, MethodSignature method, Immediate... args) {
    return newVirtualInvokeExpr(base, method, Arrays.asList(args));
  }

  public static JVirtualInvokeExpr newVirtualInvokeExpr(
      Local base, MethodSignature method, Immediate arg) {
    return newVirtualInvokeExpr(base, method, Collections.singletonList(arg));
  }

  public static JVirtualInvokeExpr newVirtualInvokeExpr(Local base, MethodSignature method) {
    return newVirtualInvokeExpr(base, method, Collections.emptyList());
  }

  /**
   * Constructs a NewInterfaceInvokeExpr(Local base, SootMethod method, List of Immediate) grammar
   * chunk.
   */
  public static JInterfaceInvokeExpr newInterfaceInvokeExpr(
      Local base, MethodSignature method, List<Immediate> args) {
    return new JInterfaceInvokeExpr(base, method, args);
  }

  /**
   * Constructs a NewInterfaceInvokeExpr(Local base, SootMethod method, List of Immediate) grammar
   * chunk.
   */
  public static JInterfaceInvokeExpr newInterfaceInvokeExpr(
      Local base, MethodSignature method, Immediate... args) {
    return newInterfaceInvokeExpr(base, method, Arrays.asList(args));
  }

  public static JInterfaceInvokeExpr newInterfaceInvokeExpr(
      Local base, MethodSignature method, Immediate arg) {
    return newInterfaceInvokeExpr(base, method, Collections.singletonList(arg));
  }

  public static JInterfaceInvokeExpr newInterfaceInvokeExpr(Local base, MethodSignature method) {
    return newInterfaceInvokeExpr(base, method, Collections.emptyList());
  }

  /** Constructs a ThrowStmt(Immediate) grammar chunk. */
  public static JThrowStmt newThrowStmt(Immediate op, StmtPositionInfo posInfo) {
    return new JThrowStmt(op, posInfo);
  }

  /** Constructs a ExitMonitorStmt(Immediate) grammar chunk. */
  public static JExitMonitorStmt newExitMonitorStmt(Immediate op, StmtPositionInfo posInfo) {
    return new JExitMonitorStmt(op, posInfo);
  }

  /** Constructs a EnterMonitorStmt(Immediate) grammar chunk. */
  public static JEnterMonitorStmt newEnterMonitorStmt(Immediate op, StmtPositionInfo posInfo) {
    return new JEnterMonitorStmt(op, posInfo);
  }

  /** Constructs a BreakpointStmt() grammar chunk. */
  public static JBreakpointStmt newBreakpointStmt(StmtPositionInfo posInfo) {
    return new JBreakpointStmt(posInfo);
  }

  /** Constructs a GotoStmt(Stmt) grammar chunk. */
  public static JGotoStmt newGotoStmt(StmtPositionInfo posInfo) {
    return new JGotoStmt(posInfo);
  }

  /** Constructs a NopStmt() grammar chunk. */
  public static JNopStmt newNopStmt(StmtPositionInfo posInfo) {
    return new JNopStmt(posInfo);
  }

  /** Constructs a ReturnVoidStmt() grammar chunk. */
  public static JReturnVoidStmt newReturnVoidStmt(StmtPositionInfo posInfo) {
    return new JReturnVoidStmt(posInfo);
  }

  /** Constructs a ReturnStmt(Immediate) grammar chunk. */
  public static JReturnStmt newReturnStmt(Immediate op, StmtPositionInfo posInfo) {
    return new JReturnStmt(op, posInfo);
  }

  /** Constructs a RetStmt(Local) grammar chunk. */
  public static JRetStmt newRetStmt(Immediate stmtAddress, StmtPositionInfo posInfo) {
    return new JRetStmt(stmtAddress, posInfo);
  }

  /** Constructs a IfStmt(Condition, Stmt) grammar chunk. */
  public static JIfStmt newIfStmt(AbstractConditionExpr condition, StmtPositionInfo posInfo) {
    return new JIfStmt(condition, posInfo);
  }

  /** Constructs a IdentityStmt(Local, IdentityRef) grammar chunk. */
  public static <L extends IdentityRef> JIdentityStmt<L> newIdentityStmt(
      Local local, L identityRef, StmtPositionInfo posInfo) {
    return new JIdentityStmt<>(local, identityRef, posInfo);
  }

  /** Constructs a AssignStmt(Variable, RValue) grammar chunk. */
  public static <L extends Value, R extends Value> JAssignStmt<L, R> newAssignStmt(
      L variable, R rvalue, StmtPositionInfo posInfo) {
    return new JAssignStmt<>(variable, rvalue, posInfo);
  }

  /** Constructs a InvokeStmt(InvokeExpr) grammar chunk. */
  public static JInvokeStmt newInvokeStmt(AbstractInvokeExpr op, StmtPositionInfo posInfo) {
    return new JInvokeStmt(op, posInfo);
  }

  /** Constructs a TableSwitchStmt(Immediate, int, int, List of Stmt, Stmt) grammar chunk. */
  public static JSwitchStmt newTableSwitchStmt(
      Immediate key, int lowIndex, int highIndex, StmtPositionInfo posInfo) {
    return new JSwitchStmt(key, lowIndex, highIndex, posInfo);
  }

  /**
   * Constructs a LookupSwitchStmt(Immediate, List of Immediate, List of Stmt, Stmt) grammar chunk.
   */
  public static JSwitchStmt newLookupSwitchStmt(
      Immediate key, List<IntConstant> lookupValues, StmtPositionInfo posInfo) {
    return new JSwitchStmt(key, lookupValues, posInfo);
  }

  /** Constructs a Local with the given name and type. */
  public static Local newLocal(String name, Type t) {
    return new Local(name, t);
  }

  /** Constructs a JStaticFieldRef(FieldSignature) grammar chunk. */
  public static JStaticFieldRef newStaticFieldRef(FieldSignature f) {
    return new JStaticFieldRef(f);
  }

  /** Constructs a ThisRef(ClassType) grammar chunk. */
  public static JThisRef newThisRef(ClassType t) {
    return new JThisRef(t);
  }

  /** Constructs a ParameterRef(SootMethod, int) grammar chunk. */
  public static JParameterRef newParameterRef(Type paramType, int number) {
    return new JParameterRef(paramType, number);
  }

  /** Constructs a InstanceFieldRef(Local, FieldSignature) grammar chunk. */
  public static JInstanceFieldRef newInstanceFieldRef(Local base, FieldSignature f) {
    return new JInstanceFieldRef(base, f);
  }

  /** Constructs a ArrayRef(Local, Immediate) grammar chunk. */
  public JArrayRef newArrayRef(Local base, Immediate index) {
    return new JArrayRef(base, index);
  }

  /** Constructs a CaughtExceptionRef() grammar chunk. */
  public abstract JCaughtExceptionRef newCaughtExceptionRef();

  /** Constructs a NewExpr(RefType) grammar chunk. */
  public static JNewExpr newNewExpr(ClassType type) {
    return new JNewExpr(type);
  }

  public static JNewMultiArrayExpr newNewMultiArrayExpr(ArrayType type, List<Immediate> sizes) {
    return new JNewMultiArrayExpr(type, sizes);
  }

  public static Trap newTrap(ClassType exception, Stmt beginStmt, Stmt endStmt, Stmt handlerStmt) {
    return new Trap(exception, beginStmt, endStmt, handlerStmt);
  }
}
