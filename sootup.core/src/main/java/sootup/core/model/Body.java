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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
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

  @NonNull private final ControlFlowGraph<?> graph;

  /** The Position Information in the Source for this Body. */
  @NonNull private final Position position;

  /** The MethodSignature associated with this Body. */
  @NonNull private final MethodSignature methodSignature;

  /**
   * Creates an body which is not associated to any method.
   *
   * @param locals please use {@link LocalGenerator} to generate local for a body.
   */
  private Body(
      @NonNull MethodSignature methodSignature,
      @NonNull Set<Local> locals,
      @NonNull ControlFlowGraph<?> controlFlowGraph,
      @NonNull Position position) {
    this.methodSignature = methodSignature;
    this.locals = Collections.unmodifiableSet(locals);
    this.graph = MutableBlockControlFlowGraph.createUnmodifiableControlFlowGraph(controlFlowGraph);
    this.position = position;
  }

  /**
   * Returns the LHS of the first identity stmt assigning from \@this.
   *
   * @return The this local
   */
  public static Local getThisLocal(ControlFlowGraph<?> controlFlowGraph) {
    for (Stmt stmt : controlFlowGraph.getNodes()) {
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
  @NonNull
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
   * Returns the \@this-assignment, which is the first stmt of the body of a non-static method (see
   * {@link sootup.core.validation.IdentityStmtsValidator}).
   *
   * @return the \@this-assignment, or null if the body does not start with one
   */
  @Nullable
  public Stmt getThisStmt() {
    Stmt first = graph.getStartingStmt();
    if (first instanceof JIdentityStmt
        && ((JIdentityStmt) first).getRightOp() instanceof JThisRef) {
      return first;
    }
    return null;
  }

  /** Return LHS of the \@this-assignment, or null if the body does not start with one. */
  @Nullable
  public Local getThisLocal() {
    final JIdentityStmt thisStmt = (JIdentityStmt) getThisStmt();
    if (thisStmt == null) {
      return null;
    }
    return thisStmt.getLeftOp();
  }

  /**
   * Return LHS of the identity stmt assigning from \@parameter i. The \@parameter-assignments
   * follow the \@this-assignment, in the order of the parameters (see {@link
   * sootup.core.validation.IdentityStmtsValidator}).
   *
   * @throws IllegalArgumentException if the method has no parameter i
   * @throws IllegalStateException if parameter i is not assigned at its position
   */
  @NonNull
  public Local getParameterLocal(int i) {
    if (i < 0 || i >= methodSignature.getParameterTypes().size()) {
      throw new IllegalArgumentException("There exists no Parameter Local with index " + i + "!");
    }
    Stmt stmt = getFirstParameterAssignment();
    for (int k = 0; k < i; k++) {
      stmt = getNextParameterAssignment(stmt);
    }
    return getParameterLocal(stmt, i);
  }

  /**
   * Get all the LHS of the identity statements assigning from parameter references.
   *
   * @return a list of size as per <code>getMethod().getParameterCount()</code> with all elements
   *     ordered as per the parameter index.
   * @throws IllegalStateException if a parameter is not assigned at its position
   */
  @NonNull
  public Collection<Local> getParameterLocals() {
    int parameterCount = methodSignature.getParameterTypes().size();
    final List<Local> retVal = new ArrayList<>(parameterCount);
    if (parameterCount > 0) {
      Stmt stmt = getFirstParameterAssignment();
      retVal.add(getParameterLocal(stmt, 0));
      for (int i = 1; i < parameterCount; i++) {
        stmt = getNextParameterAssignment(stmt);
        retVal.add(getParameterLocal(stmt, i));
      }
    }
    return Collections.unmodifiableCollection(retVal);
  }

  /** The \@parameter0-assignment comes first, or right after the \@this-assignment. */
  @Nullable
  private Stmt getFirstParameterAssignment() {
    Stmt first = graph.getStartingStmt();
    return getThisStmt() == null ? first : getNextParameterAssignment(first);
  }

  @Nullable
  private Stmt getNextParameterAssignment(@Nullable Stmt stmt) {
    if (stmt == null) {
      return null;
    }
    List<Stmt> successors = graph.successors(stmt);
    return successors.size() == 1 ? successors.get(0) : null;
  }

  @NonNull
  private Local getParameterLocal(@Nullable Stmt stmt, int i) {
    if (stmt instanceof JIdentityStmt) {
      JIdentityStmt identityStmt = (JIdentityStmt) stmt;
      if (identityStmt.getRightOp() instanceof JParameterRef
          && ((JParameterRef) identityStmt.getRightOp()).getIndex() == i) {
        return identityStmt.getLeftOp();
      }
    }
    throw new IllegalStateException(
        "Parameter "
            + i
            + " of "
            + methodSignature
            + " is not assigned at its position in the body (see IdentityStmtsValidator)");
  }

  /**
   * returns the control flow graph that represents this body into a linear List of statements. for
   * more detailed information of the underlying CFG - or just parts of it - have a look at
   * getControlFlowGraph()
   *
   * @return the statements in this Body
   */
  @NonNull
  public List<Stmt> getStmts() {
    return graph.getStmts();
  }

  @NonNull
  // TODO: [ms] should be an ImmutableControlFlowGraph!
  public ControlFlowGraph<?> getControlFlowGraph() {
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

  @NonNull
  @Override
  public Position getPosition() {
    return position;
  }

  /** returns a List of Branch targets of Branching Stmts */
  @NonNull
  public List<Stmt> getBranchTargetsOf(@NonNull BranchingStmt fromStmt) {
    return getControlFlowGraph().getBranchTargetsOf(fromStmt);
  }

  public boolean isStmtBranchTarget(@NonNull Stmt targetStmt) {
    return getControlFlowGraph().isStmtBranchTarget(targetStmt);
  }

  /** Returns the first non-identity stmt in this body. */
  @NonNull
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
    return graph.getNodes().stream().flatMap(stmt -> stmt.getUses().stream());
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

  public static Collection<LValue> getDefs(ControlFlowGraph<?> graph) {
    ArrayList<LValue> defList = new ArrayList<>();

    for (Stmt stmt : graph.getNodes()) {
      stmt.getDef().ifPresent(defList::add);
    }
    return defList;
  }

  @NonNull
  public Body withLocals(@NonNull Set<Local> locals) {
    return new Body(getMethodSignature(), locals, getControlFlowGraph(), getPosition());
  }

  public static BodyBuilder builder() {
    return new BodyBuilder();
  }

  public static BodyBuilder builder(@NonNull MutableControlFlowGraph graph) {
    return new BodyBuilder(graph);
  }

  public static BodyBuilder builder(@NonNull Body body, Set<MethodModifier> modifiers) {
    return new BodyBuilder(body, modifiers);
  }

  /** The BodyBuilder helps to create a new Body in a fluent way (see Builder Pattern) */
  public static class BodyBuilder implements HasPosition {
    @NonNull private Set<Local> locals = new LinkedHashSet<>();
    @NonNull private Set<MethodModifier> modifiers = Collections.emptySet();

    @Nullable private Position position = null;
    @NonNull private final MutableControlFlowGraph graph;
    @Nullable private MethodSignature methodSig = null;

    BodyBuilder() {
      graph = new MutableBlockControlFlowGraph();
    }

    BodyBuilder(@NonNull MutableControlFlowGraph graph) {
      this.graph = graph;
    }

    BodyBuilder(@NonNull Body body, @NonNull Set<MethodModifier> modifiers) {
      setModifiers(modifiers);
      setMethodSignature(body.getMethodSignature());
      setLocals(new LinkedHashSet<>(body.getLocals()));
      setPosition(body.getPosition());
      graph = new MutableBlockControlFlowGraph(body.getControlFlowGraph());
    }

    @NonNull
    public MutableControlFlowGraph getControlFlowGraph() {
      return graph;
    }

    /* Gets an ordered copy of the Stmts in the ControlFlowGraph */
    @NonNull
    public List<Stmt> getStmts() {
      return graph.getStmts();
    }

    @NonNull
    public Set<Local> getLocals() {
      return locals;
    }

    @NonNull
    public BodyBuilder setLocals(@NonNull Set<Local> locals) {
      this.locals = locals;
      return this;
    }

    @NonNull
    public BodyBuilder addLocal(@NonNull Local local) {
      locals.add(local);
      return this;
    }

    public void replaceLocal(@NonNull Local existingLocal, @NonNull Local newLocal) {
      if (!locals.contains(existingLocal)) {
        throw new IllegalArgumentException(
            "The given existing Local '" + existingLocal + "' is not in the body!");
      }

      for (Stmt currStmt : Lists.newArrayList(getControlFlowGraph().getNodes())) {
        final Stmt stmt = currStmt;
        if (currStmt.getUses().stream().anyMatch(v -> v == existingLocal)) {
          currStmt = currStmt.withNewUse(existingLocal, newLocal);
        }
        Optional<LValue> defOpt = currStmt.getDef();
        if (defOpt.isPresent()) {
          LValue def = defOpt.get();
          if (def == existingLocal || def.getUses().stream().anyMatch(v -> v == existingLocal)) {
            if (currStmt instanceof AbstractDefinitionStmt) {
              currStmt = ((AbstractDefinitionStmt) currStmt).withNewDef(newLocal);
            }
          }
        }
        if (stmt != currStmt) {
          getControlFlowGraph().replaceNode(stmt, currStmt);
        }
      }
      locals.remove(existingLocal);
      locals.add(newLocal);
    }

    /**
     * Replaces each key of the map by its value, like {@link #replaceLocal(Local, Local)} for every
     * entry, but in a single pass over the statements: a statement is only rewritten for the locals
     * it uses or defines. Calling {@link #replaceLocal(Local, Local)} for many locals scans the
     * whole body for each of them.
     *
     * @param replacements the locals to replace (compared by identity) and their replacements
     */
    public void replaceLocals(@NonNull Map<Local, Local> replacements) {
      if (replacements.isEmpty()) {
        return;
      }
      Map<Local, Local> byIdentity = new IdentityHashMap<>(replacements);
      for (Local existingLocal : byIdentity.keySet()) {
        if (!locals.contains(existingLocal)) {
          throw new IllegalArgumentException(
              "The given existing Local '" + existingLocal + "' is not in the body!");
        }
      }

      for (Stmt stmt : Lists.newArrayList(getControlFlowGraph().getNodes())) {
        Stmt currStmt = stmt;
        for (Local existingLocal : localsOf(stmt, byIdentity)) {
          Local newLocal = byIdentity.get(existingLocal);
          if (currStmt.getUses().stream().anyMatch(v -> v == existingLocal)) {
            currStmt = currStmt.withNewUse(existingLocal, newLocal);
          }
          Optional<LValue> defOpt = currStmt.getDef();
          if (defOpt.isPresent()) {
            LValue def = defOpt.get();
            if (def == existingLocal || def.getUses().stream().anyMatch(v -> v == existingLocal)) {
              if (currStmt instanceof AbstractDefinitionStmt) {
                currStmt = ((AbstractDefinitionStmt) currStmt).withNewDef(newLocal);
              }
            }
          }
        }
        if (stmt != currStmt) {
          getControlFlowGraph().replaceNode(stmt, currStmt);
        }
      }
      for (Map.Entry<Local, Local> replacement : byIdentity.entrySet()) {
        locals.remove(replacement.getKey());
        locals.add(replacement.getValue());
      }
    }

    /** The locals of the map that the statement uses or defines, in the order they occur */
    private static Collection<Local> localsOf(Stmt stmt, Map<Local, Local> byIdentity) {
      Set<Local> found = Collections.newSetFromMap(new IdentityHashMap<>());
      List<Local> ordered = new ArrayList<>();
      stmt.getUsesAndDefs()
          .forEach(
              value -> {
                if (value instanceof Local
                    && byIdentity.containsKey(value)
                    && found.add((Local) value)) {
                  ordered.add((Local) value);
                }
              });
      return ordered;
    }

    public BodyBuilder setModifiers(@NonNull Set<MethodModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Nullable
    @Override
    public Position getPosition() {
      return position;
    }

    @NonNull
    public BodyBuilder setPosition(@NonNull Position position) {
      this.position = position;
      return this;
    }

    public MethodSignature getMethodSignature() {
      return methodSig;
    }

    public BodyBuilder setMethodSignature(@NonNull MethodSignature methodSig) {
      this.methodSig = methodSig;
      return this;
    }

    @NonNull
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
        // TODO: already handled in MutableBlockControlFlowGraph.. check the others as well
        throw new IllegalStateException(
            methodSig
                + ": The given startingStmt '"
                + startingStmt
                + "' does not exist in the ControlFlowGraph.");
      }
      // validate statements
      try {
        graph.validateStmtConnectionsInGraph();
      } catch (Exception e) {
        throw new RuntimeException("ControlFlowGraph of " + methodSig + " is invalid.", e);
        //        System.out.println("ControlFlowGraph of " + methodSig + " is invalid." +
        // e.getCause());
      }

      return new Body(methodSig, locals, graph, position);
    }

    @NonNull
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

    public void removeDefLocalsOf(@NonNull Stmt stmt) {
      stmt.getDef()
          .ifPresent(
              def -> {
                if (def instanceof Local) {
                  List<Stmt> localOccurrences =
                      ((Local) def).getStmtsUsingOrDefiningthisLocal(this.graph.getStmts(), stmt);
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
   * Collects all using statements of a Values from a list of statements
   *
   * @param stmts The searched list of statements
   * @return A map of Values and their using statements
   */
  public static Map<Value, List<Stmt>> collectUses(Collection<Stmt> stmts) {
    return stmts.stream()
        .flatMap(stmt -> stmt.getUses().stream().map(value -> (Pair.of(value, stmt))))
        .collect(
            Collectors.groupingBy(
                Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));
  }
}
