package tag;

import main.Tag;

public class FloatOpTag implements Tag {

  public static final String NAME = "FloatOpTag";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public byte[] getValue() {
    return new byte[1];
  }
}
