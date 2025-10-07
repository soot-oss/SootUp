module modbar {
	requires modcommon;
	requires modauto2;

	opens pkgbar; // opens package for reflective access from modmain
}
