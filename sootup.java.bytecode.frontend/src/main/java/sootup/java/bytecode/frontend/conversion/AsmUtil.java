package sootup.java.bytecode.frontend.conversion;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2023 Andreas Dann, Markus Schmidt, Jan Martin Persch, Kadiray Karakaya and others
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.ResolveException;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.model.FieldModifier;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.ConstantUtil;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.ModuleModifier;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

public final class AsmUtil {

  private static final @NonNull Logger logger = LoggerFactory.getLogger(AsmUtil.class);

  private AsmUtil() {}

  public static final int SUPPORTED_ASM_OPCODE = Opcodes.ASM9;

  /**
   * Initializes a class node.
   *
   * @param classSource The source.
   * @param classNode The node to initialize
   * @return the actual class signature found in the compilation unit
   */
  protected static Optional<String> readClassName(
      @NonNull final Path classSource, @NonNull final ClassVisitor classNode) {
    try (InputStream sourceFileInputStream = Files.newInputStream(classSource)) {
      ClassReader classReader = new ClassReader(sourceFileInputStream);
      classReader.accept(classNode, ClassReader.SKIP_FRAMES);
      return Optional.of(classReader.getClassName().replace('/', '.'));
    } catch (IOException exception) {
      logger.warn("Cannot create class source for {}", classSource, exception);
    } catch (IllegalArgumentException exception) {
      logger.warn("Cannot create class source for {}", classSource, exception);
    }
    return Optional.empty();
  }

  public static SootClassSource createClassSource(
      @NonNull final AnalysisInputLocation analysisInputLocation,
      @NonNull final Path sourcePath,
      @NonNull final ClassType classType,
      @NonNull final ClassNode classNode) {
    if ((classNode.access & Opcodes.ACC_ANNOTATION) == Opcodes.ACC_ANNOTATION) {
      return new AsmAnnotationClassSource(analysisInputLocation, sourcePath, classType, classNode);
    }
    return new AsmClassSource(analysisInputLocation, sourcePath, classType, classNode);
  }

  /**
   * Determines if a type is a dword type.
   *
   * @param type the type to check.
   * @return {@code true} if its a dword type.
   */
  public static boolean isDWord(@NonNull Type type) {
    return type == PrimitiveType.getLong() || type == PrimitiveType.getDouble();
  }

  /**
   * Converts an str class name to a fully qualified name.
   *
   * @param str str name.
   * @return fully qualified name.
   */
  public static String toQualifiedName(@NonNull String str) {
    final int endpos = str.length() - 1;
    if (endpos > 2 && str.charAt(endpos) == ';' && str.charAt(0) == 'L') {
      str = str.substring(1, endpos);
    }
    return str.replace('/', '.');
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

  @NonNull
  public static Collection<JavaClassType> asmIdToSignature(
      @Nullable Iterable<String> asmClassNames) {
    if (asmClassNames == null) {
      return Collections.emptyList();
    }

    return StreamSupport.stream(asmClassNames.spliterator(), false)
        .map(AsmUtil::toJimpleClassType)
        .collect(Collectors.toList());
  }

  @NonNull
  public static JavaClassType toJimpleClassType(@NonNull String asmClassName) {
    return JavaIdentifierFactory.getInstance().getClassType(toQualifiedName(asmClassName));
  }

  @NonNull
  public static Type toJimpleType(@NonNull String desc) {
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

  @NonNull
  public static Type arrayTypetoJimpleType(@NonNull String desc) {
    if (desc.charAt(0) == '[') {
      return toJimpleType(desc);
    }
    return toJimpleClassType(desc);
  }

  /** returns the amount of dimensions of a description. */
  private static int countArrayDim(@NonNull String desc) {
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
  private static Optional<Type> toPrimitiveOrVoidType(@NonNull String desc) {
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
  @NonNull
  public static List<Type> toJimpleSignatureDesc(@NonNull String desc) {
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

  public static AnnotationUsage createAnnotationUsage(AnnotationNode annotationNode) {
    /* actually, we could move the inner loop's code (see below) here */
    return createAnnotationUsage(Collections.singletonList(annotationNode)).iterator().next();
  }

  public static Iterable<AnnotationUsage> createAnnotationUsage(
      List<? extends AnnotationNode> invisibleParameterAnnotation) {
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
            paramMap.put(annotationName, createAnnotationUsage((AnnotationNode) annotationValue));
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

      JavaClassType at =
          JavaIdentifierFactory.getInstance().getClassType(AsmUtil.toQualifiedName(e.desc));
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

  @NonNull
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
