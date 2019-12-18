package de.upb.swt.soot;

import de.upb.swt.soot.jimple.JimpleBaseVisitor;
import de.upb.swt.soot.jimple.JimpleParser;

// TODO: specify the raw type
public class JimpleVisitorImpl extends JimpleBaseVisitor {

  //  OverridingClassSource classSource;

  // = new OverridingClassSource();

  @Override
  public Object visitFile(JimpleParser.FileContext ctx) {
    return super.visitFile(ctx);
  }
}
