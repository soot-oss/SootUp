module modbar {
	requires modcommon;
	requires modauto1;

	opens pkgbar; // opens package for reflective access from modmain
}
