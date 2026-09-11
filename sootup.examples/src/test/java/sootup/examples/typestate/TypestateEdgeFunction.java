package sootup.examples.typestate;

import heros.EdgeFunction;
import heros.edgefunc.AllTop;
import heros.edgefunc.EdgeIdentity;
import java.util.Arrays;

/**
 * An edge function over {@link TypestateFact}s, represented as an explicit transfer table.
 *
 * <p>Heros requires edge functions to support composition and meet, and it composes them
 * repeatedly while building method summaries. Representing a function as "apply this list of
 * events" would let the representation grow without bound inside loops, and the solver would never
 * reach a fixed point. Because the automaton has finitely many states, every function {@code
 * TypestateFact -> TypestateFact} can instead be written down as a table: one entry per state, plus
 * one entry for each of the three special values. Composition and meet then work entry by entry and
 * {@link #equalTo} is a table comparison — so only finitely many distinct edge functions exist and
 * the solver terminates.
 */
public final class TypestateEdgeFunction implements EdgeFunction<TypestateFact> {

  // --8<-- [start:edge-function-table]
  /** Table slots for the three values that are not automaton states. */
  private static final int SLOT_TOP = 0;

  private static final int SLOT_BOTTOM = 1;
  private static final int SLOT_ERROR = 2;
  private static final int SLOT_COUNT = 3;

  private final Typestate automaton;

  /** {@code table[slotOf(code)]} is the result for an incoming value encoded as {@code code}. */
  private final int[] table;

  private static int slotOf(int code) {
    switch (code) {
      case TypestateFact.CODE_TOP:
        return SLOT_TOP;
      case TypestateFact.CODE_BOTTOM:
        return SLOT_BOTTOM;
      case TypestateFact.CODE_ERROR:
        return SLOT_ERROR;
      default:
        return SLOT_COUNT + code;
    }
  }

  /** The value a given slot stands for — the inverse of {@link #slotOf}. */
  private static int codeOf(int slot) {
    switch (slot) {
      case SLOT_TOP:
        return TypestateFact.CODE_TOP;
      case SLOT_BOTTOM:
        return TypestateFact.CODE_BOTTOM;
      case SLOT_ERROR:
        return TypestateFact.CODE_ERROR;
      default:
        return slot - SLOT_COUNT;
    }
  }

  private int apply(int incomingCode) {
    return table[slotOf(incomingCode)];
  }
  // --8<-- [end:edge-function-table]

  private TypestateEdgeFunction(Typestate automaton, int[] table) {
    this.automaton = automaton;
    this.table = table;
  }

  // --8<-- [start:edge-function-factories]
  /**
   * The constant function returning the automaton's initial state, used where a tracked object is
   * created. It has to be constant: the fact is generated out of the zero fact, and the zero fact
   * carries the bottom value — "reachable", not "in some state".
   */
  public static TypestateEdgeFunction generating(Typestate automaton) {
    int[] table = new int[SLOT_COUNT + automaton.getStateCount()];
    Arrays.fill(table, automaton.getInitialState());
    return new TypestateEdgeFunction(automaton, table);
  }

  /**
   * The function performing one protocol step. A state that has no transition for {@code event}
   * maps to {@link TypestateFact#ERROR}; the three special values map to themselves.
   */
  public static TypestateEdgeFunction transition(Typestate automaton, String event) {
    int[] table = new int[SLOT_COUNT + automaton.getStateCount()];
    table[SLOT_TOP] = TypestateFact.CODE_TOP;
    table[SLOT_BOTTOM] = TypestateFact.CODE_BOTTOM;
    // once the protocol is broken it stays broken
    table[SLOT_ERROR] = TypestateFact.CODE_ERROR;
    for (int state = 0; state < automaton.getStateCount(); state++) {
      table[SLOT_COUNT + state] = automaton.trigger(state, event);
    }
    return new TypestateEdgeFunction(automaton, table);
  }
  // --8<-- [end:edge-function-factories]

  /** The meet of two encoded values; mirrors {@link TypestateProblem}'s meet lattice. */
  private static int meetCode(int left, int right) {
    if (left == right) {
      return left;
    }
    if (left == TypestateFact.CODE_TOP) {
      return right;
    }
    if (right == TypestateFact.CODE_TOP) {
      return left;
    }
    if (left == TypestateFact.CODE_ERROR || right == TypestateFact.CODE_ERROR) {
      return TypestateFact.CODE_ERROR;
    }
    return TypestateFact.CODE_BOTTOM;
  }

  // --8<-- [start:edge-function-ops]
  @Override
  public TypestateFact computeTarget(TypestateFact source) {
    return TypestateFact.of(automaton, apply(source.code()));
  }

  /** Apply {@code this} first, then {@code secondFunction} — entry by entry. */
  @Override
  public EdgeFunction<TypestateFact> composeWith(EdgeFunction<TypestateFact> secondFunction) {
    if (secondFunction instanceof EdgeIdentity) {
      return this;
    }
    if (secondFunction instanceof TypestateEdgeFunction) {
      TypestateEdgeFunction second = (TypestateEdgeFunction) secondFunction;
      if (second.automaton == automaton) {
        int[] composed = new int[table.length];
        for (int slot = 0; slot < table.length; slot++) {
          composed[slot] = second.apply(table[slot]);
        }
        return new TypestateEdgeFunction(automaton, composed);
      }
    }
    return secondFunction;
  }

  @Override
  public EdgeFunction<TypestateFact> meetWith(EdgeFunction<TypestateFact> otherFunction) {
    if (otherFunction == this || equalTo(otherFunction)) {
      return this;
    }
    if (otherFunction instanceof AllTop) {
      return this;
    }
    int[] met = new int[table.length];
    if (otherFunction instanceof EdgeIdentity) {
      // the identity maps every value to itself, so meet it slot by slot
      for (int slot = 0; slot < table.length; slot++) {
        met[slot] = meetCode(codeOf(slot), table[slot]);
      }
      return new TypestateEdgeFunction(automaton, met);
    }
    if (otherFunction instanceof TypestateEdgeFunction) {
      TypestateEdgeFunction other = (TypestateEdgeFunction) otherFunction;
      if (other.automaton == automaton) {
        for (int slot = 0; slot < table.length; slot++) {
          met[slot] = meetCode(table[slot], other.table[slot]);
        }
        return new TypestateEdgeFunction(automaton, met);
      }
    }
    return otherFunction;
  }

  @Override
  public boolean equalTo(EdgeFunction<TypestateFact> other) {
    if (!(other instanceof TypestateEdgeFunction)) {
      return false;
    }
    TypestateEdgeFunction that = (TypestateEdgeFunction) other;
    return automaton == that.automaton && Arrays.equals(table, that.table);
  }
  // --8<-- [end:edge-function-ops]

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("edge{");
    for (int slot = 0; slot < table.length; slot++) {
      if (slot > 0) {
        sb.append(", ");
      }
      sb.append(TypestateFact.of(automaton, codeOf(slot)))
          .append("->")
          .append(TypestateFact.of(automaton, table[slot]));
    }
    return sb.append('}').toString();
  }
}
