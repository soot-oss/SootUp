package sootup.java.bytecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2023 Andreas Dann, Markus Schmidt, Jan Martin Persch and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.MethodModifier;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.ConstantUtil;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.ModuleModifier;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.AnnotationType;
import sootup.java.core.types.JavaClassType;

public final class AsmUtil {

  private AsmUtil() {}

  public static final int SUPPORTED_ASM_OPCODE = Opcodes.ASM9;

  /**
   * Initializes a class node.
   *
   * @param classSource The source.
   * @param classNode The node to initialize
   */
  protected static void initAsmClassSource(
      @Nonnull Path classSource, @Nonnull ClassVisitor classNode) throws IOException {
    try (InputStream sourceFileInputStream = Files.newInputStream(classSource)) {
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
   * Converts an str class name to a fully qualified name.
   *
   * @param str str name.
   * @return fully qualified name.
   */
  public static String toQualifiedName(@Nonnull String str) {
    final int endpos = str.length() - 1;
    if (endpos > 2 && str.charAt(endpos) == ';' && str.charAt(0) == 'L') {
      str = str.substring(1, endpos);
    }
    return str.replace('/', '.');
  }

  public static EnumSet<ClassModifier> getClassModifiers(int access) {
    EnumSet<ClassModifier> modifierEnumSet = EnumSet.noneOf(ClassModifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (ClassModifier modifier : ClassModifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }

  public static EnumSet<MethodModifier> getMethodModifiers(int access) {
    EnumSet<MethodModifier> modifierEnumSet = EnumSet.noneOf(MethodModifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (MethodModifier modifier : MethodModifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }

  public static EnumSet<FieldModifier> getFieldModifiers(int access) {
    EnumSet<FieldModifier> modifierEnumSet = EnumSet.noneOf(FieldModifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (FieldModifier modifier : FieldModifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }

  public static EnumSet<ModuleModifier> getModuleModifiers(int access) {
    EnumSet<ModuleModifier> modifierEnumSet = EnumSet.noneOf(ModuleModifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (ModuleModifier modifier : ModuleModifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }

  @Nonnull
  public static Collection<JavaClassType> asmIdToSignature(
      @Nullable Iterable<String> asmClassNames) {
    if (asmClassNames == null) {
      return Collections.emptyList();
    }

    return StreamSupport.stream(asmClassNames.spliterator(), false)
        .map(AsmUtil::toJimpleClassType)
        .collect(Collectors.toList());
  }

  @Nonnull
  public static JavaClassType toJimpleClassType(@Nonnull String asmClassName) {
    return JavaIdentifierFactory.getInstance().getClassType(toQualifiedName(asmClassName));
  }

  /**
   * Converts a type descriptor to a Jimple reference type.
   *
   * @param desc the descriptor.
   * @return the reference type.
   */
  public static Type toJimpleSignature(@Nonnull String desc) {
    return desc.charAt(0) == '['
        ? toJimpleType(desc)
        : JavaIdentifierFactory.getInstance().getClassType(toQualifiedName(desc));
  }

  @Nonnull
  public static Type toJimpleType(@Nonnull String desc) {
    int nrDims = countArrayDim(desc);
    if (nrDims > 0) {
      desc = desc.substring(nrDims);
    }

    Type baseType = toPrimitiveOrVoidType(desc).orElse(null);
    if (baseType == null) {
      if (desc.charAt(0) != 'L') {
        throw new AssertionError("Unknown descriptor: " + desc);
      }
      if (desc.charAt(desc.length() - 1) != ';') {
        throw new AssertionError("Invalid reference descriptor: " + desc);
      }
      String name = desc.substring(1, desc.length() - 1);
      baseType = JavaIdentifierFactory.getInstance().getType(toQualifiedName(name));
    }
    if ((baseType instanceof PrimitiveType || baseType instanceof VoidType) && desc.length() > 1) {
      throw new AssertionError("Invalid primitive type descriptor: " + desc);
    }
    return nrDims > 0
        ? JavaIdentifierFactory.getInstance().getArrayType(baseType, nrDims)
        : baseType;
  }

  @Nonnull
  public static Type arrayTypetoJimpleType(@Nonnull String desc) {
    if (desc.startsWith("[")) {
      return toJimpleType(desc);
    }
    return toJimpleClassType(desc);
  }

  /** returns the amount of dimensions of a description. */
  private static int countArrayDim(@Nonnull String desc) {
    int nrDims = desc.lastIndexOf('[') + 1;
    for (int index = 0; index < nrDims; index++) {
      if (desc.charAt(index) != '[') {
        throw new AssertionError("Invalid array descriptor: " + desc);
      }
    }
    return nrDims;
  }

  /**
   * Converts a description to a primitive type or the void type. If the description does not match
   * it will return an empty Optional
   */
  private static Optional<Type> toPrimitiveOrVoidType(@Nonnull String desc) {
    if (desc.length() > 1) {
      return Optional.empty();
    }
    switch (desc.charAt(0)) {
      case 'Z':
        return Optional.of(PrimitiveType.getBoolean());
      case 'B':
        return Optional.of(PrimitiveType.getByte());
      case 'C':
        return Optional.of(PrimitiveType.getChar());
      case 'S':
        return Optional.of(PrimitiveType.getShort());
      case 'I':
        return Optional.of(PrimitiveType.getInt());
      case 'F':
        return Optional.of(PrimitiveType.getFloat());
      case 'J':
        return Optional.of(PrimitiveType.getLong());
      case 'D':
        return Optional.of(PrimitiveType.getDouble());
      case 'V':
        return Optional.of(VoidType.getInstance());
      default:
    }
    return Optional.empty();
  }

  /** Converts n types contained in desc to a list of Jimple Types */
  @Nonnull
  public static List<Type> toJimpleSignatureDesc(@Nonnull String desc) {
    // [ms] more types are possibly needed for method type which is ( arg-type* ) ret-type
    List<Type> types = new ArrayList<>(1);
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
            idx = desc.indexOf(';', begin);
            String cls = desc.substring(begin, idx++);
            baseType = JavaIdentifierFactory.getInstance().getType(toQualifiedName(cls));
            break this_type;
          default:
            throw new AssertionError("Unknown type: '" + c + "' in '" + desc + "'.");
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

  public static String toString(AbstractInsnNode insn) {
    Printer printer = new Textifier();
    TraceMethodVisitor mp = new TraceMethodVisitor(printer);

    insn.accept(mp);
    StringWriter sw = new StringWriter();
    printer.print(new PrintWriter(sw));
    return Arrays.stream(sw.toString().split("\n"))
        .filter(line -> !line.trim().isEmpty())
        .reduce("", String::concat);
  }

  public static Iterable<AnnotationUsage> createAnnotationUsage(
      List<AnnotationNode> invisibleParameterAnnotation) {
    if (invisibleParameterAnnotation == null) {
      return Collections.emptyList();
    }

    List<AnnotationUsage> annotationUsages = new ArrayList<>();
    for (AnnotationNode e : invisibleParameterAnnotation) {

      Map<String, Object> paramMap = new HashMap<>();

      if (e.values != null) {
        for (int j = 0; j < e.values.size(); j++) {
          final String annotationName = (String) e.values.get(j);
          final Object annotationValue = e.values.get(++j);

          // repeatable annotations will have annotations (as a ArrayList) as value!
          if (annotationValue instanceof ArrayList
              && !((ArrayList<?>) annotationValue).isEmpty()
              && ((ArrayList<?>) annotationValue).get(0) instanceof AnnotationNode) {
            final ArrayList<AnnotationNode> annotationValueList =
                (ArrayList<AnnotationNode>) annotationValue;

            paramMap.put(annotationName, createAnnotationUsage(annotationValueList));
          } else if (annotationValue instanceof AnnotationNode) {
            paramMap.put(
                annotationName,
                createAnnotationUsage(Collections.singletonList((AnnotationNode) annotationValue)));
          } else {
            if (annotationValue instanceof ArrayList) {
              paramMap.put(
                  annotationName,
                  ((ArrayList<?>) annotationValue)
                      .stream().map(AsmUtil::convertAnnotationValue).collect(Collectors.toList()));
            } else {
              paramMap.put(annotationName, convertAnnotationValue(annotationValue));
            }
          }
        }
      }

      AnnotationType at =
          JavaIdentifierFactory.getInstance().getAnnotationType(AsmUtil.toQualifiedName(e.desc));
      annotationUsages.add(new AnnotationUsage(at, paramMap));
    }

    return annotationUsages;
  }

  public static Object convertAnnotationValue(Object annotationValue) {
    if (annotationValue instanceof String[]) {
      // is an enum
      // [0] is the type of the enum
      // [1] is the value of the enum
      // transform the enum type to a fully qualified name
      String[] enumData = (String[]) annotationValue;
      enumData[0] = AsmUtil.toQualifiedName(enumData[0]);
      return ConstantUtil.fromObject(enumData);
    } else {
      if (annotationValue instanceof org.objectweb.asm.Type) {
        // is a class constant
        // transform asm Type to ClassConstant
        ClassConstant classConstant =
            JavaJimple.getInstance()
                .newClassConstant(((org.objectweb.asm.Type) annotationValue).toString());
        return ConstantUtil.fromObject(classConstant);
      }
    }
    return ConstantUtil.fromObject(annotationValue);
  }

  @Nonnull
  public static ClassNode getModuleDescriptor(Path moduleInfoFile) {
    ClassNode moduleDescriptor;
    try (InputStream sourceFileInputStream = Files.newInputStream(moduleInfoFile)) {
      ClassReader clsr = new ClassReader(sourceFileInputStream);
      moduleDescriptor = new ClassNode(AsmUtil.SUPPORTED_ASM_OPCODE);
      clsr.accept(moduleDescriptor, ClassReader.SKIP_FRAMES);
    } catch (IOException e) {
      throw new ResolveException("Error loading the module-descriptor", moduleInfoFile, e);
    }
    return moduleDescriptor;
  }
}
