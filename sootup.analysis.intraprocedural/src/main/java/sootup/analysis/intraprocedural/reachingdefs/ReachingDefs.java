package sootup.analysis.intraprocedural.reachingdefs;

/*-
* #%L
* Soot - a J*va Optimization Framework
* %%
Copyright (C) 2024 Michael Youkeim, Stefan Schott and others
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

import java.util.*;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
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
      if (!stmt.getUses().findAny().isPresent()) continue;

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
    <B extends BasicBlock<B>> ReachingDefsAnalysis(StmtGraph<B> graph) {
      super(graph);
      execute();
    }

    @NonNull
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
        @NonNull Set<VariableDefinition> in1,
        @NonNull Set<VariableDefinition> in2,
        @NonNull Set<VariableDefinition> out) {
      out.clear();
      out.addAll(in1);
      out.addAll(in2);
    }

    @Override
    protected void copy(
        @NonNull Set<VariableDefinition> source, @NonNull Set<VariableDefinition> dest) {
      dest.clear();
      dest.addAll(source);
    }

    @Override
    protected void flowThrough(
        @NonNull Set<VariableDefinition> in, Stmt d, @NonNull Set<VariableDefinition> out) {
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
