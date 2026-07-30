package qilin.pta.toolkits.zipper.analysis;

import java.util.HashSet;
import java.util.Set;
import qilin.core.PTA;
import qilin.core.builder.callgraph.Edge;
import qilin.core.builder.callgraph.OnFlyCallGraph;
import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.VirtualCallSite;
import qilin.pta.toolkits.zipper.flowgraph.ZOAG;
import qilin.util.collect.SetFactory;
import qilin.util.collect.multimap.ConcurrentMultiMap;
import qilin.util.collect.multimap.MultiMap;
import qilin.util.queue.QueueReader;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;

public class PotentialContextElement {
  private final MultiMap<Type, SootMethod> type2PCEMethods = new ConcurrentMultiMap<>();
  private final PTA pta;
  private final MultiMap<AllocNode, SootMethod> objToInvokedMethods;

  PotentialContextElement(PTA prePTA, ZOAG oag) {
    this.pta = prePTA;
    objToInvokedMethods = buildObjAndInvokeToCallee();

    SetFactory<SootMethod> canonicalizer = new SetFactory<>();
    MultiMap<Type, AllocNode> type2Objs = oag.getType2Objs();

    type2Objs
        .keySet()
        .forEach(
            type -> {
              Set<AllocNode> objs = type2Objs.get(type);
              Set<SootMethod> methods = new HashSet<>();
              for (AllocNode obj : objs) {
                Set<SootMethod> invokedMethods = objToInvokedMethods.get(obj);
                if (invokedMethods != null) {
                  methods.addAll(invokedMethods);
                }
              }

              for (AllocNode allocatee : oag.getAllocateesOf(type)) {
                Set<SootMethod> invokedMethods = objToInvokedMethods.get(allocatee);
                if (invokedMethods != null) {
                  methods.addAll(invokedMethods);
                }
              }
              type2PCEMethods.putAll(type, canonicalizer.get(methods));
            });
  }

  private MultiMap<AllocNode, SootMethod> buildObjAndInvokeToCallee() {
    MultiMap<AllocNode, SootMethod> objToInvokedMethods = new ConcurrentMultiMap<>();
    var pag = pta.getPag();
    OnFlyCallGraph callgraph = pta.getCallGraph();
    // collect virtual callsites.
    Set<VirtualCallSite> vcallsites = new HashSet<>();
    for (Edge edge : callgraph) {
      SootMethod tgtM = edge.tgt();
      if (tgtM.isStatic()) {
        continue;
      }
      final InvokableStmt s = edge.srcStmt();
      AbstractInvokeExpr ie = s.getInvokeExpr().get();
      if (ie instanceof AbstractInstanceInvokeExpr iie) {
        LocalVarNode receiver = (LocalVarNode) pag.findValNode(iie.getBase(), edge.src());
        MethodSubSignature subSig = iie.getMethodSignature().getSubSignature();
        VirtualCallSite virtualCallSite =
            new VirtualCallSite(receiver, s, edge.getSrc(), iie, subSig, Edge.ieToKind(iie));
        vcallsites.add(virtualCallSite);
      } else {
        throw new RuntimeException("ie could not be of " + ie.getClass());
      }
    }
    vcallsites.forEach(
        vcallsite -> {
          // foreach virtualcallsite, we build mapping from their receiver objects.
          AbstractInstanceInvokeExpr iie = vcallsite.iie();
          LocalVarNode receiver =
              (LocalVarNode) pag.findValNode(iie.getBase(), vcallsite.container().method());
          for (AllocNode heap : pta.reachingObjects(receiver).toCIPointsToSet().toCollection()) {
            if (heap.getType() instanceof ArrayType) continue;
            QueueReader<SootMethod> reader = pta.getCgb().dispatch(heap.getType(), vcallsite);
            while (reader.hasNext()) {
              SootMethod calleeM = reader.next();
              if (calleeM.isStatic()) {
                throw new RuntimeException("Should not be static.");
              }
              objToInvokedMethods.put(heap, calleeM);
              for (SootMethod staticCallee : getAllStaticCalleesOf(calleeM)) {
                objToInvokedMethods.put(heap, staticCallee);
              }
            }
          }
        });

    return objToInvokedMethods;
  }

  private Set<SootMethod> getAllStaticCalleesOf(SootMethod method) {
    Set<SootMethod> result = new HashSet<>();
    Set<SootMethod> visited = new HashSet<>();
    Set<SootMethod> worklist = new HashSet<>();
    worklist.add(method);
    while (!worklist.isEmpty()) {
      SootMethod m = worklist.iterator().next();
      worklist.remove(m);
      if (visited.contains(m)) continue;
      visited.add(m);
      result.add(m);
      for (SootMethod callee : pta.getScene().getCallDetails().getCalleesOf(m)) {
        if (callee.isStatic() && !visited.contains(callee)) {
          worklist.add(callee);
        }
      }
    }
    return result;
  }

  public Set<SootMethod> PCEMethodsOf(final Type type) {
    return this.type2PCEMethods.get(type);
  }

  public Set<SootMethod> methodsInvokedOn(final AllocNode obj) {
    return this.objToInvokedMethods.get(obj);
  }
}
