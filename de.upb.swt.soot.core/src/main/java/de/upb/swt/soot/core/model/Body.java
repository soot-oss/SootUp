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
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.ref.JThisRef;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
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
          NoPositionInformation.getInstance());

  /** The locals for this Body. */
  private final Set<Local> locals;

  /** The traps for this Body. */
  private final List<Trap> traps;

  /** The stmts for this Body. */
  @Nonnull private final ImmutableGraph<Stmt> cfg;

  /** The first Stmt in this Body. */
  @Nonnull private final Stmt firstStmt;

  /** The Position Information in the Source for this Body. */
  @Nonnull private final Position position;

  /** The method associated with this Body. */
  @Nullable private volatile SootMethod method;

  /** An array containing some validators in order to validate the JimpleBody */
  @Nonnull
  private static final List<BodyValidator> validators =
      ImmutableUtils.immutableList(
          new LocalsValidator(),
          new TrapsValidator(),
          new StmtsValidator(),
          new UsesValidator(),
          new ValuesValidator(),
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
      @Nonnull Set<Local> locals,
      @Nonnull List<Trap> traps,
      @Nonnull Graph<Stmt> stmtGraph,
      @Nonnull Stmt startingStmt,
      @Nonnull Position position) {
    this.locals = Collections.unmodifiableSet(locals);
    this.traps = Collections.unmodifiableList(traps);
    // TODO: [ms] second constructor?
    this.cfg =
        stmtGraph instanceof ImmutableGraph
            ? (ImmutableGraph<Stmt>) stmtGraph
            : ImmutableGraph.copyOf(stmtGraph);
    this.position = position;
    this.firstStmt = startingStmt;

    // FIXME: [JMP] Virtual method call in constructor
    checkInit();
  }

  public Body(
      @Nonnull Set<Local> locals,
      @Nonnull List<Trap> traps,
      @Nonnull Graph<Stmt> stmtGraph,
      @Nonnull Position position) {

    // FIXME: [ms] dirty debugging hack !!!!!!
    this(
        locals,
        traps,
        stmtGraph,
        stmtGraph.nodes().iterator().hasNext() ? stmtGraph.nodes().iterator().next() : null,
        position);
  }

  @Nonnull
  public static Body getNoBody() {
    return EMPTY_BODY;
  }

  /**
   * Returns the method associated with this Body.
   *
   * @return the method that owns this body.
   */
  // FIXME: [ms] refactor to MethodSignature
  public SootMethod getMethod() {
    if (method == null) {
      throw new IllegalStateException(
          "The associated method of this body instance has not been not set yet.");
    }
    return method;
  }

  /**
   * Sets the method associated with this Body.
   *
   * @param method that should be associated with this body.
   */
  // FIXME: [ms] refactor to MethodSignature
  synchronized void setMethod(@Nullable SootMethod method) {
    if (this.method != null) {
      throw new IllegalStateException(
          "The declaring class of this SootMethod has already been set.");
    }
    this.method = method;
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

  /** Verifies that a Value is not used in more than one place. */
  public void validateValues() {
    runValidation(new ValuesValidator());
  }

  /** Verifies that each Local of getUsesAndDefs() is in this body's locals Chain. */
  public void validateLocals() {
    runValidation(new LocalsValidator());
  }

  /** Verifies that the begin, end and handler units of each trap are in this body. */
  public void validateTraps() {
    runValidation(new TrapsValidator());
  }

  /** Verifies that the Stmts of this Body all point to a Stmt contained within this body. */
  public void validateStmts() {
    runValidation(new StmtsValidator());
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

  /** Return unit containing the \@this-assignment * */
  public Stmt getThisStmt() {
    for (Stmt u : getStmts()) {
      if (u instanceof JIdentityStmt && ((JIdentityStmt) u).getRightOp() instanceof JThisRef) {
        return u;
      }
    }

    throw new RuntimeException("couldn't find this-assignment!" + " in " + getMethod());
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
        if (pr.getNum() == i) {
          return (Local) is.getLeftOp();
        }
      }
    }

    throw new RuntimeException("couldn't find JParameterRef" + i + "! in " + getMethod());
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
    final int numParams = getMethod().getParameterCount();
    final List<Local> retVal = new ArrayList<>(numParams);
    // TODO: [ms] performance: don't iterate over all stmt -> lazy vs freedom/error tolerance -> use
    // fixed index positions at the beginning?
    for (Stmt u : cfg.nodes()) {
      if (u instanceof JIdentityStmt) {
        JIdentityStmt is = (JIdentityStmt) u;
        if (is.getRightOp() instanceof JParameterRef) {
          JParameterRef pr = (JParameterRef) is.getRightOp();
          retVal.add(pr.getNum(), (Local) is.getLeftOp());
        }
      }
    }
    if (retVal.size() != numParams) {
      throw new RuntimeException("couldn't find JParameterRef! in " + getMethod());
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
  public Collection<Stmt> getTargetStmts() {
    List<Stmt> stmtList = new ArrayList<>();
    for (Stmt stmt : cfg.nodes()) {
      if (cfg.predecessors(stmt).size() > 1) {
        stmtList.add(stmt);
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
  // [ms] hint: guavas iterator uses insertion order -> don't implement otherwise elsewhere
  // otherwise JSwitchStmt will have a problem.
  @Nonnull
  public List<Stmt> getBranchTargets(@Nonnull Stmt fromStmt) {
    return new ArrayList<>(cfg.successors(fromStmt));
  }

  @Nonnull
  public boolean isStmtBranchTarget(@Nonnull Stmt targetStmt) {
    return cfg.predecessors(targetStmt).size() > 1;
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
    return new Body(locals, getTraps(), getStmtGraph(), getPosition());
  }

  @Nonnull
  public Body withTraps(@Nonnull List<Trap> traps) {
    return new Body(getLocals(), traps, getStmtGraph(), getPosition());
  }

  @Nonnull
  public Body withStmts(@Nonnull Graph<Stmt> stmtGraph) {
    return new Body(getLocals(), getTraps(), stmtGraph, getPosition());
  }

  @Nonnull
  public Body withPosition(@Nonnull Position position) {
    return new Body(getLocals(), getTraps(), getStmtGraph(), position);
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

    @Nonnull private final MutableGraph<Stmt> mutableGraph;
    @Nullable private Stmt lastAddedStmt = null;

    @Nullable private Stmt firstStmt = null;

    BodyBuilder() {
      mutableGraph = GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
    }

    BodyBuilder(Body body) {
      setLocals(body.getLocals());
      setTraps(body.getTraps());
      setPosition(body.getPosition());
      setFirstStmt(body.getFirstStmt());
      mutableGraph = GraphBuilder.from(body.getStmtGraph()).build();
    }

    public BodyBuilder setFirstStmt(@Nullable Stmt firstStmt) {
      this.firstStmt = firstStmt;
      return this;
    }

    public BodyBuilder setLocals(Set<Local> locals) {
      this.locals = locals;
      return this;
    }

    public BodyBuilder addLocal(String name, Type type) {
      this.locals.add(localGen.generateLocal(type));
      return this;
    }

    public BodyBuilder setTraps(List<Trap> traps) {
      this.traps = traps;
      return this;
    }

    public BodyBuilder addStmt(Stmt stmt) {
      return addStmt(stmt, false);
    }

    public BodyBuilder addStmt(Stmt stmt, boolean linkLastStmt) {
      mutableGraph.addNode(stmt);
      if (lastAddedStmt != null) {
        if (linkLastStmt) {
          addFlow(lastAddedStmt, stmt);
        }
      } else {
        // automatically set first statement
        firstStmt = stmt;
      }
      lastAddedStmt = stmt;
      return this;
    }

    public BodyBuilder mergeStmt(Stmt oldStmt, Stmt newStmt) {

      final Set<Stmt> predecessors = mutableGraph.predecessors(oldStmt);
      final Set<Stmt> successors = mutableGraph.successors(oldStmt);

      mutableGraph.addNode(newStmt);
      predecessors.forEach(predecessor -> mutableGraph.putEdge(predecessor, newStmt));
      successors.forEach(successor -> mutableGraph.putEdge(newStmt, successor));
      mutableGraph.removeNode(oldStmt);

      return this;
    }

    public BodyBuilder removeStmt(Stmt stmt) {
      mutableGraph.removeNode(stmt);
      return this;
    }

    public BodyBuilder addFlow(Stmt fromStmt, Stmt toStmt) {
      mutableGraph.putEdge(fromStmt, toStmt);
      // TODO: remove debug
      System.out.println(fromStmt + " => " + toStmt);
      return this;
    }

    public BodyBuilder removeFlow(Stmt fromStmt, Stmt toStmt) {
      mutableGraph.removeEdge(fromStmt, toStmt);
      return this;
    }

    public BodyBuilder setPosition(Position position) {
      this.position = position;
      return this;
    }

    public Body build() {

      // TODO: [ms] debug
      System.out.println("\n-------\n");
      for (Stmt node : mutableGraph.nodes()) {
        System.out.println(node + "");
        //        System.out.println("in " + mutableGraph.predecessors(node));

        // if( mutableGraph.successors(node).size() != 1 )
        {
          System.out.println("out " + mutableGraph.successors(node));
        }

        System.out.println("");
      }
      System.out.println("\n-------\n");

      return new Body(locals, traps, ImmutableGraph.copyOf(mutableGraph), firstStmt, position);
    }
  }
}
