package sootup.codepropertygraph.ast;

/*-
* #%L
* Soot - a J*va Optimization Framework
* %%
Copyright (C) 2024 Michael Youkeim, Stefan Schott and others
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

import java.util.List;
import java.util.Set;
import sootup.codepropertygraph.propertygraph.*;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.types.Type;

/**
 * This class is responsible for creating the Abstract Syntax Tree (AST) property graph for a given
 * Soot method.
 */
public class AstCreator {

  /**
   * Adds modifier edges to the graph builder.
   *
   * @param graphBuilder the property graph builder
   * @param parentNode the parent node
   * @param modifiers the set of method modifiers
   */
  private static void addModifierEdges(
      PropertyGraph.Builder graphBuilder,
      PropertyGraphNode parentNode,
      Set<MethodModifier> modifiers) {
    for (MethodModifier modifier : modifiers) {
      graphBuilder.addEdge(new ModifierAstEdge(parentNode, new ModifierGraphNode(modifier)));
    }
  }

  /**
   * Adds parameter type edges to the graph builder.
   *
   * @param graphBuilder the property graph builder
   * @param parentNode the parent node
   * @param parameterTypes the list of parameter types
   */
  private static void addParameterTypeEdges(
      PropertyGraph.Builder graphBuilder, PropertyGraphNode parentNode, List<Type> parameterTypes) {
    for (Type parameterType : parameterTypes) {
      graphBuilder.addEdge(new ParameterAstEdge(parentNode, new TypeGraphNode(parameterType)));
    }
  }

  /**
   * Adds body statement edges to the graph builder.
   *
   * @param graphBuilder the property graph builder
   * @param parentNode the parent node
   * @param bodyStmts the list of body statements
   */
  private static void addBodyStmtEdges(
      PropertyGraph.Builder graphBuilder, PropertyGraphNode parentNode, List<Stmt> bodyStmts) {
    for (Stmt stmt : bodyStmts) {
      AstStmtVisitor visitor = new AstStmtVisitor(graphBuilder, parentNode);
      stmt.accept(visitor);
    }
  }

  /**
   * Adds return statement edge to the graph builder.
   *
   * @param graphBuilder the property graph builder
   * @param parentNode the parent node
   * @param returnType the return type
   */
  private static void addReturnStmtEdge(
      PropertyGraph.Builder graphBuilder, PropertyGraphNode parentNode, Type returnType) {
    graphBuilder.addEdge(new ReturnTypeAstEdge(parentNode, new TypeGraphNode(returnType)));
  }

  /**
   * Creates the AST property graph for the given Soot method.
   *
   * @param method the Soot method
   * @return the AST property graph
   */
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
    addBodyStmtEdges(graphBuilder, bodyStmtsNode, method.getBody().getStmts());
    addReturnStmtEdge(graphBuilder, rootNode, method.getReturnType());

    return graphBuilder.build();
  }
}
