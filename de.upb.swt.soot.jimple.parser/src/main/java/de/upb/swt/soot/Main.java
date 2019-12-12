package de.upb.swt.soot;

public class Main {

  public static void main(String[] args) {
    // JimpleParser jparser = new JimpleParser();
    // parser.setIdentifierFactory(JavaIdentifierFactory.getInstance());

    /*
    String javaClassContent = "public class SampleClass { void DoSomething(){} }";
    JimpleparserLexer lexer = new JimpleparserLexer(CharStreams.fromString(javaClassContent));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    JimpleparserParser parser = new JimpleparserParser(tokens);
    ParseTree tree = parser.compilationUnit();

    ParseTreeWalker walker = new ParseTreeWalker();
    JimpleparserListener listener = new JimpleparserListener();

    walker.walk(listener, tree);

    assertThat(listener.getErrors().size(), is(1));
    assertThat(listener.getErrors().get(0),
            is("Method DoSomething is uppercased!"));

    */

  }
}
