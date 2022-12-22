# Installation

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

Or if you want to skip unit tests while building:

```
mvn -Dskiptests install
```

## Using the release
Alternatively you can directly get the release versions.
SootUp is available in maven central, you can include it in your project as follows.

Below we only show how you can add the ```sootup.core``` module to your project. Depending on your needs you can import other modules too. See [Modules](../#modular-architecture) to learn more about the modules.

1. Maven:

 Add the following dependency in the ```pom.xml``` file of your project.
 
```
<dependency>
  <groupId>org.soot-oss</groupId>
  <artifactId>sootup.core</artifactId>
  <version>1.0.0</version>
</dependency>
```

2. Gradle

Add the following dependency in the ```build.gradle``` file of your project.

```
compile "org.soot-oss:sootup.core:1.0.0"
```

