package qilin.pta.toolkits.debloaterx;

import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ArrayElement;
import qilin.core.pag.PAG;
import qilin.util.Stopwatch;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.spark.pag.SparkField;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/*
 * implementing rules for identifying container objects as shown in Figure 8 in the paper.
 * */
public class ContainerFinder {
    protected final PTA pta;
    protected final PAG pag;
    private final Set<AllocNode> notcontainers = ConcurrentHashMap.newKeySet();
    private final Map<AllocNode, Set<SparkField>> containers = new ConcurrentHashMap<>();

    private final XUtility utility;

    public ContainerFinder(PTA pta, XUtility utility) {
        this.pta = pta;
        this.pag = pta.getPag();
        this.utility = utility;
    }

    /*
     * classify objects into primitive containers, normal containers and notcontainers.
     * */
    public void run() {
        Stopwatch s1 = Stopwatch.newAndStart("pre-containerFinder");
        Set<AllocNode> remainObjs = new HashSet<>();
        for (AllocNode heap : pag.getAllocNodes()) {
            Type type = heap.getType();
            if (type instanceof ArrayType at) {
                JNewArrayExpr nae = (JNewArrayExpr) heap.getNewExpr();
                Value vl = nae.getSize();
                if (utility.isCoarseType(at) && (!(vl instanceof IntConstant ic) || ic.value != 0)) {
                    containers.computeIfAbsent(heap, k -> new HashSet<>()).add(ArrayElement.v());
                } else {
                    notcontainers.add(heap);
                }
            } else if (type instanceof RefType refType) {
                if (utility.isCoarseType(refType) && heap.getMethod() != null) {
                    remainObjs.add(heap);
                } else {
                    notcontainers.add(heap);
                }
            } else {
                throw new RuntimeException("invalid type for " + heap);
            }
        }
        s1.stop();
        System.out.println(s1);
        Stopwatch s2 = Stopwatch.newAndStart("mid-containerFinder");
        remainObjs.parallelStream().forEach(utility::getHCQ);
        s2.stop();
        System.out.println(s2);
        Stopwatch s3 = Stopwatch.newAndStart("remain-containerFinder");
        remainObjs.parallelStream().forEach(heap -> {
            Set<SparkField> fields = utility.getFields(heap);
            fields = fields.stream().filter(f -> utility.isCoarseType(f.getType())).collect(Collectors.toSet());
            HeapContainerQuery hcq = utility.getHCQ(heap);
            for (SparkField field : fields) {
                // check in
                boolean hasIn = hasNonThisStoreOnField(heap, field, hcq);
                if (!hasIn) {
                    continue;
                }
                // check out
                boolean hasOut = hasNonThisLoadFromField(heap, field, hcq);
                if (hasOut) {
                    containers.computeIfAbsent(heap, k -> new HashSet<>()).add(field);
                }
            }
            if (!containers.containsKey(heap)) {
                notcontainers.add(heap);
            }
        });
        s3.stop();
        System.out.println(s3);
        System.out.println("#ObjectsNotAContainer:" + notcontainers.size());
        System.out.println("#Container:" + containers.size());
    }

    private boolean hasNonThisStoreOnField(AllocNode heap, SparkField field, HeapContainerQuery hcq) {
        if (utility.hasNonThisStoreOnField(heap, field)) {
            return true;
        }
        return hcq.hasParamsStoredInto(field);
    }

    private boolean hasNonThisLoadFromField(AllocNode heap, SparkField field, HeapContainerQuery hcq) {
        if (utility.hasNonThisLoadFromField(heap, field)) {
            return true;
        }
        return hcq.hasOutMethodsWithRetOrParamValueFrom(field);
    }

    public boolean isAContainer(AllocNode heap) {
        if (this.containers.containsKey(heap)) {
            return true;
        } else if (heap.getMethod().getSignature().startsWith("<java.util.Arrays: java.lang.Object[] copyOf(java.lang.Object[],int,java.lang.Class)>")
                || heap.getMethod().getSignature().startsWith("<java.util.AbstractCollection: java.lang.Object[] toArray(java.lang.Object[])>")) {
            // We will remove such hacks in the future. The noise of [java.lang.String] types introduced by the resolving of reflection of
            // Array.newInstance makes the whole analysis imprecise. Qilin's reflection mechanism causes this. One potential solution is
            // not to resolve reflection for these two methods.
            return true;
        }
        return false;
    }
}
