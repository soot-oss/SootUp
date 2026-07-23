package sootup.apk.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.jimple.common.stmt.Stmt;

public class LabelAssigner {

  private static final Logger log = LoggerFactory.getLogger(LabelAssigner.class);

  private int lastLabelId = 0;

  private final Map<Stmt, Label> labelMap = new HashMap<>();
  private final Map<Stmt, String> labelNameMap = new HashMap<>();
  private final List<String> addedLabels = new ArrayList<>();
  private final MethodImplementationBuilder methodImplementationBuilder;

  public LabelAssigner(MethodImplementationBuilder methodImplementationBuilder) {
    this.methodImplementationBuilder = methodImplementationBuilder;
  }

  public Label getOrCreateLabel(Stmt stmt) {
    if (stmt == null) {
      throw new RuntimeException("Statement is null");
    }

    Label label = labelMap.get(stmt);
    if (label == null) {
      String labelName = "l" + lastLabelId++;
      label = methodImplementationBuilder.getLabel(labelName);
      labelMap.put(stmt, label);
      labelNameMap.put(stmt, labelName);
    }
    return label;
  }

  public boolean hasLabel(Stmt stmt) {
    return labelMap.containsKey(stmt);
  }

  public void addLabel(Stmt stmt) {
    if (labelMap.containsKey(stmt) && !addedLabels.contains(labelNameMap.get(stmt))) {
      String labelName = labelNameMap.get(stmt);
      log.info(":{}", labelName);
      methodImplementationBuilder.addLabel(labelName);
      addedLabels.add(labelName);
    }
  }
}
