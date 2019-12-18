package de.upb.swt.soot;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.jimple.JimpleBaseListener;
import de.upb.swt.soot.jimple.JimpleLexer;
import de.upb.swt.soot.jimple.JimpleParser;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * this class transforms the input from the parser into the frameworks Jimple objects used by Soot.
 *
 * @author Markus Schmidt
 */
public class JimpleReader extends JimpleBaseListener {

  Set<SootField> field = new HashSet<>();
  Set<SootMethod> methods = new HashSet<>();

  public SootClass parse(String source, IdentifierFactory identifierFactory) {
    CharStream input = CharStreams.fromString(source);
    return parse(input, identifierFactory);
  }

  public SootClass parse(Path path, IdentifierFactory identifierFactory) throws IOException {
    CharStream input = CharStreams.fromPath(path);
    return parse(input, identifierFactory);
  }

  private SootClass parse(CharStream source, IdentifierFactory identifierFactory) {
    JimpleLexer lexer = new JimpleLexer(source);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    JimpleParser parser = new JimpleParser(tokenStream);

    ParseTree tree = parser.file();
    JimpleVisitorImpl visitor = new JimpleVisitorImpl();
    visitor.visit(tree);

    // FIXME: sourcetype DefaultSourceTypeSpecifier.getInstance().sourceTypeFor(
    // identifierFactory..... )
    return null; // new SootClass( , SourceType.Application);
  }
}
