package de.upb.soot.core.inputlocation;

import javax.annotation.Nullable;

public class ClassResolvingException extends Throwable {

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
