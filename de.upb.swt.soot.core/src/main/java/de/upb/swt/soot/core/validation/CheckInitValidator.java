package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class CheckInitValidator implements BodyValidator {

  @Override
  public void validate(Body body, List<ValidationException> exception) {

    // TODO: check code copied from old soot
    /*
     * ExceptionalUnitGraph g = new ExceptionalUnitGraph(body, ThrowAnalysisFactory.checkInitThrowAnalysis(), false);
     *
     * InitAnalysis analysis = new InitAnalysis(g); for (Unit s : body.getUnits()) { FlowSet<Local> init =
     * analysis.getFlowBefore(s); for (ValueBox vBox : s.getUseBoxes()) { Value v = vBox.getValue(); if (v instanceof Local)
     * { Local l = (Local) v; if (!init.contains(l)) { throw new ValidationException(s,
     * "Local variable $1 is not definitively defined at this point".replace("$1", l.getName()), "Warning: Local variable " +
     * l + " not definitely defined at " + s + " in " + body.getMethod(), false); } } } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return false;
  }
}
