package sootup.analysis.intraprocedural;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Local;

/** simple dataflow fact for interprocedural dataflow analysis adaptable with a state enum * */
public class Fact<S> {

  /** The aliases that point to the same object. */
  @Nonnull private final Set<Local> aliases;

  /** The state of the object. */
  @Nonnull private S state;

  public Fact(@Nonnull S initialState) {
    this(new HashSet<>(), initialState);
  }

  public Fact(@Nonnull Fact<S> originFact) {
    this(new HashSet<>(originFact.aliases), originFact.state);
  }

  protected Fact(@Nonnull Set<Local> aliases, @Nonnull S initialState) {
    this.aliases = aliases;
    this.state = initialState;
  }

  public void updateState(@Nonnull S state) {
    this.state = state;
  }

  public void addAlias(@Nonnull Local alias) {
    this.aliases.add(alias);
  }

  public boolean containsAlias(@Nonnull Local value) {
    return aliases.contains(value);
  }

  @Nonnull
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
