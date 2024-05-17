package sootup.util;

import sootup.core.jimple.basic.Local;
import sootup.core.types.PrimitiveType.IntType;

public class LocalUtil {

  /**creates a dummy Local for an Object
   * Name a
   * Type dummy class type
   *
   * @return a dummy Local for an Object
   */
  public static Local createDummyLocalForObject(){
    return new Local("a", ClassTypeUtil.createDummyClassType());
  }

  /**creates a dummy Local for an Int
   * Name b
   * Type int
   *
   * @return a dummy Local for a int value
   */
  public static Local createDummyLocalForInt(){
    return new Local("b", IntType.getInstance());
  }
}
