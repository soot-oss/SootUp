package sootup.java.codepropertygraph.ast;

import static sootup.java.codepropertygraph.ast.AstNodeType.*;

import java.util.List;
import java.util.Set;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.model.Modifier;
import sootup.core.types.Type;

public class AstToGraphConverter {
  public static AstGraph convert(MethodAst methodAst) {
    AstGraph graph = new AstGraph();
    AstNode rootNode = new AstNode(methodAst.getName(), AGGREGATE);

    AstNode modifiersNode = new AstNode("Modifiers", AGGREGATE);
    AstNode parametersTypesNode = new AstNode("ParameterTypes", AGGREGATE);
    AstNode bodyStmtsNode = new AstNode("BodyStmts", AGGREGATE);

    graph.addEdge(rootNode, modifiersNode);
    graph.addEdge(rootNode, parametersTypesNode);
    graph.addEdge(rootNode, bodyStmtsNode);

    addModifierEdges(graph, modifiersNode, methodAst.getModifiers());
    addParameterTypeEdges(graph, parametersTypesNode, methodAst.getParameterTypes());
    addBodyStmtEdges(graph, bodyStmtsNode, methodAst.getBodyStmts(), methodAst.getBody());
    addReturnStmtEdge(graph, rootNode, methodAst.getReturnType());

    return graph;
  }

  private static void addModifierEdges(
      AstGraph graph, AstNode parentNode, Set<Modifier> modifiers) {
    for (Modifier modifier : modifiers) {
      graph.addEdge(parentNode, new AstNode(modifier.name(), MODIFIER));
    }
  }

  private static void addParameterTypeEdges(
      AstGraph graph, AstNode parentNode, List<Type> parameterTypes) {
    for (Type parameterType : parameterTypes) {
      graph.addEdge(parentNode, new AstNode(parameterType.toString(), PARAMETER_TYPE));
    }
  }

  private static void addBodyStmtEdges(
      AstGraph graph, AstNode parentNode, List<Stmt> bodyStmts, Body body) {
    for (Stmt stmt : bodyStmts) {
      AstNode stmtNode = new AstNode(stmt.toString(), STMT);
      graph.addEdge(parentNode, stmtNode);
      addStmtComponents(graph, stmtNode, stmt, body);
    }
  }

  private static void addStmtComponents(AstGraph graph, AstNode parentNode, Stmt stmt, Body body) {
    switch (stmt.getClass().getSimpleName()) {
      case "JIfStmt":
        {
          JIfStmt currStmt = (JIfStmt) stmt;
          graph.addEdge(parentNode, new AstNode(currStmt.getCondition().getOp1().toString(), CMP));
          graph.addEdge(parentNode, new AstNode(currStmt.getCondition().getSymbol(), CMP));
          graph.addEdge(parentNode, new AstNode(currStmt.getCondition().getOp2().toString(), CMP));
          break;
        }
      case "JAssignStmt":
        {
          JAssignStmt currStmt = (JAssignStmt) stmt;
          graph.addEdge(parentNode, new AstNode(currStmt.getLeftOp().toString(), CMP));
          graph.addEdge(parentNode, new AstNode(currStmt.getRightOp().toString(), CMP));
          break;
        }

      case "JIdentityStmt":
        {
          JIdentityStmt currStmt = (JIdentityStmt) stmt;
          graph.addEdge(parentNode, new AstNode(currStmt.getLeftOp().toString(), CMP));
          graph.addEdge(parentNode, new AstNode(currStmt.getRightOp().toString(), CMP));
          break;
        }

      case "JInvokeStmt":
        {
          JInvokeStmt currStmt = (JInvokeStmt) stmt;
          // Todo: Make sure JInvokeStmt statements are handled correctly
          graph.addEdge(parentNode, new AstNode(currStmt.getInvokeExpr().toString(), CMP));
          break;
        }

      case "JNopStmt":
        {
          JNopStmt currStmt = (JNopStmt) stmt;
          // Todo: Make sure JNoStmt statements are handled correctly
          graph.addEdge(parentNode, new AstNode("NOP", CMP));
          break;
        }

      case "JThrowStmt":
        {
          JThrowStmt currStmt = (JThrowStmt) stmt;
          // Todo: Make sure JThrowStmt statements are handled correctly
          graph.addEdge(parentNode, new AstNode(currStmt.getOp().toString(), CMP));
          break;
        }

      case "JReturnStmt":
        {
          JReturnStmt currStmt = (JReturnStmt) stmt;
          graph.addEdge(parentNode, new AstNode(currStmt.getOp().toString(), CMP));
          break;
        }

      case "JGotoStmt":
        JGotoStmt currStmt = (JGotoStmt) stmt;
        int gotoPosition =
            currStmt.getTargetStmts(body).get(0).getPositionInfo().getStmtPosition().getFirstLine();
        graph.addEdge(parentNode, new AstNode(Integer.toString(gotoPosition), CMP));
        break;

      case "JReturnVoidStmt":
        {
          break;
        }

      default:
        throw new IllegalStateException("Unexpected value: " + stmt.getClass().getSimpleName());
    }
  }

  private static void addReturnStmtEdge(AstGraph graph, AstNode parentNode, Type returnType) {
    graph.addEdge(parentNode, new AstNode(returnType.toString(), RETURN_TYPE));
  }
}
