package de.upb.soot.frontends;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 *
 * @author Linghui Luo
 *
 */
@Category(Java8Test.class)
public class ExampleTest {

  @Test
  public void test1()
  {
    String srcDir
        = "src/test/resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    String[] args = { srcDir, exclusionFilePath };
    // Example.main(args);
  }

  @Ignore
  public void test2() {
    String srcDir = "src/test/resources/java-target/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    String[] args = { srcDir, exclusionFilePath };
    // Example.main(args);
  }

}
