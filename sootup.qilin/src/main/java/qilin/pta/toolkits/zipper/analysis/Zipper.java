package qilin.pta.toolkits.zipper.analysis;

import qilin.core.PTA;
import qilin.core.pag.*;
import qilin.pta.toolkits.common.OAG;
import qilin.pta.toolkits.common.ToolUtil;
import qilin.pta.toolkits.zipper.Global;
import qilin.pta.toolkits.zipper.flowgraph.FlowAnalysis;
import qilin.pta.toolkits.zipper.flowgraph.ObjectFlowGraph;
import qilin.util.ANSIColor;
import qilin.util.Stopwatch;
import qilin.util.graph.ConcurrentDirectedGraphImpl;
import soot.RefType;
import soot.SootMethod;
import soot.Type;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static qilin.util.ANSIColor.color;

/**
 * Main class of Zipper, which computes precision-critical methods
 * in the program being analyzed.
 */
public class Zipper {
    private final PTA pta;
    private final PotentialContextElement pce;
    private final ObjectFlowGraph ofg;
    private final AtomicInteger analyzedClasses = new AtomicInteger(0);
    private final AtomicInteger totalPFGNodes = new AtomicInteger(0);
    private final AtomicInteger totalPFGEdges = new AtomicInteger(0);
    private final ConcurrentDirectedGraphImpl<Node> overallPFG = new ConcurrentDirectedGraphImpl<>();
    private final Map<SootMethod, Integer> methodPts;
    private final Map<Type, Collection<SootMethod>> pcmMap = new ConcurrentHashMap<>(1024);

    public Zipper(PTA pta) {
        this.pta = pta;
        OAG oag = new OAG(pta);
        oag.build();
        System.out.println("#OAG:" + oag.allNodes().size());
        this.pce = new PotentialContextElement(pta, oag);
        this.ofg = buildObjectFlowGraph();
        this.methodPts = getMethodPointsToSize();
    }

    public static void outputNumberOfClasses(PTA pta) {
        int nrClasses = (int) pta.getPag().getAllocNodes().stream()
                .map(AllocNode::getType)
                .distinct()
                .count();
        System.out.println("#classes: " + ANSIColor.BOLD + ANSIColor.GREEN + nrClasses + ANSIColor.RESET);
        System.out.println();
    }

    public int numberOfOverallPFGNodes() {
        return overallPFG.allNodes().size();
    }

    public int numberOfOverallPFGEdges() {
        int nrEdges = 0;
        for (Node node : overallPFG.allNodes()) {
            nrEdges += overallPFG.succsOf(node).size();
        }
        return nrEdges;
    }

    public ObjectFlowGraph buildObjectFlowGraph() {
        Stopwatch ofgTimer = Stopwatch.newAndStart("Object Flow Graph Timer");
        System.out.println("Building OFG (Object Flow Graph) ... ");
        ObjectFlowGraph ofg = new ObjectFlowGraph(pta);
        ofgTimer.stop();
        System.out.println(ofgTimer);
        outputObjectFlowGraphSize(ofg);
        return ofg;
    }

    public static void outputObjectFlowGraphSize(ObjectFlowGraph ofg) {
        int nrNodes = ofg.allNodes().size();
        int nrEdges = 0;
        for (Node node : ofg.allNodes()) {
            nrEdges += ofg.outEdgesOf(node).size();
        }

        System.out.println("#nodes in OFG: " + ANSIColor.BOLD + ANSIColor.GREEN + nrNodes + ANSIColor.RESET);
        System.out.println("#edges in OFG: " + ANSIColor.BOLD + ANSIColor.GREEN + nrEdges + ANSIColor.RESET);
        System.out.println();
    }

    /**
     * @return set of precision-critical methods in the program
     */
    public Set<SootMethod> analyze() {
        reset();
        System.out.println("Building PFGs (Pollution Flow Graphs) and computing precision-critical methods ...");
        List<RefType> types = pta.getPag().getAllocNodes().stream()
                .map(AllocNode::getType)
                .distinct()
                .sorted(Comparator.comparing(Type::toString))
                .filter(t -> t instanceof RefType)
                .map(t -> (RefType) t)
                .collect(Collectors.toList());
        if (Global.getThread() == Global.UNDEFINE) {
            computePCM(types);
        } else {
            computePCMConcurrent(types, Global.getThread());
        }
        System.out.println("#avg. nodes in PFG: " + ANSIColor.BOLD + ANSIColor.GREEN +
                Math.round(totalPFGNodes.floatValue() / analyzedClasses.get()) + ANSIColor.RESET);
        System.out.println("#avg. edges in PFG: " + ANSIColor.BOLD + ANSIColor.GREEN +
                Math.round(totalPFGEdges.floatValue() / analyzedClasses.get()) + ANSIColor.RESET);
        System.out.println("#Node:" + totalPFGNodes.intValue());
        System.out.println("#Edge:" + totalPFGEdges.intValue());
        System.out.println("#Node2:" + numberOfOverallPFGNodes());
        System.out.println("#Edge2:" + numberOfOverallPFGEdges());
        System.out.println();

        Set<SootMethod> pcm = collectAllPrecisionCriticalMethods(pcmMap,
                computePCMThreshold());
        System.out.println("#Precision-critical methods: " + ANSIColor.BOLD + ANSIColor.GREEN + pcm.size() + ANSIColor.RESET);
        return pcm;
    }

    private void computePCM(List<RefType> types) {
        FlowAnalysis fa = new FlowAnalysis(pta, pce, ofg);
        types.forEach(type -> analyze(type, fa));
    }

    private void computePCMConcurrent(List<RefType> types, int nThread) {
        ExecutorService executorService = Executors.newFixedThreadPool(nThread);
        types.forEach(type ->
                executorService.execute(() -> {
                    FlowAnalysis fa = new FlowAnalysis(pta, pce, ofg);
                    analyze(type, fa);
                }));
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param type
     * @param fa   Compute the set of precision-critical methods for a class/type and add these methods
     *             to the pcm collection.
     */
    private void analyze(RefType type, FlowAnalysis fa) {
        if (Global.isDebug()) {
            System.out.println("----------------------------------------");
        }
        // System.out.println(color(YELLOW, "Zipper: analyzing ") + type);

        // Obtain all methods of type (including inherited methods)
        Set<SootMethod> ms = pta.getPag().getAllocNodes().stream().
                filter(o -> o.getType().equals(type))
                .map(pce::methodsInvokedOn)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        if (type.toString().equals("java.util.HashMap")) {
            System.out.println("ssssss");
        }
        // Obtain IN methods
        Set<SootMethod> inms = ms.stream()
                .filter(m -> !m.isPrivate())
                .filter(m -> ToolUtil.getParameters(pta.getPag(), m).stream()
                        .anyMatch(p -> !pta.reachingObjects(p).toCIPointsToSet().isEmpty()))
                .collect(Collectors.toSet());

        // Obtain OUT methods
        Set<SootMethod> outms = new HashSet<>();
        ms.stream()
                .filter(m -> !m.isPrivate())
                .filter(m -> ToolUtil.getRetVars(pta.getPag(), m).stream()
                        .anyMatch(r -> !pta.reachingObjects(r).toCIPointsToSet().isEmpty()))
                .forEach(outms::add);

        // OUT methods of inner classes and special access$ methods
        // are also considered as the OUT methods of current type
        pce.PCEMethodsOf(type).stream()
                .filter(m -> !m.isPrivate() && !m.isStatic())
                .filter(m -> ToolUtil.isInnerType(
                        m.getDeclaringClass().getType(), type))
                .forEach(outms::add);
        pce.PCEMethodsOf(type).stream()
                .filter(m -> !m.isPrivate() && !m.isStatic())
                .filter(m -> m.getDeclaringClass().getType().equals(type)
                        && m.toString().contains("access$"))
                .forEach(outms::add);

        if (Global.isDebug()) {
            System.out.println(color(ANSIColor.YELLOW, "In methods:"));
            inms.stream()
                    .sorted(Comparator.comparing(SootMethod::toString))
                    .forEach(m -> System.out.println("  " + m));
            System.out.println(color(ANSIColor.YELLOW, "Out methods:"));
            outms.stream()
                    .sorted(Comparator.comparing(SootMethod::toString))
                    .forEach(m -> System.out.println("  " + m));
        }

        fa.initialize(type, inms, outms);
        inms.forEach(fa::analyze);
        Set<Node> flowNodes = fa.getFlowNodes();
        Set<SootMethod> precisionCriticalMethods = getPrecisionCriticalMethods(type, flowNodes);
        if (Global.isDebug()) {
            if (!precisionCriticalMethods.isEmpty()) {
                System.out.println(color(ANSIColor.BLUE, "Flow found: ") + type);
            }
        }
        mergeAnalysisResults(type, fa.numberOfPFGNodes(), fa.numberOfPFGEdges(), precisionCriticalMethods);
        mergeSinglePFG(fa.getPFG());
        fa.clear();
    }

    private void mergeSinglePFG(ConcurrentDirectedGraphImpl<Node> pfg) {
        for (Node node : pfg.allNodes()) {
            this.overallPFG.addNode(node);
            for (Node succ : pfg.succsOf(node)) {
                this.overallPFG.addEdge(node, succ);
            }
        }
    }

    private void mergeAnalysisResults(Type type, int nrPFGNodes, int nrPFGEdges, Set<SootMethod> precisionCriticalMethods) {
        analyzedClasses.incrementAndGet();
        totalPFGNodes.addAndGet(nrPFGNodes);
        totalPFGEdges.addAndGet(nrPFGEdges);
        pcmMap.put(type, new ArrayList<>(precisionCriticalMethods));
    }

    private Set<SootMethod> collectAllPrecisionCriticalMethods(
            Map<Type, Collection<SootMethod>> pcmMap, int pcmThreshold) {
        System.out.println("PCM Threshold:" + pcmThreshold);
        Set<SootMethod> pcm = new HashSet<>();
        pcmMap.forEach((type, pcms) -> {
            if (Global.isExpress() &&
                    getAccumulativePointsToSetSize(pcms) > pcmThreshold) {
                System.out.println("type: " + type + ", accumulativePTSize: " + getAccumulativePointsToSetSize(pcms));
                return;
            }
            pcm.addAll(pcms);
        });
        return pcm;
    }

    private int computePCMThreshold() {
        // Use points-to size of whole program as denominator
        int totalPTSSize = 0;
        for (ValNode var : pta.getPag().getValNodes()) {
            if (var instanceof VarNode varNode) {
//                Collection<AllocNode> pts = ToolUtil.pointsToSetOf(pta, varNode);
                totalPTSSize += pta.reachingObjects(varNode).toCIPointsToSet().size();
            }
        }
        return (int) (Global.getExpressThreshold() * totalPTSSize);
    }

    private Set<SootMethod> getPrecisionCriticalMethods(Type type, Set<Node> nodes) {
        return nodes.stream()
                .map(this::node2ContainingMethod)
                .filter(Objects::nonNull)
                .filter(pce.PCEMethodsOf(type)::contains)
                .collect(Collectors.toSet());
    }

    private SootMethod node2ContainingMethod(Node node) {
        if (node instanceof LocalVarNode lvn) {
            return lvn.getMethod();
        } else {
            ContextField ctxField = (ContextField) node;
            return ctxField.getBase().getMethod();
        }
    }

    private void reset() {
        analyzedClasses.set(0);
        totalPFGNodes.set(0);
        totalPFGEdges.set(0);
        pcmMap.clear();
    }

    private Map<SootMethod, Integer> getMethodPointsToSize() {
        Map<SootMethod, Integer> results = new HashMap<>();
        for (ValNode valnode : pta.getPag().getValNodes()) {
            if (!(valnode instanceof LocalVarNode lvn)) {
                continue;
            }
            SootMethod inMethod = lvn.getMethod();
            int ptSize = ToolUtil.pointsToSetSizeOf(pta, lvn);
            if (results.containsKey(inMethod)) {
                int oldValue = results.get(inMethod);
                results.replace(inMethod, oldValue, oldValue + ptSize);
            } else {
                results.put(inMethod, ptSize);
            }
        }
        return results;
    }

    private long getAccumulativePointsToSetSize(Collection<SootMethod> methods) {
        return methods.stream()
                .mapToInt(methodPts::get)
                .sum();
    }
}