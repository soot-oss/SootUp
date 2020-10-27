package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.AbstractView;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * The Class JimpleView manages the Sootclasses of the application being analyzed.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */

// TODO: [ms] rethink of that view per language structure -> this could be the base implementation
// for View if we really need different views in the future?
public class JimpleView extends AbstractView {

  @Nonnull
  private final Map<ClassType, AbstractClass<? extends AbstractClassSource>> cache =
      new HashMap<>();

  private volatile boolean isFullyResolved = false;

  @Nonnull
  protected Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier;

  /** Creates a new instance of the {@link de.upb.swt.soot.java.core.views.JavaView} class. */
  public JimpleView(@Nonnull Project project) {
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
      @Nonnull Project project,
      @Nonnull Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    super(project);
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
  }

  @Override
  @Nonnull
  public synchronized Collection<SootClass> getClasses() {
    return getAbstractClassSources()
        .filter(clazz -> clazz instanceof SootClass)
        .map(clazz -> (SootClass) clazz)
        .collect(Collectors.toList());
  }

  @Override
  @Nonnull
  public Stream<SootClass> getClassesStream() {
    return getClasses().stream();
  }

  @Nonnull
  synchronized Stream<AbstractClass<? extends AbstractClassSource>> getAbstractClassSources() {
    resolveAll();
    return cache.values().stream();
  }

  @Override
  @Nonnull
  public synchronized Optional<SootClass> getClass(@Nonnull ClassType type) {
    return getAbstractClass(type).map(clazz -> (SootClass) clazz);
  }

  @Nonnull
  Optional<AbstractClass<? extends AbstractClassSource>> getAbstractClass(@Nonnull ClassType type) {
    AbstractClass<? extends AbstractClassSource> cachedClass = cache.get(type);
    if (cachedClass != null) {
      return Optional.of(cachedClass);
    }

    final List<AbstractClassSource> foundClassSources =
        getProject().getInputLocations().stream()
            .map(
                location -> {
                  ClassLoadingOptions classLoadingOptions =
                      classLoadingOptionsSpecifier.apply(location);
                  if (classLoadingOptions != null) {
                    return location.getClassSource(type, classLoadingOptions);
                  } else {
                    return location.getClassSource(type);
                  }
                })
            .filter(Optional::isPresent)
            .limit(2)
            .map(Optional::get)
            .collect(Collectors.toList());

    if (foundClassSources.size() < 1) {
      throw new ResolveException("No class candidates for \"" + type + "\" found.");
    } else if (foundClassSources.size() > 1) {
      // TODO: print those analysisInputLocation to the user
      throw new ResolveException(
          "Multiple class candidates for \""
              + type
              + "\" found in the given AnalysisInputLocations. Soot can't decide which AnalysisInputLocation it should refer to for this Type.");
    }
    return buildClassFrom(foundClassSources.get(0));
  }

  @Nonnull
  private synchronized Optional<AbstractClass<? extends AbstractClassSource>> buildClassFrom(
      AbstractClassSource classSource) {
    AbstractClass<? extends AbstractClassSource> theClass =
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
              ClassLoadingOptions classLoadingOptions =
                  classLoadingOptionsSpecifier.apply(location);
              if (classLoadingOptions != null) {
                return location.getClassSources(getIdentifierFactory(), classLoadingOptions)
                    .stream();
              } else {
                return location.getClassSources(getIdentifierFactory()).stream();
              }
            })
        .forEach(this::buildClassFrom);
    isFullyResolved = true;
  }

  @Override
  public boolean doneResolving() {
    return isFullyResolved;
  }

  @Override
  @Nonnull
  public String quotedNameOf(@Nonnull String s) {
    return Jimple.escape(s);
  }
}
