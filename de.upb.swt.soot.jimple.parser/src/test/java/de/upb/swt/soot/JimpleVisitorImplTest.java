package de.upb.swt.soot;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.frontend.SootClassSource;
import java.io.IOException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;

public class JimpleVisitorImplTest {

  void checkJimpleClass(String filename) {
    // load from file
    JimpleVisitorImpl parser = new JimpleVisitorImpl();
    CharStream charStream;
    try {
      charStream = CharStreams.fromFileName(filename);
    } catch (IOException e) {
      e.printStackTrace();
      assert (false);
      return;
    }
    final SootClassSource parse = parser.parse(charStream);

    // TODO: compare printed jimple with loaded file

  }

  @Test
  public void parse() {

    JimpleVisitorImpl parser = new JimpleVisitorImpl();
    parser.parse(
        CharStreams.fromString(
            "public class EmptyClass extends java.lang.Object\n" + " { " + " \n " + "  " + "} "));
  }
}
