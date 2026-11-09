/**
 * Note that this modules does not compile as it requires modsplitfoo1 and modsplitfoo2 
 * with both export their package pkgfoo (and hence produce a split package problem at compile time)
 */
open module modmainfoo { // does not compile (as required modsplitfoo1, modsplitfoo2 both export same package pkgfoo)
    requires modsplitfoo1;
    requires modsplitfoo2;
}
