/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
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

package de.upb.soot.jimple.common.type;

import de.upb.soot.core.SootClass;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.CommonClassSignatures;
import de.upb.soot.views.View;

import java.util.ArrayDeque;

/**
 * A class that models Java's reference types. RefTypes are parameterized by a class name. Two RefType are equal iff they are
 * Parameterized by the same class name as a String. Modified by @author Linghui Luo on 25.07.2018
 */
@SuppressWarnings("serial")
public class RefType extends RefLikeType implements Comparable<RefType> {

  /** the class name that parameterizes this RefType. */
  private String className;
  private volatile SootClass sootClass;
  private AnySubType anySubType;
  private static View view;

  /**
   * Get a RefType for a class. Each class has only one RefType instance. All RefType instances are stored in {@link View}.
   * 
   * @param className
   *          The name of the class used to parameterize the created RefType.
   * @return a RefType for the given class name.
   */
  public static RefType getInstance(String className) {
    RefType rt = view.getRefType(className);
    if (rt == null) {
      rt = new RefType(className);
      view.addRefType(className, rt);
    }
    return rt;
  }

  /**
   * Get a RefType for a class. Each class has only one RefType instance. All RefType instances are stored in {@link View}.
   * 
   * @param c
   *          A SootClass for which to create a RefType.
   * @return a RefType for the given SootClass.
   */
  public static RefType getInstance(SootClass c) {
    return getInstance(c.getName());
  }


  // TODO: Please change className to ClassSignature here. No use of Strings to determine classes anymore. The first few lines are a good example why.
  private RefType(String className) {
    if (className.startsWith("[")) {
      throw new RuntimeException("Attempt to create RefType whose name starts with [ --> " + className);
    }
    if (className.indexOf("/") >= 0) {
      throw new RuntimeException("Attempt to create RefType containing a / --> " + className);
    }
    if (className.indexOf(";") >= 0) {
      throw new RuntimeException("Attempt to create RefType containing a ; --> " + className);
    }
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public int compareTo(RefType t) {
    return this.toString().compareTo(t.toString());
  }

  public boolean hasSootClass() {
    return sootClass != null;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * Set the SootClass object corresponding to this RefType.
   * 
   * @param sootClass
   *          The SootClass corresponding to this RefType.
   */
  public void setSootClass(SootClass sootClass) {
    this.sootClass = sootClass;
  }

  /**
   * 2 RefTypes are considered equal if they are parametrized by the same class name String.
   * 
   * @param t
   *          an object to test for equality. @ return true if t is a RefType parametrized by the same name as this.
   */
  @Override
  public boolean equals(Object t) {
    return ((t instanceof RefType) && className.equals(((RefType) t).className));
  }

  @Override
  public String toString() {
    return className;
  }

  /**
   * Returns a textual representation, quoted as needed, of this type for serialization, e.g. to .jimple format
   */
  @Override
  public String toQuotedString() {
    return this.getView().quotedNameOf(className);
  }

  @Override
  public int hashCode() {
    return className.hashCode();
  }

  /** Returns the least common superclass of this type and other. */
  public Type merge(Type other) {
    if (other.equals(UnknownType.getInstance()) || this.equals(other)) {
      return this;
    }

    if (!(other instanceof RefType)) {
      throw new RuntimeException("illegal type merge: " + this + " and " + other);
    }

    {
      // Return least common superclass
      // TODO: This is all highly suspicious. FQCNs should be resolved there through a SignatureFactory.
      SootClass thisClass = null; //this.getView().getSootClass(this.className);
      SootClass otherClass = null; // this.getView().getSootClass(((RefType) other).className);
      SootClass javalangObject = this.getView().getSootClass(CommonClassSignatures.JavaLangObject).orElseGet(null);

      ArrayDeque<SootClass> thisHierarchy = new ArrayDeque<>();
      ArrayDeque<SootClass> otherHierarchy = new ArrayDeque<>();

      // Build thisHierarchy
      {
        SootClass sootClass = thisClass;

        // This should never be null, so we could also use "while
        // (true)"; but better be safe than sorry.
        while (sootClass != null) {
          thisHierarchy.addFirst(sootClass);
          if (sootClass == javalangObject) {
            break;
          }

          sootClass = sootClass.getSuperclassUnsafe();
          if (sootClass == null) {
            sootClass = javalangObject;
          }
        }
      }

      // Build otherHierarchy
      {
        SootClass sootClass = otherClass;

        // This should never be null, so we could also use "while
        // (true)"; but better be safe than sorry.
        while (sootClass != null) {
          otherHierarchy.addFirst(sootClass);
          if (sootClass == javalangObject) {
            break;
          }

          sootClass = sootClass.getSuperclassUnsafe();
          if (sootClass == null) {
            sootClass = javalangObject;
          }
        }
      }

      // Find least common superclass
      {
        SootClass commonClass = null;

        while (!otherHierarchy.isEmpty() && !thisHierarchy.isEmpty()
            && otherHierarchy.getFirst() == thisHierarchy.getFirst()) {
          commonClass = otherHierarchy.removeFirst();
          thisHierarchy.removeFirst();
        }

        if (commonClass == null) {
          throw new RuntimeException("Could not find a common superclass for " + this + " and " + other);
        }

        return commonClass.getType();
      }
    }

  }

  @Override
  public Type getArrayElementType() {
    if (className.equals("java.lang.Object") || className.equals("java.io.Serializable")
        || className.equals("java.lang.Cloneable")) {
      return RefType.getInstance("java.lang.Object");
    }
    throw new RuntimeException("Attempt to get array base type of a non-array");
  }

  public AnySubType getAnySubType() {
    return anySubType;
  }

  public void setAnySubType(AnySubType anySubType) {
    this.anySubType = anySubType;
  }

  @Override
  public boolean isAllowedInFinalCode() {
    return true;
  }

  @Override
  public void accept(IVisitor sw) {
    // TODO Auto-generated method stub

  }

  /**
   * Set the current view. RefType needs access to view, since all RefTypes are stored in {@link View}.
   * 
   * @param view
   *          the current view
   */
  public static void setView(View view) {
    RefType.view = view;
  }

}
