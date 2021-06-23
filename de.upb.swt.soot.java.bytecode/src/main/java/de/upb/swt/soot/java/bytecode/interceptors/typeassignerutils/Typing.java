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
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;

import java.util.Collection;
import java.util.HashMap;

/**
 * @author Marcus Nachtigall
 */
public class Typing {
    protected HashMap<Local, Type> map;

    public Typing(Collection<Local> vs) {
        map = new HashMap<>(vs.size());
        final UnknownType bottomType = UnknownType.getInstance();
        for (Local v : vs) {
            this.map.put(v, bottomType);
        }
    }

    public Typing(Typing tg) {
        this.map = new HashMap<>(tg.map);
    }

    public Type get(Local v) {
        return this.map.get(v);
    }

    public Type set(Local v, Type t) {
        return this.map.put(v, t);
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append('{');
        for (Local v : this.map.keySet()) {
            stringBuffer.append(v);
            stringBuffer.append(':');
            stringBuffer.append(this.get(v));
            stringBuffer.append(',');
        }
        stringBuffer.append('}');
        return stringBuffer.toString();
    }
}
