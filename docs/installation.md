# Installation
## Using the latest version on the develop branch
visit [SootUp on Jitpack.io](https://jitpack.io/#soot-oss/SootUp/develop-SNAPSHOT) for configuration options of your build tool.

## Using the release
SootUp is available in maven central, you can include it in your project as follows.

Below we only show how you can add the SootUp modules to your project. It is not necessary to add all the modules as dependency. 
Depending on your needs you can import only the modules you need.
Take a look at the [Modules](whatsnew.md#modular-architecture) to learn more about which modules you might need.

### Maven

 Add the following dependency in the ```pom.xml``` file of your project to include all SootUp modules into your project.
 
```
<repositories>
 <repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
 </repository>
</repositories>

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
  <artifactId>sootup.java.sourcecode</artifactId>
  <version>{{ git_latest_release }}</version>
 </dependency>
 <dependency>
  <groupId>org.soot-oss</groupId>
  <artifactId>sootup.java.bytecode</artifactId>
  <version>{{ git_latest_release }}</version>
 </dependency>
 <dependency>
  <groupId>org.soot-oss</groupId>
  <artifactId>sootup.jimple.parser</artifactId>
  <version>{{ git_latest_release }}</version>
 </dependency>
 <dependency>
  <groupId>org.soot-oss</groupId>
  <artifactId>sootup.callgraph</artifactId>
  <version>{{ git_latest_release }}</version>
 </dependency>
 <dependency>
  <groupId>org.soot-oss</groupId>
  <artifactId>sootup.analysis</artifactId>
  <version>{{ git_latest_release }}</version>
 </dependency>
</dependencies>
```
### Gradle

Add the following dependency in the ```build.gradle``` file of your project to include all SootUp modules into your project.

```
repositories {
    mavenCentral()
    google()
    maven {
        url "https://jitpack.io"
    }
}

compile "org.soot-oss:sootup.core:{{ git_latest_release }}"
compile "org.soot-oss:sootup.java.core{{ git_latest_release }}"
compile "org.soot-oss:sootup.java.sourcecode{{ git_latest_release }}"
compile "org.soot-oss:sootup.java.bytecode{{ git_latest_release }}"
compile "org.soot-oss:sootup.jimple.parser{{ git_latest_release }}"
compile "org.soot-oss:sootup.callgraph{{ git_latest_release }}"
compile "org.soot-oss:sootup.analysis{{ git_latest_release }}"
```

## Building from Source
Build from source if you'd like to get the most recent changes.
You can download the project as a zip file, or clone it using your favorite git client app or the command line:

```
git clone https://github.com/secure-software-engineering/SootUp.git
```

SootUp is a maven project. You can import it into your favorite IDE as a maven project. Run maven clean and install tasks using your IDE's maven plugin to set up the project.

Alternatively, you can execute the following command in the project directory:

```
mvn install
```

Or if you want to skip tests while building:

```
mvn -Dskiptests install
```

