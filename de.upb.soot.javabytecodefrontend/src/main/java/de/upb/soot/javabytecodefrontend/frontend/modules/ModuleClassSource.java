package de.upb.soot.javabytecodefrontend.frontend.modules;

import de.upb.soot.core.model.Modifier;
import de.upb.soot.core.model.Position;
import de.upb.soot.core.model.SootModuleInfo;
import de.upb.soot.core.frontend.AbstractClassSource;
import de.upb.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.soot.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * Converts a single source into Soot IR (Jimple).
 *
 * @author Andreas Dann
 */
public abstract class ModuleClassSource extends AbstractClassSource {

  public ModuleClassSource(
      AnalysisInputLocation srcNamespace, Path sourcePath, JavaClassType classSignature) {
    super(srcNamespace, classSignature, sourcePath);
  }

  public abstract String getModuleName();

  public abstract Collection<SootModuleInfo.ModuleReference> requires();

  public abstract Collection<SootModuleInfo.PackageReference> exports();

  public abstract Collection<SootModuleInfo.PackageReference> opens();

  public abstract Collection<JavaClassType> provides();

  public abstract Collection<JavaClassType> uses();

  public abstract Set<Modifier> resolveModifiers();

  public abstract Position resolvePosition();
}
