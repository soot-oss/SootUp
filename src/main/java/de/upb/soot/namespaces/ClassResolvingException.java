package de.upb.soot.namespaces;

import javax.annotation.Nullable;

public class ClassResolvingException extends Throwable {
  /**
   * 
   */
  private static final long serialVersionUID = 5301366501158710956L;

  public ClassResolvingException(@Nullable String message) {
    super(message);
  }
  
  public ClassResolvingException(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }
  
  public ClassResolvingException(@Nullable Throwable cause) {
    super(cause);
  }
}
