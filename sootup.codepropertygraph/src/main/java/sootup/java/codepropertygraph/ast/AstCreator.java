package sootup.java.codepropertygraph.ast;

import static sootup.java.codepropertygraph.propertygraph.NodeType.*;

import java.util.List;
import java.util.Set;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.types.Type;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.StmtUtils;
import sootup.java.codepropertygraph.propertygraph.NodeType;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphNode;
import sootup.java.codepropertygraph.propertygraph.StmtPropertyGraphNode;

public class AstCreator {
  public static PropertyGraph convert(MethodInfo methodInfo) {
    PropertyGraph graph = new PropertyGraph();
    PropertyGraphNode rootNode = new PropertyGraphNode(methodInfo.getName(), AGGREGATE);

    PropertyGraphNode modifiersNode = new PropertyGraphNode("Modifiers", AGGREGATE);
    PropertyGraphNode parametersTypesNode = new PropertyGraphNode("Parameters", AGGREGATE);
    PropertyGraphNode bodyStmtsNode = new PropertyGraphNode("Body", AGGREGATE);

    graph.addEdge(rootNode, modifiersNode, "AST");
    graph.addEdge(rootNode, parametersTypesNode, "AST");
    graph.addEdge(rootNode, bodyStmtsNode, "AST");

    addModifierEdges(graph, modifiersNode, methodInfo.getModifiers());
    addParameterTypeEdges(graph, parametersTypesNode, methodInfo.getParameterTypes());
    addBodyStmtEdges(graph, bodyStmtsNode, methodInfo.getBodyStmts(), methodInfo.getBody());
    addReturnStmtEdge(graph, rootNode, methodInfo.getReturnType());

    return graph;
  }

  private static void addModifierEdges(
      PropertyGraph graph, PropertyGraphNode parentNode, Set<MethodModifier> modifiers) {
    for (MethodModifier modifier : modifiers) {
      graph.addEdge(parentNode, new PropertyGraphNode(modifier.name(), MODIFIER), "AST: Modifier");
    }
  }

  private static void addParameterTypeEdges(
      PropertyGraph graph, PropertyGraphNode parentNode, List<Type> parameterTypes) {
    for (Type parameterType : parameterTypes) {
      graph.addEdge(
          parentNode,
          new PropertyGraphNode(parameterType.toString(), PARAMETER_TYPE),
          "AST: Param");
    }
  }

  private static void addBodyStmtEdges(
      PropertyGraph graph, PropertyGraphNode parentNode, List<Stmt> bodyStmts, Body body) {
    for (Stmt stmt : bodyStmts) {
      StmtPropertyGraphNode stmtNode =
          new StmtPropertyGraphNode(
              StmtUtils.getStmtSource(stmt, body), NodeType.STMT, stmt.getPositionInfo(), stmt);
      graph.addEdge(parentNode, stmtNode, "AST: Stmt");
      addStmtComponents(graph, stmtNode, stmt, body);
    }
  }

  private static void addStmtComponents(
      PropertyGraph graph, PropertyGraphNode parentNode, Stmt stmt, Body body) {
    switch (stmt.getClass().getSimpleName()) {
      case "JIfStmt":
        {
          JIfStmt currStmt = (JIfStmt) stmt;
          graph.addEdge(
              parentNode,
              new PropertyGraphNode(currStmt.getCondition().getOp1().toString(), OP1),
              "AST: OP1");
          graph.addEdge(
              parentNode,
              new PropertyGraphNode(currStmt.getCondition().getSymbol(), COND),
              "AST: Cond");
          graph.addEdge(
              parentNode,
              new PropertyGraphNode(currStmt.getCondition().getOp2().toString(), OP2),
              "AST: Op2");
          break;
        }
      case "JAssignStmt":
        {
          JAssignStmt currStmt = (JAssignStmt) stmt;
          graph.addEdge(
              parentNode,
              new PropertyGraphNode(currStmt.getLeftOp().toString(), LEFTOP),
              "AST: LeftOp");
          graph.addEdge(
              parentNode,
              new PropertyGraphNode(currStmt.getRightOp().toString(), RIGHTOP),
              "AST: RightOp");
          break;
        }

      case "JIdentityStmt":
        {
          JIdentityStmt currStmt = (JIdentityStmt) stmt;
          graph.addEdge(
              parentNode,
              new PropertyGraphNode(currStmt.getLeftOp().toString(), LEFTOP),
              "AST: LeftOp");
          graph.addEdge(
              parentNode,
              new PropertyGraphNode(currStmt.getRightOp().toString(), RIGHTOP),
              "AST: RightOp");
          break;
        }

      case "JInvokeStmt":
        {
          JInvokeStmt currStmt = (JInvokeStmt) stmt;
          // Todo: Make sure JInvokeStmt statements are handled correctly
          graph.addEdge(
              parentNode,
              new PropertyGraphNode(currStmt.getInvokeExpr().toString(), CMP),
              "AST: CMP");
          break;
        }

      case "JNopStmt":
        {
          JNopStmt currStmt = (JNopStmt) stmt;
          // Todo: Make sure JNoStmt statements are handled correctly
          graph.addEdge(parentNode, new PropertyGraphNode("NOP", CMP), "AST: CMP");
          break;
        }

      case "JThrowStmt":
        {
          JThrowStmt currStmt = (JThrowStmt) stmt;
          // Todo: Make sure JThrowStmt statements are handled correctly
          graph.addEdge(
              parentNode, new PropertyGraphNode(currStmt.getOp().toString(), CMP), "AST: CMP");
          break;
        }

      case "JReturnStmt":
        {
          JReturnStmt currStmt = (JReturnStmt) stmt;
          graph.addEdge(
              parentNode, new PropertyGraphNode(currStmt.getOp().toString(), OP), "AST: Op");
          break;
        }

      case "JGotoStmt":
        {
          JGotoStmt currStmt = (JGotoStmt) stmt;
          int gotoPosition =
              currStmt
                  .getTargetStmts(body)
                  .get(0)
                  .getPositionInfo()
                  .getStmtPosition()
                  .getFirstLine();
          graph.addEdge(
              parentNode, new PropertyGraphNode(Integer.toString(gotoPosition), POS), "AST: Pos");
          break;
        }

      case "JReturnVoidStmt":
        {
          break;
        }

      case "JSwitchStmt":
        {
          JSwitchStmt currStmt = (JSwitchStmt) stmt;
          for (Stmt targetStmt : currStmt.getTargetStmts(body)) {
            graph.addEdge(parentNode, new PropertyGraphNode(targetStmt.toString(), OP), "AST: Op");
          }
          break;
        }

      case "JEnterMonitorStmt":
        {
          JEnterMonitorStmt currStmt = (JEnterMonitorStmt) stmt;
          if (currStmt.containsInvokeExpr()) {
            graph.addEdge(
                parentNode,
                new PropertyGraphNode(currStmt.getInvokeExpr().toString(), CMP),
                "AST: CMP");
          }
          graph.addEdge(
              parentNode, new PropertyGraphNode(currStmt.getOp().toString(), OP), "AST: OP");
          break;
        }

      case "JExitMonitorStmt":
        {
          JExitMonitorStmt currStmt = (JExitMonitorStmt) stmt;
          if (currStmt.containsInvokeExpr()) {
            graph.addEdge(
                parentNode,
                new PropertyGraphNode(currStmt.getInvokeExpr().toString(), CMP),
                "AST: CMP");
          }
          graph.addEdge(
              parentNode, new PropertyGraphNode(currStmt.getOp().toString(), OP), "AST: OP");
          break;
        }

      default:
        throw new IllegalStateException("Unexpected value: " + stmt.getClass().getSimpleName());
    }
  }

  private static void addReturnStmtEdge(
      PropertyGraph graph, PropertyGraphNode parentNode, Type returnType) {
    graph.addEdge(
        parentNode, new PropertyGraphNode(returnType.toString(), RETURN_TYPE), "AST: ReturnType");
  }
}
