package de.upb.swt.soot.java.bytecode.generating;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.views.View;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import jdk.internal.org.objectweb.asm.ClassWriter;

public class BytecodeWriter {

  public static void write(View<?> view, Path rootDir) {
    view.getClasses()
        .forEach(
            sc -> {
              writeFile(
                  sc, rootDir.resolve(sc.getName().replace('.', File.separatorChar) + ".class"));
            });
  }

  public static void writeFile(@Nonnull SootClass<?> sc, @Nonnull Path p) {

    byte[] bytes = generateByteCode(sc);

    try (FileOutputStream fos = new FileOutputStream(p.toFile())) {
      fos.write(bytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static byte[] generateByteCode(SootClass<?> sc) {

    ClassWriter cw = new ClassWriter(0);
    cw.visit(
        V1_5,
        ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
        "pkg/Comparable",
        null,
        "java/lang/Object",
        new String[] {"pkg/Mesurable"});
    cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "LESS", "I", null, new Integer(-1))
        .visitEnd();
    cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "EQUAL", "I", null, new Integer(0))
        .visitEnd();
    cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "GREATER", "I", null, new Integer(1))
        .visitEnd();
    cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "compareTo", "(Ljava/lang/Object;)I", null, null)
        .visitEnd();
    cw.visitEnd();

    return cw.toByteArray();
  }
}
