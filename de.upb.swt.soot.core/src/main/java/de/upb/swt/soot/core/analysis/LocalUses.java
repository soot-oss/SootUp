package de.upb.swt.soot.core.analysis;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2021 Raja Vallee-Rai, Kadiray Karakaya and others
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

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/** Provides an interface to find the Units that use a Local defined at a given Unit. */
public interface LocalUses {

  /**
   * Returns a list of the Stmt that use the Local that is defined by a given Stmt.
   *
   * @param s the stmt we wish to query for the use of the Local it defines.
   * @return a list of the uses Local's uses.
   */
  public List<Pair<Stmt, Value>> getUsesOf(Stmt s);

  /** */
  public static final class Factory {
    private Factory() {}

    public static LocalUses newLocalUses(Body body, LocalDefs localDefs) {
      return new SimpleLocalUses(body, localDefs);
    }

    public static LocalUses newLocalUses(Body body) {
      return newLocalUses(body, LocalDefs.Factory.newLocalDefs(body));
    }
  }
}
