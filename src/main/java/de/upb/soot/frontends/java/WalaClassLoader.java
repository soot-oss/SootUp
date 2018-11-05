package de.upb.soot.frontends.java;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.TypeSignature;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
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
  public Optional<SootClass> getSootClass(JavaClassSignature signature) {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourceDirPath);
    String className = walaToSoot.convertClassNameFromSoot(signature.getFullyQualifiedName());
    JavaClass walaClass
        = (JavaClass) classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).lookupClass(TypeName.findOrCreate(className));
    if (className.contains("$") && walaClass == null) {
      // this is an inner class and was not found
      Iterator<IClass> it = classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).iterateAllClasses();
      while (it.hasNext()) {
        JavaClass c = (JavaClass) it.next();
        String cname = walaToSoot.convertClassNameFromWala(c.getName().toString());
        if (cname.equals(signature.getFullyQualifiedName())) {
          walaClass = c;
        }
      }
    }
    if (walaClass == null) {
      return Optional.empty();
    }
    SootClass sootClass = walaToSoot.convertClass(walaClass);
    return Optional.ofNullable(sootClass);
  }

  public Optional<SootMethod> getSootMethod(MethodSignature signature) {
    if (classHierarchy == null) {
      buildClassHierachy();
    }
    WalaIRToJimpleConverter walaToSoot = new WalaIRToJimpleConverter(this.sourceDirPath);
    String className = walaToSoot.convertClassNameFromSoot(signature.declClassSignature.getFullyQualifiedName());
    JavaClass walaClass
        = (JavaClass) classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).lookupClass(TypeName.findOrCreate(className));

    if (className.contains("$") && walaClass == null) {
      // this is an inner class and was not found
      Iterator<IClass> it = classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).iterateAllClasses();
      while (it.hasNext()) {
        JavaClass c = (JavaClass) it.next();
        String cname = walaToSoot.convertClassNameFromWala(c.getName().toString());
        if (cname.equals(signature.declClassSignature.getFullyQualifiedName())) {
          walaClass=c;
        }
      }
    }
    if (walaClass == null) {
      return Optional.empty();
    }

    for (IMethod walaMethod : walaClass.getAllMethods()) {
      TypeSignature ret = signature.typeSignature;
      Type retType = walaToSoot.convertType(walaMethod.getReturnType());
      if (walaMethod.getName().toString().equals(signature.name)) {
        if (retType.toString().equals(ret.toString())) {
          // compare parameter types
          boolean paraMatch = true;
          List<TypeSignature> paras = signature.parameterSignatures;
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
            SootMethod method = walaToSoot.convertMethod(signature.declClassSignature, (AstMethod) walaMethod);
            return Optional.ofNullable(method);
          }

        }
      }
    }
    return Optional.empty();
  }
}
