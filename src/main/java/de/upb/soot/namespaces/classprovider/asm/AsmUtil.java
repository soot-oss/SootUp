package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.ByteType;
import de.upb.soot.jimple.common.type.CharType;
import de.upb.soot.jimple.common.type.DoubleType;
import de.upb.soot.jimple.common.type.FloatType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
import de.upb.soot.jimple.common.type.ShortType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.common.type.VoidType;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.views.IView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public final class AsmUtil {

  private AsmUtil() {
  }

  public static void initASMClassSource(AbstractClassSource classSource, ClassNode classNode) {
    java.net.URI uri = classSource.getSourcePath().toUri();

    try {
      if (classSource.getSourcePath().getFileSystem().isOpen()) {
        Path sourceFile = java.nio.file.Paths.get(uri);

        org.objectweb.asm.ClassReader clsr
            = new org.objectweb.asm.ClassReader(java.nio.file.Files.newInputStream(sourceFile));

        clsr.accept((ClassVisitor) classNode, org.objectweb.asm.ClassReader.SKIP_FRAMES);
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

          clsr.accept((ClassVisitor) classNode, org.objectweb.asm.ClassReader.SKIP_FRAMES);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Converts an internal class name to a fully qualified name.
   *
   * @param internal
   *          internal name.
   * @return fully qualified name.
   */
  public static String toQualifiedName(String internal) {
    return internal.replace('/', '.');
  }

  public static java.util.EnumSet<de.upb.soot.core.Modifier> getModifiers(int access) {
    java.util.EnumSet<de.upb.soot.core.Modifier> modifierEnumSet = java.util.EnumSet.noneOf(de.upb.soot.core.Modifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (de.upb.soot.core.Modifier modifier : de.upb.soot.core.Modifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }

  // FIXME: this is ugly...
  public static Type getType(String typeName, IView view) {
    // FIXME:
    return null;
  }

  // FIXME: migrated from old soot
  /**
   * Converts a method signature to a list of types, with the last entry in the returned list denoting the return type.
   *
   * @param desc
   *          method signature.
   * @return list of types.
   */
  public static List<Type> toJimpleDesc(String desc, IView view) {
    ArrayList<Type> types = new ArrayList<>(2);
    int len = desc.length();
    int idx = 0;
    all: while (idx != len) {
      int nrDims = 0;
      Type baseType = null;
      this_type: while (idx != len) {
        char c = desc.charAt(idx++);
        switch (c) {
          case '(':
          case ')':
            continue all;
          case '[':
            ++nrDims;
            continue this_type;
          case 'Z':
            baseType = BooleanType.getInstance();
            break this_type;
          case 'B':
            baseType = ByteType.getInstance();
            break this_type;
          case 'C':
            baseType = CharType.getInstance();
            break this_type;
          case 'S':
            baseType = ShortType.getInstance();
            break this_type;
          case 'I':
            baseType = IntType.getInstance();
            break this_type;
          case 'F':
            baseType = FloatType.getInstance();
            break this_type;
          case 'J':
            baseType = LongType.getInstance();
            break this_type;
          case 'D':
            baseType = DoubleType.getInstance();
            break this_type;
          case 'V':
            baseType = VoidType.getInstance();
            break this_type;
          case 'L':
            int begin = idx;
            while (desc.charAt(++idx) != ';') {
              ;
            }
            String cls = desc.substring(begin, idx++);
            baseType = view.getRefType(view.getSignatureFactory().getTypeSignature((AsmUtil.toQualifiedName(cls))));
            break this_type;
          default:
            throw new AssertionError("Unknown type: " + c);
        }
      }
      /*
       * if (baseType != null && nrDims > 0) { types.add(ArrayType.v(baseType, nrDims)); } else { types.add(baseType); }
       */
    }
    return types;
  }

  public static Iterable<Optional<JavaClassSignature>> asmIDToSignature(Iterable<String> modules, IView view) {
    if (modules == null) {
      return java.util.Collections.emptyList();
    }
    return StreamSupport.stream(modules.spliterator(), false).map(p -> resolveAsmNameToClassSignature(p, view))
        .collect(java.util.stream.Collectors.toList());
  }

  // FIXME: double check optional here
  public static Optional<JavaClassSignature> resolveAsmNameToClassSignature(String asmClassName, IView view) {
    String excepetionFQName = toQualifiedName(asmClassName);
    JavaClassSignature classSignature = view.getSignatureFactory().getClassSignature(excepetionFQName);
    return Optional.ofNullable(classSignature);
  }
}
