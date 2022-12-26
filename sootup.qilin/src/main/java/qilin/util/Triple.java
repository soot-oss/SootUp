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

package qilin.util;

import java.util.Objects;

public class Triple<T1, T2, T3> {
    private final T1 first;
    private final T2 second;
    private final T3 third;

    public Triple(final T1 first, final T2 second, final T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T1 getFirst() {
        return this.first;
    }

    public T2 getSecond() {
        return this.second;
    }

    public T3 getThird() {
        return this.third;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.first, this.second, this.third);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final Triple<?, ?, ?> anoTriple) {
            return Objects.equals(this.first, anoTriple.first) && Objects.equals(this.second, anoTriple.second)
                    && Objects.equals(this.third, anoTriple.third);
        }
        return false;
    }

    @Override
    public String toString() {
        return "<" + this.first + ", " + this.second + ", "
                + this.third + ">";
    }
}
