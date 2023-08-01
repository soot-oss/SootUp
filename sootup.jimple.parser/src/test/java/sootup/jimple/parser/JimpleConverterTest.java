package sootup.jimple.parser;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import org.antlr.v4.runtime.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.frontend.OverridingClassSource;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.PrimitiveType;
import sootup.core.types.VoidType;
import sootup.core.util.StringTools;
import sootup.jimple.JimpleLexer;
import sootup.jimple.JimpleParser;
import sootup.jimple.parser.categories.Java8Test;

@Category(Java8Test.class)
public class JimpleConverterTest {

  private SootClass<?> parseJimpleClass(CharStream cs) throws ResolveException {
    JimpleConverter jimpleVisitor = new JimpleConverter();
    final OverridingClassSource scs =
        jimpleVisitor.run(cs, new EagerInputLocation<>(), Paths.get(""));
    return new SootClass<>(scs, SourceType.Application);
  }

  @Test
  public void parseMinimalClass() {

    CharStream cs = CharStreams.fromString("class MinClass \n { }");
    parseJimpleClass(cs);
  }

  @Test(expected = ResolveException.class)
  public void parseEmptyFile() {
    CharStream cs = CharStreams.fromString("");
    parseJimpleClass(cs);
  }

  @Test(expected = ResolveException.class)
  public void parseNonJimpleFile() {
    CharStream cs = CharStreams.fromString("Hello World!");
    parseJimpleClass(cs);
  }

  @Test
  public void parseEmptyClass() {
    CharStream cs =
        CharStreams.fromString(
            "public class EmptyClass extends java.lang.Object\n" + " { " + " \n " + "  " + "} ");
    parseJimpleClass(cs);
  }

  @Test
  public void parseClassImplements() {
    CharStream cs1 =
        CharStreams.fromString(
            "public class Developer implements human.interaction.devices.Typing \n { public void <init>(){}"
                + "private void another(){}  } ");
    parseJimpleClass(cs1);

    CharStream cs2 =
        CharStreams.fromString(
            "public class Developer implements human.interaction.devices.Typing, human.system.KeepAwake \n { public void <init>(){} } ");
    parseJimpleClass(cs2);
  }

  @Test
  public void parseClassExtends() {
    CharStream cs =
        CharStreams.fromString(
            "public class BigTable extends Small.Table \n { public void <init>(){}"
                + "private void another(){}  } ");
    parseJimpleClass(cs);
  }

  @Test
  public void testImports() {
    CharStream cs =
        CharStreams.fromString(
            "import Small.Table; \n"
                + "import Huge.BigTable; \n"
                + "public class BigTable extends Table \n { public void <init>(){}"
                + "private void another(){}  } ");
    parseJimpleClass(cs);
  }

  @Test
  public void testValidTolerantDuplicateImports() {
    CharStream cs =
        CharStreams.fromString(
            "import Small.Table; \n"
                + "import Small.Table; \n"
                + "public class BigTable extends Table \n { public void <init>(){}"
                + "private void another(){}  } ");
    parseJimpleClass(cs);
  }

  @Test(expected = ResolveException.class)
  public void testInvalidDuplicateImports() {
    CharStream cs =
        CharStreams.fromString(
            "import Small.Table; \n"
                + "import Medium.Table; \n"
                + "public class BigTable extends Table \n { public void <init>(){}"
                + "private void another(){}  } ");
    parseJimpleClass(cs);
  }

  @Test
  public void parseClassWField() {
    CharStream cs =
        CharStreams.fromString(
            "public class StaticFieldClass extends java.lang.Object \n  { static bool globalCounter;  } ");
    parseJimpleClass(cs);

    CharStream cs1 =
        CharStreams.fromString(
            "public class StaticFieldClass extends java.lang.Object \n  { bool $flag;  } ");
    parseJimpleClass(cs1);

    CharStream cs2 =
        CharStreams.fromString(
            "public class StaticFieldClass extends java.lang.Object \n  { java.lang.String globalCounter;  } ");
    parseJimpleClass(cs2);

    CharStream cs3 =
        CharStreams.fromString(
            "public class StaticFieldClass extends java.lang.Object \n  { int globalCounter;  } ");
    parseJimpleClass(cs3);
  }

  @Test
  public void parseClassWFields() {
    CharStream cs =
        CharStreams.fromString(
            "public class InstanceField extends java.lang.Object\n"
                + " {     public int globalCounter;\n long sth;} ");
    parseJimpleClass(cs);
  }

  @Test(expected = ResolveException.class)
  public void parseDuplicateFields() {
    CharStream cs =
        CharStreams.fromString(
            "public class DuplicateField extends java.lang.Object\n"
                + " {     public int globalCounter; public int globalCounter;} ");
    parseJimpleClass(cs);
  }

  @Test(expected = ResolveException.class)
  public void parseDuplicateFieldsDiffType() {
    CharStream cs =
        CharStreams.fromString(
            "public class DuplicateField extends java.lang.Object\n"
                + " {     public int globalCounter; bool globalCounter;} ");
    parseJimpleClass(cs);
  }

  @Test
  public void parseClassMethodWOBody() {
    CharStream cs =
        CharStreams.fromString(
            "public class Noclass extends java.lang.Object \n { public abstract void nobody();  } ");
    parseJimpleClass(cs);

    CharStream cs2 =
        CharStreams.fromString(
            "public class Noclass extends java.lang.Object \n { public native void withoutbody();  } ");
    parseJimpleClass(cs2);
  }

  @Test
  public void parseClassWMethod() {
    CharStream cs =
        CharStreams.fromString(
            "public class EmptyClass extends java.lang.Object \n { public void <init>(){}  } ");
    parseJimpleClass(cs);
  }

  @Test
  public void parseClassWMethods() {
    CharStream cs =
        CharStreams.fromString(
            "public class EmptyClass extends java.lang.Object \n { public void <init>(){}"
                + "private void another(){}  } ");

    parseJimpleClass(cs);
  }

  @Test
  public void parseInterleavingClassMembers() {
    CharStream cs1 =
        CharStreams.fromString(
            "public class Interleaving1 extends java.lang.Object \n { public void <init>(){} static int globalCounter; protected void another(){}  } ");
    parseJimpleClass(cs1);

    CharStream cs2 =
        CharStreams.fromString(
            "public class Interleaving2 extends java.lang.Object \n { private bool flag; void <init>(){} static int globalCounter; protected void another(){}  } ");
    parseJimpleClass(cs2);
  }

  @Test
  public void parseMethodWithParameter() {
    CharStream cs1 =
        CharStreams.fromString(
            "public class Param extends java.lang.Object \n { public void <init>(java.lang.String){} \n void another(int, float, double, bool, java.lang.String){}  } ");
    parseJimpleClass(cs1);
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
    parseJimpleClass(cs);
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
    parseJimpleClass(cs);
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
    parseJimpleClass(cs);
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

    SootClass<?> sc = parseJimpleClass(cs);
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
    parseJimpleClass(cs);
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
    parseJimpleClass(cs);
  }

  @Test(expected = Exception.class)
  public void testLongCommentDisruptingTokenWord() {
    CharStream cs =
        CharStreams.fromString(
            "public cla/*DIRSUPT*/ss "
                + " BigTable extends Table \n {"
                + "public void <init>(){}"
                + "} ");
    parseJimpleClass(cs);
  }

  @Test
  public void testGreedyStringFix() {
    CharStream cs =
        CharStreams.fromString(
            "class de.upb.sootup.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "    public java.lang.String j;\n"
                + "    void <init>()\n"
                + "    {\n"
                + "      de.upb.sootup.concrete.fieldReference.A r0;\n"
                + "      r0 := @this: de.upb.sootup.concrete.fieldReference.A; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"something \"; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"stupid\"; \n"
                + "      return;\n"
                + "    }\n"
                + "  }\n"
                + "\n");

    parseJimpleClass(cs);
  }

  @Test
  public void testCommentInStringConstant() {
    CharStream cs =
        CharStreams.fromString(
            "class de.upb.sootup.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "    public java.lang.String j;\n"
                + "    void <init>()\n"
                + "    {\n//nonsense \n"
                + "      de.upb.sootup.concrete.fieldReference.A r0;// nonsense\n"
                + "      r0 := @this: de.upb.sootup.concrete.fieldReference.A; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"something \"; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"stupid\"; \n"
                + "      return;//nonsense\n"
                + "    }\n"
                + "  }\n"
                + "\n");

    parseJimpleClass(cs);

    CharStream cs1 =
        CharStreams.fromString(
            "class de.upb.sootup.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "    void <init>()\n"
                + "    {\n"
                + "      de.upb.sootup.concrete.fieldReference.A r0;\n"
                + "      r0 := @this: de.upb.sootup.concrete.fieldReference.A;\n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"gre/*blabla*/ater\";\n"
                + "      return;\n"
                + "    }\n"
                + "  }\n"
                + "\n");
    parseJimpleClass(cs1);
  }

  @Test
  public void testInvoke() {
    CharStream cs =
        CharStreams.fromString(
            "class Invoke{"
                + "public void <init>()\n"
                + "    {\n"
                + "      de.upb.sootup.instructions.expr.DynamicInvokeExprTest r0;\n"
                + "      r0 := @this: de.upb.sootup.instructions.expr.DynamicInvokeExprTest;\n"
                + "      specialinvoke r0.<java.lang.Object: void <init>()>();\n"
                + "      return;\n"
                + "    }"
                + "}");
    parseJimpleClass(cs);
  }

  @Test
  public void testOther() {

    CharStream cs =
        CharStreams.fromString(
            "class de.upb.sootup.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "  static int it;\n"
                + "    public java.lang.String j;\n"
                + "    void <init>()\n"
                + "    {\n"
                + "      de.upb.sootup.concrete.fieldReference.A r0;\n"
                + "      r0 := @this: de.upb.sootup.concrete.fieldReference.A;\n"
                + "      specialinvoke r0.<java.lang.Object: void <init>()>();\n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: int i> = 15;\n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"greater\";\n"
                + "      return;\n"
                + "    }\n"
                + "  }\n"
                + "\n");
    parseJimpleClass(cs);
  }

  @Test
  public void testOuterclass() {
    CharStream cs =
        CharStreams.fromString("class OuterClass$InnerClass{" + "public void <init>(){}" + "}");
    parseJimpleClass(cs);
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
      final CodePointCharStream charStream = CharStreams.fromString("Banana.\\\"class\\\".AClass");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertEquals("Banana.\\\"class\\\".AClass", parser.identifier().getText());
    }
    {
      final CodePointCharStream charStream = CharStreams.fromString("\\\"Banana.class.AClass\\\"");
      final JimpleParser parser =
          JimpleConverterUtil.createJimpleParser(
              charStream, Paths.get("InputFromString.doesNotExists"));
      assertEquals("\\\"Banana.class.AClass\\\"", parser.identifier().getText());
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

    // old kind of escaping -> every part i.e. between the dot which needed escaping was escaped
    {
      CharStream cs =
          CharStreams.fromString("public class escaped.'class' extends java.lang.Object {}");
      SootClass<?> sc = parseJimpleClass(cs);
      assertEquals("escaped.class", sc.getClassSource().getClassType().toString());
    }
    // old kind of escaping: at the beginning
    {
      CharStream cs =
          CharStreams.fromString("public class 'class'.is.escaped extends java.lang.Object {}");
      SootClass<?> sc = parseJimpleClass(cs);
      assertEquals("class.is.escaped", sc.getClassSource().getClassType().toString());
    }

    {
      // escaped word (pckg) which was unnecessarily escaped by the rules of old soot
      CharStream cs =
          CharStreams.fromString(
              "public class some.'pckg'.'class'.More extends java.lang.Object {}");
      SootClass<?> sc = parseJimpleClass(cs);
      assertEquals("some.pckg.class.More", sc.getClassSource().getClassType().toString());
    }

    {
      // current escaping
      CharStream cs =
          CharStreams.fromString("public class 'annotationinterface' extends java.lang.Object {}");
      SootClass<?> sc = parseJimpleClass(cs);
      assertEquals("annotationinterface", sc.getClassSource().getClassType().toString());
    }

    {
      // no escaping needed as "class" is not considered a token if its nested into more
      CharStream cs =
          CharStreams.fromString("public class some.pckg.class extends java.lang.Object \n {}");
      SootClass<?> sc = parseJimpleClass(cs);
      assertEquals("some.pckg.class", sc.getClassSource().getClassType().toString());
    }

    try {
      // missing escaping (i.e. class is a token which needs it!)
      CharStream cs =
          CharStreams.fromString(
              "public class class extends java.lang.Object implements java.lang.'annotation'.Annotation\n {}");
      parseJimpleClass(cs);
      fail("escaping is needed");
    } catch (Exception ignored) {
    }

    {
      // inside quotes
      CharStream cs =
          CharStreams.fromString(
              "public class \\'some.pckg.ClassObj\\' extends java.lang.Object \n {}");
      SootClass<?> sc = parseJimpleClass(cs);
      assertEquals("'some.pckg.ClassObj'", sc.getClassSource().getClassType().toString());
    }

    {
      // testing escaped string things
      CharStream cs =
          CharStreams.fromString(
              "public class 'some.'.pckg.'.ClassObj' extends java.lang.Object \n {}");
      SootClass<?> sc = parseJimpleClass(cs);
      assertEquals("some..pckg..ClassObj", sc.getClassSource().getClassType().toString());
    }

    {
      // testing escaped string things
      CharStream cs =
          CharStreams.fromString(
              "public class some.\\'.pckg.\\'.ClassObj extends java.lang.Object \n {}");
      SootClass<?> sc = parseJimpleClass(cs);
      assertEquals("some.'.pckg.'.ClassObj", sc.getClassSource().getClassType().toString());
    }

    {
      assertEquals("'class'", Jimple.unescape("\"'class'\""));
      CharStream cs =
          CharStreams.fromString(
              "public class \"'notescapedquotesinstring'\" extends java.lang.Object \n {}");
      try {
        parseJimpleClass(cs);
        fail("\" is not allowed in identifiers");
      } catch (Exception ignore) {
      }
    }

    {

      // escaped quotes in escaped sequence
      CharStream cs =
          CharStreams.fromString("public class \\'class\\' extends java.lang.Object \n {}");
      SootClass<?> sc = parseJimpleClass(cs);

      assertEquals("'class'", Jimple.unescape("\\'class\\'"));
      assertEquals("'class'", sc.getClassSource().getClassType().toString());
    }

    {
      // different escape start /end symbol
      try {
        CharStream cs =
            CharStreams.fromString("public class \"class' extends java.lang.Object \n {}");
        parseJimpleClass(cs);
        fail("start and end quote do not match.");
      } catch (Exception ignore) {
      }
    }

    assertEquals("\\", StringTools.getUnEscapedStringOf("\\\\"));
    assertEquals("\"", StringTools.getUnEscapedStringOf("\\\""));
    assertEquals("'", StringTools.getUnEscapedStringOf("\\'"));
    assertEquals("\"'", StringTools.getUnEscapedStringOf("\"\\'"));

    assertEquals("'class'", Jimple.unescape("\"'class'\""));
    assertEquals("'class'", Jimple.unescape("\"\\'class\\'\""));
    assertEquals("'class'", Jimple.unescape("\\'class\\'"));

    // necessary inner escaping
    assertEquals("\"class\"", Jimple.unescape("\"\\\"class\\\"\""));
    // unnecessary inner escaping
    assertEquals("'class'", Jimple.unescape("\"'class'\""));
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
    parseJimpleClass(cs);
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
    parseJimpleClass(cs);
  }

  /*   parse partial contents - at least for syntax highlighting */
  @Test
  public void testPartial_JustMethod() {
    CharStream cs =
        CharStreams.fromString(" public void <init>(){} \n" + "private void another(){} \n");

    JimpleLexer lexer = new JimpleLexer(cs);
    assertEquals(14, lexer.getAllTokens().size());
  }

  @Test
  public void testPartial_JustStmt() {
    CharStream cs =
        CharStreams.fromString(
            "r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"something \"; \n");

    JimpleLexer lexer = new JimpleLexer(cs);
    assertEquals(11, lexer.getAllTokens().size());
  }

  @Test
  public void testPartial_JustStmts() {
    CharStream cs =
        CharStreams.fromString(
            "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"something \"; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"stupid\"; \n");

    JimpleLexer lexer = new JimpleLexer(cs);
    assertEquals(22, lexer.getAllTokens().size());
  }

  @Test
  public void testPartial_FirstHalfOfClass() {
    CharStream cs =
        CharStreams.fromString(
            "class de.upb.sootup.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "    public java.lang.String j;\n"
                + "    public void previously_declared_method(){\n} \n"
                + "    void <init>()\n"
                + "    {\n"
                + "      de.upb.sootup.concrete.fieldReference.A r0;\n"
                + "      r0 := @this: de.upb.sootup.concrete.fieldReference.A; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"something \"; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"stupid\"; \n"
                + "      return;\n"
                + "    }\n");

    JimpleLexer lexer = new JimpleLexer(cs);
    assertEquals(54, lexer.getAllTokens().size());
  }

  @Test
  public void testPartial_SecondHalfOfClass() {
    CharStream cs =
        CharStreams.fromString(
            "       r0 := @this: de.upb.sootup.concrete.fieldReference.A; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"something \"; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"stupid\"; \n"
                + "      return;\n"
                + "    }\n"
                + "    public void another_method(){\n}\n"
                + "  }\n"
                + "\n");

    JimpleLexer lexer = new JimpleLexer(cs);
    assertEquals(38, lexer.getAllTokens().size());
  }

  @Test
  public void testPartial_InvalidStmt() {
    CharStream cs =
        CharStreams.fromString(
            "class de.upb.sootup.concrete.fieldReference.A extends java.lang.Object\n"
                + "  {\n"
                + "    public java.lang.String j;\n"
                + "    public void previously_declared_method(){\n}\n"
                + "    void <init>()\n"
                + "    {\n"
                + "      de.upb.sootup.concrete.fieldReference.A r0;\n"
                + "      r0 := @this: de.upb.sootup.concrete.fieldReference.A; \n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = // missing assignment\n"
                + "      r0.<de.upb.sootup.concrete.fieldReference.A: java.lang.String j> = \"stupid\"; \n"
                + "      return;\n"
                + "    }\n"
                + "public void another_method(){\n}\n"
                + "  }\n"
                + "\n");

    JimpleLexer lexer = new JimpleLexer(cs);
    assertEquals(60, lexer.getAllTokens().size());
  }

  @Test
  public void testQuotedTypeParsing() throws IOException {
    SootClass<?> clazz =
        parseJimpleClass(
            CharStreams.fromFileName("src/test/java/resources/jimple/SubTypeValidator.jimple"));
    Set<? extends SootMethod> methods = clazz.getMethods();
    SootMethod method = methods.iterator().next();
    Body body = method.getBody();
    assertEquals(3, body.getLocalCount());
  }

  @Test
  public void testEdgeCaseDoubleParsing() throws IOException {
    SootClass<?> clazz =
        parseJimpleClass(
            CharStreams.fromFileName("src/test/java/resources/jimple/EdgeCaseDoubleNumber.jimple"));
    Set<? extends SootField> fields = clazz.getFields();
    for (SootField field : fields) {
      assertEquals(PrimitiveType.DoubleType.getInstance(), field.getType());
    }
  }
}
