package sootup.java.bytecode.interceptors.typeresolving.types;

import javax.annotation.Nonnull;
import sootup.core.jimple.visitor.TypeVisitor;
import sootup.core.types.Type;
import sootup.java.bytecode.interceptors.LocalSplitter;
import sootup.java.bytecode.interceptors.TypeAssigner;

/**
 * The top type is a superclass of all other types. This is similar to {@code java.lang.Object} but
 * also includes primitive types. <br>
 * This type can't exist in Java source code, but it can implicitly exist in bytecode. This happens
 * when the compiler re-uses local variables with the same id, but different types.<br>
 * If you see this type when you didn't expect it, you probably need to <b>turn on the {@link
 * LocalSplitter}</b>. The {@link LocalSplitter} will remove all situations where a {@code TopType}
 * could be created by the {@link TypeAssigner} (at least when the bytecode has been generated from
 * Java source code).
 */
public class TopType extends Type {
  @Nonnull private static final TopType INSTANCE = new TopType();

  @Nonnull
  public static TopType getInstance() {
    return INSTANCE;
  }

  private TopType() {}

  @Override
  public void accept(@Nonnull TypeVisitor typeVisitor) {
    typeVisitor.defaultCaseType();
  }

  @Override
  public String toString() {
    return "TopType";
  }
}
