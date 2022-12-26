package qilin.core.util;

import java.util.Iterator;

/**
 * A numberer which also supports an iterator on newly-added objects.
 *
 * @author xiao, generalize the interface
 */
public interface IterableNumberer<E> extends Numberer<E>, Iterable<E> {
    /** Returns an iterator over all objects added to the numberer. */
    @Override
    Iterator<E> iterator();
}
