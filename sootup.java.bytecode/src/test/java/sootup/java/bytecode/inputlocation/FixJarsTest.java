package sootup.java.bytecode.inputlocation;

import categories.TestCategories;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class FixJarsTest extends BaseFixJarsTest {

  @Test
  public void executeFailedJars() {
    List<JarFailureRecord> records = getRecords();
    records.forEach(
        jarFailureRecord ->
            assertMethodConversion(
                jarFailureRecord.getFailedMethodSignature(), jarFailureRecord.getJarName()));
  }
}
