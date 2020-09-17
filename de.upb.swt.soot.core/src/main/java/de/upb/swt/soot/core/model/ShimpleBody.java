package de.upb.swt.soot.core.model;

import java.util.Map;
import javax.annotation.Nonnull;

/** Implementing the Body class for SSA implementation */
public class ShimpleBody {

  // TODO protected ShimpleOptions options;
  private ShimpleBodyBuilder sbb;

  /** Constructs a ShimpleBody from the given Body and options. */
  ShimpleBody(Body body, Map options) {

    // must happen before SPatchingChain gets created
    // TODO implemetnt Shimple options
    /*this.options = new ShimpleOptions(options);
    setSSA(true);
    isExtendedSSA = this.options.extended();

    unitChain = new SPatchingChain(this, new HashChain());*/
    sbb = new ShimpleBodyBuilder(this);
  }

  // TODO implement the ShimpleBody (extend Body/Copyable with ShimpleBodyBuilder as static class)

  public static class ShimpleBodyBuilder {

    // TODO 1. ShimpleBody without options 2. ShimpleBodyBuilder after assigning local unique names
    // (PhiNode, PiNode) 3. Work on Graph implementation algo(stmtGraph, some Google impl) 4.
    // Variable Renaming Algorithm from Cytron et al 91, P26-8
    ShimpleBodyBuilder(@Nonnull ShimpleBody shimpleBody) {}

    @Nonnull
    public ShimpleBody build() {
      ShimpleBody shimpleBody = null;

      // TODO
      return shimpleBody;
    }
  }
}
