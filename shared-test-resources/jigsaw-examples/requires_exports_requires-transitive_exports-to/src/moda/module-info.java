// moda/module.info.java

module moda {

    // Read-Abhaengigkeiten von moda zu modb und modc
    // (Compiler meckert fehlende an!)
    requires modb;
    requires transitive modc;

    // Exports von Packages von moda
    // (Compiler meckert fehlende an!)
    exports pkga1;
    exports pkga2 to modmain;  // nur an modmain

    opens pkga3;     // nur zur Laufzeit
                     // siehe Reflection

}
