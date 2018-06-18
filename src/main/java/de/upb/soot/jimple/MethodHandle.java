/* Soot - a J*va Optimization Framework
 * Copyright (C) 2005 - Jennifer Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */





package de.upb.soot.jimple;

import de.upb.soot.core.SootMethodRef;
import de.upb.soot.jimple.constant.Constant;
import de.upb.soot.jimple.type.RefType;
import de.upb.soot.jimple.type.Type;
import de.upb.soot.jimple.visitor.IConstantVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

public class MethodHandle extends Constant
{
    public final SootMethodRef methodRef;
    public int tag;

    private MethodHandle(SootMethodRef ref, int tag)
    {
        this.methodRef = ref;
        this.tag = tag;
    }

    public static MethodHandle v(SootMethodRef ref, int tag)
    {
        return new MethodHandle(ref, tag);
    }

    @Override
    public String toString()
    {
        return "handle: "+ methodRef;
    }

    @Override
    public Type getType()
    {
        return RefType.v("java.lang.invoke.MethodHandle");
    }
    
    public SootMethodRef getMethodRef() {
		return methodRef;
	}

    public void accept(IVisitor sw)
    {
        ((IConstantVisitor) sw).caseMethodHandle(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methodRef == null) ? 0 : methodRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
      return true;
    }
		if (obj == null) {
      return false;
    }
		if (getClass() != obj.getClass()) {
      return false;
    }
		MethodHandle other = (MethodHandle) obj;
		if (methodRef == null) {
			if (other.methodRef != null) {
        return false;
      }
		} else if (!methodRef.equals(other.methodRef)) {
      return false;
    }
		return true;
	}
}
