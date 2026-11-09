package sootup.examples.typestate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A finite automaton describing the legal call sequence of one API class.
 *
 * <p>States are plain {@code int}s: a state has to fit into the value that the IDE solver
 * propagates, and it has to be cheap to compare. Names are kept only for readable output.
 */
public final class Typestate {

  /** Returned by {@link #trigger} when no transition exists for the given event. */
  public static final int INVALID_STATE = -1;

  private final String[] names;
  private final List<Map<String, Integer>> transitions;
  private final boolean[] accepting;
  private final int initialState;

  private Typestate(
      String[] names, List<Map<String, Integer>> transitions, boolean[] accepting, int initial) {
    this.names = names;
    this.transitions = transitions;
    this.accepting = accepting;
    this.initialState = initial;
  }

  // --8<-- [start:automaton-api]
  public int getStateCount() {
    return names.length;
  }

  public String getStateName(int state) {
    return names[state];
  }

  public int getInitialState() {
    return initialState;
  }

  /**
   * Applies {@code event} — the name of the method that was just called — to {@code state}.
   *
   * @return the successor state, or {@link #INVALID_STATE} if the call is not allowed here.
   */
  public int trigger(int state, String event) {
    if (state < 0 || state >= transitions.size()) {
      return INVALID_STATE;
    }
    Integer target = transitions.get(state).get(event);
    return target == null ? INVALID_STATE : target;
  }

  public boolean isAccepting(int state) {
    return state >= 0 && state < accepting.length && accepting[state];
  }

  // --8<-- [end:automaton-api]

  /** True if {@code event} appears anywhere in the automaton. */
  public boolean knowsEvent(String event) {
    for (Map<String, Integer> outgoing : transitions) {
      if (outgoing.containsKey(event)) {
        return true;
      }
    }
    return false;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return "Typestate" + Arrays.toString(names);
  }

  /** Assembles a {@link Typestate}; states are created first, then wired up with transitions. */
  public static final class Builder {

    private final List<String> names = new ArrayList<>();
    private final List<Map<String, Integer>> transitions = new ArrayList<>();
    private final List<Boolean> accepting = new ArrayList<>();
    private int initialState = INVALID_STATE;

    public int addState(String name) {
      names.add(name);
      transitions.add(new HashMap<>());
      accepting.add(false);
      return names.size() - 1;
    }

    public Builder setInitialState(int state) {
      this.initialState = state;
      return this;
    }

    public Builder setAccepting(int state, boolean isAccepting) {
      accepting.set(state, isAccepting);
      return this;
    }

    /** {@code event} is the unqualified name of the API method that triggers the transition. */
    public Builder addTransition(int from, String event, int to) {
      transitions.get(from).put(event, to);
      return this;
    }

    public Typestate build() {
      if (initialState == INVALID_STATE) {
        throw new IllegalStateException("no initial state set");
      }
      boolean[] acceptingFlags = new boolean[accepting.size()];
      for (int i = 0; i < accepting.size(); i++) {
        acceptingFlags[i] = accepting.get(i);
      }
      return new Typestate(names.toArray(new String[0]), transitions, acceptingFlags, initialState);
    }
  }
}
