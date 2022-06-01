package de.upb.swt.soot.core.typehierarchy;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann
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
import de.upb.swt.soot.core.views.View.ModuleDataKey;
import java.util.function.Supplier;

/**
 * Used to store a caching supplier of the {@link TypeHierarchy} in a {@link
 * de.upb.swt.soot.core.views.View} without the core module needing a dependency on this module.
 *
 * @see #getInstance()
 * @author Christian Brüggemann
 */
class TypeHierarchyKey extends ModuleDataKey<Supplier<MutableTypeHierarchy>> {
  private static final TypeHierarchyKey instance = new TypeHierarchyKey();

  static TypeHierarchyKey getInstance() {
    return instance;
  }

  private TypeHierarchyKey() {}
}
