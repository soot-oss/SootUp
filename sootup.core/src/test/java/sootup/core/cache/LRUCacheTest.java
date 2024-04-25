package sootup.core.cache;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Tag("Java8")
@ExtendWith(MockitoExtension.class)
public class LRUCacheTest {

    @Test
    void testLRUCacheSize() {
        SootClass sc1 = mock(SootClass.class);
        SootClass sc2 = mock(SootClass.class);
        ClassType ct1 = mock(ClassType.class);
        ClassType ct2 = mock(ClassType.class);

        LRUCache cache = new LRUCache(1);
        cache.putClass(ct1, sc1);
        cache.putClass(ct2, sc2);

        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void testCorrectCachedItem() {
        SootClass sc1 = mock(SootClass.class);
        SootClass sc2 = mock(SootClass.class);
        ClassType ct1 = mock(ClassType.class);
        ClassType ct2 = mock(ClassType.class);

        LRUCache cache = new LRUCache(1);
        cache.putClass(ct1, sc1);
        cache.putClass(ct2, sc2);

        assertThat(cache.getClass(ct1)).isNull();
        assertThat(cache.getClass(ct2)).isEqualTo(sc2);
    }

}
