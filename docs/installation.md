# Installation

## Building from Source

You can download the project as a zip file, or clone it using your favorite git client app or the command line:

```
git clone https://github.com/secure-software-engineering/soot-reloaded.git
```

FutureSoot is a maven project. You can import it into your favorite IDE as a maven project. Run maven clean and install tasks using your IDE's maven plugin to set up the project.

Alternatively, you can execute the following command in the project directory:

```
mvn install
```

Or if you want to skip unit tests while building:

```
mvn -Dskiptests install
```

## Add Soot as Library Dependency to Your Project

Since the project is not available as a maven artefact, you first need to built it from source. See [Building from Source](#building-from-source)

Below we only show how you can add the ```soot.core``` module to your project. Depending on your needs you can import other modules too. See [Modules](#modules) (TODO) to learn more about the modules.

1. Maven:

 Add the following dependency in the ```pom.xml``` file of your project.
```
    <dependency>
      <groupId>de.upb.swt</groupId>
      <artifactId>soot.core</artifactId>
      <version>4.0.0-SNAPSHOT</version>
    </dependency>
```

2. Gradle

Add the following dependency in the ```build.gradle``` file of your project.

```
compile "de.upb.swt:soot.core:4.0.0-SNAPSHOT"
```