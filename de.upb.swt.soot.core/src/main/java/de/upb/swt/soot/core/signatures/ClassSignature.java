package de.upb.swt.soot.core.signatures;

import de.upb.swt.soot.core.inputlocation.FileType;
import java.nio.file.Path;

/**
 * This Interface represents
 *
 * @author Markus Schmidt
 */
public interface ClassSignature extends Signature {
  String getClassName();

  String getFullyQualifiedName();

  Path toPath(FileType fileType);

  // TODO: [ms] choose a more generic name? namespace?
  PackageName getPackageName();

  boolean isInnerClass();

  boolean isBuiltInClass();

  //  ClassSignature getType(); --> consistent here with getBaseType from ArraySignature?

}
