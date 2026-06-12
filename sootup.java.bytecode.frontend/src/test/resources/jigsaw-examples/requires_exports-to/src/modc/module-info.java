module modc {
    // Does meanwhile work in Eclipse even though modb1 and modb2 are separate Eclipse projects:
    //    Note that no longer a dependency from modc to modb1/2 is necessary just to get this "export to" compiled
	//    so we do not run into problems with forbidden cyclic dependencies any more.
    exports pkgc to modb1, modb2;
}
