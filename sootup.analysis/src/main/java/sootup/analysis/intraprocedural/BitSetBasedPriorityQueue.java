package sootup.analysis.intraprocedural;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.*;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fixed size priority queue based on bitsets. The elements of the priority queue are ordered
 * according to a given universe. This priority queue does not permit {@code null} elements.
 * Inserting of elements that are not part of the universe is also permitted (doing so will result
 * in a {@code NoSuchElementException}).
 *
 * @author Steven Lambeth
 * @param <E> the type of elements held in the universe
 */
public abstract class BitSetBasedPriorityQueue<E> extends AbstractQueue<E> {
  private static final Logger logger = LoggerFactory.getLogger(BitSetBasedPriorityQueue.class);

  private final List<? extends E> universe;
  private final Map<E, Integer> ordinalMap;
  final int N;
  int min = Integer.MAX_VALUE;

  BitSetBasedPriorityQueue(List<? extends E> universe, Map<E, Integer> ordinalMap) {
    assert ordinalMap.size() == universe.size();
    this.universe = universe;
    this.ordinalMap = ordinalMap;
    this.N = universe.size();
  }

  abstract class Itr implements Iterator<E> {
    long expected = getExpected();
    int next = min;
    int now = Integer.MAX_VALUE;

    abstract long getExpected();

    @Override
    public boolean hasNext() {
      return next < N;
    }

    @Override
    public E next() {
      if (expected != getExpected()) {
        throw new ConcurrentModificationException();
      }
      if (next >= N) {
        throw new NoSuchElementException();
      }

      now = next;
      next = nextSetBit(next + 1);
      return universe.get(now);
    }

    @Override
    public void remove() {
      if (now >= N) {
        throw new IllegalStateException();
      }
      if (expected != getExpected()) {
        throw new ConcurrentModificationException();
      }

      BitSetBasedPriorityQueue.this.remove(now);
      expected = getExpected();
      now = Integer.MAX_VALUE;
    }
  }

  int getOrdinal(@Nonnull Object o) {
    Integer i = ordinalMap.get(o);
    if (i == null) {
      throw new NoSuchElementException();
    }
    return i;
  }

  /** Adds all elements of the universe to this queue. */
  abstract void addAll();

  /**
   * Returns the index of the first bit that is set to <code>true</code> that occurs on or after the
   * specified starting index. If no such bit exists then a value bigger that {@code N} is returned.
   *
   * @param fromIndex the index to start checking from (inclusive).
   * @return the index of the next set bit.
   */
  abstract int nextSetBit(int fromIndex);

  abstract boolean remove(int ordinal);

  abstract boolean add(int ordinal);

  abstract boolean contains(int ordinal);

  /** {@inheritDoc} */
  @Override
  public final E peek() {
    return isEmpty() ? null : universe.get(min);
  }

  /** {@inheritDoc} */
  @Override
  public final E poll() {
    if (isEmpty()) {
      return null;
    }
    E e = universe.get(min);
    remove(min);
    return e;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NoSuchElementException if e not part of the universe
   * @throws NullPointerException if e is {@code null}
   */
  @Override
  public final boolean add(E e) {
    return offer(e);
  }

  /**
   * {@inheritDoc}
   *
   * @throws NoSuchElementException if e not part of the universe
   * @throws NullPointerException if e is {@code null}
   */
  @Override
  public final boolean offer(E e) {
    return add(getOrdinal(e));
  }

  /** {@inheritDoc} */
  @Override
  public final boolean remove(Object o) {
    if (o == null || isEmpty()) {
      return false;
    }
    try {
      if (o.equals(peek())) {
        remove(min);
        return true;
      } else {
        return remove(getOrdinal(o));
      }
    } catch (NoSuchElementException e) {
      logger.debug(e.getMessage());
      return false;
    }
  }

  /** {@inheritDoc} */
  @Override
  public final boolean contains(Object o) {
    if (o == null) {
      return false;
    }
    try {
      if (o.equals(peek())) {
        return true;
      } else {
        return contains(getOrdinal(o));
      }
    } catch (NoSuchElementException e) {
      logger.debug(e.getMessage());
      return false;
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return min >= N;
  }

  /**
   * Creates a new full priority queue
   *
   * @param <E>
   * @param universe
   * @return
   */
  public static <E> BitSetBasedPriorityQueue<E> of(List<? extends E> universe) {
    BitSetBasedPriorityQueue<E> q = noneOf(universe);
    q.addAll();
    return q;
  }

  /**
   * Creates a new empty priority queue
   *
   * @param <E>
   * @param universe
   * @return
   */
  public static <E> BitSetBasedPriorityQueue<E> noneOf(List<? extends E> universe) {
    Map<E, Integer> ordinalMap = new HashMap<>(2 * universe.size() / 3);
    int i = 0;
    for (E e : universe) {
      if (e == null) {
        throw new NullPointerException("null is not allowed");
      }
      if (ordinalMap.put(e, i++) != null) {
        throw new IllegalArgumentException("duplicate key found");
      }
    }
    return newPriorityQueue(universe, ordinalMap);
  }

  private static <E> BitSetBasedPriorityQueue<E> newPriorityQueue(
      List<? extends E> universe, Map<E, Integer> ordinalMap) {
    return new LargeBitSetBasedPriorityQueue<E>(universe, ordinalMap);
  }

  static class LargeBitSetBasedPriorityQueue<E> extends BitSetBasedPriorityQueue<E> {

    private final BitSet queue;
    private long modCount = 0;

    LargeBitSetBasedPriorityQueue(List<? extends E> universe, Map<E, Integer> ordinalMap) {
      super(universe, ordinalMap);
      queue = new BitSet(N);
    }

    @Override
    boolean add(int ordinal) {
      if (contains(ordinal)) {
        return false;
      }
      queue.set(ordinal);
      min = Math.min(min, ordinal);
      modCount++;
      return true;
    }

    @Override
    void addAll() {
      queue.set(0, N);
      min = 0;
      modCount++;
    }

    @Override
    int nextSetBit(int fromIndex) {
      int i = queue.nextSetBit(fromIndex);
      return (i < 0) ? Integer.MAX_VALUE : i;
    }

    @Override
    boolean remove(int ordinal) {
      if (!contains(ordinal)) {
        return false;
      }
      queue.clear(ordinal);

      if (min == ordinal) {
        min = nextSetBit(min + 1);
      }

      modCount++;
      return true;
    }

    @Override
    boolean contains(int ordinal) {
      return queue.get(ordinal);
    }

    @Override
    public Iterator<E> iterator() {
      return new Itr() {
        @Override
        long getExpected() {
          return modCount;
        }
      };
    }

    @Override
    public int size() {
      return queue.cardinality();
    }
  }
}
