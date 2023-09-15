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

package qilin.core.builder;

import qilin.CoreConfig;
import qilin.core.ArtificialMethod;
import qilin.util.PTAUtils;
import soot.*;
import soot.jimple.JimpleBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FakeMainFactory extends ArtificialMethod {
    public static FakeMainFactory instance;

    public static int implicitCallEdges;
    private final SootClass fakeClass;

    public FakeMainFactory() {
        this.localStart = 0;
        this.fakeClass = new SootClass("FakeMain");
        this.fakeClass.setResolvingLevel(SootClass.BODIES);
        this.method = new SootMethod("fakeMain", null, VoidType.v());
        this.method.setModifiers(Modifier.STATIC);
        SootField currentThread = new SootField("currentThread", RefType.v("java.lang.Thread"), Modifier.STATIC);
        SootField globalThrow = new SootField("globalThrow", RefType.v("java.lang.Exception"), Modifier.STATIC);
        fakeClass.addMethod(this.method);
        fakeClass.addField(currentThread);
        fakeClass.addField(globalThrow);
    }

    private List<SootMethod> getEntryPoints() {
        List<SootMethod> ret = new ArrayList<>();
        if (CoreConfig.v().getPtaConfig().clinitMode == CoreConfig.ClinitMode.FULL) {
            ret.addAll(EntryPoints.v().clinits());
        } else {
            // on the fly mode, resolve the clinit methods on the fly.
            ret.addAll(Collections.emptySet());
        }

        if (CoreConfig.v().getPtaConfig().singleentry) {
            List<SootMethod> entries = EntryPoints.v().application();
            if (entries.isEmpty()) {
                throw new RuntimeException("Must specify MAINCLASS when appmode enabled!!!");
            } else {
                ret.addAll(entries);
            }
        } else {
            System.out.println("include implicit entry!");
            ret.addAll(EntryPoints.v().application());
            ret.addAll(EntryPoints.v().implicit());
        }
        System.out.println("#EntrySize:" + ret.size());
        return ret;
    }

    public SootMethod getFakeMain() {
        if (body == null) {
            synchronized (this) {
                if (body == null) {
                    this.method.setSource((m, phaseName) -> new JimpleBody(this.method));
                    this.body = PTAUtils.getMethodBody(method);
                    makeFakeMain();
                }
            }
        }
        return this.method;
    }

    public Value getFieldCurrentThread() {
        return getStaticFieldRef("FakeMain", "currentThread");
    }

    public Value getFieldGlobalThrow() {
        return getStaticFieldRef("FakeMain", "globalThrow");
    }

    private void makeFakeMain() {
        implicitCallEdges = 0;
        for (SootMethod entry : getEntryPoints()) {
            if (entry.isStatic()) {
                if (entry.getSubSignature().equals("void main(java.lang.String[])")) {
                    Value mockStr = getNew(RefType.v("java.lang.String"));
                    Value strArray = getNewArray(RefType.v("java.lang.String"));
                    addAssign(getArrayRef(strArray), mockStr);
                    addInvoke(entry.getSignature(), strArray);
                    implicitCallEdges++;
                } else if (CoreConfig.v().getPtaConfig().clinitMode != CoreConfig.ClinitMode.ONFLY || !entry.isStaticInitializer()) {
                    // in the on fly mode, we won't add a call directly for <clinit> methods.
                    addInvoke(entry.getSignature());
                    implicitCallEdges++;
                }
            }
        }
        if (CoreConfig.v().getPtaConfig().singleentry) {
            return;
        }
        Value sv = getNextLocal(RefType.v("java.lang.String"));
        Value mainThread = getNew(RefType.v("java.lang.Thread"));
        Value mainThreadGroup = getNew(RefType.v("java.lang.ThreadGroup"));
        Value systemThreadGroup = getNew(RefType.v("java.lang.ThreadGroup"));

        Value gCurrentThread = getFieldCurrentThread();
        addAssign(gCurrentThread, mainThread); // Store
        Value vRunnable = getNextLocal(RefType.v("java.lang.Runnable"));

        Value lThreadGroup = getNextLocal(RefType.v("java.lang.ThreadGroup"));
        addInvoke(mainThread, "<java.lang.Thread: void <init>(java.lang.ThreadGroup,java.lang.String)>", mainThreadGroup, sv);
        Value tmpThread = getNew(RefType.v("java.lang.Thread"));
        addInvoke(tmpThread, "<java.lang.Thread: void <init>(java.lang.ThreadGroup,java.lang.Runnable)>", lThreadGroup, vRunnable);
        addInvoke(tmpThread, "<java.lang.Thread: void exit()>");

        addInvoke(systemThreadGroup, "<java.lang.ThreadGroup: void <init>()>");
        addInvoke(mainThreadGroup, "<java.lang.ThreadGroup: void <init>(java.lang.ThreadGroup,java.lang.String)>", systemThreadGroup, sv);

        Value lThread = getNextLocal(RefType.v("java.lang.Thread"));
        Value lThrowable = getNextLocal(RefType.v("java.lang.Throwable"));
        Value tmpThreadGroup = getNew(RefType.v("java.lang.ThreadGroup"));
        addInvoke(tmpThreadGroup, "<java.lang.ThreadGroup: void uncaughtException(java.lang.Thread,java.lang.Throwable)>", lThread, lThrowable); // TODO.


        // ClassLoader
        Value defaultClassLoader = getNew(RefType.v("sun.misc.Launcher$AppClassLoader"));
        addInvoke(defaultClassLoader, "<java.lang.ClassLoader: void <init>()>");
        Value vClass = getNextLocal(RefType.v("java.lang.Class"));
        Value vDomain = getNextLocal(RefType.v("java.security.ProtectionDomain"));
        addInvoke(defaultClassLoader, "<java.lang.ClassLoader: java.lang.Class loadClassInternal(java.lang.String)>", sv);
        addInvoke(defaultClassLoader, "<java.lang.ClassLoader: void checkPackageAccess(java.lang.Class,java.security.ProtectionDomain)>", vClass, vDomain);
        addInvoke(defaultClassLoader, "<java.lang.ClassLoader: void addClass(java.lang.Class)>", vClass);

        // PrivilegedActionException
        Value privilegedActionException = getNew(RefType.v("java.security.PrivilegedActionException"));
        Value gLthrow = getNextLocal(RefType.v("java.lang.Exception"));
        addInvoke(privilegedActionException, "<java.security.PrivilegedActionException: void <init>(java.lang.Exception)>", gLthrow);
    }
}
