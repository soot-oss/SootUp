package sootup.examples.typestate;

import java.util.Objects;

/**
 * The value type {@code V} of the IDE problem: what the analysis knows about one tracked object at
 * one program point.
 *
 * <p>Besides the automaton states themselves there are three special values. {@link #TOP} is the
 * neutral element of {@link TypestateProblem}'s meet lattice ("nothing known yet"), {@link #BOTTOM}
 * is its absorbing element ("two branches disagree"), and {@link #ERROR} records that a call was
 * made that the automaton does not allow.
 */
// --8<-- [start:fact]
public final class TypestateFact {

  static final int CODE_ERROR = -1;
  static final int CODE_TOP = -2;
  static final int CODE_BOTTOM = -3;

  /** No information yet. The IDE solver starts every value at top. */
  public static final TypestateFact TOP = new TypestateFact(null, CODE_TOP);

  /** Two execution paths reach this point in different states. */
  public static final TypestateFact BOTTOM = new TypestateFact(null, CODE_BOTTOM);

  /** A call was made that the automaton does not allow — the protocol was violated. */
  public static final TypestateFact ERROR = new TypestateFact(null, CODE_ERROR);

  private final Typestate automaton;
  private final int code;

  private TypestateFact(Typestate automaton, int code) {
    this.automaton = automaton;
    this.code = code;
  }

  /** Wraps a state of {@code automaton}; invalid states collapse to {@link #ERROR}. */
  public static TypestateFact of(Typestate automaton, int code) {
    switch (code) {
      case CODE_TOP:
        return TOP;
      case CODE_BOTTOM:
        return BOTTOM;
      case CODE_ERROR:
        return ERROR;
      default:
        return new TypestateFact(automaton, code);
    }
  }

  // --8<-- [end:fact]

  /** The internal encoding: a state index, or one of the {@code CODE_*} constants. */
  int code() {
    return code;
  }

  public Typestate getAutomaton() {
    return automaton;
  }

  /** True if the object is in a state in which the program may legally stop using it. */
  public boolean isAccepting() {
    return automaton != null && automaton.isAccepting(code);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TypestateFact)) {
      return false;
    }
    TypestateFact other = (TypestateFact) o;
    return code == other.code && Objects.equals(automaton, other.automaton);
  }

  @Override
  public int hashCode() {
    return Objects.hash(automaton, code);
  }

  @Override
  public String toString() {
    switch (code) {
      case CODE_TOP:
        return "TOP";
      case CODE_BOTTOM:
        return "BOTTOM";
      case CODE_ERROR:
        return "ERROR";
      default:
        return automaton.getStateName(code);
    }
  }
}
