package de.upb.soot.namespaces.classprovider;

import de.upb.soot.asm.SootModuleBuilder;
import de.upb.soot.core.SootModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.objectweb.asm.ClassReader;

public class AsmJavaModuleClassProvider extends AsmJavaClassProvider {

  public SootModule getSootModule(ClassSource classSource) {
    // get the inputstream or byte array somehow
    InputStream classSourceInputStream = null;
    try {
      classSourceInputStream = Files.newInputStream(classSource.getSourcePath());
      ClassReader clsr = new ClassReader(classSourceInputStream);
      SootModuleBuilder scb = new SootModuleBuilder();
      clsr.accept(scb, ClassReader.SKIP_FRAMES);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
