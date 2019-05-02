package de.upb.soot.frontends.java;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.IView;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/** @author Linghui Luo */
public class WalaJavaClassProvider implements IClassProvider {

  @Override
  public ClassSource createClassSource(
      INamespace srcNamespace, Path sourcePath, JavaClassType classSignature) {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  public FileType getHandledFileType() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  public IClassSourceContent getContent(ClassSource classSource) {
    // TODO Implement the interface below
//    WalaIRToJimpleConverter walaToSoot =
//        new WalaIRToJimpleConverter(
//            Utils.immutableSetOf(Stream.of(classSource.getSourcePath().toString())));
//    Iterator<IClass> it =
//        classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).iterateAllClasses();
//    if (sootClasses == null) {
//      sootClasses = new ArrayList<>();
//    }
//    while (it.hasNext()) {
//      com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass walaClass =
//          (com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass) it.next();
//      SootClass sootClass = walaToSoot.convertClass(walaClass);
//      sootClasses.add(sootClass);
//    }
//    return sootClasses;

    return new IClassSourceContent() {
      @Nonnull
      @Override
      public AbstractClass resolveClass(@Nonnull ResolvingLevel level, @Nonnull IView view)
          throws ResolveException {
        return null;
      }

      @Override
      public Set<Modifier> resolveModifiers(JavaClassType type) {
        return null;
      }

      @Override
      public Set<JavaClassType> resolveInterfaces(JavaClassType type) {
        return null;
      }

      @Override
      public Optional<JavaClassType> resolveSuperclass(JavaClassType type) {
        return Optional.empty();
      }

      @Override
      public Optional<JavaClassType> resolveOuterClass(JavaClassType type) {
        return Optional.empty();
      }

      @Override
      public Position resolvePosition(JavaClassType type) {
        return null;
      }
    };
  }
}
