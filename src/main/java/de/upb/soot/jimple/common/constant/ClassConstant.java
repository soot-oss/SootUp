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

package de.upb.soot.jimple.common.constant;

import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.jimple.visitor.IConstantVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.types.Type;
import de.upb.soot.util.StringTools;

@SuppressWarnings("serial")
public class ClassConstant implements Constant {
  private final String value;

  private ClassConstant(String s) {
    this.value = s;
  }

  /** Returns an instance of ClassConstant. */
  public static ClassConstant getInstance(String value) {
    if (value.contains(".")) {
      throw new RuntimeException("ClassConstants must use class names separated by '/', not '.'!");
    }
    return new ClassConstant(value);
  }

  // FIXME The following code is commented out due to incompatibility, but
  //   may still be needed.
  //   https://github.com/secure-software-engineering/soot-reloaded/pull/89#discussion_r266982906

  //  public static ClassConstant fromType(Type tp) {
  //    return getInstance(sootTypeToString(tp));
  //  }
  //
  //  private static String sootTypeToString(Type tp) {
  //    if (tp instanceof ArrayType) {
  //      ArrayType at = (ArrayType) tp;
  //      // TODO: [JMP] It was `at.elementType` before; please ensure that the element type is here
  // used.
  //      return "[" + sootTypeToString(at.baseType);
  //    } else if (tp instanceof ReferenceType) {
  //      return "L" + tp.toString().replaceAll("\\.", "/") + ";";
  //    } else if (tp instanceof PrimitiveType) {
  //      if (tp instanceof IntType) {
  //        return "I";
  //      } else if (tp instanceof ByteType) {
  //        return "B";
  //      } else if (tp instanceof CharType) {
  //        return "C";
  //      } else if (tp instanceof DoubleType) {
  //        return "D";
  //      } else if (tp instanceof FloatType) {
  //        return "F";
  //      } else if (tp instanceof LongType) {
  //        return "L";
  //      } else if (tp instanceof ShortType) {
  //        return "S";
  //      } else if (tp instanceof BooleanType) {
  //        return "Z";
  //      } else {
  //        throw new RuntimeException("Unsupported primitive type");
  //      }
  //    } else {
  //      throw new RuntimeException("Unsupported type" + tp);
  //    }
  //  }

  /**
   * Gets whether this class constant denotes a reference type. This does not check for arrays.
   *
   * @return True if this class constant denotes a reference type, otherwise false
   */
  public boolean isRefType() {
    return value.startsWith("L") && value.endsWith(";");
  }

  //  /** Returns numDimensions. */
  //  public Type toSootType() {
  //    int numDimensions = 0;
  //    String tmp = value;
  //    while (tmp.startsWith("[")) {
  //      numDimensions++;
  //      tmp = tmp.substring(1);
  //    }
  //
  //    Type baseType;
  //    if (tmp.startsWith("L")) {
  //      tmp = tmp.substring(1);
  //      if (tmp.endsWith(";")) {
  //        tmp = tmp.substring(0, tmp.length() - 1);
  //      }
  //      tmp = tmp.replace("/", ".");
  //      baseType = RefType.getInstance(tmp);
  //    } else if (tmp.equals("I")) {
  //      baseType = IntType.INSTANCE;
  //    } else if (tmp.equals("B")) {
  //      baseType = ByteType.INSTANCE;
  //    } else if (tmp.equals("C")) {
  //      baseType = CharType.INSTANCE;
  //    } else if (tmp.equals("D")) {
  //      baseType = DoubleType.INSTANCE;
  //    } else if (tmp.equals("F")) {
  //      baseType = FloatType.INSTANCE;
  //    } else if (tmp.equals("L")) {
  //      baseType = LongType.INSTANCE;
  //    } else if (tmp.equals("S")) {
  //      baseType = ShortType.INSTANCE;
  //    } else if (tmp.equals("Z")) {
  //      baseType = BooleanType.INSTANCE;
  //    } else {
  //      throw new RuntimeException("Unsupported class constant: " + value);
  //    }
  //
  //    return numDimensions > 0 ? ArrayType.getInstance(baseType, numDimensions) : baseType;
  //  }

  // In this case, equals should be structural equality.
  @Override
  public boolean equals(Object c) {
    return (c instanceof ClassConstant && ((ClassConstant) c).value.equals(this.value));
  }

  /** Returns a hash code for this ClassConstant object. */
  @Override
  public int hashCode() {
    return value.hashCode();
  }

  public String getValue() {
    return value;
  }

  @Override
  public Type getType() {
    // TODO: [JMP] Used cached instance
    return DefaultIdentifierFactory.getInstance().getType("java.lang.Class");
  }

  @Override
  public void accept(IVisitor sw) {
    ((IConstantVisitor) sw).caseClassConstant(this);
  }

  @Override
  public String toString() {
    return "class " + StringTools.getQuotedStringOf(value);
  }

  @Override
  public Object clone() {
    throw new RuntimeException();
  }
}
