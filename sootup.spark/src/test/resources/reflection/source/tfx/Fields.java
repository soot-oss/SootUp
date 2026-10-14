package tfx;

import java.lang.reflect.Field;

/** Field get/set, instance and static; exercises body rewriting (no call edges). */
public class Fields {
  public static void main(String[] args) throws Exception {
    Base b = new Base();
    Field f = Base.class.getField("field");
    f.set(b, new Object());
    Object v = f.get(b);
    Field s = Base.class.getField("staticField");
    s.set(null, v);
    Object w = s.get(null);
  }
}
