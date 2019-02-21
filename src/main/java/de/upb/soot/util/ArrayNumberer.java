package de.upb.soot.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A class that numbers objects, so they can be placed in bitsets.
 *
 * @author Ondrej Lhotak
 * @author xiao, generalize it.
 */

// TODO: check copied code from old soot

public class ArrayNumberer<E extends Numberable> implements IterableNumberer<E> {
  protected E[] numberToObj;
  protected int lastNumber;

  @SuppressWarnings("unchecked")
  public ArrayNumberer() {
    numberToObj = (E[]) new Numberable[1024];
    lastNumber = 0;
  }

  public ArrayNumberer(E[] elements) {
    numberToObj = elements;
    lastNumber = elements.length;
  }

  private void resize(int n) {
    numberToObj = Arrays.copyOf(numberToObj, n);
  }

  @Override
  public synchronized void add(E o) {
    if (o.getNumber() != 0) {
      return;
    }

    ++lastNumber;
    if (lastNumber >= numberToObj.length) {
      resize(numberToObj.length * 2);
    }
    numberToObj[lastNumber] = o;
    o.setNumber(lastNumber);
  }

  @Override
  public long get(E o) {
    if (o == null) {
      return 0;
    }
    int ret = o.getNumber();
    if (ret == 0) {
      throw new RuntimeException("unnumbered: " + o);
    }
    return ret;
  }

  @Override
  public E get(long number) {
    if (number == 0) {
      return null;
    }
    E ret = numberToObj[(int) number];
    if (ret == null) {
      throw new RuntimeException("no object with number " + number);
    }
    return ret;
  }

  @Override
  public int size() {
    return lastNumber;
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      int cur = 1;

      @Override
      public final boolean hasNext() {
        return cur <= lastNumber && cur < numberToObj.length && numberToObj[cur] != null;
      }

      @Override
      public final E next() {
        if (hasNext()) {
          return numberToObj[cur++];
        }
        throw new NoSuchElementException();
      }

      @Override
      public final void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
