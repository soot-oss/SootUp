package de.upb.swt.soot.java.bytecode.frontend;


import java.util.*;
import javax.annotation.Nonnull;

/**
 * Intended for small, almost gapless (i.e. not like the SparseArray from Android) Ranges of int
 * which are mapped to Type T Objects
 *
 * <p>acts like a growing array -> no moving; just replacing/setting values at given index
 *
 * @author Markus Schmidt
 */

// improvement: replace casts by other mechanism; implement more methods of List
public class ArrayBackedList<T> implements List<T> {

  private Object[] data;

  public ArrayBackedList(int size) {
    data = new Object[size];
  }

  private void grow(int capacity) {
    int newSize = capacity + Math.max(5, data.length / 5);
    Object[] tmp = new Object[newSize];
    System.arraycopy(data, 0, tmp, 0, data.length);
    data = tmp;
  }

  @Override
  public int size() {
    return data.length;
  }

  @Override
  public boolean isEmpty() {
    return data.length == 0;
  }

  @Override
  public boolean contains(Object o) {
    return Arrays.stream(data).anyMatch(e -> o == e);
  }

  @Override
  public Iterator<T> iterator() {
    @SuppressWarnings("unchecked")
    Iterator<T> iterator = (Iterator<T>) Arrays.asList(data).iterator();
    return iterator;
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public <T1> T1[] toArray(@Nonnull T1[] t1s) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public boolean add(T t) {
    throw new UnsupportedOperationException("makes no sense");
  }

  @Override
  public T get(int o) {
    @SuppressWarnings("unchecked")
    T datum = (T) data[o];
    return datum;
  }

  @Override
  public T set(int idx, T t) {
    if (idx >= data.length) {
      grow(idx);
    }
    @SuppressWarnings("unchecked")
    T old = (T) data[idx];
    data[idx] = t;
    return old;
  }

  @Override
  public void add(int i, T t) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public T remove(int i) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public int indexOf(Object o) {
    ;
    for (int i = 0; i < data.length; i++) {
      Object e = data[i];
      if (e == o) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public int lastIndexOf(Object o) {
    for (int i = data.length - 1; i >= 0; i--) {
      Object e = data[i];
      if (e == o) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public ListIterator<T> listIterator() {
    return listIterator(0);
  }

  @Override
  public ListIterator<T> listIterator(int i) {
    @SuppressWarnings("unchecked")
    ListIterator<T> tListIterator = (ListIterator<T>) Arrays.asList(data).listIterator(i);
    return tListIterator;
  }

  @Override
  public List<T> subList(int i, int i1) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public boolean remove(Object o) {
    return set((Integer) o, null) != null;
  }

  @Override
  public boolean containsAll(@Nonnull Collection<?> collection) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public boolean addAll(@Nonnull Collection<? extends T> collection) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public boolean addAll(int i, @Nonnull Collection<? extends T> collection) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public boolean removeAll(@Nonnull Collection<?> collection) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public boolean retainAll(@Nonnull Collection<?> collection) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void clear() {
    data = new Object[0];
  }

  @Override
  public String toString() {
    return Arrays.toString(data);
  }
}
