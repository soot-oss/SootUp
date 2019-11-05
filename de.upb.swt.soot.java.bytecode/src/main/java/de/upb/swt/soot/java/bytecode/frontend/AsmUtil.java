package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public final class AsmUtil {

  private AsmUtil() {}

  public static final int SUPPORTED_ASM_OPCODE = Opcodes.ASM7;

  /**
   * Initializes a class node.
   *
   * @param classSource The source.
   * @param classNode The node to initialize
   */
  protected static void initAsmClassSource(
      @Nonnull Path classSource, @Nonnull AsmJavaClassProvider.SootClassNode classNode)
      throws AsmFrontendException {
    try {
      try (InputStream sourceFileInputStream = Files.newInputStream(classSource)) {
        ClassReader clsr = new ClassReader(sourceFileInputStream);

        clsr.accept(classNode, ClassReader.SKIP_FRAMES);
      }
    } catch (IOException e) {
      throw new AsmFrontendException(e.getMessage());
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
        baseType = JavaIdentifierFactory.getInstance().getType(toQualifiedName(name));
        break;
      default:
        throw new AssertionError("Unknown descriptor: " + desc);
    }
    if (!(baseType instanceof JavaClassType) && desc.length() > 1) {
      throw new AssertionError("Invalid primitive type descriptor: " + desc);
    }
    return nrDims > 0
        ? JavaIdentifierFactory.getInstance().getArrayType(baseType, nrDims)
        : baseType;
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
            baseType = JavaIdentifierFactory.getInstance().getType(toQualifiedName(cls));
            break this_type;
          default:
            throw new AssertionError("Unknown type: " + c);
        }
      }

      if (baseType != null && nrDims > 0) {
        types.add(JavaIdentifierFactory.getInstance().getArrayType(baseType, nrDims));

      } else {
        types.add(baseType);
      }
    }
    return types;
  }

  @Nonnull
  public static Collection<JavaClassType> asmIdToSignature(
      @Nullable Iterable<String> asmClassNames) {
    if (asmClassNames == null) {
      return Collections.emptyList();
    }

    return StreamSupport.stream(asmClassNames.spliterator(), false)
        .map(p -> asmIDToSignature(p))
        .collect(Collectors.toList());
  }

  @Nullable
  public static JavaClassType asmIDToSignature(@Nonnull String asmClassName) {
    if (asmClassName.isEmpty()) {
      return null;
    }
    return JavaIdentifierFactory.getInstance().getClassType(toQualifiedName(asmClassName));
  }
}
