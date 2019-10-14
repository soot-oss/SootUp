package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.JavaClassType;

/** @author Markus Schmidt */
public class DefaultSourceTypeSpecifier implements SourceTypeSpecifier {
  public SourceType sourceTypeFor(JavaClassType type) {
    if (type.isJavaLibraryClass()) {
      return SourceType.Library;
    }
    return SourceType.Application;
  }
}
