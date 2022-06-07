package de.upb.swt.soot.java.core.tag.annotation.elem;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2005 Jennifer Lhotak
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

import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.java.core.tag.annotation.IAnnotationElemTypeVisitor;

import javax.annotation.Nonnull;

/**
 * Represents the Class annotation element each annotation can have several elements for Java 1.5.
 */
public class AnnotationClassElem extends AnnotationElem {

  private final String desc;

  public AnnotationClassElem(String s, char kind, String name) {
    super(kind, name);
    this.desc = s;
  }

  @Override
  public String toString() {
    return super.toString() + " decription: " + desc;
  }

  public String getDesc() {
    return desc;
  }


  @Override
  public void accept(@Nonnull Visitor v) {
    ((IAnnotationElemTypeVisitor) v).caseAnnotationClassElem(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((desc == null) ? 0 : desc.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    AnnotationClassElem other = (AnnotationClassElem) obj;
    if (this.desc == null) {
      if (other.desc != null) {
        return false;
      }
    } else if (!this.desc.equals(other.desc)) {
      return false;
    }
    return true;
  }


}
