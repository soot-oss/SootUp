package de.upb.swt.soot.java.core.views;

import com.google.common.collect.ImmutableSet;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.views.AbstractView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * The Class JavaView manages the Java classes of the application being analyzed.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */
public class JavaView extends AbstractView {

  /** Defines Java's reserved names. */
  @Nonnull
  public static final ImmutableSet<String> RESERVED_NAMES =
      ImmutableUtils.immutableSet(
          "newarray",
          "newmultiarray",
          "nop",
          "ret",
          "specialinvoke",
          "staticinvoke",
          "tableswitch",
          "virtualinvoke",
          "null_type",
          "unknown",
          "cmp",
          "cmpg",
          "cmpl",
          "entermonitor",
          "exitmonitor",
          "interfaceinvoke",
          "lengthof",
          "lookupswitch",
          "neg",
          "if",
          "abstract",
          "annotation",
          "boolean",
          "break",
          "byte",
          "case",
          "catch",
          "char",
          "class",
          "enum",
          "final",
          "native",
          "public",
          "protected",
          "private",
          "static",
          "synchronized",
          "transient",
          "volatile",
          "interface",
          "void",
          "short",
          "int",
          "long",
          "float",
          "double",
          "extends",
          "implements",
          "breakpoint",
          "default",
          "goto",
          "instanceof",
          "new",
          "return",
          "throw",
          "throws",
          "null",
          "from",
          "to",
          "with",
          "cls",
          "dynamicinvoke",
          "strictfp");

  @Nonnull
  private final Map<ClassType, AbstractClass<? extends AbstractClassSource>> map = new HashMap<>();

  private volatile boolean isFullyResolved = false;

  @Nonnull
  protected Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier;

  /** Creates a new instance of the {@link JavaView} class. */
  public JavaView(@Nonnull Project project) {
    this(project, analysisInputLocation -> null);
  }

  /**
   * Creates a new instance of the {@link JavaView} class.
   *
   * @param classLoadingOptionsSpecifier To use the default {@link ClassLoadingOptions} for an
   *     {@link AnalysisInputLocation}, simply return <code>null</code>, otherwise the desired
   *     options.
   */
  public JavaView(
      @Nonnull Project project,
      @Nonnull Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    super(project);
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
  }

  @Override
  @Nonnull
  public synchronized Collection<AbstractClass<? extends AbstractClassSource>> getClasses() {
    resolveAll();

    // The map may be in concurrent use, so we must return a copy
    return new ArrayList<>(map.values());
  }

  @Override
  @Nonnull
  public synchronized Optional<AbstractClass<? extends AbstractClassSource>> getClass(
      @Nonnull ClassType type) {
    AbstractClass<? extends AbstractClassSource> sootClass = map.get(type);
    if (sootClass != null) {
      return Optional.of(sootClass);
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

    if (foundClassSources.size() > 1) {
      throw new ResolveException(
          "Class candidates for \""
              + type
              + "\" found in multiple AnalysisInputLocations. Soot can't decide which AnalysisInputLocation it should refer to for this Type.");
    }
    return foundClassSources.stream().findAny().map(this::getClass).get();
  }

  @Nonnull
  private synchronized Optional<AbstractClass<? extends AbstractClassSource>> getClass(
      AbstractClassSource classSource) {
    AbstractClass<? extends AbstractClassSource> theClass =
        map.computeIfAbsent(
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
        .forEach(this::getClass);
    isFullyResolved = true;
  }

  @Override
  public boolean doneResolving() {
    return isFullyResolved;
  }

  private static final class SplitPatternHolder {
    private static final char SPLIT_CHAR = '.';

    @Nonnull
    private static final Pattern SPLIT_PATTERN =
        Pattern.compile(Character.toString(SPLIT_CHAR), Pattern.LITERAL);
  }

  @Override
  @Nonnull
  public String quotedNameOf(@Nonnull String s) {
    StringBuilder res = new StringBuilder(s.length() + 16);

    for (String part : SplitPatternHolder.SPLIT_PATTERN.split(s)) {
      if (res.length() > 0) {
        res.append(SplitPatternHolder.SPLIT_CHAR);
      }

      if (part.startsWith("-") || RESERVED_NAMES.contains(part)) {
        res.append('\'');
        res.append(part);
        res.append('\'');
      } else {
        res.append(part);
      }
    }

    return res.toString();
  }
}
