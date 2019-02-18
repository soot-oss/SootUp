package de.upb.soot.frontends.java;

import com.ibm.wala.cast.loader.AstClass;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.views.IView;

/**
 * Converts one Wala IR source file to Jimple representation
 *
 * @author Andreas Dann
 * @author Linghui Luo
 * @author Ben Hermann
 *
 */
class WalaIRIClassSourceContent implements IClassSourceContent {
  private AstClass source;
  private WalaIRToJimpleConverter converter;

  public WalaIRIClassSourceContent(AstClass source, WalaIRToJimpleConverter converter) {
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
