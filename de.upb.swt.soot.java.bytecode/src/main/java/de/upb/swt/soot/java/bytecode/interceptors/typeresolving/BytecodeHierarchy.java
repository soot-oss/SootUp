package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Zun Wang
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
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.typerhierachy.ViewTypeHierarchy;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.BottomType;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author Zun Wang */
public class BytecodeHierarchy implements IHierarchy {

  private ViewTypeHierarchy typeHierarchy;
  private IdentifierFactory factory;
  private ClassType object;
  private ClassType serializable;
  private ClassType cloneable;
  private PrimitiveHierarchy primitiveHierarchy;

  public BytecodeHierarchy(View<? extends SootClass> view) {
    this.typeHierarchy = new ViewTypeHierarchy(view);
    factory = view.getIdentifierFactory();
    object = factory.getClassType("java.lang.Object");
    serializable = factory.getClassType("java.io.Serializable");
    cloneable = factory.getClassType("java.lang.Cloneable");
    primitiveHierarchy = new PrimitiveHierarchy();
  }

  @Override
  public boolean isAncestor(Type ancestor, Type child) {
    boolean isAncestor = primitiveHierarchy.isAncestor(ancestor, child);
    if (!isAncestor && !(primitiveHierarchy.arePrimitives(ancestor, child))) {
      if (ancestor.equals(child)) {
        isAncestor = true;
      } else if (child instanceof BottomType) {
        isAncestor = true;
      } else if (ancestor instanceof BottomType) {
        return false;
      } else if (ancestor instanceof PrimitiveType || child instanceof PrimitiveType) {
        return false;
      } else if (child instanceof NullType) {
        isAncestor = true;
      } else if (ancestor instanceof NullType) {
        return false;
      } else if (child instanceof ClassType && ancestor instanceof ClassType) {

        isAncestor = canStoreType((ClassType) ancestor, (ClassType) child);

      } else if (child instanceof ArrayType && ancestor instanceof ClassType) {

        isAncestor =
            ancestor.equals(object) || ancestor.equals(serializable) || ancestor.equals(cloneable);

      } else if (child instanceof ArrayType && ancestor instanceof ArrayType) {
        ArrayType anArr = (ArrayType) ancestor;
        ArrayType chArr = (ArrayType) child;
        Type anBase = anArr.getBaseType();
        Type chBase = chArr.getBaseType();
        if (anArr.getDimension() == chArr.getDimension()) {
          if (anBase.equals(chBase)) {
            isAncestor = true;
          } else if (anBase instanceof ClassType && chBase instanceof ClassType) {
            isAncestor = canStoreType((ClassType) anBase, (ClassType) chBase);
          }
        } else if (anArr.getDimension() < chArr.getDimension()) {
          isAncestor =
              anBase.equals(object) || anBase.equals(serializable) || anBase.equals(cloneable);
        }
      }
    }
    return isAncestor;
  }

  @Override
  public Collection<Type> getLeastCommonAncestor(Type a, Type b) {
    Collection<Type> ret = new HashSet<>();
    if (a instanceof BottomType) {
      return Collections.singleton(b);
    } else if (b instanceof BottomType) {
      return Collections.singleton(a);
    } else if (a instanceof NullType) {
      return Collections.singleton(b);
    } else if (b instanceof NullType) {
      return Collections.singleton(a);
    } else if (isAncestor(a, b)) {
      return Collections.singleton(a);
    } else if (isAncestor(b, a)) {
      return Collections.singleton(b);
    } else if (a instanceof PrimitiveType && b instanceof PrimitiveType) {
      return primitiveHierarchy.getLeastCommonAncestor(a, b);
    } else if (a instanceof PrimitiveType || b instanceof PrimitiveType) {
      return Collections.emptySet();
    } else if (a instanceof ArrayType && b instanceof ArrayType) {
      Collection<Type> temp;
      Type et_a = ((ArrayType) a).getElementType();
      Type et_b = ((ArrayType) b).getElementType();
      if (et_a instanceof PrimitiveType || et_b instanceof PrimitiveType) {
        temp = Collections.emptySet();
      } else {
        temp = getLeastCommonAncestor(et_a, et_b);
      }
      if (temp.isEmpty()) {
        ret.add(object);
        ret.add(serializable);
        ret.add(cloneable);
      } else {
        for (Type type : temp) {
          ret.add(TypeUtils.makeArrayType(type, 1));
        }
      }
    } else if (a instanceof ArrayType || b instanceof ArrayType) {
      ClassType nonArray = (ClassType) ((a instanceof ArrayType) ? b : a);
      if (!nonArray.getFullyQualifiedName().equals("java.lang.Object")) {
        if (isAncestor(serializable, nonArray)) {
          ret.add(serializable);
        }
        if (isAncestor(cloneable, nonArray)) {
          ret.add(cloneable);
        }
      }
      if (ret.isEmpty()) {
        ret.add(object);
      }
    } else {
      // if a and b are both ClassType
      Set<AncestryPath> pathsA = buildAncestryPaths((ClassType) a);
      Set<AncestryPath> pathsB = buildAncestryPaths((ClassType) b);
      for (AncestryPath pathA : pathsA) {
        for (AncestryPath pathB : pathsB) {
          ClassType lcn = leastCommonNode(pathA, pathB);
          if (lcn == null) {
            continue;
          }
          boolean isLcn = true;
          for (Type l : ret) {
            if (isAncestor(lcn, l)) {
              isLcn = false;
              break;
            }
            if (isAncestor(l, lcn)) {
              ret.remove(l);
            }
          }
          if (isLcn) {
            ret.add(lcn);
          }
        }
      }
      if (ret.isEmpty()) {
        ret.add(object);
      }
    }
    return ret;
  }

  private boolean canStoreType(ClassType ancestor, ClassType child) {
    if (ancestor.equals(object)) {
      return true;
    } else {
      return typeHierarchy.subtypesOf(ancestor).contains(child);
    }
  }

  private Set<AncestryPath> buildAncestryPaths(ClassType type) {
    Deque<AncestryPath> pathNodes = new ArrayDeque<>();
    pathNodes.add(new AncestryPath(type, null));
    Set<AncestryPath> paths = new HashSet<>();
    while (!pathNodes.isEmpty()) {
      AncestryPath node = pathNodes.removeFirst();
      if (node.type.getFullyQualifiedName().equals("java.lang.Object")) {
        paths.add(node);
      } else {
        if (typeHierarchy.isInterface(node.type)) {
          Set<ClassType> superInterfaces = typeHierarchy.directlyExtendedInterfacesOf(node.type);
          if (superInterfaces.isEmpty()) {
            paths.add(node);
          } else {
            for (ClassType superInterface : superInterfaces) {
              AncestryPath superNode = new AncestryPath(superInterface, node);
              pathNodes.add(superNode);
            }
          }
        } else {
          Set<ClassType> superInterfaces = typeHierarchy.directlyImplementedInterfacesOf(node.type);
          for (ClassType superInterface : superInterfaces) {
            AncestryPath superNode = new AncestryPath(superInterface, node);
            pathNodes.add(superNode);
          }
          ClassType superClass = typeHierarchy.directlySuperClassOf(node.type);
          AncestryPath superNode = new AncestryPath(superClass, node);
          pathNodes.add(superNode);
        }
      }
    }
    return paths;
  }

  @Nullable
  private ClassType leastCommonNode(AncestryPath a, AncestryPath b) {
    ClassType lcn = null;
    while (a != null && b != null && a.type.equals(b.type)) {
      lcn = a.type;
      a = a.next;
      b = b.next;
    }
    return lcn;
  }

  private class AncestryPath {
    public AncestryPath next;
    public ClassType type;

    public AncestryPath(@Nonnull ClassType type, @Nullable AncestryPath next) {
      this.type = type;
      this.next = next;
    }
  }
}
