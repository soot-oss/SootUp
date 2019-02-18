package de.upb.soot.util;

import java.util.Iterator;

/**
 * A java.util.Map-like map with Numberable objects as the keys.
 *
 * @author Ondrej Lhotak
 */

// TODO: check copied code from old soot (whole class)

public final class SmallNumberedMap<T> {
  public SmallNumberedMap() {
    //
  }

  /** Associates a value with a key. */
  public boolean put(Numberable key, T value) {
    int pos = findPosition(key);
    if (array[pos] == key) {
      if (values[pos] == value) {
        return false;
      }
      values[pos] = value;
      return true;
    }
    size++;
    if (size * 3 > array.length * 2) {
      doubleSize();
      pos = findPosition(key);
    }
    array[pos] = key;
    values[pos] = value;
    return true;
  }

  /** Returns the value associated with a given key. */
  public T get(Numberable key) {
    return (T) values[findPosition(key)];
  }

  /** Returns the number of non-null values in this map. */
  public int nonNullSize() {
    int ret = 0;
    for (Object element : values) {
      if (element != null) {
        ret++;
      }
    }
    return ret;
  }

  /** Returns an iterator over the keys with non-null values. */
  public Iterator<Numberable> keyIterator() {
    return new KeyIterator(this);
  }

  /** Returns an iterator over the non-null values. */
  public Iterator<T> iterator() {
    return new ValueIterator(this);
  }

  abstract class SmallNumberedMapIterator<C> implements Iterator<C> {
    SmallNumberedMap<C> map;
    int cur = 0;

    SmallNumberedMapIterator(SmallNumberedMap<C> map) {
      this.map = map;
      seekNext();
    }

    protected final void seekNext() {
      try {
        while (map.values[cur] == null) {
          cur++;
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        cur = -1;
      }
    }

    public final boolean hasNext() {
      return cur != -1;
    }

    public abstract C next();

    public void remove() {
      throw new RuntimeException("Not implemented.");
    }
  }

  class KeyIterator extends SmallNumberedMapIterator<Numberable> {
    KeyIterator(SmallNumberedMap map) {
      super(map);
    }

    public final Numberable next() {
      Numberable ret = array[cur];
      cur++;
      seekNext();
      return ret;
    }
  }

  class ValueIterator extends SmallNumberedMapIterator<T> {
    ValueIterator(SmallNumberedMap<T> map) {
      super(map);
    }

    public final T next() {
      Object ret = values[cur];
      cur++;
      seekNext();
      return (T) ret;
    }
  }

  /* Private stuff. */

  private int findPosition(Numberable o) {
    int number = o.getNumber();
    if (number == 0) {
      throw new RuntimeException("unnumbered");
    }
    number = number & (array.length - 1);
    while (true) {
      if (array[number] == o) {
        return number;
      }
      if (array[number] == null) {
        return number;
      }
      number = (number + 1) & (array.length - 1);
    }
  }

  private void doubleSize() {
    Numberable[] oldArray = array;
    Object[] oldValues = values;
    int newLength = array.length * 2;
    values = new Object[newLength];
    array = new Numberable[newLength];
    for (int i = 0; i < oldArray.length; i++) {
      Numberable element = oldArray[i];
      if (element != null) {
        int pos = findPosition(element);
        array[pos] = element;
        values[pos] = oldValues[i];
      }
    }
  }

  private Numberable[] array = new Numberable[8];
  private Object[] values = new Object[8];
  private int size = 0;
}
