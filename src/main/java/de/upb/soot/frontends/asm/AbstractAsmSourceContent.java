package de.upb.soot.frontends.asm;

import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import de.upb.soot.core.Modifier;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.types.JavaClassType;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

public abstract class AbstractAsmSourceContent extends org.objectweb.asm.tree.ClassNode
    implements IClassSourceContent {

  private final ClassSource classSource;

  public AbstractAsmSourceContent(@Nonnull ClassSource classSource) {
    super(AsmUtil.SUPPORTED_ASM_OPCODE);
    this.classSource = classSource;

    // FIXME: maybe delete class reading
    AsmUtil.initAsmClassSource(classSource, this);
  }

  @Override
  public Set<Modifier> resolveModifiers(JavaClassType type) {
    EnumSet<Modifier> modifiers = AsmUtil.getModifiers(this.access);
    return modifiers;
  }

  @Override
  public Set<JavaClassType> resolveInterfaces(JavaClassType type) {
    return new HashSet<>(AsmUtil.asmIdToSignature(this.interfaces));
  }

  @Override
  public Optional<JavaClassType> resolveSuperclass(JavaClassType type) {
    return Optional.ofNullable(AsmUtil.asmIDToSignature(this.superName));
  }

  @Override
  public Optional<JavaClassType> resolveOuterClass(JavaClassType type) {
    return Optional.ofNullable(AsmUtil.asmIDToSignature(this.outerClass));
  }

  @Override
  public CAstSourcePositionMap.Position resolvePosition(JavaClassType type) {
    // FIXME: what is this??? the source code line number of the complete file?
    return null;
  }
}
