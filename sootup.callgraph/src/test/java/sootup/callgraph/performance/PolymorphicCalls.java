package sootup.callgraph.performance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

public class PolymorphicCalls {
    public static void main(String[] args) {
        Collection c = make(args[0]);
        c.add("map");
    }

    static Collection make(String collectionType) {
        if (collectionType.equals("list")) {
            return new ArrayList();
        } else if (collectionType.equals("stack")) {
            return new Stack();
        } else {
            return new HashSet();
        }
    }
}
