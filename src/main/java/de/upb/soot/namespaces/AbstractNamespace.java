package de.upb.soot.namespaces;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 22.05.2018 Manuel Benz
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

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Basic implementation of {@link INamespace}, encapsulating common behavior. Also used to keep the {@link INamespace}
 * interface clean from internal methods like {@link AbstractNamespace#getClassSource(ClassSignature)}.
 *
 * @author Manuel Benz created on 22.05.18
 */
public abstract class AbstractNamespace implements INamespace {
  protected final IClassProvider classProvider;

  public AbstractNamespace(IClassProvider classProvider) {
    this.classProvider = classProvider;
  }

  @Override
  public Collection<SootClass> getClasses() {
    return getClassSources().stream().map(cs -> new SootClass(cs)).collect(Collectors.toList());
  }

  @Override
  public Optional<SootClass> getClass(ClassSignature classSignature) {
    return getClassSource(classSignature).map(cs -> new SootClass(cs));
  }

  protected abstract Collection<ClassSource> getClassSources();

  protected abstract Optional<ClassSource> getClassSource(ClassSignature classSignature);
}
