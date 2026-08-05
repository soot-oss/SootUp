package sootup.apk.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.SwitchPayload;
import sootup.core.jimple.common.stmt.Stmt;

public class LabelAssigner {

  private static final Logger log = LoggerFactory.getLogger(LabelAssigner.class);

  private int lastLabelId = 0;

  private final Map<Object, Label> labelMap = new HashMap<>();
  private final Map<Object, String> labelNameMap = new HashMap<>();
  private final List<String> addedLabels = new ArrayList<>();
  private final MethodImplementationBuilder methodImplementationBuilder;

  public LabelAssigner(MethodImplementationBuilder methodImplementationBuilder) {
    this.methodImplementationBuilder = methodImplementationBuilder;
  }

  public Label getOrCreateLabel(Stmt stmt) {
    if (stmt == null) {
      throw new RuntimeException("Statement is null");
    }
    return getOrCreateLabelObject(stmt);
  }

  public Label getOrCreateLabel(SwitchPayload switchPayload) {
    if (switchPayload == null) {
      throw new RuntimeException("Payload is null");
    }
    return getOrCreateLabelObject(switchPayload);
  }

  private Label getOrCreateLabelObject(Object object) {
    Label label = labelMap.get(object);
    if (label == null) {
      String labelName = "l" + lastLabelId++;
      label = methodImplementationBuilder.getLabel(labelName);
      labelMap.put(object, label);
      labelNameMap.put(object, labelName);
    }
    return label;
  }

  public String getLabelName(Object object) {
    return labelNameMap.get(object);
  }

  public boolean hasLabel(Stmt stmt) {
    return labelMap.containsKey(stmt);
  }

  public void setLabel(Object labelReference) {
    if (labelMap.containsKey(labelReference)
        && !addedLabels.contains(labelNameMap.get(labelReference))) {
      String labelName = labelNameMap.get(labelReference);
      log.info(":{}", labelName);
      methodImplementationBuilder.addLabel(labelName);
      addedLabels.add(labelName);
    }
  }
}
