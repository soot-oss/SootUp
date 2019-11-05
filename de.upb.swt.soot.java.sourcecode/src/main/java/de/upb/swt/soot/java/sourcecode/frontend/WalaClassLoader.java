package de.upb.swt.soot.java.sourcecode.frontend;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
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
import com.ibm.wala.util.warnings.Warnings;
import de.upb.swt.soot.core.SourceTypeSpecifier;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class loads java source code using WALA's java source code front-end.
 *
 * @author Linghui Luo
 */
public class WalaClassLoader {
  private Set<String> sourcePath;
  private IClassHierarchy classHierarchy;
  private List<SootClass> sootClasses;
  private List<ClassSource> classSources;
  private AnalysisScope scope;
  private ClassLoaderFactory factory;
  private final File walaPropertiesFile = new File("target/classes/wala.properties");

  /** Create wala.properties to class path */
  private void createWalaproperties() {
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
    createWalaproperties();
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

  public WalaClassLoader(@Nonnull String sourceDirPath) {
    this(sourceDirPath, null);
  }

  public WalaClassLoader(@Nonnull String sourceDirPath, @Nullable String exclusionFilePath) {
    this(Collections.singleton(sourceDirPath), exclusionFilePath);
  }

  public WalaClassLoader(@Nonnull Set<String> sourcePath) {
    this(sourcePath, null);
  }

  public WalaClassLoader(@Nonnull Set<String> sourcePath, @Nullable String exclusionFilePath) {
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

  public WalaClassLoader(
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

  public WalaClassLoader(
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
      throw new RuntimeException("Failed to construct frontend.WalaClassLoader", e);
    }
  }

  /**
   * Constructor used for loading classes from given source code path.
   *
   * @param sourceDirPath
   * @param exclusionFilePath
   */
  public WalaClassLoader(
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
  public WalaClassLoader(
      @Nonnull Collection<? extends Module> moduleFiles,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    addScopesForJava();
    for (Module m : moduleFiles) {
      scope.addToScope(JavaSourceAnalysisScope.SOURCE, m);
    }
    factory = new ECJClassLoaderFactory(scope.getExclusions());
  }

  /** Use WALA's JAVA source code front-end to build class hierarchy. */
  private void buildClassHierachy() {
    try {
      this.classHierarchy = ClassHierarchyFactory.make(scope, factory);
      Warnings.clear();
    } catch (ClassHierarchyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Return ClassSources converted from WALA classes.
   *
   * @return list of classes
   */
  public List<ClassSource> getClassSources() {
    Iterator<IClass> it = iterateWalaClasses();
    if (classSources == null) {
      classSources = new ArrayList<>();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath);
    while (it.hasNext()) {
      JavaClass walaClass = (JavaClass) it.next();
      ClassSource sootClass = walaToSoot.convertToClassSource(walaClass);
      classSources.add(sootClass);
    }
    return classSources;
  }

  /**
   * Return soot classes converted from WALA classes.
   *
   * @return list of classes
   * @deprecated The frontend.WalaClassLoader should not create instances of SootClass. This is the
   *     responsibility of the View.
   */
  @Deprecated
  public List<SootClass> getSootClasses() {
    Iterator<IClass> it = iterateWalaClasses();
    if (sootClasses == null) {
      sootClasses = new ArrayList<>();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath);
    while (it.hasNext()) {
      JavaClass walaClass = (JavaClass) it.next();
      SootClass sootClass = walaToSoot.convertClass(walaClass);
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
   * @deprecated The frontend.WalaClassLoader should not create instances of SootClass. This is the
   *     responsibility of the View.
   */
  @Deprecated
  public Optional<SootClass> getSootClass(JavaClassType signature) {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath);
    JavaClass walaClass = loadWalaClass(signature, walaToSoot);
    return Optional.ofNullable(walaClass).map(walaToSoot::convertClass);
  }

  /** Return a ClassSource with the given signature converted from a WALA class. */
  public Optional<ClassSource> getClassSource(ClassType signature) {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath);
    JavaClass walaClass = loadWalaClass(signature, walaToSoot);
    return Optional.ofNullable(walaClass).map(walaToSoot::convertToClassSource);
  }

  @Nullable
  private JavaClass loadWalaClass(ClassType signature, WalaIRToJimpleConverter walaToSoot) {
    String className = walaToSoot.convertClassNameFromSoot(signature.getFullyQualifiedName());
    JavaClass walaClass =
        (JavaClass)
            classHierarchy
                .getLoader(JavaSourceAnalysisScope.SOURCE)
                .lookupClass(TypeName.findOrCreate(className));
    if (className.contains("$") && walaClass == null) {
      // this is an inner class and was not found
      Iterator<IClass> it =
          classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).iterateAllClasses();
      while (it.hasNext()) {
        JavaClass c = (JavaClass) it.next();
        String cname = walaToSoot.convertClassNameFromWala(c.getName().toString());
        if (cname.equals(signature.getFullyQualifiedName())) {
          walaClass = c;
        }
      }
    }
    return walaClass;
  }

  private void setExclusions(@Nullable String exclusionFilePath) {
    // set exclusions
    if (exclusionFilePath != null) {
      File exclusionFile = new File(exclusionFilePath);
      if (exclusionFile.isFile()) {
        FileOfClasses classes;
        try {
          classes = new FileOfClasses(new FileInputStream(exclusionFile));
          scope.setExclusions(classes);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        throw new ResolveException(
            "the given path to the exclusion file does not point to a file.");
      }
    }
  }
}
