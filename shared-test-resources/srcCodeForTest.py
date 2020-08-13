import re
import os
import fnmatch
from re import search
import pdb

srcMatches = []
srcTestMatches = []

for root, dirnames, filenames in os.walk('miniTestSuite'):
    for filename in fnmatch.filter(filenames, '*.java'):
        srcMatches.append(os.path.join(root, filename))

for root, dirnames, filenames in os.walk('..\\de.upb.swt.soot.java.sourcecode\\src\\test\\java\\de\\upb\\swt\\soot\\test\\java\\sourcecode\\minimaltestsuite\\'):
    for filename in fnmatch.filter(filenames, '*Test.java'):
        srcTestMatches.append(os.path.join(root, filename))



for filename1 in srcMatches:
	ocb=0,ccb=0,flagRegex=0
	str ="/**\n  <pre>"
	listData=[]
	data=""
	#srcData= open(filename1, 'r', encoding="utf8").read()
	f= open(filename1, 'r', encoding="utf8")
	#regexMethod =re.compile("([(]([a-zA-Z])*(\s*)([a-zA-Z])*[)](\s*)[{])")
	for line in f.readlines()
		if '{' in line :
			ocb++
		if '}' in line:
			ccb+
		regexMethod =re.search("([(]([a-zA-Z])*(\s*)([a-zA-Z])*[)](\s*)[{])")
		if regexMethod != None
			flagRegex=1
			data+=line

		#data+=regexMethod.search(line).group()
	
		if '}' in line and ocb==1:
			listData.append(data)
			
	
			
	str+=srcData
	str+="\n  <pre>*/\n"
	var1=""
	srcFileName = (filename1.rsplit('\\',1)[1]).split('.')[0]
		
			
	for filename in srcTestMatches:
		if srcFileName in filename:
			
			str2=""
			var1=""
			strSrcTest=""
			srcTestFile=filename
			str2= open(srcTestFile, 'r', encoding="utf8").read()
			for line in open(srcTestFile, 'r'):
				l_strip=line.strip()
				if	"<String> expectedBodyStmts()" in l_strip:
					break

			found= re.search(r'  public List<String> expectedBodyStmts((.+\s)*)}(\s*)((.+\s)*)}(\s*)',str2,re.DOTALL)
			
			if found != None:
				strSrcTest= var1.rsplit("\n",3)[0]+str
				strSrcTest+=found.group()
				with open(srcTestFile, 'w', encoding="utf8") as filew:
					filew.writelines(strSrcTest)
					#print("File write for file "+srcTestFile)
			#else :
				#print("No match for "+srcTestFile)