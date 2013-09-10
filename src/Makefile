idl :
	idl dropbox-like.idl
all :
	idl dropbox-like.idl
	javac -classpath ~/programs/jacorb_3_2/src/omg-03-01-02/ Dropboxlike/*.java -d build/src/
	javac -classpath ~/programs/jacorb_3_2/src/omg-03-01-02/ -classpath build/src/ -d build/src/ *.java

