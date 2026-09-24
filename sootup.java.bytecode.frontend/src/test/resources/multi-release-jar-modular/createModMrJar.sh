# /bin/bash

# To build the jar:

javac --release 8 -d classes src/main/java/de/upb/sse/multirelease/*java  
javac --release 9 -d classes-9 src/main/java9/de/upb/sse/multirelease/*java src/main/java9/module-info.java
javac --release 10 -d classes-10 src/main/java10/de/upb/sse/multirelease/*java src/main/java10/module-info.java

jar --create --file mrjar.jar --main-class multirelease.Main -C classes . --release 9 -C classes-9 . --release 10 -C classes-10 .


# cleanup
rm -r ./classes ./classes-9 ./classes-10


#More info:
#https://www.baeldung.com/java-multi-release-jar

