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

package qilin.util;

import sootup.core.model.SootMethod;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.core.JavaIdentifierFactory;

/** Well-known JDK {@link ClassType}s, plus type-hierarchy and method-kind checks. */
public final class JavaTypes {
  private JavaTypes() {}

  public static ClassType getClassType(String fullyQualifiedClassName) {
    return JavaIdentifierFactory.getInstance().getClassType(fullyQualifiedClassName);
  }

  public static final ClassType OBJECT = getClassType("java.lang.Object");
  public static final ClassType STRING = getClassType("java.lang.String");
  public static final ClassType THROWABLE = getClassType("java.lang.Throwable");
  public static final ClassType THREAD = getClassType("java.lang.Thread");
  public static final ClassType THREAD_GROUP = getClassType("java.lang.ThreadGroup");
  public static final ClassType CLASS = getClassType("java.lang.Class");
  public static final ClassType EXCEPTION = getClassType("java.lang.Exception");
  public static final ClassType RUNNABLE = getClassType("java.lang.Runnable");
  public static final ClassType STRING_BUFFER = getClassType("java.lang.StringBuffer");
  public static final ClassType STRING_BUILDER = getClassType("java.lang.StringBuilder");
  public static final ClassType PROTECTION_DOMAIN = getClassType("java.security.ProtectionDomain");
  public static final ClassType PRIVILEGED_ACTION_EXCEPTION =
      getClassType("java.security.PrivilegedActionException");
  public static final ClassType APP_CLASS_LOADER = getClassType("sun.misc.Launcher$AppClassLoader");
  public static final ClassType SYSTEM = getClassType("java.lang.System");
  public static final ClassType UNIX_FILE_SYSTEM = getClassType("java.io.UnixFileSystem");
  public static final ClassType COLLECTION = getClassType("java.util.Collection");

  public static boolean isStaticInitializer(SootMethod method) {
    return JavaIdentifierFactory.getInstance()
        .isStaticInitializerSubSignature(method.getSubSignature());
  }

  public static boolean isConstructor(SootMethod method) {
    return method.isConstructor(JavaIdentifierFactory.getInstance());
  }

  public static boolean isThrowable(View view, Type type) {
    if (type instanceof ClassType) {
      return canStoreType(view, type, THROWABLE);
    }
    return false;
  }

  public static boolean canStoreType(View view, final Type child, final Type parent) {
    if (child == parent || child.equals(parent)) {
      return true;
    }
    return view.getTypeHierarchy().isSubtype(parent, child);
  }

  public static boolean castNeverFails(View view, Type src, Type dst) {
    if (dst == null) return true;
    if (dst == src) return true;
    if (src == null) return false;
    if (dst.equals(src)) return true;
    if (src instanceof NullType) return true;
    if (dst instanceof NullType) return false;
    return canStoreType(view, src, dst);
  }

  public static boolean isPrimitiveArrayType(Type type) {
    if (type instanceof ArrayType) {
      ArrayType arrayType = (ArrayType) type;
      return arrayType.getElementType() instanceof PrimitiveType;
    }
    return false;
  }
}
