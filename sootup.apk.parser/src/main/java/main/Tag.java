package main;

public interface Tag {
  /** Returns the tag name. */
  public String getName();

  /** Returns the tag raw data. */
  public byte[] getValue();
}
