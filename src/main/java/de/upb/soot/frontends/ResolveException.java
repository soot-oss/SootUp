package de.upb.soot.frontends;

public class ResolveException extends RuntimeException {
  private static final long serialVersionUID = 1798376682042133224L;

  public ResolveException(String message) {
    super(message);
  }

  public ResolveException(String message, Throwable cause) {
    super(message, cause);
  }
}
