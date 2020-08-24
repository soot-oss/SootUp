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

for root, dirnames, filenames in os.walk('..\\de.upb.swt.soot.java.sourcecode\\src\\test\\java\\de\\upb\\swt\\soot\\test\\java\\sourcecode\\minimaltestsuite\\'):
    for filename in fnmatch.filter(filenames, '*Test.java'):
        srcTestMatches.append(os.path.join(root, filename))		

for root, dirnames, filenames in os.walk('..\\de.upb.swt.soot.java.bytecode\\src\\test\\java\\de\\upb\\swt\\soot\\test\\java\\bytecode\\minimaltestsuite\\'):
    for filename in fnmatch.filter(filenames, '*Test.java'):
        srcTestMatches.append(os.path.join(root, filename))


for srcFile in srcMatches:
	ocb=0
	ccb=0
	flagRegex=0
	str ="/**  <pre>\n"
	listData=[]
	data=""
	
	#Use this regex to form tupples of method name, method body and put it into a map
	regexMethod =re.compile( "(\w*)[(]((([a-zA-Z])*(\s+)([a-zA-Z]))*)(([,])(\s+)([a-zA-Z])*(\s+)([a-zA-Z])*)*[)]" )
	
	#f= open("..\\..\\sandbox\\DeclareLong.java", 'r', encoding="utf8")
	f= open(srcFile, 'r', encoding="utf8")
	print(srcFile)
	
	'''
	1.Use this regex for finding the start of method in the line
	2.Update the python map expectedBody_*() as key to complete method body as value. 
	3.Use this map to update the srcTest file.
	'''
	#[@Override]*(\s*)public List<String> expectedBodyStmts(\S*)[(][)] 
	
	'''
	1. Regex to find /** <pre> the code </pre> */ in the test file and replace with same/updated source code
	2. Map of 
	'''
	
	
	
	
	for line in f.readlines():
		if regexMethod.search(line):
			if '{' in line :
				ocb+=1
			if '}' in line:
				ccb+=1
            
		if ocb >= 1:	
			str+=line
			if '{' in line and ocb > 1:
				ocb+=1
			if '}' in line:
				ccb+=1
	
			if '}' in line and ocb == 1 :
				ocb=0
				exit
	
	str+="\n<pre>*/"
	var1=""
	srcFileName = (srcFile.rsplit('\\',1)[1]).split('.')[0]
		
			
	for filename in srcTestMatches:
		dummy=srcFileName+"Test"
		#TODO match exact dummy with filename
		if dummy in filename:
			
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