package de.upb.soot.views;

import static de.upb.soot.util.Utils.valueOrElse;

import com.google.common.collect.ImmutableSet;
import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.core.SourceType;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ModuleClassSource;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import de.upb.soot.util.Utils;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The Class JavaView manages the Java classes of the application being analyzed.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */
public class JavaView extends AbstractView {

  // region Fields
  /** Defines Java's reserved names. */
  @Nonnull
  public static final ImmutableSet<String> RESERVED_NAMES =
      Utils.immutableSet(
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

  @Nonnull private final Map<Type, AbstractClass> map = new HashMap<>();

  // endregion /Fields/

  // region Constructor

  /** Creates a new instance of the {@link JavaView} class. */
  public JavaView(@Nonnull Project project) {
    super(project);
  }

  // endregion /Constructor/

  // region Properties

  private volatile boolean _isFullyResolved;

  /**
   * Gets a value, indicating whether all classes have been loaded.
   *
   * @return The value to get.
   */
  boolean isFullyResolved() {
    return this._isFullyResolved;
  }

  /** Sets a value, indicating that all classes have been loaded. */
  private void markAsFullyResolved() {
    this._isFullyResolved = true;
  }

  // endregion /Properties/

  // region Methods

  @Override
  @Nonnull
  public synchronized Collection<AbstractClass> getClasses() {
    this.resolveAll();

    return Collections.unmodifiableCollection(this.map.values());
  }

  @Override
  @Nonnull
  public synchronized Stream<AbstractClass> classes() {
    return this.getClasses().stream();
  }

  @Override
  @Nonnull
  public synchronized Optional<AbstractClass> getClass(@Nonnull JavaClassType type) {
    AbstractClass sootClass = this.map.get(type);

    if (sootClass != null) return Optional.of(sootClass);
    else if (this.isFullyResolved()) return Optional.empty();
    else return Optional.ofNullable(this.__resolveSootClass(type));
  }

  @Nullable
  private AbstractClass __resolveSootClass(@Nonnull JavaClassType signature) {
    return this.getProject()
        .getNamespace()
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
        .map(it -> valueOrElse(this.map.putIfAbsent(it.getType(), it), it))
        .orElse(null);
  }

  public synchronized void resolveAll() {
    if (this.isFullyResolved()) {
      return;
    }

    this.markAsFullyResolved();

    for (AbstractClassSource cs :
        this.getProject().getNamespace().getClassSources(getIdentifierFactory())) {
      if (!this.map.containsKey(cs.getClassType())) this.__resolveSootClass(cs.getClassType());
    }
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
