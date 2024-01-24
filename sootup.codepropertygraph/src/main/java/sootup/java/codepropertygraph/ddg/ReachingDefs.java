package sootup.java.codepropertygraph.ddg;

import java.util.*;
import javax.annotation.Nonnull;
import sootup.analysis.intraprocedural.ForwardFlowAnalysis;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;

public class ReachingDefs {
  private final Map<Stmt, List<Stmt>> reachingDefs;

  public ReachingDefs(StmtGraph<? extends BasicBlock<?>> graph) {
    this.reachingDefs = new HashMap<>();

    ReachingDefsAnalysis analysis = new ReachingDefsAnalysis(graph);

    for (Stmt stmt : graph.getStmts()) {
      if (stmt.getUses().size() == 0) continue;

      List<Value> usedVars = stmt.getUses();
      Set<VariableDefinition> inset = analysis.getFlowBefore(stmt);
      reachingDefs.put(stmt, new ArrayList<>());

      for (VariableDefinition def : inset) {
        Value definedVar = def.getValue();
        Optional<Stmt> definingStmt = def.getStmt();

        for (Value usedVar : usedVars)
          if (definedVar == usedVar && definingStmt.isPresent() && definingStmt.get() != stmt)
            reachingDefs.get(stmt).add(definingStmt.get());
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
      ArrayList<LValue> defList = new ArrayList<>();
      for (Stmt stmt : graph.getNodes()) {
        defList.addAll(stmt.getDefs());
      }
      defList.forEach(def -> initialValues.add(new VariableDefinition(def, null)));
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
      out.addAll(gen(d));
    }

    private List<VariableDefinition> kill(Stmt d) {
      if (d.getDefs().size() == 0) return new ArrayList<>();

      LValue definedValue = d.getDefs().get(0);
      List<VariableDefinition> output = new ArrayList<>();
      output.add(new VariableDefinition(definedValue, null));

      for (Stmt stmt : graph) {
        if (stmt.getDefs().contains(definedValue))
          output.add(new VariableDefinition(definedValue, stmt));
      }
      return output;
    }

    private List<VariableDefinition> gen(Stmt d) {
      if (d.getDefs().size() == 0) return new ArrayList<>();

      return Collections.singletonList(new VariableDefinition(d.getDefs().get(0), d));
    }
  }
}
