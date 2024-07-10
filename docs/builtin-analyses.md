# BuiltIn Analyses
More to come!

### LocalLivenessAnalyser

LocalLivenessAnalyser is used for querying for the list of live local variables before and after a given <code>Stmt</code>.

Example:

![LocalLiveness Example](assets/figures/LocalLiveness%20Example.png)

The live local variables before and after each <code>Stmt</code> will be calculated after generating an instance of LocalLivenessAnalyser as shown the example above. They can be queried by using the methods <code>getLiveLocalsBeforeStmt</code> and <code>getLiveLocalsAfterStmt</code>.

### DominanceFinder

DomianceFinder is used for querying for the immediate dominator and dominance frontiers for a given basic block.

Example:  ![DominanceFinder Example](assets/figures/DominanceFinder%20Example.png)

After generating an instance of DominanceFinder for a <code>BlockGraph</code>, we will get the immediate dominator and dominance frontiers for each basic block. The both properties can be queried by using the methods<code>getImmediateDominator</code>and<code>getDominanceFrontiers</code>.
