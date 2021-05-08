package de.upb.swt.soot.callgraph.spark.builder;

import de.upb.swt.soot.callgraph.spark.solver.Propagator;
import de.upb.swt.soot.callgraph.spark.solver.WorklistPropagator;

public class SparkOptions {

    private boolean ignoreTypes = false; // PAG
    private boolean vta = false; // SparkTransformer ContextInsensitiveBuilder MethodNodeFactory PAG
    private boolean rta = false; // PAG
    private boolean fieldBased = false; // MethodNodeFactory PAG
    private boolean typesForSites = false; // PAG
    private boolean mergeStringBuffer = true; // MethodNodeFactory
    private boolean stringConstants = false; // MethodNodeFactory
    private boolean simulateNatives = true; // ContextInsensitiveBuilder MethodPAG
    private boolean emptiesAsAllocs = false; // MethodNodeFactory
    private boolean simpleEdgesBidirectional = false; // PAG
    private boolean onFlyCFG = true; // SparkTransformer ContextInsBuilder PAG
    private boolean simplifyOffline = false; // SparkTransformer
    private boolean simplifySCCS = false; // SparkTransformer
    private boolean ignoreTypesForSCCS = false; // SparkTransormer

    // private Propagator propagator = new WorklistPropagator // SparkTransformer, PAG
    // set impl // PAG

    public boolean isIgnoreTypes() {
        return ignoreTypes;
    }

    public void setIgnoreTypes(boolean ignoreTypes) {
        this.ignoreTypes = ignoreTypes;
    }

    public boolean isVta() {
        return vta;
    }

    public void setVta(boolean vta) {
        this.vta = vta;
    }

    public boolean isRta() {
        return rta;
    }

    public void setRta(boolean rta) {
        this.rta = rta;
    }

    public boolean isFieldBased() {
        return fieldBased;
    }

    public void setFieldBased(boolean fieldBased) {
        this.fieldBased = fieldBased;
    }

    public boolean isTypesForSites() {
        return typesForSites;
    }

    public void setTypesForSites(boolean typesForSites) {
        this.typesForSites = typesForSites;
    }

    public boolean isMergeStringBuffer() {
        return mergeStringBuffer;
    }

    public void setMergeStringBuffer(boolean mergeStringBuffer) {
        this.mergeStringBuffer = mergeStringBuffer;
    }

    public boolean isStringConstants() {
        return stringConstants;
    }

    public void setStringConstants(boolean stringConstants) {
        this.stringConstants = stringConstants;
    }

    public boolean isSimulateNatives() {
        return simulateNatives;
    }

    public void setSimulateNatives(boolean simulateNatives) {
        this.simulateNatives = simulateNatives;
    }

    public boolean isEmptiesAsAllocs() {
        return emptiesAsAllocs;
    }

    public void setEmptiesAsAllocs(boolean emptiesAsAllocs) {
        this.emptiesAsAllocs = emptiesAsAllocs;
    }

    public boolean isSimpleEdgesBidirectional() {
        return simpleEdgesBidirectional;
    }

    public void setSimpleEdgesBidirectional(boolean simpleEdgesBidirectional) {
        this.simpleEdgesBidirectional = simpleEdgesBidirectional;
    }

    public boolean isOnFlyCFG() {
        return onFlyCFG;
    }

    public void setOnFlyCFG(boolean onFlyCFG) {
        this.onFlyCFG = onFlyCFG;
    }

    public boolean isSimplifyOffline() {
        return simplifyOffline;
    }

    public void setSimplifyOffline(boolean simplifyOffline) {
        this.simplifyOffline = simplifyOffline;
    }

    public boolean isSimplifySCCS() {
        return simplifySCCS;
    }

    public void setSimplifySCCS(boolean simplifySCCS) {
        this.simplifySCCS = simplifySCCS;
    }

    public boolean isIgnoreTypesForSCCS() {
        return ignoreTypesForSCCS;
    }

    public void setIgnoreTypesForSCCS(boolean ignoreTypesForSCCS) {
        this.ignoreTypesForSCCS = ignoreTypesForSCCS;
    }
}
