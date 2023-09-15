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

package qilin.core.pag;

import qilin.core.context.ContextElement;
import soot.Unit;

/**
 * callsite based context element in the points to analysis.
 */
public class CallSite implements ContextElement {

    private final Unit unit;

    public CallSite(Unit unit) {
        this.unit = unit;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CallSite other = (CallSite) obj;
        if (unit == null) {
            return other.unit == null;
        } else if (other.unit == null) {
            return false;
        } else {
            return unit.equals(other.unit);
        }
    }

    public String toString() {
        return "CallSite: " + unit;
    }
}
