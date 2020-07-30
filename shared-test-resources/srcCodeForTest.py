import re
import os
import fnmatch
from re import search

srcMatches = []
srcTestMatches = []

for root, dirnames, filenames in os.walk('miniTestSuite'):
    for filename in fnmatch.filter(filenames, '*.java'):
        srcMatches.append(os.path.join(root, filename))

for root, dirnames, filenames in os.walk('..\\de.upb.swt.soot.java.sourcecode\\src\\test\\java\\de\\upb\\swt\\soot\\test\\java\\sourcecode\\minimaltestsuite\\'):
    for filename in fnmatch.filter(filenames, '*Test.java'):
        srcTestMatches.append(os.path.join(root, filename))


for filename1 in srcMatches:
	str ="/**\n  <pre>\n  <code>\n"
	srcData= open(filename1, 'r', encoding="utf8").read()
	str+=srcData
	str+="\n  <pre>\n  <code>*/\n"
	var1=""
	srcFileName = (filename1.rsplit('\\',1)[1]).split('.')[0]
		
			
	for filename in srcTestMatches:
		if srcFileName in filename:
			str2=""
			var1=""
			strSrcTest=""
			srcTestFile=filename
			str2= open(srcTestFile, 'r', encoding="utf8").read()
			found= re.search(r'  @Override(\s*)public List<String> expectedBodyStmts((.+\s)*)}(\s*)((.+\s)*)}(\s*)',str2,re.DOTALL);
			
			for line in open(srcTestFile, 'r'):
				if re.search(r'((.+\W))@author((.+\W)*)', line) : 
					var1+="  \n"
				else: 
					var1+=line
					l_strip=line.strip()
				if	"<String> expectedBodyStmts()" in l_strip:
					break

			# Code for removing duplicate source code entries
			'''srcCommentcode= re.search(r'(.+\s*)<pre>(\s*)<code>((.+\s*)*)(\s)((.+\s*)*)<pre>(\s*)<code>(.+\s*)',str2,re.DOTALL);'''
			
			if found != None:
				strSrcTest= var1.rsplit("\n",3)[0]+str
				strSrcTest+=found.group()
				with open(srcTestFile, 'w', encoding="utf8") as filew:
					filew.writelines(strSrcTest)
					print("File write for file "+srcTestFile)
			else :
				print("No match for "+srcTestFile)