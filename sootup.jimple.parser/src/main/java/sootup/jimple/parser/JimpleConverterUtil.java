package sootup.jimple.parser;

import java.nio.file.Path;
import java.util.*;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.*;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.Jimple;
import sootup.core.model.FullPosition;
import sootup.core.model.Position;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.JavaIdentifierFactory;
import sootup.jimple.JimpleLexer;
import sootup.jimple.JimpleParser;

/**
 * This Utility class provides common used methods in context with parsing Jimple.
 *
 * @author Markus Schmidt
 */
public class JimpleConverterUtil {

  private final IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private final Map<String, PackageName> imports = new HashMap<>();
  @Nonnull private final Path fileUri;

  public JimpleConverterUtil(@Nonnull Path file) {
    this.fileUri = file;
  }

  public IdentifierFactory getIdentifierFactory() {
    return identifierFactory;
  }

  public Type getType(String typename) {
    typename = Jimple.unescape(typename);
    PackageName packageName = imports.get(typename);
    return packageName == null
        ? identifierFactory.getType(typename)
        : identifierFactory.getType(packageName.getName() + "." + typename);
  }

  public ClassType getClassType(String typename) {
    typename = Jimple.unescape(typename);
    PackageName packageName = this.imports.get(typename);
    return packageName == null
        ? this.identifierFactory.getClassType(typename)
        : this.identifierFactory.getClassType(typename, packageName.getName());
  }

  @Nonnull
  public static Position buildPositionFromCtx(@Nonnull ParserRuleContext ctx) {

    // calc end position (line number+char offset in line) as antlr is not capable to do it
    // intuitively
    String tokenstr = ctx.getText();
    // lsp indexes everything zero based - antlr does it only chars in line; linenos are indexed
    // one-based => subtract one ;)
    int lineCount = -1;
    int fromIdx = 0;
    int lastLineBreakIdx = 0;
    while ((fromIdx = tokenstr.indexOf("\n", fromIdx)) != -1) {
      lastLineBreakIdx = fromIdx;
      lineCount++;
      fromIdx++;
    }

    int endCharLength = tokenstr.length() - lastLineBreakIdx;

    return new FullPosition(
        ctx.start.getLine() - 1,
        ctx.start.getCharPositionInLine(),
        ctx.stop.getLine() + lineCount,
        ctx.stop.getCharPositionInLine() + endCharLength);
  }

  public void addImport(JimpleParser.ImportItemContext item) {
    if (item == null || item.location == null) {
      return;
    }
    final ClassType classType =
        identifierFactory.getClassType(Jimple.unescape(item.location.getText()));
    final PackageName duplicate =
        imports.putIfAbsent(classType.getClassName(), classType.getPackageName());
    if (duplicate != null && !duplicate.equals(classType.getPackageName())) {
      throw new ResolveException(
          "Multiple Imports for the same ClassName can not be resolved!",
          fileUri,
          buildPositionFromCtx(item));
    }
  }

  @Nonnull
  public MethodSignature getMethodSignature(
      JimpleParser.Method_signatureContext ctx, ParserRuleContext parentCtx) {
    if (ctx == null) {
      throw new ResolveException(
          "MethodSignature is missing.", fileUri, buildPositionFromCtx(parentCtx));
    }

    JimpleParser.IdentifierContext class_name = ctx.class_name;
    if (class_name == null) {
      throw new ResolveException(
          "MethodSignature is not well formed.", fileUri, buildPositionFromCtx(ctx));
    }

    final MethodSubSignature methodSubSignature =
        getMethodSubSignature(ctx.method_subsignature(), ctx);
    String classname = class_name.getText();
    return identifierFactory.getMethodSignature(getClassType(classname), methodSubSignature);
  }

  @Nonnull
  public MethodSubSignature getMethodSubSignature(
      JimpleParser.Method_subsignatureContext ctx, ParserRuleContext parentCtx) {
    if (ctx == null) {
      throw new ResolveException(
          "MethodSignature is missing.", fileUri, buildPositionFromCtx(parentCtx));
    }
    JimpleParser.TypeContext typeCtx = ctx.type();
    JimpleParser.Method_nameContext method_nameCtx = ctx.method_name();
    if (typeCtx == null || method_nameCtx == null) {
      throw new ResolveException(
          "MethodSignature is not well formed.", fileUri, buildPositionFromCtx(ctx));
    }
    Type type = getType(typeCtx.getText());
    String methodname = Jimple.unescape(method_nameCtx.getText());
    List<Type> params = getTypeList(ctx.type_list());
    return identifierFactory.getMethodSubSignature(methodname, type, params);
  }

  public FieldSignature getFieldSignature(JimpleParser.Field_signatureContext ctx) {
    String classname = Jimple.unescape(ctx.classname.getText());
    Type type = getType(Jimple.unescape(ctx.type().getText()));
    String fieldname = Jimple.unescape(ctx.fieldname.getText());
    return identifierFactory.getFieldSignature(fieldname, getClassType(classname), type);
  }

  public List<Type> getTypeList(JimpleParser.Type_listContext type_list) {
    if (type_list == null) {
      return Collections.emptyList();
    }
    List<JimpleParser.TypeContext> typeList = type_list.type();
    int size = typeList.size();
    if (size < 1) {
      return Collections.emptyList();
    }
    List<Type> list = new ArrayList<>(size);
    for (JimpleParser.TypeContext typeContext : typeList) {
      list.add(identifierFactory.getType(Jimple.unescape(typeContext.getText())));
    }
    return list;
  }

  public List<ClassType> getClassTypeList(JimpleParser.Type_listContext type_list) {
    if (type_list == null) {
      return Collections.emptyList();
    }
    List<JimpleParser.TypeContext> typeList = type_list.type();
    int size = typeList.size();
    if (size < 1) {
      return Collections.emptyList();
    }
    List<ClassType> list = new ArrayList<>(size);
    for (JimpleParser.TypeContext typeContext : typeList) {
      list.add(identifierFactory.getClassType(Jimple.unescape(typeContext.getText())));
    }
    return list;
  }

  public Set<ClassType> getClassTypeSet(JimpleParser.Type_listContext type_list) {
    if (type_list == null) {
      return Collections.emptySet();
    }
    final List<JimpleParser.TypeContext> typeList = type_list.type();
    final int size = typeList.size();
    if (size < 1) {
      return Collections.emptySet();
    }
    Set<ClassType> set = new HashSet<>(size);
    for (JimpleParser.TypeContext typeContext : typeList) {
      set.add(identifierFactory.getClassType(Jimple.unescape(typeContext.getText())));
    }
    return set;
  }

  @Nonnull
  public static JimpleParser createJimpleParser(CharStream charStream, Path path) {
    JimpleLexer lexer = new JimpleLexer(charStream);

    lexer.removeErrorListeners();
    lexer.addErrorListener(
        new BaseErrorListener() {
          @Override
          public void syntaxError(
              Recognizer<?, ?> recognizer,
              Object offendingSymbol,
              int line,
              int charPositionInLine,
              String msg,
              RecognitionException e) {

            throw new ResolveException(
                "Jimple SyntaxError: " + msg,
                path,
                new FullPosition(line - 1, charPositionInLine, line - 1, Integer.MAX_VALUE));
          }
        });

    TokenStream tokens = new CommonTokenStream(lexer);
    JimpleParser parser = new JimpleParser(tokens);

    parser.removeErrorListeners();
    parser.addErrorListener(
        new BaseErrorListener() {
          @Override
          public void syntaxError(
              Recognizer<?, ?> recognizer,
              Object offendingSymbol,
              int line,
              int charPositionInLine,
              String msg,
              RecognitionException e) {

            Position position;
            if (e != null) {
              if (e.getCause() instanceof NoViableAltException) {
                Token start = ((NoViableAltException) e.getCause()).getStartToken();
                position =
                    new FullPosition(
                        start.getLine() - 1,
                        start.getCharPositionInLine(),
                        charPositionInLine - 1,
                        Integer.MAX_VALUE);
              } else {
                // hint: not precise if erroneous input spans across multiple lines!
                int sizeOfBad = e.getCtx().getText().length();
                int firstCol = Math.max(charPositionInLine - sizeOfBad, 0);
                position = new FullPosition(line - 1, firstCol, line - 1, Integer.MAX_VALUE);
              }
            } else {
              position =
                  new FullPosition(line - 1, charPositionInLine, line - 1, Integer.MAX_VALUE);
            }

            throw new ResolveException("Jimple SyntaxError: " + msg, path, position);
          }
        });
    return parser;
  }
}
