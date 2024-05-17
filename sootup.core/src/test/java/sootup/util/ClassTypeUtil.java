package sootup.util;

import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;

public class ClassTypeUtil {

  /**creates a dummy Class type
   * Classname Test
   * Package name test
   * Fully Qualified Name test.Test
   *
   * @return a dummy class type
   */
  public static ClassType createDummyClassType(){
    return new ClassType() {
      @Override
      public boolean isBuiltInClass() {
        return false;
      }

      @Override
      public String getFullyQualifiedName() {
        return "Test";
      }

      @Override
      public String getClassName() {
        return "Test";
      }

      @Override
      public PackageName getPackageName() {
        return new PackageName("test");
      }
    };
  }

}
