package sootup.core.model;

/**
 * Interface to mark Soot code objects that may contain code location information.
 *
 * @author David Baker Effendi
 */
public interface HasPosition {

  /**
   * Line and column information of the corresponding code object that this represents.
   *
   * @return a {@link sootup.core.model.Position} containing position information.
   */
  Position getPosition();
}
