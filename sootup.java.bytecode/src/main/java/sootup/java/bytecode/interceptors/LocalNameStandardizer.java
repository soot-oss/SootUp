package sootup.java.bytecode.interceptors;
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
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/jimple/toolkits/scalar/LocalNameStandardizer.java

/** @author Zun Wang */
public class LocalNameStandardizer implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {

    final Iterator<Local> iterator = getLocalIterator(builder);

    LocalGenerator lgen = new LocalGenerator(new HashSet<>());
    while (iterator.hasNext()) {
      Local local = iterator.next();
      Local newLocal;
      if (local.isFieldLocal()) {
        newLocal = lgen.generateFieldLocal(local.getType());
      } else {
        newLocal = lgen.generateLocal(local.getType());
      }
      builder.replaceLocal(local, newLocal);
    }
  }

  @Nonnull
  public static Iterator<Local> getLocalIterator(@Nonnull Body.BodyBuilder builder) {
    // Get the order of all Locals' occurrences and store them into a map
    Map<Local, Integer> localToFirstOccurrence = new HashMap<>();
    int defsCount = 0;
    for (Stmt stmt : builder.getStmtGraph()) {
      final List<Value> defs = stmt.getDefs();
      for (Value def : defs) {
        if (def instanceof Local) {
          final Local localDef = (Local) def;
          localToFirstOccurrence.putIfAbsent(localDef, defsCount);
          defsCount++;
        }
      }
    }
    // Sort all locals
    return localToFirstOccurrence.keySet().stream()
        .sorted(new LocalComparator(localToFirstOccurrence))
        .iterator();
  }

  public static class LocalComparator implements Comparator<Local> {

    Map<Local, Integer> localToFirstOccurence;

    public LocalComparator(Map<Local, Integer> localToInteger) {
      this.localToFirstOccurence = localToInteger;
    }

    @Override
    public int compare(Local local1, Local local2) {
      int result = local1.getType().toString().compareTo(local2.getType().toString());
      if (result == 0) {
        result =
            Integer.compare(localToFirstOccurence.get(local1), localToFirstOccurence.get(local2));
      }
      return result;
    }
  }
}
