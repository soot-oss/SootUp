/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core;

import qilin.core.pag.Node;
import qilin.core.sets.PointsToSet;
import soot.Context;
import soot.Local;
import soot.SootField;

/**
 * A generic interface to any type of pointer analysis.
 *
 * @author Ondrej Lhotak
 */

public interface PointsToAnalysis {

    int THIS_NODE = -1;
    int RETURN_NODE = -2;
    int THROW_NODE = -3;
    String STRING_NODE = "STRING_NODE";
    String EXCEPTION_NODE = "EXCEPTION_NODE";
    String MAIN_THREAD_GROUP_NODE_LOCAL = "MAIN_THREAD_GROUP_NODE_LOCAL";

    /**
     * Returns the set of objects pointed to by variable l.
     */
    PointsToSet reachingObjects(Local l);

    PointsToSet reachingObjects(Node n);

    /**
     * Returns the set of objects pointed to by variable l in context c.
     */
    PointsToSet reachingObjects(Context c, Local l);

    /**
     * Returns the set of objects pointed to by static field f.
     */
    PointsToSet reachingObjects(SootField f);

    /**
     * Returns the set of objects pointed to by instance field f of the objects in the PointsToSet s.
     */
    PointsToSet reachingObjects(PointsToSet s, SootField f);

    /**
     * Returns the set of objects pointed to by instance field f of the objects pointed to by l.
     */
    PointsToSet reachingObjects(Local l, SootField f);

    /**
     * Returns the set of objects pointed to by instance field f of the objects pointed to by l in context c.
     */
    PointsToSet reachingObjects(Context c, Local l, SootField f);

    /**
     * Returns the set of objects pointed to by elements of the arrays in the PointsToSet s.
     */
    PointsToSet reachingObjectsOfArrayElement(PointsToSet s);

    /*
     * Return true if l1 and l2 are aliases.
     * */
    boolean mayAlias(Local l1, Local l2);
}
