package sootup.java.codepropertygraph.ast;

import java.util.List;
import java.util.Set;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.types.Type;
import sootup.java.codepropertygraph.propertygraph.*;
import sootup.java.codepropertygraph.propertygraph.nodes.MethodGraphNode;

public class AstCreator {
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph graph = new AstPropertyGraph();

    MethodGraphNode rootNode = new MethodGraphNode(method);
    PropertyGraphNode modifiersNode = new SimpleGraphNode("Modifiers");
    PropertyGraphNode parametersTypesNode = new SimpleGraphNode("Parameters");
    PropertyGraphNode bodyStmtsNode = new SimpleGraphNode("Body");

    graph.addEdge(new PropertyGraphEdge(rootNode, modifiersNode, EdgeType.AST));
    graph.addEdge(new PropertyGraphEdge(rootNode, parametersTypesNode, EdgeType.AST));
    graph.addEdge(new PropertyGraphEdge(rootNode, bodyStmtsNode, EdgeType.AST));

    addModifierEdges(graph, modifiersNode, method.getModifiers());
    addParameterTypeEdges(graph, parametersTypesNode, method.getParameterTypes());
    addBodyStmtEdges(graph, bodyStmtsNode, method.getBody().getStmts(), method.getBody());
    addReturnStmtEdge(graph, rootNode, method.getReturnType());

    return graph;
  }

  private static void addModifierEdges(
      PropertyGraph graph, PropertyGraphNode parentNode, Set<MethodModifier> modifiers) {
    for (MethodModifier modifier : modifiers) {
      graph.addEdge(
          new PropertyGraphEdge(parentNode, new SimpleGraphNode(modifier.name()), EdgeType.AST));
    }
  }

  private static void addParameterTypeEdges(
      PropertyGraph graph, PropertyGraphNode parentNode, List<Type> parameterTypes) {
    for (Type parameterType : parameterTypes) {
      graph.addEdge(
          new PropertyGraphEdge(parentNode, new TypeGraphNode(parameterType), EdgeType.AST));
    }
  }

  private static void addBodyStmtEdges(
      PropertyGraph graph, PropertyGraphNode parentNode, List<Stmt> bodyStmts, Body body) {
    for (Stmt stmt : bodyStmts) {
      AstStmtVisitor visitor = new AstStmtVisitor(graph, parentNode, body);
      stmt.accept(visitor);
    }
  }

  private static void addReturnStmtEdge(
      PropertyGraph graph, PropertyGraphNode parentNode, Type returnType) {
    graph.addEdge(new PropertyGraphEdge(parentNode, new TypeGraphNode(returnType), EdgeType.AST));
  }
}
