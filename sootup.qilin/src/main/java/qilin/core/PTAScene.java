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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qilin.core.builder.FakeMainFactory;
import qilin.pta.PTAConfig;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import soot.jimple.toolkits.callgraph.CallGraph;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.jimple.basic.Value;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.interceptors.TypeAssigner;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

public class PTAScene {
  private static final Logger logger = LoggerFactory.getLogger(PTAScene.class);
  private static volatile PTAScene instance = null;

  private final View view;
  private CallGraph callgraph;
  private final FakeMainFactory fakeMainFactory;

  private SootClass mainClass = null;

  public void setMainClass(SootClass m) {
    mainClass = m;
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
    /**
     * Set the soot class path to point to the default class path appended with the app path (the
     * classes dir or the application jar) and jar files in the library dir of the application.
     */
    List<String> cps = new ArrayList<>();
    PTAConfig.ApplicationConfiguration appConfig = PTAConfig.v().getAppConfig();
    // note that the order is important!
    cps.add(appConfig.APP_PATH);
    cps.addAll(getLibJars(appConfig.LIB_PATH));
    cps.addAll(getJreJars(appConfig.JRE));
    final String classpath = String.join(File.pathSeparator, cps);
    System.out.println(cps.size());
    for (String s : cps) {
      System.out.println("\t cps:" + s);
    }
    //    System.out.println("xxx:" + classpath);
    logger.info("Setting Soot ClassPath: {}", classpath);
    //    System.setProperty("soot.class.path", classpath);
    this.view = createViewForClassPath(cps);
    // setup mainclass
    if (appConfig.MAIN_CLASS == null) {
      appConfig.MAIN_CLASS = PTAUtils.findMainFromMetaInfo(appConfig.APP_PATH);
    }
    this.mainClass = getSootClass(appConfig.MAIN_CLASS);
    // setup fakemain
    this.fakeMainFactory = new FakeMainFactory(view, mainClass);
  }

  private static JavaView createViewForClassPath(List<String> classPaths) {
    ClassLoadingOptions clo = () -> Collections.singletonList(new TypeAssigner());
    JavaProject.JavaProjectBuilder builder = JavaProject.builder(new JavaLanguage(8));
    for (String clazzPath : classPaths) {
      builder.addInputLocation(new JavaClassPathAnalysisInputLocation(clazzPath));
    }
    JavaProject javaProject = builder.build();
    return javaProject.createOnDemandView(analysisInputLocation -> clo);
  }

  private static Collection<String> getJreJars(String JRE) {
    if (JRE == null) {
      return Collections.emptySet();
    }
    final String jreLibDir = JRE + File.separator + "lib";
    return FileUtils.listFiles(new File(jreLibDir), new String[] {"jar"}, false).stream()
        .map(File::toString)
        .collect(Collectors.toList());
  }

  /** Returns a collection of files, one for each of the jar files in the app's lib folder */
  private static Collection<String> getLibJars(String LIB_PATH) {
    if (LIB_PATH == null) {
      return Collections.emptySet();
    }
    File libFile = new File(LIB_PATH);
    if (libFile.exists()) {
      if (libFile.isDirectory()) {
        return FileUtils.listFiles(libFile, new String[] {"jar"}, true).stream()
            .map(File::toString)
            .collect(Collectors.toList());
      } else if (libFile.isFile()) {
        if (libFile.getName().endsWith(".jar")) {
          return Collections.singletonList(LIB_PATH);
        }
        logger.error(
            "Project not configured properly. Application library path {} is not a jar file.",
            libFile);
        System.exit(1);
      }
    }
    logger.error(
        "Project not configured properly. Application library path {} is not correct.", libFile);
    System.exit(1);
    return null;
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

  public boolean canStoreType(final Type child, final Type parent) {
    return view.getTypeHierarchy().isSubtype(parent, child);
  }

  public SootMethod getMethod(String methodSignature) {
    MethodSignature mthdSig =
        JavaIdentifierFactory.getInstance().parseMethodSignature(methodSignature);
    return (SootMethod) view.getMethod(mthdSig).get();
  }

  public Collection<SootClass> getApplicationClasses() {
    Collection<SootClass> classes = view.getClasses();
    return classes.stream().filter(SootClass::isApplicationClass).collect(Collectors.toSet());
    //        for (SootClass sc : classes) {
    //            sc.isApplicationClass()
    //        }
    //        return sootScene.getApplicationClasses();
  }

  public Collection<SootClass> getLibraryClasses() {
    Collection<SootClass> classes = view.getClasses();
    return classes.stream().filter(SootClass::isLibraryClass).collect(Collectors.toSet());
    //        return sootScene.getLibraryClasses();
  }

  public boolean containsMethod(String methodSignature) {
    MethodSignature mthdSig =
        JavaIdentifierFactory.getInstance().parseMethodSignature(methodSignature);
    return view.getMethod(mthdSig).isPresent();
  }

  public boolean containsField(String fieldSignature) {
    FieldSignature fieldSig =
        JavaIdentifierFactory.getInstance().parseFieldSignature(fieldSignature);
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
    FieldSignature fieldSig =
        JavaIdentifierFactory.getInstance().parseFieldSignature(fieldSignature);
    return (SootField) view.getField(fieldSig).get();
  }
}
