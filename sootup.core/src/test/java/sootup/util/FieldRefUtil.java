package sootup.util;

import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;

public class FieldRefUtil {

  /**
   * creates a dummy static field reference Signature: dummy Field Signature
   *
   * @return a dummy JStaticFieldRef
   */
  public static JStaticFieldRef createDummyStaticFieldRef() {
    return new JStaticFieldRef(SignatureUtil.createDummyFieldSignature());
  }

  /**
   * creates a dummy instance field reference local: dummy local Signature: dummy Field Signature
   *
   * @return a dummy JInstanceFieldRef
   */
  public static JInstanceFieldRef createDummyInstanceFieldRef() {
    return new JInstanceFieldRef(
        LocalUtil.createDummyLocalForInt(), SignatureUtil.createDummyFieldSignature());
  }
}
