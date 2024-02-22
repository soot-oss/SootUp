package sootup.analysis.interprocedural.ifds;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022 Kadiray Karakaya and others
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

import static org.junit.Assert.*;

import categories.Java8Test;
import heros.InterproceduralCFG;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

@Category(Java8Test.class)
public class IFDSTaintAnalysisTest extends IFDSTaintTestSetUp {

  SootMethod getEntryPointMethod() {
    return entryMethod;
  }

  Set<String> getResultsAtLastStatement(
      JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis) {
    SootMethod m = getEntryPointMethod();
    List<Stmt> stmts = m.getBody().getStmts();
    Set<?> rawSet = analysis.ifdsResultsAt(stmts.get(stmts.size() - 1));
    Set<String> names = new HashSet<>();
    for (Object fact : rawSet) {
      if (fact instanceof Local) {
        Local l = (Local) fact;
        names.add(l.getName());
      }
      if (fact instanceof JInstanceFieldRef) {
        JInstanceFieldRef ins = (JInstanceFieldRef) fact;
        names.add(ins.getBase().getName() + "." + ins.getFieldSignature().getName());
      }
      if (fact instanceof JStaticFieldRef) {
        JStaticFieldRef stat = (JStaticFieldRef) fact;
        names.add(
            stat.getFieldSignature().getDeclClassType() + "." + stat.getFieldSignature().getName());
      }
    }
    return names;
  }

  @Test
  public void SimpleTaint() {
    JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis =
        executeStaticAnalysis("SimpleTaint");
    Set<String> result = getResultsAtLastStatement(analysis);
    assertTrue(result.contains("l1"));
    assertTrue(result.contains("l2"));
    assertTrue(result.contains("SimpleTaint.k"));
  }

  @Test
  public void SimpleTaintSanitized() {
    JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis =
        executeStaticAnalysis("SimpleTaintSanitized");
    Set<String> result = getResultsAtLastStatement(analysis);
    assertTrue(result.contains("l1"));
  }

  @Test
  public void FunctionTaint() {
    JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis =
        executeStaticAnalysis("FunctionTaint");
    Set<String> result = getResultsAtLastStatement(analysis);
    assertTrue(result.contains("l1"));
    assertTrue(result.contains("l2"));
  }

  @Test
  public void FunctionTaintPropagated() {
    JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis =
        executeStaticAnalysis("FunctionTaintPropagated");
    Set<String> result = getResultsAtLastStatement(analysis);
    assertTrue(result + " is missing an element.", result.contains("l1"));
    assertTrue(result + " is missing an element.", result.contains("l2"));
  }

  @Test
  public void FunctionTaintSanitized() {
    JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis =
        executeStaticAnalysis("FunctionTaintSanitized");
    Set<String> result = getResultsAtLastStatement(analysis);
    assertTrue(result.contains("l1"));
    assertFalse(result.contains("l2"));
  }
}
