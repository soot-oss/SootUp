package qilin.pta.toolkits.debloaterx;

import com.google.common.collect.Sets;
import qilin.core.PTA;
import qilin.core.pag.*;
import qilin.core.sets.PointsToSet;
import soot.*;
import soot.jimple.spark.pag.SparkField;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 * A container usage pattern-based approach to identifying context-independent objects for context debloating.
 * Corresponding to Algorithm 1 in the paper.
 * */
public class DebloaterX {
    private final PTA pta;
    private final PAG pag;
    private final ContainerFinder containerFinder;
    private final XUtility utility;

    public DebloaterX(PTA pta) {
        this.pta = pta;
        this.pag = pta.getPag();
        this.utility = new XUtility(pta);
        this.containerFinder = new ContainerFinder(pta, utility);
        this.containerFinder.run();
    }

    /*
     * identifying factory-created containers (Fig 11 in the paper)
     * */
    private boolean isAFactoryCreatedContainer(AllocNode heap, IntraFlowAnalysis mpag) {
        SootMethod method = heap.getMethod();
        if (method.isStatic()) {
            Type type = method.getReturnType();
            if (!(type instanceof RefLikeType)) {
                return false;
            }
            MethodPAG methodPag = pag.getMethodPAG(method);
            VarNode mRet = methodPag.nodeFactory().caseRet();
            if (pta.reachingObjects(mRet).toCIPointsToSet().toCollection().contains(heap)) {
                return mpag.isDirectlyReturnedHeap(heap);
            }
        }
        return false;
    }

    /*
     *  identifying container wrappers (Fig 12 in the paper)
     * */
    private boolean isAContainerWrapper(AllocNode heap, IntraFlowAnalysis mpag) {
        SootMethod method = heap.getMethod();
        if (method.isStatic()) {
            return false;
        }
        Type type = method.getReturnType();
        if (!(type instanceof RefLikeType)) {
            return false;
        }
        MethodPAG methodPag = pag.getMethodPAG(method);
        VarNode mRet = methodPag.nodeFactory().caseRet();
        PointsToSet pts = pta.reachingObjects(mRet);
        Collection<AllocNode> ptsSet = pts.toCIPointsToSet().toCollection();
        if (ptsSet.contains(heap)) {
            if (mpag.isDirectlyReturnedHeap(heap)) {
                return mpag.isContentFromParam(heap);
            }
        }
        return false;
    }

    /*
     * identify inner containers (Fig 9 in the paper).
     * */
    private boolean isAnInnerContainer(AllocNode heap, IntraFlowAnalysis mpag) {
        SootMethod method = heap.getMethod();
        if (method.isStatic()) {
            return false;
        }
        Set<SparkField> fields = mpag.retrieveStoreFields(heap);
        if (fields.isEmpty()) {
            return false;
        }
        Set<AllocNode> objects = this.utility.getReceiverObjects(method);
        for (AllocNode revobj : objects) {
            if (revobj.getType() instanceof RefType) {
                HeapContainerQuery hcq = this.utility.getHCQ(revobj);
                for (SparkField field : fields) {
                    if (hcq.isCSField(field)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected final Set<AllocNode> ctxDepHeaps = ConcurrentHashMap.newKeySet();
    protected final Set<AllocNode> containerFactory = ConcurrentHashMap.newKeySet();
    protected final Set<AllocNode> containerWrapper = ConcurrentHashMap.newKeySet();
    protected final Set<AllocNode> innerContainer = ConcurrentHashMap.newKeySet();

    /*
     * Implementing Step 3 of Algorithm 1 (in the paper): finding context-dependent objects according to container-usage patterns
     * */
    public void run() {
        Map<SootMethod, Set<AllocNode>> m2o = new HashMap<>();
        for (AllocNode heap : pag.getAllocNodes()) {
            SootMethod method = heap.getMethod();
            if (method == null || method.isStaticInitializer()) {
                continue;
            }
            m2o.computeIfAbsent(method, k -> new HashSet<>()).add(heap);
        }
        m2o.keySet().parallelStream().forEach(method -> {
            IntraFlowAnalysis ifa = new IntraFlowAnalysis(utility, method);
            for (AllocNode heap : m2o.get(method)) {
                if (!this.containerFinder.isAContainer(heap)) {
                    continue;
                }
                if (isAFactoryCreatedContainer(heap, ifa)) {
                    containerFactory.add(heap);
                    ctxDepHeaps.add(heap);
                }
                if (isAContainerWrapper(heap, ifa)) {
                    containerWrapper.add(heap);
                    ctxDepHeaps.add(heap);
                }
                if (isAnInnerContainer(heap, ifa)) {
                    innerContainer.add(heap);
                    ctxDepHeaps.add(heap);
                }
            }
        });
        System.out.println("#OBJECTS:" + pag.getAllocNodes().size());
        System.out.println("#CS:" + ctxDepHeaps.size());
        System.out.println("#CI:" + (pag.getAllocNodes().size() - ctxDepHeaps.size()));
        System.out.println("#ContainerFactory:" + containerFactory.size());
        System.out.println("#ContainerWrapper:" + containerWrapper.size());
        System.out.println("#InnerContainer:" + innerContainer.size());
        {
            // for drawn venn3 figure.
            int onlyInFactory = Sets.difference(Sets.difference(containerFactory, containerWrapper), innerContainer).size();
            int onlyInWrapper = Sets.difference(Sets.difference(containerWrapper, containerFactory), innerContainer).size();
            int onlyInInner = Sets.difference(Sets.difference(innerContainer, containerWrapper), containerFactory).size();
            int inAll = Sets.intersection(Sets.intersection(innerContainer, containerWrapper), containerFactory).size();
            int onlyInFactoryAndWrapper = Sets.difference(Sets.intersection(containerFactory, containerWrapper), innerContainer).size();
            int onlyInFactoryAndInner = Sets.difference(Sets.intersection(containerFactory, innerContainer), containerWrapper).size();
            int onlyInWrapperAndInner = Sets.difference(Sets.intersection(containerWrapper, innerContainer), containerFactory).size();
            System.out.println("#onlyInFactory:" + onlyInFactory);
            System.out.println("#onlyInWrapper:" + onlyInWrapper);
            System.out.println("#onlyInInner:" + onlyInInner);
            System.out.println("#inAll:" + inAll);
            System.out.println("#onlyInFactoryAndWrapper:" + onlyInFactoryAndWrapper);
            System.out.println("#onlyInFactoryAndInner:" + onlyInFactoryAndInner);
            System.out.println("#onlyInWrapperAndInner:" + onlyInWrapperAndInner);
            System.out.println("#SUM:" + (onlyInFactory + onlyInWrapper + onlyInInner + inAll + onlyInFactoryAndWrapper + onlyInFactoryAndInner + onlyInWrapperAndInner));
            System.out.println("venn3(subsets = (" + onlyInFactory + "," + onlyInWrapper + "," + onlyInFactoryAndWrapper + "," + onlyInInner + ","
                    + onlyInFactoryAndInner + "," + onlyInWrapperAndInner + ", " + inAll + "))");
        }
    }

    public Set<AllocNode> getCtxDepHeaps() {
        return ctxDepHeaps;
    }

}
