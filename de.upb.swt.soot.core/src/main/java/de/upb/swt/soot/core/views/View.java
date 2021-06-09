package de.upb.swt.soot.core.views;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Ben Hermann, Christian Brüggemann and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.Scope;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A View is essentially a collection of code belonging to a {@link de.upb.swt.soot.core.Project}.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public interface View<T extends SootClass<?>> {

  /** Return all classes in the view. */
  @Nonnull
  Collection<T> getClasses();

  /**
   * Return a class with given signature.
   *
   * @return A class with given signature.
   */
  @Nonnull
  Optional<T> getClass(@Nonnull ClassType signature);

  Optional<? extends SootField> getField(@Nonnull FieldSignature signature);

  Optional<? extends SootMethod> getMethod(@Nonnull MethodSignature signature);

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

  @Nonnull
  default T getClassOrThrow(@Nonnull ClassType classType) {
    return getClass(classType)
        .orElseThrow(() -> new ResolveException("Could not find " + classType + " in View."));
  }

  /** @see ModuleDataKey */
  <K> void putModuleData(@Nonnull ModuleDataKey<K> key, @Nonnull K value);

  /** @see ModuleDataKey */
  @Nullable
  <K> K getModuleData(@Nonnull ModuleDataKey<K> key);

  /**
   * @see java.util.Map#computeIfAbsent(Object, Function)
   * @see ModuleDataKey
   */
  default <K> K computeModuleDataIfAbsent(@Nonnull ModuleDataKey<K> key, Supplier<K> dataSupplier) {
    K moduleData = getModuleData(key);
    if (moduleData != null) {
      return moduleData;
    }

    K computedModuleData = dataSupplier.get();
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
   * @param <K> The type of the stored and retrieved data that is associated with the key
   * @author Christian Brüggemann
   */
  @SuppressWarnings("unused") // Used in modules
  abstract class ModuleDataKey<K> {}
}
