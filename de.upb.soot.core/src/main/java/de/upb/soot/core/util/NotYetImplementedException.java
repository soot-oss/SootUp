package de.upb.soot.core.util;

/**
 * Marker exception for not yet implemented functionality
 *
 * @author Ben Hermann
 */
public class NotYetImplementedException extends RuntimeException {
  /**
   * @deprecated Deprecated to remind you to implement the corresponding code before releasing the
   *     software.
   */
  @Deprecated
  public NotYetImplementedException() {}

  /**
   * @deprecated Deprecated to remind you to implement the corresponding code before releasing the
   *     software.
   */
  @Deprecated
  public NotYetImplementedException(String message) {
    super(message);
  }
}
