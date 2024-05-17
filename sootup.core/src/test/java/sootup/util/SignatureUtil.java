package sootup.util;

import java.util.Collections;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.PrimitiveType.IntType;

public class SignatureUtil {

  /**creates a dummy method signature
   * Class Type: dummy class type
   * SubSignature: dummy method sub signature
   *
   * @return a dummy method signature
   */
  public static MethodSignature createDummyMethodSignature(){
    return new MethodSignature( ClassTypeUtil.createDummyClassType(), createDummyMethodSubSignature());
  }

  /**creates a dummy method sub signature
   * name: test
   * return type: int
   * parameter list: empty
   *
   * @return a dummy method sub signature
   */
  public static MethodSubSignature createDummyMethodSubSignature(){
    return new MethodSubSignature("test", Collections.emptyList(), IntType.getInstance());
  }

  /**creates a dummy field signature
   * Class Type: dummy class type
   * SubSignature: dummy field sub signature
   *
   * @return a dummy field signature
   */
  public static FieldSignature createDummyFieldSignature(){
    return new FieldSignature( ClassTypeUtil.createDummyClassType(), createDummyFieldSubSignature());
  }

  /**creates a dummy field sub signature
   * name: test
   * type: int
   *
   * @return a dummy field sub signature
   */
  public static FieldSubSignature createDummyFieldSubSignature(){
    return new FieldSubSignature("test", IntType.getInstance());
  }


}
