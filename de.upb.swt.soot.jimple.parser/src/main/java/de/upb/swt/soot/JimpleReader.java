package de.upb.swt.soot;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.jimple.JimpleBaseListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

/**
 * this class transforms the input from the parser into the frameworks Jimple objects used by Soot.
 *
 * @author Markus Schmidt
 */
public class JimpleReader extends JimpleBaseListener {

  private ClassType clazztype;
  private ClassType outerclass;
  private ClassType superclass;
  private Set<ClassType> interfaces;
  private Set<SootMethod> methods = new HashSet<>();
  Set<SootField> fields = new HashSet<>();
  private Position position;
  EnumSet<Modifier> modifier;

  public SootClassSource parse(String source, IdentifierFactory identifierFactory) {
    CharStream input = CharStreams.fromString(source);
    return parse(input, identifierFactory);
  }

  public SootClassSource parse(Path path, IdentifierFactory identifierFactory) throws IOException {
    CharStream input = CharStreams.fromPath(path);
    return parse(input, identifierFactory);
  }

  private SootClassSource parse(CharStream source, IdentifierFactory identifierFactory) {

    JimpleVisitorImpl visitor = new JimpleVisitorImpl();
    visitor.parse(source);

    // FIXME
    AnalysisInputLocation inputlocation = null;
    // FIXME
    Path path = Paths.get("abra.kadabra");

    return new OverridingClassSource(
        inputlocation,
        path,
        clazztype,
        superclass,
        interfaces,
        outerclass,
        fields,
        methods,
        position,
        modifier);
  }
}
