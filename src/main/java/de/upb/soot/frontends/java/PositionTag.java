package de.upb.soot.frontends.java;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

public class PositionTag implements Tag {

  private Position stmtPos;

  @Override
  public String getName() {
    return "position tag";
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
}
