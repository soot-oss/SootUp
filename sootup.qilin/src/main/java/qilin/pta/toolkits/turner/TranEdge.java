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

package qilin.pta.toolkits.turner;

import java.util.Objects;

public class TranEdge {
    private final Object src;
    private final Object dst;
    private final DFA.TranCond tranCond;

    public TranEdge(Object s, Object d, DFA.TranCond tran) {
        this.src = s;
        this.dst = d;
        this.tranCond = tran;
    }

    public Object getSource() {
        return src;
    }

    public Object getTarget() {
        return dst;
    }

    public DFA.TranCond getTranCond() {
        return tranCond;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranEdge tranEdge = (TranEdge) o;
        return Objects.equals(src, tranEdge.src) &&
                Objects.equals(dst, tranEdge.dst) &&
                tranCond == tranEdge.tranCond;
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, dst, tranCond);
    }
}