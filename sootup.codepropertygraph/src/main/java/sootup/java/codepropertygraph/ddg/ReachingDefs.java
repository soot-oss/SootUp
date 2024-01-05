package sootup.java.codepropertygraph.ddg;

import java.util.*;
import javafx.util.Pair;
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
      Set<Pair<Value, Stmt>> inset = analysis.getFlowBefore(stmt);
      reachingDefs.put(stmt, new ArrayList<>());

      for (Pair<Value, Stmt> def : inset) {
        Stmt definingStmt = def.getValue();
        Value definedVar = def.getKey();

        for (Value usedVar : usedVars)
          if (definedVar == usedVar && definingStmt != null && definingStmt != stmt)
            reachingDefs.get(stmt).add(definingStmt);
      }
    }
  }

  public Map<Stmt, List<Stmt>> getReachingDefs() {
    return reachingDefs;
  }

  static class ReachingDefsAnalysis extends ForwardFlowAnalysis<Set<Pair<Value, Stmt>>> {

    /**
     * Construct the analysis from StmtGraph.
     *
     * @param graph
     */
    public <B extends BasicBlock<B>> ReachingDefsAnalysis(StmtGraph<B> graph) {
      super(graph);

      execute();
    }

    @Nonnull
    @Override
    protected Set<Pair<Value, Stmt>> newInitialFlow() {
      Set<Pair<Value, Stmt>> initialValues = new HashSet<>();
      ArrayList<LValue> defList = new ArrayList<>();
      for (Stmt stmt : graph.getNodes()) {
        defList.addAll(stmt.getDefs());
      }
      defList.forEach(def -> initialValues.add(new Pair<>(def, null)));
      return initialValues;
    }

    @Override
    protected void merge(
        @Nonnull Set<Pair<Value, Stmt>> in1,
        @Nonnull Set<Pair<Value, Stmt>> in2,
        @Nonnull Set<Pair<Value, Stmt>> out) {
      out.clear();
      out.addAll(in1);
      out.addAll(in2);
    }

    @Override
    protected void copy(
        @Nonnull Set<Pair<Value, Stmt>> source, @Nonnull Set<Pair<Value, Stmt>> dest) {
      dest.clear();
      dest.addAll(source);
    }

    @Override
    protected void flowThrough(
        @Nonnull Set<Pair<Value, Stmt>> in, Stmt d, @Nonnull Set<Pair<Value, Stmt>> out) {
      out.clear();
      out.addAll(in);
      kill(d).forEach(out::remove);
      out.addAll(gen(d));
    }

    private List<Pair<Value, Stmt>> kill(Stmt d) {
      if (d.getDefs().size() == 0) return new ArrayList<>();

      LValue definedValue = d.getDefs().get(0);
      List<Pair<Value, Stmt>> output = new ArrayList<>();
      output.add(new Pair<>(definedValue, null));

      for (Stmt stmt : graph) {
        if (stmt.getDefs().contains(definedValue)) output.add(new Pair<>(definedValue, stmt));
      }
      return output;
    }

    private List<Pair<Value, Stmt>> gen(Stmt d) {
      if (d.getDefs().size() == 0) return new ArrayList<>();

      return Collections.singletonList(new Pair<>(d.getDefs().get(0), d));
    }
  }
}
