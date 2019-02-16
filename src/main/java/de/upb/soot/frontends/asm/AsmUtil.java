package de.upb.soot.frontends.asm;

import de.upb.soot.core.Modifier;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.PrimitiveTypeSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.signatures.VoidTypeSignature;
import de.upb.soot.views.IView;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class AsmUtil {

  private AsmUtil() {
  }

  public static final int SUPPORTED_ASM_OPCODE = Opcodes.ASM7;

  /**
   * Initializes a class node.
   * 
   * @param classSource
   *          The source.
   * @param classNode
   *          The node to initialize
   */
  public static void initAsmClassSource(@Nonnull ClassSource classSource, @Nonnull ClassNode classNode) {
    URI uri = classSource.getSourcePath().toUri();

    try {
      if (classSource.getSourcePath().getFileSystem().isOpen()) {
        Path sourceFile = java.nio.file.Paths.get(uri);

        initClassNode(sourceFile, classNode);
      } else {
        // A zip file system needs to be re-opened, otherwise it crashes
        // http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html

        Map<String, String> env = new HashMap<>();
        env.put("create", "false");

        // Info: The `__zipfs` variable is intentionally unused. It is required
        // to create the ZIP file system, but the file system instance itself
        // has not explicitly to be used – this happens in the background.
        try (FileSystem __zipfs = FileSystems.newFileSystem(uri, env)) {
          Path sourceFile = Paths.get(uri);

          initClassNode(sourceFile, classNode);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
      // TODO: Exception handling
    }
  }

  /**
   * Initializes the specified class node from a class file.
   * 
   * @param sourceFile
   *          The source file.
   * @param classNode
   *          The class node.
   * @throws IOException
   *           An error occurred.
   */
  private static void initClassNode(@Nonnull Path sourceFile, @Nonnull ClassNode classNode) throws IOException {
    try (InputStream sourceFileInputStream = Files.newInputStream(sourceFile)) {
      ClassReader clsr = new ClassReader(sourceFileInputStream);

      clsr.accept(classNode, ClassReader.SKIP_FRAMES);
    }
  }

  /**
   * Determines if a type is a dword type.
   *
   * @param type
   *          the type to check.
   * @return {@code true} if its a dword type.
   */
  public static boolean isDWord(@Nonnull TypeSignature type) {
    return type == PrimitiveTypeSignature.LONG_TYPE_SIGNATURE || type == PrimitiveTypeSignature.DOUBLE_TYPE_SIGNATURE;
  }

  /**
   * Converts an internal class name to a fully qualified name.
   *
   * @param internal
   *          internal name.
   * @return fully qualified name.
   */
  public static String toQualifiedName(@Nonnull String internal) {
    return internal.replace('/', '.');
  }

  public static EnumSet<Modifier> getModifiers(int access) {
    EnumSet<Modifier> modifierEnumSet = EnumSet.noneOf(Modifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (Modifier modifier : Modifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }

  public static @Nonnull TypeSignature toJimpleType(@Nonnull IView view, @Nonnull String desc) {
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

  public static @Nonnull List<TypeSignature> toJimpleSignatureDesc(@Nonnull String desc, @Nonnull IView view) {
    List<TypeSignature> types = new ArrayList<>(2);
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

            // noinspection StatementWithEmptyBody
            while (desc.charAt(++idx) != ';') {
              // Empty while body: Just find the index of the semicolon.
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

  public static @Nonnull Iterable<JavaClassSignature> asmIdToSignature(@Nullable Iterable<String> modules,
      @Nonnull IView view) {
    if (modules == null) {
      return Collections.emptyList();
    }

    return StreamSupport.stream(modules.spliterator(), false)
        .map(p -> (view.getSignatureFactory().getClassSignature(toQualifiedName(p)))).collect(Collectors.toList());
  }
}
