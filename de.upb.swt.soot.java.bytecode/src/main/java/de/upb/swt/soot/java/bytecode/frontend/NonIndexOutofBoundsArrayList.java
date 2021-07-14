package de.upb.swt.soot.java.bytecode.frontend;

import java.util.*;

/**
 * *
 *
 * @author Markus Schmidt
 */
public class NonIndexOutofBoundsArrayList<T> extends ArrayList<T> {

  public NonIndexOutofBoundsArrayList(int i) {
    super(i);
  }

  /** returns null instead of IndexOutfBoundsException */
  @Override
  public T get(int idx) {
    final int size = size();
    if (idx >= size) {
      return null;
    }
    return super.get(idx);
  }

  /**
   * modified in the way that the underlying array grows if index >= size() and fills the gap with
   * null elements instead of throwing an IndexOutOfBoundsException
   */
  @Override
  public T set(int idx, T t) {
    final int size = size();
    if (idx >= size) {
      // copy backing array just once instead of multiple times depending on the loop
      ensureCapacity(idx);
      // fill gaps with empty values
      for (int i = size; i < idx; i++) {
        add(null);
      }
      // finally add what we want to set to index idx
      add(t);
      return null;
    } else {
      return super.set(idx, t);
    }
  }
}
