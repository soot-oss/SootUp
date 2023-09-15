package qilin.pta.toolkits.debloaterx;

import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.util.PTAUtils;
import soot.*;
import soot.jimple.spark.pag.SparkField;

import java.util.*;
import java.util.stream.Collectors;

public class HeapContainerQuery {
    private final PAG pag;
    private final XUtility utility;
    private final Set<SootMethod> invokedMs;
    private final Set<LocalVarNode> params;
    private final InterFlowAnalysis interfa;
    private final AllocNode heap;


    public HeapContainerQuery(XUtility utility, AllocNode heap) {
        this.utility = utility;
        this.pag = utility.getPta().getPag();
        this.heap = heap;
        this.invokedMs = utility.getInvokedMethods(heap);
        this.interfa = utility.getInterFlowAnalysis();
        this.params = getParameters();
    }

    /* computes input parameters for the class of refType */
    private Set<LocalVarNode> getParameters() {
        Set<LocalVarNode> ret = new HashSet<>();
        for (SootMethod m : invokedMs) {
            MethodNodeFactory mthdNF = pag.getMethodPAG(m).nodeFactory();
            for (int i = 0; i < m.getParameterCount(); ++i) {
                if (m.getParameterType(i) instanceof RefLikeType && !PTAUtils.isPrimitiveArrayType(m.getParameterType(i))) {
                    LocalVarNode param = (LocalVarNode) mthdNF.caseParm(i);
                    ret.add(param);
                }
            }
            ret.add((LocalVarNode) mthdNF.caseThis());
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* APIs for query */
    public boolean hasParamsStoredInto(SparkField field) {
        Set<LocalVarNode> tmp = interfa.getParamsStoredInto(field);
        tmp = tmp.stream().filter(this.params::contains).collect(Collectors.toSet());
        return !tmp.isEmpty();
    }

    public Set<LocalVarNode> getInParamsToCSFields() {
        Set<SparkField> fields = utility.getFields(heap);
        Set<LocalVarNode> ret = new HashSet<>();
        for (SparkField field : fields) {
            if (isCSField(field)) {
                ret.addAll(interfa.getParamsStoredInto(field));
            }
        }
        ret = ret.stream().filter(this.params::contains).collect(Collectors.toSet());
        return ret;
    }

    public boolean hasOutMethodsWithRetOrParamValueFrom(SparkField field) {
        Set<SootMethod> tmp = interfa.getOutMethodsWithRetOrParamValueFrom(field);
        tmp = tmp.stream().filter(this.invokedMs::contains).collect(Collectors.toSet());
        return !tmp.isEmpty();
    }

    public boolean isCSField(SparkField field) {
        boolean hasIn = utility.hasNonThisStoreOnField(this.heap, field) || hasParamsStoredInto(field);
        boolean hasOut = utility.hasNonThisLoadFromField(this.heap, field) || hasOutMethodsWithRetOrParamValueFrom(field);
        return hasIn && hasOut;
    }
}
