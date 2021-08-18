package de.upb.swt.soot.core.util;

import de.upb.swt.soot.core.graph.BasicBlock;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.net.URLEncoder;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Exports a StmtGraph into a GraphViz representation (see https://graphviz.org) to visualize the
 * Graph
 *
 * @author Markus Schmidt
 */
public class GraphVizExporter {

  public static String export(@Nonnull StmtGraph graph) {

    StringBuilder sb = new StringBuilder();
    sb.append("digraph G {\n")
        .append("\tcompound=true;\n")
        .append("\tstyle=filled;\n")
        .append("\tcolor=lightgrey;\n")
        .append("\tnode [shape=record, style=filled,color=white];\n\n");

    /* entrypoint */
    Stmt startingStmt = graph.getStartingStmt();
    if (startingStmt != null) {
      sb.append("\tstart [shape=Mdiamond, color=grey];\n");
      sb.append("\tstart -> ").append(graph.getStartingStmt().hashCode()).append(";\n");
    }
    sb.append("\n");

    /* print a block in a subgraph */
    int i = 0;
    for (BasicBlock block : graph.getBlocks()) {

      sb.append("\tsubgraph cluster_")
          .append(block.hashCode())
          .append(" { \n")
          .
          /* Label name */
          append("\t\tlabel = \"Block #")
          .append(++i)
          .append("\";\n");

      /* print stmts in a block*/
      List<Stmt> stmts = block.getStmts();
      for (Stmt stmt : stmts) {
        sb.append("\t\t")
            .append(stmt.hashCode())
            .append(" [label=\"")
            .append(escape(stmt.toString()))
            .append("\"]")
            .append(";\n");
      }
      if (stmts.size() > 1) {
        sb.append("\n\t\t");
        for (Stmt stmt : stmts) {
          sb.append(stmt.hashCode()).append(" -> ");
        }
        sb.delete(sb.length() - 4, sb.length());
        sb.append(";\n");
      }
      sb.append("\t}\n");

      /* add edges to other blocks */
      List<? extends BasicBlock> successors = block.getSuccessors();
      if (successors.size() > 0) {
        sb.append("\t//branching edges\n");
        for (BasicBlock successorBlock : successors) {
          sb.append("\t")
              .append(block.getTail().hashCode())
              .append(" -> ")
              .append(successorBlock.getHead().hashCode())
              //  .append(" [ltail=\"cluster_" + block.hashCode() + "\", lhead=\"cluster_" +
              // successorBlock.hashCode() + "\"]")
              .append(";\n");
        }
      }

      /* add exceptional edges */
      List<? extends BasicBlock> exceptionalSuccessors = block.getExceptionalSuccessors();
      if (exceptionalSuccessors.size() > 0) {
        sb.append("\t//exceptional edges \n");
        for (BasicBlock successorBlock : exceptionalSuccessors) {
          sb.append("\t")
              .append(block.getTail().hashCode())
              .append(" -> ")
              .append(successorBlock.getHead().hashCode())
              .append(" [color=red, ltail=\"cluster_" + block.hashCode() + "\"]")
              .append(";\n");
        }
      }

      sb.append("\n");
    }

    sb.append("}");
    return sb.toString();
  }

  private static String escape(String str) {
    // ", &, <, and >
    return StringEscapeUtils.escapeXml10(str);
  }

  public static String createUrlToWebeditor(@Nonnull StmtGraph graph) {
    return "http://magjac.com/graphviz-visual-editor/?dot=" + URLEncoder.encode(export(graph));
  }
}
