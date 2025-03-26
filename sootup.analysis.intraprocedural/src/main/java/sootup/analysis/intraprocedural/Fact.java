package sootup.analysis.intraprocedural;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Local;

/** simple dataflow fact for interprocedural dataflow analysis adaptable with a state enum * */
public class Fact<S> {

  /** The aliases that point to the same object. */
  @NonNull private final Set<Local> aliases;

  /** The state of the object. */
  @NonNull private S state;

  public Fact(@NonNull S initialState) {
    this(new HashSet<>(), initialState);
  }

  public Fact(@NonNull Fact<S> originFact) {
    this(new HashSet<>(originFact.aliases), originFact.state);
  }

  protected Fact(@NonNull Set<Local> aliases, @NonNull S initialState) {
    this.aliases = aliases;
    this.state = initialState;
  }

  public void updateState(@NonNull S state) {
    this.state = state;
  }

  public void addAlias(@NonNull Local alias) {
    this.aliases.add(alias);
  }

  public boolean containsAlias(@NonNull Local value) {
    return aliases.contains(value);
  }

  @NonNull
  public S getState() {
    return state;
  }

  @Override
  public String toString() {
    return "(" + aliases + ", " + state + ")";
  }

  @Override
  public int hashCode() {
    return Objects.hash(aliases, state);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    Fact other = (Fact) obj;
    if (!aliases.equals(other.aliases)) {
      return false;
    }

    return state == other.state;
  }
}
