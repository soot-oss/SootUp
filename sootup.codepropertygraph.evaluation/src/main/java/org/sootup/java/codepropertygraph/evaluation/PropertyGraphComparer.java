package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.*;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernCfgGenerator;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCfgGenerator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphEdge;
import sootup.java.codepropertygraph.propertygraph.StmtPropertyGraphNode;

public class PropertyGraphComparer {
  private final JoernCfgGenerator joernCfgGenerator;
  private final SootUpCfgGenerator sootUpCfgGenerator;
  private final JoernCfgAdapter joernCfgAdapter;

  public PropertyGraphComparer(
      JoernCfgGenerator joernCfgGenerator, SootUpCfgGenerator sootUpCfgGenerator) {
    this.joernCfgGenerator = joernCfgGenerator;
    this.sootUpCfgGenerator = sootUpCfgGenerator;
    joernCfgAdapter = new JoernCfgAdapter(joernCfgGenerator);
  }

  public boolean compareCfg(Graph joernCfg, Graph joernAst, PropertyGraph sootupCfg) {
    // System.out.println("-< " + joernCfg.edges().size());
    // System.out.println("-> " + sootupCfg.getEdges().size());
    // System.out.println();

    PropertyGraph joernPropertyGraph = joernCfgAdapter.getCfg(joernCfg, joernAst);

    for (PropertyGraphEdge e : joernPropertyGraph.getEdges()) {
        if (sootupCfg.getEdges().contains(e)) throw new RuntimeException("ACHIEVED !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      for (PropertyGraphEdge otherE : sootupCfg.getEdges()) {
        if (e.getSource() == null || e.getDestination() == null || otherE.getSource() == null || otherE.getDestination() == null) continue;
        System.out.printf("Comparing: %n\t%s -> %s%n\t%s -> %s%n", e.getSource().getName(), e.getDestination().getName(), otherE.getSource().getName(), otherE.getDestination().getName());
        /*if (((StmtPropertyGraphNode) otherE.getSource()).getName().toString().equals(e.getSource().getName().toString())) {
          throw new RuntimeException("The other way .... !!!!!!!!!");
        }*/

        String s1 = String.format("%s -> %s", e.getSource().getName(), e.getDestination().getName());
        String s2 = String.format("%s -> %s", otherE.getSource().getName(), otherE.getDestination().getName());
        if (s1.equals(s2)) { throw new RuntimeException("Should be equal !!!!!!!!!!!!!");}
      }
    }

    for (StoredNode node : joernCfgGenerator.getGraphVertices(joernCfg)) {
      System.out.println(node.getClass());
      if (node instanceof Block) {
        System.out.println("###: Block");
        Block block = (Block) node;
        System.out.println(block.label());
      } else if (node instanceof Identifier) {
        System.out.println("###: Identifier");
        Identifier identifier = (Identifier) node;
        System.out.printf("%s %s%n", identifier.typeFullName(), identifier.name());
      } else if (node instanceof Method) {
        System.out.println("###: Method");
        Method method = (Method) node;
        System.out.println(method.fullName());
      } else if (node instanceof JumpTarget) {
        System.out.println("###: JumpTarget");
        JumpTarget jumpTarget = (JumpTarget) node;
        System.out.println(jumpTarget.name());
      } else if (node instanceof Modifier) {
        System.out.println("###: Modifier");
        Modifier modifier = (Modifier) node;
        System.out.println(modifier.modifierType());
      } else if (node instanceof Type) {
        System.out.println("###: Type");
        Type type = (Type) node;
        System.out.println(type.fullName());
      } else if (node instanceof Call) {
        System.out.println("###: Call");
        Call call = (Call) node;
        System.out.println(call.code());
      } else if (node instanceof Expression) {
        System.out.println("###: Expression");
        Expression expr = (Expression) node;
        System.out.println(expr.code());
      } else if (node instanceof MethodReturn) {
        System.out.println("###: MethodReturn");
        MethodReturn methodReturn = (MethodReturn) node;
        System.out.println(methodReturn.code());
      } else if (node instanceof MethodParameterIn) {
        System.out.println("###: MethodParameterIn");
        MethodParameterIn MethodParameterIn = (MethodParameterIn) node;
        System.out.println(MethodParameterIn.code());
      } else if (node instanceof Local) {
        System.out.println("###: Local");
        Local local = (Local) node;
        System.out.println(local.code());
      } else if (node instanceof Member) {
        System.out.println("###: Member");
        Member member = (Member) node;
        System.out.println(member.code());
      } else if (node instanceof TypeArgument) {
        System.out.println("###: TypeArgument");
        TypeArgument typeArgument = (TypeArgument) node;
        System.out.println(typeArgument.code());
      } else if (node instanceof TypeDecl) {
        System.out.println("###: TypeDecl");
        TypeDecl typeDecl = (TypeDecl) node;
        System.out.println(typeDecl.code());
      }

      System.out.println(node.underlying().propertiesMap());
      System.out.println();
    }

    return true;
  }
}
