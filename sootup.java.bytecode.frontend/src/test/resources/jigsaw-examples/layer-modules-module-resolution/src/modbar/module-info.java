module modbar {
	requires modcommon;
	requires modversion2;

	opens pkgbar; // opens package for reflective access from modmain
}
