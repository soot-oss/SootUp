package de.upb.soot.frontends;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.types.JavaClassType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * Converts a single source into Soot IR (Jimple).
 *
 * @author Andreas Dann
 */
public abstract class ModuleClassSource extends AbstractClassSource {


  public ModuleClassSource(INamespace srcNamespace, Path sourcePath, JavaClassType classSignature) {
    super(srcNamespace, classSignature, sourcePath);
  }

  public abstract ModuleSignature getModuleName();

  public abstract Collection<SootModuleInfo.ModuleReference> requires();

  public abstract Collection<SootModuleInfo.PackageReference> exports();

  public abstract Collection<SootModuleInfo.PackageReference> opens();

  public abstract Collection<JavaClassType> provides();

  public abstract Collection<JavaClassType> uses();

  public abstract Set<Modifier> resolveModifiers();

  public abstract Position resolvePosition();
}
