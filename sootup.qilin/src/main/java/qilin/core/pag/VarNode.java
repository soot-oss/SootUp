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

import qilin.util.DataFactory;
import soot.AnySubType;
import soot.Context;
import soot.RefLikeType;
import soot.Type;
import soot.jimple.spark.pag.SparkField;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Represents a simple variable node in the pointer assignment graph.
 *
 * @author Ondrej Lhotak
 */
public abstract class VarNode extends ValNode {
    protected Object variable;
    protected Map<SparkField, FieldRefNode> fields;

    protected boolean interProcTarget = false;
    protected boolean interProcSource = false;

    protected VarNode(Object variable, Type t) {
        super(t);
        if (!(t instanceof RefLikeType) || t instanceof AnySubType) {
            throw new RuntimeException("Attempt to create VarNode of type " + t);
        }
        this.variable = variable;
    }

    public Context context() {
        return null;
    }

    /**
     * Returns all field ref nodes having this node as their base.
     */
    public Collection<FieldRefNode> getAllFieldRefs() {
        if (fields == null) {
            return Collections.emptyList();
        }
        return fields.values();
    }

    /**
     * Returns the field ref node having this node as its base, and field as its field; null if nonexistent.
     */
    public FieldRefNode dot(SparkField field) {
        return fields == null ? null : fields.get(field);
    }

    /**
     * Returns the underlying variable that this node represents.
     */
    public Object getVariable() {
        return variable;
    }

    /**
     * Designates this node as the potential target of a interprocedural assignment edge which may be added during on-the-fly
     * call graph updating.
     */
    public void setInterProcTarget() {
        interProcTarget = true;
    }

    /**
     * Returns true if this node is the potential target of a interprocedural assignment edge which may be added during
     * on-the-fly call graph updating.
     */
    public boolean isInterProcTarget() {
        return interProcTarget;
    }

    /**
     * Designates this node as the potential source of a interprocedural assignment edge which may be added during on-the-fly
     * call graph updating.
     */
    public void setInterProcSource() {
        interProcSource = true;
    }

    /**
     * Returns true if this node is the potential source of a interprocedural assignment edge which may be added during
     * on-the-fly call graph updating.
     */
    public boolean isInterProcSource() {
        return interProcSource;
    }

    public abstract VarNode base();

    /**
     * Registers a frn as having this node as its base.
     */
    void addField(FieldRefNode frn, SparkField field) {
        if (fields == null) {
            synchronized (this) {
                if (fields == null) {
                    fields = DataFactory.createMap();
                }
            }
        }
        fields.put(field, frn);
    }
}
