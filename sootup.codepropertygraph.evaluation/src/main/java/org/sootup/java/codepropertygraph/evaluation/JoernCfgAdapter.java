package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.*;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Edge;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.util.*;
import org.sootup.java.codepropertygraph.evaluation.graph.comparison.converter.joern2sootup.NodeConverter;
import org.sootup.java.codepropertygraph.evaluation.graph.comparison.converter.joern2sootup.util.NodeTypeResolver;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernCfgGenerator;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.java.codepropertygraph.propertygraph.*;

public class JoernCfgAdapter {
  private final JoernCfgGenerator joernCfgGenerator;
  private final NodeConverter nodeConverter = new NodeConverter();
  private final NodeTypeResolver nodeTypeResolver = new NodeTypeResolver();

  public JoernCfgAdapter(JoernCfgGenerator joernCfgGenerator) {

    this.joernCfgGenerator = joernCfgGenerator;
  }

  public PropertyGraph getCfg(Graph joernCfg) {
    if (joernCfg.vertices().size() == 0) {
      return new PropertyGraph();
    }

    PropertyGraph cfgGraph = new PropertyGraph();

    List<Stmt> stmts = new ArrayList<>();
    List<Stmt[]> edges = new ArrayList<>();
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

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

      Stmt src = nodeConverter.convert(joernEdgeSrc);
      Stmt dst = nodeConverter.convert(joernEdgeDst);

      if (!stmts.contains(src)) stmts.add(src);
      if (!stmts.contains(dst)) stmts.add(dst);
      edges.add(new Stmt[] {src, dst});

      /*if (joernEdgeSrc instanceof Unknown
      && src.getName().startsWith("goto")
      && dst instanceof StmtPropertyGraphNode
      && ((StmtPropertyGraphNode) dst).getPositionInfo().getStmtPosition().getFirstLine()
          != ((Unknown) joernEdgeSrc).lineNumber().get()) continue;*/

      if (nodeTypeResolver.isGotoStatement(joernEdgeSrc)
          && ((Expression) joernEdgeDst).lineNumber().nonEmpty()
          && nodeConverter
                  .getGotoStmtTarget((Unknown) joernEdgeSrc)
                  .getStmtPosition()
                  .getFirstLine()
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
              + String.format("%-60s", (src != null ? src.toString() : null))
              + "   ====>   "
              + (dst != null ? dst.toString() : null));
      System.out.println("\t" + String.join("", Collections.nCopies(100, "-")));

      // cfgGraph.addEdge(new PropertyGraphEdge(src, dst, "CFG"));
    }

    Map<Stmt, Integer> counts = new HashMap<>();
    for (Stmt stmt : stmts) {
      counts.put(stmt, 0);
    }

    for (Stmt[] edge : edges) {
      Stmt source = edge[0];
      if (source instanceof JGotoStmt) {
        BranchingStmt branchingStmt = (BranchingStmt) source;
        graph.putEdge(branchingStmt, 0, edge[1]);
      } else if (source instanceof JIfStmt) {
        int count = counts.get(source);
        graph.putEdge((BranchingStmt) source, count, edge[1]);
        counts.put(source, count + 1);
      } else if (source instanceof JThrowStmt) {

      } else {
        graph.putEdge((FallsThroughStmt) source, edge[1]);
      }
    }

    Body.BodyBuilder builder = Body.builder(new MutableBlockStmtGraph(graph));

    Set<Local> locals = new HashSet<>();
    for (Stmt stmt : builder.getStmtGraph().getNodes()) {
      for (Value value : stmt.getUsesAndDefs()) {
        if (value instanceof Local) {
          Local local = (Local) value;
          locals.add(local);
        }
      }
    }
    builder.setLocals(locals);

    // new LocalRenamer().interceptBody(builder, null);
    new CustomCastAndReturnInliner().interceptBody(builder, null);
    new HashSuffixEliminator().interceptBody(builder, null);
    new DynamicInvokeNormalizer().interceptBody(builder, null);
    new SpecialInvokeNormalizer().interceptBody(builder, null);
    new InterfaceInvokeNormalizer().interceptBody(builder, null);

    for (Stmt stmt : builder.getStmts()) {
      for (Stmt successor : builder.getStmtGraph().getAllSuccessors(stmt)) {
        String srcName, dstName;

          srcName = stmt.toString();
          dstName = successor.toString();

        srcName = srcName.replace("\\\"", "");
        srcName = srcName.replace("\\'", "");
        srcName = srcName.replace("\\\\", "\\");

        dstName = dstName.replace("\\\"", "");
        dstName = dstName.replace("\\'", "");
        dstName = dstName.replace("\\\\", "\\");

        PropertyGraphEdge edge =
            new PropertyGraphEdge(
                new StmtPropertyGraphNode(srcName, NodeType.STMT, stmt.getPositionInfo(), stmt),
                new StmtPropertyGraphNode(
                    dstName, NodeType.STMT, successor.getPositionInfo(), successor),
                "CFG");
        cfgGraph.addEdge(edge);
        System.out.println("### " + srcName + " -> " + dstName);
      }
    }

    return cfgGraph;
  }
}
