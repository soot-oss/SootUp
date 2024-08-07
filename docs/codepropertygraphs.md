Code Property Graphs (CPGs) are a representation of program code that combines different code representations into a single graph. This unified representation includes abstract syntax trees (ASTs), control flow graphs (CFGs), control dependence graphs (CDGs), and data dependence graphs (DDGs). CPGs enable comprehensive analysis, which makes them a powerful tool for detecting vulnerabilities and understanding code structure. For further details, refer to this [thesis](#).

## Usage Example

In this example, we will demonstrate how to create a CPG for a vulnerable Java method and use it to identify a potential vulnerability.

### Vulnerable Java Code

Let's assume we have the following vulnerable Java code in a file named `VulnerableClass.java`:

=== "Java"

    ```java
    public class VulnerableClass {
        public void vulnerableMethod(String userInput) {
            if (userInput.equals("admin")) {
                System.out.println("Welcome, admin!");
            }
        }
    }
    ```

### Step 1: Obtain a SootMethod

First, we assume we have a `SootMethod` for the `vulnerableMethod`. For instructions on how to obtain a `SootMethod`, refer to the [basic setup example](examples.md).

### Step 2: Create the CPG

We can create the CPG or its components using the creators:

=== "SootUp"

    ```java
    import sootup.codepropertygraph.ast.AstCreator;
    import sootup.codepropertygraph.cfg.CfgCreator;
    import sootup.codepropertygraph.cdg.CdgCreator;
    import sootup.codepropertygraph.ddg.DdgCreator;
    import sootup.codepropertygraph.cpg.CpgCreator;
    import sootup.codepropertygraph.propertygraph.PropertyGraph;
    import sootup.core.model.SootMethod;
    
    public class CpgExample {
    
        public static void main(String[] args) {
            // Assuming `sootMethod` is obtained from the setup step
            SootMethod vulnerableMethod = getVulnerableMethod();
    
            // Create individual graphs
            AstCreator astCreator = new AstCreator();
            PropertyGraph astGraph = astCreator.createGraph(vulnerableMethod);
    
            CfgCreator cfgCreator = new CfgCreator();
            PropertyGraph cfgGraph = cfgCreator.createGraph(vulnerableMethod);
    
            CdgCreator cdgCreator = new CdgCreator();
            PropertyGraph cdgGraph = cdgCreator.createGraph(vulnerableMethod);
    
            DdgCreator ddgCreator = new DdgCreator();
            PropertyGraph ddgGraph = ddgCreator.createGraph(vulnerableMethod);
    
            // Create the combined CPG
            CpgCreator cpgCreator = new CpgCreator(astCreator, cfgCreator, cdgCreator, ddgCreator);
            PropertyGraph cpg = cpgCreator.createCpg(vulnerableMethod);
    
            // Print the DOT representation of the CPG
            System.out.println(cpg.toDotGraph());
        }
    }
    ```

### Step 3: Analyzing the CPG

With the CPG created, you can now analyze it for vulnerabilities. For example, you can check for potential injection vulnerabilities by analyzing data flow dependencies.

=== "SootUp"

    ```java
    import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
    import sootup.codepropertygraph.propertygraph.edges.DdgEdge;
    
    public class VulnerabilityAnalysis {
    
        public static void main(String[] args) {
            // Assuming `cpg` is the PropertyGraph created in the previous step
            for (DdgEdge edge : cpg.getEdges(DdgEdge.class)) {
                StmtGraphNode source = (StmtGraphNode) edge.getSource();
                StmtGraphNode destination = (StmtGraphNode) edge.getDestination();
                
                if (isPotentiallyVulnerable(source, destination)) {
                    System.out.println("Potential vulnerability found between: ");
                    System.out.println("Source: " + source.getStmt());
                    System.out.println("Destination: " + destination.getStmt());
                }
            }
        }
    
        private static boolean isPotentiallyVulnerable(StmtGraphNode source, StmtGraphNode destination) {
            // Implement your vulnerability detection logic here
            return source.getStmt().toString().contains("userInput") && 
                   destination.getStmt() instanceof JInvokeStmt && 
                   destination.getStmt().toString().contains("println");
        }
    }
    ```

In this example, we check for data flow dependencies between the `userInput` variable and any `System.out.println` calls, which could indicate a potential injection vulnerability.

Similarly, we can define our own queries to detect specific patterns that identify common vulnerabilities.
