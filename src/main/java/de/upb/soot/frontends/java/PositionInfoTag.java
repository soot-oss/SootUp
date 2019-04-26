package de.upb.soot.frontends.java;

import de.upb.soot.jimple.basic.PositionInfo;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/** @author Linghui Luo */
public class PositionInfoTag implements Tag {

  private PositionInfo stmtPos;

  @Override
  public String getName() {
    return "PositionInfoTag";
  }

  @Override
  public byte[] getValue() throws AttributeValueException {
    return stmtPos.toString().getBytes();
  }

  public PositionInfoTag(PositionInfo stmtPos) {
    this.stmtPos = stmtPos;
  }

  public PositionInfo getPositionInfo() {
    return this.stmtPos;
  }

  @Override
  public String toString() {
    if (stmtPos != null) {
      return stmtPos.toString();
    } else {
      return "No position info";
    }
  }
}
