package de.upb.soot.frontends.java;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/** @author Linghui Luo */
public class PositionTag implements Tag {

  private Position pos;

  @Override
  public String getName() {
    return "PositionTag";
  }

  @Override
  public byte[] getValue() throws AttributeValueException {
    return pos.toString().getBytes();
  }

  public PositionTag(Position pos) {
    this.pos = pos;
  }

  public Position getPosition() {
    return this.pos;
  }

  @Override
  public String toString() {
    if (pos != null) {
      return pos.toString();
    } else {
      return "No position";
    }
  }
}
