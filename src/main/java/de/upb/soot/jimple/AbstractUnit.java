package de.upb.soot.jimple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.upb.soot.core.Unit;

public abstract class AbstractUnit implements Unit {

  /** Returns a deep clone of this object. */
  @Override
  public abstract Object clone();

  /**
   * Returns a list of Boxes containing Values used in this Unit. The list of boxes is dynamically
   * updated as the structure changes. Note that they are returned in usual evaluation order. (this
   * is important for aggregation)
   */
  @Override
  public List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  /**
   * Returns a list of Boxes containing Values defined in this Unit. The list of boxes is
   * dynamically updated as the structure changes.
   */
  @Override
  public List<ValueBox> getDefBoxes() {
    return Collections.emptyList();
  }

  /**
   * Returns a list of Boxes containing Units defined in this Unit; typically branch targets. The
   * list of boxes is dynamically updated as the structure changes.
   */
  @Override
  public List<UnitBox> getUnitBoxes() {
    return Collections.emptyList();
  }

  /** List of UnitBoxes pointing to this Unit. */
  List<UnitBox> boxesPointingToThis = null;

  /** Returns a list of Boxes pointing to this Unit. */
  @Override
  public List<UnitBox> getBoxesPointingToThis() {
    if (boxesPointingToThis == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(boxesPointingToThis);
  }

  @Override
  public void addBoxPointingToThis(UnitBox b) {
    if (boxesPointingToThis == null) {
      boxesPointingToThis = new ArrayList<UnitBox>();
    }
    boxesPointingToThis.add(b);
  }

  @Override
  public void removeBoxPointingToThis(UnitBox b) {
    if (boxesPointingToThis != null) {
      boxesPointingToThis.remove(b);
    }
  }

  @Override
  public void clearUnitBoxes() {
    for (UnitBox ub : getUnitBoxes()) {
      ub.setUnit(null);
    }
  }

  /** Returns a list of ValueBoxes, either used or defined in this Unit. */
  @Override
  public List<ValueBox> getUseAndDefBoxes() {
    List<ValueBox> useBoxes = getUseBoxes();
    List<ValueBox> defBoxes = getDefBoxes();
    if (useBoxes.isEmpty()) {
      return defBoxes;
    } else {
      if (defBoxes.isEmpty()) {
        return useBoxes;
      } else {
        List<ValueBox> valueBoxes = new ArrayList<ValueBox>();
        valueBoxes.addAll(defBoxes);
        valueBoxes.addAll(useBoxes);
        return valueBoxes;
      }
    }
  }

  /** Used to implement the Switchable construct. */
  @Override
  public void accept(IVisitor sw) {
  }

  @Override
  public void redirectJumpsToThisTo(Unit newLocation) {
    List<UnitBox> boxesPointing = getBoxesPointingToThis();

    UnitBox[] boxes = boxesPointing.toArray(new UnitBox[boxesPointing.size()]);
    // important to change this to an array to have a static copy

    for (UnitBox element : boxes) {
      UnitBox box = element;

      if (box.getUnit() != this) {
        throw new RuntimeException("Something weird's happening");
      }

      if (box.isBranchTarget()) {
        box.setUnit(newLocation);
      }
    }

  }
}

