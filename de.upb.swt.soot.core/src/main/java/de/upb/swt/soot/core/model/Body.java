package de.upb.swt.soot.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Markus Schmidt and others
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

import com.google.common.collect.Lists;
import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.graph.MutableStmtGraph;
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

  /** The locals for this Body. */
  private final Set<Local> locals;

  /** The stmts for this Body. */
  @Nonnull private final ImmutableStmtGraph cfg;

  /** The Position Information in the Source for this Body. */
  @Nonnull private final Position position;

  /** The MethodSignature associated with this Body. */
  @Nonnull private final MethodSignature methodSignature;

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
   */
  private Body(
      @Nonnull MethodSignature methodSignature,
      @Nonnull Set<Local> locals,
      @Nonnull StmtGraph stmtGraph,
      @Nonnull Position position) {
    this.methodSignature = methodSignature;
    this.locals = Collections.unmodifiableSet(locals);
    this.cfg = ImmutableStmtGraph.copyOf(stmtGraph);
    this.position = position;
    // FIXME: [JMP] Virtual method call in constructor
    checkInit();
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

  /** Returns an unmodifiable view of the traps found in this Body. */
  @Nonnull
  public List<Trap> getTraps() {
    return cfg.getTraps();
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
    for (Stmt stmt : cfg.nodes()) {
      if (stmt instanceof BranchingStmt) {
        if (stmt instanceof JIfStmt) {
          stmtList.add(((JIfStmt) stmt).getTarget(this));
        } else if (stmt instanceof JGotoStmt) {
          stmtList.add(((JGotoStmt) stmt).getTarget(this));
        } else if (stmt instanceof JSwitchStmt) {
          stmtList.addAll(getBranchTargetsOf((BranchingStmt) stmt));
        }
      }
    }

    for (Trap item : getTraps()) {
      stmtList.addAll(item.getStmts());
    }
    return Collections.unmodifiableCollection(stmtList);
  }

  /**
   * returns the control flow graph that represents this body into a linear List of statements.
   *
   * @return the statements in this Body
   */
  @Nonnull
  public List<Stmt> getStmts() {
    final ArrayList<Stmt> stmts = new ArrayList<>(cfg.nodes().size());
    for (Stmt stmt : cfg) {
      stmts.add(stmt);
    }
    return stmts;
  }

  public ImmutableStmtGraph getStmtGraph() {
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
  public List<Stmt> getBranchTargetsOf(@Nonnull BranchingStmt fromStmt) {
    return cfg.successors(fromStmt);
  }

  public boolean isStmtBranchTarget(@Nonnull Stmt targetStmt) {
    final List<Stmt> predecessors = cfg.predecessors(targetStmt);
    if (predecessors.size() > 1) {
      return true;
    }

    final Iterator<Stmt> iterator = predecessors.iterator();
    if (iterator.hasNext()) {
      Stmt pred = iterator.next();
      if (pred.branches()) {
        if (pred instanceof JIfStmt) {
          return ((JIfStmt) pred).getTarget(this) == targetStmt;
        }
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
    return new Body(getMethodSignature(), locals, getStmtGraph(), getPosition());
  }

  /** helps against ConcurrentModificationException; it queues changes until they are committed */
  private static class StmtGraphManipulationQueue {

    // List sizes are a multiple of 2; even: from odd: to of an edge
    @Nonnull private final List<Stmt> flowsToRemove = new ArrayList<>();
    @Nonnull private final List<Stmt> flowsToAdd = new ArrayList<>();

    void addFlow(@Nonnull Stmt from, @Nonnull Stmt to) {
      flowsToAdd.add(from);
      flowsToAdd.add(to);
    }

    void removeFlow(@Nonnull Stmt from, @Nonnull Stmt to) {
      flowsToRemove.add(from);
      flowsToRemove.add(to);
    }

    /** return true if there where queued changes */
    boolean commit(MutableStmtGraph graph) {
      if (!flowsToAdd.isEmpty() || !flowsToRemove.isEmpty()) {
        Iterator<Stmt> addIt = flowsToAdd.iterator();
        while (addIt.hasNext()) {
          final Stmt from = addIt.next();
          final Stmt to = addIt.next();
          graph.putEdge(from, to);
        }

        Iterator<Stmt> remIt = flowsToRemove.iterator();
        while (remIt.hasNext()) {
          final Stmt from = remIt.next();
          final Stmt to = remIt.next();
          graph.removeEdge(from, to);
        }
        clear();
        return true;
      }
      return false;
    }

    public void clear() {
      flowsToAdd.clear();
      flowsToRemove.clear();
    }
  }

  public static BodyBuilder builder() {
    return new BodyBuilder();
  }

  public static BodyBuilder builder(Body body) {
    return new BodyBuilder(body);
  }

  /**
   * The BodyBuilder helps to create a new Body in a fluent way (see Builder Pattern)
   *
   * <pre>
   * <code>
   * Stmt stmt1, stmt2, stmt3;
   * ...
   * Body.BodyBuilder builder = Body.builder();
   * builder.setMethodSignature( ... );
   * builder.setStartingStmt(stmt1);
   * builder.addFlow(stmt1,stmt2);
   * builder.addFlow(stmt2,stmt3);
   * ...
   * Body body = builder.build();
   *
   * </code>
   * </pre>
   */
  public static class BodyBuilder {
    @Nonnull private Set<Local> locals = new HashSet<>();
    @Nonnull private final LocalGenerator localGen = new LocalGenerator(locals);

    @Nullable private Position position = null;
    @Nonnull private final MutableStmtGraph cfg;
    @Nullable private MethodSignature methodSig = null;

    @Nullable private StmtGraphManipulationQueue changeQueue = null;
    @Nullable private List<Stmt> cachedLinearizedStmts = null;

    BodyBuilder() {
      cfg = new MutableStmtGraph();
    }

    BodyBuilder(@Nonnull Body body) {
      setMethodSignature(body.getMethodSignature());
      setLocals(body.getLocals());
      setPosition(body.getPosition());
      cfg = new MutableStmtGraph(body.getStmtGraph());
      setTraps(body.getTraps());
    }

    @Nonnull
    public StmtGraph getStmtGraph() {
      return cfg.unmodifiableStmtGraph();
    }

    @Nonnull
    public List<Stmt> getStmts() {
      cachedLinearizedStmts = Lists.newArrayList(cfg);
      return cachedLinearizedStmts;
    }

    @Nonnull
    public BodyBuilder setStartingStmt(@Nonnull Stmt startingStmt) {
      cfg.setStartingStmt(startingStmt);
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
      cfg.setTraps(traps);
      return this;
    }

    @Nonnull
    public List<Trap> getTraps() {
      return cfg.getTraps();
    }

    @Nonnull
    public BodyBuilder addFlow(@Nonnull Stmt fromStmt, @Nonnull Stmt toStmt) {
      if (changeQueue == null) {
        cfg.putEdge(fromStmt, toStmt);
        cachedLinearizedStmts = null;
      } else {
        changeQueue.addFlow(fromStmt, toStmt);
      }
      return this;
    }

    @Nonnull
    public BodyBuilder removeFlow(@Nonnull Stmt fromStmt, @Nonnull Stmt toStmt) {
      if (changeQueue == null) {
        cfg.removeEdge(fromStmt, toStmt);
        cachedLinearizedStmts = null;
      } else {
        changeQueue.removeFlow(fromStmt, toStmt);
      }
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

    /**
     * Queues changes to the StmtGraph (e.g. addFlow, removeFlow) until they are commited. helps to
     * prevent ConcurrentModificationException
     */
    public BodyBuilder enableDeferredStmtGraphChanges() {
      if (changeQueue == null) {
        changeQueue = new StmtGraphManipulationQueue();
      }
      return this;
    }

    /**
     * commits the changes that were added to the queue if that was enabled before AND disables
     * further queueing of changes.
     */
    public BodyBuilder disableAndCommitDeferredStmtGraphChanges() {
      commitDeferredStmtGraphChanges();
      changeQueue = null;
      return this;
    }

    /** commits the changes that were added to the queue if that was enabled before */
    public BodyBuilder commitDeferredStmtGraphChanges() {
      if (changeQueue != null) {
        if (changeQueue.commit(cfg)) {
          cachedLinearizedStmts = null;
        }
      }
      return this;
    }
    /** clears queued changes fot */
    public BodyBuilder clearDeferredStmtGraphChanges() {
      if (changeQueue != null) {
        changeQueue.clear();
      }
      return this;
    }

    @Nonnull
    public Body build() {

      if (methodSig == null) {
        throw new RuntimeException("There is no MethodSignature set.");
      }

      if (position == null) {
        setPosition(NoPositionInformation.getInstance());
      }

      // commit pending changes
      commitDeferredStmtGraphChanges();

      final Stmt startingStmt = cfg.getStartingStmt();
      final Set<Stmt> nodes = cfg.nodes();
      if (nodes.size() > 0 && !nodes.contains(startingStmt)) {
        throw new RuntimeException(
            methodSig
                + ": The given startingStmt '"
                + startingStmt
                + "' does not exist in the StmtGraph.");
      }

      // validate statements
      cfg.validateStmtConnectionsInGraph();

      return new Body(methodSig, locals, cfg, position);
    }
  }
}
