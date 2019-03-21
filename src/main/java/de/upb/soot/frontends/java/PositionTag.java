package de.upb.soot.frontends.java;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/** @author Linghui Luo */
public class PositionTag implements Tag {

  private Position stmtPos;

  @Override
  public String getName() {
    return "PositionTag";
  }

  @Override
  public byte[] getValue() throws AttributeValueException {
    return stmtPos.toString().getBytes();
  }

  public PositionTag(Position stmtPos) {
    this.stmtPos = stmtPos;
  }

  public Position getPosition() {
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
