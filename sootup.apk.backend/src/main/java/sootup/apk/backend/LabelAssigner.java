package sootup.apk.backend;

import java.util.*;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.AbstractPayload;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * Organizes the allocation and setting of labels LabelMapBeforeStmt: Allocates labels for if,
 * switch, goto, try-start, catch, stmts. LabelMapAfterStmt: Allocates labels for try-end stmts
 */
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

  /**
   * Reserve a label at a specific stmt
   *
   * @param stmt The stmt BEFORE a label should be set
   * @return The label created
   */
  public Label getOrCreateLabel(Stmt stmt) {
    if (stmt == null) {
      throw new RuntimeException("Statement is null");
    }
    return getOrCreateLabelObject(stmt);
  }

  /**
   * Reserve a label after a specific stmt This method is used for stmts representing the end of a
   * try block
   *
   * @param stmt The stmt AFTER a label should be set
   * @return The label created
   */
  public Label getOrCreateLabelAfterStmt(Stmt stmt) {
    if (stmt == null) {
      throw new RuntimeException("Statement is null");
    }
    Label label = labelMapAfterStmt.get(stmt);
    if (label == null) {
      String labelName = "l" + lastLabelId++;
      label = methodImplementationBuilder.getLabel(labelName);
      labelMapAfterStmt.put(stmt, label);
      labelNameMapAfterStmt.put(stmt, labelName);
    }
    return label;
  }

  /**
   * Reserve a label for a payload
   *
   * @param payload The stmt before a label should be set
   * @return The label created
   */
  public Label getOrCreateLabel(AbstractPayload payload) {
    if (payload == null) {
      throw new RuntimeException("Payload is null");
    }
    return getOrCreateLabelObject(payload);
  }

  /**
   * Reserve a label before an object(stmt/payload)
   *
   * @param object A stmt or a payload
   * @return The label created
   */
  private Label getOrCreateLabelObject(Object object) {
    Label label = labelMapBeforeStmt.get(object);
    if (label == null) {
      String labelName = "l" + lastLabelId++;
      label = methodImplementationBuilder.getLabel(labelName);
      labelMapBeforeStmt.put(object, label);
      labelNameMapBeforeStmt.put(object, labelName);
    }
    return label;
  }

  /**
   * Get the reserved label name for a stmt or payload
   *
   * @param object A stmt or a payload
   * @return The label name
   */
  public String getLabelName(Object object) {
    return labelNameMapBeforeStmt.get(object);
  }

  /** Has a label been reserved BEFORE a specific stmt */
  public boolean hasLabel(Stmt stmt) {
    return labelMapBeforeStmt.containsKey(stmt);
  }

  /** Has a label been reserved AFTER a specific stmt */
  public boolean hasLabelAfterStmt(Stmt stmt) {
    return labelMapAfterStmt.containsKey(stmt);
  }

  /**
   * Places an already reserved label at the current instruction of the methodImplementationBuilder
   *
   * @param object A stmt or a payload
   */
  public void setLabel(Object object) {
    if (labelMapBeforeStmt.containsKey(object)
        && (!addedLabelsBeforeStmt.contains(labelNameMapBeforeStmt.get(object)))) {
      String labelName = labelNameMapBeforeStmt.get(object);
      log.info(":{}", labelName);
      methodImplementationBuilder.addLabel(labelName);
      addedLabelsBeforeStmt.add(labelName);
    }
  }

  /**
   * Places an already reserved label at the current instruction of the methodImplementationBuilder
   *
   * @param object A stmt or a payload
   */
  public void setLabelAfterStmt(Object object) {
    if (labelMapAfterStmt.containsKey(object)
        && !addedLabelsAfterStmt.contains(labelNameMapAfterStmt.get(object))) {
      String labelName = labelNameMapAfterStmt.get(object);
      log.info(":{}", labelName);
      methodImplementationBuilder.addLabel(labelName);
      addedLabelsAfterStmt.add(labelName);
    }
  }

  /** Evaluates whether labels have been reserved but not set */
  public boolean areLabelsNotYetPlaced() {
    Set<String> labelNames = new HashSet<>(labelNameMapBeforeStmt.values());
    labelNames.addAll(labelNameMapAfterStmt.values());

    return labelNames.stream()
            .filter(
                label ->
                    !addedLabelsBeforeStmt.contains(label) && !addedLabelsAfterStmt.contains(label))
            .peek(label -> log.error("Label not set: {}", label))
            .count()
        > 0;
  }
}
