package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.AbstractView;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * The Class JimpleView manages the Sootclasses of the application being analyzed.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */

// TODO: [ms] rethink of that view per language structure -> this could be the base implementation
// for View if we really need different views in the future?
public class JimpleView extends AbstractView<SootClass<?>> {

  @Nonnull private final Map<ClassType, SootClass<?>> cache = new HashMap<>();

  private volatile boolean isFullyResolved = false;

  @Nonnull
  protected Function<AnalysisInputLocation<SootClass<?>>, ClassLoadingOptions>
      classLoadingOptionsSpecifier;

  /** Creates a new instance of the {@link de.upb.swt.soot.java.core.views.JavaView} class. */
  public JimpleView(@Nonnull JimpleProject project) {
    this(project, analysisInputLocation -> null);
  }

  /**
   * Creates a new instance of the {@link de.upb.swt.soot.java.core.views.JavaView} class.
   *
   * @param classLoadingOptionsSpecifier To use the default {@link ClassLoadingOptions} for an
   *     {@link AnalysisInputLocation}, simply return <code>null</code>, otherwise the desired
   *     options.
   */
  public JimpleView(
      @Nonnull Project<JimpleView, ?> project,
      @Nonnull
          Function<AnalysisInputLocation<SootClass<?>>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    super(project);
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
  }

  @Nonnull
  public List<BodyInterceptor> getBodyInterceptors(AnalysisInputLocation<SootClass<?>> clazz) {
    return classLoadingOptionsSpecifier.apply(clazz) != null
        ? classLoadingOptionsSpecifier.apply(clazz).getBodyInterceptors()
        : getBodyInterceptors();
  }

  @Nonnull
  public List<BodyInterceptor> getBodyInterceptors() {
    return Collections.emptyList();
  }

  @Override
  @Nonnull
  public synchronized Collection<SootClass<?>> getClasses() {
    return getAbstractClassSources();
  }

  @Nonnull
  synchronized Collection<SootClass<?>> getAbstractClassSources() {
    resolveAll();
    return cache.values();
  }

  @Override
  @Nonnull
  public synchronized Optional<SootClass<?>> getClass(@Nonnull ClassType type) {
    return getAbstractClass(type);
  }

  @Nonnull
  Optional<SootClass<?>> getAbstractClass(@Nonnull ClassType type) {
    SootClass<?> cachedClass = cache.get(type);
    if (cachedClass != null) {
      return Optional.of(cachedClass);
    }

    final List<AbstractClassSource<SootClass<?>>> foundClassSources =
        getProject().getInputLocations().stream()
            .map(location -> location.getClassSource(type, this))
            .filter(Optional::isPresent)
            .limit(2)
            .map(Optional::get)
            .collect(Collectors.toList());

    if (foundClassSources.size() < 1) {
      return Optional.empty();
    } else if (foundClassSources.size() > 1) {
      throw new ResolveException(
          "Multiple class candidates for \""
              + type
              + "\" found in the given AnalysisInputLocations. Soot can't decide which AnalysisInputLocation it should refer to for this Type.\n"
              + "The candidates are "
              + foundClassSources.stream()
                  .map(cs -> cs.getSourcePath().toString())
                  .collect(Collectors.joining(",")),
          foundClassSources.get(0).getSourcePath());
    }
    return buildClassFrom(foundClassSources.get(0));
  }

  @Nonnull
  private synchronized Optional<SootClass<?>> buildClassFrom(
      AbstractClassSource<SootClass<?>> classSource) {
    SootClass<?> theClass =
        cache.computeIfAbsent(
            classSource.getClassType(),
            type ->
                classSource.buildClass(getProject().getSourceTypeSpecifier().sourceTypeFor(type)));
    return Optional.of(theClass);
  }

  private synchronized void resolveAll() {
    if (isFullyResolved) {
      return;
    }

    getProject().getInputLocations().stream()
        .flatMap(
            location -> {
              return location.getClassSources(getIdentifierFactory(), this).stream();
            })
        .forEach(this::buildClassFrom);
    isFullyResolved = true;
  }
}
