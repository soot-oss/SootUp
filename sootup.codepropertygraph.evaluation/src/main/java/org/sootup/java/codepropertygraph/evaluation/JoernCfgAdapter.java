package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.*;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Edge;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernCfgGenerator;
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
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.codepropertygraph.propertygraph.*;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;

public class JoernCfgAdapter {
  private final JoernCfgGenerator joernCfgGenerator;

  public JoernCfgAdapter(JoernCfgGenerator joernCfgGenerator) {
    this.joernCfgGenerator = joernCfgGenerator;
  }

  public PropertyGraph getCfg(Graph joernCfg, Graph joernAst) {
    // thisClassType = new ClassConstant(joernCfg.vertices().)
    Method thisMethod =
        (Method) joernCfg.vertices().filter(v -> v.label().equals("METHOD")).toIterator().next();
    TypeDecl thisClass = thisMethod._typeDeclViaAstIn().get();

    PropertyGraph cfgGraph = new PropertyGraph();

    for (Edge edge : joernCfgGenerator.getGraphEdges(joernCfg)) {
      StoredNode joernEdgeSrc = edge.src();
      StoredNode joernEdgeDst = edge.dst();

      // Todo: Add the start and end edges
      if (joernEdgeSrc instanceof Method || joernEdgeDst instanceof MethodReturn) continue;

      // Only consider statements
      if (!(joernEdgeSrc._astIn().hasNext() && joernEdgeSrc._astIn().next() instanceof Block)) {
        // System.out.println("Should be skipped because of src.");
        continue;
      }
      if (!(joernEdgeDst._astIn().hasNext() && joernEdgeDst._astIn().next() instanceof Block)) {
        // System.out.println("Should be skipped because of dst.");
        while (joernEdgeDst._cfgOut().hasNext()
            && !(joernEdgeDst._astIn().next() instanceof Block)) {
          joernEdgeDst = joernEdgeDst._cfgOut().next();
        }
      }

      PropertyGraphNode src = getSootUpNode(joernEdgeSrc, joernAst, thisClass);
      PropertyGraphNode dst = getSootUpNode(joernEdgeDst, joernAst, thisClass);

      /*if (joernEdgeSrc instanceof Unknown
      && src.getName().startsWith("goto")
      && dst instanceof StmtPropertyGraphNode
      && ((StmtPropertyGraphNode) dst).getPositionInfo().getStmtPosition().getFirstLine()
          != ((Unknown) joernEdgeSrc).lineNumber().get()) continue;*/

      if (isGotoStatement(joernEdgeSrc)
          && ((Expression) joernEdgeDst).lineNumber().nonEmpty()
          && getGotoStmtTarget((Unknown) joernEdgeSrc).getStmtPosition().getFirstLine()
              != ((Expression) joernEdgeDst).lineNumber().get()) continue;

      System.out.println(
          "\t\t"
              + String.format("%-80s", "[" + joernEdgeSrc.label() + "]")
              + "["
              + joernEdgeDst.label()
              + "]");
      System.out.println(
          "\t"
              + String.format("%-60s", joernEdgeSrc.toMap().get("CODE").get())
              + "   ---->   "
              + joernEdgeDst.toMap().get("CODE").get());
      System.out.println(
          "\t"
              + String.format("%-60s", (src != null ? src.getName() : null))
              + "   ====>   "
              + (dst != null ? dst.getName() : null));
      System.out.println("\t" + String.join("", Collections.nCopies(100, "-")));

      cfgGraph.addEdge(new PropertyGraphEdge(src, dst, "CFG"));
    }

    return cfgGraph;
  }

  private PropertyGraphNode getSootUpNode(StoredNode node, Graph astGraph, TypeDecl thisClass) {

    Stmt stmt;
    if (node instanceof Call) {
      stmt = evaluateCall((Call) node);
    } else if (node instanceof Return) {
      stmt = evaluateReturn((Return) node);
    } else if (isGotoStatement(node)) {
      stmt = new JGotoStmt(getGotoStmtTarget((Unknown) node));
    } else {
      stmt =
          new JAssignStmt(
              new Local("notImplemented", PrimitiveType.getInt()),
              IntConstant.getInstance(1),
              StmtPositionInfo.createNoStmtPositionInfo());
    }

    if (stmt instanceof JGotoStmt) {
      JGotoStmt gotoStmt = (JGotoStmt) stmt;
      return new StmtPropertyGraphNode(
          gotoStmt + " " + gotoStmt.getPositionInfo().getStmtPosition().getFirstLine(),
          NodeType.STMT,
          stmt.getPositionInfo());
    }
    return new StmtPropertyGraphNode(stmt.toString(), NodeType.STMT, stmt.getPositionInfo());
  }

  private StmtPositionInfo getGotoStmtTarget(Unknown node) {
    try {
      return new SimpleStmtPositionInfo(Integer.parseInt(node.code().split(" ")[1]));
    } catch (NumberFormatException e) {
      return StmtPositionInfo.createNoStmtPositionInfo();
    }
  }

  private boolean isGotoStatement(StoredNode node) {
    return node instanceof Unknown && ((Unknown) node).code().startsWith("goto");
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
      stmtPosition = StmtPositionInfo.createNoStmtPositionInfo();
    }
    return stmtPosition;
  }

  private Stmt evaluateCall(Call call) {
    if (call.methodFullName().equals("<operator>.assignment")) {
      return evaluateAssignment(call);
    }

    if (call.methodFullName().equals("<operator>.throw")) {
      return new JThrowStmt(
          new Local("e", getNodeType(call.typeFullName())), getNodePositionInfo(call));
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
      return new Local(identifier.code().split("#")[0], getNodeType(identifier.typeFullName()));
    }

    if (expr instanceof Call) {
      Call call = (Call) expr;
      return evaluateCallExpr(call);
    }

    return new Local("notImplementedExpr", PrimitiveType.getInt());
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
        if ("#NaN".equals(constStr)) {
          constStr = "NaN";
        }
        return DoubleConstant.getInstance(Double.parseDouble(constStr));
      case "float":
        if ("#NaNF".equals(constStr)) {
          constStr = "NaN";
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
          return new ClassConstant(constStr.replace(".", "/"), getNodeType(typeFullName));
        }
        throw new RuntimeException("Unknown constant type: " + typeFullName);
    }
  }

  private Value evaluateCallExpr(Call call) {
    if (call.methodFullName().startsWith("<operator>")) {
      return evaluateOperation(call);
    }

    return evaluateInvokeExpr(call);
    // return evaluateFieldRef(call);
  }

  private Value evaluateInvokeExpr(Call call) {
    List<Immediate> args = getCallArguments(call);
    String methodFullName = call.methodFullName();

    if (MethodSignatureParser.isDynamicInvoke(methodFullName)) {
      /*MethodDetails methodDetails = MethodSignatureParser.parseMethodSignature(methodFullName);

      MethodSignature methodSignature =
          new MethodSignature(
              (ClassType) getNodeType(methodDetails.className),
              methodDetails.methodName,
              methodDetails.parameterTypes.stream()
                  .map(this::getNodeType)
                  .collect(Collectors.toList()),
              getNodeType(methodDetails.returnType));

      List<Immediate> testParameterList = Collections.singletonList(IntConstant.getInstance(2));
      MethodSignature testDynamicMethod =
          new MethodSignature(
              new JavaClassType(
                  JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.substring(
                      JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.lastIndexOf(".") + 1),
                  new PackageName(
                      JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.substring(
                          0, JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.lastIndexOf(".")))),
              methodDetails.methodName,
              methodDetails.parameterTypes.stream()
                  .map(this::getNodeType)
                  .collect(Collectors.toList()),
              getNodeType(methodDetails.returnType));
      return new JDynamicInvokeExpr(methodSignature, args, testDynamicMethod, testParameterList);*/
      return getDummyDynamicInvokeExpr();
    }

    if (call.dispatchType().equals("DYNAMIC_DISPATCH")) {
      MethodDetails methodDetails =
          MethodSignatureParser.parseMethodSignature(call.methodFullName());
      MethodSubSignature methodSubSignature =
          new MethodSubSignature(
              methodDetails.methodName,
              methodDetails.parameterTypes.stream()
                  .map(this::getNodeType)
                  .collect(Collectors.toList()),
              getNodeType(methodDetails.returnType));

      Identifier referencedObject = (Identifier) call.astOut().next();
      String referencedObjectType = getNodeType(referencedObject.typeFullName()).toString();
      MethodSignature methodSignature =
          new MethodSignature(
              new JavaClassType(
                  getClassName(methodDetails.className),
                  new PackageName(getPackageName(methodDetails.className))),
              methodSubSignature);

      args.remove(0);
      /*if (Arrays.asList("this", "superType").contains(referencedObject.name())) {
        return new JSpecialInvokeExpr(
            (Local) evaluateExpr(referencedObject), methodSignature, args);
      }*/
      return new JVirtualInvokeExpr((Local) evaluateExpr(referencedObject), methodSignature, args);

    }

    MethodDetails methodDetails = MethodSignatureParser.parseMethodSignature(call.methodFullName());
    MethodSubSignature methodSubSignature =
        new MethodSubSignature(
            methodDetails.methodName,
            methodDetails.parameterTypes.stream()
                .map(this::getNodeType)
                .collect(Collectors.toList()),
            getNodeType(methodDetails.returnType));

    try {
      MethodSignature methodSignature =
          new MethodSignature((ClassType) getNodeType(call.typeFullName()), methodSubSignature);
      return new JStaticInvokeExpr(methodSignature, args);
    } catch (RuntimeException e) {
      Identifier referencedObject = (Identifier) call.astOut().next();
      String referencedObjectType = getNodeType(referencedObject.typeFullName()).toString();
      MethodSignature methodSignature =
          new MethodSignature(
              new JavaClassType(
                  getClassName(methodDetails.className),
                  new PackageName(getPackageName(methodDetails.className))),
              methodSubSignature);

      args.remove(0);
      /*if (Arrays.asList("this", "superType").contains(referencedObject.name())) {
        return new JSpecialInvokeExpr(
            (Local) evaluateExpr(referencedObject), methodSignature, args);
      }*/
      return new JVirtualInvokeExpr((Local) evaluateExpr(referencedObject), methodSignature, args);
    }
  }

  private JDynamicInvokeExpr getDummyDynamicInvokeExpr() {
    List<Immediate> testParameterList = Collections.singletonList(IntConstant.getInstance(1));
    List<Immediate> args = Collections.emptyList();
    MethodSignature testDynamicMethod =
        new MethodSignature(
            new JavaClassType(
                JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.substring(
                    JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.lastIndexOf(".") + 1),
                new PackageName(
                    JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.substring(
                        0, JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.lastIndexOf(".")))),
            "bootstrap$",
            Collections.emptyList(),
            PrimitiveType.getInt());
    return new JDynamicInvokeExpr(testDynamicMethod, args, testDynamicMethod, testParameterList);
  }

  private List<Type> getMethodParamTypes(String methodFullName) {
    List<Type> types = new ArrayList<>();

    int start = methodFullName.indexOf('(');
    int end = methodFullName.indexOf(')', start);

    if (start != -1 && end != -1) {
      String parameters = methodFullName.substring(start + 1, end).trim();

      if (!parameters.isEmpty()) {
        for (String param : parameters.split(",")) {
          types.add(getNodeType(param.trim()));
        }
      }
    }

    return types;
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
        Type callType = getNodeType(call.typeFullName());
        if (callType instanceof ArrayType) {
          Value size = evaluateExpr(call.astOut().next());
          return new JNewArrayExpr(callType, (Immediate) size, JavaIdentifierFactory.getInstance());
        }
        return new JNewExpr((ClassType) getNodeType(call.typeFullName()));
      case "<operator>.cast":
        TypeRef castType = (TypeRef) astOut.next();
        Immediate castedValue = (Immediate) evaluateExpr(astOut.next());
        return new JCastExpr(castedValue, getNodeType(castType.typeFullName()));
      case "<operator>.equals":
        return new JEqExpr(
            (Immediate) evaluateExpr(astOut.next()), (Immediate) evaluateExpr(astOut.next()));
      case "<operator>.instanceOf":
        return new JInstanceOfExpr(
            (Immediate) evaluateExpr(astOut.next()),
            getNodeType(((TypeRef) astOut.next()).typeFullName()));
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
                getNodeType(((Call) field.astIn().next()).typeFullName()));

        if (referencedObject.code().contains(".")) {
          return new JStaticFieldRef(fieldSig);
        }
        return new JInstanceFieldRef((Local) evaluateExpr(referencedObject), fieldSig);
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

  private sootup.core.types.Type getNodeType(String typeStr) {
      switch (typeStr) {
      case "byte":
        return PrimitiveType.getByte();
      case "int":
        return PrimitiveType.getInt();
      case "long":
        return PrimitiveType.getLong();
      case "float":
        return PrimitiveType.getFloat();
      case "double":
        return PrimitiveType.getDouble();
      case "boolean":
        return PrimitiveType.getBoolean();
      case "short":
        return PrimitiveType.getShort();
      case "char":
        return PrimitiveType.getChar();
      default:
        return JavaIdentifierFactory.getInstance().getType(typeStr);
    }
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
