import re
import os
import fnmatch
from re import search
import pdb

srcMatches = []
srcTestMatches = []

#Change this variable while updating the JavaDoc for new source code files
srcTargetDir='..\\..\\target' 
for root, dirnames, filenames in os.walk(srcTargetDir):
    for filename in fnmatch.filter(filenames, '*.java'):
        srcMatches.append(os.path.join(root, filename))
for root, dirnames, filenames in os.walk('..\\sootup.java.sourcecode\\src\\test\\java\\soot\\test\\java\\sourcecode\\minimaltestsuite\\'):
    for filename in fnmatch.filter(filenames, '*Test.java'):
        srcTestMatches.append(os.path.join(root, filename))		
for root, dirnames, filenames in os.walk('..\\sootup.java.bytecode\\src\\test\\java\\soot\\test\\java\\bytecode\\minimaltestsuite\\'):
    for filename in fnmatch.filter(filenames, '*Test.java'):
        srcTestMatches.append(os.path.join(root, filename))
for srcFile in srcMatches:
	ocb=0
	ccb=0
	flagRegex=0
	str ="/**  <pre>\n"
	listData=[]
	data=""
	regexMethod =re.compile( "(\w*)[(]((([a-zA-Z])*(\s+)([a-zA-Z]))*)(([,])(\s+)([a-zA-Z])*(\s+)([a-zA-Z])*)*[)]" )
	f= open(srcFile, 'r', encoding="utf8")
	print(srcFile)
	foundFlag=0

	for line in f.readlines():
		found=regexMethod.search(line)
		if found:
			foundFlag=1
			if '{' in line :
				ocb+=1
			if '}' in line:
				ccb+=1
			str+=line
			if found:
				continue	
			
		if ocb >= 1 or foundFlag==1:	
			if '{' in line :
				ocb+=1
			if '}' in line:
				ccb+=1
			if ocb>=ccb:
				str+=line	
			if '}' in line and ocb == 1 :
				ocb=0
				exit
	
	str+="\n<pre>*/"
	var1=""
	srcFileName = (srcFile.rsplit('\\',1)[1]).split('.')[0]
	
	for filename in srcTestMatches:
		dummy=srcFileName+"Test"
		filenameDummy = (filename.rsplit('\\',1)[1]).split('.')[0]
		if dummy == filenameDummy:
			str2=""
			var1=""
			strSrcTest1=""
			strSrcTest2=""
			srcTestFile=filename
			str2= open(srcTestFile, 'r', encoding="utf8").read()
			for line in open(srcTestFile, 'r'):
				l_strip=line.strip()
				if	"<String> expectedBodyStmts()" in l_strip:
					break
				else:
					strSrcTest1+=line
					#print("Line added for "+srcTestFile+line)
				
			found= re.search(r'  @Override(\s*)public List<String> expectedBodyStmts((.+\s)*)}(\s*)((.+\s)*)}(\s*)',str2,re.DOTALL)
			
			if found != None:
				strSrcTest2= var1.rsplit("\n",3)[0]+str
				strSrcTest2+=found.group()
				New2= ""
				strSrcTestNew2 = "".join(strSrcTest1.rsplit('@Override', 1))
				#pdb.set_trace()
				strSrcTestNew2+=strSrcTest2
				#pdb.set_trace()
				with open(srcTestFile, 'w', encoding="utf8") as filew:
					filew.writelines(strSrcTestNew2)
					print("File write for file "+srcTestFile)
			else :
				print("No match for "+srcTestFile)
				dummy=""
