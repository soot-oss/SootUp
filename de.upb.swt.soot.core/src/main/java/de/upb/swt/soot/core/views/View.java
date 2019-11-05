package de.upb.swt.soot.core.views;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.Scope;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A view on code.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public interface View {

  /** Return all classes in the view. */
  @Nonnull
  Collection<AbstractClass<? extends AbstractClassSource>> getClasses();

  /** Return all classes in the view. */
  @Nonnull
  default Stream<AbstractClass<? extends AbstractClassSource>> getClassesStream() {
    return getClasses().stream();
  }

  /**
   * Return a class with given signature.
   *
   * @return A class with given signature.
   */
  @Nonnull
  Optional<AbstractClass<? extends AbstractClassSource>> getClass(@Nonnull ClassType signature);

  /**
   * Returns the scope if the view is scoped.
   *
   * @return The scope that led to the view
   */
  @Nonnull
  Optional<Scope> getScope();

  /** Returns the {@link IdentifierFactory} for this view. */
  @Nonnull
  IdentifierFactory getIdentifierFactory();

  boolean doneResolving();

  @Nonnull
  String quotedNameOf(@Nonnull String name);

  /** @see ModuleDataKey */
  <T> void putModuleData(@Nonnull ModuleDataKey<T> key, @Nonnull T value);

  /** @see ModuleDataKey */
  @Nullable
  <T> T getModuleData(@Nonnull ModuleDataKey<T> key);

  /**
   * @see java.util.Map#computeIfAbsent(Object, Function)
   * @see ModuleDataKey
   */
  default <T> T computeModuleDataIfAbsent(@Nonnull ModuleDataKey<T> key, Supplier<T> dataSupplier) {
    T moduleData = getModuleData(key);
    if (moduleData != null) {
      return moduleData;
    }

    T computedModuleData = dataSupplier.get();
    putModuleData(key, computedModuleData);
    return computedModuleData;
  }

  /**
   * A key for use with {@link #getModuleData(ModuleDataKey)}, {@link #putModuleData(ModuleDataKey,
   * Object)} and {@link #computeModuleDataIfAbsent(ModuleDataKey, Supplier)}. This allows
   * additional data to be stored or cached inside a {@link View} and to be retrieved in a type-safe
   * manner. A {@link ModuleDataKey} of type <code>T</code> can only be used to store and retrieve
   * data of type <code>T</code>.
   *
   * <p>Additionally, since it is an abstract class and not an interface, it can be assured that a
   * given class can only be a key for a single type, which avoids clashes.
   *
   * <p>Example: <br>
   * <br>
   *
   * <pre>
   *   class StringDataKey extends ModuleDataKey&lt;String&gt; {
   *     public static final StringDataKey instance = new StringDataKey();
   *     private StringDataKey() {}
   *   }
   *
   *   void storeInView(String str, View view) {
   *     view.putModuleData(StringDataKey.instance, str);
   *     String retrieved = view.getModuleData(StringDataKey.instance);
   *   }
   * </pre>
   *
   * @param <T> The type of the stored and retrieved data that is associated with the key
   * @author Christian Brüggemann
   */
  @SuppressWarnings("unused") // Used in modules
  abstract class ModuleDataKey<T> {}
}
