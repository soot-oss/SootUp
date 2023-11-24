package sootup.jimple.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;
import sootup.core.SourceTypeSpecifier;
import sootup.core.cache.ClassCache;
import sootup.core.cache.provider.ClassCacheProvider;
import sootup.core.cache.provider.FullCacheProvider;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.inputlocation.DefaultSourceTypeSpecifier;
import sootup.core.inputlocation.EmptyClassLoadingOptions;
import sootup.core.model.SootClass;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.AbstractView;

/**
 * The Class JimpleView manages the Sootclasses of the application being analyzed.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */

// TODO: [ms] rethink of that view per language structure -> this could be the base implementation
// for View if we really need different views in the future?
public class JimpleView extends AbstractView<SootClass<?>> {

  @Nonnull protected final List<AnalysisInputLocation<SootClass<?>>> inputLocations;
  @Nonnull private final ClassCache<SootClass<?>> cache;
  @Nonnull protected final SourceTypeSpecifier sourceTypeSpecifier;

  private volatile boolean isFullyResolved = false;

  @Nonnull
  protected Function<AnalysisInputLocation<SootClass<?>>, ClassLoadingOptions>
      classLoadingOptionsSpecifier;

  public JimpleView(@Nonnull AnalysisInputLocation<SootClass<?>> inputLocation) {
    this(Collections.singletonList(inputLocation), new FullCacheProvider<>());
  }

  public JimpleView(@Nonnull List<AnalysisInputLocation<SootClass<?>>> inputLocations) {
    this(inputLocations, new FullCacheProvider<>());
  }

  public JimpleView(
      @Nonnull List<AnalysisInputLocation<SootClass<?>>> inputLocations,
      @Nonnull ClassCacheProvider<SootClass<?>> cacheProvider) {
    this(inputLocations, cacheProvider, analysisInputLocation -> EmptyClassLoadingOptions.Default);
  }

  public JimpleView(
      @Nonnull List<AnalysisInputLocation<SootClass<?>>> inputLocations,
      @Nonnull ClassCacheProvider<SootClass<?>> cacheProvider,
      @Nonnull
          Function<AnalysisInputLocation<SootClass<?>>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    this(
        inputLocations,
        cacheProvider,
        classLoadingOptionsSpecifier,
        DefaultSourceTypeSpecifier.getInstance());
  }

  public JimpleView(
      @Nonnull List<AnalysisInputLocation<SootClass<?>>> inputLocations,
      @Nonnull ClassCacheProvider<SootClass<?>> cacheProvider,
      @Nonnull
          Function<AnalysisInputLocation<SootClass<?>>, ClassLoadingOptions>
              classLoadingOptionsSpecifier,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    this.inputLocations = inputLocations;
    this.cache = cacheProvider.createCache();
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
    this.sourceTypeSpecifier = sourceTypeSpecifier;
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors(AnalysisInputLocation inputLocation) {
    return classLoadingOptionsSpecifier.apply(inputLocation).getBodyInterceptors();
  }

  @Nonnull
  private List<BodyInterceptor> getBodyInterceptors() {
    return Collections.emptyList();
  }

  public void configBodyInterceptors(
      @Nonnull
          Function<AnalysisInputLocation<SootClass<?>>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
  }

  @Override
  @Nonnull
  public synchronized Collection<SootClass<?>> getClasses() {
    return getAbstractClassSources();
  }

  @Nonnull
  synchronized Collection<SootClass<?>> getAbstractClassSources() {
    resolveAll();
    return cache.getClasses();
  }

  @Override
  @Nonnull
  public synchronized Optional<SootClass<?>> getClass(@Nonnull ClassType type) {
    return getAbstractClass(type);
  }

  @Nonnull
  @Override
  public IdentifierFactory getIdentifierFactory() {
    return new JimpleLanguage().getIdentifierFactory();
  }

  @Nonnull
  Optional<SootClass<?>> getAbstractClass(@Nonnull ClassType type) {
    SootClass<?> cachedClass = cache.getClass(type);
    if (cachedClass != null) {
      return Optional.of(cachedClass);
    }

    final List<? extends AbstractClassSource<SootClass<?>>> foundClassSources =
        inputLocations.stream()
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

    ClassType classType = classSource.getClassType();
    SootClass<?> theClass;
    if (!cache.hasClass(classType)) {
      theClass = classSource.buildClass(sourceTypeSpecifier.sourceTypeFor(classSource));
      cache.putClass(classType, theClass);
    } else {
      theClass = cache.getClass(classType);
    }

    return Optional.of(theClass);
  }

  private synchronized void resolveAll() {
    if (isFullyResolved) {
      return;
    }

    inputLocations.stream()
        .flatMap(location -> location.getClassSources(this).stream())
        .forEach(this::buildClassFrom);
    isFullyResolved = true;
  }
}
