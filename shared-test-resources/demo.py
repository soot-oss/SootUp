import re
import os
import fnmatch

srcMatches = []
dirMatches = []
srcTestMatches = []
for root, dirnames, filenames in os.walk('miniTestSuite'):
    for filename in fnmatch.filter(filenames, '*.java'):
        srcMatches.append(os.path.join(root, filename))
#print(srcMatches)

#\\*\\src\\test\\java\\de\\upb\\swt\\soot\\test

for root, dirnames, filenames in os.walk('..'):
    for filename in fnmatch.filter(dirnames, 'test'):
		for srcTest in os.walk(os.path.join(root, filename))
			
        dirMatches.append(os.path.join(root, filename))
print(dirMatches)


for root, dirnames, filenames in os.walk('..'):
    for filename in fnmatch.filter(filenames, '*.java'):
		if filename1
        srcTestMatches.append(os.path.join(root, filename))
print(srcTestMatches)

'''for filename1 in srcMatches:
	#filename1 = "DeclareInt.java"
	str ="/**\n  <pre>\n  <code>\n"
	str+=open(filename1, 'r').read()
	str+="\n  <pre>\n  <code>*/\n"
	
	for root, dirnames, filenames in os.walk('..'):
		for filename in fnmatch.filter(filenames, '*.java'):
			if filename1 in filename:
				filename2=filename2

				#filename2 = "DeclareIntTest.java"
				var1=""; var2=""
				word_list=[]
				for line in open(filename2, 'r'):
					#print(line)
					var1+=line
					l_strip=line.strip()
					if	"<String> expectedBodyStmts()" in l_strip:
						break
					#print(var1)

				str2= open(filename2, 'r').read()
				str= var1.rsplit("\n",3)[0]+str
				#print(word_list)

				found= re.search('@Override(.*)expectedBodyStmts()',str2,re.DOTALL).group()
				print(found)
				str+=found

				with open(filename2, "w") as filew:
				filew.writelines(str)'''
