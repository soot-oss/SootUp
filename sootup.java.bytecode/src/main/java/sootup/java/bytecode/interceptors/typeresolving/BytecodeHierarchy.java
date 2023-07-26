package sootup.java.bytecode.interceptors.typeresolving;
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
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootClass;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.*;
import sootup.core.views.View;
import sootup.java.bytecode.interceptors.typeresolving.types.BottomType;

/** @author Zun Wang */
public class BytecodeHierarchy {

  private final ViewTypeHierarchy typeHierarchy;
  private final ClassType objectClassType;
  private final ClassType serializableClassType;
  private final ClassType cloneableClassType;

  public BytecodeHierarchy(View<? extends SootClass<?>> view) {
    this.typeHierarchy = new ViewTypeHierarchy(view);
    IdentifierFactory factory = view.getIdentifierFactory();
    objectClassType = factory.getClassType("java.lang.Object");
    serializableClassType = factory.getClassType("java.io.Serializable");
    cloneableClassType = factory.getClassType("java.lang.Cloneable");
  }

  public boolean isAncestor(@Nonnull Type ancestor, @Nonnull Type child) {
    boolean isAncestor = PrimitiveHierarchy.isAncestor(ancestor, child);
    if (!isAncestor && !(PrimitiveHierarchy.arePrimitives(ancestor, child))) {
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
            ancestor.equals(objectClassType)
                || ancestor.equals(serializableClassType)
                || ancestor.equals(cloneableClassType);

      } else if (child instanceof ArrayType && ancestor instanceof ArrayType) {
        ArrayType ancestorArr = (ArrayType) ancestor;
        ArrayType childArr = (ArrayType) child;
        Type anBase = ancestorArr.getBaseType();
        Type chBase = childArr.getBaseType();
        if (ancestorArr.getDimension() == childArr.getDimension()) {
          if (anBase.equals(chBase)) {
            isAncestor = true;
          } else if (anBase instanceof ClassType && chBase instanceof ClassType) {
            isAncestor = canStoreType((ClassType) anBase, (ClassType) chBase);
          }
        } else if (ancestorArr.getDimension() < childArr.getDimension()) {
          isAncestor =
              anBase.equals(objectClassType)
                  || anBase.equals(serializableClassType)
                  || anBase.equals(cloneableClassType);
        }
      }
    }
    return isAncestor;
  }

  public Collection<Type> getLeastCommonAncestor(Type a, Type b) {
    Collection<Type> ret = new HashSet<>();
    if (a instanceof BottomType) {
      return Collections.singleton(b);
    }
    if (b instanceof BottomType) {
      return Collections.singleton(a);
    }
    if (a instanceof NullType) {
      return Collections.singleton(b);
    }
    if (b instanceof NullType) {
      return Collections.singleton(a);
    }
    if (isAncestor(a, b)) {
      return Collections.singleton(a);
    }
    if (isAncestor(b, a)) {
      return Collections.singleton(b);
    }
    if (a instanceof PrimitiveType && b instanceof PrimitiveType) {
      return PrimitiveHierarchy.getLeastCommonAncestor(a, b);
    }
    if (a instanceof PrimitiveType || b instanceof PrimitiveType) {
      return Collections.emptySet();
    }

    if (a instanceof ArrayType && b instanceof ArrayType) {
      Collection<Type> temp;
      Type et_a = ((ArrayType) a).getElementType();
      Type et_b = ((ArrayType) b).getElementType();
      if (et_a instanceof PrimitiveType || et_b instanceof PrimitiveType) {
        temp = Collections.emptySet();
      } else {
        temp = getLeastCommonAncestor(et_a, et_b);
      }
      if (temp.isEmpty()) {
        ret.add(objectClassType);
        ret.add(serializableClassType);
        ret.add(cloneableClassType);
      } else {
        for (Type type : temp) {
          ret.add(Type.makeArrayType(type, 1));
        }
      }
    } else if (a instanceof ArrayType || b instanceof ArrayType) {
      ClassType nonArray = (ClassType) ((a instanceof ArrayType) ? b : a);
      if (!nonArray.getFullyQualifiedName().equals("java.lang.Object")) {
        if (isAncestor(serializableClassType, nonArray)) {
          ret.add(serializableClassType);
        }
        if (isAncestor(cloneableClassType, nonArray)) {
          ret.add(cloneableClassType);
        }
      }
      if (ret.isEmpty()) {
        ret.add(objectClassType);
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
        ret.add(objectClassType);
      }
    }
    return ret;
  }

  private boolean canStoreType(ClassType ancestor, ClassType child) {
    return ancestor == objectClassType || typeHierarchy.subtypesOf(ancestor).contains(child);
  }

  private Set<AncestryPath> buildAncestryPaths(ClassType type) {
    Deque<AncestryPath> pathNodes = new ArrayDeque<>();
    pathNodes.add(new AncestryPath(type, null));
    Set<AncestryPath> paths = new HashSet<>();
    while (!pathNodes.isEmpty()) {
      AncestryPath node = pathNodes.removeFirst();
      if (node.type == objectClassType) {
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
          ClassType superClass = typeHierarchy.superClassOf(node.type);
          // only java.lang.Object can have no SuperClass i.e. is null - this is already filtered
          // above
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

  // TODO: [ms] thats a linked list.. please refactor that
  private static class AncestryPath {
    public AncestryPath next;
    public ClassType type;

    public AncestryPath(@Nonnull ClassType type, @Nullable AncestryPath next) {
      this.type = type;
      this.next = next;
    }
  }
}
