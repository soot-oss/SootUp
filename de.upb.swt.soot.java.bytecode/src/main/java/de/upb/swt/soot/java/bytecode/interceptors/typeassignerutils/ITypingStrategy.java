package de.upb.swt.soot.java.bytecode.interceptors.typeassignerutils;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2008 Ben Bellamy
 *
 * All rights reserved.
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

import de.upb.swt.soot.core.jimple.basic.Local;

import java.util.List;

/**
 * Provides a way to use different was to create and minimize typings
 *
 * @author Marcus Nachtigall
 */
public interface ITypingStrategy {

    /**
     * Creates a new typing class instance with initialized bottom types for the given locals
     *
     * @param locals the locals
     * @return the created typing
     */
    public Typing createTyping(List<Local> locals);

    /**
     * Creates a new typing class as a copy from a given class
     *
     * @param typing the original typing used as a copy
     * @return the newly created typing
     */
    public Typing createTyping(Typing typing);

    /**
     * Minimize the given typing list using the hierarchy
     *
     * @param typings the typing list
     * @param hierarchy the hierarchy
     */
    public void minimize(List<Typing> typings, IHierarchy hierarchy);
}
