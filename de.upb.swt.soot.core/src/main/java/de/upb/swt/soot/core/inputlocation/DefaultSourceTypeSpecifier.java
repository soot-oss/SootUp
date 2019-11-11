package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.SourceTypeSpecifier;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;
import javax.annotation.Nonnull;

/**
 * Implements a very basic version of {@link SourceTypeSpecifier} which tells the type of a class by
 * checking if it is a language build-in class.
 *
 * @author Markus Schmidt
 */
public class DefaultSourceTypeSpecifier implements SourceTypeSpecifier {

  private static final DefaultSourceTypeSpecifier INSTANCE = new DefaultSourceTypeSpecifier();

  /** Singleton to get an Instance of this SourceTypeSpecifier */
  public static DefaultSourceTypeSpecifier getInstance() {
    return INSTANCE;
  }

  private DefaultSourceTypeSpecifier() {}

  @Nonnull
  public SourceType sourceTypeFor(ClassType type) {
    if (type.isBuiltInClass()) {
      return SourceType.Library;
    }
    return SourceType.Application;
  }
}
