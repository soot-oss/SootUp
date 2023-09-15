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
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.Chain;
import soot.util.IterableNumberer;
import soot.util.StringNumberer;

import java.util.Set;

public class PTAScene {
    private static volatile PTAScene instance = null;
    private final Scene sootScene;
    private final FakeMainFactory fakeMainFactory;

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
        G.reset();
        VirtualCalls.reset();
        instance = null;
    }

    private PTAScene() {
        this.sootScene = Scene.v();
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

    public void setMainClass(SootClass m) {
        sootScene.setMainClass(m);
    }

    /*
     *  wrapper methods of Soot Scene. Note, we do not allow you to use Soot Scene directly in qilin.qilin.pta subproject
     * to avoid confusing.
     * */
    public void setCallGraph(CallGraph cg) {
        sootScene.setCallGraph(cg);
    }

    public CallGraph getCallGraph() {
        return sootScene.getCallGraph();
    }

    public IterableNumberer<Local> getLocalNumberer() {
        return sootScene.getLocalNumberer();
    }

    public IterableNumberer<Type> getTypeNumberer() {
        return sootScene.getTypeNumberer();
    }

    public FastHierarchy getOrMakeFastHierarchy() {
        return sootScene.getOrMakeFastHierarchy();
    }

    public SootClass loadClassAndSupport(String className) {
        return sootScene.loadClassAndSupport(className);
    }

    public SootMethod getMethod(String methodSignature) {
        return sootScene.getMethod(methodSignature);
    }

    public Chain<SootClass> getApplicationClasses() {
        return sootScene.getApplicationClasses();
    }

    public Chain<SootClass> getLibraryClasses() {
        return sootScene.getLibraryClasses();
    }

    public boolean containsMethod(String methodSignature) {
        return sootScene.containsMethod(methodSignature);
    }

    public boolean containsField(String fieldSignature) {
        return sootScene.containsField(fieldSignature);
    }

    public void loadBasicClasses() {
        sootScene.loadBasicClasses();
    }

    public void addBasicClass(String name, int level) {
        sootScene.addBasicClass(name, level);
    }

    public Chain<SootClass> getClasses() {
        return sootScene.getClasses();
    }

    public Chain<SootClass> getPhantomClasses() {
        return sootScene.getPhantomClasses();
    }

    public SootClass getSootClass(String className) {
        return sootScene.getSootClass(className);
    }

    public boolean containsClass(String className) {
        return sootScene.containsClass(className);
    }

    public SootField getField(String fieldSignature) {
        return sootScene.getField(fieldSignature);
    }

    public Type getTypeUnsafe(String arg, boolean phantomNonExist) {
        return sootScene.getTypeUnsafe(arg, phantomNonExist);
    }

    public StringNumberer getSubSigNumberer() {
        return sootScene.getSubSigNumberer();
    }

    public IterableNumberer<SootMethod> getMethodNumberer() {
        return sootScene.getMethodNumberer();
    }

    public void loadNecessaryClasses() {
        sootScene.loadNecessaryClasses();
    }

    public boolean containsType(String className) {
        return sootScene.containsType(className);
    }

    public RefType getRefType(String className) {
        return sootScene.getRefType(className);
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
