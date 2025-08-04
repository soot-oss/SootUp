# Tests with Jimple files
Most tests in this folder will work with pre-generated .jimple files.
To generate a .jimple file from a SootClass, use 

```java


SootClass clazz = ...;
Utils.outputJimple(clazz, true);
```

This will create a jimpleOutput folder in the root directory of wherever the code was executed, which contains a .jimple representation of the class `clazz`.
