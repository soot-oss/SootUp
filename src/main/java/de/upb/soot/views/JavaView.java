package de.upb.soot.views;

import com.google.common.collect.ImmutableSet;
import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.core.SourceType;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ModuleClassSource;
import de.upb.soot.inputlocation.AnalysisInputLocation;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import de.upb.soot.util.ImmutableUtils;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.collections4.map.LRUMap;

/**
 * The Class JavaView manages the Java classes of the application being analyzed.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */
public class JavaView<S extends AnalysisInputLocation> extends AbstractView<S> {

  private static final int DEFAULT_CACHE_SIZE = 128;

  // region Fields
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

  @Nonnull private final Map<Type, AbstractClass<? extends AbstractClassSource>> map;

  // endregion /Fields/

  // region Constructor

  /** Creates a new instance of the {@link JavaView} class. */
  public JavaView(@Nonnull Project<S> project) {
    this(project, DEFAULT_CACHE_SIZE);
  }

  /**
   * Creates a new instance of the {@link JavaView} class.
   *
   * @param cacheSize Determines how many parsed classes should be retained in a local cache
   */
  public JavaView(@Nonnull Project<S> project, int cacheSize) {
    super(project);
    map = new LRUMap<>(cacheSize);
  }

  // endregion /Constructor/

  // region Methods

  @Override
  @Nonnull
  public synchronized Stream<AbstractClass<? extends AbstractClassSource>> getClasses() {
    return getProject().getInputLocation().getClassSources(getIdentifierFactory()).stream()
        .map(classSource -> getClass(classSource.getClassType()))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  @Override
  @Nonnull
  public synchronized Optional<AbstractClass<? extends AbstractClassSource>> getClass(
      @Nonnull JavaClassType type) {
    AbstractClass<? extends AbstractClassSource> sootClass = this.map.get(type);

    if (sootClass != null) return Optional.of(sootClass);
    else return Optional.ofNullable(this.__resolveSootClass(type));
  }

  @Nullable
  private synchronized AbstractClass<? extends AbstractClassSource> __resolveSootClass(
      @Nonnull JavaClassType signature) {
    AbstractClass<? extends AbstractClassSource> theClass =
        this.getProject()
            .getInputLocation()
            .getClassSource(signature)
            .map(
                it -> {
                  // TODO Don't use a fixed SourceType here.
                  if (it instanceof ClassSource) {
                    return new SootClass((ClassSource) it, SourceType.Application);

                  } else if (it instanceof ModuleClassSource) {
                    return new SootModuleInfo((ModuleClassSource) it, false);
                  }
                  return null;
                })
            .orElse(null);
    if (theClass != null) {
      map.putIfAbsent(theClass.getType(), theClass);
    }
    return theClass;
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

  // endregion /Methods/
}
