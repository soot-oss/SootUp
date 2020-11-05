package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.StringTools;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.jimple.JimpleLexer;
import de.upb.swt.soot.jimple.JimpleParser;
import java.nio.file.Path;
import java.util.*;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.*;

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
    typename = StringTools.getUnEscapedStringOf(typename);
    PackageName packageName = imports.get(typename);
    return packageName == null
        ? identifierFactory.getType(typename)
        : identifierFactory.getType(packageName.getPackageName() + "." + typename);
  }

  public ClassType getClassType(String typename) {
    typename = StringTools.getUnEscapedStringOf(typename);
    PackageName packageName = this.imports.get(typename);
    return packageName == null
        ? this.identifierFactory.getClassType(typename)
        : this.identifierFactory.getClassType(typename, packageName.getPackageName());
  }

  @Nonnull
  public static Position buildPositionFromCtx(@Nonnull ParserRuleContext ctx) {
    return new Position(
        ctx.start.getLine(),
        ctx.start.getCharPositionInLine(),
        ctx.stop.getLine(),
        ctx.stop.getCharPositionInLine());
  }

  public void addImport(JimpleParser.ImportItemContext item) {
    if (item == null || item.location == null) {
      return;
    }
    final ClassType classType = identifierFactory.getClassType(item.location.getText());
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
    JimpleParser.TypeContext typeCtx = ctx.method_subsignature().type();
    JimpleParser.Method_nameContext method_nameCtx = ctx.method_subsignature().method_name();
    if (class_name == null || typeCtx == null || method_nameCtx == null) {
      throw new ResolveException(
          "MethodSignature is not well formed.", fileUri, buildPositionFromCtx(ctx));
    }
    String classname = class_name.getText();
    Type type = getType(typeCtx.getText());
    String methodname = method_nameCtx.getText();
    List<Type> params = getTypeList(ctx.method_subsignature().type_list());
    return identifierFactory.getMethodSignature(methodname, getClassType(classname), type, params);
  }

  public FieldSignature getFieldSignature(JimpleParser.Field_signatureContext ctx) {
    String classname = ctx.classname.getText();
    Type type = getType(ctx.type().getText());
    String fieldname = ctx.fieldname.getText();
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
      list.add(identifierFactory.getType(typeContext.getText()));
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
      list.add(identifierFactory.getClassType(typeContext.getText()));
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
      set.add(identifierFactory.getClassType(typeContext.getText()));
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
                "Jimple Syntaxerror: " + msg, path, new Position(line, charPositionInLine, -1, -1));
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
            throw new ResolveException(
                "Jimple Syntaxerror: " + msg, path, new Position(line, charPositionInLine, -1, -1));
          }
        });
    return parser;
  }
}
