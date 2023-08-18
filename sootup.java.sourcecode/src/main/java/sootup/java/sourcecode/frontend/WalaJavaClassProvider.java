package sootup.java.sourcecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Markus Schmidt and others
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
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.config.FileOfClasses;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.SourceTypeSpecifier;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.ResolveException;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;

/**
 * A {@link ClassProvider} that can read Java source code
 *
 * @author Linghui Luo
 */
public class WalaJavaClassProvider implements ClassProvider<JavaSootClass> {

  private Set<String> sourcePath;
  private IClassHierarchy classHierarchy;
  private List<SootClass<?>> sootClasses;
  private List<SootClassSource<JavaSootClass>> classSources;
  private AnalysisScope scope;
  private ClassLoaderFactory factory;
  private final File walaPropertiesFile = new File("wala.properties");

  public WalaJavaClassProvider(@Nonnull String sourceDirPath) {
    this(sourceDirPath, null);
  }

  public WalaJavaClassProvider(@Nonnull String sourceDirPath, @Nullable String exclusionFilePath) {
    this(Collections.singleton(sourceDirPath), exclusionFilePath);
  }

  public WalaJavaClassProvider(@Nonnull Set<String> sourcePath) {
    this(sourcePath, null);
  }

  public WalaJavaClassProvider(
      @Nonnull Set<String> sourcePath, @Nullable String exclusionFilePath) {
    addScopesForJava();
    this.sourcePath = sourcePath;
    // add the source directory to scope
    for (String path : sourcePath) {
      scope.addToScope(
          JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(path)));
    }
    setExclusions(exclusionFilePath);
    factory = new ECJClassLoaderFactory(scope.getExclusions());
  }

  public WalaJavaClassProvider(
      @Nonnull Set<String> sourcePath,
      @Nonnull Set<String> libPath,
      @Nonnull String exclusionFilePath) {
    addScopesForJava();
    this.sourcePath = sourcePath;
    // add the source directory to scope
    for (String path : sourcePath) {
      scope.addToScope(
          JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(path)));
    }
    try {
      // add Jars to scope
      for (String libJar : libPath) {
        scope.addToScope(ClassLoaderReference.Primordial, new JarFile(libJar));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    setExclusions(exclusionFilePath);
    factory = new ECJClassLoaderFactory(scope.getExclusions());
  }

  public WalaJavaClassProvider(
      @Nonnull Set<String> sourcePath,
      @Nonnull String apkPath,
      @Nonnull String androidJar,
      @Nullable String exclusionFilePath) {
    addScopesForJava();
    this.sourcePath = sourcePath;
    try {
      // add the source directory to scope
      for (String path : sourcePath) {
        scope.addToScope(
            JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(path)));
      }
      scope.setLoaderImpl(
          ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
      // add androidJar and apkPath to scope
      scope.addToScope(ClassLoaderReference.Primordial, new JarFile(androidJar));
      scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(new File(apkPath)));
      setExclusions(exclusionFilePath);
      factory = new ECJClassLoaderFactory(scope.getExclusions());
    } catch (IllegalArgumentException | IOException e) {
      throw new RuntimeException("Failed to construct frontend.WalaJavaClassProvider", e);
    }
  }

  /**
   * Constructor used for loading classes from given source code path.
   *
   * @param sourceDirPath
   * @param exclusionFilePath
   */
  public WalaJavaClassProvider(
      @Nonnull String sourceDirPath,
      @Nullable String exclusionFilePath,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    addScopesForJava();
    this.sourcePath = Collections.singleton(sourceDirPath);
    // add the source directory to scope
    scope.addToScope(
        JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(sourceDirPath)));
    setExclusions(exclusionFilePath);
    factory = new ECJClassLoaderFactory(scope.getExclusions());
  }

  /**
   * Constructor used for LSP server.
   *
   * @param moduleFiles
   */
  public WalaJavaClassProvider(
      @Nonnull Collection<? extends Module> moduleFiles,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    addScopesForJava();
    for (Module m : moduleFiles) {
      scope.addToScope(JavaSourceAnalysisScope.SOURCE, m);
    }
    factory = new ECJClassLoaderFactory(scope.getExclusions());
  }

  /** Create wala.properties to class path */
  private void createWalaProperties() {
    if (!walaPropertiesFile.exists()) {
      PrintWriter pw;
      try {
        pw = new PrintWriter(walaPropertiesFile);
        String jdkPath = System.getProperty("java.home");
        pw.println("java_runtime_dir = " + new File(jdkPath).toString().replace("\\", "/"));
        pw.close();
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void addScopesForJava() {
    createWalaProperties();
    // disable System.err messages generated from eclipse jdt
    System.setProperty("wala.jdt.quiet", "true");
    scope = new JavaSourceAnalysisScope();
    try {
      // add standard libraries to scope
      String[] stdlibs = WalaProperties.getJ2SEJarFiles();
      for (String stdlib : stdlibs) {

        scope.addToScope(ClassLoaderReference.Primordial, new JarFile(stdlib));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Use WALA's JAVA source code front-end to build class hierarchy. */
  private void buildClassHierachy() {
    try {
      this.classHierarchy = ClassHierarchyFactory.make(scope, factory);
    } catch (ClassHierarchyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Return ClassSources converted from WALA classes.
   *
   * @return list of classes
   */
  public List<SootClassSource<JavaSootClass>> getClassSources(SourceType srcType) {
    Iterator<IClass> it = iterateWalaClasses();
    if (classSources == null) {
      classSources = new ArrayList<>();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath, srcType);
    while (it.hasNext()) {
      JavaSourceLoaderImpl.JavaClass walaClass = (JavaSourceLoaderImpl.JavaClass) it.next();
      SootClassSource<JavaSootClass> sootClass = walaToSoot.convertToClassSource(walaClass);
      classSources.add(sootClass);
    }
    return classSources;
  }

  /**
   * Return soot classes converted from WALA classes.
   *
   * @return list of classes
   * @deprecated The frontend.WalaJavaClassProvider should not create instances of SootClass. This
   *     is the responsibility of the View.
   */
  @Deprecated
  public List<SootClass<?>> getSootClasses() {
    Iterator<IClass> it = iterateWalaClasses();
    if (sootClasses == null) {
      sootClasses = new ArrayList<>();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath);
    while (it.hasNext()) {
      JavaSourceLoaderImpl.JavaClass walaClass = (JavaSourceLoaderImpl.JavaClass) it.next();
      SootClass<?> sootClass = walaToSoot.convertClass(walaClass);
      sootClasses.add(sootClass);
    }
    return sootClasses;
  }

  private Iterator<IClass> iterateWalaClasses() {
    if (classHierarchy == null) {
      try {
        buildClassHierachy();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).iterateAllClasses();
  }

  /**
   * Return a soot class with the given signature converted from a WALA class.
   *
   * @deprecated The frontend.WalaJavaClassProvider should not create instances of SootClass. This
   *     is the responsibility of the View.
   */
  @Deprecated
  public Optional<SootClass> getSootClass(JavaClassType signature) {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath);
    JavaSourceLoaderImpl.JavaClass walaClass = loadWalaClass(signature, walaToSoot);
    return Optional.ofNullable(walaClass).map(walaToSoot::convertClass);
  }

  /** Return a ClassSource with the given signature converted from a WALA class. */
  public Optional<SootClassSource<JavaSootClass>> getClassSource(ClassType signature) {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath);
    JavaSourceLoaderImpl.JavaClass walaClass = loadWalaClass(signature, walaToSoot);
    return Optional.ofNullable(walaClass).map(walaToSoot::convertToClassSource);
  }

  @Nullable
  private JavaSourceLoaderImpl.JavaClass loadWalaClass(
      ClassType signature, WalaIRToJimpleConverter walaToSoot) {
    String className = walaToSoot.convertClassNameFromSoot(signature.getFullyQualifiedName());

    IClass clazz =
        classHierarchy
            .getLoader(JavaSourceAnalysisScope.SOURCE)
            .lookupClass(TypeName.findOrCreate(className));

    JavaSourceLoaderImpl.JavaClass walaClass = null;
    if (clazz instanceof JavaSourceLoaderImpl.JavaClass) {
      walaClass = (JavaSourceLoaderImpl.JavaClass) clazz;
    }

    if (walaClass == null && className.contains("$")) {
      // search for a possible inner class
      Iterator<IClass> it =
          classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).iterateAllClasses();
      while (it.hasNext()) {
        JavaSourceLoaderImpl.JavaClass c = (JavaSourceLoaderImpl.JavaClass) it.next();
        String cname = walaToSoot.convertClassNameFromWala(c.getName().toString());
        if (cname.equals(signature.getFullyQualifiedName())) {
          walaClass = c;
        }
      }
    }
    return walaClass;
  }

  private void setExclusions(@Nullable String exclusionFilePath) {
    if (exclusionFilePath == null) {
      return;
    }

    File exclusionFile = new File(exclusionFilePath);
    if (exclusionFile.isFile()) {
      FileOfClasses classes;
      try {
        classes = new FileOfClasses(Files.newInputStream(exclusionFile.toPath()));
        scope.setExclusions(classes);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      throw new ResolveException(
          "the given path to the exclusion file does not point to a file.", exclusionFile.toPath());
    }
  }

  @Override
  public Optional<SootClassSource<JavaSootClass>> createClassSource(
      AnalysisInputLocation<? extends SootClass<?>> srcNamespace, Path sourcePath, ClassType type) {
    return getClassSource(type);
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JAVA;
  }
}
