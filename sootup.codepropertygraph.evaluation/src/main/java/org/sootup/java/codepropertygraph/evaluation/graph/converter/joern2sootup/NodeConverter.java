package org.sootup.java.codepropertygraph.evaluation.graph.converter.joern2sootup;

import io.shiftleft.codepropertygraph.generated.nodes.*;
import java.util.*;
import java.util.stream.Collectors;
import org.sootup.java.codepropertygraph.evaluation.graph.model.JoernMethodDetails;
import org.sootup.java.codepropertygraph.evaluation.graph.util.JoernMethodSignatureParser;
import org.sootup.java.codepropertygraph.evaluation.graph.util.NodeTypeResolver;
import scala.Option;
import scala.collection.Iterator;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;

public class NodeConverter {
  private final NodeTypeResolver nodeTypeResolver;
  private final Map<String, String> primitiveTypesInJvmFormat;

  public NodeConverter() {
    this.nodeTypeResolver = new NodeTypeResolver();

    this.primitiveTypesInJvmFormat = new HashMap<>();

    this.primitiveTypesInJvmFormat.put("byte", "B");
    this.primitiveTypesInJvmFormat.put("char", "C");
    this.primitiveTypesInJvmFormat.put("double", "D");
    this.primitiveTypesInJvmFormat.put("float", "F");
    this.primitiveTypesInJvmFormat.put("int", "I");
    this.primitiveTypesInJvmFormat.put("long", "J");
    this.primitiveTypesInJvmFormat.put("short", "S");
    this.primitiveTypesInJvmFormat.put("boolean", "Z");
  }

  public Stmt convert(StoredNode node) {

    Stmt stmt;
    if (node instanceof Call) {
      stmt = evaluateCall((Call) node);
    } else if (node instanceof Return) {
      stmt = evaluateReturn((Return) node);
    } else if (nodeTypeResolver.isGotoStatement(node)) {
      stmt = new JGotoStmt(getNodePositionInfo((CfgNode) node));
      // gotoStmtTargets.put(stmt, getGotoStmtTarget((Unknown) node));
    } else if (nodeTypeResolver.isEnterMonitorStatement(node)) {
      stmt =
          new JEnterMonitorStmt(
              (Immediate) evaluateExpr((Expression) node._astOut().next()),
              getNodePositionInfo((CfgNode) node));
    } else if (nodeTypeResolver.isExitMonitorStatement(node)) {
      stmt =
          new JExitMonitorStmt(
              (Immediate) evaluateExpr((Expression) node._astOut().next()),
              getNodePositionInfo((CfgNode) node));
    } else {
      stmt =
          new JAssignStmt(
              new sootup.core.jimple.basic.Local("notImplemented", PrimitiveType.getInt()),
              IntConstant.getInstance(1),
              StmtPositionInfo.getNoStmtPositionInfo());
    }

    return stmt;
  }

  public StmtPositionInfo getGotoStmtTarget(Unknown node) {
    try {
      return new SimpleStmtPositionInfo(Integer.parseInt(node.code().split(" ")[1]));
    } catch (NumberFormatException e) {
      return StmtPositionInfo.getNoStmtPositionInfo();
    }
  }

  private Stmt evaluateReturn(Return node) {
    Iterator<CfgNode> astOut = node.astOut();
    StmtPositionInfo positionInfo = getNodePositionInfo(node);
    if (astOut.hasNext()) {
      return new JReturnStmt((Immediate) evaluateExpr((Expression) astOut.next()), positionInfo);
    }
    return new JReturnVoidStmt(positionInfo);
  }

  private StmtPositionInfo getNodePositionInfo(CfgNode node) {
    Option<Integer> lineNumberOpt = node.lineNumber();
    StmtPositionInfo stmtPosition;
    if (lineNumberOpt.nonEmpty()) {
      stmtPosition = new SimpleStmtPositionInfo(node.lineNumber().get());
    } else {
      stmtPosition = StmtPositionInfo.getNoStmtPositionInfo();
    }
    return stmtPosition;
  }

  private Stmt evaluateCall(Call call) {
    if (call.methodFullName().equals("<operator>.assignment")) {
      return evaluateAssignment(call);
    }

    if (call.methodFullName().equals("<operator>.throw")) {
      return new JThrowStmt(
          new sootup.core.jimple.basic.Local(
              call.astOut().next().code(), nodeTypeResolver.getNodeType(call.typeFullName())),
          getNodePositionInfo(call));
    }

    Value exprCall = evaluateCallExpr(call);
    StmtPositionInfo positionInfo = getNodePositionInfo(call);

    if (exprCall instanceof AbstractConditionExpr) {
      AbstractConditionExpr conditionExpr = (AbstractConditionExpr) exprCall;
      return new JIfStmt(conditionExpr, positionInfo);
    }
    return new JInvokeStmt((AbstractInvokeExpr) evaluateCallExpr(call), positionInfo);
  }

  private Stmt evaluateAssignment(Call call) {
    Iterator<Expression> astOut = call.astOut();
    Expression lhs = astOut.next();
    Expression rhs = astOut.next();

    return new JAssignStmt(
        (LValue) evaluateExpr(lhs), evaluateExpr(rhs), getNodePositionInfo(call));
  }

  private Value evaluateExpr(Expression expr) {
    if (expr instanceof Literal) {
      Literal literal = (Literal) expr;
      return getConstant(literal.code(), literal.typeFullName());
    }

    if (expr instanceof Identifier) {
      Identifier identifier = (Identifier) expr;
      return new sootup.core.jimple.basic.Local(
          identifier.code().split("#")[0], nodeTypeResolver.getNodeType(identifier.typeFullName()));
    }

    if (expr instanceof Call) {
      Call call = (Call) expr;
      return evaluateCallExpr(call);
    }

    return new sootup.core.jimple.basic.Local("notImplementedExpr", PrimitiveType.getInt());
  }

  private Value getConstant(String constStr, String typeFullName) {
    switch (typeFullName) {
      case "int":
      case "byte":
        return IntConstant.getInstance(Integer.parseInt(constStr));
      case "long":
        if ("#NaNL".equals(constStr)) {
          constStr = "NaN";
        }
        if (constStr.endsWith("L") || constStr.endsWith("l")) {
          constStr = constStr.substring(0, constStr.length() - 1);
        }
        return LongConstant.getInstance(Long.parseLong(constStr));
      case "double":
        if (constStr.startsWith("#")) {
          constStr = constStr.substring(1);
        }
        return DoubleConstant.getInstance(Double.parseDouble(constStr));
      case "float":
        if (constStr.endsWith("F")) {
          constStr = constStr.substring(0, constStr.length() - 1);
        }
        if (constStr.startsWith("#")) {
          constStr = constStr.substring(1);
        }
        return FloatConstant.getInstance(Float.parseFloat(constStr));
      case "boolean":
        return BooleanConstant.getInstance(Boolean.parseBoolean(constStr));
      case "java.lang.String":
        return new StringConstant(
            constStr, JavaIdentifierFactory.getInstance().getType("java.lang.String"));
      case "null":
        return NullConstant.getInstance();
      default:
        if (typeFullName.equals("java.lang.Class")) {
          constStr = constStr.substring(0, constStr.length() - ".class".length());
        }
        String constStrInJvmFormat = formatConstToJvmStr(constStr);
        return new ClassConstant(constStrInJvmFormat, nodeTypeResolver.getNodeType(typeFullName));
    }
  }

  private String formatConstToJvmStr(String constStr) {
    constStr = constStr.replace(".", "/");

    int arrayDepth = constStr.split("\\[]", -1).length - 1;
    String baseType =
        constStr.substring(
            0, constStr.indexOf('[') == -1 ? constStr.length() : constStr.indexOf('['));

    StringBuilder jvmFormat = new StringBuilder();
    for (int i = 0; i < arrayDepth; i++) {
      jvmFormat.append("[");
    }

    if (primitiveTypesInJvmFormat.containsKey(baseType)) {
      jvmFormat.append(primitiveTypesInJvmFormat.get(baseType));
    } else {
      jvmFormat.append("L").append(baseType).append(";");
    }

    return jvmFormat.toString();
  }

  private Value evaluateCallExpr(Call call) {
    if (call.methodFullName().startsWith("<operator>")) {
      return evaluateOperation(call);
    }

    return evaluateInvokeExpr(call);
  }

  private Value evaluateInvokeExpr(Call call) {
    List<Immediate> args = getCallArguments(call);

    if (call.dispatchType().equals("DYNAMIC_DISPATCH")) {
      JoernMethodDetails joernMethodDetails =
          JoernMethodSignatureParser.parseMethodSignature(call.methodFullName());
      MethodSubSignature methodSubSignature =
          new MethodSubSignature(
              joernMethodDetails.getMethodName(),
              joernMethodDetails.getParameterTypes().stream()
                  .map(nodeTypeResolver::getNodeType)
                  .collect(Collectors.toList()),
              nodeTypeResolver.getNodeType(joernMethodDetails.getReturnType()));

      Identifier referencedObject = (Identifier) call.astOut().next();
      MethodSignature methodSignature =
          new MethodSignature(
              new JavaClassType(
                  getClassName(joernMethodDetails.getClassName()),
                  new PackageName(getPackageName(joernMethodDetails.getClassName()))),
              methodSubSignature);

      args.remove(0);
      return new JVirtualInvokeExpr(
          (sootup.core.jimple.basic.Local) evaluateExpr(referencedObject), methodSignature, args);
    }

    JoernMethodDetails joernMethodDetails =
        JoernMethodSignatureParser.parseMethodSignature(call.methodFullName());
    MethodSubSignature methodSubSignature =
        new MethodSubSignature(
            joernMethodDetails.getMethodName(),
            joernMethodDetails.getParameterTypes().stream()
                .map(nodeTypeResolver::getNodeType)
                .collect(Collectors.toList()),
            nodeTypeResolver.getNodeType(joernMethodDetails.getReturnType()));

    try {
      MethodSignature methodSignature =
          new MethodSignature(
              (ClassType) nodeTypeResolver.getNodeType(call.typeFullName()), methodSubSignature);
      return new JStaticInvokeExpr(methodSignature, args);
    } catch (RuntimeException e) {
      Identifier referencedObject = (Identifier) call.astOut().next();
      MethodSignature methodSignature =
          new MethodSignature(
              new JavaClassType(
                  getClassName(joernMethodDetails.getClassName()),
                  new PackageName(getPackageName(joernMethodDetails.getClassName()))),
              methodSubSignature);

      args.remove(0);
      return new JVirtualInvokeExpr(
          (sootup.core.jimple.basic.Local) evaluateExpr(referencedObject), methodSignature, args);
    }
  }

  private List<Immediate> getCallArguments(Call call) {
    List<Immediate> args = new ArrayList<>();
    Iterator<CfgNode> argOut = call.argumentOut();
    while (argOut.hasNext()) {
      args.add((Immediate) evaluateExpr((Expression) argOut.next()));
    }
    return args;
  }

  private Value evaluateOperation(Call call) {
    Iterator<Expression> astOut = call.astOut();
    switch (call.methodFullName()) {
      case "<operator>.alloc":
        sootup.core.types.Type callType = nodeTypeResolver.getNodeType(call.typeFullName());
        if (callType instanceof ArrayType) {
          Value size = evaluateExpr(call.astOut().next());
          sootup.core.types.Type arrElemType =
              nodeTypeResolver.getNodeType(call.typeFullName().replace("[]", ""));
          return new JNewArrayExpr(
              arrElemType, (Immediate) size, JavaIdentifierFactory.getInstance());
        }
        return new JNewExpr((ClassType) callType);
      case "<operator>.cast":
        TypeRef castType = (TypeRef) astOut.next();
        Immediate castedValue = (Immediate) evaluateExpr(astOut.next());
        return new JCastExpr(castedValue, nodeTypeResolver.getNodeType(castType.typeFullName()));
      case "<operator>.equals":
        return new JEqExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.instanceOf":
        return new JInstanceOfExpr(
            (Immediate) evaluateExpr(astOut.next()),
            nodeTypeResolver.getNodeType(((TypeRef) astOut.next()).typeFullName()));
      case "<operator>.greaterEqualsThan":
        return new JGeExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.lessEqualsThan":
        return new JLeExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.fieldAccess":
        Identifier referencedObject = (Identifier) astOut.next();
        FieldIdentifier field = (FieldIdentifier) astOut.next();
        FieldSignature fieldSig =
            new FieldSignature(
                new JavaClassType(
                    getClassName(referencedObject.typeFullName()),
                    new PackageName(getPackageName(referencedObject.typeFullName()))),
                field.code(),
                nodeTypeResolver.getNodeType(((Call) field.astIn().next()).typeFullName()));

        if (referencedObject.code().contains(".")) {
          return new JStaticFieldRef(fieldSig);
        }
        return new JInstanceFieldRef(
            (sootup.core.jimple.basic.Local) evaluateExpr(referencedObject), fieldSig);
      case "<operator>.lengthOf":
        return new JLengthExpr((Immediate) evaluateExpr(astOut.next()));
      case "<operator>.indexAccess":
        return new JArrayRef(
            (Local) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.addition":
        return new JAddExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.notEquals":
        return new JNeExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.multiplication":
        return new JMulExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.xor":
        return new JXorExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.lessThan":
        return new JLtExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.subtraction":
        return new JSubExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.greaterThan":
        return new JGtExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.modulo":
        return new JRemExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.compare":
        return new JCmpExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.and":
        return new JAndExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.shiftLeft":
        return new JShlExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.or":
        return new JOrExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.logicalShiftRight":
      case "<operator>.arithmeticShiftRight":
        return new JShrExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.division":
        return new JDivExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.minus":
        return new JNegExpr((Immediate) evaluateExpr(astOut.next()));
      default:
        break;
    }
    throw new RuntimeException("Unknown operation: " + call.code());
  }

  private String getClassName(String signature) {
    int lastDotIndex = signature.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return signature;
    }
    return signature.substring(lastDotIndex + 1);
  }

  private String getPackageName(String signature) {
    int lastDotIndex = signature.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return "";
    }
    return signature.substring(0, lastDotIndex);
  }
}
