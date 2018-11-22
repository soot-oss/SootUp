package de.upb.soot.frontends.java;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.views.IView;

import com.ibm.wala.cast.loader.AstClass;

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
  public AbstractClass resolve(ResolvingLevel level, IView view) {
    switch (level) {
      case HIERARCHY:
        break;
      case SIGNATURES:
        break;
      case BODIES:
        break;
      default:
          return null;
    }
      return null;
  }
}
