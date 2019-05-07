package de.upb.soot.frontends.java;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
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
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
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

  private void addScopesForJava() {
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

  public WalaClassLoader(String sourceDirPath) {
    this(sourceDirPath, null);
  }

  public WalaClassLoader(Set<String> sourcePath) {
    this(sourcePath, "");
  }

  public WalaClassLoader(Set<String> sourcePath, String exclusionFilePath) {
    addScopesForJava();
    this.sourcePath = sourcePath;
    try {
      // add the source directory to scope
      for (String path : sourcePath) {
        scope.addToScope(
            JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(path)));
      }
      // set exclusions
      if (exclusionFilePath != null) {
        File exclusionFile = new File(exclusionFilePath);
        if (exclusionFile.isFile()) {
          FileOfClasses classes;
          classes = new FileOfClasses(new FileInputStream(exclusionFile));
          scope.setExclusions(classes);
        }
      }
      factory = new ECJClassLoaderFactory(scope.getExclusions());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public WalaClassLoader(Set<String> sourcePath, Set<String> libPath, String exclusionFilePath) {
    addScopesForJava();
    this.sourcePath = sourcePath;
    try {
      // add the source directory to scope
      for (String path : sourcePath) {
        scope.addToScope(
            JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(path)));
      }
      // add Jars to scope
      for (String libJar : libPath) {
        scope.addToScope(ClassLoaderReference.Primordial, new JarFile(libJar));
      }
      // set exclusions
      if (exclusionFilePath != null) {
        File exclusionFile = new File(exclusionFilePath);
        if (exclusionFile.isFile()) {
          FileOfClasses classes;
          classes = new FileOfClasses(new FileInputStream(exclusionFile));
          scope.setExclusions(classes);
        }
      }
      factory = new ECJClassLoaderFactory(scope.getExclusions());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public WalaClassLoader(
      Set<String> sourcePath, String apkPath, String androidJar, String exclusionFilePath) {
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
      // set exclusions
      if (exclusionFilePath != null) {
        File exclusionFile = new File(exclusionFilePath);
        if (exclusionFile.isFile()) {
          FileOfClasses classes;
          classes = new FileOfClasses(new FileInputStream(exclusionFile));
          scope.setExclusions(classes);
        }
      }
      factory = new ECJClassLoaderFactory(scope.getExclusions());
    } catch (IllegalArgumentException | IOException e) {
      throw new RuntimeException("Failed to construct WalaClassLoader", e);
    }
  }

  /**
   * Constructor used for loading classes from given source code path.
   *
   * @param sourceDirPath
   * @param exclusionFilePath
   */
  public WalaClassLoader(String sourceDirPath, String exclusionFilePath) {
    addScopesForJava();
    this.sourcePath = Collections.singleton(sourceDirPath);
    try {
      // add the source directory to scope
      scope.addToScope(
          JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(sourceDirPath)));
      // set exclusions
      if (exclusionFilePath != null) {
        File exclusionFile = new File(exclusionFilePath);
        if (exclusionFile.isFile()) {
          FileOfClasses classes;
          classes = new FileOfClasses(new FileInputStream(exclusionFile));
          scope.setExclusions(classes);
        }
      }
      factory = new ECJClassLoaderFactory(scope.getExclusions());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Constructor used for LSP server.
   *
   * @param moduleFiles
   */
  public WalaClassLoader(Collection<? extends Module> moduleFiles) {
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
      com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass walaClass =
          (com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass) it.next();
      ClassSource sootClass = walaToSoot.convertToClassSource(walaClass);
      classSources.add(sootClass);
    }
    return classSources;
  }

  /**
   * Return soot classes converted from WALA classes.
   *
   * @return list of classes
   * @deprecated The WalaClassLoader should not create instances of SootClass. This is the
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
      com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass walaClass =
          (com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass) it.next();
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
   * @deprecated The WalaClassLoader should not create instances of SootClass. This is the
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
  public Optional<ClassSource> getClassSource(JavaClassType signature) {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath);
    JavaClass walaClass = loadWalaClass(signature, walaToSoot);
    return Optional.ofNullable(walaClass).map(walaToSoot::convertToClassSource);
  }

  @Nullable
  private JavaClass loadWalaClass(JavaClassType signature, WalaIRToJimpleConverter walaToSoot) {
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

  public Optional<SootMethod> getSootMethod(MethodSignature signature) {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourcePath);
    JavaClass walaClass = loadWalaClass(signature.getDeclClassSignature(), walaToSoot);
    if (walaClass == null) {
      return Optional.empty();
    }

    for (IMethod walaMethod : walaClass.getAllMethods()) {
      Type ret = signature.getSignature();
      Type retType = walaToSoot.convertType(walaMethod.getReturnType());
      if (walaMethod.getName().toString().equals(signature.getName())) {
        if (retType.toString().equals(ret.toString())) {
          // compare parameter types
          boolean paraMatch = true;
          List<Type> paras = signature.getParameterSignatures();
          int numParas = walaMethod.getNumberOfParameters();
          if (walaMethod.isStatic()) {
            if (paras.size() != numParas) {
              paraMatch = false;
            } else {
              for (int i = 0; i < numParas; i++) {
                String given = paras.get(i).toString();
                String para = walaToSoot.convertType(walaMethod.getParameterType(i)).toString();
                if (!given.equals(para)) {
                  paraMatch = false;
                }
              }
            }
          } else {
            if (paras.size() != numParas - 1) {
              paraMatch = false;
            } else {
              for (int i = 1; i < numParas; i++) {
                String given = paras.get(i - 1).toString();
                String para = walaToSoot.convertType(walaMethod.getParameterType(i)).toString();
                if (!given.equals(para)) {
                  paraMatch = false;
                }
              }
            }
          }
          if (paraMatch) {
            SootMethod method =
                walaToSoot.convertMethod(signature.getDeclClassSignature(), (AstMethod) walaMethod);
            return Optional.ofNullable(method);
          }
        }
      }
    }
    return Optional.empty();
  }
}
