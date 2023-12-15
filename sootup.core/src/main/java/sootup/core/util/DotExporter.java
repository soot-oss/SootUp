package sootup.core.util;

import com.google.common.collect.Sets;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringEscapeUtils;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

/**
 * Exports a StmtGraph into a Dot representation (see https://graphviz.org) to visualize the Graph
 *
 * @author Markus Schmidt
 */
public class DotExporter {

  public static String buildGraph(
      @Nonnull StmtGraph<?> graph,
      boolean isICFG,
      Map<Integer, MethodSignature> calls,
      MethodSignature methodSignature) {

    // TODO: hint: use edge weight to have a better top->down code like linear layouting with
    // starting stmt at the top;
    // TODO: improvement: use dfs starting with startingstmt to have a more intuitive order of
    // blocks

    StringBuilder sb = new StringBuilder();

    boolean isAdded = false;

    if (!isICFG) {
      buildDiGraphObject(sb);
    }

    /* entrypoint */
    Stmt startingStmt = graph.getStartingStmt();

    /* print a block in a subgraph */
    int i = 0;

    Collection<? extends BasicBlock<?>> blocks;
    try {
      blocks = graph.getBlocksSorted();
    } catch (Exception e) {
      blocks = graph.getBlocks();
    }

    Set<BasicBlock<?>> drawnBlocks = Sets.newHashSetWithExpectedSize(blocks.size());

    for (BasicBlock<?> block : blocks) {

      sb.append("//  lines [")
          .append(block.getHead().getPositionInfo().getStmtPosition().getFirstLine())
          .append(": ")
          .append(block.getTail().getPositionInfo().getStmtPosition().getFirstLine())
          .append("] \n");

      sb.append("\tsubgraph cluster_")
          .append(block.hashCode())
          .append(" { \n")
          .append("\t\tlabel = \"Block #")
          .append(++i)
          .append("\"\n");

      /* print stmts in a block*/
      List<Stmt> stmts = block.getStmts();
      drawnBlocks.add(block);
      for (Stmt stmt : stmts) {
        sb.append("\t\t")
            .append(stmt.hashCode())
            .append("[label=\"")
            .append(escape(stmt.toString()))
            .append("\"");
        // mark startingstmt itself
        if (startingStmt == stmt || stmt.getExpectedSuccessorCount() == 0) {
          sb.append(",shape=Mdiamond,color=grey50,fillcolor=white");
        }
        sb.append("]\n");
      }

      // add blocks internal connection
      if (stmts.size() > 1) {
        sb.append("\n\t\t");
        for (Stmt stmt : stmts) {
          if (methodSignature != null && calls != null) {
            for (Map.Entry<Integer, MethodSignature> entry : calls.entrySet()) {
              int key = entry.getKey();
              MethodSignature value = entry.getValue();
              if (methodSignature.equals(value) && !isAdded) {
                sb.append(key).append(" -> ");
                isAdded = true;
              }
            }
          }
          sb.append(stmt.hashCode()).append(" -> ");
        }
        sb.delete(sb.length() - 4, sb.length());
        sb.append("\n");
      }
      sb.append("\t}\n");

      /* add edges to other blocks */
      List<? extends BasicBlock<?>> successors = block.getSuccessors();
      if (successors.size() > 0) {
        Stmt tailStmt = block.getTail();

        Iterator<String> labelIt;
        // build edge labels for branching stmts
        if (tailStmt instanceof BranchingStmt) {
          if (tailStmt instanceof JIfStmt) {
            labelIt = Arrays.asList("false", "true").iterator();
          } else if (tailStmt instanceof JSwitchStmt) {
            labelIt =
                ((JSwitchStmt) tailStmt).getValues().stream().map(s -> "case " + s).iterator();
          } else {
            labelIt = Collections.emptyIterator();
          }
        } else {
          labelIt = Collections.emptyIterator();
        }

        for (BasicBlock<?> successorBlock : successors) {
          sb.append("\t").append(tailStmt.hashCode());
          final boolean successorIsAlreadyDrawn = drawnBlocks.contains(successorBlock);
          if (successorIsAlreadyDrawn) {
            sb.append(":e -> ");
          } else {
            sb.append(":s -> ");
          }
          sb.append(successorBlock.getHead().hashCode()).append(":n");

          if (labelIt.hasNext()) {
            sb.append("[");
            if (labelIt.hasNext()) {
              sb.append("label=\"").append(labelIt.next()).append("\"");
            }
            sb.append("]");
          }
          //          sb.append("ltail=\"cluster_").append(block.hashCode()).append("\",
          // lhead=\"cluster_").append(successorBlock.hashCode()).append("\"]");
          sb.append("\n");
        }
      }

      /* add exceptional edges */
      Map<? extends ClassType, ? extends BasicBlock<?>> exceptionalSuccessors =
          block.getExceptionalSuccessors();
      if (exceptionalSuccessors.size() > 0) {
        sb.append("\t//exceptional edges \n");
        for (Map.Entry<? extends ClassType, ? extends BasicBlock<?>> successorBlock :
            exceptionalSuccessors.entrySet()) {
          sb.append("\t")
              .append(block.getTail().hashCode())
              .append(":e -> ")
              .append(successorBlock.getValue().getHead().hashCode())
              .append(":n [label=\"\t")
              .append(successorBlock.getKey().toString())
              .append("\"color=red,ltail=\"cluster_")
              .append(block.hashCode())
              .append("\"]\n");
        }
      }

      sb.append("\n");
    }

    if (!isICFG) return sb.append("}").toString();
    else return sb.toString();
  }

  private static String escape(String str) {
    // ", &, <, and >
    return StringEscapeUtils.escapeXml10(str);
  }

  public static StringBuilder buildDiGraphObject(StringBuilder sb) {
    sb.append("digraph G {\n")
        .append("\tcompound=true\n")
        .append("\tlabelloc=b\n")
        .append("\tstyle=filled\n")
        .append("\tcolor=gray90\n")
        .append("\tnode [shape=box,style=filled,color=white]\n")
        .append("\tedge [fontsize=10,arrowsize=1.5,fontcolor=grey40]\n")
        .append("\tfontsize=10\n\n");
    return sb;
  }

  public static String createUrlToWebeditor(@Nonnull StmtGraph<?> graph) {
    try {
      return "http://magjac.com/graphviz-visual-editor/?dot="
          + URLEncoder.encode(buildGraph(graph, false, null, null), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
