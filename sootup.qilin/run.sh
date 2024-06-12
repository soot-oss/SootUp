#!/bin/sh
mvn clean compile assembly:single
mv target/sootup.qilin-1.3.0-jar-with-dependencies.jar ../artifact/Qilin-0.9.4-SNAPSHOT.jar
