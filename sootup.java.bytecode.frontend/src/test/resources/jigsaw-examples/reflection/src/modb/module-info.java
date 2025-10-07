module modb {
    // exports not enough anymore for Deep Reflection, i.e. for setAccessible(true) to allow reflection access
    exports  pkgb to modmain;
    // opens: since b142 needed for Deep Reflection
    opens    pkgb to modmain;
    
    // exports only allows Shallow Reflection
    exports  pkgb1 to modmain;

    // opens: since b142 needed for Deep Reflection
    opens    pkgbinternal /* to modmain */;
}
