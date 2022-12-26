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

package driver;

import qilin.util.Util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public enum ContextKind {
    INSENS, CALLSITE, OBJECT, TYPE, HYBOBJ, HYBTYPE;
    static final Map<String, ContextKind> contextKinds = new HashMap<>();

    static {
        Util.add(contextKinds, INSENS, "insensitive", "insens", "ci");
        Util.add(contextKinds, CALLSITE, "callsite", "call", "c");
        Util.add(contextKinds, OBJECT, "object", "obj", "o");
        Util.add(contextKinds, HYBOBJ, "hybobj", "ho", "h");
        Util.add(contextKinds, HYBTYPE, "hybtype", "ht");
        Util.add(contextKinds, TYPE, "type", "t");
    }

    public static Collection<String> contextAliases() {
        return contextKinds.keySet();
    }

    public static ContextKind toCtxKind(String name) {
        return contextKinds.getOrDefault(name, INSENS);
    }

    @Override
    public String toString() {
        return switch (this) {
            case CALLSITE -> "callsite";
            case OBJECT -> "object";
            case HYBOBJ -> "hybobj";
            case HYBTYPE -> "hybtype";
            case TYPE -> "type";
            default -> "insensitive";
        };
    }
}
