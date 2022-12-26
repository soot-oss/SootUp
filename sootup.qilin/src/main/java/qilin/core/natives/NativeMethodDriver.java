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

package qilin.core.natives;

import qilin.core.PTAScene;
import soot.SootMethod;

public class NativeMethodDriver {
    public void buildNative(SootMethod method) {
        if (!PTAScene.v().nativeBuilt.add(method)) {
            return;
        }
        String sig = method.getSignature();
        switch (sig) {
            case "<java.lang.Object: java.lang.Object clone()>":
            case "<qilin.pta.nativemodel.JavaLangObject: java.lang.Object clone()>":
                new JavaLangObjectCloneNative(method).simulate();
                break;
            case "<java.lang.System: void setIn0(java.io.InputStream)>":
                new JavaLangSystemSetIn0Native(method).simulate();
                break;
            case "<java.lang.System: void setOut0(java.io.PrintStream)>":
                new JavaLangSystemSetOut0Native(method).simulate();
                break;
            case "<java.lang.System: void setErr0(java.io.PrintStream)>":
                new JavaLangSystemSetErr0Native(method).simulate();
                break;
//            case "<java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)>":
//                new JavaLangSystemArraycopyNative(method).simulate();
//                break;
            case "<java.io.FileSystem: java.io.FileSystem getFileSystem()>":
            case "<qilin.pta.nativemodel.JavaIoFileSystem: java.lang.Object getFileSystem()>":
                new JavaIoFileSystemGetFileSystemNative(method).simulate();
                break;
            case "<java.io.UnixFileSystem: java.lang.String[] list(java.io.File)>":
            case "<qilin.pta.nativemodel.JavaIoFileSystem: java.lang.String[] list(java.io.File)>":
                new JavaIoFileSystemListNative(method).simulate();
                break;
            case "<java.lang.ref.Finalizer: void invokeFinalizeMethod(java.lang.Object)>":
                new JavaLangRefFinalizerInvokeFinalizeMethodNative(method).simulate();
                break;
            case "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction)>":
            case "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction,java.security.AccessControlContext)>":
                new JavaSecurityAccessControllerDoPrivilegedNative(method).simulate();
                break;
            case "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction)>":
            case "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction,java.security.AccessControlContext)>":
                new JavaSecurityAccessControllerDoPrivileged_ExceptionNative(method).simulate();
                break;
            case "<java.lang.Thread: java.lang.Thread currentThread()>":
                new JavaLangThreadCurrentThread(method).simulate();
                break;
            case "<java.lang.Thread: void start0()>":
                new JavaLangThreadStart0Native(method).simulate();
                break;
            case "<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>":
//                new JavaLangReflectArrayGet(method).simulate();
                break;
            case "<java.lang.reflect.Array: void set(java.lang.Object,int,java.lang.Object)>":
//                new JavaLangReflectArraySet(method).simulate();
                break;
            default:
                // System.out.println("Warning: unhandled native method " + sig);
                break;
        }
    }

}
