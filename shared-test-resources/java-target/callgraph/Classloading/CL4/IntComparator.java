// https://bitbucket.org/delors/jcg/src/master/jcg_testcases/src/main/resources/Classloading.md

// Used by CL4.java

package lib;

import java.util.Comparator;
import cl4.Demo;

public class IntComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
        Demo.callback;
        return o1.compareTo(o2);
    }
}
