package de.upb.soot.frontends.java;

import de.upb.soot.core.SootClass;
import de.upb.soot.signatures.ClassSignature;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.warnings.Warnings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;

/**
 * This class loads java source code using WALA's java source code front-end.
 * 
 * @author Linghui Luo
 *
 */
public class WalaClassLoader {
  private String sourceDirPath;
  private String exclusionFilePath;
  private IClassHierarchy classHierarchy;
  private List<SootClass> sootClasses;

  public WalaClassLoader(String sourceDirPath, String exclusionFilePath) {
    this.sourceDirPath = sourceDirPath;
    this.exclusionFilePath = exclusionFilePath;
  }

  /**
   * Use WALA's JAVA source code front-end to build class hierachy.
   */
  private void buildClassHierachy() {
    AnalysisScope scope = new JavaSourceAnalysisScope();
    try {
      // add standard libraries to scope
      String[] stdlibs = WalaProperties.getJ2SEJarFiles();
      for (String stdlib : stdlibs) {
        scope.addToScope(ClassLoaderReference.Primordial, new JarFile(stdlib));
      }
      // add the source directory
      scope.addToScope(JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(sourceDirPath)));
      if (exclusionFilePath != null && !exclusionFilePath.isEmpty()) {
        FileOfClasses classes;
        classes = new FileOfClasses(new FileInputStream(new File(exclusionFilePath)));
        scope.setExclusions(classes);
      }
      // build the class hierarchy
      this.classHierarchy = ClassHierarchyFactory.make(scope, new ECJClassLoaderFactory(scope.getExclusions()));
      Warnings.clear();
    } catch (ClassHierarchyException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Return soot classes converted from WALA classes.
   * 
   * @return list of classes
   */
  public List<SootClass> getSootClasses() {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourceDirPath);
    Iterator<IClass> it = classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).iterateAllClasses();
    if (sootClasses == null) {
      sootClasses = new ArrayList<>();
    }
    while (it.hasNext()) {
      JavaClass walaClass = (JavaClass) it.next();
      SootClass sootClass = walaToSoot.convertClass(walaClass);
      sootClasses.add(sootClass);
    }
    return sootClasses;
  }

  /**
   * Return a soot class with the given signature converted from a WALA class.
   * 
   * @param signature
   * @return
   */
  public Optional<SootClass> getSootClass(ClassSignature signature) {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourceDirPath);
    String className = WalaIRToJimpleConverter.convertClassNameFromWala(signature.getFullyQualifiedName());

    JavaClass walaClass
        = (JavaClass) classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).lookupClass(TypeName.findOrCreate(className));
    if (walaClass == null) {
      return Optional.empty();
    }
    SootClass sootClass = walaToSoot.convertClass(walaClass);
    return Optional.ofNullable(sootClass);
  }
}
