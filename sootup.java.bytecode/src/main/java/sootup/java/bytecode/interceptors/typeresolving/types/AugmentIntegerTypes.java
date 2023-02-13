package sootup.java.bytecode.interceptors.typeresolving.types;

import javax.annotation.Nonnull;
import sootup.core.jimple.visitor.TypeVisitor;
import sootup.core.types.PrimitiveType;

public abstract class AugmentIntegerTypes {

  @Nonnull
  public static Integer1Type getInteger1() {
    return Integer1Type.getInstance();
  }

  @Nonnull
  public static Integer127Type getInteger127() {
    return Integer127Type.getInstance();
  }

  @Nonnull
  public static Integer32767Type getInteger32767() {
    return Integer32767Type.getInstance();
  }

  /**
   * This type is intermediate type and used for determining the ancestor of an integer type. see:
   * AugmentHierarchy;
   */
  public static class Integer1Type extends PrimitiveType.IntType {
    // 2^0
    private static final Integer1Type INSTANCE = new Integer1Type();

    private Integer1Type() {
      super("integer1");
    }

    public static Integer1Type getInstance() {
      return INSTANCE;
    }

    @Override
    public void accept(@Nonnull TypeVisitor v) {
      // todo: case for Integer1Type
    }
  }

  /** This type is intermediate type and used for determining the ancestor of an integer type */
  public static class Integer127Type extends PrimitiveType.IntType {
    // 2^8
    private static final Integer127Type INSTANCE = new Integer127Type();

    private Integer127Type() {
      super("integer127");
    }

    public static Integer127Type getInstance() {
      return INSTANCE;
    }

    @Override
    public void accept(@Nonnull TypeVisitor v) {
      // todo: case for Integer127Type
    }
  }

  /** This type is intermediate type and used for determining the ancestor of an integer type */
  public static class Integer32767Type extends PrimitiveType.IntType {
    // 2^16
    private static final Integer32767Type INSTANCE = new Integer32767Type();

    private Integer32767Type() {
      super("integer32767");
    }

    public static Integer32767Type getInstance() {
      return INSTANCE;
    }

    @Override
    public void accept(@Nonnull TypeVisitor v) {
      // todo: case for Integer32767Type
    }
  }
}
