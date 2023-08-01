package sootup.core.model;

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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.graph.*;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.core.util.Copyable;
import sootup.core.util.EscapedWriter;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.printer.JimplePrinter;
import sootup.core.validation.*;

/**
 * Class that models the Jimple body (code attribute) of a method.
 *
 * @author Linghui Luo
 */
public class Body implements Copyable {

  /** The locals for this Body. */
  private final Set<Local> locals;

  @Nonnull private final StmtGraph<?> graph;

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
   */
  private Body(
      @Nonnull MethodSignature methodSignature,
      @Nonnull Set<Local> locals,
      @Nonnull StmtGraph<?> stmtGraph,
      @Nonnull Position position) {
    this.methodSignature = methodSignature;
    this.locals = Collections.unmodifiableSet(locals);
    this.graph = /* FIXME: [ms] make immutable when availabe */
        new MutableBlockStmtGraph(stmtGraph).unmodifiableStmtGraph();
    this.position = position;
    // FIXME: [JMP] Virtual method call in constructor
    checkInit();
  }

  /**
   * Returns the LHS of the first identity stmt assigning from \@this.
   *
   * @return The this local
   */
  public static Local getThisLocal(StmtGraph<?> stmtGraph) {
    for (Stmt stmt : stmtGraph.getNodes()) {
      if (stmt instanceof JIdentityStmt
          && ((JIdentityStmt) stmt).getRightOp() instanceof JThisRef) {
        return (Local) ((JIdentityStmt) stmt).getLeftOp();
      }
    }
    throw new RuntimeException("couldn't find *this* assignment");
  }

  /**
   * Returns the MethodSignature associated with this Body.
   *
   * @return the method that owns this body.
   */
  @Nonnull
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

  /** Verifies that a Value is not used in more than one place. */
  // TODO: #535 implement validator public void validateValues() {   runValidation(new
  // ValuesValidator());}

  /** Verifies that each Local of getUsesAndDefs() is in this body's locals Chain. */
  // TODO: #535 implement validator  public void validateLocals() {runValidation(new
  // LocalsValidator());}

  /** Verifies that each use in this Body has a def. */
  // TODO: #535 implement validator public void validateUses() {  runValidation(new
  // UsesValidator()); }
  private void checkInit() {
    runValidation(new CheckInitValidator());
  }

  /** Returns a backed chain of the locals declared in this Body. */
  public Set<Local> getLocals() {
    return locals;
  }

  /** Returns an unmodifiable view of the traps found in this Body. */
  @Nonnull
  public List<Trap> getTraps() {
    return graph.getTraps();
  }

  /** Return unit containing the \@this-assignment * */
  @Nullable
  public Stmt getThisStmt() {
    for (Stmt u : getStmts()) {
      if (u instanceof JIdentityStmt) {
        if (((JIdentityStmt<?>) u).getRightOp() instanceof JThisRef) {
          return u;
        }
      } else {
        // TODO: possible optimization see getParameterLocals()
        //  break;
      }
    }
    return null;
    //    throw new RuntimeException("couldn't find this-assignment!" + " in " +
    // getMethodSignature());
  }

  /** Return LHS of the first identity stmt assigning from \@this. */
  @Nullable
  public Local getThisLocal() {
    final JIdentityStmt<?> thisStmt = (JIdentityStmt<?>) getThisStmt();
    if (thisStmt == null) {
      return null;
    }
    return thisStmt.getLeftOp();
  }

  /** Return LHS of the first identity stmt assigning from \@parameter i. */
  @Nullable
  public Local getParameterLocal(int i) {
    for (Stmt s : getStmts()) {
      if (s instanceof JIdentityStmt) {
        if (((JIdentityStmt<?>) s).getRightOp() instanceof JParameterRef) {
          JIdentityStmt<?> idStmt = (JIdentityStmt<?>) s;
          JParameterRef pr = (JParameterRef) idStmt.getRightOp();
          if (pr.getIndex() == i) {
            return idStmt.getLeftOp();
          }
        }
      } else {
        // TODO: possible optimization see getParameterLocals()
        //  break;
      }
    }
    return null;
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
    for (Stmt u : graph.getNodes()) {
      if (u instanceof JIdentityStmt) {
        JIdentityStmt<?> idStmt = (JIdentityStmt<?>) u;
        if (idStmt.getRightOp() instanceof JParameterRef) {
          JParameterRef pr = (JParameterRef) idStmt.getRightOp();
          retVal.add(pr.getIndex(), idStmt.getLeftOp());
        }
      }
      /*  if we restrict/define that IdentityStmts MUST be at the beginnging.
      else{
        break;
      }
      * */

    }
    return Collections.unmodifiableCollection(retVal);
  }

  /**
   * returns the control flow graph that represents this body into a linear List of statements.
   *
   * @return the statements in this Body
   */
  @Nonnull
  public List<Stmt> getStmts() {
    final ArrayList<Stmt> stmts = new ArrayList<>(graph.getNodes().size());
    for (Stmt stmt : graph) {
      stmts.add(stmt);
    }
    return stmts;
  }

  @Nonnull
  // TODO: [ms] should be an ImmutableStmtGraph!
  public StmtGraph<?> getStmtGraph() {
    return graph;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    StringWriter writer = new StringWriter();
    try (PrintWriter writerOut = new PrintWriter(new EscapedWriter(writer))) {
      new JimplePrinter().printTo(this, writerOut);
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
    return getStmtGraph().getBranchTargetsOf(fromStmt);
  }

  public boolean isStmtBranchTarget(@Nonnull Stmt targetStmt) {
    return getStmtGraph().isStmtBranchTarget(targetStmt);
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

    for (Stmt stmt : graph.getNodes()) {
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

    for (Stmt stmt : graph.getNodes()) {
      defList.addAll(stmt.getDefs());
    }
    return defList;
  }

  @Nonnull
  public Body withLocals(@Nonnull Set<Local> locals) {
    return new Body(getMethodSignature(), locals, getStmtGraph(), getPosition());
  }

  public static BodyBuilder builder() {
    return new BodyBuilder();
  }

  public static BodyBuilder builder(@Nonnull MutableStmtGraph graph) {
    return new BodyBuilder(graph);
  }

  public static BodyBuilder builder(@Nonnull Body body, Set<MethodModifier> modifiers) {
    return new BodyBuilder(body, modifiers);
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
    @Nonnull private Set<Local> locals = new LinkedHashSet<>();
    @Nonnull private final LocalGenerator localGen = new LocalGenerator(locals);
    @Nonnull private Set<MethodModifier> modifiers = Collections.emptySet();

    @Nullable private Position position = null;
    @Nonnull private final MutableStmtGraph graph;
    @Nullable private MethodSignature methodSig = null;

    @Nullable private List<Stmt> cachedLinearizedStmts = null;

    BodyBuilder() {
      graph = new MutableBlockStmtGraph();
    }

    BodyBuilder(@Nonnull MutableStmtGraph graph) {
      this.graph = graph;
    }

    BodyBuilder(@Nonnull Body body, @Nonnull Set<MethodModifier> modifiers) {
      setModifiers(modifiers);
      setMethodSignature(body.getMethodSignature());
      setLocals(new LinkedHashSet<>(body.getLocals()));
      setPosition(body.getPosition());
      graph = new MutableBlockStmtGraph(body.getStmtGraph());
    }

    @Nonnull
    public MutableStmtGraph getStmtGraph() {
      return graph;
    }

    /* Gets an ordered copy of the Stmts in the StmtGraph */
    @Nonnull
    public List<Stmt> getStmts() {
      cachedLinearizedStmts = graph.getStmts();
      return cachedLinearizedStmts;
    }

    @Nonnull
    public Set<Local> getLocals() {
      return Collections.unmodifiableSet(locals);
    }

    @Nonnull
    public BodyBuilder setStartingStmt(@Nonnull Stmt startingStmt) {
      graph.setStartingStmt(startingStmt);
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

    public void replaceLocal(@Nonnull Local oldLocal, @Nonnull Local newLocal) {
      if (!locals.contains(oldLocal)) {
        throw new RuntimeException("The given old local: '" + oldLocal + "' is not in the body!");
      } else {
        for (Stmt currStmt : Lists.newArrayList(getStmtGraph().getNodes())) {
          final Stmt stmt = currStmt;
          if (currStmt.getUses().contains(oldLocal)) {
            final Stmt newStmt = currStmt.withNewUse(oldLocal, newLocal);
            if (newStmt != null) {
              currStmt = newStmt;
            }
          }
          final List<Value> defs = currStmt.getDefs();
          for (Value def : defs) {
            if (def == oldLocal || def.getUses().contains(oldLocal)) {
              if (currStmt instanceof AbstractDefinitionStmt) {
                currStmt = ((AbstractDefinitionStmt<?, ?>) currStmt).withNewDef(newLocal);
              }
            }
          }
          if (stmt != currStmt) {
            getStmtGraph().replaceNode(stmt, currStmt);
          }
        }
        locals.remove(oldLocal);
        locals.add(newLocal);
      }
    }

    /** replace the oldStmt with newStmt in stmtGraph and branches */
    @Nonnull
    public BodyBuilder replaceStmt(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
      graph.replaceNode(oldStmt, newStmt);
      return this;
    }

    /** remove the a stmt from the graph and stmt */
    @Nonnull
    public BodyBuilder removeStmt(@Nonnull Stmt stmt) {
      graph.removeNode(stmt);
      cachedLinearizedStmts = null;
      return this;
    }

    @Nonnull
    public BodyBuilder clearExceptionEdgesOf(@Nonnull Stmt stmt) {
      graph.clearExceptionalEdges(stmt);
      return this;
    }

    /*
     * Note: if there is a stmt branching to successor this is not updated to the new stmt
     * */
    @Nonnull
    public BodyBuilder insertBefore(@Nonnull Stmt beforeStmt, Stmt newstmt) {
      graph.insertBefore(beforeStmt, newstmt);
      return this;
    }

    @Nonnull
    @Deprecated
    public List<Trap> getTraps() {
      return graph.getTraps();
    }

    @Nonnull
    public BodyBuilder addFlow(@Nonnull Stmt fromStmt, @Nonnull Stmt toStmt) {
      graph.putEdge(fromStmt, toStmt);
      cachedLinearizedStmts = null;
      return this;
    }

    @Nonnull
    public BodyBuilder removeFlow(@Nonnull Stmt fromStmt, @Nonnull Stmt toStmt) {
      graph.removeEdge(fromStmt, toStmt);
      cachedLinearizedStmts = null;
      return this;
    }

    public BodyBuilder setModifiers(@Nonnull Set<MethodModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Nullable
    public Position getPosition() {
      return position;
    }

    @Nonnull
    public BodyBuilder setPosition(@Nonnull Position position) {
      this.position = position;
      return this;
    }

    public MethodSignature getMethodSignature() {
      return methodSig;
    }

    public BodyBuilder setMethodSignature(@Nonnull MethodSignature methodSig) {
      this.methodSig = methodSig;
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

      final Stmt startingStmt = graph.getStartingStmt();
      final Collection<Stmt> nodes = graph.getNodes();
      if (nodes.size() > 0 && !nodes.contains(startingStmt)) {
        // TODO: already handled in MutableBlockStmtGraph.. check the others as well
        throw new IllegalStateException(
            methodSig
                + ": The given startingStmt '"
                + startingStmt
                + "' does not exist in the StmtGraph.");
      }
      // validate statements
      try {
        graph.validateStmtConnectionsInGraph();
      } catch (Exception e) {
        throw new RuntimeException("StmtGraph of " + methodSig + " is invalid.", e);
      }

      return new Body(methodSig, locals, graph, position);
    }

    @Nonnull
    public Set<MethodModifier> getModifiers() {
      return modifiers;
    }

    @Override
    public String toString() {
      if (methodSig != null) {
        return "BodyBuilder for " + methodSig;
      } else {
        return super.toString();
      }
    }
  }

  /**
   * Collects all defining statements of a Local from a list of statements
   *
   * @param stmts The searched list of statements
   * @return A map of Locals and their using statements
   */
  public static Map<Local, Collection<Stmt>> collectDefs(Collection<Stmt> stmts) {
    Map<Local, Collection<Stmt>> allDefs = new HashMap<>();
    for (Stmt stmt : stmts) {
      List<Value> defs = stmt.getDefs();
      for (Value value : defs) {
        if (value instanceof Local) {
          Collection<Stmt> localDefs = allDefs.get(value);
          if (localDefs == null) {
            localDefs = new ArrayList<>();
          }
          localDefs.add(stmt);
          allDefs.put((Local) value, localDefs);
        }
      }
    }
    return allDefs;
  }

  /**
   * Collects all using statements of a Local from a list of statements
   *
   * @param stmts The searched list of statements
   * @return A map of Locals and their using statements
   */
  public static Map<Local, Collection<Stmt>> collectUses(List<Stmt> stmts) {
    Map<Local, Collection<Stmt>> allUses = new HashMap<>();
    for (Stmt stmt : stmts) {
      Collection<Value> uses = stmt.getUses();
      for (Value value : uses) {
        if (value instanceof Local) {
          Collection<Stmt> localUses = allUses.get(value);
          if (localUses == null) {
            localUses = new ArrayList<>();
          }
          localUses.add(stmt);
          allUses.put((Local) value, localUses);
        }
      }
    }
    return allUses;
  }
}
