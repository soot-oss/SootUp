`refl.log` recorded, not hand-written: each `refl.*` main run under the TamiFlex Play-Out agent
from https://github.com/secure-software-engineering/tamiflex/pull/15 (xaerru/tamiflex@83d0b1b, JDK 21 port).

    ant -f PlayOutAgent/build.xml agent-jar            # JAVA_HOME = JDK 21
    javac -g --release 8 -d binary source/refl/*.java
    java -javaagent:poa-trunk.jar=out-X -cp binary refl.X refl.Impl   # per program
    cat out-*/refl.log | sort -u > refl.log

JDK-internal entries kept on purpose (parser robustness). Editing sources shifts line numbers -> re-record.
