package de.upb.swt.soot.core;

import de.upb.swt.soot.core.inputlocation.SourceTypeSpecifier;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;

/**
 * Implements a very basic Version of a Source Specifier
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

  public SourceType sourceTypeFor(ClassType type) {
    if (type.isBuiltInClass()) {
      return SourceType.Library;
    }
    return SourceType.Application;
  }
}
