package de.upb.swt.soot;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.util.printer.Printer;
import java.io.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;

public class JimpleVisitorImplTest {

  void checkJimpleClass(CharStream cs) {

    JimpleVisitorImpl parser = new JimpleVisitorImpl();
    final SootClassSource scs = parser.parse(cs);

    StringWriter output = new StringWriter();
    Printer p = new Printer();
    final SootClass sc = new SootClass(scs, SourceType.Application);
    p.printTo(sc, new PrintWriter(output));

    System.out.println(output);

    /*

    final List<SootDiff.DiffItem> diff = SootDiff.diff(sc, );
    if( !diff.isEmpty()){
      SootDiff.print(diff);
    }
    assertTrue(diff.isEmpty());
    */
  }

  @Test
  public void parseMinimalClass() {

    CharStream cs = CharStreams.fromString("class MinClass \n { }");
    checkJimpleClass(cs);
  }

  @Test
  public void parseEmptyClass() {
    CharStream cs =
        CharStreams.fromString(
            "public class EmptyClass extends java.lang.Object\n" + " { " + " \n " + "  " + "} ");
    checkJimpleClass(cs);
  }

  @Test
  public void parseClassWField() {
    CharStream cs =
        CharStreams.fromString(
            "public class EmptyClass extends java.lang.Object\n"
                + " {      static int globalCounter;\n } ");
    checkJimpleClass(cs);
  }

  @Test
  public void parseClassWFields() {
    CharStream cs =
        CharStreams.fromString(
            "public class EmptyClass extends java.lang.Object\n"
                + " {     static int globalCounter;\n long sth;} ");
    checkJimpleClass(cs);
  }

  @Test
  public void parseClassWShortMethod() {
    CharStream cs =
        CharStreams.fromString(
            "public class Noclass extends java.lang.Object \n { public void nobody();  } ");
    checkJimpleClass(cs);
  }

  @Test
  public void parseClassWMethod() {
    CharStream cs =
        CharStreams.fromString(
            "public class EmptyClass extends java.lang.Object \n { public void <init>(){}  } ");
    checkJimpleClass(cs);
  }

  @Test
  public void parseClassWMethods() {
    CharStream cs =
        CharStreams.fromString(
            "public class EmptyClass extends java.lang.Object \n { public void <init>(){}"
                + "private void another(){}  } ");

    checkJimpleClass(cs);
  }

  @Test
  public void parseInterleavingClassMembers() {
    CharStream cs1 =
        CharStreams.fromString(
            "public class Interleaving1 extends java.lang.Object \n { public void <init>(){} static int globalCounter; protected void another(){}  } ");
    checkJimpleClass(cs1);

    CharStream cs2 =
        CharStreams.fromString(
            "public class Interleaving2 extends java.lang.Object \n { private bool flag; void <init>(){} static int globalCounter; protected void another(){}  } ");
    checkJimpleClass(cs2);
  }

  @Test
  public void parseClassImplements() {
    CharStream cs1 =
        CharStreams.fromString(
            "public class Developer implements human.interaction.devices.Typing \n { public void <init>(){}"
                + "private void another(){}  } ");
    checkJimpleClass(cs1);

    CharStream cs2 =
        CharStreams.fromString(
            "public class Developer implements human.interaction.devices.Typing, human.system.KeepAwake \n { public void <init>(){} } ");
    checkJimpleClass(cs2);
  }

  @Test
  public void parseClassExtends() {
    CharStream cs =
        CharStreams.fromString(
            "public class BigTable extends Small.Table \n { public void <init>(){}"
                + "private void another(){}  } ");
    checkJimpleClass(cs);
  }

  @Test
  public void testImports() {
    CharStream cs =
        CharStreams.fromString(
            "import Small.Table; \n"
                + "import Huge.BigTable; \n"
                + "public class BigTable extends Table \n { public void <init>(){}"
                + "private void another(){}  } ");
    checkJimpleClass(cs);
  }

  @Test
  public void testValidDuplicateImports() {
    CharStream cs =
        CharStreams.fromString(
            "import Small.Table; \n"
                + "import Small.Table; \n"
                + "public class BigTable extends Table \n { public void <init>(){}"
                + "private void another(){}  } ");
    checkJimpleClass(cs);
  }

  @Test(expected = IllegalStateException.class)
  public void testInvalidDuplicateImports() {
    CharStream cs =
        CharStreams.fromString(
            "import Small.Table; \n"
                + "import Medium.Table; \n"
                + "public class BigTable extends Table \n { public void <init>(){}"
                + "private void another(){}  } ");
    checkJimpleClass(cs);
  }

  @Test
  public void testSinglelineComment() {
    CharStream cs =
        CharStreams.fromString(
            "//SingleLine One \n"
                + "// SecondSingleLine \n"
                + "import Small.Table; \n"
                + "// SingleLine Two \n"
                + "public class BigTable extends Table \n {"
                + "// SingleLine One \n"
                + "public void <init>(){}"
                + "// SingleLine One \n"
                + " }"
                + "// SingleLine End");
    checkJimpleClass(cs);
  }

  @Test
  public void testLineComment() {
    CharStream cs =
        CharStreams.fromString(
            "import Medium.Table; \n"
                + "public class //Chair extends Table \n"
                + "BigTable extends Table \n {"
                + "public void <init>(){} \n"
                + "//private void another(){} \n"
                + "} ");
    checkJimpleClass(cs);
  }

  @Test
  public void testSimpleLongComment() {
    CharStream cs =
        CharStreams.fromString(
            "/* One */ /**/ /* ** * //* ** / */"
                + "import Medium.Table; \n"
                + "public class BigTable extends Table \n {"
                + " public void <init>(){} \n"
                + "private void another(){} \n"
                + "} \n");
    checkJimpleClass(cs);
  }

  @Test
  public void testLongCommentEverywhere() {
    CharStream cs =
        CharStreams.fromString(
            "/*One*/ \n"
                + "/*One */ \n"
                + "/* One*/ \n"
                + "import /*Comment*//*more */Medium.Table; \n"
                + "/* \n Two */"
                + "public /* Crumble*/ class  BigTable extends Table \n {"
                // + "\n /*\n  Three \n \n */"
                + " public void <init>(){}"
                + "private void another(){}  "
                //    + "/* Another opening /* */"
                //   + "/* \n End \n */"
                + "} ");
    checkJimpleClass(cs);
  }
}
