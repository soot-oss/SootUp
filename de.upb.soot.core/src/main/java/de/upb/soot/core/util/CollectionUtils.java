package de.upb.soot.core.util;

import java.util.Set;

public class CollectionUtils {
  /** Removes the oldValue from the set and adds the newValue afterwards. */
  public static <T> void replace(Set<T> set, T oldValue, T newValue) {
    set.remove(oldValue);
    set.add(newValue);
  }
}
