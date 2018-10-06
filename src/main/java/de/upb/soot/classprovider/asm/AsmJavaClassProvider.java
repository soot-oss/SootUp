package de.upb.soot.classprovider.asm;

import de.upb.soot.classprovider.ClassSource;
import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.views.Scene;

import java.nio.file.Path;

public class AsmJavaClassProvider extends de.upb.soot.classprovider.ClassProvider {

  // the scene for which the classprovider generates the classes
  private final Scene scene;

  public AsmJavaClassProvider(Scene scene) {
    this.scene = scene;
  }

  @Override
  public ClassSource createClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature) {
    return new ClassSource(srcNamespace, sourcePath, classSignature, this);
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  @Override
  public SootClass reify(ClassSource classSource) {

    // for modules
    if (classSource.getClassSignature().isModuleInfo()) {
      return getSootModule(classSource, new de.upb.soot.classprovider.asm.modules.UnresolvedModuleVisitor());
    }
    // for classes
    return null;

  }

  public de.upb.soot.core.SootClass resolve(de.upb.soot.core.SootClass sootClass) {
    ClassSource classSource = sootClass.getCs();
    if (classSource.getClassSignature().isModuleInfo()) {
      return getSootModule(classSource,
          new de.upb.soot.classprovider.asm.modules.ResolveModuleVisitor((de.upb.soot.core.SootModuleInfo) sootClass, this));

    }
    // for classes
    return null;
  }

  private de.upb.soot.core.SootModuleInfo getSootModule(ClassSource classSource, org.objectweb.asm.ModuleVisitor visitor) {

    de.upb.soot.classprovider.asm.modules.SootModuleBuilder scb
        = new de.upb.soot.classprovider.asm.modules.SootModuleBuilder(classSource, visitor);
    java.net.URI uri = classSource.getSourcePath().toUri();

    try {
      if (classSource.getSourcePath().getFileSystem().isOpen()) {
        Path sourceFile = java.nio.file.Paths.get(uri);

        org.objectweb.asm.ClassReader clsr
            = new org.objectweb.asm.ClassReader(java.nio.file.Files.newInputStream(sourceFile));

        clsr.accept(scb, org.objectweb.asm.ClassReader.SKIP_FRAMES);
      } else {
        // a zip file system needs to be re-openend
        // otherwise it crashes
        // http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
        java.util.Map<String, String> env = new java.util.HashMap<>();
        env.put("create", "false");
        try (java.nio.file.FileSystem zipfs = java.nio.file.FileSystems.newFileSystem(uri, env)) {
          Path sourceFile = java.nio.file.Paths.get(uri);

          org.objectweb.asm.ClassReader clsr
              = new org.objectweb.asm.ClassReader(java.nio.file.Files.newInputStream(sourceFile));

          clsr.accept(scb, org.objectweb.asm.ClassReader.SKIP_FRAMES);
        }
      }

    } catch (java.io.IOException e) {
      e.printStackTrace();
    }

    return scb.result;
  }

  public Scene getScene() {
    return scene;
  }

}
