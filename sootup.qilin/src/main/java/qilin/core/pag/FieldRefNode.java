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


import qilin.core.util.Numberable;
import sootup.core.views.View;

/**
 * Represents a field reference node in the pointer assignment graph.
 *
 * @author Ondrej Lhotak
 */
public class FieldRefNode extends Node implements Numberable {
    protected VarNode base;
    protected SparkField field;

    public FieldRefNode(View view, VarNode base, SparkField field) {
        super(view, field.getType());
        this.base = base;
        this.field = field;
        base.addField(this, field);
    }

    /**
     * Returns the base of this field reference.
     */
    public VarNode getBase() {
        return base;
    }

    /**
     * Returns the field of this field reference.
     */
    public SparkField getField() {
        return field;
    }

    public String toString() {
        return "FieldRefNode " + getNumber() + " " + base + "." + field;
    }
}
