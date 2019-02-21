/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 15.11.2018 Markus Schmidt
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

package de.upb.soot.jimple;

import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.Local;

public class IgnoreLocalNameComparator extends JimpleComparator {

    public boolean caseLocal(Local obj, Object o) {
        if( !(o instanceof Local)){
            return false;
        }
        return obj.getType().equals( ((Local) o).getType());

    }
}
