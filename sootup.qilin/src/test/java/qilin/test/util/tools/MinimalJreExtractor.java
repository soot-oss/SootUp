/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.test.util.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

/**
 * Standalone, not-run-by-the-build tool: extracts the transitive class-dependency closure of a
 * fixed set of {@code java.util} entry classes out of a real jre1.6.0_45's {@code rt.jar}, and
 * packs just those classes (unmodified jre1.6 bytecode, byte-for-byte) into a small jar. Used to
 * regenerate {@code src/test/resources/jre1.6.0_45-min/lib/jre1.6.0_45-collections-min.jar}, which
 * lets {@link qilin.test.util.QilinLegacyFrameworkTests} (and, through it, {@link
 * qilin.test.context.CollectionsTests}) run without depending on the full ~50MB rt.jar (which isn't
 * committed to this repo and isn't provisioned by CI - see that class's javadoc).
 *
 * <p>Run manually, not part of any Maven phase:
 *
 * <pre>{@code
 * mvn -pl sootup.qilin -am compile test-compile
 * java -cp sootup.qilin/target/classes:sootup.qilin/target/test-classes:$(find ~/.m2 -name 'asm-*.jar' | tr '\n' ':') \
 *     qilin.test.util.tools.MinimalJreExtractor \
 *     /path/to/jre1.6.0_45/lib/rt.jar \
 *     sootup.qilin/src/test/resources/jre1.6.0_45-min/lib/jre1.6.0_45-collections-min.jar
 * }</pre>
 *
 * If a future test needs another {@code java.util}/{@code java.lang} type not already pulled in
 * transitively, add it to {@link #ENTRY_CLASSES} and rerun. Some types are only ever reached
 * through qilin's own synthetic native-method modeling (e.g. {@code java.lang.ref.Finalizer},
 * {@code java.io.UnixFileSystem}) rather than real bytecode references - the BFS can't discover
 * those on its own, so they're listed as extra entry points too.
 */
public final class MinimalJreExtractor {

  /** Only what {@code qilin.microben.context.collections.*} actually imports. */
  private static final String[] ENTRY_CLASSES = {
    "java/util/ArrayList",
    "java/util/LinkedList",
    "java/util/Vector",
    "java/util/HashMap",
    "java/util/Map",
    "java/util/TreeMap",
    "java/util/HashSet",
    "java/util/TreeSet",
    "java/util/Hashtable",
    "java/util/PriorityQueue",
    "java/util/Stack",
    // Not referenced from any of the above by name - the JVM calls
    // java.lang.ref.Finalizer.register implicitly for every allocation of a class overriding
    // finalize() (e.g. Hashtable, PriorityQueue in jre1.6); qilin.core.solver.Solver mirrors
    // that (see handleImplicitCallToFinalizerRegister) and needs it resolvable.
    "java/lang/ref/Finalizer",
    // Hashtable/PriorityQueue's Serializable machinery reaches java.io.File's clinit
    // (FileSystem.getFileSystem()); like Finalizer, the concrete FileSystem impl is chosen at
    // runtime (os.name), so no bytecode ever references it by name - qilin's native model for
    // that native method (JavaIoFileSystemGetFileSystemNative) constructs one explicitly.
    "java/io/UnixFileSystem",
  };

  private MinimalJreExtractor() {}

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("usage: MinimalJreExtractor <path-to-rt.jar> <output-jar>");
      System.exit(1);
    }
    String rtJarPath = args[0];
    String outJarPath = args[1];

    Set<String> visited = new LinkedHashSet<>();
    Deque<String> queue = new ArrayDeque<>();
    queue.addAll(Arrays.asList(ENTRY_CLASSES));

    // internalName -> class bytes, sorted for a deterministic/reproducible output jar.
    TreeMap<String, byte[]> collected = new TreeMap<>();

    try (ZipFile rtJar = new ZipFile(rtJarPath)) {
      while (!queue.isEmpty()) {
        String name = queue.poll();
        if (!visited.add(name)) {
          continue;
        }
        ZipEntry entry = rtJar.getEntry(name + ".class");
        if (entry == null) {
          // e.g. classes outside rt.jar (shouldn't happen for java.* refs) - skip, not fatal:
          // qilin now tolerates unresolvable (super-)classes gracefully.
          System.err.println("skip (not found in rt.jar): " + name);
          continue;
        }
        byte[] bytes;
        try (InputStream in = rtJar.getInputStream(entry)) {
          bytes = in.readAllBytes();
        }
        collected.put(name, bytes);
        for (String ref : referencedClasses(bytes)) {
          if (!visited.contains(ref)) {
            queue.add(ref);
          }
        }
      }
    }

    try (JarOutputStream out = new JarOutputStream(new java.io.FileOutputStream(outJarPath))) {
      for (var e : collected.entrySet()) {
        out.putNextEntry(new JarEntry(e.getKey() + ".class"));
        out.write(e.getValue());
        out.closeEntry();
      }
    }
    System.out.println(
        "wrote " + collected.size() + " classes to " + outJarPath + " from " + rtJarPath);
  }

  /** Every internal class name referenced anywhere in this class file. */
  private static Set<String> referencedClasses(byte[] classBytes) {
    Set<String> refs = new LinkedHashSet<>();
    ClassReader cr = new ClassReader(classBytes);
    cr.accept(
        new ClassVisitor(Opcodes.ASM9) {
          @Override
          public void visit(
              int version,
              int access,
              String name,
              String signature,
              String superName,
              String[] interfaces) {
            addInternalName(refs, superName);
            if (interfaces != null) {
              for (String i : interfaces) {
                addInternalName(refs, i);
              }
            }
          }

          @Override
          public FieldVisitor visitField(
              int access, String name, String descriptor, String signature, Object value) {
            addDescriptor(refs, descriptor);
            return null;
          }

          @Override
          public MethodVisitor visitMethod(
              int access, String name, String descriptor, String signature, String[] exceptions) {
            for (Type t : Type.getArgumentTypes(descriptor)) {
              addType(refs, t);
            }
            addType(refs, Type.getReturnType(descriptor));
            if (exceptions != null) {
              for (String ex : exceptions) {
                addInternalName(refs, ex);
              }
            }
            return new MethodVisitor(Opcodes.ASM9) {
              @Override
              public void visitTypeInsn(int opcode, String type) {
                addPossiblyArrayInternalName(refs, type);
              }

              @Override
              public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                addInternalName(refs, owner);
                addDescriptor(refs, descriptor);
              }

              @Override
              public void visitMethodInsn(
                  int opcode, String owner, String name, String descriptor, boolean isInterface) {
                addPossiblyArrayInternalName(refs, owner);
                for (Type t : Type.getArgumentTypes(descriptor)) {
                  addType(refs, t);
                }
                addType(refs, Type.getReturnType(descriptor));
              }

              @Override
              public void visitLdcInsn(Object value) {
                if (value instanceof Type) {
                  addType(refs, (Type) value);
                }
              }

              @Override
              public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
                addDescriptor(refs, descriptor);
              }

              @Override
              public void visitTryCatchBlock(
                  org.objectweb.asm.Label start,
                  org.objectweb.asm.Label end,
                  org.objectweb.asm.Label handler,
                  String type) {
                if (type != null) {
                  addInternalName(refs, type);
                }
              }

              @Override
              public AnnotationVisitor visitTypeAnnotation(
                  int typeRef, TypePath typePath, String descriptor, boolean visible) {
                return null;
              }
            };
          }

          @Override
          public void visitInnerClass(String name, String outerName, String innerName, int access) {
            addInternalName(refs, name);
          }
        },
        ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    return refs;
  }

  private static void addType(Set<String> refs, Type t) {
    while (t.getSort() == Type.ARRAY) {
      t = t.getElementType();
    }
    if (t.getSort() == Type.OBJECT) {
      refs.add(t.getInternalName());
    }
  }

  private static void addDescriptor(Set<String> refs, String descriptor) {
    addType(refs, Type.getType(descriptor));
  }

  private static void addInternalName(Set<String> refs, String internalName) {
    if (internalName != null) {
      refs.add(internalName);
    }
  }

  /**
   * visitTypeInsn/visitMethodInsn operands may be a plain internal name (e.g. {@code
   * java/util/HashMap}) or an array type descriptor (e.g. {@code [Ljava/lang/Object;} for {@code
   * ANEWARRAY}/an array-typed method owner) - ASM does not normalize this for us.
   */
  private static void addPossiblyArrayInternalName(Set<String> refs, String nameOrDescriptor) {
    if (nameOrDescriptor.startsWith("[")) {
      addDescriptor(refs, nameOrDescriptor);
    } else {
      addInternalName(refs, nameOrDescriptor);
    }
  }
}
