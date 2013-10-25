all :
	idl dropbox-like.idl
	mkdir -p build/src
	javac -classpath ~/programs/jacorb_3_2/src/omg-03-01-02/ Dropboxlike/*.java -d build/src/
	javac -classpath ~/programs/jacorb_3_2/src/omg-03-01-02/ -classpath build/src/ -d build/src/ *.java

idl :
	idl dropbox-like.idl

java:
	mkdir -p build/src
	javac -classpath ~/programs/jacorb_3_2/src/omg-03-01-02/ Dropboxlike/*.java -d build/src/
	javac -classpath ~/programs/jacorb_3_2/src/omg-03-01-02/ -classpath build/src/ -d build/src/ *.java
