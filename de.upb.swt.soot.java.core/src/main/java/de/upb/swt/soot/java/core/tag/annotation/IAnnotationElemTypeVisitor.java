package de.upb.swt.soot.java.core.tag.annotation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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
import de.upb.swt.soot.java.core.tag.annotation.elem.*;

public interface IAnnotationElemTypeVisitor extends Visitor {
  public abstract void caseAnnotationAnnotationElem(AnnotationAnnotationElem v);

  public abstract void caseAnnotationArrayElem(AnnotationArrayElem v);

  public abstract void caseAnnotationBooleanElem(AnnotationBooleanElem v);

  public abstract void caseAnnotationClassElem(AnnotationClassElem v);

  public abstract void caseAnnotationDoubleElem(AnnotationDoubleElem v);

  public abstract void caseAnnotationEnumElem(AnnotationEnumElem v);

  public abstract void caseAnnotationFloatElem(AnnotationFloatElem v);

  public abstract void caseAnnotationIntElem(AnnotationIntElem v);

  public abstract void caseAnnotationLongElem(AnnotationLongElem v);

  public abstract void caseAnnotationStringElem(AnnotationStringElem v);

  public abstract void defaultCase(Object object);

}
