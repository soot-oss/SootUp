package sootup.tests;

import categories.Java8Test;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class SimpleSootClientTest {

  @Test
  public void test1() {
    String srcDir = "../shared-test-resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    String[] args = {srcDir, exclusionFilePath};
    // FIXME need to check later why WALA crashed
    // SimpleSootClient.main(args);
  }

  @Ignore
  public void test2() {
    String srcDir = "../shared-test-resources/java-target/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    String[] args = {srcDir, exclusionFilePath};
    // FIXME need to check later why WALA crashed
    //  SimpleSootClient.main(args);
  }
}
