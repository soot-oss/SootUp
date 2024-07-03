package sootup.java.codepropertygraph.ddg;

import java.util.*;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import sootup.analysis.intraprocedural.ForwardFlowAnalysis;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;

public class ReachingDefs {
  private final Map<Stmt, List<Stmt>> reachingDefs;

  public ReachingDefs(StmtGraph<? extends BasicBlock<?>> graph) {
    this.reachingDefs = new HashMap<>();

    ReachingDefsAnalysis analysis = new ReachingDefsAnalysis(graph);

    for (Stmt stmt : graph.getStmts()) {
      if (stmt.getUses().findFirst().isPresent()) continue;

      Set<VariableDefinition> inset = analysis.getFlowBefore(stmt);
      reachingDefs.put(stmt, new ArrayList<>());

      for (VariableDefinition def : inset) {
        Value definedVar = def.getValue();
        Optional<Stmt> definingStmt = def.getStmt();

        stmt.getUses()
            .filter(
                usedVar ->
                    definedVar.equivTo(usedVar)
                        && definingStmt.isPresent()
                        && definingStmt.get() != stmt)
            .forEach(usedVar -> reachingDefs.get(stmt).add(definingStmt.get()));
      }
    }
  }

  public Map<Stmt, List<Stmt>> getReachingDefs() {
    return reachingDefs;
  }

  static class ReachingDefsAnalysis extends ForwardFlowAnalysis<Set<VariableDefinition>> {

    /** Construct the analysis from StmtGraph. */
    public <B extends BasicBlock<B>> ReachingDefsAnalysis(StmtGraph<B> graph) {
      super(graph);
      execute();
    }

    @Nonnull
    @Override
    protected Set<VariableDefinition> newInitialFlow() {
      Set<VariableDefinition> initialValues = new HashSet<>();
      graph.getNodes().stream()
          .map(Stmt::getDef)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(def -> initialValues.add(new VariableDefinition(def, null)));
      return initialValues;
    }

    @Override
    protected void merge(
        @Nonnull Set<VariableDefinition> in1,
        @Nonnull Set<VariableDefinition> in2,
        @Nonnull Set<VariableDefinition> out) {
      out.clear();
      out.addAll(in1);
      out.addAll(in2);
    }

    @Override
    protected void copy(
        @Nonnull Set<VariableDefinition> source, @Nonnull Set<VariableDefinition> dest) {
      dest.clear();
      dest.addAll(source);
    }

    @Override
    protected void flowThrough(
        @Nonnull Set<VariableDefinition> in, Stmt d, @Nonnull Set<VariableDefinition> out) {
      out.clear();
      out.addAll(in);
      kill(d).forEach(out::remove);
      gen(d).forEach(out::add);
    }

    private Stream<VariableDefinition> kill(Stmt d) {
      if (!(d instanceof JAssignStmt)) return Stream.empty();

      return d.getDef()
          .map(
              definedValue -> {
                List<VariableDefinition> output = new ArrayList<>();
                output.add(new VariableDefinition(definedValue, null));
                graph.getNodes().stream()
                    .filter(
                        stmt ->
                            stmt.getDef().isPresent() && stmt.getDef().get().equals(definedValue))
                    .forEach(stmt -> output.add(new VariableDefinition(definedValue, stmt)));
                return output.stream();
              })
          .orElseGet(Stream::empty);
    }

    private Stream<VariableDefinition> gen(Stmt d) {
      return d.getDef()
          .map(def -> Stream.of(new VariableDefinition(def, d)))
          .orElseGet(Stream::empty);
    }
  }
}
