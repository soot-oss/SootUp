package de.upb.soot.jimple.basic;

import de.upb.soot.core.Position;

/**
 * This class represents the case when there is no position.
 *
 * @author Linghui Luo
 */
public class NoPositionInformation extends Position {

  public NoPositionInformation() {
    super(-1, -1, -1, -1);
  }

  @Override
  public String toString() {
    return "No position info";
  }
}
