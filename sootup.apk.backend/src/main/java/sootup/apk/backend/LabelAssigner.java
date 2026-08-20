package sootup.apk.backend;

import java.util.*;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.AbstractPayload;
import sootup.core.jimple.common.stmt.Stmt;

public class LabelAssigner {

  private static final Logger log = LoggerFactory.getLogger(LabelAssigner.class);

  private int lastLabelId = 0;

  private final Map<Object, Label> labelMapBeforeStmt = new HashMap<>();
  private final Map<Object, Label> labelMapAfterStmt = new HashMap<>();
  private final Map<Object, String> labelNameMapBeforeStmt = new HashMap<>();
  private final Map<Object, String> labelNameMapAfterStmt = new HashMap<>();
  private final List<String> addedLabelsBeforeStmt = new ArrayList<>();
  private final List<String> addedLabelsAfterStmt = new ArrayList<>();
  private final MethodImplementationBuilder methodImplementationBuilder;

  public LabelAssigner(MethodImplementationBuilder methodImplementationBuilder) {
    this.methodImplementationBuilder = methodImplementationBuilder;
  }

  public Label getOrCreateLabel(Stmt stmt) {
    if (stmt == null) {
      throw new RuntimeException("Statement is null");
    }
    log.info("get label at stmt {}", stmt);
    return getOrCreateLabelObject(stmt);
  }

  public Label getOrCreateLabelAfterStmt(Stmt stmt) {
    if (stmt == null) {
      throw new RuntimeException("Statement is null");
    }
    log.info("get label after stmt {}", stmt);
    Label label = labelMapAfterStmt.get(stmt);
    if (label == null) {
      String labelName = "l" + lastLabelId++;
      log.info("Reserve new label {}", labelName);
      label = methodImplementationBuilder.getLabel(labelName);
      labelMapAfterStmt.put(stmt, label);
      labelNameMapAfterStmt.put(stmt, labelName);
    }
    return label;
  }

  public Label getOrCreateLabel(AbstractPayload payload) {
    if (payload == null) {
      throw new RuntimeException("Payload is null");
    }
    return getOrCreateLabelObject(payload);
  }

  private Label getOrCreateLabelObject(Object object) {
    Label label = labelMapBeforeStmt.get(object);
    if (label == null) {
      String labelName = "l" + lastLabelId++;
      log.info("Reserve new label {}", labelName);
      label = methodImplementationBuilder.getLabel(labelName);
      labelMapBeforeStmt.put(object, label);
      labelNameMapBeforeStmt.put(object, labelName);
    }
    return label;
  }

  public String getLabelName(Object object) {
    return labelNameMapBeforeStmt.get(object);
  }

  public boolean hasLabel(Stmt stmt) {
    log.info("Test start label for stmt: {}", stmt);
    return labelMapBeforeStmt.containsKey(stmt);
  }

  public boolean hasLabelAfterStmt(Stmt stmt) {
    log.info("Test end label for stmt: {}", stmt);
    return labelMapAfterStmt.containsKey(stmt);
  }

  public void setLabel(Object object) {
    log.info("Set label at {}", object);
    if (labelMapBeforeStmt.containsKey(object)
        && !addedLabelsBeforeStmt.contains(labelNameMapBeforeStmt.get(object))) {
      String labelName = labelNameMapBeforeStmt.get(object);
      log.info(":{}", labelName);
      methodImplementationBuilder.addLabel(labelName);
      addedLabelsBeforeStmt.add(labelName);
    }
  }

  public void setLabelAfterStmt(Object object) {
    log.info("Set label after {}", object);
    if (labelMapAfterStmt.containsKey(object)
        && !addedLabelsAfterStmt.contains(labelNameMapAfterStmt.get(object))) {
      String labelName = labelNameMapAfterStmt.get(object);
      log.info(":{}", labelName);
      methodImplementationBuilder.addLabel(labelName);
      addedLabelsAfterStmt.add(labelName);
    }
  }

  public boolean areLabelsNotYetPlaced() {
    Set<String> labelNames = new HashSet<>(labelNameMapBeforeStmt.values());
    labelNames.addAll(labelNameMapAfterStmt.values());
    boolean labelsNotPlaced = false;
    for (String label : labelNames) {
      if (!addedLabelsBeforeStmt.contains(label) && !addedLabelsAfterStmt.contains(label)) {
        log.info("Unset label: {}", label);
        labelsNotPlaced = true;
      }
    }
    return labelsNotPlaced;
  }
}
