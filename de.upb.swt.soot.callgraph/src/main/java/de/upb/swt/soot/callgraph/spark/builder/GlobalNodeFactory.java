package de.upb.swt.soot.callgraph.spark.builder;

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.ArrayElement;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.callgraph.spark.pag.nodes.VariableNode;
import de.upb.swt.soot.callgraph.spark.pointsto.PointsToAnalysis;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;

import java.awt.*;

public class GlobalNodeFactory {

    private PointerAssignmentGraph pag;

    protected final ReferenceType rtObject;
    protected final ReferenceType rtClassLoader;
    protected final ReferenceType rtString;
    protected final ReferenceType rtThread;
    protected final ReferenceType rtThreadGroup;
    protected final ReferenceType rtThrowable;
    protected final ReferenceType rtPrivilegedActionException;

    public GlobalNodeFactory(PointerAssignmentGraph pag){
        this.pag = pag;

        JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
        rtObject = new JavaClassType("Object", identifierFactory.getPackageName("java.lang"));
        this.rtClassLoader = new JavaClassType("ClassLoader", identifierFactory.getPackageName("java.lang"));
        this.rtString = new JavaClassType("String", identifierFactory.getPackageName("java.lang"));
        this.rtThread = new JavaClassType("Thread", identifierFactory.getPackageName("java.lang"));
        this.rtThreadGroup = new JavaClassType("ThreadGroup", identifierFactory.getPackageName("java.lang"));
        this.rtThrowable = new JavaClassType("Throwable", identifierFactory.getPackageName("java.lang.Throwable"));
        this.rtPrivilegedActionException = new JavaClassType("PrivilegedActionException",identifierFactory.getPackageName("java.security"));
    }

    public Node caseDefaultClassLoader(){
        AllocationNode alloc = pag.getOrCreateAllocationNode(PointsToAnalysis.DEFAULT_CLASS_LOADER, rtClassLoader, null); // or rtObject
        VariableNode var = pag.getOrCreateGlobalVariableNode(PointsToAnalysis.DEFAULT_CLASS_LOADER_LOCAL, rtClassLoader);
        pag.addEdge(alloc, var);
        return var;
    }

    public Node caseMainClassNameString(){
        AllocationNode alloc = pag.getOrCreateAllocationNode(PointsToAnalysis.MAIN_CLASS_NAME_STRING, rtString, null);
        VariableNode var = pag.getOrCreateGlobalVariableNode(PointsToAnalysis.MAIN_CLASS_NAME_STRING_LOCAL, rtString);
        pag.addEdge(alloc, var);
        return var;
    }

    public Node caseMainThreadGroup(){
        AllocationNode alloc = pag.getOrCreateAllocationNode(PointsToAnalysis.MAIN_THREAD_GROUP_NODE, rtThreadGroup, null);
        VariableNode var = pag.getOrCreateGlobalVariableNode(PointsToAnalysis.MAIN_THREAD_GROUP_NODE_LOCAL, rtThreadGroup);
        pag.addEdge(alloc, var);
        return var;
    }

    public Node casePrivilegedActionException(){
        AllocationNode alloc = pag.getOrCreateAllocationNode(PointsToAnalysis.PRIVILEGED_ACTION_EXCEPTION, rtPrivilegedActionException, null);
        VariableNode var = pag.getOrCreateGlobalVariableNode(PointsToAnalysis.PRIVILEGED_ACTION_EXCEPTION_LOCAL, rtPrivilegedActionException);
        pag.addEdge(alloc, var);
        return var;
    }

    public Node caseCanonicalPath(){
        AllocationNode alloc = pag.getOrCreateAllocationNode(PointsToAnalysis.CANONICAL_PATH, rtString, null);
        VariableNode var = pag.getOrCreateGlobalVariableNode(PointsToAnalysis.CANONICAL_PATH_LOCAL, rtString);
        pag.addEdge(alloc, var);
        return var;
    }

    public Node caseMainThread(){
        AllocationNode alloc = pag.getOrCreateAllocationNode(PointsToAnalysis.MAIN_THREAD_NODE, rtThread, null);
        VariableNode var = pag.getOrCreateGlobalVariableNode(PointsToAnalysis.MAIN_THREAD_NODE_LOCAL, rtThread);
        pag.addEdge(alloc, var);
        return var;
    }

    public Node caseFinalizeQueue(){
        return pag.getOrCreateGlobalVariableNode(PointsToAnalysis.FINALIZE_QUEUE, rtObject);
    }

    public Node caseArgv(){
        ArrayType strArray = new ArrayType(rtString, 1);
        AllocationNode argv = pag.getOrCreateAllocationNode(PointsToAnalysis.STRING_ARRAY_NODE, strArray, null);
        VariableNode sanl = pag.getOrCreateGlobalVariableNode(PointsToAnalysis.STRING_ARRAY_NODE_LOCAL, strArray);
        AllocationNode stringNode = pag.getOrCreateAllocationNode(PointsToAnalysis.STRING_NODE, rtString, null);
        VariableNode stringNodeLocal = pag.getOrCreateGlobalVariableNode(PointsToAnalysis.STRING_ARRAY_NODE_LOCAL, rtString);
        pag.addEdge(argv, sanl);
        pag.addEdge(stringNode, stringNodeLocal);
        pag.addEdge(stringNodeLocal, pag.getOrCreateFieldReferenceNode(sanl, new ArrayElement()));
        return sanl;
    }

    public Node caseNewInstance(VariableNode cls){
        // TODO: do we need to handle ContextVarNode?
        // TODO: dynamicClasses
        VariableNode node = pag.getOrCreateGlobalVariableNode(cls, rtObject);
        return node;
    }

    public Node caseThrow(){
        VariableNode node = pag.getOrCreateGlobalVariableNode(PointsToAnalysis.EXCEPTION_NODE, rtThrowable);
        // TODO: setInterProcTarget, Source
        return node;
    }

}
