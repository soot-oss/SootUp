package de.upb.swt.soot.core.util;

import de.upb.swt.soot.core.graph.BasicBlock;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
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

    // TODO: hint: use edge weight to have a better top->down code like linear layouting with
    // starting stmt at the top;
    // TODO: improvement: use dfs starting with startingstmt to have a more intuitive order of
    // blocks

    StringBuilder sb = new StringBuilder();
    sb.append("digraph G {\n")
        .append("\tcompound=true;\n")
        .append("\tlabelloc=b;\n")
        .append("\tstyle=filled;\n")
        .append("\tcolor=gray90;\n")
        .append("\tnode [shape=box, style=filled, color=white];\n")
        .append("\tedge [fontsize=10, fontcolor=grey40]\n")
        .append("\tfontsize=10\n\n");

    /* entrypoint */
    Stmt startingStmt = graph.getStartingStmt();
    /*
    if (startingStmt != null) {
        sb.append("\tstart [shape=Mdiamond, color=grey80];\n");
        BasicBlock startingStmtBlock =
                graph.getBlocks().stream().filter(b -> b.getHead() == startingStmt).findFirst().get();
        sb.append("\tstart:s -> ")
                .append(graph.getStartingStmt().hashCode())
                .append(":n [lhead=\"cluster_" + startingStmtBlock.hashCode() + "\"];\n");
    }
    sb.append("\n");
    */

    /* print a block in a subgraph */
    int i = 0;
    for (BasicBlock block : graph.getBlocks()) {

      sb.append("\tsubgraph cluster_")
          .append(block.hashCode())
          .append(" { \n")
          .append("\t\tlabel = \"Block #")
          .append(++i)
          .append("\";\n");

      /* print stmts in a block*/
      List<Stmt> stmts = block.getStmts();
      for (Stmt stmt : stmts) {
        sb.append("\t\t")
            .append(stmt.hashCode())
            .append("[label=\"")
            .append(escape(stmt.toString()))
            .append("\"");
        // mark startingstmt itself
        if (startingStmt == stmt) {
          sb.append("shape=Mdiamond, color=grey50, fillcolor=white");
        }
        sb.append("];\n");
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
        Stmt tailStmt = block.getTail();

        Iterator<String> labelIt = null;
        // build edge labels for branching stmts
        if (tailStmt instanceof BranchingStmt) {
          if (tailStmt instanceof JIfStmt) {
            labelIt = Arrays.asList("false", "true").iterator();
          } else if (tailStmt instanceof JSwitchStmt) {
            labelIt =
                ((JSwitchStmt) tailStmt).getValues().stream().map(s -> "case " + s).iterator();
          }
          // TODO: [ms] JGoto feels still odd in the StmtGraph representation

        }

        for (BasicBlock successorBlock : successors) {
          sb.append("\t")
              .append(tailStmt.hashCode())
              .append(":s -> ")
              .append(successorBlock.getHead().hashCode())
              .append(":n");

          if (labelIt != null) {
            if (labelIt.hasNext()) {
              sb.append("[");
              sb.append("label=\"").append(labelIt.next()).append("\" ");
              sb.append("]");
            } else {
              System.err.println(
                  "invalid StmtGraph! At least one successor of "
                      + successorBlock.getTail()
                      + " is missing");
            }
          }
          //          sb.append("ltail=\"cluster_").append(block.hashCode()).append("\",
          // lhead=\"cluster_").append(successorBlock.hashCode()).append("\"]");
          sb.append(";\n");
        }
      }

      /* add exceptional edges */
      List<? extends BasicBlock> exceptionalSuccessors = block.getExceptionalSuccessors();
      if (exceptionalSuccessors.size() > 0) {
        sb.append("\t//exceptional edges \n");
        for (BasicBlock successorBlock : exceptionalSuccessors) {
          sb.append("\t")
              .append(block.getTail().hashCode())
              .append(":e -> ")
              // TODO: [ms] add exception label with signature
              .append(successorBlock.getHead().hashCode())
              .append(":n [color=red, ltail=\"cluster_")
              .append(block.hashCode())
              .append("\"]")
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
