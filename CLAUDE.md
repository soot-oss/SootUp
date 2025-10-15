# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SootUp is a complete overhaul of the Soot static analysis framework for Java bytecode analysis. It transforms JVM bytecode to the Jimple intermediate representation and provides class hierarchy generation, callgraph generation, and data-flow analysis capabilities.

## Build System and Common Commands

This is a multi-module Maven project using Java 17 (as specified in maven.compiler.release). The project requires Maven 3.x and Java 17+ to build.

### Essential Commands
- `mvn clean compile` - Compile all modules
- `mvn clean test` - Run all tests across modules
- `mvn clean verify` - Compile, test, and verify packaging
- `mvn clean package` - Build JAR files for all modules
- `mvn clean install` - Install to local Maven repository
- `mvn clean dependency:tree` - View dependency hierarchy

### Testing Commands
- `mvn test -Dtest=ClassName` - Run specific test class
- `mvn test -Dtest=ClassName#methodName` - Run specific test method
- `mvn test -pl sootup.core` - Run tests for specific module
- `mvn test -am` - Run tests including dependent modules
- `mvn test -pl sootup.core -am` - Run tests for module with dependencies

### Code Quality Commands
- `mvn fmt:format` - Format code using Google Java Style
- `mvn license:check-file-header` - Check license headers
- `mvn jacoco:report` - Generate code coverage reports

## Module Architecture

SootUp follows a modular architecture with clear separation of concerns:

### Core Modules
- **sootup.core** - Core interfaces, Jimple IR, basic graph structures, and view system
- **sootup.java.core** - Java-specific language extensions and core types
- **sootup.jimple.frontend** - Jimple parsing and serialization

### Frontend Modules
- **sootup.java.bytecode.frontend** - Java bytecode parsing and analysis
- **sootup.java.sourcecode.frontend** - Java source code parsing
- **sootup.apk.frontend** - Android APK analysis support
- **sootup.apk.parser** - Android APK parsing utilities

### Analysis Modules
- **sootup.callgraph** - Call graph construction algorithms
- **sootup.spark** - Spark points-to analysis implementation
- **sootup.qilin** - Qilin points-to analysis framework
- **sootup.analysis.interprocedural** - Interprocedural analysis frameworks (IDE/IFDS)
- **sootup.analysis.intraprocedural** - Intraprocedural analysis implementations
- **sootup.codepropertygraph** - Code property graph generation

### Supporting Modules
- **sootup.interceptors** - Body transformation interceptors
- **sootup.examples** - Usage examples and tutorials
- **sootup.tests** - Integration tests and test utilities
- **sootup.report** - Reporting and output generation

## Key Architectural Concepts

### View System
SootUp uses a view-based architecture instead of global singletons:
- `AbstractView` - Base class for all analysis views
- Views provide isolated contexts for analysis
- Multiple views can coexist for different analysis scenarios
- Type hierarchies are view-specific

### Jimple IR
- Three-address intermediate representation
- Immutable IR objects and graphs
- Located in `sootup.core.jimple` package
- Key classes: `Stmt`, `Value`, `Local`, `JimpleBody`

### Frontend System
- Pluggable frontend architecture for different input formats
- Frontends transform input to Jimple IR
- Located in respective `*frontend` modules
- Key interfaces: `InputLocation`, `AnalysisInputLocation`

### Call Graph Construction
- Multiple algorithms available (CHA, VTA, Spark, etc.)
- Modular implementation in `sootup.callgraph`
- Extensible through `CallGraphAlgorithm` interface

## Development Guidelines

### Code Style
- Follow Google Java Style Guide (enforced by fmt-maven-plugin)
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Keep methods focused and under 30 lines when possible
- License headers are automatically checked (LGPL v2.1)

### Testing
- Write unit tests for all new functionality using JUnit 6 (version 6.0.0)
- Test files located in `src/test/java` directories
- Use Mockito for mocking dependencies
- Aim for high test coverage (measured by JaCoCo)
- Tests run in parallel by default (configured in maven-surefire-plugin)

### Module Dependencies
- Core modules should not depend on analysis modules
- Frontend modules can depend on core but not on each other
- Use dependency management in root pom.xml for version consistency
- Follow dependency direction: core ← frontend ← analysis

## Working with the Codebase

### Adding New Analysis
1. Create analysis class in appropriate `sootup.analysis.*` module
2. Implement necessary interfaces from core modules
3. Add tests in corresponding test directory
4. Update documentation if public API changes

### Adding New Frontend
1. Create new frontend module following naming convention
2. Implement `AnalysisInputLocation` interface
3. Add transformation logic to convert input to Jimple
4. Add frontend to main pom.xml modules list

### Common File Locations
- Main source: `sootup.*/src/main/java/`
- Test source: `sootup.*/src/test/java/`
- Test resources: `shared-test-resources/`
- Documentation: `docs/`
- Examples: `sootup.examples/src/main/java/`

### Important Interfaces
- `View` - Analysis view context
- `AnalysisInputLocation` - Input specification for frontends
- `BodyInterceptor` - Body transformation hook
- `CallGraphAlgorithm` - Call graph construction interface
- `TypeHierarchy` - Type relationship management

## Performance Considerations

- SootUp supports parallel processing (configured in maven-surefire-plugin)
- Use lazy loading for classes and views when possible
- Immutable data structures enable better concurrency
- Consider memory usage when processing large codebases