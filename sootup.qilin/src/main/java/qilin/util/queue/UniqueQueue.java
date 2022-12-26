/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.util.queue;

import java.util.*;

public class UniqueQueue<T> implements Queue<T> {
    private final Set<T> set;
    private final Queue<T> queue;

    public UniqueQueue() {
        this.set = new HashSet<>();
        this.queue = new LinkedList<>();
    }

    @Override
    public boolean add(T t) {
        if (set.contains(t)) {
            return false;
        } else {
            set.add(t);
            queue.add(t);
            return true;
        }
    }

    @Override
    public void clear() {
        set.clear();
        queue.clear();
    }

    @Override
    public boolean offer(T t) {
        return add(t);
    }

    @Override
    public T remove() {
        return poll();
    }

    @Override
    public T poll() {
        T t = queue.poll();
        if (t != null) {
            set.remove(t);
        }
        return t;
    }

    @Override
    public T peek() {
        return queue.peek();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean ret = false;
        for (T t : collection) {
            ret |= add(t);
        }
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T element() {
        throw new UnsupportedOperationException();
    }

}
