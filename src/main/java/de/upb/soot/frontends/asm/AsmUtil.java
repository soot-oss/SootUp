package de.upb.soot.frontends.asm;

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.jimple.common.type.DoubleType;
import de.upb.soot.jimple.common.type.LongType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.PrimitiveTypeSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.signatures.VoidTypeSignature;
import de.upb.soot.views.IView;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class AsmUtil {

  private AsmUtil() {
  }

  public static void initASMClassSource(ClassSource classSource, ClassNode classNode) {
    java.net.URI uri = classSource.getSourcePath().toUri();

    try {
      if (classSource.getSourcePath().getFileSystem().isOpen()) {
        Path sourceFile = java.nio.file.Paths.get(uri);

        org.objectweb.asm.ClassReader clsr
            = new org.objectweb.asm.ClassReader(java.nio.file.Files.newInputStream(sourceFile));

        clsr.accept(classNode, org.objectweb.asm.ClassReader.SKIP_FRAMES);
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

          clsr.accept(classNode, org.objectweb.asm.ClassReader.SKIP_FRAMES);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Determines if a type is a dword type.
   *
   * @param type
   *          the type to check.
   * @return {@code true} if its a dword type.
   */
  // FIXME: this is the old methodRef using type....
  public static boolean isDWord(Type type) {
    return type instanceof LongType || type instanceof DoubleType;
  }

  public static boolean isDWord(TypeSignature type) {
    return type == PrimitiveTypeSignature.LONG_TYPE_SIGNATURE || type == PrimitiveTypeSignature.DOUBLE_TYPE_SIGNATURE;
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

  // FIXME: migrate woth the other code
  public static TypeSignature toJimpleType(IView view, String desc) {
    int idx = desc.lastIndexOf('[');
    int nrDims = idx + 1;
    if (nrDims > 0) {
      if (desc.charAt(0) != '[') {
        throw new AssertionError("Invalid array descriptor: " + desc);
      }
      desc = desc.substring(idx + 1);
    }
    TypeSignature baseType;
    switch (desc.charAt(0)) {
      case 'Z':
        baseType = PrimitiveTypeSignature.BOOLEAN_TYPE_SIGNATURE;
        break;
      case 'B':
        baseType = PrimitiveTypeSignature.BYTE_TYPE_SIGNATURE;
        break;
      case 'C':
        baseType = PrimitiveTypeSignature.CHAR_TYPE_SIGNATURE;
        break;
      case 'S':
        baseType = PrimitiveTypeSignature.SHORT_TYPE_SIGNATURE;
        break;
      case 'I':
        baseType = PrimitiveTypeSignature.INT_TYPE_SIGNATURE;
        break;
      case 'F':
        baseType = PrimitiveTypeSignature.FLOAT_TYPE_SIGNATURE;
        break;
      case 'J':
        baseType = PrimitiveTypeSignature.LONG_TYPE_SIGNATURE;
        break;
      case 'D':
        baseType = PrimitiveTypeSignature.DOUBLE_TYPE_SIGNATURE;
        break;
      case 'V':
        baseType = VoidTypeSignature.VOID_TYPE_SIGNATURE;
        break;
      case 'L':
        if (desc.charAt(desc.length() - 1) != ';') {
          throw new AssertionError("Invalid reference descriptor: " + desc);
        }
        String name = desc.substring(1, desc.length() - 1);
        name = toQualifiedName(name);
        baseType = view.getSignatureFactory().getTypeSignature(toQualifiedName(name));
        break;
      default:
        throw new AssertionError("Unknown descriptor: " + desc);
    }
    if (!(baseType instanceof JavaClassSignature) && desc.length() > 1) {
      throw new AssertionError("Invalid primitive type descriptor: " + desc);
    }
    return nrDims > 0 ? view.getSignatureFactory().getArrayTypeSignature(baseType, nrDims) : baseType;
  }

  public static List<TypeSignature> toJimpleSignatureDesc(String desc, IView view) {
    ArrayList<TypeSignature> types = new ArrayList<>(2);
    int len = desc.length();
    int idx = 0;
    all: while (idx != len) {
      int nrDims = 0;
      TypeSignature baseType = null;
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
            baseType = PrimitiveTypeSignature.BOOLEAN_TYPE_SIGNATURE;
            break this_type;
          case 'B':
            baseType = PrimitiveTypeSignature.BYTE_TYPE_SIGNATURE;
            break this_type;
          case 'C':
            baseType = PrimitiveTypeSignature.CHAR_TYPE_SIGNATURE;
            break this_type;
          case 'S':
            baseType = PrimitiveTypeSignature.SHORT_TYPE_SIGNATURE;
            break this_type;
          case 'I':
            baseType = PrimitiveTypeSignature.INT_TYPE_SIGNATURE;
            break this_type;
          case 'F':
            baseType = PrimitiveTypeSignature.FLOAT_TYPE_SIGNATURE;
            break this_type;
          case 'J':
            baseType = PrimitiveTypeSignature.LONG_TYPE_SIGNATURE;
            break this_type;
          case 'D':
            baseType = PrimitiveTypeSignature.DOUBLE_TYPE_SIGNATURE;
            break this_type;
          case 'V':
            baseType = VoidTypeSignature.VOID_TYPE_SIGNATURE;
            break this_type;
          case 'L':
            int begin = idx;
            while (desc.charAt(++idx) != ';') {
              ;
            }
            String cls = desc.substring(begin, idx++);
            baseType = view.getSignatureFactory().getTypeSignature(toQualifiedName(cls));
            break this_type;
          default:
            throw new AssertionError("Unknown type: " + c);
        }
      }

      if (baseType != null && nrDims > 0) {
        types.add(view.getSignatureFactory().getArrayTypeSignature(baseType, nrDims));

      } else {
        types.add(baseType);
      }
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
