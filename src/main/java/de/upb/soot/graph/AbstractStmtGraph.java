package de.upb.soot.graph;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.common.stmt.IStmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * Represents a CFG where the nodes are {@link IStmt} instances and edges represent unexceptional and (possibly) exceptional
 * control flow between <tt>IStmt</tt>s.
 * </p>
 *
 * <p>
 * This is an abstract class, providing the facilities used to build CFGs for specific purposes.
 * </p>
 */
public abstract class AbstractStmtGraph implements DirectedGraph<IStmt> {
  protected List<IStmt> heads;
  protected List<IStmt> tails;

  protected Map<IStmt, List<IStmt>> stmtToSuccs;
  protected Map<IStmt, List<IStmt>> stmtToPreds;
  protected SootMethod method;
  protected Body body;
  protected Collection<IStmt> orderedStmts;

  /**
   * Performs the work that is required to construct any sort of <tt>IStmtGraph</tt>.
   *
   * @param body
   *          The body of the method for which to construct a control flow graph.
   */
  protected AbstractStmtGraph(Body body) {
    this.body = body;
    orderedStmts = body.getStmts();
    method = body.getMethod();
  }

  /**
   * Utility method for <tt>IStmtGraph</tt> constructors. It computes the edges corresponding to unexceptional control flow.
   *
   * @param stmtToSuccs
   *          A {@link Map} from {@link IStmt}s to {@link List}s of {@link IStmt}s. This is an ``out parameter''; callers
   *          must pass an empty {@link Map}. <tt>buildUnexceptionalEdges</tt> will add a mapping for every <tt>IStmt</tt> in
   *          the body to a list of its unexceptional successors.
   *
   * @param stmtToPreds
   *          A {@link Map} from {@link IStmt}s to {@link List}s of {@link IStmt}s. This is an ``out parameter''; callers
   *          must pass an empty {@link Map}. <tt>buildUnexceptionalEdges</tt> will add a mapping for every <tt>IStmt</tt> in
   *          the body to a list of its unexceptional predecessors.
   */
  protected void buildUnexceptionalEdges(Map<IStmt, List<IStmt>> stmtToSuccs, Map<IStmt, List<IStmt>> stmtToPreds) {
    Iterator<IStmt> stmtIt = orderedStmts.iterator();
    IStmt currentIStmt, nextIStmt;

    nextIStmt = stmtIt.hasNext() ? (IStmt) stmtIt.next() : null;

    while (nextIStmt != null) {
      currentIStmt = nextIStmt;
      nextIStmt = stmtIt.hasNext() ? (IStmt) stmtIt.next() : null;

      ArrayList<IStmt> successors = new ArrayList<IStmt>();

      if (currentIStmt.fallsThrough()) {
        // Add the next stmt as the successor
        if (nextIStmt != null) {
          successors.add(nextIStmt);

          List<IStmt> preds = stmtToPreds.get(nextIStmt);
          if (preds == null) {
            preds = new ArrayList<IStmt>();
            stmtToPreds.put(nextIStmt, preds);
          }
          preds.add(currentIStmt);
        }
      }

      if (currentIStmt.branches()) {
        for (IStmtBox targetBox : currentIStmt.getStmtBoxes()) {
          IStmt target = targetBox.getStmt();
          // Arbitrary bytecode can branch to the same
          // target it falls through to, so we screen for duplicates:
          if (!successors.contains(target)) {
            successors.add(target);

            List<IStmt> preds = stmtToPreds.get(target);
            if (preds == null) {
              preds = new ArrayList<IStmt>();
              stmtToPreds.put(target, preds);
            }
            preds.add(currentIStmt);
          }
        }
      }

      // Store away successors
      if (!successors.isEmpty()) {
        successors.trimToSize();
        stmtToSuccs.put(currentIStmt, successors);
      }
    }
  }

  /**
   * <p>
   * Utility method used in the construction of {@link IStmtGraph}s, to be called only after the stmtToPreds and stmtToSuccs
   * maps have been built.
   * </p>
   *
   * <p>
   * <code>IStmtGraph</code> provides an implementation of <code>buildHeadsAndTails()</code> which defines the graph's set of
   * heads to include the first {@link IStmt} in the graph's body, together with any other <tt>IStmt</tt> which has no
   * predecessors. It defines the graph's set of tails to include all <tt>IStmt</tt>s with no successors. Subclasses of
   * <code>IStmtGraph</code> may override this method to change the criteria for classifying a node as a head or tail.
   * </p>
   */
  protected void buildHeadsAndTails() {
    tails = new ArrayList<IStmt>();
    heads = new ArrayList<IStmt>();

    for (IStmt s : orderedStmts) {
      List<IStmt> succs = stmtToSuccs.get(s);
      if (succs == null || succs.isEmpty()) {
        tails.add(s);
      }
      List<IStmt> preds = stmtToPreds.get(s);
      if (preds == null || preds.isEmpty()) {
        heads.add(s);
      }
    }

    // Add the first IStmt, even if it is the target of
    // a branch.
    if (!orderedStmts.isEmpty()) {
      IStmt entryPoint = orderedStmts.iterator().next();
      if (!heads.contains(entryPoint)) {
        heads.add(entryPoint);
      }
    }

  }

  /**
   * Utility method that produces a new map from the {@link IStmt}s of this graph's body to the union of the values stored in
   * the two argument {@link Map}s, used to combine the maps of exceptional and unexceptional predecessors and successors
   * into maps of all predecessors and successors. The values stored in both argument maps must be {@link List}s of
   * {@link IStmt}s, which are assumed not to contain any duplicate <tt>IStmt</tt>s.
   *
   * @param mapA
   *          The first map to be combined.
   *
   * @param mapB
   *          The second map to be combined.
   */
  protected Map<IStmt, List<IStmt>> combineMapValues(Map<IStmt, List<IStmt>> mapA, Map<IStmt, List<IStmt>> mapB) {
    // The duplicate screen
    Map<IStmt, List<IStmt>> result = new HashMap<IStmt, List<IStmt>>(mapA.size() * 2 + 1, 0.7f);
    for (IStmt stmt : orderedStmts) {
      List<IStmt> listA = mapA.get(stmt);
      if (listA == null) {
        listA = Collections.emptyList();
      }
      List<IStmt> listB = mapB.get(stmt);
      if (listB == null) {
        listB = Collections.emptyList();
      }

      int resultSize = listA.size() + listB.size();
      if (resultSize == 0) {
        result.put(stmt, Collections.<IStmt>emptyList());
      } else {
        List<IStmt> resultList = new ArrayList<IStmt>(resultSize);
        List<IStmt> list = null;
        // As a minor optimization of the duplicate screening,
        // copy the longer list first.
        if (listA.size() >= listB.size()) {
          resultList.addAll(listA);
          list = listB;
        } else {
          resultList.addAll(listB);
          list = listA;
        }
        for (IStmt element : list) {
          // It is possible for there to be both an exceptional
          // and an unexceptional edge connecting two IStmts
          // (though probably not in a class generated by
          // javac), so we need to screen for duplicates. On the
          // other hand, we expect most of these lists to have
          // only one or two elements, so it doesn't seem worth
          // the cost to build a Set to do the screening.
          if (!resultList.contains(element)) {
            resultList.add(element);
          }
        }
        result.put(stmt, resultList);
      }
    }
    return result;
  }

  /**
   * Utility method for adding an edge to maps representing the CFG.
   *
   * @param stmtToSuccs
   *          The {@link Map} from {@link IStmt}s to {@link List}s of their successors.
   *
   * @param stmtToPreds
   *          The {@link Map} from {@link IStmt}s to {@link List}s of their successors.
   *
   * @param head
   *          The {@link IStmt} from which the edge starts.
   *
   * @param tail
   *          The {@link IStmt} to which the edge flows.
   */
  protected void addEdge(Map<IStmt, List<IStmt>> stmtToSuccs, Map<IStmt, List<IStmt>> stmtToPreds, IStmt head, IStmt tail) {
    List<IStmt> headsSuccs = stmtToSuccs.get(head);
    if (headsSuccs == null) {
      headsSuccs = new ArrayList<IStmt>(3); // We expect this list to
      // remain short.
      stmtToSuccs.put(head, headsSuccs);
    }
    if (!headsSuccs.contains(tail)) {
      headsSuccs.add(tail);
      List<IStmt> tailsPreds = stmtToPreds.get(tail);
      if (tailsPreds == null) {
        tailsPreds = new ArrayList<IStmt>();
        stmtToPreds.put(tail, tailsPreds);
      }
      tailsPreds.add(head);
    }
  }

  /**
   * @return The body from which this IStmtGraph was built.
   *
   * @see Body
   */
  public Body getBody() {
    return body;
  }

  /**
   * Look for a path in graph, from def to use. This path has to lie inside an extended basic block (and this property
   * implies uniqueness.). The path returned includes from and to.
   *
   * @param from
   *          start point for the path.
   * @param to
   *          end point for the path.
   * @return null if there is no such path.
   */
  public List<IStmt> getExtendedBasicBlockPathBetween(IStmt from, IStmt to) {
    AbstractStmtGraph g = this;

    // if this holds, we're doomed to failure!!!
    if (g.getPredsOf(to).size() > 1) {
      return null;
    }

    // pathStack := list of succs lists
    // pathStackIndex := last visited index in pathStack
    LinkedList<IStmt> pathStack = new LinkedList<IStmt>();
    LinkedList<Integer> pathStackIndex = new LinkedList<Integer>();

    pathStack.add(from);
    pathStackIndex.add(0);

    int psiMax = (g.getSuccsOf(pathStack.get(0))).size();
    int level = 0;
    while (pathStackIndex.get(0) != psiMax) {
      int p = pathStackIndex.get(level);

      List<IStmt> succs = g.getSuccsOf((pathStack.get(level)));
      if (p >= succs.size()) {
        // no more succs - backtrack to previous level.

        pathStack.remove(level);
        pathStackIndex.remove(level);

        level--;
        int q = pathStackIndex.get(level);
        pathStackIndex.set(level, q + 1);
        continue;
      }

      IStmt betweenIStmt = (succs.get(p));

      // we win!
      if (betweenIStmt == to) {
        pathStack.add(to);
        return pathStack;
      }

      // check preds of betweenIStmt to see if we should visit its kids.
      if (g.getPredsOf(betweenIStmt).size() > 1) {
        pathStackIndex.set(level, p + 1);
        continue;
      }

      // visit kids of betweenIStmt.
      level++;
      pathStackIndex.add(0);
      pathStack.add(betweenIStmt);
    }
    return null;
  }

  /* DirectedGraph implementation */
  @Override
  public List<IStmt> getHeads() {
    return heads;
  }

  @Override
  public List<IStmt> getTails() {
    return tails;
  }

  @Override
  public List<IStmt> getPredsOf(IStmt u) {
    List<IStmt> l = stmtToPreds.get(u);
    if (l == null) {
      return Collections.emptyList();
    }

    return l;
  }

  @Override
  public List<IStmt> getSuccsOf(IStmt u) {
    List<IStmt> l = stmtToSuccs.get(u);
    if (l == null) {
      return Collections.emptyList();
    }

    return l;
  }

  @Override
  public int size() {
    return orderedStmts.size();
  }

  @Override
  public Iterator<IStmt> iterator() {
    return orderedStmts.iterator();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    for (IStmt u : orderedStmts) {
      buf.append("// preds: ").append(getPredsOf(u)).append('\n');
      buf.append(u).append('\n');
      buf.append("// succs ").append(getSuccsOf(u)).append('\n');
    }
    return buf.toString();
  }
}
