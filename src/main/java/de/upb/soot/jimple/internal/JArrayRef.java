/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
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






package de.upb.soot.jimple.internal;

import java.util.ArrayList;
import java.util.List;

import de.upb.soot.UnitPrinter;
import de.upb.soot.core.Local;
import de.upb.soot.core.Value;
import de.upb.soot.core.ValueBox;
import de.upb.soot.jimple.ArrayRef;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.RefSwitch;
import de.upb.soot.jimple.Switch;
import de.upb.soot.jimple.type.ArrayType;
import de.upb.soot.jimple.type.NullType;
import de.upb.soot.jimple.type.Type;
import de.upb.soot.jimple.type.UnknownType;

public class JArrayRef implements ArrayRef
{
    protected ValueBox baseBox;
    protected ValueBox indexBox;

  public JArrayRef(Value base, Value index)
    {
        this(Jimple.v().newLocalBox(base),
             Jimple.v().newImmediateBox(index));
    }


    protected JArrayRef(ValueBox baseBox, ValueBox indexBox)
    {
        this.baseBox = baseBox;
        this.indexBox = indexBox;
    }
    
    @Override
    public Object clone() 
    {
        return new JArrayRef(Jimple.cloneIfNecessary(getBase()), Jimple.cloneIfNecessary(getIndex()));
    }

    @Override
    public boolean equivTo(Object o)
    {
        if (o instanceof ArrayRef)
          {
            return (getBase().equivTo(((ArrayRef)o).getBase())
                    && getIndex().equivTo(((ArrayRef)o).getIndex()));
          }
        return false;
    }

    /** Returns a hash code for this object, consistent with structural equality. */
    @Override
    public int equivHashCode() 
    {
        return getBase().equivHashCode() * 101 + getIndex().equivHashCode() + 17;
    }

    @Override
    public String toString()
    {
        return baseBox.getValue().toString() + "[" + indexBox.getValue().toString() + "]";
    }
    
    @Override
    public void toString(UnitPrinter up) {
        baseBox.toString(up);
        up.literal("[");
        indexBox.toString(up);
        up.literal("]");
    }

    @Override
    public Value getBase()
    {
        return baseBox.getValue();
    }

    @Override
    public void setBase(Local base)
    {
        baseBox.setValue(base);
    }

    @Override
    public ValueBox getBaseBox()
    {
        return baseBox;
    }

    @Override
    public Value getIndex()
    {
        return indexBox.getValue();
    }

    @Override
    public void setIndex(Value index)
    {
        indexBox.setValue(index);
    }

    @Override
    public ValueBox getIndexBox()
    {
        return indexBox;
    }

    @Override
    public List getUseBoxes()
    {
        List useBoxes = new ArrayList();

        useBoxes.addAll(baseBox.getValue().getUseBoxes());
        useBoxes.add(baseBox);

        useBoxes.addAll(indexBox.getValue().getUseBoxes());
        useBoxes.add(indexBox);

        return useBoxes;
    }

    @Override
    public Type getType()
    {
        Value base = baseBox.getValue();
        Type type = base.getType();

        if(type.equals(UnknownType.v())) {
          return UnknownType.v();
        } else if(type.equals(NullType.v())) {
          return NullType.v();
        } else {
        	//use makeArrayType on non-array type references when they propagate to this point.
        	//kludge, most likely not correct.
        	//may stop spark from complaining when it gets passed phantoms.
        	// ideally I'd want to find out just how they manage to get this far.
        	ArrayType arrayType;
        	if (type instanceof ArrayType) {
            arrayType = (ArrayType) type;
          } else {
            arrayType = type.makeArrayType();
          }

            if(arrayType.numDimensions == 1) {
              return arrayType.baseType;
            } else {
              return ArrayType.v(arrayType.baseType, arrayType.numDimensions - 1);
            }
        }
    }

    @Override
    public void apply(Switch sw)
    {
        ((RefSwitch) sw).caseArrayRef(this);
    }

}



