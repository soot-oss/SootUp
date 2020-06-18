package de.upb.swt.soot.core.model;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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

import com.google.common.graph.*;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.ref.JThisRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.EscapedWriter;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.util.printer.Printer;
import de.upb.swt.soot.core.validation.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class that models the Jimple body (code attribute) of a method.
 *
 * @author Linghui Luo
 */
public class Body implements Copyable {

  public static final Body EMPTY_BODY =
      new Body(
          Collections.emptySet(),
          Collections.emptyList(),
          ImmutableGraph.copyOf(GraphBuilder.directed().build()),
          Collections.emptyMap(),
          NoPositionInformation.getInstance());

  /** The locals for this Body. */
  private final Set<Local> locals;

  /** The traps for this Body. */
  private final List<Trap> traps;

  /** The stmts for this Body. */
  @Nonnull private final ImmutableGraph<Stmt> cfg;

  /** Record the ordered branching edges for each branching statement. */
  @Nonnull private Map<Stmt, List<Stmt>> branches;

  /** The first Stmt in this Body. */
  @Nonnull private final Stmt firstStmt;

  /** The Position Information in the Source for this Body. */
  @Nonnull private final Position position;

  /** The method associated with this Body. */
  @Nonnull private MethodSignature methodSignature;

  /** An array containing some validators in order to validate the JimpleBody */
  @Nonnull
  private static final List<BodyValidator> validators =
      ImmutableUtils.immutableList(
          new LocalsValidator(),
          new TrapsValidator(),
          new StmtBoxesValidator(),
          new UsesValidator(),
          new ValueBoxesValidator(),
          new CheckInitValidator(),
          new CheckTypesValidator(),
          new CheckVoidLocalesValidator(),
          new CheckEscapingValidator());

  /**
   * Creates an body which is not associated to any method.
   *
   * @param locals please use {@link LocalGenerator} to generate local for a body.
   * @param startingStmt
   */
  public Body(
      @Nonnull MethodSignature methodSignature,
      @Nonnull Set<Local> locals,
      @Nonnull List<Trap> traps,
      @Nonnull Graph<Stmt> stmtGraph,
      @Nonnull Map<Stmt, List<Stmt>> branches,
      @Nonnull Stmt startingStmt,
      @Nonnull Position position) {
    this.methodSignature = methodSignature;
    this.locals = Collections.unmodifiableSet(locals);
    this.traps = Collections.unmodifiableList(traps);
    // TODO: [ms] (im)mutability via second constructor?
    this.cfg =
        stmtGraph instanceof ImmutableGraph
            ? (ImmutableGraph<Stmt>) stmtGraph
            : ImmutableGraph.copyOf(stmtGraph);
    this.branches = branches;
    this.position = position;
    this.firstStmt = startingStmt;

    // FIXME: [JMP] Virtual method call in constructor
    checkInit();
  }

  // TODO: migrate tests to use BodyBuilder
  @Deprecated
  public Body(
      @Nonnull Set<Local> locals,
      @Nonnull List<Trap> traps,
      @Nonnull Graph<Stmt> stmtGraph,
      @Nonnull Map<Stmt, List<Stmt>> branches,
      @Nonnull Position position) {

    // FIXME: [ms] remove this dirty test hack !!!!!!
    this(
        null, // will be removed anyways
        locals,
        traps,
        stmtGraph,
        branches,
        stmtGraph.nodes().iterator().hasNext() ? stmtGraph.nodes().iterator().next() : null,
        position);
  }

  @Nonnull
  public static Body getNoBody() {
    return EMPTY_BODY;
  }

  /**
   * Returns the MethodSignature associated with this Body.
   *
   * @return the method that owns this body.
   */
  public MethodSignature getMethodSignature() {
    return methodSignature;
  }

  /** Returns the number of locals declared in this body. */
  public int getLocalCount() {
    return locals.size();
  }

  private void runValidation(BodyValidator validator) {
    final List<ValidationException> exceptionList = new ArrayList<>();
    validator.validate(this, exceptionList);
    if (!exceptionList.isEmpty()) {
      throw exceptionList.get(0);
    }
  }

  /** Verifies that a ValueBox is not used in more than one place. */
  public void validateValueBoxes() {
    runValidation(new ValueBoxesValidator());
  }

  /** Verifies that each Local of getUsesAndDefs() is in this body's locals Chain. */
  public void validateLocals() {
    runValidation(new LocalsValidator());
  }

  /** Verifies that the begin, end and handler units of each trap are in this body. */
  public void validateTraps() {
    runValidation(new TrapsValidator());
  }

  /** Verifies that the StmtBoxes of this Body all point to a Stmt contained within this body. */
  public void validateStmtBoxes() {
    runValidation(new StmtBoxesValidator());
  }

  /** Verifies that each use in this Body has a def. */
  public void validateUses() {
    runValidation(new UsesValidator());
  }

  /** Returns a backed chain of the locals declared in this Body. */
  public Set<Local> getLocals() {
    return locals;
  }

  /** Returns a backed view of the traps found in this Body. */
  public List<Trap> getTraps() {
    return traps;
  }

  /** @return ordered branching edges */
  private Map<Stmt, List<Stmt>> getBranches() {
    return branches;
  }

  /** Return unit containing the \@this-assignment * */
  public Stmt getThisStmt() {
    for (Stmt u : getStmts()) {
      if (u instanceof JIdentityStmt && ((JIdentityStmt) u).getRightOp() instanceof JThisRef) {
        return u;
      }
    }

    throw new RuntimeException("couldn't find this-assignment!" + " in " + getMethodSignature());
  }

  /** Return LHS of the first identity stmt assigning from \@this. */
  public Local getThisLocal() {
    return (Local) (((JIdentityStmt) getThisStmt()).getLeftOp());
  }

  /** Return LHS of the first identity stmt assigning from \@parameter i. */
  public Local getParameterLocal(int i) {
    for (Stmt s : getStmts()) {
      if (s instanceof JIdentityStmt && ((JIdentityStmt) s).getRightOp() instanceof JParameterRef) {
        JIdentityStmt is = (JIdentityStmt) s;
        JParameterRef pr = (JParameterRef) is.getRightOp();
        if (pr.getIndex() == i) {
          return (Local) is.getLeftOp();
        }
      }
    }

    throw new RuntimeException("couldn't find JParameterRef" + i + "! in " + getMethodSignature());
  }

  /**
   * Get all the LHS of the identity statements assigning from parameter references.
   *
   * @return a list of size as per <code>getMethod().getParameterCount()</code> with all elements
   *     ordered as per the parameter index.
   * @throws RuntimeException if a JParameterRef is missing
   */
  @Nonnull
  public Collection<Local> getParameterLocals() {
    final List<Local> retVal = new ArrayList<>();
    // TODO: [ms] performance: don't iterate over all stmt -> lazy vs freedom/error tolerance -> use
    // fixed index positions at the beginning?
    for (Stmt u : cfg.nodes()) {
      if (u instanceof JIdentityStmt) {
        JIdentityStmt is = (JIdentityStmt) u;
        if (is.getRightOp() instanceof JParameterRef) {
          JParameterRef pr = (JParameterRef) is.getRightOp();
          retVal.add(pr.getIndex(), (Local) is.getLeftOp());
        }
      }
    }
    return Collections.unmodifiableCollection(retVal);
  }

  /**
   * Returns the result of iterating through all Stmts in this body. All Stmts thus found are
   * returned. Branching Stmts and statements which use PhiExpr will have Stmts; a Stmt contains a
   * Stmt that is either a target of a branch or is being used as a pointer to the end of a CFG
   * block.
   *
   * <p>This method was typically used for pointer patching, e.g. when the unit chain is cloned.
   *
   * @return A collection of all the Stmts
   */
  @Nonnull
  public Collection<Stmt> getTargetStmtsInBody() {
    List<Stmt> stmtList = new ArrayList<>();
    Iterator<Stmt> iterator = cfg.nodes().iterator();
    while (iterator.hasNext()) {
      Stmt stmt = iterator.next();

      if (stmt instanceof BranchingStmt) {
        final List<Stmt> branchTargetsOf = getBranchTargetsOf(stmt);

        if (stmt instanceof JIfStmt) {
          stmtList.add(branchTargetsOf.get(1));
        } else if (stmt instanceof JGotoStmt) {
          stmtList.add(branchTargetsOf.get(0));
        } else if (stmt instanceof JSwitchStmt) {
          stmtList.addAll(branchTargetsOf);
        }
      }
    }

    for (Trap item : traps) {
      stmtList.addAll(item.getStmts());
    }
    return Collections.unmodifiableCollection(stmtList);
  }

  /**
   * Returns the statements that make up this body. [ms] just use for tests!
   *
   * @return the statements in this Body
   */
  @Nonnull
  @Deprecated
  public List<Stmt> getStmts() {
    return new ArrayList<>(getStmtGraph().nodes());
  }

  public ImmutableGraph<Stmt> getStmtGraph() {
    return cfg;
  }

  private void checkInit() {
    runValidation(new CheckInitValidator());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    StringWriter writer = new StringWriter();
    try (PrintWriter writerOut = new PrintWriter(new EscapedWriter(writer))) {
      new Printer().printTo(this, writerOut);
    }
    return writer.toString();
  }

  @Nonnull
  public Position getPosition() {
    return position;
  }

  /** returns a List of Branch targets of Branching Stmts */
  @Nonnull
  public List<Stmt> getBranchTargetsOf(@Nonnull Stmt fromStmt) {
    return branches.getOrDefault(fromStmt, Collections.emptyList());
  }

  public boolean isStmtBranchTarget(@Nonnull Stmt targetStmt) {
    final Set<Stmt> predecessors = cfg.predecessors(targetStmt);
    if (predecessors.size() > 1) {
      return true;
    }

    final Iterator<Stmt> iterator = predecessors.iterator();
    if (iterator.hasNext()) {
      Stmt pred = iterator.next();
      if (pred instanceof JIfStmt && ((JIfStmt) pred).getTarget(this) == targetStmt) {
        return true;
      }

      if (pred instanceof JGotoStmt) {
        return true;
      }

      if (pred instanceof JSwitchStmt) {
        return true;
      }
    }

    return false;
  }

  public void validateIdentityStatements() {
    runValidation(new IdentityStatementsValidator());
  }

  /** Returns the first non-identity stmt in this body. */
  @Nonnull
  public Stmt getFirstNonIdentityStmt() {
    Iterator<Stmt> it = getStmts().iterator();
    Stmt o = null;
    while (it.hasNext()) {
      if (!((o = it.next()) instanceof JIdentityStmt)) {
        break;
      }
    }
    if (o == null) {
      throw new RuntimeException("no non-id statements!");
    }
    return o;
  }

  /**
   * Returns the results of iterating through all Stmts in this Body and querying them for Values
   * defined. All of the Values found are then returned as a List.
   *
   * @return a List of all the Values for Values defined by this Body's Stmts.
   */
  public Collection<Value> getUses() {
    ArrayList<Value> useList = new ArrayList<>();

    for (Stmt stmt : cfg.nodes()) {
      useList.addAll(stmt.getUses());
    }
    return useList;
  }

  /**
   * Returns the results of iterating through all Stmts in this Body and querying them for Values
   * defined. All of the Values found are then returned as a List.
   *
   * @return a List of all the Values for Values defined by this Body's Stmts.
   */
  public Collection<Value> getDefs() {
    ArrayList<Value> defList = new ArrayList<>();

    for (Stmt stmt : cfg.nodes()) {
      defList.addAll(stmt.getDefs());
    }
    return defList;
  }

  @Nonnull
  public Body withLocals(@Nonnull Set<Local> locals) {
    return new Body(locals, getTraps(), getStmtGraph(), getBranches(), getPosition());
  }

  @Nonnull
  public Body withTraps(@Nonnull List<Trap> traps) {
    return new Body(getLocals(), traps, getStmtGraph(), getBranches(), getPosition());
  }

  @Nonnull
  public Body withStmts(@Nonnull Graph<Stmt> stmtGraph) {
    return new Body(getLocals(), getTraps(), stmtGraph, getBranches(), getPosition());
  }

  @Nonnull
  public Body withPosition(@Nonnull Position position) {
    return new Body(getLocals(), getTraps(), getStmtGraph(), getBranches(), position);
  }

  public static BodyBuilder builder() {
    return new BodyBuilder();
  }

  public static BodyBuilder builder(Body body) {
    return new BodyBuilder(body);
  }

  public Stmt getFirstStmt() {
    return firstStmt;
  }

  public static class BodyBuilder {
    @Nonnull private Set<Local> locals = new HashSet<>();
    @Nonnull private final LocalGenerator localGen = new LocalGenerator(locals);

    @Nonnull private List<Trap> traps = new ArrayList<>();
    @Nonnull private Position position;

    @Nullable private MutableGraph<Stmt> cfg;

    @Nonnull private final Map<Stmt, List<Stmt>> branches = new HashMap<>();

    @Nullable private Stmt lastAddedStmt = null;
    @Nullable private Stmt firstStmt = null;
    @Nullable private MethodSignature methodSig = null;

    BodyBuilder() {
      cfg = new StmtGraph();
    }

    BodyBuilder(@Nonnull Body body) {
      this(body, Graphs.copyOf(body.getStmtGraph()));
    }

    BodyBuilder(@Nonnull Body body, @Nonnull MutableGraph<Stmt> graphContainer) {
      setMethodSignature(body.getMethodSignature());
      setLocals(body.getLocals());
      setTraps(body.getTraps());
      setPosition(body.getPosition());
      setFirstStmt(body.getFirstStmt());
      cfg = graphContainer;
    }

    @Nonnull
    public BodyBuilder setFirstStmt(@Nullable Stmt firstStmt) {
      this.firstStmt = firstStmt;
      return this;
    }

    @Nonnull
    public BodyBuilder setLocals(@Nonnull Set<Local> locals) {
      this.locals = locals;
      return this;
    }

    @Nonnull
    public BodyBuilder addLocal(@Nonnull String name, Type type) {
      locals.add(localGen.generateLocal(type));
      return this;
    }

    @Nonnull
    public BodyBuilder addLocal(@Nonnull Local local) {
      locals.add(local);
      return this;
    }

    @Nonnull
    public BodyBuilder setTraps(@Nonnull List<Trap> traps) {
      this.traps = traps;
      return this;
    }

    @Nonnull
    public BodyBuilder addStmt(@Nonnull Stmt stmt) {
      return addStmt(stmt, false);
    }

    @Nonnull
    public BodyBuilder addStmt(@Nonnull Stmt stmt, boolean linkLastStmt) {
      cfg.addNode(stmt);
      if (lastAddedStmt != null) {
        if (linkLastStmt && lastAddedStmt.fallsThrough()) {
          addFlow(lastAddedStmt, stmt);
        }
      } else {
        // automatically set first statement
        firstStmt = stmt;
      }
      lastAddedStmt = stmt;
      return this;
    }

    @Nonnull
    public BodyBuilder removeStmt(@Nonnull Stmt stmt) {
      cfg.removeNode(stmt);
      branches.remove(stmt);
      branches.values().forEach(fromStmt -> fromStmt.remove(stmt));
      return this;
    }

    @Nonnull
    public BodyBuilder addFlow(@Nonnull Stmt fromStmt, @Nonnull Stmt toStmt) {

      if (fromStmt instanceof BranchingStmt) {
        List<Stmt> edges = branches.computeIfAbsent(fromStmt, stmt -> new ArrayList());
        edges.add(toStmt);
      }
      cfg.putEdge(fromStmt, toStmt);
      return this;
    }

    @Nonnull
    public BodyBuilder removeFlow(@Nonnull Stmt fromStmt, @Nonnull Stmt toStmt) {
      cfg.removeEdge(fromStmt, toStmt);
      branches.get(fromStmt).remove(toStmt);
      return this;
    }

    @Nonnull
    public BodyBuilder setPosition(@Nonnull Position position) {
      this.position = position;
      return this;
    }

    public BodyBuilder setMethodSignature(MethodSignature methodSig) {
      this.methodSig = methodSig;
      return this;
    }

    @Nonnull
    public Body build() {

      // validate statements
      for (Stmt stmt : cfg.nodes()) {

        final int successorCount = cfg.successors(stmt).size();
        if (stmt instanceof BranchingStmt) {
          // validate branch stmts
          final List<Stmt> targets = branches.get(stmt);

          if (targets == null) {
            throw new IllegalArgumentException(stmt + ": targets is null - no flows set");
          }

          if (targets.size() != successorCount) {
            throw new IllegalArgumentException(
                stmt
                    + ": cfg.successors.size "
                    + targets.size()
                    + " does not match branches[stmt].size "
                    + successorCount);
          }

          for (Stmt target : targets) {
            if (target == stmt) {
              throw new IllegalArgumentException(stmt + ": a Stmt cannot branch to itself.");
            }
          }

          if (stmt instanceof JSwitchStmt) {
            if (successorCount != ((JSwitchStmt) stmt).getValueCount()) {
              throw new IllegalArgumentException(
                  stmt
                      + ": size of outgoing flows (i.e. "
                      + successorCount
                      + ") does not match the amount of switch statements case labels (i.e. "
                      + ((JSwitchStmt) stmt).getValueCount()
                      + ").");
            }
          } else if (stmt instanceof JIfStmt) {
            if (successorCount != 2) {
              throw new IllegalStateException(
                  stmt + ": must have '2' outgoing flow but has '" + successorCount + "'.");
            } else {

              // TODO: [ms] please fix order of targets of ifstmts in frontends i.e. Asmmethodsource
              final List<Stmt> edges = branches.get(stmt);
              Stmt currentNextNode = edges.get(0);
              final Iterator<Stmt> iterator = cfg.nodes().iterator();
              //noinspection StatementWithEmptyBody
              while (iterator.hasNext() && iterator.next() != stmt) {}

              // switch edge order if the order is wrong i.e. the first edge is not the following
              // stmt
              // in the node list
              if (iterator.hasNext() && iterator.next() != currentNextNode) {
                edges.set(0, edges.get(1));
                edges.set(1, currentNextNode);
              }
            }
          } else if (stmt instanceof JGotoStmt) {
            if (successorCount != 1) {
              throw new IllegalArgumentException(
                  stmt + ": Goto must have '1' outgoing flow but has '" + successorCount + "'.");
            }
          }

        } else if (stmt instanceof JReturnStmt
            || stmt instanceof JReturnVoidStmt
            || stmt instanceof JThrowStmt) {
          if (successorCount != 0) {
            throw new IllegalArgumentException(
                stmt + ": must have '0' outgoing flow but has '" + successorCount + "'.");
          }
        } else {
          if (successorCount != 1) {
            throw new IllegalArgumentException(
                stmt + ": must have '1' outgoing flow but has '" + successorCount + "'.");
          }
        }
      }

      if (methodSig == null) {
        throw new IllegalArgumentException("There is no MethodSignature set.");
      }

      // TODO: temporary DEBUG check as long as the branches array still exists
      for (Stmt stmt : cfg.nodes()) {
        if (stmt instanceof BranchingStmt) {

          int i = 0;
          final List<Stmt> branchesOfStmt = branches.get(stmt);
          final Set<Stmt> successors = cfg.successors(stmt);
          for (Stmt target : successors) {
            if (branchesOfStmt.get(i++) != target) {
              throw new IllegalArgumentException(
                  stmt + ": Wrong order between iterator and branches array!");
            }
          }
          assert (i == branchesOfStmt.size());
        }
      }

      final Body body =
          new Body(
              methodSig, locals, traps, ImmutableGraph.copyOf(cfg), branches, firstStmt, position);

      /* FIXME: [ms] order after immutablestmtgraph is applied is different!
      final ImmutableGraph<Stmt> stmtImmutableGraph = body.getStmtGraph();
      // TODO: temporary DEBUG check as long as the branches array still exists
      for (Stmt stmt : stmtImmutableGraph.nodes()) {
        if (stmt instanceof BranchingStmt) {

          int i = 0;
          final List<Stmt> branchesOfStmt = branches.get(stmt);
          final Set<Stmt> successors = stmtImmutableGraph.successors(stmt);
          for (Stmt target : successors) {
            if (branchesOfStmt.get(i++) != target) {
              throw new IllegalArgumentException(
                      stmt + ": Wrong order between iterator and branches array!");
            }
          }
          assert (i == branchesOfStmt.size());
        }
      }
      */

      return body;
    }
  }
}
