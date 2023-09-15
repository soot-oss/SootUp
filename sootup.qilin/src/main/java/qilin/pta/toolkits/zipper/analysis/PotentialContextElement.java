package qilin.pta.toolkits.zipper.analysis;

import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.AllocNode;
import qilin.core.pag.VarNode;
import qilin.core.sets.PointsToSet;
import qilin.pta.toolkits.common.OAG;
import qilin.pta.toolkits.zipper.Global;
import qilin.util.collect.SetFactory;
import qilin.util.graph.MergedNode;
import qilin.util.graph.SCCMergedGraph;
import qilin.util.graph.TopologicalSorter;
import soot.SootMethod;
import soot.Type;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * For each object o, this class compute the set of methods
 * which o could potentially be their context element.
 * <p>
 * Conversely, for each method m, this class compute the
 * set of objects which could potentially be its context element.
 */
public class PotentialContextElement {
    private final PTA pta;
    // This map maps each object to the methods invoked on it.
    // For instance methods, they are the methods whose receiver is the object.
    // For static methods, they are the methods reachable from instance methods.
    private Map<AllocNode, Set<SootMethod>> invokedMethods;
    private Map<AllocNode, Set<SootMethod>> obj2invokedMethods;
    private final Map<Type, Set<SootMethod>> typePCEMethods;
    private final Map<AllocNode, Set<SootMethod>> pceOfMap;

    private final OAG oag;
    private final Map<Type, Set<AllocNode>> typeAllocatees;
    private final Map<AllocNode, Set<AllocNode>> allocateeMap;

    PotentialContextElement(final PTA pta, final OAG oag) {
        this.pta = pta;
        this.oag = oag;
        this.typePCEMethods = new ConcurrentHashMap<>();
        this.obj2invokedMethods = new ConcurrentHashMap<>();
        this.pceOfMap = new ConcurrentHashMap<>();
        this.typeAllocatees = new ConcurrentHashMap<>();
        this.allocateeMap = new ConcurrentHashMap<>();
        this.init(oag);
    }

    public Set<SootMethod> PCEMethodsOf(final AllocNode obj) {
        return this.pceOfMap.getOrDefault(obj, Collections.emptySet());
    }

    /**
     * @param type
     * @return PCE methods of the objects of given type.
     */
    public Set<SootMethod> PCEMethodsOf(final Type type) {
        if (!this.typePCEMethods.containsKey(type)) {
            final Set<SootMethod> methods = ConcurrentHashMap.newKeySet();
            pta.getPag().getAllocNodes().stream().
                    filter(o -> o.getType().equals(type)).
                    forEach(obj -> methods.addAll(this.PCEMethodsOf(obj)));
            this.typePCEMethods.put(type, methods);
        }
        return this.typePCEMethods.getOrDefault(type, Collections.emptySet());
    }

    /**
     * Compute PCE methods for each objects.
     */
    private void init(final OAG oag) {
        final SCCMergedGraph<AllocNode> mg = new SCCMergedGraph<>(oag);
        final TopologicalSorter<MergedNode<AllocNode>> topoSorter = new TopologicalSorter<>();
        final SetFactory<SootMethod> setFactory = new SetFactory<>();
        final SetFactory<AllocNode> setFactory2 = new SetFactory<>();
        this.buildMethodsInvokedOnObjects();
        this.invokedMethods = new HashMap<>();
        topoSorter.sort(mg, true).forEach(node -> {
            final Set<SootMethod> methods = ConcurrentHashMap.newKeySet();
            methods.addAll(setFactory.get(this.getPCEMethods(node, mg)));
            final Set<AllocNode> allocatees = ConcurrentHashMap.newKeySet();
            allocatees.addAll(setFactory2.get(this.getAllocatees(node, mg)));
            node.getContent().forEach(obj -> {
                pceOfMap.put(obj, methods);
                allocateeMap.put(obj, allocatees);
            });
        });
        this.invokedMethods = null;
        if (Global.isDebug()) {
            this.computePCEObjects();
        }

        oag.allNodes().forEach(obj -> {
            final Type type = obj.getType();
            this.typeAllocatees.putIfAbsent(type, new HashSet<>());
            this.typeAllocatees.get(type).addAll(this.allocateesOf(obj));
        });
    }

    private Set<AllocNode> getAllocatees(final MergedNode<AllocNode> node, final SCCMergedGraph<AllocNode> mg) {
        final Set<AllocNode> allocatees = new HashSet<>();
        mg.succsOf(node).forEach(n -> {
            // direct allocatees
            allocatees.addAll(n.getContent());
            // indirect allocatees, here, it does not require to traverse all heaps in n.getContent()
            // because of lines 104-107.
            final AllocNode o = n.getContent().iterator().next();
            allocatees.addAll(this.allocateesOf(o));
        });
        final AllocNode obj = node.getContent().iterator().next();
        if (node.getContent().size() > 1 || oag.succsOf(obj).contains(obj)) {
            // The merged node is a true SCC
            allocatees.addAll(node.getContent());
        }
        return allocatees;
    }

    private Set<AllocNode> allocateesOf(final AllocNode obj) {
        return this.allocateeMap.getOrDefault(obj, Collections.emptySet());
    }

    public Set<AllocNode> allocateesOf(final Type type) {
        return this.typeAllocatees.getOrDefault(type, Collections.emptySet());
    }

    private Set<SootMethod> getPCEMethods(final MergedNode<AllocNode> node, final SCCMergedGraph<AllocNode> mg) {
        final Set<SootMethod> methods = new HashSet<>();
        mg.succsOf(node).forEach(n -> {
            final AllocNode o2 = n.getContent().iterator().next();
            methods.addAll(this.PCEMethodsOf(o2));
        });
        node.getContent().forEach(o -> methods.addAll(this.invokedMethodsOf(o)));
        return methods;
    }

    public Set<SootMethod> methodsInvokedOn(final AllocNode obj) {
        return this.obj2invokedMethods.getOrDefault(obj, Collections.emptySet());
    }

    private void buildMethodsInvokedOnObjects() {
        this.obj2invokedMethods = new HashMap<>();
        pta.getNakedReachableMethods().stream().filter(m -> !m.isStatic()).forEach(instMtd -> {
            MethodNodeFactory mthdNF = pta.getPag().getMethodPAG(instMtd).nodeFactory();
            VarNode thisVar = mthdNF.caseThis();
            PointsToSet pts = pta.reachingObjects(thisVar).toCIPointsToSet();
            for (Iterator<AllocNode> it = pts.iterator(); it.hasNext(); ) {
                AllocNode obj = it.next();
                obj2invokedMethods.computeIfAbsent(obj, k -> new HashSet<>()).add(instMtd);
            }
        });
    }

    private Set<SootMethod> invokedMethodsOf(final AllocNode obj) {
        if (!this.invokedMethods.containsKey(obj)) {
            final Set<SootMethod> methods = new HashSet<>();
            final Queue<SootMethod> queue = new LinkedList<>(this.methodsInvokedOn(obj));
            while (!queue.isEmpty()) {
                final SootMethod method = queue.poll();
                methods.add(method);
                pta.getCallGraph().edgesOutOf(method).forEachRemaining(edge -> {
                    SootMethod callee = edge.getTgt().method();
                    if (callee.isStatic() && !methods.contains(callee)) {
                        queue.offer(callee);
                    }
                });
            }
            this.invokedMethods.put(obj, methods);
        }
        return this.invokedMethods.get(obj);
    }

    private void computePCEObjects() {
        final Map<SootMethod, Set<AllocNode>> pceObjs = new HashMap<>();
        pta.getPag().getAllocNodes().forEach(obj -> this.PCEMethodsOf(obj).forEach(method -> {
            if (!pceObjs.containsKey(method)) {
                pceObjs.put(method, new HashSet<>());
            }
            pceObjs.get(method).add(obj);
        }));
    }
}
