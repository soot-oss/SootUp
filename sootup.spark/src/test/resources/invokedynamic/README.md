Sources in `source/indy`. Compiled with `-g`:

    javac --release 8  -d binary source/indy/{CapturingLambda,InstanceLambda,BoundMethodRef,ConstructorRef}.java
    <jdk11>/javac      -cp binary -d binary source/indy/{Payload,Worker,Concat,LambdaArgs,UnboundRef,CtorRefResult,NeverCalled}.java
    javac --release 17 -cp binary -d binary source/indy/Rec.java

`Concat` needs javac 9..18: javac 19+ calls `String.valueOf` itself before the concat invokedynamic.
