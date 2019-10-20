package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ReferenceType;
import javax.annotation.Nonnull;

/**
 * @author Christian Brüggemann
 * @author Markus Schmidt
 */
public interface SourceTypeSpecifier {
  /** Specifies which SourceType a specific JavaClassType maps to. */
  @Nonnull
  SourceType sourceTypeFor(ReferenceType type);
}
