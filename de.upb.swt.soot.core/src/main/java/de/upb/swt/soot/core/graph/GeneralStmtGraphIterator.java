package de.upb.swt.soot.core.graph;

import com.google.common.graph.EndpointPair;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.Iterator;
import javax.annotation.Nonnull;

/**
 * Iterates over all Edges of the Graph
 *
 * @author Markus Schmidt
 */
public class GeneralStmtGraphIterator implements Iterator<EndpointPair<Stmt>> {

  private final GeneralStmtGraph generalStmtGraph;
  private final Iterator<Stmt> nodeIterator;
  private Iterator<Stmt> edgeIterator;
  private Stmt currentNode = null;

  public GeneralStmtGraphIterator(@Nonnull GeneralStmtGraph generalStmtGraph) {
    this.generalStmtGraph = generalStmtGraph;
    this.nodeIterator = generalStmtGraph.stmtList.iterator();
  }

  public static GeneralStmtGraphIterator of(@Nonnull GeneralStmtGraph generalStmtGraph) {
    return new GeneralStmtGraphIterator(generalStmtGraph);
  }

  @Override
  public boolean hasNext() {
    return edgeIterator.hasNext() || nodeIterator.hasNext();
  }

  @Override
  public EndpointPair<Stmt> next() {
    // TODO: [ms] check!

    if (currentNode == null && nodeIterator.hasNext()) {
      currentNode = nodeIterator.next();
      edgeIterator = generalStmtGraph.successors(currentNode).iterator();
    }
    if (currentNode != null) {
      if (edgeIterator.hasNext()) {
        return EndpointPair.ordered(currentNode, edgeIterator.next());
      } else {
        return next();
      }
    }

    throw new IndexOutOfBoundsException("No more element to iterate.");
  }
}
