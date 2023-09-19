package soot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 Ondrej Lhotak
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

import qilin.core.PTAScene;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.java.core.JavaIdentifierFactory;

/**
 * Returns the various potential entry points of a Java program.
 *
 * @author Ondrej Lhotak
 */
public class EntryPoints {

    final MethodSubSignature sigMain;
    final MethodSubSignature sigFinalize;
    final MethodSubSignature sigExit;
    final MethodSubSignature sigClinit;
    final MethodSubSignature sigInit;
    final MethodSubSignature sigStart;
    final MethodSubSignature sigRun;
    final MethodSubSignature sigObjRun;
    final MethodSubSignature sigForName;

    private static EntryPoints instance = new EntryPoints();
    private EntryPoints() {
        sigMain = JavaIdentifierFactory.getInstance().parseMethodSubSignature(JavaMethods.SIG_MAIN);
        sigFinalize = JavaIdentifierFactory.getInstance().parseMethodSubSignature(JavaMethods.SIG_FINALIZE);

        sigExit = JavaIdentifierFactory.getInstance().parseMethodSubSignature(JavaMethods.SIG_EXIT);
        sigClinit = JavaIdentifierFactory.getInstance().parseMethodSubSignature(JavaMethods.SIG_CLINIT);
        sigInit = JavaIdentifierFactory.getInstance().parseMethodSubSignature(JavaMethods.SIG_INIT);
        sigStart = JavaIdentifierFactory.getInstance().parseMethodSubSignature(JavaMethods.SIG_START);
        sigRun = JavaIdentifierFactory.getInstance().parseMethodSubSignature(JavaMethods.SIG_RUN);
        sigObjRun = JavaIdentifierFactory.getInstance().parseMethodSubSignature(JavaMethods.SIG_OBJ_RUN);
        sigForName = JavaIdentifierFactory.getInstance().parseMethodSubSignature(JavaMethods.SIG_FOR_NAME);
    }

    public static EntryPoints v() {
        return instance;
    }

    protected void addMethod(List<SootMethod> set, SootClass cls, MethodSubSignature methodSubSig) {
        Optional<SootMethod> osm = cls.getMethod(methodSubSig);
        osm.ifPresent(set::add);
    }

    protected void addMethod(List<SootMethod> set, String methodSig) {
        MethodSignature ms = JavaIdentifierFactory.getInstance().parseMethodSignature(methodSig);
        Optional<SootMethod> osm = PTAScene.v().getView().getMethod(ms);
        osm.ifPresent(set::add);
    }

    /**
     * Returns only the application entry points, not including entry points invoked implicitly by the VM.
     */
    public List<SootMethod> application() {
        List<SootMethod> ret = new ArrayList<SootMethod>();
        if (PTAScene.v().hasMainClass()) {
            SootClass mainClass = PTAScene.v().getMainClass();
            addMethod(ret, mainClass, sigMain);
            for (SootMethod clinit : clinitsOf(mainClass)) {
                ret.add(clinit);
            }
        }
        return ret;
    }

    /** Returns only the entry points invoked implicitly by the VM. */
    public List<SootMethod> implicit() {
        List<SootMethod> ret = new ArrayList<SootMethod>();

//        if (Options.v().src_prec() == Options.src_prec_dotnet) {
//            return ret;
//        }

        addMethod(ret, JavaMethods.INITIALIZE_SYSTEM_CLASS);
        addMethod(ret, JavaMethods.THREAD_GROUP_INIT);
        // addMethod( ret, "<java.lang.ThreadGroup: void
        // remove(java.lang.Thread)>");
        addMethod(ret, JavaMethods.THREAD_EXIT);
        addMethod(ret, JavaMethods.THREADGROUP_UNCAUGHT_EXCEPTION);
        // addMethod( ret, "<java.lang.System: void
        // loadLibrary(java.lang.String)>");
        addMethod(ret, JavaMethods.CLASSLOADER_INIT);
        addMethod(ret, JavaMethods.CLASSLOADER_LOAD_CLASS_INTERNAL);
        addMethod(ret, JavaMethods.CLASSLOADER_CHECK_PACKAGE_ACC);
        addMethod(ret, JavaMethods.CLASSLOADER_ADD_CLASS);
        addMethod(ret, JavaMethods.CLASSLOADER_FIND_NATIVE);
        addMethod(ret, JavaMethods.PRIV_ACTION_EXC_INIT);
        // addMethod( ret, "<java.lang.ref.Finalizer: void
        // register(java.lang.Object)>");
        addMethod(ret, JavaMethods.RUN_FINALIZE);
        addMethod(ret, JavaMethods.THREAD_INIT_RUNNABLE);
        addMethod(ret, JavaMethods.THREAD_INIT_STRING);
        return ret;
    }

    /** Returns all the entry points. */
    public List<SootMethod> all() {
        List<SootMethod> ret = new ArrayList<SootMethod>();
        ret.addAll(application());
        ret.addAll(implicit());
        return ret;
    }

    /** Returns a list of all static initializers. */
    public List<SootMethod> clinits() {
        List<SootMethod> ret = new ArrayList<SootMethod>();
        Collection<SootClass> classes = PTAScene.v().getView().getClasses();
        for (SootClass cl : classes) {
            addMethod(ret, cl, sigClinit);
        }
        return ret;
    }

    /** Returns a list of all clinits of class cl and its superclasses. */
    public Iterable<SootMethod> clinitsOf(SootClass cl) {
        // Do not create an actual list, since this method gets called quite often
        // Instead, callers usually just want to iterate over the result.
        Optional<SootMethod> oinit = cl.getMethod(sigClinit);
        Optional<SootClass> osuperClass = cl.getSuperclass();
        // check super classes until finds a constructor or no super class there anymore.
        while (oinit.isPresent() && osuperClass.isPresent()) {
            oinit = osuperClass.get().getMethod(sigClinit);
            osuperClass = osuperClass.get().getSuperclass();
        }
        if (!oinit.isPresent()) {
            return Collections.emptyList();
        }
        SootMethod initStart = oinit.get();
        return new Iterable<SootMethod>() {

            @Override
            public Iterator<SootMethod> iterator() {
                return new Iterator<SootMethod>() {
                    SootMethod current = initStart;

                    @Override
                    public SootMethod next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        SootMethod n = current;

                        // Pre-fetch the next element
                        current = null;
                        SootClass currentClass = (SootClass) PTAScene.v().getView().getClass(n.getDeclaringClassType()).get();
                        while (true) {
                            Optional<SootClass> osuperClass = currentClass.getSuperclass();
                            if (!osuperClass.isPresent()) {
                                break;
                            }

                            Optional<SootMethod> om = osuperClass.get().getMethod(sigClinit);
                            if (om.isPresent()) {
                                current = om.get();
                                break;
                            }

                            currentClass = osuperClass.get();
                        }

                        return n;
                    }

                    @Override
                    public boolean hasNext() {
                        return current != null;
                    }
                };
            }
        };
    }
}
