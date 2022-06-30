package de.upb.swt.soot.core.views;

import de.upb.swt.soot.core.ViewChangeListener;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;

import java.util.LinkedList;
import java.util.List;

public interface MutableView {

    void addChangeListener(ViewChangeListener listener);

    void removeChangeListener(ViewChangeListener listener);
}
