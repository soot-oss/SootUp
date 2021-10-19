# To build the jar:

`javac --release 8 -d classes src\main\java\de\upb\swt\multirelease\*java`  
`javac --release 9 -d classes-9 src\main\java9\de\upb\swt\multirelease\*java`

`jar --create --file mrjar.jar --main-class de.upb.swt.multirelease.Main -C classes . --release 9 -C classes-9 .`


More info:
https://www.baeldung.com/java-multi-release-jar