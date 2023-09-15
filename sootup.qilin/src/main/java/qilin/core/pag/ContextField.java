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

import qilin.CoreConfig;
import qilin.core.context.ContextElement;
import qilin.core.context.ContextElements;
import soot.ArrayType;
import soot.Context;
import soot.RefType;
import soot.Type;
import soot.jimple.spark.pag.SparkField;

public class ContextField extends ValNode {
    protected Context context;
    protected SparkField field;

    public ContextField(Context context, SparkField field) {
        super(refineFieldType(context, field));
        this.context = context;
        this.field = field;
    }

    private static Type refineFieldType(Context context, SparkField field) {
        if (!CoreConfig.v().getPtaConfig().preciseArrayElement) {
            return RefType.v("java.lang.Object");
        }
        if (field instanceof ArrayElement) {
            ContextElement[] contextElements = ((ContextElements) context).getElements();
            if (contextElements.length > 0) {
                Type baseHeapType = ((AllocNode) ((ContextElements) context).getElements()[0]).getType();
                if (baseHeapType instanceof ArrayType arrayType) {
                    return arrayType.getArrayElementType();
                } else {
                    throw new RuntimeException(baseHeapType + " is not an array type.");
                }
            } else {
                throw new RuntimeException("Context does not have any elements:" + context + ";" + field);
            }
        }
        return field.getType();
    }

    /**
     * Returns the base AllocNode.
     */
    public Context getContext() {
        return context;
    }

    public boolean hasBase() {
        ContextElements ctxs = (ContextElements) context;
        return ctxs.size() > 0;
    }

    public AllocNode getBase() {
        ContextElements ctxs = (ContextElements) context;
        return (AllocNode) ctxs.getElements()[0];
    }

    /**
     * Returns the field of this node.
     */
    public SparkField getField() {
        return field;
    }

    public String toString() {
        return "ContextField " + getNumber() + " " + getBase() + "." + field;
    }
}
