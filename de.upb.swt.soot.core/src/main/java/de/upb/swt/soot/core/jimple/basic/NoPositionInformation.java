package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.model.Position;

/**
 * This class represents the case when there is no position.
 *
 * @author Linghui Luo
 */
public class NoPositionInformation extends Position {

  private static final NoPositionInformation INSTANCE = new NoPositionInformation();

  private NoPositionInformation() {
    super(-1, -1, -1, -1);
  }

  public static NoPositionInformation getInstance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "No position info";
  }
}
