package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
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

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.frontend.AsmUtil;
import de.upb.swt.soot.java.core.AnnotationUsage;
import de.upb.swt.soot.java.core.JavaSootField;
import de.upb.swt.soot.java.core.JavaTaggedSootClass;
import de.upb.swt.soot.java.core.JavaTaggedSootField;
import de.upb.swt.soot.java.core.tag.*;
import de.upb.swt.soot.java.core.types.AnnotationType;
import de.upb.swt.soot.java.core.views.JavaView;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents all instance and static fields of a dex class. It holds its name, its modifier, and the type
 */
public class DexField {
  private DexField() {
  }

  /**
   * Add constant tag. Should only be called if field is final.
   * 
   * @param df
   * @param sf
   */
  private static void addConstantTag(JavaTaggedSootField df, Field sf) {
    Tag tag = null;

    EncodedValue ev = sf.getInitialValue();

    if (ev instanceof BooleanEncodedValue) {
      tag = new IntegerConstantValueTag(((BooleanEncodedValue) ev).getValue() == true ? 1 : 0);
    } else if (ev instanceof ByteEncodedValue) {
      tag = new IntegerConstantValueTag(((ByteEncodedValue) ev).getValue());
    } else if (ev instanceof CharEncodedValue) {
      tag = new IntegerConstantValueTag(((CharEncodedValue) ev).getValue());
    } else if (ev instanceof DoubleEncodedValue) {
      tag = new DoubleConstantValueTag(((DoubleEncodedValue) ev).getValue());
    } else if (ev instanceof FloatEncodedValue) {
      tag = new FloatConstantValueTag(((FloatEncodedValue) ev).getValue());
    } else if (ev instanceof IntEncodedValue) {
      tag = new IntegerConstantValueTag(((IntEncodedValue) ev).getValue());
    } else if (ev instanceof LongEncodedValue) {
      tag = new LongConstantValueTag(((LongEncodedValue) ev).getValue());
    } else if (ev instanceof ShortEncodedValue) {
      tag = new IntegerConstantValueTag(((ShortEncodedValue) ev).getValue());
    } else if (ev instanceof StringEncodedValue) {
      tag = new StringConstantValueTag(((StringEncodedValue) ev).getValue());
    }

    if (tag != null) {
      df.addTag(tag);
    }
  }

  /**
   *
   * @return the Soot equivalent of a field
   */
  public static JavaTaggedSootField makeSootField(JavaTaggedSootClass declaringClass, Field f, JavaView view) {
    String name = f.getName();
    Type type = DexType.toSoot(f.getType());
    int flags = f.getAccessFlags();
    FieldSignature fieldSignature = new FieldSignature(declaringClass.getSootClass().getType(), name, type);
    EnumSet<Modifier> modifiers = AsmUtil.getModifiers(flags);
    Set<? extends Annotation> annotations = f.getAnnotations();
    DexAnnotation da = new DexAnnotation(declaringClass);
    da.handleFieldAnnotation(declaringClass, f);
    List<Tag> tags = declaringClass.getTags();
    JavaSootField sootField = new JavaSootField(fieldSignature, modifiers, null, null);
    JavaTaggedSootField sf = new JavaTaggedSootField(sootField);
    DexField.addConstantTag(sf, f);
    return sf;
  }
}
