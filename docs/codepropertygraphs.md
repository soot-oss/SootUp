### Dependencies

=== "Maven"
    ```maven
    <dependency>
    <groupId>org.soot-oss</groupId>
    <artifactId>sootup.codepropertygraph</artifactId>
    <version>{{ git_latest_release }}</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy
    compile "org.soot-oss:sootup.codepropertygraph:{{ git_latest_release }}"
    ```



Code Property Graphs (CPGs) are a representation of program code that combines different code representations into a
single graph. This unified representation includes abstract syntax trees (ASTs), control flow graphs (CFGs), control
dependence graphs (CDGs), and data dependence graphs (DDGs). CPGs enable comprehensive analysis, which makes them a
powerful tool for detecting vulnerabilities and understanding code structure. For further details, refer to
this [thesis](#).

## Usage Example

In this example, we will demonstrate how to create a CPG for a vulnerable Java method and use it to identify a potential
vulnerability.

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

First, we assume we have a `SootMethod` for the `vulnerableMethod`. For instructions on how to obtain a `SootMethod`,
refer to [Retrieving a Method](getting-started.md#retrieving-a-method).

### Step 2: Create the CPG

We can create the CPG subgraphs using the creators.

=== "AST"

    ```java
    public class AstExample {
    
        public static void main(String[] args) {
            // Assuming `sootMethod` is obtained from the setup step
            SootMethod vulnerableMethod = getVulnerableMethod();
    
            // Create the AST subgraph
            AstCreator astCreator = new AstCreator();
            PropertyGraph astGraph = astCreator.createGraph(vulnerableMethod);
    
            // Print the DOT representation of the AST
            System.out.println(astGraph.toDotGraph());
        }
    }
    ```

=== "CFG"

    ```java
    public class CfgExample {
    
        public static void main(String[] args) {
            // Assuming `sootMethod` is obtained from the setup step
            SootMethod vulnerableMethod = getVulnerableMethod();

            // Create the CFG subgraph         
            CfgCreator cfgCreator = new CfgCreator();
            PropertyGraph cfgGraph = cfgCreator.createGraph(vulnerableMethod);
    
            // Print the DOT representation of the CFG
            System.out.println(cfgGraph.toDotGraph());
        }
    }
    ```

=== "CDG"

    ```java
    public class CdgExample {
    
        public static void main(String[] args) {
            // Assuming `sootMethod` is obtained from the setup step
            SootMethod vulnerableMethod = getVulnerableMethod();
    
            // Create the CDG subgraph
            CdgCreator cdgCreator = new CdgCreator();
            PropertyGraph cdgGraph = cdgCreator.createGraph(vulnerableMethod);
    
            // Print the DOT representation of the CDG
            System.out.println(cdgGraph.toDotGraph());
        }
    }
    ```

=== "DDG"

    ```java
    public class DdgExample {
    
        public static void main(String[] args) {
            // Assuming `sootMethod` is obtained from the setup step
            SootMethod vulnerableMethod = getVulnerableMethod();
    
            // Create the DDG subgraph
            DdgCreator ddgCreator = new DdgCreator();
            PropertyGraph ddgGraph = ddgCreator.createGraph(vulnerableMethod);
    
            // Print the DOT representation of the DDG
            System.out.println(ddgGraph.toDotGraph());
        }
    }
    ```

We can create the combined CPG graph using the `CpgCreator`.

=== "CPG"

    ```java
    public class CpgExample {
    
        public static void main(String[] args) {
            // Assuming `sootMethod` is obtained from the setup step
            SootMethod vulnerableMethod = getVulnerableMethod();
    
            AstCreator astCreator = new AstCreator();
            CfgCreator cfgCreator = new CfgCreator();
            CdgCreator cdgCreator = new CdgCreator();
            DdgCreator ddgCreator = new DdgCreator();
            
            // Create the combined CPG
            CpgCreator cpgCreator = new CpgCreator(astCreator, cfgCreator, cdgCreator, ddgCreator);
            PropertyGraph cpg = cpgCreator.createCpg(vulnerableMethod);
    
            // Print the DOT representation of the CPG
            System.out.println(cpg.toDotGraph());
        }
    }
    ```

### Step 3: Analyzing the CPG

With the CPG created, you can now analyze it for vulnerabilities. For example, you can check for potential injection
vulnerabilities by analyzing data flow dependencies.

=== "SootUp"

    ```java
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

In this example, we check for data flow dependencies between the `userInput` variable and any `println`
calls, which could indicate a potential injection vulnerability.

Similarly, we can define our own queries to detect specific patterns that identify common vulnerabilities.
