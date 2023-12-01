package tag;

import main.Tag;

public class LongOpTag implements Tag {

  public static final String NAME = "LongOpTag";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public byte[] getValue() {
    return new byte[1];
  }
}
