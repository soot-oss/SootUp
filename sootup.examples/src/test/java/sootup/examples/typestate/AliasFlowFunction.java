package sootup.examples.typestate;

import heros.FlowFunction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sootup.core.jimple.common.Value;

/**
 * The flow function for an assignment {@code target = source}.
 *
 * <p>It does two things at once: the previous content of {@code target} is killed, because the
 * assignment overwrites it, and a fact for {@code target} is generated whenever the incoming fact
 * is the right-hand side. Passing {@code null} as the right-hand side turns it into a pure kill,
 * which is what an assignment from an expression the analysis does not model needs.
 */
// --8<-- [start:alias-flow]
public final class AliasFlowFunction implements FlowFunction<Value> {

  private final Value target;
  private final Value rightHandSide;

  public AliasFlowFunction(Value target, Value rightHandSide) {
    this.target = target;
    this.rightHandSide = rightHandSide;
  }

  @Override
  public Set<Value> computeTargets(Value source) {
    Set<Value> result = new HashSet<>();
    if (!source.equals(target)) {
      result.add(source);
    }
    if (rightHandSide != null && source.equals(rightHandSide)) {
      result.add(target);
    }
    return result.isEmpty() ? Collections.emptySet() : result;
  }
}
// --8<-- [end:alias-flow]
