package de.upb.swt.soot.java.core.views;

import com.google.common.collect.ImmutableSet;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.views.AbstractView;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * The Class JavaView manages the Java classes of the application being analyzed.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */
public class JavaView<S extends AnalysisInputLocation> extends AbstractView<S> {

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
  private final Map<Type, AbstractClass<? extends AbstractClassSource>> map = new HashMap<>();

  private volatile boolean isFullyResolved = false;

  /** Creates a new instance of the {@link JavaView} class. */
  public JavaView(@Nonnull Project<S> project) {
    super(project);
  }

  @Override
  @Nonnull
  public synchronized Collection<AbstractClass<? extends AbstractClassSource>> getClasses() {
    this.resolveAll();

    // The map may be in concurrent use, so we must return a copy
    return new ArrayList<>(map.values());
  }

  @Override
  @Nonnull
  public synchronized Optional<AbstractClass<? extends AbstractClassSource>> getClass(
      @Nonnull ClassType type) {
    AbstractClass<? extends AbstractClassSource> sootClass = this.map.get(type);
    if (sootClass != null) {
      return Optional.of(sootClass);
    }

    final List<AbstractClassSource> foundClassSources =
        getProject().getInputLocations().stream()
            .map(location -> location.getClassSource(type))
            .filter(Optional::isPresent)
            .limit(2)
            .map(Optional::get)
            .collect(Collectors.toList());

    if (foundClassSources.size() > 1) {
      throw new ResolveException(
          "Class candidates for \"" + type + "\" found in multiple AnalysisInputLocations.");
    }
    return foundClassSources.stream().findAny().map(this::getClass).get();
  }

  @Nonnull
  private synchronized Optional<AbstractClass<? extends AbstractClassSource>> getClass(
      AbstractClassSource classSource) {
    AbstractClass<? extends AbstractClassSource> theClass =
        this.map.get(classSource.getClassType());
    if (theClass == null) {
      theClass =
          classSource.buildClass(
              getProject().getSourceTypeSpecifier().sourceTypeFor(classSource.getClassType()));
      map.putIfAbsent(theClass.getType(), theClass);
    }
    return Optional.of(theClass);
  }

  private synchronized void resolveAll() {
    if (isFullyResolved) {
      return;
    }
    // Calling getClass fills the map
    getProject().getInputLocations().stream()
        .flatMap(location -> location.getClassSources(getIdentifierFactory()).stream())
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

  // TODO: [ms] usecase?
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
