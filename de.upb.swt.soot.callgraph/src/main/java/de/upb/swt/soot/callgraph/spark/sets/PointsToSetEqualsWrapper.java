package de.upb.swt.soot.callgraph.spark.sets;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2007-2021 Eric Bodden, Kadiray Karakaya and others
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

import de.upb.swt.soot.callgraph.spark.pointsto.PointsToSet;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.types.Type;

import java.util.Set;

/**
 * A decorator that implements equals/hashCode for {@link PointsToSet} supporting the {@link EqualsSupportingPointsToSet}
 * interface.
 *
 * @author Kadiray Karakaya
 */
public class PointsToSetEqualsWrapper implements PointsToSet {

    protected EqualsSupportingPointsToSet pts;

    public PointsToSetEqualsWrapper(EqualsSupportingPointsToSet pts) {
        this.pts = pts;
    }

    public EqualsSupportingPointsToSet unwarp() {
        return this.pts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        // delegate
        return pts.pointsToSetHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj || this.pts == obj) {
            return true;
        }

        // unwrap other
        obj = unwrapIfNecessary(obj);
        // delegate
        return pts.pointsToSetEquals(obj);
    }

    /**
     * @param other
     * @return
     * @see PointsToSet#hasNonEmptyIntersection(soot.PointsToSet)
     */
    public boolean hasNonEmptyIntersection(PointsToSet other) {
        // unwrap other
        other = (PointsToSet) unwrapIfNecessary(other);
        return pts.hasNonEmptyIntersection(other);
    }

    /**
     * @return
     * @see PointsToSet#isEmpty()
     */
    public boolean isEmpty() {
        return pts.isEmpty();
    }

    /**
     * @return
     * @see PointsToSet#possibleClassConstants()
     */
    public Set<ClassConstant> possibleClassConstants() {
        return pts.possibleClassConstants();
    }

    /**
     * @return
     * @see PointsToSet#possibleStringConstants()
     */
    public Set<String> possibleStringConstants() {
        return pts.possibleStringConstants();
    }

    /**
     * @return
     * @see PointsToSet#possibleTypes()
     */
    public Set<Type> possibleTypes() {
        return pts.possibleTypes();
    }

    protected Object unwrapIfNecessary(Object obj) {
        if (obj instanceof PointsToSetEqualsWrapper) {
            PointsToSetEqualsWrapper wrapper = (PointsToSetEqualsWrapper) obj;
            obj = wrapper.pts;
        }
        return obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return pts.toString();
    }
}