package de.upb.soot.graph;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.common.stmt.Stmt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a CFG where the nodes are {@link Stmt} instances and edges represent unexceptional and
 * (possibly) exceptional control flow between <tt>Stmt</tt>s.
 *
 * <p>This is an abstract class, providing the facilities used to build CFGs for specific purposes.
 */
public abstract class AbstractStmtGraph implements DirectedGraph<Stmt> {
  protected List<Stmt> heads;
  protected List<Stmt> tails;

  protected Map<Stmt, List<Stmt>> stmtToSuccs;
  protected Map<Stmt, List<Stmt>> stmtToPreds;
  protected SootMethod method;
  protected Body body;
  protected Collection<Stmt> orderedStmts;

  /**
   * Performs the work that is required to construct any sort of <tt>IStmtGraph</tt>.
   *
   * @param body The body of the methodRef for which to construct a control flow graph.
   */
  protected AbstractStmtGraph(Body body) {
    this.body = body;
    orderedStmts = body.getStmts();
    method = body.getMethod();
  }

  /**
   * Utility methodRef for <tt>IStmtGraph</tt> constructors. It computes the edges corresponding to
   * unexceptional control flow.
   *
   * @param stmtToSuccs A {@link Map} from {@link Stmt}s to {@link List}s of {@link Stmt}s. This is
   *     an ``out parameter''; callers must pass an empty {@link Map}.
   *     <tt>buildUnexceptionalEdges</tt> will add a mapping for every <tt>Stmt</tt> in the body to
   *     a list of its unexceptional successors.
   * @param stmtToPreds A {@link Map} from {@link Stmt}s to {@link List}s of {@link Stmt}s. This is
   *     an ``out parameter''; callers must pass an empty {@link Map}.
   *     <tt>buildUnexceptionalEdges</tt> will add a mapping for every <tt>Stmt</tt> in the body to
   *     a list of its unexceptional predecessors.
   */
  protected void buildUnexceptionalEdges(
      Map<Stmt, List<Stmt>> stmtToSuccs, Map<Stmt, List<Stmt>> stmtToPreds) {
    Iterator<Stmt> stmtIt = orderedStmts.iterator();
    Stmt currentStmt, nextStmt;

    nextStmt = stmtIt.hasNext() ? stmtIt.next() : null;

    while (nextStmt != null) {
      currentStmt = nextStmt;
      nextStmt = stmtIt.hasNext() ? stmtIt.next() : null;

      ArrayList<Stmt> successors = new ArrayList<>();

      if (currentStmt.fallsThrough()) {
        // Add the next stmt as the successor
        if (nextStmt != null) {
          successors.add(nextStmt);

          List<Stmt> preds = stmtToPreds.computeIfAbsent(nextStmt, k -> new ArrayList<>());
          preds.add(currentStmt);
        }
      }

      if (currentStmt.branches()) {
        for (StmtBox targetBox : currentStmt.getStmtBoxes()) {
          Stmt target = targetBox.getStmt();
          // Arbitrary bytecode can branch to the same
          // target it falls through to, so we screen for duplicates:
          if (!successors.contains(target)) {
            successors.add(target);

            List<Stmt> preds = stmtToPreds.computeIfAbsent(target, k -> new ArrayList<>());
            preds.add(currentStmt);
          }
        }
      }

      // Store away successors
      if (!successors.isEmpty()) {
        successors.trimToSize();
        stmtToSuccs.put(currentStmt, successors);
      }
    }
  }

  /**
   * Utility methodRef used in the construction of {@link IStmtGraph}s, to be called only after the
   * stmtToPreds and stmtToSuccs maps have been built.
   *
   * <p><code>IStmtGraph</code> provides an implementation of <code>buildHeadsAndTails()</code>
   * which defines the graph's set of heads to include the first {@link Stmt} in the graph's body,
   * together with any other <tt>Stmt</tt> which has no predecessors. It defines the graph's set of
   * tails to include all <tt>Stmt</tt>s with no successors. Subclasses of <code>IStmtGraph</code>
   * may override this methodRef to change the criteria for classifying a node as a head or tail.
   */
  protected void buildHeadsAndTails() {
    tails = new ArrayList<>();
    heads = new ArrayList<>();

    for (Stmt s : orderedStmts) {
      List<Stmt> succs = stmtToSuccs.get(s);
      if (succs == null || succs.isEmpty()) {
        tails.add(s);
      }
      List<Stmt> preds = stmtToPreds.get(s);
      if (preds == null || preds.isEmpty()) {
        heads.add(s);
      }
    }

    // Add the first Stmt, even if it is the target of
    // a branch.
    if (!orderedStmts.isEmpty()) {
      Stmt entryPoint = orderedStmts.iterator().next();
      if (!heads.contains(entryPoint)) {
        heads.add(entryPoint);
      }
    }
  }

  /**
   * Utility methodRef that produces a new map from the {@link Stmt}s of this graph's body to the
   * union of the values stored in the two argument {@link Map}s, used to combine the maps of
   * exceptional and unexceptional predecessors and successors into maps of all predecessors and
   * successors. The values stored in both argument maps must be {@link List}s of {@link Stmt}s,
   * which are assumed not to contain any duplicate <tt>Stmt</tt>s.
   *
   * @param mapA The first map to be combined.
   * @param mapB The second map to be combined.
   */
  protected Map<Stmt, List<Stmt>> combineMapValues(
      Map<Stmt, List<Stmt>> mapA, Map<Stmt, List<Stmt>> mapB) {
    // The duplicate screen
    Map<Stmt, List<Stmt>> result = new HashMap<>(mapA.size() * 2 + 1, 0.7f);
    for (Stmt stmt : orderedStmts) {
      List<Stmt> listA = mapA.get(stmt);
      if (listA == null) {
        listA = Collections.emptyList();
      }
      List<Stmt> listB = mapB.get(stmt);
      if (listB == null) {
        listB = Collections.emptyList();
      }

      int resultSize = listA.size() + listB.size();
      if (resultSize == 0) {
        result.put(stmt, Collections.emptyList());
      } else {
        List<Stmt> resultList = new ArrayList<>(resultSize);
        List<Stmt> list;
        // As a minor optimization of the duplicate screening,
        // copy the longer list first.
        if (listA.size() >= listB.size()) {
          resultList.addAll(listA);
          list = listB;
        } else {
          resultList.addAll(listB);
          list = listA;
        }
        for (Stmt element : list) {
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
   * Utility methodRef for adding an edge to maps representing the CFG.
   *
   * @param stmtToSuccs The {@link Map} from {@link Stmt}s to {@link List}s of their successors.
   * @param stmtToPreds The {@link Map} from {@link Stmt}s to {@link List}s of their successors.
   * @param head The {@link Stmt} from which the edge starts.
   * @param tail The {@link Stmt} to which the edge flows.
   */
  protected void addEdge(
      Map<Stmt, List<Stmt>> stmtToSuccs, Map<Stmt, List<Stmt>> stmtToPreds, Stmt head, Stmt tail) {
    List<Stmt> headsSuccs = stmtToSuccs.computeIfAbsent(head, k -> new ArrayList<>(3));
    // We expect this list to
    // remain short.
    if (!headsSuccs.contains(tail)) {
      headsSuccs.add(tail);
      List<Stmt> tailsPreds = stmtToPreds.computeIfAbsent(tail, k -> new ArrayList<>());
      tailsPreds.add(head);
    }
  }

  /**
   * @return The body from which this IStmtGraph was built.
   * @see Body
   */
  public Body getBody() {
    return body;
  }

  /**
   * Look for a path in graph, from def to use. This path has to lie inside an extended basic block
   * (and this property implies uniqueness.). The path returned includes from and to.
   *
   * @param from start point for the path.
   * @param to end point for the path.
   * @return null if there is no such path.
   */
  public List<Stmt> getExtendedBasicBlockPathBetween(Stmt from, Stmt to) {
    AbstractStmtGraph g = this;

    // if this holds, we're doomed to failure!!!
    if (g.getPredsOf(to).size() > 1) {
      return null;
    }

    // pathStack := list of succs lists
    // pathStackIndex := last visited index in pathStack
    List<Stmt> pathStack = new ArrayList<>();
    List<Integer> pathStackIndex = new ArrayList<>();

    pathStack.add(from);
    pathStackIndex.add(0);

    int psiMax = (g.getSuccsOf(pathStack.get(0))).size();
    int level = 0;
    while (pathStackIndex.get(0) != psiMax) {
      int p = pathStackIndex.get(level);

      List<Stmt> succs = g.getSuccsOf((pathStack.get(level)));
      if (p >= succs.size()) {
        // no more succs - backtrack to previous level.

        pathStack.remove(level);
        pathStackIndex.remove(level);

        level--;
        int q = pathStackIndex.get(level);
        pathStackIndex.set(level, q + 1);
        continue;
      }

      Stmt betweenStmt = (succs.get(p));

      // we win!
      if (betweenStmt == to) {
        pathStack.add(to);
        return pathStack;
      }

      // check preds of betweenStmt to see if we should visit its kids.
      if (g.getPredsOf(betweenStmt).size() > 1) {
        pathStackIndex.set(level, p + 1);
        continue;
      }

      // visit kids of betweenStmt.
      level++;
      pathStackIndex.add(0);
      pathStack.add(betweenStmt);
    }
    return null;
  }

  /* DirectedGraph implementation */
  @Override
  public List<Stmt> getHeads() {
    return heads;
  }

  @Override
  public List<Stmt> getTails() {
    return tails;
  }

  @Override
  public List<Stmt> getPredsOf(Stmt u) {
    List<Stmt> l = stmtToPreds.get(u);
    if (l == null) {
      return Collections.emptyList();
    }

    return l;
  }

  @Override
  public List<Stmt> getSuccsOf(Stmt u) {
    List<Stmt> l = stmtToSuccs.get(u);
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
  public Iterator<Stmt> iterator() {
    return orderedStmts.iterator();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    for (Stmt u : orderedStmts) {
      buf.append("// preds: ").append(getPredsOf(u)).append('\n');
      buf.append(u).append('\n');
      buf.append("// succs ").append(getSuccsOf(u)).append('\n');
    }
    return buf.toString();
  }
}
