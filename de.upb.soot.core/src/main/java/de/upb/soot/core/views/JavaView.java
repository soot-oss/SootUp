package de.upb.soot.core.views;

import com.google.common.collect.ImmutableSet;
import de.upb.soot.core.frontend.AbstractClassSource;
import de.upb.soot.core.frontend.ClassSource;
import de.upb.soot.core.frontend.ResolveException;
import de.upb.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.soot.core.Project;
import de.upb.soot.core.model.AbstractClass;
import de.upb.soot.core.model.SootClass;
import de.upb.soot.core.model.SootModuleInfo;
import de.upb.soot.core.model.SourceType;
import de.upb.soot.frontend.ModuleClassSource;
import de.upb.soot.core.types.JavaClassType;
import de.upb.soot.core.types.Type;
import de.upb.soot.core.util.ImmutableUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * The Class JavaView manages the Java classes of the application being analyzed.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */
public class JavaView<S extends AnalysisInputLocation> extends AbstractView<S> {

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

  @Nonnull
  private final Map<Type, AbstractClass<? extends AbstractClassSource>> map = new HashMap<>();

  private volatile boolean isFullyResolved = false;

  // endregion /Fields/

  // region Constructor

  /** Creates a new instance of the {@link JavaView} class. */
  public JavaView(@Nonnull Project<S> project) {
    super(project);
  }

  // endregion /Constructor/

  // region Methods

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
      @Nonnull JavaClassType type) {
    AbstractClass<? extends AbstractClassSource> sootClass = this.map.get(type);
    if (sootClass != null) {
      return Optional.of(sootClass);
    }

    return getProject().getInputLocation().getClassSource(type).flatMap(this::getClass);
  }

  @Nonnull
  private synchronized Optional<AbstractClass<? extends AbstractClassSource>> getClass(
      AbstractClassSource classSource) {
    AbstractClass<? extends AbstractClassSource> sootClass =
        this.map.get(classSource.getClassType());
    if (sootClass != null) {
      return Optional.of(sootClass);
    }

    AbstractClass<? extends AbstractClassSource> theClass;
    if (classSource instanceof ClassSource) {
      // TODO Don't use a fixed SourceType here.
      theClass = new SootClass((ClassSource) classSource, SourceType.Application);
    } else if (classSource instanceof ModuleClassSource) {
      theClass = new SootModuleInfo((ModuleClassSource) classSource, false);
    } else {
      throw new ResolveException("AbstractClassSource has unknown type " + classSource);
    }

    map.putIfAbsent(theClass.getType(), theClass);
    return Optional.of(theClass);
  }

  private synchronized void resolveAll() {
    if (!isFullyResolved) {
      // Calling getClass fills the map
      getProject()
          .getInputLocation()
          .getClassSources(getIdentifierFactory())
          .forEach(this::getClass);
      isFullyResolved = true;
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
