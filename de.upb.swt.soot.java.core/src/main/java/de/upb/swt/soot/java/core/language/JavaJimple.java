package de.upb.swt.soot.java.core.language;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.constant.MethodHandle;
import de.upb.swt.soot.core.jimple.common.constant.MethodType;
import de.upb.swt.soot.core.jimple.common.constant.StringConstant;
import de.upb.swt.soot.core.jimple.common.ref.FieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import java.util.List;

/**
 * JavaJimple implements the Java specific terms for {@link Jimple}
 *
 * @author Markus Schmidt
 */
public class JavaJimple extends Jimple {

  private static final JavaJimple INSTANCE = new JavaJimple();

  public static JavaJimple getInstance() {
    return INSTANCE;
  }

  @Override
  public IdentifierFactory getIdentifierFactory() {
    return JavaIdentifierFactory.getInstance();
  }

  public static boolean isJavaKeywordType(Type t) {
    // TODO: [JMP] Ensure that the check is complete.
    return t instanceof PrimitiveType || t instanceof VoidType || t instanceof NullType;
  }

  public JCaughtExceptionRef newCaughtExceptionRef() {
    return new JCaughtExceptionRef(getIdentifierFactory().getType("java.lang.Throwable"));
  }

  public ClassConstant newClassConstant(String value) {
    return new ClassConstant(value, getIdentifierFactory().getType("java.lang.Class"));
  }

  public StringConstant newStringConstant(String value) {
    return new StringConstant(value, getIdentifierFactory().getType("java.lang.String"));
  }

  public MethodHandle newMethodHandle(FieldRef ref, int tag) {
    return new MethodHandle(
        ref, tag, getIdentifierFactory().getType("java.lang.invoke.MethodHandle"));
  }

  public MethodHandle newMethodHandle(MethodSignature ref, int tag) {
    return new MethodHandle(
        ref, tag, getIdentifierFactory().getType("java.lang.invoke.MethodHandle"));
  }

  public MethodType newMethodType(List<Type> parameterTypes, Type returnType) {
    return new MethodType(
        parameterTypes,
        returnType,
        getIdentifierFactory().getClassType("java.lang.invoke.MethodType"));
  }
}
