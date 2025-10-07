import pkgannotations.*;

/**
 * This module modb is deprecated (but still really cool :-)
 */
@Deprecated(forRemoval=true, since="9")
@ReallyCoolModule(wowReason="modb is simply cool")
@CompileTimeAnnotation
@RunTimeAnnotation
module modb {
    exports pkgb;
    
    requires transitive mod.annotations;
}
