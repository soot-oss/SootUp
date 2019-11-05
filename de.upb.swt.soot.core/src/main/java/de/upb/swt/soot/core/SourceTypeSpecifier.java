package de.upb.swt.soot.core;

import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;
import javax.annotation.Nonnull;

/**
 * @author Christian Brüggemann
 * @author Markus Schmidt
 */
public interface SourceTypeSpecifier {

  /**
   * Specifies which {@link SourceType} a specific ClassType maps to.
   *
   * @param type the type
   * @return the source type
   */
  @Nonnull
  SourceType sourceTypeFor(ClassType type);
}
