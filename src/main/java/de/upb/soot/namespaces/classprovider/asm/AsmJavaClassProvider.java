package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;

import java.nio.file.Path;

public class AsmJavaClassProvider implements de.upb.soot.namespaces.classprovider.IClassProvider {

  public AsmJavaClassProvider() {
  }

  @Override
  public de.upb.soot.namespaces.classprovider.AbstractClassSource createClassSource(
      de.upb.soot.namespaces.INamespace srcNamespace, java.nio.file.Path sourcePath,
      de.upb.soot.signatures.JavaClassSignature classSignature) {
    return new de.upb.soot.namespaces.classprovider.asm.AsmClassSource(srcNamespace, sourcePath, classSignature);
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  /**
   * Provide the ASM representation of the class file.
   *
   * @param classSource
   *          The source to be read.
   * @return A representation of the class file.
   */
  @Override
  public de.upb.soot.namespaces.classprovider.ISourceContent getContent(AbstractClassSource classSource) {
    AsmClassSourceContent asmClassSourceContent = new AsmClassSourceContent();
    java.net.URI uri = classSource.getSourcePath().toUri();

    try {
      if (classSource.getSourcePath().getFileSystem().isOpen()) {
        Path sourceFile = java.nio.file.Paths.get(uri);

        org.objectweb.asm.ClassReader clsr
            = new org.objectweb.asm.ClassReader(java.nio.file.Files.newInputStream(sourceFile));

        clsr.accept(asmClassSourceContent, org.objectweb.asm.ClassReader.SKIP_FRAMES);
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

          clsr.accept(asmClassSourceContent, org.objectweb.asm.ClassReader.SKIP_FRAMES);
        }
      }

    } catch (java.io.IOException e) {
      e.printStackTrace();
    }

    return asmClassSourceContent;
  }

}
