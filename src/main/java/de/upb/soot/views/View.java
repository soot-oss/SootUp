package de.upb.soot.views;

import de.upb.soot.jimple.common.type.RefType;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class View.
 * 
 * @author Linghui Luo created on 31.07.2018
 */
public class View {
  /**
   * a static map to store the RefType of each class according to its name. RefType of each class should just have one
   * instance.
   */
  private static Map<String, RefType> nameToClass = new HashMap<String, RefType>();

  /**
   * Instantiates a new view.
   */
  public View() {
    RefType.setView(this);// set the view for RefType
  }

  /**
   * Gets the RefType instance for given class name.
   *
   * @param className
   *          the class name
   * @return the RefType instance with the given class name if exists, otherwise return null.
   */
  public RefType getRefType(String className) {
    return nameToClass.get(className);
  }

  /**
   * Adds the RefType instance created for the given class name to {@link View#nameToClass}.
   *
   * @param className
   *          the class name
   * @param ref
   *          the RefType instance with the given class name
   */
  public void addRefType(String className, RefType ref) {
    if (!nameToClass.containsKey(className)) {
      nameToClass.put(className, ref);
    }
  }

}
