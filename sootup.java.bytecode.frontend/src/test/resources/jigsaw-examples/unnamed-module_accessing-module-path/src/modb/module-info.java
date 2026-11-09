module modb {
    exports pkgb; // note that this is needed to export the package also for an access from the unnamed module!

    // As we do *NOT* export the internal package 
    //     exports pkgbinternal;
    // we run into the following RuntimeException:
    // Exception in thread "main" java.lang.IllegalAccessError: class pkgcpmain.Main (in unnamed module @0x5ae9a829) cannot access class pkgbinternal.B (in module modb) because module modb does not export pkgbinternal to unnamed module @0x5ae9a829
    //     at pkgcpmain.Main.main(Main.java:18)
}