package sootup.apk.backend.instructions;

import java.util.List;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderArrayPayload;
import sootup.apk.backend.Register;

public class ArrayPayload extends AbstractPayload {

  private final int elementWidth;
  private final List<Number> arrayElements;

  public ArrayPayload(int elementWidth, List<Number> arrayElements) {
    super(null, null);
    this.elementWidth = elementWidth;
    this.arrayElements = arrayElements;
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    return new BuilderArrayPayload(elementWidth, arrayElements);
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {}

  @Override
  public void logSmali() {
    log.info(".array");
    log.info(".array-data {}", elementWidth);
    for (Number arrayElement : arrayElements) {
      log.info(String.format("    0x%x", arrayElement.byteValue()));
    }
    log.info(".array-data");
  }
}
