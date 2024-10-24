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
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.EscapedWriter;
import sootup.core.util.printer.JimplePrinter;

/**
 * Class that models the Jimple body (code attribute) of a method.
 *
 * @author Linghui Luo
 */
public class Body implements HasPosition {

  /** The locals for this Body. */
  private final Set<Local> locals;

  @Nonnull private final StmtGraph<?> graph;

  /** The Position Information in the Source for this Body. */
  @Nonnull private final Position position;

  /** The MethodSignature associated with this Body. */
  @Nonnull private final MethodSignature methodSignature;

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
    this.graph = MutableBlockStmtGraph.createUnmodifiableStmtGraph(stmtGraph);
    this.position = position;
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
        return ((JIdentityStmt) stmt).getLeftOp();
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

  /** Returns a backed chain of the locals declared in this Body. */
  public Set<Local> getLocals() {
    return locals;
  }

  /**
   * Returns an unmodifiable view of the traps found in this Body. @Deprecated the exceptional flow
   * information is already integrated into the StmtGraphs BasicBlocks.getExceptionalFlows() -
   * exists to make porting tools from Soot easier
   */
  @Nonnull
  @Deprecated()
  public List<Trap> getTraps() {
    return graph.buildTraps();
  }

  /** Return unit containing the \@this-assignment * */
  @Nullable
  public Stmt getThisStmt() {
    for (Stmt stmt : graph) {
      if (stmt instanceof JIdentityStmt) {
        if (((JIdentityStmt) stmt).getRightOp() instanceof JThisRef) {
          return stmt;
        }
      } else {
        // TODO: possible optimisation see getParameterLocals()
        //  break;
      }
    }
    return null;
    //    throw new IllegalArgumentException("couldn't find this-assignment!" + " in " +
    // getMethodSignature());
  }

  /** Return LHS of the first identity stmt assigning from \@this. */
  @Nullable
  public Local getThisLocal() {
    final JIdentityStmt thisStmt = (JIdentityStmt) getThisStmt();
    if (thisStmt == null) {
      return null;
    }
    return thisStmt.getLeftOp();
  }

  /** Return LHS of the first identity stmt assigning from \@parameter i. */
  @Nonnull
  public Local getParameterLocal(int i) {
    for (Stmt stmt : graph) {
      // TODO: possible optimisation see getParameterLocals()
      if (stmt instanceof JIdentityStmt) {
        if (((JIdentityStmt) stmt).getRightOp() instanceof JParameterRef) {
          JIdentityStmt idStmt = (JIdentityStmt) stmt;
          JParameterRef pr = (JParameterRef) idStmt.getRightOp();
          if (pr.getIndex() == i) {
            return idStmt.getLeftOp();
          }
        }
      }
    }
    throw new IllegalArgumentException("There exists no Parameter Local with index " + i + "!");
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
    for (Stmt u : graph) {
      if (u instanceof JIdentityStmt) {
        JIdentityStmt idStmt = (JIdentityStmt) u;
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
   * returns the control flow graph that represents this body into a linear List of statements. for
   * more detailed information of the underlying CFG - or just parts of it - have a look at
   * getStmtGraph()
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
  @Override
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
  public Stream<Value> getUses() {
    return graph.getNodes().stream().flatMap(Stmt::getUses);
  }

  /**
   * Returns the results of iterating through all Stmts in this Body and querying them for Values
   * defined. All of the Values found are then returned as a List.
   *
   * @return a List of all the Values for Values defined by this Body's Stmts.
   */
  public Collection<LValue> getDefs() {
    return getDefs(graph);
  }

  public static Collection<LValue> getDefs(StmtGraph<?> graph) {
    ArrayList<LValue> defList = new ArrayList<>();

    for (Stmt stmt : graph.getNodes()) {
      stmt.getDef().ifPresent(defList::add);
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

  /** The BodyBuilder helps to create a new Body in a fluent way (see Builder Pattern) */
  public static class BodyBuilder implements HasPosition {
    @Nonnull private Set<Local> locals = new LinkedHashSet<>();
    @Nonnull private Set<MethodModifier> modifiers = Collections.emptySet();

    @Nullable private Position position = null;
    @Nonnull private final MutableStmtGraph graph;
    @Nullable private MethodSignature methodSig = null;

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
      return graph.getStmts();
    }

    @Nonnull
    public Set<Local> getLocals() {
      return locals;
    }

    @Nonnull
    public BodyBuilder setLocals(@Nonnull Set<Local> locals) {
      this.locals = locals;
      return this;
    }

    @Nonnull
    public BodyBuilder addLocal(@Nonnull Local local) {
      locals.add(local);
      return this;
    }

    public void replaceLocal(@Nonnull Local existingLocal, @Nonnull Local newLocal) {
      if (!locals.contains(existingLocal)) {
        throw new IllegalArgumentException(
            "The given existing Local '" + existingLocal + "' is not in the body!");
      }

      for (Stmt currStmt : Lists.newArrayList(getStmtGraph().getNodes())) {
        final Stmt stmt = currStmt;
        if (currStmt.getUses().anyMatch(v -> v == existingLocal)) {
          currStmt = currStmt.withNewUse(existingLocal, newLocal);
        }
        Optional<LValue> defOpt = currStmt.getDef();
        if (defOpt.isPresent()) {
          LValue def = defOpt.get();
          if (def == existingLocal || def.getUses().anyMatch(v -> v == existingLocal)) {
            if (currStmt instanceof AbstractDefinitionStmt) {
              currStmt = ((AbstractDefinitionStmt) currStmt).withNewDef(newLocal);
            }
          }
        }
        if (stmt != currStmt) {
          getStmtGraph().replaceNode(stmt, currStmt);
        }
      }
      locals.remove(existingLocal);
      locals.add(newLocal);
    }

    public BodyBuilder setModifiers(@Nonnull Set<MethodModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Nullable
    @Override
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
        //        System.out.println("StmtGraph of " + methodSig + " is invalid." + e.getCause());
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

    public void removeDefLocalsOf(@Nonnull Stmt stmt) {
      stmt.getDef()
          .ifPresent(
              def -> {
                if (def instanceof Local) {
                  List<Stmt> localOccurrences =
                      ((Local) def).getStmtsUsingthisLocal(this.graph.getStmts(), stmt);
                  // after removing stmt, if the local variable doesn't occur anywhere else then
                  // safely remove
                  if (localOccurrences.isEmpty()) {
                    locals.remove(def);
                  }
                }
              });
    }
  }

  /**
   * Collects all defining statements of a Local from a list of statements
   *
   * @param stmts The searched list of statements
   * @return A map of Locals and their using statements
   */
  public static Map<LValue, Collection<Stmt>> collectDefs(Collection<Stmt> stmts) {
    Map<LValue, Collection<Stmt>> allDefs = new HashMap<>();
    for (Stmt stmt : stmts) {
      Optional<LValue> defOPt = stmt.getDef();
      if (defOPt.isPresent()) {
        LValue def = defOPt.get();
        Collection<Stmt> localDefs = allDefs.computeIfAbsent(def, key -> new ArrayList<>());
        localDefs.add(stmt);
        allDefs.put(def, localDefs);
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
  public static Map<Value, Collection<Stmt>> collectUses(Collection<Stmt> stmts) {
    Map<Value, Collection<Stmt>> allUses = new HashMap<>();
    for (Stmt stmt : stmts) {
      for (Iterator<Value> iterator = stmt.getUses().iterator(); iterator.hasNext(); ) {
        Value value = iterator.next();
        Collection<Stmt> localUses = allUses.computeIfAbsent(value, key -> new ArrayList<>());
        localUses.add(stmt);
        allUses.put(value, localUses);
      }
    }
    return allUses;
  }
}
