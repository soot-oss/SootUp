package de.upb.swt.soot.callgraph.spark.pointsto;

import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.types.Type;

import java.util.Collections;
import java.util.Set;

public class EmptyPointsToSet implements PointsToSet{
    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean hasNonEmptyIntersection(PointsToSet other) {
        return false;
    }

    @Override
    public Set<Type> possibleTypes() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> possibleStringConstants() {
        return Collections.emptySet();
    }

    @Override
    public Set<ClassConstant> possibleClassConstants() {
        return Collections.emptySet();
    }
}
