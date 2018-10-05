package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.views.Scene;

import java.nio.file.Path;
import java.util.Optional;

public class AsmJavaClassProvider implements IClassProvider {

  // the scene for which the classprovider generates the classes
  private final Scene scene;

  public AsmJavaClassProvider(Scene scene) {
    this.scene = scene;
  }

  @Override
  public ClassSource createClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature) {
    return new ClassSource(srcNamespace, sourcePath, classSignature);
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  @Override
  public Optional<SootClass> reify(ClassSource classSource) {
    return scene.reifyClass(classSource);
  }

  public Scene getScene() {
    return scene;
  }

}
