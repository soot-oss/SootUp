package de.upb.swt.soot.java.sourcecode.frontend;

import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import de.upb.swt.soot.core.frontend.MethodSource;
import de.upb.swt.soot.core.frontend.OverridingMethodSource;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaSootMethod;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WalaSootMethod extends JavaSootMethod {

  @Nullable private final DebuggingInformation debugInfo;

  /**
   * Constructs a SootMethod object with the given attributes.
   *
   * @param source
   * @param methodSignature
   * @param modifiers
   * @param thrownExceptions
   */
  public WalaSootMethod(
      @Nonnull MethodSource source,
      @Nonnull MethodSignature methodSignature,
      @Nonnull Iterable<Modifier> modifiers,
      @Nonnull Iterable<ClassType> thrownExceptions,
      DebuggingInformation debugInfo) {
    super(source, methodSignature, modifiers, thrownExceptions);
    this.debugInfo = debugInfo;
  }

  public int getJavaSourceStartLineNumber() {
    return debugInfo.getCodeBodyPosition().getFirstLine();
  }

  @Nullable
  public DebuggingInformation getDebugInfo() {
    return this.debugInfo;
  }

  @Nonnull
  public SootMethod withOverridingMethodSource(
      Function<OverridingMethodSource, OverridingMethodSource> overrider) {
    return new WalaSootMethod(
        overrider.apply(new OverridingMethodSource(methodSource)),
        getSignature(),
        getModifiers(),
        exceptions,
        debugInfo);
  }

  // TODO: check if withers are used by javasourcecodefrontend
  @Nonnull
  public SootMethod withSource(MethodSource source) {
    return new WalaSootMethod(source, getSignature(), getModifiers(), exceptions, debugInfo);
  }

  @Nonnull
  public SootMethod withModifiers(Iterable<Modifier> modifiers) {
    return new WalaSootMethod(methodSource, getSignature(), getModifiers(), exceptions, debugInfo);
  }

  @Nonnull
  public SootMethod withThrownExceptions(Iterable<ClassType> thrownExceptions) {
    return new WalaSootMethod(
        methodSource, getSignature(), getModifiers(), thrownExceptions, debugInfo);
  }

  @Nonnull
  public SootMethod withDebugInfo(DebuggingInformation debugInfo) {
    return new WalaSootMethod(methodSource, getSignature(), getModifiers(), exceptions, debugInfo);
  }
}
