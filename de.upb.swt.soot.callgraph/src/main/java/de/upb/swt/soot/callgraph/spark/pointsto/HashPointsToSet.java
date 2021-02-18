package de.upb.swt.soot.callgraph.spark.pointsto;

import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.types.Type;

import java.util.Set;

public class HashPointsToSet implements PointsToSet{

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean hasNonEmptyIntersection(PointsToSet other) {
        return false;
    }

    @Override
    public Set<Type> possibleTypes() {
        return null;
    }

    @Override
    public Set<String> possibleStringConstants() {
        return null;
    }

    @Override
    public Set<ClassConstant> possibleClassConstants() {
        return null;
    }
}
