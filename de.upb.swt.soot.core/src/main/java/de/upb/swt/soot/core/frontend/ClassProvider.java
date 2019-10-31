package de.upb.swt.soot.core.frontend;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.types.ReferenceType;
import java.nio.file.Path;

/**
 * Responsible for creating {@link ClassSource}es based on the handled file type (.class, .jimple,
 * .java, .dex, etc).
 *
 * @author Manuel Benz
 */
public interface ClassProvider {

  AbstractClassSource createClassSource(
      AnalysisInputLocation srcNamespace, Path sourcePath, ReferenceType classSignature);

  /** Returns the file type that is handled by this provider, e.g. class, jimple, java */
  FileType getHandledFileType();
}
