/*
 * @author Linghui Luo
 */
package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Abstract class for module source.
 *
 * @author Linghui Luo
 */
public abstract class AbstractModuleClassSource extends AbstractClassSource {

  public AbstractModuleClassSource(
      AnalysisInputLocation srcNamespace, ClassType classSignature, Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
  }

  public AbstractClass buildClass(@Nonnull SourceType sourceType) {
    return new JavaModuleInfo(this, false);
  }

  public abstract String getModuleName();

  public abstract Collection<JavaModuleInfo.ModuleReference> requires();

  public abstract Collection<JavaModuleInfo.PackageReference> exports();

  public abstract Collection<JavaModuleInfo.PackageReference> opens();

  public abstract Collection<JavaClassType> provides();

  public abstract Collection<JavaClassType> uses();

  public abstract Set<Modifier> resolveModifiers();

  public abstract Position resolvePosition();
}
