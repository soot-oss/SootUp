# Installation

## Use the latest [develop branch](https://github.com/soot-oss/SootUp/tree/develop)
For configuration options of your build tool please visit [SootUp on Jitpack.io](https://jitpack.io/#soot-oss/SootUp/develop-SNAPSHOT)


## Use Releases on Maven Central
The code below shows you how to import all submodules of the SootUp repository.
You can import fewer modules if your use case allows it.

Add the following dependencies to your ```pom.xml``` / ```build.gradle```.

=== "Maven"

    ```mvn
    <dependencies>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.core</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.java.core</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.java.bytecode.frontend</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.jimple.frontend</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.apk.frontend</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.callgraph</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.analysis.intraprocedural</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.analysis.interprocedural</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.qilin</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.codepropertygraph</artifactId>
            <version>{{ git_latest_release }}</version>
        </dependency>
    </dependencies>
    ```

=== "Gradle"

    ```groovy
    repositories {
        mavenCentral()
        google()
    }
    
    compile "org.soot-oss:sootup.core:{{ git_latest_release }}"
    compile "org.soot-oss:sootup.java.core:{{ git_latest_release }}"
    compile "org.soot-oss:sootup.java.bytecode.frontend:{{ git_latest_release }}"
    compile "org.soot-oss:sootup.jimple.frontend:{{ git_latest_release }}"
    compile "org.soot-oss:sootup.apk.frontend:{{ git_latest_release }}"
    compile "org.soot-oss:sootup.callgraph:{{ git_latest_release }}"
    compile "org.soot-oss:sootup.analysis.intraprocedural:{{ git_latest_release }}"
    compile "org.soot-oss:sootup.analysis.interprocedural:{{ git_latest_release }}"
    compile "org.soot-oss:sootup.qilin:{{ git_latest_release }}"
    compile "org.soot-oss:sootup.codepropertygraph:{{ git_latest_release }}"
    ```

## Build from Source
If you'd like to get the most recent changes, you can build SootUp from source yourself and install it into your local maven repository.
```sh
git clone https://github.com/secure-software-engineering/SootUp.git
mvn install
```