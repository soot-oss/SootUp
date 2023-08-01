package sootup.java.sourcecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Markus Schmidt
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
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import java.util.Collections;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootMethod;

// TODO: [ms] is it possible to get rid of this class? necessity to hold DebuggingInformation
// (getDebugInfo is not called)
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
      @Nonnull BodySource source,
      @Nonnull MethodSignature methodSignature,
      @Nonnull Iterable<MethodModifier> modifiers,
      @Nonnull Iterable<ClassType> thrownExceptions,
      DebuggingInformation debugInfo) {

    super(
        source,
        methodSignature,
        modifiers,
        thrownExceptions,
        Collections.emptyList(), // TODO: [ms] implement annotations
        NoPositionInformation.getInstance() // TODO: fixme
        );
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
  public JavaSootMethod withOverridingMethodSource(
      Function<OverridingBodySource, OverridingBodySource> overrider) {
    return new WalaSootMethod(
        overrider.apply(new OverridingBodySource(bodySource)),
        getSignature(),
        getModifiers(),
        exceptions,
        debugInfo);
  }

  @Nonnull
  public JavaSootMethod withSource(BodySource source) {
    return new WalaSootMethod(source, getSignature(), getModifiers(), exceptions, debugInfo);
  }

  @Nonnull
  public JavaSootMethod withModifiers(Iterable<MethodModifier> modifiers) {
    return new WalaSootMethod(bodySource, getSignature(), getModifiers(), exceptions, debugInfo);
  }

  @Nonnull
  public JavaSootMethod withThrownExceptions(Iterable<ClassType> thrownExceptions) {
    return new WalaSootMethod(
        bodySource, getSignature(), getModifiers(), thrownExceptions, debugInfo);
  }

  @Nonnull
  public SootMethod withDebugInfo(DebuggingInformation debugInfo) {
    return new WalaSootMethod(bodySource, getSignature(), getModifiers(), exceptions, debugInfo);
  }
}
