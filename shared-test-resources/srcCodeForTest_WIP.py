import re
import os
import fnmatch
from re import search
import pdb

srcMatches = []
srcTestMatches = []

#Change this variable while updating the JavaDoc for new source code files
srcTargetDir='..\\..\\target'
#srcTargetDir='miniTestSuite'  

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
	listData=[]
	data=""
	
	#Use this regex to form tupples of method name, method body and put it into a map
	regexMethod =re.compile( "(\w*)[(]((([a-zA-Z])*(\s+)([a-zA-Z]))*)(([,])(\s+)([a-zA-Z])*(\s+)([a-zA-Z])*)*[)]" )
	
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
	
	methodMap={} 
	foundFlag=0
	
	#Making the map of methodname to methodbody
	for line in f.readlines():
		found=regexMethod.search(line)
		if found:
			foundFlag=1
			methodName=""
			methodBody="/**  <pre>\n"
			methodName = found.group().split('(',1)[0]
			methodName = " ".join([word[0].upper() + word[1:] for word in methodName.split()])
			#methodName=found.group().split('(',0)[0].capitalize()
			methodBody+=line
			methodMap[methodName]= ""
			if '{' in line :
				ocb+=1
			if '}' in line:
				ccb+=1
			if found:
				continue	
		if ocb >= 1 or foundFlag ==1:	
			if ocb>ccb:
				methodBody+=line
			if '{' in line :
				ocb+=1
			if '}' in line:
				ccb+=1
			methodMap[methodName]=methodBody+"\n<pre>*/"
			if '}' in line and ocb == 1 :
				ocb=0
				exit
		
	var1=""
	srcFileName = (srcFile.rsplit('\\',1)[1]).split('.')[0]
			
	#Taping the methodmap[methodname] to the correspondong expectedBodtStmts method
	for filename in srcTestMatches:
		dummy=srcFileName+"Test"
		if dummy in filename:
			regexExp= re.compile("[@Override]*(\s*)public List<String> expectedBodyStmts(\S*)[(][)]")
			str2=""
			var1=""
			strSrcTest1=""
			strSrcTest2=""
			srcTestFile=filename
			#str2= open(srcTestFile, 'r', encoding="utf8").read()
			srcFinalMap={}

			for line in open(srcTestFile, 'r'):
				targetKey= "expectedBodyStmts"
				targetValue=""
					
				for key in methodMap:
					if key in line:
						targetValue=methodMap[key]
						targetKey+=key
					if key not in line or "List<String> expectedBodyStmts()" in line:
						targetValue=methodMap[key]	
				srcFinalMap[targetKey]=targetValue
			
			
			ocb=0
			ccb=0
			expBodyStmtflag=0	
			for line in open(srcTestFile, 'r'):
				found= regexExp.search(line)
				for key in srcFinalMap:
					if found:
						expBodyStmtflag=1
						targetValue+=line
					if expBodyStmtflag==1:	
						if ocb>ccb:
							targetValue+=line
						if '{' in line :
							ocb+=1
						if '}' in line:
							ccb+=1
						if ccb==1:
							expBodyStmtflag=0
							targetValue+=line
							srcFinalMap[key]=targetValue

					
						
					if not found and expBodyStmtflag==0:
						strSrcTest1+=line
						
			print(strSrcTest1)
					
			for key in srcFinalMap:
						print("Key :"+key+" Value :"+srcFinalMap[key]+"\n\n")



			
			
			
		
			#Write the updated contents into srcTestFile 

			#found= re.search(r'  @Override(\s*)public List<String> expectedBodyStmts((.+\s)*)}(\s*)((.+\s)*)}(\s*)',str2,re.DOTALL)
			
							