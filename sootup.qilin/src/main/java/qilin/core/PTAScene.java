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

package qilin.core;

import qilin.core.builder.FakeMainFactory;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.Chain;
import soot.util.IterableNumberer;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.core.JavaIdentifierFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class PTAScene {
    private static volatile PTAScene instance = null;

    private final View view;
    private CallGraph callgraph;
    private final FakeMainFactory fakeMainFactory;

    private SootClass mainClass = null;

    public void setMainClass(SootClass m) {
        mainClass = m;
    }

    public boolean hasMainClass() {
        return mainClass != null;
    }

    public SootClass getMainClass() {
        if (!hasMainClass()) {
            throw new RuntimeException("There is no main class set!");
        }
        return mainClass;
    }

    public static PTAScene v() {
        if (instance == null) {
            synchronized (PTAScene.class) {
                if (instance == null) {
                    instance = new PTAScene();
                }
            }
        }
        return instance;
    }

    public static void junitReset() {
        VirtualCalls.reset();
        instance = null;
    }

    public static void reset() {
        VirtualCalls.reset();
        instance = null;
    }

    private PTAScene() {
        this.fakeMainFactory = new FakeMainFactory();
    }

    public final Set<SootMethod> nativeBuilt = DataFactory.createSet();
    public final Set<SootMethod> reflectionBuilt = DataFactory.createSet();
    public final Set<SootMethod> arraycopyBuilt = DataFactory.createSet();

    /*
     * wrapper methods for FakeMain.
     * */
    public SootMethod getFakeMainMethod() {
        return this.fakeMainFactory.getFakeMain();
    }

    public Value getFieldCurrentThread() {
        return this.fakeMainFactory.getFieldCurrentThread();
    }

    public Value getFieldGlobalThrow() {
        return this.fakeMainFactory.getFieldGlobalThrow();
    }

    /*
     *  wrapper methods of Soot Scene. Note, we do not allow you to use Soot Scene directly in qilin.qilin.pta subproject
     * to avoid confusing.
     * */
    public void setCallGraph(CallGraph cg) {
        this.callgraph = cg;
    }

    public View getView() {
        return view;
    }

    public CallGraph getCallGraph() {
        return this.callgraph;
    }

    public IterableNumberer<Local> getLocalNumberer() {
        return sootScene.getLocalNumberer();
    }

    public boolean canStoreType(final Type child, final Type parent) {
        return view.getTypeHierarchy().isSubtype(parent, child);
    }

    public SootClass loadClassAndSupport(String className) {
        return sootScene.loadClassAndSupport(className);
    }

    public SootMethod getMethod(String methodSignature) {
        MethodSignature mthdSig = JavaIdentifierFactory.getInstance().parseMethodSignature(methodSignature);
        return (SootMethod) view.getMethod(mthdSig).get();
    }

    public Chain<SootClass> getApplicationClasses() {
        return sootScene.getApplicationClasses();
    }

    public Chain<SootClass> getLibraryClasses() {
        return sootScene.getLibraryClasses();
    }

    public boolean containsMethod(String methodSignature) {
        MethodSignature mthdSig = JavaIdentifierFactory.getInstance().parseMethodSignature(methodSignature);
        return view.getMethod(mthdSig).isPresent();
    }

    public boolean containsField(String fieldSignature) {
        FieldSignature fieldSig = JavaIdentifierFactory.getInstance().parseFieldSignature(fieldSignature);
        return view.getField(fieldSig).isPresent();
    }

    public Collection<SootClass> getClasses() {
        return view.getClasses();
    }

    public Collection<SootClass> getPhantomClasses() {
        return Collections.emptySet();
    }

    public SootClass getSootClass(String className) {
        ClassType classType = PTAUtils.getClassType(className);
        return (SootClass) view.getClass(classType).get();
    }

    public boolean containsClass(String className) {
        ClassType classType = PTAUtils.getClassType(className);
        Optional<SootClass> oclazz = view.getClass(classType);
        return oclazz.isPresent();
    }

    public SootField getField(String fieldSignature) {
        FieldSignature fieldSig = JavaIdentifierFactory.getInstance().parseFieldSignature(fieldSignature);
        return (SootField) view.getField(fieldSig).get();
    }


    public void loadNecessaryClasses() {
        sootScene.loadNecessaryClasses();
    }

    /*
     * copy from doop's soot-fact-generator
     * */
    public void addBasicClasses() {
        /*
         * Set resolution level for sun.net.www.protocol.ftp.FtpURLConnection
         * to 1 (HIERARCHY) before calling produceFacts(). The following line is necessary to avoid
         * a runtime exception when running soot with java 1.8, however it leads to different
         * input fact generation thus leading to different analysis results
         */
        sootScene.addBasicClass("sun.net.www.protocol.ftp.FtpURLConnection", SootClass.HIERARCHY);
        sootScene.addBasicClass("javax.crypto.extObjectInputStream");
        sootScene.addBasicClass("sun.misc.Launcher$AppClassLoader");
        /*
         * For simulating the FileSystem class, we need the implementation
         * of the FileSystem, but the classes are not loaded automatically
         * due to the indirection via native code.
         */
        addCommonDynamicClass("java.io.UnixFileSystem");
        addCommonDynamicClass("java.io.WinNTFileSystem");
        addCommonDynamicClass("java.io.Win32FileSystem");

        /* java.net.URL loads handlers dynamically */
        addCommonDynamicClass("sun.net.www.protocol.file.Handler");
        addCommonDynamicClass("sun.net.www.protocol.ftp.Handler");
        addCommonDynamicClass("sun.net.www.protocol.http.Handler");
        addCommonDynamicClass("sun.net.www.protocol.https.Handler");
        addCommonDynamicClass("sun.net.www.protocol.jar.Handler");
    }

    private void addCommonDynamicClass(String className) {
        if (SourceLocator.v().getClassSource(className) != null) {
            sootScene.addBasicClass(className);
        }
    }
}
