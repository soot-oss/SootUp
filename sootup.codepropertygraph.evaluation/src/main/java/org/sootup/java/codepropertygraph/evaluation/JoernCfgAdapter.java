package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.*;
import io.shiftleft.codepropertygraph.generated.nodes.MethodParameterIn;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Edge;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernCfgGenerator;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.JGeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.codepropertygraph.propertygraph.*;
import sootup.java.core.JavaIdentifierFactory;

public class JoernCfgAdapter {
  private final JoernCfgGenerator joernCfgGenerator;

  public JoernCfgAdapter(JoernCfgGenerator joernCfgGenerator) {
    this.joernCfgGenerator = joernCfgGenerator;
  }

  public PropertyGraph getCfg(Graph joernCfg, Graph joernAst) {
    PropertyGraph cfgGraph = new PropertyGraph();

    for (Edge edge : joernCfgGenerator.getGraphEdges(joernCfg)) {
      StoredNode joernEdgeSrc = edge.src();
      StoredNode joernEdgeDst = edge.dst();
      PropertyGraphNode src = getSootUpNode(joernEdgeSrc, joernAst);
      PropertyGraphNode dst = getSootUpNode(joernEdgeDst, joernAst);

      if (joernEdgeSrc instanceof Method || joernEdgeDst instanceof MethodReturn) continue;

      cfgGraph.addEdge(new PropertyGraphEdge(src, dst, "CFG"));
    }

    return cfgGraph;
  }

  private PropertyGraphNode getSootUpNode(StoredNode node, Graph astGraph) {
    List<StoredNode> astVars = new ArrayList<>();
    joernCfgGenerator
        .getGraphVertices(astGraph)
        .forEach(
            v -> {
              if (v instanceof Local || v instanceof MethodParameterIn) {
                astVars.add(v);
              }
            });

    System.out.println(node.getClass());
    if (node instanceof Block) {
      Block block = (Block) node;
      System.out.println(block.label());
    } else if (node instanceof Identifier) {
      Identifier identifier = (Identifier) node;
      System.out.printf("%s %s%n", identifier.typeFullName(), identifier.name());
    } else if (node instanceof Method) {
      Method method = (Method) node;
      System.out.println(method.fullName());
    } else if (node instanceof JumpTarget) {
      JumpTarget jumpTarget = (JumpTarget) node;
      System.out.println(jumpTarget.name());
    } else if (node instanceof Modifier) {
      Modifier modifier = (Modifier) node;
      System.out.println(modifier.modifierType());
    } else if (node instanceof Type) {
      Type type = (Type) node;
      System.out.println(type.fullName());
    } else if (node instanceof Call) {
      Call call = (Call) node;
      System.out.println(call.code());

      StmtPositionInfo positionInfo;
      if (call.lineNumber().nonEmpty()) {
        positionInfo = new SimpleStmtPositionInfo(call.lineNumber().get());
      } else {
        positionInfo = StmtPositionInfo.createNoStmtPositionInfo();
      }

      if (call.methodFullName().equals("<operator>.assignment")) {
        String[] assignArgs = call.code().split(" = ");

        String varName = assignArgs[0];
        Value varValue = getConstantValue(assignArgs[1], call.typeFullName());

        Stmt assignStmt =
            new JAssignStmt(
                new sootup.core.jimple.basic.Local(varName, varValue.getType()),
                varValue,
                positionInfo);
        return new StmtPropertyGraphNode(
            assignStmt.toString(), NodeType.STMT, assignStmt.getPositionInfo());
      }

      if (call.methodFullName().equals("<operator>.greaterEqualsThan")) {
        String[] stmtTokens = call.code().split(" >= ");
        String varName = stmtTokens[0];
        String varType = null;

        boolean varFound = false;
        Immediate newLocal = null;
        for (StoredNode astVar : astVars) {
            if (astVar instanceof Local) {
              Local locVar = (Local) astVar;
              if (locVar.name().equals(varName)) {
                varFound = true;
                varType = locVar.typeFullName();
                break;
              }
            }
            else if (astVar instanceof MethodParameterIn) {
              MethodParameterIn parameterIn = (MethodParameterIn) astVar;
              if (parameterIn.name().equals(varName)) {
                varFound = true;
                varType = parameterIn.typeFullName();
                break;
              }
            }

        }

        if (!varFound) {throw new RuntimeException("Could not find variable " + varName); }
        Value value = getConstantValue(stmtTokens[1], varType);
        newLocal = new sootup.core.jimple.basic.Local(varName, value.getType());

        Stmt ifStmt =
            new JIfStmt(
                new JGeExpr(
                        newLocal,
                    (Immediate) value),
                positionInfo);

        return new StmtPropertyGraphNode(
            ifStmt.toString(), NodeType.STMT, ifStmt.getPositionInfo());
      }
    } else if (node instanceof Expression) {
      Expression expr = (Expression) node;
      System.out.println(expr.code());
      return new StmtPropertyGraphNode(
          expr.code(), NodeType.STMT, new SimpleStmtPositionInfo(expr.lineNumber().get()));

    } else if (node instanceof MethodReturn) {
      MethodReturn methodReturn = (MethodReturn) node;
      System.out.println(methodReturn.code());
    } else if (node instanceof MethodParameterIn) {
      MethodParameterIn MethodParameterIn = (MethodParameterIn) node;
      System.out.println(MethodParameterIn.code());
    } else if (node instanceof Local) {
      Local local = (Local) node;
      System.out.println(local.code());
    } else if (node instanceof Member) {
      Member member = (Member) node;
      System.out.println(member.code());
    } else if (node instanceof TypeArgument) {
      TypeArgument typeArgument = (TypeArgument) node;
      System.out.println(typeArgument.code());
    } else if (node instanceof TypeDecl) {
      TypeDecl typeDecl = (TypeDecl) node;
      System.out.println(typeDecl.code());
    }

    return null;
  }

  private Value getConstantValue(String constString, String typeStr) {
    Value value;
    switch (typeStr) {
      case "int":
      case "byte":
        value = IntConstant.getInstance(Integer.parseInt(constString));
        break;
      case "long":
        value = LongConstant.getInstance(Long.parseLong(constString));
        break;
      case "double":
        value = DoubleConstant.getInstance(Double.parseDouble(constString));
        break;
      case "float":
        value = FloatConstant.getInstance(Float.parseFloat(constString));
        break;
      case "boolean":
        value = BooleanConstant.getInstance(Boolean.parseBoolean(constString));
        break;
      case "java.lang.String":
        value =
            new StringConstant(
                constString, JavaIdentifierFactory.getInstance().getType("java.lang.String"));
        break;
      default:
        value =
            new ClassConstant(
                constString, JavaIdentifierFactory.getInstance().getType(constString));
    }
    return value;
  }
}
