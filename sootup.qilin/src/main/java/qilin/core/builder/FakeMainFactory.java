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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import qilin.core.ArtificialMethod;
import qilin.core.config.PointerAnalysisConfig;
import qilin.util.PTAUtils;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.*;

public class FakeMainFactory extends ArtificialMethod {

  private int implicitCallEdgeCount;
  private final SootClass mainClass;
  private final PointerAnalysisConfig config;
  private final EntryPoints entryPoints;
  private final FieldSignature currentThreadSig;
  private final FieldSignature globalThrowSig;

  public FakeMainFactory(View view, SootClass mainClazz, PointerAnalysisConfig config) {
    super(view);
    this.mainClass = mainClazz;
    this.config = config;
    this.entryPoints = new EntryPoints();
    this.localStart = 0;
    String className = "qilin.pta.FakeMain";
    IdentifierFactory fact = view.getIdentifierFactory();
    ClassType declaringClassSignature = fact.getClassType(className);
    this.currentThreadSig =
        fact.getFieldSignature("currentThread", declaringClassSignature, "java.lang.Thread");
    JavaSootField currentThread =
        new JavaSootField(
            currentThreadSig,
            EnumSet.of(FieldModifier.STATIC),
            NoPositionInformation.getInstance());
    this.globalThrowSig =
        fact.getFieldSignature("globalThrow", declaringClassSignature, "java.lang.Exception");
    JavaSootField globalThrow =
        new JavaSootField(
            globalThrowSig, EnumSet.of(FieldModifier.STATIC), NoPositionInformation.getInstance());

    MethodSignature methodSignatureOne =
        fact.getMethodSignature(className, "main", "void", Collections.emptyList());

    StmtPositionInfo noPosInfo = StmtPositionInfo.getNoStmtPositionInfo();
    final JReturnVoidStmt returnVoidStmt = new JReturnVoidStmt(noPosInfo);
    final JNopStmt jNop = new JNopStmt(noPosInfo);
    this.bodyBuilder = Body.builder();
    makeFakeMain(currentThread);
    final MutableControlFlowGraph controlFlowGraph = bodyBuilder.getControlFlowGraph();
    controlFlowGraph.addBlock(stmtList);
    controlFlowGraph.setStartingStmt(jNop);
    controlFlowGraph.putEdge(jNop, stmtList.get(0));
    controlFlowGraph.putEdge((FallsThroughStmt) stmtList.get(stmtList.size() - 1), returnVoidStmt);

    bodyBuilder
        .setMethodSignature(methodSignatureOne)
        .setPosition(NoPositionInformation.getInstance());

    Body bodyOne = bodyBuilder.build();
    JavaSootMethod dummyMainMethod =
        new JavaSootMethod(
            new OverridingBodySource(methodSignatureOne, bodyOne),
            methodSignatureOne,
            EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            Collections.emptyList(),
            NoPositionInformation.getInstance());
    this.method = dummyMainMethod;
  }

  public SootMethod getFakeMain() {
    return this.method;
  }

  public int getImplicitCallEdgeCount() {
    return implicitCallEdgeCount;
  }

  private List<SootMethod> getEntryPoints() {
    List<SootMethod> ret = new ArrayList<>();
    if (config.isSeedEntryPointClinits()) {
      ret.addAll(entryPoints.clinits());
    }
    // otherwise, resolve the clinit methods on the fly instead of seeding them upfront.

    if (config.isSingleEntry()) {
      List<SootMethod> entries = entryPoints.application();
      if (entries.isEmpty()) {
        throw new RuntimeException("Must specify MAINCLASS when appmode enabled!!!");
      } else {
        ret.addAll(entries);
      }
    } else {
      ret.addAll(entryPoints.application());
      ret.addAll(entryPoints.implicit());
    }
    System.out.println("#EntrySize:" + ret.size());
    return ret;
  }

  public JStaticFieldRef getFieldCurrentThread() {
    return Jimple.newStaticFieldRef(currentThreadSig);
  }

  public Value getFieldGlobalThrow() {
    return Jimple.newStaticFieldRef(globalThrowSig);
  }

  private void makeFakeMain(SootField currentThread) {
    implicitCallEdgeCount = 0;
    for (SootMethod entry : getEntryPoints()) {
      if (entry.isStatic()) {
        if (entry
            .getSignature()
            .getSubSignature()
            .toString()
            .equals("void main(java.lang.String[])")) {
          Value mockStr = getNew(PTAUtils.STRING);
          Immediate strArray = getNewArray(PTAUtils.STRING);
          addAssign(getArrayRef(strArray), mockStr);
          addInvoke(entry.getSignature().toString(), strArray);
          implicitCallEdgeCount++;
        } else if (config.isSeedEntryPointClinits() || !PTAUtils.isStaticInitializer(entry)) {
          // when not eagerly seeding, we won't add a call directly for <clinit> methods - they're
          // resolved on the fly instead.
          addInvoke(entry.getSignature().toString());
          implicitCallEdgeCount++;
        }
      }
    }
    if (config.isSingleEntry()) {
      return;
    }
    Local sv = getNextLocal(PTAUtils.STRING);
    Local mainThread = getNew(PTAUtils.THREAD);
    Local mainThreadGroup = getNew(PTAUtils.THREAD_GROUP);
    Local systemThreadGroup = getNew(PTAUtils.THREAD_GROUP);

    JStaticFieldRef gCurrentThread = Jimple.newStaticFieldRef(currentThread.getSignature());
    addAssign(gCurrentThread, mainThread); // Store
    Local vRunnable = getNextLocal(PTAUtils.RUNNABLE);

    Local lThreadGroup = getNextLocal(PTAUtils.THREAD_GROUP);
    addInvoke(
        mainThread,
        "<java.lang.Thread: void <init>(java.lang.ThreadGroup,java.lang.String)>",
        mainThreadGroup,
        sv);
    Local tmpThread = getNew(PTAUtils.THREAD);
    addInvoke(
        tmpThread,
        "<java.lang.Thread: void <init>(java.lang.ThreadGroup,java.lang.Runnable)>",
        lThreadGroup,
        vRunnable);
    addInvoke(tmpThread, "<java.lang.Thread: void exit()>");

    addInvoke(systemThreadGroup, "<java.lang.ThreadGroup: void <init>()>");
    addInvoke(
        mainThreadGroup,
        "<java.lang.ThreadGroup: void <init>(java.lang.ThreadGroup,java.lang.String)>",
        systemThreadGroup,
        sv);

    Local lThread = getNextLocal(PTAUtils.THREAD);
    Local lThrowable = getNextLocal(PTAUtils.THROWABLE);
    Local tmpThreadGroup = getNew(PTAUtils.THREAD_GROUP);
    addInvoke(
        tmpThreadGroup,
        "<java.lang.ThreadGroup: void uncaughtException(java.lang.Thread,java.lang.Throwable)>",
        lThread,
        lThrowable); // TODO.

    // ClassLoader
    Local defaultClassLoader = getNew(PTAUtils.APP_CLASS_LOADER);
    addInvoke(defaultClassLoader, "<java.lang.ClassLoader: void <init>()>");
    Local vClass = getNextLocal(PTAUtils.CLASS);
    Local vDomain = getNextLocal(PTAUtils.PROTECTION_DOMAIN);
    addInvoke(
        defaultClassLoader,
        "<java.lang.ClassLoader: java.lang.Class loadClassInternal(java.lang.String)>",
        sv);
    addInvoke(
        defaultClassLoader,
        "<java.lang.ClassLoader: void checkPackageAccess(java.lang.Class,java.security.ProtectionDomain)>",
        vClass,
        vDomain);
    addInvoke(
        defaultClassLoader, "<java.lang.ClassLoader: void addClass(java.lang.Class)>", vClass);

    // PrivilegedActionException
    Local privilegedActionException =
        getNew(PTAUtils.PRIVILEGED_ACTION_EXCEPTION);
    Local gLthrow = getNextLocal(PTAUtils.EXCEPTION);
    addInvoke(
        privilegedActionException,
        "<java.security.PrivilegedActionException: void <init>(java.lang.Exception)>",
        gLthrow);
  }

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

    private EntryPoints() {
      JavaIdentifierFactory identifierFactory = (JavaIdentifierFactory) view.getIdentifierFactory();
      sigMain = identifierFactory.parseMethodSubSignature(JavaDefinitions.SIG_MAIN);
      sigFinalize = identifierFactory.parseMethodSubSignature(JavaDefinitions.SIG_FINALIZE);

      sigExit = identifierFactory.parseMethodSubSignature(JavaDefinitions.SIG_EXIT);
      sigClinit = identifierFactory.parseMethodSubSignature(JavaDefinitions.SIG_CLINIT);
      sigInit = identifierFactory.parseMethodSubSignature(JavaDefinitions.SIG_INIT);
      sigStart = identifierFactory.parseMethodSubSignature(JavaDefinitions.SIG_START);
      sigRun = identifierFactory.parseMethodSubSignature(JavaDefinitions.SIG_RUN);
      sigObjRun = identifierFactory.parseMethodSubSignature(JavaDefinitions.SIG_OBJ_RUN);
      sigForName = identifierFactory.parseMethodSubSignature(JavaDefinitions.SIG_FOR_NAME);
    }

    protected void addMethod(List<SootMethod> set, SootClass cls, MethodSubSignature methodSubSig) {
      Optional<? extends SootMethod> osm = cls.getMethod(methodSubSig);
      osm.ifPresent(set::add);
    }

    protected void addMethod(List<SootMethod> set, String methodSig) {
      MethodSignature ms = view.getIdentifierFactory().parseMethodSignature(methodSig);
      Optional<? extends SootMethod> osm = view.getMethod(ms);
      osm.ifPresent(set::add);
    }

    /**
     * Returns only the application entry points, not including entry points invoked implicitly by
     * the VM.
     */
    public List<SootMethod> application() {
      List<SootMethod> ret = new ArrayList<>();
      if (mainClass != null) {
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

      addMethod(ret, JavaDefinitions.INITIALIZE_SYSTEM_CLASS);
      addMethod(ret, JavaDefinitions.THREAD_GROUP_INIT);
      // addMethod( ret, "<java.lang.ThreadGroup: void
      // remove(java.lang.Thread)>");
      addMethod(ret, JavaDefinitions.THREAD_EXIT);
      addMethod(ret, JavaDefinitions.THREADGROUP_UNCAUGHT_EXCEPTION);
      // addMethod( ret, "<java.lang.System: void
      // loadLibrary(java.lang.String)>");
      addMethod(ret, JavaDefinitions.CLASSLOADER_INIT);
      addMethod(ret, JavaDefinitions.CLASSLOADER_LOAD_CLASS_INTERNAL);
      addMethod(ret, JavaDefinitions.CLASSLOADER_CHECK_PACKAGE_ACC);
      addMethod(ret, JavaDefinitions.CLASSLOADER_ADD_CLASS);
      addMethod(ret, JavaDefinitions.CLASSLOADER_FIND_NATIVE);
      addMethod(ret, JavaDefinitions.PRIV_ACTION_EXC_INIT);
      // addMethod( ret, "<java.lang.ref.Finalizer: void
      // register(java.lang.Object)>");
      addMethod(ret, JavaDefinitions.RUN_FINALIZE);
      addMethod(ret, JavaDefinitions.THREAD_INIT_RUNNABLE);
      addMethod(ret, JavaDefinitions.THREAD_INIT_STRING);
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
      List<SootMethod> ret = new ArrayList<>();
      view.getClasses().forEach(cl -> addMethod(ret, cl, sigClinit));
      return ret;
    }

    /** Returns a list of all clinits of class cl and its superclasses. */
    public Iterable<SootMethod> clinitsOf(SootClass cl) {
      // Do not create an actual list, since this method gets called quite often
      // Instead, callers usually just want to iterate over the result.
      Optional<? extends SootMethod> oinit = cl.getMethod(sigClinit);
      Optional<? extends ClassType> osuperClass = cl.getSuperclass();
      // check super classes until finds a constructor or no super class there anymore.
      while (oinit.isEmpty() && osuperClass.isPresent()) {
        ClassType superType = osuperClass.get();
        Optional<? extends SootClass> oSuperClass = view.getClass(superType);
        if (oSuperClass.isEmpty()) {
          break;
        }
        SootClass superClass = oSuperClass.get();
        oinit = superClass.getMethod(sigClinit);
        osuperClass = superClass.getSuperclass();
      }
      if (oinit.isEmpty()) {
        return Collections.emptyList();
      }
      SootMethod initStart = oinit.get();
      return () ->
          new Iterator<SootMethod>() {
            SootMethod current = initStart;

            @Override
            public SootMethod next() {
              if (!hasNext()) {
                throw new NoSuchElementException();
              }
              SootMethod n = current;

              // Pre-fetch the next element
              current = null;
              Optional<? extends SootClass> oCurrentClass =
                  view.getClass(n.getDeclaringClassType());
              if (oCurrentClass.isEmpty()) {
                return n;
              }
              SootClass currentClass = oCurrentClass.get();
              while (true) {
                Optional<? extends ClassType> osuperType1 = currentClass.getSuperclass();
                if (osuperType1.isEmpty()) {
                  break;
                }
                ClassType classType = osuperType1.get();
                Optional<? extends SootClass> osuperClass1 = view.getClass(classType);
                if (osuperClass1.isEmpty()) {
                  break;
                }
                SootClass superClass = osuperClass1.get();
                Optional<? extends SootMethod> om = superClass.getMethod(sigClinit);
                if (om.isPresent()) {
                  current = om.get();
                  break;
                }
                currentClass = superClass;
              }

              return n;
            }

            @Override
            public boolean hasNext() {
              return current != null;
            }
          };
    }
  }
}
