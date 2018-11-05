package de.upb.soot.frontends;

import de.upb.soot.frontends.java.Example;

import org.junit.Ignore;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 *
 * @author Linghui Luo
 *
 */
@Category(Java8Test.class)
public class ExampleTest {

  @Ignore
  public void test()
  {
    String srcDir
        = "src/test/resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    String[] args = { srcDir, exclusionFilePath };
    Example.main(args);
  }

}
