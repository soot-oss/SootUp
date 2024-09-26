package sootup.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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
import javax.annotation.Nonnull;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.Type;
import sootup.core.views.View;

/** @author Zun Wang */
public class LocalNameStandardizer implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View view) {

    MutableStmtGraph graph = builder.getStmtGraph();
    // Get the order of all Locals' occurrences and store them into a map
    Map<Local, Integer> localToFirstOccurrence = new HashMap<>();
    int defsCount = 0;
    for (Stmt stmt : graph) {
      final Optional<LValue> defOpt = stmt.getDef();
      if (defOpt.isPresent()) {
        LValue def = defOpt.get();
        if (def instanceof Local) {
          final Local localDef = (Local) def;
          localToFirstOccurrence.putIfAbsent(localDef, defsCount);
          defsCount++;
        }
      }
    }

    final Iterator<Local> iterator =
        localToFirstOccurrence.keySet().stream()
            .sorted(new LocalComparator(localToFirstOccurrence))
            .iterator();

    LocalGenerator lgen = new LocalGenerator(new HashSet<>());
    while (iterator.hasNext()) {
      Local local = iterator.next();
      Local newLocal;
      Type type = local.getType();
      newLocal = lgen.generateLocal(type);
      builder.replaceLocal(local, newLocal);
    }
  }

  public static class LocalComparator implements Comparator<Local> {

    Map<Local, Integer> localToFirstOccurence;

    public LocalComparator(Map<Local, Integer> localToInteger) {
      this.localToFirstOccurence = localToInteger;
    }

    @Override
    public int compare(Local localA, Local localB) {
      int result = localA.getType().toString().compareTo(localB.getType().toString());
      if (result == 0) {
        result =
            Integer.compare(localToFirstOccurence.get(localA), localToFirstOccurence.get(localB));
      }
      return result;
    }
  }
}
