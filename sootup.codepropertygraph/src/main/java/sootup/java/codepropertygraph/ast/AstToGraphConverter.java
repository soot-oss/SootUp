package sootup.java.codepropertygraph.ast;

import static sootup.java.codepropertygraph.ast.AstNodeType.*;

import java.util.List;
import java.util.Set;
import sootup.core.jimple.common.stmt.Stmt;
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
    addBodyStmtEdges(graph, bodyStmtsNode, methodAst.getBodyStmts());
    addReturnStmtEdge(graph, rootNode, methodAst.getReturnType());

    return graph;
  }

  private static void addModifierEdges(AstGraph graph, AstNode rootNode, Set<Modifier> modifiers) {
    for (Modifier modifier : modifiers) {
      graph.addEdge(rootNode, new AstNode(modifier.name(), MODIFIER));
    }
  }

  private static void addParameterTypeEdges(
      AstGraph graph, AstNode rootNode, List<Type> parameterTypes) {
    for (Type parameterType : parameterTypes) {
      graph.addEdge(rootNode, new AstNode(parameterType.toString(), PARAMETER_TYPE));
    }
  }

  private static void addBodyStmtEdges(AstGraph graph, AstNode rootNode, List<Stmt> bodyStmts) {
    for (Stmt stmt : bodyStmts) {
      graph.addEdge(rootNode, new AstNode(stmt.toString(), STMT));
    }
  }

  private static void addReturnStmtEdge(AstGraph graph, AstNode rootNode, Type returnType) {
    graph.addEdge(rootNode, new AstNode(returnType.toString(), RETURN_TYPE));
  }
}
