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
import sootup.java.core.types.JavaClassType;

/** Well-known JDK {@link ClassType}s, plus type-hierarchy and method-kind checks. */
public final class JavaTypes {
  private JavaTypes() {}

  public static final ClassType OBJECT = JavaClassType.of("java.lang.Object");
  public static final ClassType STRING = JavaClassType.of("java.lang.String");
  public static final ClassType THROWABLE = JavaClassType.of("java.lang.Throwable");
  public static final ClassType THREAD = JavaClassType.of("java.lang.Thread");
  public static final ClassType THREAD_GROUP = JavaClassType.of("java.lang.ThreadGroup");
  public static final ClassType CLASS = JavaClassType.of("java.lang.Class");
  public static final ClassType EXCEPTION = JavaClassType.of("java.lang.Exception");
  public static final ClassType RUNNABLE = JavaClassType.of("java.lang.Runnable");
  public static final ClassType STRING_BUFFER = JavaClassType.of("java.lang.StringBuffer");
  public static final ClassType STRING_BUILDER = JavaClassType.of("java.lang.StringBuilder");
  public static final ClassType PROTECTION_DOMAIN =
      JavaClassType.of("java.security.ProtectionDomain");
  public static final ClassType PRIVILEGED_ACTION_EXCEPTION =
      JavaClassType.of("java.security.PrivilegedActionException");
  public static final ClassType APP_CLASS_LOADER =
      JavaClassType.of("sun.misc.Launcher$AppClassLoader");
  public static final ClassType SYSTEM = JavaClassType.of("java.lang.System");
  public static final ClassType UNIX_FILE_SYSTEM = JavaClassType.of("java.io.UnixFileSystem");
  public static final ClassType COLLECTION = JavaClassType.of("java.util.Collection");

  public static boolean isStaticInitializer(View view, SootMethod method) {
    return view.getIdentifierFactory().isStaticInitializerSubSignature(method.getSubSignature());
  }

  public static boolean isConstructor(View view, SootMethod method) {
    return method.isConstructor(view.getIdentifierFactory());
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
