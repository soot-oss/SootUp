package de.upb.swt.soot.jimple.parser;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.jimple.JimpleParser;
import java.nio.file.Paths;
import java.util.Collections;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.junit.Test;

public class JimpleConverterTest {

  SootClass<?> checkJimpleClass(CharStream cs) throws ResolveException {

    JimpleConverter jimpleVisitor = new JimpleConverter();
    final OverridingClassSource scs = jimpleVisitor.run(cs, null, Paths.get(""));
    final SootClass<?> sc = new SootClass<>(scs, SourceType.Application);

    /* print for debugging:
    StringWriter output = new StringWriter();
    Printer p = new Printer();
    p.printTo(sc, new PrintWriter(output));
    System.out.println(output);
    */

    return sc;
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
  public void testValidTolerantDuplicateImports() {
    CharStream cs =
        CharStreams.fromString(
            "import Small.Table; \n"
                + "import Small.Table; \n"
                + "public class BigTable extends Table \n { public void <init>(){}"
                + "private void another(){}  } ");
    checkJimpleClass(cs);
  }

  @Test(expected = ResolveException.class)
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
  public void parseClassWField() {
    CharStream cs =
        CharStreams.fromString(
            "public class StaticFieldClass extends java.lang.Object \n  { static bool globalCounter;  } ");
    checkJimpleClass(cs);

    CharStream cs1 =
        CharStreams.fromString(
            "public class StaticFieldClass extends java.lang.Object \n  { bool $flag;  } ");
    checkJimpleClass(cs1);

    CharStream cs2 =
        CharStreams.fromString(
            "public class StaticFieldClass extends java.lang.Object \n  { java.lang.String globalCounter;  } ");
    checkJimpleClass(cs2);

    CharStream cs3 =
        CharStreams.fromString(
            "public class StaticFieldClass extends java.lang.Object \n  { int globalCounter;  } ");
    checkJimpleClass(cs3);
  }

  @Test
  public void parseClassWFields() {
    CharStream cs =
        CharStreams.fromString(
            "public class InstanceField extends java.lang.Object\n"
                + " {     public int globalCounter;\n long sth;} ");
    checkJimpleClass(cs);
  }

  @Test(expected = ResolveException.class)
  public void parseDuplicateFields() {
    CharStream cs =
        CharStreams.fromString(
            "public class DuplicateField extends java.lang.Object\n"
                + " {     public int globalCounter; public int globalCounter;} ");
    checkJimpleClass(cs);
  }

  @Test(expected = ResolveException.class)
  public void parseDuplicateFieldsDiffType() {
    CharStream cs =
        CharStreams.fromString(
            "public class DuplicateField extends java.lang.Object\n"
                + " {     public int globalCounter; bool globalCounter;} ");
    checkJimpleClass(cs);
  }

  @Test
  public void parseClassMethodWOBody() {
    CharStream cs =
        CharStreams.fromString(
            "public class Noclass extends java.lang.Object \n { public abstract void nobody();  } ");
    checkJimpleClass(cs);

    CharStream cs2 =
        CharStreams.fromString(
            "public class Noclass extends java.lang.Object \n { public native void withoutbody();  } ");
    checkJimpleClass(cs2);
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
  public void parseMethodWithParameter() {
    CharStream cs1 =
        CharStreams.fromString(
            "public class Param extends java.lang.Object \n { public void <init>(java.lang.String){} \n void another(int, float, double, bool, java.lang.String){}  } ");
    checkJimpleClass(cs1);
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
  public void testNonGreeedyCommentEverywhere() {
    CharStream cs =
        CharStreams.fromString(
            "public class BigTable extends Table \n {"
                + " public void <init>(){} \n"
                + "/* FirstComment */"
                + "private void another(){} \n"
                + "/* SecondComment */"
                + "} \n");

    SootClass<?> sc = checkJimpleClass(cs);
    assertTrue(
        sc.getMethod(
                new MethodSubSignature("another", Collections.emptyList(), VoidType.getInstance()))
            .isPresent());
  }

  @Test
  public void testLongCommentEverywhere() {
    CharStream cs =
        CharStreams.fromString(
            "/*One*/ \n"
                + "/* Another opening /* \n */"
                + "/* One*/ \n"
                + "import /*Comment*//*more */Medium.Table; \n"
                + "/* \n Two */"
                + "public /* Crumble*/ class \n"
                + "/* aclassdestroysit */ \n" // this one breaks it
                + " BigTable extends Table"
                + " \n \n \n {"
                + "// line comment foobar \n"
                //       + "/*\n  Three \n \n */ "
                + "public void <init>(){}"
                + "private void another(){}  "
                + "} ");
    checkJimpleClass(cs);
  }

  @Test(expected = ResolveException.class)
  public void testLongCommentDisruptingToken() {
    CharStream cs =
        CharStreams.fromString(
            "public cla/* DISRUPT */ss\n"
                + " BigTable extends Table"
                + " \n {"
                + "public void <init>(){}"
                + "} ");
    checkJimpleClass(cs);
  }

  @Test(expected = Exception.class)
  public void testLongCommentDisruptingTokenWord() {
    CharStream cs =
        CharStreams.fromString(
            "public cla/*DIRSUPT*/ss "
                + " BigTable extends Table \n {"
                + "public void <init>(){}"
                + "} ");
    checkJimpleClass(cs);
  }

  @Test
  public void testGreedyStringFix() {
    CharStream cs =
        CharStreams.fromString(
            "class de.upb.soot.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "    public java.lang.String j;\n"
                + "    void <init>()\n"
                + "    {\n"
                + "      de.upb.soot.concrete.fieldReference.A r0;\n"
                + "      r0 := @this: de.upb.soot.concrete.fieldReference.A; \n"
                + "      r0.<de.upb.soot.concrete.fieldReference.A: java.lang.String j> = \"something \"; \n"
                + "      r0.<de.upb.soot.concrete.fieldReference.A: java.lang.String j> = \"stupid\"; \n"
                + "      return;\n"
                + "    }\n"
                + "  }\n"
                + "\n");

    checkJimpleClass(cs);
  }

  @Test
  public void testCommentInStringConstant() {
    CharStream cs =
        CharStreams.fromString(
            "class de.upb.soot.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "    public java.lang.String j;\n"
                + "    void <init>()\n"
                + "    {\n//nonsense \n"
                + "      de.upb.soot.concrete.fieldReference.A r0;// nonsense\n"
                + "      r0 := @this: de.upb.soot.concrete.fieldReference.A; \n"
                + "      r0.<de.upb.soot.concrete.fieldReference.A: java.lang.String j> = \"something \"; \n"
                + "      r0.<de.upb.soot.concrete.fieldReference.A: java.lang.String j> = \"stupid\"; \n"
                + "      return;//nonsense\n"
                + "    }\n"
                + "  }\n"
                + "\n");

    checkJimpleClass(cs);

    CharStream cs1 =
        CharStreams.fromString(
            "class de.upb.soot.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "    void <init>()\n"
                + "    {\n"
                + "      de.upb.soot.concrete.fieldReference.A r0;\n"
                + "      r0 := @this: de.upb.soot.concrete.fieldReference.A;\n"
                + "      r0.<de.upb.soot.concrete.fieldReference.A: java.lang.String j> = \"gre/*blabla*/ater\";\n"
                + "      return;\n"
                + "    }\n"
                + "  }\n"
                + "\n");
    checkJimpleClass(cs1);
  }

  @Test
  public void testInvoke() {
    CharStream cs =
        CharStreams.fromString(
            "class Invoke{"
                + "public void <init>()\n"
                + "    {\n"
                + "      de.upb.soot.instructions.expr.DynamicInvokeExprTest r0;\n"
                + "      r0 := @this: de.upb.soot.instructions.expr.DynamicInvokeExprTest;\n"
                + "      specialinvoke r0.<java.lang.Object: void <init>()>();\n"
                + "      return;\n"
                + "    }"
                + "}");
    checkJimpleClass(cs);
  }

  @Test
  public void testOther() {

    CharStream cs =
        CharStreams.fromString(
            "class de.upb.soot.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "  static int it;\n"
                + "    public java.lang.String j;\n"
                + "    void <init>()\n"
                + "    {\n"
                + "      de.upb.soot.concrete.fieldReference.A r0;\n"
                + "      r0 := @this: de.upb.soot.concrete.fieldReference.A;\n"
                + "      specialinvoke r0.<java.lang.Object: void <init>()>();\n"
                + "      r0.<de.upb.soot.concrete.fieldReference.A: int i> = 15;\n"
                + "      r0.<de.upb.soot.concrete.fieldReference.A: java.lang.String j> = \"greater\";\n"
                + "      return;\n"
                + "    }\n"
                + "  }\n"
                + "\n");
    checkJimpleClass(cs);
  }

  @Test
  public void testOuterclass() {
    CharStream cs =
        CharStreams.fromString("class OuterClass$InnerClass{" + "public void <init>(){}" + "}");
    checkJimpleClass(cs);
  }

  @Test
  public void testParsingEscapedIdentifiers() {

    // keyword
    {
      try {
        final CodePointCharStream charStream = CharStreams.fromString("class");
        final JimpleParser parser =
            JimpleConverterUtil.createJimpleParser(
                charStream, Paths.get("InputFromString.doesNotExists"));
        assertEquals("class", parser.identifier().getText());
        fail("class can not be an unescaped identifier");
      } catch (ResolveException ignored) {
      }
    }

    {
      final CodePointCharStream charStream = CharStreams.fromString("Banana.class.AClass");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertEquals("Banana.class.AClass", parser.identifier().getText());
    }
    {
      final CodePointCharStream charStream = CharStreams.fromString("Banana.\"class\".AClass");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertEquals("Banana.\"class\".AClass", parser.identifier().getText());
    }
    {
      final CodePointCharStream charStream = CharStreams.fromString("\"Banana.class.AClass\"");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertEquals("\"Banana.class.AClass\"", parser.identifier().getText());
    }

    {
      final CodePointCharStream charStream = CharStreams.fromString("\"Banana. class .AClass\"");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertEquals("\"Banana. class .AClass\"", parser.identifier().getText());
    }
    {
      final CodePointCharStream charStream = CharStreams.fromString("Banana. class .AClass");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertNotEquals("Banana. class .AClass", parser.identifier().getText());
    }

    // escapechar
    {
      final CodePointCharStream charStream = CharStreams.fromString("Banana.\\\"AClass");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertEquals("Banana.\\\"AClass", parser.identifier().getText());
    }

    {
      final CodePointCharStream charStream = CharStreams.fromString("Banana.\\\\AClass");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertEquals("Banana.\\\\AClass", parser.identifier().getText());
    }
    // unnecessary escaping -> problem -> parser is not tolerant to that
    {
      final CodePointCharStream charStream = CharStreams.fromString("Banana\\AClass");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertNotEquals("Banana\\AClass", parser.identifier().getText());
    }

    final CodePointCharStream charStream = CharStreams.fromString("Banana");
    final JimpleParser parser =
        JimpleConverterUtil.createJimpleParser(
            charStream, Paths.get("InputFromString.doesNotExists"));
    assertEquals("Banana", parser.identifier().getText());
  }

  @Test
  public void testEscaping() {
    assertEquals("αρετη", Jimple.unescape("\\u03b1\\u03c1\\u03b5\\u03c4\\u03b7"));

    assertEquals("a", Jimple.escape("a"));
    assertEquals("name$morename", Jimple.escape("name$morename"));
    assertEquals("$i0", Jimple.escape("$i0"));

    // keywords
    assertEquals("\"class\"", Jimple.escape("class"));
    assertEquals("\"throws\"", Jimple.escape("throws"));

    // unescape from old soot too (escaped package names partially)
    assertEquals("java.annotation.something", Jimple.unescape("java.\"annotation\".something"));
    assertEquals("java.annotation.something", Jimple.unescape("\"java\".\"annotation\".something"));
    assertEquals("java.annotation.something", Jimple.unescape("java.\"annotation\".something"));

    // "normal" escaping / unescaping of a single item
    assertEquals(
        "stringWithEscaped\"something", Jimple.unescape("\"stringWithEscaped\\\"something\""));
    assertEquals("java.annotation.something", Jimple.unescape("\"java.\"annotation\".something\""));

    assertNotEquals(
        "stringWithEscaped\\\"something", Jimple.unescape("\"stringWithEscaped\"something\""));
    assertNotEquals(
        "stringWithEscaped\\\"something", Jimple.unescape("\"stringWithEscaped\"something"));
    assertEquals("stringWithEscaped\"something", Jimple.unescape("stringWithEscaped\\\"something"));

    // from: usual string constant assignment
    assertEquals("usual string", Jimple.unescape("\"usual string\""));
  }

  @Test
  public void testSingleQuoteEscapeSeq() {
    CharStream cs =
        CharStreams.fromString(
            "public annotation interface android.support.'annotation'.SomeAnnotation extends java.lang.Object implements java.lang.'annotation'.Annotation\n {}");
    checkJimpleClass(cs);
  }

  @Test(expected = RuntimeException.class)
  public void testWholefile() {
    CharStream cs =
        CharStreams.fromString(
            "import Medium.Table; \n"
                + "public class BigTable extends Table \n {"
                + " public void <init>(){} \n"
                + "private void another(){} \n"
                + "} \n"
                + "bla bla \n"
                + "\n");
    checkJimpleClass(cs);
  }

  @Test(expected = RuntimeException.class)
  public void testFileMultipleClasses() {
    CharStream cs =
        CharStreams.fromString(
            "import Medium.Table; \n"
                + "public class BigTable extends Table \n {"
                + " public void <init>(){} \n"
                + "private void another(){} \n"
                + "} \n"
                + "public class AnotherTable extends Table \n {"
                + " public void <init>(){} \n"
                + "private void anotherChair(){} \n"
                + "} \n");
    checkJimpleClass(cs);
  }
}
