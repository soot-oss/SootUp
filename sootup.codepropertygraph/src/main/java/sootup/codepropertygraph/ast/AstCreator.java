package sootup.codepropertygraph.ast;

import java.util.List;
import java.util.Set;
import sootup.codepropertygraph.propertygraph.*;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.types.Type;

public class AstCreator {
  private static void addModifierEdges(
      PropertyGraph.Builder graphBuilder,
      PropertyGraphNode parentNode,
      Set<MethodModifier> modifiers) {
    for (MethodModifier modifier : modifiers) {
      graphBuilder.addEdge(new ModifierAstEdge(parentNode, new ModifierGraphNode(modifier)));
    }
  }

  private static void addParameterTypeEdges(
      PropertyGraph.Builder graphBuilder, PropertyGraphNode parentNode, List<Type> parameterTypes) {
    for (Type parameterType : parameterTypes) {
      graphBuilder.addEdge(new ParameterAstEdge(parentNode, new TypeGraphNode(parameterType)));
    }
  }

  private static void addBodyStmtEdges(
      PropertyGraph.Builder graphBuilder,
      PropertyGraphNode parentNode,
      List<Stmt> bodyStmts,
      Body body) {
    for (Stmt stmt : bodyStmts) {
      AstStmtVisitor visitor = new AstStmtVisitor(graphBuilder, parentNode, body);
      stmt.accept(visitor);
    }
  }

  private static void addReturnStmtEdge(
      PropertyGraph.Builder graphBuilder, PropertyGraphNode parentNode, Type returnType) {
    graphBuilder.addEdge(new ReturnTypeAstEdge(parentNode, new TypeGraphNode(returnType)));
  }

  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph.Builder graphBuilder = new AstPropertyGraph.Builder();
    graphBuilder.setName("ast_" + method.getName());

    if (method.isAbstract() || method.isNative()) {
      return graphBuilder.build();
    }

    MethodGraphNode rootNode = new MethodGraphNode(method);
    PropertyGraphNode modifiersNode = new AggregateGraphNode("Modifiers");
    PropertyGraphNode parametersTypesNode = new AggregateGraphNode("Parameters");
    PropertyGraphNode bodyStmtsNode = new AggregateGraphNode("Body");

    graphBuilder.addEdge(new ModifierAstEdge(rootNode, modifiersNode));
    graphBuilder.addEdge(new ParameterAstEdge(rootNode, parametersTypesNode));
    graphBuilder.addEdge(new StmtAstEdge(rootNode, bodyStmtsNode));

    addModifierEdges(graphBuilder, modifiersNode, method.getModifiers());
    addParameterTypeEdges(graphBuilder, parametersTypesNode, method.getParameterTypes());
    addBodyStmtEdges(graphBuilder, bodyStmtsNode, method.getBody().getStmts(), method.getBody());
    addReturnStmtEdge(graphBuilder, rootNode, method.getReturnType());

    return graphBuilder.build();
  }
}
