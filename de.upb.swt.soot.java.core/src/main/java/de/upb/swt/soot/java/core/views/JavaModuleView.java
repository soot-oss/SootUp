package de.upb.swt.soot.java.core.views;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Java View since Java 9.
 *
 * @author Linghui Luo
 */
public class JavaModuleView extends JavaView {

  public JavaModuleView(
      @Nonnull Project project,
      Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    super(project, classLoadingOptionsSpecifier);
  }
  /** Creates a new instance of the {@link JavaView} class. */
  public JavaModuleView(@Nonnull Project project) {
    this(project, analysisInputLocation -> null);
  }

  @Nonnull
  public synchronized Collection<JavaModuleInfo> getModuleInfos() {
    return getAbstractClasses()
        .filter(clazz -> clazz instanceof JavaModuleInfo)
        .map(clazz -> (JavaModuleInfo) clazz)
        .collect(Collectors.toList());
  }

  @Nonnull
  public synchronized Optional<JavaModuleInfo> getModuleInfo(@Nonnull ClassType type) {
    return getAbstractClass(type)
        .map(
            clazz -> {
              if (clazz instanceof JavaModuleInfo) {
                return (JavaModuleInfo) clazz;
              } else {
                throw new ResolveException(type + " is not a module-info class!");
              }
            });
  }
}
