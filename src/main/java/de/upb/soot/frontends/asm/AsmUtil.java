package de.upb.soot.frontends.asm;

import de.upb.soot.core.Modifier;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;
import de.upb.soot.types.VoidType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public final class AsmUtil {

  private AsmUtil() {}

  public static final int SUPPORTED_ASM_OPCODE = Opcodes.ASM7;

  /**
   * Initializes a class node.
   *
   * @param classSource The source.
   * @param classNode The node to initialize
   */
  public static void initAsmClassSource(
      @Nonnull ClassSource classSource, @Nonnull ClassNode classNode) {
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
   * @param sourceFile The source file.
   * @param classNode The class node.
   * @throws IOException An error occurred.
   */
  private static void initClassNode(@Nonnull Path sourceFile, @Nonnull ClassNode classNode)
      throws IOException {
    try (InputStream sourceFileInputStream = Files.newInputStream(sourceFile)) {
      ClassReader clsr = new ClassReader(sourceFileInputStream);

      clsr.accept(classNode, ClassReader.SKIP_FRAMES);
    }
  }

  /**
   * Determines if a type is a dword type.
   *
   * @param type the type to check.
   * @return {@code true} if its a dword type.
   */
  public static boolean isDWord(@Nonnull Type type) {
    return type == PrimitiveType.getLong() || type == PrimitiveType.getDouble();
  }

  /**
   * Converts an internal class name to a fully qualified name.
   *
   * @param internal internal name.
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

  @Nonnull
  public static Type toJimpleType(@Nonnull String desc) {
    int idx = desc.lastIndexOf('[');
    int nrDims = idx + 1;
    if (nrDims > 0) {
      if (desc.charAt(0) != '[') {
        throw new AssertionError("Invalid array descriptor: " + desc);
      }
      desc = desc.substring(idx + 1);
    }
    Type baseType;
    switch (desc.charAt(0)) {
      case 'Z':
        baseType = PrimitiveType.getBoolean();
        break;
      case 'B':
        baseType = PrimitiveType.getByte();
        break;
      case 'C':
        baseType = PrimitiveType.getChar();
        break;
      case 'S':
        baseType = PrimitiveType.getShort();
        break;
      case 'I':
        baseType = PrimitiveType.getInt();
        break;
      case 'F':
        baseType = PrimitiveType.getFloat();
        break;
      case 'J':
        baseType = PrimitiveType.getLong();
        break;
      case 'D':
        baseType = PrimitiveType.getDouble();
        break;
      case 'V':
        baseType = VoidType.getInstance();
        break;
      case 'L':
        if (desc.charAt(desc.length() - 1) != ';') {
          throw new AssertionError("Invalid reference descriptor: " + desc);
        }
        String name = desc.substring(1, desc.length() - 1);
        name = toQualifiedName(name);
        baseType = DefaultTypeFactory.getInstance().getType(toQualifiedName(name));
        break;
      default:
        throw new AssertionError("Unknown descriptor: " + desc);
    }
    if (!(baseType instanceof JavaClassType) && desc.length() > 1) {
      throw new AssertionError("Invalid primitive type descriptor: " + desc);
    }
    return nrDims > 0 ? DefaultTypeFactory.getInstance().getArrayType(baseType, nrDims) : baseType;
  }

  @Nonnull
  public static List<Type> toJimpleSignatureDesc(@Nonnull String desc) {
    List<Type> types = new ArrayList<>(2);
    int len = desc.length();
    int idx = 0;
    all:
    while (idx != len) {
      int nrDims = 0;
      Type baseType = null;
      this_type:
      while (idx != len) {
        char c = desc.charAt(idx++);
        switch (c) {
          case '(':
          case ')':
            continue all;
          case '[':
            ++nrDims;
            continue this_type;
          case 'Z':
            baseType = PrimitiveType.getBoolean();
            break this_type;
          case 'B':
            baseType = PrimitiveType.getByte();
            break this_type;
          case 'C':
            baseType = PrimitiveType.getChar();
            break this_type;
          case 'S':
            baseType = PrimitiveType.getShort();
            break this_type;
          case 'I':
            baseType = PrimitiveType.getInt();
            break this_type;
          case 'F':
            baseType = PrimitiveType.getFloat();
            break this_type;
          case 'J':
            baseType = PrimitiveType.getLong();
            break this_type;
          case 'D':
            baseType = PrimitiveType.getDouble();
            break this_type;
          case 'V':
            baseType = VoidType.getInstance();
            break this_type;
          case 'L':
            int begin = idx;

            // noinspection StatementWithEmptyBody
            while (desc.charAt(++idx) != ';') {
              // Empty while body: Just find the index of the semicolon.
            }

            String cls = desc.substring(begin, idx++);
            baseType = DefaultTypeFactory.getInstance().getType(toQualifiedName(cls));
            break this_type;
          default:
            throw new AssertionError("Unknown type: " + c);
        }
      }

      if (baseType != null && nrDims > 0) {
        types.add(DefaultTypeFactory.getInstance().getArrayType(baseType, nrDims));

      } else {
        types.add(baseType);
      }
    }
    return types;
  }

  @Nonnull
  public static Collection<JavaClassType> asmIdToSignature(@Nullable Iterable<String> modules) {
    if (modules == null) {
      return Collections.emptyList();
    }

    return StreamSupport.stream(modules.spliterator(), false)
        .map(p -> (DefaultTypeFactory.getInstance().getClassType(toQualifiedName(p))))
        .collect(Collectors.toList());
  }
}
