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

package qilin.pta.toolkits.conch;

public enum Trilean {
    TRUE, FALSE, UNKNOWN;

    public static Trilean OR(Trilean x1, Trilean x2) {
        if (x1 == TRUE || x2 == TRUE) {
            return TRUE;
        } else if (x1 == UNKNOWN || x2 == UNKNOWN) {
            return UNKNOWN;
        } else {
            return FALSE;
        }
    }
}
