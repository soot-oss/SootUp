package sootup.tests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Linghui Luo
 */
public class SimpleSootClientTest {

  @Test
  public void test1() {
    String srcDir = "../shared-test-resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    String[] args = {srcDir, exclusionFilePath};
    // FIXME need to check later why WALA crashed
    // SimpleSootClient.main(args);
  }

  @Disabled
  public void test2() {
    String srcDir = "../shared-test-resources/java-target/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    String[] args = {srcDir, exclusionFilePath};
    // FIXME need to check later why WALA crashed
    //  SimpleSootClient.main(args);
  }
}
