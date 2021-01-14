package de.upb.swt.soot.callgraph.spark.builder;

import de.upb.swt.soot.callgraph.CallGraph;

public class PointsToAnalysisBuilder {

    private ContextSensitivity contextSensitivity = ContextSensitivity.CONTEXT_INSENSITIVE;
    private FieldSensitivity fieldSensitivity = FieldSensitivity.FIELD_INSENSITIVE;
    private boolean ignoreTypes = false;
    private boolean useTypesForSites = false;
    private boolean mergeStringBuffer = true;
    private boolean makeSimpleEdgesBidirectional = false;
    private boolean computeCallGraphOnTheFly = true;



    // TODO: [kk] consider a simplifier interface, to switch between implementations instead of these flags
    private boolean simplifySingleEntrySubgraphs = false;
    private boolean simplifyStronglyConnectedComponents = false;
    private boolean ignoreTypesForStronglyConnectedComponents = false;

    // TODO: [kk] define propagator interface and set concrete propagator

    //VTA: Setting VTA to true has the effect of setting:
    // - field-based,
    // - types-for-sites,
    // - simplify-sccs to true,
    // - on-fly-cg to false, to simulate Variable Type Analysis,

    //RTA: Setting RTA to true sets
    // - types-for-sites to true,
    // - causes Spark to use a single points-to set for all variables

    CallGraph callGraph;



}
