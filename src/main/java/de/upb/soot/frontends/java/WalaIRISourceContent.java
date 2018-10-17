package de.upb.soot.frontends.java;

import com.ibm.wala.cast.loader.AstClass;

import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.views.IView;

/**
 * Converts one Wala IR source file to Jimple representation
 *
 * @author Andreas Dann
 * @author Linghui Luo
 * @author Ben Hermann
 *
 */
public class WalaIRISourceContent implements de.upb.soot.namespaces.classprovider.ISourceContent {
  private AstClass source;
  private WalaIRToJimpleConverter converter;

  public WalaIRISourceContent(AstClass source, WalaIRToJimpleConverter converter) {
    this.source = source;
    this.converter = converter;
  }

  @Override
  public SootClass resolve(ResolvingLevel level, IView view) {
    return converter.convertClass(source);
  }
}
